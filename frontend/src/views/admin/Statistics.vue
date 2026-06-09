<template>
  <div class="statistics">
    <div class="page-header">
      <div class="page-title">
        <el-button link type="primary" @click="handleBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h2>统计分析</h2>
      </div>
      <div class="header-actions">
        <el-button @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
        <el-button type="primary" @click="exportReport">
          <el-icon><Download /></el-icon>
          导出报告
        </el-button>
      </div>
    </div>

    <!-- 时间范围选择 -->
    <el-card class="time-range-card">
      <el-form :model="timeForm" inline>
        <el-form-item label="统计时间">
          <el-date-picker
            v-model="timeForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleTimeChange"
            :popper-options="{
              strategy: 'fixed',
              modifiers: [
                { name: 'offset', options: { offset: [0, 4] } },
                { name: 'computeStyles', options: { adaptive: true, gpuAcceleration: true } }
              ]
            }"
          />
        </el-form-item>
        <el-form-item label="统计维度">
          <el-select 
            v-model="timeForm.dimension" 
            @change="handleDimensionChange"
            :popper-options="{
              strategy: 'fixed',
              modifiers: [
                { name: 'offset', options: { offset: [0, 4] } },
                { name: 'computeStyles', options: { adaptive: true, gpuAcceleration: true } }
              ]
            }"
          >
            <el-option label="按日" value="day" />
            <el-option label="按周" value="week" />
            <el-option label="按月" value="month" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadStatistics">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 核心指标 -->
    <el-row :gutter="20" class="metrics-row">
      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-content">
            <div class="metric-icon users">
              <el-icon><User /></el-icon>
            </div>
            <div class="metric-info">
              <div class="metric-value">{{ metrics.totalUsers }}</div>
              <div class="metric-label">总用户数</div>
              <div class="metric-change positive">+12.5%</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-content">
            <div class="metric-icon orders">
              <el-icon><Document /></el-icon>
            </div>
            <div class="metric-info">
              <div class="metric-value">{{ metrics.totalOrders }}</div>
              <div class="metric-label">总订单数</div>
              <div class="metric-change positive">+8.3%</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-content">
            <div class="metric-icon revenue">
              <el-icon><Money /></el-icon>
            </div>
            <div class="metric-info">
              <div class="metric-value">¥{{ metrics.totalRevenue }}</div>
              <div class="metric-label">总收入</div>
              <div class="metric-change positive">+15.2%</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-content">
            <div class="metric-icon conversion">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="metric-info">
              <div class="metric-value">{{ metrics.conversionRate }}%</div>
              <div class="metric-label">转化率</div>
              <div class="metric-change negative">-2.1%</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>用户增长趋势</span>
            <el-button link type="primary" @click="viewUserDetail">查看详情</el-button>
          </template>
          <div class="chart-container">
            <div id="userTrendChart" style="height: 300px;"></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>订单统计</span>
            <el-button link type="primary" @click="viewOrderDetail">查看详情</el-button>
          </template>
          <div class="chart-container">
            <div id="orderChart" style="height: 300px;"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>收入分析</span>
            <el-button link type="primary" @click="viewRevenueDetail">查看详情</el-button>
          </template>
          <div class="chart-container">
            <div id="revenueChart" style="height: 300px;"></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>热门景点排行</span>
            <el-button link type="primary" @click="viewScenicDetail">查看详情</el-button>
          </template>
          <div class="chart-container">
            <div id="scenicRankChart" style="height: 300px;"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细数据表格 -->
    <el-card class="data-table-card">
      <template #header>
        <span>详细数据</span>
        <el-button link type="primary" @click="exportTableData">导出数据</el-button>
      </template>
      
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="用户数据" name="users">
          <el-table :data="userData" style="width: 100%">
            <el-table-column prop="date" label="日期" width="120" />
            <el-table-column prop="newUsers" label="新增用户" width="100" />
            <el-table-column prop="activeUsers" label="活跃用户" width="100" />
            <el-table-column prop="totalUsers" label="总用户数" width="100" />
            <el-table-column prop="userGrowth" label="增长率" width="100">
              <template #default="scope">
                <span :class="scope.row.userGrowth >= 0 ? 'positive' : 'negative'">
                  {{ scope.row.userGrowth >= 0 ? '+' : '' }}{{ scope.row.userGrowth }}%
                </span>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="订单数据" name="orders">
          <el-table :data="orderData" style="width: 100%">
            <el-table-column prop="date" label="日期" width="120" />
            <el-table-column prop="newOrders" label="新增订单" width="100" />
            <el-table-column prop="completedOrders" label="完成订单" width="100" />
            <el-table-column prop="cancelledOrders" label="取消订单" width="100" />
            <el-table-column prop="orderValue" label="订单金额" width="120">
              <template #default="scope">
                ¥{{ scope.row.orderValue }}
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="景点数据" name="scenics">
          <el-table :data="scenicData" style="width: 100%">
            <el-table-column prop="name" label="景点名称" />
            <el-table-column prop="views" label="浏览量" width="100" />
            <el-table-column prop="orders" label="订单数" width="100" />
            <el-table-column prop="revenue" label="收入" width="120">
              <template #default="scope">
                ¥{{ scope.row.revenue }}
              </template>
            </el-table-column>
            <el-table-column prop="rating" label="评分" width="100">
              <template #default="scope">
                <el-rate 
                  :model-value="getRatingValue(scope.row.rating)" 
                  disabled 
                  size="small"
                  show-score
                  text-color="#ff9900"
                />
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useRealtimeData } from '../../composables/useRealtimeData'
const router = useRouter()

// 实时数据
const { data: realtimeUsers } = useRealtimeData('user', [])
const { data: realtimeOrders } = useRealtimeData('order', [])
const { data: realtimeScenics } = useRealtimeData('scenic_spot', [])
const { data: realtimeMerchants } = useRealtimeData('merchant', [])

const handleBack = () => {
  router.back()
}

const loading = ref(false)
const activeTab = ref('users')

const timeForm = reactive({
  dateRange: [],
  dimension: 'day'
})

const metrics = ref({
  totalUsers: 1234,
  totalOrders: 456,
  totalRevenue: 89650,
  conversionRate: 12.5
})

const userData = ref([
  { date: '2024-01-01', newUsers: 15, activeUsers: 120, totalUsers: 1200, userGrowth: 1.2 },
  { date: '2024-01-02', newUsers: 18, activeUsers: 135, totalUsers: 1218, userGrowth: 1.5 },
  { date: '2024-01-03', newUsers: 22, activeUsers: 142, totalUsers: 1240, userGrowth: 1.8 },
  { date: '2024-01-04', newUsers: 16, activeUsers: 138, totalUsers: 1256, userGrowth: 1.3 },
  { date: '2024-01-05', newUsers: 20, activeUsers: 145, totalUsers: 1276, userGrowth: 1.6 }
])

const orderData = ref([
  { date: '2024-01-01', newOrders: 25, completedOrders: 23, cancelledOrders: 2, orderValue: 4560 },
  { date: '2024-01-02', newOrders: 32, completedOrders: 30, cancelledOrders: 2, orderValue: 5890 },
  { date: '2024-01-03', newOrders: 28, completedOrders: 26, cancelledOrders: 2, orderValue: 5120 },
  { date: '2024-01-04', newOrders: 35, completedOrders: 33, cancelledOrders: 2, orderValue: 6780 },
  { date: '2024-01-05', newOrders: 30, completedOrders: 28, cancelledOrders: 2, orderValue: 5890 }
])

const scenicData = ref([
  { name: '古窑民俗博览区', views: 1234, orders: 89, revenue: 8455, rating: 4.8 },
  { name: '景德镇陶瓷博物馆', views: 987, orders: 67, revenue: 0, rating: 4.6 },
  { name: '三宝国际陶艺村', views: 876, orders: 54, revenue: 4320, rating: 4.7 },
  { name: '瑶里古镇', views: 654, orders: 43, revenue: 6450, rating: 4.5 },
  { name: '陶溪川文创街区', views: 543, orders: 32, revenue: 0, rating: 4.4 }
])

const handleTimeChange = () => {
  loadStatistics()
}

const handleDimensionChange = () => {
  loadStatistics()
}

const loadStatistics = () => {
  loading.value = true
  setTimeout(() => {
    // 若有实时数据，覆盖核心指标
    metrics.value = {
      totalUsers: realtimeUsers.value?.length ?? metrics.value.totalUsers,
      totalOrders: realtimeOrders.value?.length ?? metrics.value.totalOrders,
      totalRevenue: metrics.value.totalRevenue,
      conversionRate: metrics.value.conversionRate
    }
    loading.value = false
    initCharts()
    ElMessage.success('数据加载完成')
  }, 1000)
}

const refreshData = () => {
  loadStatistics()
}

const exportReport = () => {
  ElMessage.success('报告导出功能开发中...')
}

// 获取评分值（确保返回数字类型）
const getRatingValue = (rating) => {
  if (rating === null || rating === undefined) {
    return 0
  }
  // 如果是字符串，转换为数字
  if (typeof rating === 'string') {
    const num = parseFloat(rating)
    return isNaN(num) ? 0 : num
  }
  // 如果是数字，直接返回
  if (typeof rating === 'number') {
    return rating
  }
  // 如果是对象（BigDecimal），尝试获取数值
  if (typeof rating === 'object' && rating !== null) {
    if (rating.value !== undefined) {
      return Number(rating.value) || 0
    }
    if (rating.toString) {
      const num = parseFloat(rating.toString())
      return isNaN(num) ? 0 : num
    }
  }
  // 其他情况返回0
  return 0
}

const exportTableData = () => {
  ElMessage.success('数据导出功能开发中...')
}

const viewUserDetail = () => {
  activeTab.value = 'users'
}

const viewOrderDetail = () => {
  activeTab.value = 'orders'
}

const viewRevenueDetail = () => {
  ElMessage.info('收入详情功能开发中...')
}

const viewScenicDetail = () => {
  activeTab.value = 'scenics'
}

const handleTabChange = (tab) => {
  activeTab.value = tab
}

const initCharts = () => {
  // 用户增长趋势图
  const userTrendChart = echarts.init(document.getElementById('userTrendChart'))
  const userTrendOption = {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: ['1月', '2月', '3月', '4月', '5月', '6月']
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      name: '新增用户',
      data: [120, 200, 150, 80, 70, 110],
      type: 'line',
      smooth: true,
      areaStyle: {}
    }]
  }
  userTrendChart.setOption(userTrendOption)

  // 订单统计图
  const orderChart = echarts.init(document.getElementById('orderChart'))
  const orderOption = {
    tooltip: {
      trigger: 'item'
    },
    series: [{
      type: 'pie',
      radius: '50%',
      data: [
        { value: 335, name: '已完成' },
        { value: 310, name: '进行中' },
        { value: 234, name: '已取消' },
        { value: 135, name: '待支付' }
      ],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  }
  orderChart.setOption(orderOption)

  // 收入分析图
  const revenueChart = echarts.init(document.getElementById('revenueChart'))
  const revenueOption = {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: ['1月', '2月', '3月', '4月', '5月', '6月']
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      name: '收入',
      data: [12000, 15000, 18000, 16000, 20000, 22000],
      type: 'bar',
      itemStyle: {
        color: '#409eff'
      }
    }]
  }
  revenueChart.setOption(revenueOption)

  // 热门景点排行图
  const scenicRankChart = echarts.init(document.getElementById('scenicRankChart'))
  const scenicRankOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    xAxis: {
      type: 'value'
    },
    yAxis: {
      type: 'category',
      data: ['陶溪川文创街区', '瑶里古镇', '三宝国际陶艺村', '景德镇陶瓷博物馆', '古窑民俗博览区']
    },
    series: [{
      name: '浏览量',
      data: [543, 654, 876, 987, 1234],
      type: 'bar',
      itemStyle: {
        color: '#67c23a'
      }
    }]
  }
  scenicRankChart.setOption(scenicRankOption)
}

onMounted(() => {
  // 设置默认时间范围
  const endDate = new Date()
  const startDate = new Date()
  startDate.setMonth(startDate.getMonth() - 1)
  
  timeForm.dateRange = [
    startDate.toISOString().split('T')[0],
    endDate.toISOString().split('T')[0]
  ]
  
  setTimeout(() => {
    initCharts()
  }, 100)
})

// 实时数据变更时更新核心指标
watch(
  [realtimeUsers, realtimeOrders, realtimeScenics, realtimeMerchants],
  () => {
    metrics.value = {
      totalUsers: realtimeUsers.value?.length ?? metrics.value.totalUsers,
      totalOrders: realtimeOrders.value?.length ?? metrics.value.totalOrders,
      totalRevenue: metrics.value.totalRevenue,
      conversionRate: metrics.value.conversionRate
    }
  }
)
</script>

<style scoped>
.statistics {
  padding: 20px;
  background: #fff;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.time-range-card {
  margin-bottom: 20px;
}

.metrics-row {
  margin-bottom: 20px;
}

.metric-card {
  height: 120px;
}

.metric-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.metric-icon {
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

.metric-icon.users {
  background: #409eff;
}

.metric-icon.orders {
  background: #67c23a;
}

.metric-icon.revenue {
  background: #e6a23c;
}

.metric-icon.conversion {
  background: #f56c6c;
}

.metric-info {
  flex: 1;
}

.metric-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.metric-label {
  color: #909399;
  font-size: 14px;
  margin-bottom: 4px;
}

.metric-change {
  font-size: 12px;
  font-weight: 600;
}

.metric-change.positive {
  color: #67c23a;
}

.metric-change.negative {
  color: #f56c6c;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-container {
  width: 100%;
}

.data-table-card {
  margin-bottom: 20px;
}

.positive {
  color: #67c23a;
}

.negative {
  color: #f56c6c;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .metrics-row .el-col {
    margin-bottom: 16px;
  }
  
  .charts-row .el-col {
    margin-bottom: 20px;
  }
  
  .time-range-card .el-form {
    flex-direction: column;
  }
  
  .time-range-card .el-form-item {
    margin-right: 0;
    margin-bottom: 16px;
  }
}
</style>
