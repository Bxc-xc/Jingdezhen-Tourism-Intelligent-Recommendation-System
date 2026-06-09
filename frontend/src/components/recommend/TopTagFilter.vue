<template>
  <div class="tag-filter" role="tablist" aria-label="排序筛选">
    <div
      v-for="item in options"
      :key="item.value"
      class="sort-chip"
      :class="{ active: sortField === item.value }"
    >
      <span class="sort-label">{{ item.label }}</span>
      <div class="sort-controls" role="group" :aria-label="`${item.label}排序控制`">
        <button
          type="button"
          class="dir-btn dir-btn-asc"
          :class="{ active: sortField === item.value && sortOrder === 'asc' }"
          @click="$emit('set', { field: item.value, order: sortField === item.value && sortOrder === 'asc' ? '' : 'asc' })"
          :aria-label="`${item.label}升序`"
          title="升序"
        >
          <span class="arrow">↑</span>
        </button>
        <button
          type="button"
          class="dir-btn dir-btn-desc"
          :class="{ active: sortField === item.value && sortOrder === 'desc' }"
          @click="$emit('set', { field: item.value, order: sortField === item.value && sortOrder === 'desc' ? '' : 'desc' })"
          :aria-label="`${item.label}降序`"
          title="降序"
        >
          <span class="arrow">↓</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  sortField: {
    type: String,
    default: ''
  },
  sortOrder: {
    type: String,
    default: ''
  },
  options: {
    type: Array,
    default: () => []
  }
})

defineEmits(['set'])
</script>

<style scoped>
.tag-filter {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.sort-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  border-radius: 20px;
  background: #ffffff;
  border: 1.5px solid #e4e7ed;
  transition: all 0.2s ease;
  cursor: default;
}

.sort-chip:hover {
  border-color: #c0c4cc;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.sort-chip.active {
  border-color: #409eff;
  background: #ecf5ff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.2);
}

.sort-label {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  user-select: none;
}

.sort-controls {
  display: inline-flex;
  gap: 4px;
  align-items: center;
}

.dir-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 12px;
  padding: 6px 12px;
  min-height: 32px;
  min-width: 36px;
  font-size: 16px;
  font-weight: 600;
  color: #606266;
  background: #f5f7fa;
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
}

.dir-btn:hover {
  background: #e4e7ed;
  color: #409eff;
  transform: translateY(-1px);
}

.dir-btn:active {
  transform: translateY(0) scale(0.95);
}

.dir-btn.active {
  color: #ffffff;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.dir-btn.active:hover {
  background: linear-gradient(135deg, #66b1ff 0%, #409eff 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}

.arrow {
  font-size: 18px;
  line-height: 1;
  display: inline-block;
}

@media (max-width: 768px) {
  .tag-filter {
    gap: 8px;
    width: 100%;
  }
  
  .sort-chip {
    flex: 1;
    min-width: 0;
    justify-content: space-between;
    padding: 8px 12px;
  }
  
  .sort-label {
    font-size: 13px;
  }
  
  .dir-btn {
    min-width: 32px;
    min-height: 28px;
    padding: 4px 10px;
  }
  
  .arrow {
    font-size: 16px;
  }
}
</style>
