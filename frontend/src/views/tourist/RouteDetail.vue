<template>
  <div class="route-detail-page">
    <Header />

    <div class="container" v-loading="loading">
      <div class="back-button-container">
        <el-button @click="goBack" :icon="ArrowLeft" circle />
      </div>

      <el-empty v-if="!loading && !routeData" description="未找到该路线信息" />

      <div v-else-if="routeData">
        <!-- 顶部信息栏 -->
        <div class="header-section">
          <div class="info-wrapper">
            <div class="title-row">
              <h1 class="route-title">{{ routeData.name }}</h1>
              <el-tag type="primary">{{ routeData.days }}天{{ routeData.days > 1 ? (routeData.days - 1) + '晚' : '' }}</el-tag>
              <el-tag type="info" v-if="routeData.difficulty">{{ routeData.difficulty }}</el-tag>
            </div>
            <p class="route-desc" v-if="routeData.description">{{ routeData.description }}</p>
            <div class="meta-row">
              <span class="meta-item">
                <el-icon><Location /></el-icon>
                {{ routeData.scenicSpots?.length || 0 }} 个景点
              </span>
              <span class="meta-item">
                <el-icon><Wallet /></el-icon>
                {{ routeData.totalPrice > 0 ? `约 ¥${routeData.totalPrice}` : '免费游览' }}
              </span>
              <span class="meta-item" v-if="routeData.tags?.length">
                <el-icon><CollectionTag /></el-icon>
                {{ routeData.tags.join(' · ') }}
              </span>
            </div>
          </div>
          <div class="header-cta">
            <div class="price-display" v-if="routeData.totalPrice > 0">
              <span class="yen">¥</span>{{ routeData.totalPrice }}<span class="suffix">/人参考</span>
            </div>
            <div class="price-display free" v-else>免费游览</div>
            <el-button type="primary" size="large" class="cta-btn" @click="useRouteForPlan">
              <el-icon><Calendar /></el-icon>
              用此路线规划行程
            </el-button>
          </div>
        </div>

        <!-- 主内容：左右分栏 -->
        <div class="content-section">
          <!-- 左侧：封面图 + 行程时间线 -->
          <div class="left-column">
            <!-- 封面图 -->
            <el-card class="detail-card cover-card" shadow="hover" :body-style="{ padding: 0 }">
              <img :src="getRouteImage(routeData)" :alt="routeData.name" class="cover-image" />
            </el-card>

            <!-- 行程时间线 -->
            <el-card
              v-for="(dayGroup, di) in groupedByDay"
              :key="di"
              class="detail-card day-card"
              shadow="hover"
            >
              <template #header>
                <div class="day-header">
                  <span class="day-badge">Day {{ dayGroup.day }}</span>
                  <span class="day-title">第 {{ dayGroup.day }} 天行程</span>
                  <span class="day-count">共 {{ dayGroup.spots.length }} 站</span>
                </div>
              </template>

              <div class="spots-timeline">
                <div
                  v-for="(spot, si) in dayGroup.spots"
                  :key="si"
                  class="spot-item"
                >
                  <div class="timeline-left">
                    <div class="tl-dot"></div>
                    <div class="tl-line" v-if="si < dayGroup.spots.length - 1"></div>
                  </div>
                  <div class="spot-content">
                    <div class="spot-title-row">
                      <span class="spot-time" v-if="spot.timeRange">{{ spot.timeRange }}</span>
                      <h3 class="spot-name clickable" @click="goToSpot(spot)">{{ spot.name }}</h3>
                      <el-tag
                        size="small"
                        :type="spot.cost === 'free' || spot.cost === '免费' ? 'success' : 'warning'"
                        effect="plain"
                      >
                        {{ spot.cost === 'free' || spot.cost === '免费' ? '免费' : spot.cost || '免费' }}
                      </el-tag>
                    </div>
                    <div class="spot-meta" v-if="spot.duration || spot.transport">
                      <el-tag size="small" type="info" effect="plain" v-if="spot.duration">
                        <el-icon><Timer /></el-icon> {{ spot.duration }}
                      </el-tag>
                      <el-tag size="small" type="primary" effect="plain" v-if="spot.transport">
                        <el-icon><Van /></el-icon> {{ spot.transport }}
                      </el-tag>
                    </div>
                    <div class="spot-tip" v-if="spot.tips">
                      <el-icon><InfoFilled /></el-icon>
                      {{ spot.tips }}
                    </div>
                    <div class="sub-spots" v-if="spot.subSpots?.length">
                      <el-tag
                        v-for="(sub, ssi) in spot.subSpots"
                        :key="ssi"
                        size="small"
                        type="info"
                        class="sub-tag"
                      >{{ ssi + 1 }}. {{ sub }}</el-tag>
                    </div>
                  </div>
                </div>
              </div>
            </el-card>
          </div>

          <!-- 右侧：概览卡片 -->
          <div class="right-column">
            <!-- 路线概览 -->
            <el-card class="detail-card sidebar-card" shadow="hover">
              <template #header>
                <span class="card-title">路线概览</span>
              </template>
              <div class="overview-list">
                <div class="overview-item">
                  <el-icon><Calendar /></el-icon>
                  <span class="ov-label">行程天数</span>
                  <span class="ov-value">{{ routeData.days }} 天</span>
                </div>
                <div class="overview-item">
                  <el-icon><Location /></el-icon>
                  <span class="ov-label">景点数量</span>
                  <span class="ov-value">{{ routeData.scenicSpots?.length || 0 }} 处</span>
                </div>
                <div class="overview-item">
                  <el-icon><Flag /></el-icon>
                  <span class="ov-label">难度等级</span>
                  <span class="ov-value">{{ routeData.difficulty || '轻松' }}</span>
                </div>
                <div class="overview-item">
                  <el-icon><Wallet /></el-icon>
                  <span class="ov-label">参考费用</span>
                  <span class="ov-value price-val">
                    {{ routeData.totalPrice > 0 ? `¥${routeData.totalPrice}` : '免费' }}
                  </span>
                </div>
              </div>
            </el-card>

            <!-- 景点速览 -->
            <el-card class="detail-card sidebar-card" shadow="hover">
              <template #header>
                <span class="card-title">景点速览</span>
              </template>
              <div class="spot-quick-list">
                <div
                  v-for="(spot, i) in routeData.scenicSpots"
                  :key="i"
                  class="quick-spot-item"
                >
                  <span class="quick-index">{{ i + 1 }}</span>
                  <span class="quick-name clickable" @click="goToSpot(spot)">{{ spot.name || spot }}</span>
                  <el-tag size="small" type="info" effect="plain" v-if="spot.day">Day {{ spot.day }}</el-tag>
                </div>
              </div>
            </el-card>

            <!-- 标签 -->
            <el-card class="detail-card sidebar-card" shadow="hover" v-if="routeData.tags?.length">
              <template #header>
                <span class="card-title">路线标签</span>
              </template>
              <div class="tags-wrap">
                <el-tag
                  v-for="tag in routeData.tags"
                  :key="tag"
                  type="primary"
                  effect="light"
                  class="route-tag"
                >{{ tag }}</el-tag>
              </div>
            </el-card>

            <!-- 规划按钮（固定在右侧底部） -->
            <el-button
              type="primary"
              size="large"
              class="plan-btn-sidebar"
              @click="useRouteForPlan"
            >
              <el-icon><Calendar /></el-icon>
              用此路线规划行程
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 图片预览 -->
    <el-image-viewer
      v-if="previewVisible"
      :url-list="previewList"
      :initial-index="previewIndex"
      @close="previewVisible = false"
    />

    <Footer />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft, Calendar, Location, Wallet, Flag, Timer, Van, InfoFilled, CollectionTag
} from '@element-plus/icons-vue'
import { getRouteDetail } from '../../api/route'
import { searchScenic } from '../../api/scenic'
import Header from '../../components/Layout/Header.vue'
import Footer from '../../components/Layout/Footer.vue'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const routeData = ref(null)
const previewVisible = ref(false)
const previewList = ref([])
const previewIndex = ref(0)
const spotIdCache = ref({})

const groupedByDay = computed(() => {
  if (!routeData.value?.scenicSpots?.length) return []
  const spots = routeData.value.scenicSpots
  const days = routeData.value.days || 1
  const hasDayField = spots.some(s => s.day != null)
  if (hasDayField) {
    const map = {}
    spots.forEach(s => {
      const d = Number(s.day) || 1
      if (!map[d]) map[d] = []
      map[d].push(s)
    })
    return Object.keys(map).sort((a, b) => a - b).map(d => ({ day: Number(d), spots: map[d] }))
  }
  const perDay = Math.ceil(spots.length / days)
  return Array.from({ length: days }, (_, i) => ({
    day: i + 1,
    spots: spots.slice(i * perDay, (i + 1) * perDay)
  })).filter(g => g.spots.length > 0)
})

const getRouteImage = (r) => {
  if (r?.image && r.image !== '') return r.image
  return '/images/placeholders/route_placeholder.jpg'
}

const goBack = () => router.push({ name: 'Recommend', query: { type: 'route' } })

const useRouteForPlan = () => {
  router.push({ path: '/plan', query: { routeId: routeData.value?.id } })
}

const goToSpot = async (spot) => {
  const name = spot?.name || spot || ''

  // 陶瓷工坊类关键词 → 跳转陶瓷体验列表
  if (/陶瓷|工坊|陶艺|DIY/i.test(name)) {
    router.push({ name: 'CeramicExperience' })
    return
  }

  if (spot?.id) {
    router.push({ name: 'ScenicDetail', params: { id: spot.id } })
    return
  }
  if (!name) return
  if (spotIdCache.value[name]) {
    router.push({ name: 'ScenicDetail', params: { id: spotIdCache.value[name] } })
    return
  }
  try {
    const res = await searchScenic(name)
    const list = Array.isArray(res) ? res : (res?.data || [])
    if (list.length > 0) {
      const id = list[0].id
      spotIdCache.value[name] = id
      router.push({ name: 'ScenicDetail', params: { id } })
    }
  } catch { /* 静默处理 */ }
}

const loadRouteDetail = async () => {
  const routeId = route.params.id
  if (!routeId) { ElMessage.error('路线ID不存在'); return }
  loading.value = true
  try {
    const response = await getRouteDetail(routeId)
    let routeInfo = response.data || response

    if (routeInfo.scenicSpots) {
      if (typeof routeInfo.scenicSpots === 'string') {
        try { routeInfo.scenicSpots = JSON.parse(routeInfo.scenicSpots) }
        catch { routeInfo.scenicSpots = [] }
      } else if (!Array.isArray(routeInfo.scenicSpots)) {
        routeInfo.scenicSpots = []
      }
    } else {
      routeInfo.scenicSpots = []
    }

    if (routeInfo.tags) {
      if (typeof routeInfo.tags === 'string') {
        routeInfo.tags = routeInfo.tags.split(',').map(t => t.trim()).filter(Boolean)
      } else if (!Array.isArray(routeInfo.tags)) {
        routeInfo.tags = []
      }
    } else {
      routeInfo.tags = []
    }

    routeData.value = routeInfo
  } catch (error) {
    console.error('加载路线详情失败:', error)
    ElMessage.error('加载路线详情失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => { loadRouteDetail() })
</script>

<style scoped>
.route-detail-page {
  min-height: 100vh;
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

/* ── 顶部信息栏 ── */
.header-section {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 20px;
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  box-shadow: 0 2px 10px rgba(0,0,0,.06);
}

.info-wrapper {
  flex: 1;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}

.route-title {
  margin: 0;
  font-size: 26px;
  font-weight: 700;
  color: #1f2937;
}

.route-desc {
  margin: 0 0 12px;
  color: #606266;
  font-size: 14px;
  line-height: 1.7;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #6b7280;
}

.meta-item .el-icon {
  color: #2f6efb;
}

.header-cta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
  flex-shrink: 0;
}

.price-display {
  font-size: 22px;
  font-weight: 800;
  color: #1f2937;
}

.price-display .yen { font-size: 16px; margin-right: 1px; }
.price-display .suffix { font-size: 12px; margin-left: 2px; color: #6b7280; font-weight: 600; }
.price-display.free { color: #67c23a; font-size: 18px; }

.cta-btn { font-weight: 700; }

/* ── 主内容分栏 ── */
.content-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
  align-items: flex-start;
}

.left-column,
.right-column {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-card {
  border-radius: 12px;
}

/* ── 封面图 ── */
.cover-card {
  overflow: hidden;
}

.cover-image {
  width: 100%;
  height: 280px;
  object-fit: cover;
  display: block;
}

/* ── 天标题 ── */
.day-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.day-badge {
  background: #2f6efb;
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  padding: 3px 12px;
  border-radius: 20px;
}

.day-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  flex: 1;
}

.day-count {
  font-size: 12px;
  color: #909399;
}

/* ── 时间线 ── */
.spots-timeline {
  display: flex;
  flex-direction: column;
}

.spot-item {
  display: flex;
  gap: 14px;
}

.timeline-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
  width: 16px;
}

.tl-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #2f6efb;
  border: 2px solid #fff;
  box-shadow: 0 0 0 2px #2f6efb;
  flex-shrink: 0;
  margin-top: 4px;
}

.tl-line {
  flex: 1;
  width: 2px;
  background: #e4e7ed;
  margin: 4px 0;
  min-height: 24px;
}

.spot-content {
  flex: 1;
  padding-bottom: 20px;
}

.spot-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 6px;
}

.spot-time {
  font-size: 12px;
  color: #2f6efb;
  font-weight: 600;
  background: #eef5ff;
  padding: 2px 8px;
  border-radius: 10px;
  white-space: nowrap;
}

.spot-name {
  font-size: 15px;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
  flex: 1;
}

.spot-name.clickable,
.quick-name.clickable {
  cursor: pointer;
  transition: color 0.15s;
}

.spot-name.clickable:hover,
.quick-name.clickable:hover {
  color: #2f6efb;
  text-decoration: underline;
}

.spot-meta {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 6px;
}

.spot-tip {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  font-size: 13px;
  color: #606266;
  background: #f5f7fa;
  border-left: 3px solid #dbeafe;
  border-radius: 0 6px 6px 0;
  padding: 8px 12px;
  line-height: 1.6;
  margin-bottom: 6px;
}

.spot-tip .el-icon {
  color: #2f6efb;
  flex-shrink: 0;
  margin-top: 2px;
}

.sub-spots {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.sub-tag { margin: 0; }

/* ── 右侧卡片 ── */
.sidebar-card { width: 100%; }

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}

.overview-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.overview-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.overview-item .el-icon {
  color: #2f6efb;
  font-size: 16px;
  flex-shrink: 0;
}

.ov-label {
  color: #6b7280;
  flex: 1;
}

.ov-value {
  font-weight: 600;
  color: #1f2937;
}

.price-val { color: #e6a23c; }

/* 景点速览 */
.spot-quick-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.quick-spot-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.quick-index {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #eef5ff;
  color: #2f6efb;
  font-size: 11px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.quick-name {
  flex: 1;
  color: #374151;
}

/* 标签 */
.tags-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.route-tag { margin: 0; }

/* 规划按钮 */
.plan-btn-sidebar {
  width: 100%;
  font-weight: 700;
  height: 44px;
}

/* 响应式 */
@media (max-width: 900px) {
  .content-section {
    grid-template-columns: 1fr;
  }
  .header-section {
    flex-direction: column;
  }
  .header-cta {
    align-items: flex-start;
    flex-direction: row;
    align-items: center;
  }
}
</style>
