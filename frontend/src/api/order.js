import request from '../utils/request'

// 创建订单
export function createOrder(orderData) {
  return request.post('/orders', orderData)
}

// 获取全部订单列表（主要用于后台/商家管理）
export function getOrderList(params = {}) {
  return request.get('/orders', { params })
}

// 根据用户获取订单列表（个人中心 - 我的订单）
export function getUserOrders(userId) {
  return request.get(`/orders/user/${userId}`)
}

// 根据商家获取订单列表（商家中心 - 订单管理）
export function getMerchantOrders(merchantId) {
  return request.get(`/orders/merchant/${merchantId}`)
}

// 获取订单详情
export function getOrderDetail(id) {
  return request.get(`/orders/${id}`)
}

// 更新订单状态
export function updateOrderStatus(id, status) {
  return request.put(`/orders/${id}/status`, { status })
}

// 取消订单
export function cancelOrder(id) {
  return request.put(`/orders/${id}/cancel`)
}

// 确认订单
export function confirmOrder(id) {
  return request.put(`/orders/${id}/confirm`)
}

