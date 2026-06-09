import request from '../utils/request'

// 用户登录
export function login(credentials) {
  return request.post('/auth/login', credentials)
}

// 用户注册
export function register(userData) {
  return request.post('/auth/register', userData)
}

// 忘记密码 - 通过用户名+手机号验证后重置
export function forgotPassword(data) {
  return request.post('/auth/forgot-password', data)
}

// 获取用户信息
export function getUserInfo() {
  return request.get('/user/info')
}

// 更新用户信息
export function updateUserInfo(userData) {
  return request.put('/user/info', userData)
}

// 修改密码
export function changePassword(passwordData) {
  return request.put('/user/password', passwordData)
}

// 获取所有用户（管理员）
export function getAllUsers() {
  return request.get('/user')
}

// 删除用户（管理员）
export function deleteUser(id) {
  return request.delete(`/user/${id}`)
}

// 管理员重置用户密码
export function adminResetPassword(id, newPassword) {
  return request.put(`/user/${id}/reset-password`, { newPassword })
}

// 上传文件（头像等）
export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}