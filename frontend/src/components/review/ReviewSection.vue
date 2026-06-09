<template>
  <div class="review-section">
    <div class="review-section-header">
      <h2>{{ $t('detail.userReviews') }}</h2>
      <span class="review-count-badge" v-if="reviews.length > 0">{{ reviews.length }} 条评价</span>
    </div>

    <!-- 发布评论 -->
    <div class="review-form" v-if="userStore.isLoggedIn">
      <CompositeReviewInput
        v-model:rating="reviewForm.rating"
        v-model:content="reviewForm.content"
        v-model:images="reviewForm.images"
        :placeholder="placeholder"
        :upload-action="`${API_BASE_URL}/upload`"
        :upload-headers="uploadHeaders"
        @submit="handleSubmitReview"
      />
    </div>
    <div v-else class="login-prompt">
      <el-alert :title="$t('detail.loginToReview')" type="info" :closable="false" show-icon />
    </div>

    <!-- 评论列表 -->
    <div class="review-list-wrap" v-loading="loading">
      <div class="review-list">
        <div v-for="review in reviews" :key="review.id" class="review-item">
          <!-- 评论头部 -->
          <div class="review-header">
            <el-avatar :size="40" :src="review.user?.avatar ? normalizeUrl(review.user.avatar) : null">
              {{ review.user?.username?.charAt(0) || 'U' }}
            </el-avatar>
            <div class="review-info">
              <div class="review-user">{{ review.user?.username || $t('detail.anonymous') }}</div>
              <div class="review-meta">
                <el-rate v-model="review.rating" disabled size="small" />
                <span class="review-time">{{ formatTime(review.createTime) }}</span>
              </div>
            </div>
          </div>

          <!-- 评论内容 -->
          <div class="review-content">{{ review.content }}</div>

          <!-- 评论图片 -->
          <div v-if="Array.isArray(review.images) && review.images.length" class="review-images">
            <el-image
              v-for="(img, idx) in review.images"
              :key="idx"
              :src="normalizeUrl(img)"
              :preview-src-list="review.images.map(normalizeUrl)"
              :initial-index="idx"
              fit="cover"
              class="review-image-item"
            />
          </div>

          <!-- 用户操作 -->
          <div class="review-actions" v-if="userStore.isLoggedIn">
            <el-button size="small" link @click="startReplyToReview(review)">
              <el-icon><ChatDotRound /></el-icon>{{ $t('detail.reply') }}
            </el-button>
            <el-button
              v-if="review.user?.id === userStore.user?.id || userStore.userRole === 'ADMIN'"
              size="small" type="danger" link @click="emit('delete-review', review)"
            >{{ $t('common.delete') || '删除' }}</el-button>
          </div>

          <!-- 回复输入框（回复评论） -->
          <div v-if="replyingReviewId === review.id && !replyingReplyId" class="reply-form-inline">
            <el-input
              v-model="replyContent"
              type="textarea"
              :rows="2"
              maxlength="200"
              show-word-limit
              :placeholder="$t('detail.reply') + '...'"
            />
            <div class="reply-actions">
              <el-button size="small" type="primary" @click="submitReplyToReview(review)">{{ $t('common.submit') }}</el-button>
              <el-button size="small" @click="cancelReply">{{ $t('common.cancel') }}</el-button>
            </div>
          </div>

          <!-- 回复列表 -->
          <div v-if="reviewReplies[review.id]?.length" class="replies-container">
            <template v-for="reply in reviewReplies[review.id]" :key="reply.id">
              <!-- 商家回复 - 特殊样式 -->
              <div v-if="isMerchantReply(reply)" class="reply-item merchant-reply">
                <div class="merchant-reply-badge">
                  <el-icon><Shop /></el-icon>
                  <span>商家回复</span>
                </div>
                <div class="reply-header-info">
                  <el-avatar :size="32" class="merchant-avatar">
                    {{ reply.merchant?.shopName?.charAt(0) || 'M' }}
                  </el-avatar>
                  <div class="reply-info">
                    <div class="reply-user">
                      <span class="replier-name merchant-name">{{ reply.merchant?.shopName || '商家' }}</span>
                      <span class="reply-time">{{ formatTime(reply.createTime) }}</span>
                    </div>
                    <div class="reply-content-text merchant-reply-text">{{ reply.content }}</div>
                  </div>
                </div>
              </div>

              <!-- 普通用户回复 -->
              <div v-else class="reply-item">
                <div class="reply-header-info">
                  <el-avatar :size="32" :src="reply.user?.avatar ? normalizeUrl(reply.user.avatar) : null">
                    {{ reply.user?.username?.charAt(0) || 'U' }}
                  </el-avatar>
                  <div class="reply-info">
                    <div class="reply-user">
                      <span class="replier-name">{{ reply.user?.username || '用户' }}</span>
                      <span class="reply-time">{{ formatTime(reply.createTime) }}</span>
                    </div>
                    <div class="reply-content-text">{{ reply.content }}</div>
                  </div>
                </div>
                <div class="reply-actions-inline" v-if="userStore.isLoggedIn">
                  <el-button size="small" link @click="startReplyToReply(review, reply)">{{ $t('detail.reply') }}</el-button>
                </div>
                <div v-if="replyingReplyId === reply.id" class="reply-form-inline nested">
                  <div class="reply-to-hint">{{ $t('detail.replyTo') }}{{ reply.user?.username }}：</div>
                  <el-input v-model="replyContent" type="textarea" :rows="2" maxlength="200" show-word-limit />
                  <div class="reply-actions">
                    <el-button size="small" type="primary" @click="submitReplyToReply(review, reply)">{{ $t('common.submit') }}</el-button>
                    <el-button size="small" @click="cancelReply">{{ $t('common.cancel') }}</el-button>
                  </div>
                </div>

                <!-- 子回复 -->
                <div v-if="reply.childReplies?.length" class="child-replies">
                  <div v-for="child in reply.childReplies" :key="child.id" class="child-reply-item">
                    <el-avatar :size="26" :src="child.user?.avatar ? normalizeUrl(child.user.avatar) : null">
                      {{ (child.merchant?.shopName || child.user?.username || (isMerchantReply(child) ? '商' : 'U'))?.charAt(0) || 'U' }}
                    </el-avatar>
                    <div class="child-reply-info">
                      <div class="child-reply-user">
                        <span class="replier-name" :class="{ 'merchant-name': isMerchantReply(child) }">
                          {{ child.merchant?.shopName || child.user?.username || (isMerchantReply(child) ? '商家' : '用户') }}
                        </span>
                        <el-tag v-if="isMerchantReply(child)" class="merchant-tag" size="small">商家</el-tag>
                        <span class="reply-time">{{ formatTime(child.createTime) }}</span>
                      </div>
                      <div class="child-reply-content" :class="{ 'merchant-reply-text': isMerchantReply(child) }">{{ child.content }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </template>
          </div>
        </div>

        <el-empty v-if="!loading && reviews.length === 0" :description="$t('detail.noReviews')" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Shop } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'
import CompositeReviewInput from './CompositeReviewInput.vue'
import { getRepliesByReviewId, createUserReply, createUserReplyToReply } from '../../api/reply'

const { t } = useI18n()
const userStore = useUserStore()

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

const props = defineProps({
  reviews: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  placeholder: { type: String, default: '分享你的体验...' },
  // 用于提交评论的函数，由父组件传入
  onSubmitReview: { type: Function, default: null },
  /** 为 true 时由父组件通过 externalRepliesMap 提供嵌套回复，不再逐条请求 */
  embedReplies: { type: Boolean, default: false },
  /** reviewId -> 回复树（与 /comment/list 或 /replies/review/:id 结构一致） */
  externalRepliesMap: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['submit-review', 'delete-review', 'refresh', 'replies-updated'])

const reviewForm = ref({ rating: 5, content: '', images: [] })
const replyingReviewId = ref(null)
const replyingReplyId = ref(null)
const replyContent = ref('')
const reviewReplies = ref({})

const uploadHeaders = ref({
  Authorization: `Bearer ${localStorage.getItem('token') || sessionStorage.getItem('token') || ''}`
})

const normalizeReplyTree = (list) => {
  if (!Array.isArray(list)) return []
  return list.map((r) => ({
    ...r,
    childReplies: normalizeReplyTree(r.childReplies || [])
  }))
}

const isMerchantReply = (reply) =>
  !!(reply?.merchant || String(reply?.role || '').toLowerCase() === 'merchant')

// 嵌入模式：从父级 map 同步；否则按评论 id 拉取回复树
watch(
  () => ({ embed: props.embedReplies, revs: props.reviews, map: props.externalRepliesMap }),
  async ({ embed, revs, map }) => {
    if (embed) {
      const next = {}
      for (const review of revs) {
        const raw = map[review.id]
        next[review.id] = normalizeReplyTree(Array.isArray(raw) ? raw : [])
      }
      reviewReplies.value = next
      return
    }
    for (const review of revs) {
      if (!reviewReplies.value[review.id]) {
        await loadRepliesForReview(review.id)
      }
    }
  },
  { immediate: true, deep: true }
)

const loadRepliesForReview = async (reviewId) => {
  try {
    const res = await getRepliesByReviewId(reviewId)
    const raw = res?.data || res || []
    const tree = normalizeReplyTree(Array.isArray(raw) ? raw : [])
    reviewReplies.value = { ...reviewReplies.value, [reviewId]: tree }
    if (props.embedReplies) {
      emit('replies-updated', { reviewId, replies: tree })
    }
  } catch {
    reviewReplies.value = { ...reviewReplies.value, [reviewId]: [] }
  }
}

const handleSubmitReview = async (data) => {
  emit('submit-review', data)
  reviewForm.value = { rating: 5, content: '', images: [] }
}

const startReplyToReview = (review) => {
  replyingReviewId.value = review.id
  replyingReplyId.value = null
  replyContent.value = ''
}

const startReplyToReply = (review, reply) => {
  replyingReviewId.value = review.id
  replyingReplyId.value = reply.id
  replyContent.value = ''
}

const cancelReply = () => {
  replyingReviewId.value = null
  replyingReplyId.value = null
  replyContent.value = ''
}

const submitReplyToReview = async (review) => {
  if (!replyContent.value.trim()) return ElMessage.warning('请输入回复内容')
  try {
    await createUserReply(review.id, userStore.user.id, replyContent.value.trim())
    ElMessage.success('回复成功')
    await loadRepliesForReview(review.id)
    cancelReply()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '回复失败')
  }
}

const submitReplyToReply = async (review, parentReply) => {
  if (!replyContent.value.trim()) return ElMessage.warning('请输入回复内容')
  try {
    await createUserReplyToReply(review.id, parentReply.id, userStore.user.id, replyContent.value.trim())
    ElMessage.success('回复成功')
    await loadRepliesForReview(review.id)
    cancelReply()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '回复失败')
  }
}

const normalizeUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) {
    try {
      const parsed = new URL(url)
      const host = parsed.hostname
      if (host === 'localhost' || host === '127.0.0.1' || host === '0.0.0.0') {
        return parsed.pathname
      }
    } catch {}
    return url
  }
  // /uploads/ 路径直接走站点根路径（Vite 代理已配置），不能拼 /api 前缀
  if (url.startsWith('/uploads/') || url.startsWith('uploads/')) {
    return url.startsWith('/') ? url : `/${url}`
  }
  return `${API_BASE_URL}${url.startsWith('/') ? '' : '/'}${url}`
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}

// 暴露给父组件，用于手动刷新某条评论的回复
defineExpose({ loadRepliesForReview, reviewReplies })
</script>

<style scoped>
/* ===== 区块头部 ===== */
.review-section {
  border-top: 1px solid #eef1f6;
  padding-top: 36px;
}

.review-section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 22px;
}

.review-section-header h2 {
  margin: 0;
  color: #111827;
  font-size: 22px;
  font-weight: 700;
}

.review-count-badge {
  background: #f0f4ff;
  color: #4f6ef7;
  font-size: 13px;
  font-weight: 600;
  padding: 2px 10px;
  border-radius: 20px;
}

/* ===== 发布表单 ===== */
.review-form {
  background: linear-gradient(180deg, rgba(0,0,0,0.02), rgba(0,0,0,0.03));
  padding: 18px;
  border-radius: 14px;
  margin-bottom: 26px;
  border: 1px solid rgba(0,0,0,0.06);
}

.login-prompt {
  margin-bottom: 26px;
}

/* ===== 评论列表容器 ===== */
.review-list-wrap {
  overflow-y: visible;
  padding-right: 4px;
}


.review-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ===== 单条评论 ===== */
.review-item {
  padding: 16px 0 20px;
  border-bottom: 1px solid #f2f4f7;
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  gap: 12px;
  margin-bottom: 10px;
}

.review-info {
  flex: 1;
}

.review-user {
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
  font-size: 15px;
}

.review-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.review-time {
  color: #9ca3af;
  font-size: 12px;
}

.review-content {
  color: #374151;
  line-height: 1.7;
  font-size: 15px;
  margin-bottom: 10px;
}

.review-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}

.review-image-item {
  width: 100px;
  height: 100px;
  border-radius: 10px;
  object-fit: cover;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.review-actions {
  margin-top: 6px;
  display: flex;
  gap: 4px;
}

/* ===== 回复输入框 ===== */
.reply-form-inline {
  margin-top: 12px;
  background: #f9fafb;
  padding: 12px;
  border-radius: 8px;
}

.reply-form-inline.nested {
  margin-left: 44px;
}

.reply-actions {
  margin-top: 8px;
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.reply-to-hint {
  font-size: 12px;
  color: #4f6ef7;
  margin-bottom: 6px;
  font-weight: 500;
}

/* ===== 回复列表容器 ===== */
.replies-container {
  margin-top: 14px;
  margin-left: 52px;
  border-left: 2px solid #e8ecf4;
  padding-left: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* ===== 普通回复 ===== */
.reply-item {
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
}

.reply-item:last-child {
  border-bottom: none;
}

.reply-header-info {
  display: flex;
  gap: 10px;
}

.reply-info {
  flex: 1;
}

.reply-user {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.replier-name {
  font-weight: 600;
  color: #374151;
  font-size: 13px;
}

.reply-content-text {
  color: #6b7280;
  line-height: 1.6;
  font-size: 13px;
}

.reply-actions-inline {
  margin-top: 6px;
  margin-left: 42px;
}

/* ===== 商家回复 - 特殊样式 ===== */
.merchant-reply {
  background: linear-gradient(135deg, #fff7ed 0%, #eff6ff 100%);
  border-radius: 10px;
  padding: 12px 14px !important;
  border-bottom: none !important;
  margin-bottom: 8px;
  border: 1px solid #fdba74;
}

.merchant-reply-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: #ea580c;
  font-size: 11px;
  font-weight: 700;
  margin-bottom: 8px;
  letter-spacing: 0.3px;
  padding: 2px 8px;
  border-radius: 6px;
  background: rgba(234, 88, 12, 0.08);
}

.merchant-reply-badge .el-icon {
  font-size: 13px;
}

.merchant-avatar {
  background: linear-gradient(135deg, #3b82f6, #2563eb) !important;
  color: white !important;
}

.merchant-name {
  color: #1d4ed8 !important;
  font-weight: 700 !important;
}

.merchant-reply-text {
  color: #1e3a8a !important;
  font-size: 12px !important;
  line-height: 1.65 !important;
}

.merchant-tag {
  background: #dbeafe !important;
  color: #1d4ed8 !important;
  border-color: #bfdbfe !important;
  font-size: 11px;
}

/* ===== 子回复 ===== */
.child-replies {
  margin-top: 10px;
  margin-left: 42px;
  padding-left: 14px;
  border-left: 2px solid #f0f0f0;
}

.child-reply-item {
  display: flex;
  gap: 8px;
  padding: 8px 0;
  border-bottom: 1px solid #f9fafb;
}

.child-reply-item:last-child {
  border-bottom: none;
}

.child-reply-info {
  flex: 1;
}

.child-reply-user {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 3px;
}

.child-reply-content {
  color: #6b7280;
  font-size: 13px;
  line-height: 1.5;
}
</style>
