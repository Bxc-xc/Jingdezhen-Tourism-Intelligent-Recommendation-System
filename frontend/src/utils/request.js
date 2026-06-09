import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'

// 创建axios实例（支持环境变量配置）
const API_BASE_URL = import.meta?.env?.VITE_API_BASE_URL || '/api'

const request = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
      // 调试信息：只在开发环境显示
      if (import.meta.env.DEV && config.url?.includes('/merchant-application')) {
        console.log('发送请求:', config.url, 'Token:', userStore.token.substring(0, 20) + '...')
      }
    } else {
      // 如果没有 token，记录警告（但不阻止请求，因为某些 API 可能不需要认证）
      if (config.url?.includes('/merchant-application')) {
        console.warn('请求缺少 token:', config.url)
      }
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const { data } = response
    
    // 如果后端返回的数据结构是 { success, data, message }
    if (data.success !== undefined) {
      if (data.success) {
        return data
      } else {
        // 对于美食和路线推荐API，不显示错误提示（有降级方案）
        const url = response.config?.url || ''
        if (!url.includes('/food/') && !url.includes('/route/')) {
          ElMessage.error(data.message || '请求失败')
        }
        return Promise.reject(new Error(data.message || '请求失败'))
      }
    }
    
    // 如果后端直接返回数据
    return data
  },
  error => {
    const { response } = error
    
    if (response) {
      const { status, data } = response
      
      console.log('=== 请求错误详情 ===')
      console.log('状态码:', status)
      console.log('响应数据:', data)
      console.log('==================')
      
      switch (status) {
        case 400:
          // 400错误通常是业务错误，直接返回后端的错误信息
          if (data && data.message) {
            // 对于登录API，不在拦截器中显示错误，让调用方处理
            const isLoginApi = error.config?.url?.includes('/auth/login')
            // 对于美食和路线推荐API，不显示错误提示（有降级方案）
            const isRecommendApi = error.config?.url?.includes('/food/') || error.config?.url?.includes('/route/')
            
            if (!isLoginApi && !isRecommendApi) {
              ElMessage.error(data.message)
            }
            return Promise.reject(new Error(data.message))
          }
          // 没有message的情况
          const isLoginApi = error.config?.url?.includes('/auth/login')
          const isRecommendApi = error.config?.url?.includes('/food/') || error.config?.url?.includes('/route/')
          if (!isLoginApi && !isRecommendApi) {
            ElMessage.error('请求参数错误')
          }
          return Promise.reject(new Error('请求参数错误'))
        case 401:
          {
            const hasAuthHeader = !!error.config?.headers?.Authorization
            const method = (error.config?.method || '').toLowerCase()
            const isGet = method === 'get'

            // 未登录（没有 Authorization）访问接口返回 401：
            // 不弹“未授权”提示，避免从登录/注册页点击“标签”进入公开页面时刷屏
            if (!hasAuthHeader) {
              // 对非 GET（写操作）仍提示，避免用户无感失败
              if (!isGet) {
                ElMessage.error(data?.message || '未授权，请先登录')
              }
              break
            }

            // 带 token 的请求返回 401：token 失效/过期，提示并跳转登录
            ElMessage.error('未授权，请重新登录')
            const userStore = useUserStore()
            userStore.logout()

            // 带 redirect，登录后可返回当前页（避免“回弹感”）
            const current = `${window.location.pathname}${window.location.search}${window.location.hash}`
            const isAuthPage =
              window.location.pathname === '/login' ||
              window.location.pathname === '/register' ||
              window.location.pathname === '/forgot-password'
            if (!isAuthPage) {
              window.location.href = `/login?redirect=${encodeURIComponent(current)}`
            }
          }
          break
        case 403:
          // 403错误：权限被拒绝，可能是未登录或token无效
          const errorMsg = data?.message || '权限不足，请检查是否已登录'
          const url = error.config?.url || ''
          const method = error.config?.method || ''
          
          // 调试信息
          console.error('403错误详情:', {
            url,
            method,
            hasAuthHeader: !!error.config?.headers?.Authorization,
            token: error.config?.headers?.Authorization?.substring(0, 20) + '...',
            responseData: data
          })
          
          // 对于GET请求获取申请状态的API，403可能是申请不存在，不应该跳转
          if (method === 'get' && url.includes('/merchant-application/user/')) {
            // 静默处理，不显示错误，不跳转
            break
          }
          
          {
            // 检查是否有认证头
            const hasAuthHeader = !!error.config?.headers?.Authorization
            const m = (method || '').toLowerCase()
            const isGet = m === 'get'

            // 未登录：GET 请求不刷错误提示；写操作仍提示
            if (!hasAuthHeader) {
              if (!isGet) {
                ElMessage.error(errorMsg)
              }
              // 不强制跳转登录页
              break
            }

            // 已登录但权限不足：提示即可（不自动跳转）
            ElMessage.error(errorMsg)
          }
          break
        case 404:
          // 对于美食和路线推荐API，404不显示错误提示（有降级方案）
          if (!error.config?.url?.includes('/food/') && 
              !error.config?.url?.includes('/route/') &&
              !error.config?.url?.endsWith('/route')) {
            ElMessage.error('请求的资源不存在')
          }
          break
        case 500:
          // 对于美食和路线推荐API，500不显示错误提示（有降级方案）
          if (!error.config?.url?.includes('/food/') && 
              !error.config?.url?.includes('/route/') &&
              !error.config?.url?.endsWith('/route')) {
            ElMessage.error('服务器内部错误')
          }
          break
        default:
          // 对于美食和路线推荐API，不显示错误提示（有降级方案）
          if (!error.config?.url?.includes('/food/') && 
              !error.config?.url?.includes('/route/') &&
              !error.config?.url?.endsWith('/route')) {
            ElMessage.error(data?.message || '请求失败')
          }
      }
    } else {
      // 对于美食和路线推荐API，网络错误不显示提示（有降级方案）
      const url = error.config?.url || ''
      if (!url.includes('/food/') && !url.includes('/route/') && !url.endsWith('/route')) {
        ElMessage.error('网络错误，请检查网络连接')
      }
    }
    
    return Promise.reject(error)
  }
)

export default request