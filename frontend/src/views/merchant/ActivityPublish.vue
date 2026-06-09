<template>
  <div class="activity-publish-page">
    <div class="page-header">
      <div class="header-left">
        <el-button @click="$router.back()" circle class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <div class="title-section">
          <h2>发布营销活动</h2>
          <span class="subtitle">创建新的促销活动以吸引更多顾客</span>
        </div>
      </div>
    </div>

    <div class="content-wrapper">
      <el-card class="publish-card">
        <el-form :model="form" label-width="120px" :rules="rules" ref="formRef">
          <el-form-item label="活动标题" prop="title">
            <el-input v-model="form.title" placeholder="请输入引人注目的活动标题" />
          </el-form-item>

          <el-form-item label="活动类型" prop="type">
            <el-select v-model="form.type" placeholder="请选择活动类型" style="width: 100%">
              <el-option label="限时折扣" value="discount" />
              <el-option label="满减优惠" value="full_reduction" />
              <el-option label="节日特惠" value="festival" />
              <el-option label="新品推广" value="new_arrival" />
            </el-select>
          </el-form-item>

          <el-form-item label="活动时间" prop="dateRange">
            <el-date-picker
              v-model="form.dateRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="活动封面" prop="image">
            <el-upload
              class="avatar-uploader"
              action="#"
              :show-file-list="false"
              :auto-upload="false"
              :on-change="handleImageChange"
            >
              <img v-if="imageUrl" :src="imageUrl" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
            <div class="upload-tip">建议尺寸: 800x450px，支持 jpg/png 格式</div>
          </el-form-item>

          <el-form-item label="活动描述" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="4"
              placeholder="请详细描述活动内容、规则及优惠详情..."
            />
          </el-form-item>

          <el-form-item label="优惠力度" prop="discount" v-if="form.type === 'discount'">
            <el-input-number v-model="form.discount" :min="1" :max="9.9" :step="0.1" label="折扣" />
            <span class="suffix-text">折</span>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="submitForm(formRef)" :loading="loading">立即发布</el-button>
            <el-button @click="$router.back()">取消</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user' // Import user store
import { ElMessage } from 'element-plus'
import { ArrowLeft, Plus } from '@element-plus/icons-vue'
import request from '../../utils/request'

const router = useRouter()
const userStore = useUserStore() // Use user store
const formRef = ref(null)
const loading = ref(false)
const imageUrl = ref('')

const form = reactive({
  title: '',
  type: '',
  dateRange: [],
  description: '',
  discount: 9.0,
  image: null
})

const rules = {
  title: [
    { required: true, message: '请输入活动标题', trigger: 'blur' },
    { min: 3, max: 50, message: '长度在 3 到 50 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择活动类型', trigger: 'change' }
  ],
  dateRange: [
    { required: true, message: '请选择活动时间', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入活动描述', trigger: 'blur' }
  ]
}

const handleImageChange = async (file) => {
  imageUrl.value = URL.createObjectURL(file.raw)
  // 立即上传到服务器，获取真实 URL
  try {
    const formData = new FormData()
    formData.append('file', file.raw)
    const res = await request.post('/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    // 后端返回 { url: '...' } 或直接返回 url 字符串
    form.image = res?.url || res?.data?.url || res
  } catch (e) {
    ElMessage.error('图片上传失败，请重试')
    form.image = null
  }
}

const submitForm = async (formEl) => {
  if (!formEl) return
  
  await formEl.validate(async (valid, fields) => {
    if (valid) {
      loading.value = true
      
      try {
        // Prepare payload
        const payload = {
          title: form.title,
          type: form.type,
          description: form.description,
          startTime: form.dateRange[0],
          endTime: form.dateRange[1],
          discount: form.type === 'discount' ? form.discount : null,
          image: form.image // 使用上传后的服务器 URL
        }

        // Try API first
        if (userStore.user?.id) {
          try {
            await createMerchantActivity(userStore.user.id, payload)
            ElMessage.success('活动发布成功！')
            router.push('/merchant/shop')
            return
          } catch (apiError) {
            console.warn('API publish failed, falling back to local storage:', apiError)
          }
        }

        // Fallback to localStorage (Demo Mode)
        const newActivity = {
          id: Date.now(),
          merchantId: userStore.user?.id,
          title: form.title,
          type: form.type,
          description: form.description,
          startTime: form.dateRange[0],
          endTime: form.dateRange[1],
          discount: form.type === 'discount' ? form.discount : null,
          image: imageUrl.value,
          status: 'active',
          createTime: new Date().toISOString()
        }

        const storedActivities = JSON.parse(localStorage.getItem('merchant_activities') || '[]')
        storedActivities.push(newActivity)
        localStorage.setItem('merchant_activities', JSON.stringify(storedActivities))

        loading.value = false
        ElMessage.success('活动发布成功！(本地模式)')
        router.push('/merchant/shop')
      } catch (error) {
        console.error('Error saving activity:', error)
        loading.value = false
        ElMessage.error('发布失败，请重试')
      }
    } else {
      console.log('error submit!', fields)
    }
  })
}
</script>

<style scoped>
.activity-publish-page {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.page-header {
  display: flex;
  align-items: center;
  padding: 20px 30px;
  background: white;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  border: none;
  background-color: #f5f7fa;
}

.back-btn:hover {
  background-color: #e6f7ff;
  color: #1890ff;
}

.title-section h2 {
  margin: 0;
  font-size: 20px;
  color: #1f1f1f;
  font-weight: 600;
  line-height: 1.4;
}

.subtitle {
  color: #8c8c8c;
  font-size: 13px;
  margin-top: 2px;
  display: block;
}

.content-wrapper {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 24px 24px;
}

.publish-card {
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.avatar-uploader .avatar {
  width: 178px;
  height: 178px;
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
  height: 178px;
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
</style>