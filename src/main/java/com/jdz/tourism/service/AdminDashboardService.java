package com.jdz.tourism.service;

import com.jdz.tourism.dto.dashboard.DashboardResponseDto;
import com.jdz.tourism.dto.dashboard.MerchantApplicationItemDto;
import com.jdz.tourism.dto.dashboard.StatCardsDto;
import com.jdz.tourism.dto.dashboard.TrendDataDto;
import com.jdz.tourism.dto.dashboard.UserTrendDto;
import com.jdz.tourism.entity.MerchantApplication;
import com.jdz.tourism.entity.Order;
import com.jdz.tourism.entity.User;
import com.jdz.tourism.repository.MerchantApplicationRepository;
import com.jdz.tourism.repository.OrderRepository;
import com.jdz.tourism.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class AdminDashboardService {
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("MM-dd", Locale.SIMPLIFIED_CHINESE);
    private static final String[] MOCK_TYPES = {"民宿", "餐饮", "陶瓷工坊", "旅行社", "文创市集"};

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final MerchantApplicationRepository merchantApplicationRepository;
    private final boolean useMock;

    public AdminDashboardService(
            OrderRepository orderRepository,
            UserRepository userRepository,
            MerchantApplicationRepository merchantApplicationRepository,
            @Value("${dashboard.use-mock:true}") boolean useMock) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.merchantApplicationRepository = merchantApplicationRepository;
        this.useMock = useMock;
    }

    public DashboardResponseDto getDashboardData() {
        if (useMock) {
            return buildMockDashboardData();
        }
        return buildRealDashboardData();
    }

    private DashboardResponseDto buildRealDashboardData() {
        List<Order> orders = orderRepository.findAll();
        List<User> users = userRepository.findAll();
        List<MerchantApplication> pendingApplications =
                merchantApplicationRepository.findByStatus(MerchantApplication.ApplicationStatus.PENDING);

        TrendDataDto trend30 = buildTrendFromEntities(30, orders, users);
        TrendDataDto trend7 = sliceTrend(trend30, 7);

        int todayOrders = trend30.getOrderData().isEmpty() ? 0 : trend30.getOrderData().get(trend30.getOrderData().size() - 1);
        int yesterdayOrders = trend30.getOrderData().size() >= 2
                ? trend30.getOrderData().get(trend30.getOrderData().size() - 2)
                : todayOrders;
        int todayUsers = trend30.getUserData().isEmpty() ? 0 : trend30.getUserData().get(trend30.getUserData().size() - 1);
        int yesterdayUsers = trend30.getUserData().size() >= 2
                ? trend30.getUserData().get(trend30.getUserData().size() - 2)
                : todayUsers;

        StatCardsDto statCards = new StatCardsDto();
        statCards.setTotalUsers(users.size());
        statCards.setTotalOrders(orders.size());
        statCards.setTodayNewUsers(todayUsers);
        statCards.setTodayOrders(todayOrders);
        statCards.setOrderTrend(percentDelta(todayOrders, yesterdayOrders));
        statCards.setUserTrend(percentDelta(todayUsers, yesterdayUsers));

        DashboardResponseDto dto = new DashboardResponseDto();
        dto.setStatCards(statCards);
        dto.setOrderTrend30d(trend30);
        dto.setOrderTrend7d(trend7);

        UserTrendDto userTrend = new UserTrendDto();
        userTrend.setDates(trend30.getDates());
        userTrend.setUserData(trend30.getUserData());
        dto.setUserTrend30d(userTrend);

        dto.setMerchantApplications(
                pendingApplications.stream()
                        .sorted(Comparator.comparing(MerchantApplication::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                        .limit(5)
                        .map(this::mapMerchantApplication)
                        .collect(Collectors.toList())
        );
        return dto;
    }

    private TrendDataDto buildTrendFromEntities(int days, List<Order> orders, List<User> users) {
        Map<LocalDate, Integer> orderCountMap = new HashMap<>();
        for (Order order : orders) {
            if (order.getOrderTime() == null) {
                continue;
            }
            LocalDate day = order.getOrderTime().toLocalDate();
            orderCountMap.put(day, orderCountMap.getOrDefault(day, 0) + 1);
        }

        Map<LocalDate, Integer> userCountMap = new HashMap<>();
        for (User user : users) {
            if (user.getCreatedAt() == null) {
                continue;
            }
            LocalDate day = user.getCreatedAt().toLocalDate();
            userCountMap.put(day, userCountMap.getOrDefault(day, 0) + 1);
        }

        LocalDate today = LocalDate.now();
        List<String> dates = new ArrayList<>();
        List<Integer> orderData = new ArrayList<>();
        List<Integer> userData = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            dates.add(day.format(DAY_FORMATTER));
            orderData.add(orderCountMap.getOrDefault(day, 0));
            userData.add(userCountMap.getOrDefault(day, 0));
        }

        TrendDataDto trend = new TrendDataDto();
        trend.setDates(dates);
        trend.setOrderData(orderData);
        trend.setUserData(userData);
        return trend;
    }

    private DashboardResponseDto buildMockDashboardData() {
        Random random = new Random();
        TrendDataDto trend30 = buildMockTrend(30, random);
        TrendDataDto trend7 = sliceTrend(trend30, 7);

        StatCardsDto statCards = new StatCardsDto();
        long totalUsers = 26000L + trend30.getUserData().stream().mapToLong(Integer::longValue).sum();
        long totalOrders = 138000L + trend30.getOrderData().stream().mapToLong(Integer::longValue).sum();
        int todayUsers = trend30.getUserData().get(trend30.getUserData().size() - 1);
        int yesterdayUsers = trend30.getUserData().get(trend30.getUserData().size() - 2);
        int todayOrders = trend30.getOrderData().get(trend30.getOrderData().size() - 1);
        int yesterdayOrders = trend30.getOrderData().get(trend30.getOrderData().size() - 2);

        statCards.setTotalUsers(totalUsers);
        statCards.setTotalOrders(totalOrders);
        statCards.setTodayNewUsers(todayUsers);
        statCards.setTodayOrders(todayOrders);
        statCards.setUserTrend(percentDelta(todayUsers, yesterdayUsers));
        statCards.setOrderTrend(percentDelta(todayOrders, yesterdayOrders));

        DashboardResponseDto dto = new DashboardResponseDto();
        dto.setStatCards(statCards);
        dto.setOrderTrend7d(trend7);
        dto.setOrderTrend30d(trend30);

        UserTrendDto userTrend = new UserTrendDto();
        userTrend.setDates(trend30.getDates());
        userTrend.setUserData(trend30.getUserData());
        dto.setUserTrend30d(userTrend);
        dto.setMerchantApplications(buildMockApplications(random));
        return dto;
    }

    private TrendDataDto buildMockTrend(int days, Random random) {
        List<String> dates = new ArrayList<>();
        List<Integer> orderData = new ArrayList<>();
        List<Integer> userData = new ArrayList<>();
        LocalDate today = LocalDate.now();
        int orderSeed = 185 + random.nextInt(40);
        int userSeed = 52 + random.nextInt(16);

        for (int i = days - 1; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            int dayOfWeek = day.getDayOfWeek().getValue();
            double weekendBoost = (dayOfWeek == 6 || dayOfWeek == 7) ? 1.16 : 1.0;
            double campaignBoost = (i == 4 || i == 13) ? 1.24 : 1.0;

            int dailyOrders = (int) Math.max(40, Math.round((orderSeed + random.nextInt(90) - 40) * weekendBoost * campaignBoost));
            int dailyUsers = (int) Math.max(8, Math.round((userSeed + random.nextInt(20) - 8) * weekendBoost));

            dates.add(day.format(DAY_FORMATTER));
            orderData.add(dailyOrders);
            userData.add(dailyUsers);

            orderSeed = Math.max(120, orderSeed + random.nextInt(8) - 3);
            userSeed = Math.max(35, userSeed + random.nextInt(5) - 1);
        }

        TrendDataDto trend = new TrendDataDto();
        trend.setDates(dates);
        trend.setOrderData(orderData);
        trend.setUserData(userData);
        return trend;
    }

    private TrendDataDto sliceTrend(TrendDataDto source, int lastDays) {
        int size = source.getDates().size();
        int start = Math.max(0, size - lastDays);
        TrendDataDto trend = new TrendDataDto();
        trend.setDates(new ArrayList<>(source.getDates().subList(start, size)));
        trend.setOrderData(new ArrayList<>(source.getOrderData().subList(start, size)));
        trend.setUserData(new ArrayList<>(source.getUserData().subList(start, size)));
        return trend;
    }

    private List<MerchantApplicationItemDto> buildMockApplications(Random random) {
        List<MerchantApplicationItemDto> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            MerchantApplicationItemDto item = new MerchantApplicationItemDto();
            item.setId((long) (i + 1));
            item.setShopName("商家申请-" + (100 + i));
            item.setType(MOCK_TYPES[random.nextInt(MOCK_TYPES.length)]);
            item.setSubmittedAt((2 + random.nextInt(22)) + "小时前");
            item.setStatus(MerchantApplication.ApplicationStatus.PENDING.name());
            list.add(item);
        }
        return list;
    }

    private MerchantApplicationItemDto mapMerchantApplication(MerchantApplication application) {
        MerchantApplicationItemDto item = new MerchantApplicationItemDto();
        item.setId(application.getId());
        String username = application.getUser() != null ? application.getUser().getUsername() : null;
        item.setShopName((username == null || username.isBlank()) ? "商家申请-" + application.getId() : username);
        item.setType(resolveTypeByDescription(application.getDescription()));
        item.setSubmittedAt(formatSubmittedAt(application.getCreatedAt()));
        item.setStatus(application.getStatus().name());
        return item;
    }

    private String resolveTypeByDescription(String description) {
        if (description == null || description.isBlank()) {
            return "综合商家";
        }
        if (description.contains("民宿") || description.contains("酒店")) {
            return "民宿";
        }
        if (description.contains("餐") || description.contains("美食")) {
            return "餐饮";
        }
        if (description.contains("陶") || description.contains("工坊")) {
            return "陶瓷工坊";
        }
        if (description.contains("旅行") || description.contains("路线")) {
            return "旅行社";
        }
        return "文创市集";
    }

    private String formatSubmittedAt(LocalDateTime createdAt) {
        if (createdAt == null) {
            return "未知";
        }
        Duration duration = Duration.between(createdAt, LocalDateTime.now());
        long hours = Math.max(1, duration.toHours());
        if (hours < 24) {
            return hours + "小时前";
        }
        long days = Math.max(1, duration.toDays());
        return days + "天前";
    }

    private double percentDelta(int current, int previous) {
        if (previous <= 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        double delta = ((double) (current - previous) / previous) * 100.0;
        return Math.round(delta * 10.0) / 10.0;
    }
}
