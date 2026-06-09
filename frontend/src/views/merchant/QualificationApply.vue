<template>
  <div class="qualification-apply">
    <!-- 返回键（仅在独立页面时显示） -->
    <div class="back-button-container" v-if="showBackButton">
      <el-button @click="goBack" :icon="ArrowLeft" circle />
    </div>
    
    <div class="page-header">
      <div class="header-left">
      <h2>资质申请</h2>
        <p class="header-subtitle">提交您的商家资质材料，完成认证后即可使用商家功能</p>
      </div>
      <div class="header-actions">
        <el-button 
          @click="loadApplicationStatus" 
          :loading="loadingStatus"
          :icon="Refresh"
          circle
          title="刷新状态"
        />
        <el-tag :type="getStatusType(applicationStatus)" size="large" effect="dark">
          {{ getStatusText(applicationStatus) }}
        </el-tag>
      </div>
    </div>

    <!-- 申请状态说明 -->
    <el-card 
      class="status-card" 
      v-if="applicationStatus !== 'none'" 
      :class="{ 'status-updated': showStatusUpdateAnimation }"
      :key="`status-card-${applicationStatus}-${showStatusUpdateAnimation}`"
      shadow="hover"
    >
      <div class="status-content">
        <div class="status-icon-wrapper" :class="applicationStatus">
          <el-icon class="status-icon">
          <Check v-if="applicationStatus === 'approved'" />
          <Clock v-else-if="applicationStatus === 'pending'" />
          <Close v-else-if="applicationStatus === 'rejected'" />
        </el-icon>
        </div>
        <div class="status-details">
          <h3>{{ getStatusTitle(applicationStatus) }}</h3>
          <p>{{ getStatusDescription(applicationStatus) }}</p>
          <div v-if="auditOpinion && (applicationStatus === 'approved' || applicationStatus === 'rejected')" class="audit-opinion">
            <div class="audit-opinion-header">
              <el-icon><Document /></el-icon>
              <strong>审核意见</strong>
            </div>
            <p class="audit-opinion-text">{{ auditOpinion }}</p>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 审核通过成功提示对话框 -->
    <el-dialog
      v-model="showSuccessDialog"
      title="🎉 审核通过"
      width="500px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
      center
      :key="`success-dialog-${showSuccessDialog}`"
    >
      <div class="success-dialog-content">
        <div class="success-icon">
          <el-icon :size="80" color="#67c23a">
            <CircleCheck />
          </el-icon>
        </div>
        <h3>恭喜您！</h3>
        <p>您的商家资质申请已通过审核</p>
        <p class="success-message">现在您可以正常使用商家功能了</p>
        <div v-if="auditOpinion" class="audit-opinion-box">
          <strong>审核意见：</strong>
          <p>{{ auditOpinion }}</p>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="closeSuccessDialog" size="large">
          知道了
        </el-button>
      </template>
    </el-dialog>

    <!-- 申请表单 -->
    <el-card v-if="applicationStatus === 'none' || applicationStatus === 'rejected'" class="form-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><DocumentChecked /></el-icon>
        <span>商家资质申请</span>
        </div>
      </template>
      
      <el-form :model="applicationForm" :rules="rules" ref="formRef" label-width="140px" class="application-form">
        <el-form-item label="营业执照" prop="businessLicense">
          <div class="upload-wrapper">
          <el-upload
            class="upload-demo"
            :http-request="customUpload"
            :file-list="businessLicenseList"
            :on-success="handleBusinessLicenseSuccess"
            :on-error="handleUploadError"
            :on-remove="handleBusinessLicenseRemove"
            list-type="picture-card"
            :limit="1"
            :before-upload="beforeUpload"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
            <div class="upload-tip">
              <el-icon><InfoFilled /></el-icon>
              <span>请上传清晰的营业执照照片，支持JPG、PNG格式，大小不超过5MB</span>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="身份证" prop="identityCard">
          <div class="upload-wrapper">
          <el-upload
            class="upload-demo"
            :http-request="customUpload"
            :file-list="identityCardList"
            :on-success="handleIdentityCardSuccess"
            :on-error="handleUploadError"
            :on-remove="handleIdentityCardRemove"
            list-type="picture-card"
            :limit="2"
            :before-upload="beforeUpload"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
            <div class="upload-tip">
              <el-icon><InfoFilled /></el-icon>
              <span>请上传身份证正反面照片，确保信息清晰可见</span>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="店铺照片" prop="shopPhotos">
          <div class="upload-wrapper">
          <el-upload
            class="upload-demo"
            :http-request="customUpload"
            :file-list="shopPhotosList"
            :on-success="handleShopPhotosSuccess"
            :on-error="handleUploadError"
            :on-remove="handleShopPhotosRemove"
            list-type="picture-card"
            :limit="5"
            :before-upload="beforeUpload"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
            <div class="upload-tip">
              <el-icon><InfoFilled /></el-icon>
              <span>请上传店铺内外环境照片（最多5张），展示店铺真实环境</span>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="申请描述" prop="description">
          <el-input
            v-model="applicationForm.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述您的业务范围、服务特色等"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item>
          <div class="form-actions">
            <el-button type="primary" @click="submitApplication" :loading="submitting" size="large">
              <el-icon><Check /></el-icon>
            提交申请
          </el-button>
            <el-button @click="resetForm" size="large">
              <el-icon><RefreshLeft /></el-icon>
              重置
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 重新申请按钮 -->
    <div v-if="applicationStatus === 'rejected'" class="reapply-section">
      <el-button type="primary" @click="reapply">
        重新申请
      </el-button>
    </div>

    <!-- 展示申请资料（待审核或已通过状态） -->
    <el-card v-if="(applicationStatus === 'pending' || applicationStatus === 'approved') && applicationData" class="application-data-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><FolderOpened /></el-icon>
          <span>我的资质申请资料</span>
          <el-tag v-if="applicationStatus === 'pending'" type="warning" effect="dark" class="header-tag">
            审核中
          </el-tag>
          <el-tag v-else-if="applicationStatus === 'approved'" type="success" effect="dark" class="header-tag">
            已通过
          </el-tag>
        </div>
      </template>
      
      <el-descriptions :column="2" border class="application-info">
        <el-descriptions-item label="申请时间" :span="1">
          <div class="info-value">
            <el-icon><Calendar /></el-icon>
            <span>{{ formatDate(applicationData.createdAt) }}</span>
          </div>
        </el-descriptions-item>
        <el-descriptions-item v-if="applicationStatus === 'approved'" label="审核时间" :span="1">
          <div class="info-value">
            <el-icon><Clock /></el-icon>
            <span>{{ formatDate(applicationData.updatedAt) }}</span>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="申请状态" :span="applicationStatus === 'approved' ? 2 : 1">
          <el-tag :type="getStatusType(applicationStatus)" effect="dark" size="default">
            {{ getStatusText(applicationStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请描述" :span="2">
          <div class="description-text">{{ applicationData.description }}</div>
        </el-descriptions-item>
      </el-descriptions>

      <div class="application-images">
        <div class="image-section" v-if="applicationData.businessLicense">
          <h4>营业执照</h4>
          <div class="image-list">
            <el-image
              :src="applicationData.businessLicense"
              :preview-src-list="[applicationData.businessLicense]"
              fit="cover"
              class="application-image"
            />
          </div>
        </div>

        <div class="image-section" v-if="applicationData.identityCard">
          <h4>身份证</h4>
          <div class="image-list">
            <el-image
              v-for="(url, index) in identityCardUrls"
              :key="`identity-${index}-${url?.substring(0, 20)}`"
              :src="url"
              :preview-src-list="identityCardUrls"
              :initial-index="index"
              fit="cover"
              class="application-image"
              :lazy="true"
              @error="handleImageError($event, 'identityCard', index)"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                  <span>图片加载失败</span>
                </div>
              </template>
            </el-image>
          </div>
        </div>

        <div class="image-section" v-if="applicationData.shopPhotos">
          <h4>店铺照片</h4>
          <div class="image-list">
            <el-image
              v-for="(url, index) in shopPhotosUrls"
              :key="`shop-${index}-${url?.substring(0, 20)}`"
              :src="url"
              :preview-src-list="shopPhotosUrls"
              :initial-index="index"
              fit="cover"
              class="application-image"
              :lazy="true"
              @error="handleImageError($event, 'shopPhotos', index)"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                  <span>图片加载失败</span>
                </div>
              </template>
            </el-image>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, CircleCheck, Refresh, Picture, Document, DocumentChecked, InfoFilled, RefreshLeft, FolderOpened, Calendar } from '@element-plus/icons-vue'
import { merchantApplicationApi } from '../../api/merchantApplication'
import { useUserStore } from '../../stores/user'
import realtimeSync from '../../utils/websocket'

const router = useRouter()

// 检测是否在商家中心内部（通过路由路径判断）
const showBackButton = computed(() => {
  return router.currentRoute.value.path === '/merchant/qualification'
})

const formRef = ref()
const submitting = ref(false)
const applicationStatus = ref('none') // none, pending, approved, rejected
const auditOpinion = ref('')
const applicationData = ref(null) // 保存完整的申请数据
const showSuccessDialog = ref(false) // 显示成功对话框
const showStatusUpdateAnimation = ref(false) // 显示状态更新动画
const loadingStatus = ref(false) // 加载状态
const userStore = useUserStore()

const applicationForm = reactive({
  businessLicense: '',
  identityCard: '',
  shopPhotos: '',
  description: ''
})

const businessLicenseList = ref([])
const identityCardList = ref([])
const shopPhotosList = ref([])

const rules = {
  businessLicense: [
    { required: true, message: '请上传营业执照', trigger: 'change' }
  ],
  identityCard: [
    { required: true, message: '请上传身份证', trigger: 'change' }
  ],
  shopPhotos: [
    { required: true, message: '请上传店铺照片', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入申请描述', trigger: 'blur' },
    { min: 10, message: '描述至少10个字符', trigger: 'blur' }
  ]
}

const getStatusType = (status) => {
  const types = {
    none: 'info',
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    none: '未申请',
    pending: '审核中',
    approved: '已通过',
    rejected: '已拒绝'
  }
  return texts[status] || '未知'
}

const getStatusTitle = (status) => {
  const titles = {
    pending: '申请审核中',
    approved: '申请已通过',
    rejected: '申请被拒绝'
  }
  return titles[status] || ''
}

const getStatusDescription = (status) => {
  const descriptions = {
    pending: '您的资质申请正在审核中，请耐心等待。通常需要1-3个工作日。',
    approved: '恭喜！您的商家资质申请已通过审核，现在可以正常使用商家功能。',
    rejected: '很抱歉，您的申请未通过审核。请查看审核意见并重新提交申请。'
  }
  return descriptions[status] || ''
}

const beforeUpload = (file) => {
  return true
}

// 自定义上传方法
const customUpload = async (options) => {
  const { file, onSuccess, onError } = options
  
  try {
    const userStore = useUserStore()
    const formData = new FormData()
    formData.append('file', file.raw || file)
    
    const headers = {}
    if (userStore.token) {
      headers['Authorization'] = `Bearer ${userStore.token}`
    }
    
    const response = await fetch('/api/upload', {
      method: 'POST',
      headers,
      body: formData
    })
    
    if (response.ok) {
      const data = await response.json()
      if (data.success && data.url) {
        onSuccess(data)
      } else {
        ElMessage.error(data.message || '图片上传失败，请重试')
        onError(new Error(data.message || '上传失败'))
      }
    } else {
      const errText = await response.text().catch(() => '')
      ElMessage.error(`图片上传失败（${response.status}），请重试`)
      onError(new Error(`上传失败: ${response.status} ${errText}`))
    }
  } catch (error) {
    ElMessage.error('图片上传失败，请检查网络连接后重试')
    onError(error)
  }
}

const handleUploadError = (error, file) => {
  // 错误已在 customUpload 中处理并提示
}

const handleBusinessLicenseSuccess = (response) => {
  if (!response) return
  // 兼容不同的响应格式
  const url = response.url || response.data?.url || (typeof response === 'string' ? response : (response instanceof Blob || response instanceof File ? URL.createObjectURL(response) : ''))
  
  if (!url) return

  applicationForm.businessLicense = url
  if (businessLicenseList.value.length === 0) {
    businessLicenseList.value.push({
      name: response.name || '营业执照',
      url: url
    })
  }
}

const handleBusinessLicenseRemove = () => {
  applicationForm.businessLicense = ''
  businessLicenseList.value = []
}

const handleIdentityCardSuccess = (response) => {
  if (!response) return
  // 兼容不同的响应格式
  const url = response.url || response.data?.url || (typeof response === 'string' ? response : (response instanceof Blob || response instanceof File ? URL.createObjectURL(response) : ''))
  
  if (!url) return

  if (!applicationForm.identityCard) {
    applicationForm.identityCard = url
  } else {
    applicationForm.identityCard += ',' + url
  }
}

const handleIdentityCardRemove = (file) => {
  if (applicationForm.identityCard) {
    const urls = applicationForm.identityCard.split(',')
    const removeUrl = file.response?.url || file.url
    const newUrls = urls.filter(url => url !== removeUrl)
    applicationForm.identityCard = newUrls.join(',')
  }
}

const handleShopPhotosSuccess = (response) => {
  if (!response) return
  // 兼容不同的响应格式
  const url = response.url || response.data?.url || (typeof response === 'string' ? response : (response instanceof Blob || response instanceof File ? URL.createObjectURL(response) : ''))
  
  if (!url) return

  if (!applicationForm.shopPhotos) {
    applicationForm.shopPhotos = url
  } else {
    applicationForm.shopPhotos += ',' + url
  }
}

const handleShopPhotosRemove = (file) => {
  if (applicationForm.shopPhotos) {
    const urls = applicationForm.shopPhotos.split(',')
    const removeUrl = file.response?.url || file.url
    const newUrls = urls.filter(url => url !== removeUrl)
    applicationForm.shopPhotos = newUrls.join(',')
  }
}

const submitApplication = async () => {
  try {
    // 检查用户是否已登录
    if (!userStore.isLoggedIn || !userStore.user || !userStore.user.id) {
      ElMessage.warning('请先登录')
      router.push('/login')
      return
    }
    
    // 检查token是否存在
    if (!userStore.token) {
      ElMessage.warning('登录已过期，请重新登录')
      userStore.logout()
      router.push('/login')
      return
    }
    
    // 检查申请状态，防止重复提交
    if (applicationStatus.value === 'pending') {
      ElMessage.warning('您已有一个待审核的申请，请等待审核结果')
      return
    }
    
    if (applicationStatus.value === 'approved') {
      ElMessage.warning('您的商家资质已通过审核，无需重复申请')
      return
    }
    
    // 验证表单
    await formRef.value.validate()
    
    // 检查必填字段
    if (!applicationForm.businessLicense) {
      ElMessage.warning('请上传营业执照')
      return
    }
    if (!applicationForm.identityCard) {
      ElMessage.warning('请上传身份证')
      return
    }
    if (!applicationForm.shopPhotos) {
      ElMessage.warning('请上传店铺照片')
      return
    }
    if (!applicationForm.description || applicationForm.description.trim().length < 10) {
      ElMessage.warning('请输入申请描述，至少10个字符')
      return
    }
    
    submitting.value = true
    
    // 调试信息：检查 token 和用户信息
    console.log('提交申请前检查:', {
      hasToken: !!userStore.token,
      tokenLength: userStore.token?.length,
      tokenPreview: userStore.token?.substring(0, 20) + '...',
      userId: userStore.user?.id,
      userRole: userStore.user?.role
    })
    
    const data = {
      userId: userStore.user.id,
      businessLicense: applicationForm.businessLicense,
      identityCard: applicationForm.identityCard,
      shopPhotos: applicationForm.shopPhotos,
      description: applicationForm.description.trim()
    }
    
    const response = await merchantApplicationApi.submitApplication(data)
    
    if (response && response.success) {
      ElMessage.success('申请提交成功，请等待审核')
      
      // 如果响应中包含申请数据，直接使用
      if (response.data) {
        applicationData.value = response.data
        applicationStatus.value = response.data.status?.toLowerCase() || 'pending'
      }
      
      // 清空表单
      resetForm()
      // 重新加载申请状态（确保数据完整）
      await loadApplicationStatus()
    } else {
      ElMessage.error(response?.message || response?.data?.message || '提交失败')
    }
  } catch (error) {
    // 如果是表单验证错误，不显示错误提示（Element Plus会自动显示）
    if (error.fields) {
      // 这是表单验证错误，Element Plus会自动显示
      return
    }
    
    // 处理403错误（权限被拒绝）
    if (error.response && error.response.status === 403) {
      const errorMessage = error.response?.data?.message || '权限不足，请检查是否已登录'
      
      console.error('403错误详情:', {
        errorMessage,
        hasToken: !!userStore.token,
        tokenPreview: userStore.token?.substring(0, 30) + '...',
        userId: userStore.user?.id,
        userRole: userStore.user?.role,
        requestUrl: error.config?.url,
        requestMethod: error.config?.method
      })
      
      // 检查是否有 token
      if (!userStore.token) {
        ElMessage.error('请先登录')
        router.push('/login')
        return
      }
      
      // 有 token 但返回 403，可能是 token 无效、过期或权限不足
      // 尝试重新获取用户信息来验证 token 是否有效
      try {
        await userStore.initUser()
        // 如果 initUser 后 token 被清除，说明 token 无效
        if (!userStore.token) {
          ElMessage.error('登录已过期，请重新登录')
          router.push('/login')
          return
        }
        // Token 有效但返回 403，可能是权限问题
        ElMessage.error(errorMessage || '权限不足，请联系管理员')
      } catch (initError) {
        // initUser 失败，说明 token 无效，已经清除
        ElMessage.error('登录已过期，请重新登录')
        router.push('/login')
      }
      return
    }
    
    // 处理401错误（未授权）
    if (error.response && error.response.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      userStore.logout()
      router.push('/login')
      return
    }
    
    // 其他错误才显示
    console.error('提交失败:', error)
    const errorMessage = error.response?.data?.message || error.message || '提交失败，请重试'
    ElMessage.error(errorMessage)
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  formRef.value.resetFields()
  businessLicenseList.value = []
  identityCardList.value = []
  shopPhotosList.value = []
}

const reapply = () => {
  applicationStatus.value = 'none'
  resetForm()
}

const loadApplicationStatus = async () => {
  // 检查用户是否已登录
  if (!userStore.user || !userStore.user.id) {
    applicationStatus.value = 'none'
    return
  }

  loadingStatus.value = true
  try {
    console.log('加载申请状态，用户ID:', userStore.user.id)
    const response = await merchantApplicationApi.getApplicationByUserId(userStore.user.id)
    console.log('申请状态响应:', response)
    console.log('响应数据结构:', {
      response: response,
      'response.data': response?.data,
      'response.data.data': response?.data?.data,
      'response.success': response?.success
    })
    
    // 处理不同的响应结构
    let application = null
    if (response?.data?.success && response?.data?.data) {
      // 结构: {data: {success: true, data: {...}}}
      application = response.data.data
    } else if (response?.success && response?.data) {
      // 结构: {success: true, data: {...}}
      // 检查data是否为null或空对象
      if (response.data && response.data !== null && Object.keys(response.data).length > 0) {
        application = response.data
      }
    } else if (response?.data && Object.keys(response.data).length > 0 && !response.data.success) {
      // 结构: {data: {...}} (直接返回数据)
      application = response.data
    }
    
    console.log('解析后的申请数据:', application)
    console.log('application是否为有效数据:', application && typeof application === 'object' && application.id)
    
    if (application && application.id) {
      const oldStatus = applicationStatus.value
      // 确保状态转换为小写
      let newStatus = application.status
      if (typeof newStatus === 'string') {
        newStatus = newStatus.toLowerCase()
      }
      
      console.log('状态变化:', oldStatus, '->', newStatus, '原始状态:', application.status)
      
      // 检查是否是首次变为approved状态（用于触发效果）
      const isFirstTimeApproved = (oldStatus !== 'approved' && newStatus === 'approved')
      const isStatusChanged = (oldStatus !== newStatus)
      
      // 如果状态发生变化，更新状态
      if (isStatusChanged) {
        applicationStatus.value = newStatus
        
        // 保存完整的申请数据
        applicationData.value = application
        
        // 更新审核意见（已通过或已拒绝状态都可能有审核意见）
        if (application.auditOpinion) {
        auditOpinion.value = application.auditOpinion
        } else {
          auditOpinion.value = ''
        }
        
        // 如果状态变为approved（从任何状态），显示通知和动画效果
        if (isFirstTimeApproved) {
          console.log('检测到首次变为approved状态，触发更新效果')
          // 检查是否已经显示过弹窗
          if (!hasShownDialog(application.id)) {
            // 显示成功对话框
            showSuccessDialog.value = true
            // 触发状态更新动画
            triggerStatusUpdateAnimation()
            ElMessage.success('恭喜！您的商家资质申请已通过审核')
          }
        } else if (oldStatus === 'pending' && newStatus === 'rejected') {
          ElMessage.warning('您的商家资质申请未通过审核，请查看审核意见')
        }
      } else {
        // 即使状态没变化，也更新数据（确保数据是最新的）
        applicationData.value = application
        if (application.auditOpinion) {
          auditOpinion.value = application.auditOpinion
        }
        
        // 如果当前是approved状态且之前没有显示过对话框，检查是否需要显示效果
        if (newStatus === 'approved') {
          console.log('当前状态是approved，检查是否需要显示效果')
          console.log('showSuccessDialog当前值:', showSuccessDialog.value)
          
          // 检查更新时间，如果是最近10分钟内更新的，显示效果
          const updatedAt = application.updatedAt
          if (updatedAt) {
            const updateTime = new Date(updatedAt).getTime()
            const now = Date.now()
            const timeDiff = now - updateTime
            
            console.log('检查审核时间，时间差:', timeDiff / 1000, '秒', '更新时间:', updatedAt)
            
            // 如果是在最近10分钟内更新的，显示效果
            if (timeDiff < 600000 && !hasShownDialog(application.id)) { // 10分钟
              console.log('检测到最近审核通过的状态，触发更新效果')
              console.log('设置showSuccessDialog为true')
              showSuccessDialog.value = true
              console.log('showSuccessDialog设置后值:', showSuccessDialog.value)
              triggerStatusUpdateAnimation()
              ElMessage.success('恭喜！您的商家资质申请已通过审核')
            } else {
              console.log('时间差超过10分钟，不显示效果')
            }
          } else {
            console.log('没有updatedAt时间，直接显示效果')
            // 如果没有更新时间，也显示效果（可能是刚审核通过）
            if (!showSuccessDialog.value && !hasShownDialog(application.id)) {
              showSuccessDialog.value = true
              triggerStatusUpdateAnimation()
              ElMessage.success('恭喜！您的商家资质申请已通过审核')
            }
          }
        }
      }
    } else {
      applicationStatus.value = 'none'
      applicationData.value = null
    }
  } catch (error) {
    // 如果是404错误（申请不存在），这是正常情况，不显示错误
    if (error.response && error.response.status === 404) {
      applicationStatus.value = 'none'
      return
    }
    
    // 如果是401错误（未授权），用户可能未登录，静默处理
    if (error.response && error.response.status === 401) {
      applicationStatus.value = 'none'
      return
    }
    
    // 如果是403错误（权限被拒绝），可能是用户没有权限或申请不存在，静默处理
    if (error.response && error.response.status === 403) {
      applicationStatus.value = 'none'
      return
    }
    
    // 网络错误或其他错误，只在开发环境显示
    if (process.env.NODE_ENV === 'development') {
      console.error('加载申请状态失败:', error)
    }
    
    applicationStatus.value = 'none'
  } finally {
    loadingStatus.value = false
  }
}

const goBack = () => {
  router.go(-1)
}

// 触发状态更新动画
const triggerStatusUpdateAnimation = () => {
  console.log('触发状态更新动画')
  showStatusUpdateAnimation.value = true
  console.log('showStatusUpdateAnimation设置为:', showStatusUpdateAnimation.value)
  // 3秒后移除动画类
  setTimeout(() => {
    showStatusUpdateAnimation.value = false
  }, 3000)
}

// 关闭成功对话框
const closeSuccessDialog = () => {
  showSuccessDialog.value = false
  // 标记已显示过，保存到localStorage
  if (applicationData.value?.id) {
    const dialogShownKey = `qualification_dialog_shown_${applicationData.value.id}`
    localStorage.setItem(dialogShownKey, 'true')
  }
}

// 检查是否已经显示过弹窗
const hasShownDialog = (applicationId) => {
  if (!applicationId) return false
  const dialogShownKey = `qualification_dialog_shown_${applicationId}`
  return localStorage.getItem(dialogShownKey) === 'true'
}

// 强制显示成功效果（用于测试和调试）
const forceShowSuccessEffect = () => {
  console.log('强制触发成功效果')
  showSuccessDialog.value = true
  triggerStatusUpdateAnimation()
  ElMessage.success('恭喜！您的商家资质申请已通过审核')
}

// 处理图片加载错误
const handleImageError = (event, type, index) => {
  console.error(`图片加载失败 - 类型: ${type}, 索引: ${index}`, event)
  const urls = type === 'identityCard' ? identityCardUrls.value : shopPhotosUrls.value
  const failedUrl = urls[index]
  console.error('失败的URL:', failedUrl?.substring(0, 100))
  
  // 尝试修复URL
  if (failedUrl && !failedUrl.startsWith('data:image/') && !failedUrl.startsWith('http')) {
    console.log('尝试修复URL格式')
    // 可能是base64数据但没有前缀
    const fixedUrl = cleanImageUrl(failedUrl)
    if (fixedUrl && fixedUrl !== failedUrl) {
      // 更新URL
      if (type === 'identityCard') {
        const newUrls = [...identityCardUrls.value]
        newUrls[index] = fixedUrl
        // 注意：这里不能直接修改computed，需要在数据源修改
      }
    }
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 验证和清理图片URL
const cleanImageUrl = (url) => {
  if (!url || typeof url !== 'string') {
    return null
  }
  
  const trimmed = url.trim()
  
  // 如果已经是完整的data URI，直接返回
  if (trimmed.startsWith('data:image/')) {
    return trimmed
  }
  
  // 如果是HTTP/HTTPS URL，直接返回
  if (trimmed.startsWith('http://') || trimmed.startsWith('https://')) {
    return trimmed
  }
  
  // 如果是相对路径（以/开头但不是base64），直接返回
  if (trimmed.startsWith('/') && !trimmed.startsWith('/9j/') && trimmed.length < 100) {
    return trimmed
  }
  
  // 检测base64数据特征
  // JPEG base64通常以 /9j/4AAQ 开头
  // PNG base64通常以 iVBORw0KGgo 开头
  // 或者是很长的字符串（base64数据通常很长）
  const isLikelyBase64 = (
    trimmed.startsWith('/9j/') || 
    trimmed.startsWith('iVBORw0KGgo') ||
    trimmed.startsWith('UklGR') || // WebP
    (trimmed.length > 100 && /^[A-Za-z0-9+/=]+$/.test(trimmed.replace(/\s/g, '')))
  )
  
  if (isLikelyBase64) {
    // 判断图片类型
    let mimeType = 'image/jpeg' // 默认JPEG
    
    if (trimmed.startsWith('iVBORw0KGgo')) {
      mimeType = 'image/png'
    } else if (trimmed.startsWith('UklGR')) {
      mimeType = 'image/webp'
    } else if (trimmed.startsWith('/9j/') || trimmed.startsWith('data:image/jpeg')) {
      mimeType = 'image/jpeg'
    }
    
    // 如果已经有data:前缀但格式不对，修复它
    if (trimmed.includes('data:image/')) {
      // 提取base64部分
      const base64Part = trimmed.split('base64,')[1] || trimmed
      return `data:${mimeType};base64,${base64Part}`
    }
    
    // 添加data URI前缀
    return `data:${mimeType};base64,${trimmed}`
  }
  
  // 其他情况，尝试作为相对路径
  return trimmed.startsWith('/') ? trimmed : `/${trimmed}`
}

// 计算属性：身份证图片URL列表
// 解析图片字段字符串为 URL 数组（正确处理逗号分隔的路径和 base64）
const parseImageField = (data) => {
  if (!data) return []
  if (Array.isArray(data)) return data.map(u => cleanImageUrl(u)).filter(Boolean)
  if (typeof data !== 'string') return []

  const s = data.trim()
  if (!s) return []

  // 单个 data URI，直接返回
  if (s.startsWith('data:image/')) return [s]

  // 用正则提取所有合法 URL（data URI / http / /uploads 路径）
  const urlPattern = /(?:data:image\/[^;]+;base64,[A-Za-z0-9+/=]+|https?:\/\/[^\s,]+|\/uploads\/[^\s,]+|uploads\/[^\s,]+)/g
  const matches = s.match(urlPattern)
  if (matches && matches.length > 0) {
    return matches.map(u => cleanImageUrl(u)).filter(Boolean)
  }

  // 降级：普通逗号分割（适用于普通短路径）
  return s.split(',').map(u => cleanImageUrl(u.trim())).filter(Boolean)
}

const identityCardUrls = computed(() => {
  if (!applicationData.value?.identityCard) return []
  return parseImageField(applicationData.value.identityCard)
})

// 计算属性：店铺照片URL列表
const shopPhotosUrls = computed(() => {
  if (!applicationData.value?.shopPhotos) return []
  return parseImageField(applicationData.value.shopPhotos)
})

// 轮询间隔（毫秒）- 如果状态是pending，每10秒检查一次
let statusPollingInterval = null
const POLLING_INTERVAL = 10000 // 10秒（更频繁的检查）

// 启动状态轮询（仅在pending状态时）
const startStatusPolling = () => {
  // 清除之前的轮询
  stopStatusPolling()
  
  // 如果状态是pending，启动轮询
  if (applicationStatus.value === 'pending') {
    console.log('启动状态轮询，每', POLLING_INTERVAL / 1000, '秒检查一次')
    statusPollingInterval = setInterval(async () => {
      console.log('轮询检查申请状态...')
      await loadApplicationStatus()
    }, POLLING_INTERVAL)
  }
}

// 停止状态轮询
const stopStatusPolling = () => {
  if (statusPollingInterval) {
    clearInterval(statusPollingInterval)
    statusPollingInterval = null
  }
}

// 处理WebSocket数据更新
const handleApplicationStatusUpdate = (updateInfo) => {
  console.log('收到WebSocket数据更新:', updateInfo)
  const { operation, data } = updateInfo
  
  // 只处理更新操作
  if (operation === 'update' && data) {
    // 检查是否是当前用户的申请
    if (userStore.user && data.userId === userStore.user.id) {
      console.log('检测到当前用户的申请更新')
      const newStatus = data.status?.toLowerCase() || data.status
      const oldStatus = applicationStatus.value
      
      console.log('WebSocket状态变化:', oldStatus, '->', newStatus)
      
      // 如果状态发生变化
      if (newStatus && newStatus !== oldStatus) {
        applicationStatus.value = newStatus
        
        // 更新审核意见
        if (data.auditOpinion) {
          auditOpinion.value = data.auditOpinion
        }
        
        // 更新申请数据（如果WebSocket消息包含完整数据）
        if (data.id || data.businessLicense || data.description) {
          applicationData.value = { ...applicationData.value, ...data }
        }
        
        // 如果状态从pending变为approved或rejected，显示通知和动画效果
        if (oldStatus === 'pending' && (newStatus === 'approved' || newStatus === 'rejected')) {
          console.log('WebSocket触发状态变化效果')
          if (newStatus === 'approved') {
            // 检查是否已经显示过弹窗
            if (!hasShownDialog(data.id || applicationData.value?.id)) {
              // 显示成功对话框
              showSuccessDialog.value = true
              // 触发状态更新动画
              triggerStatusUpdateAnimation()
              ElMessage.success('恭喜！您的商家资质申请已通过审核')
            }
            // 重新加载完整数据以显示申请资料
            setTimeout(() => {
  loadApplicationStatus()
            }, 500)
          } else if (newStatus === 'rejected') {
            ElMessage.warning('您的商家资质申请未通过审核，请查看审核意见')
          }
        }
        
        // 停止轮询（因为状态已更新）
        stopStatusPolling()
      } else {
        // 即使状态没变化，也重新加载数据确保同步
        loadApplicationStatus()
      }
    }
  }
}

// 处理WebSocket通知
const handleNotification = (notification) => {
  console.log('收到WebSocket通知:', notification)
  // 检查是否是申请审核相关的通知
  if (notification.type === 'merchant_application_audited' || 
      notification.type === 'application_status_changed') {
    const { userId, status, auditOpinion: opinion } = notification.data || {}
    
    console.log('处理审核通知，用户ID:', userId, '状态:', status)
    
    // 检查是否是当前用户的申请
    if (userStore.user && userId === userStore.user.id) {
      const newStatus = status?.toLowerCase() || status
      const oldStatus = applicationStatus.value
      
      console.log('通知状态变化:', oldStatus, '->', newStatus)
      
      if (newStatus && newStatus !== oldStatus) {
        applicationStatus.value = newStatus
        
        if (opinion) {
          auditOpinion.value = opinion
        }
        
        if (oldStatus === 'pending') {
          if (newStatus === 'approved') {
            console.log('通知触发审核通过效果')
            // 检查是否已经显示过弹窗（需要先获取applicationId）
            const applicationId = applicationData.value?.id || notification.data?.applicationId
            if (!hasShownDialog(applicationId)) {
              // 显示成功对话框
              showSuccessDialog.value = true
              // 触发状态更新动画
              triggerStatusUpdateAnimation()
              ElMessage.success('恭喜！您的商家资质申请已通过审核')
            }
            // 重新加载完整数据以显示申请资料
            setTimeout(() => {
              loadApplicationStatus()
            }, 500)
          } else if (newStatus === 'rejected') {
            ElMessage.warning('您的商家资质申请未通过审核，请查看审核意见')
          }
        }
        
        stopStatusPolling()
      } else {
        // 即使状态没变化，也重新加载数据确保同步
        loadApplicationStatus()
      }
    }
  }
}

// 监听状态变化，自动启动/停止轮询
watch(applicationStatus, (newStatus, oldStatus) => {
  if (newStatus === 'pending') {
    startStatusPolling()
  } else {
    stopStatusPolling()
  }
  
  // 如果状态从pending变为approved，触发动画效果
  if (oldStatus === 'pending' && newStatus === 'approved') {
    // 延迟一点时间，确保DOM已更新
    setTimeout(() => {
      triggerStatusUpdateAnimation()
    }, 100)
  }
}, { immediate: true })

onMounted(async () => {
  // 加载申请状态
  await loadApplicationStatus()
  
  // 如果加载后发现是approved状态，检查是否需要显示效果
  if (applicationStatus.value === 'approved' && applicationData.value) {
    console.log('页面加载完成，检查是否需要显示效果')
    // 检查更新时间，如果是最近10分钟内更新的，显示效果
    const updatedAt = applicationData.value.updatedAt
    if (updatedAt) {
      const updateTime = new Date(updatedAt).getTime()
      const now = Date.now()
      const timeDiff = now - updateTime
      
      console.log('onMounted检查时间差:', timeDiff / 1000, '秒')
      
      // 如果是在最近10分钟内更新的，显示效果
      if (timeDiff < 600000 && !hasShownDialog(applicationData.value.id)) { // 10分钟
        console.log('onMounted检测到最近审核通过，触发更新效果')
        setTimeout(() => {
          console.log('onMounted设置showSuccessDialog为true')
          showSuccessDialog.value = true
          triggerStatusUpdateAnimation()
          ElMessage.success('恭喜！您的商家资质申请已通过审核')
        }, 500)
      } else {
        console.log('onMounted: 时间差超过10分钟或已显示过对话框')
      }
    } else {
      console.log('onMounted: 没有updatedAt时间')
    }
  }
  
  // 连接WebSocket（如果未连接）
  if (!realtimeSync.isConnected) {
    realtimeSync.connect()
  }
  
  // 订阅商家申请状态更新
  realtimeSync.subscribe('merchant_application', handleApplicationStatusUpdate)
  
  // 订阅通知
  realtimeSync.addEventListener('notification', handleNotification)
})

onUnmounted(() => {
  // 停止轮询
  stopStatusPolling()
  
  // 取消WebSocket订阅
  realtimeSync.unsubscribe('merchant_application', handleApplicationStatusUpdate)
  realtimeSync.removeEventListener('notification', handleNotification)
})
</script>

<style scoped>
.qualification-apply {
  padding: 20px;
  background: white;
  min-height: 100%;
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
}

.back-button-container {
  margin-bottom: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 2px solid #f0f2f5;
}

.header-left {
  flex: 1;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 28px;
  font-weight: 600;
}

.header-subtitle {
  margin: 0;
  color: #909399;
  font-size: 14px;
  line-height: 1.5;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-card {
  margin-bottom: 24px;
  transition: all 0.5s ease;
  border-radius: 12px;
  overflow: hidden;
}

.status-card.status-updated {
  animation: statusUpdatePulse 1.5s ease-in-out;
  box-shadow: 0 0 20px rgba(103, 194, 58, 0.5);
  border: 2px solid #67c23a;
}

@keyframes statusUpdatePulse {
  0% {
    transform: scale(1);
    box-shadow: 0 0 0 0 rgba(103, 194, 58, 0.7);
  }
  50% {
    transform: scale(1.02);
    box-shadow: 0 0 0 10px rgba(103, 194, 58, 0);
  }
  100% {
    transform: scale(1);
    box-shadow: 0 0 0 0 rgba(103, 194, 58, 0);
  }
}

.status-content {
  display: flex;
  align-items: flex-start;
  gap: 24px;
  padding: 8px 0;
}

.status-icon-wrapper {
  flex-shrink: 0;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
}

.status-icon-wrapper.approved {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
}

.status-icon-wrapper.pending {
  background: linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%);
}

.status-icon-wrapper.rejected {
  background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
}

.status-icon {
  font-size: 48px;
}

.status-icon.approved {
  color: #67c23a;
}

.status-icon.pending {
  color: #e6a23c;
}

.status-icon.rejected {
  color: #f56c6c;
}

.status-details {
  flex: 1;
}

.status-details h3 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 20px;
  font-weight: 600;
}

.status-details p {
  margin: 0 0 16px 0;
  color: #606266;
  line-height: 1.8;
  font-size: 15px;
}

.audit-opinion {
  margin-top: 16px;
  padding: 16px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.audit-opinion-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  color: #303133;
  font-weight: 600;
}

.audit-opinion-header .el-icon {
  color: #409eff;
  font-size: 18px;
}

.audit-opinion-text {
  margin: 0;
  color: #606266;
  line-height: 1.8;
  padding-left: 26px;
}

.upload-wrapper {
  width: 100%;
}

.upload-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #909399;
  font-size: 13px;
  margin-top: 12px;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 6px;
}

.upload-tip .el-icon {
  color: #409eff;
  font-size: 16px;
}

.form-card {
  border-radius: 12px;
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.card-header .el-icon {
  font-size: 20px;
  color: #409eff;
}

.header-tag {
  margin-left: auto;
}

.application-form {
  padding: 8px 0;
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.form-actions .el-button {
  min-width: 120px;
}

.reapply-section {
  text-align: center;
  margin-top: 24px;
  padding: 24px;
  background: #fff7ed;
  border-radius: 8px;
  border: 1px dashed #f59e0b;
}

.reapply-section .el-button {
  min-width: 140px;
  height: 40px;
}

.application-data-card {
  margin-top: 24px;
  border-radius: 12px;
  overflow: hidden;
}

.application-images {
  margin-top: 24px;
}

.image-section {
  margin-bottom: 32px;
  padding: 20px;
  background: #fafbfc;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.image-section:last-child {
  margin-bottom: 0;
}

.image-section h4 {
  margin: 0 0 20px 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  padding-bottom: 12px;
  border-bottom: 2px solid #e4e7ed;
}

.image-section h4::before {
  content: '';
  width: 4px;
  height: 18px;
  background: #409eff;
  border-radius: 2px;
}

.application-info {
  margin-bottom: 8px;
}

.info-value {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
}

.info-value .el-icon {
  color: #909399;
  font-size: 16px;
}

.description-text {
  color: #606266;
  line-height: 1.8;
  padding: 8px 0;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.application-image {
  width: 220px;
  height: 220px;
  border-radius: 12px;
  border: 2px solid #e4e7ed;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.application-image:hover {
  border-color: #409eff;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.2);
  transform: translateY(-2px);
}

.image-error {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #909399;
  font-size: 14px;
}

.image-error .el-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

/* 成功对话框样式 */
.success-dialog-content {
  text-align: center;
  padding: 20px 0;
}

.success-icon {
  margin-bottom: 20px;
  animation: successIconBounce 0.6s ease-in-out;
}

@keyframes successIconBounce {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.2);
  }
}

.success-dialog-content h3 {
  margin: 20px 0 10px 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.success-dialog-content p {
  margin: 10px 0;
  color: #606266;
  font-size: 16px;
  line-height: 1.6;
}

.success-message {
  color: #67c23a !important;
  font-weight: 500;
  font-size: 18px !important;
  margin-top: 20px !important;
}

.audit-opinion-box {
  margin-top: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  text-align: left;
}

.audit-opinion-box strong {
  display: block;
  margin-bottom: 8px;
  color: #303133;
}

.audit-opinion-box p {
  margin: 0;
  color: #606266;
  line-height: 1.6;
}

/* 状态图标动画 */
.status-icon.approved {
  animation: approvedIconSpin 1s ease-in-out;
}

@keyframes approvedIconSpin {
  0% {
    transform: rotate(0deg) scale(1);
  }
  50% {
    transform: rotate(180deg) scale(1.2);
  }
  100% {
    transform: rotate(360deg) scale(1);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .page-header h2 {
    font-size: 24px;
  }
  
  .header-actions {
    width: 100%;
    justify-content: space-between;
  }
  
  .status-content {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }
  
  .status-icon-wrapper {
    margin: 0 auto;
  }
  
  .application-form {
    padding: 0;
  }
  
  .application-form :deep(.el-form-item__label) {
    width: 100% !important;
    text-align: left;
    margin-bottom: 8px;
  }
  
  .form-actions {
    flex-direction: column;
    width: 100%;
  }
  
  .form-actions .el-button {
    width: 100%;
  }
  
  .image-list {
    justify-content: center;
  }
  
  .application-image {
    width: 100%;
    max-width: 300px;
    height: auto;
    aspect-ratio: 1;
  }
  
  .image-section {
    padding: 16px;
  }
}
</style>