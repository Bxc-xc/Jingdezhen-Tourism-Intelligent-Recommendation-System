<template>
  <div class="login">
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

    <div class="login-container">
      <div class="login-form">
        <div class="form-header animate-fade-in">
          <div class="header-icon-wrap">
            <el-icon class="header-icon"><User /></el-icon>
          </div>
          <h1>{{ $t('auth.login.title') }}</h1>
          <p>{{ $t('auth.login.welcome') }}</p>
        </div>
        
        <el-form
          :model="loginForm"
          :rules="loginRules"
          ref="loginFormRef"
          label-width="0"
          size="large"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              :placeholder="$t('auth.login.username')"
              prefix-icon="User"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              :placeholder="$t('auth.login.password')"
              prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          
          <el-form-item>
            <div class="form-options">
              <el-checkbox v-model="loginForm.remember">{{ $t('auth.login.remember') }}</el-checkbox>
              <el-button link type="primary" @click="$router.push({ path: '/forgot-password', query: { username: loginForm.username } })">
                {{ $t('auth.login.forgotPassword') }}
              </el-button>
            </div>
          </el-form-item>
          
          <el-form-item>
            <el-button
              type="primary"
              class="login-button"
              :loading="loading"
              @click="handleLogin"
            >
              {{ $t('auth.login.loginButton') }}
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="form-footer">
          <p>{{ $t('auth.login.noAccount') }} <el-button link type="primary" @click="$router.push('/register')">{{ $t('auth.login.registerNow') }}</el-button></p>
        </div>
      </div>
    </div>
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
    scenic: { path: '/recommend', query: { type: 'scenic' } }
  }
  const target = map[key]
  if (!target) return
  router.push(target).catch(() => {})
}

const loading = ref(false)
const loginFormRef = ref()

const loginForm = reactive({
  username: '',
  password: '',
  remember: false
})

const loginRules = computed(() => ({
  username: [
    { required: true, message: t('auth.login.validation.usernameRequired'), trigger: 'blur' }
  ],
  password: [
    { required: true, message: t('auth.login.validation.passwordRequired'), trigger: 'blur' },
    { min: 6, message: t('auth.login.validation.passwordMinLength'), trigger: 'blur' }
  ]
}))

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
    loading.value = true
    
    const credentials = {
      username: loginForm.username.trim(),
      password: loginForm.password
    }
    
    await userStore.loginUser(credentials, loginForm.remember)
    
    ElMessage.success(t('auth.login.loginSuccess'))
    
    // 根据用户角色跳转到对应页面
    console.log('用户角色:', userStore.userRole)
    console.log('用户信息:', userStore.user)
    
    if (userStore.userRole === 'TOURIST') {
      console.log('跳转到游客首页')
      router.push('/').then(() => {
        console.log('游客首页跳转成功')
      }).catch(err => {
        console.error('游客首页跳转失败:', err)
      })
    } else if (userStore.userRole === 'MERCHANT') {
      console.log('跳转到商家首页')
      router.push('/merchant').then(() => {
        console.log('商家首页跳转成功')
      }).catch(err => {
        console.error('商家首页跳转失败:', err)
      })
    } else if (userStore.userRole === 'ADMIN') {
      console.log('跳转到管理员首页')
      router.push('/admin').then(() => {
        console.log('管理员首页跳转成功')
      }).catch(err => {
        console.error('管理员首页跳转失败:', err)
      })
    } else {
      console.log('未知角色，跳转到首页')
      router.push('/').then(() => {
        console.log('默认首页跳转成功')
      }).catch(err => {
        console.error('默认首页跳转失败:', err)
      })
    }
  } catch (error) {
    // 处理登录错误
    console.error('=== 登录错误详情 ===')
    console.error('错误对象:', error)
    console.error('错误消息:', error.message)
    console.error('响应数据:', error.response?.data)
    console.error('状态码:', error.response?.status)
    console.error('==================')
    
    const errorMessage = error.message || t('auth.login.loginFailed')
    
    // 根据错误类型显示不同的提示
    if (errorMessage.includes('用户名') || errorMessage.includes('密码') || errorMessage.includes('错误') || errorMessage.includes('username') || errorMessage.includes('password') || errorMessage.includes('error')) {
      // 如果是admin账号登录失败，提供更详细的提示
      if (loginForm.username.trim().toLowerCase() === 'admin') {
        ElMessage.error({
          message: t('auth.login.adminLoginFailed'),
          duration: 5000
        })
      } else {
        ElMessage.error(errorMessage)
      }
    } else if (errorMessage.includes('网络') || errorMessage.includes('连接') || errorMessage.includes('network') || errorMessage.includes('connection')) {
      ElMessage.error(t('auth.login.networkError'))
    } else {
      ElMessage.error(errorMessage || t('auth.login.loginFailedRetry'))
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login {
  min-height: 100vh;
  background: url('/images/backgrounds/bt8.jpg') center/cover no-repeat fixed;
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

.login-container {
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

.login-container::before {
  content: '';
  position: absolute;
  inset: 0;
  width: 100%;
  background: linear-gradient(145deg, rgba(248, 250, 255, 0.70) 0%, rgba(242, 246, 252, 0.35) 100%);
  pointer-events: none;
  z-index: 0;
}

.login-form {
  position: relative;
  z-index: 1;
}

.login-container:hover {
  box-shadow: 
    0 22px 46px -16px rgba(0, 0, 0, 0.10),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
}

.login-form {
  flex: 1;
  padding: 60px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

/* 书签标签容器，改为顶部标签页样式 */
.bookmark-tags {
  position: absolute;
  top: -36px;
  right: 32px;
  transform: none;
  display: flex;
  flex-direction: row;
  gap: 8px;
  z-index: 0; /* 放在卡片底层 */
}

.bookmark-tag {
  position: relative;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 18px 12px 18px;
  min-width: auto;
  /* 系统主题色：带一点透明度和光泽感 */
  background: linear-gradient(180deg, #5ba7ff 0%, #409EFF 100%);
  color: #fff;
  border-radius: 16px 16px 0 0; /* 更圆润的顶部 */
  cursor: pointer;
  box-shadow: 
    0 -2px 10px rgba(0, 0, 0, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.4); /* 顶部高光 */
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-bottom: none;
  transform-origin: bottom center;
  animation: bookmarkSlideUp 0.5s ease-out forwards;
  animation-delay: calc(0.1s + var(--delay) * 0.1s);
  opacity: 0;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.bookmark-tag::after {
  display: none;
}

.bookmark-tag:hover {
  transform: translateY(-8px);
  background: linear-gradient(180deg, #79bbff 0%, #5ba7ff 100%);
  padding-bottom: 20px;
  box-shadow: 
    0 -6px 16px rgba(64, 158, 255, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
  z-index: 1;
}

.bookmark-icon {
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.25);
  border-radius: 50%;
  color: #fff;
}

.bookmark-text {
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

@keyframes bookmarkSlideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.form-header {
  text-align: center;
  margin-bottom: 40px;
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

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: -5px;
  width: 100%;
}

.form-options :deep(.el-checkbox) {
  margin-right: 0;
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 24px rgba(64, 158, 255, 0.22);
}

.form-footer {
  text-align: center;
  margin-top: 30px;
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
  .login-container {
    flex-direction: column;
    max-width: 400px;
  }
  
  .login-banner {
    display: none;
  }
  
  .login-form {
    padding: 40px 20px;
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
}
</style>
