package com.readcircle.service;

import com.readcircle.model.*;
import com.readcircle.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class DistributionService {

    private final DistributionSessionRepository sessionRepository;
    private final AssignmentRepository assignmentRepository;
    private final ResourceRepository resourceRepository;

    // --- GRUP A: Adet (Kopya) Bazlı Çoğaltılacak Kaynaklar ---
    private static final List<String> MULTIPLIER_CODES = Arrays.asList(
            "KURAN", "QURAN", "KURAN-I KERIM",
            "CEVSEN",
            "BEDIR",
            "UHUD",
            "TEVHIDNAME"
    );

    public DistributionService(DistributionSessionRepository sessionRepository,
                               AssignmentRepository assignmentRepository,
                               ResourceRepository resourceRepository) {
        this.sessionRepository = sessionRepository;
        this.assignmentRepository = assignmentRepository;
        this.resourceRepository = resourceRepository;
    }

    @Transactional
    public void createAssignments(DistributionSession session, Resource resource, int participantCount, Integer manualTotalUnits) {
        List<Assignment> assignments = new ArrayList<>();

        // Eğer dışarıdan bir sayı gelmişse onu kullan, gelmemişse veritabanındaki default değeri kullan
        int effectiveTotalUnits = (manualTotalUnits != null && manualTotalUnits > 0)
                ? manualTotalUnits
                : resource.getTotalUnits();

        String codeKey = resource.getCodeKey() != null ? resource.getCodeKey().toUpperCase() : "";
        boolean isQuran = codeKey.contains("KURAN") || codeKey.contains("QURAN");

        // Kuran ise ve dışarıdan özel bir sayı girilmemişse 600 birim (sayfa) kullan
        int calculationUnits = effectiveTotalUnits;
        if (isQuran && (manualTotalUnits == null || manualTotalUnits == 0)) {
            calculationUnits = 600;
        }

        if (resource.getType() == ResourceType.JOINT) {
            // Ortak (Joint) kaynaklarda her katılımcıya girilen toplam hedef atanır
            for (int i = 0; i < participantCount; i++) {
                Assignment assignment = createAssignmentObj(session, resource, i + 1, 1, effectiveTotalUnits);
                assignment.setCurrentCount(effectiveTotalUnits);
                assignments.add(assignment);
            }
        } else {
            // Dağıtımlı kaynaklarda hesaplanan birimler üzerinden paylaştır
            distributeUnits(session, resource, participantCount, calculationUnits, assignments, true, isQuran);
        }
        assignmentRepository.saveAll(assignments);
    }
    @Transactional
    public void addResourceToSession(String code, Long resourceId, String username, Integer totalUnits) {
        DistributionSession session = sessionRepository.findByCode(code);
        if (session == null) throw new RuntimeException("Oturum bulunamadı.");
        if (!session.getCreatorName().equals(username)) {
            throw new RuntimeException("Bu işlem için yetkiniz yok.");
        }

        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Kaynak bulunamadı."));

        boolean alreadyExists = session.getAssignments().stream()
                .anyMatch(a -> a.getResource().getId().equals(resourceId));
        if (alreadyExists) {
            throw new RuntimeException("Bu kaynak zaten bu halkada mevcut.");
        }

        // BURAYI GÜNCELLEYİN: totalUnits değerini createAssignments'a iletin
        createAssignments(session, resource, session.getParticipants(), totalUnits);
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
                int calculationUnits; // Dağıtım matematiğinde kullanılacak birim sayısı

                String codeKey = resource.getCodeKey() != null ? resource.getCodeKey().toUpperCase() : "";
                boolean isMultiplierGroup = MULTIPLIER_CODES.stream().anyMatch(codeKey::contains);
                boolean isQuran = codeKey.contains("KURAN") || codeKey.contains("QURAN");

                if (customCountsMap != null && customCountsMap.containsKey(resId)) {
                    int inputValue = customCountsMap.get(resId);

                    if (isMultiplierGroup) {
                        // GRUP A (Kuran, Cevşen vb.): Girdi = ADET
                        totalUnits = resource.getTotalUnits() * inputValue;

                        if (isQuran) {
                            // Kuran için: Adet * 600 (Sanal Sayfa)
                            calculationUnits = 600 * inputValue;
                        } else {
                            calculationUnits = totalUnits;
                        }
                    } else {
                        // GRUP B (Fetih, Yasin vb.): Girdi = HEDEF
                        totalUnits = inputValue;
                        calculationUnits = inputValue;
                    }
                } else {
                    totalUnits = resource.getTotalUnits();
                    calculationUnits = isQuran ? 600 : totalUnits;
                }

                if (resource.getType() == ResourceType.JOINT &&
                        (customCountsMap == null || !customCountsMap.containsKey(resId))) {
                    for (int i = 0; i < participantCount; i++) {
                        Assignment assignment = createAssignmentObj(session, resource, i + 1, 1, totalUnits);
                        assignment.setCurrentCount(totalUnits);
                        assignments.add(assignment);
                    }
                } else {
                    distributeUnits(session, resource, participantCount, calculationUnits, assignments, isMultiplierGroup, isQuran);
                }
            }
        }

        assignmentRepository.saveAll(assignments);
        session.setAssignments(assignments);
        return session;
    }

    private void distributeUnits(DistributionSession session, Resource resource, int participantCount, int calculationUnits, List<Assignment> assignments, boolean useWrapping, boolean isQuran) {
        int baseAmount = calculationUnits / participantCount;
        int remainder = calculationUnits % participantCount;

        int currentCumulativeStart = 1;

        // Kuran için sanal limit 600, diğerleri için kendi limiti
        int singleResourceLimit = isQuran ? 600 : resource.getTotalUnits();

        if (!useWrapping) {
            singleResourceLimit = Integer.MAX_VALUE;
        }

        for (int i = 0; i < participantCount; i++) {
            int myAmount = baseAmount + (i < remainder ? 1 : 0);

            if (myAmount > 0) {
                if (useWrapping) {
                    // --- SARMAL (WRAPPING) DAĞITIM ---
                    int remainingAmountToAssign = myAmount;
                    int tempTotalCounter = currentCumulativeStart;

                    while (remainingAmountToAssign > 0) {
                        int localStart = (tempTotalCounter - 1) % singleResourceLimit + 1;
                        int spaceInBook = singleResourceLimit - localStart + 1;
                        int chunk = Math.min(remainingAmountToAssign, spaceInBook);
                        int localEnd = localStart + chunk - 1;

                        // KURAN İÇİN SAYFA DÖNÜŞÜMÜ (Mapping)
                        // Sanal (1-600) -> Gerçek (1-604)
                        int mappedStart = isQuran ? mapQuranPage(localStart) : localStart;
                        int mappedEnd = isQuran ? mapQuranPage(localEnd) : localEnd;

                        Assignment assignment = createAssignmentObj(session, resource, i + 1, mappedStart, mappedEnd);

                        if (resource.getType() == ResourceType.COUNTABLE) {
                            assignment.setCurrentCount(chunk);
                        }

                        assignments.add(assignment);

                        remainingAmountToAssign -= chunk;
                        tempTotalCounter += chunk;
                    }
                } else {
                    // --- DÜZ (LINEAR) DAĞITIM ---
                    int currentEnd = currentCumulativeStart + myAmount - 1;
                    Assignment assignment = createAssignmentObj(session, resource, i + 1, currentCumulativeStart, currentEnd);
                    assignment.setCurrentCount(myAmount);
                    assignments.add(assignment);
                }
                currentCumulativeStart += myAmount;
            }
        }
    }

    // --- Kuran Sayfa Dönüştürücü ---
    // 30. Cüz'ü (Sanal 581-600) -> Gerçek (581-604)'e genişletir.
    private int mapQuranPage(int virtualPage) {
        if (virtualPage <= 580) {
            // İlk 29 Cüz birebir eşleşir (1-580)
            return virtualPage;
        } else {
            // 30. Cüz: 20 sanal birimi 24 gerçek sayfaya yay
            // Formül: 581 + (Ofset * 23 / 19)
            int offset = virtualPage - 581;
            int scaledOffset = (int) Math.round(offset * (23.0 / 19.0));
            return 581 + scaledOffset;
        }
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
        if (!assignment.getAssignedToName().equals(name)) throw new RuntimeException("Yetkisiz işlem.");
        assignment.setCurrentCount(newCount);
        assignmentRepository.save(assignment);
    }

    @Transactional
    public Assignment completeAssignment(Long assignmentId, String name) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Parça bulunamadı."));
        if (assignment.getAssignedToName() == null || !assignment.getAssignedToName().equals(name)) {
            throw new RuntimeException("Yetkisiz işlem.");
        }

        List<Assignment> relatedAssignments = assignmentRepository.findBySession_Id(assignment.getSession().getId())
                .stream()
                .filter(a -> a.getResource().getId().equals(assignment.getResource().getId())
                        && a.getParticipantNumber() == assignment.getParticipantNumber())
                .toList();

        for (Assignment a : relatedAssignments) {
            a.setCompleted(true);
            a.setCurrentCount(0);
        }
        assignmentRepository.saveAll(relatedAssignments);
        return assignment;
    }

    @Transactional
    public void deleteSession(String code, String username) {
        DistributionSession session = sessionRepository.findByCode(code);
        if (session == null) throw new RuntimeException("Oturum bulunamadı.");
        if (!session.getCreatorName().equals(username)) throw new RuntimeException("Yetkisiz işlem.");
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
                List<Assignment> relatedAssignments = assignmentRepository.findBySession_Id(mainAssignment.getSession().getId())
                        .stream()
                        .filter(a -> a.getResource().getId().equals(mainAssignment.getResource().getId())
                                && a.getParticipantNumber() == mainAssignment.getParticipantNumber())
                        .toList();

                for (Assignment a : relatedAssignments) {
                    a.setTaken(false);
                    a.setAssignedToName(null);
                    a.setCompleted(false);
                    int initialCount = a.getEndUnit() - a.getStartUnit() + 1;
                    a.setCurrentCount(initialCount);
                }
                assignmentRepository.saveAll(relatedAssignments);
                return mainAssignment;
            }
        }
        throw new RuntimeException("Yetkisiz işlem.");
    }

    @Transactional
    public void resetSession(String code, String username) {
        DistributionSession session = sessionRepository.findByCode(code);
        if (session == null) throw new RuntimeException("Oturum bulunamadı.");
        if (!session.getCreatorName().equals(username)) throw new RuntimeException("Yetkisiz işlem.");

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