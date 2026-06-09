import request from '../utils/request'

// 商家资质申请相关API
export const merchantApplicationApi = {
  // 提交申请
  submitApplication(data) {
    return request({
      url: '/merchant-application',
      method: 'post',
      data
    })
  },

  // 获取用户申请状态
  getApplicationByUserId(userId) {
    return request({
      url: `/merchant-application/user/${userId}`,
      method: 'get'
    })
  },

  // 获取所有申请（管理员用）
  getAllApplications(status) {
    return request({
      url: '/merchant-application',
      method: 'get',
      params: { status }
    })
  },

  // 审核申请
  auditApplication(id, data) {
    return request({
      url: `/merchant-application/${id}/audit`,
      method: 'put',
      data
    })
  },

  // 获取申请详情
  getApplicationById(id) {
    return request({
      url: `/merchant-application/${id}`,
      method: 'get'
    })
  }
}