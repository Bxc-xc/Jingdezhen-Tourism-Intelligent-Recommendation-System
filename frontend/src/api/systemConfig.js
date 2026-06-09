import request from '../utils/request'

// 获取系统设置（公开接口）
export function getSystemSettings() {
  return request.get('/system-config/settings')
}

// 获取所有系统配置（需要管理员权限）
export function getAllConfigs() {
  return request.get('/system-config')
}

// 保存系统设置（需要管理员权限）
export function saveSystemSettings(settings) {
  return request.post('/system-config/settings', settings)
}

// 根据配置键获取配置值
export function getConfigByKey(configKey) {
  return request.get(`/system-config/${configKey}`)
}

// 保存或更新单个配置（需要管理员权限）
export function saveConfig(configData) {
  return request.post('/system-config', configData)
}

// 删除配置（需要管理员权限）
export function deleteConfig(configKey) {
  return request.delete(`/system-config/${configKey}`)
}

// 数据备份（需要管理员权限）
export function backupData() {
  return request.post('/system-config/backup')
}

// 数据恢复（需要管理员权限）
export function restoreData(backupData) {
  return request.post('/system-config/restore', backupData)
}

