<template>
  <div class="str-wrap">
    <!-- 顶部 header -->
    <div class="str-header">
      <span class="str-back" @click="$emit('back')">‹</span>
      <span class="str-title">行程规划</span>
      <div class="str-header-actions">
        <el-button size="small" :loading="regenerating" plain @click="$emit('regenerate')">重新生成</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="$emit('save')">保存</el-button>
      </div>
    </div>

    <!-- 概览区 -->
    <div class="str-overview">
      <!-- 天数 tab -->
      <div class="str-day-tabs">
        <span
          :class="['str-dtab', { active: activeDay === 0 }]"
          @click="activeDay = 0"
        >总览</span>
        <span
          v-for="day in result"
          :key="day.day"
          :class="['str-dtab', { active: activeDay === day.day }]"
          @click="activeDay = day.day; scrollToDay(day.day)"
        >第{{ day.day }}天</span>
      </div>

      <!-- 概览卡片 -->
      <div class="str-overview-cards">
        <div
          v-for="day in result"
          :key="day.day"
          v-show="activeDay === 0 || activeDay === day.day"
          class="str-ov-card"
          @click="activeDay = day.day; scrollToDay(day.day)"
        >
          <span class="str-ov-day">第{{ day.day }}天</span>
          <span class="str-ov-date" v-if="day.date">{{ formatDateShort(day.date) }}</span>
          <span class="str-ov-sep">·</span>
          <span class="str-ov-summary">{{ daySummary(day.items) }}</span>
        </div>
      </div>
    </div>

    <!-- 时间线主体 -->
    <div class="str-body" ref="bodyRef">
      <TripDay
        v-for="day in result"
        :key="day.day"
        :day="day"
        @navigate="handleNavigate"
        @dayRef="({ day: d, el }) => { if (el) dayRefs[d] = el }"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import TripDay from './TripDay.vue'

const props = defineProps({
  result: { type: Array, default: () => [] },
  regenerating: { type: Boolean, default: false },
  saving: { type: Boolean, default: false }
})

const emit = defineEmits(['regenerate', 'save', 'back'])

const activeDay = ref(0)
const bodyRef = ref(null)
const dayRefs = ref({})

watch(() => props.result, () => { activeDay.value = 0 })

const formatDateShort = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getMonth() + 1}/${d.getDate()}`
}

const daySummary = (items) => {
  if (!items?.length) return ''
  return items.slice(0, 4).map(i => i.name).join(' → ')
}

const scrollToDay = (day) => {
  const el = dayRefs.value[day]
  if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

const handleNavigate = (item) => {
  // 保存行程状态，返回时恢复
  sessionStorage.setItem('smartTripResult', JSON.stringify(props.result))
}
</script>

<style scoped>
.str-wrap {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f5f6f8;
  font-size: 14px;
  color: #333;
  line-height: 1.6;
}

/* Header */
.str-header {
  display: flex;
  align-items: center;
  padding: 10px 14px;
  background: #fff;
  border-bottom: 1px solid #eee;
  flex-shrink: 0;
  gap: 8px;
}
.str-back {
  font-size: 24px;
  color: #333;
  cursor: pointer;
  padding: 0 6px 0 0;
  line-height: 1;
  flex-shrink: 0;
}
.str-title {
  font-size: 16px;
  font-weight: 600;
  color: #111;
  flex: 1;
}
.str-header-actions {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}

/* 概览区 */
.str-overview {
  background: #fff;
  border-bottom: 1px solid #eee;
  flex-shrink: 0;
}

.str-day-tabs {
  display: flex;
  padding: 0 16px;
  overflow-x: auto;
  border-bottom: 1px solid #f0f0f0;
}
.str-dtab {
  padding: 8px 14px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  white-space: nowrap;
  transition: all 0.2s;
  flex-shrink: 0;
}
.str-dtab.active {
  color: #1677ff;
  border-bottom-color: #1677ff;
  font-weight: 600;
}

.str-overview-cards {
  padding: 8px 16px 10px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.str-ov-card {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #f8f9fa;
  border-radius: 10px;
  padding: 8px 12px;
  cursor: pointer;
  transition: background 0.15s;
  flex-wrap: wrap;
}
.str-ov-card:hover { background: #eef3ff; }
.str-ov-day {
  font-size: 13px;
  font-weight: 600;
  color: #1677ff;
  flex-shrink: 0;
}
.str-ov-date {
  font-size: 12px;
  color: #999;
  flex-shrink: 0;
}
.str-ov-sep { color: #ccc; font-size: 12px; }
.str-ov-summary {
  font-size: 12px;
  color: #555;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  min-width: 0;
}

/* 主体 */
.str-body {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0 20px;
}
</style>
