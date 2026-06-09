<template>
  <div class="merchant-profile">
    <div class="profile-header">
      <div class="header-content">
        <el-button @click="goBack" :icon="ArrowLeft" circle />
        <h2>个人资料</h2>
      </div>
    </div>

    <div class="profile-content">
      <el-card class="profile-card">
        <template #header>
          <div class="card-header">
            <span>基本信息</span>
            <el-button 
              v-if="!editMode" 
              type="primary" 
              size="small" 
              @click="enterEditMode"
            >
              编辑
            </el-button>
          </div>
        </template>

        <div v-if="editMode" class="edit-form">
          <!-- 头像上传 -->
          <el-form :model="userForm" label-width="120px">
            <el-form-item label="头像">
              <div class="avatar-upload-section">
                <el-upload
                  class="avatar-uploader"
                  :http-request="customAvatarUpload"
                  :show-file-list="false"
                  :on-success="handleAvatarSuccess"
                  :on-error="handleAvatarError"
                  :before-upload="beforeAvatarUpload"
                  accept="image/*"
                >
                  <div class="avatar-preview">
                    <img v-if="userForm.avatar" :src="userForm.avatar" class="avatar-image" />
                    <div v-else class="avatar-placeholder">
                      <el-icon class="upload-icon"><Plus /></el-icon>
                      <div class="upload-text">上传头像</div>
                    </div>
                  </div>
                </el-upload>
                <div class="avatar-hint">
                  <p>建议上传正方形图片，尺寸不小于 200x200px</p>
                  <p>支持 JPG、PNG 格式，文件大小不超过 5MB</p>
                </div>
              </div>
            </el-form-item>

            <el-form-item label="用户名" required>
              <el-input 
                v-model="userForm.username" 
                placeholder="请输入用户名"
                maxlength="50"
                show-word-limit
              />
              <div class="field-hint">用户名将用于登录和显示</div>
            </el-form-item>

            <el-form-item label="手机号">
              <el-input 
                v-model="userForm.phone" 
                placeholder="请输入手机号"
                maxlength="20"
              />
              <div class="field-hint">用于接收重要通知和验证</div>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="saveUserInfo" :loading="saving">
                保存
              </el-button>
              <el-button @click="cancelEdit">取消</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div v-else class="info-display">
          <div class="info-row">
            <div class="info-label">头像：</div>
            <div class="info-value">
              <el-avatar :size="80" :src="userStore.user?.avatar">
                {{ userStore.user?.username?.charAt(0) }}
              </el-avatar>
            </div>
          </div>
          <div class="info-row">
            <div class="info-label">用户名：</div>
            <div class="info-value">{{ userStore.user?.username || '未设置' }}</div>
          </div>
          <div class="info-row">
            <div class="info-label">手机号：</div>
            <div class="info-value">{{ userStore.user?.phone || '未设置' }}</div>
          </div>
          <div class="info-row">
            <div class="info-label">角色：</div>
            <div class="info-value">
              <el-tag type="success">商家</el-tag>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Plus } from '@element-plus/icons-vue'
import { getUserInfo, updateUserInfo } from '../../api/auth'
import request from '../../utils/request'

const router = useRouter()
const userStore = useUserStore()

const editMode = ref(false)
const saving = ref(false)

const userForm = reactive({
  username: '',
  phone: '',
  avatar: ''
})

const goBack = () => {
  router.go(-1)
}

const enterEditMode = () => {
  // 从 userStore 加载当前用户信息
  userForm.username = userStore.user?.username || ''
  userForm.phone = userStore.user?.phone || ''
  userForm.avatar = userStore.user?.avatar || ''
  editMode.value = true
}

const cancelEdit = () => {
  editMode.value = false
  // 重置表单
  userForm.username = userStore.user?.username || ''
  userForm.phone = userStore.user?.phone || ''
  userForm.avatar = userStore.user?.avatar || ''
}

const beforeAvatarUpload = (file) => {
  // 检查文件类型
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  
  // 检查文件大小（5MB）
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB！')
    return false
  }
  
  return true
}

// 自定义头像上传方法
const customAvatarUpload = async (options) => {
  const { file, onSuccess, onError } = options
  
  try {
    const formData = new FormData()
    formData.append('file', file.raw || file)
    
    // 使用 request 工具，确保携带 token
    const response = await request.post('/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    // 处理响应
    const url = response.url || response.data?.url
    if (response.success && url) {
      // 上传成功，更新表单中的头像
      userForm.avatar = url
      onSuccess({ url: url })
      ElMessage.success('头像上传成功')
    } else {
      const errorMsg = response.message || '上传失败，服务器未返回图片地址'
      throw new Error(errorMsg)
    }
  } catch (error) {
    console.error('头像上传失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '头像上传失败，请重试'
    ElMessage.error(errorMsg)
    onError(error)
  }
}

const handleAvatarError = (error, file) => {
  console.error('头像上传错误:', error)
  ElMessage.error('头像上传失败，请重试')
}

const handleAvatarSuccess = (response) => {
  // 响应已经在 customAvatarUpload 中处理了
  if (response && response.url && !userForm.avatar) {
    userForm.avatar = response.url
  }
}

const saveUserInfo = async () => {
  // 验证必填字段
  if (!userForm.username || userForm.username.trim() === '') {
    ElMessage.warning('请输入用户名')
    return
  }

  // 验证用户名长度
  if (userForm.username.trim().length < 2) {
    ElMessage.warning('用户名至少需要2个字符')
    return
  }

  // 避免将超长的 base64 数据写入数据库
  let avatarForServer = userForm.avatar
  if (avatarForServer && typeof avatarForServer === 'string' && avatarForServer.startsWith('data:')) {
    ElMessage.warning('检测到头像未成功上传到服务器，请重新上传头像后再保存')
    return
  }

  saving.value = true
  try {
    const payload = {
      username: userForm.username.trim(),
      phone: userForm.phone?.trim() || null,
      avatar: avatarForServer || null
    }

    const res = await updateUserInfo(payload)
    
    // 兼容不同响应结构
    let data = res?.data?.data || res?.data || null
    if (res?.success && res?.data) {
      data = res.data
    }

    if (data) {
      // 更新 userStore 中的用户信息
      if (data.username) {
        userStore.user.username = data.username
      }
      if (data.phone !== undefined) {
        userStore.user.phone = data.phone
      }
      if (data.avatar !== undefined) {
        userStore.user.avatar = data.avatar
      }
      
      // 保存到 localStorage
      userStore.saveUser()
      
      editMode.value = false
      ElMessage.success('个人信息更新成功')
    } else {
      ElMessage.error('更新失败，服务器未返回数据')
    }
  } catch (error) {
    console.error('更新用户信息失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '更新失败，请稍后再试'
    ElMessage.error(errorMsg)
  } finally {
    saving.value = false
  }
}

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const res = await getUserInfo()
    let data = res?.data?.data || res?.data || null
    if (res?.success && res?.data) {
      data = res.data
    }
    
    if (data) {
      // 更新 userStore
      if (data.username) {
        userStore.user.username = data.username
      }
      if (data.phone !== undefined) {
        userStore.user.phone = data.phone
      }
      if (data.avatar !== undefined) {
        userStore.user.avatar = data.avatar
      }
      userStore.saveUser()
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.merchant-profile {
  min-height: 100vh;
  background: #f5f7fa;
}

.profile-header {
  background: white;
  border-bottom: 1px solid #e4e7ed;
  padding: 16px 24px;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-content h2 {
  margin: 0;
  color: #303133;
  font-size: 20px;
}

.profile-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.profile-card {
  background: white;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.edit-form {
  padding: 20px 0;
}

.avatar-upload-section {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.avatar-uploader :deep(.el-upload) {
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
}

.avatar-uploader :deep(.el-upload):hover {
  border-color: #409eff;
}

.avatar-preview {
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.avatar-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8c939d;
}

.upload-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.upload-text {
  font-size: 14px;
}

.avatar-hint {
  flex: 1;
  padding-top: 8px;
}

.avatar-hint p {
  margin: 4px 0;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}

.field-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.5;
}

.info-display {
  padding: 20px 0;
}

.info-row {
  display: flex;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  width: 120px;
  font-weight: 500;
  color: #606266;
  flex-shrink: 0;
}

.info-value {
  flex: 1;
  color: #303133;
}

@media (max-width: 768px) {
  .profile-content {
    padding: 16px;
  }

  .avatar-upload-section {
    flex-direction: column;
  }

  .info-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .info-label {
    width: 100%;
  }
}
</style>

