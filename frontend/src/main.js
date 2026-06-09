import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import { useUserStore } from './stores/user'
import i18n, { elementPlusLocales } from './locales'
import { useLocaleStore } from './stores/locale'
import './styles/tokens.css'

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

const pinia = createPinia()
app.use(pinia)
app.use(router)
app.use(i18n)

// 初始化语言设置
const localeStore = useLocaleStore(pinia)
localeStore.initLocale()

// 使用 Element Plus（双保险：既通过 app.use 注入 locale，也通过 ElConfigProvider 覆盖）
app.use(ElementPlus, { locale: localeStore.elementPlusLocale })

// 运行时证据：打印分页器文案，判断 JS 层是否已变成 ???
if (import.meta.env.DEV) {
  try {
    // eslint-disable-next-line no-console
    console.log('[ElementPlus locale] pagination.prev =', localeStore.elementPlusLocale?.el?.pagination?.prev)
    // eslint-disable-next-line no-console
    console.log('[ElementPlus locale] pagination.next =', localeStore.elementPlusLocale?.el?.pagination?.next)
  } catch (e) {
    // ignore
  }
}

// 应用启动时初始化用户信息（同步本地与服务端状态）
const userStore = useUserStore(pinia)
userStore.initUser().catch(() => {/* 忽略初始化失败，保持未登录状态 */})

app.mount('#app')