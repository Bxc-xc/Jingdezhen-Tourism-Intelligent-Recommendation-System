package com.jdz.tourism.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jdz.tourism.annotation.LogOperation;
import com.jdz.tourism.entity.OperationLog;
import com.jdz.tourism.entity.User;
import com.jdz.tourism.repository.UserRepository;
import com.jdz.tourism.service.OperationLogService;
import com.jdz.tourism.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Optional;

@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Pointcut("@annotation(com.jdz.tourism.annotation.LogOperation)")
    public void logPointcut() {}

    @Around("logPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        Object result = null;
        try {
            // Execute the method
            result = point.proceed();
            
            // Save log asynchronously (optional, but good for performance)
            // For simplicity, we'll do it synchronously here
            saveLog(point, null);
            
            return result;
        } catch (Throwable e) {
            saveLog(point, e);
            throw e;
        }
    }

    private void saveLog(ProceedingJoinPoint point, Throwable exception) {
        try {
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            
            OperationLog log = new OperationLog();
            
            // Get annotation value
            LogOperation logAnnotation = method.getAnnotation(LogOperation.class);
            if (logAnnotation != null) {
                log.setOperation(logAnnotation.value());
            }
            
            // If exception occurred, mark as failed
            if (exception != null) {
                log.setOperation(log.getOperation() + " (失败)");
            }
            
            // Method name
            String className = point.getTarget().getClass().getName();
            String methodName = signature.getName();
            log.setMethod(className + "." + methodName + "()");
            
            // Params (truncate if too long)
            try {
                Object[] args = point.getArgs();
                StringBuilder paramsBuilder = new StringBuilder();
                
                if (args != null && args.length > 0) {
                    String params = objectMapper.writeValueAsString(args);
                    
                    // Mask passwords and other sensitive fields
                    params = params.replaceAll("\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"******\"");
                    params = params.replaceAll("\"oldPassword\"\\s*:\\s*\"[^\"]*\"", "\"oldPassword\":\"******\"");
                    params = params.replaceAll("\"newPassword\"\\s*:\\s*\"[^\"]*\"", "\"newPassword\":\"******\"");
                    
                    paramsBuilder.append(params);
                }
                
                // Append exception info
                if (exception != null) {
                    if (paramsBuilder.length() > 0) {
                        paramsBuilder.append("\n\n");
                    }
                    paramsBuilder.append("Exception: ").append(exception.getClass().getName());
                    paramsBuilder.append(": ").append(exception.getMessage());
                }
                
                String finalParams = paramsBuilder.toString();
                if (finalParams.length() > 2000) { // Limit length
                    finalParams = finalParams.substring(0, 2000) + "...";
                }
                log.setParams(finalParams);
                
            } catch (Exception e) {
                // Ignore param serialization errors
                log.setParams("Error serializing params");
            }
            
            // IP Address
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                // 如果无法获取请求属性，记录日志但不保存
                return;
            }
            HttpServletRequest request = attributes.getRequest();
            log.setIpAddress(getIpAddress(request));
            
            // User Info - 优先从SecurityContextHolder获取，如果失败则从JWT token获取
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = null;
            Long userId = null;
            String userRole = null;
            
            if (authentication != null && authentication.isAuthenticated() && 
                !"anonymousUser".equals(authentication.getPrincipal())) {
                // 从SecurityContextHolder获取
                username = authentication.getName();
                Optional<User> userOpt = userRepository.findByUsername(username);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    userId = user.getId();
                    userRole = user.getRole().name();
                }
            } else {
                // 如果SecurityContextHolder中没有用户信息，尝试从JWT token获取
                try {
                    String authHeader = request.getHeader("Authorization");
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        if (jwtUtil.validateToken(token)) {
                            username = jwtUtil.extractUsername(token);
                            userId = jwtUtil.extractUserId(token);
                            userRole = jwtUtil.extractRole(token);
                            
                            // 如果从token中获取到用户信息，尝试从数据库获取完整信息以确保一致性
                            if (userId != null) {
                                Optional<User> userOpt = userRepository.findById(userId);
                                if (userOpt.isPresent()) {
                                    User user = userOpt.get();
                                    username = user.getUsername(); // 使用数据库中的用户名
                                    userRole = user.getRole().name(); // 使用数据库中的角色
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    // 如果JWT解析失败，忽略错误，继续使用默认值
                    System.out.println("从JWT token获取用户信息失败: " + e.getMessage());
                }
            }
            
            // 设置用户信息
            if (username != null) {
                log.setUsername(username);
            } else {
                log.setUsername("Anonymous");
            }
            
            if (userId != null) {
                log.setUserId(userId);
            }
            
            if (userRole != null) {
                log.setUserRole(userRole);
            }
            
            operationLogService.saveLog(log);
            
            // 调试日志：记录保存成功的日志
            System.out.println("操作日志已保存: " + log.getOperation() + 
                ", 用户: " + log.getUsername() + 
                ", 角色: " + log.getUserRole() +
                ", 时间: " + log.getCreateTime());
            
        } catch (Exception e) {
            System.err.println("保存操作日志失败: " + e.getMessage());
            e.printStackTrace();
            // Don't fail the request if logging fails
        }
    }
    
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
