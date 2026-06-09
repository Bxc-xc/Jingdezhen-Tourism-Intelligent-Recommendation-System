<template>
  <section class="hotel-card hotel-info">
    <div class="header-row">
      <div class="left">
        <div class="title-row">
          <h1 class="title">{{ scenic?.name || '暂无信息' }}</h1>
          <div class="stars" v-if="displayStars > 0">
            <span v-for="n in displayStars" :key="n" class="star">★</span>
          </div>
        </div>
        <div class="subline">
          <el-icon><Location /></el-icon>
          <span class="addr">{{ scenic?.address || '暂无信息' }}</span>
          <el-button link type="primary" class="map-link" @click="$emit('open-map')">显示地图</el-button>
        </div>
      </div>
      <div class="right">
        <div class="price" v-if="priceDisplay">
          <span class="yen">¥</span>{{ priceDisplay }}<span class="suffix">起</span>
        </div>
        <el-button type="primary" size="large" class="cta" @click="$emit('choose-room')">选择房间</el-button>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { Location } from '@element-plus/icons-vue'

const props = defineProps({
  scenic: { type: Object, default: () => ({}) }
})
defineEmits(['open-map','choose-room'])

const displayStars = computed(() => {
  const s = Number(props.scenic?.stars || props.scenic?.rating || 5)
  if (!Number.isFinite(s)) return 0
  return Math.min(5, Math.max(0, Math.round(s)))
})

const priceDisplay = computed(() => {
  const p = props.scenic?.startPrice ?? props.scenic?.price
  if (p === undefined || p === null || isNaN(Number(p))) return ''
  return Number(p).toFixed(0)
})
</script>

<style scoped>
.hotel-card {
  background: #fff;
  border-radius: 14px;
  border: 1px solid #eef1f6;
  padding: 16px;
  box-shadow: 0 6px 18px rgba(0,0,0,0.06);
}
.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.left { min-width: 0; flex: 1; }
.right { display: flex; align-items: center; gap: 12px; }
.title-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.title {
  margin: 0;
  font-size: 24px;
  font-weight: 800;
  color: #1f2937;
}
.stars .star { color: #d4af37; font-size: 18px; }
.subline {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #6b7280;
  margin-top: 8px;
}
.map-link { margin-left: 4px; }
.price {
  color: #1f2937;
  font-weight: 800;
  font-size: 22px;
}
.price .yen { font-size: 16px; margin-right: 1px; }
.price .suffix { font-size: 12px; margin-left: 2px; color: #6b7280; font-weight: 600; }
.cta { font-weight: 700; }
</style>

