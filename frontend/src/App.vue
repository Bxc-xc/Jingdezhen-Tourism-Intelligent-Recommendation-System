<template>
  <el-config-provider :locale="localeStore.elementPlusLocale">
    <div id="app">
      <router-view />
    </div>
  </el-config-provider>
</template>

<script setup>
import { onMounted } from 'vue'
import { ElConfigProvider } from 'element-plus'
import { useUserStore } from './stores/user'
import { useLocaleStore } from './stores/locale'

const userStore = useUserStore()
const localeStore = useLocaleStore()

onMounted(() => {
  // 初始化用户信息
  userStore.initUser()
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

/* 全局滚动条美化 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* NProgress 自定义颜色 - 呼应底部渐变色 */
#nprogress .bar {
  background: #4facfe !important;
  height: 3px !important;
}

#nprogress .peg {
  box-shadow: 0 0 10px #4facfe, 0 0 5px #4facfe !important;
}

body {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  background: white;
  color: #303133;
  overflow-x: hidden; /* 全局缩放后防止横向滚动 */
  margin: 0;
  padding: 0;
  height: auto;
}

html {
  /* 使用 zoom 替代 transform: scale 以避免布局空白问题 */
  zoom: 0.8;
  margin: 0;
  padding: 0;
  height: auto;
}

#app {
  min-height: 125vh; /* 补偿缩放: 100vh / 0.8 = 125vh */
  height: auto;
  display: flex;
  flex-direction: column;
}

/* 确保所有页面组件填满高度 */
#app > * {
  min-height: 100%;
  display: flex;
  flex-direction: column;
  flex: 1;
}

/* 全局 Footer 自动置底 */
.footer {
  margin-top: auto;
}

/* 全局下拉菜单样式修复 - 抵消全局缩放影响 */
:deep(.el-popper) {
  /* 使用 fixed 策略的下拉菜单不受全局缩放影响 */
  transform: none !important;
}

/* 确保所有下拉菜单正确定位 */
:deep(.el-popper[data-popper-strategy="fixed"]) {
  transform: none !important;
}

/* 修复 el-select 下拉菜单 */
:deep(.el-select-dropdown) {
  transform: none !important;
}

/* 修复 el-date-picker 下拉菜单 */
:deep(.el-picker__popper) {
  transform: none !important;
}

/* 修复 el-dropdown 下拉菜单 */
:deep(.el-dropdown-menu) {
  transform: none !important;
}

/* 移除下拉菜单的弹窗样式 - 让它们看起来更简洁，不像弹窗 */
:deep(.el-popper.is-light) {
  /* 减少阴影，让下拉菜单看起来更轻量 */
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08) !important;
  /* 使用更淡的边框 */
  border: 1px solid #dcdfe6 !important;
  /* 背景色保持白色但更简洁 */
  background-color: #ffffff !important;
  /* 使用较小的圆角 */
  border-radius: 4px !important;
  /* 移除 padding */
  padding: 0 !important;
}

/* el-select 下拉菜单样式 - 简洁风格 */
:deep(.el-select-dropdown) {
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08) !important;
  border: 1px solid #dcdfe6 !important;
  border-radius: 4px !important;
  padding: 0 !important;
  background-color: #ffffff !important;
}

/* el-date-picker 下拉菜单样式 - 简洁风格 */
:deep(.el-picker__popper) {
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08) !important;
  border: 1px solid #dcdfe6 !important;
  border-radius: 4px !important;
  background-color: #ffffff !important;
}

/* el-dropdown 下拉菜单样式 - 简洁风格 */
:deep(.el-dropdown-menu) {
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08) !important;
  border: 1px solid #dcdfe6 !important;
  border-radius: 4px !important;
  padding: 4px 0 !important;
  background-color: #ffffff !important;
  /* 移除可能存在的遮罩效果 */
  backdrop-filter: none !important;
  -webkit-backdrop-filter: none !important;
}

/* 移除下拉菜单的弹窗动画效果 */
:deep(.el-popper[data-popper-placement]) {
  transition: none !important;
  animation: none !important;
}

/* 确保下拉菜单项样式简洁 */
:deep(.el-dropdown-menu__item),
:deep(.el-select-dropdown__item) {
  background-color: transparent !important;
  transition: background-color 0.2s !important;
}

:deep(.el-dropdown-menu__item:hover),
:deep(.el-select-dropdown__item:hover) {
  background-color: #f5f7fa !important;
}

/* 确保所有下拉菜单的 z-index 足够高，不被触发元素遮挡 */
:deep(.el-popper) {
  z-index: 3000 !important;
  pointer-events: auto !important;
}

:deep(.el-select-dropdown) {
  z-index: 3000 !important;
  pointer-events: auto !important;
}

:deep(.el-picker__popper) {
  z-index: 3000 !important;
  pointer-events: auto !important;
}

:deep(.el-dropdown-menu) {
  z-index: 3000 !important;
  pointer-events: auto !important;
}

/* 确保触发元素不会覆盖下拉菜单 */
:deep(.user-menu),
:deep(.user-dropdown) {
  z-index: 1 !important;
  position: relative;
}

/* 确保下拉菜单内容可以点击 */
:deep(.el-dropdown-menu__item),
:deep(.el-select-dropdown__item),
:deep(.el-picker__panel) {
  pointer-events: auto !important;
  position: relative;
  z-index: 1;
}
</style>