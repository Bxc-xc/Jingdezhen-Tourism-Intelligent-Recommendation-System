<template>
  <div class="group-buy-manage">
    <div class="back-button-container">
      <el-button @click="goBack" :icon="ArrowLeft" circle />
    </div>

    <div class="page-header">
      <h2>团购管理</h2>
      <el-button v-if="activeTab === 'groupbuys'" type="primary" :icon="Plus" @click="openCreate">发布团购</el-button>
    </div>

    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <!-- 团购活动列表 -->
      <el-tab-pane label="团购活动" name="groupbuys">
        <el-card class="list-card">
          <el-table :data="list" v-loading="loading" border stripe>
            <el-table-column label="图片" width="80">
              <template #default="{ row }">
                <el-image v-if="row.image" :src="row.image" style="width:56px;height:40px;border-radius:4px" fit="cover" />
                <span v-else style="color:#c0c4cc;font-size:12px">无图</span>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="团购名称" min-width="140" />
            <el-table-column label="团购价" width="90">
              <template #default="{ row }">
                <span style="color:#ff2d2d;font-weight:700">¥{{ row.groupPrice }}</span>
              </template>
            </el-table-column>
            <el-table-column label="原价" width="80">
              <template #default="{ row }">
                <span style="color:#c0c4cc;text-decoration:line-through">¥{{ row.originalPrice }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="stock" label="剩余库存" width="90" />
            <el-table-column prop="soldCount" label="已售" width="70" />
            <el-table-column label="有效期" width="120">
              <template #default="{ row }">{{ row.validEnd }}</template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" fixed="right">
              <template #default="{ row }">
                <el-button size="small" link type="primary" @click="openEdit(row)">编辑</el-button>
                <el-popconfirm title="确认删除该团购？" @confirm="handleDelete(row.id)">
                  <template #reference>
                    <el-button size="small" link type="danger">删除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 团购订单列表 -->
      <el-tab-pane name="orders">
        <template #label>
          团购订单
          <el-badge v-if="newOrderCount > 0" :value="newOrderCount" class="order-badge" />
        </template>
        <el-card class="list-card">
          <el-table :data="orders" v-loading="ordersLoading" border stripe>
            <el-table-column prop="id" label="订单号" width="80" />
            <el-table-column prop="groupBuyName" label="团购名称" min-width="140" />
            <el-table-column label="金额" width="100">
              <template #default="{ row }">
                <span style="color:#e6a23c;font-weight:600">¥{{ Number(row.totalPrice).toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="70" />
            <el-table-column prop="contactName" label="联系人" width="100" />
            <el-table-column prop="contactPhone" label="联系电话" width="130" />
            <el-table-column prop="useDate" label="使用日期" width="120" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="orderStatusType(row.status)" size="small">{{ orderStatusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="下单时间" width="160">
              <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!ordersLoading && orders.length === 0" description="暂无团购订单" />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 发布/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑团购' : '发布团购'"
      width="560px"
      destroy-on-close
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="团购名称" prop="name">
          <el-input v-model="form.name" placeholder="如：陶艺手工体验套餐" />
        </el-form-item>
        <el-form-item label="团购图片">
          <el-upload
            :http-request="handleImageUpload"
            :show-file-list="false"
            :before-upload="beforeImageUpload"
            accept="image/*"
          >
            <div class="upload-area">
              <img v-if="form.image" :src="normalizeImageUrl(form.image)" class="preview-img" />
              <div v-else class="upload-placeholder">
                <el-icon :size="24"><Plus /></el-icon>
                <span>上传图片</span>
              </div>
            </div>
          </el-upload>
          <div v-if="form.image" style="margin-top:4px">
            <el-button link type="danger" size="small" @click="form.image = ''">移除图片</el-button>
          </div>
        </el-form-item>
        <el-form-item label="团购价" prop="groupPrice">
          <el-input-number v-model="form.groupPrice" :min="0.01" :precision="2" :step="1" style="width:160px" />
          <span style="margin-left:8px;color:#909399">元</span>
        </el-form-item>
        <el-form-item label="原价" prop="originalPrice">
          <el-input-number v-model="form.originalPrice" :min="0.01" :precision="2" :step="1" style="width:160px" />
          <span style="margin-left:8px;color:#909399">元</span>
        </el-form-item>
        <el-form-item label="库存数量" prop="stock">
          <el-input-number v-model="form.stock" :min="1" :step="10" style="width:160px" />
        </el-form-item>
        <el-form-item label="有效期" prop="validEnd">
          <el-date-picker
            v-model="validRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="截止日期"
            value-format="YYYY-MM-DD"
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="使用说明">
          <el-input v-model="form.usageDesc" type="textarea" :rows="2" placeholder="简短说明，如：需提前1天预约" />
        </el-form-item>
        <el-form-item label="详细介绍">
          <el-input v-model="form.detail" type="textarea" :rows="4" placeholder="套餐详细内容介绍" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ editingId ? '保存修改' : '发布' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElNotification } from 'element-plus'
import { ArrowLeft, Plus } from '@element-plus/icons-vue'
import {
  getMerchantGroupBuys,
  createGroupBuy,
  updateGroupBuy,
  deleteGroupBuy,
  getMerchantGroupBuyOrders
} from '../../api/groupBuy'
import { getMerchantByUserId } from '../../api/merchant'
import { useUserStore } from '../../stores/user'
import request from '../../utils/request'
import { normalizeImageUrl } from '../../utils/image'
import realtimeSync from '../../utils/websocket'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const list = ref([])
const dialogVisible = ref(false)
const submitting = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const merchantInfo = ref(null)

const validRange = ref([])

const form = ref({
  name: '',
  image: '',
  groupPrice: 99,
  originalPrice: 199,
  stock: 100,
  validStart: '',
  validEnd: '',
  usageDesc: '',
  detail: ''
})

const rules = {
  name: [{ required: true, message: '请输入团购名称', trigger: 'blur' }],
  groupPrice: [{ required: true, message: '请输入团购价', trigger: 'blur' }],
  originalPrice: [{ required: true, message: '请输入原价', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
  validEnd: [{ required: true, message: '请选择有效期', trigger: 'change' }]
}

const merchantId = computed(() => merchantInfo.value?.id)

const statusLabel = (s) => ({ PENDING: '待审核', APPROVED: '已上架', REJECTED: '已拒绝', OFFLINE: '已下架' }[s] || s)
const statusType = (s) => ({ PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger', OFFLINE: 'info' }[s] || '')

const orderStatusLabel = (s) => ({ PAID: '已支付', USED: '已使用', REFUNDED: '已退款', CANCELLED: '已取消' }[s] || s)
const orderStatusType = (s) => ({ PAID: 'success', USED: 'info', REFUNDED: 'warning', CANCELLED: 'danger' }[s] || '')

const activeTab = ref('groupbuys')
const orders = ref([])
const ordersLoading = ref(false)
const newOrderCount = ref(0)

const formatDate = (d) => d ? new Date(d).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }) : ''

const loadOrders = async () => {
  if (!merchantId.value) return
  ordersLoading.value = true
  try {
    const res = await getMerchantGroupBuyOrders(merchantId.value)
    orders.value = res?.data?.data || res?.data || []
  } catch (e) {
    ElMessage.error('加载订单失败')
  } finally {
    ordersLoading.value = false
  }
}

const onTabChange = (tab) => {
  if (tab === 'orders') {
    newOrderCount.value = 0
    loadOrders()
  }
}

const handleOrderRealtime = async ({ operation, data: d }) => {
  if (!merchantInfo.value?.id) return
  if (d?.type !== 'group_buy_order') return
  if (d?.merchantId && String(d.merchantId) !== String(merchantInfo.value.id)) return
  if (activeTab.value === 'orders') {
    await loadOrders()
  } else {
    newOrderCount.value++
    ElNotification({ title: '新团购订单', message: `收到团购订单：${d.groupBuyName || ''}`, type: 'success', duration: 5000 })
  }
}

const goBack = () => router.go(-1)
const loadList = async () => {
  if (!merchantId.value) return
  loading.value = true
  try {
    const res = await getMerchantGroupBuys(merchantId.value)
    list.value = res?.data?.data || res?.data || []
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  editingId.value = null
  validRange.value = []
  form.value = { name: '', image: '', groupPrice: 99, originalPrice: 199, stock: 100, validStart: '', validEnd: '', usageDesc: '', detail: '' }
  dialogVisible.value = true
}

const openEdit = (row) => {
  editingId.value = row.id
  form.value = {
    name: row.name,
    image: row.image || '',
    groupPrice: parseFloat(row.groupPrice),
    originalPrice: parseFloat(row.originalPrice),
    stock: row.stock,
    validStart: row.validStart || '',
    validEnd: row.validEnd || '',
    usageDesc: row.usageDesc || '',
    detail: row.detail || ''
  }
  validRange.value = [row.validStart || '', row.validEnd || '']
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    // 同步日期范围
    if (validRange.value && validRange.value.length === 2) {
      form.value.validStart = validRange.value[0]
      form.value.validEnd = validRange.value[1]
    }
    if (!form.value.validEnd) {
      ElMessage.warning('请选择有效期')
      return
    }
    submitting.value = true
    try {
      if (editingId.value) {
        await updateGroupBuy(merchantId.value, editingId.value, form.value)
        ElMessage.success('修改成功，等待重新审核')
      } else {
        await createGroupBuy(merchantId.value, form.value)
        ElMessage.success('发布成功，等待审核')
      }
      dialogVisible.value = false
      await loadList()
    } catch (e) {
      ElMessage.error(e?.response?.data?.message || '操作失败')
    } finally {
      submitting.value = false
    }
  })
}

const handleDelete = async (id) => {
  try {
    await deleteGroupBuy(merchantId.value, id)
    ElMessage.success('删除成功')
    await loadList()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) { ElMessage.error('只能上传图片文件'); return false }
  if (!isLt5M) { ElMessage.error('图片大小不能超过 5MB'); return false }
  return true
}

const handleImageUpload = async (options) => {
  const formData = new FormData()
  formData.append('file', options.file)
  try {
    const res = await request.post('/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    const url = res?.url || res?.data?.url
    if (url) {
      form.value.image = url
      ElMessage.success('上传成功')
    } else {
      ElMessage.error('上传失败，未获取到图片地址')
    }
  } catch (e) {
    ElMessage.error('上传失败')
  }
}

onMounted(async () => {
  try {
    const res = await getMerchantByUserId(userStore.user?.id)
    if (res?.success && res?.data) merchantInfo.value = res.data
    else if (res?.data) merchantInfo.value = res.data
  } catch (e) { /* ignore */ }
  await loadList()
  if (!realtimeSync.isConnected) realtimeSync.connect()
  realtimeSync.subscribe('order', handleOrderRealtime)
})

onUnmounted(() => {
  realtimeSync.unsubscribe('order', handleOrderRealtime)
})
</script>

<style scoped>
.group-buy-manage { padding: 20px; }
.back-button-container { margin-bottom: 16px; }
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.page-header h2 { margin: 0; font-size: 22px; color: #303133; }
.list-card { border-radius: 8px; }
.order-badge { margin-left: 6px; vertical-align: middle; }

.upload-area {
  width: 120px;
  height: 90px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
  transition: border-color 0.2s;
}
.upload-area:hover { border-color: #409eff; }
.preview-img { width: 100%; height: 100%; object-fit: cover; }
.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  color: #909399;
  font-size: 12px;
}
</style>
