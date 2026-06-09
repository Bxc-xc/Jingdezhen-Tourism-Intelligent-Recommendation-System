package com.jdz.tourism.controller;

import com.jdz.tourism.entity.User;
import com.jdz.tourism.service.UserService;
import com.jdz.tourism.utils.JwtUtil;
import com.jdz.tourism.annotation.LogOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 用户注册
     */
    @LogOperation("用户注册")
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, Object> registerData) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 提取用户基本信息
            User user = new User();
            user.setUsername(registerData.get("username").toString());
            user.setPassword(registerData.get("password").toString());
            user.setPhone(registerData.get("phone") != null ? registerData.get("phone").toString() : null);
            
            // 设置角色
            String roleStr = registerData.get("role") != null ? registerData.get("role").toString() : "TOURIST";
            user.setRole(User.UserRole.valueOf(roleStr));
            
            // 注册用户
            User savedUser = userService.register(user, registerData);
            
            response.put("success", true);
            response.put("message", "注册成功");
            response.put("data", savedUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 用户登录
     */
    @LogOperation("用户登录")
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("=== 后端接收登录请求 ===");
            System.out.println("接收到的数据: " + loginData);
            
            String username = loginData.get("username");
            String password = loginData.get("password");
            
            if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "用户名和密码不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 验证用户凭据
            User user = userService.login(username.trim(), password);
            
            // 生成JWT Token
            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name());
            
            // 构建响应数据（不包含密码）
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("username", user.getUsername());
            userData.put("role", user.getRole().name());
            userData.put("phone", user.getPhone());
            userData.put("avatar", user.getAvatar());
            
            System.out.println("登录成功: " + user.getUsername() + ", 角色: " + user.getRole());
            
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("data", userData);
            response.put("token", token);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("登录失败: " + e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 忘记密码 - 通过用户名+手机号验证后重置（无需短信服务）
     */
    @LogOperation("忘记密码")
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody Map<String, String> data) {
        Map<String, Object> response = new HashMap<>();
        try {
            String username = data.get("username");
            String phone = data.get("phone");
            String newPassword = data.get("newPassword");
            
            if (username == null || phone == null || newPassword == null || 
                username.trim().isEmpty() || phone.trim().isEmpty() || newPassword.length() < 6) {
                response.put("success", false);
                response.put("message", "请填写完整信息，新密码至少6位");
                return ResponseEntity.badRequest().body(response);
            }
            
            userService.resetPasswordByPhone(username.trim(), phone.trim(), newPassword);
            response.put("success", true);
            response.put("message", "密码重置成功，请使用新密码登录");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 验证Token
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyToken(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("success", false);
                response.put("message", "Token格式错误");
                return ResponseEntity.badRequest().body(response);
            }
            
            String token = authHeader.substring(7);
            
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);
                Long userId = jwtUtil.extractUserId(token);
                String role = jwtUtil.extractRole(token);
                
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", userId);
                userData.put("username", username);
                userData.put("role", role);
                
                response.put("success", true);
                response.put("message", "Token有效");
                response.put("data", userData);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Token无效或已过期");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Token验证失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}