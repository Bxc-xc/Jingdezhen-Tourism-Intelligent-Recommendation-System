<template>
  <div class="tl-wrap">
    <div class="tl-stem"></div>
    <div class="tl-row">
      <span class="tl-icon"><el-icon><component :is="info.icon" /></el-icon></span>
      <span class="tl-text">{{ info.label }}</span>
      <span class="tl-meta">{{ info.dist }} · {{ info.time }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  fromLat: Number,
  fromLng: Number,
  toLat: Number,
  toLng: Number,
  fromName: String,
  toName: String,
  // 可选：外部传入距离(km)，没有坐标时用
  distKm: { type: Number, default: null }
})

// Haversine
function haversine(lat1, lng1, lat2, lng2) {
  const R = 6371
  const dLat = (lat2 - lat1) * Math.PI / 180
  const dLng = (lng2 - lng1) * Math.PI / 180
  const a = Math.sin(dLat / 2) ** 2 +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) * Math.sin(dLng / 2) ** 2
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
}

const BUSES = ['3路', '5路', '8路', '12路', '20路', '25路']

// 用名称做稳定随机种子，避免每次渲染都变
function stableRandom(str) {
  let h = 0
  for (let i = 0; i < str.length; i++) h = (Math.imul(31, h) + str.charCodeAt(i)) | 0
  return Math.abs(h)
}

const info = computed(() => {
  let dist = props.distKm
  // 只有两端坐标都有效（非 null、非 0）才计算真实距离
  const hasCoords = props.fromLat != null && props.toLat != null
    && props.fromLat !== 0 && props.fromLng !== 0
    && props.toLat !== 0 && props.toLng !== 0
  if (dist === null && hasCoords) {
    dist = haversine(props.fromLat, props.fromLng, props.toLat, props.toLng)
  }
  // 无坐标时用名称做稳定随机，给一个合理的估算距离（1-5km）
  if (dist === null || dist === 0) {
    const seed = stableRandom((props.fromName || '') + (props.toName || ''))
    dist = 1 + (seed % 40) / 10  // 1.0 ~ 5.0 km
  }

  const distStr = dist < 1
    ? `${Math.round(dist * 1000)}m`
    : `${dist.toFixed(1)}km`

  if (dist < 1) {
    const mins = Math.max(5, Math.round(dist * 1000 / 80))
    return { icon: 'Bicycle', label: '步行', dist: distStr, time: `约${mins}分钟` }
  }
  if (dist < 2.5) {
    const mins = Math.max(8, Math.round(dist * 4))
    return { icon: 'Bicycle', label: '骑行', dist: distStr, time: `约${mins}分钟` }
  }
  if (dist < 6) {
    const seed = stableRandom((props.fromName || '') + (props.toName || ''))
    const bus = BUSES[seed % BUSES.length]
    const mins = Math.max(10, Math.round(dist * 4.5 + 5))
    return { icon: 'Van', label: `公交（${bus}）`, dist: distStr, time: `约${mins}分钟` }
  }
  const mins = Math.max(10, Math.round(dist * 2.5 + 5))
  return { icon: 'Promotion', label: '打车', dist: distStr, time: `约${mins}分钟` }
})
</script>

<style scoped>
.tl-wrap {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 0 0 0 20px;
  position: relative;
}
.tl-stem {
  width: 1px;
  height: 10px;
  background: #ddd;
  margin-left: 10px;
}
.tl-row {
  display: flex;
  align-items: center;
  gap: 5px;
  background: #f5f6f8;
  border-radius: 20px;
  padding: 4px 12px;
  margin: 2px 0;
}
.tl-icon { font-size: 14px; display: flex; align-items: center; color: #1677ff; }
.tl-text { font-size: 12px; color: #555; font-weight: 500; }
.tl-meta { font-size: 12px; color: #aaa; }
</style>
