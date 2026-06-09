<template>
  <div class="audit-manage">
    <div class="page-header">
      <div class="page-title">
        <el-button link type="primary" @click="handleBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h2>审核管理</h2>
      </div>
      <div class="header-actions">
        <el-button @click="refreshAudits">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button type="primary" @click="batchApprove" :disabled="selectedItems.length === 0">
          <el-icon><Check /></el-icon>
          批量通过
        </el-button>
      </div>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :model="filterForm" inline>
        <el-form-item label="审核状态">
          <el-select 
            v-model="filterForm.status" 
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
            <el-option label="待审核" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="已拒绝" value="rejected" />
          </el-select>
        </el-form-item>
        <el-form-item label="申请时间">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            :popper-options="{
              strategy: 'fixed',
              modifiers: [
                { name: 'offset', options: { offset: [0, 4] } },
                { name: 'computeStyles', options: { adaptive: true, gpuAcceleration: true } }
              ]
            }"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">筛选</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 审核统计 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon pending">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ auditStats.pending }}</div>
              <div class="stat-label">待审核</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon approved">
              <el-icon><Check /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ auditStats.approved }}</div>
              <div class="stat-label">已通过</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon rejected">
              <el-icon><Close /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ auditStats.rejected }}</div>
              <div class="stat-label">已拒绝</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon total">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ auditStats.total }}</div>
              <div class="stat-label">总申请数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 审核列表 -->
    <el-card class="audits-card">
      <template #header>
        <span>审核列表</span>
        <span class="audit-count">共 {{ filteredAudits.length }} 条申请</span>
      </template>
      
      <el-table
        :data="paginatedAudits"
        v-loading="loading"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="申请ID" width="100" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="scope">
            <el-tag :type="getTypeTagType(scope.row.type)">
              {{ getTypeText(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="申请标题" />
        <el-table-column prop="applicant" label="申请人" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="viewDetail(scope.row)">查看</el-button>
            <el-button
              v-if="scope.row.status === 'pending'"
              size="small"
              type="success"
              @click="approveAudit(scope.row.id)"
            >
              通过
            </el-button>
            <el-button
              v-if="scope.row.status === 'pending'"
              size="small"
              type="danger"
              @click="rejectAudit(scope.row.id)"
            >
              拒绝
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
          :total="filteredAudits.length"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 审核详情对话框 -->
    <el-dialog v-model="showDetail" title="审核详情" width="800px">
      <div v-if="selectedAudit" class="audit-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="申请ID">{{ selectedAudit.id }}</el-descriptions-item>
          <el-descriptions-item label="申请类型">
            <el-tag :type="getTypeTagType(selectedAudit.type)">
              {{ getTypeText(selectedAudit.type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请人">{{ selectedAudit.applicant }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ formatDate(selectedAudit.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="getStatusTagType(selectedAudit.status)">
              {{ getStatusText(selectedAudit.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="审核人">{{ selectedAudit.auditor || '未审核' }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="audit-content">
          <h4>申请内容</h4>
          <div class="content-detail">
            <h5>{{ selectedAudit.title }}</h5>
            <p>{{ selectedAudit.description }}</p>
            
            <!-- 商家申请详细信息 -->
            <div v-if="selectedAudit.type === 'merchant'" class="merchant-detail-info">
              <div class="detail-section">
                <h5 class="section-title">基本信息</h5>
                <el-descriptions :column="2" border>
                  <el-descriptions-item label="申请人">
                    {{ selectedAudit.applicant }}
                  </el-descriptions-item>
                  <el-descriptions-item label="用户ID">
                    {{ selectedAudit.userId }}
                  </el-descriptions-item>
                  <el-descriptions-item label="申请时间" :span="2">
                    {{ formatDate(selectedAudit.createTime) }}
                  </el-descriptions-item>
                </el-descriptions>
              </div>
              
              <div class="detail-section">
                <h5 class="section-title">资质证件</h5>
                <el-row :gutter="20">
                  <!-- 营业执照 -->
                  <el-col :span="8" v-if="selectedAudit.businessLicense">
                    <div class="document-card">
                      <div class="card-header">营业执照</div>
                      <div class="card-body">
                        <el-image
                          :src="getImageUrl(selectedAudit.businessLicense)"
                          :preview-src-list="[getImageUrl(selectedAudit.businessLicense)]"
                          class="document-image single"
                          fit="cover"
                        />
                      </div>
                    </div>
                  </el-col>
                  
                  <!-- 身份证 -->
                  <el-col :span="16" v-if="selectedAudit.identityCard && selectedAudit.identityCard.length > 0">
                    <div class="document-card">
                      <div class="card-header">身份证 ({{selectedAudit.identityCard.length}}张)</div>
                      <div class="card-body row-images">
                        <el-image
                          v-for="(image, index) in selectedAudit.identityCard"
                          :key="index"
                          :src="getImageUrl(image)"
                          :preview-src-list="selectedAudit.identityCard.map(img => getImageUrl(img))"
                          :initial-index="index"
                          preview-teleported
                          class="document-image"
                          fit="cover"
                        />
                      </div>
                    </div>
                  </el-col>
                </el-row>
              </div>
              
              <!-- 店铺照片 -->
              <div v-if="selectedAudit.shopPhotos && selectedAudit.shopPhotos.length > 0" class="detail-section">
                <h5 class="section-title">店铺照片 ({{selectedAudit.shopPhotos.length}}张)</h5>
                <div class="shop-photos-grid">
                  <el-image
                    v-for="(image, index) in selectedAudit.shopPhotos"
                    :key="index"
                    :src="getImageUrl(image)"
                    :preview-src-list="selectedAudit.shopPhotos.map(img => getImageUrl(img))"
                    :initial-index="index"
                    preview-teleported
                    class="shop-image"
                    fit="cover"
                  />
                </div>
              </div>
            </div>
            
            <!-- 其他类型的图片展示 -->
            <div v-else-if="selectedAudit.images && selectedAudit.images.length > 0" class="content-images">
              <el-image
                v-for="(image, index) in selectedAudit.images"
                :key="index"
                :src="getImageUrl(image)"
                :preview-src-list="selectedAudit.images.map(img => getImageUrl(img))"
                :initial-index="index"
                preview-teleported
                class="content-image"
                fit="cover"
              />
            </div>
          </div>
        </div>

        <div v-if="selectedAudit.auditResult" class="audit-result">
          <h4>审核结果</h4>
          <div class="result-content">
            <p><strong>审核意见：</strong>{{ selectedAudit.auditResult.opinion }}</p>
            <p><strong>审核时间：</strong>{{ formatDate(selectedAudit.auditResult.auditTime) }}</p>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showDetail = false">关闭</el-button>
        <el-button
          v-if="selectedAudit?.status === 'pending'"
          type="success"
          @click="approveAudit(selectedAudit.id)"
        >
          通过审核
        </el-button>
        <el-button
          v-if="selectedAudit?.status === 'pending'"
          type="danger"
          @click="rejectAudit(selectedAudit.id)"
        >
          拒绝审核
        </el-button>
      </template>
    </el-dialog>

    <!-- 审核意见对话框 -->
    <el-dialog v-model="showAuditDialog" title="审核意见" width="500px">
      <el-form :model="auditForm" label-width="80px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.result">
            <el-radio value="approved">通过</el-radio>
            <el-radio value="rejected">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input
            v-model="auditForm.opinion"
            type="textarea"
            :rows="4"
            placeholder="请输入审核意见..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAuditDialog = false">取消</el-button>
        <el-button type="primary" @click="submitAudit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Refresh, Check, Close, Clock, Document } from '@element-plus/icons-vue'
import { useRealtimeData } from '../../composables/useRealtimeData'
import { merchantApplicationApi } from '../../api/merchantApplication'
const router = useRouter()

// 实时审核数据（如有推送则替换列表）
const { data: realtimeAudits } = useRealtimeData('audit', [])

const handleBack = () => {
  router.back()
}

const loading = ref(false)
const showDetail = ref(false)
const showAuditDialog = ref(false)
const selectedAudit = ref(null)
const selectedItems = ref([])

const currentPage = ref(1)
const pageSize = ref(10)

const filterForm = reactive({
  status: '',
  dateRange: []
})

const auditForm = reactive({
  result: 'approved',
  opinion: ''
})

const auditStats = ref({
  pending: 15,
  approved: 89,
  rejected: 12,
  total: 116
})

// 解析图片字段（处理可能的数组字符串格式）
const parseImages = (data) => {
  if (!data) return []
  if (Array.isArray(data)) return data
  if (typeof data === 'string') {
    // 移除Java toString()产生的方括号，以及可能的引号
    const cleanStr = data.replace(/^\[|\]$/g, '').replace(/["']/g, '').trim()
    if (!cleanStr) return []

    // 如果整个字符串是单个Base64，直接返回
    if (cleanStr.startsWith('data:image')) {
      return [cleanStr]
    }

    // 用正则按逗号分割，但跳过 base64 data URI 内部的逗号
    // base64 data URI 格式: data:image/xxx;base64,<base64data>
    // 分割策略：找到所有以 /uploads/ 或 http 或 data:image 开头的片段
    const urlPattern = /(?:data:image\/[^;]+;base64,[A-Za-z0-9+/=]+|https?:\/\/[^,]+|\/uploads\/[^,]+|uploads\/[^,]+)/g
    const matches = cleanStr.match(urlPattern)
    if (matches && matches.length > 0) {
      return matches.map(s => s.trim()).filter(Boolean)
    }

    // 降级：普通逗号分割（适用于普通URL路径）
    return cleanStr.split(',').map(s => s.trim()).filter(Boolean)
  }
  return []
}

// 从后端API加载商家申请数据
const loadMerchantApplications = async () => {
  try {
    const response = await merchantApplicationApi.getAllApplications()
    if (response && response.success && response.data) {
      const applications = Array.isArray(response.data) ? response.data : []
      return applications.map(app => ({
        id: app.id || 'AUD' + Date.now() + Math.random(),
        type: 'merchant',
        title: app.description || '商家资质申请',
        description: app.description || '商家资质申请',
        applicant: app.user?.username || `用户ID: ${app.userId}`,
        status: app.status === 'PENDING' ? 'pending' : (app.status === 'APPROVED' ? 'approved' : (app.status === 'REJECTED' ? 'rejected' : 'pending')),
        createTime: app.createdAt ? new Date(app.createdAt) : new Date(),
        auditor: null,
        images: parseImages(app.shopPhotos),
        auditResult: app.auditOpinion ? {
          opinion: app.auditOpinion,
          auditTime: app.updatedAt ? new Date(app.updatedAt) : new Date()
        } : null,
        businessLicense: app.businessLicense,
        identityCard: parseImages(app.identityCard),
        shopPhotos: parseImages(app.shopPhotos),
        userId: app.userId,
        applicationId: app.id,
        user: app.user
      }))
    }
  } catch (error) {
    console.error('加载商家申请失败:', error)
  }
  return []
}

// 初始化审核列表
const audits = ref([])

// 加载所有审核数据
const loadAllAudits = async () => {
  loading.value = true
  try {
    // 加载商家申请
    const merchantApps = await loadMerchantApplications()
    
    // 只展示商家申请
    audits.value = merchantApps
    
    // 更新统计数据
    updateAuditStats()
  } catch (error) {
    console.error('加载审核数据失败:', error)
    ElMessage.error('加载审核数据失败')
  } finally {
    loading.value = false
  }
}

// 更新审核统计数据
const updateAuditStats = () => {
  auditStats.value = {
    pending: audits.value.filter(a => a.status === 'pending').length,
    approved: audits.value.filter(a => a.status === 'approved').length,
    rejected: audits.value.filter(a => a.status === 'rejected').length,
    total: audits.value.length
  }
}

// 如果有实时数据推送，替换本地列表
watch(realtimeAudits, (val) => {
  if (Array.isArray(val) && val.length > 0) {
    audits.value = val.map(item => ({
      id: item.id || item.auditId || 'AUD' + Date.now(),
      type: item.type || 'content',
      title: item.title || item.name || '审核事项',
      description: item.description || item.detail || '',
      applicant: item.applicant || item.user || '用户',
      status: item.status || 'pending',
      createTime: item.createTime ? new Date(item.createTime) : new Date(),
      auditor: item.auditor || null,
      images: item.images || [],
      auditResult: item.auditResult || null
    }))
  }
})

const filteredAudits = computed(() => {
  let filtered = audits.value

  if (filterForm.status) {
    filtered = filtered.filter(audit => audit.status === filterForm.status)
  }

  if (filterForm.dateRange && filterForm.dateRange.length === 2) {
    const [startDate, endDate] = filterForm.dateRange
    filtered = filtered.filter(audit => {
      const auditDate = new Date(audit.createTime).toISOString().split('T')[0]
      return auditDate >= startDate && auditDate <= endDate
    })
  }

  // 按申请时间降序排列，最新的在最前面
  filtered = [...filtered].sort((a, b) => new Date(b.createTime) - new Date(a.createTime))

  return filtered
})

const paginatedAudits = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredAudits.value.slice(start, end)
})

const handleFilter = () => {
  currentPage.value = 1
}

const resetFilter = () => {
  Object.assign(filterForm, {
    status: '',
    dateRange: []
  })
  currentPage.value = 1
}

const batchApprove = async () => {
  try {
    await ElMessageBox.confirm(`确认要通过选中的 ${selectedItems.value.length} 条申请吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    selectedItems.value.forEach(item => {
      const audit = audits.value.find(a => a.id === item.id)
      if (audit) {
        audit.status = 'approved'
        audit.auditor = '管理员'
        audit.auditResult = {
          opinion: '批量审核通过',
          auditTime: new Date()
        }
      }
    })

    selectedItems.value = []
    ElMessage.success('批量审核完成')
  } catch {
    // 用户取消
  }
}

const handleSelectionChange = (selection) => {
  selectedItems.value = selection
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val) => {
  currentPage.value = val
}

const getTypeTagType = (type) => {
  const types = {
    merchant: 'success',
    scenic: 'primary',
    activity: 'warning',
    content: 'danger'
  }
  return types[type] || 'info'
}

const getTypeText = (type) => {
  const texts = {
    merchant: '商家申请',
    scenic: '景点信息',
    activity: '活动申请',
    content: '内容审核'
  }
  return texts[type] || type
}

const getStatusTagType = (status) => {
  const types = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    pending: '待审核',
    approved: '已通过',
    rejected: '已拒绝'
  }
  return texts[status] || status
}

const viewDetail = async (audit) => {
  // 如果是商家申请，尝试从后端加载完整数据
  if (audit.type === 'merchant' && audit.applicationId) {
    try {
      loading.value = true
      const response = await merchantApplicationApi.getApplicationById(audit.applicationId)
      if (response && response.success && response.data) {
        const app = response.data
          // 映射真实数据
          // 处理身份证和店铺照片数组
          const identityCardList = parseImages(app.identityCard)
          const shopPhotosList = parseImages(app.shopPhotos)

          selectedAudit.value = {
            id: app.id,
            type: 'merchant',
            title: '商家资质申请',
            description: app.description || '商家资质申请',
            applicant: app.user?.username || `用户ID: ${app.userId}`,
            status: app.status === 'PENDING' ? 'pending' : (app.status === 'APPROVED' ? 'approved' : (app.status === 'REJECTED' ? 'rejected' : 'pending')),
            createTime: app.createdAt ? new Date(app.createdAt) : new Date(),
            auditor: app.auditor || (app.status !== 'PENDING' ? 'admin' : null),
            images: shopPhotosList,
            auditResult: app.auditOpinion ? {
              opinion: app.auditOpinion,
              auditTime: app.updatedAt ? new Date(app.updatedAt) : new Date()
            } : null,
            businessLicense: app.businessLicense,
            identityCard: identityCardList,
            shopPhotos: shopPhotosList,
            userId: app.userId,
            applicationId: app.id,
            user: app.user
          }
      } else {
        // 如果API失败，使用传入的audit数据
        selectedAudit.value = audit
      }
    } catch (error) {
      console.error('加载审核详情失败:', error)
      // 如果API失败，使用传入的audit数据
      selectedAudit.value = audit
    } finally {
      loading.value = false
    }
  } else {
    // 非商家申请，直接使用传入的数据
    selectedAudit.value = audit
  }
  showDetail.value = true
}

const approveAudit = (auditId) => {
  selectedAudit.value = audits.value.find(a => a.id === auditId)
  auditForm.result = 'approved'
  auditForm.opinion = ''
  showAuditDialog.value = true
}

const rejectAudit = (auditId) => {
  selectedAudit.value = audits.value.find(a => a.id === auditId)
  auditForm.result = 'rejected'
  auditForm.opinion = ''
  showAuditDialog.value = true
}

const submitAudit = async () => {
  if (!auditForm.opinion.trim()) {
    ElMessage.warning('请输入审核意见')
    return
  }

  const audit = audits.value.find(a => a.id === selectedAudit.value.id)
  if (!audit) {
    ElMessage.error('未找到审核项')
    return
  }

  // 如果是商家申请，调用后端API
  if (audit.type === 'merchant' && audit.applicationId) {
    try {
      loading.value = true
      const response = await merchantApplicationApi.auditApplication(audit.applicationId, {
        status: auditForm.result.toUpperCase(), // APPROVED 或 REJECTED
        opinion: auditForm.opinion
      })
      
      if (response && response.success) {
        // 更新本地数据
        audit.status = auditForm.result
        audit.auditor = '管理员'
        audit.auditResult = {
          opinion: auditForm.opinion,
          auditTime: new Date()
        }

        // 更新统计
        updateAuditStats()
        
        ElMessage.success('审核完成')
        showAuditDialog.value = false
      } else {
        ElMessage.error(response?.message || '审核失败')
      }
    } catch (error) {
      console.error('审核失败:', error)
      ElMessage.error(error.response?.data?.message || error.message || '审核失败')
    } finally {
      loading.value = false
    }
  } else {
    // 其他类型的审核（暂时只更新本地数据）
    audit.status = auditForm.result
    audit.auditor = 'admin'
    audit.auditResult = {
      opinion: auditForm.opinion,
      auditTime: new Date()
    }

    // 更新统计
    updateAuditStats()
    
    showAuditDialog.value = false
    ElMessage.success('审核完成')
  }
}

// 处理图片URL
const getImageUrl = (path) => {
  if (!path) return ''
  // 确保path是字符串
  const pathStr = String(path).trim()
  
  // Base64图片直接返回
  if (pathStr.startsWith('data:image')) return pathStr

  // 如果字符串太长且不是http开头，可能是错误的Base64片段，直接返回避免拼接导致431错误
  if (pathStr.length > 200 && !pathStr.startsWith('http')) {
    return pathStr
  }

  // 已经是完整URL
  if (pathStr.startsWith('http') || pathStr.startsWith('blob:')) return pathStr
  
  // 已经是绝对路径（以/uploads开头）
  if (pathStr.startsWith('/uploads/')) return pathStr
  if (pathStr.startsWith('/uploads')) return pathStr + '/' // 补充斜杠
  
  // 相对路径
  if (pathStr.startsWith('uploads/')) return '/' + pathStr
  if (pathStr.startsWith('uploads')) return '/' + pathStr
  
  // 只有文件名
  return '/uploads/' + pathStr
}

const formatDate = (date) => {
  return new Date(date).toLocaleString()
}

// 刷新审核列表
const refreshAudits = async () => {
  await loadAllAudits()
  ElMessage.success('刷新成功')
}

onMounted(() => {
  // 加载所有审核数据
  loadAllAudits()
})
</script>

<style scoped>
.audit-manage {
  padding: 20px;
  background: #fff;
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

.page-header h2 {
  margin: 0;
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

.stat-icon.pending {
  background: #e6a23c;
}

.stat-icon.approved {
  background: #67c23a;
}

.stat-icon.rejected {
  background: #f56c6c;
}

.stat-icon.total {
  background: #409eff;
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

.audits-card {
  margin-bottom: 20px;
}

.audit-count {
  color: #909399;
  font-size: 14px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.audit-detail {
  padding: 20px 0;
}

.audit-content {
  margin: 20px 0;
}

.audit-content h4 {
  margin-bottom: 12px;
  color: #303133;
}

.content-detail h5 {
  margin: 0 0 8px 0;
  color: #303133;
}

.content-detail p {
  margin: 0 0 12px 0;
  color: #606266;
  line-height: 1.6;
}

.merchant-detail-info {
  margin-top: 20px;
}

.detail-section {
  margin-bottom: 24px;
}

.section-title {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 15px;
  font-weight: 600;
  border-left: 3px solid #409eff;
  padding-left: 8px;
  line-height: 1.2;
}

.document-card {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-header {
  background: #f5f7fa;
  padding: 8px 12px;
  font-size: 13px;
  color: #606266;
  border-bottom: 1px solid #dcdfe6;
  font-weight: 500;
}

.card-body {
  padding: 12px;
  background: #fff;
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 160px;
}

.card-body.row-images {
  justify-content: flex-start;
  gap: 12px;
  flex-wrap: wrap;
  align-items: flex-start;
}

.document-image {
  width: 100%;
  height: 160px;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
  cursor: pointer;
  transition: all 0.3s;
}

.document-image:hover {
  border-color: #409eff;
  transform: scale(1.02);
}

.document-image.single {
  max-width: 100%;
  height: auto;
  max-height: 160px;
}

.card-body.row-images .document-image {
  width: calc(50% - 6px);
  height: 160px;
  object-fit: cover;
}

.shop-photos-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 12px;
}

.shop-image {
  width: 100%;
  aspect-ratio: 4/3;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
  cursor: pointer;
  transition: all 0.3s;
}

.shop-image:hover {
  border-color: #409eff;
  transform: scale(1.05);
  box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
}

.content-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.content-image {
  width: 100px;
  height: 100px;
  border-radius: 4px;
}

.audit-result {
  margin-top: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 6px;
}

.audit-result h4 {
  margin: 0 0 12px 0;
  color: #303133;
}

.result-content p {
  margin: 0 0 8px 0;
  color: #606266;
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
}
</style>