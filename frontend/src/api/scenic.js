import request from '../utils/request'

// 获取景点列表
export function getScenicList(params = {}) {
  return request.get('/scenic', { params })
}

// 获取景点详情
export function getScenicDetail(id) {
  return request.get(`/scenic/${id}`)
}

// 获取推荐景点
export function getRecommendScenic(params = {}) {
  return request.get('/scenic/recommend', { params })
}

// 获取热门景点
export function getPopularScenic(params = {}) {
  return request.get('/scenic/popular', { params })
}

// 搜索景点
export function searchScenic(keyword) {
  return request.get('/scenic/search', { params: { keyword } })
}

// 收藏景点
export function favoriteScenic(userId, scenicId) {
  return request.post('/scenic/favorite', { userId, scenicId })
}

// 取消收藏
export function unfavoriteScenic(userId, scenicId) {
  return request.delete(`/scenic/favorite/${scenicId}?userId=${userId}`)
}

// 获取收藏列表
export function getFavoriteList(userId) {
  return request.get(`/scenic/favorites?userId=${userId}`)
}

// 检查是否已收藏
export function checkFavoriteScenic(userId, scenicId) {
  return request.get(`/scenic/favorite/${scenicId}?userId=${userId}`)
}

// 删除景点
export function deleteScenic(id) {
  return request.delete(`/scenic/${id}`)
}

// 创建景点
export function createScenic(scenicData) {
  return request.post('/scenic', scenicData)
}

// 更新景点
export function updateScenic(id, scenicData) {
  return request.put(`/scenic/${id}`, scenicData)
}