<template>
  <div class="not-found">
    <div class="not-found-content">
      <div class="error-code">404</div>
      <div class="error-message">{{ $t('notFound.title') }}</div>
      <div class="error-description">
        {{ $t('notFound.description') }}
      </div>
      <div class="error-actions">
        <el-button type="primary" @click="goHome">{{ $t('notFound.goHome') }}</el-button>
        <el-button @click="goBack">{{ $t('notFound.goBack') }}</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  router.go(-1)
}
</script>

<style scoped>
.not-found {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}

.not-found-content {
  text-align: center;
  padding: 40px;
}

.error-code {
  font-size: 120px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 20px;
  line-height: 1;
}

.error-message {
  font-size: 32px;
  color: #303133;
  margin-bottom: 16px;
}

.error-description {
  font-size: 16px;
  color: #606266;
  margin-bottom: 32px;
}

.error-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

@media (max-width: 768px) {
  .error-code {
    font-size: 80px;
  }
  
  .error-message {
    font-size: 24px;
  }
  
  .error-actions {
    flex-direction: column;
    align-items: center;
  }
}
</style>