<template>
  <div class="pd-page">
    <Header />

    <div v-if="loading" class="pd-loading">
      <el-skeleton :rows="6" animated style="padding: 40px" />
    </div>

    <div v-else-if="!plan" class="pd-empty">
      <el-empty description="行程不存在或已删除">
        <el-button type="primary" @click="$router.push('/plan')">返回行程规划</el-button>
      </el-empty>
    </div>

    <div v-else class="pd-body">
      <!-- 左侧 -->
      <div class="pd-left">

        <!-- 顶部操作栏 -->
        <div class="pd-topbar">
          <el-button text size="small" @click="$router.push('/plan')" class="pd-back">← 返回</el-button>
          <div class="pd-topbar-actions">
            <template v-if="!editing">
              <el-button size="default" type="primary" @click="startEdit"><el-icon><EditPen /></el-icon> 编辑行程</el-button>
              <el-button size="default" @click="confirmDelete"><el-icon><Delete /></el-icon> 删除</el-button>
            </template>
            <template v-else>
              <el-button size="default" type="primary" :loading="saving" @click="savePlan"><el-icon><Check /></el-icon> 保存</el-button>
              <el-button size="default" @click="cancelEdit">取消</el-button>
            </template>
          </div>
        </div>

        <!-- 行程标题 / 元信息 -->
        <div class="pd-header">
          <template v-if="editing">
            <el-input v-model="editForm.title" size="large" placeholder="行程标题" class="pd-title-input" />
            <div class="pd-meta">
              <el-date-picker v-model="editForm.startDate" type="date" placeholder="出发日期"
                value-format="YYYY-MM-DD" size="small" style="width:150px" />
              <span class="pd-meta-sep">→</span>
              <el-date-picker v-model="editForm.endDate" type="date" placeholder="结束日期"
                value-format="YYYY-MM-DD" size="small" style="width:150px" />
            </div>
            <el-input v-model="editForm.description" type="textarea" :rows="2"
              placeholder="行程描述（可选）" resize="none" class="pd-desc-input" />
          </template>
          <template v-else>
            <h1 class="pd-title">{{ plan.title }}</h1>
            <div class="pd-meta">
              <span><el-icon><Calendar /></el-icon> {{ plan.startDate }} → {{ plan.endDate }}</span>
              <span><el-icon><Clock /></el-icon> {{ plan.days }} 天</span>
              <span v-if="plan.budgetType"><el-icon><Money /></el-icon> {{ budgetLabel(plan.budgetType) }}</span>
            </div>
            <p class="pd-desc" v-if="plan.description">{{ plan.description }}</p>
          </template>
        </div>

        <!-- 编辑模式：每天编辑面板 -->
        <template v-if="editing">
          <div v-for="(day, di) in editForm.days" :key="di" class="pd-edit-day">
            <div class="pd-edit-day-hd">
              <div class="pd-day-badge">Day {{ day.day }}</div>
              <el-input v-model="day.title" size="small" placeholder="今日标题" class="pd-day-title-input" />
              <el-input v-model="day.date" size="small" placeholder="日期" style="width:130px" />
              <el-button size="small" type="danger" text @click="removeDay(di)" :disabled="editForm.days.length <= 1">
                删除此天
              </el-button>
            </div>

            <!-- 景点 -->
            <div class="pd-edit-section">
              <div class="pd-edit-sec-title"><el-icon><Compass /></el-icon> 景点 / 陶瓷工坊 / 餐饮 / 市集</div>
              <div v-for="(item, ii) in day.scenic" :key="'s'+ii" class="pd-edit-item-row">
                <el-autocomplete
                  v-model="day.scenic[ii]"
                  :fetch-suggestions="querySpotSuggestions"
                  placeholder="搜索景点、陶瓷工坊、餐饮、市集..."
                  size="small"
                  style="flex:1"
                  :trigger-on-focus="false"
                  @select="(v) => { day.scenic[ii] = v.value }"
                >
                  <template #default="{ item }">
                    <div class="pd-suggest-row">
                      <span class="pd-suggest-name">{{ item.value }}</span>
                      <span class="pd-suggest-tag" :data-type="item._type">{{ item.typeLabel }}</span>
                    </div>
                    <div v-if="item.address" class="pd-suggest-addr">{{ item.address }}</div>
                  </template>
                </el-autocomplete>
                <el-button size="small" type="primary" plain @click="quickSaveField(day, ii, 'scenic')">保存</el-button>
                <el-button size="small" text type="danger" @click="day.scenic.splice(ii,1)">✕</el-button>
              </div>
              <el-button size="small" text type="primary" @click="day.scenic.push('')">+ 添加</el-button>
            </div>

            <!-- 美食 -->
            <div class="pd-edit-section">
              <div class="pd-edit-sec-title"><el-icon><Food /></el-icon> 美食</div>
              <div v-for="(item, ii) in day.food" :key="'f'+ii" class="pd-edit-item-row">
                <el-autocomplete
                  v-model="day.food[ii]"
                  :fetch-suggestions="querySpotSuggestions"
                  placeholder="搜索餐厅、美食..."
                  size="small"
                  style="flex:1"
                  :trigger-on-focus="false"
                  @select="(v) => { day.food[ii] = v.value }"
                >
                  <template #default="{ item }">
                    <div class="pd-suggest-row">
                      <span class="pd-suggest-name">{{ item.value }}</span>
                      <span class="pd-suggest-tag" :data-type="item._type">{{ item.typeLabel }}</span>
                    </div>
                    <div v-if="item.address" class="pd-suggest-addr">{{ item.address }}</div>
                  </template>
                </el-autocomplete>
                <el-button size="small" type="primary" plain @click="quickSaveField(day, ii, 'food')">保存</el-button>
                <el-button size="small" text type="danger" @click="day.food.splice(ii,1)">✕</el-button>
              </div>
              <el-button size="small" text type="primary" @click="day.food.push('')">+ 添加美食</el-button>
            </div>

            <!-- 住宿 -->
            <div class="pd-edit-section">
              <div class="pd-edit-sec-title"><el-icon><OfficeBuilding /></el-icon> 住宿</div>
              <div v-for="(item, ii) in day.hotel" :key="'h'+ii" class="pd-edit-item-row">
                <el-autocomplete
                  v-model="day.hotel[ii]"
                  :fetch-suggestions="queryHotelSuggestions"
                  placeholder="搜索酒店..."
                  size="small"
                  style="flex:1"
                  :trigger-on-focus="false"
                  @select="(v) => { day.hotel[ii] = v.value }"
                >
                  <template #default="{ item }">
                    <div class="pd-suggest-row">
                      <span class="pd-suggest-name">{{ item.value }}</span>
                      <span class="pd-suggest-tag" data-type="HOTEL">酒店</span>
                    </div>
                    <div v-if="item.address" class="pd-suggest-addr">{{ item.address }}</div>
                  </template>
                </el-autocomplete>
                <el-button size="small" type="primary" plain @click="quickSaveMerchant(day, ii)">保存</el-button>
                <el-button size="small" text type="danger" @click="day.hotel.splice(ii,1)">✕</el-button>
              </div>
              <el-button size="small" text type="primary" @click="day.hotel.push('')">+ 添加住宿</el-button>
            </div>

            <!-- 交通 -->
            <div class="pd-edit-section">
              <div class="pd-edit-sec-title"><el-icon><Van /></el-icon> 交通</div>
              <div v-for="(item, ii) in day.transport" :key="'t'+ii" class="pd-edit-item-row">
                <el-input v-model="day.transport[ii]" size="small" placeholder="交通方式" />
                <el-button size="small" type="primary" plain @click="quickSaveField(day, ii, 'transport')">保存</el-button>
                <el-button size="small" text type="danger" @click="day.transport.splice(ii,1)">✕</el-button>
              </div>
              <el-button size="small" text type="primary" @click="day.transport.push('')">+ 添加交通</el-button>
            </div>

            <!-- 备注 -->
            <div class="pd-edit-section">
              <div class="pd-edit-sec-title"><el-icon><Memo /></el-icon> 备注</div>
              <el-input v-model="day.note" type="textarea" :rows="2" placeholder="今日备注" resize="none" />
            </div>
          </div>

          <!-- 添加天 -->
          <el-button class="pd-add-day-btn" @click="addDay">+ 添加第 {{ editForm.days.length + 1 }} 天</el-button>

          <!-- 全局备注 -->
          <div class="pd-edit-day">
            <div class="pd-edit-sec-title" style="font-size:15px;font-weight:700;margin-bottom:10px"><el-icon><Memo /></el-icon> 全局备注</div>
            <el-input v-model="editForm.notes" type="textarea" :rows="3" placeholder="整体行程备注..." resize="none" />
          </div>
        </template>

        <!-- 查看模式：时间线 -->
        <template v-else>
          <div class="pd-timeline" v-if="parsedDays.length">
            <div v-for="day in parsedDays" :key="day.day" class="pd-day">
              <div class="pd-day-hd">
                <div class="pd-day-badge">Day {{ day.day }}</div>
                <span class="pd-day-title">{{ day.title }}</span>
                <span class="pd-day-date" v-if="day.date">{{ day.date }}</span>
              </div>
              <div class="pd-day-items">
                <div v-for="(item, idx) in allItems(day)" :key="idx" class="pd-item">
                  <div class="pd-item-connector">
                    <div class="pd-item-dot" :class="dotClass(item)"></div>
                    <div v-if="idx < allItems(day).length - 1" class="pd-item-line"></div>
                  </div>
                  <div class="pd-item-card">
                    <div class="pd-item-main" :class="{ 'has-image': !!item.image }">
                      <img
                        v-if="item.image"
                        :src="item.image"
                        :alt="item.title || item.name"
                        class="pd-item-image pd-item-image--hero"
                        @error="onItemImageError"
                      />
                      <div class="pd-item-content">
                        <div class="pd-item-name">
                          <span class="pd-item-type-icon"><el-icon><component :is="typeIcon(item)" /></el-icon></span>
                          <span
                            class="pd-item-link"
                            :class="{ 'is-clickable': isItemClickable(item) }"
                            @click="goToItemDetail(item)"
                          >
                            {{ item.title || item.name }}
                          </span>
                        </div>
                        <div class="pd-item-desc" v-if="item.description">{{ item.description }}</div>
                      </div>
                    </div>
                    <div class="pd-item-meta">
                      <span v-if="item.duration" class="pd-meta-tag"><el-icon><Timer /></el-icon> {{ item.duration }}</span>
                      <span v-if="item.ticketPrice > 0" class="pd-meta-tag"><el-icon><Ticket /></el-icon> ¥{{ item.ticketPrice }}</span>
                    </div>
                    <div class="pd-item-tips" v-if="item.tips"><el-icon><InfoFilled /></el-icon> {{ item.tips }}</div>
                  </div>
                </div>
              </div>
              <div class="pd-day-note" v-if="day.note"><el-icon><Memo /></el-icon> {{ day.note }}</div>
            </div>
          </div>
          <div v-else class="pd-no-detail">
            <el-empty :image-size="60" description="暂无详细行程，点击「编辑行程」开始添加" />
          </div>
          <div class="pd-notes" v-if="parsedNotes">
            <div class="pd-notes-title"><el-icon><Memo /></el-icon> 备注</div>
            <p>{{ parsedNotes }}</p>
          </div>
        </template>

      </div>

      <!-- 右侧：地图 -->
      <div class="pd-right">
        <AmapRouteComponent v-if="activePlanDetails" :planDetails="activePlanDetails" :showMap="true" class="pd-map" />
        <div v-else class="pd-map-placeholder">
          <el-empty :image-size="60" description="暂无地图路线数据" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import Header from '../../components/Layout/Header.vue'
import AmapRouteComponent from '../../components/AmapRouteComponent.vue'
import { getPlanById, deletePlan, updatePlan } from '../../api/plan'
import { globalSearch } from '../../api/search'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const saving = ref(false)
const editing = ref(false)
const plan = ref(null)

// 编辑表单
const editForm = reactive({
  title: '',
  description: '',
  startDate: '',
  endDate: '',
  notes: '',
  days: []   // [{ day, title, date, note, scenic[], food[], hotel[], transport[] }]
})

// ── 解析 planDetails ──────────────────────────────────────────

const parsedDetails = computed(() => {
  if (!plan.value?.planDetails) return { days: [], notes: '' }
  try {
    return typeof plan.value.planDetails === 'string'
      ? JSON.parse(plan.value.planDetails)
      : plan.value.planDetails
  } catch { return { days: [], notes: '' } }
})

const parsedDays = computed(() => parsedDetails.value.days || [])
const parsedNotes = computed(() => parsedDetails.value.notes || '')

// 把各种格式的 schedule/items 统一成展示列表
const allItems = (day) => {
  // 新格式：{ scenic[], food[], hotel[], transport[] }
  if (Array.isArray(day.scenic) || Array.isArray(day.food) || Array.isArray(day.hotel)) {
    const items = []
    ;(day.scenic || []).filter(Boolean).forEach(n => items.push({ type: 'SCENIC', title: n }))
    ;(day.food || []).filter(Boolean).forEach(n => items.push({ type: 'FOOD', title: n }))
    ;(day.hotel || []).filter(Boolean).forEach(n => items.push({ type: 'HOTEL', title: n }))
    ;(day.transport || []).filter(Boolean).forEach(n => items.push({ type: 'TRANSPORT', title: n }))
    return items
  }
  // 旧格式：schedule[]
  return day.schedule || day.items || []
}

// 地图数据
const activePlanDetails = computed(() => {
  if (!parsedDays.value.length) return null
  return {
    days: parsedDays.value.map(d => ({
      day: d.day,
      schedule: allItems(d).map(item => ({
        type: item.type || 'SCENIC',
        title: item.title || item.name || '',
        scenicId: item.scenicId || null
      }))
    }))
  }
})

// ── 工具函数 ──────────────────────────────────────────────────

const dotClass = (item) => {
  const t = (item.type || '').toUpperCase()
  if (t === 'HOTEL' || item.type === 'hotel') return 'pd-dot--hotel'
  if (t === 'FOOD' || item.type === 'food') return 'pd-dot--food'
  if (t === 'TRANSPORT') return 'pd-dot--transport'
  return 'pd-dot--scenic'
}

const typeIcon = (item) => {
  const t = (item.type || '').toUpperCase()
  if (t === 'HOTEL' || item.type === 'hotel') return 'OfficeBuilding'
  if (t === 'FOOD' || item.type === 'food') return 'Food'
  if (t === 'TRANSPORT') return 'Van'
  return 'Compass'
}

const isItemClickable = (item) => {
  const t = (item?.type || '').toUpperCase()
  if (t === 'TRANSPORT') return false
  return Boolean(item?.scenicId || item?.hotelId || item?.foodId || item?.id)
}

const goToItemDetail = (item) => {
  if (!isItemClickable(item)) return

  const t = (item?.type || '').toUpperCase()
  const targetId = item?.scenicId || item?.hotelId || item?.foodId || item?.id
  if (!targetId) return

  const backQuery = { from: 'plan-detail', planId: String(route.params.id || '') }

  if (t === 'HOTEL') {
    router.push({ path: `/hotel/${targetId}`, query: backQuery })
    return
  }
  if (t === 'FOOD') {
    router.push({ path: `/food/${targetId}`, query: backQuery })
    return
  }
  if (t === 'CERAMIC') {
    router.push({ path: `/ceramic-workshop/${targetId}`, query: backQuery })
    return
  }
  if (t === 'MARKETPLACE' || t === 'MARKET') {
    router.push({ path: `/marketplace/${targetId}`, query: backQuery })
    return
  }
  // 默认按景点处理（SCENIC）
  router.push({ path: `/scenic/${targetId}`, query: backQuery })
}

const onItemImageError = (e) => {
  e.target.style.display = 'none'
}

const budgetLabel = (b) => ({ low: '经济型', medium: '舒适型', high: '豪华型' }[b] || b)

// ── 编辑逻辑 ──────────────────────────────────────────────────

// 把 parsedDays 转成可编辑的结构
const toEditDays = (days) => days.map((d, i) => {
  // 兼容旧格式 schedule[]，并保留路线模板中未显式支持的模块类型
  const items = allItems(d)
  const itemName = (x) => x.title || x.name || x.spotName || x.location || ''
  const t = (x) => (x.type || x.category || '').toString().toUpperCase()

  // 记录“名称 -> 原条目”，用于保存时无损回写（time/duration/tips/ticketPrice/tags/scenicId等）
  const originByName = {}
  items.forEach(x => {
    const name = itemName(x)
    if (!name) return
    if (!originByName[name]) originByName[name] = []
    originByName[name].push({ ...x })
  })

  // 保留原始顺序，避免编辑后顺序漂移
  const orderNames = items.map(itemName).filter(Boolean)

  return {
    day: d.day || i + 1,
    title: d.title || '',
    date: d.date || '',
    note: d.note || '',
    // 路线模板 createPlanFromRoute 生成的 schedule 常无 type，按 SCENIC 兜底，避免编辑后丢失
    scenic: items.filter(x => {
      const tp = t(x)
      return !tp || ['SCENIC', 'CERAMIC', 'MARKETPLACE', 'MARKET'].includes(tp)
    }).map(itemName).filter(Boolean),
    food: items.filter(x => t(x) === 'FOOD').map(itemName).filter(Boolean),
    hotel: items.filter(x => t(x) === 'HOTEL').map(itemName).filter(Boolean),
    transport: items.filter(x => t(x) === 'TRANSPORT').map(itemName).filter(Boolean),
    __originByName: originByName,
    __orderNames: orderNames
  }
})

const startEdit = () => {
  editForm.title = plan.value.title || ''
  editForm.description = plan.value.description || ''
  editForm.startDate = plan.value.startDate || ''
  editForm.endDate = plan.value.endDate || ''
  editForm.notes = parsedNotes.value
  editForm.days = toEditDays(parsedDays.value.length ? parsedDays.value : [{ day: 1, title: '', date: '', schedule: [] }])
  editing.value = true
}

const cancelEdit = () => { editing.value = false }

const addDay = () => {
  const n = editForm.days.length + 1
  editForm.days.push({ day: n, title: '', date: '', note: '', scenic: [], food: [], hotel: [], transport: [] })
}

const removeDay = (i) => {
  editForm.days.splice(i, 1)
  // 重新编号
  editForm.days.forEach((d, idx) => { d.day = idx + 1 })
}

// ── 搜索建议 ──────────────────────────────────────────────────

const searchCache = ref({})
const hotelCache = ref({})

const querySpotSuggestions = async (keyword, cb) => {
  if (!keyword || keyword.length < 1) { cb([]); return }
  if (searchCache.value[keyword]) { cb(searchCache.value[keyword]); return }
  try {
    const res = await globalSearch(keyword)
    const d = res?.data || {}
    const typeLabel = { SCENIC: '景点', FOOD: '餐饮', CERAMIC: '陶瓷工坊', MARKETPLACE: '陶瓷市集' }
    const list = [
      ...(d.scenic || []).map(s => ({ ...s, _type: 'SCENIC' })),
      ...(d.ceramic || []).map(s => ({ ...s, _type: 'CERAMIC' })),
      ...(d.food || []).map(s => ({ ...s, _type: 'FOOD' })),
      ...(d.marketplace || []).map(s => ({ ...s, _type: 'MARKETPLACE' }))
    ].map(s => ({ value: s.name, address: s.address || '', typeLabel: typeLabel[s._type] || '', _type: s._type }))
    searchCache.value[keyword] = list
    cb(list)
  } catch { cb([]) }
}

const queryHotelSuggestions = async (keyword, cb) => {
  if (!keyword || keyword.length < 1) { cb([]); return }
  if (hotelCache.value[keyword]) { cb(hotelCache.value[keyword]); return }
  try {
    const res = await globalSearch(keyword)
    const list = (res?.data?.hotel || []).map(h => ({ value: h.name, address: h.address || '' }))
    hotelCache.value[keyword] = list
    cb(list)
  } catch { cb([]) }
}

const buildPlanDetailsForSave = () => {
  const toType = (name, day) => {
    if ((day.food || []).includes(name)) return 'FOOD'
    if ((day.hotel || []).includes(name)) return 'HOTEL'
    if ((day.transport || []).includes(name)) return 'TRANSPORT'
    return 'SCENIC'
  }

  const takeOriginItem = (name, day, expectType) => {
    const list = day.__originByName?.[name]
    if (!Array.isArray(list) || list.length === 0) return null
    const idx = list.findIndex(it => ((it.type || it.category || '').toString().toUpperCase()) === expectType)
    if (idx >= 0) {
      const [picked] = list.splice(idx, 1)
      return { ...picked }
    }
    const [fallback] = list.splice(0, 1)
    return { ...fallback }
  }

  const buildScheduleItem = (name, type, originItem, orderIndex) => {
    const normalizedType = type === 'MARKET' ? 'MARKETPLACE' : type
    if (originItem) {
      return {
        ...originItem,
        type: normalizedType,
        title: name
      }
    }
    const hour = 9 + orderIndex * 2
    return {
      type: normalizedType,
      title: name,
      time: `${String(hour).padStart(2, '0')}:00`,
      description: '',
      duration: '',
      ticketPrice: 0,
      tips: ''
    }
  }

  return {
    days: editForm.days.map(d => {
      const scenic = (d.scenic || []).filter(Boolean)
      const food = (d.food || []).filter(Boolean)
      const hotel = (d.hotel || []).filter(Boolean)
      const transport = (d.transport || []).filter(Boolean)

      const allEditedNames = [...scenic, ...food, ...hotel, ...transport]
      const editedSet = new Set(allEditedNames)

      const orderedNames = [
        ...((d.__orderNames || []).filter(n => editedSet.has(n))),
        ...allEditedNames.filter(n => !(d.__orderNames || []).includes(n))
      ]

      const schedule = orderedNames.map((name, idx) => {
        const itemType = toType(name, d)
        const origin = takeOriginItem(name, d, itemType)
        return buildScheduleItem(name, itemType, origin, idx)
      })

      return {
        day: d.day,
        title: d.title,
        date: d.date,
        note: d.note,
        schedule
      }
    }),
    notes: editForm.notes
  }
}

const quickSaveMerchant = async (day, hotelIndex) => {
  const merchantName = (day?.hotel?.[hotelIndex] || '').trim()
  if (!merchantName) {
    ElMessage.warning('请先填写商家名称')
    return
  }
  if (!editForm.title.trim()) {
    ElMessage.warning('请先填写行程标题')
    return
  }
  try {
    const planDetails = buildPlanDetailsForSave()
    const payload = {
      title: editForm.title,
      description: editForm.description,
      startDate: editForm.startDate || null,
      endDate: editForm.endDate || null,
      days: editForm.days.length,
      planDetails: JSON.stringify(planDetails)
    }
    const res = await updatePlan(route.params.id, payload)
    if (res?.success !== false) {
      plan.value = { ...plan.value, ...payload, planDetails: JSON.stringify(planDetails) }
      ElMessage.success(`商家「${merchantName}」已保存`)
    } else {
      ElMessage.error(res?.message || '保存失败')
    }
  } catch {
    ElMessage.error('保存失败，请稍后重试')
  }
}

const savePlan = async () => {
  if (!editForm.title.trim()) { ElMessage.warning('请填写行程标题'); return }

  saving.value = true
  try {
    const planDetails = buildPlanDetailsForSave()

    const payload = {
      title: editForm.title,
      description: editForm.description,
      startDate: editForm.startDate || null,
      endDate: editForm.endDate || null,
      days: editForm.days.length,
      planDetails: JSON.stringify(planDetails)
    }

    const res = await updatePlan(route.params.id, payload)
    if (res?.success !== false) {
      // 更新本地数据
      plan.value = { ...plan.value, ...payload, planDetails: JSON.stringify(planDetails) }
      editing.value = false
      ElMessage.success('行程已保存')
    } else {
      ElMessage.error(res?.message || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

// ── 删除 ──────────────────────────────────────────────────────

const confirmDelete = async () => {
  try {
    await ElMessageBox.confirm('确定删除此行程吗？', '删除行程', {
      confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning'
    })
    await deletePlan(route.params.id)
    ElMessage.success('已删除')
    router.push('/plan')
  } catch { /* 取消 */ }
}

// ── 加载 ──────────────────────────────────────────────────────

const loadPlan = async () => {
  loading.value = true
  try {
    const res = await getPlanById(route.params.id)
    plan.value = res?.data || res
  } catch {
    plan.value = null
  } finally {
    loading.value = false
  }
}

onMounted(loadPlan)
</script>

<style scoped>
.pd-page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #f0f2f5;
}

.pd-loading, .pd-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
}

.pd-body {
  display: flex;
  flex: 1;
  height: calc(100vh - 60px);
}

/* ── 左侧 ── */
.pd-left {
  width: 50%;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  scrollbar-width: thin;
  scrollbar-color: #ccc transparent;
}
.pd-left::-webkit-scrollbar { width: 5px; }
.pd-left::-webkit-scrollbar-thumb { background: #ccc; border-radius: 3px; }

/* ── 顶部操作栏 ── */
.pd-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-radius: 12px;
  padding: 12px 18px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.06);
}
.pd-back { color: #1677ff; font-size: 14px; }
.pd-topbar-actions { display: flex; gap: 10px; }

/* ── 行程头部 ── */
.pd-header {
  background: #fff;
  border-radius: 14px;
  padding: 20px 24px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.06);
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.pd-title { font-size: 22px; font-weight: 700; color: #222; margin: 0; }
.pd-title-input :deep(.el-input__inner) { font-size: 20px; font-weight: 700; }
.pd-meta { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; font-size: 14px; color: #888; }
.pd-meta span { display: flex; align-items: center; gap: 4px; }
.pd-meta-sep { color: #ccc; }
.pd-desc { font-size: 14px; color: #666; margin: 0; line-height: 1.6; }
.pd-desc-input { margin-top: 4px; }

/* ── 查看模式时间线 ── */
.pd-timeline { display: flex; flex-direction: column; gap: 16px; }

.pd-day {
  background: #fff;
  border-radius: 14px;
  padding: 18px 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.06);
}

.pd-day-hd {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}
.pd-day-badge {
  background: #1677ff;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  padding: 3px 12px;
  border-radius: 20px;
  flex-shrink: 0;
}
.pd-day-title { font-size: 15px; font-weight: 600; color: #333; flex: 1; }
.pd-day-date { font-size: 12px; color: #aaa; }
.pd-day-note { font-size: 13px; color: #888; margin-top: 10px; padding: 8px 12px; background: #fffbe6; border-radius: 6px; }

.pd-day-items { display: flex; flex-direction: column; }

.pd-item { display: flex; align-items: flex-start; }

.pd-item-connector {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 22px;
  flex-shrink: 0;
  padding-top: 14px;
}
.pd-item-dot {
  width: 10px; height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
  border: 2px solid #fff;
  box-shadow: 0 0 0 2px currentColor;
}
.pd-dot--scenic    { color: #1677ff; background: #1677ff; }
.pd-dot--food      { color: #f5a623; background: #f5a623; }
.pd-dot--hotel     { color: #52c41a; background: #52c41a; }
.pd-dot--transport { color: #909399; background: #909399; }

.pd-item-line { width: 2px; flex: 1; min-height: 16px; background: #e8e8e8; margin: 4px 0; }

.pd-item-card {
  flex: 1;
  padding: 10px 12px;
  margin-bottom: 8px;
  margin-left: 8px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
}
.pd-item-main {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}
.pd-item-main.has-image {
  display: block;
}
.pd-item-image {
  width: 72px;
  height: 54px;
  border-radius: 6px;
  object-fit: cover;
  flex-shrink: 0;
  border: 1px solid #ececec;
  background: #f5f5f5;
}
.pd-item-image--hero {
  width: 100%;
  max-width: 340px;
  height: 150px;
  border-radius: 8px;
  margin-bottom: 10px;
  border: none;
}
.pd-item-content {
  flex: 1;
  min-width: 0;
}
.pd-item-name { font-size: 14px; font-weight: 600; color: #222; display: flex; align-items: center; gap: 6px; margin-bottom: 4px; }
.pd-item-type-icon { font-size: 15px; display: flex; align-items: center; }
.pd-item-link {
  color: inherit;
  transition: color .2s ease;
}
.pd-item-link.is-clickable {
  cursor: pointer;
  color: #1677ff;
}
.pd-item-link.is-clickable:hover {
  color: #4096ff;
  text-decoration: underline;
}
.pd-item-desc { font-size: 12px; color: #888; line-height: 1.5; margin-bottom: 6px; }
.pd-item-meta { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 4px; }
.pd-meta-tag { font-size: 11px; background: #f0f5ff; color: #1677ff; padding: 2px 8px; border-radius: 10px; }
.pd-item-tips { font-size: 11px; color: #aaa; line-height: 1.5; }

.pd-notes {
  background: #fff;
  border-radius: 14px;
  padding: 18px 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.06);
}
.pd-notes-title { font-size: 14px; font-weight: 600; color: #333; margin-bottom: 8px; }
.pd-notes p { font-size: 13px; color: #666; line-height: 1.7; margin: 0; }

.pd-no-detail {
  background: #fff;
  border-radius: 14px;
  padding: 40px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.06);
  text-align: center;
}

/* ── 编辑模式 ── */
.pd-edit-day {
  background: #fff;
  border-radius: 14px;
  padding: 18px 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.06);
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.pd-edit-day-hd {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.pd-day-title-input { flex: 1; min-width: 120px; }

.pd-edit-section { display: flex; flex-direction: column; gap: 8px; }
.pd-edit-sec-title { font-size: 13px; font-weight: 600; color: #555; }

.pd-edit-item-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.pd-edit-item-row .el-input { flex: 1; }

.pd-add-day-btn {
  width: 100%;
  height: 44px;
  border: 2px dashed #d0d0d0;
  border-radius: 12px;
  color: #888;
  font-size: 14px;
  background: transparent;
  transition: all 0.2s;
}
.pd-add-day-btn:hover { border-color: #1677ff; color: #1677ff; background: #f0f5ff; }

/* ── 右侧地图 ── */
.pd-right {
  width: 50%;
  flex-shrink: 0;
  overflow: hidden;
  position: sticky;
  top: 0;
  height: calc(100vh - 60px);
}
.pd-map { height: 100%; }
.pd-map-placeholder {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
}
/* ── 搜索建议 ── */
.pd-suggest-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
}
.pd-suggest-name {
  font-size: 13px;
  color: #222;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.pd-suggest-tag {
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 10px;
  flex-shrink: 0;
  background: #e8f0fe;
  color: #1677ff;
}
.pd-suggest-tag[data-type="FOOD"] { background: #fff7e6; color: #d46b08; }
.pd-suggest-tag[data-type="CERAMIC"] { background: #f9f0ff; color: #722ed1; }
.pd-suggest-tag[data-type="MARKETPLACE"] { background: #f6ffed; color: #389e0d; }
.pd-suggest-tag[data-type="HOTEL"] { background: #e6f7ff; color: #0958d9; }
.pd-suggest-addr {
  font-size: 11px;
  color: #aaa;
  margin-top: 1px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
