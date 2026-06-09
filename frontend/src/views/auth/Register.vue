<template>
  <div class="register">
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

    <div class="register-container">
      <div class="register-form">
        <div class="form-header animate-fade-in">
          <div class="header-icon-wrap">
            <el-icon class="header-icon"><User /></el-icon>
          </div>
          <h1>{{ $t('auth.register.title') }}</h1>
          <p>{{ $t('auth.register.welcome') }}</p>
        </div>
        
        <el-form
          :model="registerForm"
          :rules="registerRules"
          ref="registerFormRef"
          label-width="0"
          size="default"
        >
          <el-form-item prop="username">
            <el-input
              v-model="registerForm.username"
              :placeholder="$t('auth.register.username')"
              prefix-icon="User"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              :placeholder="$t('auth.register.password')"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          
          <el-form-item prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              :placeholder="$t('auth.register.confirmPassword')"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          
          <el-form-item prop="phone">
            <el-input
              v-model="registerForm.phone"
              :placeholder="$t('auth.register.phone')"
              prefix-icon="Phone"
            />
          </el-form-item>

          <el-form-item prop="role">
            <el-select 
              v-model="registerForm.role" 
              :placeholder="$t('auth.register.selectRole')"
              style="width: 100%"
            >
              <el-option :label="$t('auth.register.tourist')" value="TOURIST" />
              <el-option :label="$t('auth.register.merchant')" value="MERCHANT" />
            </el-select>
          </el-form-item>

          <!-- 商家分类选择 -->
          <el-form-item 
            v-if="registerForm.role === 'MERCHANT'" 
            prop="merchantCategory"
            class="merchant-category-item animate-slide-in"
          >
            <el-select 
              v-model="registerForm.merchantCategory" 
              :placeholder="$t('auth.register.selectMerchantCategory')"
              style="width: 100%"
            >
              <el-option :label="$t('auth.register.foodMerchant')" value="FOOD" />
              <el-option :label="$t('auth.register.ceramicWorkshop')" value="CERAMIC" />
              <el-option :label="$t('auth.register.hotelMerchant')" value="HOTEL" />
              <el-option :label="$t('auth.register.scenicMerchant')" value="SCENIC" />
              <el-option :label="$t('auth.register.other')" value="OTHER" />
            </el-select>
          </el-form-item>
          
          <el-form-item prop="agreement">
            <el-checkbox v-model="registerForm.agreement">
              {{ $t('auth.register.agreement') }}
              <el-button link type="primary" @click="showAgreement = true">{{ $t('auth.register.userAgreement') }}</el-button>
              {{ $t('auth.register.and') }}
              <el-button link type="primary" @click="showPrivacy = true">{{ $t('auth.register.privacyPolicy') }}</el-button>
            </el-checkbox>
          </el-form-item>
          
          <el-form-item>
            <el-button
              type="primary"
              class="register-button"
              :loading="loading"
              @click="handleRegister"
            >
              {{ $t('auth.register.registerButton') }}
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="form-footer">
          <p>{{ $t('auth.register.hasAccount') }} <el-button link type="primary" @click="$router.push('/login')">{{ $t('auth.register.loginNow') }}</el-button></p>
        </div>
      </div>
    </div>

    <!-- 用户协议对话框 -->
    <el-dialog v-model="showAgreement" :title="$t('auth.register.agreementDialog.title')" width="600px">
      <div class="agreement-content">
        <h3>{{ $t('auth.register.agreementDialog.content.title') }}</h3>
        <p>{{ $t('auth.register.agreementDialog.content.p1') }}</p>
        <p>{{ $t('auth.register.agreementDialog.content.p2') }}</p>
        <p>{{ $t('auth.register.agreementDialog.content.p3') }}</p>
        <p>{{ $t('auth.register.agreementDialog.content.p4') }}</p>
        <p>{{ $t('auth.register.agreementDialog.content.p5') }}</p>
      </div>
      <template #footer>
        <el-button @click="showAgreement = false">{{ $t('common.cancel') }}</el-button>
      </template>
    </el-dialog>

    <!-- 隐私政策对话框 -->
    <el-dialog v-model="showPrivacy" :title="$t('auth.register.privacyDialog.title')" width="600px">
      <div class="privacy-content">
        <h3>{{ $t('auth.register.privacyDialog.content.title') }}</h3>
        <p>{{ $t('auth.register.privacyDialog.content.p1') }}</p>
        <p>{{ $t('auth.register.privacyDialog.content.p2') }}</p>
        <p>{{ $t('auth.register.privacyDialog.content.p3') }}</p>
        <p>{{ $t('auth.register.privacyDialog.content.p4') }}</p>
        <p>{{ $t('auth.register.privacyDialog.content.p5') }}</p>
      </div>
      <template #footer>
        <el-button @click="showPrivacy = false">{{ $t('common.cancel') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { User, ArrowRight } from '@element-plus/icons-vue'
import BrandIcon from '../../components/BrandIcon.vue'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()

const goBanner = (key) => {
  const map = {
    recommend: { path: '/recommend' },
    plan: { path: '/plan' },
    scenic: { path: '/recommend', query: { type: 'scenic' } },
    // 评价入口：未登录也能在推荐列表看到评分/评价数
    reviews: { path: '/recommend', query: { type: 'scenic' } }
  }
  const target = map[key]
  if (!target) return
  router.push(target).catch(() => {})
}

const loading = ref(false)
const registerFormRef = ref()
const showAgreement = ref(false)
const showPrivacy = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  role: 'TOURIST',
  merchantCategory: '', // 商家分类
  agreement: false
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error(t('auth.register.validation.passwordMismatch')))
  } else {
    callback()
  }
}

const registerRules = computed(() => ({
  username: [
    { required: true, message: t('auth.register.validation.usernameRequired'), trigger: 'blur' },
    { min: 3, max: 20, message: t('auth.register.validation.usernameLength'), trigger: 'blur' }
  ],
  password: [
    { required: true, message: t('auth.register.validation.passwordRequired'), trigger: 'blur' },
    { min: 6, message: t('auth.register.validation.passwordMinLength'), trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: t('auth.register.validation.confirmPasswordRequired'), trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  phone: [
    { required: true, message: t('auth.register.validation.phoneRequired'), trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: t('auth.register.validation.phoneInvalid'), trigger: 'blur' }
  ],
  role: [
    { required: true, message: t('auth.register.validation.roleRequired'), trigger: 'change' }
  ],
  merchantCategory: [
    { 
      required: true, 
      message: t('auth.register.validation.merchantCategoryRequired'), 
      trigger: 'change',
      validator: (rule, value, callback) => {
        if (registerForm.role === 'MERCHANT' && !value) {
          callback(new Error(t('auth.register.validation.merchantCategoryRequired')))
        } else {
          callback()
        }
      }
    }
  ],
  agreement: [
    { required: true, message: t('auth.register.validation.agreementRequired'), trigger: 'change' }
  ]
}))

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  try {
    await registerFormRef.value.validate()
    loading.value = true
    
    await userStore.registerUser(registerForm)
    
    ElMessage.success(t('auth.register.registerSuccess'))
    router.push('/login')
  } catch (error) {
    if (error.message) {
      ElMessage.error(error.message)
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register {
  min-height: 100vh;
  background: url('/images/backgrounds/banner3.jpg') center/cover no-repeat fixed;
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

/* 动态背景装饰 */
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
.register-container {
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
  max-width: 500px;
  width: 100%;
  min-height: auto;
  transition: box-shadow 0.3s ease;
}

.register-container::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(145deg, rgba(248, 250, 255, 0.80) 0%, rgba(242, 246, 252, 0.45) 100%);
  pointer-events: none;
  z-index: 0;
}

.register-form {
  position: relative;
  z-index: 1;
}

.register-container:hover {
  box-shadow: 
    0 22px 46px -16px rgba(0, 0, 0, 0.10),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
}

.register-form {
  flex: 1;
  padding: 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  max-width: 100%;
  overflow: visible;
}

.form-header {
  text-align: center;
  margin-bottom: 32px;
}

.header-icon-wrap {
  width: 52px;
  height: 52px;
  margin: 0 auto 16px;
  background: linear-gradient(135deg, #409EFF 0%, #4facfe 100%);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.35);
}

.header-icon {
  font-size: 26px;
  color: white;
}

/* 增加表单项之间的间距 */
:deep(.el-form-item) {
  margin-bottom: 20px;
}

/* 商家分类字段额外间距 */
.merchant-category-item {
  margin-bottom: 20px;
}

/* 协议复选框字段间距 */
:deep(.el-form-item:has(.el-checkbox)) {
  margin-bottom: 20px;
}

/* 按钮字段间距 */
:deep(.el-form-item:has(.register-button)) {
  margin-top: 8px;
  margin-bottom: 0;
}

.form-header h1 {
  font-size: 34px;
  color: #303133;
  margin-bottom: 6px;
  font-weight: 650;
  letter-spacing: 0.2px;
  line-height: 1.1;
}

.form-header p {
  color: #606266;
  font-size: 14px;
}

/* 输入框增强 */
:deep(.el-input__wrapper),
:deep(.el-select .el-input__wrapper) {
  border-radius: 12px;
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover),
:deep(.el-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 1px rgba(64, 158, 255, 0.14);
}

.register-button {
  width: 100%;
  height: 42px;
  font-size: 15px;
  border-radius: 10px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.register-button:hover {
  transform: translateY(-1px);
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

.agreement-content,
.privacy-content {
  line-height: 1.6;
}

.agreement-content h3,
.privacy-content h3 {
  margin-bottom: 16px;
  color: #303133;
}

.agreement-content p,
.privacy-content p {
  margin-bottom: 12px;
  color: #606266;
}

@media (max-width: 768px) {
  .register-container {
    flex-direction: column;
    max-width: 420px;
    min-height: auto;
  }
  
  .register-banner {
    display: none;
  }
  
  .register-form {
    padding: 40px 24px;
    max-width: 100%;
  }
  
  .form-header {
    margin-bottom: 24px;
  }
  
  .form-header h1 {
    font-size: 22px;
  }
  
  .header-icon-wrap {
    width: 48px;
    height: 48px;
  }
  
  .header-icon {
    font-size: 24px;
  }
}
</style>
