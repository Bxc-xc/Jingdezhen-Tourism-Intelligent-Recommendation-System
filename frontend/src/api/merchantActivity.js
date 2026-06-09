import request from '../utils/request'

// 获取商家的活动列表
export function getMerchantActivities(merchantId) {
  return request({
    // request 的 baseURL 已经是 '/api'，这里不要再加 '/api' 前缀
    url: `/merchant/activity/list/${merchantId}`,
    method: 'get'
  })
}

// 发布活动
export function createMerchantActivity(merchantId, data) {
  return request({
    url: `/merchant/activity/${merchantId}`,
    method: 'post',
    data
  })
}

// 更新活动
export function updateMerchantActivity(activityId, data) {
  return request({
    url: `/merchant/activity/${activityId}`,
    method: 'put',
    data
  })
}

// 删除活动
export function deleteMerchantActivity(activityId) {
  return request({
    url: `/merchant/activity/${activityId}`,
    method: 'delete'
  })
}

// 管理员：获取所有活动
export function adminGetAllActivities(params) {
  return request({
    url: '/merchant/activity/admin/all',
    method: 'get',
    params
  })
}

// 管理员：强制下架活动
export function adminOfflineActivity(activityId) {
  return request({
    url: `/merchant/activity/admin/${activityId}/offline`,
    method: 'put'
  })
}
