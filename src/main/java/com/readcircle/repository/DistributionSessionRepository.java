package com.readcircle.repository;

import com.readcircle.model.DistributionSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistributionSessionRepository extends JpaRepository<DistributionSession, Long> {

    DistributionSession findByCode(String code);

    @Query("SELECT DISTINCT s FROM DistributionSession s LEFT JOIN FETCH s.assignments a LEFT JOIN FETCH a.resource WHERE s.code = :code")
    DistributionSession findByCodeWithAssignments(@Param("code") String code);

    List<DistributionSession> findByCreatorNameOrderByIdDesc(String creatorName);
}