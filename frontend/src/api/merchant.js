import request from '../utils/request'

// 根据分类获取商家列表（支持个性化推荐，传 userId 时走算法排序）
export function getMerchantsByCategory(category, userId) {
  const params = userId ? { userId } : {}
  return request.get(`/merchant/category/${category}`, { params })
}

// 获取所有商家
export function getAllMerchants() {
  return request.get('/merchant')
}

// 根据ID获取商家详情
export function getMerchantById(id) {
  return request.get(`/merchant/${id}`)
}

// 根据景点ID获取关联的商家
export function getMerchantByScenicId(scenicId) {
  return request.get(`/merchant/scenic/${scenicId}`)
}

// 根据用户ID获取商家
export function getMerchantByUserId(userId) {
  return request.get(`/merchant/user/${userId}`)
}

// 创建商家
export function createMerchant(merchantData) {
  return request.post('/merchant', merchantData)
}

// 更新商家信息
export function updateMerchant(id, merchantData) {
  return request.put(`/merchant/${id}`, merchantData)
}

// 更新商家基础信息（店铺名称、描述、地址、电话、营业时间、头像等）
export function updateMerchantBasicInfo(id, basicInfo) {
  return request.put(`/merchant/${id}/basic-info`, basicInfo)
}

// 删除商家
export function deleteMerchant(id) {
  return request.delete(`/merchant/${id}`)
}

// 创建异步删除商家任务（接口秒回）
export function createMerchantDeleteJob(id) {
  return request.post(`/merchant/${id}/delete-jobs`)
}

// 查询异步删除商家任务状态
export function getMerchantDeleteJobStatus(jobId) {
  return request.get(`/merchant/delete-jobs/${jobId}`)
}

// 管理员新增餐饮商家（创建可登录账号）
export function createFoodMerchantAdmin(payload) {
  return request.post('/admin/merchant/food', payload)
}
