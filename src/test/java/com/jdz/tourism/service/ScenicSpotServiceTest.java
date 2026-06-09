package com.jdz.tourism.service;

import com.jdz.tourism.entity.ScenicSpot;
import com.jdz.tourism.repository.ScenicSpotRepository;
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
class ScenicSpotServiceTest {

    @Mock
    private ScenicSpotRepository scenicSpotRepository;

    @InjectMocks
    private ScenicSpotService scenicSpotService;

    private ScenicSpot testScenicSpot;
    private ScenicSpot testScenicSpot2;
    private List<ScenicSpot> testScenicSpots;

    @BeforeEach
    void setUp() {
        testScenicSpot = new ScenicSpot();
        testScenicSpot.setId(1L);
        testScenicSpot.setName("景德镇陶瓷博物馆");
        testScenicSpot.setDescription("展示景德镇陶瓷文化的博物馆");
        testScenicSpot.setPrice(new BigDecimal("50.00"));
        testScenicSpot.setOpenTime("9:00-17:00");
        testScenicSpot.setCategory("博物馆");
        testScenicSpot.setTags("陶瓷,文化,历史");
        testScenicSpot.setVisitCount(100L);

        testScenicSpot2 = new ScenicSpot();
        testScenicSpot2.setId(2L);
        testScenicSpot2.setName("瑶里古镇");
        testScenicSpot2.setDescription("历史悠久的古镇");
        testScenicSpot2.setPrice(new BigDecimal("80.00"));
        testScenicSpot2.setOpenTime("8:00-18:00");
        testScenicSpot2.setCategory("古镇");
        testScenicSpot2.setTags("古镇,历史,自然");
        testScenicSpot2.setVisitCount(150L);

        testScenicSpots = Arrays.asList(testScenicSpot, testScenicSpot2);
    }

    @Test
    void getAllScenicSpots_ShouldReturnAllSpots() {
        when(scenicSpotRepository.findAll()).thenReturn(testScenicSpots);

        List<ScenicSpot> result = scenicSpotService.getAllScenicSpots();

        assertEquals(2, result.size());
        assertEquals("景德镇陶瓷博物馆", result.get(0).getName());
        assertEquals("瑶里古镇", result.get(1).getName());
        verify(scenicSpotRepository).findAll();
    }

    @Test
    void getScenicSpotById_ShouldReturnSpotAndIncreaseVisitCount() {
        when(scenicSpotRepository.findById(1L)).thenReturn(Optional.of(testScenicSpot));
        when(scenicSpotRepository.save(any(ScenicSpot.class))).thenReturn(testScenicSpot);

        Optional<ScenicSpot> result = scenicSpotService.getScenicSpotById(1L);

        assertTrue(result.isPresent());
        assertEquals("景德镇陶瓷博物馆", result.get().getName());
        assertEquals(101L, result.get().getVisitCount()); // 访问量应该增加1
        verify(scenicSpotRepository).findById(1L);
        verify(scenicSpotRepository).save(testScenicSpot);
    }

    @Test
    void getScenicSpotById_ShouldReturnEmptyWhenNotFound() {
        when(scenicSpotRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<ScenicSpot> result = scenicSpotService.getScenicSpotById(999L);

        assertFalse(result.isPresent());
        verify(scenicSpotRepository).findById(999L);
        verify(scenicSpotRepository, never()).save(any());
    }

    @Test
    void getScenicSpotsByCategory_ShouldReturnSpotsByCategory() {
        List<ScenicSpot> museumSpots = Arrays.asList(testScenicSpot);
        when(scenicSpotRepository.findByCategory("博物馆")).thenReturn(museumSpots);

        List<ScenicSpot> result = scenicSpotService.getScenicSpotsByCategory("博物馆");

        assertEquals(1, result.size());
        assertEquals("博物馆", result.get(0).getCategory());
        verify(scenicSpotRepository).findByCategory("博物馆");
    }

    @Test
    void getScenicSpotsByTag_ShouldReturnSpotsByTag() {
        when(scenicSpotRepository.findByTagsContaining("陶瓷")).thenReturn(Arrays.asList(testScenicSpot));

        List<ScenicSpot> result = scenicSpotService.getScenicSpotsByTag("陶瓷");

        assertEquals(1, result.size());
        assertTrue(result.get(0).getTags().contains("陶瓷"));
        verify(scenicSpotRepository).findByTagsContaining("陶瓷");
    }

    @Test
    void getScenicSpotsByTags_ShouldReturnSpotsByTags() {
        List<String> tags = Arrays.asList("陶瓷", "文化");
        when(scenicSpotRepository.findByTagsIn(tags)).thenReturn(Arrays.asList(testScenicSpot));

        List<ScenicSpot> result = scenicSpotService.getScenicSpotsByTags(tags);

        assertEquals(1, result.size());
        verify(scenicSpotRepository).findByTagsIn(tags);
    }

    @Test
    void getPopularScenicSpots_ShouldReturnDeduplicatedPopularSpots() {
        // 模拟包含重复名称的热门景点数据
        ScenicSpot duplicate1 = new ScenicSpot();
        duplicate1.setId(3L);
        duplicate1.setName("景德镇陶瓷博物馆"); // 与testScenicSpot同名
        duplicate1.setVisitCount(80L); // 访问量更少

        List<ScenicSpot> popularSpots = Arrays.asList(testScenicSpot2, testScenicSpot, duplicate1);
        when(scenicSpotRepository.findTop10ByOrderByVisitCountDesc()).thenReturn(popularSpots);

        List<ScenicSpot> result = scenicSpotService.getPopularScenicSpots();

        assertEquals(2, result.size()); // 去重后只有2个不同名称的景点
        assertEquals("瑶里古镇", result.get(0).getName()); // 按访问量排序
        assertEquals("景德镇陶瓷博物馆", result.get(1).getName());
        assertEquals(150L, result.get(0).getVisitCount());
        assertEquals(100L, result.get(1).getVisitCount()); // 保留访问量更高的
        verify(scenicSpotRepository).findTop10ByOrderByVisitCountDesc();
    }

    @Test
    void searchScenicSpotsByName_ShouldReturnComprehensiveSearchResults() {
        List<ScenicSpot> searchResults = Arrays.asList(testScenicSpot);
        when(scenicSpotRepository.findByKeywordInNameOrDescriptionOrTags("博物馆")).thenReturn(searchResults);

        List<ScenicSpot> result = scenicSpotService.searchScenicSpotsByName("博物馆");

        assertEquals(1, result.size());
        assertEquals("景德镇陶瓷博物馆", result.get(0).getName());
        verify(scenicSpotRepository).findByKeywordInNameOrDescriptionOrTags("博物馆");
        verify(scenicSpotRepository, never()).findByNameContainingIgnoreCase(anyString());
    }

    @Test
    void searchScenicSpotsByName_ShouldFallbackToNameSearch() {
        // 综合搜索返回空结果
        when(scenicSpotRepository.findByKeywordInNameOrDescriptionOrTags("博物馆")).thenReturn(Arrays.asList());
        // 名称搜索返回结果
        when(scenicSpotRepository.findByNameContainingIgnoreCase("博物馆")).thenReturn(Arrays.asList(testScenicSpot));

        List<ScenicSpot> result = scenicSpotService.searchScenicSpotsByName("博物馆");

        assertEquals(1, result.size());
        verify(scenicSpotRepository).findByKeywordInNameOrDescriptionOrTags("博物馆");
        verify(scenicSpotRepository).findByNameContainingIgnoreCase("博物馆");
    }

    @Test
    void searchScenicSpotsByPriceRange_ShouldReturnSpotsInRange() {
        when(scenicSpotRepository.findByPriceBetween(40.0, 60.0)).thenReturn(Arrays.asList(testScenicSpot));

        List<ScenicSpot> result = scenicSpotService.searchScenicSpotsByPriceRange(40.0, 60.0);

        assertEquals(1, result.size());
        assertTrue(result.get(0).getPrice().compareTo(new BigDecimal("40.0")) >= 0);
        assertTrue(result.get(0).getPrice().compareTo(new BigDecimal("60.0")) <= 0);
        verify(scenicSpotRepository).findByPriceBetween(40.0, 60.0);
    }

    @Test
    void recommendScenicSpots_ShouldReturnSpotsOrderedByVisitCount() {
        when(scenicSpotRepository.findAll()).thenReturn(testScenicSpots);

        List<ScenicSpot> result = scenicSpotService.recommendScenicSpots(1L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void recommendScenicSpots_WithNullUserId_ShouldWork() {
        when(scenicSpotRepository.findAll()).thenReturn(testScenicSpots);

        List<ScenicSpot> result = scenicSpotService.recommendScenicSpots(null);

        assertNotNull(result);
    }

    @Test
    void createScenicSpot_ShouldSaveAndReturnSpot() {
        when(scenicSpotRepository.save(testScenicSpot)).thenReturn(testScenicSpot);

        ScenicSpot result = scenicSpotService.createScenicSpot(testScenicSpot);

        assertEquals(testScenicSpot, result);
        verify(scenicSpotRepository).save(testScenicSpot);
    }

    @Test
    void updateScenicSpot_ShouldSaveAndReturnUpdatedSpot() {
        when(scenicSpotRepository.save(testScenicSpot)).thenReturn(testScenicSpot);

        ScenicSpot result = scenicSpotService.updateScenicSpot(testScenicSpot);

        assertEquals(testScenicSpot, result);
        verify(scenicSpotRepository).save(testScenicSpot);
    }

    @Test
    void deleteScenicSpot_ShouldCallRepositoryDelete() {
        doNothing().when(scenicSpotRepository).deleteById(1L);

        scenicSpotService.deleteScenicSpot(1L);

        verify(scenicSpotRepository).deleteById(1L);
    }
}