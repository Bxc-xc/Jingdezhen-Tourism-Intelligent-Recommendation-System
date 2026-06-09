package com.jdz.tourism.config;

import com.jdz.tourism.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 认证过滤器：从 Authorization: Bearer <token> 解析并注入认证信息。
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.extractUsername(token);
                    String role = jwtUtil.extractRole(token); // ADMIN / MERCHANT / TOURIST

                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, List.of(authority));
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    // 调试日志
                    System.out.println("JWT认证成功 - 用户: " + username + ", 角色: " + role + ", 请求: " + request.getRequestURI());
                } else {
                    // Token 验证失败
                    System.out.println("JWT Token 验证失败 - 请求: " + request.getRequestURI() + ", Token: " + token.substring(0, Math.min(20, token.length())) + "...");
                }
            } catch (Exception e) {
                // 记录异常信息以便调试
                System.out.println("JWT认证异常 - 请求: " + request.getRequestURI() + ", 错误: " + e.getMessage());
                e.printStackTrace();
                // 保持匿名，交给后续过滤器处理
            }
        } else {
            // 没有认证头
            if (request.getRequestURI().startsWith("/api/merchant-application")) {
                System.out.println("JWT认证失败 - 缺少Authorization头 - 请求: " + request.getRequestURI());
            }
        }

        filterChain.doFilter(request, response);
    }
}


