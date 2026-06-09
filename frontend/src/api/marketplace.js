import request from '../utils/request'

// 游客端：获取启用的市集列表
export function getPublicMarketplaces() {
  return request.get('/marketplace/public')
}

// 游客端：获取市集详情
export function getMarketplaceDetail(id) {
  return request.get(`/marketplace/public/${id}`)
}

// 管理员端：获取全部市集（含未启用）
export function getAllMarketplaces() {
  return request.get('/marketplace')
}

// 管理员端：新增市集
export function createMarketplace(data) {
  return request.post('/marketplace', data)
}

// 管理员端：更新市集
export function updateMarketplace(id, data) {
  return request.put(`/marketplace/${id}`, data)
}

// 管理员端：删除市集
export function deleteMarketplace(id) {
  return request.delete(`/marketplace/${id}`)
}

// 收藏相关
export function favoriteMarketplace(userId, marketplaceId) {
  return request.post('/marketplace/favorite', { userId, marketplaceId })
}

export function unfavoriteMarketplace(userId, marketplaceId) {
  return request.delete(`/marketplace/favorite/${marketplaceId}`, { params: { userId } })
}

export function checkMarketplaceFavorite(userId, marketplaceId) {
  return request.get(`/marketplace/favorite/${marketplaceId}`, { params: { userId } })
}

export function getMarketplaceFavorites(userId) {
  return request.get('/marketplace/favorite/list', { params: { userId } })
}


