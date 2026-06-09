<template>
  <div class="amap-route-container glass-panel">
    <div class="map-header" v-if="showTransport">
      <h3>
        <el-icon><Location /></el-icon>
        智能交通规划
      </h3>
      <div class="header-actions">
        <el-button 
          v-if="showMap" 
          text 
          size="small" 
          @click="toggleMap"
          :icon="mapExpanded ? ArrowDown : ArrowUp"
        >
          {{ mapExpanded ? '收起地图' : '展开地图' }}
        </el-button>
        <el-button 
          v-if="routePlans.length > 0"
          text 
          size="small" 
          @click="refreshRoutes"
          :loading="loading"
          :icon="Refresh"
        >
          刷新
        </el-button>
      </div>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading && routePlans.length === 0" class="loading-state">
      <el-skeleton :rows="3" animated />
      <p class="loading-text">正在计算交通方案...</p>
    </div>
    
    <!-- 地图容器 -->
    <div 
      v-show="showMap && mapExpanded" 
      ref="mapContainer" 
      class="map-container"
      :class="{ 'map-loading': mapLoading, 'map-container--full': !showTransport }"
    >
      <div v-if="mapLoading" class="map-loading-overlay">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>地图加载中...</span>
      </div>
      <div v-if="!hasPlanDetails && !mapLoading && !errorMessage" class="map-empty-tip">
        <el-icon :size="48"><InfoFilled /></el-icon>
        <p>暂无行程数据，请先添加行程安排</p>
        <p style="font-size: 12px; margin-top: 8px; color: #909399;">请在左侧表格中添加行程地点，然后点击"保存行程"按钮</p>
      </div>
    </div>
    
    <!-- 交通方案列表 (可折叠面板) -->
    <div v-if="showTransport && routePlans.length > 0" class="route-plans-panel" :class="{ 'is-collapsed': !routeListExpanded }">
      <div class="route-plans-header" @click.stop="toggleRouteList">
        <span class="header-title"><el-icon><Guide /></el-icon> 详细交通方案</span>
        <div class="header-toggle-btn" title="点击展开/收起">
          <el-icon :class="{ 'is-rotated': !routeListExpanded }"><ArrowDown /></el-icon>
        </div>
      </div>
      
      <div class="route-plans-content custom-scrollbar">
        <div 
          v-for="(plan, index) in routePlans" 
          :key="index"
          class="route-plan-item"
        >
          <div class="route-plan-header-row">
            <div class="route-plan-title">
              <el-icon><Calendar /></el-icon>
              <span class="route-label">第{{ plan.day }}天</span>
              <el-tag size="small" type="info">{{ plan.segments.length }} 段行程</el-tag>
            </div>
            <div class="route-time">
              <span>{{ plan.startTime }}</span>
              <el-icon><ArrowRight /></el-icon>
              <span>{{ plan.endTime }}</span>
            </div>
          </div>
          
          <div class="route-segments">
            <div 
              v-for="(segment, segIndex) in plan.segments" 
              :key="segIndex"
              class="route-segment"
              @click="highlightRoute(segment, index, segIndex)"
              :class="{ 'is-highlighted': highlightedSegment === `${index}-${segIndex}` }"
            >
              <div class="segment-route-info">
                <div class="segment-from-to">
                  <span class="segment-point segment-from">{{ segment.from }}</span>
                  <el-icon class="segment-arrow"><ArrowRight /></el-icon>
                  <span class="segment-point segment-to">{{ segment.to }}</span>
                </div>
                <!-- 多方案展示 -->
                <div class="segment-options">
                  <div
                    v-for="(opt, oi) in (segment.options || [segment])"
                    :key="oi"
                    class="segment-option"
                  >
                    <el-tag :type="getModeTagType(opt.mode)" size="small" effect="plain" class="opt-tag">
                      {{ getModeLabel(opt.mode) }}
                    </el-tag>
                    <span class="opt-detail">{{ opt.routeDetail }}</span>
                    <span class="opt-meta">
                      <span>约{{ opt.durationMinutes }}分钟</span>
                      <span v-if="opt.costEstimate > 0"> · ¥{{ opt.costEstimate }}</span>
                      <span v-else> · 免费</span>
                    </span>
                  </div>
                </div>
              </div>
              <div class="segment-meta" v-if="!segment.options">
                <span class="meta-item">
                  <el-icon><Timer /></el-icon>
                  约 {{ segment.durationMinutes }} 分钟
                </span>
                <span class="meta-item" v-if="segment.costEstimate > 0">
                  <el-icon><Money /></el-icon>
                  约 {{ segment.costEstimate }} 元
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 空状态 -->
    <div v-else-if="!loading && hasPlanDetails" class="empty-state">
      <el-empty description="暂无交通方案" :image-size="80">
        <template #description>
          <p>请确保行程中至少包含2个景点</p>
        </template>
      </el-empty>
    </div>
    
    <!-- 无行程数据状态 -->
    <div v-else-if="!hasPlanDetails" class="empty-state">
      <el-empty description="暂无行程数据" :image-size="80">
        <template #description>
          <p>请先创建或编辑行程</p>
        </template>
      </el-empty>
    </div>
    
    <!-- 错误提示 -->
    <el-alert
      v-if="errorMessage"
      :title="errorMessage"
      type="warning"
      :closable="true"
      @close="errorMessage = ''"
      style="margin-top: 16px"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Location, 
  ArrowDown, 
  ArrowUp, 
  Refresh, 
  Loading,
  Calendar,
  ArrowRight,
  Timer,
  Money,
  InfoFilled
} from '@element-plus/icons-vue'
import { getTransportOptions } from '../api/plan'

const props = defineProps({
  // 行程详情数据
  planDetails: {
    type: Object,
    default: () => ({ days: [] })
  },
  // 是否显示地图
  showMap: {
    type: Boolean,
    default: true
  },
  // 高德地图 API Key（从环境变量获取）
  amapKey: {
    type: String,
    default: () => import.meta.env.VITE_AMAP_KEY || ''
  },
  // 是否显示详细交通方案面板
  showTransport: {
    type: Boolean,
    default: true
  }
})

const mapContainer = ref(null)
const mapExpanded = ref(false)
const loading = ref(false)
const mapLoading = ref(false)
const routePlans = ref([])
const errorMessage = ref('')
const highlightedSegment = ref('')
const routeListExpanded = ref(true)

let map = null
let markers = []
let polylines = []
let amapScriptLoaded = false

// 计算属性
const hasPlanDetails = computed(() => {
  return props.planDetails && 
         props.planDetails.days && 
         Array.isArray(props.planDetails.days) && 
         props.planDetails.days.length > 0
})

const hasRoutes = computed(() => {
  return routePlans.value.length > 0 && routePlans.value.some(plan => plan.segments.length > 0)
})

// 加载高德地图脚本
const loadAmapScript = () => {
  return new Promise((resolve, reject) => {
    if (window.AMap) {
      amapScriptLoaded = true
      resolve()
      return
    }
    
    if (amapScriptLoaded) {
      resolve()
      return
    }
    
    if (!props.amapKey) {
      reject(new Error('高德地图 API Key 未配置，请在环境变量中设置 VITE_AMAP_KEY'))
      return
    }
    
    // 创建全局回调函数
    window.initAmapCallback = () => {
      // 检查是否有错误
      if (window.AMap && window.AMap.config) {
        // 如果 AMap 加载成功但没有正确初始化，可能是 Key 错误
        amapScriptLoaded = true
        resolve()
      } else {
        // 延迟检查，因为脚本可能还在加载
        setTimeout(() => {
          if (window.AMap) {
            amapScriptLoaded = true
            resolve()
          } else {
            reject(new Error('高德地图 API 加载失败，请检查 API Key 是否正确'))
          }
        }, 1000)
      }
    }
    
    // 监听全局错误，捕获 "Error key!" 错误
    const errorListener = (event) => {
      if (event.message && event.message.includes('Error key')) {
        window.removeEventListener('error', errorListener)
        reject(new Error('API Key 错误：请检查 Key 是否正确、是否启用了 Web 服务、是否有域名/IP 白名单限制'))
      }
    }
    window.addEventListener('error', errorListener)
    
    // 5秒后移除监听器
    setTimeout(() => {
      window.removeEventListener('error', errorListener)
    }, 5000)
    
    // 错误回调函数
    window.initAmapErrorCallback = (error) => {
      console.error('高德地图加载错误:', error)
      let errorMsg = '高德地图脚本加载失败'
      if (error && error.message) {
        if (error.message.includes('USERKEY_PLAT_NOMATCH')) {
          errorMsg = 'API Key 平台类型不匹配，请确保使用的是 Web 端（JS API）的 Key'
        } else if (error.message.includes('INVALID_USER_KEY')) {
          errorMsg = 'API Key 无效，请检查 Key 是否正确'
        } else {
          errorMsg = error.message
        }
      }
      reject(new Error(errorMsg))
    }
    
    const script = document.createElement('script')
    script.src = `https://webapi.amap.com/maps?v=2.0&key=${props.amapKey}&callback=initAmapCallback`
    script.async = true
    
    script.onerror = () => {
      reject(new Error('高德地图脚本加载失败，请检查网络连接和 API Key'))
    }
    
    document.head.appendChild(script)
    
    // 监听全局错误
    const errorHandler = (event) => {
      if (event.message && event.message.includes('USERKEY_PLAT_NOMATCH')) {
        window.removeEventListener('error', errorHandler)
        reject(new Error('API Key 平台类型不匹配，请确保使用的是 Web 端（JS API）的 Key，而不是 Android/iOS 的 Key'))
      }
    }
    window.addEventListener('error', errorHandler)
    
    // 5秒后移除错误监听器
    setTimeout(() => {
      window.removeEventListener('error', errorHandler)
    }, 5000)
  })
}

// 初始化地图
const initMap = async () => {
  if (!props.showMap || !mapContainer.value) {
    console.warn('地图初始化跳过：showMap=', props.showMap, 'mapContainer=', mapContainer.value)
    return
  }
  
  // 调试信息
  console.log('开始初始化地图，API Key:', props.amapKey ? props.amapKey.substring(0, 10) + '...' : '未配置')
  
  try {
    await loadAmapScript()
    
    if (!window.AMap) {
      throw new Error('高德地图 API 未加载')
    }
    
    mapLoading.value = true
    
    await nextTick()
    
    // 确保容器有高度（flex 布局下由父容器决定，无需强制设置）
    if (!mapContainer.value.offsetHeight) {
      mapContainer.value.style.height = '100%'
    }
    
    // 初始化地图，默认中心点为景德镇
    try {
      map = new window.AMap.Map(mapContainer.value, {
        zoom: 13,
        center: [117.214664, 29.29256], // 景德镇中心坐标
        mapStyle: 'amap://styles/normal',
        viewMode: '2D'
      })
      
      // 监听地图错误事件
      map.on('error', (e) => {
        console.error('地图错误事件:', e)
        if (e && (e.message && e.message.includes('key') || e.type === 'error')) {
          mapLoading.value = false
          errorMessage.value = 'API Key 错误：请检查 Key 是否正确、是否启用了 Web 服务、是否有域名/IP 白名单限制'
          ElMessage.error('API Key 错误，请检查配置')
        }
      })
      
      // 等待地图加载完成
      map.on('complete', () => {
        console.log('地图加载完成')
        mapLoading.value = false
        if (routePlans.value.length > 0) {
          drawRoutesOnMap(routePlans.value)
        }
      })
      
      mapExpanded.value = true
    } catch (mapError) {
      console.error('创建地图实例失败:', mapError)
      throw new Error('创建地图失败：' + (mapError.message || '请检查 API Key 配置，错误信息：Error key'))
    }
  } catch (error) {
    console.error('地图初始化失败', error)
    mapLoading.value = false
    let errorMsg = error.message || '地图加载失败，将仅显示文字方案'
    
    // 针对特定错误给出更友好的提示
    if (errorMsg.includes('USERKEY_PLAT_NOMATCH') || errorMsg.includes('平台类型不匹配')) {
      errorMsg = 'API Key 平台类型不匹配：请确保使用的是 Web 端（JS API）的 Key，而不是 Android/iOS 的 Key。请在高德开放平台重新创建 Web 端 Key。'
    } else if (errorMsg.includes('INVALID_USER_KEY') || errorMsg.includes('Error key') || errorMsg.includes('key')) {
      errorMsg = `API Key 错误，请检查以下几点：
1. Key 是否正确（当前 Key: ${props.amapKey ? props.amapKey.substring(0, 20) + '...' : '未配置'}）
2. 确保 Key 已启用"Web服务（JS API）"
3. 检查是否有域名/IP白名单限制（如有，请添加当前域名：${window.location.hostname}）
4. 确认 Key 未过期或被禁用
5. 如果启用了安全密钥，需要配置 securityJsCode`
    }
    
    errorMessage.value = errorMsg
    ElMessage.error('地图加载失败：' + errorMsg)
    // 即使失败也展开地图容器，显示错误信息
    mapExpanded.value = true
  }
}

// 计算交通方案
const calculateRoutes = async () => {
  if (!hasPlanDetails.value) {
    routePlans.value = []
    return
  }
  
  loading.value = true
  errorMessage.value = ''
  
  try {
    const plans = []
    
    for (const day of props.planDetails.days) {
      if (!day.schedule || day.schedule.length === 0) continue
      
      // 提取所有有位置的地点（景点/餐饮/陶瓷工坊/市集/酒店，排除交通段本身）
      const scenicItems = day.schedule.filter(
        item => {
          const t = (item.type || '').toUpperCase()
          return t !== 'TRANSPORT' && item.title
        }
      )
      
      // 至少需要2个地点才能计算交通
      if (scenicItems.length < 2) continue
      
      const segments = []
      
      // 计算相邻景点之间的交通
      for (let i = 0; i < scenicItems.length - 1; i++) {
        const origin = scenicItems[i]
        const dest = scenicItems[i + 1]
        
        try {
          // 优先用坐标，其次用景点ID，最后用名称
          let res
          if (origin.lat && origin.lng && dest.lat && dest.lng) {
            res = await getTransportOptions(origin.title, dest.title, null, null, origin.lat, origin.lng, dest.lat, dest.lng)
          } else if (origin.scenicId && dest.scenicId) {
            res = await getTransportOptions(null, null, origin.scenicId, dest.scenicId)
          } else {
            res = await getTransportOptions(origin.title, dest.title)
          }
          
          const options = Array.isArray(res?.data) ? res.data : (Array.isArray(res) ? res : [])
          
          if (options.length > 0) {
            // 保留全部方案，按优先级排序
            const sorted = [...options].sort((a, b) => {
              const p = { WALK: 1, BIKE: 2, BUS: 3, METRO: 3, DIDI: 4, TAXI: 4, SELF_DRIVE: 5 }
              return (p[a.mode] || 9) - (p[b.mode] || 9)
            })
            segments.push({
              from: origin.title,
              to: dest.title,
              fromTime: origin.time || '',
              toTime: dest.time || '',
              fromId: origin.scenicId,
              toId: dest.scenicId,
              options: sorted,
              // 兼容旧字段：取第一个方案作为默认
              ...sorted[0]
            })
          }
        } catch (error) {
          console.error(`计算 ${origin.title} → ${dest.title} 的交通方案失败`, error)
          // 单个失败不影响整体，继续处理下一个
        }
      }
      
      if (segments.length > 0) {
        plans.push({
          day: day.day,
          startTime: scenicItems[0].time || '09:00',
          endTime: scenicItems[scenicItems.length - 1].time || '18:00',
          segments
        })
      }
    }
    
    routePlans.value = plans
    
    // 如果地图已初始化，在地图上绘制路线
    if (map && plans.length > 0) {
      await nextTick()
      drawRoutesOnMap(plans)
    }
    
    if (plans.length === 0) {
      ElMessage.info('未找到交通方案，请确保行程中至少包含2个地点')
    }
  } catch (error) {
    console.error('计算交通方案失败', error)
    errorMessage.value = '计算交通方案失败：' + (error.message || '未知错误')
    ElMessage.error('计算交通方案失败')
  } finally {
    loading.value = false
  }
}

// 选择最优交通方案
const selectBestOption = (options) => {
  // 优先级：公交/地铁 > 打车 > 步行 > 自驾
  const priority = { 
    BUS: 1, 
    METRO: 1, 
    DIDI: 2, 
    TAXI: 2, 
    WALK: 3, 
    SELF_DRIVE: 4 
  }
  
  return options.sort((a, b) => {
    const aPriority = priority[a.mode] || 99
    const bPriority = priority[b.mode] || 99
    if (aPriority !== bPriority) {
      return aPriority - bPriority
    }
    // 如果优先级相同，选择时间最短的
    return (a.durationMinutes || 999) - (b.durationMinutes || 999)
  })[0]
}

// 在地图上绘制路线
const drawRoutesOnMap = async (plans) => {
  if (!map || !window.AMap) return
  
  // 清除之前的标记和路线
  clearMap()
  
  try {
    const allPoints = []
    
    for (const plan of plans) {
      for (const segment of plan.segments) {
        allPoints.push({
          from: segment.from,
          to: segment.to,
          fromId: segment.fromId,
          toId: segment.toId,
          segment,
          mode: segment.mode
        })
      }
    }
    
    if (allPoints.length === 0) return

    // 批量获取坐标
    const coordinates = await Promise.all(
      allPoints.map(async (point) => {
        const originCoord = await geocodeAddress(point.from, point.fromId)
        const destCoord = await geocodeAddress(point.to, point.toId)
        return { point, originCoord, destCoord }
      })
    )
    
    // 过滤掉无法获取坐标的点
    const validPoints = coordinates.filter(c => c.originCoord && c.destCoord)
    
    if (validPoints.length === 0) {
      console.warn('无法获取景点坐标，跳过地图绘制')
      return
    }
    
    const lngLatList = []
    // 收集所有唯一地点（去重），用于统一编号
    const uniquePoints = []
    const pointIndexMap = new Map()

    const getPointIndex = (name) => {
      if (!pointIndexMap.has(name)) {
        pointIndexMap.set(name, uniquePoints.length)
        uniquePoints.push(name)
      }
      return pointIndexMap.get(name)
    }

    // 类型图标映射
    const typeIcon = (name) => {
      if (/酒店|民宿|客栈/.test(name)) return { icon: '🏨', color: '#52c41a', bg: '#f6ffed', border: '#b7eb8f' }
      if (/餐|美食|饭|食/.test(name)) return { icon: '🍜', color: '#d46b08', bg: '#fff7e6', border: '#ffd591' }
      if (/陶瓷|工坊|窑|瓷/.test(name)) return { icon: '🏺', color: '#722ed1', bg: '#f9f0ff', border: '#d3adf7' }
      if (/市集|市场|集市/.test(name)) return { icon: '🛍', color: '#c41d7f', bg: '#fff0f6', border: '#ffadd2' }
      return { icon: '📍', color: '#1677ff', bg: '#e8f0fe', border: '#91caff' }
    }

    for (let index = 0; index < validPoints.length; index++) {
      const { point, originCoord, destCoord } = validPoints[index]
      
      const originLngLat = new window.AMap.LngLat(originCoord[0], originCoord[1])
      const destLngLat = new window.AMap.LngLat(destCoord[0], destCoord[1])
      lngLatList.push(originLngLat, destLngLat)

      // 起点标牌
      const fromIdx = getPointIndex(point.from)
      const fromStyle = typeIcon(point.from)
      const originMarker = new window.AMap.Marker({
        position: originLngLat,
        title: point.from,
        zIndex: 100,
        content: `<div class="amap-custom-marker" style="background:${fromStyle.bg};border:1.5px solid ${fromStyle.border};color:${fromStyle.color}">
          <span class="amap-marker-num">${fromIdx + 1}</span>
          <span class="amap-marker-icon">${fromStyle.icon}</span>
          <span class="amap-marker-name">${point.from}</span>
          <div class="amap-marker-tail" style="border-top-color:${fromStyle.border}"></div>
        </div>`,
        offset: new window.AMap.Pixel(-60, -44)
      })
      map.add(originMarker)
      markers.push(originMarker)
      
      // 终点标牌（最后一段或下一段起点不同时才加）
      const isLast = index === validPoints.length - 1
      const nextFrom = !isLast ? validPoints[index + 1].point.from : null
      if (isLast || point.to !== nextFrom) {
        const toIdx = getPointIndex(point.to)
        const toStyle = typeIcon(point.to)
        const destMarker = new window.AMap.Marker({
          position: destLngLat,
          title: point.to,
          zIndex: 100,
          content: `<div class="amap-custom-marker" style="background:${toStyle.bg};border:1.5px solid ${toStyle.border};color:${toStyle.color}">
            <span class="amap-marker-num">${toIdx + 1}</span>
            <span class="amap-marker-icon">${toStyle.icon}</span>
            <span class="amap-marker-name">${point.to}</span>
            <div class="amap-marker-tail" style="border-top-color:${toStyle.border}"></div>
          </div>`,
          offset: new window.AMap.Pixel(-60, -44)
        })
        map.add(destMarker)
        markers.push(destMarker)
      }
      
      // 绘制路线折线（直线连接，避免 Driving/Transfer 异步问题）
      const color = point.mode === 'WALK' ? '#909399'
        : (point.mode === 'BUS' || point.mode === 'METRO') ? '#67C23A'
        : '#E6A23C'
      const polyline = new window.AMap.Polyline({
        path: [originLngLat, destLngLat],
        strokeColor: color,
        strokeWeight: 4,
        strokeStyle: point.mode === 'WALK' ? 'dashed' : 'solid',
        strokeOpacity: 0.8,
        zIndex: 50
      })
      map.add(polyline)
      polylines.push(polyline)
    }
    
    // 自适应视野
    if (lngLatList.length > 0) {
      map.setFitView(markers.concat(polylines), false, [60, 60, 60, 60])
    }
  } catch (error) {
    console.error('绘制地图路线失败', error)
    errorMessage.value = '地图路线绘制失败：' + (error.message || '未知错误')
  }
}

// 地理编码：将地址转换为坐标
const geocodeAddress = async (address, scenicId) => {
  return new Promise((resolve) => {
    if (!window.AMap || !address || address.trim() === '') {
      resolve(null)
      return
    }
    
    const searchAddress = address.includes('景德镇') ? address : `景德镇${address}`
    
    // v2.0 必须通过 AMap.plugin() 确保插件已加载
    window.AMap.plugin('AMap.Geocoder', () => {
      const geocoder = new window.AMap.Geocoder({ city: '景德镇' })
      geocoder.getLocation(searchAddress, (status, result) => {
        if (status === 'complete' && result.geocodes && result.geocodes.length > 0) {
          const location = result.geocodes[0].location
          resolve([location.lng, location.lat])
        } else {
          console.warn(`无法获取 "${address}" 的坐标`)
          resolve(null)
        }
      })
    })
  })
}

// 清除地图上的标记和路线
const clearMap = () => {
  if (map) {
    markers.forEach(marker => map.remove(marker))
    polylines.forEach(polyline => map.remove(polyline))
  }
  markers = []
  polylines = []
}

// 高亮路线
const highlightRoute = (segment, planIndex, segIndex) => {
  highlightedSegment.value = `${planIndex}-${segIndex}`
  // 可以在这里添加地图上的高亮逻辑
}

// 刷新路线
const refreshRoutes = async () => {
  await calculateRoutes()
  ElMessage.success('交通方案已刷新')
}

const toggleMap = () => {
  mapExpanded.value = !mapExpanded.value
  if (mapExpanded.value && map) {
    nextTick(() => {
      map.resize()
    })
  }
}

const toggleRouteList = () => {
  routeListExpanded.value = !routeListExpanded.value
}

const getModeLabel = (mode) => {
  const map = {
    BUS: '公交',
    METRO: '地铁',
    DIDI: '打车',
    TAXI: '出租车',
    WALK: '步行',
    SELF_DRIVE: '自驾'
  }
  return map[mode] || mode
}

const getModeTagType = (mode) => {
  const map = {
    BUS: 'success',
    METRO: 'success',
    DIDI: 'warning',
    TAXI: 'warning',
    WALK: 'info',
    SELF_DRIVE: 'danger'
  }
  return map[mode] || 'info'
}

// 监听行程详情变化，自动计算交通方案
watch(() => props.planDetails, (newVal) => {
  if (newVal && newVal.days) {
    calculateRoutes()
  }
}, { deep: true, immediate: true })

onMounted(async () => {
  if (props.showMap) {
    // 默认展开地图
    mapExpanded.value = true
    try {
      await initMap()
    } catch (error) {
      console.error('地图初始化失败', error)
      // 地图失败不影响文字方案显示
    }
  }
})

onUnmounted(() => {
  clearMap()
  if (map) {
    map.destroy()
    map = null
  }
})
</script>

<style scoped>
/* 地图自定义标牌 */
:global(.amap-custom-marker) {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 5px 10px 5px 6px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
  box-shadow: 0 2px 8px rgba(0,0,0,0.18);
  cursor: pointer;
  max-width: 140px;
  line-height: 1.4;
}
:global(.amap-marker-num) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: currentColor;
  color: #fff !important;
  font-size: 11px;
  font-weight: 700;
  flex-shrink: 0;
  filter: brightness(0.85);
}
:global(.amap-marker-icon) {
  font-size: 13px;
  flex-shrink: 0;
}
:global(.amap-marker-name) {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 90px;
}
:global(.amap-marker-tail) {
  position: absolute;
  bottom: -7px;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 0;
  border-left: 6px solid transparent;
  border-right: 6px solid transparent;
  border-top: 7px solid;
}

.amap-route-container {
  padding: 0;
  margin-bottom: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

.map-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0;
  padding: 12px 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  flex-shrink: 0;
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

.header-actions {
  display: flex;
  gap: 8px;
}

.map-container {
  width: 100%;
  height: 320px;
  flex-shrink: 0;
  border-radius: 0;
  overflow: hidden;
  margin-bottom: 0;
  border: none;
  border-bottom: 1px solid #eee;
  position: relative;
  background: #f5f7fa;
}

/* 无交通方案时地图撑满整个容器 */
.map-container--full {
  flex: 1;
  height: 0;
  min-height: 0;
  border-bottom: none;
}

.map-loading {
  position: relative;
}

.map-loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  z-index: 1000;
  color: #606266;
  font-size: 14px;
}

.loading-state {
  padding: 20px 0;
}

.loading-text {
  text-align: center;
  color: #909399;
  margin-top: 12px;
  font-size: 14px;
}

.map-empty-tip {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.95);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  z-index: 999;
  color: #909399;
  font-size: 14px;
}

.map-empty-tip p {
  margin: 0;
  color: #909399;
}

.map-error-tip {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.95);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  z-index: 999;
  color: #f56c6c;
  font-size: 14px;
}

.map-error-tip p {
  margin: 0;
  color: #f56c6c;
}

/* Route Plans Panel */
.route-plans-panel {
  flex: 1;
  min-height: 0;
  background: #fff;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  border-top: 1px solid #eee;
}

.route-plans-panel.is-collapsed .route-plans-content {
  display: none;
}

/* Ensure header is always clickable and visible */
.route-plans-header {
  height: 48px; /* Fixed height for header */
  min-height: 48px;
  padding: 0 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.95);
  border-bottom: 1px solid rgba(0,0,0,0.05);
  font-weight: 600;
  color: #303133;
  flex-shrink: 0; /* Prevent header from shrinking */
  z-index: 301;
  user-select: none;
  transition: background-color 0.2s;
}

.route-plans-header:active {
  background: rgba(240, 242, 245, 0.95);
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-toggle-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  transition: background-color 0.2s;
}

.header-toggle-btn:hover {
  background-color: rgba(0, 0, 0, 0.05);
}

.route-plans-header .el-icon {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 16px;
}

.route-plans-header .el-icon.is-rotated {
  transform: rotate(180deg);
}

.route-plans-content {
  overflow-y: auto;
  padding: 8px;
  background: rgba(245, 247, 250, 0.5);
  flex: 1; /* Take remaining space */
  overscroll-behavior: contain;
}

.route-plan-item {
  background: rgba(255, 255, 255, 0.6);
  border-radius: 8px;
  padding: 10px 12px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.route-plan-item:hover {
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.route-plan-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e4e7ed;
}

.route-plan-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.route-label {
  font-weight: 600;
  color: #303133;
  font-size: 16px;
}

.route-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #909399;
}

.route-segments {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.route-segment {
  background: white;
  padding: 8px 10px;
  border-radius: 6px;
  border-left: 3px solid #409eff;
  cursor: pointer;
  transition: all 0.2s;
}

.route-segment:hover {
  border-left-color: #66b1ff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
}

.route-segment.is-highlighted {
  border-left-color: #409eff;
  background: #ecf5ff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.2);
}

.segment-route-info {
  margin-bottom: 8px;
}

.segment-from-to {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 14px;
}

.segment-point {
  font-weight: 500;
  color: #303133;
}

.segment-from {
  color: #f56c6c;
}

.segment-to {
  color: #409eff;
}

.segment-arrow {
  color: #909399;
  font-size: 12px;
}

.segment-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.segment-options {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-top: 6px;
}

.segment-option {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  flex-wrap: nowrap;
  min-width: 0;
}

.opt-tag { flex-shrink: 0; }

.opt-detail {
  flex: 1;
  font-size: 12px;
  color: #555;
  line-height: 1.5;
  min-width: 0;
}

.opt-meta {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
  flex-shrink: 0;
}

.segment-route-detail {
  flex: 1;
  color: #606266;
  font-size: 13px;
  line-height: 1.5;
  min-width: 200px;
}

.segment-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #909399;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.empty-state {
  padding: 40px 0;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .map-container {
    min-height: 200px;
  }
  
  .route-plan-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .segment-from-to {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .segment-arrow {
    transform: rotate(90deg);
  }
  
  .segment-route-detail {
    min-width: auto;
  }
  
  .segment-meta {
    flex-direction: column;
    gap: 8px;
  }
}
</style>

