<template>
  <div class="workshop-detail">
    <Header />

    <div class="container" v-loading="loading">
      <div class="back-button-container">
        <el-button @click="goBack" :icon="ArrowLeft" circle />
      </div>

      <el-empty
        v-if="!loading && !workshop"
        :description="$t('ceramic.workshop.notFound')"
      />

      <div v-else-if="workshop">
        <div class="header-section">
          <div class="cover-wrapper">
            <el-carousel
              v-if="imageList.length"
              height="240px"
              indicator-position="outside"
            >
              <el-carousel-item
                v-for="(img, index) in imageList"
                :key="index"
              >
                <img
                  :src="img"
                  :alt="`${workshop.shopName} ${$t('card.noImage')} ${index + 1}`"
                  class="cover-image"
                />
              </el-carousel-item>
            </el-carousel>
            <div v-else class="placeholder-image">
              <div class="placeholder-content">
                <el-icon :size="48"><Picture /></el-icon>
                <p>{{ $t('card.noImage') }}</p>
              </div>
            </div>
          </div>
          <div class="info-wrapper">
            <div class="title-row">
              <h1>{{ workshop.shopName }}</h1>
              <el-button
                :type="isFavorited ? 'danger' : 'default'"
                circle
                @click="toggleFavorite"
                :title="isFavorited ? '取消收藏' : '收藏'"
              >
                <el-icon><StarFilled v-if="isFavorited" /><Star v-else /></el-icon>
              </el-button>
            </div>
            <div class="rating-row">
              <el-rate
                v-model="workshop.rating"
                disabled
                show-score
                text-color="#ff9900"
                :allow-half="true"
              />
              <span class="rating-score" v-if="workshop.rating">
                {{ workshop.rating.toFixed(1) }}
              </span>
              <span class="review-count" v-if="workshop.reviewCount !== undefined">
                {{ workshop.reviewCount }} {{ $t('card.reviews') }}
              </span>
              <el-tag type="success">
                {{ $t('ceramic.workshop.title') }}
              </el-tag>
            </div>
            <p class="description">
              {{ workshop.description || $t('ceramic.workshop.description') }}
            </p>
            <el-descriptions :column="2" border class="base-info">
              <el-descriptions-item :label="$t('detail.address')">
                {{ workshop.address || $t('detail.noAddress') }}
              </el-descriptions-item>
              <el-descriptions-item :label="$t('detail.phone')">
                {{ workshop.phone || $t('detail.noPhone') }}
              </el-descriptions-item>
              <el-descriptions-item :label="$t('detail.openTime')">
                {{ workshop.openTime || $t('detail.noOpenTime') }}
              </el-descriptions-item>
              <el-descriptions-item :label="$t('detail.merchantType')">
                {{ $t('ceramic.workshop.title') }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </div>

        <div class="content-section">
          <el-card class="section-card">
            <template #header>
              <span>{{ $t('ceramic.workshop.description') }}</span>
            </template>
            <p class="detail-text">
              {{ workshop.description || $t('ceramic.workshop.noDescription') }}
            </p>
          </el-card>

          <el-card class="section-card">
            <template #header>
              <span>{{ $t('ceramic.workshop.experienceDesc') }}</span>
            </template>
            <p class="detail-text">
              {{ $t('ceramic.workshop.experienceDescText') }}
            </p>
          </el-card>

          <!-- 团购套餐 -->
          <GroupBuySection v-if="merchantId" :merchantId="merchantId" class="section-card-full" />

          <!-- 商家活动 -->
          <el-card v-if="activities.filter(a => isActivityActive(a)).length > 0" class="section-card">
            <template #header>
              <span>{{ $t('detail.merchantActivities') }}</span>
            </template>
            <div class="activities-list">
              <div
                v-for="activity in activities.filter(a => isActivityActive(a))"
                :key="activity.id"
                class="activity-item"
              >
                <div class="activity-header">
                  <h4>{{ activity.title }}</h4>
                  <el-tag v-if="isActivityActive(activity)" type="success" size="small">
                    {{ $t('detail.active') }}
                  </el-tag>
                  <el-tag v-else type="info" size="small">
                    {{ $t('detail.ended') }}
                  </el-tag>
                </div>
                <p class="activity-description">{{ activity.description }}</p>
                <div v-if="activity.startTime || activity.endTime" class="activity-time">
                  <el-icon><Clock /></el-icon>
                  <span v-if="activity.startTime && activity.endTime">
                    {{ formatDateTime(activity.startTime) }} - {{ formatDateTime(activity.endTime) }}
                  </span>
                  <span v-else-if="activity.startTime">
                    {{ $t('detail.startTime') }}{{ formatDateTime(activity.startTime) }}
                  </span>
                  <span v-else-if="activity.endTime">
                    {{ $t('detail.endTime') }}{{ formatDateTime(activity.endTime) }}
                  </span>
                </div>
                <div v-if="activity.image || activity.images" class="activity-images">
                  <el-image
                    v-for="(img, index) in getActivityImages(activity.image || activity.images)"
                    :key="index"
                    :src="img"
                    :preview-src-list="getActivityImages(activity.image || activity.images)"
                    class="activity-image"
                    fit="cover"
                  />
                </div>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 评论区 -->
        <ReviewSection
          :reviews="reviews"
          :loading="loading"
          embed-replies
          :external-replies-map="repliesByReview"
          :placeholder="$t('detail.workshopReviewPlaceholder')"
          @submit-review="handleSubmitReview"
          @replies-updated="patchReplies"
        />
      </div>
    </div>

    <Footer />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ArrowLeft, Picture, ChatDotRound, Clock, Star, StarFilled } from '@element-plus/icons-vue'
import { ElMessage, ElNotification } from 'element-plus'
import Header from '../../components/Layout/Header.vue'
import Footer from '../../components/Layout/Footer.vue'
import ReviewSection from '../../components/review/ReviewSection.vue'
import GroupBuySection from '../../components/GroupBuySection.vue'
import { getMerchantById } from '../../api/merchant'
import { createMerchantReview } from '../../api/review'
import { getCommentList } from '../../api/comment'
import { getMerchantActivities } from '../../api/merchantActivity'
import { useUserStore } from '../../stores/user'
import realtimeSync from '../../utils/websocket'
import { normalizeUrl } from '../../utils/image'
import { addFavorite, removeFavorite, checkFavorite } from '../../api/favoriteGateway'
import { FAVORITE_TYPE } from '../../utils/favoriteType'

const { t } = useI18n()

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const workshop = ref(null)
const merchantId = ref(null)
const isFavorited = ref(false)
const reviews = ref([])
const repliesByReview = ref({})
const reloadingReviews = ref(false)
const activities = ref([])

const API_BASE_URL = import.meta?.env?.VITE_API_BASE_URL || '/api'

// 兼容：后端可能返回 images 为数组 / JSON 字符串 / 逗号分隔字符串
const toImageArray = (images) => {
  if (!images) return []
  if (Array.isArray(images)) return images.filter(Boolean)
  if (typeof images === 'string') {
    try {
      const parsed = JSON.parse(images)
      if (Array.isArray(parsed)) return parsed.filter(Boolean)
    } catch (e) {}
    return images.split(',').map(s => s && s.trim()).filter(Boolean)
  }
  return []
}
const uploadHeaders = computed(() => {
  return userStore.token ? { Authorization: `Bearer ${userStore.token}` } : {}
})

// 工坊图片列表：avatar + shopImages
const imageList = computed(() => {
  const w = workshop.value
  const list = []
  if (!w) return []

  if (w.avatar) list.push(w.avatar)

  if (w.shopImages) {
    if (Array.isArray(w.shopImages)) {
      w.shopImages.forEach(url => {
        if (url && !list.includes(url)) list.push(url)
      })
    } else if (typeof w.shopImages === 'string') {
      w.shopImages
        .split(',')
        .map(s => s.trim())
        .filter(Boolean)
        .forEach(url => {
          if (!list.includes(url)) list.push(url)
        })
    }
  }

  return list
})

const goBack = () => {
  router.go(-1)
}

const normalizeWorkshop = (raw) => {
  if (!raw) return null
  const w = { ...raw }
  
  // 优先使用管理员评分
  if (w.adminRating !== undefined && w.adminRating !== null && w.adminRating > 0) {
    w.rating = parseFloat(w.adminRating)
  } else if (w.rating === undefined || w.rating === null) {
    w.rating = 4.5
  } else if (typeof w.rating === 'string') {
    w.rating = parseFloat(w.rating) || 4.5
  }
  
  if (w.reviewCount === undefined || w.reviewCount === null) {
    w.reviewCount = 0
  }
  return w
}

const loadWorkshopDetail = async () => {
  const id = route.params.id
  if (!id) {
    ElMessage.error('缺少工坊ID')
    return
  }
  loading.value = true
  try {
    const res = await getMerchantById(id)
    let data = res?.data?.data || res?.data || null
    if (res?.success && res?.data) {
      data = res.data
    }
    // 限定为陶瓷工坊
    if (data && (data.category === 'CERAMIC' || data.category === 'ceramic' || data.category === '陶瓷')) {
      workshop.value = normalizeWorkshop(data)
      merchantId.value = data.id
      await Promise.all([loadReviews(), loadActivities(data.id)])
      // 检查收藏状态
      if (userStore.isLoggedIn) {
        try {
          const res = await checkFavorite({ userId: userStore.user.id, targetId: data.id, targetType: FAVORITE_TYPE.MERCHANT })
          isFavorited.value = !!(res?.success && res?.data === true)
        } catch {}
      }
    } else {
      workshop.value = null
      ElMessage.warning('未找到该陶瓷工坊信息')
    }
  } catch (e) {
    console.error('加载陶瓷工坊详情失败:', e)
    ElMessage.error('加载陶瓷工坊详情失败')
  } finally {
    loading.value = false
  }
}

const handleSubmitReview = async (data) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再发表评价')
    const redirect = encodeURIComponent(window.location.pathname + window.location.search)
    router.push(`/login?redirect=${redirect}`)
    return
  }
  if (!merchantId.value) {
    ElMessage.error('无法获取工坊信息')
    return
  }
  try {
    await createMerchantReview({
      userId: userStore.user?.id,
      merchantId: merchantId.value,
      rating: data?.rating ?? 5,
      content: data?.content ?? '',
      images: Array.isArray(data?.images) ? data.images : []
    })
    ElMessage.success('评价发布成功')
    await loadReviews()
  } catch (error) {
    console.error('发布评价失败:', error)
    ElMessage.error(error?.response?.data?.message || '发布评价失败')
  }
}

const patchReplies = ({ reviewId, replies }) => {
  repliesByReview.value = { ...repliesByReview.value, [reviewId]: replies }
}

const loadReviews = async () => {
  if (!merchantId.value) return
  try {
    const res = await getCommentList(merchantId.value)
    const threads = Array.isArray(res?.data) ? res.data : []
    const map = {}
    reviews.value = threads.map((t) => {
      const { replies, ...rest } = t
      map[t.id] = replies || []
      return {
        ...rest,
        images: toImageArray(rest.images)
      }
    })
    repliesByReview.value = map
  } catch (e) {
    console.error('加载评价失败:', e)
  }
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

// 加载商家活动
const loadActivities = async (merchantId) => {
  if (!merchantId) return
  try {
    const res = await getMerchantActivities(merchantId)
    // request 拦截器已经把响应解包为 { success, data, message } 或直接返回数组
    if (res && res.success) {
      const data = res.data || []
      activities.value = Array.isArray(data) ? data : []
    } else if (Array.isArray(res)) {
      activities.value = res
    } else {
      activities.value = []
    }
  } catch (error) {
    console.error('加载活动失败:', error)
    activities.value = []
  }
}

// 判断活动是否进行中
const isActivityActive = (activity) => {
  const now = new Date()
  const end = activity.endTime ? new Date(activity.endTime) : null
  const start = activity.startTime ? new Date(activity.startTime) : null
  if (end && end < now) return false
  if (start && start > now) return false
  return true
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取活动图片列表
const getActivityImages = (images) => {
  if (!images) return []
  if (typeof images === 'string') {
    return images.split(',').filter(Boolean)
  }
  if (Array.isArray(images)) {
    return images
  }
  return []
}

// 处理实时数据更新
const handleRealtimeUpdate = async (updateInfo) => {
  const { operation, data: updateData } = updateInfo
  
  // 确保 workshop 已加载
  if (!workshop.value || !merchantId.value) return

  // 检查是否为当前商家的更新
  const currentId = String(merchantId.value)
  const updateId = updateData?.id ? String(updateData.id) : null

  if (operation === 'update' && updateId === currentId) {
    console.log('CeramicWorkshopDetail received update:', updateData)
    
    // 判断是否为评分/评论数变化（用户评论导致的更新）
    const isRatingUpdate = updateData.rating !== undefined || updateData.reviewCount !== undefined
    
    // 只有评分/评论数变化时才显示通知（用户评论导致）
    // 管理员更新商家信息时不显示通知
    if (isRatingUpdate) {
      ElNotification({
        title: '信息更新',
        message: '工坊信息已实时更新',
        type: 'success',
        position: 'bottom-right',
        duration: 3000
      })
    }
    
    // 更新本地数据
    const merged = { ...workshop.value, ...updateData }
    workshop.value = normalizeWorkshop(merged)

    // 评分/评论数变化时，刷新评论列表
    if (isRatingUpdate) {
      if (reloadingReviews.value) return
      reloadingReviews.value = true
      try {
        await loadReviews()
      } finally {
        reloadingReviews.value = false
      }
    }
  }
}

const toggleFavorite = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再收藏')
    router.push('/login')
    return
  }
  try {
    if (isFavorited.value) {
      await removeFavorite({ userId: userStore.user.id, targetId: merchantId.value, targetType: FAVORITE_TYPE.MERCHANT })
      isFavorited.value = false
      ElMessage.success('已取消收藏')
    } else {
      await addFavorite({ userId: userStore.user.id, targetId: merchantId.value, targetType: FAVORITE_TYPE.MERCHANT })
      isFavorited.value = true
      ElMessage.success('已添加到收藏')
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '操作失败')
  }
}

onMounted(() => {
  loadWorkshopDetail()
  
  // 连接WebSocket并订阅商家更新
  if (!realtimeSync.isConnected) {
    realtimeSync.connect()
  }
  realtimeSync.subscribe('merchant', handleRealtimeUpdate)
})

onUnmounted(() => {
  realtimeSync.unsubscribe('merchant', handleRealtimeUpdate)
})
</script>

<style scoped>
.workshop-detail {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.container {
  flex: 1;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.back-button-container {
  margin-bottom: 20px;
}

.header-section {
  display: flex;
  gap: 24px;
  margin-bottom: 32px;
}

.cover-wrapper {
  flex-shrink: 0;
  width: 360px;
  height: 240px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.placeholder-image {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}

.placeholder-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  gap: 8px;
}

.placeholder-content p {
  margin: 0;
  font-size: 14px;
}

.info-wrapper {
  flex: 1;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.title-row h1 {
  margin: 0;
  font-size: 28px;
  color: #303133;
}

.rating-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.rating-score {
  font-size: 18px;
  font-weight: 600;
  color: #e6a23c;
}

.review-count {
  font-size: 14px;
  color: #909399;
}

.description {
  margin: 0 0 16px 0;
  color: #606266;
  line-height: 1.6;
}

.base-info {
  margin-top: 8px;
}

.content-section {
  display: grid;
  grid-template-columns: 2fr 1.2fr;
  gap: 24px;
  margin-bottom: 40px;
}

.section-card {
  height: 100%;
}

.section-card-full {
  grid-column: 1 / -1;
}

.detail-text {
  color: #606266;
  line-height: 1.8;
}

.review-section {
  margin-top: 40px;
}

.activities-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.activity-item {
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fafafa;
  transition: all 0.3s;
}

.activity-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.activity-header h4 {
  margin: 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.activity-description {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 12px;
}

.activity-time {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
  font-size: 14px;
  margin-bottom: 12px;
}

.activity-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.activity-image {
  width: 120px;
  height: 90px;
  border-radius: 4px;
  cursor: pointer;
}

@media (max-width: 900px) {
  .header-section {
    flex-direction: column;
  }

  .cover-wrapper {
    width: 100%;
  }

  .content-section {
    grid-template-columns: 1fr;
  }
}
</style>


