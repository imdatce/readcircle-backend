package com.readcircle.repository;

import com.readcircle.model.DistributionSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DistributionSessionRepository extends JpaRepository<DistributionSession, Long> {
    DistributionSession findByCode(String code);}