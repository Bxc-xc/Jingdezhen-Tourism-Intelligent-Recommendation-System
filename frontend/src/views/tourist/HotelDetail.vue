<template>
  <div class="hotel-detail">
    <Header />

    <div class="container" v-loading="loading">
      <div class="back-button-container">
        <el-button @click="goBack" :icon="ArrowLeft" circle />
      </div>

      <el-empty
        v-if="!loading && !hotel"
        :description="$t('detail.notFound')"
      />

      <div v-if="hotel">
        <!-- 头部区域：紧凑信息行 -->
        <div class="header-section compact">
          <div class="info-wrapper">
            <div class="title-row">
              <h1 class="hotel-title">{{ hotel.shopName }}</h1>
              <div class="stars" v-if="hotel.rating">
                <el-rate v-model="hotel.rating" disabled :allow-half="true" />
              </div>
              <span class="review-count" v-if="hotel.reviewCount !== undefined">
                {{ hotel.reviewCount }} {{ $t('card.reviews') }}
              </span>
            </div>
            <div class="sub-row">
              <el-icon><Location /></el-icon>
              <span class="addr">{{ hotel.address || $t('detail.noAddress') }}</span>
              <el-button link type="primary" class="map-link" @click="openMap">显示地图</el-button>
            </div>
          </div>
          <div class="price-cta">
            <div class="price" v-if="minPriceDisplay">
              <span class="yen">¥</span>{{ minPriceDisplay }}<span class="suffix">起</span>
            </div>
            <el-button type="primary" size="large" class="cta" @click="handleBook">选择房间</el-button>
          </div>
        </div>

        <!-- 内容区域：左右分栏 -->
        <div class="content-section">
          <!-- 左侧：图片拼图 + 设施 + 简介 + 活动 -->
          <div class="left-column">
            <!-- 图片拼图布局（左大右小） -->
            <HotelGallery
              class="detail-card"
              :images="galleryImages"
              :favorited="isFavorited"
              @preview="openGalleryPreview"
              @view-all="openGalleryPreview(0)"
              @favorite="toggleFavorite"
            />

            <!-- 酒店亮点 -->
            <el-card class="detail-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <span class="title">酒店亮点</span>
                </div>
              </template>
              <div class="highlights">
                <el-tag
                  v-for="(h, i) in highlights"
                  :key="i"
                  effect="light"
                  type="success"
                  class="mr-8"
                >{{ h }}</el-tag>
              </div>
            </el-card>

            <!-- 设施模块 -->
            <HotelFacilities class="detail-card" :facilities="facilitiesList" />

            <el-card class="detail-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <span class="title">酒店介绍</span>
                </div>
              </template>
              <div class="rich-text-content">
                {{ introCopy }}
              </div>
            </el-card>

            <!-- 商家活动 -->
            <el-card v-if="activities.filter(a => isActivityActive(a)).length > 0" class="detail-card activity-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <span class="title">{{ $t('detail.merchantActivities') }}</span>
                  <el-tag v-if="activities.filter(a => isActivityActive(a)).length > 0" type="danger" effect="plain" size="small">{{ $t('detail.activeCount', { count: activities.filter(a => isActivityActive(a)).length }) }}</el-tag>
                </div>
              </template>
              <div class="activities-list">
                <div
                  v-for="activity in activities.filter(a => isActivityActive(a))"
                  :key="activity.id"
                  class="activity-item"
                >
                  <div class="activity-main">
                    <div class="activity-header">
                      <h4>{{ activity.title }}</h4>
                      <el-tag v-if="isActivityActive(activity)" type="success" size="small" effect="dark">
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
                    </div>
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
            
            <!-- 评论区 -->
            <div id="user-reviews" class="review-section">
              <div class="section-header">
                <h2>{{ $t('detail.userReviews') }}</h2>
                <div class="review-stats">
                  <span class="score">{{ hotel.rating?.toFixed(1) || '0.0' }}</span>
                  <span class="total">/ 5.0</span>
                </div>
              </div>
              
              <div class="review-form" v-if="userStore.isLoggedIn">
                <CompositeReviewInput
                  v-model:rating="reviewForm.rating"
                  v-model:content="reviewForm.content"
                  v-model:images="reviewForm.images"
                  :placeholder="$t('detail.reviewPlaceholder')"
                  :upload-action="`${API_BASE_URL}/upload`"
                  :upload-headers="uploadHeaders"
                  @submit="handleSubmitReview"
                />
              </div>
              <div v-else class="login-prompt">
                <el-alert
                  :title="$t('detail.loginToReview')"
                  type="info"
                  :closable="false"
                  show-icon
                  center
                >
                  <template #default>
                    <el-button type="primary" link @click="router.push('/login')">{{ $t('common.login') }}</el-button>
                  </template>
                </el-alert>
              </div>
              
              <div class="review-list">
                <div v-for="review in (reviews.length ? reviews : sampleReviews)" :key="review.id" class="review-item">
                  <div class="review-header">
                    <el-avatar
                      :size="40"
                      :src="review.user?.avatar ? normalizeUrl(review.user.avatar) : null"
                    >{{ review.user?.username?.charAt(0) || 'U' }}</el-avatar>
                    <div class="review-info">
                      <div class="review-user-row">
                        <span class="review-user">{{ review.user?.username || $t('detail.anonymous') }}</span>
                        <el-rate v-model="review.rating" disabled size="small" />
                      </div>
                      <span class="review-time">{{ formatTime(review.createTime) }}</span>
                    </div>
                  </div>
                  <div class="review-content">{{ review.content }}</div>
                  <!-- 评价图片展示 -->
                  <div v-if="Array.isArray(review.images) && review.images.length" class="review-images">
                    <el-image
                      v-for="(img, idx) in review.images"
                      :key="idx"
                      :src="normalizeUrl(img)"
                      :preview-src-list="review.images.map(normalizeUrl)"
                      :initial-index="idx"
                      fit="cover"
                      class="review-image-item"
                    />
                  </div>
                  
                  <div class="review-footer">
                    <div class="review-actions" v-if="userStore.isLoggedIn">
                      <el-button size="small" link @click="startReplyToReview(review)">
                        <el-icon><ChatDotRound /></el-icon> {{ $t('detail.reply') }}
                      </el-button>
                    </div>
                  </div>

                  <!-- 回复输入框 -->
                  <div v-if="replyingReviewId === review.id && !replyingReplyId" class="reply-input-area">
                    <el-input
                      v-model="replyContent"
                      type="textarea"
                      :rows="2"
                      maxlength="200"
                      show-word-limit
                      :placeholder="$t('detail.reply') + '...'"
                    />
                    <div class="reply-actions">
                      <el-button size="small" @click="cancelReply">{{ $t('common.cancel') }}</el-button>
                      <el-button size="small" type="primary" @click="submitReplyToReview(review)">{{ $t('common.submit') }}</el-button>
                    </div>
                  </div>
                  
                  <!-- 回复列表 -->
                  <div v-if="reviewReplies[review.id] && reviewReplies[review.id].length > 0" class="replies-container">
                    <div v-for="reply in reviewReplies[review.id]" :key="reply.id" class="reply-item">
                      <div class="reply-content-wrapper">
                        <div class="reply-header-row">
                          <el-avatar
                            :size="28"
                            :src="reply.merchant?.avatar ? normalizeUrl(reply.merchant.avatar) : (reply.user?.avatar ? normalizeUrl(reply.user.avatar) : null)"
                          >{{ getReplierName(reply)?.charAt(0) || 'U' }}</el-avatar>
                          <span class="replier-name" :class="{ 'is-merchant': reply.merchant }">
                            {{ getReplierName(reply) }}
                            <el-tag v-if="reply.merchant" type="warning" size="small" effect="dark">{{ $t('header.role.merchant') }}</el-tag>
                          </span>
                          <span class="reply-time">{{ formatTime(reply.createTime) }}</span>
                        </div>
                        <div class="reply-text">{{ reply.content }}</div>
                        <!-- 回复图片展示（如后端返回） -->
                        <div v-if="Array.isArray(reply.images) && reply.images.length" class="reply-images">
                          <el-image
                            v-for="(img, ridx) in reply.images"
                            :key="ridx"
                            :src="normalizeUrl(img)"
                            :preview-src-list="reply.images.map(normalizeUrl)"
                            :initial-index="ridx"
                            fit="cover"
                            class="reply-image-item"
                          />
                        </div>
                        <div class="reply-actions-inline" v-if="userStore.isLoggedIn">
                          <el-button size="small" link @click="startReplyToReply(review, reply)">{{ $t('detail.reply') }}</el-button>
                        </div>
                      </div>

                      <!-- 嵌套回复输入框 -->
                      <div v-if="replyingReplyId === reply.id" class="reply-input-area nested">
                        <div class="reply-target">{{ $t('detail.replyTo') }}{{ getReplierName(reply) }}：</div>
                        <el-input
                          v-model="replyContent"
                          type="textarea"
                          :rows="2"
                          maxlength="200"
                          show-word-limit
                          :placeholder="$t('detail.reply') + '...'"
                        />
                        <div class="reply-actions">
                          <el-button size="small" @click="cancelReply">{{ $t('common.cancel') }}</el-button>
                          <el-button size="small" type="primary" @click="submitReplyToReply(review, reply)">{{ $t('common.submit') }}</el-button>
                        </div>
                      </div>
                      
                      <!-- 子回复（二级嵌套） -->
                      <div v-if="reply.childReplies && reply.childReplies.length > 0" class="child-replies">
                        <div v-for="childReply in reply.childReplies" :key="childReply.id" class="child-reply-item">
                          <div class="reply-header-row">
                            <el-avatar
                              :size="26"
                              :src="childReply.merchant?.avatar ? normalizeUrl(childReply.merchant.avatar) : (childReply.user?.avatar ? normalizeUrl(childReply.user.avatar) : null)"
                            >{{ getReplierName(childReply)?.charAt(0) || 'U' }}</el-avatar>
                            <span class="replier-name" :class="{ 'is-merchant': childReply.merchant }">
                              {{ getReplierName(childReply) }}
                              <el-tag v-if="childReply.merchant" type="warning" size="small" effect="dark">{{ $t('header.role.merchant') }}</el-tag>
                            </span>
                            <span class="reply-target-text">{{ $t('detail.reply') }}</span>
                            <span class="replier-name">{{ getReplierName(reply) }}</span>
                            <span class="reply-time">{{ formatTime(childReply.createTime) }}</span>
                          </div>
                          <div class="reply-text">{{ childReply.content }}</div>
                          <!-- 子回复图片展示（如后端返回） -->
                          <div v-if="Array.isArray(childReply.images) && childReply.images.length" class="reply-images">
                            <el-image
                              v-for="(img, cidx) in childReply.images"
                              :key="cidx"
                              :src="normalizeUrl(img)"
                              :preview-src-list="childReply.images.map(normalizeUrl)"
                              :initial-index="cidx"
                              fit="cover"
                              class="reply-image-item"
                            />
                          </div>
                          <div class="reply-actions-inline" v-if="userStore.isLoggedIn">
                            <el-button size="small" link @click="startReplyToReply(review, childReply)">{{ $t('detail.reply') }}</el-button>
                          </div>

                          <div v-if="replyingReplyId === childReply.id" class="reply-input-area nested">
                            <div class="reply-target">{{ $t('detail.replyTo') }}{{ getReplierName(childReply) }}：</div>
                            <el-input
                              v-model="replyContent"
                              type="textarea"
                              :rows="2"
                              maxlength="200"
                              show-word-limit
                              :placeholder="$t('detail.reply') + '...'"
                            />
                            <div class="reply-actions">
                              <el-button size="small" @click="cancelReply">{{ $t('common.cancel') }}</el-button>
                              <el-button size="small" type="primary" @click="submitReplyToReply(review, childReply)">{{ $t('common.submit') }}</el-button>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <el-empty v-if="!loading && reviews.length === 0" :description="$t('detail.noReviews')" />
              </div>
            </div>
          </div>

          <!-- 右侧：侧边栏信息（评分+附近） -->
          <div class="right-column">
            <HotelSidebar
              class="detail-card sidebar-card"
              :rating="hotel?.rating || 0"
              :review-count="hotel?.reviewCount || 0"
              :summary="reviewSummary"
              :nearby="nearbyListDisplay"
              @open-map="openMap"
              @choose-room="handleBook"
            />

            <!-- 评价标签 -->
            <el-card class="detail-card sidebar-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <span class="title">热门评价标签</span>
                </div>
              </template>
              <div class="tag-grid">
                <el-tag
                  v-for="(tag, idx) in reviewTags"
                  :key="idx"
                  type="info"
                  effect="plain"
                  class="tag-chip"
                >{{ tag }}</el-tag>
              </div>
            </el-card>

            <el-card class="detail-card sidebar-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <span class="title">{{ $t('detail.bookingNotes') }}</span>
                </div>
              </template>
              <ul class="tips-list">
                <li>{{ $t('detail.bookingTip1') }}</li>
                <li>{{ $t('detail.bookingTip2') }}</li>
                <li>{{ $t('detail.bookingTip3') }}</li>
              </ul>
            </el-card>
          </div>
        </div>
      </div>
    </div>

    <Footer />

    <el-dialog
      v-model="bookingDialogVisible"
      :title="$t('detail.bookNow')"
      width="640px"
      :close-on-click-modal="false"
    >
      <el-form :model="bookingForm" label-width="80px">
        <!-- 房型卡片选择 -->
        <el-form-item :label="$t('detail.roomType')" required>
          <div v-if="roomTypes.length === 0" class="form-tip">{{ $t('detail.noRoomType') }}</div>
          <div v-else class="room-type-list">
            <div
              v-for="rt in roomTypes"
              :key="rt.id"
              class="room-type-card"
              :class="{ selected: bookingForm.roomTypeId === rt.id }"
              @click="bookingForm.roomTypeId = rt.id"
            >
              <el-image
                v-if="rt.images"
                :src="rt.images"
                fit="cover"
                class="room-card-img"
              >
                <template #error>
                  <div class="room-card-img-placeholder"><el-icon><Picture /></el-icon></div>
                </template>
              </el-image>
              <div v-else class="room-card-img-placeholder"><el-icon><Picture /></el-icon></div>
              <div class="room-card-info">
                <div class="room-card-name">{{ rt.name }}</div>
                <div class="room-card-meta">
                  <span v-if="rt.bedType">{{ rt.bedType }}</span>
                  <span v-if="rt.area">· {{ rt.area }}</span>
                  <el-tag v-if="rt.breakfast" type="success" size="small" style="margin-left:4px">含早</el-tag>
                  <el-tag v-else type="info" size="small" style="margin-left:4px">不含早</el-tag>
                </div>
                <div class="room-card-stock" v-if="rt.stock != null">剩余 {{ rt.stock }} 间</div>
                <div class="room-card-desc" v-if="rt.description">{{ rt.description }}</div>
              </div>
              <div class="room-card-price">
                <span class="price-num">¥{{ rt.price }}</span>
                <span class="price-unit">/晚</span>
              </div>
            </div>
          </div>
        </el-form-item>

        <el-form-item :label="$t('detail.checkInDate')" required>
          <el-date-picker
            v-model="bookingForm.dateRange"
            type="daterange"
            :range-separator="$t('detail.to')"
            :start-placeholder="$t('detail.checkInDate')"
            :end-placeholder="$t('detail.checkOutDate')"
            :disabled-date="disabledDate"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item :label="$t('detail.roomQuantity')" required>
          <el-input-number v-model="bookingForm.quantity" :min="1" :max="10" />
        </el-form-item>
        <el-form-item :label="$t('detail.contactName')" required>
          <el-input v-model="bookingForm.contactName" :placeholder="$t('detail.enterContactName')" />
        </el-form-item>
        <el-form-item label="手机号" required>
          <el-input v-model="bookingForm.contactPhone" placeholder="请输入联系手机号" maxlength="11" />
        </el-form-item>
        <el-form-item :label="$t('detail.note')">
          <el-input v-model="bookingForm.note" type="textarea" :placeholder="$t('detail.enterNote')" />
        </el-form-item>
        <div class="total-price" v-if="totalPrice > 0">
          {{ $t('detail.estimatedTotal') }}：<span class="price">￥{{ totalPrice.toFixed(2) }}</span>
        </div>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="bookingDialogVisible = false">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" @click="confirmBooking" :loading="bookingLoading">
            {{ $t('common.confirm') }}
          </el-button>
        </span>
      </template>
    </el-dialog>

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
import { ArrowLeft, ChatDotRound, Clock, Picture, Location, Phone, Star, Share, MapLocation, Calendar, Close } from '@element-plus/icons-vue'
import { ElMessage, ElNotification } from 'element-plus'
import Header from '../../components/Layout/Header.vue'
import Footer from '../../components/Layout/Footer.vue'
import CompositeReviewInput from '../../components/review/CompositeReviewInput.vue'
import AddToPlanDialog from '../../components/AddToPlanDialog.vue'
import HotelGallery from '../../components/hotel/HotelGallery.vue'
import HotelFacilities from '../../components/hotel/HotelFacilities.vue'
import HotelSidebar from '../../components/hotel/HotelSidebar.vue'
import { getMerchantById } from '../../api/merchant'
import { createMerchantReview, getMerchantReviewList, replyReview } from '../../api/review'
import { getMerchantActivities } from '../../api/merchantActivity'
import { getRepliesByReviewId, createUserReply, createUserReplyToReply } from '../../api/reply'
import { useUserStore } from '../../stores/user'
import { getRoomTypes } from '../../api/roomType'
import { createOrder } from '../../api/order'
import realtimeSync from '../../utils/websocket'
import { normalizeUrl, FALLBACK_IMAGE } from '../../utils/image'
import { addFavorite, removeFavorite, checkFavorite } from '../../api/favoriteGateway'
import { FAVORITE_TYPE } from '../../utils/favoriteType'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const userStore = useUserStore()

const loading = ref(false)
const hotel = ref(null)
const merchantId = ref(null)
const reviews = ref([])
const reloadingReviews = ref(false)
const activities = ref([])
const replyingReviewId = ref(null)
const replyingReplyId = ref(null)
const replyContent = ref('')
const reviewReplies = ref({}) // 存储每个评论的回复列表 { reviewId: [replies] }
const showAddToPlanDialog = ref(false)
const itemToAdd = ref({})
const clickPosition = ref(null)

const roomTypes = ref([])
const bookingDialogVisible = ref(false)
const bookingLoading = ref(false)
const bookingForm = ref({
    roomTypeId: null,
    dateRange: [],
    quantity: 1,
    contactName: '',
    contactPhone: '',
    note: ''
})

const totalPrice = computed(() => {
    if (!bookingForm.value.dateRange || bookingForm.value.dateRange.length < 2) return 0
    // 如果没有房型，使用默认起步价或其他逻辑，这里假设必须有房型
    // 或者如果后端支持无房型预约（generic reservation），可以使用 merchant.startPrice
    if (roomTypes.value.length === 0) {
         // Fallback to startPrice if available, but calculation of nights * price might need adjustment
         return 0 
    }
    const roomType = roomTypes.value.find(rt => rt.id === bookingForm.value.roomTypeId)
    if (!roomType) return 0
    
    const start = new Date(bookingForm.value.dateRange[0])
    const end = new Date(bookingForm.value.dateRange[1])
    const nights = (end - start) / (1000 * 60 * 60 * 24)
    
    return roomType.price * nights * bookingForm.value.quantity
})

const reviewForm = ref({
  rating: 5,
  content: '',
  images: []
})
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

// 计算酒店图片列表：
// - 优先使用商家的多张店铺图片（shopImages）
// - 如果有 avatar，则作为第一张封面图片
// 计算酒店图片列表：
// - 优先使用后端返回的 images 数组（enrichMerchantWithRating 已处理）
// - 兜底使用 cover/avatar
// - 最后使用占位图
const imageList = computed(() => {
  const h = hotel.value
  if (!h) {
    return [FALLBACK_IMAGE]
  }

  const list = []
  
  // 1. 优先使用后端返回的 images 数组（enrichMerchantWithRating 已处理好）
  if (Array.isArray(h.images) && h.images.length > 0) {
    h.images.forEach(url => {
      const normalized = normalizeUrl(url)
      if (normalized && normalized !== FALLBACK_IMAGE && !list.includes(normalized)) {
        list.push(normalized)
      }
    })
  }
  
  // 2. 如果 images 数组为空，尝试使用 cover 或 avatar 作为兜底
  if (list.length === 0) {
    const cover = h.cover || h.avatar
    if (cover) {
      const normalized = normalizeUrl(cover)
      if (normalized && normalized !== FALLBACK_IMAGE) {
        list.push(normalized)
      }
    }
  }

  // 3. 如果还是没有图片，使用占位图
  if (list.length === 0) {
    list.push(FALLBACK_IMAGE)
  }
  
  return list
})

// 酒店卡片封面：默认使用图片列表中的第一张
const coverImage = computed(() => {
  return imageList.value[0] || FALLBACK_IMAGE
})

// 拼图用图片（已经过 normalizeUrl 处理，直接使用）
const galleryImages = computed(() => {
  const imgs = imageList.value || []
  // 最多显示6张图片
  const result = imgs.slice(0, 6)
  // 如果图片不足6张，用占位图填充以保持布局完整
  while (result.length < 6) {
    result.push(FALLBACK_IMAGE)
  }
  return result
})

// 价格展示（房型最低价优先，其次使用商家起步价/price）
const minPriceDisplay = computed(() => {
  const prices = []
  if (Array.isArray(roomTypes.value) && roomTypes.value.length) {
    roomTypes.value.forEach(rt => { if (Number(rt.price) > 0) prices.push(Number(rt.price)) })
  }
  const start = Number(hotel.value?.startPrice || hotel.value?.price)
  if (Number.isFinite(start) && start > 0) prices.push(start)
  if (!prices.length) return ''
  return Math.min(...prices).toFixed(0)
})

// 设施兜底
const facilitiesList = computed(() => {
  const raw = hotel.value?.facilities
  const parsed = Array.isArray(raw)
    ? raw
    : (typeof raw === 'string' ? raw.split(',').map(s => s && s.trim()).filter(Boolean) : [])
  const base = ['免费WiFi', '停车场', '游泳池', '健身房', '餐厅', '接送服务']
  return (parsed.length ? parsed : base).slice(0, 20)
})

// 附近信息 —— 用高德周边搜索获取真实数据
const nearbyList = ref([])
const nearbyLoading = ref(false)

const AMAP_KEY = 'your_amap_key_here' // 从环境变量或配置读取

async function loadNearby() {
  if (!hotel.value?.address) return
  nearbyLoading.value = true
  try {
    // 1. 地理编码：地址 → 坐标
    const geoRes = await fetch(
      `https://restapi.amap.com/v3/geocode/geo?key=${import.meta.env.VITE_AMAP_KEY || '67aaaaa21d1edeceddaaf63fd6ad1ac4'}&address=${encodeURIComponent('景德镇' + hotel.value.address)}&city=景德镇`
    ).then(r => r.json())

    const location = geoRes?.geocodes?.[0]?.location
    if (!location) {
      console.warn('地理编码失败，地址:', hotel.value.address)
      return
    }

    // 2. 周边搜索，按类型分批查，半径20km
    const types = [
      { type: '110000', label: '景点',   radius: 5000  },
      { type: '150100', label: '火车站', radius: 20000 },
      { type: '150200', label: '机场',   radius: 50000 },
      { type: '060000', label: '餐饮',   radius: 1000  },
      { type: '060100', label: '中餐厅', radius: 1000  },
      { type: '080000', label: '购物',   radius: 2000  },
    ]

    const results = []
    const key = import.meta.env.VITE_AMAP_KEY || '67aaaaa21d1edeceddaaf63fd6ad1ac4'

    for (const t of types) {
      if (results.length >= 6) break
      try {
        const res = await fetch(
          `https://restapi.amap.com/v3/place/around?key=${key}&location=${location}&types=${t.type}&radius=${t.radius}&offset=3&extensions=base`
        ).then(r => r.json())

        const pois = res?.pois || []
        for (const poi of pois.slice(0, 1)) {
          if (results.length >= 6) break
          const dist = poi.distance
            ? (Number(poi.distance) >= 1000
                ? `约${(Number(poi.distance) / 1000).toFixed(1)}公里`
                : `约${poi.distance}米`)
            : '附近'
          results.push({ name: poi.name, distance: dist })
        }
      } catch (e) {
        console.warn(`周边搜索 ${t.label} 失败:`, e.message)
      }
    }

    if (results.length) nearbyList.value = results
  } catch (e) {
    console.warn('加载附近信息失败:', e.message)
  } finally {
    nearbyLoading.value = false
  }
}

// 兜底数据（加载失败或无地址时使用）
const nearbyListDisplay = computed(() => {
  if (nearbyList.value.length) return nearbyList.value
  const city = (hotel.value?.address || '').slice(0, 6) || '市区'
  return [
    { name: `${city}机场` },
    { name: `${city}火车站` },
    { name: '市中心广场' },
    { name: '城市博物馆' },
  ]
})

// 评价标签（从评论提取或兜底）
const reviewTags = computed(() => {
  const contentList = (reviews.value || []).map(r => r?.content || '')
  const tags = new Set()
  contentList.forEach(c => {
    if (/位置|地理|交通/.test(c)) tags.add('位置优越')
    if (/服务|前台|热情/.test(c)) tags.add('服务周到')
    if (/干净|卫生|整洁/.test(c)) tags.add('干净卫生')
    if (/早餐|餐厅|美食/.test(c)) tags.add('早餐不错')
    if (/亲子|儿童|家庭/.test(c)) tags.add('亲子友好')
    if (/安静|隔音/.test(c)) tags.add('安静舒适')
  })
  const base = ['位置优越', '服务周到', '干净卫生']
  const arr = Array.from(tags)
  return arr.length ? arr.slice(0, 8) : base
})

// 评分摘要（右侧卡片用）
const reviewSummary = computed(() => {
  const score = Number(hotel.value?.rating || 0)
  if (!Number.isFinite(score) || score <= 0) return '暂无评分'
  if (score >= 4.7) return '超棒 · 多位住客推荐'
  if (score >= 4.3) return '很棒 · 性价比高'
  if (score >= 4.0) return '不错 · 体验良好'
  return '一般 · 有待提升'
})

// 酒店亮点（新增模块）
const highlights = computed(() => {
  const list = []
  if ((hotel.value?.address || '').includes('海')) list.push('海景房优选')
  if ((hotel.value?.address || '').includes('景区') || (hotel.value?.address || '').includes('中心')) list.push('交通便利')
  if (!list.length) list.push('亲子友好')
  list.push('房间整洁舒适')
  return Array.from(new Set(list)).slice(0, 3)
})

// 生成带氛围感的介绍文案（兜底）
const introCopy = computed(() => {
  const name = hotel.value?.shopName || '这家酒店'
  const desc = (hotel.value?.description || '').trim()
  if (desc) {
    // 避免出现 null / undefined / detail.xxx
    if (/null|undefined|detail\./i.test(desc)) {
      return `${name}坐落于城市便捷地段，从清晨的第一缕阳光到夜幕的璀璨灯火，都能收获恰到好处的松弛与舒适。酒店提供丰富的设施与细致服务，无论是商务差旅还是周末小住，都是安心之选。`
    }
    return desc
  }
  return `${name}位于城市动静之间，步行可达热门打卡点。宽敞明亮的客房以温润色调铺陈，搭配柔软床品与贴心备品，营造出恰到好处的度假氛围。清晨在餐厅开启元气早餐，黄昏回到泳池舒展身心，让旅途的每一刻都更轻松。`
})

// 当无真实评论时，提供 1-2 条自然示例（仅展示，不提交到后端）
const sampleReviews = computed(() => {
  if ((reviews.value || []).length > 0) return []
  const now = Date.now()
  return [
    {
      id: 'sample-1',
      rating: 5,
      content: '位置真的很方便，步行就能到商圈。前台小姐姐很热情，房间也特别干净，下次还会再来！',
      createTime: new Date(now - 86400000).toISOString(),
      user: { username: 'M***n' },
      images: []
    },
    {
      id: 'sample-2',
      rating: 4,
      content: '早餐选择挺多，床很舒服，住着踏实。就是热门时段电梯稍微等了一会儿，总体很满意～',
      createTime: new Date(now - 3 * 86400000).toISOString(),
      user: { username: 'Y***g' },
      images: []
    }
  ]
})

const openGalleryPreview = (startIndex = 0) => {
  // el-image 预览由 HotelGallery 内部处理，此处保留兼容
}

const toggleFavorite = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再收藏')
    router.push('/login')
    return
  }
  const userId = userStore.user.id
  try {
    if (isFavorited.value) {
      await removeFavorite({
        userId,
        targetId: merchantId.value,
        targetType: FAVORITE_TYPE.MERCHANT
      })
      isFavorited.value = false
      ElMessage.success('已取消收藏')
    } else {
      await addFavorite({
        userId,
        targetId: merchantId.value,
        targetType: FAVORITE_TYPE.MERCHANT
      })
      isFavorited.value = true
      ElMessage.success('已添加到收藏')
    }
  } catch (e) {
    const status = e?.response?.status
    const msg = status === 400 || status === 404
      ? '收藏对象不存在或已下架'
      : (e?.response?.data?.message || e.message || '操作失败')
    ElMessage.error(msg)
    try {
      const res = await checkFavorite({
        userId,
        targetId: merchantId.value,
        targetType: FAVORITE_TYPE.MERCHANT
      })
      isFavorited.value = !!(res && res.success && res.data === true)
    } catch {}
  }
}
const isFavorited = ref(false)

const goBack = () => {
  // 若从行程详情进入，则返回对应行程详情
  if (route.query?.from === 'plan-detail' && route.query?.planId) {
    router.push(`/plan/${route.query.planId}`)
    return
  }
  // 默认返回到酒店推荐页
  router.push({
    name: 'Recommend',
    query: { type: 'hotel' }
  })
}

const handleBook = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后预订')
    router.push('/login')
    return
  }
  
  // 确保 roomTypes 已经加载，如果没有加载则尝试重新加载
  if (roomTypes.value.length === 0 && merchantId.value) {
      getRoomTypes(merchantId.value).then(res => {
          if (res.success) {
              roomTypes.value = res.data || [] // 确保是数组
              // 重新初始化表单
              bookingForm.value.roomTypeId = roomTypes.value.length > 0 ? roomTypes.value[0].id : null
          }
      }).catch(err => {
          console.error('加载房型失败', err)
          roomTypes.value = [] // 确保出错也是空数组
      }).finally(() => {
          // 无论是否加载成功，都显示对话框
          openBookingDialog()
      })
  } else {
      openBookingDialog()
  }
}

const openBookingDialog = () => {
  bookingForm.value = {
      roomTypeId: roomTypes.value.length > 0 ? roomTypes.value[0].id : null,
      dateRange: [],
      quantity: 1,
      contactName: userStore.user?.username || '',
      contactPhone: userStore.user?.phone || '',
      note: ''
  }
  bookingDialogVisible.value = true
}

const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

const confirmBooking = async () => {
    if (!bookingForm.value.dateRange || bookingForm.value.dateRange.length < 2) {
        ElMessage.warning('请选择入住日期')
        return
    }
    if (!bookingForm.value.roomTypeId && roomTypes.value.length > 0) {
        ElMessage.warning('请选择房型')
        return
    }
    if (!bookingForm.value.contactPhone || !/^1\d{10}$/.test(bookingForm.value.contactPhone)) {
        ElMessage.warning('请输入正确的手机号')
        return
    }
    
    bookingLoading.value = true
    try {
        const payload = {
            userId: userStore.user.id,
            merchantId: merchantId.value,
            roomTypeId: bookingForm.value.roomTypeId,
            checkInDate: bookingForm.value.dateRange[0],
            checkOutDate: bookingForm.value.dateRange[1],
            quantity: bookingForm.value.quantity,
            totalPrice: totalPrice.value,
            reservationNote: bookingForm.value.note,
            reservationTimeSlot: `${bookingForm.value.dateRange[0]} 至 ${bookingForm.value.dateRange[1]}`,
            contactName: bookingForm.value.contactName,
            contactPhone: bookingForm.value.contactPhone
        }
        
        const res = await createOrder(payload)
        if (res.success || res.data?.success) {
            ElMessage.success('等待商家确认中，可前往个人中心中我的订单查看')
            bookingDialogVisible.value = false
        }
    } catch (error) {
        console.error('预订失败', error)
        ElMessage.error(error.response?.data?.message || '预订失败')
    } finally {
        bookingLoading.value = false
    }
}

const handleAddToPlan = (event) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后加入行程')
    router.push('/login')
    return
  }
  
  if (!hotel.value) return
  
  if (event) {
    clickPosition.value = {
      x: event.clientX,
      y: event.clientY
    }
  } else {
    clickPosition.value = null
  }

  itemToAdd.value = {
    id: hotel.value.id,
    title: hotel.value.shopName,
    type: hotel.value.category === 'HOTEL' ? 'hotel' : 'food',
    description: hotel.value.description || (hotel.value.category === 'HOTEL' ? '入住酒店' : '享用美食')
  }
  showAddToPlanDialog.value = true
}

const handleShare = () => {
  // 复制当前页面链接
  const url = window.location.href
  navigator.clipboard.writeText(url).then(() => {
    ElMessage.success('链接已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制链接')
  })
}

const handleContact = () => {
  if (hotel.value?.phone) {
    ElMessage.info(`请拨打商家电话: ${hotel.value.phone}`)
  } else {
    ElMessage.warning('商家暂未提供联系电话')
  }
}

const openMap = () => {
  if (!hotel.value) return
  const keyword = encodeURIComponent(`${hotel.value.shopName} 景德镇`)
  window.open(`https://www.amap.com/search?query=${keyword}`, '_blank')
}

const normalizeHotelData = (raw) => {
  if (!raw) return null
  const h = { ...raw }
  
  // 优先使用管理员评分
  if (h.adminRating !== undefined && h.adminRating !== null && h.adminRating > 0) {
    h.rating = parseFloat(h.adminRating)
  } else if (h.rating === undefined || h.rating === null) {
    h.rating = 4.0
  } else if (typeof h.rating === 'string') {
    h.rating = parseFloat(h.rating) || 4.0
  }
  
  if (h.reviewCount === undefined || h.reviewCount === null) {
    h.reviewCount = 0
  }
  // 兜底电话
  if (!h.phone) h.phone = '暂无信息'
  return h
}

const loadHotelDetail = async () => {
  const id = route.params.id
  if (!id) {
    ElMessage.error('缺少酒店ID')
    return
  }
  loading.value = true
  try {
    const res = await getMerchantById(id)
    let data = res?.data?.data || res?.data || res?.merchant || null
    if (res?.success && res?.data) {
      data = res.data
    }
    hotel.value = normalizeHotelData(data)
    if (data) {
      merchantId.value = data.id
    }
    if (!hotel.value) {
      ElMessage.warning('未找到该酒店信息')
    } else {
      // 加载评价列表和活动
      await Promise.all([loadReviews(), loadActivities(data.id)])
      
      // 加载附近信息
      loadNearby()

      // 如果是酒店，加载房型
      if (data.category === 'HOTEL') {
          try {
              const rtRes = await getRoomTypes(data.id)
              if (rtRes.success) {
                  roomTypes.value = rtRes.data || [] // 确保是数组
              }
          } catch (e) {
              console.error('加载房型失败', e)
              roomTypes.value = [] // 确保出错也是空数组
          }
      }
    }
  } catch (e) {
    console.error('加载酒店详情失败:', e)
    ElMessage.error('加载酒店详情失败')
  } finally {
    loading.value = false
  }
}

const handleSubmitReview = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再发表评价')
    const redirect = encodeURIComponent(window.location.pathname + window.location.search)
    router.push(`/login?redirect=${redirect}`)
    return
  }
  
  if (!reviewForm.value.content.trim()) {
    ElMessage.warning('请输入评价内容')
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
      rating: reviewForm.value.rating,
      content: reviewForm.value.content,
      images: Array.isArray(reviewForm.value.images) ? reviewForm.value.images : []
    })
    
    ElMessage.success('评价发布成功')
    reviewForm.value = { rating: 5, content: '', images: [] }
    await loadReviews()
  } catch (error) {
    console.error('发布评价失败:', error)
    ElMessage.error(error?.response?.data?.message || '发布评价失败')
  }
}

const loadReviews = async () => {
  if (!merchantId.value) return
  
  try {
    const response = await getMerchantReviewList(merchantId.value)
    const data = response?.data?.data || response?.data || []
    reviews.value = (Array.isArray(data) ? data : []).map(r => ({
      ...r,
      images: toImageArray(r?.images)
    }))
    // 为每个评论加载回复列表
    for (const review of reviews.value) {
      await loadRepliesForReview(review.id)
    }
  } catch (error) {
    console.error('加载评价失败:', error)
  }
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
  if (end && end < now) return false   // 已过结束时间 → 已结束
  if (start && start > now) return false // 未到开始时间 → 未开始
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

const loadRepliesForReview = async (reviewId) => {
  try {
    const response = await getRepliesByReviewId(reviewId)
    const replies = response.data || response || []
    const normalizeReplies = (list) => {
      return (Array.isArray(list) ? list : []).map(rep => ({
        ...rep,
        images: toImageArray(rep?.images),
        childReplies: normalizeReplies(rep?.childReplies)
      }))
    }
    reviewReplies.value[reviewId] = normalizeReplies(replies)
  } catch (error) {
    console.error('加载回复失败:', error)
    reviewReplies.value[reviewId] = []
  }
}

const startReplyToReview = (review) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再回复')
    const redirect = encodeURIComponent(window.location.pathname + window.location.search)
    router.push(`/login?redirect=${redirect}`)
    return
  }
  replyingReviewId.value = review.id
  replyingReplyId.value = null
  replyContent.value = ''
}

const startReplyToReply = (review, reply) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再回复')
    const redirect = encodeURIComponent(window.location.pathname + window.location.search)
    router.push(`/login?redirect=${redirect}`)
    return
  }
  replyingReviewId.value = review.id
  replyingReplyId.value = reply.id
  replyContent.value = ''
}

const cancelReply = () => {
  replyingReviewId.value = null
  replyingReplyId.value = null
  replyContent.value = ''
}

const submitReplyToReview = async (review) => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  try {
    const res = await createUserReply(review.id, userStore.user.id, replyContent.value)
    ElMessage.success('回复成功')
    replyingReviewId.value = null
    replyContent.value = ''
    // 重新加载该评论的回复列表
    await loadRepliesForReview(review.id)
  } catch (error) {
    console.error('回复失败:', error)
    ElMessage.error(error?.response?.data?.message || '回复失败')
  }
}

const submitReplyToReply = async (review, parentReply) => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  try {
    const res = await createUserReplyToReply(review.id, parentReply.id, userStore.user.id, replyContent.value)
    ElMessage.success('回复成功')
    replyingReviewId.value = null
    replyingReplyId.value = null
    replyContent.value = ''
    // 重新加载该评论的回复列表
    await loadRepliesForReview(review.id)
  } catch (error) {
    console.error('回复失败:', error)
    ElMessage.error(error?.response?.data?.message || '回复失败')
  }
}

const getReplierName = (reply) => {
  if (reply.merchant) {
    return reply.merchant.shopName || '商家'
  } else if (reply.user) {
    return reply.user.username || '匿名用户'
  }
  return '匿名用户'
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

// 处理实时数据更新
const handleRealtimeUpdate = async (updateInfo) => {
  const { operation, data: updateData } = updateInfo
  
  // 确保 hotel 已加载
  if (!hotel.value || !merchantId.value) return

  // 检查是否为当前商家的更新
  const currentId = String(merchantId.value)
  const updateId = updateData?.id ? String(updateData.id) : null

  if (operation === 'update' && updateId === currentId) {
    console.log('HotelDetail received update:', updateData)
    
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
    
    const merged = { ...hotel.value, ...updateData }
    hotel.value = normalizeHotelData(merged)

    // 评分/评论数变化时，刷新评论列表（满足：删除评价后其他游客实时刷新）
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

const handleRoomTypeUpdate = (updateInfo) => {
  const { operation, data } = updateInfo
  if (!merchantId.value) return
  const currentId = String(merchantId.value)
  const items = Array.isArray(data) ? data : [data]
  const related = items.filter(it => String(it?.merchantId || it?.merchant_id || it?.merchant?.id) === currentId)
  if (related.length === 0) return
  if (operation === 'create' || operation === 'add') {
    const exists = new Set(roomTypes.value.map(rt => rt.id))
    const merged = [...roomTypes.value]
    related.forEach(rt => { if (!exists.has(rt.id)) merged.push(rt) })
    roomTypes.value = merged
  } else if (operation === 'update') {
    const map = new Map(related.map(rt => [rt.id, rt]))
    roomTypes.value = roomTypes.value.map(rt => map.get(rt.id) || rt)
  } else if (operation === 'delete' || operation === 'remove') {
    const removeIds = new Set(related.map(rt => rt.id))
    roomTypes.value = roomTypes.value.filter(rt => !removeIds.has(rt.id))
  } else if (operation === 'list' || operation === 'replace') {
    roomTypes.value = related
  } else {
    getRoomTypes(merchantId.value).then(res => {
      if (res?.success) {
        roomTypes.value = Array.isArray(res.data) ? res.data : []
      }
    }).catch(() => {})
  }
  if (bookingDialogVisible.value) {
    const curId = bookingForm.value.roomTypeId
    const has = roomTypes.value.some(rt => rt.id === curId)
    bookingForm.value.roomTypeId = has ? curId : (roomTypes.value[0]?.id || null)
  }
}

onMounted(async () => {
  loadHotelDetail()
  // 初始化收藏态（服务端）
  if (userStore.isLoggedIn) {
    try {
      const res = await checkFavorite({
        userId: userStore.user.id,
        targetId: route.params.id,
        targetType: FAVORITE_TYPE.MERCHANT
      })
      isFavorited.value = !!(res && res.success && res.data === true)
    } catch { isFavorited.value = false }
  }
  
  // 连接WebSocket并订阅商家更新
  if (!realtimeSync.isConnected) {
    realtimeSync.connect()
  }
  realtimeSync.subscribe('merchant', handleRealtimeUpdate)
  realtimeSync.subscribe('room_type', handleRoomTypeUpdate)
})

onUnmounted(() => {
  realtimeSync.unsubscribe('merchant', handleRealtimeUpdate)
  realtimeSync.unsubscribe('room_type', handleRoomTypeUpdate)
})
</script>

<style scoped>
.hotel-detail {
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
.header-section.compact {
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}
.title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.sub-row {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  margin-top: 6px;
}
.addr { max-width: 560px; white-space: nowrap; text-overflow: ellipsis; overflow: hidden; }
.price-cta { display: flex; align-items: center; gap: 12px; }
.price { color: #1f2937; font-weight: 800; font-size: 22px; }
.price .yen { font-size: 16px; margin-right: 1px; }
.price .suffix { font-size: 12px; margin-left: 2px; color: #6b7280; font-weight: 600; }
.cta { font-weight: 700; }

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

.content-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
  margin-bottom: 24px;
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
  scroll-margin-top: 80px;
  transition: all 0.3s ease;
}

/* 高亮效果 */
.review-section.highlight-section {
  animation: highlight-pulse 2s ease;
}

@keyframes highlight-pulse {
  0%, 100% {
    background: transparent;
  }
  50% {
    background: rgba(64, 158, 255, 0.08);
    border-radius: 8px;
  }
}

.review-section h2 {
  margin-bottom: 20px;
  color: #303133;
  font-size: 24px;
}

.review-form {
  margin-bottom: 30px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.login-prompt {
  margin-bottom: 30px;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.review-item {
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: white;
}

.review-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.review-info {
  flex: 1;
}

.review-user {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.review-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.review-time {
  color: #909399;
  font-size: 12px;
}

.review-content {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 12px;
}

.review-images {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.review-image-item {
  width: 100px;
  height: 100px;
  border-radius: 6px;
  object-fit: cover;
}

.review-actions {
  margin-top: 8px;
}

.reply-form-inline {
  margin-top: 12px;
}

.reply-actions {
  margin-top: 8px;
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.replies-container {
  margin-top: 16px;
  padding-left: 52px;
  border-left: 2px solid #ebeef5;
}

.reply-item {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.reply-item:last-child {
  border-bottom: none;
}

.reply-header-info {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
}

.reply-info {
  flex: 1;
}

.reply-user {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.replier-name {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.reply-content-text {
  color: #606266;
  line-height: 1.6;
  font-size: 14px;
}

.reply-actions-inline {
  margin-top: 8px;
  margin-left: 44px;
}

.reply-form-inline.nested {
  margin-top: 12px;
  margin-left: 44px;
}

.child-replies {
  margin-top: 12px;
  margin-left: 20px;
  padding-left: 20px;
  border-left: 2px solid #f0f0f0;
}

.child-reply-item {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
  padding-bottom: 8px;
}

.child-reply-item:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
}

.child-reply-info {
  flex: 1;
}

.child-reply-user {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
}

.child-reply-user .replier-name {
  font-size: 13px;
}

.child-reply-content {
  color: #606266;
  line-height: 1.5;
  font-size: 13px;
  margin-bottom: 4px;
}

.child-reply-actions {
  margin-top: 4px;
}

.reply-form-inline.nested-child {
  margin-top: 8px;
  margin-left: 0;
}

.reply-to-hint {
  font-size: 12px;
  color: #409eff;
  margin-bottom: 4px;
  font-weight: 500;
}

.review-reply {
  margin-top: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
  border-left: 3px solid #409eff;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  color: #409eff;
  font-weight: 600;
  font-size: 14px;
}

.reply-time {
  margin-left: auto;
  color: #909399;
  font-size: 12px;
  font-weight: normal;
}

.reply-content {
  color: #606266;
  line-height: 1.6;
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

.hotel-title {
  margin: 0 0 16px 0;
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.category-tag {
  margin-left: auto;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 14px;
}

.info-item .el-icon {
  font-size: 16px;
  color: #909399;
}

.action-buttons {
  display: flex;
  gap: 16px;
  margin-top: auto;
}

.detail-card {
  margin-bottom: 16px;
  border-radius: 8px;
  border: none;
  box-shadow: 0 2px 10px 0 rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header .title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  border-left: 4px solid #409eff;
  padding-left: 10px;
}

.rich-text-content {
  color: #606266;
  line-height: 1.7;
  font-size: 14px;
}

.activity-card .el-card__body {
  padding: 0;
}

.activity-main {
  flex: 1;
  padding: 14px 16px 12px;
}

.activity-item {
  display: flex;
  border-bottom: 1px solid #f0f2f5;
  position: relative;
  transition: background 200ms ease;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-item::before {
  content: '';
  display: block;
  width: 4px;
  flex-shrink: 0;
  background: linear-gradient(180deg, #e6a23c, #f5a623);
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
  font-weight: 600;
  color: #1a1a1a;
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
  flex-direction: column;
  justify-content: center;
  gap: 6px;
  padding: 14px 14px 14px 0;
  flex-shrink: 0;
}

.activity-image {
  width: 100px;
  height: 80px;
  border-radius: 8px;
  cursor: pointer;
  object-fit: cover;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.review-stats {
  display: flex;
  align-items: baseline;
}

.review-stats .score {
  font-size: 32px;
  font-weight: 700;
  color: #ff9900;
  line-height: 1;
}

.review-stats .total {
  font-size: 14px;
  color: #909399;
  margin-left: 4px;
}

.form-rating {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.form-rating span {
  font-size: 14px;
  color: #606266;
  margin-right: 8px;
}

.review-footer {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.reply-input-area {
  margin-top: 16px;
  background: #f9fafc;
  padding: 16px;
  border-radius: 8px;
}

.reply-input-area.nested {
  margin-top: 12px;
}

.reply-target {
  font-size: 13px;
  color: #409eff;
  margin-bottom: 8px;
}

.reply-content-wrapper {
  padding: 12px 0;
}

.reply-header-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  flex-wrap: wrap;
}

.reply-target-text {
  color: #909399;
  font-size: 13px;
}

.is-merchant {
  color: #e6a23c !important;
}

.reply-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

.reply-images {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.reply-image-item {
  width: 90px;
  height: 90px;
  border-radius: 6px;
  object-fit: cover;
}

.sidebar-card {
  position: sticky;
  top: 80px;
}
.tag-grid { display: flex; flex-wrap: wrap; gap: 8px; }
.tag-chip { margin: 0; }

.sidebar-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}

.map-preview {
  margin-top: 8px;
  height: 80px;
  background: #f0f2f5;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  cursor: pointer;
  transition: all 0.3s;
}

.map-preview:hover {
  background: #e6e8eb;
  color: #409eff;
}

.map-preview .el-icon {
  font-size: 24px;
  margin-bottom: 4px;
}

.sidebar-actions {
  margin-top: 16px;
}

.full-width {
  width: 100%;
}

.tips-list {
  margin: 0;
  padding-left: 20px;
  color: #606266;
  font-size: 14px;
  line-height: 1.8;
}

.tips-list li {
  margin-bottom: 8px;
}

.form-tip {
  font-size: 12px;
  color: #f56c6c;
  margin-top: 4px;
}

/* 房型卡片选择 */
.room-type-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
  max-height: 320px;
  overflow-y: auto;
}

.room-type-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  border: 2px solid #ebeef5;
  border-radius: 8px;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s;
}

.room-type-card:hover { border-color: #409eff; background: #f0f7ff; }
.room-type-card.selected { border-color: #409eff; background: #ecf5ff; }

.room-card-img {
  width: 90px;
  height: 70px;
  border-radius: 6px;
  flex-shrink: 0;
}

.room-card-img-placeholder {
  width: 90px;
  height: 70px;
  border-radius: 6px;
  flex-shrink: 0;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  font-size: 24px;
}

.room-card-info { flex: 1; min-width: 0; }
.room-card-name { font-weight: 600; font-size: 14px; color: #303133; margin-bottom: 4px; }
.room-card-meta { font-size: 12px; color: #606266; margin-bottom: 4px; display: flex; align-items: center; flex-wrap: wrap; gap: 2px; }
.room-card-stock { font-size: 12px; color: #e6a23c; }
.room-card-desc { font-size: 12px; color: #909399; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.room-card-price { flex-shrink: 0; text-align: right; }
.price-num { font-size: 18px; font-weight: 700; color: #f56c6c; }
.price-unit { font-size: 12px; color: #909399; }

.total-price {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  text-align: right;
  font-size: 16px;
  color: #606266;
}

.total-price .price {
  font-size: 24px;
  color: #f56c6c;
  font-weight: bold;
}

.room-type-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
  padding: 20px;
}

.room-type-item {
  display: flex;
  gap: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.room-type-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.room-image-wrapper {
  width: 120px;
  height: 90px;
  flex-shrink: 0;
  border-radius: 4px;
  overflow: hidden;
  background-color: #f5f7fa;
  display: flex;
  justify-content: center;
  align-items: center;
}

.room-image {
  width: 100%;
  height: 100%;
}

.room-image-placeholder {
  color: #909399;
  font-size: 24px;
}

.room-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.room-info h4 {
  margin: 0 0 5px 0;
  font-size: 16px;
  color: #303133;
}

.room-tags {
  display: flex;
  gap: 5px;
  margin-bottom: 5px;
}

.room-desc {
  font-size: 13px;
  color: #606266;
  margin: 0 0 5px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.room-price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

