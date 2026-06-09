package com.jdz.tourism.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 安全异常处理器：处理认证和授权异常
 */
@Component
public class SecurityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    /**
     * 处理认证异常（未登录或token无效）
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        System.out.println("认证失败 - URI: " + request.getRequestURI() + ", 错误: " + authException.getMessage());
        
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"success\":false,\"message\":\"未授权，请先登录\"}");
    }

    /**
     * 处理授权异常（已登录但权限不足）
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        System.out.println("授权失败 - URI: " + request.getRequestURI() + ", 错误: " + accessDeniedException.getMessage());
        
        // 检查 SecurityContext 中是否有认证信息
        if (request.getUserPrincipal() == null) {
            System.out.println("SecurityContext 中没有认证信息，可能是 JWT 验证失败");
        } else {
            System.out.println("SecurityContext 中有认证信息，用户: " + request.getUserPrincipal().getName());
        }
        
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"success\":false,\"message\":\"权限不足，请检查是否已登录\"}");
    }
}

