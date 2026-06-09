<template>
  <div class="order-manage">
    <!-- 返回按钮 -->
    <div class="back-button-container">
      <el-button @click="goBack" :icon="ArrowLeft" circle />
    </div>
    
    <div class="page-header">
      <h2>订单管理</h2>
      <div class="header-actions">
        <el-button @click="refreshOrders">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button type="primary" @click="exportOrders">
          <el-icon><Download /></el-icon>
          导出订单
        </el-button>
      </div>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :model="filterForm" inline>
        <el-form-item label="订单状态">
          <el-select 
            v-model="filterForm.status" 
            placeholder="全部状态" 
            clearable
            :popper-options="{
              strategy: 'fixed',
              modifiers: [
                { name: 'offset', options: { offset: [0, 4] } },
                { name: 'computeStyles', options: { adaptive: true, gpuAcceleration: true } }
              ]
            }"
          >
            <el-option label="全部" value="" />
            <el-option label="待确认" value="pending" />
            <el-option label="已确认" value="confirmed" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-form-item>
        <el-form-item label="下单时间">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            :popper-options="{
              strategy: 'fixed',
              modifiers: [
                { name: 'offset', options: { offset: [0, 4] } },
                { name: 'computeStyles', options: { adaptive: true, gpuAcceleration: true } }
              ]
            }"
          />
        </el-form-item>
        <el-form-item label="客户姓名">
          <el-input v-model="filterForm.customerName" placeholder="请输入客户姓名" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">筛选</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 订单统计 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon pending">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ orderStats.pending }}</div>
              <div class="stat-label">待确认</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon confirmed">
              <el-icon><Check /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ orderStats.confirmed }}</div>
              <div class="stat-label">已确认</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon cancelled">
              <el-icon><Close /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ orderStats.cancelled }}</div>
              <div class="stat-label">已取消</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon revenue">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ Number(orderStats.revenue).toFixed(2) }}</div>
              <div class="stat-label">总收入</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 订单列表 -->
    <el-card class="orders-card">
      <template #header>
        <span>订单列表</span>
        <span class="order-count">共 {{ filteredOrders.length }} 条订单</span>
      </template>
      
      <el-table
        :data="paginatedOrders"
        v-loading="loading"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="订单号" width="120" />
        <el-table-column prop="customerName" label="客户姓名" width="120" />
        <el-table-column prop="customerPhone" label="联系电话" width="130" />
        <el-table-column prop="scenicName" :label="projectLabel" />
        <el-table-column prop="price" label="订单金额" width="100">
          <template #default="scope">
            <span class="price-text">¥{{ Number(scope.row.price).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="viewOrder(scope.row)">查看</el-button>
            <el-button
              v-if="scope.row.status === 'pending'"
              size="small"
              type="success"
              @click="confirmOrder(scope.row.id)"
            >
              确认
            </el-button>
            <el-button
              v-if="scope.row.status === 'pending'"
              size="small"
              type="danger"
              @click="rejectOrder(scope.row.id)"
            >
              拒绝
            </el-button>
            <el-button
              v-if="scope.row.status === 'confirmed'"
              size="small"
              type="warning"
              @click="cancelOrder(scope.row.id)"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="filteredOrders.length"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 订单详情对话框 -->
    <el-dialog v-model="showOrderDetail" title="订单详情" width="600px">
      <div v-if="selectedOrder" class="order-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ selectedOrder.id }}</el-descriptions-item>
          <el-descriptions-item label="客户姓名">{{ selectedOrder.customerName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ selectedOrder.customerPhone }}</el-descriptions-item>
          <el-descriptions-item label="体验项目">{{ selectedOrder.scenicName }}</el-descriptions-item>
          <el-descriptions-item label="订单金额">
            <span style="color:#e6a23c;font-weight:600">¥{{ selectedOrder.price.toFixed(2) }}</span>
            <span v-if="selectedOrder.roomTypePrice && selectedOrder.nights" style="color:#909399;font-size:12px;margin-left:8px">
              (¥{{ selectedOrder.roomTypePrice }} × {{ selectedOrder.nights }}晚 × {{ selectedOrder.quantity }}间)
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusType(selectedOrder.status)">
              {{ getStatusText(selectedOrder.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ formatDate(selectedOrder.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="入住日期" v-if="selectedOrder.checkInDate">{{ selectedOrder.checkInDate }}</el-descriptions-item>
          <el-descriptions-item label="离店日期" v-if="selectedOrder.checkOutDate">{{ selectedOrder.checkOutDate }}</el-descriptions-item>
          <el-descriptions-item label="房间数量" v-if="selectedOrder.quantity">{{ selectedOrder.quantity }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ selectedOrder.remark || '无' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="showOrderDetail = false">关闭</el-button>
        <el-button
          v-if="selectedOrder?.status === 'pending'"
          type="success"
          @click="confirmOrder(selectedOrder.id)"
        >
          确认订单
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getMerchantByUserId } from '../../api/merchant'
import { getMerchantOrders, confirmOrder as confirmOrderApi, cancelOrder as cancelOrderApi } from '../../api/order'
import { getMerchantGroupBuyOrders } from '../../api/groupBuy'
import { useUserStore } from '../../stores/user'
import realtimeSync from '../../utils/websocket'

const router = useRouter()
const userStore = useUserStore()
const merchantInfo = ref(null)

// 根据商家分类动态显示项目名称标签
const projectLabel = computed(() => {
  const category = merchantInfo.value?.category || 'OTHER'
  const labelMap = {
    CERAMIC: '体验项目',
    FOOD: '菜品/服务',
    HOTEL: '房型',
    OTHER: '服务项目'
  }
  return labelMap[category] || '服务项目'
})

const getDefaultNameByCategory = (category) => {
  const map = {
    CERAMIC: '陶瓷体验预约',
    FOOD: '餐饮服务',
    HOTEL: '客房预订',
    OTHER: '服务预约'
  }
  return map[category] || '服务预约'
}

const goBack = () => {
  router.go(-1)
}

const loading = ref(false)
const showOrderDetail = ref(false)
const selectedOrder = ref(null)
const selectedOrders = ref([])

const currentPage = ref(1)
const pageSize = ref(10)

const filterForm = reactive({
  status: '',
  dateRange: [],
  customerName: ''
})

const orderStats = ref({
  pending: 0,
  confirmed: 0,
  cancelled: 0,
  revenue: 0
})

// 根据商家分类生成不同的订单示例数据
const getOrdersByCategory = (category) => {
  const ordersMap = {
    CERAMIC: [
      { id: 'ORD001', customerName: '张三', customerPhone: '13800138001', scenicName: '陶瓷制作体验', price: 128, status: 'pending', createTime: new Date(), remark: '希望安排在上午，需要专业老师指导' },
      { id: 'ORD002', customerName: '李四', customerPhone: '13800138002', scenicName: '陶艺拉坯体验', price: 98, status: 'confirmed', createTime: new Date(Date.now() - 86400000), remark: '两人同行，希望体验传统拉坯工艺' },
      { id: 'ORD003', customerName: '王五', customerPhone: '13800138003', scenicName: '陶瓷绘画体验', price: 88, status: 'confirmed', createTime: new Date(Date.now() - 172800000), remark: '带小朋友一起体验，需要儿童专用工具' },
      { id: 'ORD004', customerName: '赵六', customerPhone: '13800138004', scenicName: '陶瓷烧制体验', price: 168, status: 'cancelled', createTime: new Date(Date.now() - 259200000), remark: '客户取消' },
      { id: 'ORD005', customerName: '钱七', customerPhone: '13800138005', scenicName: '陶瓷文化讲解+制作体验', price: 158, status: 'confirmed', createTime: new Date(Date.now() - 345600000), remark: '团队预约，共5人，需要讲解景德镇陶瓷历史' },
      { id: 'ORD006', customerName: '孙八', customerPhone: '13800138006', scenicName: '陶艺拉坯体验', price: 98, status: 'pending', createTime: new Date(Date.now() - 432000000), remark: '希望周末体验' },
      { id: 'ORD007', customerName: '周九', customerPhone: '13800138007', scenicName: '陶瓷制作体验', price: 128, status: 'confirmed', createTime: new Date(Date.now() - 518400000), remark: '想要制作一个纪念品' }
    ],
    FOOD: [
      { id: 'ORD001', customerName: '张三', customerPhone: '13800138001', scenicName: '特色菜品套餐', price: 128, status: 'pending', createTime: new Date(), remark: '希望安排在中午，需要包间' },
      { id: 'ORD002', customerName: '李四', customerPhone: '13800138002', scenicName: '定制菜单服务', price: 298, status: 'confirmed', createTime: new Date(Date.now() - 86400000), remark: '生日宴，需要定制蛋糕' },
      { id: 'ORD003', customerName: '王五', customerPhone: '13800138003', scenicName: '外卖服务', price: 68, status: 'confirmed', createTime: new Date(Date.now() - 172800000), remark: '送到指定地址' },
      { id: 'ORD004', customerName: '赵六', customerPhone: '13800138004', scenicName: '特色菜品套餐', price: 168, status: 'cancelled', createTime: new Date(Date.now() - 259200000), remark: '客户取消' },
      { id: 'ORD005', customerName: '钱七', customerPhone: '13800138005', scenicName: '团队套餐', price: 588, status: 'confirmed', createTime: new Date(Date.now() - 345600000), remark: '公司聚餐，共10人' },
      { id: 'ORD006', customerName: '孙八', customerPhone: '13800138006', scenicName: '外卖服务', price: 88, status: 'pending', createTime: new Date(Date.now() - 432000000), remark: '希望尽快送达' },
      { id: 'ORD007', customerName: '周九', customerPhone: '13800138007', scenicName: '特色菜品套餐', price: 158, status: 'confirmed', createTime: new Date(Date.now() - 518400000), remark: '庆祝纪念日' }
    ],
    HOTEL: [
      { id: 'ORD001', customerName: '张三', customerPhone: '13800138001', scenicName: '标准间预订', price: 298, status: 'pending', createTime: new Date(), remark: '希望安排在高层，需要安静' },
      { id: 'ORD002', customerName: '李四', customerPhone: '13800138002', scenicName: '豪华间预订', price: 588, status: 'confirmed', createTime: new Date(Date.now() - 86400000), remark: '蜜月旅行，需要浪漫布置' },
      { id: 'ORD003', customerName: '王五', customerPhone: '13800138003', scenicName: '套房预订', price: 888, status: 'confirmed', createTime: new Date(Date.now() - 172800000), remark: '家庭出游，需要加床' },
      { id: 'ORD004', customerName: '赵六', customerPhone: '13800138004', scenicName: '标准间预订', price: 298, status: 'cancelled', createTime: new Date(Date.now() - 259200000), remark: '客户取消' },
      { id: 'ORD005', customerName: '钱七', customerPhone: '13800138005', scenicName: '家庭房预订', price: 688, status: 'confirmed', createTime: new Date(Date.now() - 345600000), remark: '带老人小孩，需要无障碍设施' },
      { id: 'ORD006', customerName: '孙八', customerPhone: '13800138006', scenicName: '标准间预订', price: 298, status: 'pending', createTime: new Date(Date.now() - 432000000), remark: '商务出差' },
      { id: 'ORD007', customerName: '周九', customerPhone: '13800138007', scenicName: '豪华间预订', price: 588, status: 'confirmed', createTime: new Date(Date.now() - 518400000), remark: '需要延迟退房' }
    ],
    OTHER: [
      { id: 'ORD001', customerName: '张三', customerPhone: '13800138001', scenicName: '通用服务', price: 100, status: 'pending', createTime: new Date(), remark: '需要详细咨询' },
      { id: 'ORD002', customerName: '李四', customerPhone: '13800138002', scenicName: '定制服务', price: 200, status: 'confirmed', createTime: new Date(Date.now() - 86400000), remark: '需要个性化定制' },
      { id: 'ORD003', customerName: '王五', customerPhone: '13800138003', scenicName: '咨询服务', price: 50, status: 'confirmed', createTime: new Date(Date.now() - 172800000), remark: '需要专业建议' },
      { id: 'ORD004', customerName: '赵六', customerPhone: '13800138004', scenicName: '通用服务', price: 100, status: 'cancelled', createTime: new Date(Date.now() - 259200000), remark: '客户取消' },
      { id: 'ORD005', customerName: '钱七', customerPhone: '13800138005', scenicName: '定制服务', price: 300, status: 'confirmed', createTime: new Date(Date.now() - 345600000), remark: '团队服务需求' },
      { id: 'ORD006', customerName: '孙八', customerPhone: '13800138006', scenicName: '咨询服务', price: 50, status: 'pending', createTime: new Date(Date.now() - 432000000), remark: '需要电话咨询' },
      { id: 'ORD007', customerName: '周九', customerPhone: '13800138007', scenicName: '通用服务', price: 150, status: 'confirmed', createTime: new Date(Date.now() - 518400000), remark: '需要上门服务' }
    ]
  }
  return ordersMap[category] || ordersMap.OTHER
}

const orders = ref([])

const computeOrderStats = () => {
  const stats = {
    pending: 0,
    confirmed: 0,
    cancelled: 0,
    revenue: 0
  }

  orders.value.forEach(order => {
    const status = order.status
    if (status === 'pending') {
      stats.pending++
    } else if (status === 'confirmed') {
      stats.confirmed++
    } else if (status === 'cancelled') {
      stats.cancelled++
    }
    stats.revenue += Number(order.price || 0)
  })

  orderStats.value = stats
}

const filteredOrders = computed(() => {
  let filtered = orders.value

  if (filterForm.status) {
    filtered = filtered.filter(order => order.status === filterForm.status)
  }

  if (filterForm.customerName) {
    filtered = filtered.filter(order =>
      order.customerName.includes(filterForm.customerName)
    )
  }

  if (filterForm.dateRange && filterForm.dateRange.length === 2) {
    const [startDate, endDate] = filterForm.dateRange
    filtered = filtered.filter(order => {
      const orderDate = new Date(order.createTime).toISOString().split('T')[0]
      return orderDate >= startDate && orderDate <= endDate
    })
  }

  return filtered
})

const paginatedOrders = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredOrders.value.slice(start, end)
})

const handleFilter = () => {
  currentPage.value = 1
}

const resetFilter = () => {
  Object.assign(filterForm, {
    status: '',
    dateRange: [],
    customerName: ''
  })
  currentPage.value = 1
}

const refreshOrders = async () => {
  await loadOrders()
  ElMessage.success('刷新成功')
}

const exportOrders = () => {
  ElMessage.success('导出功能开发中...')
}

const handleSelectionChange = (selection) => {
  selectedOrders.value = selection
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val) => {
  currentPage.value = val
}

const getStatusType = (status) => {
  const types = {
    pending: 'warning',
    confirmed: 'success',
    cancelled: 'danger',
    paid: 'success',
    used: 'info',
    refunded: 'warning'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    pending: '待确认',
    confirmed: '已确认',
    cancelled: '已取消',
    paid: '已支付',
    used: '已使用',
    refunded: '已退款'
  }
  return texts[status] || status
}

const viewOrder = (order) => {
  selectedOrder.value = order
  showOrderDetail.value = true
}

const confirmOrder = async (orderId) => {
  try {
    await ElMessageBox.confirm('确认要确认这个订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    loading.value = true
    try {
      await confirmOrderApi(orderId)
      
      const order = orders.value.find(o => o.id === orderId)
      if (order) {
        order.status = 'confirmed'
        orderStats.value.pending--
        orderStats.value.confirmed++
        orderStats.value.revenue += order.price
      }

      ElMessage.success('订单确认成功')
      // 重新加载列表以确保数据一致
      await loadOrders()
    } catch (error) {
      console.error('确认订单失败:', error)
      ElMessage.error(error?.response?.data?.message || '确认订单失败')
    } finally {
      loading.value = false
    }
  } catch {
    // 用户取消
  }
}

const rejectOrder = async (orderId) => {
  try {
    await ElMessageBox.confirm('确认要拒绝这个订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    loading.value = true
    try {
      // 拒绝本质上也是取消订单
      await cancelOrderApi(orderId)

      const order = orders.value.find(o => o.id === orderId)
      if (order) {
        order.status = 'cancelled'
        orderStats.value.pending--
        orderStats.value.cancelled++
      }

      ElMessage.success('订单已拒绝')
      await loadOrders()
    } catch (error) {
      console.error('拒绝订单失败:', error)
      ElMessage.error(error?.response?.data?.message || '拒绝订单失败')
    } finally {
      loading.value = false
    }
  } catch {
    // 用户取消
  }
}

const cancelOrder = async (orderId) => {
  try {
    await ElMessageBox.confirm('确认要取消这个订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    loading.value = true
    try {
      await cancelOrderApi(orderId)

      const order = orders.value.find(o => o.id === orderId)
      if (order) {
        order.status = 'cancelled'
        orderStats.value.confirmed--
        orderStats.value.cancelled++
        orderStats.value.revenue -= order.price
      }

      ElMessage.success('订单已取消')
      await loadOrders()
    } catch (error) {
      console.error('取消订单失败:', error)
      ElMessage.error(error?.response?.data?.message || '取消订单失败')
    } finally {
      loading.value = false
    }
  } catch {
    // 用户取消
  }
}

const formatDate = (date) => {
  return new Date(date).toLocaleString()
}

const loadOrders = async () => {
  if (!merchantInfo.value?.id) {
    ElMessage.warning('无法获取商家信息')
    return
  }

  loading.value = true
  try {
    // 并行加载普通订单和团购订单
    const [normalRes, groupBuyRes] = await Promise.allSettled([
      getMerchantOrders(merchantInfo.value.id),
      getMerchantGroupBuyOrders(merchantInfo.value.id)
    ])

    // 处理普通订单
    let normalList = []
    if (normalRes.status === 'fulfilled') {
      const response = normalRes.value
      if (response && response.success && response.data) {
        normalList = Array.isArray(response.data) ? response.data : []
      } else if (Array.isArray(response)) {
        normalList = response
      }
    }

    const mappedNormal = normalList.map(order => {
      const user = order.user || {}
      const scenic = order.scenicSpot || order.scenicspot || null
      const roomType = order.roomType || order.room_type || null
      const statusText = (order.status || '').toString().toLowerCase()
      const displayName = roomType?.name || scenic?.name || getDefaultNameByCategory(merchantInfo.value?.category)

      let computedPrice = Number(order.totalPrice ?? order.price ?? 0)
      let nights = 0
      if (roomType?.price && order.checkInDate && order.checkOutDate) {
        const inDate = new Date(order.checkInDate)
        const outDate = new Date(order.checkOutDate)
        nights = Math.max(1, Math.round((outDate - inDate) / (1000 * 60 * 60 * 24)))
        const qty = Number(order.quantity ?? 1)
        computedPrice = Number(roomType.price) * nights * qty
      }

      return {
        id: order.id,
        orderType: 'normal',
        customerName: user.username || user.name || '游客',
        customerPhone: user.phone || user.mobile || '',
        scenicName: displayName,
        price: computedPrice,
        roomTypePrice: roomType?.price ?? null,
        nights,
        quantity: order.quantity ?? 1,
        status: statusText,
        createTime: order.orderTime || order.createTime || order.create_time,
        remark: order.reservationNote || order.remark || '',
        checkInDate: order.checkInDate,
        checkOutDate: order.checkOutDate,
      }
    })

    // 处理团购订单，统一格式
    let groupBuyList = []
    if (groupBuyRes.status === 'fulfilled') {
      const res = groupBuyRes.value
      groupBuyList = res?.data || res || []
      if (!Array.isArray(groupBuyList)) groupBuyList = []
    }

    const mappedGroupBuy = groupBuyList.map(order => ({
      id: `GB-${order.id}`,
      orderType: 'group_buy',
      customerName: order.contactName || '游客',
      customerPhone: order.contactPhone || '',
      scenicName: `【团购】${order.groupBuyName || '团购套餐'}`,
      price: Number(order.totalPrice ?? 0),
      quantity: order.quantity ?? 1,
      status: (order.status || 'paid').toLowerCase(),
      createTime: order.createdAt,
      remark: `使用日期：${order.useDate || ''}`,
      checkInDate: order.useDate,
      checkOutDate: null,
    }))

    // 合并并按时间倒序
    orders.value = [...mappedNormal, ...mappedGroupBuy].sort((a, b) => {
      return new Date(b.createTime || 0) - new Date(a.createTime || 0)
    })

    computeOrderStats()
  } catch (error) {
    console.error('加载订单失败:', error)
    ElMessage.error(error?.response?.data?.message || '加载订单失败')
  } finally {
    loading.value = false
  }
}

const loadMerchantInfo = async () => {
  try {
    if (!userStore.user?.id) return
    const response = await getMerchantByUserId(userStore.user.id)

    if (response && response.success && response.data) {
      merchantInfo.value = response.data
    } else if (response && response.id) {
      merchantInfo.value = response
    } else {
      ElMessage.warning('当前账号还没有关联的商家信息，请先完成资质申请并通过审核')
    }
  } catch (e) {
    console.error('获取商家信息失败:', e)
    ElMessage.error(e?.response?.data?.message || '获取商家信息失败，请稍后重试')
  }
}

const handleOrderRealtime = async ({ operation, data: d }) => {
  if (!merchantInfo.value?.id) return
  if (d?.merchantId && String(d.merchantId) !== String(merchantInfo.value.id)) return
  await loadOrders()
  if (operation === 'create') {
    const isGroupBuy = d?.type === 'group_buy_order'
    ElNotification({
      title: isGroupBuy ? '新团购订单' : '新订单',
      message: isGroupBuy ? `收到团购订单：${d.groupBuyName || ''}` : '收到一笔新订单，请及时处理',
      type: 'success',
      duration: 5000
    })
  }
}

onMounted(async () => {
  await loadMerchantInfo()
  await loadOrders()
  if (!realtimeSync.isConnected) realtimeSync.connect()
  realtimeSync.subscribe('order', handleOrderRealtime)
})

onUnmounted(() => {
  realtimeSync.unsubscribe('order', handleOrderRealtime)
})
</script>

<style scoped>
.order-manage {
  padding: 20px;
  background: white;
  min-height: 100%;
  max-width: 1400px;
  margin: 0 auto;
}

.back-button-container {
  margin-bottom: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.filter-card {
  margin-bottom: 20px;
}

.stats-row {
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
  font-size: 24px;
  margin-right: 16px;
  transition: all 0.3s;
}

.stat-card:hover .stat-icon {
  transform: scale(1.05);
}

.stat-icon.pending {
  background: #fdf6ec;
  color: #e6a23c;
}

.stat-icon.confirmed {
  background: #f0f9eb;
  color: #67c23a;
}

.stat-icon.cancelled {
  background: #fef0f0;
  color: #f56c6c;
}

.stat-icon.revenue {
  background: #ecf5ff;
  color: #409eff;
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

.orders-card {
  margin-bottom: 20px;
}

.order-count {
  color: #909399;
  font-size: 14px;
}

.price-text {
  color: #e6a23c;
  font-weight: 600;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.order-detail {
  padding: 20px 0;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .stats-row .el-col {
    margin-bottom: 16px;
  }
  
  .filter-card .el-form {
    flex-direction: column;
  }
  
  .filter-card .el-form-item {
    margin-right: 0;
    margin-bottom: 16px;
  }
}
</style>
