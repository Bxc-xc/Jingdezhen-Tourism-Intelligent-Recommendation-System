<template>
  <el-dialog
    v-model="dialogVisible"
    title="交通方案查询"
    width="700px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="transport-query">
      <!-- 查询表单 -->
      <el-form :model="queryForm" label-width="100px" class="query-form">
        <el-form-item label="起点">
          <el-input
            v-model="queryForm.originName"
            placeholder="请输入起点名称或从行程中选择"
            clearable
            @focus="showOriginSuggestions = true"
            @blur="handleOriginBlur"
          />
          <div v-if="showOriginSuggestions && (originSuggestions.length > 0 || planScenics.length > 0)" class="suggestions">
            <!-- 优先显示行程中的景点 -->
            <div v-if="planScenics.length > 0" class="suggestion-section">
              <div class="suggestion-section-title">行程中的景点</div>
              <div
                v-for="spot in filteredPlanScenics"
                :key="'plan-' + spot.id"
                class="suggestion-item plan-item"
                @click="selectOrigin(spot)"
              >
                <span class="spot-name">{{ spot.name }}</span>
                <span class="spot-day">第{{ spot.day }}天</span>
              </div>
            </div>
            <!-- 显示其他景点建议 -->
            <div v-if="originSuggestions.length > 0" class="suggestion-section">
              <div v-if="planScenics.length > 0" class="suggestion-section-title">其他景点</div>
              <div
                v-for="spot in originSuggestions"
                :key="spot.id"
                class="suggestion-item"
                @click="selectOrigin(spot)"
              >
                {{ spot.name }}
              </div>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="终点">
          <el-input
            v-model="queryForm.destName"
            placeholder="请输入终点名称或从行程中选择"
            clearable
            @focus="showDestSuggestions = true"
            @blur="handleDestBlur"
          />
          <div v-if="showDestSuggestions && (destSuggestions.length > 0 || planScenics.length > 0)" class="suggestions">
            <!-- 优先显示行程中的景点 -->
            <div v-if="planScenics.length > 0" class="suggestion-section">
              <div class="suggestion-section-title">行程中的景点</div>
              <div
                v-for="spot in filteredPlanScenicsDest"
                :key="'plan-' + spot.id"
                class="suggestion-item plan-item"
                @click="selectDest(spot)"
              >
                <span class="spot-name">{{ spot.name }}</span>
                <span class="spot-day">第{{ spot.day }}天</span>
              </div>
            </div>
            <!-- 显示其他景点建议 -->
            <div v-if="destSuggestions.length > 0" class="suggestion-section">
              <div v-if="planScenics.length > 0" class="suggestion-section-title">其他景点</div>
              <div
                v-for="spot in destSuggestions"
                :key="spot.id"
                class="suggestion-item"
                @click="selectDest(spot)"
              >
                {{ spot.name }}
              </div>
            </div>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="queryTransport" :loading="loading" style="width: 100%">
            <el-icon><Search /></el-icon> 查询交通方案
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 查询结果 -->
      <div v-if="transportOptions.length > 0" class="transport-results">
        <h4>推荐方案（共 {{ transportOptions.length }} 个）</h4>
        <div class="options-list">
          <div
            v-for="(option, index) in transportOptions"
            :key="index"
            class="transport-option"
            :class="{ 'is-selected': selectedIndex === index }"
            @click="selectOption(index)"
          >
            <div class="option-header">
              <el-tag :type="getModeTagType(option.mode)" size="small">
                {{ getModeLabel(option.mode) }}
              </el-tag>
              <span class="option-label">{{ option.label }}</span>
            </div>
            <div class="option-content">
              <p class="route-detail">{{ option.routeDetail }}</p>
              <div class="option-meta">
                <span class="meta-item">
                  <el-icon><Timer /></el-icon>
                  约 {{ option.durationMinutes }} 分钟
                </span>
                <span class="meta-item">
                  <el-icon><Money /></el-icon>
                  约 {{ option.costEstimate }} 元
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && transportOptions.length === 0 && hasQueried" class="empty-results">
        <el-empty description="暂无交通方案，请尝试其他起终点" :image-size="80" />
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button
          v-if="selectedOption"
          type="primary"
          @click="applyOption"
        >
          应用选中方案
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Timer, Money } from '@element-plus/icons-vue'
import { getTransportOptions } from '../api/plan'
import { getScenicList } from '../api/scenic'
import { computed } from 'vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  // 预设的起点和终点
  defaultOrigin: {
    type: String,
    default: ''
  },
  defaultDest: {
    type: String,
    default: ''
  },
  // 当前行程中的景点列表，用于快速选择
  planScenics: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'apply'])

const dialogVisible = ref(false)
const loading = ref(false)
const hasQueried = ref(false)
const selectedIndex = ref(-1)
const transportOptions = ref([])
const scenicSpots = ref([])
const showOriginSuggestions = ref(false)
const showDestSuggestions = ref(false)
const originSuggestions = ref([])
const destSuggestions = ref([])

const queryForm = reactive({
  originName: '',
  destName: '',
  originId: null,
  destId: null
})

watch(() => props.modelValue, (val) => {
  dialogVisible.value = val
  if (val) {
    queryForm.originName = props.defaultOrigin || ''
    queryForm.destName = props.defaultDest || ''
    loadScenicSpots()
  }
})

// 计算属性：过滤行程中的景点（用于起点）
const filteredPlanScenics = computed(() => {
  if (!props.planScenics || props.planScenics.length === 0) return []
  const keyword = queryForm.originName.toLowerCase()
  if (!keyword) return props.planScenics.slice(0, 10)
  return props.planScenics
    .filter(spot => spot.name && spot.name.toLowerCase().includes(keyword))
    .slice(0, 10)
})

// 计算属性：过滤行程中的景点（用于终点）
const filteredPlanScenicsDest = computed(() => {
  if (!props.planScenics || props.planScenics.length === 0) return []
  const keyword = queryForm.destName.toLowerCase()
  if (!keyword) return props.planScenics.slice(0, 10)
  return props.planScenics
    .filter(spot => spot.name && spot.name.toLowerCase().includes(keyword))
    .slice(0, 10)
})

watch(() => queryForm.originName, (val) => {
  if (val && scenicSpots.value.length > 0) {
    // 排除已经在行程中的景点，避免重复显示
    const planSpotNames = props.planScenics.map(s => s.name?.toLowerCase() || '')
    originSuggestions.value = scenicSpots.value
      .filter(spot => {
        const name = spot.name?.toLowerCase() || ''
        return name.includes(val.toLowerCase()) && !planSpotNames.includes(name)
      })
      .slice(0, 5)
  } else {
    originSuggestions.value = []
  }
})

watch(() => queryForm.destName, (val) => {
  if (val && scenicSpots.value.length > 0) {
    // 排除已经在行程中的景点，避免重复显示
    const planSpotNames = props.planScenics.map(s => s.name?.toLowerCase() || '')
    destSuggestions.value = scenicSpots.value
      .filter(spot => {
        const name = spot.name?.toLowerCase() || ''
        return name.includes(val.toLowerCase()) && !planSpotNames.includes(name)
      })
      .slice(0, 5)
  } else {
    destSuggestions.value = []
  }
})

const loadScenicSpots = async () => {
  try {
    const res = await getScenicList()
    scenicSpots.value = Array.isArray(res?.data) ? res.data : (Array.isArray(res) ? res : [])
  } catch (error) {
    console.error('加载景点列表失败', error)
    // 失败不影响使用，只是没有自动补全功能
  }
}

const selectOrigin = (spot) => {
  queryForm.originName = spot.name
  showOriginSuggestions.value = false
  // 如果选择了行程中的景点，可以保存ID以便后续使用
  if (spot.id) {
    queryForm.originId = spot.id
  }
}

const selectDest = (spot) => {
  queryForm.destName = spot.name
  showDestSuggestions.value = false
  // 如果选择了行程中的景点，可以保存ID以便后续使用
  if (spot.id) {
    queryForm.destId = spot.id
  }
}

// 处理输入框失焦，延迟隐藏建议列表
const handleOriginBlur = () => {
  setTimeout(() => {
    showOriginSuggestions.value = false
  }, 200)
}

const handleDestBlur = () => {
  setTimeout(() => {
    showDestSuggestions.value = false
  }, 200)
}

const queryTransport = async () => {
  if (!queryForm.originName || !queryForm.destName) {
    ElMessage.warning('请填写起点和终点')
    return
  }

  loading.value = true
  hasQueried.value = true
  selectedIndex.value = -1
  transportOptions.value = []

  try {
    // 优先使用景点ID查询（如果可用），否则使用名称
    let res
    if (queryForm.originId && queryForm.destId) {
      res = await getTransportOptions(null, null, queryForm.originId, queryForm.destId)
    } else {
      res = await getTransportOptions(queryForm.originName, queryForm.destName)
    }
    
    const options = Array.isArray(res?.data) ? res.data : (Array.isArray(res) ? res : [])

    if (options.length === 0) {
      ElMessage.warning('暂无交通方案')
    } else {
      transportOptions.value = options
      ElMessage.success(`找到 ${options.length} 个交通方案`)
    }
  } catch (error) {
    console.error('查询交通方案失败', error)
    ElMessage.error('查询失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const selectOption = (index) => {
  selectedIndex.value = index
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
    BUS: 'primary',
    METRO: 'success',
    DIDI: 'warning',
    TAXI: 'warning',
    WALK: 'info',
    SELF_DRIVE: 'danger'
  }
  return map[mode] || ''
}

const selectedOption = computed(() => {
  return selectedIndex.value >= 0 ? transportOptions.value[selectedIndex.value] : null
})

const applyOption = () => {
  if (selectedOption.value) {
    emit('apply', {
      ...selectedOption.value,
      originName: queryForm.originName,
      destName: queryForm.destName
    })
    handleClose()
  }
}

const handleClose = () => {
  dialogVisible.value = false
  emit('update:modelValue', false)
  // 重置状态
  transportOptions.value = []
  selectedIndex.value = -1
  hasQueried.value = false
  showOriginSuggestions.value = false
  showDestSuggestions.value = false
  queryForm.originId = null
  queryForm.destId = null
}
</script>

<style scoped>
.transport-query {
  min-height: 300px;
}

.query-form {
  margin-bottom: 20px;
}

.query-form .el-form-item {
  position: relative;
}

.suggestions {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  z-index: 1000;
  background: white;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  max-height: 200px;
  overflow-y: auto;
  margin-top: 4px;
}

.suggestion-item {
  padding: 8px 12px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.suggestion-item:hover {
  background-color: #f5f7fa;
}

.suggestion-section {
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 4px;
  margin-bottom: 4px;
}

.suggestion-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.suggestion-section-title {
  padding: 6px 12px;
  font-size: 12px;
  color: #909399;
  font-weight: 600;
  background-color: #f5f7fa;
}

.plan-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.spot-name {
  flex: 1;
}

.spot-day {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}

.transport-results {
  margin-top: 20px;
}

.transport-results h4 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 16px;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.transport-option {
  padding: 16px;
  border: 2px solid #dcdfe6;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background: #fff;
}

.transport-option:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.2);
}

.transport-option.is-selected {
  border-color: #409eff;
  background: #ecf5ff;
}

.option-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.option-label {
  font-weight: 600;
  color: #303133;
}

.option-content {
  margin-left: 0;
}

.route-detail {
  margin: 0 0 8px 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
}

.option-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #909399;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.empty-results {
  padding: 40px 0;
  text-align: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>

