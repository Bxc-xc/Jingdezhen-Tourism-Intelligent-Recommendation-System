package com.jdz.tourism.controller;

import com.jdz.tourism.entity.OperationLog;
import com.jdz.tourism.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/logs")
@CrossOrigin(origins = "*")
@Tag(name = "操作日志管理", description = "管理员查看系统操作日志")
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    @Operation(summary = "获取操作日志列表", description = "分页获取操作日志，支持关键词搜索")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        
        try {
            Page<OperationLog> logsPage = operationLogService.getLogs(page, size, keyword);
            
            System.out.println("查询操作日志 - 页码: " + page + ", 大小: " + size + 
                ", 关键词: " + keyword + ", 总数: " + logsPage.getTotalElements());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", logsPage.getContent());
            response.put("total", logsPage.getTotalElements());
            response.put("totalPages", logsPage.getTotalPages());
            response.put("currentPage", page);
            response.put("pageSize", size);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("获取操作日志失败: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取操作日志失败: " + e.getMessage());
            response.put("data", new java.util.ArrayList<>());
            response.put("total", 0);
            return ResponseEntity.ok(response);
        }
    }
}
