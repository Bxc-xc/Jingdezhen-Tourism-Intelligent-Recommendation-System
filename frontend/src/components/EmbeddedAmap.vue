<template>
  <div class="embedded-amap glass-panel">
    <div class="map-header">
      <h3>
        <el-icon><Location /></el-icon>
        智能交通规划
      </h3>
    </div>
    <div class="map-container">
      <iframe
        class="map-iframe"
        :src="mapSrc"
        frameborder="0"
        scrolling="no"
      ></iframe>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Location } from '@element-plus/icons-vue'

// 接收来自行程表的地点关键字（例如第一行的「位置」）
const props = defineProps({
  keyword: {
    type: String,
    default: ''
  }
})

// 景德镇中心坐标，经度,纬度（无关键字时的默认中心）
const JINGDEZHEN_LNG = 117.214664
const JINGDEZHEN_LAT = 29.29256

// 根据是否有关键字，决定使用搜索链接还是固定坐标
const mapSrc = computed(() => {
  const kw = (props.keyword || '').trim()

  if (kw) {
    const encoded = encodeURIComponent(kw)
    // 使用高德搜索 URI，根据关键字在景德镇范围内搜索并居中
    return `https://uri.amap.com/search?keyword=${encoded}&city=景德镇&coordinate=gaode&callnative=0`
  }

  // 默认回退到景德镇市中心
  return `https://uri.amap.com/marker?position=${JINGDEZHEN_LNG},${JINGDEZHEN_LAT}&name=景德镇&coordinate=gaode&callnative=0`
})
</script>

<style scoped>
.embedded-amap {
  padding: 20px;
  margin-bottom: 20px;
}

.map-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.map-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.map-container {
  width: 100%;
  height: 400px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #dcdfe6;
  background: #f5f7fa;
}

.map-iframe {
  width: 100%;
  height: 100%;
  border: 0;
}

@media (max-width: 768px) {
  .map-container {
    height: 300px;
  }
}
</style>

