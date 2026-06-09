package com.jdz.tourism.repository;

import com.jdz.tourism.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {
    
    List<Food> findByCategory(String category);
    
    List<Food> findByRatingGreaterThanEqual(BigDecimal rating);
    
    List<Food> findByNameContaining(String keyword);
}

