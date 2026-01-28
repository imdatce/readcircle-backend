package com.readcircle.repository;

import com.readcircle.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
    Resource findByCodeKey(String codeKey);
}