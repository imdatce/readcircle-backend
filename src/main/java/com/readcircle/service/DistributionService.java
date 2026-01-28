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
    public DistributionSession createDistribution(
            List<Long> resourceIds,
            int participantCount,
            java.util.Map<Long, Integer> customCountsMap
    ) {
        DistributionSession session = new DistributionSession();
        session.setCode(generateUniqueCode());
        session.setParticipants(participantCount);
        session = sessionRepository.save(session);

        List<Assignment> assignments = new ArrayList<>();

        for (Long resId : resourceIds) {
            Resource resource = resourceRepository.findById(resId).orElse(null);
            if (resource != null) {

                int totalUnits;

                if ((resource.getType() == ResourceType.COUNTABLE || resource.getType() == ResourceType.JOINT) &&
                        customCountsMap != null &&
                        customCountsMap.containsKey(resId)) {

                    totalUnits = customCountsMap.get(resId);
                } else {
                    totalUnits = resource.getTotalUnits();
                }

                if (resource.getType() == ResourceType.JOINT) {
                    for (int i = 0; i < participantCount; i++) {
                        Assignment assignment = new Assignment();
                        assignment.setSession(session);
                        assignment.setResource(resource);
                        assignment.setParticipantNumber(i + 1);

                        assignment.setStartUnit(1);
                        assignment.setEndUnit(totalUnits);

                        assignment.setTaken(false);
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
                            Assignment assignment = new Assignment();
                            assignment.setSession(session);
                            assignment.setResource(resource);
                            assignment.setParticipantNumber(i + 1);
                            assignment.setStartUnit(currentStart);
                            assignment.setEndUnit(currentStart + myAmount - 1);
                            assignment.setTaken(false);

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
    public DistributionSession getSessionByCode(String code) {
        return sessionRepository.findByCode(code);
    }

    public Assignment claimAssignment(Long assignmentId, String name) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Parça bulunamadı"));

        if (assignment.isTaken()) {
            throw new RuntimeException("Bu parça maalesef başkası tarafından alındı.");
        }

        assignment.setTaken(true);
        assignment.setAssignedToName(name);
        return assignmentRepository.save(assignment);
    }

    private String generateUniqueCode() {

        return UUID.randomUUID().toString().substring(0, 8);
    }
}