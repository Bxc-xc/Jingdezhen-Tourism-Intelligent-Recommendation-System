<template>
  <div class="composite-review-input">
    <!-- 评分 -->
    <div class="rating-section">
      <span class="rating-label">评分</span>
      <el-rate v-model="localRating" show-text :texts="['极差', '失望', '一般', '满意', '惊喜']" />
    </div>
    
    <!-- 复合输入框：文本 + 图片上传 + 预览 全部在一个容器内 -->
    <div class="composite-input-box">
      <!-- 文本输入区 -->
      <textarea
        v-model="localContent"
        :placeholder="placeholder"
        maxlength="500"
        class="composite-textarea"
        @input="handleInput"
      ></textarea>
      
      <!-- 图片预览区（在输入框内部） -->
      <div v-if="localImages.length > 0" class="inner-image-preview">
        <div 
          v-for="(img, idx) in localImages" 
          :key="idx" 
          class="preview-thumb"
        >
          <img :src="img" alt="预览图" />
          <button type="button" class="thumb-remove" @click="removeImage(idx)">
            <el-icon><Close /></el-icon>
          </button>
        </div>
      </div>
      
      <!-- 工具栏（在输入框内部底部） -->
      <div class="inner-toolbar">
        <div class="toolbar-left">
          <el-upload
            :action="uploadAction"
            name="file"
            multiple
            :limit="6"
            :headers="uploadHeaders"
            :before-upload="beforeUpload"
            :on-success="onUploadSuccess"
            accept=".jpg,.jpeg,.png,.gif,.webp"
            :show-file-list="false"
            class="inner-upload"
          >
            <button type="button" class="upload-btn">
              <el-icon><Picture /></el-icon>
              <span>上传图片</span>
            </button>
          </el-upload>
          <span v-if="localImages.length > 0" class="upload-count">
            {{ localImages.length }}/6
          </span>
        </div>
        <div class="toolbar-right">
          <span class="char-count">{{ localContent.length }}/500</span>
        </div>
      </div>
    </div>
    
    <!-- 提交按钮 -->
    <div class="submit-section">
      <el-button type="primary" @click="handleSubmit" :disabled="!canSubmit">
        发布评价
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Close, Picture } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  rating: { type: Number, default: 5 },
  content: { type: String, default: '' },
  images: { type: Array, default: () => [] },
  placeholder: { type: String, default: '分享你的入住体验...' },
  uploadAction: { type: String, required: true },
  uploadHeaders: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:rating', 'update:content', 'update:images', 'submit'])

const localRating = ref(props.rating)
const localContent = ref(props.content)
const localImages = ref([...props.images])

watch(() => props.rating, (val) => { localRating.value = val })
watch(() => props.content, (val) => { localContent.value = val })
watch(() => props.images, (val) => { localImages.value = [...val] })

const canSubmit = computed(() => {
  return localContent.value.trim().length > 0
})

const handleInput = () => {
  emit('update:content', localContent.value)
}

const removeImage = (index) => {
  localImages.value.splice(index, 1)
  emit('update:images', localImages.value)
}

const beforeUpload = (file) => {
  const isImage = file.type?.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB')
    return false
  }
  if (localImages.value.length >= 6) {
    ElMessage.warning('最多上传6张图片')
    return false
  }
  return true
}

const onUploadSuccess = (res) => {
  if (res && res.success && res.url) {
    localImages.value.push(res.url)
    emit('update:images', localImages.value)
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(res?.message || '图片上传失败')
  }
}

const handleSubmit = () => {
  if (!canSubmit.value) {
    ElMessage.warning('请输入评价内容')
    return
  }
  emit('submit', {
    rating: localRating.value,
    content: localContent.value,
    images: localImages.value
  })
}
</script>

<style scoped>
.composite-review-input {
  width: 100%;
}

/* 评分区域 */
.rating-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.rating-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

/* 复合输入框容器 - 模拟一个完整的输入框 */
.composite-input-box {
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  background: #ffffff;
  overflow: hidden;
  transition: all 0.2s;
  display: flex;
  flex-direction: column;
  margin-bottom: 16px;
}

.composite-input-box:hover {
  border-color: #c0c4cc;
}

.composite-input-box:focus-within {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

/* 文本输入区 - 无边框，融入容器 */
.composite-textarea {
  width: 100%;
  min-height: 120px;
  padding: 12px 16px;
  border: none;
  outline: none;
  resize: vertical;
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
  font-family: inherit;
  background: transparent;
}

.composite-textarea::placeholder {
  color: #c0c4cc;
}

/* 图片预览区 - 在输入框内部 */
.inner-image-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 0 16px 12px 16px;
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
}

.preview-thumb {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #e4e7ed;
  background: #f5f7fa;
  flex-shrink: 0;
}

.preview-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumb-remove {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  color: #ffffff;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  transition: all 0.2s;
  padding: 0;
}

.thumb-remove:hover {
  background: rgba(0, 0, 0, 0.8);
  transform: scale(1.1);
}

.thumb-remove .el-icon {
  font-size: 12px;
}

/* 内部工具栏 - 在输入框底部 */
.inner-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.toolbar-right {
  display: flex;
  align-items: center;
}

/* 上传按钮 - 在工具栏内 */
.inner-upload {
  display: inline-block;
}

.upload-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #ffffff;
  color: #606266;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.upload-btn:hover {
  color: #409eff;
  border-color: #409eff;
  background: #ecf5ff;
}

.upload-btn .el-icon {
  font-size: 16px;
}

.upload-count {
  font-size: 12px;
  color: #909399;
  padding: 4px 8px;
  background: #f0f0f0;
  border-radius: 4px;
  font-weight: 500;
}

.char-count {
  font-size: 12px;
  color: #909399;
}

/* 提交区域 */
.submit-section {
  display: flex;
  justify-content: flex-end;
}

/* 响应式 */
@media (max-width: 768px) {
  .composite-textarea {
    min-height: 100px;
  }
  
  .preview-thumb {
    width: 70px;
    height: 70px;
  }
  
  .inner-toolbar {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .toolbar-right {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
