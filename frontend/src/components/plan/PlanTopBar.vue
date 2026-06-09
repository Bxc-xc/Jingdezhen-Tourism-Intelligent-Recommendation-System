<template>
  <div class="plan-top-bar">
    <!-- 出发地 tag -->
    <div class="bar-item origin-item">
      <el-tag
        v-if="form.originCity"
        closable
        @close="clearOrigin"
        class="origin-tag"
      >
        {{ form.originCity }}
      </el-tag>
      <el-input
        v-else
        v-model="originInput"
        placeholder="请输入出发地"
        size="small"
        class="origin-input"
        @keyup.enter="confirmOrigin"
        @blur="confirmOrigin"
      />
    </div>

    <div class="bar-divider">→</div>

    <!-- 目的地 -->
    <div class="bar-item">
      <el-input
        v-model="form.destinationCity"
        placeholder="目的地 / 城市 / 地标"
        size="small"
        class="dest-input"
        @input="emitUpdate"
      />
    </div>

    <!-- 日期 -->
    <div class="bar-item">
      <el-date-picker
        v-model="form.startDate"
        type="date"
        placeholder="日期/时间"
        format="YYYY-MM-DD"
        value-format="YYYY-MM-DD"
        size="small"
        :disabled-date="disabledDate"
        class="date-picker"
        @change="emitUpdate"
      />
    </div>

    <!-- 旅行偏好 -->
    <div class="bar-item">
      <el-popover
        v-model:visible="popoverVisible"
        trigger="click"
        placement="bottom-start"
        :width="420"
        popper-class="preference-popper"
      >
        <template #reference>
          <el-button size="small" class="pref-btn">
            ♡ 旅行偏好
            <el-badge v-if="selectedCount > 0" :value="selectedCount" class="pref-badge" />
          </el-button>
        </template>

        <!-- PreferencePopover 内容 -->
        <div class="pref-popover">
          <!-- 同行伙伴 -->
          <div class="pref-section">
            <div class="pref-label">同行伙伴</div>
            <div class="pref-options">
              <span
                v-for="opt in companionOptions"
                :key="opt.value"
                :class="['pref-chip', { active: localPrefs.companionType === opt.value }]"
                @click="localPrefs.companionType = localPrefs.companionType === opt.value ? '' : opt.value"
              >{{ opt.icon }} {{ opt.label }}</span>
            </div>
          </div>

          <!-- 风格偏好 -->
          <div class="pref-section">
            <div class="pref-label">风格偏好</div>
            <div class="pref-options">
              <span
                v-for="opt in styleOptions"
                :key="opt.value"
                :class="['pref-chip', { active: localPrefs.stylePreference.includes(opt.value) }]"
                @click="toggleStyle(opt.value)"
              >{{ opt.icon }} {{ opt.label }}</span>
            </div>
          </div>

          <!-- 行程节奏 -->
          <div class="pref-section">
            <div class="pref-label">行程节奏</div>
            <div class="pref-options">
              <span
                v-for="opt in paceOptions"
                :key="opt.value"
                :class="['pref-chip', { active: localPrefs.pacePreference === opt.value }]"
                @click="localPrefs.pacePreference = localPrefs.pacePreference === opt.value ? '' : opt.value"
              >{{ opt.icon }} {{ opt.label }}</span>
            </div>
          </div>

          <!-- 住宿偏好 -->
          <div class="pref-section">
            <div class="pref-label">住宿偏好</div>
            <div class="pref-options">
              <span
                v-for="opt in accommodationOptions"
                :key="opt.value"
                :class="['pref-chip', { active: localPrefs.accommodationPreference === opt.value }]"
                @click="localPrefs.accommodationPreference = localPrefs.accommodationPreference === opt.value ? '' : opt.value"
              >{{ opt.label }}</span>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="pref-actions">
            <el-button size="small" @click="resetPrefs">重置</el-button>
            <el-button size="small" type="primary" @click="confirmPrefs">确定</el-button>
          </div>
        </div>
      </el-popover>
    </div>

    <div class="bar-spacer" />

    <!-- 操作按钮 -->
    <el-button size="small" round @click="$emit('create')">手动创建线路</el-button>
    <el-button size="small" type="primary" round @click="$emit('generate')">
      ✦ AI规划旅程
    </el-button>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'generate', 'create'])

// 本地表单副本，避免直接修改 props
const form = reactive({ ...props.modelValue })
const originInput = ref('')
const popoverVisible = ref(false)

// 偏好筛选本地状态（点确定才同步）
const localPrefs = reactive({
  companionType: props.modelValue.companionType || '',
  stylePreference: [...(props.modelValue.stylePreference || [])],
  pacePreference: props.modelValue.pacePreference || '',
  accommodationPreference: props.modelValue.accommodationPreference || ''
})

// 已选偏好数量
const selectedCount = computed(() => {
  return [localPrefs.companionType, localPrefs.pacePreference, localPrefs.accommodationPreference]
    .filter(v => v !== '').length + localPrefs.stylePreference.length
})

// 选项配置
const companionOptions = [
  { value: '独自出行', label: '独自出行', icon: '🚶' },
  { value: '家庭出行', label: '家庭出行', icon: '👨‍👩‍👧‍👦' },
  { value: '情侣出行', label: '情侣出行', icon: '💑' },
  { value: '朋友出行', label: '朋友出行', icon: '👥' },
  { value: '老人同行', label: '老人同行', icon: '👴' }
]
const styleOptions = [
  { value: '文化体验', label: '文化体验', icon: '🎨' },
  { value: '经典必去', label: '经典必去', icon: '⭐' },
  { value: '自然风光', label: '自然风光', icon: '🌿' },
  { value: '城市景观', label: '城市景观', icon: '🏙️' },
  { value: '历史古迹', label: '历史古迹', icon: '🏛️' }
]
const paceOptions = [
  { value: '紧凑', label: '紧凑', icon: '🏃' },
  { value: '适中', label: '适中', icon: '🚶' },
  { value: '宽松', label: '宽松', icon: '🧘' }
]
const accommodationOptions = [
  { value: '舒适型', label: '舒适型' },
  { value: '高档型', label: '高档型' },
  { value: '豪华型', label: '豪华型' }
]

const disabledDate = (time) => time.getTime() < Date.now() - 8.64e7

const emitUpdate = () => emit('update:modelValue', { ...form, ...localPrefs })

const clearOrigin = () => {
  form.originCity = ''
  emitUpdate()
}

const confirmOrigin = () => {
  if (originInput.value.trim()) {
    form.originCity = originInput.value.trim()
    originInput.value = ''
    emitUpdate()
  }
}

const toggleStyle = (val) => {
  const idx = localPrefs.stylePreference.indexOf(val)
  if (idx > -1) localPrefs.stylePreference.splice(idx, 1)
  else localPrefs.stylePreference.push(val)
}

const resetPrefs = () => {
  localPrefs.companionType = ''
  localPrefs.stylePreference = []
  localPrefs.pacePreference = ''
  localPrefs.accommodationPreference = ''
}

const confirmPrefs = () => {
  popoverVisible.value = false
  emitUpdate()
}

// 同步外部 modelValue 变化
watch(() => props.modelValue, (val) => {
  Object.assign(form, val)
  localPrefs.companionType = val.companionType || ''
  localPrefs.stylePreference = [...(val.stylePreference || [])]
  localPrefs.pacePreference = val.pacePreference || ''
  localPrefs.accommodationPreference = val.accommodationPreference || ''
}, { deep: true })
</script>

<style scoped>
.plan-top-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 56px;
  padding: 0 16px;
  background: rgba(255, 255, 255, 0.97);
  border-bottom: 1px solid #e8e8e8;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  flex-shrink: 0;
}

.bar-item {
  display: flex;
  align-items: center;
}

.bar-divider {
  color: #999;
  font-size: 14px;
  padding: 0 2px;
}

.bar-spacer {
  flex: 1;
}

.origin-tag {
  font-size: 13px;
  height: 28px;
}

.origin-input,
.dest-input {
  width: 140px;
}

.date-picker {
  width: 150px;
}

.pref-btn {
  position: relative;
  color: #555;
}

.pref-badge {
  position: absolute;
  top: -6px;
  right: -6px;
}

.pref-popover {
  padding: 4px 0;
}

.pref-section {
  margin-bottom: 14px;
}

.pref-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}

.pref-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.pref-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 16px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
}

.pref-chip:hover {
  border-color: #667eea;
  color: #667eea;
}

.pref-chip.active {
  background: #667eea;
  border-color: #667eea;
  color: white;
}

.pref-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
  margin-top: 4px;
}
</style>
