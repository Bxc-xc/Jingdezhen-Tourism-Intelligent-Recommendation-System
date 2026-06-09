package com.jdz.tourism.repository;

import com.jdz.tourism.entity.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {
    
    // Find logs by username containing (search)
    Page<OperationLog> findByUsernameContaining(String username, Pageable pageable);
    
    // Find logs by operation containing
    Page<OperationLog> findByOperationContaining(String operation, Pageable pageable);
    
    // Find all (ordered by createTime desc by default if pageable is set so)
}
