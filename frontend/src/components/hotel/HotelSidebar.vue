<template>
  <aside class="side">
    <HotelReviewCard
      :rating="rating"
      :review-count="reviewCount"
      :summary="summary"
    />
    <section class="nearby-card">
      <div class="head">
        <h3>附近</h3>
        <el-button link type="primary" @click="$emit('open-map')">在地图上查看</el-button>
      </div>
      <ul class="list">
        <li v-for="(n, idx) in nearby || []" :key="idx">
          <span class="dot"></span>
          <span class="txt">{{ n?.name || '未知' }}</span>
        </li>
      </ul>
    </section>
    <el-button class="cta" type="primary" size="large" @click="$emit('choose-room')">选择房间</el-button>
  </aside>
</template>

<script setup>
import HotelReviewCard from './HotelReviewCard.vue'

defineProps({
  rating: { type: [String, Number], default: 0 },
  reviewCount: { type: Number, default: 0 },
  summary: { type: String, default: '' },
  nearby: { type: Array, default: () => [] }
})
defineEmits(['open-map', 'choose-room'])
</script>

<style scoped>
.side { position: sticky; top: 108px; display: flex; flex-direction: column; gap: 14px; }
.nearby-card {
  border: 1px solid #eef1f6; border-radius: 12px; background: #fff; padding: 12px 14px;
  box-shadow: 0 6px 18px rgba(0,0,0,0.06);
}
.head { display: flex; align-items: center; justify-content: space-between; }
.head h3 { margin: 0; font-size: 16px; font-weight: 800; color: #1f2937; }
.list { list-style: none; padding: 0; margin: 8px 0 0 0; display: flex; flex-direction: column; gap: 8px; }
.list li { display: flex; align-items: center; gap: 8px; color: #4b5563; }
.dot { width: 6px; height: 6px; border-radius: 50%; background: #009688; display: inline-block; }
.cta { width: 100%; font-weight: 700; }
</style>

