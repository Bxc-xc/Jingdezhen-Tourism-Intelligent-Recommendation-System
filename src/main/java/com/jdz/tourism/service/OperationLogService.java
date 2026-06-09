package com.jdz.tourism.service;

import com.jdz.tourism.entity.OperationLog;
import com.jdz.tourism.repository.OperationLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class OperationLogService {

    @Autowired
    private OperationLogRepository operationLogRepository;

    public void saveLog(OperationLog log) {
        operationLogRepository.save(log);
    }

    public Page<OperationLog> getLogs(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        
        if (keyword != null && !keyword.isEmpty()) {
            // Try to match username first
            Page<OperationLog> byUsername = operationLogRepository.findByUsernameContaining(keyword, pageable);
            if (byUsername.hasContent()) {
                return byUsername;
            }
            // If no match by username, try operation
            return operationLogRepository.findByOperationContaining(keyword, pageable);
        }
        
        return operationLogRepository.findAll(pageable);
    }
    
    public void deleteLog(Long id) {
        operationLogRepository.deleteById(id);
    }
}
