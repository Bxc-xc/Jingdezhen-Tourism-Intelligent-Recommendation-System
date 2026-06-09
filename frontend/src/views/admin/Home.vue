<template>
  <div class="admin-home">
    <div class="admin-header">
      <div class="header-content">
        <div class="logo">
          <h1>管理后台</h1>
        </div>
        <div class="user-info">
          <el-dropdown 
            @command="handleUserCommand" 
            placement="bottom-end"
            trigger="click"
            :popper-options="{
              strategy: 'fixed',
              modifiers: [
                {
                  name: 'offset',
                  options: {
                    offset: [0, 8]
                  }
                },
                {
                  name: 'computeStyles',
                  options: {
                    adaptive: true,
                    gpuAcceleration: true
                  }
                }
              ]
            }"
          >
            <span class="user-dropdown">
              <el-avatar :size="32" :src="getAvatarUrl(userStore.user?.avatar)">
                {{ userStore.user?.username?.charAt(0) }}
              </el-avatar>
              <span>{{ userStore.user?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu class="admin-user-dropdown">
                <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>

    <div class="admin-content">
      <div class="sidebar">
        <el-menu
          :default-active="activeMenu"
          class="admin-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="dashboard">
            <el-icon><Odometer /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>
          <el-menu-item index="statistics">
            <el-icon><TrendCharts /></el-icon>
            <span>统计分析</span>
          </el-menu-item>
          <el-menu-item index="system">
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </el-menu-item>
          <el-menu-item index="audit">
            <el-icon><DocumentChecked /></el-icon>
            <span>审核管理</span>
          </el-menu-item>
          <el-menu-item index="room-types">
            <el-icon><House /></el-icon>
            <span>酒店房型管理</span>
          </el-menu-item>
          <el-menu-item index="group-buy">
            <el-icon><Ticket /></el-icon>
            <span>陶瓷工坊团购管理</span>
          </el-menu-item>
          <el-menu-item index="activities">
            <el-icon><Calendar /></el-icon>
            <span>店铺活动管理</span>
          </el-menu-item>
          <el-menu-item index="review">
            <el-icon><ChatLineRound /></el-icon>
            <span>评价管理</span>
          </el-menu-item>
          <el-menu-item index="logs">
            <el-icon><List /></el-icon>
            <span>操作日志</span>
          </el-menu-item>
        </el-menu>
      </div>

      <div class="main-content">
        <!-- 仪表盘 -->
        <Dashboard v-if="activeMenu === 'dashboard'" />

        <!-- 其他页面内容 -->
        <div v-else class="page-content">
          <router-view />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { useRealtimeData } from '../../composables/useRealtimeData'
import Dashboard from './components/Dashboard.vue'
import request from '../../utils/request'
import { getScenicList } from '../../api/scenic'
import { merchantApplicationApi } from '../../api/merchantApplication'
import { adminGetAllGroupBuys } from '../../api/groupBuy'
import * as echarts from 'echarts'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = ref('dashboard')

// 根据路由设置当前激活的菜单
watch(() => route.path, (path) => {
  if (path.includes('/admin/audit')) {
    activeMenu.value = 'audit'
  } else if (path.includes('/admin/statistics')) {
    activeMenu.value = 'statistics'
  } else if (path.includes('/admin/system')) {
    activeMenu.value = 'system'
  } else if (path.includes('/admin/review')) {
    activeMenu.value = 'review'
  } else if (path.includes('/admin/room-types')) {
    activeMenu.value = 'room-types'
  } else if (path.includes('/admin/logs')) {
    activeMenu.value = 'logs'
  } else if (path.includes('/admin/group-buy')) {
    activeMenu.value = 'group-buy'
  } else if (path.includes('/admin/activities')) {
    activeMenu.value = 'activities'
  } else if (path === '/admin') {
    activeMenu.value = 'dashboard'
  }
}, { immediate: true })

// 使用实时数据同步
const { data: users } = useRealtimeData('user', [])
const { data: merchants } = useRealtimeData('merchant', [])
const { data: scenicSpots } = useRealtimeData('scenic_spot', [])
const { data: orders } = useRealtimeData('order', [])

// 加载初始数据
const loadInitialData = async () => {
  // 获取用户列表
  try {
    const usersResponse = await request.get('/user')
    if (usersResponse.success && usersResponse.data) {
      users.value = Array.isArray(usersResponse.data) ? usersResponse.data : []
    } else if (Array.isArray(usersResponse)) {
      users.value = usersResponse
    }
  } catch (error) {
    // 403 错误可能是权限不足，不影响其他数据加载
    if (error.response?.status === 403) {
      console.warn('获取用户列表失败: 权限不足')
    } else {
      console.warn('获取用户列表失败:', error.message || error)
    }
  }
  
  // 获取商家列表
  try {
    const merchantsResponse = await request.get('/merchant')
    if (merchantsResponse.success && merchantsResponse.data) {
      merchants.value = Array.isArray(merchantsResponse.data) ? merchantsResponse.data : []
    } else if (Array.isArray(merchantsResponse)) {
      merchants.value = merchantsResponse
    }
  } catch (error) {
    if (error.response?.status === 403) {
      console.warn('获取商家列表失败: 权限不足')
    } else {
      console.warn('获取商家列表失败:', error.message || error)
    }
  }
  
  // 获取景点列表
  try {
    const scenicsResponse = await getScenicList()
    if (scenicsResponse.success && scenicsResponse.data) {
      scenicSpots.value = Array.isArray(scenicsResponse.data) ? scenicsResponse.data : []
    } else if (Array.isArray(scenicsResponse)) {
      scenicSpots.value = scenicsResponse
    }
  } catch (error) {
    if (error.response?.status === 403) {
      console.warn('获取景点列表失败: 权限不足')
    } else {
      console.warn('获取景点列表失败:', error.message || error)
    }
  }
  
  // 获取订单列表
  try {
    const ordersResponse = await request.get('/orders')
    if (ordersResponse.success && ordersResponse.data) {
      orders.value = Array.isArray(ordersResponse.data) ? ordersResponse.data : []
    } else if (Array.isArray(ordersResponse)) {
      orders.value = ordersResponse
    }
  } catch (error) {
    // 403 错误可能是权限不足，不影响其他数据加载
    if (error.response?.status === 403) {
      console.warn('获取订单列表失败: 权限不足，可能需要管理员权限')
    } else {
      console.warn('获取订单列表失败:', error.message || error)
    }
  }
}

const stats = ref({
  totalUsers: 0,
  totalMerchants: 0,
  totalScenics: 0,
  totalOrders: 0
})

// 监听数据变化，更新统计
const updateStats = () => {
  stats.value = {
    totalUsers: users.value.length,
    totalMerchants: merchants.value.length,
    totalScenics: scenicSpots.value.length,
    totalOrders: orders.value.length
  }
}

// 监听数据变化，自动更新统计
watch([users, merchants, scenicSpots, orders], () => {
  updateStats()
}, { immediate: true, deep: true })

const pendingAudits = ref([])
const notifications = ref([])

// 加载待审核商家申请
const loadPendingAudits = async () => {
  try {
    const res = await merchantApplicationApi.getAllApplications()
    const list = (res?.data || []).filter(a => a.status === 'PENDING')
    pendingAudits.value = list.slice(0, 5).map(a => ({
      id: a.id,
      applicationId: a.id,
      title: `商家申请：${a.shopName || a.applicantName || '未知'}`,
      description: a.businessType || a.description || '新商家入驻申请',
      createTime: a.createTime || a.createdAt || new Date()
    }))
  } catch (e) {
    console.warn('加载待审核申请失败:', e.message)
  }
}

// 生成系统通知（基于真实数据）
const buildNotifications = () => {
  const items = []
  const now = Date.now()
  if (stats.value.totalUsers > 0) {
    items.push({ id: 1, title: '用户统计', content: `当前共有 ${stats.value.totalUsers} 名注册用户`, createTime: new Date(now) })
  }
  if (stats.value.totalOrders > 0) {
    items.push({ id: 2, title: '订单统计', content: `系统共有 ${stats.value.totalOrders} 笔订单`, createTime: new Date(now - 60000) })
  }
  if (stats.value.totalMerchants > 0) {
    items.push({ id: 3, title: '商家统计', content: `当前共有 ${stats.value.totalMerchants} 家入驻商家`, createTime: new Date(now - 120000) })
  }
  notifications.value = items
}

const handleMenuSelect = (key) => {
  activeMenu.value = key
  if (key !== 'dashboard') {
    router.push(`/admin/${key}`)
  }}

// 获取头像URL（处理相对路径和绝对路径）
const getAvatarUrl = (avatar) => {
  if (!avatar || typeof avatar !== 'string' || avatar.trim() === '') {
    return ''
  }
  
  const pathStr = avatar.trim()
  
  // 已经是完整URL（http/https/blob）
  if (pathStr.startsWith('http://') || pathStr.startsWith('https://') || pathStr.startsWith('blob:')) {
    return pathStr
  }
  
  // 已经是绝对路径（以/uploads开头）
  if (pathStr.startsWith('/uploads/')) {
    return pathStr
  }
  if (pathStr.startsWith('/uploads')) {
    return pathStr + '/'
  }
  
  // 相对路径（uploads/xxx 或 uploads）
  if (pathStr.startsWith('uploads/')) {
    return '/' + pathStr
  }
  if (pathStr.startsWith('uploads')) {
    return '/' + pathStr + '/'
  }
  
  // 如果是以 / 开头的其他路径，直接返回（vite代理会处理）
  if (pathStr.startsWith('/')) {
    return pathStr
  }
  
  // 只有文件名或其他情况，尝试添加 /uploads/ 前缀
  return '/uploads/' + pathStr
}

const handleUserCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/admin/profile')
  }
}

const approveItem = (id) => {
  pendingAudits.value = pendingAudits.value.filter(item => item.id !== id)
  ElMessage.success('审核通过')
}

const rejectItem = (id) => {
  pendingAudits.value = pendingAudits.value.filter(item => item.id !== id)
  ElMessage.success('已拒绝')
}

const formatDate = (date) => {
  return new Date(date).toLocaleString()
}

const initCharts = () => {
  const userChartElement = document.getElementById('userChart')
  const orderChartElement = document.getElementById('orderChart')
  if (!userChartElement || !orderChartElement) return

  // 用户增长趋势：按注册月份统计
  const userChart = echarts.init(userChartElement)
  const monthLabels = getLast6Months()
  const userMonthCounts = countByMonth(users.value, 'createTime')
  userChart.setOption({
    title: { text: '近6个月用户增长' },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: monthLabels },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{ data: monthLabels.map(m => userMonthCounts[m] || 0), type: 'line', smooth: true, areaStyle: {} }]
  })

  // 订单状态分布：用真实订单数据
  const orderChart = echarts.init(orderChartElement)
  const orderStatusMap = { PENDING: '待确认', CONFIRMED: '已确认', CANCELLED: '已取消' }
  const orderStatusCount = {}
  orders.value.forEach(o => {
    const label = orderStatusMap[o.status] || o.status || '未知'
    orderStatusCount[label] = (orderStatusCount[label] || 0) + 1
  })
  const orderPieData = Object.entries(orderStatusCount).map(([name, value]) => ({ name, value }))
  orderChart.setOption({
    title: { text: '订单状态分布' },
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie', radius: '50%',
      data: orderPieData.length ? orderPieData : [{ value: 0, name: '暂无数据' }],
      emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.5)' } }
    }]
  })
}

// 获取近6个月标签，格式 YYYY-MM
const getLast6Months = () => {
  const result = []
  const now = new Date()
  for (let i = 5; i >= 0; i--) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1)
    result.push(`${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`)
  }
  return result
}

// 按月统计列表中的数量
const countByMonth = (list, dateField) => {
  const map = {}
  list.forEach(item => {
    const raw = item[dateField] || item.createdAt || item.createTime
    if (!raw) return
    const d = new Date(raw)
    if (isNaN(d.getTime())) return
    const key = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
    map[key] = (map[key] || 0) + 1
  })
  return map
}

onMounted(async () => {
  // 加载初始数据
  await loadInitialData()
  
  // 初始化图表
  setTimeout(() => {
    initCharts()
  }, 100)
})
</script>

<style scoped>
.admin-home {
  min-height: 100vh;
  background: #f5f7fa;
}

.admin-header {
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
  max-width: 1200px;
  margin: 0 auto;
}

.logo h1 {
  margin: 0;
  color: #303133;
  font-size: 24px;
}

.user-info {
  position: relative;
  display: flex;
  align-items: center;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-dropdown:hover {
  background-color: #f5f7fa;
}

/* 确保下拉菜单在头像下方显示并右对齐 */
:deep(.el-popper.is-light[data-popper-placement^="bottom-end"]) {
  margin-top: 0 !important;
}

:deep(.admin-user-dropdown) {
  margin-top: 0 !important;
  min-width: 120px;
  padding: 4px 0;
}

/* 确保下拉菜单项对齐 */
:deep(.admin-user-dropdown .el-dropdown-menu__item) {
  text-align: left;
  padding: 0 20px;
  line-height: 36px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
}

/* 确保下拉菜单的右边缘与触发元素右边缘精确对齐 */
:deep(.el-popper[data-popper-placement="bottom-end"]) {
  transform-origin: top right !important;
}

.admin-content {
  display: flex;
  max-width: 1200px;
  margin: 0 auto;
  min-height: calc(100vh - 60px);
}

.sidebar {
  width: 200px;
  background: white;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

.admin-menu {
  border-right: none;
}

.main-content {
  flex: 1;
  padding: 20px;
}

.dashboard {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  height: 100px;
}

.stat-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  margin-right: 16px;
}

.stat-icon.users {
  background: #409eff;
}

.stat-icon.merchants {
  background: #67c23a;
}

.stat-icon.scenics {
  background: #e6a23c;
}

.stat-icon.orders {
  background: #f56c6c;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  color: #909399;
  font-size: 14px;
}

.dashboard-charts {
  margin-bottom: 20px;
}

.chart-container {
  width: 100%;
}

.recent-activities {
  margin-top: 20px;
}

.activity-list,
.notification-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.activity-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: #fafafa;
}

.activity-content {
  flex: 1;
}

.activity-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.activity-desc {
  color: #606266;
  font-size: 14px;
  margin-bottom: 4px;
}

.activity-time {
  color: #909399;
  font-size: 12px;
}

.activity-actions {
  display: flex;
  gap: 8px;
}

.notification-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: #fafafa;
}

.notification-icon {
  width: 32px;
  height: 32px;
  background: #409eff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
}

.notification-content {
  flex: 1;
}

.notification-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.notification-desc {
  color: #606266;
  font-size: 14px;
  margin-bottom: 4px;
}

.notification-time {
  color: #909399;
  font-size: 12px;
}

.page-content {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

@media (max-width: 768px) {
  .admin-content {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
  }
  
  .admin-menu {
    display: flex;
    overflow-x: auto;
  }
  
  .stats-cards .el-col {
    margin-bottom: 16px;
  }
  
  .recent-activities .el-col {
    margin-bottom: 20px;
  }
}
</style>
