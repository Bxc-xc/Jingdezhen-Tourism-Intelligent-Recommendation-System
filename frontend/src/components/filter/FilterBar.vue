<template>
  <div class="filter-bar">
    <div class="filter-bar-inner">
      <button
        v-for="tab in tabs"
        :key="tab.value"
        class="tab-item"
        :class="{ active: modelValue === tab.value }"
        @click="$emit('update:modelValue', tab.value)"
      >
        <el-icon><component :is="tab.icon" /></el-icon>
        <span>{{ tab.label }}</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { Compass, KnifeFork, Guide, OfficeBuilding } from '@element-plus/icons-vue'

defineProps({
  modelValue: { type: String, required: true },
})
defineEmits(['update:modelValue'])

const tabs = [
  { label: '景点', value: 'scenic',  icon: Compass },
  { label: '餐饮', value: 'food',    icon: KnifeFork },
  { label: '路线', value: 'route',   icon: Guide },
  { label: '酒店', value: 'hotel',   icon: OfficeBuilding },
]
</script>

<style scoped>
@import '../../styles/tokens.css';

.filter-bar {
  position: sticky;
  top: 64px;
  z-index: 100;
  background: #fff;
  border-bottom: 1px solid var(--border);
  box-shadow: var(--shadow-sm);
}

.filter-bar-inner {
  display: flex;
  align-items: center;
  padding: 0 var(--space-24);
  height: 52px;
  gap: 4px;
}

.tab-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 18px;
  border-radius: 999px;
  border: none;
  background: transparent;
  color: var(--text-muted);
  font-size: 14px;
  cursor: pointer;
  transition: all .15s;
  white-space: nowrap;
}
.tab-item:hover {
  color: var(--text-strong);
  background: var(--bg-soft);
}
.tab-item.active {
  color: var(--brand-500);
  font-weight: 600;
  background: var(--brand-50);
}
</style>
