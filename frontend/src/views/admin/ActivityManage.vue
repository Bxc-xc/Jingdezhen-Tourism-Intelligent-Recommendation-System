<template>
  <div class="admin-activity">
    <div class="page-header">
      <div style="display:flex;align-items:center;gap:12px">
        <el-button :icon="ArrowLeft" circle @click="router.go(-1)" />
        <h2>店铺活动管理</h2>
      </div>
      <div class="header-actions">
        <el-input
          v-model="keyword"
          placeholder="搜索活动名称或商家"
          clearable
          style="width:200px"
          @clear="loadList"
          @keyup.enter="loadList"
        />
        <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width:120px" @change="loadList">
          <el-option label="全部" value="" />
          <el-option label="进行中" value="active" />
          <el-option label="未开始" value="pending" />
          <el-option label="已结束" value="ended" />
        </el-select>
        <el-button :icon="Refresh" @click="loadList">刷新</el-button>
      </div>
    </div>

    <el-card>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column label="封面" width="80">
          <template #default="{ row }">
            <el-image v-if="row.image" :src="row.image" style="width:56px;height:40px;border-radius:4px" fit="cover" />
            <span v-else style="color:#c0c4cc;font-size:12px">无图</span>
          </template>
        </el-table-column>
        <el-table-column prop="merchantName" label="商家" width="130" />
        <el-table-column prop="title" label="活动名称" min-width="140" />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ typeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="折扣" width="80">
          <template #default="{ row }">
            <span v-if="row.discount" style="color:#ff2d2d;font-weight:700">{{ row.discount }}折</span>
            <span v-else style="color:#c0c4cc">-</span>
          </template>
        </el-table-column>
        <el-table-column label="活动时间" width="200">
          <template #default="{ row }">
            <div style="font-size:12px">
              <div>{{ formatDateTime(row.startTime) }}</div>
              <div style="color:#909399">至 {{ formatDateTime(row.endTime) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-popconfirm
              v-if="row.status !== 'ended'"
              title="确认强制下架该活动？"
              @confirm="handleOffline(row)"
            >
              <template #reference>
                <el-button size="small" link type="danger">强制下架</el-button>
              </template>
            </el-popconfirm>
            <span v-else style="color:#c0c4cc;font-size:12px">已结束</span>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && list.length === 0" description="暂无活动数据" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Refresh, ArrowLeft } from '@element-plus/icons-vue'
import { adminGetAllActivities, adminOfflineActivity } from '../../api/merchantActivity'

const router = useRouter()
const loading = ref(false)
const list = ref([])
const keyword = ref('')
const filterStatus = ref('')

const typeLabel = (t) => ({ discount: '优惠', experience: '体验', exhibition: '展览', other: '其他' }[t] || t || '-')
const statusLabel = (s) => ({ active: '进行中', pending: '未开始', ended: '已结束' }[s] || s)
const statusType = (s) => ({ active: 'success', pending: 'warning', ended: 'info' }[s] || '')

const formatDateTime = (dt) => {
  if (!dt) return '-'
  return new Date(dt).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

const loadList = async () => {
  loading.value = true
  try {
    const res = await adminGetAllActivities({ keyword: keyword.value, status: filterStatus.value })
    list.value = res?.data || []
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleOffline = async (row) => {
  try {
    await adminOfflineActivity(row.id)
    ElMessage.success('已强制下架')
    await loadList()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

onMounted(loadList)
</script>

<style scoped>
.admin-activity { padding: 20px; }
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.page-header h2 { margin: 0; font-size: 22px; color: #303133; }
.header-actions { display: flex; gap: 12px; align-items: center; }
</style>
