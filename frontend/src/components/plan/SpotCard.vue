<template>
  <div class="sc-card" :class="{ expanded }" @click="expanded = !expanded">
    <!-- 头部：序号 + 名称 + 评分 + 类型 -->
    <div class="sc-head">
      <div class="sc-num" :class="'sc-num--' + item.type">{{ index + 1 }}</div>
      <div class="sc-title-wrap">
        <span class="sc-name">{{ item.name }}</span>
        <span class="sc-rating" v-if="item.rating">
          <span class="sc-star">★</span>{{ item.rating.toFixed(1) }}
        </span>
        <span class="sc-badge" :class="'sc-badge--' + item.type">{{ typeLabel(item.type) }}</span>
      </div>
      <span class="sc-arrow">{{ expanded ? '∧' : '∨' }}</span>
    </div>

    <!-- 时间 + 时长 -->
    <div class="sc-time" v-if="item.timeRange || item.duration">
      <span v-if="item.timeRange" class="sc-time-range"><el-icon><Clock /></el-icon> {{ item.timeRange }}</span>
      <span v-if="item.duration" class="sc-duration"><el-icon><Timer /></el-icon> {{ item.duration }}</span>
    </div>

    <!-- 图片（最多2张并排） -->
    <div class="sc-imgs" v-if="images.length">
      <img
        v-for="(img, i) in images.slice(0, 2)"
        :key="i"
        :src="img"
        :alt="item.name"
        class="sc-img"
        @error="e => e.target.style.display='none'"
        @click.stop="navigateToDetail"
      />
    </div>

    <!-- 简介 -->
    <div class="sc-desc" v-if="item.description">{{ item.description }}</div>

    <!-- 展开内容 -->
    <div class="sc-extra" v-if="expanded">
      <div class="sc-addr" v-if="item.address"><el-icon><Location /></el-icon> {{ item.address }}</div>
      <div class="sc-meta">
        <span v-if="item.price > 0" class="sc-price">¥{{ item.price }}</span>
        <span v-else-if="item.price === 0" class="sc-free">免费</span>
        <span v-if="item.tips" class="sc-tips"><el-icon><InfoFilled /></el-icon> {{ item.tips }}</span>
      </div>
      <div class="sc-link" @click.stop="navigateToDetail" v-if="item.id && (item.type === 'scenic' || item.type === 'hotel')">
        查看详情 →
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  item: { type: Object, required: true },
  index: { type: Number, default: 0 }
})

const emit = defineEmits(['navigate'])
const router = useRouter()
const expanded = ref(false)

const typeLabel = (t) => ({ scenic: '景点', food: '美食', hotel: '住宿', ceramic: '陶瓷工坊', market: '陶瓷市集' }[t] || t)

const images = computed(() => {
  if (!props.item.image) return []
  return props.item.image.split(',').filter(Boolean)
})

const navigateToDetail = () => {
  emit('navigate', props.item)
  sessionStorage.setItem('smartTripResult', sessionStorage.getItem('smartTripResult') || '[]')
  if (props.item.type === 'scenic' && props.item.id) router.push(`/scenic/${props.item.id}`)
  else if (props.item.type === 'hotel' && props.item.id) router.push(`/hotel/${props.item.id}`)
}
</script>

<style scoped>
.sc-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  padding: 12px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: box-shadow 0.2s;
}
.sc-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.12); }

/* 头部 */
.sc-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}
.sc-num {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 700;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.sc-num--scenic  { background: #1677ff; }
.sc-num--food    { background: #f5a623; }
.sc-num--hotel   { background: #52c41a; }
.sc-num--ceramic { background: #9254de; }
.sc-num--market  { background: #eb2f96; }

.sc-title-wrap {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
  flex-wrap: wrap;
}
.sc-name {
  font-size: 15px;
  font-weight: 600;
  color: #111;
  line-height: 1.4;
}
.sc-rating {
  font-size: 12px;
  color: #ff9500;
  display: flex;
  align-items: center;
}
.sc-star { font-size: 11px; margin-right: 1px; }
.sc-badge {
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 4px;
  font-weight: 500;
}
.sc-badge--scenic  { background: #e8f0fe; color: #1677ff; }
.sc-badge--food    { background: #fff7e6; color: #d46b08; }
.sc-badge--hotel   { background: #f6ffed; color: #389e0d; }
.sc-badge--ceramic { background: #f9f0ff; color: #722ed1; }
.sc-badge--market  { background: #fff0f6; color: #c41d7f; }

.sc-arrow { font-size: 12px; color: #ccc; margin-left: auto; }

/* 时间 */
.sc-time {
  display: flex;
  gap: 10px;
  margin-bottom: 8px;
}
.sc-time-range { font-size: 12px; color: #1677ff; font-weight: 500; display: flex; align-items: center; gap: 3px; }
.sc-duration   { font-size: 12px; color: #999; display: flex; align-items: center; gap: 3px; }

/* 图片 */
.sc-imgs {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}
.sc-img {
  flex: 1;
  height: 130px;
  object-fit: cover;
  border-radius: 10px;
  min-width: 0;
  cursor: pointer;
  max-width: calc(50% - 4px);
}

/* 简介 */
.sc-desc {
  font-size: 13px;
  color: #555;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 展开内容 */
.sc-extra { margin-top: 8px; border-top: 1px solid #f5f5f5; padding-top: 8px; }
.sc-addr  { font-size: 12px; color: #888; margin-bottom: 4px; display: flex; align-items: center; gap: 3px; }
.sc-meta  { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; margin-bottom: 4px; }
.sc-price { font-size: 12px; color: #f5222d; font-weight: 500; }
.sc-free  { font-size: 12px; color: #52c41a; font-weight: 500; }
.sc-tips  { font-size: 12px; color: #888; }
.sc-link  { font-size: 12px; color: #1677ff; cursor: pointer; }
.sc-link:hover { text-decoration: underline; }
</style>
