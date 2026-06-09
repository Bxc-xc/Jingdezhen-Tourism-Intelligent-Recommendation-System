<template>
  <div class="sort-supsub" role="tablist" aria-label="排序">
    <div
      v-for="item in options"
      :key="item.value"
      class="sort-item"
      :class="{ active: sortField === item.value && !!sortOrder }"
      role="tab"
      :aria-selected="sortField === item.value"
    >
      <span class="sort-label-pill">
        <span class="sort-label">{{ item.label }}</span>
      </span>

      <div class="sort-marks" role="group" :aria-label="`${item.label}排序方向`">
        <!-- 上箭头：高优先（desc） -->
        <button
          type="button"
          class="mark-btn mark-up"
          :class="{ on: sortField === item.value && sortOrder === 'desc' }"
          :aria-pressed="sortField === item.value && sortOrder === 'desc'"
          aria-label="高优先排序"
          @click="toggle(item.value, 'desc')"
        >
          <svg class="icon" width="10" height="10" viewBox="0 0 1024 1024" aria-hidden="true">
            <path
              d="M512 192c-12 0-24 5-33 14L256 429c-18 18-18 47 0 66s47 18 66 0l190-190 190 190c18 18 47 18 66 0s18-47 0-66L545 206c-9-9-21-14-33-14z"
              fill="currentColor"
            />
          </svg>
        </button>

        <!-- 下箭头：低优先（asc） -->
        <button
          type="button"
          class="mark-btn mark-down"
          :class="{ on: sortField === item.value && sortOrder === 'asc' }"
          :aria-pressed="sortField === item.value && sortOrder === 'asc'"
          aria-label="低优先排序"
          @click="toggle(item.value, 'asc')"
        >
          <svg class="icon" width="10" height="10" viewBox="0 0 1024 1024" aria-hidden="true">
            <path
              d="M512 832c12 0 24-5 33-14l223-223c18-18 18-47 0-66s-47-18-66 0L512 719 322 529c-18-18-47-18-66 0s-18 47 0 66l223 223c9 9 21 14 33 14z"
              fill="currentColor"
            />
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  sortField: { type: String, default: '' },
  sortOrder: { type: String, default: '' },
  options: { type: Array, default: () => [] }
})

const emit = defineEmits(['set'])

const toggle = (field, order) => {
  const isSame = props.sortField === field && props.sortOrder === order
  emit('set', { field, order: isSame ? '' : order })
}
</script>

<style scoped>
.sort-supsub {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 14px; /* 12~16 */
}

.sort-item {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  border-radius: 999px;
  color: #374151;
  user-select: none;
  background: transparent;
  border: 1px solid transparent;
  transition: background-color 0.18s ease, border-color 0.18s ease;
}

.sort-item:hover {
  background: rgba(22, 119, 255, 0.06); /* 微高亮 */
  border-color: rgba(22, 119, 255, 0.10);
}

.sort-item.active {
  background: transparent; /* 选中时只高亮文字胶囊，不影响箭头区域 */
  border-color: transparent;
}

.sort-item.active .sort-label {
  color: #111827;
}

.sort-label-pill {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  background: transparent;
  border: 1px solid transparent;
  transition: background-color 0.18s ease, border-color 0.18s ease;
}

.sort-item.active .sort-label-pill {
  background: rgba(22, 119, 255, 0.18); /* 小胶囊蓝底仅包裹文字 */
  border-color: rgba(22, 119, 255, 0.22);
}

.sort-label {
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.2px;
  color: #374151;
  line-height: 1.1;
}

.sort-marks {
  /* 固定图标容器尺寸，保证所有排序项图标位置完全一致 */
  width: 28px;
  height: 40px;
  display: inline-flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 4px;
}

.mark-btn {
  border: 1px solid rgba(17, 24, 39, 0.10);
  background: transparent;
  color: #9aa0aa;
  padding: 4px;
  width: 28px;
  height: 18px;
  border-radius: 10px;
  cursor: pointer;
  line-height: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transform: scale(var(--pressScale, 1));
  transform-origin: center;
  transition: transform 0.12s ease, background-color 0.18s ease, border-color 0.18s ease, color 0.18s ease, box-shadow 0.18s ease;
}

.icon {
  width: 12px;
  height: 12px;
  display: block;
}

.mark-btn:hover {
  color: #4b5563;
  border-color: rgba(22, 119, 255, 0.35);
  background: rgba(22, 119, 255, 0.06);
}

.mark-btn:active {
  --pressScale: 0.96;
}

/* active 高亮（同时保留上下标偏移） */
.mark-btn.on {
  color: #1677ff;
  border-color: rgba(22, 119, 255, 0.40);
  background: rgba(22, 119, 255, 0.10); /* 极浅色微高亮 */
}

@media (max-width: 768px) {
  .sort-supsub {
    gap: 12px;
  }
}
</style>
