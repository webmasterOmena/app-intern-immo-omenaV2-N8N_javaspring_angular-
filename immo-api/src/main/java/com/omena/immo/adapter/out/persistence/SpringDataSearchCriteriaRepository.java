package com.omena.immo.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface SpringDataSearchCriteriaRepository extends JpaRepository<SearchCriteriaJpaEntity, UUID> {

    List<SearchCriteriaJpaEntity> findAllByOrderByCreatedAtDesc();

    List<SearchCriteriaJpaEntity> findByActiveTrueOrderByCreatedAtDesc();
}
