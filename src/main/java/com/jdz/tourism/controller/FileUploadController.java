package com.jdz.tourism.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*")
public class FileUploadController {
    
    @Value("${file.upload-dir:uploads/}")
    private String uploadDir;
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "文件不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 检查文件大小（限制为5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                response.put("success", false);
                response.put("message", "文件大小不能超过5MB");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !isAllowedFileType(contentType)) {
                response.put("success", false);
                response.put("message", "不支持的文件类型");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 创建上传目录
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;
            
            // 保存文件
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);
            
            // 返回文件URL（只返回相对路径，由前端根据环境拼接完整URL）
            String relativeUrl = "/uploads/" + filename;
            
            response.put("success", true);
            response.put("message", "文件上传成功");
            response.put("url", relativeUrl);
            response.put("filename", filename);
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "文件上传失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    private String buildBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();
        boolean isDefaultPort = ("http".equalsIgnoreCase(scheme) && serverPort == 80) || ("https".equalsIgnoreCase(scheme) && serverPort == 443);
        String portPart = isDefaultPort ? "" : (":" + serverPort);
        String ctx = (contextPath == null || contextPath.isBlank()) ? "" : contextPath;
        return scheme + "://" + serverName + portPart + ctx;
    }
    
    /**
     * 检查文件类型是否允许
     */
    private boolean isAllowedFileType(String contentType) {
        String[] allowedTypes = {
            "image/jpeg",
            "image/jpg", 
            "image/png",
            "image/gif",
            "image/webp",
            "application/pdf"
        };
        
        for (String allowedType : allowedTypes) {
            if (contentType.equals(allowedType)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 删除文件
     */
    @DeleteMapping("/{filename}")
    public ResponseEntity<Map<String, Object>> deleteFile(@PathVariable String filename) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Path filePath = Paths.get(uploadDir, filename);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                response.put("success", true);
                response.put("message", "文件删除成功");
            } else {
                response.put("success", false);
                response.put("message", "文件不存在");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "文件删除失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}