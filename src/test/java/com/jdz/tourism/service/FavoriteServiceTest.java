package com.jdz.tourism.service;

import com.jdz.tourism.entity.Favorite;
import com.jdz.tourism.entity.ScenicSpot;
import com.jdz.tourism.entity.User;
import com.jdz.tourism.repository.FavoriteRepository;
import com.jdz.tourism.repository.ScenicSpotRepository;
import com.jdz.tourism.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock private FavoriteRepository favoriteRepository;
    @Mock private UserRepository userRepository;
    @Mock private ScenicSpotRepository scenicSpotRepository;
    @Mock private RealtimeDataService realtimeDataService;

    @InjectMocks
    private FavoriteService favoriteService;

    private User testUser;
    private ScenicSpot testScenic;
    private Favorite testFavorite;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("tourist1");

        testScenic = new ScenicSpot();
        testScenic.setId(1L);
        testScenic.setName("景德镇陶瓷博物馆");
        testScenic.setPrice(new BigDecimal("50.00"));

        testFavorite = new Favorite(testUser, testScenic);
    }

    // ---- 添加收藏 ----

    @Test
    void addFavorite_ShouldSucceed() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(scenicSpotRepository.findById(1L)).thenReturn(Optional.of(testScenic));
        when(favoriteRepository.existsByUserIdAndScenicSpotId(1L, 1L)).thenReturn(false);
        when(favoriteRepository.save(any(Favorite.class))).thenReturn(testFavorite);
        doNothing().when(realtimeDataService).pushFavoriteUpdate(any(), eq("create"));

        Favorite result = favoriteService.addFavorite(1L, 1L);

        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertEquals(testScenic, result.getScenicSpot());
        verify(favoriteRepository).save(any(Favorite.class));
        verify(realtimeDataService).pushFavoriteUpdate(any(), eq("create"));
    }

    @Test
    void addFavorite_ShouldThrowWhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> favoriteService.addFavorite(999L, 1L));
        assertEquals("用户不存在", ex.getMessage());
        verify(favoriteRepository, never()).save(any());
    }

    @Test
    void addFavorite_ShouldThrowWhenScenicNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(scenicSpotRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> favoriteService.addFavorite(1L, 999L));
        assertEquals("景点不存在", ex.getMessage());
        verify(favoriteRepository, never()).save(any());
    }

    @Test
    void addFavorite_ShouldThrowWhenAlreadyFavorited() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(scenicSpotRepository.findById(1L)).thenReturn(Optional.of(testScenic));
        when(favoriteRepository.existsByUserIdAndScenicSpotId(1L, 1L)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> favoriteService.addFavorite(1L, 1L));
        assertEquals("已经收藏过该景点", ex.getMessage());
        verify(favoriteRepository, never()).save(any());
    }

    // ---- 取消收藏 ----

    @Test
    void removeFavorite_ShouldSucceed() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(scenicSpotRepository.existsById(1L)).thenReturn(true);
        when(favoriteRepository.existsByUserIdAndScenicSpotId(1L, 1L)).thenReturn(true);
        doNothing().when(favoriteRepository).deleteByUserIdAndScenicSpotId(1L, 1L);
        doNothing().when(realtimeDataService).pushFavoriteUpdate(any(), eq("delete"));

        assertDoesNotThrow(() -> favoriteService.removeFavorite(1L, 1L));

        verify(favoriteRepository).deleteByUserIdAndScenicSpotId(1L, 1L);
        verify(realtimeDataService).pushFavoriteUpdate(any(), eq("delete"));
    }

    @Test
    void removeFavorite_ShouldThrowWhenUserNotFound() {
        when(userRepository.existsById(999L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> favoriteService.removeFavorite(999L, 1L));
        assertEquals("用户不存在", ex.getMessage());
        verify(favoriteRepository, never()).deleteByUserIdAndScenicSpotId(any(), any());
    }

    @Test
    void removeFavorite_ShouldThrowWhenNotFavorited() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(scenicSpotRepository.existsById(1L)).thenReturn(true);
        when(favoriteRepository.existsByUserIdAndScenicSpotId(1L, 1L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> favoriteService.removeFavorite(1L, 1L));
        assertEquals("收藏记录不存在", ex.getMessage());
    }

    // ---- 查询 ----

    @Test
    void isFavorited_ShouldReturnTrue() {
        when(favoriteRepository.existsByUserIdAndScenicSpotId(1L, 1L)).thenReturn(true);
        assertTrue(favoriteService.isFavorited(1L, 1L));
    }

    @Test
    void isFavorited_ShouldReturnFalse() {
        when(favoriteRepository.existsByUserIdAndScenicSpotId(1L, 2L)).thenReturn(false);
        assertFalse(favoriteService.isFavorited(1L, 2L));
    }

    @Test
    void getUserFavorites_ShouldReturnList() {
        when(favoriteRepository.findByUserId(1L)).thenReturn(Arrays.asList(testFavorite));

        List<Favorite> result = favoriteService.getUserFavorites(1L);

        assertEquals(1, result.size());
        verify(favoriteRepository).findByUserId(1L);
    }

    @Test
    void getUserFavoriteScenics_ShouldReturnScenicList() {
        when(favoriteRepository.findByUserId(1L)).thenReturn(Arrays.asList(testFavorite));

        List<ScenicSpot> result = favoriteService.getUserFavoriteScenics(1L);

        assertEquals(1, result.size());
        assertEquals("景德镇陶瓷博物馆", result.get(0).getName());
    }
}
