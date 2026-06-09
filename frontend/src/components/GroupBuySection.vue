<template>
  <div class="group-buy-section">
    <div class="section-title">
      <span class="title-icon">🎫</span>
      <span>团购套餐</span>
      <span class="title-sub">限时特惠，立省更多</span>
    </div>

    <div v-if="loading" class="loading-wrap">
      <el-skeleton :rows="3" animated />
    </div>

    <el-empty v-else-if="!list.length" description="暂无团购套餐" :image-size="80" />

    <div v-else class="group-buy-list">
      <div
        v-for="item in list"
        :key="item.id"
        class="group-buy-card"
        @click="openDetail(item)"
      >
        <div class="card-image">
          <img v-if="item.image" :src="normalizeImageUrl(item.image)" :alt="item.name" />
          <div v-else class="img-placeholder">
            <el-icon :size="32"><Picture /></el-icon>
          </div>
          <div class="sold-badge">已售 {{ item.soldCount }}</div>
        </div>
        <div class="card-body">
          <div class="card-name">{{ item.name }}</div>
          <div class="card-usage">{{ item.usageDesc }}</div>
          <div class="card-valid" v-if="item.validEnd">
            <el-icon><Calendar /></el-icon>
            {{ item.validEnd }} 前有效
          </div>
          <div class="card-price-row">
            <div class="price-group">
              <span class="group-price">¥{{ item.groupPrice }}</span>
              <span class="original-price">¥{{ item.originalPrice }}</span>
              <span class="discount-tag">{{ calcDiscount(item) }}折</span>
            </div>
            <el-button type="danger" size="small" round :disabled="item.stock <= 0" @click.stop="openBuy(item)">
              {{ item.stock <= 0 ? '已售罄' : '立即购买' }}
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="currentItem?.name" width="520px" destroy-on-close>
      <div v-if="currentItem" class="detail-dialog">
        <img v-if="currentItem.image" :src="normalizeImageUrl(currentItem.image)" class="detail-img" />
        <div class="detail-price-row">
          <span class="group-price large">¥{{ currentItem.groupPrice }}</span>
          <span class="original-price">¥{{ currentItem.originalPrice }}</span>
        </div>
        <div class="detail-meta">
          <span>已售 {{ currentItem.soldCount }}</span>
          <span v-if="currentItem.validEnd">· 有效期至 {{ currentItem.validEnd }}</span>
          <span>· 剩余库存 {{ currentItem.stock }}</span>
        </div>
        <el-divider />
        <div class="detail-block" v-if="currentItem.usageDesc">
          <div class="block-label">使用说明</div>
          <div class="block-content">{{ currentItem.usageDesc }}</div>
        </div>
        <div class="detail-block" v-if="currentItem.detail">
          <div class="block-label">详细介绍</div>
          <div class="block-content">{{ currentItem.detail }}</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="danger" :disabled="currentItem?.stock <= 0" @click="openBuyFromDetail">
          {{ currentItem?.stock <= 0 ? '已售罄' : '立即购买' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 购买弹窗 -->
    <el-dialog v-model="buyVisible" title="填写购买信息" width="480px" destroy-on-close>
      <el-form :model="buyForm" :rules="buyRules" ref="buyFormRef" label-width="90px">
        <el-form-item label="套餐">
          <span class="buy-item-name">{{ currentItem?.name }}</span>
        </el-form-item>
        <el-form-item label="使用日期" prop="useDate">
          <el-date-picker
            v-model="buyForm.useDate"
            type="date"
            placeholder="选择使用日期"
            :disabled-date="disabledDate"
            value-format="YYYY-MM-DD"
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="使用人姓名" prop="contactName">
          <el-input v-model="buyForm.contactName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="contactPhone">
          <el-input v-model="buyForm.contactPhone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="购买数量" prop="quantity">
          <el-input-number
            v-model="buyForm.quantity"
            :min="1"
            :max="currentItem?.stock || 99"
            @change="calcTotal"
          />
        </el-form-item>
        <el-form-item label="合计">
          <span class="total-price">¥{{ totalPrice }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="buyVisible = false">取消</el-button>
        <el-button type="danger" :loading="submitting" @click="submitBuy">确认购买</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Picture, Calendar } from '@element-plus/icons-vue'
import { getPublicGroupBuys, placeGroupBuyOrder } from '../api/groupBuy'
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'
import { normalizeImageUrl } from '../utils/image'

const props = defineProps({
  merchantId: { type: [Number, String], required: true }
})

const userStore = useUserStore()
const router = useRouter()

const loading = ref(false)
const list = ref([])
const detailVisible = ref(false)
const buyVisible = ref(false)
const currentItem = ref(null)
const submitting = ref(false)
const buyFormRef = ref(null)

const buyForm = ref({
  useDate: '',
  contactName: '',
  contactPhone: '',
  quantity: 1
})

const buyRules = {
  useDate: [{ required: true, message: '请选择使用日期', trigger: 'change' }],
  contactName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  contactPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  quantity: [{ required: true, message: '请选择数量', trigger: 'change' }]
}

const totalPrice = computed(() => {
  if (!currentItem.value) return '0.00'
  return (parseFloat(currentItem.value.groupPrice) * buyForm.value.quantity).toFixed(2)
})

const calcDiscount = (item) => {
  if (!item.originalPrice || item.originalPrice <= 0) return '-'
  const d = (item.groupPrice / item.originalPrice * 10).toFixed(1)
  return d
}

const disabledDate = (date) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  if (date < today) return true
  if (currentItem.value?.validEnd) {
    return date > new Date(currentItem.value.validEnd)
  }
  return false
}

const calcTotal = () => {}

const loadList = async () => {
  loading.value = true
  try {
    const res = await getPublicGroupBuys(props.merchantId)
    list.value = res?.data?.data || res?.data || []
  } catch (e) {
    console.error('加载团购失败', e)
  } finally {
    loading.value = false
  }
}

const openDetail = (item) => {
  currentItem.value = item
  detailVisible.value = true
}

const openBuy = (item) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push(`/login?redirect=${encodeURIComponent(window.location.pathname)}`)
    return
  }
  currentItem.value = item
  buyForm.value = {
    useDate: '',
    contactName: userStore.user?.realName || userStore.user?.username || '',
    contactPhone: userStore.user?.phone || '',
    quantity: 1
  }
  buyVisible.value = true
}

const openBuyFromDetail = () => {
  detailVisible.value = false
  openBuy(currentItem.value)
}

const submitBuy = async () => {
  if (!buyFormRef.value) return
  await buyFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      await placeGroupBuyOrder({
        userId: userStore.user?.id,
        groupBuyId: currentItem.value.id,
        quantity: buyForm.value.quantity,
        useDate: buyForm.value.useDate,
        contactName: buyForm.value.contactName,
        contactPhone: buyForm.value.contactPhone
      })
      ElMessage.success('购买成功！')
      buyVisible.value = false
      await loadList()
    } catch (e) {
      ElMessage.error(e?.response?.data?.message || e?.message || '购买失败')
    } finally {
      submitting.value = false
    }
  })
}

onMounted(loadList)
</script>

<style scoped>
.group-buy-section {
  margin-top: 32px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 16px;
}

.title-icon { font-size: 22px; }

.title-sub {
  font-size: 13px;
  font-weight: 400;
  color: #909399;
}

.loading-wrap { padding: 20px 0; }

.group-buy-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.group-buy-card {
  display: flex;
  gap: 0;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.2s;
  border: 1px solid #f0f0f0;
}

.group-buy-card:hover {
  box-shadow: 0 4px 16px rgba(0,0,0,0.14);
  transform: translateY(-2px);
}

.card-image {
  position: relative;
  width: 120px;
  min-height: 100px;
  flex-shrink: 0;
  background: #f5f7fa;
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.img-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  min-height: 100px;
}

.sold-badge {
  position: absolute;
  bottom: 6px;
  left: 6px;
  background: rgba(0,0,0,0.55);
  color: #fff;
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 10px;
}

.card-body {
  flex: 1;
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.card-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
}

.card-usage {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

.card-valid {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-price-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
  padding-top: 8px;
}

.price-group {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.group-price {
  font-size: 22px;
  font-weight: 700;
  color: #ff2d2d;
}

.group-price.large {
  font-size: 28px;
}

.original-price {
  font-size: 13px;
  color: #c0c4cc;
  text-decoration: line-through;
}

.discount-tag {
  font-size: 11px;
  background: #fff0f0;
  color: #ff2d2d;
  border: 1px solid #ffcdd2;
  border-radius: 4px;
  padding: 1px 5px;
}

/* 详情弹窗 */
.detail-dialog { padding: 0 4px; }

.detail-img {
  width: 100%;
  max-height: 220px;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 16px;
}

.detail-price-row {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 8px;
}

.detail-meta {
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
}

.detail-block { margin-bottom: 12px; }

.block-label {
  font-size: 13px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 4px;
}

.block-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.7;
  white-space: pre-wrap;
}

/* 购买弹窗 */
.buy-item-name {
  font-weight: 600;
  color: #303133;
}

.total-price {
  font-size: 22px;
  font-weight: 700;
  color: #ff2d2d;
}
</style>
