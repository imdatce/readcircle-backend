package com.readcircle.repository;

import com.readcircle.model.Assignment;
import com.readcircle.model.DistributionSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findBySession_Id(Long sessionId);

    @Query("SELECT DISTINCT a.session FROM Assignment a WHERE a.assignedToName = :name")
    List<DistributionSession> findSessionsByUserName(@Param("name") String name);
}