package com.jdz.tourism.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jdz.tourism.entity.ScenicSpot;
import com.jdz.tourism.service.ScenicSpotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ScenicSpotController.class)
class ScenicSpotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScenicSpotService scenicSpotService;

    @Autowired
    private ObjectMapper objectMapper;

    private ScenicSpot testScenicSpot;
    private List<ScenicSpot> testScenicSpots;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testScenicSpot = new ScenicSpot();
        testScenicSpot.setId(1L);
        testScenicSpot.setName("景德镇陶瓷博物馆");
        testScenicSpot.setDescription("展示景德镇陶瓷文化的博物馆");
        testScenicSpot.setPrice(new BigDecimal("50.00"));
        testScenicSpot.setOpenTime("9:00-17:00");
        testScenicSpot.setCategory("博物馆");
        testScenicSpot.setTags("陶瓷,文化,历史");
        testScenicSpot.setVisitCount(100L);

        ScenicSpot testScenicSpot2 = new ScenicSpot();
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
    void getAllScenicSpots_ShouldReturnAllSpots() throws Exception {
        when(scenicSpotService.getAllScenicSpots()).thenReturn(testScenicSpots);

        mockMvc.perform(get("/api/scenic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("景德镇陶瓷博物馆"))
                .andExpect(jsonPath("$.data[1].name").value("瑶里古镇"));

        verify(scenicSpotService).getAllScenicSpots();
    }

    @Test
    void getAllScenicSpots_ShouldHandleException() throws Exception {
        when(scenicSpotService.getAllScenicSpots()).thenThrow(new RuntimeException("数据库连接失败"));

        mockMvc.perform(get("/api/scenic"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("数据库连接失败"));

        verify(scenicSpotService).getAllScenicSpots();
    }

    @Test
    void getScenicSpotById_ShouldReturnSpot() throws Exception {
        when(scenicSpotService.getScenicSpotById(1L)).thenReturn(Optional.of(testScenicSpot));

        mockMvc.perform(get("/api/scenic/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("景德镇陶瓷博物馆"));

        verify(scenicSpotService).getScenicSpotById(1L);
    }

    @Test
    void getScenicSpotById_ShouldReturnNotFound() throws Exception {
        when(scenicSpotService.getScenicSpotById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/scenic/999"))
                .andExpect(status().isNotFound());

        verify(scenicSpotService).getScenicSpotById(999L);
    }

    @Test
    void getRecommendScenicSpots_ShouldReturnRecommendations() throws Exception {
        when(scenicSpotService.recommendScenicSpots(1L)).thenReturn(testScenicSpots);

        mockMvc.perform(get("/api/scenic/recommend").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(scenicSpotService).recommendScenicSpots(1L);
    }

    @Test
    void getRecommendScenicSpots_WithoutUserId_ShouldWork() throws Exception {
        when(scenicSpotService.recommendScenicSpots(null)).thenReturn(testScenicSpots);

        mockMvc.perform(get("/api/scenic/recommend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(scenicSpotService).recommendScenicSpots(null);
    }

    @Test
    void getPopularScenicSpots_ShouldReturnPopularSpots() throws Exception {
        when(scenicSpotService.getPopularScenicSpots()).thenReturn(testScenicSpots);

        mockMvc.perform(get("/api/scenic/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        verify(scenicSpotService).getPopularScenicSpots();
    }

    @Test
    void getScenicSpotsByCategory_ShouldReturnSpotsByCategory() throws Exception {
        List<ScenicSpot> museumSpots = Arrays.asList(testScenicSpot);
        when(scenicSpotService.getScenicSpotsByCategory("博物馆")).thenReturn(museumSpots);

        mockMvc.perform(get("/api/scenic/category/博物馆"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].category").value("博物馆"));

        verify(scenicSpotService).getScenicSpotsByCategory("博物馆");
    }

    @Test
    void searchScenicSpotsByTag_ShouldReturnSpotsByTag() throws Exception {
        when(scenicSpotService.getScenicSpotsByTag("陶瓷")).thenReturn(Arrays.asList(testScenicSpot));

        mockMvc.perform(get("/api/scenic/search/tag").param("tag", "陶瓷"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(scenicSpotService).getScenicSpotsByTag("陶瓷");
    }

    @Test
    void searchScenicSpotsByName_ShouldReturnSpotsByName() throws Exception {
        when(scenicSpotService.searchScenicSpotsByName("博物馆")).thenReturn(Arrays.asList(testScenicSpot));

        mockMvc.perform(get("/api/scenic/search/name").param("name", "博物馆"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.message").value("搜索完成，找到 1 个结果"));

        verify(scenicSpotService).searchScenicSpotsByName("博物馆");
    }

    @Test
    void searchScenicSpots_ShouldReturnComprehensiveSearch() throws Exception {
        when(scenicSpotService.searchScenicSpotsByName("陶瓷")).thenReturn(Arrays.asList(testScenicSpot));

        mockMvc.perform(get("/api/scenic/search").param("keyword", "陶瓷"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("综合搜索完成，找到 1 个结果"));

        verify(scenicSpotService).searchScenicSpotsByName("陶瓷");
    }

    @Test
    void searchScenicSpotsByPriceRange_ShouldReturnSpotsInRange() throws Exception {
        when(scenicSpotService.searchScenicSpotsByPriceRange(40.0, 60.0))
                .thenReturn(Arrays.asList(testScenicSpot));

        mockMvc.perform(get("/api/scenic/search/price")
                .param("minPrice", "40.0")
                .param("maxPrice", "60.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(scenicSpotService).searchScenicSpotsByPriceRange(40.0, 60.0);
    }

    @Test
    void createScenicSpot_ShouldCreateNewSpot() throws Exception {
        ScenicSpot newSpot = new ScenicSpot();
        newSpot.setName("新景点");
        newSpot.setDescription("新的景点描述");
        newSpot.setPrice(new BigDecimal("30.00"));

        ScenicSpot savedSpot = new ScenicSpot();
        savedSpot.setId(3L);
        savedSpot.setName("新景点");
        savedSpot.setDescription("新的景点描述");
        savedSpot.setPrice(new BigDecimal("30.00"));

        when(scenicSpotService.createScenicSpot(any(ScenicSpot.class))).thenReturn(savedSpot);

        mockMvc.perform(post("/api/scenic")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newSpot)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("创建成功"))
                .andExpect(jsonPath("$.data.id").value(3));

        verify(scenicSpotService).createScenicSpot(any(ScenicSpot.class));
    }

    @Test
    void updateScenicSpot_ShouldUpdateExistingSpot() throws Exception {
        ScenicSpot updatedSpot = new ScenicSpot();
        updatedSpot.setId(1L);
        updatedSpot.setName("更新后的景点");
        updatedSpot.setDescription("更新后的描述");

        when(scenicSpotService.updateScenicSpot(any(ScenicSpot.class))).thenReturn(updatedSpot);

        mockMvc.perform(put("/api/scenic/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedSpot)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("更新成功"))
                .andExpect(jsonPath("$.data.name").value("更新后的景点"));

        verify(scenicSpotService).updateScenicSpot(any(ScenicSpot.class));
    }

    @Test
    void deleteScenicSpot_ShouldDeleteSpot() throws Exception {
        doNothing().when(scenicSpotService).deleteScenicSpot(1L);

        mockMvc.perform(delete("/api/scenic/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("删除成功"));

        verify(scenicSpotService).deleteScenicSpot(1L);
    }

    @Test
    void deleteScenicSpot_ShouldHandleException() throws Exception {
        doThrow(new RuntimeException("删除失败")).when(scenicSpotService).deleteScenicSpot(999L);

        mockMvc.perform(delete("/api/scenic/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("删除失败"));

        verify(scenicSpotService).deleteScenicSpot(999L);
    }
}