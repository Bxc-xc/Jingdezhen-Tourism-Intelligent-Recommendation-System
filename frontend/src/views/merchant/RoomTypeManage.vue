<template>
  <div class="room-type-manage">
    <div class="page-header">
      <el-button :icon="ArrowLeft" circle @click="goBack" style="margin-right:12px" />
      <h2>房型管理</h2>
      <el-button type="primary" @click="openRoomTypeDialog()">
        <el-icon><Plus /></el-icon>
        添加房型
      </el-button>
    </div>

    <el-card class="room-types-card">
      <el-table :data="roomTypes" style="width: 100%" v-loading="loading">
        <el-table-column label="图片" width="100">
          <template #default="scope">
            <el-image 
              v-if="scope.row.images" 
              :src="scope.row.images" 
              :preview-src-list="[scope.row.images]" 
              fit="cover" 
              style="width: 60px; height: 60px; border-radius: 4px;"
            />
            <div v-else style="width: 60px; height: 60px; background: #f5f7fa; border-radius: 4px; display: flex; justify-content: center; align-items: center; color: #909399;">
              <el-icon><Picture /></el-icon>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="房型名称" />
        <el-table-column prop="price" label="价格" width="120">
            <template #default="scope">¥{{ scope.row.price }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="bedType" label="床型" width="120" />
        <el-table-column prop="area" label="面积" width="100" />
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
            <el-button size="small" @click="openRoomTypeDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDeleteRoomType(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 房型编辑对话框 -->
    <el-dialog v-model="showRoomTypeDialog" :title="roomTypeDialogTitle" width="500px">
        <el-form :model="roomTypeForm" label-width="100px">
            <el-form-item label="房型名称" required>
                <el-input v-model="roomTypeForm.name" placeholder="例如：豪华大床房" />
            </el-form-item>
            <el-form-item label="价格" required>
                <el-input-number v-model="roomTypeForm.price" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
            <el-form-item label="库存" required>
                <el-input-number v-model="roomTypeForm.stock" :min="0" :precision="0" style="width: 100%" />
            </el-form-item>
            <el-form-item label="床型">
                <el-select v-model="roomTypeForm.bedType" placeholder="请选择床型" style="width: 100%">
                    <el-option label="单人床" value="单人床" />
                    <el-option label="双人床" value="双人床" />
                    <el-option label="大床" value="大床" />
                    <el-option label="双床" value="双床" />
                    <el-option label="亲子床" value="亲子床" />
                </el-select>
            </el-form-item>
            <el-form-item label="面积">
                <el-input v-model="roomTypeForm.area" placeholder="例如：30㎡" />
            </el-form-item>
            <el-form-item label="是否含早">
                <el-radio-group v-model="roomTypeForm.breakfast">
                    <el-radio :value="1">含早</el-radio>
                    <el-radio :value="0">不含早</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item label="描述">
                <el-input v-model="roomTypeForm.description" type="textarea" placeholder="请输入房型描述" />
            </el-form-item>
            <el-form-item label="房型图片">
                <el-upload
                    class="room-image-uploader"
                    action=""
                    :show-file-list="false"
                    :http-request="customImageUpload"
                    :before-upload="beforeImageUpload"
                    :on-success="handleImageSuccess"
                >
                    <div v-if="roomTypeForm.images" class="image-upload-container">
                        <img :src="roomTypeForm.images" class="uploaded-image" />
                        <div class="image-actions" @click.stop>
                            <el-icon class="delete-icon" @click="removeImage"><Delete /></el-icon>
                        </div>
                    </div>
                    <div v-else class="image-upload-container">
                        <el-icon class="image-upload-icon"><Plus /></el-icon>
                    </div>
                </el-upload>
            </el-form-item>
        </el-form>
        <template #footer>
            <el-button @click="showRoomTypeDialog = false">取消</el-button>
            <el-button type="primary" @click="saveRoomType">保存</el-button>
        </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()
const goBack = () => router.go(-1)
import { getMerchantByUserId } from '../../api/merchant'
import { getRoomTypes, createRoomType, updateRoomType, deleteRoomType as deleteRoomTypeApi } from '../../api/roomType'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, Picture } from '@element-plus/icons-vue'
import request from '../../utils/request'

const userStore = useUserStore()
const merchantInfo = ref(null)
const roomTypes = ref([])
const loading = ref(false)
const showRoomTypeDialog = ref(false)
const roomTypeDialogTitle = ref('添加房型')

const roomTypeForm = reactive({
    id: null,
    name: '',
    price: 0,
    stock: 1,
    bedType: '',
    area: '',
    breakfast: 0,
    description: '',
    images: ''
})

// 图片上传相关
const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB！')
    return false
  }
  return true
}

const customImageUpload = async (options) => {
  const { file, onSuccess, onError } = options
  try {
    const formData = new FormData()
    formData.append('file', file.raw || file)
    
    const response = await request.post('/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    const url = response.url || response.data?.url
    if (response.success && url) {
      roomTypeForm.images = url
      onSuccess({ url: url })
      ElMessage.success('图片上传成功')
    } else {
      throw new Error(response.message || '上传失败')
    }
  } catch (error) {
    console.error('图片上传失败:', error)
    ElMessage.error('图片上传失败，请重试')
    onError(error)
  }
}

const handleImageSuccess = (response) => {
  if (response && response.url) {
    roomTypeForm.images = response.url
  }
}

const removeImage = () => {
  roomTypeForm.images = ''
}

const loadMerchantInfo = async () => {
  try {
    if (!userStore.user?.id) return
    const res = await getMerchantByUserId(userStore.user.id)
    if (res.success && res.data) {
      merchantInfo.value = res.data
      if (merchantInfo.value.category === 'HOTEL') {
        fetchRoomTypes()
      } else {
        ElMessage.warning('当前账号不是酒店商家，无法管理房型')
      }
    }
  } catch (error) {
    console.error('加载商家信息失败', error)
  }
}

const fetchRoomTypes = async () => {
    if (!merchantInfo.value?.id) return
    loading.value = true
    try {
        const res = await getRoomTypes(merchantInfo.value.id)
        if (res.success) {
            roomTypes.value = res.data || []
        }
    } catch (error) {
        console.error('获取房型失败', error)
        ElMessage.error('获取房型列表失败')
    } finally {
        loading.value = false
    }
}

const openRoomTypeDialog = (roomType = null) => {
    if (roomType) {
        roomTypeDialogTitle.value = '编辑房型'
        Object.assign(roomTypeForm, {
            ...roomType,
            images: roomType.images || ''
        })
    } else {
        roomTypeDialogTitle.value = '添加房型'
        Object.assign(roomTypeForm, {
            id: null,
            name: '',
            price: 0,
            stock: 1,
            bedType: '',
            area: '',
            breakfast: 0,
            description: '',
            images: ''
        })
    }
    showRoomTypeDialog.value = true
}

const saveRoomType = async () => {
    if (!roomTypeForm.name || !roomTypeForm.price) {
        ElMessage.warning('请填写房型名称和价格')
        return
    }
    try {
        if (roomTypeForm.id) {
            await updateRoomType(roomTypeForm.id, roomTypeForm)
            ElMessage.success('房型更新成功')
        } else {
            await createRoomType(merchantInfo.value.id, roomTypeForm)
            ElMessage.success('房型创建成功')
        }
        showRoomTypeDialog.value = false
        fetchRoomTypes()
    } catch (error) {
        console.error('保存房型失败', error)
        ElMessage.error(error.response?.data?.message || '保存失败')
    }
}

const handleDeleteRoomType = async (id) => {
    try {
        await ElMessageBox.confirm('确定要删除该房型吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        })
        await deleteRoomTypeApi(id)
        ElMessage.success('房型删除成功')
        fetchRoomTypes()
    } catch (error) {
        if (error !== 'cancel') {
            console.error('删除房型失败', error)
            ElMessage.error('删除失败')
        }
    }
}

onMounted(() => {
    loadMerchantInfo()
})
</script>

<style scoped>
.room-type-manage {
  padding: 20px;
  background: white;
  min-height: 100%;
  max-width: 1400px;
  margin: 0 auto;
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

.image-upload-container {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 178px;
  height: 178px;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: border-color 0.3s;
}

.image-upload-container:hover {
  border-color: #409eff;
}

.uploaded-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-upload-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.image-actions {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.image-upload-container:hover .image-actions {
  opacity: 1;
}

.delete-icon {
  color: white;
  font-size: 20px;
  cursor: pointer;
}
</style>
