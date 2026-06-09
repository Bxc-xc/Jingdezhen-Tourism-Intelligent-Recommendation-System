<template>
  <div class="activity-manage">
    <div class="main-content">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="header-left">
          <el-button @click="goBack" :icon="ArrowLeft" circle />
          <div class="title-section">
            <h2>活动管理</h2>
            <span class="subtitle">管理您的营销活动，吸引更多顾客</span>
          </div>
        </div>
        <el-button type="primary" @click="openPublishDialog">
          <el-icon><Plus /></el-icon>
          发布活动
        </el-button>
      </div>

      <!-- 筛选和搜索栏 -->
      <el-card class="filter-card">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索活动标题或描述"
              clearable
              @input="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="6">
            <el-select
              v-model="statusFilter"
              placeholder="筛选状态"
              clearable
              @change="handleFilter"
            >
              <el-option label="全部" value="" />
              <el-option label="进行中" value="active" />
              <el-option label="未开始" value="pending" />
              <el-option label="已结束" value="ended" />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-select
              v-model="typeFilter"
              placeholder="筛选类型"
              clearable
              @change="handleFilter"
            >
              <el-option label="全部类型" value="" />
              <el-option label="限时折扣" value="discount" />
              <el-option label="满减优惠" value="full_reduction" />
              <el-option label="节日特惠" value="festival" />
              <el-option label="新品推广" value="new_arrival" />
              <el-option label="优惠活动" value="discount" />
              <el-option label="体验活动" value="experience" />
              <el-option label="展览活动" value="exhibition" />
              <el-option label="其他" value="other" />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-button @click="resetFilters">重置</el-button>
          </el-col>
        </el-row>
      </el-card>

      <!-- 活动列表 -->
      <el-card class="activities-card">
        <template #header>
          <div class="card-header">
            <span>活动列表 ({{ filteredActivities.length }})</span>
            <div class="view-toggle">
              <el-button-group>
                <el-button
                  :type="viewMode === 'table' ? 'primary' : ''"
                  @click="viewMode = 'table'"
                  :icon="List"
                >
                  列表
                </el-button>
                <el-button
                  :type="viewMode === 'card' ? 'primary' : ''"
                  @click="viewMode = 'card'"
                  :icon="Grid"
                >
                  卡片
                </el-button>
              </el-button-group>
            </div>
          </div>
        </template>

        <!-- 表格视图 -->
        <el-table
          v-if="viewMode === 'table'"
          :data="filteredActivities"
          style="width: 100%"
          v-loading="loading"
        >
          <el-table-column prop="title" label="活动标题" min-width="150" />
          <el-table-column prop="type" label="活动类型" width="120">
            <template #default="scope">
              <el-tag :type="getTypeTagType(scope.row.type)">
                {{ getTypeLabel(scope.row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="活动描述" min-width="200" show-overflow-tooltip />
          <el-table-column prop="startTime" label="开始时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.startTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="endTime" label="结束时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.endTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusTagType(scope.row.status)">
                {{ getStatusLabel(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="scope">
              <el-button size="small" @click="viewActivity(scope.row)">查看</el-button>
              <el-button size="small" @click="editActivity(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 卡片视图 -->
        <div v-else class="card-view">
          <el-row :gutter="20" v-loading="loading">
            <el-col
              v-for="activity in filteredActivities"
              :key="activity.id"
              :span="8"
              :xs="24"
              :sm="12"
              :md="8"
            >
              <el-card class="activity-card" shadow="hover">
                <template #header>
                  <div class="card-header-small">
                    <span class="activity-title">{{ activity.title }}</span>
                    <el-tag :type="getStatusTagType(activity.status)" size="small">
                      {{ getStatusLabel(activity.status) }}
                    </el-tag>
                  </div>
                </template>
                <div class="activity-content">
                  <div class="activity-type">
                    <el-tag :type="getTypeTagType(activity.type)" size="small">
                      {{ getTypeLabel(activity.type) }}
                    </el-tag>
                  </div>
                  <p class="activity-description">{{ activity.description }}</p>
                  <div class="activity-time">
                    <el-icon><Clock /></el-icon>
                    <span>{{ formatDateTime(activity.startTime) }} - {{ formatDateTime(activity.endTime) }}</span>
                  </div>
                  <div class="activity-actions">
                    <el-button size="small" @click="viewActivity(activity)">查看</el-button>
                    <el-button size="small" @click="editActivity(activity)">编辑</el-button>
                    <el-button size="small" type="danger" @click="handleDelete(activity.id)">删除</el-button>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
          <el-empty v-if="!loading && filteredActivities.length === 0" description="暂无活动" />
        </div>
      </el-card>
    </div>

    <!-- 发布/编辑活动对话框 -->
    <el-dialog
      v-model="showActivityDialog"
      :title="isEditMode ? '编辑活动' : '发布活动'"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form
        :model="activityForm"
        :rules="activityRules"
        ref="activityFormRef"
        label-width="120px"
      >
        <el-form-item label="活动标题" prop="title">
          <el-input
            v-model="activityForm.title"
            placeholder="请输入引人注目的活动标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="活动类型" prop="type">
          <el-select
            v-model="activityForm.type"
            placeholder="请选择活动类型"
            style="width: 100%"
          >
            <el-option label="限时折扣" value="discount" />
            <el-option label="满减优惠" value="full_reduction" />
            <el-option label="节日特惠" value="festival" />
            <el-option label="新品推广" value="new_arrival" />
            <el-option label="体验活动" value="experience" />
            <el-option label="展览活动" value="exhibition" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>

        <el-form-item label="活动时间" prop="timeRange">
          <el-date-picker
            v-model="activityForm.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm"
            style="width: 100%"
            :popper-options="{
              strategy: 'fixed',
              modifiers: [
                { name: 'offset', options: { offset: [0, 4] } },
                { name: 'computeStyles', options: { adaptive: true, gpuAcceleration: true } }
              ]
            }"
          />
        </el-form-item>

        <el-form-item label="活动封面">
          <el-upload
            class="avatar-uploader"
            :http-request="customImageUpload"
            :show-file-list="false"
            :limit="1"
            :on-exceed="() => ElMessage.warning('仅支持1张封面')"
            :on-success="handleImageSuccess"
            :on-error="handleImageError"
            :before-upload="beforeImageUpload"
            accept="image/*"
          >
            <img v-if="activityForm.image" :src="activityForm.image" class="avatar" @error="onCoverError" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">建议尺寸: 800x450px，支持 jpg/png 格式</div>
        </el-form-item>

        <el-form-item label="活动描述" prop="description">
          <el-input
            v-model="activityForm.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述活动内容、规则及优惠详情..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item
          label="优惠力度"
          prop="discount"
          v-if="activityForm.type === 'discount'"
        >
          <el-input-number
            v-model="activityForm.discount"
            :min="1"
            :max="9.9"
            :step="0.1"
            :precision="1"
          />
          <span class="suffix-text">折</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showActivityDialog = false">取消</el-button>
        <el-button type="primary" @click="saveActivity" :loading="saving">
          {{ isEditMode ? '保存' : '发布' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 活动详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="活动详情" width="600px">
      <div v-if="selectedActivity" class="activity-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="活动标题">
            {{ selectedActivity.title }}
          </el-descriptions-item>
          <el-descriptions-item label="活动类型">
            <el-tag :type="getTypeTagType(selectedActivity.type)">
              {{ getTypeLabel(selectedActivity.type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="活动状态">
            <el-tag :type="getStatusTagType(selectedActivity.status)">
              {{ getStatusLabel(selectedActivity.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="开始时间">
            {{ formatDateTime(selectedActivity.startTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="结束时间">
            {{ formatDateTime(selectedActivity.endTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="活动描述">
            {{ selectedActivity.description }}
          </el-descriptions-item>
          <el-descriptions-item label="优惠力度" v-if="selectedActivity.discount">
            {{ selectedActivity.discount }}折
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  Plus,
  Search,
  List,
  Grid,
  Clock
} from '@element-plus/icons-vue'
import { getMerchantByUserId } from '../../api/merchant'
import {
  getMerchantActivities,
  createMerchantActivity,
  updateMerchantActivity,
  deleteMerchantActivity
} from '../../api/merchantActivity'
import request from '../../utils/request'
import { normalizeImageUrl } from '../../utils/image'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const merchantInfo = ref(null)
const activities = ref([])
const searchKeyword = ref('')
const statusFilter = ref('')
const typeFilter = ref('')
const viewMode = ref('table')
const showActivityDialog = ref(false)
const showDetailDialog = ref(false)
const isEditMode = ref(false)
const selectedActivity = ref(null)
const activityFormRef = ref(null)

const activityForm = reactive({
  id: null,
  title: '',
  type: '',
  timeRange: [],
  description: '',
  image: '',
  discount: 9.0
})

const activityRules = {
  title: [
    { required: true, message: '请输入活动标题', trigger: 'blur' },
    { min: 3, max: 100, message: '长度在 3 到 100 个字符', trigger: 'blur' }
  ],
  type: [{ required: true, message: '请选择活动类型', trigger: 'change' }],
  timeRange: [{ required: true, message: '请选择活动时间', trigger: 'change' }],
  description: [
    { required: true, message: '请输入活动描述', trigger: 'blur' },
    { min: 10, max: 500, message: '长度在 10 到 500 个字符', trigger: 'blur' }
  ]
}

// 筛选后的活动列表
const filteredActivities = computed(() => {
  let result = activities.value

  // 状态筛选
  if (statusFilter.value) {
    result = result.filter(a => a.status === statusFilter.value)
  }

  // 类型筛选
  if (typeFilter.value) {
    result = result.filter(a => a.type === typeFilter.value)
  }

  // 搜索关键词
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(
      a =>
        a.title.toLowerCase().includes(keyword) ||
        (a.description && a.description.toLowerCase().includes(keyword))
    )
  }

  return result
})

const goBack = () => {
  router.go(-1)
}

const handleSearch = () => {
  // 搜索逻辑已在 computed 中处理
}

const handleFilter = () => {
  // 筛选逻辑已在 computed 中处理
}

const resetFilters = () => {
  searchKeyword.value = ''
  statusFilter.value = ''
  typeFilter.value = ''
}

const openPublishDialog = () => {
  isEditMode.value = false
  resetActivityForm()
  showActivityDialog.value = true
}

const resetActivityForm = () => {
  Object.assign(activityForm, {
    id: null,
    title: '',
    type: '',
    timeRange: [],
    description: '',
    image: '',
    discount: 9.0
  })
  if (activityFormRef.value) {
    activityFormRef.value.clearValidate()
  }
}

const editActivity = (activity) => {
  isEditMode.value = true
  Object.assign(activityForm, {
    id: activity.id,
    title: activity.title,
    type: activity.type,
    timeRange: [
      formatDateTimeForPicker(activity.startTime),
      formatDateTimeForPicker(activity.endTime)
    ],
    description: activity.description,
    image: normalizeImageUrl(activity.image || ''),
    discount: activity.discount || 9.0
  })
  showActivityDialog.value = true
}

const viewActivity = (activity) => {
  selectedActivity.value = activity
  showDetailDialog.value = true
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个活动吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    let success = false
    try {
      await deleteMerchantActivity(id)
      success = true
    } catch (error) {
      console.warn('API delete failed:', error)
      // 尝试从本地存储删除
      try {
        const storedActivities = JSON.parse(
          localStorage.getItem('merchant_activities') || '[]'
        )
        const newStored = storedActivities.filter(a => a.id !== id)
        localStorage.setItem('merchant_activities', JSON.stringify(newStored))
        success = true
      } catch (e) {
        console.error('Local storage delete failed:', e)
      }
    }

    if (success) {
      activities.value = activities.value.filter(a => a.id !== id)
      ElMessage.success('活动删除成功')
    } else {
      ElMessage.error('删除失败')
    }
  } catch (error) {
    // 用户取消
  }
}

const saveActivity = async () => {
  if (!activityFormRef.value) return

  await activityFormRef.value.validate(async (valid) => {
    if (!valid) return

    if (!merchantInfo.value?.id) {
      ElMessage.warning('请先完善并保存店铺信息')
      return
    }

    saving.value = true
    try {
      const payload = {
        title: activityForm.title,
        description: activityForm.description,
        startTime: activityForm.timeRange[0],
        endTime: activityForm.timeRange[1],
        type: activityForm.type || 'other',
        image: activityForm.image || null,
        discount: activityForm.type === 'discount' ? activityForm.discount : null
      }

      if (isEditMode.value) {
        // 更新活动
        await updateMerchantActivity(activityForm.id, payload)
        ElMessage.success('活动更新成功')
      } else {
        // 创建活动
        await createMerchantActivity(merchantInfo.value.id, payload)
        ElMessage.success('活动发布成功')
      }

      showActivityDialog.value = false
      await loadActivities()
      resetActivityForm()
    } catch (error) {
      console.error('保存活动失败:', error)
      ElMessage.error(error?.response?.data?.message || '保存失败，请重试')
    } finally {
      saving.value = false
    }
  })
}

const loadActivities = async () => {
  if (!merchantInfo.value?.id) return

  loading.value = true
  let apiActivities = []
  try {
    const res = await getMerchantActivities(merchantInfo.value.id)
    if (res && res.success) {
      const data = res.data || []
      apiActivities = Array.isArray(data) ? data : []
    } else if (Array.isArray(res)) {
      apiActivities = res
    }
  } catch (error) {
    console.error('加载活动失败:', error)
  }

  // 合并本地存储的活动（Fallback模式）
  try {
    const storedActivities = JSON.parse(
      localStorage.getItem('merchant_activities') || '[]'
    )
    const localActivities = storedActivities.filter(
      a => String(a.merchantId) === String(merchantInfo.value.id)
    )

    // 简单的去重合并
    const existingIds = new Set(apiActivities.map(a => a.id))
    const uniqueLocal = localActivities.filter(a => !existingIds.has(a.id))

    activities.value = [...apiActivities, ...uniqueLocal]
    // 更新活动状态
    activities.value.forEach(updateActivityStatus)
  } catch (e) {
    console.warn('Load local activities failed:', e)
    activities.value = apiActivities
  } finally {
    loading.value = false
  }
}

const updateActivityStatus = (activity) => {
  const now = new Date()
  const startTime = new Date(activity.startTime)
  const endTime = new Date(activity.endTime)

  if (now < startTime) {
    activity.status = 'pending'
  } else if (now > endTime) {
    activity.status = 'ended'
  } else {
    activity.status = 'active'
  }
}

const loadMerchantInfo = async () => {
  try {
    if (!userStore.user?.id) return
    const response = await getMerchantByUserId(userStore.user.id)
    if (response.success && response.data) {
      merchantInfo.value = response.data
    } else {
      const raw = response?.data?.data || response?.data || response?.merchant || null
      if (raw) {
        merchantInfo.value = raw
      }
    }
  } catch (e) {
    console.error('加载商家信息失败:', e)
  }
}

// 图片上传相关
const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB！')
    return false
  }
  return true
}

const customImageUpload = async (options) => {
  const { file, onSuccess, onError } = options
  try {
    const formData = new FormData()
    formData.append('file', file.raw || file)

    const response = await request.post('/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    const url = normalizeImageUrl(response.url || response.data?.url)
    if (response.success && url) {
      activityForm.image = url
      onSuccess({ url })
      ElMessage.success('图片上传成功')
    } else {
      throw new Error(response.message || '上传失败')
    }
  } catch (error) {
    console.error('图片上传失败:', error)
    ElMessage.error('图片上传失败，请重试')
    onError(error)
  }
}

const handleImageSuccess = () => {
  // 已在 customImageUpload 中处理
}

const handleImageError = () => {
  ElMessage.error('图片上传失败')
}

const onCoverError = (e) => {
  e.target.src = 'https://via.placeholder.com/356x200?text=Image+Error'
}

// 工具函数
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatDateTimeForPicker = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}`
}

const getStatusLabel = (status) => {
  const map = {
    active: '进行中',
    pending: '未开始',
    ended: '已结束'
  }
  return map[status] || status
}

const getStatusTagType = (status) => {
  const map = {
    active: 'success',
    pending: 'warning',
    ended: 'info'
  }
  return map[status] || 'info'
}

const getTypeLabel = (type) => {
  const map = {
    discount: '限时折扣',
    full_reduction: '满减优惠',
    festival: '节日特惠',
    new_arrival: '新品推广',
    experience: '体验活动',
    exhibition: '展览活动',
    other: '其他'
  }
  return map[type] || type || '其他'
}

const getTypeTagType = (type) => {
  const map = {
    discount: 'danger',
    full_reduction: 'warning',
    festival: 'success',
    new_arrival: 'primary',
    experience: 'info',
    exhibition: '',
    other: 'info'
  }
  return map[type] || 'info'
}

onMounted(async () => {
  await loadMerchantInfo()
  await loadActivities()
  // 定时更新活动状态（每分钟）
  setInterval(() => {
    activities.value.forEach(updateActivityStatus)
  }, 60000)
})
</script>

<style scoped>
.activity-manage {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.title-section h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
  font-weight: 600;
}

.subtitle {
  color: #909399;
  font-size: 14px;
  margin-top: 4px;
  display: block;
}

.filter-card {
  margin-bottom: 20px;
}

.activities-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.view-toggle {
  display: flex;
  align-items: center;
}

.card-view {
  min-height: 200px;
}

.activity-card {
  margin-bottom: 20px;
  transition: all 0.3s;
}

.activity-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.card-header-small {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.activity-title {
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}

.activity-content {
  padding: 0;
}

.activity-type {
  margin-bottom: 12px;
}

.activity-description {
  color: #606266;
  line-height: 1.6;
  margin: 12px 0;
  min-height: 48px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.activity-time {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
  font-size: 14px;
  margin-bottom: 16px;
}

.activity-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.avatar-uploader .avatar {
  width: 178px;
  height: 100px;
  display: block;
  object-fit: cover;
  border-radius: 6px;
}

.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 100px;
  text-align: center;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.suffix-text {
  margin-left: 8px;
  color: #606266;
}

.activity-detail {
  padding: 10px 0;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .filter-card .el-row {
    flex-direction: column;
  }

  .filter-card .el-col {
    width: 100% !important;
    margin-bottom: 12px;
  }
}
</style>

