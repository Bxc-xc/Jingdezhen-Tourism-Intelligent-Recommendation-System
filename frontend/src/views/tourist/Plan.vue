<template>
  <div class="plan-page">
    <Header />

    <div class="plan-body">

      <!-- ── 左侧：手动创建 ── -->
      <div v-if="showManualPanel"
        class="left-panel left-panel--editor"
        :class="{ 'left-panel--editor-expanded': manualExpanded }">
        <ManualPlanPanel @close="showManualPanel = false" @saved="onPlanSaved" @expand="manualExpanded = $event" />
      </div>

      <!-- ── 左侧：智能行程结果 ── -->
      <div v-else-if="showSmartResult" class="left-panel left-panel--smart">
        <SmartTripResult
          :result="smartResult"
          :regenerating="generating"
          :saving="savingSmartPlan"
          @regenerate="doSmartGenerate(true)"
          @save="saveSmartPlan"
          @back="showSmartResult = false; activePlanDetails = null"
        />
      </div>

      <!-- ── 左侧：主页面 ── -->
      <div v-else class="left-panel left-panel--main">

        <!-- Hero 区 -->
        <div class="hero-section">
          <div class="hero-bg"></div>
          <div class="hero-content">
            <div class="hero-tag"><el-icon><Compass /></el-icon> 景德镇智能旅行规划</div>
            <h1 class="hero-heading">规划你的<br /><span class="hero-em">千年瓷都</span>之旅</h1>
            <p class="hero-desc">陶瓷文化 · 千年窑火 · 匠心之城</p>
          </div>
        </div>

        <!-- 规划卡片 -->
        <div class="plan-card">

          <!-- 两栏：出发日期 + 偏好设置 -->
          <div class="plan-card-row">

            <!-- 左栏：出发日期 -->
            <div class="plan-card-col">
              <div class="pc-col-label">出发日期</div>
              <el-popover v-model:visible="datePickerVisible" trigger="click"
                placement="bottom-start" :width="680" popper-class="date-popover" :teleported="true">
                <template #reference>
                  <div class="pc-trigger" :class="{ active: datePickerVisible }">
                    <span class="pc-trigger-icon"><el-icon><Calendar /></el-icon></span>
                    <div class="pc-trigger-body">
                      <span v-if="planForm.startDate" class="pc-trigger-value">
                        {{ planForm.startDate }}<span v-if="planForm.endDate"> → {{ planForm.endDate }}</span>
                      </span>
                      <span v-else class="pc-trigger-placeholder">选择日期或天数</span>
                    </div>
                    <span v-if="planForm.startDate || flexibleDays" class="pc-trigger-clear" @click.stop="clearDate(); flexibleDays = 0; smartForm.days = 1"><el-icon><Close /></el-icon></span>
                    <span class="pc-trigger-chevron"><el-icon><ArrowRight /></el-icon></span>
                  </div>
                </template>
                <!-- 日历弹窗 -->
                <div class="date-popover-inner">
                  <div class="date-mode-tabs">
                    <span :class="['mode-tab', { active: timeMode === 'date' }]" @click="timeMode = 'date'">选择日期</span>
                    <span :class="['mode-tab', { active: timeMode === 'flexible' }]" @click="timeMode = 'flexible'">灵活天数</span>
                  </div>
                  <div v-if="timeMode === 'date'" class="dual-calendar">
                    <div class="cal-month">
                      <div class="cal-header">
                        <button class="cal-nav" @click.stop="prevMonth">‹</button>
                        <span class="cal-title">{{ leftYear }}年{{ leftMonth + 1 }}月</span>
                        <button class="cal-nav invisible"></button>
                      </div>
                      <div class="cal-weekdays"><span v-for="w in weekdays" :key="w" class="cal-wd">{{ w }}</span></div>
                      <div class="cal-days">
                        <span v-for="(cell, i) in leftCells" :key="'l'+i" :class="dayClass(cell)"
                          @click.stop="cell.date && !isDisabled(cell.date) && selectDate(cell.date)">{{ cell.day }}</span>
                      </div>
                    </div>
                    <div class="cal-month">
                      <div class="cal-header">
                        <button class="cal-nav invisible"></button>
                        <span class="cal-title">{{ rightYear }}年{{ rightMonth + 1 }}月</span>
                        <button class="cal-nav" @click.stop="nextMonth">›</button>
                      </div>
                      <div class="cal-weekdays"><span v-for="w in weekdays" :key="w" class="cal-wd">{{ w }}</span></div>
                      <div class="cal-days">
                        <span v-for="(cell, i) in rightCells" :key="'r'+i" :class="dayClass(cell)"
                          @click.stop="cell.date && !isDisabled(cell.date) && selectDate(cell.date)">{{ cell.day }}</span>
                      </div>
                    </div>
                  </div>
                  <div v-else class="flexible-wrap">
                    <div class="flex-section-header">
                      <span class="flex-section-title">选择天数</span>
                      <div class="flex-days-ctrl">
                        <button class="flex-ctrl-btn" @click.stop="flexibleDays > 1 && (flexibleDays--, smartForm.days = flexibleDays)">−</button>
                        <span class="flex-days-val">{{ flexibleDays || '–' }} {{ flexibleDays ? '天' : '' }}</span>
                        <button class="flex-ctrl-btn" @click.stop="flexibleDays++; smartForm.days = flexibleDays">+</button>
                      </div>
                    </div>
                    <div class="flex-grid">
                      <span v-for="d in [1,2,3,4,5,6,7]" :key="d"
                        :class="['flex-chip', { active: flexibleDays === d }]"
                        @click.stop="flexibleDays = flexibleDays === d ? 0 : d; smartForm.days = flexibleDays || 1">{{ d }}天</span>
                    </div>
                  </div>
                  <div class="date-panel-footer">
                    <span class="date-range-label">
                      <template v-if="timeMode === 'flexible' && flexibleDays">灵活 {{ flexibleDays }} 天</template>
                      <template v-else>{{ planForm.startDate || '请选择出发日期' }}{{ planForm.endDate ? ' → ' + planForm.endDate : '' }}</template>
                    </span>
                    <el-button type="primary" @click="datePickerVisible = false">确定</el-button>
                  </div>
                </div>
              </el-popover>
            </div>

            <!-- 右栏：偏好设置 -->
            <div class="plan-card-col">
              <div class="pc-col-label">偏好设置</div>
              <div class="pc-trigger" :class="{ active: prefDialogVisible }" @click="prefDialogVisible = true">
                <span class="pc-trigger-icon"><el-icon><Setting /></el-icon></span>
                <div class="pc-trigger-body">
                  <span v-if="prefSummary" class="pc-trigger-value">{{ prefSummary }}</span>
                  <span v-else class="pc-trigger-placeholder">偏好、同行、预算</span>
                </div>
                <span class="pc-trigger-chevron"><el-icon><ArrowRight /></el-icon></span>
              </div>
            </div>
          </div>

          <!-- 偏好设置弹窗 -->
          <el-dialog v-model="prefDialogVisible" title="偏好设置" width="420px" :append-to-body="true" class="pref-dialog">
            <div class="pref-dialog-body">
              <div class="pref-section">
                <div class="pref-section-label">偏好类型 <span class="pref-multi">可多选</span></div>
                <div class="pref-chips">
                  <span v-for="p in prefOptions" :key="p.value"
                    :class="['pref-chip', { active: smartForm.preferences.includes(p.value) }]"
                    @click="togglePref(p.value)">
                    <el-icon v-if="p.icon"><component :is="p.icon" /></el-icon>
                    {{ p.label }}
                  </span>
                </div>
              </div>
              <div class="pref-section">
                <div class="pref-section-label">同行伙伴</div>
                <div class="pref-chips">
                  <span v-for="opt in companionOptions" :key="opt.value"
                    :class="['pref-chip', { active: localPrefs.companionType === opt.value }]"
                    @click="localPrefs.companionType = localPrefs.companionType === opt.value ? '' : opt.value">
                    <el-icon v-if="opt.icon"><component :is="opt.icon" /></el-icon>
                    {{ opt.label }}
                  </span>
                </div>
              </div>
              <div class="pref-section">
                <div class="pref-section-label">预算范围</div>
                <div class="pref-chips">
                  <span v-for="b in budgetOptions" :key="b.value"
                    :class="['pref-chip pref-chip--wide', { active: smartForm.budget === b.value }]"
                    @click="smartForm.budget = smartForm.budget === b.value ? '' : b.value">{{ b.label }}</span>
                </div>
              </div>
            </div>
            <template #footer>
              <el-button @click="prefDialogVisible = false">取消</el-button>
              <el-button type="primary" @click="prefDialogVisible = false">确定</el-button>
            </template>
          </el-dialog>

          <!-- 分割线 -->
          <div class="divider"></div>

          <!-- 操作按钮 -->
          <div class="action-btns">
            <button class="btn-manual" @click="createManually">
              <span class="btn-icon"><el-icon><EditPen /></el-icon></span>
              手动创建
            </button>
            <button class="btn-smart" :class="{ loading: generating }" @click="doSmartGenerate">
              <span v-if="!generating" class="btn-icon"><el-icon><MagicStick /></el-icon></span>
              <span v-else class="btn-spin"><el-icon class="is-loading"><Loading /></el-icon></span>
              {{ generating ? '生成中...' : '智能规划行程' }}
            </button>
          </div>
        </div>

        <!-- 我的行程 -->
        <div class="my-plans-section">
          <div class="section-hd">
            <span class="section-hd-title">我的行程</span>
            <span class="section-hd-count">{{ myPlans.length }} 条</span>
          </div>

          <!-- 骨架屏 -->
          <template v-if="loadingPlans">
            <div v-for="i in 2" :key="i" class="plan-item-skeleton">
              <el-skeleton :rows="2" animated />
            </div>
          </template>

          <!-- 错误 -->
          <div v-else-if="plansError" class="state-box">
            <p>{{ plansError }}</p>
            <el-button size="small" @click="loadMyPlans">重试</el-button>
          </div>

          <!-- 空 -->
          <div v-else-if="myPlans.length === 0" class="state-box state-box--empty">
            <div class="empty-icon"><el-icon><MapLocation /></el-icon></div>
            <p>还没有行程，快来规划吧</p>
          </div>

          <!-- 列表 -->
          <div v-else class="plans-grid">
            <div v-for="plan in myPlans" :key="plan.id"
              class="plan-card-item" @click="goToPlan(plan.id)">
              <div class="pci-cover">
                <img :src="plan.coverImage || '/images/backgrounds/banner1.jpg'" :alt="plan.title" />
                <div class="pci-days-badge">{{ plan.days }}天</div>
                <button class="pci-del" title="删除" @click.stop="confirmDeletePlan(plan)"><el-icon><Delete /></el-icon></button>
              </div>
              <div class="pci-info">
                <div class="pci-title">{{ plan.title }}</div>
                <div class="pci-meta">
                  <span>{{ plan.spotCount || 0 }} 个景点</span>
                  <span class="pci-dot">·</span>
                  <span v-if="plan.startDate">{{ plan.startDate }}</span>
                  <span v-else>{{ plan.cityCount || 1 }} 个城市</span>
                </div>
                <div v-if="plan.spots && plan.spots.length" class="pci-spots">
                  <span v-for="(spot, si) in plan.spots" :key="si" class="pci-spot-tag" :data-type="spot.type">
                    {{ spot.count }} {{ spot.label }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

      </div>
      <!-- /左侧主面板 -->

      <!-- ── 右侧：地图 ── -->
      <div class="map-panel" :class="{ 'map-panel--wide': showManualPanel }">
        <AmapRouteComponent
          v-if="activePlanDetails"
          :planDetails="activePlanDetails"
          :showMap="true"
          :showTransport="showSmartResult || showManualPanel"
          class="map-fill"
        />
        <template v-else>
          <div v-if="mapError" class="map-error">
            <div class="map-error-icon"><el-icon><Location /></el-icon></div>
            <p>地图加载失败，请刷新页面重试</p>
          </div>
          <div v-else id="plan-map" class="map-fill"></div>
        </template>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import Header from '../../components/Layout/Header.vue'
import ManualPlanPanel from '../../components/plan/ManualPlanPanel.vue'
import AmapRouteComponent from '../../components/AmapRouteComponent.vue'
import SmartTripResult from '../../components/plan/SmartTripResult.vue'
import { generatePlan as generatePlanAPI, getMyPlans, createPlanFromRoute, getPlanById, deletePlan, smartGenerateTrip, createManualPlan } from '../../api/plan'

const router = useRouter()
const route = useRoute()

// 当前激活的行程详情（用于地图路线显示）
const activePlanDetails = ref(null)

const planForm = reactive({
  originCity: '',
  destinationCity: '景德镇',
  endDate: '',
  companionType: '',
  stylePreference: [],
  pacePreference: '适中',
  accommodationPreference: '舒适型'
})

const originInput = ref('')
const popoverVisible = ref(false)
const generating = ref(false)
const datePickerVisible = ref(false)
const timeMode = ref('date') // 'date' | 'flexible'
const flexibleDays = ref(0)
const flexibleMonths = ref([])

// 双月日历
const today = new Date()
const calBase = ref({ year: today.getFullYear(), month: today.getMonth() }) // 左月基准

const leftYear = computed(() => calBase.value.year)
const leftMonth = computed(() => calBase.value.month)
const rightYear = computed(() => leftMonth.value === 11 ? leftYear.value + 1 : leftYear.value)
const rightMonth = computed(() => leftMonth.value === 11 ? 0 : leftMonth.value + 1)

const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

function buildCells(year, month) {
  const first = new Date(year, month, 1).getDay()
  const days = new Date(year, month + 1, 0).getDate()
  const cells = []
  for (let i = 0; i < first; i++) cells.push({ day: '', date: null })
  for (let d = 1; d <= days; d++) {
    const date = `${year}-${String(month + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`
    cells.push({ day: d, date, weekday: new Date(year, month, d).getDay() })
  }
  return cells
}

const leftCells = computed(() => buildCells(leftYear.value, leftMonth.value))
const rightCells = computed(() => buildCells(rightYear.value, rightMonth.value))

const prevMonth = () => {
  if (calBase.value.month === 0) {
    calBase.value = { year: calBase.value.year - 1, month: 11 }
  } else {
    calBase.value = { year: calBase.value.year, month: calBase.value.month - 1 }
  }
}
const nextMonth = () => {
  if (calBase.value.month === 11) {
    calBase.value = { year: calBase.value.year + 1, month: 0 }
  } else {
    calBase.value = { year: calBase.value.year, month: calBase.value.month + 1 }
  }
}

const selectDate = (date) => {
  if (!planForm.startDate || (planForm.startDate && planForm.endDate)) {
    planForm.startDate = date
    planForm.endDate = ''
  } else {
    if (date < planForm.startDate) {
      planForm.endDate = planForm.startDate
      planForm.startDate = date
    } else {
      planForm.endDate = date
    }
  }
}

const dayClass = (cell) => {
  if (!cell.date) return 'cal-day empty'
  const classes = ['cal-day']
  if (cell.weekday === 0 || cell.weekday === 6) classes.push('weekend')
  if (cell.date === planForm.startDate || cell.date === planForm.endDate) classes.push('selected')
  if (planForm.startDate && planForm.endDate && cell.date > planForm.startDate && cell.date < planForm.endDate) classes.push('in-range')
  const todayStr = `${today.getFullYear()}-${String(today.getMonth()+1).padStart(2,'0')}-${String(today.getDate()).padStart(2,'0')}`
  if (cell.date < todayStr) classes.push('disabled')
  return classes.join(' ')
}

const clearDate = () => {
  planForm.startDate = ''
  planForm.endDate = ''
}

const isDisabled = (date) => {
  const todayStr = `${today.getFullYear()}-${String(today.getMonth()+1).padStart(2,'0')}-${String(today.getDate()).padStart(2,'0')}`
  return date < todayStr
}

const toggleMonth = (m) => {
  const idx = flexibleMonths.value.indexOf(m)
  if (idx > -1) flexibleMonths.value.splice(idx, 1)
  else flexibleMonths.value.push(m)
}

const localPrefs = reactive({
  companionType: '',
  stylePreference: [],
  pacePreference: '',
  accommodationPreference: ''
})

const selectedCount = computed(() =>
  [localPrefs.companionType, localPrefs.pacePreference, localPrefs.accommodationPreference]
    .filter(v => v !== '').length + localPrefs.stylePreference.length
)

const companionOptions = [
  { value: '独自出行', label: '独自出行', icon: 'User' },
  { value: '家庭出行', label: '家庭出行', icon: 'House' },
  { value: '情侣出行', label: '情侣出行', icon: 'Star' },
  { value: '朋友出行', label: '朋友出行', icon: 'UserFilled' },
  { value: '老人同行', label: '老人同行', icon: 'Avatar' }
]
const styleOptions = [
  { value: '文化体验', label: '文化体验', icon: '🎨' },
  { value: '经典必去', label: '经典必去', icon: '🗺️' },
  { value: '自然风光', label: '自然风光', icon: '🌿' },
  { value: '城市景观', label: '城市景观', icon: '🏙️' },
  { value: '历史古迹', label: '历史古迹', icon: '🏛️' }
]
const paceOptions = [
  { value: '紧凑', label: '紧凑', icon: '🏃' },
  { value: '适中', label: '适中', icon: '🚶' },
  { value: '宽松', label: '宽松', icon: '🧘' }
]
const accommodationOptions = [
  { value: '舒适型', label: '舒适型' },
  { value: '高档型', label: '高档型' },
  { value: '豪华型', label: '豪华型' }
]

const disabledDate = (time) => time.getTime() < Date.now() - 8.64e7

const confirmOrigin = () => {
  if (originInput.value.trim()) {
    planForm.originCity = originInput.value.trim()
    originInput.value = ''
  }
}

const toggleStyle = (val) => {
  const idx = localPrefs.stylePreference.indexOf(val)
  if (idx > -1) localPrefs.stylePreference.splice(idx, 1)
  else localPrefs.stylePreference.push(val)
}

const resetPrefs = () => {
  localPrefs.companionType = ''
  localPrefs.stylePreference = []
  localPrefs.pacePreference = ''
  localPrefs.accommodationPreference = ''
}

const myPlans = ref([])
const loadingPlans = ref(false)
const plansError = ref(null)
const mapError = ref(false)

const scenicSpots = [
  { id: 1, name: '景德镇古窑民俗博览区', lat: 29.3089, lng: 117.1823, description: '国家4A级旅游景区，展示千年制瓷历史' },
  { id: 2, name: '陶溪川文创街区', description: '工业遗址改造的文创园区，陶瓷艺术聚集地' },
  { id: 3, name: '瑶里古镇', lat: 29.4398, lng: 117.4267, description: '千年古镇，徽派建筑与陶瓷文化融合' },
  { id: 4, name: '浮梁古县衙', description: '保存完好的清代县衙建筑群' },
  { id: 5, name: '御窑厂国家考古遗址公园', lat: 29.2978, lng: 117.2134, description: '明清皇家御用瓷器烧造地' },
  { id: 6, name: '中国陶瓷博物馆', description: '全国最大的陶瓷专题博物馆' },
  { id: 7, name: '三宝国际陶艺村', description: '国际陶艺交流中心，自然与艺术融合' },
  { id: 8, name: '景德镇陶瓷大学', description: '中国唯一以陶瓷命名的高等学府' }
]

const loadMyPlans = async () => {
  loadingPlans.value = true
  plansError.value = null
  const res = await getMyPlans()
  loadingPlans.value = false
  if (res.success) {
    const list = res.data || []
    // 为每个行程提取随机封面图和景点列表
    myPlans.value = list.map(plan => ({
      ...plan,
      coverImage: plan.coverImage || extractRandomCover(plan),
      spots: extractSpots(plan)
    }))
  } else {
    plansError.value = res.message || '加载失败'
  }
}

// banner 图池，用于封面拼图
const COVER_POOL = [
  '/images/backgrounds/banner1.jpg',
  '/images/backgrounds/banner2.jpg',
  '/images/backgrounds/banner3.jpg',
  '/images/backgrounds/banner4.jpg',
  '/images/backgrounds/banner5.jpg',
  '/images/backgrounds/banner6.jpg',
  '/images/backgrounds/banner7.jpg',
  '/images/backgrounds/banner8.jpg',
  '/images/backgrounds/banner9.jpg',
  '/images/backgrounds/bjt.jpg',
  '/images/backgrounds/bjt2.jpg',
  '/images/backgrounds/bt3.jpg',
  '/images/backgrounds/bt5.jpg',
  '/images/backgrounds/bt6.jpg',
  '/images/backgrounds/bt7.jpg',
  '/images/backgrounds/bt8.jpg',
  '/images/backgrounds/bt10.jpg',
  '/images/backgrounds/bt11.jpg',
  '/images/backgrounds/bt12.jpg',
]

// 用行程 ID 做偏移，从图池里取一张封面，保证每个行程不一样
const extractRandomCover = (plan) => {
  const offset = Number(plan.id || 0) % COVER_POOL.length
  return COVER_POOL[offset]
}

// 从 planDetails 统计各类型数量
const extractSpots = (plan) => {
  try {
    const details = plan.planDetails
      ? (typeof plan.planDetails === 'string' ? JSON.parse(plan.planDetails) : plan.planDetails)
      : null
    if (!details?.days) return []
    const counts = { scenic: 0, ceramic: 0, marketplace: 0, food: 0 }
    const normalize = (t) => {
      const s = (t || '').toLowerCase()
      if (s === 'scenic') return 'scenic'
      if (s === 'ceramic') return 'ceramic'
      if (s === 'marketplace' || s === 'market') return 'marketplace'
      if (s === 'food') return 'food'
      return null
    }
    for (const day of details.days) {
      const items = [
        ...(day.schedule || []),
        ...(day.scenic || []).map(s => ({ type: s?.category || 'scenic' }))
      ]
      for (const item of items) {
        const key = normalize(item.type || item.category)
        if (key) counts[key]++
      }
    }
    const labelMap = { scenic: '景点', ceramic: '陶瓷工坊', marketplace: '陶瓷市集', food: '餐饮' }
    return Object.entries(counts)
      .filter(([, n]) => n > 0)
      .map(([type, count]) => ({ type, label: labelMap[type], count }))
  } catch {
    return []
  }
}

const goToPlan = (id) => router.push(`/plan/${id}`)

const confirmDeletePlan = async (plan) => {
  try {
    await ElMessageBox.confirm(`确定删除「${plan.title}」吗？`, '删除行程', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deletePlan(plan.id)
    if (res?.success !== false) {
      myPlans.value = myPlans.value.filter(p => p.id !== plan.id)
      ElMessage.success('已删除')
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch {
    // 用户取消
  }
}

// 旧的 AI 生成入口已由 doSmartGenerate 替代

const showManualPanel = ref(false)
const manualExpanded = ref(false)
const createManually = () => {
  showManualPanel.value = true
  manualExpanded.value = false
  // 清除智能生成的地图路线
  activePlanDetails.value = null
}

// 智能生成
const showSmartResult = ref(false)
const smartResult = ref([])
const savingSmartPlan = ref(false)
const prefDialogVisible = ref(false)

const smartForm = reactive({
  days: 2,
  preferences: [],
  budget: ''
})

const prefOptions = [
  { value: 'scenic_tour',      label: '景点观光', icon: 'Compass' },
  { value: 'food_experience',  label: '美食体验', icon: 'Food' },
  { value: 'culture',          label: '文化体验', icon: 'Reading' },
  { value: 'leisure',          label: '轻松休闲', icon: 'Sunny' }
]
const budgetOptions = [
  { value: 'low',    label: '经济型 <¥300/天' },
  { value: 'medium', label: '舒适型 ¥300-600/天' },
  { value: 'high',   label: '豪华型 >¥600/天' }
]

const togglePref = (val) => {
  const idx = smartForm.preferences.indexOf(val)
  if (idx > -1) smartForm.preferences.splice(idx, 1)
  else smartForm.preferences.push(val)
}

const prefSummary = computed(() => {
  const parts = []
  if (smartForm.preferences.length) {
    parts.push(smartForm.preferences.map(v => prefOptions.find(p => p.value === v)?.label).filter(Boolean).join('、'))
  }
  if (localPrefs.companionType) parts.push(localPrefs.companionType)
  if (smartForm.budget) parts.push(budgetOptions.find(b => b.value === smartForm.budget)?.label?.split(' ')[0])
  return parts.filter(Boolean).join(' · ')
})

const getSelectedTripDays = () => {
  // 1) 明确选择了起止日期：按日期差计算（含首尾）
  if (planForm.startDate && planForm.endDate) {
    const start = new Date(planForm.startDate)
    const end = new Date(planForm.endDate)
    const diff = Math.floor((end - start) / (1000 * 60 * 60 * 24)) + 1
    return diff > 0 ? diff : 1
  }
  // 2) 仅选择了出发日期：至少 1 天
  if (planForm.startDate) return 1
  // 3) 灵活天数
  if (flexibleDays.value > 0) return flexibleDays.value
  // 4) 兜底
  return smartForm.days || 1
}

const doSmartGenerate = async (skipValidation = false) => {
  // 校验：必须有日期或天数（重新生成时跳过）
  if (!skipValidation) {
    const hasDays = (planForm.startDate) || (flexibleDays.value > 0)
    if (!hasDays) {
      ElMessage.warning('请先选择出发日期或旅行天数')
      datePickerVisible.value = true
      return
    }
    // 校验：必须有至少一个偏好
    if (smartForm.preferences.length === 0) {
      ElMessage.warning('请至少选择一个旅行偏好')
      prefDialogVisible.value = true
      return
    }
  }

  const selectedDays = getSelectedTripDays()
  smartForm.days = selectedDays

  generating.value = true
  try {
    const res = await smartGenerateTrip({
      days: selectedDays,
      preferences: smartForm.preferences,
      budget: smartForm.budget || null,
      startDate: planForm.startDate || null
    })
    if (res?.success && res.data?.length) {
      smartResult.value = res.data
      showSmartResult.value = true
      // 同步到地图：把第一天的景点构造成 planDetails 格式
      activePlanDetails.value = buildPlanDetailsFromSmartResult(res.data)
      ElMessage.success('行程生成成功')
    } else {
      ElMessage.error(res?.message || '生成失败，请稍后重试')
    }
  } catch (e) {
    ElMessage.error('生成行程失败，请稍后重试')
  } finally {
    generating.value = false
  }
}

const buildPlanDetailsFromSmartResult = (days) => ({
  days: days.map(d => ({
    day: d.day,
    date: d.date,
    title: `第${d.day}天`,
    schedule: d.items.map(item => ({
      type: item.type === 'scenic' ? 'SCENIC' : item.type === 'food' ? 'FOOD' : 'HOTEL',
      title: item.name,
      scenicId: item.type === 'scenic' ? item.id : null,
      lat: item.lat || null,
      lng: item.lng || null
    }))
  }))
})

const saveSmartPlan = async () => {
  const user = JSON.parse(localStorage.getItem('user') || sessionStorage.getItem('user') || 'null')
  if (!user?.id) { ElMessage.warning('请先登录'); return }
  savingSmartPlan.value = true
  try {
    const planDetails = buildPlanDetailsFromSmartResult(smartResult.value)
    const res = await createManualPlan({
      userId: user.id,
      title: `${smartForm.days}天景德镇智能行程`,
      days: smartForm.days,
      startDate: planForm.startDate || null,
      manualPlanDetails: JSON.stringify(planDetails)
    })
    if (res && (res.success || res.data)) {
      ElMessage.success('行程已保存')
      sessionStorage.removeItem('smartTripResult')
      loadMyPlans()
      showSmartResult.value = false
    } else {
      ElMessage.error(res?.message || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    savingSmartPlan.value = false
  }
}
const onPlanSaved = (plan) => {
  // 手动创建保存后，直接进入行程详情页
  const planId = plan?.id || plan?.planId || plan?.data?.id || plan?.data?.planId
  if (planId) {
    router.push(`/plan/${planId}`)
    return
  }

  // 兜底：未拿到 id 时仍保留原行为
  loadMyPlans()
  if (plan && plan.id && !myPlans.value.find(p => p.id === plan.id)) {
    const newPlan = {
      id: plan.id,
      title: plan.title || '手动创建行程',
      days: plan.days || 1,
      startDate: plan.startDate || '',
      coverImage: plan.coverImage || extractRandomCover(plan),
      spotCount: 0,
      cityCount: 1,
      status: plan.status || 'draft',
      spots: extractSpots(plan)
    }
    myPlans.value.unshift(newPlan)
  }
  if (plan?.planDetails) {
    try {
      activePlanDetails.value = typeof plan.planDetails === 'string'
        ? JSON.parse(plan.planDetails)
        : plan.planDetails
    } catch {}
  }
}

/** 按行程 id 拉取详情并显示在地图上 */
const loadPlanDetailsToMap = async (planId) => {
  try {
    const res = await getPlanById(planId)
    const plan = res?.data || res
    if (plan?.planDetails) {
      activePlanDetails.value = typeof plan.planDetails === 'string'
        ? JSON.parse(plan.planDetails)
        : plan.planDetails
    }
  } catch (e) {
    console.error('加载行程详情失败', e)
  }
}

const initMap = () => {
  if (typeof window.AMap === 'undefined') {
    mapError.value = true
    return
  }
  nextTick(() => {
    const container = document.getElementById('plan-map')
    if (!container) return
    const map = new window.AMap.Map('plan-map', {
      center: [117.178, 29.268],
      zoom: 13,
      resizeEnable: true
    })
  })
}

onMounted(async () => {
  // 从详情页返回时恢复智能行程状态
  const saved = sessionStorage.getItem('smartTripResult')
  if (saved) {
    try {
      smartResult.value = JSON.parse(saved)
      showSmartResult.value = true
    } catch {}
    sessionStorage.removeItem('smartTripResult')
  }

  loadMyPlans()

  // 处理 routeId 参数：从路线模板创建行程
  const routeId = route.query.routeId
  if (routeId) {
    try {
      const user = JSON.parse(localStorage.getItem('user') || sessionStorage.getItem('user') || 'null')
      if (!user?.id) {
        ElMessage.warning('请先登录后再规划行程')
      } else {
        ElMessage.info('正在从路线模板创建行程...')
        const res = await createPlanFromRoute(user.id, routeId, null)
        const planData = res?.data || res
        if (planData?.id) {
          ElMessage.success('行程创建成功')
          router.push({ name: 'PlanDetail', params: { id: planData.id } })
        }
      }
    } catch (e) {
      ElMessage.error('从路线创建行程失败')
    }
  }

  // 动态加载高德地图 SDK（仅在无行程数据时用于默认地图）
  const amapKey = import.meta.env.VITE_AMAP_KEY
  if (!amapKey) {
    mapError.value = true
    return
  }

  if (typeof window.AMap !== 'undefined') {
    initMap()
    return
  }

  const script = document.createElement('script')
  script.src = `https://webapi.amap.com/maps?v=2.0&key=${amapKey}`
  script.async = true
  script.onload = () => initMap()
  script.onerror = () => { mapError.value = true }
  document.head.appendChild(script)
})
</script>

<style scoped>
/* ── 页面基础 ── */
.plan-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f0f2f5;
}
.plan-body {
  display: flex;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

/* ── 左侧面板通用 ── */
.left-panel {
  width: 30%;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: #ddd transparent;
}
.left-panel::-webkit-scrollbar { width: 4px; }
.left-panel::-webkit-scrollbar-thumb { background: #ddd; border-radius: 2px; }

.left-panel--main {
  gap: 0;
  background: #f0f2f5;
}

.left-panel--editor {
  padding: 0;
  overflow: hidden;
  width: 20%;
  transition: width 0.25s ease;
  position: relative;
}
.left-panel--editor-expanded { width: 30%; }

.left-panel--smart {
  width: 30%;
  overflow: hidden;
}

/* ── Hero 区 ── */
.hero-section {
  position: relative;
  height: 200px;
  overflow: hidden;
  flex-shrink: 0;
}
.hero-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #0f2027 0%, #203a43 50%, #2c5364 100%);
}
.hero-bg::after {
  content: '';
  position: absolute;
  inset: 0;
  background: url('/images/backgrounds/banner1.jpg') center/cover no-repeat;
  opacity: 0.35;
}
.hero-content {
  position: relative;
  z-index: 1;
  padding: 36px 32px 28px;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}
.hero-tag {
  font-size: 11px;
  color: rgba(255,255,255,0.6);
  letter-spacing: 2px;
  text-transform: uppercase;
  margin-bottom: 10px;
}
.hero-heading {
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  line-height: 1.25;
  margin: 0 0 8px;
  letter-spacing: -0.3px;
}
.hero-em {
  color: #ffd666;
}
.hero-desc {
  font-size: 13px;
  color: rgba(255,255,255,0.55);
  letter-spacing: 1.5px;
  margin: 0;
}

/* ── 规划卡片 ── */
.plan-card {
  background: #fff;
  margin: -20px 20px 0;
  border-radius: 20px;
  padding: 24px 24px 20px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.10);
  position: relative;
  z-index: 2;
  flex-shrink: 0;
}

/* ── 规划卡片两栏 ── */
.plan-card-row {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}
.plan-card-col {
  flex: 1;
  min-width: 0;
}
.pc-col-label {
  font-size: 11px;
  font-weight: 600;
  color: #aaa;
  letter-spacing: 1.5px;
  text-transform: uppercase;
  margin-bottom: 8px;
}
.pc-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 11px 14px;
  border: 1.5px solid #e0e0e0;
  border-radius: 12px;
  cursor: pointer;
  background: #fafafa;
  transition: all 0.2s;
  min-height: 48px;
}
.pc-trigger:hover, .pc-trigger.active {
  border-color: #1677ff;
  background: #f0f7ff;
}
.pc-trigger-icon { font-size: 16px; flex-shrink: 0; display: flex; align-items: center; color: #888; }
.pc-trigger-body { flex: 1; min-width: 0; }
.pc-trigger-value { font-size: 13px; color: #111; font-weight: 500; display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.pc-trigger-placeholder { font-size: 13px; color: #bbb; }
.pc-trigger-clear { font-size: 11px; color: #bbb; cursor: pointer; padding: 2px 4px; border-radius: 3px; flex-shrink: 0; }
.pc-trigger-clear:hover { color: #ff4d4f; background: #fff1f0; }
.pc-trigger-chevron { font-size: 14px; color: #ccc; display: flex; align-items: center; flex-shrink: 0; }

/* 偏好弹窗 */
.pref-dialog-body { padding: 4px 0; }
.pref-section { margin-bottom: 20px; }
.pref-section:last-child { margin-bottom: 0; }
.pref-section-label {
  font-size: 11px;
  font-weight: 600;
  color: #aaa;
  letter-spacing: 1.2px;
  text-transform: uppercase;
  margin-bottom: 10px;
  display: block;
}
.pref-multi { font-size: 10px; color: #ccc; font-weight: 400; text-transform: none; letter-spacing: 0; margin-left: 4px; }
.pref-chips { display: flex; flex-wrap: wrap; gap: 8px; }
.pref-chip {
  padding: 8px 16px;
  border: 1.5px solid #e0e0e0;
  border-radius: 8px;
  font-size: 13px;
  color: #333;
  cursor: pointer;
  transition: all 0.18s;
  user-select: none;
  background: #fff;
}
.pref-chip:hover { border-color: #1677ff; color: #1677ff; background: #f0f7ff; }
.pref-chip.active { background: #e8f0fe; border-color: #1677ff; color: #1677ff; font-weight: 600; }
.pref-chip--wide { padding: 8px 12px; }

/* 灵活天数加减控件 */
.flex-ctrl-btn {
  width: 28px; height: 28px;
  border: 1.5px solid #e0e0e0;
  border-radius: 6px;
  background: #fff;
  color: #1677ff;
  font-size: 16px;
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: all 0.15s;
  line-height: 1;
  padding: 0;
}
.flex-ctrl-btn:hover { border-color: #1677ff; background: #f0f7ff; }



.divider {
  height: 1px;
  background: #f0f0f0;
  margin: 0 0 20px;
}

/* 操作按钮 */
.action-btns {
  display: flex;
  gap: 8px;
}
.btn-manual {
  flex: 1;
  height: 38px;
  border: 1.5px solid #e0e0e0;
  border-radius: 10px;
  background: #fafafa;
  color: #555;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  transition: all 0.2s;
}
.btn-manual:hover {
  border-color: #1677ff;
  color: #1677ff;
  background: #f0f7ff;
}
.btn-smart {
  flex: 2;
  height: 38px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.2s;
  letter-spacing: 0.3px;
  box-shadow: 0 3px 10px rgba(22,119,255,0.3);
}
.btn-smart:hover {
  background: linear-gradient(135deg, #0e5fd8 0%, #2f7ef5 100%);
  box-shadow: 0 5px 16px rgba(22,119,255,0.4);
  transform: translateY(-1px);
}
.btn-smart.loading { opacity: 0.8; cursor: not-allowed; }
.btn-icon { font-size: 15px; display: flex; align-items: center; }
.btn-spin { display: flex; align-items: center; font-size: 15px; }

/* ── 我的行程 ── */
.my-plans-section {
  padding: 24px 20px 32px;
  flex: 1;
}
.section-hd {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 16px;
}
.section-hd-title { font-size: 17px; font-weight: 700; color: #111; }
.section-hd-count {
  font-size: 12px;
  color: #fff;
  background: #1677ff;
  padding: 1px 8px;
  border-radius: 10px;
  font-weight: 500;
}

.plan-item-skeleton { margin-bottom: 12px; }

.state-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 20px;
  color: #bbb;
  font-size: 14px;
  gap: 10px;
  text-align: center;
}
.state-box--empty .empty-icon { font-size: 40px; display: flex; justify-content: center; color: #c0c4cc; }

.plans-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.plan-card-item {
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 1px 4px rgba(0,0,0,0.07);
}
.plan-card-item:hover {
  box-shadow: 0 6px 20px rgba(0,0,0,0.12);
  transform: translateY(-2px);
}

.pci-cover {
  position: relative;
  width: 100%;
  aspect-ratio: 16/9;
  overflow: hidden;
  background: #f0f2f5;
}
.pci-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}
.plan-card-item:hover .pci-cover img { transform: scale(1.04); }

.pci-days-badge {
  position: absolute;
  bottom: 8px;
  left: 8px;
  background: rgba(0,0,0,0.55);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 8px;
  backdrop-filter: blur(4px);
}

.pci-del {
  position: absolute;
  top: 6px;
  right: 6px;
  background: rgba(0,0,0,0.45);
  border: none;
  color: #fff;
  font-size: 12px;
  cursor: pointer;
  padding: 4px 6px;
  border-radius: 6px;
  opacity: 0;
  transition: opacity 0.15s;
  backdrop-filter: blur(4px);
}
.plan-card-item:hover .pci-del { opacity: 1; }
.pci-del:hover { background: rgba(255,77,79,0.85); }

.pci-info {
  padding: 10px 12px 12px;
}
.pci-title {
  font-size: 13px;
  font-weight: 600;
  color: #111;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 4px;
}
.pci-meta {
  font-size: 11px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 4px;
}
.pci-dot { color: #ddd; }

.pci-spots {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 6px;
}
.pci-spot-tag {
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 4px;
  background: #f0f2f5;
  color: #555;
  white-space: nowrap;
}
.pci-spot-tag[data-type="scenic"],
.pci-spot-tag[data-type="SCENIC"] { background: #e8f0fe; color: #1677ff; }
.pci-spot-tag[data-type="ceramic"],
.pci-spot-tag[data-type="CERAMIC"] { background: #f9f0ff; color: #722ed1; }
.pci-spot-tag[data-type="marketplace"],
.pci-spot-tag[data-type="MARKETPLACE"],
.pci-spot-tag[data-type="market"] { background: #fff0f6; color: #c41d7f; }
.pci-spot-tag[data-type="food"],
.pci-spot-tag[data-type="FOOD"] { background: #fff7e6; color: #d46b08; }

/* ── 右侧地图 ── */
.map-panel {
  flex: 1;
  min-width: 0;
  position: relative;   /* 子元素 absolute 的锚点 */
  overflow: hidden;
  transition: width 0.25s ease;
}
.map-panel--wide { width: 80%; }

/* 所有地图子元素统一用绝对定位撑满 */
.map-fill {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.map-error {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  color: #bbb;
  gap: 12px;
}
.map-error-icon { font-size: 48px; display: flex; justify-content: center; color: #c0c4cc; }

/* ── 日历（scoped 内也需要） ── */
.dual-calendar { display: flex; gap: 32px; padding: 8px 0; }
.cal-month { flex: 1; }
.cal-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.cal-title { font-size: 15px; font-weight: 700; color: #222; }
.cal-nav {
  background: none; border: none; font-size: 20px; color: #1677ff;
  cursor: pointer; padding: 4px 8px; border-radius: 4px; line-height: 1;
}
.cal-nav:hover { background: #f0f7ff; }
.cal-nav.invisible { visibility: hidden; }
.cal-weekdays { display: grid; grid-template-columns: repeat(7, 1fr); margin-bottom: 8px; }
.cal-wd { text-align: center; font-size: 12px; color: #1677ff; padding: 4px 0; }
.cal-days { display: grid; grid-template-columns: repeat(7, 1fr); gap: 2px; }
.cal-day {
  text-align: center; padding: 9px 4px; font-size: 14px; color: #222;
  cursor: pointer; border-radius: 6px; transition: all 0.15s; user-select: none;
}
.cal-day:hover:not(.empty):not(.disabled) { background: #e8f0fe; color: #1677ff; }
.cal-day.weekend { color: #1677ff; }
.cal-day.selected { background: #1677ff; color: #fff !important; }
.cal-day.in-range { background: #e8f0fe; color: #1677ff; border-radius: 0; }
.cal-day.disabled { color: #ccc; cursor: not-allowed; }
.cal-day.empty { cursor: default; }

.flexible-wrap { padding: 4px 0; }
.flex-section-title { font-size: 15px; font-weight: 600; color: #222; margin-bottom: 12px; }
.flex-grid { display: grid; grid-template-columns: repeat(7, 1fr); gap: 8px; }
.flex-chip {
  display: flex; align-items: center; justify-content: center;
  padding: 10px 4px; border: 1.5px solid #eee; border-radius: 8px;
  font-size: 13px; cursor: pointer; transition: all 0.2s; user-select: none;
  color: #555; background: #fafafa;
}
.flex-chip:hover { border-color: #1677ff; color: #1677ff; }
.flex-chip.active { background: #e8f0fe; border-color: #1677ff; color: #1677ff; font-weight: 600; }

.date-panel-footer {
  display: flex; align-items: center; justify-content: space-between;
  padding-top: 16px; border-top: 1px solid #f0f0f0; margin-top: 16px;
}
.date-range-label { font-size: 13px; color: #666; }

@media (max-width: 768px) {
  .left-panel { width: 100%; }
  .map-panel { display: none; }
}
</style>



<style>
/* 日期弹层全局样式（非 scoped） */
.date-popover {
  padding: 0 !important;
  border-radius: 12px !important;
  box-shadow: 0 8px 32px rgba(0,0,0,0.12) !important;
}

.date-popover-inner {
  padding: 20px 24px;
}

.date-popover-inner .date-mode-tabs {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 20px;
  overflow: hidden;
  width: fit-content;
  margin-left: auto;
  margin-right: auto;
}

.date-popover-inner .mode-tab {
  padding: 8px 28px;
  font-size: 14px;
  cursor: pointer;
  color: #666;
  transition: all 0.2s;
  user-select: none;
}

.date-popover-inner .mode-tab.active {
  background: #1677ff;
  color: white;
  border-radius: 20px;
}

.date-popover-inner .dual-calendar {
  display: flex;
  gap: 32px;
}

.date-popover-inner .cal-month {
  flex: 1;
}

.date-popover-inner .cal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.date-popover-inner .cal-title {
  font-size: 16px;
  font-weight: 700;
  color: #222;
}

.date-popover-inner .cal-nav {
  background: none;
  border: none;
  font-size: 20px;
  color: #1677ff;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  line-height: 1;
}

.date-popover-inner .cal-nav:hover { background: #f0f5ff; }
.date-popover-inner .cal-nav.invisible { visibility: hidden; }

.date-popover-inner .cal-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-bottom: 8px;
}

.date-popover-inner .cal-wd {
  text-align: center;
  font-size: 13px;
  color: #1677ff;
  padding: 4px 0;
}

.date-popover-inner .cal-days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px;
}

.date-popover-inner .cal-day {
  text-align: center;
  padding: 10px 4px;
  font-size: 15px;
  color: #222;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.15s;
  user-select: none;
}

.date-popover-inner .cal-day:hover:not(.empty):not(.disabled) {
  background: #e8f0fe;
  color: #1677ff;
}

.date-popover-inner .cal-day.weekend { color: #1677ff; }
.date-popover-inner .cal-day.selected { background: #1677ff; color: white !important; }
.date-popover-inner .cal-day.in-range { background: #e8f0fe; color: #1677ff; border-radius: 0; }
.date-popover-inner .cal-day.disabled { color: #ccc; cursor: not-allowed; }
.date-popover-inner .cal-day.empty { cursor: default; }

.date-popover-inner .date-panel-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  margin-top: 16px;
}

.date-popover-inner .date-range-label {
  font-size: 14px;
  color: #666;
}

.date-popover-inner .flexible-wrap { padding: 4px 0; }

.date-popover-inner .flex-section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.date-popover-inner .flex-section-title {
  font-size: 16px;
  font-weight: 600;
  color: #222;
}

.date-popover-inner .flex-days-ctrl {
  display: flex;
  align-items: center;
  gap: 10px;
}

.date-popover-inner .flex-days-val {
  font-size: 14px;
  color: #333;
  min-width: 60px;
  text-align: center;
}

.date-popover-inner .flex-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 10px;
}

.date-popover-inner .flex-chip {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px 8px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
  color: #333;
  background: white;
}

.date-popover-inner .flex-chip:hover { border-color: #1677ff; color: #1677ff; }
.date-popover-inner .flex-chip.active { background: #e8f0fe; border-color: #1677ff; color: #1677ff; font-weight: 500; }

.date-popover-inner .flex-ctrl-btn {
  width: 28px; height: 28px;
  border: 1.5px solid #e0e0e0;
  border-radius: 6px;
  background: #fff;
  color: #1677ff;
  font-size: 16px;
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: all 0.15s;
  line-height: 1;
  padding: 0;
}
.date-popover-inner .flex-ctrl-btn:hover { border-color: #1677ff; background: #f0f7ff; }
</style>

<style>
/* 旅行偏好悬浮弹层 */
.pref-float-popover {
  padding: 0 !important;
  border-radius: 14px !important;
  box-shadow: 0 8px 32px rgba(0,0,0,0.14) !important;
  border: 1px solid #e8e8e8 !important;
}

.pref-float-popover .pref-popover {
  padding: 20px 20px 16px;
}

.pref-float-popover .pref-section {
  margin-bottom: 16px;
}

.pref-float-popover .pref-label {
  font-size: 13px;
  font-weight: 600;
  color: #555;
  margin-bottom: 10px;
}

.pref-float-popover .pref-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.pref-float-popover .pref-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  border: 1px solid #e0e0e0;
  border-radius: 20px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.18s;
  user-select: none;
  background: white;
  color: #444;
}

.pref-float-popover .pref-chip:hover {
  border-color: #667eea;
  color: #667eea;
  background: #f5f3ff;
}

.pref-float-popover .pref-chip.active {
  background: #667eea;
  border-color: #667eea;
  color: white;
}

.pref-float-popover .pref-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 14px;
  border-top: 1px solid #f0f0f0;
  margin-top: 4px;
}
</style>
