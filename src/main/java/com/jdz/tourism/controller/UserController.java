package com.jdz.tourism.controller;

import com.jdz.tourism.entity.User;
import com.jdz.tourism.service.UserService;
import com.jdz.tourism.utils.JwtUtil;
import com.jdz.tourism.annotation.LogOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<User> users = userService.getAllUsers();
            response.put("success", true);
            response.put("data", users);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getCurrentUserInfo(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                response.put("success", false);
                response.put("message", "未提供认证信息");
                return ResponseEntity.status(401).body(response);
            }
            String token = authorization.substring(7);
            if (!jwtUtil.validateToken(token)) {
                response.put("success", false);
                response.put("message", "Token无效或已过期");
                return ResponseEntity.status(401).body(response);
            }
            Long userId = jwtUtil.extractUserId(token);
            Optional<User> user = userService.findById(userId);
            if (user.isPresent()) {
                response.put("success", true);
                response.put("data", user.get());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.status(401).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<User> user = userService.findById(id);
            if (user.isPresent()) {
                response.put("success", true);
                response.put("data", user.get());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @LogOperation("更新用户信息")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long id, @Valid @RequestBody User user) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<User> existingUser = userService.findById(id);
            if (existingUser.isPresent()) {
                user.setId(id);
                User updatedUser = userService.updateUser(user);
                response.put("success", true);
                response.put("message", "更新成功");
                response.put("data", updatedUser);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @LogOperation("删除用户")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            userService.deleteUser(id);
            response.put("success", true);
            response.put("message", "删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @LogOperation("更新当前用户信息")
    @PutMapping("/info")
    public ResponseEntity<Map<String, Object>> updateCurrentUserInfo(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Map<String, Object> userData) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                response.put("success", false);
                response.put("message", "未提供认证信息");
                return ResponseEntity.status(401).body(response);
            }
            String token = authorization.substring(7);
            if (!jwtUtil.validateToken(token)) {
                response.put("success", false);
                response.put("message", "Token无效或已过期");
                return ResponseEntity.status(401).body(response);
            }
            Long userId = jwtUtil.extractUserId(token);
            Optional<User> userOpt = userService.findById(userId);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.notFound().build();
            }
            User user = userOpt.get();
            if (userData.containsKey("username")) {
                String newUsername = userData.get("username") != null ? userData.get("username").toString() : null;
                if (newUsername != null && !newUsername.trim().isEmpty()) {
                    Optional<User> existingUser = userService.findByUsername(newUsername);
                    if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                        response.put("success", false);
                        response.put("message", "用户名已被使用");
                        return ResponseEntity.badRequest().body(response);
                    }
                    user.setUsername(newUsername.trim());
                }
            }
            if (userData.containsKey("avatar")) {
                String avatar = userData.get("avatar") != null ? userData.get("avatar").toString() : null;
                user.setAvatar(avatar);
            }
            if (userData.containsKey("phone")) {
                String phone = userData.get("phone") != null ? userData.get("phone").toString() : null;
                user.setPhone(phone);
            }
            if (userData.containsKey("email")) {
                String email = userData.get("email") != null ? userData.get("email").toString() : null;
                user.setEmail(email);
            }
            if (userData.containsKey("bio")) {
                String bio = userData.get("bio") != null ? userData.get("bio").toString() : null;
                user.setBio(bio);
            }
            User updatedUser = userService.updateUser(user);
            Map<String, Object> userDataResponse = new HashMap<>();
            userDataResponse.put("id", updatedUser.getId());
            userDataResponse.put("username", updatedUser.getUsername());
            userDataResponse.put("role", updatedUser.getRole().name());
            userDataResponse.put("phone", updatedUser.getPhone());
            userDataResponse.put("email", updatedUser.getEmail());
            userDataResponse.put("bio", updatedUser.getBio());
            userDataResponse.put("avatar", updatedUser.getAvatar());
            response.put("success", true);
            response.put("message", "更新成功");
            response.put("data", userDataResponse);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @LogOperation("修改密码")
    @PutMapping("/password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Map<String, String> passwordData) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                response.put("success", false);
                response.put("message", "未提供认证信息");
                return ResponseEntity.status(401).body(response);
            }
            String token = authorization.substring(7);
            if (!jwtUtil.validateToken(token)) {
                response.put("success", false);
                response.put("message", "Token无效或已过期");
                return ResponseEntity.status(401).body(response);
            }
            Long userId = jwtUtil.extractUserId(token);
            String oldPassword = passwordData.get("oldPassword");
            String newPassword = passwordData.get("newPassword");
            if (oldPassword == null || newPassword == null) {
                response.put("success", false);
                response.put("message", "原密码和新密码不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            userService.changePassword(userId, oldPassword, newPassword);
            response.put("success", true);
            response.put("message", "密码修改成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @LogOperation("管理员重置用户密码")
    @PutMapping("/{id}/reset-password")
    public ResponseEntity<Map<String, Object>> adminResetPassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            String newPassword = body.get("newPassword");
            if (newPassword == null || newPassword.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "新密码不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            userService.adminResetPassword(id, newPassword.trim());
            response.put("success", true);
            response.put("message", "密码重置成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
