<template>
  <div class="ceramic-experience">
    <Header />

    <div class="container">
      <!-- 返回键 -->
      <div class="back-button-container">
        <el-button @click="goBack" :icon="ArrowLeft" circle />
      </div>

      <!-- ===== 第一板块：陶瓷市集 ===== -->
      <div class="section-container market-section">
        <div class="section-header">
          <div class="section-badge">陶瓷文化</div>
          <h2>{{ $t('ceramic.marketplace.title') }}</h2>
          <p>{{ $t('ceramic.marketplace.subtitle') }}</p>
        </div>

        <!-- 市集卡片网格 -->
        <div v-loading="marketplaceLoading" class="market-grid">
          <div
            v-for="(item, index) in filteredMarketplaces"
            :key="item.id"
            class="market-card"
            :class="{ 'market-card--featured': index === 0 }"
            @click="viewMarketplaceDetail(item)"
          >
            <div class="card-img-wrap">
              <img :src="item.image" :alt="item.name" @error="handleImgError" />
              <!-- 渐变遮罩 -->
              <div class="card-overlay"></div>
              <!-- 序号标签 -->
              <div class="card-index">{{ String(index + 1).padStart(2, '0') }}</div>
              <!-- 收藏按钮 -->
              <button
                class="fav-btn"
                :class="{ favorited: isFavorited(item.id) }"
                @click.stop="toggleFavorite(item)"
                :title="isFavorited(item.id) ? '取消收藏' : '收藏'"
              >
                <span v-if="isFavorited(item.id)">❤️</span>
                <span v-else>🤍</span>
              </button>
              <!-- 图片底部信息（悬浮时显示） -->
              <div class="card-img-footer">
                <span v-if="item.openTime" class="img-meta">
                  <el-icon><Clock /></el-icon> {{ item.openTime }}
                </span>
              </div>
            </div>
            <div class="card-body">
              <h3 class="card-name">{{ item.name }}</h3>
              <p class="card-desc">{{ item.description || '暂无描述' }}</p>
              <div class="card-footer">
                <span v-if="item.address" class="meta-item addr">
                  <el-icon><Location /></el-icon>
                  <span class="addr-text">{{ item.address }}</span>
                </span>
                <el-button type="warning" size="small" round @click.stop="viewMarketplaceDetail(item)">
                  探索 →
                </el-button>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-if="!marketplaceLoading && filteredMarketplaces.length === 0" class="empty-state">
            <el-empty description="暂无市集数据" />
          </div>
        </div>
      </div>

      <!-- ===== 第二板块：陶艺 DIY 工坊 ===== -->
      <div class="section-container workshop-section">
        <div class="section-bg-decoration"></div>
        <div class="section-header">
          <h2>{{ $t('ceramic.workshop.title') }}</h2>
          <p>{{ $t('ceramic.workshop.subtitle') }}</p>
        </div>

        <div class="workshop-intro-visual">
          <div class="intro-left">
            <div class="intro-image-wrapper">
              <video
                :src="videoPath"
                class="main-intro-img"
                controls
                controlsList="nodownload"
                loop
                preload="metadata"
              >
                您的浏览器不支持视频播放。
              </video>
            </div>
          </div>

          <div class="intro-right">
            <h3>{{ $t('ceramic.workshop.about') }}</h3>
            <p class="intro-desc">{{ $t('ceramic.workshop.aboutDesc') }}</p>

            <div class="intro-features-grid">
              <div class="visual-feature-card" v-for="feature in workshopFeatures" :key="feature.title">
                <div class="feature-icon-wrapper">
                  <el-icon>
                    <User v-if="feature.icon === 'User'" />
                    <Tools v-else-if="feature.icon === 'Tools'" />
                    <Trophy v-else-if="feature.icon === 'Trophy'" />
                    <MagicStick v-else-if="feature.icon === 'MagicStick'" />
                  </el-icon>
                </div>
                <h4>{{ feature.title }}</h4>
                <p>{{ feature.description }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 推荐工坊列表 -->
      <div class="workshop-list">
        <h3 class="subsection-title">{{ $t('ceramic.workshop.recommended') }}</h3>
        <el-row :gutter="20">
          <el-col
            v-for="workshop in recommendedWorkshops"
            :key="workshop.id"
            :xs="24" :sm="12" :md="8"
          >
            <el-card class="workshop-card" shadow="hover" :body-style="{ padding: '0px' }">
              <div class="workshop-card-wrapper">
                <div class="workshop-card-image">
                  <img :src="workshop.image" :alt="workshop.name" @click="viewWorkshopDetail(workshop)" />
                </div>
                <div class="workshop-card-content">
                  <div class="workshop-card-title-row">
                    <h4>{{ workshop.name }}</h4>
                    <div style="display: flex; gap: 5px;">
                      <el-tag v-if="workshop.qualificationApproved" type="success" size="small" effect="plain">
                        {{ $t('ceramic.workshop.qualificationApproved') }}
                      </el-tag>
                      <el-tag v-if="workshop.activities && workshop.activities.length > 0" type="danger" size="small" effect="plain">
                        {{ $t('card.promotion') }}
                      </el-tag>
                    </div>
                  </div>
                  <p>{{ workshop.description }}</p>
                  <div class="workshop-card-meta">
                    <span class="price">¥{{ workshop.price }}</span>
                    <span class="duration">{{ workshop.duration }}</span>
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </div>

    <Footer />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ArrowLeft, Location, Clock, User, Tools, Trophy, MagicStick, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'
import { getMerchantsByCategory } from '../../api/merchant'
import { getMerchantActivities } from '../../api/merchantActivity'
import {
  getPublicMarketplaces,
  favoriteMarketplace,
  unfavoriteMarketplace,
  checkMarketplaceFavorite,
  getMarketplaceFavorites
} from '../../api/marketplace'
import Header from '../../components/Layout/Header.vue'
import Footer from '../../components/Layout/Footer.vue'

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()

const videoPath = '/media/ceramic-tutorial.mp4'

// ===== 市集数据 =====
const ceramicMarketplaces = ref([])
const marketplaceLoading = ref(false)
const searchKeyword = ref('')
const activeCategory = ref('all')

const categories = [
  { label: '全部', value: 'all' },
  { label: '陶瓷摆件', value: '陶瓷摆件' },
  { label: '茶具', value: '茶具' },
  { label: '手工艺品', value: '手工艺品' },
]

// ===== 收藏状态 =====
// key: marketplaceId, value: boolean
const favoritedMap = ref({})

const isFavorited = (id) => !!favoritedMap.value[id]

// 初始化收藏状态（批量）
const initFavoriteStatus = async (list) => {
  if (!userStore.isLoggedIn || !list.length) return
  try {
    // 优先用列表接口批量获取
    const res = await getMarketplaceFavorites(userStore.user.id)
    const favList = res?.data || []
    const favIds = new Set(favList.map(f => f.id || f.marketplaceId))
    list.forEach(item => {
      favoritedMap.value[item.id] = favIds.has(item.id)
    })
  } catch {
    // 降级：逐个检查
    await Promise.allSettled(
      list.map(async (item) => {
        try {
          const r = await checkMarketplaceFavorite(userStore.user.id, item.id)
          favoritedMap.value[item.id] = !!(r?.success && r?.data === true)
        } catch { /* ignore */ }
      })
    )
  }
}

const toggleFavorite = async (item) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再收藏')
    router.push('/login')
    return
  }
  const id = item.id
  const current = isFavorited(id)
  try {
    if (current) {
      await unfavoriteMarketplace(userStore.user.id, id)
      favoritedMap.value[id] = false
      ElMessage.success('已取消收藏')
    } else {
      await favoriteMarketplace(userStore.user.id, id)
      favoritedMap.value[id] = true
      ElMessage.success('已添加到收藏')
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '操作失败')
  }
}

// ===== 筛选 =====
const filteredMarketplaces = computed(() => {
  let list = ceramicMarketplaces.value
  const kw = searchKeyword.value.trim().toLowerCase()
  if (kw) {
    list = list.filter(m =>
      (m.name || '').toLowerCase().includes(kw) ||
      (m.description || '').toLowerCase().includes(kw)
    )
  }
  if (activeCategory.value !== 'all') {
    list = list.filter(m =>
      (m.crafts || []).includes(activeCategory.value) ||
      (m.description || '').includes(activeCategory.value)
    )
  }
  return list
})

// ===== 加载市集 =====
const loadMarketplaces = async () => {
  marketplaceLoading.value = true
  try {
    const res = await getPublicMarketplaces()
    const list = res?.data || []
    ceramicMarketplaces.value = (Array.isArray(list) ? list : []).map((m) => {
      const images = (m.carouselImages || '').split(',').map(s => s.trim()).filter(Boolean)
      const cover = m.coverImage || images[0] || `https://picsum.photos/400/250?random=${m.id}`
      return {
        id: m.id,
        name: m.name,
        description: m.description || '',
        address: m.address || '',
        openTime: m.openTime || '',
        crafts: [],
        image: cover,
        images: images.length ? images : [cover]
      }
    })
    await initFavoriteStatus(ceramicMarketplaces.value)
  } catch (e) {
    console.error('加载陶瓷市集失败:', e)
    ElMessage.error('加载陶瓷市集失败')
    ceramicMarketplaces.value = []
  } finally {
    marketplaceLoading.value = false
  }
}

// ===== 工坊 =====
const workshopFeatures = computed(() => [
  { title: t('ceramic.workshop.features.professional.title'), description: t('ceramic.workshop.features.professional.desc'), icon: 'User' },
  { title: t('ceramic.workshop.features.tools.title'), description: t('ceramic.workshop.features.tools.desc'), icon: 'Tools' },
  { title: t('ceramic.workshop.features.firing.title'), description: t('ceramic.workshop.features.firing.desc'), icon: 'Trophy' },
  { title: t('ceramic.workshop.features.creative.title'), description: t('ceramic.workshop.features.creative.desc'), icon: 'MagicStick' }
])

const recommendedWorkshops = ref([])

const loadWorkshops = async () => {
  try {
    const res = await getMerchantsByCategory('CERAMIC')
    const list = res.data || res || []
    const shuffled = list.sort(() => Math.random() - 0.5).slice(0, 10)
    recommendedWorkshops.value = shuffled.map(merchant => {
      let image = merchant.avatar || ''
      if (merchant.shopImages) {
        const first = merchant.shopImages.split(',').map(s => s.trim()).filter(Boolean)[0]
        if (first) image = first
      }
      return {
        id: merchant.id,
        merchantId: merchant.id,
        name: merchant.shopName,
        description: merchant.description || '优质陶艺体验工坊',
        qualificationApproved: merchant.qualificationApproved,
        price: merchant.startPrice || 99,
        duration: merchant.duration || '2-3小时',
        image,
        activities: []
      }
    })
    await attachActivitiesToList(recommendedWorkshops.value)
  } catch (e) {
    console.error('加载陶瓷工坊失败:', e)
  }
}

const attachActivitiesToList = async (list) => {
  if (!Array.isArray(list) || list.length === 0) return
  await Promise.allSettled(
    list.map(async (item) => {
      if (!item?.merchantId) return
      try {
        const res = await getMerchantActivities(item.merchantId)
        item.activities = res?.success ? (res.data || []) : (Array.isArray(res) ? res : [])
      } catch { item.activities = [] }
    })
  )
}

const handleImgError = (e) => {
  e.target.src = 'https://picsum.photos/400/250?random=marketplace'
}

const viewMarketplaceDetail = (item) => {
  if (!item?.id) return
  router.push({ name: 'MarketplaceDetail', params: { id: item.id } })
}

const viewWorkshopDetail = (workshop) => {
  if (!workshop?.id) return
  router.push({ name: 'CeramicWorkshopDetail', params: { id: workshop.id } })
}

const goBack = () => router.go(-1)

onMounted(() => {
  loadMarketplaces()
  loadWorkshops()
})
</script>

<style scoped>
/* ===== 基础布局 ===== */
.ceramic-experience {
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

.section-container {
  margin-bottom: 60px;
}

.section-header {
  text-align: center;
  margin-bottom: 30px;
}

.section-header h2 {
  font-size: 32px;
  color: #303133;
  margin-bottom: 12px;
  font-weight: 600;
  font-family: "Noto Serif SC", "SimSun", "Songti SC", serif;
}

.section-header h2::before,
.section-header h2::after {
  content: '';
  display: inline-block;
  width: 24px;
  height: 24px;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%23e6a23c'%3E%3Cpath d='M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.54c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z'/%3E%3C/svg%3E");
  background-size: contain;
  background-repeat: no-repeat;
  vertical-align: middle;
  opacity: 0.6;
}

.section-header h2::before { margin-right: 12px; }
.section-header h2::after { margin-left: 12px; transform: scaleX(-1); }

.section-header p {
  font-size: 16px;
  color: #909399;
}

/* ===== 市集板块背景 ===== */
.market-section {
  background: linear-gradient(135deg, #fffdf7 0%, #fff8ee 50%, #fffdf7 100%);
  border-radius: 24px;
  padding: 48px 40px;
  border: 1px solid rgba(230, 162, 60, 0.12);
  box-shadow: 0 4px 24px rgba(230, 162, 60, 0.06);
}

/* 板块徽章 */
.section-badge {
  display: inline-block;
  padding: 4px 14px;
  background: linear-gradient(90deg, #e6a23c, #f0c060);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  border-radius: 20px;
  letter-spacing: 1px;
  margin-bottom: 12px;
}

/* ===== 工具栏：搜索 + 分类 ===== */
.market-toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;
  flex-wrap: wrap;
  background: #fff;
  padding: 14px 20px;
  border-radius: 14px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.market-search {
  width: 260px;
}

.category-tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.cat-tab {
  padding: 6px 18px;
  border-radius: 20px;
  font-size: 13px;
  cursor: pointer;
  border: 1.5px solid #ebeef5;
  color: #606266;
  background: #fafafa;
  transition: all 0.2s;
  user-select: none;
  font-weight: 500;
}

.cat-tab:hover {
  border-color: #e6a23c;
  color: #e6a23c;
  background: #fffbf0;
}

.cat-tab.active {
  background: linear-gradient(90deg, #e6a23c, #f0c060);
  border-color: transparent;
  color: #fff;
  box-shadow: 0 3px 10px rgba(230, 162, 60, 0.35);
}

/* ===== 市集卡片网格 ===== */
.market-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  min-height: 200px;
}

.market-card {
  background: #fff;
  border-radius: 18px;
  overflow: hidden;
  border: 1px solid rgba(230, 162, 60, 0.1);
  cursor: pointer;
  transition: transform 0.3s cubic-bezier(0.25, 0.8, 0.25, 1), box-shadow 0.3s ease;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
}

.market-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 40px rgba(230, 162, 60, 0.18);
  border-color: rgba(230, 162, 60, 0.3);
}

/* 首张卡片特殊样式（精选） */
.market-card--featured {
  border-color: rgba(230, 162, 60, 0.25);
  box-shadow: 0 6px 24px rgba(230, 162, 60, 0.12);
}

/* 图片区域 */
.card-img-wrap {
  position: relative;
  width: 100%;
  padding-top: 62%;
  overflow: hidden;
  background: #f3f4f6;
}

.card-img-wrap img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.market-card:hover .card-img-wrap img {
  transform: scale(1.07);
}

/* 渐变遮罩 */
.card-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to bottom,
    transparent 40%,
    rgba(0, 0, 0, 0.45) 100%
  );
  z-index: 1;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.market-card:hover .card-overlay {
  opacity: 1;
}

/* 序号标签 */
.card-index {
  position: absolute;
  top: 12px;
  left: 12px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
  color: #e6a23c;
  z-index: 2;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 收藏按钮 */
.fav-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(4px);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
  transition: transform 0.2s, box-shadow 0.2s;
  z-index: 3;
}

.fav-btn:hover {
  transform: scale(1.18);
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.18);
}

.fav-btn.favorited {
  background: rgba(255, 235, 235, 0.95);
}

/* 图片底部信息（hover 时从下方滑入） */
.card-img-footer {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 10px 14px;
  z-index: 2;
  transform: translateY(100%);
  transition: transform 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.market-card:hover .card-img-footer {
  transform: translateY(0);
}

.img-meta {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.9);
}

/* 卡片内容 */
.card-body {
  padding: 18px 18px 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.card-name {
  font-size: 17px;
  font-weight: 700;
  color: #303133;
  margin: 0;
  font-family: "Noto Serif SC", "SimSun", serif;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-desc {
  font-size: 13px;
  color: #909399;
  margin: 0;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.card-footer {
  margin-top: 4px;
  padding-top: 12px;
  border-top: 1px solid #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.meta-item.addr {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #b0b3b8;
  flex: 1;
  min-width: 0;
}

.addr-text {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.empty-state {
  grid-column: 1 / -1;
  display: flex;
  justify-content: center;
  padding: 60px 0;
}

/* ===== 工坊板块（保留原样式） ===== */
.workshop-section {
  position: relative;
  padding: 40px 0;
  overflow: hidden;
}

.section-bg-decoration {
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 100%;
  z-index: 0;
  background-image:
    radial-gradient(circle at 10% 20%, rgba(64, 158, 255, 0.03) 0%, transparent 20%),
    radial-gradient(circle at 90% 80%, rgba(230, 162, 60, 0.03) 0%, transparent 20%);
  pointer-events: none;
}

.workshop-intro-visual {
  display: flex;
  gap: 40px;
  align-items: center;
  position: relative;
  z-index: 1;
  margin-bottom: 50px;
}

.intro-left { flex: 1; position: relative; }

.intro-image-wrapper {
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 20px 20px 60px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.intro-image-wrapper:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.16);
}

.main-intro-img {
  width: 100%;
  height: 400px;
  object-fit: cover;
  display: block;
  background: #000;
}

.intro-right { flex: 1.2; }

.intro-desc {
  font-size: 15px;
  color: #606266;
  line-height: 1.8;
  margin-bottom: 25px;
  max-width: 600px;
}

.intro-features-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-top: 20px;
}

.visual-feature-card {
  background: #fff;
  padding: 20px;
  border-radius: 16px;
  border: 1px solid #f0f2f5;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.visual-feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
  border-color: #e6a23c;
}

.feature-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: #fffbf0;
  color: #e6a23c;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 15px;
  font-size: 24px;
}

.visual-feature-card h4 {
  margin: 0 0 8px;
  font-size: 16px;
  color: #303133;
  font-family: "Noto Serif SC", serif;
}

.visual-feature-card p {
  margin: 0;
  font-size: 13px;
  color: #909399;
  line-height: 1.6;
}

/* ===== 推荐工坊 ===== */
.workshop-list { margin-top: 20px; }

.subsection-title {
  font-size: 20px;
  color: #303133;
  margin-bottom: 16px;
}

.workshop-card {
  border-radius: 14px;
  overflow: visible;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.04);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  border: 1px solid transparent;
  margin-bottom: 20px;
}

.workshop-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.08);
  border-color: rgba(230, 162, 60, 0.3);
}

.workshop-card-wrapper { display: flex; flex-direction: column; }

.workshop-card-image {
  height: 200px;
  width: 100%;
  overflow: hidden;
}

.workshop-card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  cursor: pointer;
  display: block;
  transition: transform 0.25s ease;
}

.workshop-card:hover .workshop-card-image img {
  transform: scale(1.03);
}

.workshop-card-content {
  padding: 15px;
  flex: 1;
}

.workshop-card-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 6px;
}

.workshop-card-title-row h4 {
  margin: 0;
  font-size: 15px;
  color: #303133;
}

.workshop-card-content p {
  font-size: 13px;
  color: #909399;
  margin: 0 0 10px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.workshop-card-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  color: #606266;
}

.workshop-card-meta .price {
  color: #e6212a;
  font-weight: 700;
  font-size: 16px;
}

.workshop-card-meta .duration {
  color: #909399;
}
</style>
