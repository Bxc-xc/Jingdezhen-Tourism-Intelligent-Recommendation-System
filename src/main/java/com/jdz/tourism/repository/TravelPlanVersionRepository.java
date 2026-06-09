package com.jdz.tourism.repository;

import com.jdz.tourism.entity.TravelPlanVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TravelPlanVersionRepository extends JpaRepository<TravelPlanVersion, Long> {

    List<TravelPlanVersion> findByPlanIdOrderByVersionNumberDesc(Long planId);

    Optional<TravelPlanVersion> findTopByPlanIdOrderByVersionNumberDesc(Long planId);
}


