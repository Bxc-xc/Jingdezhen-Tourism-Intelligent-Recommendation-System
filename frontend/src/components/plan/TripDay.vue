<template>
  <div class="td-block" :ref="el => el && emit('dayRef', { day: day.day, el })">
    <!-- 天标题 -->
    <div class="td-day-hd">
      <div class="td-dot"></div>
      <span class="td-day-label">第{{ day.day }}天</span>
      <span class="td-day-date" v-if="day.date">{{ formatDate(day.date) }}</span>
    </div>
    <!-- 备注 -->
    <div class="td-day-note" v-if="day.note">{{ day.note }}</div>

    <!-- 地点 + 交通 -->
    <div class="td-items">
      <template v-for="(item, idx) in day.items" :key="idx">
        <SpotCard
          :item="item"
          :index="idx"
          @navigate="emit('navigate', $event)"
        />
        <TransportLine
          v-if="idx < day.items.length - 1"
          :fromLat="item.lat"
          :fromLng="item.lng"
          :toLat="day.items[idx + 1].lat"
          :toLng="day.items[idx + 1].lng"
          :fromName="item.name"
          :toName="day.items[idx + 1].name"
        />
      </template>
    </div>
  </div>
</template>

<script setup>
import SpotCard from './SpotCard.vue'
import TransportLine from './TransportLine.vue'

const props = defineProps({
  day: { type: Object, required: true }
})
const emit = defineEmits(['navigate', 'dayRef'])

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const wds = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return `${d.getMonth() + 1}月${d.getDate()}日 ${wds[d.getDay()]}`
}
</script>

<style scoped>
.td-block { margin-bottom: 4px; }

/* 天标题 */
.td-day-hd {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 16px 10px;
  position: relative;
}
.td-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #1677ff;
  flex-shrink: 0;
  box-shadow: 0 0 0 3px rgba(22,119,255,0.15);
}
.td-day-label {
  font-size: 16px;
  font-weight: 700;
  color: #111;
}
.td-day-date {
  font-size: 12px;
  color: #999;
}

/* 备注 */
.td-day-note {
  font-size: 12px;
  color: #888;
  padding: 0 16px 10px 34px;
  line-height: 1.5;
}

/* 地点列表 */
.td-items {
  padding: 0 16px 8px;
}
</style>
