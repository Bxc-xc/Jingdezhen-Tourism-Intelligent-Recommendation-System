<template>
  <div class="plan-page">
    <Header />
    <PlanTopBar
      v-model="planForm"
      @generate="generatePlan"
      @create="createManually"
    />

    <div class="plan-body">
      <!-- 左侧：手动创建面板 -->
      <div v-if="showManualPanel" class="my-route-panel my-route-panel--editor">
        <ManualPlanPanel @close="showManualPanel = false" @saved="onPlanSaved" />
      </div>

      <!-- 左侧：我的线路 -->
      <div v-else class="my-route-panel">
        <div class="panel-title">我的线路</div>

        <!-- 加载中 -->
        <template v-if="loadingPlans">
          <div v-for="i in 3" :key="i" class="route-skeleton">
            <el-skeleton :rows="3" animated />
          </div>
        </template>

        <!-- 错误状态 -->
        <div v-else-if="plansError" class="panel-error">
          <p>{{ plansError }}</p>
          <el-button size="small" @click="loadMyPlans">重试</el-button>
        </div>

        <!-- 空状态 -->
        <div v-else-if="myPlans.length === 0" class="panel-empty">
          <el-empty :image-size="80" description="暂无行程，点击 AI 规划开始创建" />
        </div>

        <!-- 线路卡片列表 -->
        <div v-else class="route-list">
          <div
            v-for="plan in myPlans"
            :key="plan.id"
            class="route-card"
            @click="goToPlan(plan.id)"
          >
            <div class="card-cover">
              <img :src="plan.coverImage || '/images/backgrounds/banner1.jpg'" :alt="plan.title" />
              <span class="card-days">{{ plan.days }}天</span>
            </div>
            <div class="card-info">
              <div class="card-title">{{ plan.title }}</div>
              <div class="card-meta">
                <span>{{ plan.days }}天</span>
                <span v-if="plan.spotCount">· {{ plan.spotCount }}个景点</span>
                <span v-if="plan.cityCount">· {{ plan.cityCount }}个城市</span>
              </div>
              <div class="card-date">{{ plan.startDate }}</div>
              <el-tag
                :type="plan.status === 'saved' ? 'success' : 'info'"
                size="small"
                class="card-status"
              >
                {{ plan.status === 'saved' ? '已保存' : '草稿' }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：地图 -->
      <div class="map-panel">
        <div v-if="mapError" class="map-error">
          <p>地图加载失败，请刷新页面重试</p>
        </div>
        <div v-else id="plan-map" class="map-container"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import Header from '../../components/Layout/Header.vue'
import PlanTopBar from '../../components/plan/PlanTopBar.vue'
import ManualPlanPanel from '../../components/plan/ManualPlanPanel.vue'
import { generatePlan as generatePlanAPI } from '@/api/plan'
import { getMyPlans } from '@/api/plan'

const router = useRouter()

// 表单数据
const planForm = reactive({
  originCity: '',
  destinationCity: '景德镇',
  startDate: '',
  companionType: '',
  stylePreference: [],
  pacePreference: '适中',
  accommodationPreference: '舒适型'
})

// 我的线路状态
const myPlans = ref([])
const loadingPlans = ref(false)
const plansError = ref(null)

// 地图状态
const mapError = ref(false)

// 景德镇主要景点静态数据
const scenicSpots = [
  { id: 1, name: '景德镇古窑民俗博览区', lat: 29.3012, lng: 117.2143, description: '国家4A级旅游景区，展示千年制瓷历史' },
  { id: 2, name: '陶溪川文创街区', lat: 29.2756, lng: 117.1892, description: '工业遗址改造的文创园区，陶瓷艺术聚集地' },
  { id: 3, name: '瑶里古镇', lat: 29.4521, lng: 117.4312, description: '千年古镇，徽派建筑与陶瓷文化融合' },
  { id: 4, name: '浮梁古县衙', lat: 29.3521, lng: 117.2156, description: '保存完好的清代县衙建筑群' },
  { id: 5, name: '御窑厂国家考古遗址公园', lat: 29.2891, lng: 117.1978, description: '明清皇家御用瓷器烧造地' },
  { id: 6, name: '中国陶瓷博物馆', lat: 29.2634, lng: 117.1823, description: '全国最大的陶瓷专题博物馆' }
]

// 加载我的线路
const loadMyPlans = async () => {
  loadingPlans.value = true
  plansError.value = null
  const res = await getMyPlans()
  loadingPlans.value = false
  if (res.success) {
    myPlans.value = res.data || []
  } else {
    plansError.value = res.message || '加载失败'
  }
}

// 跳转行程详情
const goToPlan = (id) => router.push(`/plan/${id}`)

// AI 规划
const generatePlan = async () => {
  if (!planForm.startDate) {
    ElMessage.warning('请选择出发日期')
    return
  }
  try {
    const res = await generatePlanAPI({
      ...planForm,
      stylePreference: planForm.stylePreference.join(',')
    })
    if (res.success) {
      ElMessage.success('行程生成成功！')
      router.push(`/plan/${res.data.id}`)
    }
  } catch (e) {
    ElMessage.error('生成行程失败，请稍后重试')
  }
}

// 手动创建
const showManualPanel = ref(false)
const createManually = () => { showManualPanel.value = true }
const onPlanSaved = (savedPlan) => {
  // 手动创建保存后，直接进入行程详情，而不是回到规划页
  const planId = savedPlan?.id || savedPlan?.planId
  if (planId) {
    router.push(`/plan/${planId}`)
    return
  }
  // 兜底：若后端未返回 id，则先刷新列表
  loadMyPlans()
}

// 初始化百度地图
const initMap = () => {
  if (typeof window.BMap === 'undefined') {
    mapError.value = true
    return
  }
  const map = new window.BMap.Map('plan-map')
  const center = new window.BMap.Point(117.178, 29.268)
  map.centerAndZoom(center, 13)
  map.enableScrollWheelZoom(true)

  // 添加景点 pin 点
  scenicSpots.forEach(spot => {
    const point = new window.BMap.Point(spot.lng, spot.lat)
    const marker = new window.BMap.Marker(point)
    map.addOverlay(marker)

    // 景点名称 label
    const label = new window.BMap.Label(spot.name, {
      offset: new window.BMap.Size(10, -20)
    })
    label.setStyle({ fontSize: '12px', color: '#333', border: 'none', background: 'transparent' })
    marker.setLabel(label)

    // 点击展示 InfoWindow
    marker.addEventListener('click', () => {
      const infoContent = `
        <div style="padding:8px;min-width:180px">
          <div style="font-weight:600;margin-bottom:4px">${spot.name}</div>
          <div style="font-size:12px;color:#666;margin-bottom:8px">${spot.description}</div>
          <button onclick="window.__addToRoute && window.__addToRoute(${spot.id})"
            style="background:#667eea;color:white;border:none;padding:4px 12px;border-radius:4px;cursor:pointer;font-size:12px">
            加入行程
          </button>
        </div>
      `
      const infoWindow = new window.BMap.InfoWindow(infoContent)
      map.openInfoWindow(infoWindow, point)
    })
  })
}

onMounted(() => {
  loadMyPlans()
  // 等待百度地图 SDK 加载完成
  if (typeof window.BMap !== 'undefined') {
    initMap()
  } else {
    // SDK 异步加载，轮询等待
    let attempts = 0
    const timer = setInterval(() => {
      attempts++
      if (typeof window.BMap !== 'undefined') {
        clearInterval(timer)
        initMap()
      } else if (attempts >= 20) {
        clearInterval(timer)
        mapError.value = true
      }
    }, 300)
  }
})
</script>

<style scoped lang="scss">
.plan-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background: #f5f5f5;
}

.plan-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

// 左侧面板
.my-route-panel {
  width: 320px;
  flex-shrink: 0;
  background: white;
  border-right: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.my-route-panel--editor {
  padding: 0;
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  padding: 16px 16px 12px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.route-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.route-skeleton {
  padding: 12px;
}

.panel-error,
.panel-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px;
  text-align: center;
  color: #999;
  font-size: 13px;
  gap: 12px;
}

// 线路卡片
.route-card {
  border: 1px solid #e8e8e8;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;
  background: white;

  &:hover {
    border-color: #667eea;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
    transform: translateY(-1px);
  }
}

.card-cover {
  position: relative;
  width: 100%;
  padding-top: 56.25%; // 16:9
  overflow: hidden;

  img {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .card-days {
    position: absolute;
    top: 8px;
    right: 8px;
    background: rgba(102, 126, 234, 0.9);
    color: white;
    font-size: 11px;
    font-weight: 600;
    padding: 2px 8px;
    border-radius: 10px;
  }
}

.card-info {
  padding: 10px 12px 12px;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-meta {
  font-size: 12px;
  color: #888;
  margin-bottom: 4px;
}

.card-date {
  font-size: 12px;
  color: #aaa;
  margin-bottom: 6px;
}

.card-status {
  font-size: 11px;
}

// 右侧地图
.map-panel {
  flex: 1;
  position: relative;
  overflow: hidden;
}

.map-container {
  width: 100%;
  height: 100%;
}

.map-error {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f9f9f9;
  color: #999;
  font-size: 14px;
}

// 移动端响应式
@media (max-width: 768px) {
  .my-route-panel {
    display: none;
  }

  .map-panel {
    width: 100%;
  }
}
</style>
