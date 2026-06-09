<template>
  <!-- ================================================================
       NEW UI (重构版) — 如需回滚，删除此块，取消注释 OLD UI 块
       ================================================================ -->
  <div class="mpd-page">
    <Header />

    <div v-if="loading" class="mpd-loading">
      <el-skeleton :rows="8" animated style="max-width:900px;margin:40px auto;padding:0 20px" />
    </div>

    <div v-else-if="!marketplace" class="mpd-empty">
      <el-empty description="市集不存在" />
    </div>

    <template v-else>
      <!-- ── 主体内容 ── -->
      <div class="mpd-body">
        <div class="mpd-main">

          <!-- 返回按钮 -->
          <button class="mpd-back-btn-inline" @click="$router.go(-1)">
            <el-icon><ArrowLeft /></el-icon>
            <span>返回</span>
          </button>

          <!-- 标题 + 收藏 -->
          <div class="mpd-title-row">
            <h1 class="mpd-title">{{ marketplace.name }}</h1>
            <button class="mpd-fav-btn" :class="{ active: isFavorited }" @click="toggleFavorite">
              <el-icon><StarFilled v-if="isFavorited" /><Star v-else /></el-icon>
              {{ isFavorited ? '已收藏' : '收藏' }}
            </button>
          </div>

          <!-- 状态 + 营业时间 -->
          <div class="mpd-meta-row">
            <span class="mpd-badge mpd-badge--open" v-if="isOpen(marketplace.openTime)">营业中</span>
            <span class="mpd-badge mpd-badge--closed" v-else>已打烊</span>
            <span class="mpd-meta-item" v-if="marketplace.openTime">
              <el-icon><Clock /></el-icon>{{ marketplace.openTime }}
            </span>
            <span class="mpd-meta-item" v-if="marketplace.address">
              <el-icon><Location /></el-icon>{{ marketplace.address }}
            </span>
          </div>

          <!-- 图片区 -->
          <div class="mpd-gallery" v-if="imageList.length">
            <img class="mpd-gallery-main" :src="imageList[0]" :alt="marketplace.name" @click="openLightbox(0)" />
            <div class="mpd-gallery-thumbs" v-if="imageList.length > 1">
              <img
                v-for="(img, i) in imageList.slice(1, 4)"
                :key="i"
                :src="img"
                :alt="marketplace.name"
                class="mpd-gallery-thumb"
                @click="openLightbox(i + 1)"
              />
              <div v-if="imageList.length > 4" class="mpd-gallery-more" @click="openLightbox(3)">
                +{{ imageList.length - 4 }}
              </div>
            </div>
          </div>

          <!-- 分割线 -->
          <div class="mpd-divider"></div>

          <!-- 简介 -->
          <section class="mpd-section">
            <h2 class="mpd-section-title">关于市集</h2>
            <p class="mpd-desc">{{ marketplace.description || '暂无介绍' }}</p>
          </section>

          <!-- 标签 -->
          <section class="mpd-section" v-if="marketplace.crafts && marketplace.crafts.length">
            <h2 class="mpd-section-title">特色标签</h2>
            <div class="mpd-tags">
              <span v-for="c in marketplace.crafts" :key="c" class="mpd-tag">{{ c }}</span>
            </div>
          </section>

          <!-- 全部图片 -->
          <section class="mpd-section" v-if="imageList.length > 4">
            <h2 class="mpd-section-title">更多图片</h2>
            <div class="mpd-photo-grid">
              <img
                v-for="(img, i) in imageList"
                :key="i"
                :src="img"
                :alt="marketplace.name"
                class="mpd-photo-item"
                @click="openLightbox(i)"
              />
            </div>
          </section>

        </div>

        <!-- ── 右侧信息卡 ── -->
        <aside class="mpd-aside">
          <div class="mpd-info-card">
            <div class="mpd-info-row" v-if="marketplace.openTime">
              <el-icon class="mpd-info-icon"><Clock /></el-icon>
              <div>
                <div class="mpd-info-label">营业时间</div>
                <div class="mpd-info-val">{{ marketplace.openTime }}</div>
              </div>
            </div>
            <div class="mpd-info-row" v-if="marketplace.address">
              <el-icon class="mpd-info-icon"><Location /></el-icon>
              <div>
                <div class="mpd-info-label">地址</div>
                <div class="mpd-info-val">{{ marketplace.address }}</div>
              </div>
            </div>

            <!-- 地图 -->
            <div id="mpd-leaflet-map" class="mpd-map"></div>

            <!-- 导航按钮 -->
            <div class="mpd-nav-btns">
              <button class="mpd-nav-btn" @click="openMap('amap')">高德导航</button>
              <button class="mpd-nav-btn mpd-nav-btn--outline" @click="openMap('baidu')">百度地图</button>
            </div>
          </div>
        </aside>
      </div>
    </template>

    <!-- 灯箱 -->
    <div class="mpd-lightbox" v-if="lightboxIndex !== null" @click.self="lightboxIndex = null">
      <button class="mpd-lb-close" @click="lightboxIndex = null">✕</button>
      <button class="mpd-lb-prev" @click="lightboxIndex = (lightboxIndex - 1 + imageList.length) % imageList.length" v-if="imageList.length > 1">‹</button>
      <img class="mpd-lb-img" :src="imageList[lightboxIndex]" />
      <button class="mpd-lb-next" @click="lightboxIndex = (lightboxIndex + 1) % imageList.length" v-if="imageList.length > 1">›</button>
      <div class="mpd-lb-counter">{{ lightboxIndex + 1 }} / {{ imageList.length }}</div>
    </div>

    <Footer />
  </div>
  <!-- ================================================================
       END NEW UI
       ================================================================ -->
</template>

<script setup>
import { ref, computed, onMounted, nextTick, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Clock, Location, Star, StarFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import Header from '../../components/Layout/Header.vue'
import Footer from '../../components/Layout/Footer.vue'
import { getMarketplaceDetail, favoriteMarketplace, unfavoriteMarketplace, checkMarketplaceFavorite } from '../../api/marketplace'
import { useUserStore } from '../../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const marketplace = ref(null)
const isFavorited = ref(false)
const lightboxIndex = ref(null)
let map = null

const imageList = computed(() => {
  if (!marketplace.value) return []
  const m = marketplace.value
  let imgs = []
  if (m.carouselImages) {
    imgs = Array.isArray(m.carouselImages)
      ? m.carouselImages
      : m.carouselImages.split(',').map(s => s.trim()).filter(Boolean)
  }
  if (!imgs.length && m.coverImage) imgs.push(m.coverImage)
  return imgs
})

const openLightbox = (i) => { lightboxIndex.value = i }

const isOpen = (timeStr) => !!timeStr

const loadDetail = async () => {
  const id = route.params.id
  if (!id) { ElMessage.error('无效的市集ID'); return }
  loading.value = true
  try {
    const res = await getMarketplaceDetail(id)
    if (res.success || res.data) {
      marketplace.value = res.data || res
      if (!marketplace.value.crafts) {
        marketplace.value.crafts = ['陶瓷', '手工艺品', '创意市集']
      }
      if (userStore.isLoggedIn && marketplace.value.id) {
        try {
          const favRes = await checkMarketplaceFavorite(userStore.user.id, marketplace.value.id)
          isFavorited.value = !!(favRes?.success && favRes?.data === true)
        } catch {}
      }
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch {
    ElMessage.error('加载市集详情失败')
  } finally {
    loading.value = false
  }
}

const toggleFavorite = async () => {
  if (!userStore.isLoggedIn) { ElMessage.warning('请先登录'); router.push('/login'); return }
  const id = marketplace.value?.id
  if (!id) return
  try {
    if (isFavorited.value) {
      await unfavoriteMarketplace(userStore.user.id, id)
      isFavorited.value = false
      ElMessage.success('已取消收藏')
    } else {
      await favoriteMarketplace(userStore.user.id, id)
      isFavorited.value = true
      ElMessage.success('已收藏')
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '操作失败')
  }
}

const openMap = (type) => {
  if (!marketplace.value?.address) { ElMessage.warning('暂无地址信息'); return }
  const addr = encodeURIComponent(marketplace.value.address)
  const urls = {
    amap: `https://uri.amap.com/search?keyword=${addr}`,
    baidu: `http://api.map.baidu.com/geocoder?address=${addr}&output=html&src=webapp.baidu.com`
  }
  window.open(urls[type], '_blank')
}

const initMap = async () => {
  if (!marketplace.value) return

  const amapKey = import.meta.env.VITE_AMAP_KEY || '67aaaaa21d1edeceddaaf63fd6ad1ac4'

  // 优先用数据库里的精确坐标，没有才走地理编码
  let lat = marketplace.value.latitude ? Number(marketplace.value.latitude) : null
  let lng = marketplace.value.longitude ? Number(marketplace.value.longitude) : null

  if (!lat || !lng) {
    // fallback：高德地理编码
    lat = 29.293; lng = 117.207
    if (marketplace.value.address) {
      try {
        const geoRes = await fetch(
          `https://restapi.amap.com/v3/geocode/geo?key=${amapKey}&address=${encodeURIComponent('景德镇' + marketplace.value.address)}&city=景德镇`
        ).then(r => r.json())
        if (geoRes.status === '1' && geoRes.geocodes?.length) {
          const [lo, la] = geoRes.geocodes[0].location.split(',').map(Number)
          lng = lo; lat = la
        }
      } catch {}
    }
  }

  const loadAmap = () => new Promise((resolve, reject) => {
    if (window.AMap) { resolve(); return }
    const s = document.createElement('script')
    s.src = `https://webapi.amap.com/maps?v=2.0&key=${amapKey}`
    s.onload = resolve; s.onerror = reject
    document.head.appendChild(s)
  })

  try {
    await loadAmap()
    nextTick(() => {
      const el = document.getElementById('mpd-leaflet-map')
      if (!el) return
      if (map) map.destroy?.()
      map = new window.AMap.Map('mpd-leaflet-map', { center: [lng, lat], zoom: 16, resizeEnable: true })
      new window.AMap.Marker({ position: [lng, lat], title: marketplace.value.name }).addTo(map)
    })
  } catch {}
}

onMounted(async () => {
  await loadDetail()
  initMap()
})
onUnmounted(() => { if (map) map.destroy?.() })
</script>

<style scoped>
/* ================================================================
   NEW UI STYLES
   ================================================================ */

.mpd-page {
  min-height: 100vh;
  background: #f7f8fa;
  display: flex;
  flex-direction: column;
}

/* ── Loading / Empty ── */
.mpd-loading, .mpd-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

/* ── Body ── */
.mpd-body {
  max-width: 1100px;
  margin: 0 auto;
  padding: 32px 20px 60px;
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 32px;
  width: 100%;
  box-sizing: border-box;
}

/* ── Main ── */
.mpd-main {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.06);
}

.mpd-back-btn-inline {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  color: #999;
  font-size: 13px;
  cursor: pointer;
  padding: 0;
  margin-bottom: 20px;
  transition: color 0.2s;
}
.mpd-back-btn-inline:hover { color: #333; }

/* ── 内嵌图片区 ── */
.mpd-gallery {
  display: grid;
  grid-template-columns: 2fr 1fr;
  grid-template-rows: 160px 160px;
  gap: 4px;
  border-radius: 12px;
  overflow: hidden;
  margin: 20px 0;
}
.mpd-gallery-main {
  grid-row: 1 / 3;
  width: 100%;
  height: 100%;
  object-fit: cover;
  cursor: pointer;
  transition: opacity 0.2s;
}
.mpd-gallery-main:hover { opacity: 0.92; }
.mpd-gallery-thumbs {
  display: grid;
  grid-template-rows: repeat(2, 1fr);
  gap: 4px;
  position: relative;
}
.mpd-gallery-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
  cursor: pointer;
  transition: opacity 0.2s;
}
.mpd-gallery-thumb:hover { opacity: 0.85; }
.mpd-gallery-more {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.45);
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 0 0 12px 0;
}

.mpd-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}
.mpd-title {
  font-size: 26px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
  line-height: 1.3;
}
.mpd-fav-btn {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 7px 16px;
  border-radius: 20px;
  border: 1.5px solid #ddd;
  background: #fff;
  color: #666;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}
.mpd-fav-btn:hover { border-color: #f56c6c; color: #f56c6c; }
.mpd-fav-btn.active { border-color: #f56c6c; color: #f56c6c; background: #fff5f5; }

.mpd-meta-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
}
.mpd-badge {
  font-size: 12px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 12px;
}
.mpd-badge--open { background: #f0fdf4; color: #16a34a; }
.mpd-badge--closed { background: #f5f5f5; color: #999; }
.mpd-meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #666;
}

.mpd-divider {
  height: 1px;
  background: #f0f0f0;
  margin: 20px 0;
}

.mpd-section {
  margin-bottom: 32px;
}
.mpd-section-title {
  font-size: 16px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 14px;
  padding-left: 10px;
  border-left: 3px solid #e6a23c;
}
.mpd-desc {
  font-size: 15px;
  color: #555;
  line-height: 1.9;
  margin: 0;
}

.mpd-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.mpd-tag {
  padding: 5px 14px;
  border-radius: 20px;
  background: #fdf6ec;
  color: #b45309;
  font-size: 13px;
  border: 1px solid #fde68a;
}

.mpd-photo-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}
.mpd-photo-item {
  width: 100%;
  aspect-ratio: 4/3;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s, opacity 0.2s;
}
.mpd-photo-item:hover { transform: scale(1.02); opacity: 0.9; }

/* ── Aside ── */
.mpd-aside {
  position: sticky;
  top: 80px;
  align-self: start;
}
.mpd-info-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.06);
}
.mpd-info-row {
  display: flex;
  gap: 12px;
  margin-bottom: 18px;
}
.mpd-info-icon {
  font-size: 18px;
  color: #e6a23c;
  flex-shrink: 0;
  margin-top: 2px;
}
.mpd-info-label {
  font-size: 11px;
  color: #999;
  margin-bottom: 2px;
}
.mpd-info-val {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
}
.mpd-map {
  width: 100%;
  height: 200px;
  border-radius: 10px;
  overflow: hidden;
  margin: 16px 0 14px;
  border: 1px solid #eee;
}
.mpd-nav-btns {
  display: flex;
  gap: 8px;
}
.mpd-nav-btn {
  flex: 1;
  padding: 9px 0;
  border-radius: 8px;
  border: none;
  background: #e6a23c;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}
.mpd-nav-btn:hover { background: #cf8a1f; }
.mpd-nav-btn--outline {
  background: #fff;
  color: #e6a23c;
  border: 1.5px solid #e6a23c;
}
.mpd-nav-btn--outline:hover { background: #fdf6ec; }

/* ── Lightbox ── */
.mpd-lightbox {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.92);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
}
.mpd-lb-img {
  max-width: 90vw;
  max-height: 85vh;
  object-fit: contain;
  border-radius: 4px;
}
.mpd-lb-close {
  position: absolute;
  top: 20px;
  right: 24px;
  background: none;
  border: none;
  color: #fff;
  font-size: 28px;
  cursor: pointer;
  opacity: 0.8;
}
.mpd-lb-close:hover { opacity: 1; }
.mpd-lb-prev, .mpd-lb-next {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(255,255,255,0.15);
  border: none;
  color: #fff;
  font-size: 36px;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}
.mpd-lb-prev { left: 20px; }
.mpd-lb-next { right: 20px; }
.mpd-lb-prev:hover, .mpd-lb-next:hover { background: rgba(255,255,255,0.3); }
.mpd-lb-counter {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  color: rgba(255,255,255,0.7);
  font-size: 13px;
}

/* ── 响应式 ── */
@media (max-width: 768px) {
  .mpd-body { grid-template-columns: 1fr; }
  .mpd-hero-gallery { grid-template-columns: 1fr; grid-template-rows: 260px; }
  .mpd-hero-thumbs { display: none; }
  .mpd-photo-grid { grid-template-columns: repeat(2, 1fr); }
  .mpd-aside { position: static; }
}

/* ================================================================
   END NEW UI STYLES
   ================================================================ */
</style>
