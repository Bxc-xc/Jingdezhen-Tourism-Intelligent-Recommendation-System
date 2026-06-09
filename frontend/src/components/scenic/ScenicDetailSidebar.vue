<template>
  <div class="scenic-detail-sidebar">
    <!-- 附近美食 -->
    <section v-if="nearbyFood.length" class="sidebar-card">
      <div class="card-header">
        <h3 class="card-title">附近美食</h3>
        <el-button link type="primary" @click="$emit('view-more', 'food')">
          推荐美食
        </el-button>
      </div>
      <div class="nearby-list">
        <div 
          v-for="item in nearbyFood" 
          :key="item.id" 
          class="nearby-item"
          @click="$emit('navigate', item)"
        >
          <img :src="getItemImage(item)" :alt="item.name" class="item-image" />
          <div class="item-info">
            <h4 class="item-name">{{ item.name }}</h4>
            <div class="item-meta">
              <span class="item-rating">{{ getItemRating(item) }}</span>
              <span class="item-reviews">{{ getItemReviews(item) }}条点评</span>
            </div>
            <div class="item-distance">直线距离{{ item.distance || '1.2km' }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 附近购物 -->
    <section v-if="nearbyShopping.length" class="sidebar-card">
      <div class="card-header">
        <h3 class="card-title">附近购物</h3>
        <el-button link type="primary" @click="$emit('view-more', 'shopping')">
          更多
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
      <div class="nearby-list">
        <div 
          v-for="item in nearbyShopping" 
          :key="item.id" 
          class="nearby-item"
          @click="$emit('navigate', item)"
        >
          <img :src="getItemImage(item)" :alt="item.name" class="item-image" />
          <div class="item-info">
            <h4 class="item-name">{{ item.name }}</h4>
            <div class="item-meta">
              <span class="item-rating">{{ getItemRating(item) }}</span>
              <span class="item-reviews">{{ getItemReviews(item) }}条点评</span>
            </div>
            <div class="item-distance">直线距离{{ item.distance || '410m' }}</div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import { normalizeUrl, FALLBACK_IMAGE } from '../../utils/image'

const props = defineProps({
  nearbyFood: {
    type: Array,
    default: () => []
  },
  nearbyShopping: {
    type: Array,
    default: () => []
  }
})

defineEmits(['view-more', 'navigate'])

const getItemImage = (item) => {
  const img = item.image || item.avatar || item.cover || item.shopImages?.split(',')[0]
  return normalizeUrl(img) || FALLBACK_IMAGE
}

const getItemRating = (item) => {
  const rating = item.score || item.rating || item.adminRating || 5.0
  return Number(rating).toFixed(1)
}

const getItemReviews = (item) => {
  return item.commentCount || item.comment_count || item.reviewCount || 14
}
</script>

<style scoped>
.scenic-detail-sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.sidebar-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.card-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
}

.nearby-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.nearby-item {
  display: flex;
  gap: 12px;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.nearby-item:hover {
  transform: translateX(4px);
}

.item-image {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  object-fit: cover;
  flex-shrink: 0;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.item-name {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.item-rating {
  color: #f59e0b;
  font-weight: 600;
}

.item-reviews {
  color: #6b7280;
}

.item-distance {
  font-size: 12px;
  color: #9ca3af;
}

@media (max-width: 1024px) {
  .scenic-detail-sidebar {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
}

@media (max-width: 768px) {
  .scenic-detail-sidebar {
    grid-template-columns: 1fr;
  }
}
</style>
