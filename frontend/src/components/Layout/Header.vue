<template>
  <div>
    <!-- 系统公告栏 - 流动效果 -->
    <div v-if="announcement" class="announcement-bar">
      <div class="announcement-container">
        <div class="announcement-icon-wrapper">
          <el-icon class="announcement-icon"><Bell /></el-icon>
        </div>
        <div class="announcement-scroll-wrapper">
          <div 
            class="announcement-scroll-content" 
            :style="{ animationDuration: scrollDuration + 's' }"
          >
            <span class="announcement-text">{{ announcement }}</span>
          </div>
        </div>
      </div>
    </div>
    
    <el-header class="header">
      <div class="header-content">
        <div class="logo">
          <router-link to="/">
            <el-icon size="24"><Location /></el-icon>
            <span>{{ $t('header.welcome') }}</span>
          </router-link>
        </div>
      
        <!-- 导航菜单 -->
        <nav class="nav-menu">
          <router-link to="/" class="nav-item" active-class="active">
            <el-icon class="nav-icon"><House /></el-icon>
            <span>{{ $t('menu.home') }}</span>
          </router-link>
          <router-link to="/recommend" class="nav-item" active-class="active">
            <el-icon class="nav-icon"><Compass /></el-icon>
            <span>{{ $t('menu.recommend') }}</span>
          </router-link>
          <router-link to="/ceramic-experience" class="nav-item" active-class="active">
            <el-icon class="nav-icon"><Brush /></el-icon>
            <span>{{ $t('menu.ceramicExperience') }}</span>
          </router-link>
          <router-link to="/plan" class="nav-item" active-class="active">
            <el-icon class="nav-icon"><MapLocation /></el-icon>
            <span>{{ $t('menu.plan') }}</span>
          </router-link>
        </nav>
      
        <!-- 右侧操作区 -->
        <div class="header-actions">
          <div class="search-wrapper">
            <div class="search-trigger" :class="{ 'is-active': isSearchActive }">
              <el-icon class="search-icon"><Search /></el-icon>
              <input 
                ref="searchInput"
                v-model="searchKeyword"
                :placeholder="$t('common.searchPlaceholder')"
                class="search-input"
                @focus="isSearchActive = true"
                @blur="isSearchActive = false"
                @keyup.enter="handleSearch"
              />
            </div>
          </div>
          
          <div v-if="!userStore.isLoggedIn" class="auth-buttons">
            <button class="btn-text" @click="$router.push('/login')">{{ $t('common.login') }}</button>
            <button class="btn-primary" @click="$router.push('/register')">{{ $t('common.register') }}</button>
          </div>
          
          <div v-else class="user-menu" ref="userMenuRef">
            <div 
              class="user-profile-trigger" 
              :class="{ 'is-active': isUserPanelOpen }"
              @click="toggleUserPanel"
            >
              <div class="user-avatar-container">
                <el-avatar :size="36" :src="userStore.user?.avatar" class="user-avatar">
                  {{ userStore.user?.username?.charAt(0) }}
                </el-avatar>
                <div class="status-badge"></div>
              </div>
              <div class="user-name-wrapper">
                <span class="header-username">{{ userStore.user?.username }}</span>
                <span class="header-role">{{ getUserRoleText(userStore.userRole) }}</span>
              </div>
              <el-icon class="dropdown-arrow" :class="{ 'is-rotated': isUserPanelOpen }"><ArrowDown /></el-icon>
            </div>
            
            <!-- 自定义用户面板 -->
            <transition name="fade-slide">
              <div v-if="isUserPanelOpen" class="user-panel">
                <!-- 面板头部 -->
                <div class="panel-header">
                  <el-avatar :size="48" :src="userStore.user?.avatar" class="panel-avatar">
                    {{ userStore.user?.username?.charAt(0) }}
                  </el-avatar>
                  <div class="panel-user-info">
                    <span class="panel-name">{{ userStore.user?.username }}</span>
                    <span class="panel-role">{{ getUserRoleText(userStore.userRole) }}</span>
                  </div>
                </div>
                
                <!-- 面板菜单 -->
                <div class="panel-menu">
                  <div class="menu-item" @click="handleUserCommand('profile')">
                    <el-icon><User /></el-icon>
                    <span>{{ $t('menu.personalCenter') }}</span>
                  </div>
                  <div v-if="userStore.userRole === 'MERCHANT'" class="menu-item" @click="handleUserCommand('merchant')">
                    <el-icon><Shop /></el-icon>
                    <span>{{ $t('menu.merchantCenter') }}</span>
                  </div>
                  <div v-if="userStore.userRole === 'ADMIN'" class="menu-item" @click="handleUserCommand('admin')">
                    <el-icon><Monitor /></el-icon>
                    <span>{{ $t('menu.adminCenter') }}</span>
                  </div>
                  <div class="menu-divider"></div>
                  <div class="menu-item danger" @click="handleUserCommand('logout')">
                    <el-icon><SwitchButton /></el-icon>
                    <span>{{ $t('common.logout') }}</span>
                  </div>
                </div>
              </div>
            </transition>
          </div>

          <!-- 语言切换按钮放到最右侧，低调一点 -->
          <LanguageSwitcher />
        </div>
    </div>
    </el-header>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { Bell, Search, Close, Location, User, Shop, Monitor, SwitchButton, House, Compass, Brush, MapLocation, ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'
import { getSystemSettings } from '../../api/systemConfig'
import LanguageSwitcher from '../LanguageSwitcher.vue'

const { t } = useI18n()
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const searchKeyword = ref('')
const isSearchActive = ref(false)
const announcement = ref('')
const scrollDuration = ref(20) // 滚动动画时长（秒）

const isUserPanelOpen = ref(false)
const userMenuRef = ref(null)

const toggleUserPanel = () => {
  isUserPanelOpen.value = !isUserPanelOpen.value
}

const closeUserPanel = (e) => {
  if (userMenuRef.value && !userMenuRef.value.contains(e.target)) {
    isUserPanelOpen.value = false
  }
}

const getUserRoleText = (role) => {
  const roleMap = {
    'ADMIN': t('header.role.admin'),
    'MERCHANT': t('header.role.merchant'),
    'USER': t('header.role.user')
  }
  return roleMap[role] || t('header.role.guest')
}

// 加载系统公告
const loadAnnouncement = async () => {
  try {
    const response = await getSystemSettings()
    if (response.success && response.data && response.data.announcement) {
      const text = response.data.announcement.trim()
      if (text) {
        announcement.value = text
        // 根据文字长度动态调整滚动速度（文字越长，速度越慢）
        const textLength = text.length
        scrollDuration.value = Math.max(15, Math.min(30, textLength * 0.3))
      } else {
        announcement.value = ''
      }
    }
  } catch (error) {
    console.error('加载系统公告失败:', error)
  }
}

onMounted(() => {
  loadAnnouncement()
  document.addEventListener('click', closeUserPanel)
})

onUnmounted(() => {
  document.removeEventListener('click', closeUserPanel)
})

const activeIndex = computed(() => {
  return route.path
})

const handleSelect = (key) => {
  router.push(key)
}

const handleSearch = () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    return
  }
  // 跳转到首页并传递搜索参数，首页会监听query参数并执行搜索
  router.push({
    path: '/',
    query: { search: keyword }
  })
}

const handleUserCommand = (command) => {
  isUserPanelOpen.value = false
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'merchant':
      router.push('/merchant')
      break
    case 'admin':
      router.push('/admin')
      break
    case 'logout':
      userStore.logout()
      router.push('/')
      break
  }
}
</script>

<style scoped>
.announcement-bar {
  width: 100%;
  background: linear-gradient(135deg, #f5f5f5 0%, #e8e8e8 100%);
  border-bottom: 1px solid #ebeef5;
  overflow: hidden;
  position: relative;
  box-shadow: 0 2px 8px rgba(204, 206, 217, 0.963);
  border-radius: 4px;
  margin: 4px 8px 0 8px;
}

.announcement-container {
  display: flex;
  align-items: center;
  height: 40px;
  max-width: 100%;
  overflow: hidden;
}

.announcement-icon-wrapper {
  flex-shrink: 0;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 4px 0 0 4px;
}

.announcement-icon {
  font-size: 18px;
  color: #409eff;
  animation: bell-shake 2s ease-in-out infinite;
}

@keyframes bell-shake {
  0%, 100% {
    transform: rotate(0deg);
  }
  10%, 30% {
    transform: rotate(-10deg);
  }
  20%, 40% {
    transform: rotate(10deg);
  }
  50% {
    transform: rotate(0deg);
  }
}

.announcement-scroll-wrapper {
  flex: 1;
  overflow: hidden;
  position: relative;
  height: 100%;
  display: flex;
  align-items: center;
}

.announcement-scroll-content {
  display: inline-flex;
  white-space: nowrap;
  animation: scroll-text linear infinite;
  will-change: transform;
  position: relative;
}

.announcement-text {
  display: inline-block;
  padding-right: 50px;
  color: #606266;
  font-size: 14px;
  font-weight: 500;
  line-height: 40px;
}

/* 流动动画 - 从最右边滚动到左边 */
@keyframes scroll-text {
  0% {
    transform: translateX(100%);
  }
  100% {
    transform: translateX(-100%);
  }
}


/* 鼠标悬停时暂停动画 */
.announcement-bar:hover .announcement-scroll-content {
  animation-play-state: paused;
}

.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0;
  height: 60px;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.logo a {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  color: #409eff;
  font-weight: bold;
  font-size: 18px;
}

/* 导航菜单 */
.nav-menu {
  display: flex;
  gap: 12px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  color: #606266;
  font-weight: 500;
  font-size: 15px;
  padding: 8px 16px;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.nav-item:hover {
  background: rgba(64, 158, 255, 0.08);
  color: #409eff;
  transform: translateY(-1px);
}

.nav-item.active {
  background: #ecf5ff;
  color: #409eff;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
}

.nav-item.active::after {
  display: none; /* 移除旧的下划线 */
}

.nav-icon {
  font-size: 18px;
  transition: transform 0.3s;
}

.nav-item:hover .nav-icon {
  transform: scale(1.1);
}

/* 右侧操作区 - 重构 */
.header-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

/* 搜索框 - 现代简约风 */
.search-wrapper {
  position: relative;
  height: 40px;
}

.search-trigger {
  display: flex;
  align-items: center;
  background: #f3f4f6;
  border-radius: 20px;
  padding: 0 16px;
  height: 40px;
  width: 240px; /* 固定宽度，不再伸缩 */
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.search-trigger:hover, .search-trigger:focus-within {
  background: #fff;
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
  width: 280px; /* 聚焦时微微变宽 */
}

.search-icon {
  font-size: 16px;
  color: #909399;
  margin-right: 8px;
  transition: color 0.3s;
}

.search-trigger:focus-within .search-icon {
  color: #409eff;
}

.search-input {
  border: none;
  background: transparent;
  outline: none;
  flex: 1;
  font-size: 14px;
  color: #333;
}

.search-input::placeholder {
  color: #a8abb2;
}

/* 用户头像 - 高级感设计 */
.user-menu {
  display: flex;
  align-items: center;
}

.user-profile-trigger {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 4px 8px 4px 4px;
  border-radius: 24px;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.user-profile-trigger:hover {
  background: #f5f7fa;
  border-color: #ebeef5;
}

.user-avatar-container {
  position: relative;
}

.user-avatar {
  background: linear-gradient(135deg, #a0cfff 0%, #409eff 100%);
  color: #fff;
  font-weight: 600;
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.status-badge {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 10px;
  height: 10px;
  background: #67c23a;
  border: 2px solid #fff;
  border-radius: 50%;
  box-shadow: 0 1px 2px rgba(0,0,0,0.1);
}

.user-name-wrapper {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.header-username {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.header-role {
  font-size: 11px;
  color: #909399;
}

.dropdown-arrow {
  font-size: 12px;
  color: #c0c4cc;
  transition: transform 0.3s;
}

.user-profile-trigger:hover .dropdown-arrow {
  color: #606266;
  transform: rotate(180deg);
}

/* 按钮组 */
.auth-buttons {
  display: flex;
  gap: 12px;
  align-items: center;
}

.user-menu {
  position: relative;
}

.user-panel {
  position: absolute;
  top: calc(100% + 12px);
  right: 0;
  width: 260px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  border: 1px solid #ebeef5;
  z-index: 2000;
  overflow: hidden;
  transform-origin: top right;
}

.panel-header {
  padding: 20px;
  background: linear-gradient(135deg, #f0f9ff 0%, #ecf5ff 100%);
  display: flex;
  align-items: center;
  gap: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.panel-avatar {
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
  background: #409eff;
  flex-shrink: 0;
}

.panel-user-info {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.panel-role {
  font-size: 12px;
  color: #909399;
  background: rgba(255, 255, 255, 0.5);
  padding: 2px 8px;
  border-radius: 10px;
  align-self: flex-start;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.panel-menu {
  padding: 8px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 8px;
  cursor: pointer;
  color: #606266;
  font-size: 14px;
  transition: all 0.2s;
  margin-bottom: 2px;
}

.menu-item:hover {
  background: #f5f7fa;
  color: #409eff;
  transform: translateX(4px);
}

.menu-item .el-icon {
  font-size: 18px;
}

.menu-item.danger {
  color: #f56c6c;
}

.menu-item.danger:hover {
  background: #fef0f0;
  color: #f56c6c;
}

.menu-divider {
  height: 1px;
  background: #ebeef5;
  margin: 8px 0;
}

/* Animation */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px) scale(0.95);
}

.dropdown-arrow.is-rotated {
  transform: rotate(180deg);
}

.user-profile-trigger.is-active {
  background: #ecf5ff;
  border-color: #d9ecff;
}
</style>