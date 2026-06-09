import { createI18n } from 'vue-i18n'
import zhCN from './zh-CN'
import enUS from './en-US'
import zhCNLocale from './element-plus-zhcn-safe'
import enUSLocale from 'element-plus/dist/locale/en.mjs'

// 从localStorage读取保存的语言设置，根据角色区分
const getDefaultLocale = () => {
  let role = 'TOURIST'
  try {
    const userStr = localStorage.getItem('user') || sessionStorage.getItem('user')
    if (userStr) {
      const user = JSON.parse(userStr)
      if (user && user.role) {
        role = user.role
      }
    }
  } catch (e) {
    console.error('Error parsing user from localStorage', e)
  }

  const keyMap = {
    'TOURIST': 'locale_tourist',
    'MERCHANT': 'locale_merchant',
    'ADMIN': 'locale_admin'
  }
  
  const storageKey = keyMap[role] || 'locale_tourist'
  // 优先读取角色特定的语言设置，其次读取旧的全局设置，最后默认为简体中文
  const savedLocale = localStorage.getItem(storageKey)
  const legacyLocale = localStorage.getItem('locale')
  
  return savedLocale || legacyLocale || 'zh-CN'
}

const i18n = createI18n({
  legacy: false, // 使用Composition API模式
  locale: getDefaultLocale(),
  fallbackLocale: 'zh-CN',
  messages: {
    'zh-CN': zhCN,
    'en-US': enUS
  }
})

// Element Plus 语言包映射
export const elementPlusLocales = {
  'zh-CN': zhCNLocale,
  'en-US': enUSLocale
}

export default i18n

