import { defineStore } from 'pinia'
import { ref, watch } from 'vue'
import i18n, { elementPlusLocales } from '../locales'
import { ElMessage, ElConfigProvider } from 'element-plus'
import { useUserStore } from './user'

export const useLocaleStore = defineStore('locale', () => {
  const userStore = useUserStore()

  // Helper to get storage key based on role
  const getStorageKey = (role) => {
    const keyMap = {
      'TOURIST': 'locale_tourist',
      'MERCHANT': 'locale_merchant',
      'ADMIN': 'locale_admin'
    }
    return keyMap[role] || 'locale_tourist'
  }

  // 当前语言，初始值已由locales/index.js处理
  const currentLocale = ref(i18n.global.locale.value)
  
  // Element Plus 语言包响应式引用
  const elementPlusLocale = ref(elementPlusLocales[currentLocale.value])

  // 设置语言
  const setLocale = (locale, silent = false) => {
    if (!['zh-CN', 'en-US'].includes(locale)) {
      ElMessage.error('不支持的语言')
      return
    }

    currentLocale.value = locale
    i18n.global.locale.value = locale
    
    // 更新Element Plus语言包
    elementPlusLocale.value = elementPlusLocales[locale]
    
    // 保存到角色特定的localStorage
    const key = getStorageKey(userStore.userRole)
    localStorage.setItem(key, locale)
    
    // 触发全局事件，通知其他组件更新
    window.dispatchEvent(new CustomEvent('locale-change', { detail: { locale } }))
    
    if (!silent) {
      ElMessage.success(locale === 'zh-CN' ? '已切换到简体中文' : 'Switched to English')
    }
  }

  // 切换语言（在zh-CN和en-US之间切换）
  const toggleLocale = () => {
    const newLocale = currentLocale.value === 'zh-CN' ? 'en-US' : 'zh-CN'
    setLocale(newLocale)
  }

  // 获取当前语言
  const getLocale = () => {
    return currentLocale.value
  }

  // 初始化：确保Element Plus使用正确的语言包
  const initLocale = () => {
    // 再次确认语言设置（防止初始化时localStorage变动）
    const key = getStorageKey(userStore.userRole)
    const savedLocale = localStorage.getItem(key)
    if (savedLocale && savedLocale !== currentLocale.value) {
      setLocale(savedLocale, true)
    } else {
      elementPlusLocale.value = elementPlusLocales[currentLocale.value]
    }
  }

  // 监听角色变化（登录/登出）
  watch(() => userStore.userRole, (newRole) => {
    const key = getStorageKey(newRole)
    // 切换角色时，读取该角色的语言设置，默认为zh-CN
    const savedLocale = localStorage.getItem(key) || 'zh-CN'
    if (savedLocale !== currentLocale.value) {
      setLocale(savedLocale, true) // 静默切换
    }
  })

  return {
    currentLocale,
    elementPlusLocale,
    setLocale,
    toggleLocale,
    getLocale,
    initLocale
  }
})

