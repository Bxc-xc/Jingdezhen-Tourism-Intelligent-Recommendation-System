<template>
  <div v-if="modelValue">
    <!-- 透明遮罩，点击关闭 -->
    <div class="popup-overlay" v-if="!inline" @click="$emit('update:modelValue', false)"></div>
    
    <!-- 弹窗内容 -->
    <div 
      class="popup-content"
      :class="{ 'is-inline': inline }"
      :style="popupStyle"
      @click.stop
    >
      <div class="popup-header">
        <span class="popup-title">添加到行程</span>
        <el-icon class="close-icon" @click="$emit('update:modelValue', false)"><Close /></el-icon>
      </div>
      
      <div class="popup-body" v-loading="loading">
        <div v-if="plans.length === 0 && !loading" class="no-plans">
          <el-empty description="您还没有创建行程" :image-size="80">
            <el-button type="primary" size="small" @click="goToCreatePlan">去创建行程</el-button>
          </el-empty>
        </div>

        <el-form v-else :model="form" label-width="70px" size="small">
          <el-form-item label="选择行程">
            <el-select 
              v-model="form.planId" 
              placeholder="请选择行程" 
              style="width: 100%" 
              @change="handlePlanChange"
              :teleported="false"
            >
              <el-option
                v-for="plan in plans"
                :key="plan.id"
                :label="plan.title || `${plan.days}天行程 (${plan.startDate})`"
                :value="plan.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="选择天数" v-if="selectedPlan">
            <el-select 
              v-model="form.dayIndex" 
              placeholder="请选择第几天" 
              style="width: 100%"
              :teleported="false"
            >
              <el-option
                v-for="(day, index) in planDays"
                :key="index"
                :label="`第${day.day}天 ${day.date ? '(' + day.date + ')' : ''}`"
                :value="index"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="活动时间">
            <el-time-select
              v-model="form.time"
              start="06:00"
              step="00:30"
              end="23:00"
              placeholder="选择时间"
              style="width: 100%"
              :teleported="false"
            />
          </el-form-item>

          <el-form-item label="备注信息">
            <el-input v-model="form.description" type="textarea" :rows="2" placeholder="可选填备注信息" />
          </el-form-item>
        </el-form>
      </div>

      <div class="popup-footer">
        <el-button size="small" @click="$emit('update:modelValue', false)">取消</el-button>
        <el-button type="primary" size="small" @click="handleConfirm" :disabled="!form.planId || plans.length === 0" :loading="submitting">
          确定
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { getUserPlans, updatePlan } from '../api/plan'
import { ElMessage } from 'element-plus'
import { Close } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    required: true
  },
  item: {
    type: Object,
    required: true,
  },
  // 点击位置坐标 {x, y}
  clickPosition: {
    type: Object,
    default: () => null
  },
  // 是否内联显示（覆盖在父容器上）
  inline: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'success'])
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const plans = ref([])
const selectedPlan = ref(null)

// 计算弹窗位置
const popupStyle = computed(() => {
  if (props.inline) {
    return {
      position: 'absolute',
      top: 0,
      left: 0,
      width: '100%',
      height: '100%',
      margin: 0,
      transform: 'none',
      borderRadius: 'inherit' // 继承父容器圆角
    }
  }

  // 始终保持在屏幕中央
  return {
    top: '50%',
    left: '50%',
    transform: 'translate(-50%, -50%)',
    position: 'fixed',
    margin: '0'
  }
})

const form = reactive({
  planId: null,
  dayIndex: 0,
  time: '09:00',
  description: ''
})

const planDays = computed(() => {
  if (!selectedPlan.value) return []
  try {
    const details = typeof selectedPlan.value.planDetails === 'string'
      ? JSON.parse(selectedPlan.value.planDetails)
      : selectedPlan.value.planDetails
    
    // If details is empty or doesn't have days, construct basic structure based on plan.days
    if (!details || !details.days || details.days.length === 0) {
      const days = []
      const startDate = new Date(selectedPlan.value.startDate)
      for (let i = 1; i <= selectedPlan.value.days; i++) {
        const date = new Date(startDate)
        date.setDate(startDate.getDate() + (i - 1))
        days.push({
          day: i,
          date: date.toLocaleDateString('zh-CN'),
          schedule: []
        })
      }
      return days
    }
    
    return details.days
  } catch (e) {
    console.error('Parse plan details error', e)
    return []
  }
})

watch(() => props.modelValue, (val) => {
  if (val) {
    loadPlans()
    // Reset form but keep previous selections if valid? No, reset is safer.
    // Actually keeping planId is nice if user adds multiple items.
    if (!form.planId) {
      form.dayIndex = 0
      form.time = '09:00'
    }
    form.description = ''
  }
})

const loadPlans = async () => {
  if (!userStore.isLoggedIn) return
  loading.value = true
  try {
    const res = await getUserPlans(userStore.user.id)
    plans.value = Array.isArray(res.data) ? res.data : (Array.isArray(res) ? res : [])
    
    // Auto select first plan if none selected
    if (plans.value.length > 0 && !form.planId) {
      form.planId = plans.value[0].id
      handlePlanChange(form.planId)
    }
  } catch (error) {
    console.error('Failed to load plans', error)
    ElMessage.error('获取行程列表失败')
  } finally {
    loading.value = false
  }
}

const handlePlanChange = (planId) => {
  selectedPlan.value = plans.value.find(p => p.id === planId)
  form.dayIndex = 0
}

const goToCreatePlan = () => {
  emit('update:modelValue', false)
  router.push('/plan')
}

const handleConfirm = async () => {
  if (!selectedPlan.value) return
  
  submitting.value = true
  try {
    // Parse current details
    let details
    try {
      details = typeof selectedPlan.value.planDetails === 'string'
        ? JSON.parse(selectedPlan.value.planDetails)
        : selectedPlan.value.planDetails
    } catch (e) {
      details = { days: [] }
    }

    // Ensure days array exists and matches plan duration
    if (!details.days || details.days.length === 0) {
      details.days = []
      const startDate = new Date(selectedPlan.value.startDate)
      for (let i = 1; i <= selectedPlan.value.days; i++) {
        const date = new Date(startDate)
        date.setDate(startDate.getDate() + (i - 1))
        details.days.push({
          day: i,
          date: date.toLocaleDateString('zh-CN'),
          schedule: []
        })
      }
    }

    // Add item to selected day
    const day = details.days[form.dayIndex]
    if (!day.schedule) day.schedule = []

    const newItem = {
      id: Date.now(),
      time: form.time,
      title: props.item.title,
      description: form.description || props.item.description || '',
      tags: [getCategoryLabel(props.item.type)],
      originalId: props.item.id,
      // 行程内部类型：默认视为景点/活动，后续可在编辑器中切换为交通段
      type: 'SCENIC',
      playMinutes: 120,
      costEstimate: null
    }

    day.schedule.push(newItem)
    
    // Sort schedule by time
    day.schedule.sort((a, b) => {
      return a.time.localeCompare(b.time)
    })

    // Update plan
    await updatePlan(selectedPlan.value.id, {
      ...selectedPlan.value,
      planDetails: JSON.stringify(details)
    })

    ElMessage.success('已成功加入行程')
    emit('success')
    emit('update:modelValue', false)
    
  } catch (error) {
    console.error('Failed to update plan', error)
    ElMessage.error('加入行程失败')
  } finally {
    submitting.value = false
  }
}

const getCategoryLabel = (type) => {
  const map = {
    scenic: '景点',
    hotel: '酒店',
    food: '美食',
    route: '路线'
  }
  return map[type] || '活动'
}
</script>

<style scoped>
.no-plans {
  padding: 20px 0;
  text-align: center;
}
.popup-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 2000;
  background: transparent;
}
.popup-content {
  position: fixed;
  z-index: 2001;
  background: white;
  width: 360px;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  border: 1px solid #ebeef5;
  animation: popup-fade-in 0.2s ease-out;
}
.popup-content.is-inline {
  width: auto;
  box-shadow: none;
  border: none;
  z-index: 10;
}
.popup-header {
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f5f7fa;
  border-radius: 8px 8px 0 0;
}
.popup-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
.close-icon {
  cursor: pointer;
  color: #909399;
  font-size: 16px;
}
.close-icon:hover {
  color: #409eff;
}
.popup-body {
  padding: 16px;
  max-height: 400px;
  overflow-y: auto;
}
.popup-footer {
  padding: 10px 16px;
  border-top: 1px solid #ebeef5;
  text-align: right;
  background: #fff;
  border-radius: 0 0 8px 8px;
}
@keyframes popup-fade-in {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}
</style>
