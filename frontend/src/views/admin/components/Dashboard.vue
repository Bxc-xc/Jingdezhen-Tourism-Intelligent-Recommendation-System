<template>
  <div class="dashboard">
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-main">
            <div class="icon-wrap user"><el-icon><User /></el-icon></div>
            <div>
              <div class="stat-value">{{ data.statCards.totalUsers.toLocaleString() }}</div>
              <div class="stat-label">总用户数</div>
            </div>
          </div>
          <div class="trend up">
            <span>↑</span>
            <span>{{ trendText(data.statCards.userTrend) }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-main">
            <div class="icon-wrap order"><el-icon><Document /></el-icon></div>
            <div>
              <div class="stat-value">{{ data.statCards.totalOrders.toLocaleString() }}</div>
              <div class="stat-label">总订单数</div>
            </div>
          </div>
          <div class="trend up">
            <span>↑</span>
            <span>{{ trendText(data.statCards.orderTrend) }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-main">
            <div class="icon-wrap growth"><el-icon><UserFilled /></el-icon></div>
            <div>
              <div class="stat-value">{{ data.statCards.todayNewUsers }}</div>
              <div class="stat-label">今日新增用户</div>
            </div>
          </div>
          <div class="trend neutral">实时更新</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-main">
            <div class="icon-wrap today-order"><el-icon><Histogram /></el-icon></div>
            <div>
              <div class="stat-value">{{ data.statCards.todayOrders }}</div>
              <div class="stat-label">今日订单量</div>
            </div>
          </div>
          <div class="trend neutral">较昨日波动</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="panel-card">
      <template #header>
        <div class="panel-header">
          <span>订单统计</span>
          <el-radio-group v-model="rangeType" size="small" @change="initCharts">
            <el-radio-button label="7天" value="7d" />
            <el-radio-button label="30天" value="30d" />
          </el-radio-group>
        </div>
      </template>
      <div ref="orderTrendChartRef" class="chart"></div>
    </el-card>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="16">
        <el-card class="panel-card">
          <template #header>
            <span>用户增长趋势</span>
          </template>
          <div ref="userTrendChartRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="panel-card merchant-card">
          <template #header>
            <div class="panel-header">
              <span>商家申请待审核</span>
              <el-tag type="warning">{{ data.merchantApplications.length }} 条</el-tag>
            </div>
          </template>
          <div class="merchant-list">
            <div v-for="item in data.merchantApplications" :key="item.id" class="merchant-item">
              <div class="merchant-title">{{ item.shopName }}</div>
              <div class="merchant-meta">{{ item.type }} · {{ item.submittedAt }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, reactive, ref } from 'vue'
import * as echarts from 'echarts'
import { getDashboardData } from '../../../api/adminDashboard'

const rangeType = ref('7d')
const orderTrendChartRef = ref(null)
const userTrendChartRef = ref(null)
const charts = reactive({
  order: null,
  user: null
})

const data = reactive({
  statCards: {
    totalUsers: 0,
    totalOrders: 0,
    todayNewUsers: 0,
    todayOrders: 0,
    userTrend: 0,
    orderTrend: 0
  },
  orderTrend7d: { dates: [], orderData: [], userData: [] },
  orderTrend30d: { dates: [], orderData: [], userData: [] },
  userTrend30d: { dates: [], userData: [] },
  merchantApplications: []
})

const trendText = (value) => `${value > 0 ? '+' : ''}${value}%`

const initCharts = () => {
  if (!orderTrendChartRef.value || !userTrendChartRef.value) return

  const orderSource = rangeType.value === '7d' ? data.orderTrend7d : data.orderTrend30d
  charts.order?.dispose()
  charts.order = echarts.init(orderTrendChartRef.value)
  charts.order.setOption({
    color: ['#3B82F6', '#60A5FA'],
    tooltip: { trigger: 'axis' },
    legend: { data: ['订单量'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: orderSource.dates
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: '订单量',
        type: 'line',
        smooth: true,
        areaStyle: { opacity: 0.16 },
        data: orderSource.orderData
      }
    ]
  })

  charts.user?.dispose()
  charts.user = echarts.init(userTrendChartRef.value)
  charts.user.setOption({
    color: ['#2563EB'],
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: data.userTrend30d.dates
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: '新增用户',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { width: 3 },
        data: data.userTrend30d.userData
      }
    ]
  })
}

const loadDashboard = async () => {
  const dashboardData = await getDashboardData()
  Object.assign(data, dashboardData)
  initCharts()
}

const handleResize = () => {
  charts.order?.resize()
  charts.user?.resize()
}

onMounted(async () => {
  await loadDashboard()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  charts.order?.dispose()
  charts.user?.dispose()
})
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stat-row {
  margin-bottom: 0;
}

.stat-card {
  border-radius: 12px;
  box-shadow: 0 6px 20px rgba(37, 99, 235, 0.1);
}

.stat-main {
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
}

.icon-wrap.user,
.icon-wrap.order,
.icon-wrap.growth,
.icon-wrap.today-order {
  background: linear-gradient(135deg, #2563eb 0%, #60a5fa 100%);
}

.stat-value {
  color: #0f172a;
  font-size: 24px;
  font-weight: 700;
}

.stat-label {
  color: #64748b;
  font-size: 13px;
}

.trend {
  margin-top: 10px;
  font-size: 12px;
  font-weight: 600;
}

.trend.up {
  color: #16a34a;
}

.trend.neutral {
  color: #64748b;
}

.panel-card {
  border-radius: 12px;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.06);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-row {
  margin-top: 0;
}

.chart {
  width: 100%;
  height: 320px;
}

.merchant-card .chart {
  height: auto;
}

.merchant-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 320px;
}

.merchant-item {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 10px 12px;
  background: #f8fafc;
}

.merchant-title {
  color: #0f172a;
  font-weight: 600;
}

.merchant-meta {
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
}
</style>
