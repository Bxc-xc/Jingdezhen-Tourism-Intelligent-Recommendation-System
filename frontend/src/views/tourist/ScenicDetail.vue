<template>
  <div class="scenic-detail">
    <Header />
 
    <div class="container" v-loading="scenicStore.loading">
      <!-- 返回键 -->
      <div class="back-button-container">
        <el-button @click="goBack" :icon="ArrowLeft" circle />
      </div>

      <!-- 酒店/民宿 两栏产品化布局 -->
      <div v-if="scenicStore.currentScenic && isHotelLike" class="hotel-two-col">
        <!-- 左侧主内容 -->
        <div class="hotel-main">
          <HotelInfo
            :scenic="scenicStore.currentScenic"
            @open-map="openInMap"
            @choose-room="handlePlan"
          />
          <HotelGallery
            :images="galleryImages"
            @preview="previewImage"
            @view-all="() => showImagePreview = true"
            @favorite="handleFavorite"
          />
          <HotelFacilities
            :facilities="hotelFacilities"
          />
          <section class="hotel-card">
            <h2 class="card-title">酒店简介</h2>
            <div class="desc" :class="{ collapsed: !introExpanded }">
              {{ scenicStore.currentScenic.description || '暂无信息' }}
            </div>
            <el-button link type="primary" class="link-more" @click="introExpanded = !introExpanded">
              {{ introExpanded ? '收起' : '查看更多' }}
            </el-button>
          </section>
        </div>
        <!-- 右侧信息栏 -->
        <div class="hotel-side">
          <HotelSidebar
            :rating="scoreNumber"
            :review-count="scenicStore.currentScenic.reviewCount || 0"
            :summary="scoreSummary"
            :nearby="nearbyList"
            @open-map="openInMap"
            @choose-room="handlePlan"
          />
        </div>
      </div>
      
      <div v-else-if="scenicStore.currentScenic" class="detail-content">
        <!-- ===== 新布局：左右两栏，右侧 sticky ===== -->
        <div class="scenic-two-col">

          <!-- 左栏：图片 + 主内容 + 评论 -->
          <div class="scenic-left-col">
            <!-- 仅图片区（隐藏信息卡，信息卡移到右栏） -->
            <ScenicDetailHeader
              :scenic="scenicStore.currentScenic"
              :images="galleryImages"
              :live-review-count="reviews.length"
              :hide-info="true"
              @scroll-to-reviews="scrollToReviews"
            />

            <!-- 主内容区域 -->
            <div class="scenic-main-content">
            <!-- 景点详细信息组件 -->
            <ScenicDetailInfo :scenic="scenicStore.currentScenic" />
            
            <!-- 商家活动 -->
            <div v-if="scenicStore.currentScenic.activities && scenicStore.currentScenic.activities.filter(a => isActivityActive(a)).length > 0" class="scenic-activities">
              <h3>{{ $t('detail.promotion') }}</h3>
              <div class="activities-list">
                <div
                  v-for="activity in scenicStore.currentScenic.activities.filter(a => isActivityActive(a))"
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
              </div>
            </div>

            <!-- 景点特色 -->
            <div v-if="scenicFeaturesList.length" class="scenic-features">
              <h3>{{ $t('detail.features') }}</h3>
              <div class="features-grid">
                <div
                  v-for="feature in scenicFeaturesList"
                  :key="feature"
                  class="feature-item"
                >
                  <el-icon><Star /></el-icon>
                  <span>{{ feature }}</span>
                </div>
              </div>
            </div>

            <!-- 亮点推荐 -->
            <div v-if="scenicHighlightsList.length" class="scenic-highlights">
              <h3>{{ $t('detail.highlights') }}</h3>
              <div class="highlights-list">
                <div
                  v-for="highlight in scenicHighlightsList"
                  :key="highlight"
                  class="highlight-item"
                >
                  <el-icon><Check /></el-icon>
                  <span>{{ highlight }}</span>
                </div>
              </div>
            </div>

            <!-- 设施服务 -->
            <div v-if="scenicFacilitiesList.length" class="scenic-facilities">
              <h3>{{ $t('detail.facilities') }}</h3>
              <div class="facilities-grid">
                <div
                  v-for="facility in scenicFacilitiesList"
                  :key="facility"
                  class="facility-item"
                >
                  <el-icon><Setting /></el-icon>
                  <span>{{ facility }}</span>
                </div>
              </div>
            </div>

            <div class="scenic-details">
              <el-row :gutter="20">
                <el-col :span="12">
                  <div class="detail-item">
                    <el-icon><Clock /></el-icon>
                    <span>{{ $t('detail.openTime') }}: {{ scenicStore.currentScenic.openTime || $t('detail.allDay') }}</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="detail-item">
                    <el-icon><Location /></el-icon>
                    <span>{{ $t('detail.address') }}: {{ scenicStore.currentScenic.address || $t('detail.defaultAddress') }}</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="detail-item">
                    <el-icon><Phone /></el-icon>
                    <span>{{ $t('detail.phone') }}: {{ scenicStore.currentScenic.phone || $t('detail.noInfo') }}</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="detail-item">
                    <el-icon><Star /></el-icon>
                    <span>{{ $t('detail.category') }}: {{ scenicStore.currentScenic.category || $t('merchant.category.scenic') }}</span>
                  </div>
                </el-col>
              </el-row>
            </div>

            <!-- 游玩提示 -->
            <div v-if="scenicStore.currentScenic.tips" class="scenic-tips">
              <h3>{{ $t('detail.tips') }}</h3>
              <div class="tips-content">
                <el-icon><InfoFilled /></el-icon>
                <span>{{ scenicStore.currentScenic.tips }}</span>
              </div>
            </div>

            <!-- 地图位置 -->
            <div v-if="scenicStore.currentScenic.coordinates" class="scenic-map">
              <h3>{{ $t('detail.locationInfo') }}</h3>
              <div class="map-container">
                <div class="map-placeholder">
                  <el-icon><Location /></el-icon>
                  <span>{{ $t('detail.mapLoading') }}</span>
                </div>
                <div class="map-actions">
                  <el-button size="small" @click="openInMap">
                    <el-icon><Location /></el-icon>
                    {{ $t('detail.viewInMap') }}
                  </el-button>
                  <el-button size="small" @click="getDirections">
                    <el-icon><Guide /></el-icon>
                    {{ $t('detail.getDirections') }}
                  </el-button>
                </div>
              </div>
            </div>
          </div><!-- /scenic-main-content -->

            <!-- 评论区（在左栏内） -->
            <ReviewSection
              ref="reviewSection"
              :reviews="reviews"
              :loading="loading"
              embed-replies
              :external-replies-map="repliesByReview"
              :placeholder="$t('detail.reviewPlaceholder')"
              @submit-review="handleSubmitReview"
              @delete-review="deleteMyReview"
              @replies-updated="patchReplies"
            />
          </div><!-- /scenic-left-col -->

          <!-- 右栏：信息卡 + 按钮组 + 附近推荐（sticky） -->
          <div class="scenic-right-col">

            <!-- 信息卡：名称、评分、地址、开放时间 -->
            <div class="scenic-info-card">
              <!-- 标题 -->
              <div class="sic-title-row">
                <h1 class="sic-title">{{ scenicStore.currentScenic.name }}</h1>
                <el-tag v-if="scenicStore.currentScenic.tag" type="primary" class="sic-tag">{{ scenicStore.currentScenic.tag }}</el-tag>
              </div>

              <!-- 评分行 -->
              <div class="sic-rating-row" @click="scrollToReviews">
                <span class="sic-score-badge">{{ scoreNumber }}</span>
                <span class="sic-score-text">{{ scoreNumber }}/5</span>
                <span class="sic-review-count">{{ reviews.length }}条点评</span>
                <el-icon class="sic-arrow"><ArrowRight /></el-icon>
              </div>

              <!-- 分隔线 -->
              <div class="sic-divider"></div>

              <!-- 地址 -->
              <div class="sic-row">
                <el-icon class="sic-icon"><Location /></el-icon>
                <span class="sic-value">{{ scenicStore.currentScenic.address || '暂无地址信息' }}</span>
              </div>

              <!-- 开放时间 -->
              <div class="sic-row">
                <el-icon class="sic-icon"><Clock /></el-icon>
                <div class="sic-value sic-time-row">
                  <span class="sic-open-badge" :class="isOpenNow ? 'sic-open-badge--open' : 'sic-open-badge--closed'">
                    {{ isOpenNow ? '开放中' : '已关闭' }}
                  </span>
                  <span>{{ scenicStore.currentScenic.openTime || '全天开放' }}</span>
                </div>
              </div>

              <!-- 电话（有则显示） -->
              <div v-if="scenicStore.currentScenic.phone" class="sic-row">
                <el-icon class="sic-icon"><Phone /></el-icon>
                <span class="sic-value">{{ scenicStore.currentScenic.phone }}</span>
              </div>
            </div>

            <!-- 操作按钮组 -->
            <div class="prd-action-card">
              <button class="prd-btn prd-btn--primary" @click="handlePlan">
                <span class="prd-btn__icon">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="3" y="4" width="18" height="18" rx="3"/>
                    <line x1="16" y1="2" x2="16" y2="6"/>
                    <line x1="8" y1="2" x2="8" y2="6"/>
                    <line x1="3" y1="10" x2="21" y2="10"/>
                    <line x1="12" y1="14" x2="12" y2="18"/>
                    <line x1="10" y1="16" x2="14" y2="16"/>
                  </svg>
                </span>
                <span class="prd-btn__label">{{ $t('detail.addToPlan') }}</span>
              </button>
              <div class="prd-btn-row">
                <button
                  class="prd-btn prd-btn--ghost"
                  :class="{ 'prd-btn--favorited': isFavorited }"
                  :disabled="favoriteLoading"
                  @click="handleFavorite"
                >
                  <span class="prd-btn__icon">
                    <svg viewBox="0 0 24 24" :fill="isFavorited ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                    </svg>
                  </span>
                  <span class="prd-btn__label">{{ isFavorited ? $t('card.favorited') : $t('card.favorite') }}</span>
                </button>
                <button class="prd-btn prd-btn--ghost" @click="handleShare">
                  <span class="prd-btn__icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                      <circle cx="18" cy="5" r="3"/>
                      <circle cx="6" cy="12" r="3"/>
                      <circle cx="18" cy="19" r="3"/>
                      <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/>
                      <line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/>
                    </svg>
                  </span>
                  <span class="prd-btn__label">{{ $t('detail.share') }}</span>
                </button>
              </div>
            </div>

            <!-- 附近推荐 -->
            <ScenicDetailSidebar
              :nearby-food="nearbyFoodList"
              :nearby-shopping="nearbyShoppingList"
              @view-more="handleViewMore"
              @navigate="handleNavigateToMerchant"
            />

          </div><!-- /scenic-right-col -->
        </div><!-- /scenic-two-col -->
      </div>
    </div>

    <AddToPlanDialog
      v-model="showAddToPlanDialog"
      :item="itemToAdd"
      :click-position="clickPosition"
    />

    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useScenicStore } from '../../stores/scenic'
import { useUserStore } from '../../stores/user'
import { createReview, deleteReview as deleteReviewApi } from '../../api/review'
import { getCommentList } from '../../api/comment'
import { addFavorite, removeFavorite, checkFavorite } from '../../api/favoriteGateway'
import { getMerchantActivities } from '../../api/merchantActivity'
import { getMerchantById, getMerchantByScenicId } from '../../api/merchant'
import { ElMessage, ElNotification, ElMessageBox } from 'element-plus'
import { ArrowLeft, ArrowRight, Star, Calendar, Share, Location, Phone, Clock, InfoFilled, Check, Setting, Guide, Picture } from '@element-plus/icons-vue'
import Header from '../../components/Layout/Header.vue'
import Footer from '../../components/Layout/Footer.vue'
import ReviewSection from '../../components/review/ReviewSection.vue'
import realtimeSync from '../../utils/websocket'
import GalleryMosaic from '../../components/common/GalleryMosaic.vue'
import ScenicCard from '../../components/ScenicCard.vue'
import HotelInfo from '../../components/hotel/HotelInfo.vue'
import HotelGallery from '../../components/hotel/HotelGallery.vue'
import HotelFacilities from '../../components/hotel/HotelFacilities.vue'
import HotelSidebar from '../../components/hotel/HotelSidebar.vue'
// 新增：景点详情组件
import ScenicDetailHeader from '../../components/scenic/ScenicDetailHeader.vue'
import ScenicDetailInfo from '../../components/scenic/ScenicDetailInfo.vue'
import ScenicDetailSidebar from '../../components/scenic/ScenicDetailSidebar.vue'
import AddToPlanDialog from '../../components/AddToPlanDialog.vue'
import { FAVORITE_TYPE } from '../../utils/favoriteType'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const scenicStore = useScenicStore()
const userStore = useUserStore()

const isFavorited = ref(false)
const favoriteLoading = ref(false)
const loading = ref(false)
const reviews = ref([])
const repliesByReview = ref({})
const showImagePreview = ref(false)
const previewImageIndex = ref(0)
const currentImageIndex = ref(0)
const carouselRef = ref(null)

const reviewSection = ref(null)
const scrollToReviews = () => {
  reviewSection.value?.$el?.scrollIntoView({ behavior: 'smooth' })
}

const showAddToPlanDialog = ref(false)
const itemToAdd = ref({})
const clickPosition = ref(null)

import { normalizeUrl, normalizeList, FALLBACK_IMAGE, buildSrcSet } from '../../utils/image'
const API_BASE_URL = import.meta?.env?.VITE_API_BASE_URL || '/api'
const uploadHeaders = computed(() => {
  return userStore.token ? { Authorization: `Bearer ${userStore.token}` } : {}
})

// 是否渲染酒店化布局（不改API，基于已有字段推断）
const isHotelLike = computed(() => {
  const cur = scenicStore.currentScenic || {}
  const cat = String(cur.category || cur.type || '').toLowerCase()
  const name = String(cur.name || '').toLowerCase()
  return ['酒店','民宿','hotel','inn','stay'].some(k => cat.includes(k)) ||
         ['酒店','民宿','hotel','inn','stay'].some(k => name.includes(k))
})

// 评分显示
const scoreNumber = computed(() => {
  const r = Number(scenicStore.currentScenic?.rating || 4.8)
  return r.toFixed(1)
})

// 动态判断是否在开放时间内，openTime 格式如 "08:00-18:00" 或 "全天开放"
const isOpenNow = computed(() => {
  const openTime = scenicStore.currentScenic?.openTime
  if (!openTime) return true // 无信息默认开放
  if (openTime.includes('全天') || openTime.includes('24')) return true
  const match = openTime.match(/(\d{1,2}):(\d{2})\s*[-~–]\s*(\d{1,2}):(\d{2})/)
  if (!match) return true
  const now = new Date()
  const cur = now.getHours() * 60 + now.getMinutes()
  const open = parseInt(match[1]) * 60 + parseInt(match[2])
  const close = parseInt(match[3]) * 60 + parseInt(match[4])
  return cur >= open && cur <= close
})
const scoreSummary = computed(() => {
  const c = scenicStore.currentScenic?.reviewCount || 0
  return c > 0 ? `房间宽敞，服务贴心，位置优越。来自${c}位住客的评价摘要。` : '暂无评价'
})

// 将字符串或数组统一解析为数组（逗号/顿号/换行分隔）
const parseStringList = (raw) => {
  if (!raw) return []
  if (Array.isArray(raw)) return raw.map(s => String(s).trim()).filter(Boolean)
  if (typeof raw === 'string') {
    try {
      const parsed = JSON.parse(raw)
      if (Array.isArray(parsed)) return parsed.map(s => String(s).trim()).filter(Boolean)
    } catch {}
    return raw.split(/[,，、\n]/).map(s => s.trim()).filter(Boolean)
  }
  return []
}

// 景点特色列表
const scenicFeaturesList = computed(() => parseStringList(scenicStore.currentScenic?.features))
// 亮点推荐列表
const scenicHighlightsList = computed(() => parseStringList(scenicStore.currentScenic?.highlights))
// 设施服务列表（景点用）
const scenicFacilitiesList = computed(() => parseStringList(scenicStore.currentScenic?.facilities))

// 设施：优先后端字段，缺省给出常见与景德镇特色
const hotelFacilities = computed(() => {
  const list = parseStringList(scenicStore.currentScenic?.facilities)
  if (list.length) return list
  return ['泳池','餐厅','健身房','停车场','亲子活动','陶艺体验工坊','陶瓷文创商店','自助早餐']
})

// 简介折叠
const introExpanded = ref(false)

// 推荐列表（非侵入：尽量从已存在的数据中推导）
const recommendedList = computed(() => {
  const s = scenicStore
  const cur = s.currentScenic
  if (!cur) return []
  // 优先使用可能存在的推荐字段
  const direct =
    s.relatedScenics ||
    s.recommendations ||
    s.similar ||
    s.similarScenics ||
    []
  if (Array.isArray(direct) && direct.length) {
    return direct.filter(i => i && i.id !== cur.id).slice(0, 8)
  }
  // 兜底：从全量或列表中过滤同分类/同标签
  const source = s.list || s.scenics || []
  const curTags = (cur.tags || '').split(',').map(t => t && t.trim()).filter(Boolean)
  const byTag = source.filter(i => {
    if (!i || i.id === cur.id) return false
    if (i.category && cur.category && i.category === cur.category) return true
    const tags = (i.tags || '').split(',').map(t => t && t.trim()).filter(Boolean)
    return tags.some(t => curTags.includes(t))
  })
  // 最多 8 个
  return byTag.slice(0, 8)
})

// 附近美食列表
const nearbyFoodList = computed(() => {
  const source = scenicStore.list || scenicStore.scenics || []
  return source
    .filter(item => {
      const cat = String(item.category || '').toLowerCase()
      return cat.includes('餐饮') || cat.includes('美食') || cat.includes('food')
    })
    .slice(0, 3)
})

// 附近购物列表
const nearbyShoppingList = computed(() => {
  const source = scenicStore.list || scenicStore.scenics || []
  return source
    .filter(item => {
      const cat = String(item.category || '').toLowerCase()
      return cat.includes('购物') || cat.includes('商店') || cat.includes('shop') || cat.includes('陶瓷')
    })
    .slice(0, 3)
})

// 将后端返回的 images 字段规范为数组
const toImageArray = (images) => {
  if (!images) return []
  if (Array.isArray(images)) return images.filter(Boolean)
  if (typeof images === 'string') {
    try {
      // 尝试解析为 JSON 数组字符串
      const parsed = JSON.parse(images)
      if (Array.isArray(parsed)) return parsed.filter(Boolean)
    } catch (e) {}
    // 回退：按逗号分隔
    return images.split(',').map(s => s && s.trim()).filter(Boolean)
  }
  return []
}

const galleryImages = computed(() => {
  const imgs = []
  const s = scenicStore.currentScenic || {}

  // 后端可能返回 scenicImages / images / shopImages 三种字段之一，均为逗号分隔
  let multiImages = s.scenicImages || s.images || s.shopImages || ''
  imgs.push(...normalizeList(multiImages))

  // 单张封面
  if (s.image) imgs.push(normalizeUrl(s.image))
  else if (s.avatar) imgs.push(normalizeUrl(s.avatar))

  // 去重保持顺序
  const dedup = Array.from(new Set(imgs.filter(Boolean)))
  return dedup.length > 0 ? dedup : [FALLBACK_IMAGE]
})

const getValidImage = (url) => normalizeUrl(url)

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

const handleImageError = (event) => {
  event.target.src = FALLBACK_IMAGE
}

// 图片预览相关函数
const previewImage = (index) => {
  previewImageIndex.value = index
  showImagePreview.value = true
}

const closeImagePreview = () => {
  showImagePreview.value = false
}

const prevImage = () => {
  if (previewImageIndex.value > 0) {
    previewImageIndex.value--
  }
}

const nextImage = () => {
  if (previewImageIndex.value < galleryImages.value.length - 1) {
    previewImageIndex.value++
  }
}

const handleCarouselChange = (index) => {
  currentImageIndex.value = index
}

const switchToImage = (index) => {
  currentImageIndex.value = index
  if (carouselRef.value) {
    carouselRef.value.setActiveItem(index)
  }
}

const handleFavorite = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再收藏')
    return
  }
  if (!scenicStore.currentScenic?.id) return

  try {
    favoriteLoading.value = true
    if (!isFavorited.value) {
      await addFavorite({
        userId: userStore.user.id,
        targetId: scenicStore.currentScenic.id,
        targetType: FAVORITE_TYPE.SCENIC
      })
      isFavorited.value = true
      ElMessage.success('已收藏')
    } else {
      await removeFavorite({
        userId: userStore.user.id,
        targetId: scenicStore.currentScenic.id,
        targetType: FAVORITE_TYPE.SCENIC
      })
      isFavorited.value = false
      ElMessage.success('已取消收藏')
    }
  } catch (e) {
    const status = e?.response?.status
    ElMessage.error(status === 400 || status === 404 ? '收藏对象不存在或已下架' : (e?.message || '操作失败'))
    try {
      const res = await checkFavorite({
        userId: userStore.user.id,
        targetId: scenicStore.currentScenic.id,
        targetType: FAVORITE_TYPE.SCENIC
      })
      isFavorited.value = !!(res?.data ?? res)
    } catch {}
  } finally {
    favoriteLoading.value = false
  }
}

const handlePlan = (event) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再加入行程')
    const redirect = encodeURIComponent(window.location.pathname + window.location.search)
    router.push(`/login?redirect=${redirect}`)
    return
  }

  if (!scenicStore.currentScenic) return

  if (event) {
    clickPosition.value = {
      x: event.clientX,
      y: event.clientY
    }
  } else {
    clickPosition.value = null
  }

  itemToAdd.value = {
    id: scenicStore.currentScenic.id,
    title: scenicStore.currentScenic.name,
    type: 'scenic',
    description: scenicStore.currentScenic.description || '景点游玩'
  }
  showAddToPlanDialog.value = true
}

const handleShare = async () => {
  try {
    await navigator.clipboard.writeText(window.location.href)
    ElMessage.success('链接已复制')
  } catch (e) {
    ElMessage.error('复制失败，请手动复制地址栏链接')
  }
}

// 处理查看更多附近推荐
const handleViewMore = (type) => {
  if (type === 'food') {
    router.push({ path: '/recommend', query: { category: '餐饮' } })
  } else if (type === 'shopping') {
    router.push({ path: '/recommend', query: { category: '购物' } })
  }
}

// 处理导航到商家详情
const handleNavigateToMerchant = (item) => {
  if (!item || !item.id) return
  
  const cat = String(item.category || '').toLowerCase()
  if (cat.includes('酒店') || cat.includes('民宿')) {
    router.push(`/hotel/${item.id}`)
  } else if (cat.includes('餐饮') || cat.includes('美食')) {
    router.push(`/food/${item.id}`)
  } else if (cat.includes('陶瓷') || cat.includes('工坊')) {
    router.push(`/ceramic/${item.id}`)
  } else {
    router.push(`/scenic/${item.id}`)
  }
}

const handleSubmitReview = async (data) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再发布评价')
    return
  }
  if (!scenicStore.currentScenic?.id) {
    ElMessage.error('景点信息加载中，请稍后再试')
    return
  }
  try {
    await createReview({
      userId: userStore.user?.id,
      scenicSpotId: scenicStore.currentScenic.id,
      rating: data?.rating ?? 5,
      content: data?.content ?? '',
      images: Array.isArray(data?.images) ? data.images : []
    })
    ElMessage.success('评价发布成功')
    await loadReviews()
  } catch (error) {
    console.error('发布评价失败:', error)
    ElMessage.error(error?.response?.data?.message || error?.message || '发布评价失败，请稍后再试')
  }
}

const deleteMyReview = async (review) => {
  try {
    await ElMessageBox.confirm('确认删除这条评价？', '提示', { type: 'warning' })
    await deleteReviewApi(Number(review.id))
    ElMessage.success('已删除')
    reviews.value = reviews.value.filter(r => r.id !== review.id)
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e?.response?.data?.message || '删除失败')
    }
  }
}

const patchReplies = ({ reviewId, replies }) => {
  repliesByReview.value = { ...repliesByReview.value, [reviewId]: replies }
}

const loadReviews = async () => {
  if (!scenicStore.currentScenic?.id) return
  try {
    loading.value = true
    const response = await getCommentList(scenicStore.currentScenic.id)
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
  } finally {
    loading.value = false
  }
}

const formatTime = (time) => {
  return new Date(time).toLocaleDateString()
}

const openInMap = () => {
  if (scenicStore.currentScenic.coordinates) {
    const { lat, lng } = scenicStore.currentScenic.coordinates
    const mapUrl = `https://map.baidu.com/?q=${lat},${lng}&title=${scenicStore.currentScenic.name}`
    window.open(mapUrl, '_blank')
  }
}

const getDirections = () => {
  if (scenicStore.currentScenic.coordinates) {
    const { lat, lng } = scenicStore.currentScenic.coordinates
    const address = scenicStore.currentScenic.address || scenicStore.currentScenic.name
    const mapUrl = `https://map.baidu.com/?q=${address}&coord=${lat},${lng}&mode=driving`
    window.open(mapUrl, '_blank')
  }
}

const goBack = () => {
  // 返回到景点推荐页面，确保筛选选中“景点”
  router.push({
    name: 'Recommend',
    query: { type: 'scenic' }
  })
}

// 加载景点活动（如果景点是商家类型）
const loadScenicActivities = async () => {
  // 活动信息已由后端在景点详情接口中直接注入，无需单独请求
}

// 处理实时数据更新
const handleRealtimeUpdate = (updateInfo) => {
  const { operation, data: updateData } = updateInfo
  
  // 确保景点已加载
  if (!scenicStore.currentScenic || !scenicStore.currentScenic.id) return
  
  // 检查是否为当前景点关联商家的更新
  // 景点可能通过商家ID或景点ID关联
  const currentId = String(scenicStore.currentScenic.id)
  const updateId = updateData?.id ? String(updateData.id) : null
  
  if (operation === 'update' && updateId === currentId) {
    console.log('ScenicDetail received update:', updateData)
    
    // 显示通知
    ElNotification({
      title: '信息更新',
      message: '景点信息已实时更新',
      type: 'success',
      position: 'bottom-right',
      duration: 3000
    })
    
    // 更新景点信息
    if (scenicStore.currentScenic) {
      // 更新评分和评论数量
      if (updateData.rating !== undefined) {
        scenicStore.currentScenic.rating = updateData.rating
      }
      if (updateData.reviewCount !== undefined) {
        scenicStore.currentScenic.reviewCount = updateData.reviewCount
      }
      // 更新其他字段
      if (updateData.shopName) {
        scenicStore.currentScenic.name = updateData.shopName
      }
      if (updateData.description !== undefined) {
        scenicStore.currentScenic.description = updateData.description
      }
      if (updateData.shopImages) {
        scenicStore.currentScenic.images = Array.isArray(updateData.shopImages) 
          ? updateData.shopImages.join(',') 
          : updateData.shopImages
      }
      if (updateData.address !== undefined) {
        scenicStore.currentScenic.address = updateData.address
      }
      if (updateData.phone !== undefined) {
        scenicStore.currentScenic.phone = updateData.phone
      }
      if (updateData.openTime !== undefined) {
        scenicStore.currentScenic.openTime = updateData.openTime
      }
      if (updateData.startPrice !== undefined) {
        scenicStore.currentScenic.price = updateData.startPrice
      }
      // 更新活动列表
      if (updateData.activities) {
        scenicStore.currentScenic.activities = updateData.activities
      }
    }
    
    // 如果评分更新，重新加载评论列表
    if (updateData.rating !== undefined || updateData.reviewCount !== undefined) {
      nextTick(() => {
        loadReviews()
      })
    }
  }
}

// 收到 review 事件（新评论/商家回复）时刷新评论区
const handleReviewRealtime = () => {
  nextTick(() => loadReviews())
}

onMounted(async () => {
  const scenicId = route.params.id
  try {
    await scenicStore.fetchScenicDetail(scenicId)
    await Promise.all([loadReviews(), loadScenicActivities()])
    // 初始化收藏状态
    if (userStore.isLoggedIn && scenicStore.currentScenic?.id) {
      try {
        const res = await checkFavorite({
          userId: userStore.user.id,
          targetId: scenicStore.currentScenic.id,
          targetType: FAVORITE_TYPE.SCENIC
        })
        // 后端返回 { success, data: boolean }
        isFavorited.value = !!(res.data ?? res)
      } catch (e) {
        // 忽略初始化失败
      }
    }
    
    // 连接WebSocket并订阅商家更新
    if (!realtimeSync.isConnected) {
      realtimeSync.connect()
    }
    realtimeSync.subscribe('merchant', handleRealtimeUpdate)
    realtimeSync.subscribe('review', handleReviewRealtime)
  } catch (error) {
    console.error('加载景点详情失败:', error)
  }
})

onUnmounted(() => {
  realtimeSync.unsubscribe('merchant', handleRealtimeUpdate)
  realtimeSync.unsubscribe('review', handleReviewRealtime)
})

// 监听路由参数变化，按需重新加载
watch(
  () => route.params.id,
  async (newId, oldId) => {
    if (newId && newId !== oldId) {
      try {
        await scenicStore.fetchScenicDetail(newId)
        await Promise.all([loadReviews(), loadScenicActivities()])
        // 切换景点时刷新收藏状态
        if (userStore.isLoggedIn && scenicStore.currentScenic?.id) {
          try {
            const res = await checkFavorite({
              userId: userStore.user.id,
              targetId: scenicStore.currentScenic.id,
              targetType: FAVORITE_TYPE.SCENIC
            })
            isFavorited.value = !!(res.data ?? res)
          } catch (e) {}
        } else {
          isFavorited.value = false
        }
      } catch (error) {
        console.error('切换景点时加载失败:', error)
      }
    }
  }
)
</script>

<style scoped>
/* 推荐模块 */
.section { margin: 24px 0; }
.section-title {
  margin: 0 0 12px 0;
  font-size: 20px;
  font-weight: 800;
  color: #111827;
}
.rec-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
</style>

<style scoped>
.hotel-two-col {
  display: grid;
  grid-template-columns: 7fr 3fr; /* 70% / 30% */
  gap: 24px;
  margin-bottom: 48px;
}
.hotel-card {
  background: #fff;
  border-radius: 14px;
  border: 1px solid #eef1f6;
  padding: 16px;
  box-shadow: 0 6px 18px rgba(0,0,0,0.06);
}
.card-title {
  margin: 0 0 10px 0;
  font-size: 18px;
  font-weight: 800;
  color: #1f2937;
}
.desc {
  color: #4b5563;
  line-height: 1.7;
  font-size: 14px;
  max-height: none;
  overflow: hidden;
  transition: max-height .25s ease;
}
.desc.collapsed {
  display: -webkit-box;
  line-clamp: 3;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}
.link-more { margin-top: 6px; }

.scenic-detail {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background:
    radial-gradient(1200px 600px at 10% -10%, rgba(64,158,255,0.06), transparent 60%),
    radial-gradient(1000px 500px at 90% -10%, rgba(103,194,58,0.06), transparent 60%),
    #ffffff;
}

.container {
  flex: 1;
  max-width: 1280px;
  margin: 0 auto;
  padding: 20px 24px 48px;
}

.back-button-container {
  margin-bottom: 12px;
  position: sticky;
  top: 84px;
  z-index: 5;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 48px;
  animation: fadeUp 400ms ease-out both;
}

/* 新增：景点内容布局 - 左侧主内容 + 右侧边栏 */
.scenic-content-layout {
  display: grid;
  grid-template-columns: 7fr 3fr;
  gap: 20px;
  align-items: flex-start;
}

.scenic-main-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.scenic-sidebar-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: sticky;
  top: 72px;
  max-height: calc(100vh - 88px);
  overflow-y: auto;
  overflow-x: hidden;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.scenic-sidebar-content::-webkit-scrollbar {
  display: none;
}

/* 响应式：平板和手机端改为单栏 */
@media (max-width: 1024px) {
  .scenic-content-layout {
    grid-template-columns: 1fr;
  }
  
  .scenic-sidebar-content {
    position: static;
    max-height: none;
    overflow-y: visible;
  }
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 32px;
  margin-bottom: 72px;
  animation: fadeUp 400ms ease-out both;
}

.scenic-gallery {
  flex: 1 1 56%;
  min-width: 540px;
  margin-bottom: 24px;
  position: relative;
}

.scenic-gallery img {
  width: 100%;
  height: 460px;
  object-fit: cover;
  border-radius: 16px;
  background: #f5f7fa;
  box-shadow:
    0 10px 30px rgba(0,0,0,0.06),
    0 2px 10px rgba(0,0,0,0.04);
}

.carousel-image-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  cursor: pointer;
  overflow: hidden;
}

.gallery-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 320ms cubic-bezier(.2,.8,.2,1), box-shadow 320ms ease;
  border-radius: 16px;
}

.gallery-image:hover {
  transform: scale(1.012);
  box-shadow:
    0 18px 50px rgba(0,0,0,0.10),
    0 8px 18px rgba(0,0,0,0.06);
}

.carousel-image-wrapper::after {
  content: "";
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 45%;
  border-bottom-left-radius: 16px;
  border-bottom-right-radius: 16px;
  background: linear-gradient(180deg, rgba(0,0,0,0) 0%, rgba(0,0,0,0.28) 60%, rgba(0,0,0,0.45) 100%);
  pointer-events: none;
}

.image-counter {
  position: absolute;
  bottom: 14px;
  right: 14px;
  backdrop-filter: saturate(1.2) blur(6px);
  background: rgba(0, 0, 0, 0.45);
  color: white;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
  letter-spacing: .2px;
}

.image-thumbnails {
  display: flex;
  gap: 10px;
  margin-top: 14px;
  overflow-x: auto;
  padding: 8px 2px;
}

.thumbnail-item {
  flex-shrink: 0;
  width: 90px;
  height: 68px;
  border: 2px solid transparent;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 240ms ease, border-color 240ms ease, box-shadow 240ms ease;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.thumbnail-item:hover {
  border-color: #409eff;
  transform: translateY(-2px) scale(1.03);
  box-shadow: 0 8px 18px rgba(64,158,255,0.18);
}

.thumbnail-item.active {
  border-color: #409eff;
  box-shadow: 0 0 0 4px rgba(64,158,255,0.12), 0 6px 16px rgba(64,158,255,0.18);
}

.thumbnail-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* Carousel indicators refinement */
:deep(.el-carousel__indicators--outside) {
  margin-top: 10px;
}
:deep(.el-carousel__indicator) button {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: #d7e3ff;
  opacity: 1;
  transition: width 220ms ease, background 220ms ease;
}
:deep(.el-carousel__indicator.is-active) button {
  width: 20px;
  background: #409eff;
}

.hero-overlay {
  position: absolute;
  left: 20px;
  right: 20px;
  bottom: 20px;
  z-index: 2;
  pointer-events: none;
}

.hero-meta {
  display: flex;
  flex-direction: column;
  gap: 10px;
  color: #ffffff;
  text-shadow: 0 1px 2px rgba(0,0,0,0.3);
}

.hero-title {
  margin: 0;
  font-size: 34px;
  line-height: 1.15;
  font-weight: 800;
  letter-spacing: -0.2px;
}

.hero-sub {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.hero-rating {
  display: flex;
  align-items: center;
  gap: 8px;
}

.hero-rating-text {
  color: rgba(255,255,255,0.9);
  font-weight: 600;
}

.hero-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.hero-current {
  color: #ffebb5;
  font-weight: 800;
  font-size: 28px;
}

.hero-original {
  color: rgba(255,255,255,0.7);
  text-decoration: line-through;
  font-size: 14px;
}

.hero-free {
  color: #c9f6cf;
  font-weight: 800;
  font-size: 22px;
}

.image-preview-container {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 500px;
}

.preview-image {
  max-width: 100%;
  max-height: 70vh;
  object-fit: contain;
}

.preview-nav-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  z-index: 10;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(0,0,0,0.08);
  box-shadow: 0 6px 16px rgba(0,0,0,0.12);
}

.preview-nav-btn.prev {
  left: 20px;
}

.preview-nav-btn.next {
  right: 20px;
}

.preview-counter {
  text-align: center;
  margin-top: 16px;
  color: #606266;
  font-size: 14px;
}

.scenic-info {
  display: flex;
  gap: 28px;
  flex: 1 1 45%;
  min-width: 480px;
}

.info-main {
  flex: 2;
}

.scenic-name {
  font-size: 36px;
  line-height: 1.15;
  letter-spacing: -0.2px;
  color: #202124;
  margin-bottom: 14px;
  font-weight: 700;
}

.scenic-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 26px;
  padding-bottom: 18px;
  border-bottom: 1px solid #eef1f6;
}

.rating-text {
  margin-left: 10px;
  color: #909399;
  font-weight: 500;
}

.price-value {
  font-size: 26px;
  font-weight: 700;
}

.current-price {
  color: #e6a23c;
  font-size: 26px;
}

.original-price {
  color: #909399;
  text-decoration: line-through;
  font-size: 16px;
  margin-left: 8px;
}

.price-value.free {
  color: #67c23a;
  font-size: 26px;
  font-weight: 700;
}

/* 商家活动样式 */
.scenic-activities {
  margin-top: 22px;
  padding-top: 22px;
  border-top: 1px solid #f3f5f8;
}

.scenic-activities h3 {
  font-size: 18px;
  color: #303133;
  margin-bottom: 14px;
  font-weight: 700;
  letter-spacing: .2px;
}

.activities-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.activity-item {
  display: flex;
  gap: 0;
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

/* 左侧彩色竖条 */
.activity-item::before {
  content: '';
  display: block;
  width: 4px;
  flex-shrink: 0;
  background: linear-gradient(180deg, #e6a23c, #f5a623);
  border-radius: 0;
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
  font-size: 15px;
  color: #1a1a1a;
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

.scenic-description,
.scenic-details,
.scenic-features,
.scenic-highlights,
.scenic-facilities,
.scenic-tips,
.scenic-map {
  margin-bottom: 26px;
}

.features-grid,
.facilities-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
  margin-top: 12px;
}

.feature-item,
.facility-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: #f8fafc;
  border-radius: 10px;
  color: #4b5563;
  border: 1px solid #eef2f7;
}

.highlights-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 12px;
}

.highlight-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #67c23a;
  font-weight: 500;
}

.tips-content {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 14px;
  background: linear-gradient(180deg, rgba(64,158,255,0.08), rgba(64,158,255,0.06));
  border-radius: 10px;
  color: #1d6adf;
  margin-top: 12px;
  border: 1px solid rgba(64,158,255,0.18);
}

.map-container {
  margin-top: 12px;
}

.map-placeholder {
  height: 200px;
  background: #f5f7fa;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #909399;
  margin-bottom: 12px;
  border: 1px dashed #e5e7eb;
}

.map-actions {
  display: flex;
  gap: 8px;
}

.scenic-description h3,
.scenic-tags h3 {
  margin-bottom: 12px;
  color: #111827;
  font-weight: 700;
}

.scenic-description p {
  line-height: 1.6;
  color: #4b5563;
  font-size: 15px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  color: #4b5563;
}

.tag-item {
  margin-right: 8px;
  margin-bottom: 8px;
}

.tag-item :deep(.el-tag) {
  border-radius: 999px !important;
  background: #f2f6ff !important;
  border-color: #e0eaff !important;
  color: #3a6fd9 !important;
  font-weight: 600 !important;
}

.info-sidebar {
  flex: 1;
  width: 100% !important;
  box-sizing: border-box !important;
  position: sticky;
  top: 108px;
}

.contact-info p {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  color: #4b5563;
}

.review-section {
  animation: fadeUp 420ms ease-out both;
}

/* Micro animation */
@keyframes fadeUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 768px) {
  .detail-content {
    flex-direction: column;
    gap: 18px;
  }
  
  .scenic-info {
    flex-direction: column;
    min-width: 100%;
  }
  
  .scenic-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .scenic-gallery {
    min-width: 100%;
  }
  
  .hero-title {
    font-size: 26px;
  }
  .hero-current {
    font-size: 22px;
  }
}

/* ============================================================
   产品级操作按钮组 - 新样式
   ============================================================ */

.prd-action-card {
  background: #ffffff;
  border-radius: 20px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow:
    0 1px 3px rgba(0, 0, 0, 0.06),
    0 8px 24px rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* 主按钮：加入行程 */
.prd-btn--primary {
  width: 100%;
  height: 52px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: #ffffff;
  border: none;
  border-radius: 14px;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 0.3px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  box-shadow:
    0 4px 14px rgba(59, 130, 246, 0.35),
    0 1px 3px rgba(59, 130, 246, 0.2);
  transition: transform 180ms ease, box-shadow 180ms ease, filter 180ms ease;
  position: relative;
  overflow: hidden;
}

.prd-btn--primary::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(255,255,255,0.15) 0%, transparent 60%);
  border-radius: inherit;
  pointer-events: none;
}

.prd-btn--primary:hover {
  transform: translateY(-2px);
  box-shadow:
    0 8px 22px rgba(59, 130, 246, 0.42),
    0 2px 6px rgba(59, 130, 246, 0.25);
  filter: brightness(1.04);
}

.prd-btn--primary:active {
  transform: translateY(0);
  box-shadow:
    0 2px 8px rgba(59, 130, 246, 0.3);
}

/* 次操作行 */
.prd-btn-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

/* 幽灵按钮基础 */
.prd-btn--ghost {
  height: 46px;
  background: #f8f9fa;
  color: #374151;
  border: 1.5px solid #e5e7eb;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: background 160ms ease, border-color 160ms ease, color 160ms ease, transform 160ms ease, box-shadow 160ms ease;
}

.prd-btn--ghost:hover {
  background: #f0f2f5;
  border-color: #d1d5db;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.prd-btn--ghost:active {
  transform: translateY(0);
  background: #e9ecef;
}

/* 收藏激活态 */
.prd-btn--favorited {
  background: #fff1f2;
  border-color: #fca5a5;
  color: #ef4444;
}

.prd-btn--favorited:hover {
  background: #ffe4e6;
  border-color: #f87171;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.15);
}

/* 按钮图标 */
.prd-btn__icon {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.prd-btn__icon svg {
  width: 100%;
  height: 100%;
}

.prd-btn--primary .prd-btn__icon {
  width: 22px;
  height: 22px;
}

/* 禁用态 */
.prd-btn--ghost:disabled {
  opacity: 0.55;
  cursor: not-allowed;
  transform: none;
}

/* ============================================================
   新两栏布局
   ============================================================ */

.scenic-two-col {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
  align-items: flex-start;
}

.scenic-left-col {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.scenic-right-col {
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: sticky;
  top: 72px;
  max-height: calc(100vh - 88px);
  overflow-y: auto;
  overflow-x: hidden;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.scenic-right-col::-webkit-scrollbar {
  display: none;
}

/* 信息卡 */
.scenic-info-card {
  background: #ffffff;
  border-radius: 18px;
  padding: 20px;
  box-shadow:
    0 1px 3px rgba(0, 0, 0, 0.06),
    0 8px 24px rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.sic-title-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  flex-wrap: wrap;
}

.sic-title {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  color: #111827;
  line-height: 1.3;
  flex: 1;
}

.sic-tag {
  font-size: 12px;
  font-weight: 600;
  border-radius: 999px;
  flex-shrink: 0;
}

.sic-rating-row {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.sic-score-badge {
  background: #ef4444;
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  padding: 3px 10px;
  border-radius: 8px;
  flex-shrink: 0;
}

.sic-score-text {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.sic-review-count {
  font-size: 13px;
  color: #3b82f6;
  text-decoration: underline;
  text-underline-offset: 2px;
}

.sic-arrow {
  margin-left: auto;
  color: #9ca3af;
  font-size: 14px;
}

.sic-divider {
  display: none; /* 已用 border-bottom 代替 */
}

.sic-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  font-size: 13px;
  color: #374151;
  line-height: 1.5;
}

.sic-icon {
  font-size: 15px;
  color: #6b7280;
  flex-shrink: 0;
  margin-top: 1px;
}

.sic-value {
  flex: 1;
  min-width: 0;
}

.sic-time-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.sic-open-badge {
  display: inline-block;
  padding: 1px 8px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
  background: #d1fae5;
  color: #065f46;
  flex-shrink: 0;
}
.sic-open-badge--closed {
  background: #fee2e2;
  color: #991b1b;
}

/* 响应式：小屏改为单栏 */
@media (max-width: 1024px) {
  .scenic-two-col {
    grid-template-columns: 1fr;
  }

  .scenic-right-col {
    position: static;
    max-height: none;
    overflow-y: visible;
    /* 小屏时右栏移到左栏上方 */
    order: -1;
  }
}
</style>
