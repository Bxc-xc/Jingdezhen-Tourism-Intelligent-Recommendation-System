package com.jdz.tourism.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, 
                                          JwtAuthenticationFilter jwtAuthenticationFilter,
                                          SecurityExceptionHandler securityExceptionHandler) throws Exception {
        http
            // 禁用CSRF保护
            .csrf(csrf -> csrf.disable())
            
            // 配置CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 配置会话管理为无状态
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 配置异常处理
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(securityExceptionHandler)  // 处理认证异常（未登录）
                .accessDeniedHandler(securityExceptionHandler)        // 处理授权异常（权限不足）
            )
            
            // 配置请求授权
            .authorizeHttpRequests(auth -> auth
                // 允许所有人访问认证相关接口
                .requestMatchers("/api/auth/**").permitAll()
                // 允许所有人访问景点信息接口
                .requestMatchers("/api/scenic/**").permitAll()
                // 允许所有人访问美食推荐接口
                .requestMatchers("/api/food/**").permitAll()
                // 允许所有人访问路线推荐接口
                .requestMatchers("/api/route", "/api/route/**").permitAll()
                // 允许所有人访问市集接口（GET请求）
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/marketplace/**").permitAll()
                // 允许所有人访问房型信息接口（游客预订时需要查看）
                .requestMatchers("/api/merchant/room-types/**").permitAll()
                // 允许所有人访问行程规划接口
                .requestMatchers("/api/plan/**").permitAll()
                // 允许所有人访问评论接口
                .requestMatchers("/api/reviews/**").permitAll()
                // 游客查看回复树、评论嵌套列表（GET）
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/replies/**").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/comment/**").permitAll()
                // 允许所有人访问系统配置的公开接口（获取系统设置）
                .requestMatchers("/api/system-config/settings", "/api/system-config/**").permitAll()
                // 允许所有人访问WebSocket
                .requestMatchers("/ws/**").permitAll()
                // 允许所有人访问静态资源
                .requestMatchers("/static/**", "/public/**").permitAll()
                // 允许所有人访问上传的文件
                .requestMatchers("/uploads/**").permitAll()
                // 允许访问错误页面（避免404时再次触发认证）
                .requestMatchers("/error").permitAll()
                // 临时放行账号同步接口（解决权限抵抗问题）
                .requestMatchers("/api/merchant/sync-users").permitAll()
                // 游客可访问（公开读取）商家相关接口：
                // 仅放行 GET，确保推荐页/详情页在未登录状态可浏览，写操作仍必须登录
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/merchant/category/**").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/merchant/*").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/merchant/scenic/**").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/merchant/activity/list/**").permitAll()
                // 商家资质申请接口需要认证（已登录用户都可以提交申请）
                .requestMatchers("/api/merchant-application/**").authenticated()
                // 其他请求需要认证
                .anyRequest().authenticated()
            )
            // 注册 JWT 过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(com.jdz.tourism.utils.JwtUtil jwtUtil) {
        return new JwtAuthenticationFilter(jwtUtil);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允许的源
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // 允许携带凭证
        configuration.setAllowCredentials(true);
        
        // 预检请求的缓存时间
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}