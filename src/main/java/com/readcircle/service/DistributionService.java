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
    public DistributionSession createDistribution(
            List<Long> resourceIds,
            int participantCount,
            java.util.Map<Long, Integer> customCountsMap,
            String creatorName
    ) {
        DistributionSession session = new DistributionSession();
        session.setCode(UUID.randomUUID().toString().substring(0, 8));
        session.setDescription("Çoklu Kaynak Dağıtımı");
        session.setParticipants(participantCount);

         session.setCreatorName(creatorName);

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

    public DistributionSession getSessionByCode(String code) {
        return sessionRepository.findByCode(code);
    }

    public Assignment claimAssignment(Long assignmentId, String name) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Parça bulunamadı"));

        if (assignment.isTaken()) {
            if (!assignment.getAssignedToName().equals(name)) {
                throw new RuntimeException("Bu parça maalesef başkası tarafından alındı.");
            }
        }

        assignment.setTaken(true);
        assignment.setAssignedToName(name);
        return assignmentRepository.save(assignment);
    }

     public void updateProgress(Long assignmentId, int newCount) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Parça bulunamadı"));
        assignment.setCurrentCount(newCount);
        assignmentRepository.save(assignment);
    }
}