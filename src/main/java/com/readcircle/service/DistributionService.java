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

        // 3. Zaten ekli mi kontrol et (İsteğe bağlı, aynı kaynaktan 2 tane olsun istersen bu kontrolü kaldır)
        boolean alreadyExists = session.getAssignments().stream()
                .anyMatch(a -> a.getResource().getId().equals(resourceId));
        if (alreadyExists) {
            throw new RuntimeException("Bu kaynak zaten bu halkada mevcut.");
        }

        // 4. Dağıtımı yap (Mevcut kişi sayısına göre)
        createAssignments(session, resource, session.getParticipants());
    }

    @Transactional
    public DistributionSession createDistribution(
            List<Long> resourceIds,
            int participantCount,
            java.util.Map<Long, Integer> customCountsMap,
            String creatorName,
            String description // <--- YENİ PARAMETRE EKLENDİ
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
                 if ((resource.getType().toString().equals("COUNTABLE") || resource.getType().toString().equals("JOINT")) &&
                        customCountsMap != null &&
                        customCountsMap.containsKey(resId)) {
                    totalUnits = customCountsMap.get(resId);
                } else {
                    totalUnits = resource.getTotalUnits();
                }

                 if (resource.getType().toString().equals("JOINT")) {
                    for (int i = 0; i < participantCount; i++) {
                        Assignment assignment = createAssignmentObj(session, resource, i+1, 1, totalUnits);
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
                            Assignment assignment = createAssignmentObj(session, resource, i+1, currentStart, currentStart + myAmount - 1);
                            if (resource.getType().toString().equals("COUNTABLE")) {
                                assignment.setCurrentCount(myAmount);
                            }
                            assignments.add(assignment);
                            currentStart += myAmount;
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

    public Assignment claimAssignment(Long assignmentId, String name) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Parça bulunamadı"));

        if (assignment.isTaken()) {
            if (!assignment.getAssignedToName().equals(name)) {
                throw new RuntimeException("Bu parça maalesef başkası tarafından alındı.");
            }
            return assignment;
        }

        assignment.setTaken(true);
        assignment.setAssignedToName(name);

        return assignmentRepository.save(assignment);
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

        // Güvenlik: Sadece parçayı alan kişi tamamlayabilir
        if (assignment.getAssignedToName() == null || !assignment.getAssignedToName().equals(name)) {
            throw new RuntimeException("Bu parçayı tamamlama yetkiniz yok.");
        }

        // Durumu güncelle
        assignment.setCompleted(true);

        // --- GÜNCELLENEN KISIM BAŞLANGIÇ ---
        // Hem COUNTABLE (Şahsi) hem de JOINT (Ortak) türler için sayacı 0 yapıyoruz.
        // Böylece veritabanında da işlem bitmiş olarak görünüyor.
        if (assignment.getResource().getType() == ResourceType.COUNTABLE ||
                assignment.getResource().getType() == ResourceType.JOINT) {

            assignment.setCurrentCount(0);
        }
        // --- GÜNCELLENEN KISIM BİTİŞ ---

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

    public Assignment cancelAssignment(Long assignmentId, String name) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Parça bulunamadı"));

        if (assignment.isTaken() && assignment.getAssignedToName() != null) {
            if (assignment.getAssignedToName().equals(name)) {
                // 1. Sahipliği kaldır
                assignment.setTaken(false);
                assignment.setAssignedToName(null);

                // 2. --- EKLENEN KISIM: Tamamlandı işaretini kaldır ---
                assignment.setCompleted(false);

                // 3. --- EKLENEN KISIM: Sayacı başlangıç değerine (full) getir ---
                // Böylece tekrar alındığında sayaç 0'dan değil, en baştan başlar.
                if (assignment.getResource().getType() == ResourceType.COUNTABLE ||
                        assignment.getResource().getType() == ResourceType.JOINT) {
                    int initialCount = assignment.getEndUnit() - assignment.getStartUnit() + 1;
                    assignment.setCurrentCount(initialCount);
                }

                return assignmentRepository.save(assignment);
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

        // Güvenlik: Sadece oluşturan kişi sıfırlayabilir
        if (!session.getCreatorName().equals(username)) {
            throw new RuntimeException("Bu oturumu sıfırlamaya yetkiniz yok.");
        }

        List<Assignment> assignments = assignmentRepository.findBySession_Id(session.getId());

        for (Assignment assignment : assignments) {
            // 1. Sahipliği kaldır
            assignment.setTaken(false);
            assignment.setAssignedToName(null);

            // 2. Tamamlandı işaretini kaldır
            assignment.setCompleted(false);

            // 3. Sayacı başlangıç değerine (full) getir (Geri sayım mantığı olduğu için)
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