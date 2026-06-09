<template>
  <div class="s-card">
    <div class="s-cover shimmer"></div>
    <div class="s-lines">
      <div class="s-line shimmer" style="width: 70%"></div>
      <div class="s-line shimmer" style="width: 50%"></div>
      <div class="s-tags">
        <span class="s-tag shimmer"></span>
        <span class="s-tag shimmer"></span>
        <span class="s-tag shimmer"></span>
      </div>
    </div>
  </div>
  </template>

<script setup>
</script>

<style scoped>
@import '../../styles/tokens.css';

.s-card {
  background: #fff;
  border-radius: var(--radius-16);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}
.s-cover {
  height: 200px;
  background: #f2f3f5;
}
.s-lines {
  padding: var(--space-16);
  display: grid;
  gap: 10px;
}
.s-line {
  height: 12px;
  background: #f2f3f5;
  border-radius: 8px;
}
.s-tags {
  display: flex;
  gap: 8px;
  margin-top: 6px;
}
.s-tag {
  width: 56px;
  height: 18px;
  background: #f2f3f5;
  border-radius: 999px;
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

