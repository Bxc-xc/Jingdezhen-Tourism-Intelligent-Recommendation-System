import request from '../utils/request'

// 生成行程规划
export function generatePlan(planData) {
  return request.post('/plan/generate', planData)
}

// 获取用户行程列表
export function getUserPlans(userId) {
  return request.get(`/plan/user/${userId}`)
}

// 获取行程详情
export function getPlanById(planId) {
  return request.get(`/plan/${planId}`)
}

// 更新行程
export function updatePlan(planId, planData) {
  return request.put(`/plan/${planId}`, planData)
}

// 删除行程
export function deletePlan(planId) {
  return request.delete(`/plan/${planId}`)
}

// 获取起终点之间的推荐交通方案
// 支持多种方式：
// 1. 通过景点名称：getTransportOptions('起点名称', '终点名称')
// 2. 通过景点ID：getTransportOptions(null, null, originId, destId)
// 3. 通过经纬度：getTransportOptions('起点名称', '终点名称', null, null, originLat, originLng, destLat, destLng)
export function getTransportOptions(originName, destName, originId, destId, originLat, originLng, destLat, destLng) {
  const params = {}
  // 只添加非空值到参数中
  if (originName != null && originName !== '') params.originName = originName
  if (destName != null && destName !== '') params.destName = destName
  if (originId != null && originId !== 0) params.originId = originId
  if (destId != null && destId !== 0) params.destId = destId
  if (originLat != null && !isNaN(originLat)) params.originLat = originLat
  if (originLng != null && !isNaN(originLng)) params.originLng = originLng
  if (destLat != null && !isNaN(destLat)) params.destLat = destLat
  if (destLng != null && !isNaN(destLng)) params.destLng = destLng
  
  return request.get('/plan/transport-options', { params })
}

// 从推荐路线创建行程（模板功能）
export function createPlanFromRoute(userId, routeId, startDate) {
  return request.post('/plan/from-route', {
    userId,
    routeId,
    startDate
  })
}

// 获取行程的版本列表
export function getPlanVersions(planId) {
  return request.get(`/plan/${planId}/versions`)
}

// 从指定版本恢复行程
export function restorePlanFromVersion(planId, versionId) {
  return request.post(`/plan/${planId}/restore/${versionId}`)
}

// 获取当前登录用户的行程列表
export async function getMyPlans({ page = 1, pageSize = 20 } = {}) {
  try {
    // 从 localStorage 读取用户信息
    const user = JSON.parse(localStorage.getItem('user') || sessionStorage.getItem('user') || 'null')
    if (!user?.id) return { success: true, data: [] }
    const res = await request.get(`/plan/user/${user.id}`)
    // 统一返回格式
    const list = Array.isArray(res) ? res : (res.data || [])
    return { success: true, data: list }
  } catch (error) {
    return { success: false, message: error.message || '获取行程列表失败' }
  }
}

// 手动创建行程（不走 AI 生成）
export async function createManualPlan(planData) {
  try {
    const res = await request.post('/plan/manual', planData)
    return res
  } catch (error) {
    return { success: false, message: error.message || '创建失败' }
  }
}

// 智能生成行程（仅预览，不保存）
export async function smartGenerateTrip(params) {
  try {
    const res = await request.post('/plan/smart-generate', params)
    return res
  } catch (error) {
    return { success: false, message: error.message || '生成失败' }
  }
}
