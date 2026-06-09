package com.jdz.tourism.service;

import com.jdz.tourism.entity.User;
import com.jdz.tourism.repository.UserRepository;
import com.jdz.tourism.repository.OrderRepository;
import com.jdz.tourism.repository.FavoriteRepository;
import com.jdz.tourism.repository.MerchantFavoriteRepository;
import com.jdz.tourism.repository.GroupBuyOrderRepository;
import com.jdz.tourism.service.TravelPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RealtimeDataService realtimeDataService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private MerchantFavoriteRepository merchantFavoriteRepository;

    @Autowired
    private GroupBuyOrderRepository groupBuyOrderRepository;

    @Autowired(required = false)
    @Lazy
    private TravelPlanService travelPlanService;

    @Autowired(required = false)
    @Lazy
    private ReviewService reviewService;

    public User register(User user) {
        return register(user, null);
    }

    @Transactional
    public User register(User user, Map<String, Object> extraData) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        if (user.getPhone() != null && userRepository.findByPhone(user.getPhone()).isPresent()) {
            throw new RuntimeException("手机号已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getCreatedAt() == null) {
            java.time.ZoneId zoneId = java.time.ZoneId.of("Asia/Shanghai");
            user.setCreatedAt(java.time.LocalDateTime.now(zoneId));
        }
        User savedUser = userRepository.save(user);
        if (user.getRole() == User.UserRole.MERCHANT && extraData != null) {
            String category = extraData.get("merchantCategory") != null ?
                    extraData.get("merchantCategory").toString() : "OTHER";
            String shopName = extraData.get("shopName") != null ?
                    extraData.get("shopName").toString() : user.getUsername() + "的店铺";
            String description = extraData.get("description") != null ?
                    extraData.get("description").toString() : "";
            try {
                merchantService.createMerchant(savedUser.getId(), shopName, description, category);
            } catch (Exception e) {
                System.err.println("创建商家失败: " + e.getMessage());
            }
        }
        realtimeDataService.pushUserUpdate(savedUser, "create");
        return savedUser;
    }

    public User login(String username, String password) {
        System.out.println("=== UserService.login 开始 ===");
        System.out.println("用户名: " + username);
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String storedPassword = user.getPassword();
            if (storedPassword == null || storedPassword.isEmpty()) {
                throw new RuntimeException("用户名或密码错误");
            }
            boolean isPlainText = !storedPassword.startsWith("$2a$") &&
                    !storedPassword.startsWith("$2b$") &&
                    storedPassword.length() < 60;
            if (isPlainText) {
                if (storedPassword.equals(password)) {
                    user.setPassword(passwordEncoder.encode(password));
                    userRepository.save(user);
                    return user;
                } else {
                    throw new RuntimeException("用户名或密码错误");
                }
            } else {
                if (passwordEncoder.matches(password, storedPassword)) {
                    return user;
                } else {
                    throw new RuntimeException("用户名或密码错误");
                }
            }
        }
        throw new RuntimeException("用户名或密码错误");
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User updateUser(User user) {
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        User user = userOpt.get();
        String storedPassword = user.getPassword();
        if (storedPassword == null || storedPassword.isEmpty()) {
            throw new RuntimeException("原密码错误");
        }
        boolean isPlainText = !storedPassword.startsWith("$2a$") &&
                !storedPassword.startsWith("$2b$") &&
                storedPassword.length() < 60;
        boolean passwordMatches;
        if (isPlainText) {
            passwordMatches = storedPassword.equals(oldPassword);
        } else {
            passwordMatches = passwordEncoder.matches(oldPassword, storedPassword);
        }
        if (!passwordMatches) {
            throw new RuntimeException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void resetPasswordByPhone(String username, String phone, String newPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户名不存在");
        }
        User user = userOpt.get();
        if (user.getPhone() == null || !user.getPhone().equals(phone)) {
            throw new RuntimeException("手机号与注册时不一致，请确认后重试");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("用户不存在");
        }
        // 先处理该用户关联的商家（包括已逻辑删除的），清理商家子表后物理删除 merchant 记录
        try {
            merchantService.deleteAllByUserId(id);
        } catch (Exception e) {
            System.err.println("删除用户关联商家失败: " + e.getMessage());
        }
        // 删除用户的行程规划（含版本历史）
        if (travelPlanService != null) {
            try {
                travelPlanService.deletePlansByUserId(id);
            } catch (Exception e) {
                System.err.println("删除用户行程失败: " + e.getMessage());
            }
        }
        orderRepository.deleteAll(orderRepository.findByUserId(id));
        groupBuyOrderRepository.deleteAll(groupBuyOrderRepository.findByUserIdOrderByCreatedAtDesc(id));
        favoriteRepository.deleteAll(favoriteRepository.findByUserId(id));
        merchantFavoriteRepository.deleteAll(merchantFavoriteRepository.findByUserId(id));
        if (reviewService != null) {
            reviewService.getReviewsByUserId(id).forEach(r -> reviewService.deleteReview(r.getId()));
        }
        userRepository.deleteById(id);
    }

    public List<String> getUserPreferenceTags(Long userId) {
        return List.of("陶瓷", "博物馆", "文化");
    }

    /**
     * 管理员直接重置用户密码（无需旧密码）
     */
    @Transactional
    public void adminResetPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
