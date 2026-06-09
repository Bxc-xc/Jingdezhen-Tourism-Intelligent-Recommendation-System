import request from '../utils/request'

// 获取商家活动列表
export function getActivitiesByMerchant(merchantId) {
  return request.get(`/activities/merchant/${merchantId}`)
}

// 获取所有活动
export function getAllActiveActivities() {
  return request.get('/activities/active')
}

// 获取活动详情
export function getActivityById(id) {
  return request.get(`/activities/${id}`)
}

// 创建活动
export function createActivity(merchantId, activityData) {
  return request.post(`/activities/merchant/${merchantId}`, activityData)
}

// 更新活动
export function updateActivity(id, activityData) {
  return request.put(`/activities/${id}`, activityData)
}

// 删除活动
export function deleteActivity(id) {
  return request.delete(`/activities/${id}`)
}

