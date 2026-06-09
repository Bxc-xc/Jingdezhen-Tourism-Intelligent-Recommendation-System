<template>
  <div class="smart-copy" aria-label="智能推荐说明">
    <div class="smart-title">{{ title }}</div>
    <div class="smart-desc">{{ desc }}</div>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, default: '智能推荐' },
  desc: {
    type: String,
    default: '基于您的需求，为您推荐景德镇最合适的景点、美食、路线与酒店'
  }
})
</script>

<style scoped>
.smart-copy {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.smart-title {
  font-size: 24px;
  font-weight: 600;
  color: #111827;
  letter-spacing: 0.2px;
  line-height: 1.2;
}

.smart-desc {
  font-size: 12px;
  color: #999;
  line-height: 1.55;
  max-width: 64ch;
}
</style>
