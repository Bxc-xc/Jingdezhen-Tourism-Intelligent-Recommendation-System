<template>
  <div class="home">
    <Header />
    
    <!-- Hero Banner & Search -->
    <section class="hero-section">
      <el-carousel 
        height="500px" 
        indicator-position="outside" 
        arrow="hover" 
        :interval="5000"
        class="hero-carousel"
      >
        <el-carousel-item v-for="item in banners" :key="item.id">
          <div class="hero-item" :style="{ backgroundImage: `url(${item.image})` }">
            <div class="hero-overlay"></div>
            <div class="hero-gradient"></div>
          </div>
        </el-carousel-item>
      </el-carousel>
      
      <div class="hero-content">
        <div class="hero-text-bg">
          <h1 class="main-title">{{ $t('home.hero.title') }}</h1>
          <p class="sub-title">{{ $t('home.hero.subtitle') }}</p>
        </div>
        
        <!-- 搜索框悬浮在 Banner 上 -->
        <div class="search-container-hero">
          <el-input
            v-model="searchKeyword"
            :placeholder="$t('home.hero.searchPlaceholder')"
            size="large"
            class="hero-search-input"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon class="search-icon"><Search /></el-icon>
            </template>
            <template #suffix>
              <el-button type="primary" class="search-btn" @click="handleSearch">
                {{ $t('common.search') }}
              </el-button>
            </template>
          </el-input>
        </div>
      </div>

      <!-- 快捷导航栏 (悬浮在 Banner 底部) -->
      <div class="quick-access-bar">
        <div class="container">
          <div class="access-grid">
            <div class="access-item" @click="$router.push('/recommend?type=scenic')">
              <div class="access-icon scenic"><el-icon><Compass /></el-icon></div>
              <span class="access-title">{{ $t('home.access.scenic') }}</span>
              <span class="access-desc">{{ $t('home.access.scenicDesc') }}</span>
            </div>
            <div class="access-item" @click="$router.push('/recommend?type=food')">
              <div class="access-icon food"><el-icon><Food /></el-icon></div>
              <span class="access-title">{{ $t('home.access.food') }}</span>
              <span class="access-desc">{{ $t('home.access.foodDesc') }}</span>
            </div>
            <div class="access-item" @click="$router.push('/recommend?type=route')">
              <div class="access-icon route"><el-icon><MapLocation /></el-icon></div>
              <span class="access-title">{{ $t('home.access.route') }}</span>
              <span class="access-desc">{{ $t('home.access.routeDesc') }}</span>
            </div>
            <div class="access-item" @click="$router.push('/recommend?type=hotel')">
              <div class="access-icon hotel"><el-icon><OfficeBuilding /></el-icon></div>
              <span class="access-title">{{ $t('home.access.hotel') }}</span>
              <span class="access-desc">{{ $t('home.access.hotelDesc') }}</span>
            </div>
            <div class="access-item" @click="$router.push('/recommend?type=ceramic')">
              <div class="access-icon ceramic"><el-icon><Brush /></el-icon></div>
              <span class="access-title">{{ $t('home.access.ceramic') }}</span>
              <span class="access-desc">{{ $t('home.access.ceramicDesc') }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 悬浮侧边栏 -->
    <div class="floating-sidebar">
      <div class="float-btn" @click="scrollToTop" :title="$t('home.sidebar.top')">
        <el-icon><ArrowUp /></el-icon>
      </div>
      <div class="float-btn" @click="$router.push('/profile')" :title="$t('home.sidebar.profile')">
        <el-icon><User /></el-icon>
      </div>
      <div class="float-btn" @click="$router.push('/plan')" :title="$t('home.sidebar.plan')">
        <el-icon><Calendar /></el-icon>
      </div>
    </div>

    <!-- 热门景点 -->
    <section class="section scenic-section">
      <div class="container">
        <div class="section-header">
          <div class="header-left">
            <h2>{{ route.query.search ? $t('home.section.searchResult', { keyword: route.query.search }) : $t('home.section.popularScenic') }}</h2>
            <span class="header-subtitle">{{ $t('home.section.popularScenicSub') }}</span>
          </div>
          <el-button 
            v-if="route.query.search" 
            link
            class="view-more-btn"
            @click="clearSearch"
          >
            {{ $t('home.action.clearSearch') }} <el-icon><Close /></el-icon>
          </el-button>
          <el-button 
            v-else 
            link
            class="view-more-btn"
            @click="$router.push('/recommend?type=scenic')"
          >
            {{ $t('home.action.viewMore') }} <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
        
        <el-row :gutter="24" v-loading="scenicStore.loading || isSearching">
          <el-col
            v-for="item in displayScenicList"
            :key="item.id"
            :xs="24" :sm="12" :md="8" :lg="6"
          >
            <ScenicCard :scenic="item" />
          </el-col>
          <el-col v-if="displayScenicList.length === 0 && !scenicStore.loading && !isSearching" :span="24">
            <el-empty :description="$t('home.status.noScenic')" />
          </el-col>
        </el-row>
      </div>
    </section>

    <!-- 陶瓷体验 -->
    <section class="section ceramic-section" v-loading="loadingCeramic">
      <div class="container">
        <div class="section-header">
          <div class="header-left">
            <h2>{{ $t('home.section.ceramic') }}</h2>
            <span class="header-subtitle">{{ $t('home.section.ceramicSub') }}</span>
          </div>
          <el-button link class="view-more-btn" @click="$router.push('/recommend?type=food')">
            {{ $t('home.action.viewMore') }} <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
        
        <el-row :gutter="24" v-if="ceramicExperiences.length > 0">
          <el-col 
            :xs="24" 
            :sm="12" 
            :md="8" 
            v-for="item in ceramicExperiences" 
            :key="item.id"
          >
            <div class="official-card" @click="goToCeramicDetail(item)">
              <div class="card-image-wrapper">
                <img 
                  :src="item.image" 
                  :alt="item.title"
                  @error="handleImageError"
                />
              </div>
              <div class="card-content">
                <h3>{{ item.title }}</h3>
                <p class="description">{{ item.description }}</p>
                <div class="card-meta">
                  <span class="price" :class="{ 'free': item.price === '免费' || item.price === 'Free' }">
                    {{ item.price }}
                  </span>
                  <span class="duration">
                    <el-icon><Timer /></el-icon> {{ item.duration }}
                  </span>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
        
        <!-- 空状态 -->
        <el-empty 
          v-else-if="!loadingCeramic" 
          :description="$t('home.status.noCeramic')"
          :image-size="120"
        />
      </div>
    </section>

    <!-- 推荐景点 -->
    <section class="section recommend-section">
      <div class="container">
        <div class="section-header">
          <div class="header-left">
            <h2>{{ $t('home.section.recommend') }}</h2>
            <span class="header-subtitle">{{ $t('home.section.recommendSub') }}</span>
          </div>
          <el-button link class="view-more-btn" @click="$router.push('/recommend')">
            {{ $t('home.action.viewMore') }} <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
        
        <el-row :gutter="24" v-loading="loadingMixed">
          <el-col
            v-for="item in mixedRecommendList"
            :key="`${item.type}-${item.id}`"
            :xs="24" :sm="12" :md="8" :lg="6"
          >
            <ScenicCard :scenic="item" />
          </el-col>
        </el-row>
      </div>
    </section>

    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useScenicStore } from '../../stores/scenic'
import { ElMessage } from 'element-plus'
import Header from '../../components/Layout/Header.vue'
import Footer from '../../components/Layout/Footer.vue'
import ScenicCard from '../../components/ScenicCard.vue'
import { getMerchantsByCategory } from '../../api/merchant'
import { globalSearch } from '../../api/search'

const { t } = useI18n()
const router = useRouter()
const route = useRoute()
const scenicStore = useScenicStore()

const searchKeyword = ref('')
const isSearching = ref(false)
// 搜索关键词存在时，用于存放“相关商家”搜索结果
const merchantSearchResults = ref([])

// 陶瓷体验数据
const ceramicExperiences = ref([])
const loadingCeramic = ref(false)

const banners = ref([
  { id: 1, image: '/images/backgrounds/banner1.jpg' },
  { id: 2, image: '/images/backgrounds/banner8.jpg' },
  { id: 3, image: '/images/backgrounds/banner9.jpg' },
  { id: 4, image: '/images/backgrounds/bjt.jpg' },
  { id: 5, image: '/images/backgrounds/bjt2.jpg' },
  { id: 6, image: '/images/backgrounds/bt3.jpg' },
  { id: 7, image: '/images/backgrounds/bt6.jpg' },
  { id: 8, image: '/images/backgrounds/bt8.jpg' },
  { id: 9, image: '/images/backgrounds/bt11.jpg' },
  { id: 10, image: '/images/backgrounds/bt12.jpg' },
  { id: 11, image: '/images/backgrounds/login.jpg' },
])

const mixedRecommendList = ref([])
const loadingMixed = ref(false)

// 格式化商家数据为卡片格式（用于混合推荐区）
const formatMerchantToCard = (merchant, type) => {
  // 图片优先级：images 数组 > shopImages 字符串 > avatar > 随机占位图
  let image = ''
  if (Array.isArray(merchant.images) && merchant.images.length > 0) {
    image = merchant.images[0]
  } else if (merchant.shopImages) {
    const images = merchant.shopImages.split(',').map(s => s.trim()).filter(Boolean)
    if (images.length > 0) image = images[0]
  } else if (merchant.cover) {
    image = merchant.cover
  } else if (merchant.avatar) {
    image = merchant.avatar
  }
  // Fallback image
  if (!image) image = `https://picsum.photos/400/300?random=${merchant.id}`

  return {
    id: merchant.id,
    name: merchant.shopName,
    description: merchant.description,
    price: merchant.startPrice || merchant.price || 0,
    rating: merchant.rating || 5.0,
    image: image,
    type: type, // 'FOOD' or 'HOTEL'
    tags: merchant.tags || (type === 'FOOD' ? '美食' : '酒店'),
    qualificationApproved: merchant.qualificationApproved,
    activities: merchant.activities || [],
    // 餐饮商家专属字段
    cuisineType: merchant.cuisineType || null,
    avgPrice: merchant.avgPrice || null,
    // 酒店商家专属字段
    starRating: merchant.starRating || null,
    roomCount: merchant.roomCount || null
  }
}

// 获取混合推荐数据（景点 + 美食 + 酒店）
const fetchMixedRecommendations = async () => {
  loadingMixed.value = true
  try {
    const [scenicRes, foodRes, hotelRes] = await Promise.all([
      scenicStore.fetchRecommendScenic({ limit: 6 }), // 获取推荐景点
      getMerchantsByCategory('FOOD'),
      getMerchantsByCategory('HOTEL')
    ])

    // 处理景点数据
    const scenics = (scenicStore.recommendList || []).map(s => ({...s, type: 'SCENIC'}))
    
    // 处理美食数据
    let foods = []
    if (foodRes) {
      const list = Array.isArray(foodRes) ? foodRes : (foodRes.data || [])
      // 只取部分美食商家
      foods = list.slice(0, 3).map(m => formatMerchantToCard(m, 'FOOD'))
    }

    // 处理酒店数据
    let hotels = []
    if (hotelRes) {
      const list = Array.isArray(hotelRes) ? hotelRes : (hotelRes.data || [])
      // 只取部分酒店商家
      hotels = list.slice(0, 3).map(m => formatMerchantToCard(m, 'HOTEL'))
    }

    // 混合数据：简单的交叉混合算法
    const mixed = []
    const maxLength = Math.max(scenics.length, foods.length, hotels.length)
    
    for (let i = 0; i < maxLength; i++) {
      if (i < scenics.length) mixed.push(scenics[i])
      // 每隔一个景点插入一个非景点商家，增加多样性
      if (i < foods.length && i % 2 === 0) mixed.push(foods[i])
      if (i < hotels.length && i % 2 !== 0) mixed.push(hotels[i])
    }
    
    // 如果还有剩余的非景点商家，追加到后面
    if (foods.length > Math.ceil(maxLength/2)) {
      mixed.push(...foods.slice(Math.ceil(maxLength/2)))
    }
    if (hotels.length > Math.floor(maxLength/2)) {
      mixed.push(...hotels.slice(Math.floor(maxLength/2)))
    }

    // 限制总数量，保持页面整洁
    mixedRecommendList.value = mixed.slice(0, 12)
    
  } catch (error) {
    console.error('加载混合推荐数据失败:', error)
    // 降级处理：如果失败，至少显示景点
    mixedRecommendList.value = (scenicStore.recommendList || []).map(s => ({...s, type: 'SCENIC'}))
  } finally {
    loadingMixed.value = false
  }
}

// “热门景点 / 搜索结果”区域实际展示的数据源
const displayScenicList = computed(() => {
  return route.query.search ? merchantSearchResults.value : scenicStore.scenicList
})

// 将后端统一搜索结果扁平化为卡片列表
const flattenSearchResults = (data) => {
  const results = []
  const typeMap = {
    scenic: 'SCENIC',
    food: 'FOOD',
    hotel: 'HOTEL',
    ceramic: 'CERAMIC',
    marketplace: 'MARKETPLACE',
    route: 'ROUTE'
  }
  for (const [key, list] of Object.entries(data)) {
    const type = typeMap[key] || key.toUpperCase()
    for (const item of list) {
      const image = item.image || `https://picsum.photos/400/300?random=${item.id}`
      results.push({
        id: item.id,
        name: item.name,
        description: item.description || '',
        image,
        price: item.price ?? 0,
        rating: item.rating || 0,
        tags: item.tags,
        type,
        category: type,
        scenicSpotId: item.scenicSpotId,
        days: item.days,
        difficulty: item.difficulty
      })
    }
  }
  return results
}

// 全局模糊搜索：景点、陶瓷市集、陶瓷工坊、餐饮、酒店、路线
const handleSearch = async () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    ElMessage.warning(t('home.message.inputKeyword'))
    return
  }

  isSearching.value = true
  try {
    const res = await globalSearch(keyword)
    const data = res?.data || {}
    merchantSearchResults.value = flattenSearchResults(data)

    router.replace({ path: '/', query: { search: keyword } })

    if (merchantSearchResults.value.length === 0) {
      ElMessage.info(t('home.message.noResult'))
    } else {
      ElMessage.success(t('home.message.foundResult', { count: merchantSearchResults.value.length }))
    }
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error(t('home.message.searchFailed'))
  } finally {
    isSearching.value = false
  }
}

const clearSearch = async () => {
  searchKeyword.value = ''
  router.replace({ path: '/', query: {} })
  merchantSearchResults.value = []
  isSearching.value = true
  try {
    await scenicStore.fetchScenicList({ limit: 8 })
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    isSearching.value = false
  }
}

// 监听路由参数变化，处理从Header搜索框跳转过来的情况
watch(() => route.query.search, async (searchValue, oldValue) => {
  // 避免重复搜索相同的关键词
  if (searchValue === oldValue) return
  
  if (searchValue) {
    searchKeyword.value = searchValue
    await handleSearch()
  } else if (oldValue !== undefined) {
    // 只有当从有搜索参数变为无搜索参数时才重新加载（避免首次加载时重复）
    searchKeyword.value = ''
    isSearching.value = true
    try {
      await scenicStore.fetchScenicList({ limit: 8 })
    } catch (error) {
      console.error('加载数据失败:', error)
    } finally {
      isSearching.value = false
    }
  }
}, { immediate: false })

// 获取陶瓷体验数据
const fetchCeramicExperiences = async () => {
  loadingCeramic.value = true
  try {
    // 从后端获取CERAMIC分类的商家
    const res = await getMerchantsByCategory('CERAMIC')
    const list = res.data || res || []
    
    // 只取前3个用于首页展示
    const limitedList = list.slice(0, 3)
    
    // 转换数据格式
    ceramicExperiences.value = limitedList.map((merchant, index) => {
      // 处理图片：优先使用shopImages的第一张，其次使用avatar
      let image = merchant.avatar || ''
      if (merchant.shopImages) {
        const images = merchant.shopImages.split(',').map(s => s.trim()).filter(Boolean)
        if (images.length > 0) {
          image = images[0]
        }
      }
      
      // 如果没有图片，使用默认占位图
      if (!image) {
        image = `https://picsum.photos/400/200?random=${merchant.id || index + 1}`
      }
      
      // 格式化价格
      let price = '¥80'
      if (merchant.startPrice !== undefined && merchant.startPrice !== null) {
        if (merchant.startPrice === 0) {
          price = t('card.free')
        } else {
          price = `¥${merchant.startPrice}`
        }
      }
      
      return {
        id: merchant.id,
        merchantId: merchant.id,
        title: merchant.shopName || t('home.ceramicData.workshop.title'),
        description: merchant.description || t('home.ceramicData.workshop.desc'),
        image: image,
        price: price,
        duration: merchant.duration || '2-3小时',
        qualificationApproved: merchant.qualificationApproved
      }
    })
    
    // 如果后端没有数据，使用默认数据作为fallback
    if (ceramicExperiences.value.length === 0) {
      ceramicExperiences.value = [
        {
          id: 1,
          title: t('home.ceramicData.workshop.title'),
          description: t('home.ceramicData.workshop.desc'),
          image: 'https://picsum.photos/400/200?random=1',
          price: '¥120',
          duration: '2-3小时'
        },
        {
          id: 2,
          title: t('home.ceramicData.market.title'),
          description: t('home.ceramicData.market.desc'),
          image: 'https://picsum.photos/400/200?random=2',
          price: t('card.free'),
          duration: '1-2小时'
        },
        {
          id: 3,
          title: t('home.ceramicData.kiln.title'),
          description: t('home.ceramicData.kiln.desc'),
          image: 'https://picsum.photos/400/200?random=3',
          price: '¥80',
          duration: '1小时'
        }
      ]
    }
  } catch (error) {
    console.error('加载陶瓷体验数据失败:', error)
    // 出错时使用默认数据
    ceramicExperiences.value = [
      {
        id: 1,
        title: t('home.ceramicData.workshop.title'),
        description: t('home.ceramicData.workshop.desc'),
        image: 'https://picsum.photos/400/200?random=1',
        price: '¥120',
        duration: '2-3小时'
      },
      {
        id: 2,
        title: t('home.ceramicData.market.title'),
        description: t('home.ceramicData.market.desc'),
        image: 'https://picsum.photos/400/200?random=2',
        price: t('card.free'),
        duration: '1-2小时'
      },
      {
        id: 3,
        title: t('home.ceramicData.kiln.title'),
        description: t('home.ceramicData.kiln.desc'),
        image: 'https://picsum.photos/400/200?random=3',
        price: '¥80',
        duration: '1小时'
      }
    ]
  } finally {
    loadingCeramic.value = false
  }
}

// 图片加载错误处理
const handleImageError = (event) => {
  // 如果图片加载失败，使用默认占位图
  event.target.src = 'https://picsum.photos/400/200?random=ceramic'
}

// 跳转到陶瓷详情页
const goToCeramicDetail = (item) => {
  if (item.merchantId) {
    router.push(`/ceramic-workshop/${item.merchantId}`)
  } else {
    router.push('/ceramic-experience')
  }
}

const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

onMounted(async () => {
  try {
    // 从其他页面通过 Header 跳转到 /?search=xxx 时，watch(immediate:false) 不会在首次挂载触发
    // 这里显式执行一次搜索，确保导航栏关键词模糊搜索生效
    if (route.query.search) {
      searchKeyword.value = String(route.query.search)
      await handleSearch()
    } else {
      // 并行获取热门景点、推荐景点和陶瓷体验数据
      await Promise.all([
        scenicStore.fetchScenicList({ limit: 8 }),
        fetchMixedRecommendations(), // 获取混合推荐数据（替代原有的纯景点推荐）
        fetchCeramicExperiences() // 获取陶瓷体验数据
      ])
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  }
})
</script>

<style scoped>
.home {
  display: flex;
  flex-direction: column;
  background: #ffffff;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

/* ================= 方向B：功能导向 Hero ================= */
.hero-functional {
  background: linear-gradient(135deg, #1d4ed8 0%, #2563eb 50%, #4f46e5 100%);
  padding: 48px 24px 0;
  position: relative;
  overflow: hidden;
}

/* 背景图叠层 */
.hero-functional-bg {
  position: absolute;
  inset: 0;
  background: url('/images/backgrounds/banner1.jpg') center/cover no-repeat;
  opacity: 0.15;
  pointer-events: none;
  z-index: 0;
}

/* 背景装饰圆 */
.hero-functional::before {
  content: '';
  position: absolute;
  top: -60px;
  right: -60px;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.06);
  pointer-events: none;
}

.hero-functional::after {
  content: '';
  position: absolute;
  bottom: 60px;
  left: -80px;
  width: 220px;
  height: 220px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.04);
  pointer-events: none;
}

.hero-functional-inner {
  max-width: 720px;
  margin: 0 auto;
  text-align: center;
  position: relative;
  z-index: 1;
  padding-bottom: 40px;
}

.hf-title {
  font-size: 36px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 10px;
  letter-spacing: 2px;
}

.hf-sub {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.75);
  margin-bottom: 28px;
  letter-spacing: 1px;
}

.hf-search-wrap {
  max-width: 600px;
  margin: 0 auto;
}

.hf-search-input :deep(.el-input__wrapper) {
  background-color: #ffffff;
  border-radius: 50px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15) !important;
  padding: 4px 8px 4px 16px;
  border: none;
}

.hf-access-bar {
  position: relative;
  z-index: 2;
  padding: 0 24px;
  margin-top: -28px;
}

/* ================= Hero Section（旧，保留样式供参考） ================= */
.hero-section {
  position: relative;
  height: auto;
  width: 100%;
  margin-bottom: 60px;
  overflow: hidden;
}

.hero-carousel {
  position: relative;
}

.hero-item {
  height: 100%;
  width: 100%;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  position: relative;
  animation: zoomIn 20s ease-in-out infinite alternate;
}

@keyframes zoomIn {
  0% {
    transform: scale(1);
  }
  100% {
    transform: scale(1.1);
  }
}

.hero-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.2);
  z-index: 1;
}

.hero-gradient {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 60%;
  background: linear-gradient(
    to top,
    rgba(0, 0, 0, 0.6) 0%,
    rgba(0, 0, 0, 0.3) 50%,
    transparent 100%
  );
  z-index: 2;
}

.hero-content {
  position: absolute;
  top: 45%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  z-index: 10;
  width: 100%;
  max-width: 800px;
  padding: 0 20px;
}

.hero-text-bg {
  display: inline-block;
  padding: 35px 60px;
  border-radius: 20px;
  margin-bottom: 40px;
  transition: all 0.3s ease;
}

.hero-text-bg:hover {
  transform: translateY(-2px);
}

/* 快捷导航栏 */
.quick-access-bar {
  position: relative;
  margin-top: -60px; /* 负边距，使其覆盖在 Banner 下方 */
  z-index: 20;
  padding: 0 24px;
}

.access-grid {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0,0,0,0.08);
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  padding: 30px 20px;
  gap: 20px;
}

.access-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s;
  padding: 10px;
  border-radius: 8px;
}

.access-item:hover {
  background: #f9fafb;
  transform: translateY(-5px);
}

.access-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  margin-bottom: 12px;
  color: #fff;
  transition: all 0.3s;
}

.access-icon.scenic { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.access-icon.food { background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 99%, #fecfef 100%); }
.access-icon.route { background: linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%); }
.access-icon.hotel { background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%); }
.access-icon.ceramic { background: linear-gradient(135deg, #f6d365 0%, #fda085 100%); }

.access-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.access-desc {
  font-size: 12px;
  color: #999;
}

/* 悬浮侧边栏 */
.floating-sidebar {
  position: fixed;
  right: 30px;
  bottom: 100px;
  z-index: 999;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.float-btn {
  width: 50px;
  height: 50px;
  background: #fff;
  border-radius: 50%;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #606266;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #ebeef5;
}

.float-btn:hover {
  background: #059669;
  color: #fff;
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(5, 150, 105, 0.3);
}

.main-title {
  font-size: 56px;
  color: #fff;
  font-weight: 400;
  margin-bottom: 16px;
  letter-spacing: 4px;
  text-shadow: 0 2px 10px rgba(0,0,0,0.3), 0 0 20px rgba(255,255,255,0.2);
  font-family: "华文楷体", "STKaiti", "KaiTi", "SimKai", "FangSong", serif;
}

.sub-title {
  font-size: 20px;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 0;
  font-weight: 300;
  letter-spacing: 1px;
  font-family: "华文楷体", "STKaiti", "KaiTi", "SimKai", "FangSong", serif;
}

/* 轮播图淡入淡出效果 */
.hero-section :deep(.el-carousel__item) {
  transition: opacity 1.2s ease-in-out !important;
}

/* 轮播指示器样式优化 */
.hero-section :deep(.el-carousel__indicators) {
  bottom: 30px;
  z-index: 10;
}

.hero-section :deep(.el-carousel__indicator) {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  border: 2px solid rgba(255, 255, 255, 0.8);
  margin: 0 6px;
  transition: all 0.3s ease;
}

.hero-section :deep(.el-carousel__indicator:hover) {
  background: rgba(255, 255, 255, 0.8);
  transform: scale(1.2);
}

.hero-section :deep(.el-carousel__indicator.is-active) {
  width: 30px;
  border-radius: 5px;
  background: rgba(255, 255, 255, 0.95);
  border-color: rgba(255, 255, 255, 1);
  box-shadow: 0 0 10px rgba(255, 255, 255, 0.6);
}

.hero-section :deep(.el-carousel__indicator.is-active .el-carousel__button) {
  background: rgba(255, 255, 255, 0.95);
}

/* 轮播箭头样式优化 */
.hero-section :deep(.el-carousel__arrow) {
  width: 50px;
  height: 50px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  color: #fff;
  font-size: 20px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.hero-section :deep(.el-carousel__arrow:hover) {
  background: rgba(255, 255, 255, 0.35);
  border-color: rgba(255, 255, 255, 0.5);
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.25);
}

.hero-section :deep(.el-carousel__arrow--left) {
  left: 30px;
}

.hero-section :deep(.el-carousel__arrow--right) {
  right: 30px;
}

/* Hero Search Input - 升级版 */
.search-container-hero {
  max-width: 720px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.25);
  padding: 10px;
  border-radius: 100px;
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.search-container-hero:hover {
  background: rgba(255, 255, 255, 0.35);
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.2);
  border-color: rgba(255, 255, 255, 0.6);
}

.hero-search-input :deep(.el-input__wrapper) {
  background-color: #ffffff;
  border-radius: 50px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05) !important;
  padding: 4px 8px 4px 24px;
}

.hero-search-input :deep(.el-input__inner) {
  height: 52px;
  font-size: 16px;
  color: #333;
  letter-spacing: 0.5px;
}

.hero-search-input :deep(.el-input__inner)::placeholder {
  color: #9ca3af;
}

.search-icon {
  font-size: 22px;
  color: #409eff;
  margin-right: 12px;
  font-weight: bold;
}

.search-btn {
  height: 44px;
  border-radius: 40px;
  padding: 0 32px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #409eff 0%, #79bbff 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transition: all 0.3s;
  letter-spacing: 1px;
}

.search-btn:hover {
  background: linear-gradient(135deg, #337ecc 0%, #409eff 100%);
  transform: scale(1.05);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}

/* ================= Sections General ================= */
.section {
  padding: 80px 0;
  margin-bottom: 0; /* 确保没有多余的底部边距 */
}

/* 确保最后一个 section 紧贴 Footer */
.section:last-of-type {
  margin-bottom: 0;
  padding-bottom: 80px; /* 保持底部内边距 */
}

.container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 48px;
  border-bottom: 1px solid #f3f4f6;
  padding-bottom: 16px;
}

.header-left {
  display: flex;
  flex-direction: column;
}

.section-header h2 {
  font-size: 32px;
  color: #111827;
  font-weight: 700;
  margin: 0 0 8px 0;
  letter-spacing: -0.5px;
}

.header-subtitle {
  font-size: 12px;
  color: #6b7280;
  font-weight: 600;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.view-more-btn {
  font-size: 14px;
  color: #4b5563;
  display: flex;
  align-items: center;
  transition: color 0.3s;
}

.view-more-btn:hover {
  color: #059669;
}

/* ================= Specific Sections ================= */
.scenic-section {
  background-color: #fff;
}

.ceramic-section {
  background-color: #f9fafb; /* 浅灰背景区分 */
}

.recommend-section {
  background-color: #fff;
}

/* ================= Official Card Style (Ceramic) ================= */
.official-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  height: 100%;
  display: flex;
  flex-direction: column;
  margin-bottom: 24px;
}

.official-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
  border-color: #d1d5db;
}

.card-image-wrapper {
  height: 220px;
  width: 100%;
  overflow: hidden;
  background: #f3f4f6;
  border-bottom: 1px solid #f3f4f6;
}

.card-image-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.official-card:hover .card-image-wrapper img {
  transform: scale(1.05);
}

.card-content {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-content h3 {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 8px 0;
  line-height: 1.4;
}

.card-content .description {
  font-size: 14px;
  color: #6b7280;
  line-height: 1.6;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.card-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #f3f4f6;
}

.price {
  font-size: 20px;
  font-weight: 700;
  color: #059669;
}

.price.free {
  color: #059669;
}

.duration {
  font-size: 13px;
  color: #6b7280;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* ================= Responsive ================= */
@media (max-width: 768px) {
  .hero-section {
    height: 400px;
  }

  .hero-section :deep(.el-carousel) {
    height: 400px !important;
  }

  .hero-item {
    animation: none; /* 移动端禁用动画以提升性能 */
  }

  .hero-content {
    top: 40%;
    padding: 0 16px;
  }

  .hero-text-bg {
    padding: 20px 30px;
    border-radius: 16px;
  }

  .main-title {
    font-size: 28px;
    letter-spacing: 2px;
  }

  .sub-title {
    font-size: 14px;
  }

  .search-container-hero {
    max-width: 100%;
    padding: 8px;
  }

  .hero-section :deep(.el-carousel__arrow) {
    width: 40px;
    height: 40px;
    font-size: 16px;
  }

  .hero-section :deep(.el-carousel__arrow--left) {
    left: 10px;
  }

  .hero-section :deep(.el-carousel__arrow--right) {
    right: 10px;
  }

  .hero-section :deep(.el-carousel__indicators) {
    bottom: 20px;
  }

  .hero-section :deep(.el-carousel__indicator) {
    width: 8px;
    height: 8px;
    margin: 0 4px;
  }

  .hero-section :deep(.el-carousel__indicator.is-active) {
    width: 24px;
  }
  
  .section {
    padding: 40px 0;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .view-more-btn {
    align-self: flex-end;
  }
}
</style>
