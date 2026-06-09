<template>
  <div class="review-manage">
    <div class="page-header">
      <div class="page-title">
        <el-button link type="primary" @click="handleBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h2>评价管理</h2>
      </div>
      <div class="header-actions">
        <el-button @click="refreshReviews">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button
          type="danger"
          :disabled="!selectedItems.length"
          @click="handleBatchDelete"
        >
          批量删除
        </el-button>
        <el-button type="primary" @click="handleAddReview">
          <el-icon><Plus /></el-icon>
          新增评价
        </el-button>
      </div>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :model="filterForm" inline>
        <el-form-item label="商家类型">
          <el-select 
            v-model="filterForm.merchantCategory" 
            placeholder="全部类型" 
            clearable
            style="width: 150px"
            @change="handleFilter"
          >
            <el-option label="全部" value="" />
            <el-option label="景点商家" value="SCENIC" />
            <el-option label="陶瓷工坊" value="CERAMIC" />
            <el-option label="酒店" value="HOTEL" />
            <el-option label="餐饮" value="FOOD" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="商家">
          <el-select 
            v-model="filterForm.merchantId" 
            placeholder="选择商家" 
            clearable
            filterable
            style="width: 200px"
            @change="handleFilter"
          >
            <el-option
              v-for="merchant in filteredMerchants"
              :key="merchant.id"
              :label="merchant.shopName"
              :value="merchant.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="评分">
          <el-select 
            v-model="filterForm.rating" 
            placeholder="全部评分" 
            clearable
            style="width: 120px"
            @change="handleFilter"
          >
            <el-option label="全部" value="" />
            <el-option label="5星" :value="5" />
            <el-option label="4星" :value="4" />
            <el-option label="3星" :value="3" />
            <el-option label="2星" :value="2" />
            <el-option label="1星" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="评价时间">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 240px"
            @change="handleFilter"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">筛选</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 评价统计 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon total">
              <el-icon><Document /></el-icon>
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
            <div class="stat-icon scenic">
              <el-icon><Location /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ reviewStats.scenicReviews }}</div>
              <div class="stat-label">景点评价</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon merchant">
              <el-icon><Shop /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ reviewStats.merchantReviews }}</div>
              <div class="stat-label">商家评价</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon today">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ reviewStats.todayReviews }}</div>
              <div class="stat-label">今日新增</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 评价列表 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="paginatedReviews"
        stripe
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="评价类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.merchant ? 'success' : 'info'">
              {{ row.merchant ? merchantCategoryLabel(row.merchant.category) : '景点' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="评价对象" width="200">
          <template #default="{ row }">
            <div v-if="row.merchant">
              <div class="merchant-name">{{ row.merchant.shopName }}</div>
              <div class="merchant-category">{{ row.merchant.category }}</div>
            </div>
            <div v-else-if="row.scenicSpot">
              {{ row.scenicSpot.name }}
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="评价用户" width="150">
          <template #default="{ row }">
            <div v-if="row.user">
              {{ row.user.username }}
            </div>
            <span v-else>用户ID: {{ row.userId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="评分" width="100">
          <template #default="{ row }">
            <el-rate
              v-model="row.rating"
              disabled
              show-score
              text-color="#ff9900"
              score-template="{value}"
            />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评价内容" min-width="200" show-overflow-tooltip />
        <el-table-column label="评价时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="回复状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.replyContent" type="success" size="small">已回复</el-tag>
            <el-tag v-else type="info" size="small">未回复</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              size="small"
              @click="handleViewDetail(row)"
            >
              查看
            </el-button>
            <el-button
              link
              type="primary"
              size="small"
              @click="handleEditReview(row)"
            >
              编辑
            </el-button>
            <el-button
              link
              type="danger"
              size="small"
              @click="handleDeleteReview(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="filteredReviews.length"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 评价详情对话框 -->
    <el-dialog v-model="showDetail" title="评价详情" width="800px">
      <div v-if="selectedReview" class="review-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="评价ID">{{ selectedReview.id }}</el-descriptions-item>
          <el-descriptions-item label="评价类型">
            <el-tag :type="selectedReview.merchant ? 'success' : 'info'">
              {{ selectedReview.merchant ? '陶瓷工坊' : '景点' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="评价对象">
            <div v-if="selectedReview.merchant">
              {{ selectedReview.merchant.shopName }}
            </div>
            <div v-else-if="selectedReview.scenicSpot">
              {{ selectedReview.scenicSpot.name }}
            </div>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="评价用户">
            <div v-if="selectedReview.user">
              {{ selectedReview.user.username }}
            </div>
            <span v-else>用户ID: {{ selectedReview.userId }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="评分">
            <el-rate
              v-model="selectedReview.rating"
              disabled
              show-score
              text-color="#ff9900"
              score-template="{value}星"
            />
          </el-descriptions-item>
          <el-descriptions-item label="评价时间">
            {{ formatDate(selectedReview.createTime) }}
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="review-content">
          <h4>评价内容</h4>
          <div class="content-text">{{ selectedReview.content || '无内容' }}</div>
        </div>

        <div v-if="selectedReview.replyContent" class="review-reply">
          <h4>回复内容</h4>
          <div class="reply-text">{{ selectedReview.replyContent }}</div>
          <div class="reply-time">回复时间：{{ formatDate(selectedReview.replyTime) }}</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showDetail = false">关闭</el-button>
        <el-button type="primary" @click="handleEditReview(selectedReview)">编辑</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑评价对话框 -->
    <el-dialog 
      v-model="showEditDialog" 
      :title="editMode === 'add' ? '新增评价' : '编辑评价'" 
      width="600px"
    >
      <el-form
        ref="reviewFormRef"
        :model="reviewForm"
        :rules="reviewFormRules"
        label-width="100px"
      >
        <el-form-item label="评价类型" prop="type">
          <el-radio-group v-model="reviewForm.type">
            <el-radio value="merchant">陶瓷工坊</el-radio>
            <el-radio value="scenic">景点</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item 
          v-if="reviewForm.type === 'merchant'" 
          label="陶瓷工坊" 
          prop="merchantId"
        >
          <el-select 
            v-model="reviewForm.merchantId" 
            placeholder="请选择陶瓷工坊" 
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="merchant in filteredMerchants"
              :key="merchant.id"
              :label="merchant.shopName"
              :value="merchant.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item 
          v-if="reviewForm.type === 'scenic'" 
          label="景点" 
          prop="scenicSpotId"
        >
          <el-select 
            v-model="reviewForm.scenicSpotId" 
            placeholder="请选择景点" 
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="scenic in scenicSpots"
              :key="scenic.id"
              :label="scenic.name"
              :value="scenic.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="评价用户" prop="userId">
          <el-select 
            v-model="reviewForm.userId" 
            placeholder="请选择用户" 
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="user in users"
              :key="user.id"
              :label="user.username"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="评分" prop="rating">
          <el-rate
            v-model="reviewForm.rating"
            :max="5"
            show-score
            text-color="#ff9900"
            score-template="{value}星"
          />
        </el-form-item>
        <el-form-item label="评价内容" prop="content">
          <el-input
            v-model="reviewForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入评价内容..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item v-if="editMode === 'edit'" label="回复内容">
          <el-input
            v-model="reviewForm.replyContent"
            type="textarea"
            :rows="3"
            placeholder="请输入回复内容（可选）..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitReview">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowLeft, Refresh, Plus, Document, Shop, Star, Calendar, Location 
} from '@element-plus/icons-vue'
import { 
  getAllReviews, 
  createReview, 
  createMerchantReview,
  deleteReview,
  batchDeleteReviews,
  getReviewsByUserId
} from '../../api/review'
import { getAllMerchants } from '../../api/merchant'
import request from '../../utils/request'
import { getScenicList } from '../../api/scenic'

const router = useRouter()

const loading = ref(false)
const showDetail = ref(false)
const showEditDialog = ref(false)
const selectedReview = ref(null)
const selectedItems = ref([])
const editMode = ref('add') // 'add' or 'edit'

const currentPage = ref(1)
const pageSize = ref(10)

const reviews = ref([])
const merchantOptions = ref([])
const scenicSpots = ref([])
const users = ref([])

const filterForm = reactive({
  merchantCategory: '',
  merchantId: '',
  rating: '',
  dateRange: []
})

const reviewForm = reactive({
  merchantCategory: '',
  merchantId: null,
  scenicSpotId: null,
  userId: null,
  rating: 5,
  content: '',
  replyContent: ''
})

const reviewFormRef = ref(null)

const reviewFormRules = {
  merchantCategory: [
    { required: false, message: '请选择商家类型', trigger: 'change' }
  ],
  merchantId: [
    { required: false, message: '请选择商家', trigger: 'change' }
  ],
  scenicSpotId: [
    { required: true, message: '请选择景点', trigger: 'change' }
  ],
  userId: [
    { required: true, message: '请选择评价用户', trigger: 'change' }
  ],
  rating: [
    { required: true, message: '请选择评分', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入评价内容', trigger: 'blur' }
  ]
}

const reviewStats = computed(() => {
  const merchantReviews = reviews.value.filter(r => r.merchant).length
  const scenicReviews = reviews.value.filter(r => r.scenicSpot).length
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const todayReviews = reviews.value.filter(r => {
    const reviewDate = new Date(r.createTime)
    reviewDate.setHours(0, 0, 0, 0)
    return reviewDate.getTime() === today.getTime()
  }).length
  
  const totalRating = reviews.value.reduce((sum, r) => sum + (r.rating || 0), 0)
  const averageRating = reviews.value.length > 0 ? totalRating / reviews.value.length : 0
  
  return {
    total: reviews.value.length,
    merchantReviews,
    scenicReviews,
    averageRating,
    todayReviews
  }
})

const filteredReviews = computed(() => {
  let result = reviews.value

  // 已移除“评价类型”筛选

  // 按商家类型筛选
  if (filterForm.merchantCategory) {
    if (filterForm.merchantCategory === 'SCENIC') {
      // 景点商家：兼容两种来源
      // 1) 评价直接关联 scenicSpot（没有 merchant 字段）
      // 2) 评价关联 merchant，且 merchant.category === SCENIC
      result = result.filter(r =>
        (r.merchant && r.merchant.category === 'SCENIC') || (!!r.scenicSpot)
      )
    } else {
      // 其他商家类型：必须有关联商家，且分类匹配
      result = result.filter(r => r.merchant && r.merchant.category === filterForm.merchantCategory)
    }
  }

  // 按商家筛选
  if (filterForm.merchantId) {
    result = result.filter(r => r.merchant && r.merchant.id === filterForm.merchantId)
  }

  // 按评分筛选
  if (filterForm.rating) {
    result = result.filter(r => r.rating === filterForm.rating)
  }

  // 按时间筛选
  if (filterForm.dateRange && filterForm.dateRange.length === 2) {
    const startDate = new Date(filterForm.dateRange[0])
    const endDate = new Date(filterForm.dateRange[1])
    endDate.setHours(23, 59, 59, 999)
    result = result.filter(r => {
      const reviewDate = new Date(r.createTime)
      return reviewDate >= startDate && reviewDate <= endDate
    })
  }

  // 按时间倒序排列
  return result.sort((a, b) => {
    return new Date(b.createTime) - new Date(a.createTime)
  })
})

const paginatedReviews = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredReviews.value.slice(start, end)
})

const filteredMerchants = computed(() => {
  let list = merchantOptions.value
  if (filterForm.merchantCategory) {
    list = list.filter(m => m.category === filterForm.merchantCategory)
  }
  return list
})

const merchantCategoryLabel = (category) => {
  const map = {
    SCENIC: '景点商家',
    CERAMIC: '陶瓷工坊',
    FOOD: '餐饮',
    HOTEL: '酒店',
    OTHER: '商家'
  }
  return map[category] || '商家'
}

const handleBack = () => {
  router.back()
}

const loadReviews = async () => {
  loading.value = true
  try {
    const response = await getAllReviews()
    if (response && response.success && response.data) {
      reviews.value = Array.isArray(response.data) ? response.data : []
    } else if (Array.isArray(response)) {
      reviews.value = response
    }
  } catch (error) {
    console.error('加载评价列表失败:', error)
    ElMessage.error('加载评价列表失败')
  } finally {
    loading.value = false
  }
}

const loadMerchants = async () => {
  try {
    const response = await getAllMerchants()
    if (response && response.success && response.data) {
      const allMerchants = Array.isArray(response.data) ? response.data : []
      merchantOptions.value = allMerchants
    } else if (Array.isArray(response)) {
      merchantOptions.value = response
    }
  } catch (error) {
    console.error('加载商家列表失败:', error)
  }
}

const loadScenicSpots = async () => {
  try {
    const response = await getScenicList()
    if (response && response.success && response.data) {
      scenicSpots.value = Array.isArray(response.data) ? response.data : []
    } else if (Array.isArray(response)) {
      scenicSpots.value = response
    }
  } catch (error) {
    console.error('加载景点列表失败:', error)
  }
}

const loadUsers = async () => {
  try {
    const response = await request.get('/user')
    if (response && response.success && response.data) {
      users.value = Array.isArray(response.data) ? response.data : []
    } else if (Array.isArray(response)) {
      users.value = response
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
  }
}

const refreshReviews = () => {
  loadReviews()
  ElMessage.success('刷新成功')
}

const handleFilter = () => {
  currentPage.value = 1
}

const resetFilter = () => {
  filterForm.merchantCategory = ''
  filterForm.merchantId = ''
  filterForm.rating = ''
  filterForm.dateRange = []
  handleFilter()
}

const handleSelectionChange = (selection) => {
  selectedItems.value = selection
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (page) => {
  currentPage.value = page
}

const handleViewDetail = (row) => {
  selectedReview.value = row
  showDetail.value = true
}

const handleAddReview = () => {
  editMode.value = 'add'
  Object.assign(reviewForm, {
    type: 'merchant',
    merchantId: null,
    scenicSpotId: null,
    userId: null,
    rating: 5,
    content: '',
    replyContent: ''
  })
  showEditDialog.value = true
}

const handleEditReview = (row) => {
  editMode.value = 'edit'
  selectedReview.value = row
  Object.assign(reviewForm, {
    type: row.merchant ? 'merchant' : 'scenic',
    merchantId: row.merchant ? row.merchant.id : null,
    scenicSpotId: row.scenicSpot ? row.scenicSpot.id : null,
    userId: row.user ? row.user.id : row.userId,
    rating: Number(row.rating) || 5, // 确保 rating 是数字类型
    content: row.content || '',
    replyContent: row.replyContent || ''
  })
  showEditDialog.value = true
}

const handleSubmitReview = async () => {
  if (!reviewFormRef.value) return
  
  await reviewFormRef.value.validate(async (valid) => {
    if (!valid) return

    // 动态验证
    if (reviewForm.type === 'merchant' && !reviewForm.merchantId) {
      ElMessage.warning('请选择商家')
      return
    }
    if (reviewForm.type === 'scenic' && !reviewForm.scenicSpotId) {
      ElMessage.warning('请选择景点')
      return
    }

    try {
      if (editMode.value === 'add') {
        // 新增评价
        const reviewData = {
          userId: reviewForm.userId,
          rating: reviewForm.rating,
          content: reviewForm.content
        }

        if (reviewForm.type === 'merchant') {
          reviewData.merchantId = reviewForm.merchantId
          await createMerchantReview(reviewData)
        } else {
          reviewData.scenicSpotId = reviewForm.scenicSpotId
          await createReview(reviewData)
        }

        ElMessage.success('评价添加成功')
      } else {
        // 编辑评价
        const updateData = {
          rating: Number(reviewForm.rating) || 5, // 确保 rating 是数字类型
          content: reviewForm.content
        }

        if (reviewForm.replyContent) {
          updateData.replyContent = reviewForm.replyContent
        }

        const response = await request.put(`/reviews/${selectedReview.value.id}`, updateData)
        ElMessage.success('评价更新成功')
        
        // 如果响应包含更新的数据，更新本地状态
        if (response?.data?.data) {
          const updatedReview = response.data.data
          const index = reviews.value.findIndex(r => r.id === updatedReview.id)
          if (index !== -1) {
            // 更新本地数据，保持关联对象
            reviews.value[index] = {
              ...updatedReview,
              user: reviews.value[index].user,
              merchant: reviews.value[index].merchant,
              scenicSpot: reviews.value[index].scenicSpot
            }
          }
        }
      }

      showEditDialog.value = false
      // 重新加载数据以确保数据同步
      await loadReviews()
    } catch (error) {
      console.error('保存评价失败:', error)
      ElMessage.error(error.response?.data?.message || '保存评价失败')
    }
  })
}

const handleDeleteReview = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除这条评价吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteReview(row.id)
    ElMessage.success('删除成功')
    await loadReviews()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评价失败:', error)
      ElMessage.error(error.response?.data?.message || '删除评价失败')
    }
  }
}

const handleBatchDelete = async () => {
  if (!selectedItems.value.length) return
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedItems.value.length} 条评价吗？`,
      '批量删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // 分批 + 重试 + 导出失败CSV
    const allIds = selectedItems.value.map(it => Number(it.id)).filter(id => Number.isFinite(id))
    const chunkSize = 200
    const failed = []

    for (let i = 0; i < allIds.length; i += chunkSize) {
      const batch = allIds.slice(i, i + chunkSize)
      try {
        const res = await batchDeleteReviews(batch)
        const failedIds = Array.isArray(res?.failedIds) ? res.failedIds : []
        failed.push(...failedIds.map(id => Number(id)).filter(id => Number.isFinite(id)))
      } catch (e) {
        // 整批失败，重试一次
        try {
          const res2 = await batchDeleteReviews(batch)
          const failedIds2 = Array.isArray(res2?.failedIds) ? res2.failedIds : []
          failed.push(...failedIds2.map(id => Number(id)).filter(id => Number.isFinite(id)))
        } catch (e2) {
          failed.push(...batch)
        }
      }
    }

    if (failed.length === 0) {
      ElMessage.success('批量删除完成')
    } else {
      ElMessage.error(`部分删除失败：${failed.length} 条，已导出失败明细`)
      exportFailedCsv(Array.from(new Set(failed)))
    }

    selectedItems.value = []
    await loadReviews()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error(error.response?.data?.message || '批量删除失败')
    }
  }
}

const exportFailedCsv = (ids) => {
  if (!ids || !ids.length) return
  const header = 'id\n'
  const body = ids.map(id => `${id}`).join('\n')
  const csv = header + body
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `failed_review_ids_${Date.now()}.csv`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(async () => {
  await Promise.all([
    loadReviews(),
    loadMerchants(),
    loadScenicSpots(),
    loadUsers()
  ])
})
</script>

<style scoped>
.review-manage {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.filter-card {
  margin-bottom: 20px;
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

.stat-icon.merchant {
  background: #67c23a;
}

.stat-icon.scenic {
  background: #409eff;
}

.stat-icon.average {
  background: #e6a23c;
}

.stat-icon.today {
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

.table-card {
  margin-bottom: 20px;
}

.merchant-name {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.merchant-category {
  font-size: 12px;
  color: #909399;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.review-detail {
  padding: 10px 0;
}

.review-content {
  margin-top: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 4px;
}

.review-content h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 16px;
}

.content-text {
  color: #606266;
  line-height: 1.6;
  white-space: pre-wrap;
}

.review-reply {
  margin-top: 20px;
  padding: 16px;
  background: #ecf5ff;
  border-radius: 4px;
  border-left: 4px solid #409eff;
}

.review-reply h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 16px;
}

.reply-text {
  color: #606266;
  line-height: 1.6;
  white-space: pre-wrap;
  margin-bottom: 8px;
}

.reply-time {
  font-size: 12px;
  color: #909399;
}
</style>

