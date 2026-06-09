<template>
  <nav class="category-nav" :class="{ collapsed }" aria-label="推荐分类导航">
    <button
      v-for="item in navItems"
      :key="item.value"
      class="category-nav-item"
      :class="{ active: activeType === item.value }"
      type="button"
      :title="collapsed ? item.label : ''"
      @click="$emit('change', item.value)"
    >
      <el-icon class="category-nav-icon">
        <component :is="item.icon" />
      </el-icon>
      <span v-if="!collapsed" class="category-nav-text">{{ item.label }}</span>
    </button>
  </nav>
</template>

<script setup>
import { Compass, KnifeFork, Guide, OfficeBuilding, Brush } from '@element-plus/icons-vue'

defineProps({
  activeType: {
    type: String,
    default: 'scenic'
  },
  collapsed: {
    type: Boolean,
    default: false
  }
})

defineEmits(['change'])

const navItems = [
  { value: 'scenic', label: '景点', icon: Compass },
  { value: 'food', label: '美食', icon: KnifeFork },
  { value: 'route', label: '路线', icon: Guide },
  { value: 'hotel', label: '酒店', icon: OfficeBuilding },
  { value: 'ceramic', label: '陶瓷工坊', icon: Brush }
]
</script>

<style scoped>
.category-nav {
  width: 100%;
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 12px;
}

.category-nav-item {
  width: 100%;
  min-height: 56px;
  border: none;
  border-radius: 14px;
  background: linear-gradient(180deg, #f6f8fc, #f3f6fb);
  color: #606266;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: flex-start;
  gap: 10px;
  padding: 0 14px;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.25s ease, background 0.2s ease, color 0.2s ease;
  box-shadow: 0 6px 16px rgba(17, 24, 39, 0.06);
}

.category-nav-item:hover {
  transform: translateY(-1px);
  background: linear-gradient(180deg, rgba(64, 158, 255, 0.14), rgba(64, 158, 255, 0.08));
  color: #409eff;
  box-shadow: 0 10px 22px rgba(64, 158, 255, 0.18);
}

.category-nav-item.active {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: #ffffff;
  box-shadow: 0 8px 18px rgba(64, 158, 255, 0.26);
  transform: translateY(-1px) scale(1.01);
}

.category-nav-icon {
  font-size: 17px;
  flex-shrink: 0;
  opacity: 0.95;
}

.category-nav-text {
  font-size: 14px;
  font-weight: 700;
  line-height: 1.2;
}

.category-nav.collapsed .category-nav-item {
  justify-content: center;
  padding: 0;
  gap: 0;
}

.category-nav.collapsed .category-nav-icon {
  font-size: 18px;
}

.category-nav.collapsed .category-nav-text {
  display: none;
}

@media (max-width: 768px) {
  .category-nav-item {
    min-height: 54px;
  }

  .category-nav-text {
    font-size: 13px;
  }
}
</style>
