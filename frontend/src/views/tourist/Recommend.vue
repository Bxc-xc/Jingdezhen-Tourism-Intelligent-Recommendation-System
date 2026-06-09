<template>
  <div class="recommend">
    <Header />

    <div class="recommend-main">
      <RecommendSidebar :active-type="recommendType" @change="changeRecommendType" />

      <!-- 推荐内容区域 -->
      <div class="recommend-content">
        <div class="page-header">
          <SmartRecommendText />
          <SortSupSub
            :sort-field="sortField"
            :sort-order="sortOrder"
            :options="sortOptions"
            @set="setSort"
          />
        </div>

        <!-- 推荐结果 -->
        <div class="recommend-results">
          <!-- 景点推荐列表 -->
          <div
            v-if="recommendType === 'scenic'"
            v-loading="scenicStore.loading"
            class="scenic-results-container"
          >
            <div class="scenic-grid">
              <div
                v-for="scenic in pagedRecommendList"
                :key="scenic.id"
                class="scenic-grid-item"
              >
                <ScenicCard :scenic="scenic" :hide-tags="true" />
              </div>
            </div>
          </div>

          <!-- 美食推荐列表 -->
          <el-row v-if="recommendType === 'food'" :gutter="12" v-loading="loading">
            <el-col
              v-for="food in pagedFoodList"
              :key="food.id"
              :xs="24" :sm="12" :md="12" :lg="8"
            >
              <el-card class="food-card" shadow="hover" @click="viewFoodDetail(food)">
                <div class="food-image">
                  <img :src="food.image || FALLBACK_IMAGE" :alt="food.name" @error="e => e.target.src = FALLBACK_IMAGE" />
                  <div class="food-rating">
                    <el-rate v-model="food.rating" disabled show-score text-color="#ff9900" />
                    <span class="review-count">({{ food.reviewCount }}条评价)</span>
                  </div>
                </div>
                <div class="food-info">
                  <h3>{{ food.name }}</h3>
                  <p class="food-description">{{ food.description }}</p>
                  <div class="food-meta">
                    <el-tag size="small" type="info">{{ food.category }}</el-tag>
                    <el-tag 
                      v-if="hasActiveActivities(food.activities)" 
                      size="small" 
                      type="danger" 
                      effect="plain" 
                      style="margin-left: 5px"
                    >
                      有活动
                    </el-tag>
                    <span class="food-price">¥{{ food.price }}/人</span>
                  </div>
                  <div class="food-tags">
                    <el-tag
                      v-for="tag in food.tags"
                      :key="tag"
                      size="small"
                      type="info"
                      class="tag-item"
                    >
                      {{ tag }}
                    </el-tag>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <!-- 酒店推荐列表 -->
          <el-row v-if="recommendType === 'hotel'" :gutter="12" v-loading="loading">
            <el-col
              v-for="hotel in pagedHotelList"
              :key="hotel.id"
              :xs="24" :sm="12" :md="12" :lg="8"
            >
              <el-card class="hotel-card" shadow="hover" @click="viewHotelDetail(hotel)">
                <div class="hotel-image">
                  <img :src="getHotelImage(hotel)" :alt="hotel.shopName" @error="e => e.target.src = FALLBACK_IMAGE" />
                  <div class="hotel-rating-overlay">
                    <el-rate
                      v-model="hotel.rating"
                      disabled
                      show-score
                      text-color="#ff9900"
                      :allow-half="true"
                    />
                    <span class="review-count" v-if="hotel.reviewCount !== undefined && hotel.reviewCount > 0">
                      ({{ hotel.reviewCount }}条评价)
                    </span>
                  </div>
                </div>
                <div class="hotel-info">
                  <h3>{{ hotel.shopName }}</h3>
                  <p class="hotel-description">
                    {{ hotel.description || '暂无描述' }}
                  </p>
                  <div class="hotel-meta">
                    <el-tag size="small" type="success">酒店</el-tag>
                    <el-tag 
                      v-if="hasActiveActivities(hotel.activities)" 
                      size="small" 
                      type="danger" 
                      effect="plain" 
                      style="margin-left: 5px"
                    >
                      有活动
                    </el-tag>

                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <!-- 陶瓷工坊推荐列表 -->
          <el-row v-if="recommendType === 'ceramic'" :gutter="12" v-loading="loading">
            <el-col
              v-for="workshop in pagedCeramicList"
              :key="workshop.id"
              :xs="24" :sm="12" :md="12" :lg="8"
            >
              <el-card class="hotel-card" shadow="hover" @click="viewCeramicDetail(workshop)">
                <div class="hotel-image">
                  <img :src="getHotelImage(workshop)" :alt="workshop.shopName" @error="e => e.target.src = FALLBACK_IMAGE" />
                  <div class="hotel-rating-overlay">
                    <el-rate
                      v-model="workshop.rating"
                      disabled
                      show-score
                      text-color="#ff9900"
                      :allow-half="true"
                    />
                    <span class="review-count" v-if="workshop.reviewCount > 0">
                      ({{ workshop.reviewCount }}条评价)
                    </span>
                  </div>
                </div>
                <div class="hotel-info">
                  <h3>{{ workshop.shopName }}</h3>
                  <p class="hotel-description">{{ workshop.description || '暂无描述' }}</p>
                  <div class="hotel-meta">
                    <el-tag size="small" type="warning">陶瓷工坊</el-tag>
                    <el-tag v-if="hasActiveActivities(workshop.activities)" size="small" type="danger" effect="plain" style="margin-left:5px">有活动</el-tag>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <!-- 路线推荐列表 -->
          <div
            v-if="recommendType === 'route'"
            v-loading="loading"
            class="route-grid"
          >
            <RouteCard
              v-for="routeItem in pagedRouteList"
              :key="routeItem.id"
              :route="routeItem"
            />
          </div>

          <!-- 分页 -->
          <div class="pagination" v-if="total > 0">
            <el-button
              text
              :disabled="currentPage === 1"
              @click="goPrevPage"
            >
              上一页
            </el-button>
            <span class="pager-info">第 {{ currentPage }} / {{ totalPages }} 页</span>
            <el-button
              text
              :disabled="currentPage >= totalPages"
              @click="goNextPage"
            >
              下一页
            </el-button>
          </div>

          <!-- 空状态 -->
          <el-empty
            v-if="!loading && (
              (recommendType === 'scenic' && scenicStore.recommendList.length === 0) ||
              (recommendType === 'food' && foodList.length === 0) ||
              (recommendType === 'route' && routeList.length === 0) ||
              (recommendType === 'hotel' && hotelList.length === 0) ||
              (recommendType === 'ceramic' && ceramicList.length === 0)
            )"
            description="暂无推荐内容"
          />
        </div>
      </div>
    </div>

    <Footer />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useScenicStore } from '../../stores/scenic'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'
import {
  Location,
  Calendar
} from '@element-plus/icons-vue'
import { getRecommendFood } from '../../api/food'
import { getRecommendRoutes, getRouteDetail } from '../../api/route'
import { getMerchantsByCategory } from '../../api/merchant'
import { getMerchantActivities } from '../../api/merchantActivity'
import Header from '../../components/Layout/Header.vue'
import Footer from '../../components/Layout/Footer.vue'
import ScenicCard from '../../components/ScenicCard.vue'
import SmartRecommendText from '../../components/recommend/SmartRecommendText.vue'
import SortSupSub from '../../components/recommend/SortSupSub.vue'
import RecommendSidebar from '../../components/recommend/RecommendSidebar.vue'
import RouteCard from '../../components/recommend/RouteCard.vue'
import { FALLBACK_IMAGE } from '../../utils/image'

const router = useRouter()
const route = useRoute()
const scenicStore = useScenicStore()
const userStore = useUserStore()

// 筛选状态持久化 key
const FILTER_STORAGE_KEY = 'recommend_filter_state'

// 从 sessionStorage 读取上次筛选状态
const savedFilter = (() => {
  try { return JSON.parse(sessionStorage.getItem(FILTER_STORAGE_KEY) || '{}') } catch { return {} }
})()

// 从路由查询参数中获取推荐类型，默认 'scenic'
const recommendType = ref(route.query.type || savedFilter.type || 'scenic')
const currentPage = ref(Number(route.query.page) || Number(savedFilter.page) || 1)
const pageSize = ref(8)
const total = ref(0)
const sortField = ref(route.query.sortField || savedFilter.sortField || '')
const sortOrder = ref(route.query.sortOrder || savedFilter.sortOrder || '')

// 保存筛选状态到 sessionStorage
const saveFilterState = () => {
  sessionStorage.setItem(FILTER_STORAGE_KEY, JSON.stringify({
    type: recommendType.value,
    page: currentPage.value,
    sortField: sortField.value,
    sortOrder: sortOrder.value
  }))
}
const loading = ref(false)

// 判断活动是否进行中
const isActivityActive = (activity) => {
  const now = new Date()
  const end = activity.endTime ? new Date(activity.endTime) : null
  const start = activity.startTime ? new Date(activity.startTime) : null
  if (end && end < now) return false
  if (start && start > now) return false
  return true
}
const hasActiveActivities = (activities) =>
  Array.isArray(activities) && activities.some(isActivityActive)

const sortOptions = [
  { label: '评分', value: 'rating' },
  { label: '价格', value: 'price', defaultOrder: 'desc' },
  { label: '热度', value: 'favoriteCount' }
]

// 美食、酒店和路线数据
const foodList = ref([])
const hotelList = ref([])
const routeList = ref([])
const ceramicList = ref([])

// 景点数据缓存（用于路线图片生成）
const scenicCache = ref(new Map())

const toNumber = (value, fallback = 0) => {
  const num = Number(value)
  return Number.isFinite(num) ? num : fallback
}

const getRatingValue = (item) => toNumber(item?.rating, 0)
const getPriceValue = (item) => {
  // 依次尝试各价格字段，startPrice 可能是 BigDecimal 序列化的字符串
  const raw = item?.price ?? item?.totalPrice ?? item?.startPrice ?? item?.avgPrice ?? 0
  if (typeof raw === 'string') {
    // avgPrice 可能是 "50-100" 这种范围，取上限
    const parts = raw.split('-').map(Number).filter(Number.isFinite)
    if (parts.length > 0) return Math.max(...parts)
  }
  return toNumber(raw, 0)
}
const getFavoriteValue = (item) => toNumber(item?.favoriteCount ?? item?.collectCount ?? item?.favoriteNum ?? 0, 0)

const sortListByRule = (list) => {
  const sorted = [...list]
  if (!sortField.value || !sortOrder.value) return sorted

  // 约定：desc = 高优先（高评分/高价/收藏多），asc = 低优先
  const isHighFirst = sortOrder.value === 'desc'
  sorted.sort((a, b) => {
    if (sortField.value === 'rating') {
      const av = getRatingValue(a)
      const bv = getRatingValue(b)
      return isHighFirst ? bv - av : av - bv
    }

    if (sortField.value === 'price') {
      const av = getPriceValue(a)
      const bv = getPriceValue(b)
      return isHighFirst ? bv - av : av - bv
    }

    if (sortField.value === 'favoriteCount') {
      const av = getFavoriteValue(a)
      const bv = getFavoriteValue(b)
      return isHighFirst ? bv - av : av - bv
    }

    return 0
  })

  return sorted
}

const totalPages = computed(() => {
  if (recommendType.value === 'scenic') {
    if (!total.value || !pageSize.value) return 1
    return Math.max(1, Math.ceil(total.value / pageSize.value))
  }
  let listLength = 0
  if (recommendType.value === 'food') {
    listLength = sortedFoodList.value.length
  } else if (recommendType.value === 'route') {
    listLength = sortedRouteList.value.length
  } else if (recommendType.value === 'hotel') {
    listLength = sortedHotelList.value.length
  } else if (recommendType.value === 'ceramic') {
    listLength = sortedCeramicList.value.length
  }
  if (!listLength || !pageSize.value) return 1
  return Math.max(1, Math.ceil(listLength / pageSize.value))
})

// 景点：后端已分页且已排序，直接展示
const sortedRecommendList = computed(() => scenicStore.recommendList)
const sortedFoodList = computed(() => sortListByRule(foodList.value))
const sortedHotelList = computed(() => sortListByRule(hotelList.value))
const sortedRouteList = computed(() => sortListByRule(routeList.value))
const sortedCeramicList = computed(() => sortListByRule(ceramicList.value))

// 景点直接展示后端返回的当页数据
const pagedRecommendList = computed(() => sortedRecommendList.value)

const pagedFoodList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return sortedFoodList.value.slice(start, end)
})

const pagedHotelList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return sortedHotelList.value.slice(start, end)
})

const pagedCeramicList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return sortedCeramicList.value.slice(start, end)
})

const pagedRouteList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + 6
  return sortedRouteList.value.slice(start, end)
})

const handleTypeChange = async () => {
  currentPage.value = 1
  if (recommendType.value === 'food') {
    await loadFoodData()
  } else if (recommendType.value === 'route') {
    await loadRouteData()
  } else if (recommendType.value === 'hotel') {
    await loadHotelData()
  } else if (recommendType.value === 'ceramic') {
    await loadCeramicData()
  } else {
    await loadRecommendData()
  }
}

// 左侧导航：切换推荐内容类型
const changeRecommendType = async (type) => {
  if (recommendType.value === type) return
  recommendType.value = type
  sortField.value = ''
  sortOrder.value = ''
  currentPage.value = 1
  router.replace({
    query: { type }
  })
  saveFilterState()
  await handleTypeChange()
}

// 规范化美食数据
const normalizeFoodData = (food) => {
  // 确保 tags 是数组格式
  if (food.tags) {
    if (typeof food.tags === 'string') {
      // 如果是字符串（逗号分隔），转换为数组
      food.tags = food.tags.split(',').map(tag => tag.trim()).filter(tag => tag)
    } else if (!Array.isArray(food.tags)) {
      food.tags = []
    }
  } else {
    food.tags = []
  }
  
  // 确保图片URL存在（优先使用 images 数组、cover、avatar，最后占位图）
  if (!food.image || food.image === '') {
    // 1. 优先使用 images 数组第一张
    if (Array.isArray(food.images) && food.images.length > 0) {
      food.image = food.images[0]
    }
    // 2. 解析 shopImages 字符串
    else if (food.shopImages && typeof food.shopImages === 'string') {
      const imgs = food.shopImages.split(',').map(s => s.trim()).filter(Boolean)
      if (imgs.length > 0) food.image = imgs[0]
    }
    // 3. 使用 cover 或 avatar
    else if (food.cover) {
      food.image = food.cover
    } else if (food.avatar) {
      food.image = food.avatar
    }
    // 4. 占位图
    else {
      food.image = '/images/placeholders/food_placeholder.jpg'
    }
  }
  
  // 规范化人均价格：优先使用 avgPrice，其次 startPrice，最后才是 price
  // 避免首页美食卡片因 price 字段缺失被错误显示为 0
  const resolvedPrice = food.avgPrice ?? food.startPrice ?? food.price
  if (resolvedPrice === undefined || resolvedPrice === null || resolvedPrice === '') {
    food.price = 0
  } else if (typeof resolvedPrice === 'string') {
    // 兼容 "50-80"、"约60" 等字符串，提取数字并取最大值作为展示价
    const numbers = resolvedPrice.match(/\d+(\.\d+)?/g)?.map(Number).filter(Number.isFinite) || []
    food.price = numbers.length > 0 ? Math.max(...numbers) : 0
  } else {
    const num = Number(resolvedPrice)
    food.price = Number.isFinite(num) ? num : 0
  }
  
  // 确保评分是数字
  if (food.rating === undefined || food.rating === null) {
    food.rating = 4.0
  } else if (typeof food.rating === 'string') {
    food.rating = parseFloat(food.rating) || 4.0
  }
  
  // 确保评论数是数字
  if (food.reviewCount === undefined || food.reviewCount === null) {
    food.reviewCount = 0
  } else if (typeof food.reviewCount === 'string') {
    food.reviewCount = parseInt(food.reviewCount) || 0
  }
  
  return food
}

// 加载美食数据
const loadFoodData = async () => {
  loading.value = true
  try {
    const userId = userStore.isLoggedIn ? userStore.user?.id : undefined
    const response = await getRecommendFood({ userId })
    // 处理不同的响应格式
    let foods = []
    if (response.success !== undefined) {
      if (response.success) {
        foods = response.data || []
        total.value = response.total || foods.length
      } else {
        // 如果后端返回失败，使用降级方案
        foods = []
        total.value = 0
      }
    } else {
      // 直接返回数组的情况
      foods = Array.isArray(response) ? response : (response.data || [])
      total.value = foods.length
    }
    
    // 规范化所有美食数据
    foodList.value = foods.map(food => normalizeFoodData(food))

    // 后台异步附加活动信息，不阻塞列表渲染
    attachActivitiesToList(foodList.value)
  } catch (error) {
    console.error('加载美食推荐失败:', error)
    foodList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 加载路线数据
const loadRouteData = async () => {
  loading.value = true
  try {
    const userId = userStore.isLoggedIn ? userStore.user?.id : undefined
    const response = await getRecommendRoutes({ userId })
    let routes = []
    if (response && response.data !== undefined) {
      routes = Array.isArray(response.data) ? response.data : []
      total.value = response.total || routes.length
    } else if (Array.isArray(response)) {
      routes = response
      total.value = routes.length
    } else {
      routes = []
      total.value = 0
    }
    routeList.value = routes.map(r => normalizeRouteData(r))
  } catch (error) {
    console.error('加载路线推荐失败:', error)
    routeList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 规范化路线数据，处理景点列表和生成图片
const normalizeRouteData = (route) => {
  const normalized = { ...route }
  
  // 处理景点列表：如果是JSON字符串，解析为数组
  if (normalized.scenicSpots) {
    if (typeof normalized.scenicSpots === 'string') {
      try {
        normalized.scenicSpots = JSON.parse(normalized.scenicSpots)
      } catch (e) {
        console.warn('解析路线景点列表失败:', e)
        normalized.scenicSpots = []
      }
    } else if (!Array.isArray(normalized.scenicSpots)) {
      normalized.scenicSpots = []
    }
  } else {
    normalized.scenicSpots = []
  }
  
  // 处理标签：如果是字符串，转换为数组
  if (normalized.tags) {
    if (typeof normalized.tags === 'string') {
      normalized.tags = normalized.tags.split(',').map(tag => tag.trim()).filter(tag => tag)
    } else if (!Array.isArray(normalized.tags)) {
      normalized.tags = []
    }
  } else {
    normalized.tags = []
  }
  
  // 如果没有图片，根据行程安排生成图片
  if (!normalized.image || normalized.image === '') {
    normalized.image = generateRouteImage(normalized)
  }
  
  return normalized
}

// 根据行程安排生成路线图片（同步，不发网络请求）
const generateRouteImage = (route) => {
  // 如果路线有景点列表，尝试从已缓存的景点数据获取图片
  if (route.scenicSpots && route.scenicSpots.length > 0) {
    for (const spot of route.scenicSpots) {
      if (spot && spot.id) {
        const scenicData = scenicCache.value.get(spot.id)
        if (scenicData) {
          if (scenicData.scenicImages) {
            const images = typeof scenicData.scenicImages === 'string'
              ? scenicData.scenicImages.split(',').map(img => img.trim()).filter(img => img)
              : scenicData.scenicImages
            if (images && images.length > 0) return images[0]
          }
          if (scenicData.image) return scenicData.image
        }
        // 景点对象本身可能带有 image 字段
        if (spot.image) return spot.image
      }
    }
  }
  return '/images/placeholders/route_placeholder.jpg'
}

// 获取路线图片（用于显示）
const getRouteImage = (route) => {
  // 如果路线有图片，直接使用
  if (route.image && route.image !== '') {
    return route.image
  }
  
  // 如果路线有景点列表，尝试从景点获取图片
  if (route.scenicSpots && route.scenicSpots.length > 0) {
    // 从第一个景点获取图片（如果已缓存）
    const firstSpot = route.scenicSpots[0]
    if (firstSpot && firstSpot.id) {
      const scenicData = scenicCache.value.get(firstSpot.id)
      if (scenicData) {
        if (scenicData.scenicImages) {
          const images = typeof scenicData.scenicImages === 'string' 
            ? scenicData.scenicImages.split(',').map(img => img.trim()).filter(img => img)
            : scenicData.scenicImages
          if (images && images.length > 0) {
            return images[0]
          }
        }
        if (scenicData.image) {
          return scenicData.image
        }
      }
    }
  }
  
  // 默认占位图
  return '/images/placeholders/route_placeholder.jpg'
}

// 加载酒店商家数据
const loadHotelData = async () => {
  loading.value = true
  try {
    const userId = userStore.isLoggedIn ? userStore.user?.id : undefined
    const response = await getMerchantsByCategory('HOTEL', userId)
    let merchants = []
    if (response?.success && Array.isArray(response.data)) {
      merchants = response.data
    } else if (Array.isArray(response)) {
      merchants = response
    } else if (response?.data && Array.isArray(response.data.data)) {
      merchants = response.data.data
    }
  // 规范化评分数据
  hotelList.value = (merchants || []).map(hotel => normalizeHotelRating({ ...hotel }))
  total.value = hotelList.value.length
  // 后台异步附加活动信息，不阻塞列表渲染
  attachActivitiesToList(hotelList.value)
  } catch (error) {
    console.error('加载酒店推荐失败:', error)
    hotelList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 获取酒店展示图片（优先使用 images 数组第一张，其次 avatar，最后占位图）
const getHotelImage = (hotel) => {
  // 1. 优先使用 images 数组第一张
  if (Array.isArray(hotel.images) && hotel.images.length > 0) {
    return hotel.images[0]
  }
  // 2. 解析 shopImages 字符串（逗号分隔）
  if (hotel.shopImages && typeof hotel.shopImages === 'string') {
    const imgs = hotel.shopImages.split(',').map(s => s.trim()).filter(Boolean)
    if (imgs.length > 0) return imgs[0]
  }
  // 3. 使用 cover 或 avatar
  if (hotel.cover) return hotel.cover
  if (hotel.avatar) return hotel.avatar
  // 4. 占位图
  return '/images/placeholders/hotel_placeholder.jpg'
}

// 根据酒店星级生成默认收藏数（五星级最高，依次递减）
const getDefaultFavoriteCount = (hotel) => {
  const starMap = {
    '5_STAR': [800, 1200],
    'LUXURY': [800, 1200],
    '4_STAR': [400, 700],
    '3_STAR': [150, 350],
    'ECONOMY': [30, 120],
  }
  const range = starMap[hotel.starRating] || [20, 80]
  return Math.floor(range[0] + Math.random() * (range[1] - range[0]))
}

// 确保评分是数字类型
const normalizeHotelRating = (hotel) => {
  if (hotel.rating === undefined || hotel.rating === null) {
    hotel.rating = 4.0
  } else if (typeof hotel.rating === 'string') {
    hotel.rating = parseFloat(hotel.rating) || 4.0
  }
  if (hotel.reviewCount === undefined || hotel.reviewCount === null) {
    hotel.reviewCount = 0
  }
  // 若后端没有返回收藏数，按星级生成默认值
  if (!hotel.favoriteCount && !hotel.collectCount && !hotel.favoriteNum) {
    hotel.favoriteCount = getDefaultFavoriteCount(hotel)
  }
  return hotel
}

// 为商家列表附加活动数组
const attachActivitiesToList = async (list) => {
  if (!Array.isArray(list) || list.length === 0) return
  try {
    await Promise.all(
      list.map(async (item) => {
        if (!item || !item.id) return
        try {
          const res = await getMerchantActivities(item.id)
          if (res && res.success) {
            const data = res.data || []
            item.activities = Array.isArray(data) ? data : []
          } else if (Array.isArray(res)) {
            item.activities = res
          } else {
            item.activities = []
          }
        } catch (e) {
          // 404 表示该条目不是商家，静默处理
          if (e?.response?.status !== 404) {
            console.error('加载商家活动失败（用于卡片标签）:', e)
          }
          item.activities = []
        }
      })
    )
  } catch (e) {
    // 整体失败时静默处理
    console.error('批量加载商家活动失败:', e)
  }
}

// 加载陶瓷工坊数据
const loadCeramicData = async () => {
  loading.value = true
  try {
    const userId = userStore.isLoggedIn ? userStore.user?.id : undefined
    const response = await getMerchantsByCategory('CERAMIC', userId)
    let merchants = []
    if (response?.success && Array.isArray(response.data)) {
      merchants = response.data
    } else if (Array.isArray(response)) {
      merchants = response
    } else if (response?.data && Array.isArray(response.data.data)) {
      merchants = response.data.data
    }
    ceramicList.value = (merchants || []).map(m => normalizeHotelRating({ ...m }))
    total.value = ceramicList.value.length
    // 后台异步附加活动信息，不阻塞列表渲染
    attachActivitiesToList(ceramicList.value)
  } catch (error) {
    console.error('加载陶瓷工坊推荐失败:', error)
    ceramicList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 查看陶瓷工坊详情
const viewCeramicDetail = (workshop) => {
  if (!workshop?.id) return
  router.push({ name: 'CeramicWorkshopDetail', params: { id: workshop.id } })
}

// 查看酒店详情
const viewHotelDetail = (hotel) => {
  if (!hotel?.id) {
    ElMessage.warning('暂时无法查看该酒店详情')
    return
  }
  router.push({ name: 'HotelDetail', params: { id: hotel.id } })
}

// 查看美食详情
const viewFoodDetail = (food) => {
  if (!food?.id) {
    ElMessage.warning('美食信息不完整，无法查看详情')
    return
  }
  router.push({ name: 'FoodDetail', params: { id: food.id } })
}

// 查看路线详情
const viewRouteDetail = (route) => {
  router.push({ name: 'RouteDetail', params: { id: route.id } })
}

// 使用路线规划行程
const useRouteForPlan = (route) => {
  if (!route?.id) {
    ElMessage.warning('路线信息不完整')
    return
  }
  router.push({ path: '/plan', query: { routeId: route.id } })
}

const setSort = async ({ field, order }) => {
  currentPage.value = 1
  if (!order) {
    sortField.value = ''
    sortOrder.value = ''
  } else {
    sortField.value = field
    sortOrder.value = order // asc(<) / desc(>)
  }
  router.replace({ query: { ...route.query, sortField: sortField.value, sortOrder: sortOrder.value, page: 1 } })
  saveFilterState()
  if (recommendType.value === 'scenic') {
    await loadRecommendData()
  }
}

const handleCurrentChange = async (val) => {
  currentPage.value = val
  router.replace({ query: { ...route.query, page: val } })
  saveFilterState()
  if (recommendType.value === 'scenic') {
    await loadRecommendData()
  }
}

const goPrevPage = () => {
  if (currentPage.value <= 1) return
  handleCurrentChange(currentPage.value - 1)
}

const goNextPage = () => {
  if (currentPage.value >= totalPages.value) return
  handleCurrentChange(currentPage.value + 1)
}

const loadRecommendData = async () => {
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      ...(sortField.value && sortOrder.value ? { sortField: sortField.value, sortOrder: sortOrder.value } : {})
    }

    // 清空之前的推荐数据
    scenicStore.recommendList = []
    
    const response = await scenicStore.fetchRecommendScenic(params)
    total.value = response?.total || scenicStore.recommendList.length

  // 防止页码越界
  if (currentPage.value > totalPages.value) {
      currentPage.value = totalPages.value
    }
  } catch (error) {
    console.error('加载推荐数据失败:', error)
    ElMessage.error('加载推荐数据失败')
  }
}

// 监听路由查询参数变化
watch(() => route.query.type, (newType) => {
  if (newType && ['scenic', 'food', 'route', 'hotel', 'ceramic'].includes(newType)) {
    recommendType.value = newType
    handleTypeChange()
  }
})

onMounted(() => {
  if (recommendType.value === 'scenic') {
    loadRecommendData()
  } else if (recommendType.value === 'food') {
    loadFoodData()
  } else if (recommendType.value === 'route') {
    loadRouteData()
  } else if (recommendType.value === 'hotel') {
    loadHotelData()
  } else if (recommendType.value === 'ceramic') {
    loadCeramicData()
  }
})
</script>

<style scoped>
.recommend {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.recommend-main {
  flex: 1;
  max-width: 1200px;
  margin: 24px auto 32px;
  padding: 0 16px;
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.recommend-content {
  min-width: 0;
  flex: 1;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  /* 移除多余的背景/边框样式 */
  padding: 2px 0 10px;
  border-radius: 0;
  background: transparent;
  border: none;
  box-shadow: none;
  backdrop-filter: none;
  gap: 18px;
}

.page-header h1 {
  font-size: 24px !important;
  color: #303133;
  margin: 0 0 8px 0;
  font-weight: 600;
}

.page-header p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

/* 美食卡片样式 */
.food-card {
  cursor: pointer;
  transition: transform 0.3s;
  margin-bottom: 12px;
}

.food-card:hover {
  transform: translateY(-5px);
}

.food-image {
  position: relative;
  width: 100%;
  height: 260px;
  overflow: hidden;
  border-radius: 4px;
  margin-bottom: 12px;
}

.food-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.food-rating {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
  padding: 8px;
  color: white;
  display: flex;
  align-items: center;
  gap: 8px;
}

.review-count {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.9);
}

.food-info h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: #303133;
}

.food-description {
  color: #606266;
  font-size: 14px;
  margin: 0 0 12px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.food-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.food-price {
  font-size: 16px;
  font-weight: bold;
  color: #e6a23c;
}

.food-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

/* 路线网格布局 3列×2行 */
.route-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

/* 路线详情弹窗头部 */
.route-detail-header {
  display: flex;
  gap: 20px;
  margin-bottom: 24px;
}

.route-detail-image {
  width: 300px;
  height: 200px;
  object-fit: cover;
  border-radius: 4px;
}

.route-detail-info {
  flex: 1;
}

.route-detail-description {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 16px;
}

.route-detail-meta {
  display: flex;
  gap: 12px;
  align-items: center;
}

.route-detail-price {
  font-size: 24px;
  font-weight: bold;
  color: #e6a23c;
}

.route-detail-spots {
  margin-top: 24px;
}

.route-detail-spots h4 {
  margin-bottom: 16px;
  color: #303133;
}

.route-detail-actions {
  margin-top: 24px;
  text-align: center;
}

/* 路线详情弹窗样式 */
.route-detail-dialog :deep(.el-dialog) {
  margin-top: 8vh !important;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
}

.route-detail-dialog :deep(.el-dialog__body) {
  max-height: calc(85vh - 120px);
  overflow-y: auto;
  padding: 20px;
}

.route-detail-dialog :deep(.el-dialog__header) {
  padding: 20px 20px 10px;
  border-bottom: 1px solid #ebeef5;
}

.route-detail-dialog :deep(.el-dialog__title) {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.results-header h2 {
  font-size: 24px;
  color: #303133;
  margin: 0;
}

.pagination {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  margin-top: 32px;
  color: #606266;
}

.pager-info {
  font-size: 14px;
}

.location-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.location-prompt {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
}

.location-hint {
  color: #909399;
  font-size: 14px;
}

/* 景点结果容器 */
.scenic-results-container {
  width: 100%;
}

/* 景点列表 - 使用 Grid，避免 flex-wrap 错行空白 */
.scenic-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
  align-items: stretch;
}

.scenic-grid-item {
  min-width: 0;
}

/* 统一卡片高度来源：由容器控制，卡片内部自适应填满 */
.scenic-grid :deep(.scenic-card-wrapper) {
  height: 420px;
  min-height: 420px;
}

.scenic-grid :deep(.scenic-card) {
  height: 100%;
}

@media (max-width: 992px) {
  .scenic-grid :deep(.scenic-card-wrapper) {
    height: 400px;
    min-height: 400px;
  }
}

/* 酒店卡片 */
.hotel-card {
  cursor: pointer;
  transition: transform 0.3s;
  margin-bottom: 12px;
  height: 100%;
}

.hotel-card:hover {
  transform: translateY(-5px);
}

.hotel-image {
  position: relative;
  width: 100%;
  height: 260px;
  overflow: hidden;
  border-radius: 4px;
  margin-bottom: 12px;
}

.hotel-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hotel-rating-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
  padding: 8px;
  color: white;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.hotel-rating-overlay .review-count {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.9);
}

.hotel-info h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: #303133;
}

.hotel-description {
  color: #606266;
  font-size: 14px;
  margin: 0 0 12px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.hotel-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  flex-wrap: wrap;
  gap: 8px;
}

.hotel-rating-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.review-count-text {
  font-size: 12px;
  color: #909399;
}

@media (max-width: 768px) {
  .recommend-main {
    grid-template-columns: 1fr;
    gap: 14px;
  }

  .results-header {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
  
  .filter-section .el-row .el-col {
    margin-bottom: 12px;
  }
  
  .hotel-meta {
    flex-direction: column;
    align-items: flex-start;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>
