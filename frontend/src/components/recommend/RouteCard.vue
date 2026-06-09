<template>
  <el-card class="route-card" shadow="hover" @click="goDetail">
    <!-- 封面图 -->
    <div class="route-image">
      <img :src="coverImage" :alt="route.name" />
      <div class="route-badge">
        <el-tag type="primary" size="small">{{ route.days }}天{{ route.days > 1 ? (route.days - 1) + '晚' : '' }}</el-tag>
        <el-tag type="info" size="small" v-if="route.difficulty">{{ route.difficulty }}</el-tag>
      </div>
      <div class="route-price-overlay">
        <span v-if="route.totalPrice > 0">约 ¥{{ route.totalPrice }}</span>
        <span v-else class="free">免费游览</span>
      </div>
    </div>

    <!-- 路线信息 -->
    <div class="route-info">
      <h3>{{ route.name }}</h3>
      <p class="route-description" v-if="route.description">{{ route.description }}</p>

      <!-- 时间线 -->
      <div class="route-timeline">
        <div
          v-for="(dayGroup, di) in groupedByDay"
          :key="di"
          class="timeline-day"
        >
          <div class="day-dot"></div>
          <div class="day-label">Day {{ dayGroup.day }}</div>
          <div class="day-spots">
            <template v-for="(spot, si) in dayGroup.spots" :key="si">
              <span class="spot-name" @click.stop="goToSpot(spot)">{{ spot.name || spot }}</span>
              <span v-if="si < dayGroup.spots.length - 1" class="spot-arrow">→</span>
            </template>
          </div>
        </div>
      </div>

      <!-- 标签 -->
      <div class="route-tags" v-if="route.tags?.length">
        <el-tag
          v-for="tag in route.tags.slice(0, 3)"
          :key="tag"
          size="small"
          type="info"
          class="tag-item"
        >{{ tag }}</el-tag>
      </div>

      <!-- 操作按钮 -->
      <el-button
        type="primary"
        size="small"
        style="width:100%;margin-top:12px"
        @click.stop="usePlan"
      >
        <el-icon><Calendar /></el-icon>
        用此路线规划行程
      </el-button>
    </div>
  </el-card>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Calendar } from '@element-plus/icons-vue'
import { searchScenic } from '../../api/scenic'

const props = defineProps({
  route: { type: Object, required: true }
})

const router = useRouter()
// 缓存景点名 -> id，避免重复请求
const spotIdCache = ref({})

const coverImage = computed(() => {
  if (props.route.image && props.route.image !== '') return props.route.image
  if (Array.isArray(props.route.scenicSpots) && props.route.scenicSpots.length > 0) {
    const first = props.route.scenicSpots[0]
    if (first?.image) return first.image
  }
  return '/images/placeholders/route_placeholder.jpg'
})

const groupedByDay = computed(() => {
  const spots = props.route.scenicSpots || []
  const days = props.route.days || 1
  if (!spots.length) return []

  const hasDayField = spots.some(s => s?.day != null)
  if (hasDayField) {
    const map = {}
    spots.forEach(s => {
      const d = Number(s.day) || 1
      if (!map[d]) map[d] = []
      map[d].push(s)
    })
    return Object.keys(map).sort((a, b) => a - b).map(d => ({ day: Number(d), spots: map[d] }))
  }

  const perDay = Math.ceil(spots.length / days)
  return Array.from({ length: days }, (_, i) => ({
    day: i + 1,
    spots: spots.slice(i * perDay, (i + 1) * perDay)
  })).filter(g => g.spots.length > 0)
})

const goDetail = () => {
  router.push({ name: 'RouteDetail', params: { id: props.route.id } })
}

const usePlan = () => {
  router.push({ path: '/plan', query: { routeId: props.route.id } })
}

const goToSpot = async (spot) => {
  const name = spot?.name || spot || ''

  // 陶瓷工坊类关键词 → 跳转陶瓷体验列表
  if (/陶瓷|工坊|陶艺|DIY/i.test(name)) {
    router.push({ name: 'CeramicExperience' })
    return
  }

  // 如果景点对象直接带 id，直接跳转
  if (spot?.id) {
    router.push({ name: 'ScenicDetail', params: { id: spot.id } })
    return
  }

  if (!name) return

  // 命中缓存
  if (spotIdCache.value[name]) {
    router.push({ name: 'ScenicDetail', params: { id: spotIdCache.value[name] } })
    return
  }

  try {
    const res = await searchScenic(name)
    const list = Array.isArray(res) ? res : (res?.data || [])
    if (list.length > 0) {
      const id = list[0].id
      spotIdCache.value[name] = id
      router.push({ name: 'ScenicDetail', params: { id } })
    }
  } catch { /* 静默处理 */ }
}
</script>

<style scoped>
.route-card {
  cursor: pointer;
  transition: transform 0.3s;
  margin-bottom: 12px;
  height: 100%;
}

.route-card:hover {
  transform: translateY(-5px);
}

/* 封面图 */
.route-image {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
  border-radius: 4px;
  margin-bottom: 12px;
}

.route-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.route-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  display: flex;
  gap: 6px;
}

.route-price-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0,0,0,0.65));
  padding: 20px 10px 8px;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
}

.route-price-overlay .free {
  color: #95f0a0;
}

/* 路线信息 */
.route-info h3 {
  margin: 0 0 8px;
  font-size: 18px;
  color: #303133;
}

.route-description {
  color: #606266;
  font-size: 14px;
  margin: 0 0 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 时间线 */
.route-timeline {
  border-left: 2px solid #e4e7ed;
  padding-left: 14px;
  margin-bottom: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.timeline-day {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  position: relative;
}

.day-dot {
  position: absolute;
  left: -19px;
  top: 5px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--brand-500, #2f6efb);
  border: 2px solid #fff;
  box-shadow: 0 0 0 2px var(--brand-500, #2f6efb);
  flex-shrink: 0;
}

.day-label {
  font-size: 12px;
  font-weight: 700;
  color: var(--brand-500, #2f6efb);
  white-space: nowrap;
  min-width: 38px;
  padding-top: 1px;
}

.day-spots {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 3px;
  font-size: 13px;
  color: #303133;
  line-height: 1.6;
}

.spot-name {
  cursor: pointer;
  transition: color 0.15s;
}

.spot-name:hover {
  color: var(--brand-500, #2f6efb);
  text-decoration: underline;
}

.spot-arrow {
  color: #c0c4cc;
  font-size: 11px;
}

/* 标签 */
.route-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.tag-item {
  margin: 0;
}
</style>
