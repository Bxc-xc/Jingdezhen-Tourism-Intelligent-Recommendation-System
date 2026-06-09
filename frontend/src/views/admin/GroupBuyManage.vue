<template>
  <div class="admin-group-buy">
    <div class="page-header">
      <div style="display:flex;align-items:center;gap:12px">
        <el-button :icon="ArrowLeft" circle @click="router.go(-1)" />
        <h2>陶瓷工坊团购管理</h2>
      </div>
      <div class="header-actions">
        <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width:130px" @change="loadList">
          <el-option label="全部" value="" />
          <el-option label="待审核" value="PENDING" />
          <el-option label="已上架" value="APPROVED" />
          <el-option label="已拒绝" value="REJECTED" />
          <el-option label="已下架" value="OFFLINE" />
        </el-select>
        <el-button :icon="Refresh" @click="loadList">刷新</el-button>
      </div>
    </div>

    <el-card>
      <el-table :data="filteredList" v-loading="loading" border stripe>
        <el-table-column label="图片" width="80">
          <template #default="{ row }">
            <el-image v-if="row.image" :src="row.image" style="width:56px;height:40px;border-radius:4px" fit="cover" />
            <span v-else style="color:#c0c4cc;font-size:12px">无图</span>
          </template>
        </el-table-column>
        <el-table-column prop="merchantName" label="商家" width="140" />
        <el-table-column prop="name" label="团购名称" min-width="140" />
        <el-table-column label="团购价" width="90">
          <template #default="{ row }">
            <span style="color:#ff2d2d;font-weight:700">¥{{ row.groupPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column label="原价" width="80">
          <template #default="{ row }">
            <span style="color:#c0c4cc;text-decoration:line-through">¥{{ row.originalPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="70" />
        <el-table-column prop="soldCount" label="已售" width="70" />
        <el-table-column label="有效期至" width="110">
          <template #default="{ row }">{{ row.validEnd }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING'"
              size="small" link type="success"
              @click="audit(row, 'APPROVED')"
            >通过</el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              size="small" link type="danger"
              @click="openReject(row)"
            >拒绝</el-button>
            <el-button
              v-if="row.status === 'APPROVED'"
              size="small" link type="warning"
              @click="audit(row, 'OFFLINE')"
            >下架</el-button>
            <el-button size="small" link type="primary" @click="openEdit(row)">修改</el-button>
            <el-popconfirm title="确认删除该团购？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button size="small" link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 拒绝原因弹窗 -->
    <el-dialog v-model="rejectVisible" title="填写拒绝原因" width="400px" destroy-on-close>
      <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入拒绝原因" />
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认拒绝</el-button>
      </template>
    </el-dialog>

    <!-- 修改弹窗 -->
    <el-dialog v-model="editVisible" title="修改团购" width="560px" destroy-on-close>
      <el-form :model="editForm" ref="editFormRef" label-width="100px">
        <el-form-item label="团购名称" :rules="[{required:true,message:'必填'}]">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="团购图片">
          <el-input v-model="editForm.image" placeholder="图片URL" />
        </el-form-item>
        <el-form-item label="团购价">
          <el-input-number v-model="editForm.groupPrice" :min="0.01" :precision="2" style="width:160px" />
        </el-form-item>
        <el-form-item label="原价">
          <el-input-number v-model="editForm.originalPrice" :min="0.01" :precision="2" style="width:160px" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="editForm.stock" :min="0" style="width:160px" />
        </el-form-item>
        <el-form-item label="有效期">
          <el-date-picker
            v-model="editValidRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="截止"
            value-format="YYYY-MM-DD"
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editForm.status" style="width:160px">
            <el-option label="待审核" value="PENDING" />
            <el-option label="已上架" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="已下架" value="OFFLINE" />
          </el-select>
        </el-form-item>
        <el-form-item label="使用说明">
          <el-input v-model="editForm.usageDesc" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="详细介绍">
          <el-input v-model="editForm.detail" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleEditSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Refresh, ArrowLeft } from '@element-plus/icons-vue'
import {
  adminGetAllGroupBuys,
  adminAuditGroupBuy,
  adminUpdateGroupBuy,
  adminDeleteGroupBuy
} from '../../api/groupBuy'

const router = useRouter()
const loading = ref(false)
const list = ref([])
const filterStatus = ref('')
const rejectVisible = ref(false)
const rejectReason = ref('')
const rejectTarget = ref(null)
const editVisible = ref(false)
const submitting = ref(false)
const editFormRef = ref(null)
const editValidRange = ref([])

const editForm = ref({
  name: '', image: '', groupPrice: 0, originalPrice: 0,
  stock: 0, validStart: '', validEnd: '', status: 'PENDING',
  usageDesc: '', detail: ''
})

const filteredList = computed(() => {
  if (!filterStatus.value) return list.value
  return list.value.filter(i => i.status === filterStatus.value)
})

const statusLabel = (s) => ({ PENDING: '待审核', APPROVED: '已上架', REJECTED: '已拒绝', OFFLINE: '已下架' }[s] || s)
const statusType = (s) => ({ PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger', OFFLINE: 'info' }[s] || '')

const loadList = async () => {
  loading.value = true
  try {
    const res = await adminGetAllGroupBuys()
    list.value = res?.data?.data || res?.data || []
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const audit = async (row, status) => {
  try {
    await adminAuditGroupBuy(row.id, status, null)
    ElMessage.success('操作成功')
    await loadList()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const openReject = (row) => {
  rejectTarget.value = row
  rejectReason.value = ''
  rejectVisible.value = true
}

const confirmReject = async () => {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请填写拒绝原因')
    return
  }
  try {
    await adminAuditGroupBuy(rejectTarget.value.id, 'REJECTED', rejectReason.value)
    ElMessage.success('已拒绝')
    rejectVisible.value = false
    await loadList()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const openEdit = (row) => {
  editForm.value = {
    name: row.name,
    image: row.image || '',
    groupPrice: parseFloat(row.groupPrice),
    originalPrice: parseFloat(row.originalPrice),
    stock: row.stock,
    validStart: row.validStart || '',
    validEnd: row.validEnd || '',
    status: row.status,
    usageDesc: row.usageDesc || '',
    detail: row.detail || ''
  }
  editValidRange.value = [row.validStart || '', row.validEnd || '']
  editForm.value._id = row.id
  editVisible.value = true
}

const handleEditSubmit = async () => {
  if (editValidRange.value && editValidRange.value.length === 2) {
    editForm.value.validStart = editValidRange.value[0]
    editForm.value.validEnd = editValidRange.value[1]
  }
  submitting.value = true
  try {
    await adminUpdateGroupBuy(editForm.value._id, editForm.value)
    ElMessage.success('修改成功')
    editVisible.value = false
    await loadList()
  } catch (e) {
    ElMessage.error('修改失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await adminDeleteGroupBuy(id)
    ElMessage.success('删除成功')
    await loadList()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

onMounted(loadList)
</script>

<style scoped>
.admin-group-buy { padding: 20px; }
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.page-header h2 { margin: 0; font-size: 22px; color: #303133; }
.header-actions { display: flex; gap: 12px; align-items: center; }
</style>
