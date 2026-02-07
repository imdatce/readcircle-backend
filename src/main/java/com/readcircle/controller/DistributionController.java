package com.readcircle.controller;

import com.readcircle.dto.CreateDistributionRequest;
import com.readcircle.model.*;
import com.readcircle.repository.AssignmentRepository;
import com.readcircle.repository.DistributionSessionRepository;
import com.readcircle.repository.ResourceRepository;
import com.readcircle.service.DistributionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/distribution")
public class DistributionController {

    private final DistributionService service;
    private final ResourceRepository resourceRepository;
    private final DistributionSessionRepository distributionSessionRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    public DistributionController(DistributionService service, ResourceRepository resourceRepository, DistributionSessionRepository distributionSessionRepository) {
        this.service = service;
        this.resourceRepository = resourceRepository;
        this.distributionSessionRepository = distributionSessionRepository;
    }

    @Value("${app.security.db-reset-enabled:false}")
    private boolean isDbResetEnabled;

    @GetMapping("/resources")
    public List<Resource> getAllResources() {
        return resourceRepository.findAll();
    }

     @GetMapping("/create")
    public ResponseEntity<?> createDistributionGet(
            @RequestParam(name = "resourceIds") List<Long> resourceIds,
            @RequestParam int participants,
            @RequestParam String creatorName
    ) {
         DistributionSession session = new DistributionSession();
        String uniqueCode = UUID.randomUUID().toString().substring(0, 8);
        session.setCode(uniqueCode);
        session.setParticipants(participants);
        session.setCreatorName(creatorName);
        session = distributionSessionRepository.save(session);

         if (resourceIds != null) {
            for (Long resId : resourceIds) {
                Resource resource = resourceRepository.findById(resId).orElse(null);
                if (resource != null) {
                    service.createAssignments(session, resource, participants);
                }
            }
        }

        return ResponseEntity.ok(session);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createDistribution(@RequestBody Map<String, Object> payload) {
        try {
            String creatorName = (String) payload.get("creatorName");
            int count = (int) payload.get("participants");

             List<Integer> resourceIdsInt = (List<Integer>) payload.get("resourceIds");
            List<Long> resourceIds = resourceIdsInt.stream().map(Integer::longValue).toList();

             Map<String, String> customTotalsRaw = (Map<String, String>) payload.get("customTotals");
            Map<Long, Integer> customCountsMap = new java.util.HashMap<>();

            if (customTotalsRaw != null) {
                for (Map.Entry<String, String> entry : customTotalsRaw.entrySet()) {
                    customCountsMap.put(Long.parseLong(entry.getKey()), Integer.parseInt(entry.getValue()));
                }
            }

             DistributionSession session = service.createDistribution(
                    resourceIds,
                    count,
                    customCountsMap,
                    creatorName
            );

            return ResponseEntity.ok(session);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Hata: " + e.getMessage());
        }
    }

    @GetMapping("/my-created-sessions")
    public ResponseEntity<List<DistributionSession>> getMyCreatedSessions(@RequestParam String name) {
        List<DistributionSession> sessions = distributionSessionRepository.findByCreatorNameOrderByIdDesc(name);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/get/{code}")
    public ResponseEntity<DistributionSession> getSession(@PathVariable String code) {
        DistributionSession session = service.getSessionByCode(code);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(session);
    }
    @GetMapping("/take/{assignmentId}")
    public ResponseEntity<?> takeAssignment(@PathVariable Long assignmentId, @RequestParam String name) {
         Assignment assignment = assignmentRepository.findById(assignmentId).orElse(null);
        if (assignment == null) return ResponseEntity.notFound().build();

         if (assignment.isTaken()) {
            if (assignment.getAssignedToName() != null && assignment.getAssignedToName().equals(name)) {
                return ResponseEntity.ok(assignment);
            }
            return ResponseEntity.status(HttpStatus.CONFLICT).body("ALREADY_TAKEN");
        }

         Assignment updatedAssignment = service.claimAssignment(assignmentId, name);
        return ResponseEntity.ok(updatedAssignment);
    }

    @GetMapping("/my-sessions")
    public ResponseEntity<List<DistributionSession>> getMySessions(@RequestParam String name) {
        List<DistributionSession> sessions = assignmentRepository.findSessionsByUserName(name);
        return ResponseEntity.ok(sessions);
    }

    @PostMapping("/update-progress/{id}")
    @Transactional
    public ResponseEntity<?> updateProgress(@PathVariable Long id, @RequestParam int count) {
        Assignment assignment = assignmentRepository.findById(id).orElse(null);
        if (assignment == null) return ResponseEntity.notFound().build();

        assignment.setCurrentCount(count);
        assignmentRepository.saveAndFlush(assignment);
        return ResponseEntity.ok("Progress saved: " + count);
    }

    @PostMapping("/cancel/{id}")
    public ResponseEntity<?> cancelAssignment(@PathVariable Long id, @RequestParam String name) {
        try {
             service.cancelAssignment(id, name);
            return ResponseEntity.ok("İptal edildi");
        } catch (Exception e) {
             return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/init")
    public ResponseEntity<String> initData() {
        // Eğer özellik kapalıysa işlemi reddet
        if (!isDbResetEnabled) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Bu özellik güvenlik nedeniyle devre dışı bırakılmıştır.");
        }

        service.initDatabase();
        return ResponseEntity.ok("Veritabanı başarıyla sıfırlandı.");
    }

    @PostMapping("/complete/{id}")
    public ResponseEntity<?> completeAssignment(@PathVariable Long id, @RequestParam String name) {
        try {
            Assignment updated = service.completeAssignment(id, name);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

}