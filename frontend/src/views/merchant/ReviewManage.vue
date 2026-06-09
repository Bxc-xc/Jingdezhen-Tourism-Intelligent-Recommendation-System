<template>
  <div class="review-manage">
    <!-- 返回按钮 -->
    <div class="back-button-container">
      <el-button @click="goBack" :icon="ArrowLeft" circle />
    </div>
    
    <div class="page-header">
      <h2>评论管理</h2>
      <div class="header-actions">
        <el-button @click="refreshReviews">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button type="primary" @click="exportReviews">
          <el-icon><Download /></el-icon>
          导出评价
        </el-button>
      </div>
    </div>

    <!-- 评价统计 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon total">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ reviewStats.total }}</div>
              <div class="stat-label">总评价数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon average">
              <el-icon><Star /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ reviewStats.average }}</div>
              <div class="stat-label">平均评分</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon replied">
              <el-icon><Check /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ reviewStats.replied }}</div>
              <div class="stat-label">已回复</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon pending">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ reviewStats.pending }}</div>
              <div class="stat-label">待回复</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :model="filterForm" inline>
        <el-form-item label="评分">
          <el-select 
            v-model="filterForm.rating" 
            placeholder="全部评分" 
            clearable
            :popper-options="{
              strategy: 'fixed',
              modifiers: [
                { name: 'offset', options: { offset: [0, 4] } },
                { name: 'computeStyles', options: { adaptive: true, gpuAcceleration: true } }
              ]
            }"
          >
            <el-option label="全部" value="" />
            <el-option label="5星" value="5" />
            <el-option label="4星" value="4" />
            <el-option label="3星" value="3" />
            <el-option label="2星" value="2" />
            <el-option label="1星" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="回复状态">
          <el-select 
            v-model="filterForm.replied" 
            placeholder="全部状态" 
            clearable
            :popper-options="{
              strategy: 'fixed',
              modifiers: [
                { name: 'offset', options: { offset: [0, 4] } },
                { name: 'computeStyles', options: { adaptive: true, gpuAcceleration: true } }
              ]
            }"
          >
            <el-option label="全部" value="" />
            <el-option label="已回复" value="true" />
            <el-option label="未回复" value="false" />
          </el-select>
        </el-form-item>
        <el-form-item label="评价时间">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :popper-options="{
              strategy: 'fixed',
              modifiers: [
                { name: 'offset', options: { offset: [0, 4] } },
                { name: 'computeStyles', options: { adaptive: true, gpuAcceleration: true } }
              ]
            }"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="filterForm.keyword" placeholder="搜索评价内容" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">筛选</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 评价列表 -->
    <el-card class="reviews-card">
      <template #header>
        <span>评价列表</span>
        <span class="review-count">共 {{ filteredReviews.length }} 条评价</span>
      </template>
      
      <div class="review-list" v-loading="loading">
        <div v-for="review in paginatedReviews" :key="review.id" class="review-item">
          <div class="review-header">
            <div class="user-info">
              <el-avatar :size="40">{{ review.user?.username?.charAt(0) || 'U' }}</el-avatar>
              <div class="user-details">
                <div class="username">{{ review.user?.username || '匿名用户' }}</div>
                <div class="review-time">{{ formatDate(review.createTime) }}</div>
              </div>
            </div>
            <div class="review-rating">
              <el-rate v-model="review.rating" disabled size="small" />
              <span class="rating-text">{{ review.rating }}分</span>
            </div>
          </div>
          
          <div class="review-content">
            <p>{{ review.content }}</p>
            <div v-if="review.images && review.images.length > 0" class="review-images">
              <el-image
                v-for="(image, index) in review.images"
                :key="index"
                :src="normalizeUrl(image)"
                :preview-src-list="review.images.map(normalizeUrl)"
                class="review-image"
                fit="cover"
              />
            </div>
          </div>
          
          <!-- 回复列表 -->
          <div v-if="reviewReplies[review.id] && reviewReplies[review.id].length > 0" class="replies-container">
            <div
              v-for="reply in reviewReplies[review.id]"
              :key="reply.id"
              :class="['reply-item', isMerchantReply(reply) ? 'merchant-reply-block' : '']"
            >
              <div v-if="isMerchantReply(reply)" class="merchant-reply-label">商家回复</div>
              <div class="reply-header-info">
                <el-avatar :size="32">{{ getReplierName(reply)?.charAt(0) || 'U' }}</el-avatar>
                <div class="reply-info">
                  <div class="reply-user">
                    <span class="replier-name" :class="{ 'merchant-replier': isMerchantReply(reply) }">{{ getReplierName(reply) }}</span>
                    <el-tag v-if="isMerchantReply(reply)" type="warning" size="small">商家</el-tag>
                    <span class="reply-time">{{ formatDate(reply.createTime) }}</span>
                  </div>
                  <div class="reply-content-text" :class="{ 'merchant-reply-body': isMerchantReply(reply) }">{{ reply.content }}</div>
                </div>
              </div>
              <div class="reply-actions-inline">
                <el-button size="small" link @click="startReplyToReply(review, reply)">回复</el-button>
                <el-button v-if="isMerchantReply(reply)" size="small" type="danger" link @click="handleDeleteReply(review, reply)">删除</el-button>
              </div>
              <div v-if="editingReplyId === reply.id" class="reply-form-inline nested">
                <el-input
                  v-model="editingReplyContent"
                  type="textarea"
                  :rows="2"
                  maxlength="200"
                  show-word-limit
                  placeholder="输入回复内容..."
                />
                <div class="reply-actions">
                  <el-button size="small" @click="cancelEdit">取消</el-button>
                  <el-button size="small" type="primary" @click="saveReplyToReply(review, reply)">发送</el-button>
                </div>
              </div>
              
              <!-- 子回复（回复的回复） -->
              <div v-if="reply.childReplies && reply.childReplies.length > 0" class="child-replies">
                <div v-for="childReply in reply.childReplies" :key="childReply.id" class="child-reply-item">
                  <el-avatar :size="28">{{ getReplierName(childReply)?.charAt(0) || 'U' }}</el-avatar>
                  <div class="child-reply-info">
                    <div class="child-reply-user">
                      <span class="replier-name" :class="{ 'merchant-replier': isMerchantReply(childReply) }">{{ getReplierName(childReply) }}</span>
                      <el-tag v-if="isMerchantReply(childReply)" type="warning" size="small">商家</el-tag>
                      <span class="reply-time">{{ formatDate(childReply.createTime) }}</span>
                    </div>
                    <div class="child-reply-content" :class="{ 'merchant-reply-body': isMerchantReply(childReply) }">{{ childReply.content }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 内联编辑区域（回复评论） -->
          <div v-if="editingReviewId === review.id && !editingReplyId" class="reply-edit-area">
            <div class="edit-header">
              <span>回复评价</span>
            </div>
            <el-input
              v-model="editingReplyContent"
              type="textarea"
              :rows="4"
              placeholder="请输入回复内容..."
              maxlength="500"
              show-word-limit
              class="reply-input"
            />
            <div class="edit-actions">
              <el-button size="small" @click="cancelEdit">取消</el-button>
              <el-button size="small" type="primary" @click="saveReplyToReview(review)">保存</el-button>
            </div>
          </div>
          
          <div class="review-actions">
            <el-button
              v-if="editingReviewId !== review.id"
              size="small"
              type="primary"
              @click="startEdit(review)"
            >
              回复
            </el-button>
            <el-button
              v-if="editingReviewId !== review.id"
              size="small"
              type="danger"
              @click="deleteReview(review.id)"
            >
              删除
            </el-button>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="filteredReviews.length"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'
import { getMerchantByUserId } from '../../api/merchant'
import { getMerchantReviewList, deleteReview as deleteReviewApi } from '../../api/review'
import { getRepliesByReviewId } from '../../api/reply'
import { postCommentReply } from '../../api/comment'
import { deleteMerchantReply } from '../../api/reply'
import realtimeSync from '../../utils/websocket'

const router = useRouter()
const userStore = useUserStore()
const merchantInfo = ref(null)

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

// 将后端 images 字段（字符串/数组）统一转为数组
const toImageArray = (images) => {
  if (!images) return []
  if (Array.isArray(images)) return images.filter(Boolean)
  if (typeof images === 'string') {
    try {
      const parsed = JSON.parse(images)
      if (Array.isArray(parsed)) return parsed.filter(Boolean)
    } catch {}
    return images.split(',').map(s => s.trim()).filter(Boolean)
  }
  return []
}

// 处理 localhost 绝对路径，走 Vite 代理
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
  if (url.startsWith('/uploads/') || url.startsWith('uploads/')) {
    return url.startsWith('/') ? url : `/${url}`
  }
  return `${API_BASE_URL}${url.startsWith('/') ? '' : '/'}${url}`
}

const goBack = () => {
  router.go(-1)
}

const loading = ref(false)
const editingReviewId = ref(null)
const editingReplyId = ref(null)
const editingReplyContent = ref('')
const reviewReplies = ref({}) // 存储每个评论的回复列表 { reviewId: [replies] }

const currentPage = ref(1)
const pageSize = ref(10)

const filterForm = reactive({
  rating: '',
  replied: '',
  dateRange: [],
  keyword: ''
})

const reviewStats = ref({
  total: 48,
  average: 4.6,
  replied: 35,
  pending: 13
})

const loadReviews = async () => {
  if (!merchantInfo.value?.id) {
    ElMessage.warning('无法获取商家信息')
    return
  }
  
  loading.value = true
  try {
    const response = await getMerchantReviewList(merchantInfo.value.id)
    const data = response?.data?.data || response?.data || []
    reviews.value = Array.isArray(data) ? data.map(r => ({ ...r, images: toImageArray(r.images) })) : []
    
    // 为每个评论加载回复列表
    for (const review of reviews.value) {
      await loadRepliesForReview(review.id)
    }
    
    // 更新统计数据
    updateReviewStats()
  } catch (error) {
    console.error('加载评价失败:', error)
    ElMessage.error('加载评价失败')
  } finally {
    loading.value = false
  }
}

const normalizeReplyTree = (list) => {
  if (!Array.isArray(list)) return []
  return list.map((r) => ({
    ...r,
    childReplies: normalizeReplyTree(r.childReplies || [])
  }))
}

const isMerchantReply = (reply) =>
  !!(reply?.merchant || String(reply?.role || '').toLowerCase() === 'merchant')

const loadRepliesForReview = async (reviewId) => {
  try {
    const response = await getRepliesByReviewId(reviewId)
    const replies = response?.data ?? response ?? []
    reviewReplies.value = {
      ...reviewReplies.value,
      [reviewId]: normalizeReplyTree(Array.isArray(replies) ? replies : [])
    }
  } catch (error) {
    console.error('加载回复失败:', error)
    reviewReplies.value = { ...reviewReplies.value, [reviewId]: [] }
  }
}

const updateReviewStats = () => {
  const total = reviews.value.length
  // 统计有回复的评论数（包括多级回复）
  const replied = reviews.value.filter(r => {
    const replies = reviewReplies.value[r.id] || []
    return replies.length > 0
  }).length
  const pending = total - replied
  
  // 计算平均评分
  const avgRating = reviews.value.length > 0
    ? (reviews.value.reduce((sum, r) => sum + (r.rating || 0), 0) / reviews.value.length).toFixed(1)
    : '0.0'
  
  reviewStats.value = {
    total,
    average: avgRating,
    replied,
    pending
  }
}

const reviews = ref([])

const filteredReviews = computed(() => {
  let filtered = reviews.value

  if (filterForm.rating) {
    filtered = filtered.filter(review => review.rating === parseInt(filterForm.rating))
  }

  if (filterForm.replied !== '') {
    const isReplied = filterForm.replied === 'true'
    filtered = filtered.filter(review => {
      const replies = reviewReplies.value[review.id] || []
      return (replies.length > 0) === isReplied
    })
  }

  if (filterForm.keyword) {
    filtered = filtered.filter(review =>
      review.content?.includes(filterForm.keyword) ||
      review.user?.username?.includes(filterForm.keyword)
    )
  }

  if (filterForm.dateRange && filterForm.dateRange.length === 2) {
    const [startDate, endDate] = filterForm.dateRange
    filtered = filtered.filter(review => {
      const reviewDate = new Date(review.createTime).toISOString().split('T')[0]
      return reviewDate >= startDate && reviewDate <= endDate
    })
  }

  return filtered
})

const paginatedReviews = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredReviews.value.slice(start, end)
})

const handleFilter = () => {
  currentPage.value = 1
}

const resetFilter = () => {
  Object.assign(filterForm, {
    rating: '',
    replied: '',
    dateRange: [],
    keyword: ''
  })
  currentPage.value = 1
}

const refreshReviews = async () => {
  loading.value = true
  try {
    await loadReviews()
    ElMessage.success('刷新成功')
  } catch (error) {
    console.error('刷新失败:', error)
    ElMessage.error('刷新失败')
  } finally {
    loading.value = false
  }
}

const exportReviews = () => {
  ElMessage.success('导出功能开发中...')
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val) => {
  currentPage.value = val
}

const startEdit = (review) => {
  editingReviewId.value = review.id
  editingReplyId.value = null
  editingReplyContent.value = ''
}

const startReplyToReply = (review, reply) => {
  editingReviewId.value = review.id
  editingReplyId.value = reply.id
  editingReplyContent.value = ''
}

const cancelEdit = () => {
  editingReviewId.value = null
  editingReplyId.value = null
  editingReplyContent.value = ''
}

const saveReplyToReview = async (review) => {
  if (!editingReplyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }

  try {
    await postCommentReply({
      reviewId: review.id,
      content: editingReplyContent.value.trim()
    })
    ElMessage.success('回复成功')
    await loadRepliesForReview(review.id)
    updateReviewStats()
  } catch (error) {
    console.error('回复失败:', error)
    ElMessage.error(error?.response?.data?.message || error?.message || '回复失败')
  }

  cancelEdit()
}

const saveReplyToReply = async (review, parentReply) => {
  if (!editingReplyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }

  try {
    await postCommentReply({
      reviewId: review.id,
      parentReplyId: parentReply.id,
      content: editingReplyContent.value.trim()
    })
    ElMessage.success('回复成功')
    await loadRepliesForReview(review.id)
  } catch (error) {
    console.error('回复失败:', error)
    ElMessage.error(error?.response?.data?.message || error?.message || '回复失败')
  }

  cancelEdit()
}

const handleDeleteReply = async (review, reply) => {
  try {
    await ElMessageBox.confirm('确认删除这条商家回复吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteMerchantReply(reply.id)
    ElMessage.success('删除成功')
    await loadRepliesForReview(review.id)
    updateReviewStats()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e?.response?.data?.message || '删除失败')
    }
  }
}

const getReplierName = (reply) => {
  if (isMerchantReply(reply)) {
    return reply.merchant?.shopName || '商家'
  }
  if (reply.user) {
    return reply.user.username || '匿名用户'
  }
  return '匿名用户'
}

const deleteReview = async (reviewId) => {
  try {
    await ElMessageBox.confirm('确认要删除这条评价吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await deleteReviewApi(reviewId)

    // 同样，这里 response 是 { success, message, data }
    if (response && response.success) {
      const review = reviews.value.find(r => r.id === reviewId)
      if (review) {
        if (review.replyContent) {
          reviewStats.value.replied--
        } else {
          reviewStats.value.pending--
        }
        reviewStats.value.total--
      }

      reviews.value = reviews.value.filter(r => r.id !== reviewId)
      ElMessage.success('删除成功')
    } else {
      ElMessage.error(response?.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(error?.response?.data?.message || '删除失败')
    }
  }
}

const formatDate = (date) => {
  return new Date(date).toLocaleString()
}

const merchantScenicSpotId = () =>
  merchantInfo.value?.scenicSpotId ?? merchantInfo.value?.scenicSpot?.id ?? null

const handleReviewRealtime = (updateInfo) => {
  const { data: d } = updateInfo
  if (!merchantInfo.value?.id) return
  // 有新评论或新回复时刷新列表
  const mid = d?.merchantId
  if (!mid || String(mid) === String(merchantInfo.value.id)) {
    loadReviews()
  }
}

const handleMerchantRealtime = (updateInfo) => {
  const { operation, data: d } = updateInfo
  if (operation !== 'update' || !merchantInfo.value?.id) return
  if (String(d?.id) === String(merchantInfo.value.id)) {
    loadReviews()
  }
}

const handleScenicRealtime = (updateInfo) => {
  const { operation, data: d } = updateInfo
  if (operation !== 'update' || d?.id == null) return
  const sid = merchantScenicSpotId()
  if (sid != null && String(d.id) === String(sid)) {
    loadReviews()
  }
}

const loadMerchantInfo = async () => {
  try {
    if (!userStore.user?.id) return
    const response = await getMerchantByUserId(userStore.user.id)

    // request 封装会在后端返回 { success, data } 时直接返回这一层
    // 因此这里应直接使用 response.success / response.data
    if (response && response.success && response.data) {
      merchantInfo.value = response.data
    } else if (response && response.id) {
      // 兼容后端直接返回实体的情况
      merchantInfo.value = response
    } else {
      ElMessage.warning('当前账号还没有关联的商家信息，请先完成资质申请并通过审核')
    }
  } catch (e) {
    console.error('获取商家信息失败:', e)
    ElMessage.error(e?.response?.data?.message || '获取商家信息失败，请稍后重试')
  }
}

onMounted(async () => {
  await loadMerchantInfo()
  await loadReviews()
  if (!realtimeSync.isConnected) {
    realtimeSync.connect()
  }
  realtimeSync.subscribe('review', handleReviewRealtime)
  realtimeSync.subscribe('merchant', handleMerchantRealtime)
  realtimeSync.subscribe('scenic_spot', handleScenicRealtime)
})

onUnmounted(() => {
  realtimeSync.unsubscribe('review', handleReviewRealtime)
  realtimeSync.unsubscribe('merchant', handleMerchantRealtime)
  realtimeSync.unsubscribe('scenic_spot', handleScenicRealtime)
})
</script>

<style scoped>
.review-manage {
  padding: 20px;
  background: white;
  min-height: 100%;
  max-width: 1400px;
  margin: 0 auto;
}

.back-button-container {
  margin-bottom: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  height: 100px;
}

.stat-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  margin-right: 16px;
}

.stat-icon.total {
  background: #409eff;
}

.stat-icon.average {
  background: #e6a23c;
}

.stat-icon.replied {
  background: #67c23a;
}

.stat-icon.pending {
  background: #f56c6c;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  color: #909399;
  font-size: 14px;
}

.filter-card {
  margin-bottom: 20px;
}

.reviews-card {
  margin-bottom: 20px;
}

.review-count {
  color: #909399;
  font-size: 14px;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.review-item {
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: white;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.username {
  font-weight: 600;
  color: #303133;
}

.review-time {
  color: #909399;
  font-size: 12px;
}

.review-rating {
  display: flex;
  align-items: center;
  gap: 8px;
}

.rating-text {
  color: #e6a23c;
  font-weight: 600;
}

.review-content {
  margin-bottom: 12px;
}

.review-content p {
  margin: 0 0 12px 0;
  color: #606266;
  line-height: 1.6;
}

.review-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.review-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
}

.replies-container {
  margin-top: 16px;
  padding-left: 52px;
  border-left: 2px solid #ebeef5;
}

.merchant-reply-block {
  background: linear-gradient(135deg, #fff7ed 0%, #eff6ff 100%);
  border: 1px solid #fdba74;
  border-radius: 10px;
  padding: 12px 14px !important;
  margin-bottom: 12px;
}

.merchant-reply-label {
  font-size: 11px;
  font-weight: 700;
  color: #ea580c;
  margin-bottom: 8px;
  letter-spacing: 0.3px;
}

.merchant-replier {
  color: #1d4ed8 !important;
}

.merchant-reply-body {
  font-size: 12px !important;
  color: #1e3a8a !important;
  line-height: 1.65 !important;
}

.reply-item {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.reply-item:last-child {
  border-bottom: none;
}

.reply-header-info {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
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
  color: #303133;
  font-size: 14px;
}

.reply-content-text {
  color: #606266;
  line-height: 1.6;
  font-size: 14px;
}

.reply-actions-inline {
  margin-top: 8px;
  margin-left: 44px;
}

.reply-form-inline.nested {
  margin-top: 12px;
  margin-left: 44px;
}

.reply-actions {
  margin-top: 8px;
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.child-replies {
  margin-top: 12px;
  margin-left: 20px;
  padding-left: 20px;
  border-left: 2px solid #f0f0f0;
}

.child-reply-item {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
  padding-bottom: 8px;
}

.child-reply-item:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
}

.child-reply-info {
  flex: 1;
}

.child-reply-user {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
}

.child-reply-user .replier-name {
  font-size: 13px;
}

.child-reply-content {
  color: #606266;
  line-height: 1.5;
  font-size: 13px;
}

.reply-edit-area {
  margin-top: 12px;
  padding: 16px;
  background: white;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
}

.edit-header {
  margin-bottom: 12px;
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.reply-input {
  margin-bottom: 12px;
}

.edit-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.review-actions {
  display: flex;
  gap: 8px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.original-review {
  background: white;
  padding: 12px;
  border-radius: 6px;
  margin-bottom: 12px;
  border: 1px solid #e4e7ed;
}

.original-review p {
  margin: 0 0 8px 0;
  color: #606266;
}

.review-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #909399;
  font-size: 12px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .stats-row .el-col {
    margin-bottom: 16px;
  }
  
  .filter-card .el-form {
    flex-direction: column;
  }
  
  .filter-card .el-form-item {
    margin-right: 0;
    margin-bottom: 16px;
  }
  
  .review-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .review-actions {
    flex-wrap: wrap;
  }
}
</style>