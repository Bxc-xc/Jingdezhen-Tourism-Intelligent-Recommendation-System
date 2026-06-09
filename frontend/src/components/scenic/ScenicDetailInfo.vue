<template>
  <div class="scenic-detail-info">
    <!-- 介绍 -->
    <section class="info-card">
      <h2 class="card-title">介绍</h2>
      <div class="description-content" :class="{ collapsed: !expanded }">
        {{ scenic.description || '暂无介绍信息' }}
      </div>
      <el-button v-if="needsExpand" link type="primary" @click="expanded = !expanded">
        {{ expanded ? '收起' : '全文' }}
        <el-icon><ArrowDown v-if="!expanded" /><ArrowUp v-else /></el-icon>
      </el-button>
    </section>

    <!-- 开放时间 -->
    <section v-if="displayOpeningHours" class="info-card">
      <h2 class="card-title">开放时间</h2>
      <div class="opening-hours-content">
        <p class="time-detail">{{ displayOpeningHours }}</p>
      </div>
    </section>

    <!-- 优待政策 -->
    <section v-if="hasPreferentialPolicy" class="info-card">
      <h2 class="card-title">优待政策</h2>
      <div class="policy-content">
        <div v-html="formatPolicy(scenic.preferentialPolicy || scenic.preferential_policy || scenic.discountPolicy)"></div>
      </div>
    </section>

    <!-- 官方电话 -->
    <section v-if="displayOfficialPhone" class="info-card">
      <h2 class="card-title">官方电话</h2>
      <div class="phone-content">
        <div class="phone-item">
          <span class="phone-label">票务咨询</span>
          <span class="phone-number">{{ displayOfficialPhone }}</span>
        </div>
      </div>
    </section>

    <!-- 服务设施 -->
    <section v-if="hasFacilities" class="info-card">
      <h2 class="card-title">服务设施</h2>
      <div class="facilities-content">
        <div v-html="formatFacilities(scenic.facilitiesDetail || scenic.facilities_detail)"></div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ArrowDown, ArrowUp } from '@element-plus/icons-vue'

const props = defineProps({
  scenic: {
    type: Object,
    required: true
  }
})

const expanded = ref(false)

// 是否需要展开按钮
const needsExpand = computed(() => {
  const desc = props.scenic.description || ''
  return desc.length > 200
})

// 开放时间显示
const displayOpeningHours = computed(() => {
  const detail = props.scenic.openingHoursDetail || props.scenic.opening_hours_detail
  if (detail) return detail
  
  const openTime = props.scenic.openTime || props.scenic.open_time
  if (openTime) return openTime
  
  return '' // 没有数据就不显示
})

// 官方电话显示
const displayOfficialPhone = computed(() => {
  return props.scenic.officialPhone || props.scenic.official_phone || props.scenic.phone || ''
})

// 是否有优待政策
const hasPreferentialPolicy = computed(() => {
  return !!(props.scenic.preferentialPolicy || props.scenic.preferential_policy || props.scenic.discountPolicy)
})

// 是否有服务设施
const hasFacilities = computed(() => {
  return !!(props.scenic.facilitiesDetail || props.scenic.facilities_detail)
})

// 格式化政策文本
const formatPolicy = (text) => {
  if (!text) return ''
  return text.replace(/\n/g, '<br>')
}

// 格式化设施文本
const formatFacilities = (text) => {
  if (!text) return ''
  return text.replace(/\n/g, '<br>')
}
</script>

<style scoped>
.scenic-detail-info {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.card-title {
  margin: 0 0 16px 0;
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
}

.description-content {
  color: #4b5563;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
}

.description-content.collapsed {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.opening-hours-content {
  color: #4b5563;
  line-height: 1.6;
}

.time-detail {
  margin: 0;
  font-size: 14px;
}

.policy-content,
.facilities-content {
  color: #4b5563;
  line-height: 1.8;
  font-size: 14px;
}

.default-policy p,
.default-facilities p {
  margin: 0 0 12px 0;
}

.default-policy p:last-child,
.default-facilities p:last-child {
  margin-bottom: 0;
}

.phone-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.phone-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 6px;
}

.phone-label {
  font-weight: 600;
  color: #6b7280;
  min-width: 80px;
}

.phone-number {
  color: #1f2937;
  font-size: 16px;
}

@media (max-width: 768px) {
  .info-card {
    padding: 16px;
  }
  
  .card-title {
    font-size: 18px;
  }
}
</style>
