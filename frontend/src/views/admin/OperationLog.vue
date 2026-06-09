<template>
  <div class="operation-log">
    <!-- 返回按钮 -->
    <div class="back-button-container">
      <el-button @click="goBack" :icon="ArrowLeft" circle />
    </div>
    
    <div class="header-actions">
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索用户名或操作..."
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>
      <el-button @click="loadLogs">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <!-- 统计信息卡片 -->
    <div class="stats-bar" v-if="total > 0">
      <div class="stat-item">
        <span class="stat-label">总记录数：</span>
        <span class="stat-value">{{ total }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">当前页：</span>
        <span class="stat-value">{{ currentPage }} / {{ totalPages }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">每页显示：</span>
        <span class="stat-value">{{ pageSize }} 条</span>
      </div>
    </div>

    <el-table
      v-loading="loading"
      :data="logList"
      border
      stripe
      style="width: 100%"
      empty-text="暂无操作日志数据"
    >
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="userRole" label="角色" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.userRole === 'ADMIN'" type="danger" size="small">管理员</el-tag>
          <el-tag v-else-if="scope.row.userRole === 'MERCHANT'" type="warning" size="small">商家</el-tag>
          <el-tag v-else type="success" size="small">用户</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="operation" label="操作描述" width="180" />
      <el-table-column prop="method" label="调用方法" show-overflow-tooltip />
      <el-table-column prop="ipAddress" label="IP地址" width="140" />
      <el-table-column prop="createTime" label="操作时间" width="180">
        <template #default="scope">
          {{ formatDateTime(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="详情" width="80" align="center">
        <template #default="scope">
          <el-button link type="primary" size="small" @click="viewDetails(scope.row)">
            查看
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 优化后的分页器 -->
    <div class="pagination-wrapper" v-if="total > 0">
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
      <div class="pagination-info">
        <span class="info-text">
          显示第 <strong>{{ (currentPage - 1) * pageSize + 1 }}</strong> - 
          <strong>{{ Math.min(currentPage * pageSize, total) }}</strong> 条，
          共 <strong>{{ total }}</strong> 条记录
        </span>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty 
      v-if="!loading && total === 0 && logList.length === 0" 
      description="暂无操作日志数据"
      :image-size="120"
    >
      <template #image>
        <el-icon :size="120" color="#c0c4cc"><Document /></el-icon>
      </template>
      <el-button type="primary" @click="loadLogs">
        <el-icon><Refresh /></el-icon>
        刷新数据
      </el-button>
    </el-empty>

    <!-- 详情弹窗 -->
    <el-dialog
      v-model="detailsVisible"
      title="日志详情"
      width="600px"
    >
      <div v-if="currentLog" class="log-details">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="操作用户">{{ currentLog.username }} (ID: {{ currentLog.userId }})</el-descriptions-item>
          <el-descriptions-item label="用户角色">{{ currentLog.userRole }}</el-descriptions-item>
          <el-descriptions-item label="操作内容">{{ currentLog.operation }}</el-descriptions-item>
          <el-descriptions-item label="调用方法">{{ currentLog.method }}</el-descriptions-item>
          <el-descriptions-item label="IP地址">{{ currentLog.ipAddress }}</el-descriptions-item>
          <el-descriptions-item label="操作时间">{{ formatDateTime(currentLog.createTime) }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="params-section">
          <h4>请求参数:</h4>
          <pre class="params-content">{{ formatParams(currentLog.params) }}</pre>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Refresh, Document, ArrowLeft } from '@element-plus/icons-vue'
import { getOperationLogs } from '../../api/operationLog'
import { ElMessage } from 'element-plus'

const router = useRouter()

const goBack = () => {
  router.back()
}

const loading = ref(false)
const logList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')

const detailsVisible = ref(false)
const currentLog = ref(null)

// 计算总页数
const totalPages = computed(() => {
  return Math.ceil(total.value / pageSize.value) || 1
})

const loadLogs = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1, // Spring Boot pagination starts at 0
      size: pageSize.value,
      keyword: searchKeyword.value || undefined
    }
    console.log('请求操作日志，参数:', params)
    const response = await getOperationLogs(params)
    console.log('操作日志响应:', response)
    
    // 处理响应数据
    if (response && response.success) {
      logList.value = response.data || []
      total.value = response.total || 0
      console.log('加载日志成功，数量:', logList.value.length, '总数:', total.value)
      
      if (logList.value.length === 0) {
        ElMessage.info('暂无操作日志数据')
      }
    } else {
      console.warn('响应格式异常:', response)
      logList.value = []
      total.value = 0
      ElMessage.warning('获取日志数据失败')
    }
  } catch (error) {
    console.error('加载日志失败:', error)
    console.error('错误详情:', error.response || error.message)
    logList.value = []
    total.value = 0
    ElMessage.error('加载日志失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadLogs()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  loadLogs()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadLogs()
}

const viewDetails = (log) => {
  currentLog.value = log
  detailsVisible.value = true
}

const formatDateTime = (timeStr) => {
  if (!timeStr) return '-'
  return new Date(timeStr).toLocaleString('zh-CN')
}

const formatParams = (paramsStr) => {
  if (!paramsStr) return '无参数'
  try {
    // 尝试格式化JSON
    return JSON.stringify(JSON.parse(paramsStr), null, 2)
  } catch (e) {
    return paramsStr
  }
}

onMounted(() => {
  loadLogs()
})
</script>

<style scoped>
.operation-log {
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
}

.back-button-container {
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.search-box {
  width: 300px;
}

/* 统计信息栏 */
.stats-bar {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf0 100%);
  border-radius: 6px;
  margin-bottom: 16px;
  border: 1px solid #e4e7ed;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.stat-label {
  color: #606266;
  font-size: 14px;
}

.stat-value {
  color: #303133;
  font-size: 14px;
  font-weight: 600;
}

/* 分页器容器 */
.pagination-wrapper {
  margin-top: 24px;
  padding: 20px;
  background: #fff;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-bottom: 12px;
}

.pagination-info {
  display: flex;
  justify-content: center;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #e4e7ed;
}

.info-text {
  color: #606266;
  font-size: 13px;
}

.info-text strong {
  color: #409eff;
  font-weight: 600;
  margin: 0 2px;
}

/* Element Plus 分页器样式优化 */
:deep(.el-pagination) {
  justify-content: center;
}

:deep(.el-pagination .el-pagination__total) {
  color: #606266;
  font-weight: 500;
}

:deep(.el-pagination .btn-prev),
:deep(.el-pagination .btn-next) {
  background-color: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  transition: all 0.3s;
}

:deep(.el-pagination .btn-prev:hover),
:deep(.el-pagination .btn-next:hover) {
  background-color: #409eff;
  border-color: #409eff;
  color: #fff;
}

:deep(.el-pagination .el-pager li) {
  background-color: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  margin: 0 2px;
  transition: all 0.3s;
}

:deep(.el-pagination .el-pager li:hover) {
  background-color: #ecf5ff;
  border-color: #409eff;
  color: #409eff;
}

:deep(.el-pagination .el-pager li.is-active) {
  background-color: #409eff;
  border-color: #409eff;
  color: #fff;
  font-weight: 600;
}

:deep(.el-pagination .el-select .el-input__inner) {
  border-radius: 4px;
}

:deep(.el-pagination .el-pagination__jump) {
  color: #606266;
}

:deep(.el-pagination .el-pagination__jump .el-input__inner) {
  border-radius: 4px;
  border: 1px solid #dcdfe6;
  transition: all 0.3s;
}

:deep(.el-pagination .el-pagination__jump .el-input__inner:focus) {
  border-color: #409eff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stats-bar {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .pagination-wrapper {
    padding: 16px;
  }
  
  .pagination-container {
    margin-bottom: 16px;
  }
  
  :deep(.el-pagination) {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .pagination-info {
    text-align: center;
  }
}

.params-section {
  margin-top: 20px;
}

.params-content {
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  overflow-x: auto;
  font-family: monospace;
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>
