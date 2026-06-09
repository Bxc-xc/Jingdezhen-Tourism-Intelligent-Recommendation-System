<template>
  <div class="base-card" @click="$emit('click')">
    <div v-if="$slots.header" class="base-card__header">
      <slot name="header" />
    </div>
    <div class="base-card__content">
      <slot />
      <slot name="content" />
    </div>
    <div v-if="$slots.footer" class="base-card__footer">
      <slot name="footer" />
    </div>
  </div>
  </template>

<script setup>
defineEmits(['click'])
</script>

<style scoped>
@import '../../styles/tokens.css';

.base-card {
  background: #fff;
  border-radius: var(--radius-16);
  box-shadow: var(--shadow-sm);
  transition: transform .2s ease, box-shadow .2s ease;
  overflow: hidden;
}
.base-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}
.base-card__header {
  padding: var(--space-16) var(--space-16) 0 var(--space-16);
}
.base-card__content {
  padding: var(--space-16);
}
.base-card__footer {
  padding: 0 var(--space-16) var(--space-16) var(--space-16);
}
</style>

