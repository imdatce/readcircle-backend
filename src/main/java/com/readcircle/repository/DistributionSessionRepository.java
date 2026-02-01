package com.readcircle.repository;

import com.readcircle.model.DistributionSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistributionSessionRepository extends JpaRepository<DistributionSession, Long> {
     DistributionSession findByCode(String code);
    List<DistributionSession> findByCreatorNameOrderByIdDesc(String creatorName);
}