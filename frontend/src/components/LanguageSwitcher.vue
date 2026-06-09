<template>
  <!-- header 最右侧的极简小文字按钮 -->
  <button class="lang-toggle" @click="localeStore.toggleLocale" title="切换语言">
    {{ shortLabel }}
  </button>
</template>

<script setup>
import { computed } from 'vue'
import { useLocaleStore } from '../stores/locale'

const localeStore = useLocaleStore()

// 按钮上只显示“中 / EN”，尽量不显眼
const shortLabel = computed(() =>
  localeStore.currentLocale === 'zh-CN' ? '中' : 'EN'
)
</script>

<style scoped>
.lang-toggle {
  border: none;
  background: transparent;
  padding: 4px 6px;
  font-size: 12px;
  color: #909399;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.2s, color 0.2s;
}

.lang-toggle:hover {
  background-color: rgba(64, 158, 255, 0.06);
  color: #409eff;
}
</style>