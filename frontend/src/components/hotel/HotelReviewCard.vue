<template>
  <section class="score-card">
    <div class="row">
      <span class="score">{{ ratingDisplay }}</span>
      <span class="badge">超棒</span>
    </div>
    <p class="summary">{{ summary || '暂无信息' }}</p>
    <el-button link type="primary" @click="scrollToReviews">查看全部评论（{{ reviewCount }}）</el-button>
  </section>
</template>

<script setup>
import { computed } from 'vue'
const props = defineProps({
  rating: { type: [String, Number], default: 0 },
  reviewCount: { type: Number, default: 0 },
  summary: { type: String, default: '' }
})
const ratingDisplay = computed(() => Number(props.rating || 0).toFixed(1))

const scrollToReviews = () => {
  const reviewSection = document.getElementById('user-reviews')
  if (reviewSection) {
    reviewSection.scrollIntoView({ 
      behavior: 'smooth', 
      block: 'start' 
    })
    // 高亮效果
    reviewSection.classList.add('highlight-section')
    setTimeout(() => {
      reviewSection.classList.remove('highlight-section')
    }, 2000)
  }
}
</script>

<style scoped>
.score-card {
  border: 1px solid #e7f2f0;
  border-radius: 12px;
  padding: 16px;
  background: #f9fbff;
  box-shadow: 0 6px 18px rgba(0,0,0,0.04);
}
.row { display: flex; align-items: center; gap: 10px; }
.score { color: #1d6adf; font-weight: 900; font-size: 32px; }
.badge { background: #1d6adf; color: #fff; border-radius: 999px; padding: 4px 10px; font-weight: 700; font-size: 12px; }
.summary { color: #6b7280; margin: 8px 0 6px; }
</style>

