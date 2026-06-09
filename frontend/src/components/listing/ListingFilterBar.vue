<template>
  <div class="filter-bar">
    <div class="group">
      <span class="label">分类</span>
      <div class="pills">
        <button
          v-for="c in categories"
          :key="c.value"
          class="pill"
          :class="{ active: categoryModel === c.value }"
          @click="setCategory(c.value)"
        >{{ c.label }}</button>
      </div>
    </div>
    <div class="group">
      <span class="label">价格</span>
      <div class="price-range">
        <el-input-number v-model="minPriceModel" :min="0" :max="maxPriceModel || 99999" :step="10" size="small" />
        <span class="dash">—</span>
        <el-input-number v-model="maxPriceModel" :min="minPriceModel || 0" :max="99999" :step="10" size="small" />
      </div>
    </div>
    <div class="group">
      <span class="label">排序</span>
      <div class="pills">
        <button
          v-for="s in sorts"
          :key="s.value"
          class="pill"
          :class="{ active: sortModel === s.value }"
          @click="setSort(s.value)"
        >{{ s.label }}</button>
      </div>
    </div>
  </div>
  </template>

<script setup>
import { computed } from 'vue'

const categoryModel = defineModel('category', { type: String, default: '' })
const minPriceModel = defineModel('minPrice', { type: Number, default: undefined })
const maxPriceModel = defineModel('maxPrice', { type: Number, default: undefined })
const sortModel = defineModel('sort', { type: String, default: 'recommend' })

const categories = [
  { label: '全部', value: '' },
  { label: '景点', value: 'SCENIC' },
  { label: '酒店', value: 'HOTEL' },
  { label: '美食', value: 'FOOD' },
]

const sorts = [
  { label: '推荐', value: 'recommend' },
  { label: '热度', value: 'hot' },
  { label: '价格', value: 'price' },
]

function setCategory(val) {
  categoryModel.value = val
}
function setSort(val) {
  sortModel.value = val
}
</script>

<style scoped>
@import '../../styles/tokens.css';

.filter-bar {
  position: sticky;
  top: 64px;
  z-index: 5;
  background: #fff;
  border-radius: var(--radius-16);
  box-shadow: var(--shadow-sm);
  padding: var(--space-16);
  display: grid;
  grid-template-columns: 1.5fr 1.2fr 1.3fr;
  gap: var(--space-16);
}
.group {
  display: flex;
  align-items: center;
  gap: var(--space-12);
  flex-wrap: wrap;
}
.label {
  color: var(--text-muted);
  font-size: 13px;
}
.pills {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.pill {
  padding: 6px 14px;
  border-radius: 999px;
  border: 1px solid var(--border);
  background: #fff;
  color: var(--text);
  cursor: pointer;
  transition: all .2s ease;
}
.pill:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}
.pill.active {
  background: var(--brand-50);
  border-color: var(--brand-500);
  color: var(--brand-600);
}
.price-range {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.dash { color: var(--text-muted); }
</style>

