<template>
  <div class="room-type-manage">
    <div class="page-header">
      <div style="display:flex;align-items:center;gap:12px">
        <el-button :icon="ArrowLeft" circle @click="router.go(-1)" />
        <h2>酒店房型管理</h2>
      </div>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>
        新增房型
      </el-button>
    </div>

    <!-- 筛选栏 -->
    <el-card class="filter-card">
      <el-row :gutter="16" align="middle">
        <el-col :span="8">
          <el-input
            v-model="filterKeyword"
            placeholder="搜索房型名称 / 酒店名称"
            clearable
            @input="applyFilter"
          />
        </el-col>
        <el-col :span="8">
          <el-select
            v-model="filterMerchantId"
            placeholder="按酒店筛选"
            clearable
            style="width: 100%"
            @change="applyFilter"
          >
            <el-option
              v-for="m in hotelMerchants"
              :key="m.id"
              :label="m.shopName"
              :value="m.id"
            />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button @click="resetFilter">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card>
      <el-table :data="filteredRoomTypes" style="width: 100%" v-loading="loading">
        <el-table-column label="图片" width="80">
          <template #default="scope">
            <el-image
              v-if="scope.row.images"
              :src="scope.row.images"
              fit="cover"
              style="width: 50px; height: 50px; border-radius: 4px;"
            />
            <div v-else style="width: 50px; height: 50px; background: #f5f7fa; border-radius: 4px; display: flex; justify-content: center; align-items: center;">
              <el-icon><Picture /></el-icon>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="所属酒店" width="160">
          <template #default="scope">
            {{ scope.row.merchant?.shopName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="name" label="房型名称" />
        <el-table-column label="价格" width="100">
          <template #default="scope">¥{{ scope.row.price }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="70" />
        <el-table-column prop="bedType" label="床型" width="100" />
        <el-table-column prop="area" label="面积" width="90" />
        <el-table-column label="含早" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.breakfast ? 'success' : 'info'" size="small">
              {{ scope.row.breakfast ? '含早' : '不含' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="openDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="所属酒店" required>
          <el-select v-model="form.merchantId" placeholder="请选择酒店" style="width: 100%" :disabled="!!form.id">
            <el-option
              v-for="m in hotelMerchants"
              :key="m.id"
              :label="m.shopName"
              :value="m.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="房型名称" required>
          <el-input v-model="form.name" placeholder="例如：豪华大床房" />
        </el-form-item>
        <el-form-item label="价格" required>
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="库存" required>
          <el-input-number v-model="form.stock" :min="0" :precision="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="床型">
          <el-select v-model="form.bedType" placeholder="请选择床型" style="width: 100%">
            <el-option label="单人床" value="单人床" />
            <el-option label="双人床" value="双人床" />
            <el-option label="大床" value="大床" />
            <el-option label="双床" value="双床" />
            <el-option label="亲子床" value="亲子床" />
          </el-select>
        </el-form-item>
        <el-form-item label="面积">
          <el-input v-model="form.area" placeholder="例如：30㎡" />
        </el-form-item>
        <el-form-item label="是否含早">
          <el-radio-group v-model="form.breakfast">
            <el-radio :value="1">含早</el-radio>
            <el-radio :value="0">不含早</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入房型描述" />
        </el-form-item>
        <el-form-item label="房型图片">
          <el-upload
            class="room-image-uploader"
            action=""
            :show-file-list="false"
            :http-request="customUpload"
            :before-upload="beforeUpload"
          >
            <div class="upload-box">
              <img v-if="form.images" :src="form.images" class="uploaded-img" />
              <el-icon v-else class="upload-icon"><Plus /></el-icon>
            </div>
          </el-upload>
          <el-button v-if="form.images" link type="danger" @click="form.images = ''">移除图片</el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Picture, ArrowLeft } from '@element-plus/icons-vue'
import { adminGetAllRoomTypes, adminCreateRoomType, adminUpdateRoomType, adminDeleteRoomType } from '../../api/roomType'
import { getAllMerchants } from '../../api/merchant'
import request from '../../utils/request'

const router = useRouter()

const loading = ref(false)
const roomTypes = ref([])
const hotelMerchants = ref([])
const filterKeyword = ref('')
const filterMerchantId = ref(null)
const dialogVisible = ref(false)
const dialogTitle = ref('新增房型')

const form = reactive({
  id: null,
  merchantId: null,
  name: '',
  price: 0,
  stock: 1,
  bedType: '',
  area: '',
  breakfast: 0,
  description: '',
  images: ''
})

const filteredRoomTypes = computed(() => {
  return roomTypes.value.filter(rt => {
    const kw = filterKeyword.value.trim().toLowerCase()
    const matchKw = !kw ||
      rt.name?.toLowerCase().includes(kw) ||
      rt.merchant?.shopName?.toLowerCase().includes(kw)
    const matchMerchant = !filterMerchantId.value || rt.merchant?.id === filterMerchantId.value
    return matchKw && matchMerchant
  })
})

const applyFilter = () => {} // computed 自动响应
const resetFilter = () => {
  filterKeyword.value = ''
  filterMerchantId.value = null
}

const fetchAll = async () => {
  loading.value = true
  try {
    const res = await adminGetAllRoomTypes()
    if (res.success) roomTypes.value = res.data || []
  } catch (e) {
    ElMessage.error('加载房型失败')
  } finally {
    loading.value = false
  }
}

const fetchMerchants = async () => {
  try {
    const res = await getAllMerchants()
    if (res.success) {
      hotelMerchants.value = (res.data || []).filter(m => m.category === 'HOTEL')
    }
  } catch (e) {
    console.error('加载商家列表失败', e)
  }
}

const openDialog = (row = null) => {
  if (row) {
    dialogTitle.value = '编辑房型'
    Object.assign(form, {
      id: row.id,
      merchantId: row.merchant?.id,
      name: row.name,
      price: row.price,
      stock: row.stock ?? 0,
      bedType: row.bedType || '',
      area: row.area || '',
      breakfast: row.breakfast ?? 0,
      description: row.description || '',
      images: row.images || ''
    })
  } else {
    dialogTitle.value = '新增房型'
    Object.assign(form, { id: null, merchantId: null, name: '', price: 0, stock: 1, bedType: '', area: '', breakfast: 0, description: '', images: '' })
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!form.merchantId && !form.id) {
    ElMessage.warning('请选择所属酒店')
    return
  }
  if (!form.name || form.price == null) {
    ElMessage.warning('请填写房型名称和价格')
    return
  }
  try {
    const payload = {
      name: form.name,
      price: form.price,
      stock: form.stock,
      bedType: form.bedType,
      area: form.area,
      breakfast: form.breakfast,
      description: form.description,
      images: form.images
    }
    if (form.id) {
      await adminUpdateRoomType(form.id, payload)
      ElMessage.success('更新成功')
    } else {
      await adminCreateRoomType(form.merchantId, payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchAll()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该房型吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await adminDeleteRoomType(id)
    ElMessage.success('删除成功')
    fetchAll()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const beforeUpload = (file) => {
  if (!file.type.startsWith('image/')) { ElMessage.error('只能上传图片'); return false }
  if (file.size > 5 * 1024 * 1024) { ElMessage.error('图片不能超过5MB'); return false }
  return true
}

const customUpload = async (options) => {
  const { file, onSuccess, onError } = options
  try {
    const formData = new FormData()
    formData.append('file', file.raw || file)
    const res = await request.post('/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
    const url = res.url || res.data?.url
    if (res.success && url) {
      form.images = url
      onSuccess({ url })
      ElMessage.success('上传成功')
    } else {
      throw new Error(res.message || '上传失败')
    }
  } catch (e) {
    ElMessage.error('图片上传失败')
    onError(e)
  }
}

onMounted(() => {
  fetchAll()
  fetchMerchants()
})
</script>

<style scoped>
.room-type-manage {
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.page-header h2 { margin: 0; color: #303133; }
.filter-card { margin-bottom: 16px; }
.upload-box {
  width: 120px;
  height: 120px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  overflow: hidden;
}
.upload-box:hover { border-color: #409eff; }
.uploaded-img { width: 100%; height: 100%; object-fit: cover; }
.upload-icon { font-size: 28px; color: #8c939d; }
</style>
