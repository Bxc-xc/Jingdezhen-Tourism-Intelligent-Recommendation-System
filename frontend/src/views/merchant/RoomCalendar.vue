<template>
  <div class="room-calendar">
    <div class="page-header">
      <el-button :icon="ArrowLeft" circle @click="goBack" style="margin-right:12px" />
      <h2>房态管理</h2>
    </div>
    <el-card class="selector-card">
      <el-form :inline="true">
        <el-form-item label="选择日期">
          <el-date-picker
            v-model="selectedDate"
            type="date"
            placeholder="请选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveAvailability">保存房态</el-button>
          <el-button @click="clearAvailability" style="margin-left: 8px;">清空当日</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <el-card class="calendar-card">
      <el-calendar v-model="calendarDate" />
    </el-card>
    <el-card class="availability-card">
      <template #header>
        <div class="card-header">
          <span>{{ selectedDate || today }} 房态</span>
        </div>
      </template>
      <el-table :data="roomTypeRows" style="width: 100%">
        <el-table-column prop="name" label="房型名称" />
        <el-table-column prop="price" label="基础价格" width="120">
          <template #default="scope">
            ¥{{ scope.row.price }}
          </template>
        </el-table-column>
        <el-table-column label="可售间数" width="160">
          <template #default="scope">
            <el-input-number v-model="availabilityForm[scope.row.id].available" :min="0" />
          </template>
        </el-table-column>
        <el-table-column label="当日价格" width="160">
          <template #default="scope">
            <el-input-number v-model="availabilityForm[scope.row.id].price" :min="0" :precision="2" />
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()
const goBack = () => router.go(-1)
import { useUserStore } from '../../stores/user'
import { getMerchantByUserId } from '../../api/merchant'
import { getRoomTypes } from '../../api/roomType'

const userStore = useUserStore()
const merchantInfo = ref(null)
const roomTypes = ref([])

const today = new Date().toISOString().split('T')[0]
const selectedDate = ref(today)
const calendarDate = ref(new Date())

const availabilityForm = reactive({})

const storageKey = computed(() => {
  if (!merchantInfo.value?.id) return ''
  return `room_calendar_${merchantInfo.value.id}`
})

const roomTypeRows = computed(() => {
  return Array.isArray(roomTypes.value) ? roomTypes.value : []
})

const loadMerchant = async () => {
  const res = await getMerchantByUserId(userStore.user.id)
  if (res?.success && res.data) {
    merchantInfo.value = res.data
  }
}

const loadRoomTypes = async () => {
  if (!merchantInfo.value?.id) return
  const res = await getRoomTypes(merchantInfo.value.id)
  if (res?.success) {
    roomTypes.value = Array.isArray(res.data) ? res.data : []
    roomTypes.value.forEach(rt => {
      if (!availabilityForm[rt.id]) {
        availabilityForm[rt.id] = { available: 0, price: rt.price }
      }
    })
    restoreAvailability()
  }
}

const restoreAvailability = () => {
  if (!storageKey.value || !selectedDate.value) return
  const raw = localStorage.getItem(storageKey.value)
  if (!raw) return
  let saved = {}
  try { saved = JSON.parse(raw) || {} } catch { saved = {} }
  const dayData = saved[selectedDate.value] || {}
  roomTypeRows.value.forEach(rt => {
    const cur = dayData[rt.id] || {}
    availabilityForm[rt.id] = {
      available: Number(cur.available || 0),
      price: Number(cur.price || rt.price || 0)
    }
  })
}

const saveAvailability = () => {
  if (!storageKey.value || !selectedDate.value) return
  const raw = localStorage.getItem(storageKey.value)
  let saved = {}
  try { saved = raw ? JSON.parse(raw) || {} : {} } catch { saved = {} }
  const dayData = {}
  roomTypeRows.value.forEach(rt => {
    const cur = availabilityForm[rt.id] || {}
    dayData[rt.id] = {
      available: Number(cur.available || 0),
      price: Number(cur.price || rt.price || 0)
    }
  })
  saved[selectedDate.value] = dayData
  localStorage.setItem(storageKey.value, JSON.stringify(saved))
  ElMessage.success('房态已保存')
}

const clearAvailability = () => {
  if (!storageKey.value || !selectedDate.value) return
  const raw = localStorage.getItem(storageKey.value)
  let saved = {}
  try { saved = raw ? JSON.parse(raw) || {} : {} } catch { saved = {} }
  delete saved[selectedDate.value]
  localStorage.setItem(storageKey.value, JSON.stringify(saved))
  roomTypeRows.value.forEach(rt => {
    availabilityForm[rt.id] = { available: 0, price: rt.price }
  })
  ElMessage.success('已清空当日房态')
}

onMounted(async () => {
  await loadMerchant()
  await loadRoomTypes()
})
</script>

<style scoped>
.room-calendar {
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
.selector-card {
  margin-bottom: 16px;
}
.calendar-card {
  margin-bottom: 16px;
}
</style>
