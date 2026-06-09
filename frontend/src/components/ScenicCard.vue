<template>
  <div class="scenic-card-wrapper">
    <BaseCard class="scenic-card" @click="handleClick">
      <div class="cover">
        <img :src="scenic.image || '/placeholder.jpg'" :alt="scenic.name" loading="lazy" />
        <div class="gradient"></div>
        <div class="overlay">
          <div class="overlay-top">
            <el-tag v-if="hasActiveActivity" size="small" type="danger" effect="dark">优惠活动</el-tag>

          </div>
          <div class="overlay-bottom">
            <h3 class="title">{{ scenic.name }}</h3>
            <div class="meta">
              <div class="rating">
                <el-rate :model-value="getRatingValue(scenic.rating)" disabled size="small" />
              </div>
              <div class="price" :class="{ free: formatPrice(scenic.price)==='免费' }">
                <span class="price-value">{{ formatPrice(scenic.price) }}</span>
              </div>
            </div>
          </div>
        </div>
        <div class="quick-actions" @click.stop>
          <el-button class="icon-btn" :class="{ active: isFavorited }" circle :title="isFavorited ? '已收藏' : '收藏'" @click="handleFavorite">
            <el-icon><Star /></el-icon>
            <span class="fav-badge" :class="{ pulse: isFavorited }">{{ favCount }}</span>
          </el-button>
          <el-button class="pill-btn" type="primary" round @click="handleAddToPlan">
            <el-icon><Calendar /></el-icon>
            <span>加入行程</span>
          </el-button>
        </div>
      </div>
      <div class="tags" v-if="!hideTags && scenic.tags">
        <el-tag
          v-for="tag in (Array.isArray(scenic.tags) ? scenic.tags : scenic.tags?.split(','))"
          :key="tag"
          size="small"
          type="info"
          effect="plain"
        >
          {{ tag }}
        </el-tag>
      </div>
    </BaseCard>

    <AddToPlanDialog
      v-model="showAddToPlanDialog"
      :item="itemToAdd"
      :inline="true"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Star, Calendar, Money } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { addFavorite, removeFavorite, checkFavorite } from '../api/favoriteGateway'
import AddToPlanDialog from './AddToPlanDialog.vue'
import BaseCard from './base/BaseCard.vue'
import { useWishlistCounts } from '../composables/useWishlistCounts'
import { resolveFavoriteType } from '../utils/favoriteType'

const props = defineProps({
  scenic: {
    type: Object,
    required: true
  },
  hideTags: {
    type: Boolean,
    default: false
  }
})

const router = useRouter()
const userStore = useUserStore()
const isFavorited = ref(false)
const showAddToPlanDialog = ref(false)
const itemToAdd = ref({})
const clickPosition = ref(null)
const { getCount, increment, decrement } = useWishlistCounts()
const favCount = ref(0)

const formatPrice = (price) => {
  if (price === null || price === undefined || price === '' || price === '免费') {
    return '免费'
  }
  // 如果已经是带¥的字符串，直接返回
  if (typeof price === 'string' && price.startsWith('¥')) {
    return price
  }
  const num = parseFloat(price)
  if (isNaN(num)) {
    return price // 非数字字符串直接显示
  }
  if (num === 0) {
    return '免费'
  }
  return `¥${price}`
}


const handleClick = () => {
  // 根据类型跳转到不同的详情页
  const sourceType = props.scenic.type || props.scenic.category
  const type = sourceType === 'MERCHANT' ? props.scenic.category : sourceType
  
  if (type === 'FOOD') {
    router.push(`/food/${props.scenic.id}`)
  } else if (type === 'HOTEL') {
    router.push(`/hotel/${props.scenic.id}`)
  } else if (type === 'CERAMIC') {
    router.push(`/ceramic-workshop/${props.scenic.id}`)
  } else if (type === 'MARKETPLACE') {
    router.push(`/marketplace/${props.scenic.id}`)
  } else if (type === 'ROUTE') {
    router.push(`/route/${props.scenic.id}`)
  } else if (type === 'SCENIC' && props.scenic.scenicSpotId) {
    // 如果是景点类型的商家，且有景点ID，跳转到景点详情
    router.push(`/scenic/${props.scenic.scenicSpotId}`)
  } else {
    // 默认为景点
    router.push(`/scenic/${props.scenic.id}`)
  }
}

// 检查收藏状态
const checkFavoriteStatus = async () => {
  const userId = userStore.user?.id
  if (!userId) {
    isFavorited.value = false
    return
  }
  
  try {
    const response = await checkFavorite({
      userId,
      targetId: props.scenic.id,
      targetType: resolveFavoriteType(props.scenic)
    })
    // 后端返回格式: { success: true, data: boolean }
    if (response.success && response.data === true) {
      isFavorited.value = true
    } else {
      isFavorited.value = false
    }
  } catch (error) {
    // 如果接口返回404或错误，说明未收藏
    if (error.response?.status === 404) {
      isFavorited.value = false
    } else {
      // 其他错误也默认为未收藏
      isFavorited.value = false
    }
  }
}

const handleFavorite = async () => {
  try {
    const userId = userStore.user?.id
    if (!userId) {
      ElMessage.warning('请先登录')
      router.push('/login')
      return
    }
    
    const targetType = resolveFavoriteType(props.scenic)

    // 先检查当前收藏状态
    if (isFavorited.value) {
      // 如果已收藏，显示提示并取消收藏
      try {
        await removeFavorite({
          userId,
          targetId: props.scenic.id,
          targetType
        })
        isFavorited.value = false
        decrement(props.scenic.id)
        favCount.value = getCount(props.scenic.id)
        ElMessage.success('已取消收藏')
      } catch (error) {
        console.error('取消收藏失败:', error)
        const errorMessage = error.response?.data?.message || error.message || '取消收藏失败，请重试'
        ElMessage.error(errorMessage)
      }
    } else {
      // 尝试添加收藏
      try {
        await addFavorite({
          userId,
          targetId: props.scenic.id,
          targetType
        })
        isFavorited.value = true
        increment(props.scenic.id)
        favCount.value = getCount(props.scenic.id)
        ElMessage.success('已添加到收藏')
      } catch (addError) {
        // 如果后端返回"已经收藏过"的错误，更新状态并显示提示
        const errorMsg = addError.response?.data?.message || addError.message || ''
        if (errorMsg.includes('已经收藏') || errorMsg.includes('已收藏')) {
          isFavorited.value = true
          increment(props.scenic.id)
          favCount.value = getCount(props.scenic.id)
          ElMessage.info('该景点已收藏')
        } else {
          throw addError
        }
      }
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    const status = error?.response?.status
    const errorMessage =
      status === 400 || status === 404
        ? '收藏对象不存在或已下架'
        : (error.response?.data?.message || error.message || '操作失败，请重试')
    ElMessage.error(errorMessage)
    await checkFavoriteStatus()
  }
}

// 组件挂载时检查收藏状态
onMounted(() => {
  if (userStore.isLoggedIn) {
    checkFavoriteStatus()
  }
  favCount.value = getCount(props.scenic.id)
})

// 加入行程
const handleAddToPlan = (event) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  const favoriteType = resolveFavoriteType(props.scenic)
  const itemType = favoriteType === 'MERCHANT'
    ? String(props.scenic.category || '').toLowerCase()
    : 'scenic'

  clickPosition.value = null
  itemToAdd.value = {
    id: props.scenic.id,
    title: props.scenic.name,
    type: itemType || 'scenic',
    description: props.scenic.description || '游览景点'
  }
  showAddToPlanDialog.value = true
}

// 判断活动是否进行中（复用逻辑）
const isActivityActive = (activity) => {
  const now = new Date()
  const end = activity.endTime ? new Date(activity.endTime) : null
  const start = activity.startTime ? new Date(activity.startTime) : null
  if (end && end < now) return false
  if (start && start > now) return false
  return true
}

// 是否有进行中的活动（用于封面标签）
const hasActiveActivity = computed(() =>
  Array.isArray(props.scenic.activities) && props.scenic.activities.some(isActivityActive)
)
const getRatingValue = (rating) => {
  if (rating === null || rating === undefined) {
    return 0
  }
  // 如果是字符串，转换为数字
  if (typeof rating === 'string') {
    const num = parseFloat(rating)
    return isNaN(num) ? 0 : num
  }
  // 如果是数字，直接返回
  if (typeof rating === 'number') {
    return rating
  }
  // 其他情况返回0
  return 0
}

// 监听登录状态变化
watch(() => userStore.isLoggedIn, (isLoggedIn) => {
  if (isLoggedIn) {
    checkFavoriteStatus()
  } else {
    isFavorited.value = false
  }
})
</script>

<style scoped>
.scenic-card-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 400px;
}
.scenic-card {
  cursor: pointer;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.cover {
  position: relative;
  height: 100%;
  min-height: 360px;
  border-radius: var(--radius-16);
  overflow: hidden;
}
.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transform: scale(1.0);
  transition: transform .25s ease;
  display: block;
}
.scenic-card:hover .cover img {
  transform: scale(1.03);
}
.gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(0,0,0,0.0) 40%, rgba(0,0,0,0.55) 100%);
  pointer-events: none;
}
.overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 16px;
}
.overlay-top {
  display: flex;
}
.overlay-bottom {
  color: #fff;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0) 0%, rgba(0, 0, 0, 0.36) 100%);
  border-radius: 10px;
  padding: 10px 12px;
  min-height: 84px;
}
.title {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 700;
  text-shadow: 0 1px 3px rgba(0,0,0,.35);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.price .price-value {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  text-shadow: 0 1px 3px rgba(0,0,0,.35);
}
.price.free .price-value {
  opacity: 0.9;
}
.quick-actions {
  position: absolute;
  right: 12px;
  top: 12px;
  display: flex;
  gap: 8px;
  opacity: 0;
  transform: translateY(-4px);
  transition: opacity .2s ease, transform .2s ease;
  pointer-events: none;
}
.scenic-card:hover .quick-actions {
  opacity: 1;
  transform: translateY(0);
  pointer-events: auto;
}
.icon-btn {
  width: 40px;
  height: 40px;
  background: rgba(255,255,255,.9);
  border: none;
  box-shadow: var(--shadow-sm);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.icon-btn:hover {
  background: #fff;
}
.pill-btn {
  border: none;
  box-shadow: var(--shadow-sm);
}
.icon-btn.active {
  background: #fff;
}
.fav-badge {
  position: absolute;
  top: -6px;
  right: -6px;
  min-width: 18px;
  height: 18px;
  padding: 0 4px;
  border-radius: 999px;
  background: var(--brand-500);
  color: #fff;
  font-size: 11px;
  line-height: 18px;
  text-align: center;
  box-shadow: var(--shadow-sm);
  transform: scale(0.9);
  transition: transform .2s ease;
}
.scenic-card:hover .fav-badge { transform: scale(1); }
.pulse {
  animation: pulse .35s ease;
}
@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.12); }
  100% { transform: scale(1); }
}
.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}

@media (max-width: 992px) {
  .scenic-card-wrapper {
    min-height: 380px;
  }

  .cover {
    min-height: 340px;
  }
}
</style>