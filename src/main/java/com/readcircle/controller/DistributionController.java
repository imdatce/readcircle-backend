package com.readcircle.controller;

import com.readcircle.dto.CreateDistributionRequest;
import com.readcircle.model.Assignment;
import com.readcircle.model.DistributionSession;
import com.readcircle.model.Resource;
import com.readcircle.repository.AssignmentRepository;
import com.readcircle.repository.DistributionSessionRepository;
import com.readcircle.repository.ResourceRepository;
import com.readcircle.service.DistributionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import jakarta.validation.Valid;

import java.util.List;

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

    @PostMapping("/create")
    public ResponseEntity<?> createDistribution(@Valid @RequestBody CreateDistributionRequest request) {
        String creatorName = getCurrentUsername();

        DistributionSession session = service.createDistribution(
                request.getResourceIds(),
                request.getParticipants(),
                request.getCustomTotals(),
                creatorName
        );

        return ResponseEntity.ok(session);
    }

    @GetMapping("/my-created-sessions")
    public ResponseEntity<List<DistributionSession>> getMyCreatedSessions() {
        String name = getCurrentUsername();
        List<DistributionSession> sessions = distributionSessionRepository.findByCreatorNameOrderByIdDesc(name);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/my-sessions")
    public ResponseEntity<List<DistributionSession>> getMySessions() {
        String name = getCurrentUsername();
        List<DistributionSession> sessions = assignmentRepository.findSessionsByUserName(name);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/get/{code}")
    public DistributionSession getSession(@PathVariable String code) {
        return service.getSessionByCode(code);
    }

     @PostMapping("/take/{assignmentId}")
    public ResponseEntity<?> takeAssignment(@PathVariable Long assignmentId, @RequestParam(required = false) String name) {
        String effectiveName;
        try {
            effectiveName = getEffectiveUsername(name);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }

        Assignment assignment = assignmentRepository.findById(assignmentId).orElse(null);
        if (assignment == null) return ResponseEntity.notFound().build();

        if (assignment.isTaken()) {
            if (assignment.getAssignedToName() != null && assignment.getAssignedToName().equals(effectiveName)) {
                return ResponseEntity.ok(assignment);
            }
            return ResponseEntity.status(HttpStatus.CONFLICT).body("ALREADY_TAKEN");
        }

        Assignment updatedAssignment = service.claimAssignment(assignmentId, effectiveName);
        return ResponseEntity.ok(updatedAssignment);
    }

     @PostMapping("/update-progress/{id}")
    @Transactional
    public ResponseEntity<?> updateProgress(@PathVariable Long id, @RequestParam int count, @RequestParam(required = false) String name) {
        try {
             String effectiveName = getEffectiveUsername(name);

             service.updateProgress(id, count, effectiveName);

            return ResponseEntity.ok("Progress saved: " + count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

     @PostMapping("/cancel/{id}")
    public ResponseEntity<?> cancelAssignment(@PathVariable Long id, @RequestParam(required = false) String name) {
        try {
            String effectiveName = getEffectiveUsername(name);
            service.cancelAssignment(id, effectiveName);
            return ResponseEntity.ok("İptal edildi");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

     @PostMapping("/complete/{id}")
    public ResponseEntity<?> completeAssignment(@PathVariable Long id, @RequestParam(required = false) String name) {
        try {
            String effectiveName = getEffectiveUsername(name);
            Assignment updated = service.completeAssignment(id, effectiveName);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/init")
    public ResponseEntity<String> initData() {
        if (!isDbResetEnabled) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Bu özellik güvenlik nedeniyle devre dışı bırakılmıştır.");
        }

        service.initDatabase();
        return ResponseEntity.ok("Veritabanı başarıyla sıfırlandı.");
    }

    @DeleteMapping("/delete-session/{code}")
    public ResponseEntity<?> deleteSession(@PathVariable String code) {
        try {
            String username = getCurrentUsername();
            service.deleteSession(code, username);
            return ResponseEntity.ok("Oturum başarıyla silindi.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/leave-session/{code}")
    public ResponseEntity<?> leaveSession(@PathVariable String code) {
        try {
            String username = getCurrentUsername();
            service.leaveSession(code, username);
            return ResponseEntity.ok("Halkadan ayrıldınız.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reset-session/{code}")
    public ResponseEntity<?> resetSession(@PathVariable String code) {
        try {
            String username = getCurrentUsername();
            service.resetSession(code, username);
            return ResponseEntity.ok("Oturum başarıyla sıfırlandı.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

     private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new RuntimeException("Kullanıcı oturumu bulunamadı.");
        }
        return authentication.getName();
    }

    private String getEffectiveUsername(String paramName) {
        try {
             return getCurrentUsername();
        } catch (Exception e) {
             if (paramName != null && !paramName.trim().isEmpty()) {
                return paramName;
            }
            throw new RuntimeException("Lütfen isminizi giriniz veya giriş yapınız.");
        }
    }
}