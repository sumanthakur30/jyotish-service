package com.shopmanagement.jyotishservice.persistence.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopmanagement.jyotishservice.persistence.entity.MatchingKootaScoreEntity;

public interface MatchingKootaScoreRepository extends JpaRepository<MatchingKootaScoreEntity, Long> {

  List<MatchingKootaScoreEntity> findByMatchingIdOrderBySortOrderAsc(Long matchingId);
}
