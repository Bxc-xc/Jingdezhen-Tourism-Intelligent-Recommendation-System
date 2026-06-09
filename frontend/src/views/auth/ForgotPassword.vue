<template>
  <div class="forgot-password">
    <!-- 动态背景装饰 -->
    <div class="auth-bg-decoration">
      <div class="bg-blob blob-1"></div>
      <div class="bg-blob blob-2"></div>
      <div class="bg-blob blob-3"></div>
      <div class="bg-gradient-orb orb-1"></div>
      <div class="bg-gradient-orb orb-2"></div>
      <div class="floating-shapes">
        <span class="shape shape-1"></span>
        <span class="shape shape-2"></span>
        <span class="shape shape-3"></span>
        <span class="shape shape-4"></span>
        <span class="shape shape-5"></span>
      </div>
    </div>

    <div class="forgot-password-container">
      <div class="forgot-password-form">
        <!-- 返回登录链接 -->
        <div class="back-link animate-fade-in">
          <el-button text @click="$router.push('/login')" class="back-btn">
            <el-icon><ArrowLeft /></el-icon>
            {{ $t('auth.forgotPassword.backToLogin') }}
          </el-button>
        </div>

        <div class="form-header animate-fade-in">
          <div class="header-icon-wrap">
            <el-icon class="header-icon"><Lock /></el-icon>
          </div>
          <h1>{{ $t('auth.forgotPassword.title') }}</h1>
          <p>{{ $t('auth.forgotPassword.welcome') }}</p>
          <div class="security-badge">
            <el-icon><CircleCheck /></el-icon>
            <span>{{ $t('auth.forgotPassword.banner.title') }}</span>
          </div>
        </div>
        
        <el-form class="auth-form"
          :model="forgotForm"
          :rules="forgotRules"
          ref="forgotFormRef"
          label-width="0"
          size="large"
        >
          <el-form-item prop="username">
            <el-input
              v-model="forgotForm.username"
              :placeholder="$t('auth.forgotPassword.username')"
              prefix-icon="User"
            />
          </el-form-item>
          
          <el-form-item prop="phone">
            <el-input
              v-model="forgotForm.phone"
              :placeholder="$t('auth.forgotPassword.phone')"
              prefix-icon="Phone"
            />
          </el-form-item>
          
          <el-form-item prop="newPassword">
            <el-input
              v-model="forgotForm.newPassword"
              type="password"
              :placeholder="$t('auth.forgotPassword.newPassword')"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          
          <el-form-item prop="confirmPassword">
            <el-input
              v-model="forgotForm.confirmPassword"
              type="password"
              :placeholder="$t('auth.forgotPassword.confirmPassword')"
              prefix-icon="Lock"
              show-password
              @keyup.enter="handleReset"
            />
          </el-form-item>
          
          <el-form-item>
            <el-button
              type="primary"
              class="reset-button"
              :loading="loading"
              @click="handleReset"
            >
              {{ $t('auth.forgotPassword.resetButton') }}
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="form-footer animate-fade-in">
          <p>{{ $t('auth.forgotPassword.rememberPassword') }} <el-button link type="primary" @click="$router.push('/login')">{{ $t('auth.forgotPassword.backToLogin') }}</el-button></p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { forgotPassword } from '../../api/auth'
import { ElMessage } from 'element-plus'
import BrandIcon from '../../components/BrandIcon.vue'
import { ArrowLeft, Lock, User, Phone, CircleCheck } from '@element-plus/icons-vue'

const { t } = useI18n()
const router = useRouter()

const loading = ref(false)
const forgotFormRef = ref()

const forgotForm = reactive({
  username: '',
  phone: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== forgotForm.newPassword) {
    callback(new Error(t('auth.forgotPassword.validation.passwordMismatch')))
  } else {
    callback()
  }
}

const forgotRules = computed(() => ({
  username: [
    { required: true, message: t('auth.forgotPassword.validation.usernameRequired'), trigger: 'blur' }
  ],
  phone: [
    { required: true, message: t('auth.forgotPassword.validation.phoneRequired'), trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: t('auth.forgotPassword.validation.phoneInvalid'), trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: t('auth.forgotPassword.validation.passwordRequired'), trigger: 'blur' },
    { min: 6, message: t('auth.forgotPassword.validation.passwordMinLength'), trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: t('auth.forgotPassword.validation.confirmPasswordRequired'), trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}))

const handleReset = async () => {
  if (!forgotFormRef.value) return
  
  try {
    await forgotFormRef.value.validate()
    loading.value = true
    
    const res = await forgotPassword({
      username: forgotForm.username.trim(),
      phone: forgotForm.phone.trim(),
      newPassword: forgotForm.newPassword
    })
    
    if (res.success) {
      ElMessage.success(t('auth.forgotPassword.resetSuccess'))
      router.push('/login')
    } else {
      ElMessage.error(res.message || t('auth.forgotPassword.resetFailed'))
    }
  } catch (error) {
    // 表单验证失败时不重复提示；API 错误由 request 拦截器统一处理
    if (!error.response && error.message && !error.message.includes('validation')) {
      ElMessage.error(error.message)
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.forgot-password {
  min-height: 100vh;
  background: url('/images/backgrounds/bjt2.jpg') center/cover no-repeat fixed;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
  font-family: ui-sans-serif, system-ui, -apple-system, "Segoe UI", Roboto, "PingFang SC", "Microsoft YaHei", "Noto Sans CJK SC", "Helvetica Neue", Arial;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.auth-bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
}

.bg-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.5;
  animation: floatBlob 25s infinite ease-in-out;
}

.bg-blob.blob-1 {
  top: -10%;
  right: -5%;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(64, 158, 255, 0.25) 0%, transparent 70%);
  animation-delay: 0s;
}

.bg-blob.blob-2 {
  bottom: -15%;
  left: -5%;
  width: 350px;
  height: 350px;
  background: radial-gradient(circle, rgba(79, 172, 254, 0.2) 0%, transparent 70%);
  animation-delay: -8s;
  animation-duration: 20s;
}

.bg-blob.blob-3 {
  top: 50%;
  left: 30%;
  width: 250px;
  height: 250px;
  background: radial-gradient(circle, rgba(103, 194, 58, 0.15) 0%, transparent 70%);
  animation-delay: -15s;
  animation-duration: 22s;
}

.bg-gradient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.3;
  animation: pulseOrb 8s infinite ease-in-out;
}

.bg-gradient-orb.orb-1 {
  top: 20%;
  left: 10%;
  width: 200px;
  height: 200px;
  background: rgba(64, 158, 255, 0.4);
}

.bg-gradient-orb.orb-2 {
  bottom: 25%;
  right: 15%;
  width: 180px;
  height: 180px;
  background: rgba(79, 172, 254, 0.35);
  animation-delay: -4s;
}

.floating-shapes .shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.6);
  animation: floatShape 15s infinite ease-in-out;
}

.floating-shapes .shape-1 { width: 8px; height: 8px; top: 15%; left: 20%; animation-delay: 0s; }
.floating-shapes .shape-2 { width: 6px; height: 6px; top: 70%; right: 25%; animation-delay: -3s; animation-duration: 12s; }
.floating-shapes .shape-3 { width: 10px; height: 10px; bottom: 20%; left: 15%; animation-delay: -6s; animation-duration: 18s; }
.floating-shapes .shape-4 { width: 5px; height: 5px; top: 40%; right: 10%; animation-delay: -9s; }
.floating-shapes .shape-5 { width: 7px; height: 7px; top: 80%; left: 30%; animation-delay: -12s; animation-duration: 14s; }

@keyframes floatBlob {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -25px) scale(1.08); }
  66% { transform: translate(-25px, 20px) scale(0.95); }
}

@keyframes pulseOrb {
  0%, 100% { opacity: 0.25; transform: scale(1); }
  50% { opacity: 0.4; transform: scale(1.2); }
}

@keyframes floatShape {
  0%, 100% { transform: translate(0, 0); opacity: 0.6; }
  50% { transform: translate(20px, -15px); opacity: 1; }
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(15px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes slideInRight {
  from { opacity: 0; transform: translateX(30px); }
  to { opacity: 1; transform: translateX(0); }
}

.animate-fade-in {
  animation: fadeIn 0.6s ease-out forwards;
}

.animate-slide-in {
  animation: slideInRight 0.7s ease-out 0.2s forwards;
  opacity: 0;
}

/* 玻璃拟态卡片 */
.forgot-password-container {
  position: relative;
  z-index: 1;
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 
    0 18px 40px -14px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
  overflow: visible;
  display: flex;
  max-width: 480px;
  width: 100%;
  min-height: auto;
  transition: box-shadow 0.3s ease;
}

.forgot-password-container::before {
  content: '';
  position: absolute;
  inset: 0;
  width: 100%;
  background: linear-gradient(145deg, rgba(248, 250, 255, 0.70) 0%, rgba(242, 246, 252, 0.35) 100%);
  pointer-events: none;
  z-index: 0;
}

.forgot-password-form {
  position: relative;
  z-index: 1;
}

.forgot-password-container:hover {
  box-shadow: 
    0 22px 46px -16px rgba(0, 0, 0, 0.10),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
}

.forgot-password-form {
  flex: 1;
  padding: 60px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.back-link {
  position: absolute;
  top: 20px;
  left: 20px;
}

.back-btn {
  font-size: 14px;
  color: #606266;
  padding: 8px 12px;
}

.back-btn:hover {
  color: #409EFF;
  background: rgba(64, 158, 255, 0.1);
}

.form-header {
  text-align: center;
  margin-bottom: 40px;
  margin-top: 20px;
}

.header-icon-wrap {
  width: 64px;
  height: 64px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, #409EFF 0%, #4facfe 100%);
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.35);
}

.header-icon {
  font-size: 32px;
  color: white;
}

.form-header h1 {
  font-size: 38px;
  color: #303133;
  margin-bottom: 8px;
  font-weight: 650;
  letter-spacing: 0.2px;
  line-height: 1.1;
}

.form-header p {
  color: #606266;
  font-size: 16px;
  margin-bottom: 16px;
}

.security-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  background: rgba(103, 194, 58, 0.1);
  color: #67c23a;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

/* 输入框增强 */
:deep(.el-input__wrapper) {
  border-radius: 12px;
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover),
:deep(.el-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 1px rgba(64, 158, 255, 0.14);
}

.reset-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.reset-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 24px rgba(64, 158, 255, 0.22);
}

.form-footer {
  text-align: center;
  margin-top: 20px;
}

.form-footer p {
  color: #606266;
  margin: 0;
}

@keyframes contentReveal {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 768px) {
  .forgot-password-container {
    flex-direction: column;
    max-width: 400px;
  }
  
  .forgot-password-banner {
    display: none;
  }
  
  .forgot-password-form {
    padding: 50px 24px 40px;
  }
  
  .back-link {
    top: 16px;
    left: 16px;
  }
  
  .back-btn {
    font-size: 13px;
    padding: 6px 10px;
  }
  
  .form-header h1 {
    font-size: 24px;
  }
  
  .header-icon-wrap {
    width: 56px;
    height: 56px;
  }
  
  .header-icon {
    font-size: 28px;
  }
  
  .security-badge {
    font-size: 12px;
    padding: 5px 12px;
  }
}
</style>
