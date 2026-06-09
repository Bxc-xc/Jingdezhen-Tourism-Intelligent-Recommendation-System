<template>
  <aside class="recommend-sidebar" :class="{ collapsed: isCollapsed }" aria-label="筛选栏">
    <div class="sidebar-card">
      <div class="card-header">
        <div class="header-left">
          <button class="menu-btn" type="button" @click="toggleSidebar" aria-label="展开/收起侧边栏">
            <el-icon><Menu /></el-icon>
          </button>
          <div v-if="!isCollapsed" class="titles">
            <div class="card-title">分类</div>
            <div class="card-subtitle">快速切换推荐内容</div>
          </div>
        </div>
      </div>
      <div class="card-body">
        <LeftCategoryNav :active-type="activeType" :collapsed="isCollapsed" @change="$emit('change', $event)" />
      </div>
    </div>
  </aside>
</template>

<script setup>
import { ref } from 'vue'
import { Menu } from '@element-plus/icons-vue'
import LeftCategoryNav from './LeftCategoryNav.vue'

defineProps({
  activeType: {
    type: String,
    default: 'scenic'
  }
})

defineEmits(['change'])

const isCollapsed = ref(true)

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}
</script>

<style scoped>
.recommend-sidebar {
  position: sticky;
  top: 88px;
  width: 260px;
  transition: width 0.26s ease;
}

.recommend-sidebar.collapsed {
  width: 76px;
}

.sidebar-card {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.92), rgba(255, 255, 255, 0.98));
  border-radius: 16px;
  padding: 14px;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.05);
  backdrop-filter: blur(8px);
}

.card-header {
  padding: 2px 4px 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.menu-btn {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 12px;
  background: rgba(64, 158, 255, 0.12);
  color: #409eff;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.12s ease, background 0.2s ease, box-shadow 0.22s ease;
}

.menu-btn:hover {
  background: rgba(64, 158, 255, 0.2);
  box-shadow: 0 10px 18px rgba(64, 158, 255, 0.18);
}

.menu-btn:active {
  transform: scale(0.96);
}

.titles {
  min-width: 0;
}

.card-title {
  font-size: 15px;
  font-weight: 800;
  color: #1f2937;
  letter-spacing: 0.2px;
}

.card-subtitle {
  margin-top: 4px;
  font-size: 12px;
  color: #8a94a6;
}

.card-body {
  padding-top: 2px;
}

@media (max-width: 768px) {
  .recommend-sidebar {
    position: static;
    width: 100%;
  }
  .recommend-sidebar.collapsed {
    width: 100%;
  }
}
</style>
