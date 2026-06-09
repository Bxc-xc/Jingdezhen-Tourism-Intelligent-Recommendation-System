package com.jdz.tourism.repository;

import com.jdz.tourism.entity.TravelRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TravelRouteRepository extends JpaRepository<TravelRoute, Long> {
    
    List<TravelRoute> findByDays(Integer days);
    
    List<TravelRoute> findByDifficulty(String difficulty);
}

