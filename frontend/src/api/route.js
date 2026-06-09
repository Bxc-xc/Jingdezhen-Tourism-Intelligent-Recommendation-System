import request from '../utils/request'

// 获取推荐路线列表
export function getRecommendRoutes(params = {}) {
  return request.get('/route/recommend', { params })
}

// 获取路线详情
export function getRouteDetail(id) {
  return request.get(`/route/${id}`)
}

// 获取全部路线（管理员用）
export function getAllRoutes() {
  return request.get('/route')
}

// 创建路线
export function createRoute(data) {
  return request.post('/route', data)
}

// 更新路线
export function updateRoute(id, data) {
  return request.put(`/route/${id}`, data)
}

// 删除路线
export function deleteRoute(id) {
  return request.delete(`/route/${id}`)
}
