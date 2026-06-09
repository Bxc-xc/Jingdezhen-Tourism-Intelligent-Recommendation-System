package com.jdz.tourism.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jdz.tourism.entity.User;
import com.jdz.tourism.service.UserService;
import com.jdz.tourism.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setPhone("13800138000");
        // 假设User有Role枚举
        // testUser.setRole(User.Role.USER);
    }

    @Test
    void register_ShouldRegisterNewUser() throws Exception {
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("password123");
        newUser.setPhone("13800138001");

        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setUsername("newuser");
        savedUser.setPhone("13800138001");

        when(userService.register(any(User.class))).thenReturn(savedUser);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("注册成功"))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.username").value("newuser"));

        verify(userService).register(any(User.class));
    }

    @Test
    void register_ShouldHandleRegistrationFailure() throws Exception {
        User newUser = new User();
        newUser.setUsername("existinguser");
        newUser.setPassword("password123");

        when(userService.register(any(User.class)))
                .thenThrow(new RuntimeException("用户名已存在"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("用户名已存在"));

        verify(userService).register(any(User.class));
    }

    @Test
    void login_ShouldLoginSuccessfully() throws Exception {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", "testuser");
        loginData.put("password", "password123");

        when(userService.login("testuser", "password123")).thenReturn(testUser);
        when(jwtUtil.generateToken(eq(1L), eq("testuser"), anyString())).thenReturn("mock-jwt-token");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("登录成功"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.token").value("mock-jwt-token"));

        verify(userService).login("testuser", "password123");
        verify(jwtUtil).generateToken(eq(1L), eq("testuser"), anyString());
    }

    @Test
    void login_ShouldFailWithEmptyUsername() throws Exception {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", "");
        loginData.put("password", "password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginData)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("用户名和密码不能为空"));

        verify(userService, never()).login(anyString(), anyString());
    }

    @Test
    void login_ShouldFailWithNullPassword() throws Exception {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", "testuser");
        loginData.put("password", null);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginData)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("用户名和密码不能为空"));

        verify(userService, never()).login(anyString(), anyString());
    }

    @Test
    void login_ShouldFailWithInvalidCredentials() throws Exception {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", "testuser");
        loginData.put("password", "wrongpassword");

        when(userService.login("testuser", "wrongpassword"))
                .thenThrow(new RuntimeException("用户名或密码错误"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginData)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));

        verify(userService).login("testuser", "wrongpassword");
    }

    @Test
    void verifyToken_ShouldVerifyValidToken() throws Exception {
        String token = "valid-jwt-token";
        String authHeader = "Bearer " + token;

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractUsername(token)).thenReturn("testuser");
        when(jwtUtil.extractUserId(token)).thenReturn(1L);
        when(jwtUtil.extractRole(token)).thenReturn("USER");

        mockMvc.perform(post("/api/auth/verify")
                .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Token有效"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.role").value("USER"));

        verify(jwtUtil).validateToken(token);
        verify(jwtUtil).extractUsername(token);
        verify(jwtUtil).extractUserId(token);
        verify(jwtUtil).extractRole(token);
    }

    @Test
    void verifyToken_ShouldFailWithInvalidTokenFormat() throws Exception {
        String invalidAuthHeader = "InvalidFormat token";

        mockMvc.perform(post("/api/auth/verify")
                .header("Authorization", invalidAuthHeader))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Token格式错误"));

        verify(jwtUtil, never()).validateToken(anyString());
    }

    @Test
    void verifyToken_ShouldFailWithMissingAuthHeader() throws Exception {
        mockMvc.perform(post("/api/auth/verify"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void verifyToken_ShouldFailWithInvalidToken() throws Exception {
        String token = "invalid-jwt-token";
        String authHeader = "Bearer " + token;

        when(jwtUtil.validateToken(token)).thenReturn(false);

        mockMvc.perform(post("/api/auth/verify")
                .header("Authorization", authHeader))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Token无效或已过期"));

        verify(jwtUtil).validateToken(token);
        verify(jwtUtil, never()).extractUsername(anyString());
    }

    @Test
    void verifyToken_ShouldHandleException() throws Exception {
        String token = "problematic-token";
        String authHeader = "Bearer " + token;

        when(jwtUtil.validateToken(token)).thenThrow(new RuntimeException("Token解析错误"));

        mockMvc.perform(post("/api/auth/verify")
                .header("Authorization", authHeader))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Token验证失败: Token解析错误"));

        verify(jwtUtil).validateToken(token);
    }
}