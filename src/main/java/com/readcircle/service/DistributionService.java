package com.readcircle.service;

import com.readcircle.model.*;
import com.readcircle.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DistributionService {

    private final DistributionSessionRepository sessionRepository;
    private final AssignmentRepository assignmentRepository;
    private final ResourceRepository resourceRepository;

    public DistributionService(DistributionSessionRepository sessionRepository,
                               AssignmentRepository assignmentRepository,
                               ResourceRepository resourceRepository) {
        this.sessionRepository = sessionRepository;
        this.assignmentRepository = assignmentRepository;
        this.resourceRepository = resourceRepository;
    }

    @Transactional
    public void createAssignments(DistributionSession session, Resource resource, int participantCount) {
        // Bu metod tekil kaynak eklemelerinde (Add Resource) kullanılır.
        // Varsayılan olarak 1 adet üzerinden işlem yapar.
        List<Assignment> assignments = new ArrayList<>();
        int totalUnits = resource.getTotalUnits();

        if (resource.getType().toString().equals("JOINT")) {
            for (int i = 0; i < participantCount; i++) {
                Assignment assignment = createAssignmentObj(session, resource, i + 1, 1, totalUnits);
                assignment.setCurrentCount(totalUnits);
                assignments.add(assignment);
            }
        }
        else {
            int baseAmount = totalUnits / participantCount;
            int remainder = totalUnits % participantCount;
            int currentStart = 1;

            for (int i = 0; i < participantCount; i++) {
                int myAmount = baseAmount + (i < remainder ? 1 : 0);

                if (myAmount > 0) {
                    int currentEnd = currentStart + myAmount - 1;
                    Assignment assignment = createAssignmentObj(session, resource, i + 1, currentStart, currentEnd);

                    if (resource.getType().toString().equals("COUNTABLE")) {
                        assignment.setCurrentCount(myAmount);
                    }

                    assignments.add(assignment);
                    currentStart += myAmount;
                }
            }
        }
        assignmentRepository.saveAll(assignments);
    }

    @Transactional
    public void addResourceToSession(String code, Long resourceId, String username) {
        // 1. Oturumu ve yetkiyi kontrol et
        DistributionSession session = sessionRepository.findByCode(code);
        if (session == null) throw new RuntimeException("Oturum bulunamadı.");
        if (!session.getCreatorName().equals(username)) {
            throw new RuntimeException("Bu işlem için yetkiniz yok.");
        }

        // 2. Kaynağı bul
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Kaynak bulunamadı."));

        // 3. Zaten ekli mi kontrol et
        boolean alreadyExists = session.getAssignments().stream()
                .anyMatch(a -> a.getResource().getId().equals(resourceId));
        if (alreadyExists) {
            throw new RuntimeException("Bu kaynak zaten bu halkada mevcut.");
        }

        // 4. Dağıtımı yap
        createAssignments(session, resource, session.getParticipants());
    }

    @Transactional
    public DistributionSession createDistribution(
            List<Long> resourceIds,
            int participantCount,
            java.util.Map<Long, Integer> customCountsMap,
            String creatorName,
            String description
    ) {
        DistributionSession session = new DistributionSession();
        session.setCode(UUID.randomUUID().toString().substring(0, 8));
        session.setParticipants(participantCount);
        session.setCreatorName(creatorName);
        session.setDescription(description != null && !description.isEmpty() ? description : "Manevi Halka");
        session = sessionRepository.save(session);
        List<Assignment> assignments = new ArrayList<>();

        for (Long resId : resourceIds) {
            Resource resource = resourceRepository.findById(resId).orElse(null);
            if (resource != null) {

                int totalUnits;
                boolean isCountableType = resource.getType().toString().equals("COUNTABLE") ||
                        resource.getType().toString().equals("JOINT");

                // 1. TOPLAM HEDEFİ HESAPLA
                if (customCountsMap != null && customCountsMap.containsKey(resId)) {
                    int inputValue = customCountsMap.get(resId);

                    if (isCountableType) {
                        // Zikirler için girilen sayı direkt hedeftir (örn: 5000 Salavat)
                        totalUnits = inputValue;
                    } else {
                        // Kitaplar için girilen sayı ADET'tir (örn: 2 Hatim = 2 * 604 sayfa)
                        totalUnits = resource.getTotalUnits() * inputValue;
                    }
                } else {
                    totalUnits = resource.getTotalUnits();
                }

                // 2. DAĞITIM MANTIĞI
                if (resource.getType().toString().equals("JOINT")) {
                    for (int i = 0; i < participantCount; i++) {
                        Assignment assignment = createAssignmentObj(session, resource, i+1, 1, totalUnits);
                        assignment.setCurrentCount(totalUnits);
                        assignments.add(assignment);
                    }
                }
                else {
                    // Dağıtılabilir (Distributed) Kaynaklar
                    int baseAmount = totalUnits / participantCount;
                    int remainder = totalUnits % participantCount;
                    int currentCumulativeStart = 1; // Kümülatif başlangıç (1'den 1208'e kadar gider)
                    int singleResourceLimit = resource.getTotalUnits(); // Tek bir kitabın limiti (örn: 604)

                    for (int i = 0; i < participantCount; i++) {
                        int myAmount = baseAmount + (i < remainder ? 1 : 0);

                        if (myAmount > 0) {
                            // --- DÜZELTİLEN MANTIK: SAYFA BÖLME VE BAŞA SARMA ---
                            // Eğer kaynak COUNTABLE değilse (yani Kuran, Cevşen gibi sayfalıysa)
                            // ve kişiye düşen pay kitap sınırını aşıyorsa bölmemiz gerekir.
                            if (!resource.getType().toString().equals("COUNTABLE")) {
                                int remainingAmountToAssign = myAmount;
                                int tempStart = currentCumulativeStart;

                                while (remainingAmountToAssign > 0) {
                                    // Mevcut başlangıç noktasının kitap içindeki gerçek karşılığını bul
                                    // Örn: 605. sayfa aslında 1. sayfadır.
                                    // Matematik: (605 - 1) % 604 + 1 = 1
                                    int localStart = (tempStart - 1) % singleResourceLimit + 1;

                                    // Bu kitabın sonuna kadar kaç sayfa var?
                                    // Örn: Başlangıç 600, Limit 604 -> 5 sayfa var (600,601,602,603,604)
                                    int spaceInBook = singleResourceLimit - localStart + 1;

                                    // Bu turda ne kadar verebiliriz? (Ya ihtiyacı kadar ya da kitap bitene kadar)
                                    int chunk = Math.min(remainingAmountToAssign, spaceInBook);

                                    int localEnd = localStart + chunk - 1;

                                    // Parçayı oluştur
                                    Assignment assignment = createAssignmentObj(session, resource, i+1, localStart, localEnd);
                                    assignments.add(assignment);

                                    // İlerlet
                                    remainingAmountToAssign -= chunk;
                                    tempStart += chunk;
                                }
                            }
                            else {
                                // Zikirmatik (COUNTABLE) ise bölmeye gerek yok, kümülatif devam etsin
                                int currentEnd = currentCumulativeStart + myAmount - 1;
                                Assignment assignment = createAssignmentObj(session, resource, i+1, currentCumulativeStart, currentEnd);
                                assignment.setCurrentCount(myAmount);
                                assignments.add(assignment);
                            }

                            // Bir sonraki kişi için kümülatif sayacı ilerlet
                            currentCumulativeStart += myAmount;
                        }
                    }
                }
            }
        }

        assignmentRepository.saveAll(assignments);
        session.setAssignments(assignments);
        return session;
    }


    private Assignment createAssignmentObj(DistributionSession session, Resource resource, int pNum, int start, int end) {
        Assignment assignment = new Assignment();
        assignment.setSession(session);
        assignment.setResource(resource);
        assignment.setParticipantNumber(pNum);
        assignment.setStartUnit(start);
        assignment.setEndUnit(end);
        assignment.setTaken(false);
        return assignment;
    }

    @Transactional(readOnly = true)
    public DistributionSession getSessionByCode(String code) {
        DistributionSession session = sessionRepository.findByCodeWithAssignments(code);
        if (session == null) return sessionRepository.findByCode(code);
        if (session.getAssignments() != null) {
            session.getAssignments().forEach(a -> {
                if (a.getResource() != null && a.getResource().getTranslations() != null) {
                    a.getResource().getTranslations().size();
                }
            });
        }
        return session;
    }

    @Transactional
    public Assignment claimAssignment(Long assignmentId, String name) {
        Assignment mainAssignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Parça bulunamadı"));

        if (mainAssignment.isTaken()) {
            if (!mainAssignment.getAssignedToName().equals(name)) {
                throw new RuntimeException("Bu parça maalesef başkası tarafından alındı.");
            }
            return mainAssignment;
        }

        // --- GÜNCELLEME: AYNI KATILIMCI NUMARASINA AİT TÜM PARÇALARI BUL VE ATA ---
        // Örneğin: Kuran 2 Hatim ise ve kullanıcı 5. sıradaysa,
        // hem (485-604) hem de (1-1) aralıklarını tek seferde üzerine alır.
        List<Assignment> relatedAssignments = assignmentRepository.findBySession_Id(mainAssignment.getSession().getId())
                .stream()
                .filter(a -> a.getResource().getId().equals(mainAssignment.getResource().getId())
                        && a.getParticipantNumber() == mainAssignment.getParticipantNumber())
                .toList();

        for (Assignment a : relatedAssignments) {
            a.setTaken(true);
            a.setAssignedToName(name);
        }

        assignmentRepository.saveAll(relatedAssignments);
        return mainAssignment;
    }

    public void updateProgress(Long assignmentId, int newCount, String name) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Parça bulunamadı"));

        if (!assignment.getAssignedToName().equals(name)) {
            throw new RuntimeException("Bu parçayı güncelleme yetkiniz yok.");
        }

        assignment.setCurrentCount(newCount);
        assignmentRepository.save(assignment);
    }

    @Transactional
    public Assignment completeAssignment(Long assignmentId, String name) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Parça bulunamadı."));

        if (assignment.getAssignedToName() == null || !assignment.getAssignedToName().equals(name)) {
            throw new RuntimeException("Bu parçayı tamamlama yetkiniz yok.");
        }

        assignment.setCompleted(true);

        if (assignment.getResource().getType().toString().equals("COUNTABLE") ||
                assignment.getResource().getType().toString().equals("JOINT")) {

            assignment.setCurrentCount(0);
        }

        return assignmentRepository.save(assignment);
    }

    @Transactional
    public void deleteSession(String code, String username) {
        DistributionSession session = sessionRepository.findByCode(code);
        if (session == null) {
            throw new RuntimeException("Oturum bulunamadı.");
        }

        if (!session.getCreatorName().equals(username)) {
            throw new RuntimeException("Bu oturumu silmeye yetkiniz yok.");
        }

        assignmentRepository.deleteBySession_Id(session.getId());

        sessionRepository.delete(session);
    }

    @Transactional
    public void leaveSession(String code, String username) {
        List<Assignment> userAssignments = assignmentRepository.findBySession_CodeAndAssignedToName(code, username);

        for (Assignment assignment : userAssignments) {
            assignment.setTaken(false);
            assignment.setAssignedToName(null);
            assignment.setCurrentCount(0);
            assignment.setCompleted(false);
        }

        assignmentRepository.saveAll(userAssignments);
    }
    @Transactional
    public Assignment cancelAssignment(Long assignmentId, String name) {
        Assignment mainAssignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Parça bulunamadı"));

        if (mainAssignment.isTaken() && mainAssignment.getAssignedToName() != null) {
            if (mainAssignment.getAssignedToName().equals(name)) {

                // --- GÜNCELLEME: AYNI KİŞİYE AİT TÜM İLGİLİ PARÇALARI İPTAL ET ---
                List<Assignment> relatedAssignments = assignmentRepository.findBySession_Id(mainAssignment.getSession().getId())
                        .stream()
                        .filter(a -> a.getResource().getId().equals(mainAssignment.getResource().getId())
                                && a.getParticipantNumber() == mainAssignment.getParticipantNumber())
                        .toList();

                for (Assignment a : relatedAssignments) {
                    a.setTaken(false);
                    a.setAssignedToName(null);
                    a.setCompleted(false);

                    if (a.getResource().getType().toString().equals("COUNTABLE") ||
                            a.getResource().getType().toString().equals("JOINT")) {
                        int initialCount = a.getEndUnit() - a.getStartUnit() + 1;
                        a.setCurrentCount(initialCount);
                    }
                }

                assignmentRepository.saveAll(relatedAssignments);
                return mainAssignment;
            }
        }
        throw new RuntimeException("Bu işlemi yapmaya yetkiniz yok.");
    }

    @Transactional
    public void resetSession(String code, String username) {
        DistributionSession session = sessionRepository.findByCode(code);
        if (session == null) {
            throw new RuntimeException("Oturum bulunamadı.");
        }

        if (!session.getCreatorName().equals(username)) {
            throw new RuntimeException("Bu oturumu sıfırlamaya yetkiniz yok.");
        }

        List<Assignment> assignments = assignmentRepository.findBySession_Id(session.getId());

        for (Assignment assignment : assignments) {
            assignment.setTaken(false);
            assignment.setAssignedToName(null);
            assignment.setCompleted(false);

            int initialCount = assignment.getEndUnit() - assignment.getStartUnit() + 1;
            assignment.setCurrentCount(initialCount);
        }

        assignmentRepository.saveAll(assignments);
    }


    public void initDatabase() {
        assignmentRepository.deleteAll();
        sessionRepository.deleteAll();
    }
}