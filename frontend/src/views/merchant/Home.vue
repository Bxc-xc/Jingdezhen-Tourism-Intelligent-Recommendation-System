<template>
  <div class="merchant-home">
    <div class="merchant-header">
      <div class="header-content">
        <div class="logo">
          <h1>{{ merchantTitle }}</h1>
          <span class="merchant-badge" v-if="merchantInfo?.category">
            {{ categoryLabel }}
          </span>
        </div>
        <div class="user-info">
          <LanguageSwitcher />
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
              <el-avatar :size="32" :src="userStore.user?.avatar">
                {{ userStore.user?.username?.charAt(0) }}
              </el-avatar>
              <span>{{ userStore.user?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu class="user-dropdown-menu">
                <el-dropdown-item command="profile" class="profile-item">
                  <el-icon style="margin-right: 8px;"><User /></el-icon>
                  <span>{{ $t('common.profile') }}</span>
                </el-dropdown-item>
                <el-dropdown-item command="logout" class="logout-item">
                  <el-icon style="margin-right: 8px;"><SwitchButton /></el-icon>
                  <span>{{ $t('common.logout') }}</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>

    <div class="merchant-content">
      <div class="sidebar">
        <el-menu
          :default-active="activeMenu"
          class="merchant-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="dashboard">
            <el-icon><Odometer /></el-icon>
            <span>{{ $t('menu.dashboard') }}</span>
          </el-menu-item>
          <el-menu-item index="shop">
            <el-icon><Shop /></el-icon>
            <span>{{ $t('menu.shop') }}</span>
          </el-menu-item>
          <!-- 美食商家专属菜单 -->
          <el-menu-item 
            v-if="merchantInfo?.category === 'FOOD'" 
            index="menu"
            disabled
            :title="$t('menu.menuDeveloping')"
          >
            <el-icon><Food /></el-icon>
            <span>{{ $t('menu.menu') }}</span>
          </el-menu-item>
          <!-- 酒店商家专属菜单 -->
          <el-menu-item 
            v-if="merchantInfo?.category === 'HOTEL'" 
            index="rooms"
          >
            <el-icon><House /></el-icon>
            <span>{{ $t('menu.rooms') }}</span>
          </el-menu-item>
          <el-menu-item 
            v-if="merchantInfo?.category === 'HOTEL'" 
            index="room-calendar"
          >
            <el-icon><Calendar /></el-icon>
            <span>{{ $t('menu.roomCalendar') }}</span>
          </el-menu-item>
          <el-menu-item index="orders">
            <el-icon><Document /></el-icon>
            <span>{{ $t('menu.orders') }}</span>
          </el-menu-item>
          <el-menu-item index="reviews">
            <el-icon><ChatDotRound /></el-icon>
            <span>{{ $t('menu.reviews') }}</span>
          </el-menu-item>
          <el-menu-item index="qualification">
            <el-icon><DocumentChecked /></el-icon>
            <span>{{ $t('menu.qualification') }}</span>
          </el-menu-item>

          <el-menu-item index="statistics">
            <el-icon><TrendCharts /></el-icon>
            <span>{{ $t('menu.statistics') }}</span>
          </el-menu-item>
          <el-menu-item index="activities">
            <el-icon><Present /></el-icon>
            <span>{{ $t('menu.activities') }}</span>
          </el-menu-item>
          <!-- 陶瓷工坊专属：团购管理 -->
          <el-menu-item
            v-if="merchantInfo?.category === 'CERAMIC'"
            index="group-buy"
          >
            <el-icon><Ticket /></el-icon>
            <span>团购管理</span>
          </el-menu-item>
        </el-menu>
      </div>

      <div class="main-content">
        <!-- 仪表盘 -->
        <div v-if="activeMenu === 'dashboard'" class="dashboard">
          <!-- 欢迎信息 -->
          <div class="welcome-section">
            <div class="welcome-banner">
              <div class="welcome-content">
                <div class="welcome-text">
                  <h2>{{ welcomeTitle }}</h2>
                  <p>{{ welcomeMessage }}</p>
                  <div class="weather-widget">
                    <el-icon><Sunny /></el-icon> <span>{{ $t('merchant.weather.sunny') }}</span>
                  </div>
                </div>
                <div class="welcome-decoration">
                  <el-icon v-if="merchantInfo?.category === 'FOOD'" size="120"><Food /></el-icon>
                  <el-icon v-else-if="merchantInfo?.category === 'HOTEL'" size="120"><House /></el-icon>
                  <el-icon v-else size="120"><Shop /></el-icon>
                </div>
              </div>
            </div>
            
            <!-- 快捷入口 -->
            <div class="quick-actions">
              <div class="action-card" @click="handleQuickAction('add-product')">
                <div class="action-icon" style="background: #e6f7ff; color: #1890ff">
                  <el-icon><Plus /></el-icon>
                </div>
                <span>{{ $t('merchant.quickActions.addProduct') }}</span>
              </div>
              <div class="action-card" @click="handleQuickAction('orders')">
                <div class="action-icon" style="background: #fff7e6; color: #fa8c16">
                  <el-icon><Document /></el-icon>
                </div>
                <span>{{ $t('merchant.quickActions.handleOrders') }}</span>
              </div>
              <div class="action-card" @click="handleQuickAction('reviews')">
                <div class="action-icon" style="background: #f6ffed; color: #52c41a">
                  <el-icon><ChatDotRound /></el-icon>
                </div>
                <span>{{ $t('merchant.quickActions.replyReviews') }}</span>
              </div>
              <div class="action-card" @click="handleQuickAction('promotion')">
                <div class="action-icon" style="background: #fff0f6; color: #eb2f96">
                  <el-icon><Present /></el-icon>
                </div>
                <span>{{ $t('merchant.quickActions.promotion') }}</span>
              </div>
            </div>
          </div>
          
          <div class="stats-cards">
            <el-row :gutter="24">
              <el-col :span="6" :xs="12">
                <div class="stat-card blue-theme">
                  <div class="stat-icon-bg"><Document /></div>
                  <div class="stat-header">
                    <span class="stat-label">{{ $t('merchant.stats.totalOrders') }}</span>
                    <el-tag size="small" type="success">+12%</el-tag>
                  </div>
                  <div class="stat-value">{{ stats.totalOrders }}</div>
                  <div class="stat-footer">{{ $t('merchant.stats.ordersIncrease') }} 4 {{ $t('merchant.stats.orders') }}</div>
                </div>
              </el-col>
              <el-col :span="6" :xs="12">
                <div class="stat-card purple-theme">
                  <div class="stat-icon-bg"><Money /></div>
                  <div class="stat-header">
                    <span class="stat-label">{{ $t('merchant.stats.totalRevenue') }}</span>
                    <el-tag size="small" type="success">+8.5%</el-tag>
                  </div>
                  <div class="stat-value">¥{{ stats.totalRevenue }}</div>
                  <div class="stat-footer">{{ $t('merchant.stats.revenueIncrease') }} ¥1,200</div>
                </div>
              </el-col>
              <el-col :span="6" :xs="12">
                <div class="stat-card orange-theme">
                  <div class="stat-icon-bg"><Star /></div>
                  <div class="stat-header">
                    <span class="stat-label">{{ $t('merchant.stats.averageRating') }}</span>
                    <el-tag size="small" type="warning">Top 5%</el-tag>
                  </div>
                  <div class="stat-value">{{ stats.averageRating }}</div>
                  <div class="stat-footer">{{ $t('merchant.stats.totalReviews') }} 45 {{ $t('merchant.stats.reviews') }}</div>
                </div>
              </el-col>
              <el-col :span="6" :xs="12">
                <div class="stat-card green-theme">
                  <div class="stat-icon-bg"><View /></div>
                  <div class="stat-header">
                    <span class="stat-label">{{ $t('merchant.stats.totalViews') }}</span>
                    <el-tag size="small" type="success">+24%</el-tag>
                  </div>
                  <div class="stat-value">{{ stats.totalViews }}</div>
                  <div class="stat-footer">{{ $t('merchant.stats.viewsIncrease') }} 156 {{ $t('merchant.stats.views') }}</div>
                </div>
              </el-col>
            </el-row>
          </div>

          <div class="dashboard-grid">
            <div class="main-chart-section">
              <el-card class="chart-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <span>{{ $t('merchant.dashboard.dataTrend') }}</span>
                    <el-radio-group v-model="chartPeriod" size="small">
                      <el-radio-button label="week">{{ $t('merchant.dashboard.last7Days') }}</el-radio-button>
                      <el-radio-button label="month">{{ $t('merchant.dashboard.last30Days') }}</el-radio-button>
                    </el-radio-group>
                  </div>
                </template>
                <div class="chart-container">
                  <div id="orderChart" style="height: 350px;"></div>
                </div>
              </el-card>
            </div>
            
            <div class="side-panel-section">
              <el-card class="todo-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <span>{{ $t('merchant.dashboard.todoList') }}</span>
                    <el-badge :value="3" class="item" type="danger" />
                  </div>
                </template>
                <div class="todo-list">
                  <div class="todo-item">
                    <el-checkbox v-model="todo1">{{ $t('merchant.todo.confirmNewOrders') }} (2)</el-checkbox>
                    <el-tag size="small" type="danger" effect="plain">{{ $t('merchant.todo.urgent') }}</el-tag>
                  </div>
                  <div class="todo-item">
                    <el-checkbox v-model="todo2">{{ $t('merchant.todo.replyBadReviews') }} (1)</el-checkbox>
                    <el-tag size="small" type="warning" effect="plain">{{ $t('merchant.todo.important') }}</el-tag>
                  </div>
                  <div class="todo-item">
                    <el-checkbox v-model="todo3">{{ $t('merchant.todo.updateInventory') }}</el-checkbox>
                    <el-tag size="small" type="info" effect="plain">{{ $t('merchant.todo.normal') }}</el-tag>
                  </div>
                </div>
              </el-card>
              
              <el-card class="notice-card" shadow="hover">
                 <template #header>
                  <div class="card-header">
                    <span>{{ $t('merchant.dashboard.systemNotice') }}</span>
                  </div>
                </template>
                <div class="notice-list">
                  <div class="notice-item">
                    <span class="notice-tag">{{ $t('merchant.notice.activity') }}</span>
                    <span class="notice-title">{{ $t('merchant.notice.springPromotion') }}</span>
                  </div>
                  <div class="notice-item">
                    <span class="notice-tag new">{{ $t('merchant.notice.system') }}</span>
                    <span class="notice-title">{{ $t('merchant.notice.systemUpgrade') }}</span>
                  </div>
                </div>
              </el-card>
            </div>
          </div>

          <div class="recent-orders">
            <el-card class="chart-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>{{ $t('merchant.dashboard.recentOrders') }}</span>
                  <el-button link type="primary" @click="handleMenuSelect('orders')">{{ $t('common.viewAll') }}</el-button>
                </div>
              </template>
              <el-table :data="recentOrders" style="width: 100%">
                <el-table-column prop="id" :label="$t('merchant.dashboard.orderNumber')" width="120" />
                <el-table-column prop="customerName" :label="$t('merchant.dashboard.customer')" />
                <el-table-column prop="scenicName" :label="projectLabel" />
                <el-table-column prop="price" :label="$t('merchant.dashboard.amount')" width="100">
                  <template #default="scope">
                    ¥{{ scope.row.price }}
                  </template>
                </el-table-column>
                <el-table-column prop="status" :label="$t('common.status')" width="100">
                  <template #default="scope">
                    <el-tag :type="getStatusType(scope.row.status)">
                      {{ getStatusText(scope.row.status) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" :label="$t('merchant.dashboard.orderTime')" width="180">
                  <template #default="scope">
                    {{ formatDate(scope.row.createTime) }}
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </div>
        </div>

        <!-- 资质申请页面 -->
        <div v-else-if="activeMenu === 'qualification'" class="page-content">
          <QualificationApply />
        </div>
        
        
        <!-- 其他页面内容 -->
        <div v-else class="page-content">
          <router-view />
        </div>
      </div>
    </div>
  </div>

  <!-- 资质申请提醒弹窗 -->
  <el-dialog
    v-model="showQualificationNotice"
    title="资质申请提醒"
    width="460px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
  >
    <div style="display:flex;align-items:flex-start;gap:12px;">
      <el-icon size="24" color="#e6a23c" style="flex-shrink:0;margin-top:2px;"><Warning /></el-icon>
      <p style="margin:0;line-height:1.6;color:#606266;">
        您还未进行资质申请，无法上架在系统。请前往「资质申请」页面提交材料，审核通过后即可在游客端展示。
      </p>
    </div>
    <div style="margin-top:16px;">
      <el-checkbox v-model="neverShowAgain">不再提示</el-checkbox>
    </div>
    <template #footer>
      <el-button @click="handleQualificationLater">稍后再说</el-button>
      <el-button type="primary" @click="handleQualificationApply">立即申请</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '../../stores/user'
import { useLocaleStore } from '../../stores/locale'
import { getMerchantByUserId } from '../../api/merchant'
import { merchantApplicationApi } from '../../api/merchantApplication'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { 
  Food, 
  House, 
  ArrowDown, 
  Odometer, 
  Shop, 
  Document, 
  ChatDotRound, 
  DocumentChecked, 
  Calendar, 
  TrendCharts,
  Money,
  Star,
  View,
  Sunny,
  Plus,
  Present,
  Ticket,
  User,
  SwitchButton,
  Bell,
  Warning
} from '@element-plus/icons-vue'
import QualificationApply from './QualificationApply.vue'

import realtimeSync from '../../utils/websocket'
import LanguageSwitcher from '../../components/LanguageSwitcher.vue'

const { t } = useI18n()

const router = useRouter()
const userStore = useUserStore()
const localeStore = useLocaleStore()
const merchantInfo = ref(null)

let chartInstance = null // 保存图表实例

const chartPeriod = ref('week')
const todo1 = ref(false)
const todo2 = ref(false)
const todo3 = ref(false)

// 时间问候语
const timeGreeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return t('merchant.welcome.dawn')
  if (hour < 9) return t('merchant.welcome.morning')
  if (hour < 12) return t('merchant.welcome.morning')
  if (hour < 14) return t('merchant.welcome.noon')
  if (hour < 17) return t('merchant.welcome.afternoon')
  if (hour < 19) return t('merchant.welcome.evening')
  return t('merchant.welcome.night')
})

const handleQuickAction = (action) => {
  if (action === 'orders') {
    activeMenu.value = 'orders'
    router.push('/merchant/orders')
  } else if (action === 'reviews') {
    activeMenu.value = 'reviews'
    router.push('/merchant/reviews')
  } else if (action === 'promotion') {
    router.push('/merchant/activities')
  } else {
    // 暂未实现的功能
    ElMessage.info(t('merchant.developing'))
  }
}

// 根据商家分类动态显示标题
const merchantTitle = computed(() => {
  const category = merchantInfo.value?.category
  if (category === 'FOOD') {
    return t('merchant.title.food')
  } else if (category === 'HOTEL') {
    return t('merchant.title.hotel')
  } else if (category === 'CERAMIC') {
    return t('merchant.title.ceramic')
  } else if (category === 'SCENIC') {
    return t('merchant.title.scenic')
  }
  return t('merchant.title.default')
})

// 商家分类标签
const categoryLabel = computed(() => {
  const category = merchantInfo.value?.category
  if (category === 'FOOD') return t('merchant.category.food')
  if (category === 'HOTEL') return t('merchant.category.hotel')
  if (category === 'CERAMIC') return t('merchant.category.ceramic')
  if (category === 'SCENIC') return t('merchant.category.scenic')
  return t('merchant.category.default')
})

// 欢迎标题
const welcomeTitle = computed(() => {
  const category = merchantInfo.value?.category
  const time = timeGreeting.value
  if (category === 'FOOD') return `${time}，${t('merchant.welcome.food')}`
  if (category === 'HOTEL') return `${time}，${t('merchant.welcome.hotel')}`
  if (category === 'CERAMIC') return `${time}，${t('merchant.welcome.ceramic')}`
  if (category === 'SCENIC') return `${time}，${t('merchant.welcome.scenic')}`
  return `${time}，${t('merchant.welcome.default')}`
})

// 欢迎信息
const welcomeMessage = computed(() => {
  const category = merchantInfo.value?.category
  if (category === 'FOOD') return t('merchant.welcome.foodMessage')
  if (category === 'HOTEL') return t('merchant.welcome.hotelMessage')
  if (category === 'CERAMIC') return t('merchant.welcome.ceramicMessage')
  if (category === 'SCENIC') return t('merchant.welcome.scenicMessage')
  return t('merchant.welcome.defaultMessage')
})

// 根据商家分类动态显示项目名称标签
const projectLabel = computed(() => {
  const category = merchantInfo.value?.category || 'OTHER'
  const labelMap = {
    CERAMIC: t('merchant.dashboard.project.ceramic'),
    FOOD: t('merchant.dashboard.project.food'),
    HOTEL: t('merchant.dashboard.project.hotel'),
    OTHER: t('merchant.dashboard.project.default')
  }
  return labelMap[category] || t('merchant.dashboard.project.default')
})

const activeMenu = ref('dashboard')

const stats = ref({
  totalOrders: 156,
  totalRevenue: 45680,
  averageRating: 4.8,
  totalViews: 2340
})

// 根据商家分类生成不同的订单示例数据
const getOrdersByCategory = (category) => {
  const ordersMap = {
    CERAMIC: [
      { id: 'ORD001', customerName: '张三', scenicName: '陶瓷制作体验', price: 95, status: 'pending', createTime: new Date() },
      { id: 'ORD002', customerName: '李四', scenicName: '陶艺拉坯体验', price: 0, status: 'confirmed', createTime: new Date(Date.now() - 86400000) },
      { id: 'ORD003', customerName: '王五', scenicName: '陶瓷绘画体验', price: 80, status: 'confirmed', createTime: new Date(Date.now() - 172800000) }
    ],
    FOOD: [
      { id: 'ORD001', customerName: '张三', scenicName: '特色菜品套餐', price: 128, status: 'pending', createTime: new Date() },
      { id: 'ORD002', customerName: '李四', scenicName: '定制菜单服务', price: 0, status: 'confirmed', createTime: new Date(Date.now() - 86400000) },
      { id: 'ORD003', customerName: '王五', scenicName: '外卖服务', price: 68, status: 'confirmed', createTime: new Date(Date.now() - 172800000) }
    ],
    HOTEL: [
      { id: 'ORD001', customerName: '张三', scenicName: '标准间预订', price: 298, status: 'pending', createTime: new Date() },
      { id: 'ORD002', customerName: '李四', scenicName: '豪华间预订', price: 0, status: 'confirmed', createTime: new Date(Date.now() - 86400000) },
      { id: 'ORD003', customerName: '王五', scenicName: '套房预订', price: 588, status: 'confirmed', createTime: new Date(Date.now() - 172800000) }
    ],
    OTHER: [
      { id: 'ORD001', customerName: '张三', scenicName: '通用服务', price: 100, status: 'pending', createTime: new Date() },
      { id: 'ORD002', customerName: '李四', scenicName: '定制服务', price: 0, status: 'confirmed', createTime: new Date(Date.now() - 86400000) },
      { id: 'ORD003', customerName: '王五', scenicName: '咨询服务', price: 50, status: 'confirmed', createTime: new Date(Date.now() - 172800000) }
    ]
  }
  return ordersMap[category] || ordersMap.OTHER
}

const recentOrders = ref([])

const handleMenuSelect = (key) => {
  activeMenu.value = key
  if (key !== 'dashboard') {
    router.push(`/merchant/${key}`)
  }
}

const handleUserCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/merchant/profile')
  }
}

const getStatusType = (status) => {
  const types = {
    pending: 'warning',
    confirmed: 'success',
    cancelled: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    pending: t('merchant.orderStatus.pending'),
    confirmed: t('merchant.orderStatus.confirmed'),
    cancelled: t('merchant.orderStatus.cancelled')
  }
  return texts[status] || status
}

const formatDate = (date) => {
  return new Date(date).toLocaleString()
}

const initCharts = () => {
  const chartDom = document.getElementById('orderChart')
  if (!chartDom) return
  
  // 如果图表已存在，先销毁
  if (chartInstance) {
    chartInstance.dispose()
  }
  
  chartInstance = echarts.init(chartDom)
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        crossStyle: {
          color: '#999'
        }
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    legend: {
      data: [t('merchant.dashboard.orders'), t('merchant.dashboard.revenue')]
    },
    xAxis: [
      {
        type: 'category',
        data: [
          t('merchant.dashboard.monday'),
          t('merchant.dashboard.tuesday'),
          t('merchant.dashboard.wednesday'),
          t('merchant.dashboard.thursday'),
          t('merchant.dashboard.friday'),
          t('merchant.dashboard.saturday'),
          t('merchant.dashboard.sunday')
        ],
        axisPointer: {
          type: 'shadow'
        }
      }
    ],
    yAxis: [
      {
        type: 'value',
        name: t('merchant.dashboard.orders'),
        min: 0,
        max: 30,
        interval: 5,
        axisLabel: {
          formatter: '{value}'
        }
      },
      {
        type: 'value',
        name: t('merchant.dashboard.revenue'),
        min: 0,
        max: 3000,
        interval: 500,
        axisLabel: {
          formatter: '¥{value}'
        }
      }
    ],
    series: [
      {
        name: t('merchant.dashboard.orders'),
        type: 'bar',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        },
        data: [12, 8, 15, 20, 18, 25, 22]
      },
      {
        name: t('merchant.dashboard.revenue'),
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        itemStyle: {
          color: '#52c41a'
        },
        data: [1200, 800, 1500, 2000, 1800, 2500, 2200]
      }
    ]
  }
  chartInstance.setOption(option)
  
  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    if (chartInstance) {
      chartInstance.resize()
    }
  })
}

// 监听语言变化，重新初始化图表
watch(() => localeStore.currentLocale, () => {
  if (activeMenu.value === 'dashboard') {
    setTimeout(() => {
      initCharts()
    }, 100)
  }
})

// 加载商家信息
const loadMerchantInfo = async () => {
  try {
    // 经过 axios 封装后，这里返回的就是后端的 { success, data, message } 对象
    const res = await getMerchantByUserId(userStore.user.id)

    if (res.success && res.data) {
      merchantInfo.value = res.data
      recentOrders.value = getOrdersByCategory(merchantInfo.value.category || 'OTHER')

      // 从商家信息中获取真实评分数据（后端已经通过 enrichMerchantWithRating 计算好）
      const ratingValue = merchantInfo.value.rating
      console.log('商家评分数据:', { ratingValue, merchantInfo: merchantInfo.value })
      
      if (ratingValue != null && ratingValue !== undefined) {
        const rating =
          typeof ratingValue === 'number'
            ? ratingValue
            : parseFloat(ratingValue) || 0
        // 保留1位小数，与游客端保持一致
        stats.value.averageRating = rating > 0 ? parseFloat(rating.toFixed(1)) : 0
        console.log('更新仪表盘评分为:', stats.value.averageRating)
      } else {
        // 如果没有评分，使用默认值 0
        stats.value.averageRating = 0
        console.log('商家没有评分数据，设置为0')
      }
    } else {
      // 理论上 res.success 为 false 时已经在拦截器里抛错，这里只是兜底
      console.warn('获取商家信息失败，res:', res)
      stats.value.averageRating = 0
      recentOrders.value = getOrdersByCategory('OTHER')
    }
  } catch (error) {
    console.error('加载商家信息失败:', error)
    // 请求失败时，不再保留 4.8 这种写死值，降级为 0
    stats.value.averageRating = 0
    recentOrders.value = getOrdersByCategory('OTHER')
  }
}

// 处理实时数据更新（商家更新时重新加载评分）
const handleRealtimeUpdate = async (updateInfo) => {
  const { operation, data: updateData } = updateInfo
  
  // 只处理商家更新操作
  if (operation === 'update' && updateData?.id) {
    // 检查是否是当前商家的更新
    if (merchantInfo.value && String(merchantInfo.value.id) === String(updateData.id)) {
      console.log('收到商家实时更新，重新加载评分数据:', updateData)
      // 重新加载商家信息以获取最新评分
      await loadMerchantInfo()
    }
  }
}

// 检查资质申请状态，未申请则弹出提示（支持"不再提示"）
const checkQualificationStatus = async () => {
  const userId = userStore.user?.id
  if (!userId) return

  // 商家已启用（enabled=true）说明已过审，无需提示
  if (merchantInfo.value?.enabled !== false) return

  const dismissKey = `qualification_notice_dismissed_${userId}`
  if (localStorage.getItem(dismissKey) === '1') return

  try {
    const res = await merchantApplicationApi.getApplicationByUserId(userId)
    const hasApplication = res?.success && res?.data
    if (hasApplication) return
  } catch (e) {
    // 404 或其他错误视为没有申请记录，继续弹提示
  }

  showQualificationNotice.value = true
}

const showQualificationNotice = ref(false)
const neverShowAgain = ref(false)

const handleQualificationApply = () => {
  if (neverShowAgain.value) {
    localStorage.setItem(`qualification_notice_dismissed_${userStore.user?.id}`, '1')
  }
  showQualificationNotice.value = false
  activeMenu.value = 'qualification'
  router.push('/merchant/qualification')
}

const handleQualificationLater = () => {
  if (neverShowAgain.value) {
    localStorage.setItem(`qualification_notice_dismissed_${userStore.user?.id}`, '1')
  }
  showQualificationNotice.value = false
}

onMounted(async () => {
  await loadMerchantInfo()
  await checkQualificationStatus()
  setTimeout(() => {
    initCharts()
  }, 100)
  
  // 连接WebSocket并订阅商家更新
  if (!realtimeSync.isConnected) {
    realtimeSync.connect()
  }
  realtimeSync.subscribe('merchant', handleRealtimeUpdate)
})

onUnmounted(() => {
  // 取消订阅
  realtimeSync.unsubscribe('merchant', handleRealtimeUpdate)
  // 销毁图表实例
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})
</script>

<style scoped>
.merchant-home {
  min-height: 100vh;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
}

.merchant-header {
  background: white;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0 24px;
  position: relative;
  z-index: 10;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 64px;
  max-width: 1400px;
  margin: 0 auto;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo h1 {
  margin: 0;
  color: #1890ff;
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.merchant-badge {
  padding: 2px 10px;
  background: #e6f7ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-dropdown:hover {
  background-color: rgba(0, 0, 0, 0.025);
}

.user-dropdown span {
  font-size: 14px;
  color: #333;
}

/* Match tourist UI feel for dropdown items */
:deep(.user-dropdown-menu) {
  padding: 8px;
  border-radius: 12px;
}

:deep(.user-dropdown-menu .el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
  border-radius: 8px;
  margin: 2px 0;
  transition: all 0.2s;
}

:deep(.user-dropdown-menu .el-dropdown-menu__item:hover) {
  background: #f5f7fa;
  color: #409eff;
}

/* Danger style for logout, like tourist header */
:deep(.user-dropdown-menu .el-dropdown-menu__item.logout-item) {
  color: #f56c6c;
}

:deep(.user-dropdown-menu .el-dropdown-menu__item.logout-item:hover) {
  background: #fef0f0;
  color: #f56c6c;
}

.merchant-content {
  display: flex;
  max-width: 1400px;
  margin: 20px auto;
  width: 100%;
  padding: 0 20px;
  flex: 1;
  gap: 20px;
}

.sidebar {
  width: 240px;
  flex-shrink: 0;
  background: white;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  height: fit-content;
}

.merchant-menu {
  border-right: none;
  padding: 8px 0;
}

.merchant-menu :deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
  margin: 4px 8px;
  border-radius: 4px;
}

.merchant-menu :deep(.el-menu-item.is-active) {
  background-color: #e6f7ff;
  color: #1890ff;
  font-weight: 500;
}

.merchant-menu :deep(.el-menu-item:hover) {
  background-color: rgba(0, 0, 0, 0.025);
}

.merchant-menu :deep(.el-menu-item.is-active:hover) {
  background-color: #e6f7ff;
}

.merchant-menu :deep(.el-icon) {
  font-size: 18px;
  margin-right: 10px;
}

.main-content {
  flex: 1;
  min-width: 0; /* 防止子元素撑开 */
}

/* Dashboard Styles */
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.welcome-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
}

.welcome-banner {
  background: linear-gradient(135deg, #e6f7ff 0%, #f0f5ff 100%);
  border-radius: 12px;
  padding: 30px;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  border: 1px solid #bae7ff;
}

.welcome-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 1;
}

.welcome-text h2 {
  margin: 0 0 12px 0;
  color: #1f1f1f;
  font-size: 24px;
}

.welcome-text p {
  margin: 0 0 20px 0;
  color: #595959;
  font-size: 14px;
}

.weather-widget {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #1890ff;
  font-weight: 500;
  font-size: 14px;
  background: rgba(255, 255, 255, 0.6);
  padding: 6px 12px;
  border-radius: 20px;
  width: fit-content;
}

.welcome-decoration {
  color: rgba(24, 144, 255, 0.1);
  transform: rotate(-15deg) translateY(10px);
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.action-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  border: 1px solid #f0f0f0;
}

.action-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: #1890ff;
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.action-card span {
  font-size: 14px;
  color: #595959;
  font-weight: 500;
}

.stats-cards {
  margin-bottom: 0;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  position: relative;
  overflow: hidden;
  height: 140px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  border: 1px solid #f0f0f0;
}

.stat-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.stat-icon-bg {
  position: absolute;
  right: -10px;
  bottom: -10px;
  font-size: 100px;
  opacity: 0.1;
  transform: rotate(-15deg);
}

.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-label {
  color: #8c8c8c;
  font-size: 14px;
}

.stat-value {
  font-size: 32px;
  font-weight: 600;
  color: #1f1f1f;
  margin: 12px 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial;
}

.stat-footer {
  font-size: 12px;
  color: #8c8c8c;
}

/* Theme Colors */
.blue-theme .stat-icon-bg { color: #1890ff; }
.purple-theme .stat-icon-bg { color: #722ed1; }
.orange-theme .stat-icon-bg { color: #fa8c16; }
.green-theme .stat-icon-bg { color: #52c41a; }

.dashboard-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
}

.chart-card {
  border-radius: 12px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  border: none;
}

.chart-card :deep(.el-card__header) {
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 500;
  color: #1f1f1f;
}

.side-panel-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.todo-card, .notice-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.todo-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
}

.todo-item:last-child {
  border-bottom: none;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.notice-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  font-size: 14px;
  line-height: 1.5;
  color: #595959;
  cursor: pointer;
}

.notice-item:hover {
  color: #1890ff;
}

.notice-tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  background: #f5f5f5;
  color: #8c8c8c;
  flex-shrink: 0;
}

.notice-tag.new {
  background: #fff1f0;
  color: #f5222d;
}

.page-content {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  min-height: 500px;
}

@media (max-width: 1200px) {
  .welcome-section, .dashboard-grid {
    grid-template-columns: 1fr;
  }
  
  .quick-actions {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 768px) {
  .merchant-content {
    flex-direction: column;
    padding: 10px;
  }
  
  .sidebar {
    width: 100%;
    margin-bottom: 20px;
  }
  
  .quick-actions {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .merchant-menu {
    display: flex;
    overflow-x: auto;
    padding: 0;
  }
  
  .merchant-menu :deep(.el-menu-item) {
    margin: 0;
    border-radius: 0;
    flex-shrink: 0;
  }
}
</style>
