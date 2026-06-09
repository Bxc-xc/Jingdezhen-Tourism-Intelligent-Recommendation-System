<template>
  <div class="statistics-page">
    <div class="page-header">
      <div class="header-left">
        <el-button @click="$router.back()" circle class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <div class="title-section">
          <h2>数据统计</h2>
          <span class="subtitle">全面掌握店铺经营状况</span>
        </div>
      </div>
      <div class="header-right">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :shortcuts="shortcuts"
          size="default"
          @change="handleDateChange"
          style="width: 260px;"
        />
        <el-button type="primary" class="export-btn" @click="exportData">
          <el-icon><Download /></el-icon> 导出报表
        </el-button>
      </div>
    </div>

    <div class="content-wrapper">
      <!-- 核心指标卡片 -->
      <el-row :gutter="20" class="metrics-row">
        <el-col :span="6" :xs="12">
          <div class="metric-card blue-theme">
            <div class="metric-icon">
              <el-icon><Money /></el-icon>
            </div>
            <div class="metric-content">
              <div class="metric-label">总销售额</div>
              <div class="metric-value">¥{{ metrics.totalRevenue.toLocaleString() }}</div>
              <div class="metric-trend">
                <span :class="metrics.revenueGrowth >= 0 ? 'up' : 'down'">
                  <el-icon v-if="metrics.revenueGrowth >= 0"><CaretTop /></el-icon>
                  <el-icon v-else><CaretBottom /></el-icon>
                  {{ Math.abs(metrics.revenueGrowth) }}%
                </span>
                <span class="trend-text">较上期</span>
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="6" :xs="12">
          <div class="metric-card purple-theme">
            <div class="metric-icon">
              <el-icon><Document /></el-icon>
            </div>
            <div class="metric-content">
              <div class="metric-label">订单总量</div>
              <div class="metric-value">{{ metrics.totalOrders.toLocaleString() }}</div>
              <div class="metric-trend">
                <span :class="metrics.orderGrowth >= 0 ? 'up' : 'down'">
                  <el-icon v-if="metrics.orderGrowth >= 0"><CaretTop /></el-icon>
                  <el-icon v-else><CaretBottom /></el-icon>
                  {{ Math.abs(metrics.orderGrowth) }}%
                </span>
                <span class="trend-text">较上期</span>
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="6" :xs="12">
          <div class="metric-card orange-theme">
            <div class="metric-icon">
              <el-icon><Wallet /></el-icon>
            </div>
            <div class="metric-content">
              <div class="metric-label">客单价</div>
              <div class="metric-value">¥{{ metrics.avgOrderValue }}</div>
              <div class="metric-trend">
                <span :class="metrics.aovGrowth >= 0 ? 'up' : 'down'">
                  <el-icon v-if="metrics.aovGrowth >= 0"><CaretTop /></el-icon>
                  <el-icon v-else><CaretBottom /></el-icon>
                  {{ Math.abs(metrics.aovGrowth) }}%
                </span>
                <span class="trend-text">较上期</span>
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="6" :xs="12">
          <div class="metric-card green-theme">
            <div class="metric-icon">
              <el-icon><View /></el-icon>
            </div>
            <div class="metric-content">
              <div class="metric-label">店铺访问量</div>
              <div class="metric-value">{{ metrics.totalViews.toLocaleString() }}</div>
              <div class="metric-trend">
                <span :class="metrics.viewGrowth >= 0 ? 'up' : 'down'">
                  <el-icon v-if="metrics.viewGrowth >= 0"><CaretTop /></el-icon>
                  <el-icon v-else><CaretBottom /></el-icon>
                  {{ Math.abs(metrics.viewGrowth) }}%
                </span>
                <span class="trend-text">较上期</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 图表区域 -->
      <div class="charts-section">
        <el-row :gutter="20">
          <el-col :span="16">
            <el-card class="chart-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>营收趋势</span>
                  <div class="chart-actions">
                    <el-radio-group v-model="revenueChartType" size="small">
                      <el-radio-button label="revenue">销售额</el-radio-button>
                      <el-radio-button label="orders">订单量</el-radio-button>
                    </el-radio-group>
                  </div>
                </div>
              </template>
              <div class="chart-container" id="trendChart" style="height: 320px;"></div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card class="chart-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>订单状态分布</span>
                </div>
              </template>
              <div class="chart-container" id="pieChart" style="height: 320px;"></div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 商品/服务排名 -->
      <div class="ranking-section">
        <el-card class="ranking-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>热销商品/服务排名</span>
              <el-button text type="primary">查看更多</el-button>
            </div>
          </template>
          <el-table :data="rankingList" style="width: 100%" :row-class-name="tableRowClassName" size="default">
            <el-table-column type="index" label="排名" width="80" align="center">
              <template #default="scope">
                <span class="rank-badge" :class="'rank-' + (scope.$index + 1)">{{ scope.$index + 1 }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="商品/服务名称" min-width="200">
              <template #default="scope">
                <div class="product-info">
                  <el-image :src="scope.row.image" class="product-thumb" fit="cover" />
                  <span class="product-name">{{ scope.row.name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="category" label="分类" width="100" />
            <el-table-column prop="sales" label="销量" width="100" sortable />
            <el-table-column prop="revenue" label="销售额" width="140" sortable>
              <template #default="scope">
                ¥{{ scope.row.revenue.toLocaleString() }}
              </template>
            </el-table-column>
            <el-table-column label="转化率" width="180">
              <template #default="scope">
                <div class="progress-wrapper">
                  <el-progress :percentage="scope.row.conversionRate" :status="scope.row.conversionRate > 80 ? 'success' : ''" :stroke-width="8" />
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import { Money, Document, Wallet, View, Download, CaretTop, CaretBottom, ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// 日期范围
const dateRange = ref([])
const shortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    },
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    },
  },
  {
    text: '最近三个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
      return [start, end]
    },
  },
]

// 核心指标数据
const metrics = ref({
  totalRevenue: 125800,
  revenueGrowth: 12.5,
  totalOrders: 456,
  orderGrowth: 8.2,
  avgOrderValue: 275,
  aovGrowth: 3.4,
  totalViews: 12450,
  viewGrowth: -2.1
})

// 图表控制
const revenueChartType = ref('revenue')
let trendChart = null
let pieChart = null

// 排名数据
const rankingList = ref([
  { name: '景德镇手绘青花瓷茶具套装', image: 'https://placehold.co/100x100?text=TeaSet', category: '陶瓷', sales: 128, revenue: 38144, conversionRate: 85 },
  { name: '传统陶艺拉坯体验课程', image: 'https://placehold.co/100x100?text=Course', category: '体验', sales: 96, revenue: 19200, conversionRate: 72 },
  { name: '玲珑瓷餐具家用套装', image: 'https://placehold.co/100x100?text=Plate', category: '陶瓷', sales: 75, revenue: 22350, conversionRate: 68 },
  { name: '精品窑变花瓶摆件', image: 'https://placehold.co/100x100?text=Vase', category: '陶瓷', sales: 54, revenue: 16200, conversionRate: 55 },
  { name: '儿童陶艺彩绘体验', image: 'https://placehold.co/100x100?text=Kids', category: '体验', sales: 42, revenue: 6300, conversionRate: 90 },
])

const handleDateChange = (val) => {
  ElMessage.success('数据已更新')
  // 这里应该调用后端API获取新日期范围内的数据
  updateCharts()
}

const exportData = () => {
  ElMessage.success('报表导出中，请稍候...')
}

const initTrendChart = () => {
  const chartDom = document.getElementById('trendChart')
  if (!chartDom) return
  trendChart = echarts.init(chartDom)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: revenueChartType.value === 'revenue' ? '销售额' : '订单量',
        type: 'line',
        smooth: true,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(24, 144, 255, 0.3)' },
            { offset: 1, color: 'rgba(24, 144, 255, 0.05)' }
          ])
        },
        itemStyle: {
          color: '#1890ff'
        },
        data: revenueChartType.value === 'revenue' 
          ? [12000, 13200, 10100, 13400, 9000, 23000, 21000]
          : [45, 52, 38, 55, 30, 85, 80]
      }
    ]
  }
  trendChart.setOption(option)
}

const initPieChart = () => {
  const chartDom = document.getElementById('pieChart')
  if (!chartDom) return
  pieChart = echarts.init(chartDom)
  
  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      bottom: '5%',
      left: 'center'
    },
    series: [
      {
        name: '订单状态',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: 1048, name: '已完成', itemStyle: { color: '#52c41a' } },
          { value: 735, name: '待支付', itemStyle: { color: '#fa8c16' } },
          { value: 580, name: '待发货', itemStyle: { color: '#1890ff' } },
          { value: 484, name: '已取消', itemStyle: { color: '#d9d9d9' } },
          { value: 300, name: '售后中', itemStyle: { color: '#ff4d4f' } }
        ]
      }
    ]
  }
  pieChart.setOption(option)
}

const updateCharts = () => {
  if (trendChart) {
    trendChart.setOption({
      series: [
        {
          name: revenueChartType.value === 'revenue' ? '销售额' : '订单量',
          data: revenueChartType.value === 'revenue' 
            ? [12000, 13200, 10100, 13400, 9000, 23000, 21000]
            : [45, 52, 38, 55, 30, 85, 80]
        }
      ]
    })
  }
}

watch(revenueChartType, () => {
  updateCharts()
})

onMounted(() => {
  // 设置默认日期范围为最近一周
  const end = new Date()
  const start = new Date()
  start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
  dateRange.value = [start, end]
  
  nextTick(() => {
    initTrendChart()
    initPieChart()
    
    window.addEventListener('resize', () => {
      trendChart && trendChart.resize()
      pieChart && pieChart.resize()
    })
  })
})
</script>

<style scoped>
.statistics-page {
  padding: 0;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 30px;
  background: white;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  border: none;
  background-color: #f5f7fa;
}

.back-btn:hover {
  background-color: #e6f7ff;
  color: #1890ff;
}

.title-section h2 {
  margin: 0;
  font-size: 20px;
  color: #1f1f1f;
  font-weight: 600;
  line-height: 1.4;
}

.subtitle {
  color: #8c8c8c;
  font-size: 13px;
  margin-top: 2px;
  display: block;
}

.header-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.export-btn {
  background-color: #f0f5ff;
  color: #1890ff;
  border-color: #adc6ff;
}

.export-btn:hover {
  background-color: #1890ff;
  color: #fff;
  border-color: #1890ff;
}

.content-wrapper {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px 24px;
}

/* Metric Cards */
.metrics-row {
  margin-bottom: 20px;
}

.metric-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  border: 1px solid #f0f0f0;
  transition: all 0.3s;
  height: 100px;
}

.metric-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.metric-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.metric-content {
  flex: 1;
  min-width: 0;
}

.metric-label {
  font-size: 13px;
  color: #8c8c8c;
  margin-bottom: 4px;
}

.metric-value {
  font-size: 24px;
  font-weight: 600;
  color: #1f1f1f;
  margin-bottom: 4px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.metric-trend {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.metric-trend span {
  display: flex;
  align-items: center;
  font-weight: 500;
}

.metric-trend span.up {
  color: #52c41a;
}

.metric-trend span.down {
  color: #ff4d4f;
}

.trend-text {
  color: #8c8c8c;
  margin-left: 4px;
}

/* Themes */
.blue-theme .metric-icon { background: #e6f7ff; color: #1890ff; }
.blue-theme:hover { border-color: #1890ff; }

.purple-theme .metric-icon { background: #f9f0ff; color: #722ed1; }
.purple-theme:hover { border-color: #722ed1; }

.orange-theme .metric-icon { background: #fff7e6; color: #fa8c16; }
.orange-theme:hover { border-color: #fa8c16; }

.green-theme .metric-icon { background: #f6ffed; color: #52c41a; }
.green-theme:hover { border-color: #52c41a; }

/* Charts Section */
.charts-section {
  margin-bottom: 20px;
}

.chart-card {
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  border: none;
}

.chart-card :deep(.el-card__header) {
  padding: 12px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 15px;
  font-weight: 600;
  color: #1f1f1f;
}

/* Ranking Section */
.ranking-card {
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  border: none;
}

.rank-badge {
  display: inline-block;
  width: 22px;
  height: 22px;
  line-height: 22px;
  text-align: center;
  border-radius: 50%;
  background: #f0f0f0;
  color: #8c8c8c;
  font-weight: 600;
  font-size: 12px;
}

.rank-badge.rank-1 { background: #f5222d; color: #fff; }
.rank-badge.rank-2 { background: #fa8c16; color: #fff; }
.rank-badge.rank-3 { background: #faad14; color: #fff; }

.product-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-thumb {
  width: 36px;
  height: 36px;
  border-radius: 4px;
  border: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.product-name {
  font-size: 13px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.progress-wrapper {
  width: 100%;
  padding-right: 10px;
}
</style>