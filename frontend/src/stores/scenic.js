import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getScenicList, getScenicDetail, getRecommendScenic, getPopularScenic, searchScenic } from '../api/scenic'

export const useScenicStore = defineStore('scenic', () => {
  const scenicList = ref([]) // 热门景点列表
  const currentScenic = ref(null)
  const recommendList = ref([]) // 推荐景点列表
  const loading = ref(false)

  // 获取热门景点列表
  const fetchScenicList = async (params = {}) => {
    loading.value = true
    try {
      const response = await getPopularScenic(params)
      scenicList.value = response.data || response
      return response
    } catch (error) {
      console.error('获取热门景点列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 获取景点详情
  const fetchScenicDetail = async (id) => {
    loading.value = true
    try {
      const response = await getScenicDetail(id)
      currentScenic.value = response.data || response
      return response
    } catch (error) {
      console.error('获取景点详情失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 获取推荐景点
  const fetchRecommendScenic = async (params = {}) => {
    loading.value = true
    try {
      const response = await getRecommendScenic(params)
      // 后端返回 { success, data: [...], total }
      const list = response?.data
      recommendList.value = Array.isArray(list) ? list : (list?.records || list?.list || [])
      return response
    } catch (error) {
      console.error('获取推荐景点失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 搜索景点（用于首页搜索）
  const fetchSearchScenic = async (keyword) => {
    if (!keyword) return []
    loading.value = true
    try {
      const response = await searchScenic(keyword)
      // 后端可能返回 {success,data} 或直接数组
      scenicList.value = response.data || response
      return scenicList.value
    } catch (error) {
      console.error('搜索景点失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  return {
    scenicList,
    currentScenic,
    recommendList,
    loading,
    fetchScenicList,
    fetchScenicDetail,
    fetchRecommendScenic,
    fetchSearchScenic
  }
})