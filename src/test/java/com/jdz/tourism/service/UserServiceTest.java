package com.jdz.tourism.service;

import com.jdz.tourism.entity.User;
import com.jdz.tourism.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RealtimeDataService realtimeDataService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setPhone("13800138000");
    }

    @Test
    void register_ShouldRegisterNewUser() {
        // 准备测试数据
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("rawPassword");
        newUser.setPhone("13800138001");

        // Mock行为
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.findByPhone("13800138001")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        doNothing().when(realtimeDataService).pushUserUpdate(any(User.class), eq("create"));

        // 执行测试
        User result = userService.register(newUser);

        // 验证结果
        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).findByPhone("13800138001");
        verify(passwordEncoder).encode("rawPassword");
        verify(userRepository).save(newUser);
        verify(realtimeDataService).pushUserUpdate(newUser, "create");
        assertEquals("encodedPassword", newUser.getPassword());
    }

    @Test
    void register_ShouldThrowExceptionWhenUsernameExists() {
        User newUser = new User();
        newUser.setUsername("existinguser");

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.register(newUser);
        });

        assertEquals("用户名已存在", exception.getMessage());
        verify(userRepository).existsByUsername("existinguser");
        verify(userRepository, never()).save(any());
        verify(realtimeDataService, never()).pushUserUpdate(any(), any());
    }

    @Test
    void register_ShouldThrowExceptionWhenPhoneExists() {
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPhone("13800138000");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.findByPhone("13800138000")).thenReturn(Optional.of(testUser));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.register(newUser);
        });

        assertEquals("手机号已存在", exception.getMessage());
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).findByPhone("13800138000");
        verify(userRepository, never()).save(any());
        verify(realtimeDataService, never()).pushUserUpdate(any(), any());
    }

    @Test
    void register_ShouldAllowNullPhone() {
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("rawPassword");
        newUser.setPhone(null);

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        doNothing().when(realtimeDataService).pushUserUpdate(any(User.class), eq("create"));

        User result = userService.register(newUser);

        assertNotNull(result);
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository, never()).findByPhone(any());
        verify(userRepository).save(newUser);
    }

    @Test
    void login_ShouldReturnUserWhenCredentialsAreValid() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);

        User result = userService.login("testuser", "password123");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password123", testUser.getPassword());
    }

    @Test
    void login_ShouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.login("nonexistent", "password");
        });

        assertEquals("用户名或密码错误", exception.getMessage());
        verify(userRepository).findByUsername("nonexistent");
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void login_ShouldThrowExceptionWhenPasswordIsWrong() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", testUser.getPassword())).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.login("testuser", "wrongpassword");
        });

        assertEquals("用户名或密码错误", exception.getMessage());
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("wrongpassword", testUser.getPassword());
    }

    @Test
    void findById_ShouldReturnUserWhenExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(userRepository).findById(1L);
    }

    @Test
    void findById_ShouldReturnEmptyWhenNotExists() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<User> result = userService.findById(999L);

        assertFalse(result.isPresent());
        verify(userRepository).findById(999L);
    }

    @Test
    void findByUsername_ShouldReturnUserWhenExists() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findByUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void findByUsername_ShouldReturnEmptyWhenNotExists() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        Optional<User> result = userService.findByUsername("nonexistent");

        assertFalse(result.isPresent());
        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    void updateUser_ShouldSaveAndReturnUser() {
        when(userRepository.save(testUser)).thenReturn(testUser);

        User result = userService.updateUser(testUser);

        assertEquals(testUser, result);
        verify(userRepository).save(testUser);
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");

        List<User> users = Arrays.asList(testUser, user2);
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        assertEquals("user2", result.get(1).getUsername());
        verify(userRepository).findAll();
    }

    @Test
    void deleteUser_ShouldCallRepositoryDelete() {
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void getUserPreferenceTags_ShouldReturnDefaultTags() {
        List<String> result = userService.getUserPreferenceTags(1L);

        assertEquals(3, result.size());
        assertTrue(result.contains("陶瓷"));
        assertTrue(result.contains("博物馆"));
        assertTrue(result.contains("文化"));
    }
}