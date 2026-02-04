//package com.readcircle.controller;
//
//import com.readcircle.dto.CreateDistributionRequest;
//import com.readcircle.model.*;
//import com.readcircle.repository.AssignmentRepository;
//import com.readcircle.repository.DistributionSessionRepository;
//import com.readcircle.repository.ResourceRepository;
//import com.readcircle.service.DistributionService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/distribution")
//public class DistributionController {
//
//    private final DistributionService service;
//    private final ResourceRepository resourceRepository;
//    private final DistributionSessionRepository distributionSessionRepository;
//
//    @Autowired
//    private AssignmentRepository assignmentRepository;
//
//    public DistributionController(DistributionService service, ResourceRepository resourceRepository, DistributionSessionRepository distributionSessionRepository) {
//        this.service = service;
//        this.resourceRepository = resourceRepository;
//        this.distributionSessionRepository = distributionSessionRepository;
//    }
//
//    @GetMapping("/resources")
//    public List<Resource> getAllResources() {
//        return resourceRepository.findAll();
//    }
//
//    @PostMapping("/create")
//    public ResponseEntity<?> createDistribution(@RequestBody CreateDistributionRequest request) {
//        DistributionSession session = new DistributionSession();
//        String uniqueCode = UUID.randomUUID().toString().substring(0, 8);
//        session.setCode(uniqueCode);
//        session.setParticipants(request.getCount());
//        session.setCreatorName(request.getCreatorName());
//        session = distributionSessionRepository.save(session);
//
//        Resource resource = resourceRepository.findByCodeKey(request.getType());
//        if (resource == null) {
//            return ResponseEntity.badRequest().body("Geçersiz dağıtım türü: " + request.getType());
//        }
//
//        service.createAssignments(session, resource, request.getCount());
//        return ResponseEntity.ok(session);
//    }
//
//    @GetMapping("/my-created-sessions")
//    public ResponseEntity<List<DistributionSession>> getMyCreatedSessions(@RequestParam String name) {
//        List<DistributionSession> sessions = distributionSessionRepository.findByCreatorNameOrderByIdDesc(name);
//        return ResponseEntity.ok(sessions);
//    }
//
//    @GetMapping("/get/{code}")
//    public DistributionSession getSession(@PathVariable String code) {
//        return service.getSessionByCode(code);
//    }
//
//    @GetMapping("/take/{assignmentId}")
//    public ResponseEntity<?> takeAssignment(@PathVariable Long assignmentId, @RequestParam String name) {
//        Assignment assignment = assignmentRepository.findById(assignmentId).orElse(null);
//        if (assignment == null) return ResponseEntity.notFound().build();
//
//        if (assignment.isTaken()) {
//            if (assignment.getAssignedToName() != null && assignment.getAssignedToName().equals(name)) {
//                return ResponseEntity.ok(assignment);
//            }
//            return ResponseEntity.status(HttpStatus.CONFLICT).body("ALREADY_TAKEN");
//        }
//        Assignment updatedAssignment = service.claimAssignment(assignmentId, name);
//        return ResponseEntity.ok(updatedAssignment);
//    }
//
//    @GetMapping("/my-sessions")
//    public ResponseEntity<List<DistributionSession>> getMySessions(@RequestParam String name) {
//        List<DistributionSession> sessions = assignmentRepository.findSessionsByUserName(name);
//        return ResponseEntity.ok(sessions);
//    }
//
//    @PostMapping("/update-progress/{id}")
//    @Transactional
//    public ResponseEntity<?> updateProgress(@PathVariable Long id, @RequestParam int count) {
//        Assignment assignment = assignmentRepository.findById(id).orElse(null);
//        if (assignment == null) return ResponseEntity.notFound().build();
//
//        assignment.setCurrentCount(count);
//        assignmentRepository.saveAndFlush(assignment);
//        return ResponseEntity.ok("Progress saved: " + count);
//    }
//}

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

import java.util.List;
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

    @GetMapping("/resources")
    public List<Resource> getAllResources() {
        return resourceRepository.findAll();
    }

    // --- YENİ EKLENEN GET METODU (Frontend Uyumluluğu İçin) ---
    @GetMapping("/create")
    public ResponseEntity<?> createDistributionGet(
            @RequestParam(name = "resourceIds") List<Long> resourceIds, // Virgülle ayrılmış ID'leri listeye çevirir
            @RequestParam int participants,
            @RequestParam String creatorName
    ) {
        // 1. Yeni bir dağıtım oturumu oluştur
        DistributionSession session = new DistributionSession();
        String uniqueCode = UUID.randomUUID().toString().substring(0, 8);
        session.setCode(uniqueCode);
        session.setParticipants(participants);
        session.setCreatorName(creatorName);
        session = distributionSessionRepository.save(session);

        // 2. Seçilen her kaynak için atamaları (Assignments) oluştur
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

    // Mevcut POST metodu (Alternatif kullanım için kalabilir)
    @PostMapping("/create")
    public ResponseEntity<?> createDistribution(@RequestBody CreateDistributionRequest request) {
        DistributionSession session = new DistributionSession();
        String uniqueCode = UUID.randomUUID().toString().substring(0, 8);
        session.setCode(uniqueCode);
        session.setParticipants(request.getCount());
        session.setCreatorName(request.getCreatorName());
        session = distributionSessionRepository.save(session);

        Resource resource = resourceRepository.findByCodeKey(request.getType());
        if (resource == null) {
            return ResponseEntity.badRequest().body("Geçersiz dağıtım türü: " + request.getType());
        }

        service.createAssignments(session, resource, request.getCount());
        return ResponseEntity.ok(session);
    }

    @GetMapping("/my-created-sessions")
    public ResponseEntity<List<DistributionSession>> getMyCreatedSessions(@RequestParam String name) {
        List<DistributionSession> sessions = distributionSessionRepository.findByCreatorNameOrderByIdDesc(name);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/get/{code}")
    public DistributionSession getSession(@PathVariable String code) {
        return service.getSessionByCode(code);
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
}