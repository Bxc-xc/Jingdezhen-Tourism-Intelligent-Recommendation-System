import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, register, getUserInfo } from '../api/auth'

export const useUserStore = defineStore('user', () => {
  const user = ref(JSON.parse((localStorage.getItem('user') || sessionStorage.getItem('user') || 'null')))
  const token = ref(localStorage.getItem('token') || sessionStorage.getItem('token') || '')

  const isLoggedIn = computed(() => !!token.value && !!user.value)
  const userRole = computed(() => user.value?.role || 'TOURIST')

  // 初始化用户信息
  const initUser = async () => {
    if (token.value) {
      try {
        const response = await getUserInfo()
        const userInfo = response.data || response
        user.value = userInfo
        // 写回与原来相同的存储
        const storage = localStorage.getItem('token') ? localStorage : sessionStorage
        storage.setItem('user', JSON.stringify(userInfo))
      } catch (error) {
        // 如果是401未授权错误（token过期或无效），静默处理
        if (error.response?.status === 401) {
          // token已过期或无效，清除本地存储，不显示错误
          logout()
          return
        }
        
        // 如果是网络错误（后端未启动等），也静默处理
        if (!error.response) {
          // 网络错误，可能是后端未启动，不清除token，保持当前状态
          // 不显示错误信息，避免初次加载时的干扰
          return
        }
        
        // 其他错误才显示并清除（但不在控制台显示，避免干扰）
        logout()
      }
    }
  }

  // 登录
  const loginUser = async (credentials, remember = false) => {
    try {
      const response = await login(credentials)
      if (response.success && response.data && response.token) {
        token.value = response.token
        user.value = response.data
        // 根据"记住我"决定存储位置
        const storage = remember ? localStorage : sessionStorage
        // 清除另一个存储，避免残留
        if (remember) {
          sessionStorage.removeItem('token')
          sessionStorage.removeItem('user')
        } else {
          localStorage.removeItem('token')
          localStorage.removeItem('user')
        }
        storage.setItem('token', response.token)
        storage.setItem('user', JSON.stringify(response.data))
        return response
      } else {
        const errorMessage = response.message || '登录失败，请检查用户名和密码'
        throw new Error(errorMessage)
      }
    } catch (error) {
      if (error.message) {
        throw error
      }
      throw new Error(error.response?.data?.message || '登录失败，请检查用户名和密码')
    }
  }

  // 注册
  const registerUser = async (userData) => {
    try {
      const response = await register(userData)
      return response
    } catch (error) {
      throw error
    }
  }

  // 登出
  const logout = () => {
    user.value = null
    token.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('user')
  }

  // 更新用户信息
  const updateUser = (userData) => {
    user.value = { ...user.value, ...userData }
  }

  // 保存用户信息到本地存储
  const saveUser = () => {
    if (user.value) {
      localStorage.setItem('user', JSON.stringify(user.value))
    }
  }

  return {
    user,
    token,
    isLoggedIn,
    userRole,
    initUser,
    loginUser,
    registerUser,
    logout,
    updateUser,
    saveUser
  }
})