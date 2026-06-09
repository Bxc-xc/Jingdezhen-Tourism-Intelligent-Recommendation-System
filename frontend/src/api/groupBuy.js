import request from '../utils/request'

// ===== 用户端 =====
export function getPublicGroupBuys(merchantId) {
  return request.get(`/group-buy/merchant/${merchantId}/public`)
}

export function placeGroupBuyOrder(data) {
  return request.post('/group-buy/order', data)
}

export function getUserGroupBuyOrders(userId) {
  return request.get(`/group-buy/order/user/${userId}`)
}

// ===== 商家端 =====
export function getMerchantGroupBuys(merchantId) {
  return request.get(`/group-buy/merchant/${merchantId}`)
}

export function createGroupBuy(merchantId, data) {
  return request.post(`/group-buy/merchant/${merchantId}`, data)
}

export function updateGroupBuy(merchantId, id, data) {
  return request.put(`/group-buy/merchant/${merchantId}/${id}`, data)
}

export function deleteGroupBuy(merchantId, id) {
  return request.delete(`/group-buy/merchant/${merchantId}/${id}`)
}

export function getMerchantGroupBuyOrders(merchantId) {
  return request.get(`/group-buy/order/merchant/${merchantId}`)
}

// ===== 管理员端 =====
export function adminGetAllGroupBuys() {
  return request.get('/group-buy/admin/list')
}

export function adminAuditGroupBuy(id, status, rejectReason) {
  return request.put(`/group-buy/admin/${id}/audit`, { status, rejectReason })
}

export function adminUpdateGroupBuy(id, data) {
  return request.put(`/group-buy/admin/${id}`, data)
}

export function adminDeleteGroupBuy(id) {
  return request.delete(`/group-buy/admin/${id}`)
}
