<template>
  <div class="food-detail">
    <Header />

    <div class="container" v-loading="loading">
      <div class="back-button-container">
        <el-button @click="goBack" :icon="ArrowLeft" circle />
      </div>

      <el-empty
        v-if="!loading && !food"
        :description="$t('detail.notFound')"
      />

      <div v-else>
        <div class="header-section">
          <div class="cover-wrapper">
            <!-- 美食图片轮播图 -->
            <el-carousel
              v-if="imageList.length"
              height="320px"
              indicator-position="outside"
            >
              <el-carousel-item
                v-for="(img, index) in imageList"
                :key="index"
              >
                <img
                  :src="img"
                  :alt="`${food.name} ${$t('card.noImage')} ${index + 1}`"
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
            <h1>{{ food.name }}</h1>
            <div class="rating-row">
              <el-rate
                v-model="food.rating"
                disabled
                show-score
                text-color="#ff9900"
                :allow-half="true"
              />
              <span class="rating-score" v-if="food.rating">
                {{ food.rating.toFixed(1) }}
              </span>
              <span class="review-count" v-if="food.reviewCount !== undefined">
                {{ food.reviewCount }} {{ $t('card.reviews') }}
              </span>
              <el-tag type="success">
                {{ $t('detail.food') }}
              </el-tag>
              <el-tag 
                v-if="food.qualificationApproved" 
                type="success" 
                effect="plain" 
                style="margin-left: 8px"
              >
                {{ $t('card.qualificationApproved') }}
              </el-tag>
            </div>
            <p class="description">
              {{ food.description || $t('detail.foodDescription') }}
            </p>
            <el-descriptions :column="2" border class="base-info">
              <el-descriptions-item :label="$t('detail.perPerson')">
                {{ food.price ? `¥${food.price}/${$t('detail.person')}` : $t('detail.noPrice') }}
              </el-descriptions-item>
              <el-descriptions-item :label="$t('detail.cuisineType')">
                {{ getCuisineTypeLabel(food.category) }}
              </el-descriptions-item>
              <el-descriptions-item :label="$t('detail.address')">
                {{ food.address || $t('detail.noAddress') }}
              </el-descriptions-item>
              <el-descriptions-item :label="$t('detail.phone')" v-if="food.phone">
                {{ food.phone }}
              </el-descriptions-item>
            </el-descriptions>
            <div v-if="food.tags && food.tags.length" class="tags-section">
              <el-tag
                v-for="tag in food.tags"
                :key="tag"
                type="info"
                class="tag-item"
              >
                {{ tag }}
              </el-tag>
            </div>

            <div class="action-buttons">
              <el-button
                size="large"
                :type="isFavorited ? 'danger' : 'default'"
                :loading="favoriteLoading"
                @click="handleFavorite"
              >
                <el-icon><Star /></el-icon>
                {{ isFavorited ? $t('card.favorited') : $t('card.favorite') }}
              </el-button>
              <el-button type="success" size="large" @click="handleAddToPlan">
                <el-icon><Calendar /></el-icon>
                {{ $t('detail.addToPlan') }}
              </el-button>
              <el-button size="large" @click="handleShare">
                <el-icon><Share /></el-icon>
                {{ $t('detail.share') }}
              </el-button>
            </div>
          </div>
        </div>

        <div class="content-section">
          <el-card class="section-card">
            <template #header>
              <span>{{ $t('detail.introduction') }}</span>
            </template>
            <p class="detail-text">
              {{ food.description || $t('detail.noDescription') }}
            </p>
          </el-card>

          <el-card class="section-card">
            <template #header>
              <span>{{ $t('detail.merchantInfo') }}</span>
            </template>
            <p class="detail-text">
              {{ $t('detail.merchantInfoText') }}
            </p>
          </el-card>

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
                <div class="activity-body">
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
                    开始时间：{{ formatDateTime(activity.startTime) }}
                  </span>
                  <span v-else-if="activity.endTime">
                    结束时间：{{ formatDateTime(activity.endTime) }}
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
            </div>
          </el-card>
        </div>

        <!-- 评论区 -->
        <ReviewSection
          :reviews="reviews"
          :loading="loading"
          embed-replies
          :external-replies-map="repliesByReview"
          :placeholder="$t('detail.reviewPlaceholder')"
          @submit-review="handleSubmitReview"
          @replies-updated="patchReplies"
        />
      </div>
    </div>

    <Footer />

    <AddToPlanDialog
      v-model="showAddToPlanDialog"
      :item="itemToAdd"
      :click-position="clickPosition"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ArrowLeft, Picture, Clock, Share, Calendar, Star } from '@element-plus/icons-vue'
import { ElMessage, ElNotification } from 'element-plus'
import Header from '../../components/Layout/Header.vue'
import Footer from '../../components/Layout/Footer.vue'
import AddToPlanDialog from '../../components/AddToPlanDialog.vue'
import ReviewSection from '../../components/review/ReviewSection.vue'
import { getFoodDetail } from '../../api/food'
import { getMerchantById } from '../../api/merchant'
import { createMerchantReview } from '../../api/review'
import { getCommentList } from '../../api/comment'
import { getMerchantActivities } from '../../api/merchantActivity'
import { addFavorite, removeFavorite, checkFavorite } from '../../api/favoriteGateway'
import { FAVORITE_TYPE } from '../../utils/favoriteType'
import { useUserStore } from '../../stores/user'
import realtimeSync from '../../utils/websocket'
import { normalizeUrl } from '../../utils/image'

const { t } = useI18n()

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const food = ref(null)
const merchantId = ref(null)
const isFavorited = ref(false)
const favoriteLoading = ref(false)
const reviews = ref([])
const repliesByReview = ref({})
const reloadingReviews = ref(false)
const activities = ref([])
const showAddToPlanDialog = ref(false)
const itemToAdd = ref({})
const clickPosition = ref(null)

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

// 计算美食图片列表
const imageList = computed(() => {
  const f = food.value
  const list = []
  if (!f) {
    return []
  }

  // 1. 优先使用后端返回的 images 数组（enrichMerchantWithRating 已处理）
  if (Array.isArray(f.images) && f.images.length > 0) {
    f.images.forEach(url => {
      if (url && !list.includes(url)) {
        list.push(url)
      }
    })
  } else {
    // 2. 如果没有 images 数组，解析 shopImages 字符串
    if (f.shopImages) {
      if (Array.isArray(f.shopImages)) {
        f.shopImages.forEach(url => {
          if (url && !list.includes(url)) {
            list.push(url)
          }
        })
      } else if (typeof f.shopImages === 'string') {
        f.shopImages
          .split(',')
          .map(s => s.trim())
          .filter(Boolean)
          .forEach(url => {
            if (!list.includes(url)) {
              list.push(url)
            }
          })
      }
    }
    
    // 3. 如果还是没有图片，使用 cover、avatar 或 image
    const cover = f.cover || f.avatar || f.image
    if (cover && !list.includes(cover)) {
      list.unshift(cover) // 放在第一位作为封面
    }
  }

  return list
})

const goBack = () => {
  // 返回到美食推荐页面
  router.push({ 
    name: 'Recommend', 
    query: { type: 'food' } 
  })
}

const handleAddToPlan = (event) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后加入行程')
    router.push('/login')
    return
  }
  
  if (!food.value) return
  
  if (event) {
    clickPosition.value = {
      x: event.clientX,
      y: event.clientY
    }
  } else {
    clickPosition.value = null
  }

  itemToAdd.value = {
    id: food.value.id,
    title: food.value.name,
    type: 'food',
    description: food.value.description || '享用美食'
  }
  showAddToPlanDialog.value = true
}

const handleShare = () => {
  const url = window.location.href
  navigator.clipboard.writeText(url).then(() => {
    ElMessage.success('链接已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制链接')
  })
}

const handleFavorite = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再收藏')
    router.push('/login')
    return
  }
  if (!merchantId.value) return
  try {
    favoriteLoading.value = true
    if (!isFavorited.value) {
      await addFavorite({ userId: userStore.user.id, targetId: merchantId.value, targetType: FAVORITE_TYPE.MERCHANT })
      isFavorited.value = true
      ElMessage.success('已收藏')
    } else {
      await removeFavorite({ userId: userStore.user.id, targetId: merchantId.value, targetType: FAVORITE_TYPE.MERCHANT })
      isFavorited.value = false
      ElMessage.success('已取消收藏')
    }
  } catch (e) {
    ElMessage.error(e?.message || '操作失败')
    try {
      const res = await checkFavorite({ userId: userStore.user.id, targetId: merchantId.value, targetType: FAVORITE_TYPE.MERCHANT })
      isFavorited.value = !!(res?.data ?? res)
    } catch {}
  } finally {
    favoriteLoading.value = false
  }
}

const normalizeFoodData = (raw) => {
  if (!raw) return null
  const f = { ...raw }
  
  // 处理评分 (优先使用管理员评分)
  if (f.adminRating !== undefined && f.adminRating !== null && f.adminRating > 0) {
    f.rating = parseFloat(f.adminRating)
  } else if (f.rating === undefined || f.rating === null) {
    f.rating = 4.0
  } else if (typeof f.rating === 'string') {
    f.rating = parseFloat(f.rating) || 4.0
  }
  
  // 处理评价数量
  if (f.reviewCount === undefined || f.reviewCount === null) {
    f.reviewCount = 0
  }

  // 处理人均消费：优先 price，其次 avgPrice，支持 "50-100" 范围字符串
  const rawPrice = f.price ?? f.avgPrice ?? null
  if (rawPrice === null || rawPrice === '' || rawPrice === undefined) {
    f.price = null
  } else if (typeof rawPrice === 'string') {
    const parts = rawPrice.split('-').map(Number).filter(Number.isFinite)
    f.price = parts.length > 0 ? rawPrice : null // 保留原始字符串用于显示
  } else {
    f.price = rawPrice
  }

  // 处理标签（可能是字符串，需要转换为数组）
  if (f.tags && typeof f.tags === 'string') {
    f.tags = f.tags.split(',').map(s => s.trim()).filter(Boolean)
  } else if (!f.tags) {
    f.tags = []
  }
  
  return f
}

const loadFoodDetail = async () => {
  const id = route.params.id
  if (!id) {
    ElMessage.error('缺少美食ID')
    return
  }
  loading.value = true
  try {
    // 先尝试从商家 API 获取（因为美食推荐列表中的商家数据使用商家ID）
    // 这样可以避免Food表和Merchant表的ID冲突问题
    try {
      const merchantRes = await getMerchantById(id)
      let merchantData = merchantRes?.data?.data || merchantRes?.data || merchantRes?.merchant || null
      if (merchantRes?.success && merchantRes?.data) {
        merchantData = merchantRes.data
      }
      // 严格检查：必须是 FOOD 类型的商家
      if (merchantData && (merchantData.category === 'FOOD' || merchantData.category === 'food')) {
        // 保存商家ID用于评价
        merchantId.value = merchantData.id
        // 将商家数据转换为美食数据格式
        food.value = normalizeFoodData({
          id: merchantData.id,
          name: merchantData.shopName || merchantData.name,
          description: merchantData.description,
          image: merchantData.avatar,
          images: merchantData.shopImages ? (Array.isArray(merchantData.shopImages) ? merchantData.shopImages : merchantData.shopImages.split(',')) : [],
          rating: merchantData.rating,
          reviewCount: merchantData.reviewCount,
          price: merchantData.avgPrice ?? merchantData.avg_price ?? merchantData.price ?? merchantData.startPrice ?? null,
          category: merchantData.cuisineType || '其他',
          address: merchantData.address,
          phone: merchantData.phone,
          tags: []
        })
        // 加载评价列表和活动
        await Promise.all([loadReviews(), loadActivities(merchantData.id)])
        return
      }
    } catch (e) {
      console.log('商家API未找到，尝试从Food API获取:', e)
    }
    
    // 如果商家 API 失败，尝试从 food API 获取（Food表中的数据）
    try {
      const res = await getFoodDetail(id)
      let data = res?.data?.data || res?.data || res?.food || null
      if (res?.success && res?.data) {
        data = res.data
      }
      if (data) {
        food.value = normalizeFoodData(data)
        return
      }
    } catch (e) {
      console.log('Food API 也未找到:', e)
    }
    
    ElMessage.warning('未找到该美食信息')
  } catch (e) {
    console.error('加载美食详情失败:', e)
    ElMessage.error('加载美食详情失败')
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
    ElMessage.error('无法获取商家信息')
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
    const response = await getCommentList(merchantId.value)
    const threads = Array.isArray(response?.data) ? response.data : []
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
  } catch (error) {
    console.error('加载评价失败:', error)
  }
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

// 餐饮类型映射：将英文值转换为中文显示
const getCuisineTypeLabel = (type) => {
  const typeMap = {
    'SNACK': '小吃',
    'BEVERAGE': '奶茶饮品',
    'DESSERT': '甜品',
    'FAST_FOOD': '快餐',
    'HOTPOT': '火锅',
    'BBQ': '烧烤',
    'NOODLE': '面食',
    'BAKERY': '烘焙',
    'COFFEE': '咖啡',
    'LIGHT_MEAL': '轻食',
    'LOCAL': '地方特色',
    'OTHER': '其他',
    // 兼容旧的菜系值
    'GAN': '赣菜',
    'CHUAN': '川菜',
    'XIANG': '湘菜',
    'YUE': '粤菜',
    'LU': '鲁菜',
    'SU': '苏菜',
    'ZHE': '浙菜',
    'HUI': '徽菜',
    'MIN': '闽菜'
  }
  return typeMap[type] || type || '其他'
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
  
  // 确保 food 已加载
  if (!food.value || !merchantId.value) return

  // 检查是否为当前商家的更新
  const currentId = String(merchantId.value)
  const updateId = updateData?.id ? String(updateData.id) : null

  if (operation === 'update' && updateId === currentId) {
    console.log('FoodDetail received update:', updateData)
    
    // 判断是否为评分/评论数变化（用户评论导致的更新）
    const isRatingUpdate = updateData.rating !== undefined || updateData.reviewCount !== undefined
    
    // 只有评分/评论数变化时才显示通知（用户评论导致）
    // 管理员更新商家信息时不显示通知
    if (isRatingUpdate) {
      ElNotification({
        title: '信息更新',
        message: '商家信息已实时更新',
        type: 'success',
        position: 'bottom-right',
        duration: 3000
      })
    }
    
    // 构建映射后的数据，保持 normalizeFoodData 需要的结构
    // 注意：updateData 是 Merchant 结构，food.value 是 Food View Model 结构
    const mappedData = {
        id: updateData.id || food.value.id,
        name: updateData.shopName || updateData.name || food.value.name,
        description: updateData.description !== undefined ? updateData.description : food.value.description,
        image: updateData.avatar !== undefined ? updateData.avatar : food.value.image,
        images: updateData.shopImages ? (Array.isArray(updateData.shopImages) ? updateData.shopImages : updateData.shopImages.split(',')) : food.value.images,
        rating: updateData.rating !== undefined ? updateData.rating : food.value.rating,
        reviewCount: updateData.reviewCount !== undefined ? updateData.reviewCount : food.value.reviewCount,
        price: updateData.avgPrice !== undefined ? updateData.avgPrice : food.value.price,
        category: updateData.cuisineType || updateData.category || food.value.category,
        address: updateData.address !== undefined ? updateData.address : food.value.address,
        phone: updateData.phone !== undefined ? updateData.phone : food.value.phone,
        // 保留原有的 tags
        tags: food.value.tags
    }

    // 更新本地信息
    food.value = normalizeFoodData(mappedData)

    // 评分/评论数变化时，刷新评论列表
    if (updateData.rating !== undefined || updateData.reviewCount !== undefined) {
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

onMounted(() => {
  loadFoodDetail().then(async () => {
    if (userStore.isLoggedIn && merchantId.value) {
      try {
        const res = await checkFavorite({ userId: userStore.user.id, targetId: merchantId.value, targetType: FAVORITE_TYPE.MERCHANT })
        isFavorited.value = !!(res?.data ?? res)
      } catch {}
    }
  })
  
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
.food-detail {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #ffffff;
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
  width: 480px;
  height: 320px;
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

.info-wrapper h1 {
  margin: 0 0 12px 0;
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

.tags-section {
  margin-top: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  margin: 0;
}

.content-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
  margin-bottom: 40px;
}

.section-card {
  height: 100%;
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
  gap: 12px;
}

.activity-item {
  display: flex;
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
  border: 1px solid #eef0f4;
  transition: box-shadow 220ms ease, transform 220ms ease;
}

.activity-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
}

.activity-item::before {
  content: '';
  display: block;
  width: 4px;
  flex-shrink: 0;
  background: linear-gradient(180deg, #e6a23c, #f5a623);
}

.activity-body {
  flex: 1;
  padding: 14px 16px 12px;
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  gap: 10px;
}

.activity-header h4 {
  margin: 0;
  color: #1a1a1a;
  font-size: 15px;
  font-weight: 600;
  line-height: 1.4;
}

.activity-description {
  color: #606266;
  font-size: 13px;
  line-height: 1.65;
  margin: 0 0 8px;
}

.activity-time {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: #909399;
  font-size: 12px;
  background: #f7f8fa;
  padding: 3px 8px;
  border-radius: 20px;
}

.activity-images {
  display: flex;
  gap: 6px;
  margin-top: 10px;
  flex-wrap: wrap;
}

.activity-image {
  width: 72px;
  height: 72px;
  border-radius: 6px;
  cursor: pointer;
  object-fit: cover;
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

