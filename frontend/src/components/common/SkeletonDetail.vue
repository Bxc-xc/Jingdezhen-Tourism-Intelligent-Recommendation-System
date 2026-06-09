<template>
  <div class="s-detail">
    <div class="s-hero shimmer"></div>
    <div class="s-container">
      <div class="s-left">
        <div class="s-block shimmer" style="height: 180px"></div>
        <div class="s-block shimmer" style="height: 320px"></div>
        <div class="s-block shimmer" style="height: 220px"></div>
      </div>
      <div class="s-right">
        <div class="s-card shimmer"></div>
        <div class="s-card shimmer" style="height: 140px"></div>
      </div>
    </div>
  </div>
  </template>

<script setup>
</script>

<style scoped>
@import '../../styles/tokens.css';

.s-hero {
  height: 320px;
  background: #f2f3f5;
  border-radius: var(--radius-16);
  margin-bottom: var(--space-16);
}
.s-container {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: var(--space-16);
}
.s-left { display: grid; gap: var(--space-16); }
.s-right { display: grid; gap: var(--space-16); }
.s-card {
  height: 200px;
  background: #f2f3f5;
  border-radius: var(--radius-16);
}
.s-block {
  background: #f2f3f5;
  border-radius: var(--radius-16);
}
.shimmer {
  position: relative;
  overflow: hidden;
}
.shimmer::after {
  content: "";
  position: absolute;
  inset: 0;
  transform: translateX(-100%);
  background: linear-gradient(90deg, rgba(255,255,255,0) 0%, rgba(255,255,255,.6) 50%, rgba(255,255,255,0) 100%);
  animation: shimmer 1.4s infinite;
}
@keyframes shimmer {
  100% { transform: translateX(100%); }
}
</style>

