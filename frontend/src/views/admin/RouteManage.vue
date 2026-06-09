<template>
  <div class="route-manage">
    <div class="page-header">
      <h2>推荐路线管理</h2>
      <el-button type="primary" @click="showAddDialog = true" :icon="Plus">
        新增路线
      </el-button>
    </div>

    <!-- 路线列表 -->
    <el-table :data="routes" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="路线名称" width="200" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column prop="days" label="天数" width="80" align="center" />
      <el-table-column prop="difficulty" label="难度" width="100" align="center" />
      <el-table-column prop="companionType" label="同行伙伴" width="120" />
      <el-table-column prop="stylePreference" label="风格偏好" width="150" show-overflow-tooltip />
      <el-table-column prop="pacePreference" label="行程节奏" width="100" align="center" />
      <el-table-column prop="totalPrice" label="预估价格" width="100" align="center">
        <template #default="{ row }">
          ¥{{ row.totalPrice }}
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="浏览次数" width="100" align="center" />
      <el-table-column prop="useCount" label="使用次数" width="100" align="center" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="editRoute(row)">编辑</el-button>
          <el-button type="danger" size="small" @click="deleteRoute(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="editingRoute ? '编辑路线' : '新增路线'"
      width="800px"
      @close="resetForm"
    >
      <el-form :model="routeForm" label-width="120px" ref="formRef">
        <el-form-item label="路线名称" required>
          <el-input v-model="routeForm.name" placeholder="请输入路线名称" />
        </el-form-item>

        <el-form-item label="路线描述" required>
          <el-input
            v-model="routeForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入路线描述"
          />
        </el-form-item>

        <el-form-item label="封面图片">
          <el-input v-model="routeForm.coverImage" placeholder="请输入图片路径或URL" />
          <div class="form-tip">例如：/images/backgrounds/banner1.jpg</div>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="天数" required>
              <el-input-number
                v-model="routeForm.days"
                :min="1"
                :max="15"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="难度">
              <el-select v-model="routeForm.difficulty" style="width: 100%">
                <el-option label="轻松" value="轻松" />
                <el-option label="中等" value="中等" />
                <el-option label="困难" value="困难" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="预估价格">
              <el-input-number
                v-model="routeForm.totalPrice"
                :min="0"
                :step="10"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="同行伙伴">
          <el-select v-model="routeForm.companionType" placeholder="请选择" style="width: 100%">
            <el-option label="独自出行" value="独自出行" />
            <el-option label="家庭出行" value="家庭出行" />
            <el-option label="情侣出行" value="情侣出行" />
            <el-option label="朋友出行" value="朋友出行" />
            <el-option label="老人同行" value="老人同行" />
          </el-select>
        </el-form-item>

        <el-form-item label="风格偏好">
          <el-checkbox-group v-model="routeForm.stylePreferenceArray">
            <el-checkbox label="文化体验" />
            <el-checkbox label="经典必去" />
            <el-checkbox label="自然风光" />
            <el-checkbox label="城市景观" />
            <el-checkbox label="历史古迹" />
          </el-checkbox-group>
        </el-form-item>

        <el-form-item label="行程节奏">
          <el-radio-group v-model="routeForm.pacePreference">
            <el-radio label="紧凑" />
            <el-radio label="适中" />
            <el-radio label="宽松" />
          </el-radio-group>
        </el-form-item>

        <el-form-item label="标签">
          <el-input
            v-model="routeForm.tags"
            placeholder="多个标签用逗号分隔，例如：文化,陶瓷,体验"
          />
        </el-form-item>

        <el-form-item label="路线详情">
          <el-input
            v-model="routeForm.routeDetails"
            type="textarea"
            :rows="6"
            placeholder="请输入JSON格式的路线详情"
          />
          <div class="form-tip">
            格式示例：[{"day":1,"title":"第一天","schedule":[{"time":"09:00","location":"景点名称","action":"活动描述","duration":120,"cost":50}]}]
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveRoute" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getRecommendRoutes, createRoute, updateRoute, deleteRoute as deleteRouteAPI } from '@/api/route'

const loading = ref(false)
const saving = ref(false)
const showAddDialog = ref(false)
const editingRoute = ref(null)
const routes = ref([])
const formRef = ref(null)

const routeForm = reactive({
  name: '',
  description: '',
  coverImage: '',
  days: 3,
  difficulty: '中等',
  tags: '',
  companionType: '',
  stylePreferenceArray: [],
  pacePreference: '适中',
  routeDetails: '',
  totalPrice: 0
})

const loadRoutes = async () => {
  loading.value = true
  try {
    const response = await getRecommendRoutes()
    if (response.success) {
      routes.value = response.data || []
    }
  } catch (error) {
    console.error('加载路线失败:', error)
    ElMessage.error('加载路线失败')
  } finally {
    loading.value = false
  }
}

const editRoute = (route) => {
  editingRoute.value = route
  routeForm.name = route.name
  routeForm.description = route.description
  routeForm.coverImage = route.coverImage || ''
  routeForm.days = route.days
  routeForm.difficulty = route.difficulty || '中等'
  routeForm.tags = route.tags || ''
  routeForm.companionType = route.companionType || ''
  routeForm.stylePreferenceArray = route.stylePreference ? route.stylePreference.split(',') : []
  routeForm.pacePreference = route.pacePreference || '适中'
  routeForm.routeDetails = typeof route.routeDetails === 'string' ? route.routeDetails : JSON.stringify(route.routeDetails, null, 2)
  routeForm.totalPrice = route.totalPrice || 0
  showAddDialog.value = true
}

const saveRoute = async () => {
  if (!routeForm.name) {
    ElMessage.warning('请输入路线名称')
    return
  }

  if (!routeForm.description) {
    ElMessage.warning('请输入路线描述')
    return
  }

  saving.value = true
  try {
    const data = {
      name: routeForm.name,
      description: routeForm.description,
      coverImage: routeForm.coverImage,
      days: routeForm.days,
      difficulty: routeForm.difficulty,
      tags: routeForm.tags,
      companionType: routeForm.companionType,
      stylePreference: routeForm.stylePreferenceArray.join(','),
      pacePreference: routeForm.pacePreference,
     