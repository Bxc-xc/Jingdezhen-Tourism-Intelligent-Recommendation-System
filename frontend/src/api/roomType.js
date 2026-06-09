import request from '../utils/request'

// ===== 商家端 =====

export function getRoomTypes(merchantId) {
  return request({ url: `/merchant/room-types/list/${merchantId}`, method: 'get' })
}

export function createRoomType(merchantId, data) {
  return request({ url: `/merchant/room-types/${merchantId}`, method: 'post', data })
}

export function updateRoomType(id, data) {
  return request({ url: `/merchant/room-types/${id}`, method: 'put', data })
}

export function deleteRoomType(id) {
  return request({ url: `/merchant/room-types/${id}`, method: 'delete' })
}

// ===== 管理员端 =====

export function adminGetAllRoomTypes() {
  return request({ url: '/admin/room-types', method: 'get' })
}

export function adminCreateRoomType(merchantId, data) {
  return request({ url: `/admin/room-types/${merchantId}`, method: 'post', data })
}

export function adminUpdateRoomType(id, data) {
  return request({ url: `/admin/room-types/${id}`, method: 'put', data })
}

export function adminDeleteRoomType(id) {
  return request({ url: `/admin/room-types/${id}`, method: 'delete' })
}
