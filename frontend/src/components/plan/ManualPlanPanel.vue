<template>
  <div class="mpp-sidebar" :class="{ 'mpp-sidebar--expanded': expanded }" ref="sidebarRef" @wheel.passive="onSidebarWheel">

    <!-- ── 顶部标题栏 ── -->
    <div class="mpp-topbar">
      <!-- ⇄ 返回上一层 -->
      <span class="mpp-back-icon" title="返回" @click="$emit('close')">⇄</span>

      <!-- 线路名称 + 编辑按钮 -->
      <div class="mpp-title-area">
        <template v-if="editingPlanName">
          <el-input
            v-model="planName"
            size="small"
            class="mpp-planname-input"
            @blur="editingPlanName = false"
            @keyup.enter="editingPlanName = false"
            autofocus
          />
        </template>
        <template v-else>
          <span class="mpp-plan-name">{{ planName }}</span>
          <span class="mpp-edit-icon" title="编辑名称" @click="editingPlanName = true">✎</span>
        </template>
      </div>

      <!-- 出发日期 -->
      <div class="mpp-date-area" @click="showDatePanel = !showDatePanel">
        <span class="mpp-date-icon">📅</span>
        <span class="mpp-date-label">{{ startDate ? formatDate(startDate) : '设置出发日期' }}</span>
      </div>
    </div>

    <!-- ── 出发日期面板 ── -->
    <transition name="date-panel-slide">
      <div v-if="showDatePanel" class="mpp-date-panel">
        <div class="mpp-date-panel-header">
          <span class="mpp-date-panel-title">选择出发日期</span>
          <span class="mpp-date-panel-close" @click="showDatePanel = false">✕</span>
        </div>
        <div class="mpp-cal-nav-row">
          <button class="mpp-cal-nav" @click="prevCalMonth">‹</button>
          <span class="mpp-cal-month-title">{{ calYear }}年{{ calMonth + 1 }}月</span>
          <button class="mpp-cal-nav" @click="nextCalMonth">›</button>
        </div>
        <div class="mpp-cal-weekdays">
          <span v-for="w in ['日','一','二','三','四','五','六']" :key="w">{{ w }}</span>
        </div>
        <div class="mpp-cal-days">
          <span
            v-for="(cell, i) in calCells"
            :key="i"
            :class="calDayClass(cell)"
            @click="cell.date && !isCalDisabled(cell.date) && selectStartDate(cell.date)"
          >{{ cell.day }}</span>
        </div>
        <div class="mpp-date-panel-footer">
          <el-button size="small" @click="startDate = ''; showDatePanel = false">清除</el-button>
          <el-button size="small" type="primary" @click="showDatePanel = false">确定</el-button>
        </div>
      </div>
    </transition>

    <!-- ── Tab 行 ── -->
    <div class="mpp-tabs-row">
      <span :class="['mpp-tab', { active: activeTab === 'detail' }]" @click="activeTab = 'detail'">行程详情</span>
    </div>

    <!-- ── 内容区（侧边垂直 / 展开横向） ── -->
    <div v-if="!expanded" class="mpp-body-wrap">
      <!-- 竖向分段导航 -->
      <div class="mpp-seg-nav">
        <span
          v-for="seg in segments"
          :key="seg.key"
          :class="['mpp-seg', { active: activeSeg === seg.key }]"
          @click="scrollToSeg(seg.key)"
        >{{ seg.label }}</span>
        <span class="mpp-seg-add" @click="addDay" title="添加天数">
          <el-icon><Plus /></el-icon>
        </span>
      </div>

      <!-- 滚动内容 -->
      <div class="mpp-body-vertical" ref="sidebarBodyRef">

      <!-- 总览 -->
      <div class="mpp-section-block" id="seg-overview">
        <div class="mpp-block-title">总览</div>
        <div class="mpp-overview-stats">
          <div class="mpp-stat-item">
            <span class="mpp-stat-num">{{ days.length }}</span>
            <span class="mpp-stat-label">天</span>
          </div>
          <div class="mpp-stat-divider"></div>
          <div class="mpp-stat-item">
            <span class="mpp-stat-num">{{ totalScenicCount }}</span>
            <span class="mpp-stat-label">个景点</span>
          </div>
          <div class="mpp-stat-divider"></div>
          <div class="mpp-stat-item">
            <span class="mpp-stat-num">{{ totalHotelCount }}</span>
            <span class="mpp-stat-label">处住宿</span>
          </div>
        </div>
        <div v-if="startDate" class="mpp-overview-dates">
          <span class="mpp-overview-date-item">📅 出发：{{ startDate }}</span>
          <span v-if="endDateStr" class="mpp-overview-date-item">🏁 返回：{{ endDateStr }}</span>
        </div>
      </div>

      <!-- 待安排 -->
      <div class="mpp-section-block" id="seg-unscheduled">
        <div class="mpp-block-title">待安排</div>
        <div class="mpp-block-card">
          <div v-for="(item, i) in unscheduled" :key="i" class="mpp-list-item mpp-list-item--editable">
            <template v-if="editingUnscheduled === i">
              <el-input
                v-model="unscheduled[i]"
                size="small"
                class="mpp-inline-input"
                @blur="editingUnscheduled = -1"
                @keyup.enter="editingUnscheduled = -1"
                autofocus
              />
            </template>
            <template v-else>
              <span class="mpp-item-text" @click="editingUnscheduled = i">{{ item }}</span>
              <span class="mpp-item-del" @click="unscheduled.splice(i, 1)">✕</span>
            </template>
          </div>
          <div class="mpp-add-row" @click="addUnscheduled">
            <span class="mpp-plus-text">+</span> 添加待安排的地点
          </div>
        </div>
      </div>

      <!-- 每天 -->
      <div v-for="(day, i) in days" :key="i" class="mpp-section-block" :id="'seg-day-' + i">
        <div class="mpp-day-header">
          <div class="mpp-day-title-wrap">
            <span class="mpp-day-bar"></span>
            <el-input
              v-if="editingDayTitle === i"
              v-model="day.title"
              size="small"
              class="mpp-day-title-input"
              @blur="editingDayTitle = -1"
              @keyup.enter="editingDayTitle = -1"
              autofocus
            />
            <span v-else class="mpp-day-title">
              第{{ i + 1 }}天 · {{ day.title || '编辑标题' }}
            </span>
          </div>
          <el-dropdown trigger="click" @command="handleDayCmd($event, i)">
            <span class="mpp-day-more">···</span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="edit">编辑标题</el-dropdown-item>
                <el-dropdown-item command="delete" divided>删除此天</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <div class="mpp-day-date" v-if="day.date">{{ day.date }}</div>
        <div class="mpp-day-note-wrap">
          <el-input
            v-if="editingDayNote === i"
            v-model="day.note"
            size="small"
            placeholder="添加备注..."
            class="mpp-day-note-input"
            @blur="editingDayNote = -1"
            @keyup.enter="editingDayNote = -1"
            autofocus
          />
          <div v-else class="mpp-day-note-hint" @click="editingDayNote = i">
            {{ day.note || '添加备注' }}
          </div>
        </div>

        <div class="mpp-block-card mpp-day-card">
          <!-- 交通 -->
          <div class="mpp-day-sec">
            <div class="mpp-day-sec-hd">
              <span class="mpp-sec-icon">🚌</span>
              <span class="mpp-sec-title">如何到达?</span>
            </div>
            <div v-for="(t, ti) in day.transport" :key="ti" class="mpp-list-item mpp-list-item--indent mpp-list-item--editable">
              <template v-if="editingItem.dayIdx === i && editingItem.type === 'transport' && editingItem.itemIdx === ti">
                <el-input v-model="day.transport[ti]" size="small" class="mpp-inline-input"
                  @blur="clearEditingItem" @keyup.enter="clearEditingItem" autofocus />
              </template>
              <template v-else>
                <span class="mpp-item-text" @click="setEditingItem(i, 'transport', ti)">{{ t }}</span>
                <span class="mpp-item-del" @click="day.transport.splice(ti, 1)">✕</span>
              </template>
            </div>
            <div class="mpp-add-row mpp-add-row--indent" @click="addItem(day, 'transport')">
              <span class="mpp-plus-text">+</span> 交通
            </div>
          </div>
          <!-- 景点 -->
          <div class="mpp-day-sec">
            <div class="mpp-day-sec-hd">
              <span class="mpp-sec-icon">🎯</span>
              <span class="mpp-sec-title">玩什么?</span>
            </div>
            <div v-for="(s, si) in day.scenic" :key="si" class="mpp-list-item mpp-list-item--indent mpp-list-item--editable">
              <template v-if="editingItem.dayIdx === i && editingItem.type === 'scenic' && editingItem.itemIdx === si">
                <el-autocomplete
                  v-model="day.scenic[si].name"
                  :fetch-suggestions="queryScenicSuggestions"
                  placeholder="搜索景点、餐饮、陶瓷工坊、市集..."
                  size="small"
                  class="mpp-inline-input"
                  :trigger-on-focus="false"
                  @select="(val) => onScenicSelect(day, si, val)"
                  @blur="clearEditingItem"
                >
                  <template #default="{ item }">
                    <div class="mpp-suggest-row">
                      <span class="mpp-suggest-name">{{ item.value }}</span>
                      <span class="mpp-suggest-tag" :data-type="item._type">{{ item.typeLabel }}</span>
                    </div>
                    <div v-if="item.address" class="mpp-suggest-addr">{{ item.address }}</div>
                  </template>
                </el-autocomplete>
              </template>
              <template v-else>
                <span class="mpp-item-text" @click="setEditingItem(i, 'scenic', si)">{{ getScenicName(s) }}</span>
                <span class="mpp-item-del" @click="day.scenic.splice(si, 1)">✕</span>
              </template>
            </div>
            <div class="mpp-add-row mpp-add-row--indent" @click="addItem(day, 'scenic')">
              <span class="mpp-plus-text">+</span> 景点
            </div>
          </div>
          <!-- 住宿 -->
          <div class="mpp-day-sec">
            <div class="mpp-day-sec-hd">
              <span class="mpp-sec-icon">🏨</span>
              <span class="mpp-sec-title">住哪里?</span>
            </div>
            <div v-for="(h, hi) in day.hotel" :key="hi" class="mpp-list-item mpp-list-item--indent mpp-list-item--editable">
              <template v-if="editingItem.dayIdx === i && editingItem.type === 'hotel' && editingItem.itemIdx === hi">
                <el-autocomplete
                  v-model="day.hotel[hi].name"
                  :fetch-suggestions="queryHotelSuggestions"
                  placeholder="搜索酒店..."
                  size="small"
                  class="mpp-inline-input"
                  :trigger-on-focus="false"
                  @select="(val) => onHotelSelect(day, hi, val)"
                  @blur="clearEditingItem"
                >
                  <template #default="{ item }">
                    <div class="mpp-suggest-row">
                      <span class="mpp-suggest-name">{{ item.value }}</span>
                      <span v-if="item.rating" class="mpp-suggest-tag" data-type="HOTEL">⭐ {{ item.rating }}</span>
                    </div>
                    <div v-if="item.address" class="mpp-suggest-addr">{{ item.address }}</div>
                  </template>
                </el-autocomplete>
              </template>
              <template v-else>
                <span class="mpp-item-text" @click="setEditingItem(i, 'hotel', hi)">{{ getHotelName(h) }}</span>
                <span class="mpp-item-del" @click="day.hotel.splice(hi, 1)">✕</span>
              </template>
            </div>
            <div class="mpp-add-row mpp-add-row--indent" @click="addItem(day, 'hotel')">
              <span class="mpp-plus-text">+</span> 住宿
            </div>
          </div>
        </div>
      </div>

      <!-- 备注 -->
      <div class="mpp-section-block" id="seg-notes">
        <div class="mpp-block-title">备注</div>
        <div class="mpp-block-card">
          <el-input
            v-model="notes"
            type="textarea"
            :rows="4"
            placeholder="在这里记录你的旅行计划、关注的信息..."
            resize="none"
            class="mpp-notes-ta"
          />
        </div>
      </div>
    </div>
    </div>

    <!-- ── 展开横向看板 ── -->
    <div v-else class="mpp-body-board" ref="boardRef">
      <!-- 总览列 -->
      <div class="mpp-col" id="col-overview">
        <div class="mpp-col-hd">
          <span class="mpp-col-title">总览</span>
          <el-icon class="mpp-col-pin"><Star /></el-icon>
        </div>
        <div class="mpp-overview-map">
          <el-icon class="mpp-map-icon"><Location /></el-icon>
          <span>在地图上查看</span>
        </div>
      </div>

      <!-- 待安排列 -->
      <div class="mpp-col" id="col-unscheduled">
        <div class="mpp-col-hd">
          <span class="mpp-col-title">待安排</span>
          <el-icon class="mpp-col-pin"><Star /></el-icon>
        </div>
        <div v-for="(item, i) in unscheduled" :key="i" class="mpp-list-item mpp-list-item--editable" style="padding: 8px 14px">
          <template v-if="editingUnscheduled === i">
            <el-input v-model="unscheduled[i]" size="small" class="mpp-inline-input" @blur="editingUnscheduled = -1" @keyup.enter="editingUnscheduled = -1" autofocus />
          </template>
          <template v-else>
            <span class="mpp-item-text" @click="editingUnscheduled = i">{{ item }}</span>
            <span class="mpp-item-del" @click="unscheduled.splice(i, 1)">✕</span>
          </template>
        </div>
        <div class="mpp-add-row" style="padding: 8px 14px" @click="addUnscheduled">
          <span class="mpp-plus-text">+</span> 添加待安排地点
        </div>
      </div>

      <!-- 每天列 -->
      <div v-for="(day, i) in days" :key="i" class="mpp-col mpp-col--day" :id="'col-day-' + i">
        <div class="mpp-col-hd">
          <el-input v-if="editingDayTitle === i" v-model="day.title" size="small" class="mpp-day-title-input" @blur="editingDayTitle = -1" @keyup.enter="editingDayTitle = -1" autofocus />
          <span v-else class="mpp-col-title" @click="editingDayTitle = i">第{{ i + 1 }}天 · {{ day.title || '编辑标题' }}</span>
          <div class="mpp-col-actions">
            <el-icon @click="removeDay(i)"><Close /></el-icon>
            <el-icon><Star /></el-icon>
          </div>
        </div>
        <div class="mpp-day-date" v-if="day.date" style="padding: 4px 14px; font-size:12px; color:#999">{{ day.date }}</div>
        <div class="mpp-day-note-wrap" style="padding: 0 14px 4px">
          <el-input v-if="editingDayNote === i" v-model="day.note" size="small" placeholder="添加备注..." @blur="editingDayNote = -1" @keyup.enter="editingDayNote = -1" autofocus />
          <div v-else class="mpp-day-note-hint" @click="editingDayNote = i">{{ day.note || '添加备注' }}</div>
        </div>
        <div class="mpp-board-day-sec">
          <div class="mpp-day-sec-hd"><span class="mpp-sec-icon">🚌</span><span class="mpp-sec-title transport">如何到达?</span></div>
          <div v-for="(t, ti) in day.transport" :key="ti" class="mpp-list-item mpp-list-item--indent mpp-list-item--editable">
            <template v-if="editingItem.dayIdx === i && editingItem.type === 'transport' && editingItem.itemIdx === ti">
              <el-input v-model="day.transport[ti]" size="small" class="mpp-inline-input" @blur="clearEditingItem" @keyup.enter="clearEditingItem" autofocus />
            </template>
            <template v-else>
              <span class="mpp-item-text" @click="setEditingItem(i, 'transport', ti)">{{ t }}</span>
              <span class="mpp-item-del" @click="day.transport.splice(ti, 1)">✕</span>
            </template>
          </div>
          <div class="mpp-add-row mpp-add-row--indent" @click="addItem(day, 'transport')"><span class="mpp-plus-text">+</span> 交通</div>
        </div>
        <div class="mpp-board-day-sec">
          <div class="mpp-day-sec-hd"><span class="mpp-sec-icon">🎯</span><span class="mpp-sec-title scenic">玩什么?</span></div>
          <div v-for="(s, si) in day.scenic" :key="si" class="mpp-list-item mpp-list-item--indent mpp-list-item--editable">
            <template v-if="editingItem.dayIdx === i && editingItem.type === 'scenic' && editingItem.itemIdx === si">
              <el-autocomplete
                v-model="day.scenic[si].name"
                :fetch-suggestions="queryScenicSuggestions"
                placeholder="搜索景点、餐饮、陶瓷工坊、市集..."
                size="small"
                class="mpp-inline-input"
                :trigger-on-focus="false"
                @select="(val) => onScenicSelect(day, si, val)"
                @blur="clearEditingItem"
              >
                <template #default="{ item }">
                  <div class="mpp-suggest-row">
                    <span class="mpp-suggest-name">{{ item.value }}</span>
                    <span class="mpp-suggest-tag" :data-type="item._type">{{ item.typeLabel }}</span>
                  </div>
                  <div v-if="item.address" class="mpp-suggest-addr">{{ item.address }}</div>
                </template>
              </el-autocomplete>
            </template>
            <template v-else>
              <span class="mpp-item-text" @click="setEditingItem(i, 'scenic', si)">{{ getScenicName(s) }}</span>
              <span class="mpp-item-del" @click="day.scenic.splice(si, 1)">✕</span>
            </template>
          </div>
          <div class="mpp-add-row mpp-add-row--indent" @click="addItem(day, 'scenic')"><span class="mpp-plus-text">+</span> 景点</div>
        </div>
        <div class="mpp-board-day-sec">
          <div class="mpp-day-sec-hd"><span class="mpp-sec-icon">🏨</span><span class="mpp-sec-title hotel">住哪里?</span></div>
          <div v-for="(h, hi) in day.hotel" :key="hi" class="mpp-list-item mpp-list-item--indent mpp-list-item--editable">
            <template v-if="editingItem.dayIdx === i && editingItem.type === 'hotel' && editingItem.itemIdx === hi">
              <el-autocomplete
                v-model="day.hotel[hi].name"
                :fetch-suggestions="queryHotelSuggestions"
                placeholder="搜索酒店..."
                size="small"
                class="mpp-inline-input"
                :trigger-on-focus="false"
                @select="(val) => onHotelSelect(day, hi, val)"
                @blur="clearEditingItem"
              >
                <template #default="{ item }">
                  <div class="mpp-suggest-row">
                    <span class="mpp-suggest-name">{{ item.value }}</span>
                    <span v-if="item.rating" class="mpp-suggest-tag" data-type="HOTEL">⭐ {{ item.rating }}</span>
                  </div>
                  <div v-if="item.address" class="mpp-suggest-addr">{{ item.address }}</div>
                </template>
              </el-autocomplete>
            </template>
            <template v-else>
              <span class="mpp-item-text" @click="setEditingItem(i, 'hotel', hi)">{{ getHotelName(h) }}</span>
              <span class="mpp-item-del" @click="day.hotel.splice(hi, 1)">✕</span>
            </template>
          </div>
          <div class="mpp-add-row mpp-add-row--indent" @click="addItem(day, 'hotel')"><span class="mpp-plus-text">+</span> 住宿</div>
        </div>
      </div>

      <!-- 备注列 -->
      <div class="mpp-col" id="col-notes">
        <div class="mpp-col-hd">
          <span class="mpp-col-title">备注</span>
          <el-icon class="mpp-col-pin"><Star /></el-icon>
        </div>
        <el-input v-model="notes" type="textarea" :rows="8" placeholder="在这里记录你的旅行计划..." resize="none" class="mpp-notes-board" />
      </div>

      <!-- 添加天 -->
      <div class="mpp-add-day-col" @click="addDay">
        <el-icon><Plus /></el-icon>
        <span>添加第{{ days.length + 1 }}天</span>
      </div>
    </div>

    <!-- ── 底部 ── -->
    <div class="mpp-footer">
      <el-button size="small" @click="$emit('close')">关闭</el-button>
      <el-button size="small" type="primary" :loading="saving" @click="savePlan">保存行程</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, reactive, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Close, Star, Location, Plus } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'
import { createManualPlan } from '../../api/plan'
import { globalSearch } from '../../api/search'

const emit = defineEmits(['close', 'saved', 'expand'])
const userStore = useUserStore()

// ── 状态 ──
const expanded = ref(false)

watch(expanded, (val) => emit('expand', val))
const activeTab = ref('detail')
const activeSeg = ref('overview')
const editingDayTitle = ref(-1)
const editingDayNote = ref(-1)
const editingPlanName = ref(false)
const editingUnscheduled = ref(-1)
const planName = ref('景德镇陶瓷文化之旅')
const startDate = ref('')
const showDatePanel = ref(false)
const saving = ref(false)
const notes = ref('')
const unscheduled = ref([])
const sidebarRef = ref(null)
const sidebarBodyRef = ref(null)
const boardRef = ref(null)

// 行内编辑状态：{ dayIdx, type, itemIdx }
const editingItem = reactive({ dayIdx: -1, type: '', itemIdx: -1 })
const setEditingItem = (dayIdx, type, itemIdx) => {
  // 兼容旧字符串格式的景点/酒店数据
  if (type === 'scenic') {
    const val = days.value[dayIdx].scenic[itemIdx]
    if (typeof val === 'string') {
      days.value[dayIdx].scenic[itemIdx] = { name: val, id: null, lat: null, lng: null }
    }
  }
  if (type === 'hotel') {
    const val = days.value[dayIdx].hotel[itemIdx]
    if (typeof val === 'string') {
      days.value[dayIdx].hotel[itemIdx] = { name: val, id: null }
    }
  }
  editingItem.dayIdx = dayIdx
  editingItem.type = type
  editingItem.itemIdx = itemIdx
}
const clearEditingItem = () => {
  editingItem.dayIdx = -1
  editingItem.type = ''
  editingItem.itemIdx = -1
}

// 总览统计
const totalScenicCount = computed(() => days.value.reduce((sum, d) => sum + d.scenic.length, 0))
const totalHotelCount = computed(() => days.value.reduce((sum, d) => sum + d.hotel.length, 0))
const endDateStr = computed(() => {
  if (!startDate.value || !days.value.length) return ''
  const base = new Date(startDate.value)
  base.setDate(base.getDate() + days.value.length - 1)
  return `${base.getFullYear()}-${String(base.getMonth()+1).padStart(2,'0')}-${String(base.getDate()).padStart(2,'0')}`
})

// 把整个面板的 wheel 事件转发给内容滚动区，确保鼠标在任意子元素上都能滚动
const onSidebarWheel = (e) => {
  const target = expanded.value ? boardRef.value : sidebarBodyRef.value
  if (!target) return
  // 如果 wheel 事件已经发生在滚动容器内部则不处理（避免双重滚动）
  if (target.contains(e.target)) return
  target.scrollTop += e.deltaY
}

const days = ref([
  { title: '编辑标题', transport: [], scenic: [], hotel: [], date: '', note: '' }
])

// ── 日历 ──
const today = new Date()
const calBase = ref({ year: today.getFullYear(), month: today.getMonth() })
const calYear = computed(() => calBase.value.year)
const calMonth = computed(() => calBase.value.month)

function buildCalCells(year, month) {
  const first = new Date(year, month, 1).getDay()
  const total = new Date(year, month + 1, 0).getDate()
  const cells = []
  for (let i = 0; i < first; i++) cells.push({ day: '', date: null })
  for (let d = 1; d <= total; d++) {
    const mm = String(month + 1).padStart(2, '0')
    const dd = String(d).padStart(2, '0')
    cells.push({ day: d, date: `${year}-${mm}-${dd}`, weekday: new Date(year, month, d).getDay() })
  }
  return cells
}

const calCells = computed(() => buildCalCells(calYear.value, calMonth.value))

const prevCalMonth = () => {
  const { year, month } = calBase.value
  calBase.value = month === 0 ? { year: year - 1, month: 11 } : { year, month: month - 1 }
}
const nextCalMonth = () => {
  const { year, month } = calBase.value
  calBase.value = month === 11 ? { year: year + 1, month: 0 } : { year, month: month + 1 }
}

const todayStr = computed(() => {
  const y = today.getFullYear(), m = String(today.getMonth() + 1).padStart(2, '0'), d = String(today.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
})

const isCalDisabled = (date) => date < todayStr.value

const calDayClass = (cell) => {
  if (!cell.date) return 'mpp-cal-day mpp-cal-day--empty'
  const cls = ['mpp-cal-day']
  if (cell.weekday === 0 || cell.weekday === 6) cls.push('mpp-cal-day--weekend')
  if (cell.date === startDate.value) cls.push('mpp-cal-day--selected')
  if (isCalDisabled(cell.date)) cls.push('mpp-cal-day--disabled')
  return cls.join(' ')
}

// 选择出发日期，自动填充到每天
const selectStartDate = (date) => {
  startDate.value = date
  // 自动填充每天日期
  const base = new Date(date)
  days.value.forEach((day, i) => {
    const d = new Date(base)
    d.setDate(base.getDate() + i)
    day.date = `${d.getFullYear()}/${String(d.getMonth()+1).padStart(2,'0')}/${String(d.getDate()).padStart(2,'0')}`
  })
}

// 当天数变化时也同步日期
watch(() => days.value.length, () => {
  if (!startDate.value) return
  const base = new Date(startDate.value)
  days.value.forEach((day, i) => {
    const d = new Date(base)
    d.setDate(base.getDate() + i)
    day.date = `${d.getFullYear()}/${String(d.getMonth()+1).padStart(2,'0')}/${String(d.getDate()).padStart(2,'0')}`
  })
})

const formatDate = (date) => {
  if (!date) return ''
  const [y, m, d] = date.split('-')
  return `${m}月${d}日出发`
}

// ── 分段导航 ──
const segments = computed(() => [
  { key: 'overview', label: '总览' },
  { key: 'unscheduled', label: '待安排' },
  ...days.value.map((_, i) => ({ key: 'day-' + i, label: '第' + (i + 1) + '天' })),
  { key: 'notes', label: '备注' }
])

const scrollToSeg = (key) => {
  activeSeg.value = key
  if (expanded.value) return // 展开模式下导航列已高亮，不需要滚动
  const idMap = {
    overview: 'seg-overview',
    unscheduled: 'seg-unscheduled',
    notes: 'seg-notes'
  }
  const elId = idMap[key] || ('seg-' + key)
  const el = document.getElementById(elId)
  const container = sidebarBodyRef.value
  if (el && container) {
    const containerTop = container.getBoundingClientRect().top
    const elTop = el.getBoundingClientRect().top
    container.scrollBy({ top: elTop - containerTop - 8, behavior: 'smooth' })
  }
}

// ── 操作 ──
const addDay = () => {
  const newDay = { title: '编辑标题', transport: [], scenic: [], hotel: [], date: '', note: '' }
  days.value.push(newDay)
}

const removeDay = (i) => {
  if (days.value.length <= 1) { ElMessage.warning('至少保留一天'); return }
  days.value.splice(i, 1)
}

const handleDayCmd = (cmd, i) => {
  if (cmd === 'delete') removeDay(i)
  if (cmd === 'edit') editingDayTitle.value = i
}

const addUnscheduled = () => {
  unscheduled.value.push('')
  nextTick(() => { editingUnscheduled.value = unscheduled.value.length - 1 })
}

const addItem = (day, type) => {
  if (type === 'scenic') {
    day[type].push({ name: '', id: null, lat: null, lng: null })
  } else if (type === 'hotel') {
    day[type].push({ name: '', id: null })
  } else {
    day[type].push('')
  }
  const dayIdx = days.value.indexOf(day)
  nextTick(() => setEditingItem(dayIdx, type, day[type].length - 1))
}

// 检查景点是否已在当天存在（按 id 或名称去重）
const isScenicDuplicate = (day, name, id, ignoreIdx = -1) => {
  return day.scenic.some((s, idx) => {
    if (idx === ignoreIdx) return false
    if (!s || (!s.name && !s.id)) return false
    if (id && s.id && s.id === id) return true
    return s.name === name
  })
}

const scenicSearchCache = ref({})
const queryScenicSuggestions = async (keyword, cb) => {
  if (!keyword || keyword.length < 1) { cb([]); return }
  if (scenicSearchCache.value[keyword]) { cb(scenicSearchCache.value[keyword]); return }
  try {
    const res = await globalSearch(keyword)
    const d = res?.data || {}
    const typeLabel = { SCENIC: '景点', FOOD: '餐饮', CERAMIC: '陶瓷工坊', MARKETPLACE: '陶瓷市集' }
    const suggestions = [
      ...(d.scenic || []).map(s => ({ ...s, _type: 'SCENIC' })),
      ...(d.ceramic || []).map(s => ({ ...s, _type: 'CERAMIC' })),
      ...(d.food || []).map(s => ({ ...s, _type: 'FOOD' })),
      ...(d.marketplace || []).map(s => ({ ...s, _type: 'MARKETPLACE' }))
    ].map(s => ({
      value: s.name,
      id: s.id,
      lat: s.lat || null,
      lng: s.lng || null,
      address: s.address || '',
      description: s.description || '',
      image: s.image || s.cover || s.avatar || '',
      typeLabel: typeLabel[s._type] || s._type,
      _type: s._type
    }))
    scenicSearchCache.value[keyword] = suggestions
    cb(suggestions)
  } catch { cb([]) }
}

// 住哪里：搜索系统酒店
const hotelSearchCache = ref({})
const queryHotelSuggestions = async (keyword, cb) => {
  if (!keyword || keyword.length < 1) { cb([]); return }
  if (hotelSearchCache.value[keyword]) { cb(hotelSearchCache.value[keyword]); return }
  try {
    const res = await globalSearch(keyword)
    const list = res?.data?.hotel || []
    const suggestions = list.map(h => ({
      value: h.name,
      id: h.id,
      address: h.address || '',
      rating: h.rating || null,
      description: h.description || '',
      image: h.image || h.cover || h.avatar || ''
    }))
    hotelSearchCache.value[keyword] = suggestions
    cb(suggestions)
  } catch { cb([]) }
}

const onScenicSelect = (day, idx, selected) => {
  // 去重：同一景点不能在同一天重复添加（忽略当前编辑项）
  if (isScenicDuplicate(day, selected.value, selected.id, idx)) {
    ElMessage.warning('该景点已在当天行程中')
    return
  }

  const selectedType = (selected._type || 'SCENIC').toUpperCase()

  // 如果选择的是餐饮，自动放到「如何到达」看起来像“跳到交通”，这里应放到餐饮/景点容器（当前UI无独立餐饮区，统一归景点）
  day.scenic[idx] = {
    name: selected.value,
    id: selected.id,
    lat: selected.lat,
    lng: selected.lng,
    category: selectedType,
    description: selected.description || '',
    image: selected.image || ''
  }
  clearEditingItem()
}

const onHotelSelect = (day, idx, selected) => {
  day.hotel[idx] = {
    name: selected.value,
    id: selected.id,
    description: selected.description || '',
    image: selected.image || ''
  }
  clearEditingItem()
}

const getScenicName = (s) => (typeof s === 'string' ? s : s?.name || '')
const getHotelName = (h) => (typeof h === 'string' ? h : h?.name || '')

// ── 保存 ──
const savePlan = async () => {
  saving.value = true
  try {
    const planDetails = {
      days: days.value.map((d, i) => ({
        day: i + 1, title: d.title, date: d.date, note: d.note || '',
        schedule: [
          ...d.transport.filter(t => t).map(t => ({ type: 'TRANSPORT', title: t })),
          ...d.scenic.filter(s => s && (typeof s === 'string' ? s : s.name)).map(s => ({
            type: typeof s === 'object' && s.category ? s.category : 'SCENIC',
            title: typeof s === 'string' ? s : s.name,
            scenicId: typeof s === 'object' ? s.id : null,
            lat: typeof s === 'object' ? s.lat : null,
            lng: typeof s === 'object' ? s.lng : null,
            description: typeof s === 'object' ? (s.description || '') : '',
            image: typeof s === 'object' ? (s.image || '') : ''
          })),
          ...d.hotel.filter(h => h && (typeof h === 'string' ? h : h.name)).map(h => ({
            type: 'HOTEL',
            title: typeof h === 'string' ? h : h.name,
            hotelId: typeof h === 'object' ? h.id : null,
            description: typeof h === 'object' ? (h.description || '') : '',
            image: typeof h === 'object' ? (h.image || '') : ''
          }))
        ]
      })),
      unscheduled: unscheduled.value.filter(u => u),
      notes: notes.value
    }

    if (!userStore.user?.id) {
      ElMessage.warning('请先登录')
      saving.value = false
      return
    }

    const res = await createManualPlan({
      userId: userStore.user.id,
      title: planName.value || `手动创建行程 (${days.value.length}天)`,
      days: days.value.length,
      startDate: startDate.value || null,
      manualPlanDetails: JSON.stringify(planDetails)
    })

    if (res && (res.success || res.data)) {
      ElMessage.success('行程保存成功')
      // 兼容多种后端返回：{data:{id}}, {data: id}, {id}
      const savedPayload = {
        id: res?.data?.id || res?.data?.planId || res?.id || res?.planId ||
            (typeof res?.data === 'number' || typeof res?.data === 'string' ? res.data : undefined),
        raw: res
      }
      emit('saved', savedPayload)
      emit('close')
    } else {
      ElMessage.error(res?.message || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
/* ── 整体容器 ── */
.mpp-sidebar {
  display: flex;
  flex-direction: column;
  position: absolute;
  inset: 0;
  background: #fff;
  overflow: hidden;
}

/* 展开时撑满父容器宽度（左侧面板已是50%） */
.mpp-sidebar--expanded {
  /* 不改变宽度，依附左侧 */
}

/* ── 顶部标题栏 ── */
.mpp-topbar {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 14px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
  min-height: 48px;
}

.mpp-back-icon {
  font-size: 18px;
  color: #1677ff;
  cursor: pointer;
  flex-shrink: 0;
  padding: 2px 4px;
  border-radius: 4px;
  transition: background 0.15s;
}
.mpp-back-icon:hover { background: #f0f5ff; }

.mpp-title-area {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
  min-width: 0;
}

.mpp-plan-name {
  font-size: 14px;
  font-weight: 600;
  color: #222;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.mpp-edit-icon {
  font-size: 14px;
  color: #bbb;
  cursor: pointer;
  flex-shrink: 0;
  padding: 2px 4px;
  border-radius: 3px;
  transition: all 0.15s;
}
.mpp-edit-icon:hover { color: #1677ff; background: #f0f5ff; }

.mpp-planname-input { flex: 1; }
.mpp-planname-input :deep(.el-input__inner) { font-size: 14px; font-weight: 600; }

.mpp-date-area {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  padding: 3px 8px;
  border-radius: 6px;
  border: 1px solid #e8e8e8;
  flex-shrink: 0;
  transition: all 0.15s;
}
.mpp-date-area:hover { border-color: #1677ff; color: #1677ff; }

.mpp-date-icon { font-size: 13px; }
.mpp-date-label { font-size: 12px; color: #666; white-space: nowrap; }

.mpp-expand-btn {
  font-size: 16px;
  color: #aaa;
  cursor: pointer;
  flex-shrink: 0;
  padding: 3px 5px;
  border-radius: 4px;
  transition: color 0.15s;
  line-height: 1;
}
.mpp-expand-btn:hover { color: #1677ff; }

/* ── 日期面板 ── */
.mpp-date-panel {
  position: absolute;
  top: 48px;
  left: 0;
  right: 0;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  z-index: 100;
  padding: 12px 16px 14px;
}

.date-panel-slide-enter-active,
.date-panel-slide-leave-active { transition: all 0.2s ease; }
.date-panel-slide-enter-from,
.date-panel-slide-leave-to { opacity: 0; transform: translateY(-8px); }

.mpp-date-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.mpp-date-panel-title { font-size: 13px; font-weight: 600; color: #333; }
.mpp-date-panel-close { font-size: 14px; color: #aaa; cursor: pointer; }
.mpp-date-panel-close:hover { color: #333; }

.mpp-cal-nav-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.mpp-cal-month-title { font-size: 13px; font-weight: 600; color: #222; }
.mpp-cal-nav {
  background: none;
  border: none;
  font-size: 18px;
  color: #1677ff;
  cursor: pointer;
  padding: 2px 6px;
  border-radius: 4px;
  line-height: 1;
}
.mpp-cal-nav:hover { background: #f0f5ff; }

.mpp-cal-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-bottom: 4px;
}
.mpp-cal-weekdays span {
  text-align: center;
  font-size: 11px;
  color: #1677ff;
  padding: 3px 0;
}

.mpp-cal-days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px;
}

.mpp-cal-day {
  text-align: center;
  padding: 7px 2px;
  font-size: 13px;
  color: #222;
  cursor: pointer;
  border-radius: 5px;
  transition: all 0.12s;
  user-select: none;
}
.mpp-cal-day:hover:not(.mpp-cal-day--empty):not(.mpp-cal-day--disabled) { background: #e8f0fe; color: #1677ff; }
.mpp-cal-day--weekend { color: #1677ff; }
.mpp-cal-day--selected { background: #1677ff !important; color: #fff !important; }
.mpp-cal-day--disabled { color: #ccc; cursor: not-allowed; }
.mpp-cal-day--empty { cursor: default; }

.mpp-date-panel-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
}

/* ── Tab 行 ── */
.mpp-tabs-row {
  display: flex;
  align-items: center;
  padding: 0 16px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.mpp-tab {
  padding: 9px 4px;
  font-size: 14px;
  color: #888;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  margin-right: 16px;
  transition: all 0.2s;
  user-select: none;
}
.mpp-tab.active { color: #222; border-bottom-color: #222; font-weight: 500; }

/* ── 内容区包裹（竖向模式） ── */
.mpp-body-wrap {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: row;
  position: relative;
  overflow: hidden;
}

/* ── 分段导航（竖向） ── */
.mpp-seg-nav {
  width: 60px;
  flex-shrink: 0;
  flex: 0 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 10px 4px;
  border-right: 1px solid #f0f0f0;
  overflow-y: auto;
  overflow-x: hidden;
  scrollbar-width: thin;
  scrollbar-color: #c0c0c0 #f0f0f0;
  background: #fafafa;
  /* 关键：让高度被父容器约束而不是撑开 */
  position: absolute;
  top: 0;
  left: 0;
  bottom: 0;
}
.mpp-seg-nav::-webkit-scrollbar { width: 4px; }
.mpp-seg-nav::-webkit-scrollbar-track { background: #f0f0f0; border-radius: 2px; }
.mpp-seg-nav::-webkit-scrollbar-thumb { background: #c0c0c0; border-radius: 2px; }
.mpp-seg-nav::-webkit-scrollbar-thumb:hover { background: #999; }

.mpp-seg {
  width: 40px;
  padding: 6px 4px;
  border-radius: 8px;
  font-size: 11px;
  color: #666;
  cursor: pointer;
  border: 1px solid #e8e8e8;
  transition: all 0.2s;
  user-select: none;
  flex-shrink: 0;
  text-align: center;
  line-height: 1.3;
  word-break: break-all;
}
.mpp-seg:hover { border-color: #1677ff; color: #1677ff; background: #f0f5ff; }
.mpp-seg.active { background: #222; color: #fff; border-color: #222; }

.mpp-seg-add {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #1677ff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 14px;
  flex-shrink: 0;
  margin-top: 2px;
}

/* ── 垂直内容区 ── */
.mpp-body-vertical {
  flex: 1;
  min-height: 0;
  min-width: 0;
  margin-left: 60px;
  overflow-y: scroll;
  padding: 12px 14px;
  overscroll-behavior: contain;
  scrollbar-width: thin;
  scrollbar-color: #c0c0c0 #f5f5f5;
}
.mpp-body-vertical::-webkit-scrollbar { width: 6px; }
.mpp-body-vertical::-webkit-scrollbar-track { background: #f5f5f5; border-radius: 3px; }
.mpp-body-vertical::-webkit-scrollbar-thumb { background: #c0c0c0; border-radius: 3px; }
.mpp-body-vertical::-webkit-scrollbar-thumb:hover { background: #999; }

.mpp-section-block { margin-bottom: 18px; }

.mpp-block-title {
  font-size: 16px;
  font-weight: 700;
  color: #111;
  margin-bottom: 6px;
}

.mpp-block-card {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
}

.mpp-overview-card {
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: linear-gradient(135deg, #e8f4f8, #d4e8f0);
  font-size: 13px;
  color: #5a9ab5;
  cursor: pointer;
}
.mpp-map-icon { font-size: 20px; }

.mpp-list-item {
  padding: 7px 14px;
  font-size: 13px;
  color: #555;
  border-bottom: 1px solid #f5f5f5;
}
.mpp-list-item--indent { padding-left: 36px; }

.mpp-add-row {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 7px 14px;
  font-size: 13px;
  color: #1677ff;
  cursor: pointer;
  transition: background 0.15s;
}
.mpp-add-row:hover { background: #f0f5ff; }
.mpp-add-row--indent { padding-left: 36px; }

.mpp-plus-text { font-size: 15px; line-height: 1; }

/* 天标题 */
.mpp-day-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 2px;
}
.mpp-day-title-wrap { display: flex; align-items: center; gap: 8px; }
.mpp-day-bar { width: 3px; height: 16px; background: #1677ff; border-radius: 2px; flex-shrink: 0; }
.mpp-day-title { font-size: 16px; font-weight: 700; color: #111; }
.mpp-day-title-input { width: 150px; }
.mpp-day-date { font-size: 11px; color: #aaa; padding-left: 11px; margin-bottom: 2px; }
.mpp-day-more { font-size: 16px; color: #bbb; cursor: pointer; padding: 0 4px; letter-spacing: 2px; }
.mpp-day-more:hover { color: #666; }
.mpp-day-note-hint { font-size: 12px; color: #bbb; margin-bottom: 6px; padding-left: 11px; cursor: pointer; }
.mpp-day-note-hint:hover { color: #1677ff; }

.mpp-day-card { padding: 2px 0; }

.mpp-day-sec { border-bottom: 1px solid #f5f5f5; padding: 4px 0; }
.mpp-day-sec:last-child { border-bottom: none; }

.mpp-day-sec-hd { display: flex; align-items: center; gap: 6px; padding: 4px 14px; }
.mpp-sec-icon { font-size: 15px; }
.mpp-sec-title { font-size: 15px; font-weight: 700; color: #111; }
.mpp-sec-title.transport { color: #111; }
.mpp-sec-title.scenic { color: #111; }
.mpp-sec-title.hotel { color: #111; }

.mpp-list-item--editable {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}
.mpp-list-item--editable:hover { background: #f5f7ff; }

.mpp-item-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mpp-item-del {
  font-size: 11px;
  color: #ccc;
  cursor: pointer;
  flex-shrink: 0;
  padding: 2px 4px;
  border-radius: 3px;
  opacity: 0;
  transition: opacity 0.15s;
}
.mpp-list-item--editable:hover .mpp-item-del { opacity: 1; }
.mpp-item-del:hover { color: #ff4d4f; background: #fff1f0; }

.mpp-inline-input { flex: 1; }
.mpp-inline-input :deep(.el-input__inner) { font-size: 13px; height: 28px; }

/* 总览统计 */
.mpp-overview-stats {
  display: flex;
  align-items: center;
  gap: 0;
  padding: 12px 16px;
  background: #f8f9ff;
  border-radius: 8px;
  margin-bottom: 8px;
}
.mpp-stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}
.mpp-stat-num { font-size: 22px; font-weight: 700; color: #1677ff; line-height: 1.2; }
.mpp-stat-label { font-size: 11px; color: #999; margin-top: 2px; }
.mpp-stat-divider { width: 1px; height: 32px; background: #e8e8e8; }

.mpp-overview-dates {
  display: flex;
  gap: 16px;
  padding: 6px 0 2px;
}
.mpp-overview-date-item { font-size: 12px; color: #666; }

/* 天备注 */
.mpp-day-note-wrap { padding-left: 11px; margin-bottom: 6px; }

/* 备注 */
.mpp-notes-ta :deep(.el-textarea__inner) {
  border: none;
  padding: 10px 14px;
  font-size: 13px;
  color: #555;
  background: transparent;
}

/* ── 展开横向看板 ── */
.mpp-body-board {
  flex: 1;
  display: flex;
  overflow-x: auto;
  overflow-y: hidden;
  padding: 12px;
  background: #f5f6f8;
  gap: 10px;
}
.mpp-body-board::-webkit-scrollbar { height: 5px; }
.mpp-body-board::-webkit-scrollbar-thumb { background: #d0d0d0; border-radius: 3px; }

.mpp-col {
  width: 240px;
  min-width: 240px;
  background: #fff;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  flex-shrink: 0;
}
.mpp-col--day { width: 260px; min-width: 260px; }

.mpp-col-hd {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px 8px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}
.mpp-col-title { font-size: 13px; font-weight: 600; color: #333; cursor: pointer; }
.mpp-col-pin { color: #ccc; font-size: 14px; cursor: pointer; }
.mpp-col-pin:hover { color: #f5a623; }
.mpp-col-actions { display: flex; gap: 6px; color: #bbb; font-size: 14px; }
.mpp-col-actions .el-icon { cursor: pointer; }
.mpp-col-actions .el-icon:hover { color: #666; }

.mpp-overview-map {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  background: linear-gradient(135deg, #e8f4f8, #d4e8f0);
  margin: 10px;
  border-radius: 8px;
  min-height: 120px;
  cursor: pointer;
  font-size: 12px;
  color: #5a9ab5;
}

.mpp-board-day-sec { border-bottom: 1px solid #f5f5f5; padding: 4px 0; }
.mpp-board-day-sec:last-of-type { border-bottom: none; }

.mpp-notes-board { margin: 10px; width: calc(100% - 20px); }
.mpp-notes-board :deep(.el-textarea__inner) { border: none; background: transparent; font-size: 12px; color: #555; resize: none; padding: 0; }

.mpp-add-day-col {
  width: 100px;
  min-width: 100px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: 2px dashed #d0d0d0;
  border-radius: 10px;
  color: #aaa;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}
.mpp-add-day-col:hover { border-color: #1677ff; color: #1677ff; background: #f0f5ff; }
.mpp-add-day-col .el-icon { font-size: 18px; }

/* ── 搜索建议下拉样式 ── */
.mpp-suggest-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
}
.mpp-suggest-name {
  font-size: 13px;
  color: #222;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.mpp-suggest-tag {
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 10px;
  flex-shrink: 0;
  background: #e8f0fe;
  color: #1677ff;
}
.mpp-suggest-tag[data-type="FOOD"] { background: #fff7e6; color: #d46b08; }
.mpp-suggest-tag[data-type="CERAMIC"] { background: #f9f0ff; color: #722ed1; }
.mpp-suggest-tag[data-type="MARKETPLACE"] { background: #f6ffed; color: #389e0d; }
.mpp-suggest-tag[data-type="HOTEL"] { background: #e6f7ff; color: #0958d9; }
.mpp-suggest-addr {
  font-size: 11px;
  color: #aaa;
  margin-top: 1px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.mpp-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 8px 14px;
  border-top: 1px solid #f0f0f0;
  flex-shrink: 0;
}
</style>
