<template>
  <div class="profile">
    <Header />
    
    <div class="container">
      <!-- 返回键 -->
      <div class="back-button-container">
        <el-button @click="goBack" :icon="ArrowLeft" circle />
      </div>
      
      <div class="profile-content">
        <!-- 侧边栏 -->
        <div class="profile-sidebar">
          <el-card class="user-card" shadow="hover" :body-style="{ padding: '0' }">
            <div class="user-card-header"></div>
            <div class="user-info">
              <div class="avatar-container">
                <el-avatar :size="100" :src="getAvatarUrl(userStore.user?.avatar)" class="user-avatar">
                  {{ userStore.user?.username?.charAt(0) }}
                </el-avatar>
                <el-upload
                  class="avatar-uploader"
                  :action="uploadAction"
                  :show-file-list="false"
                  :before-upload="beforeAvatarUpload"
                  :on-success="handleAvatarSuccess"
                  :on-error="handleAvatarError"
                  :headers="uploadHeaders"
                >
                  <el-button type="primary" size="small" circle class="upload-btn">
                    <el-icon><Upload /></el-icon>
                  </el-button>
                </el-upload>
              </div>
              <h3>{{ userStore.user?.username }}</h3>
              <p class="user-role">
                <el-tag size="small" effect="plain" round>
                  {{
                    userStore.user?.role === 'TOURIST'
                      ? $t('role.tourist')
                      : userStore.user?.role === 'MERCHANT'
                        ? $t('role.merchant')
                        : userStore.user?.role === 'ADMIN'
                          ? $t('role.admin')
                          : userStore.user?.role
                  }}
                </el-tag>
              </p>
            </div>
          </el-card>
          
          <el-menu
            :default-active="activeTab"
            class="profile-menu"
            @select="handleMenuSelect"
          >
            <el-menu-item index="info">
              <el-icon><User /></el-icon>
              <span>{{ $t('profile.menu.info') }}</span>
            </el-menu-item>
            <el-menu-item index="favorites">
              <el-icon><Star /></el-icon>
              <span>{{ $t('profile.menu.favorites') }}</span>
            </el-menu-item>
            <el-menu-item index="reviews">
              <el-icon><ChatDotRound /></el-icon>
              <span>{{ $t('profile.menu.reviews') }}</span>
            </el-menu-item>
            <el-menu-item index="orders">
              <el-icon><ShoppingBag /></el-icon>
              <span>{{ $t('profile.menu.orders') }}</span>
            </el-menu-item>
            <el-menu-item index="plans">
              <el-icon><Calendar /></el-icon>
              <span>{{ $t('profile.menu.plans') }}</span>
            </el-menu-item>
            <el-menu-item index="settings">
              <el-icon><Setting /></el-icon>
              <span>{{ $t('profile.menu.settings') }}</span>
            </el-menu-item>
          </el-menu>
        </div>

        <!-- 主内容区 -->
        <div class="profile-main">
          <transition name="slide-up" mode="out-in">
            <div :key="activeTab" class="content-wrapper">
              <!-- 个人信息 -->
              <div v-if="activeTab === 'info'" class="tab-content">
                <el-card class="profile-detail-card">
                  <template #header>
                    <div class="card-header-flex">
                      <span class="card-title">{{ $t('profile.info.title') }}</span>
                      <el-button type="primary" size="small" @click="editMode = true" v-if="!editMode" plain>
                        <el-icon class="el-icon--left"><Edit /></el-icon>{{ $t('common.edit') }}
                      </el-button>
                    </div>
                  </template>
              
              <el-form :model="userForm" label-width="100px" v-if="editMode" ref="userFormRef">
                <el-form-item :label="$t('profile.info.username')" prop="username" :rules="[{ required: true, message: $t('profile.validation.usernameRequired'), trigger: 'blur' }]">
                  <el-input v-model="userForm.username" :placeholder="$t('profile.placeholder.username')" />
                </el-form-item>
                <el-form-item :label="$t('profile.info.phone')" prop="phone" :rules="[{ pattern: /^1[3-9]\d{9}$/, message: $t('profile.validation.phoneInvalid'), trigger: 'blur' }]">
                  <el-input v-model="userForm.phone" :placeholder="$t('profile.placeholder.phone')" />
                </el-form-item>
                <el-form-item :label="$t('profile.info.email')" prop="email" :rules="[{ type: 'email', message: $t('profile.validation.emailInvalid'), trigger: 'blur' }]">
                  <el-input v-model="userForm.email" :placeholder="$t('profile.placeholder.email')" />
                </el-form-item>
                <el-form-item :label="$t('profile.info.bio')" prop="bio">
                  <el-input
                    v-model="userForm.bio"
                    type="textarea"
                    :rows="4"
                    :placeholder="$t('profile.placeholder.bio')"
                    maxlength="500"
                    show-word-limit
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="saveUserInfo" :loading="saving">{{ $t('common.save') }}</el-button>
                  <el-button @click="cancelEdit">{{ $t('common.cancel') }}</el-button>
                </el-form-item>
              </el-form>
              
              <div v-else class="info-display">
                <div class="info-item">
                  <label>{{ $t('profile.info.username') }}：</label>
                  <span>{{ userStore.user?.username }}</span>
                </div>
                <div class="info-item">
                  <label>{{ $t('profile.info.phone') }}：</label>
                  <span>{{ userStore.user?.phone || $t('profile.info.notSet') }}</span>
                </div>
                <div class="info-item">
                  <label>{{ $t('profile.info.email') }}：</label>
                  <span>{{ userStore.user?.email || $t('profile.info.notSet') }}</span>
                </div>
                <div class="info-item">
                  <label>{{ $t('profile.info.registerTime') }}：</label>
                  <span>{{ formatDate(userStore.user?.createdAt || userStore.user?.createTime) }}</span>
                </div>
                <div class="info-item">
                  <label>{{ $t('profile.info.bio') }}：</label>
                  <span>{{ userStore.user?.bio || $t('profile.info.defaultBio') }}</span>
                </div>
              </div>
            </el-card>
          </div>

          <!-- 我的收藏 -->
          <div v-if="activeTab === 'favorites'" class="tab-content">
            <el-card>
              <template #header>
                <span>{{ $t('profile.favorites.title') }} ({{ favorites.length }})</span>
              </template>
              
              <el-row :gutter="20" v-loading="loading">
                <el-col
                  v-for="scenic in favorites"
                  :key="scenic.id"
                  :xs="24" :sm="12" :md="8" :lg="6"
                >
                  <ScenicCard :scenic="scenic" />
                </el-col>
              </el-row>
              
              <el-empty v-if="!loading && favorites.length === 0" :description="$t('profile.favorites.empty')" />
            </el-card>
          </div>

          <!-- 我的评价 -->
          <div v-if="activeTab === 'reviews'" class="tab-content">
            <el-card>
              <template #header>
                <span>{{ $t('profile.reviews.title') }} ({{ reviews.length }})</span>
              </template>
              
              <div class="review-list">
                <div v-for="review in reviews" :key="review.id" class="review-item">
                  <div class="review-header">
                    <h4>{{ review.scenicSpot?.name || $t('profile.reviews.unknownScenic') }}</h4>
                    <el-rate :model-value="review.rating" disabled size="small" />
                  </div>
                  <div class="review-content">{{ review.content }}</div>
                  <div class="review-meta">
                    <span class="review-time">{{ formatDate(review.createTime) }}</span>
                    <el-button size="small" type="danger" @click="deleteReview(review.id)">
                      {{ $t('common.delete') }}
                    </el-button>
                  </div>
                </div>
              </div>
              
              <el-empty v-if="!loading && reviews.length === 0" :description="$t('profile.reviews.empty')" />
            </el-card>
          </div>

          <!-- 我的订单 -->
          <div v-if="activeTab === 'orders'" class="tab-content">
            <!-- 团购订单 -->
            <el-card style="margin-bottom: 20px;">
              <template #header>
                <span>团购订单</span>
              </template>
              <el-table :data="groupBuyOrders" v-loading="loading" style="width: 100%;">
                <el-table-column prop="id" label="订单号" width="80" />
                <el-table-column label="团购名称" min-width="180">
                  <template #default="scope">
                    {{ scope.row.groupBuyName || '—' }}
                  </template>
                </el-table-column>
                <el-table-column label="商家" min-width="140">
                  <template #default="scope">
                    {{ scope.row.merchantName || '—' }}
                  </template>
                </el-table-column>
                <el-table-column label="数量" width="70">
                  <template #default="scope">{{ scope.row.quantity }}</template>
                </el-table-column>
                <el-table-column label="总价" width="110">
                  <template #default="scope">
                    <span style="color: #e6a23c; font-weight: 600;">¥{{ scope.row.totalPrice }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="使用日期" width="120">
                  <template #default="scope">{{ scope.row.useDate }}</template>
                </el-table-column>
                <el-table-column label="状态" width="100">
                  <template #default="scope">
                    <el-tag :type="getGroupBuyStatusType(scope.row.status)" size="small">
                      {{ getGroupBuyStatusText(scope.row.status) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="下单时间" width="170">
                  <template #default="scope">{{ formatDateTime(scope.row.createdAt) }}</template>
                </el-table-column>
              </el-table>
              <el-empty v-if="!loading && groupBuyOrders.length === 0" description="暂无团购订单" :image-size="60" />
            </el-card>

            <!-- 房间预订订单 -->
            <el-card>
              <template #header>
                <span>房间预订</span>
              </template>
              <el-table :data="hotelOrders" v-loading="loading" style="width: 100%;">
                <el-table-column prop="id" label="订单号" width="80" />
                <el-table-column label="酒店" min-width="160">
                  <template #default="scope">
                    {{ scope.row.merchant?.shopName || '—' }}
                  </template>
                </el-table-column>
                <el-table-column label="房型" min-width="140">
                  <template #default="scope">
                    {{ scope.row.roomType?.name || '—' }}
                  </template>
                </el-table-column>
                <el-table-column label="入住/离店" min-width="200">
                  <template #default="scope">
                    {{ scope.row.checkInDate }} 至 {{ scope.row.checkOutDate }}
                  </template>
                </el-table-column>
                <el-table-column label="数量" width="70">
                  <template #default="scope">{{ scope.row.quantity || 1 }}</template>
                </el-table-column>
                <el-table-column label="总价" width="110">
                  <template #default="scope">
                    <span style="color: #e6a23c; font-weight: 600;">¥{{ scope.row.totalPrice || 0 }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="状态" width="100">
                  <template #default="scope">
                    <el-tag :type="getStatusType(scope.row.status)" size="small">
                      {{ getStatusText(scope.row.status) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="160" fixed="right">
                  <template #default="scope">
                    <el-button size="small" @click="viewOrder(scope.row.id)">详情</el-button>
                    <el-button
                      v-if="scope.row.status === 'PENDING'"
                      size="small"
                      type="danger"
                      @click="cancelOrder(scope.row.id)"
                    >取消</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-if="!loading && hotelOrders.length === 0" description="暂无房间预订" :image-size="60" />
            </el-card>
          </div>

          <!-- 我的行程 -->
          <div v-if="activeTab === 'plans'" class="tab-content">
            <el-card>
              <template #header>
                <span>{{ $t('profile.plans.title') }}</span>
                <el-button type="primary" size="small" @click="$router.push('/plan')">
                  {{ $t('profile.plans.create') }}
                </el-button>
              </template>
              
              <div class="plan-list">
                <div v-for="plan in plans" :key="plan.id" class="plan-item">
                  <div class="plan-header">
                    <h4>{{ plan.title }}</h4>
                    <el-tag type="success">{{ plan.days }}{{ $t('profile.plans.daysSuffix') }}</el-tag>
                  </div>
                  <div class="plan-info">
                    <p>{{ getPlanCardDesc(plan) }}</p>
                    <div class="plan-meta">
                      <span>{{ $t('profile.plans.createTime') }}：{{ formatDate(plan.createTime) }}</span>
                      <span>{{ $t('profile.plans.estimatedCost') }}：¥{{ plan.totalCost }}</span>
                    </div>
                  </div>
                  <div class="plan-actions">
                    <el-button size="small" @click="viewPlan(plan.id)">{{ $t('common.view') || $t('profile.action.view') }}</el-button>
                    <el-button size="small" type="primary" @click="goPlanEditor(plan.id)">{{ $t('common.edit') }}</el-button>
                    <el-button size="small" type="danger" @click="deletePlan(plan.id)">{{ $t('common.delete') }}</el-button>
                  </div>
                </div>
              </div>
              
              <el-empty v-if="!loading && plans.length === 0" :description="$t('profile.plans.empty')" />
            </el-card>
          </div>

          <!-- 账户设置 -->
          <div v-if="activeTab === 'settings'" class="tab-content">
            <el-card>
              <template #header>
                <span>{{ $t('profile.settings.title') }}</span>
              </template>
              
              <el-form :model="settingsForm" label-width="120px">
                <el-form-item :label="$t('profile.settings.changePassword')">
                  <el-button @click="showChangePassword = true">{{ $t('profile.settings.changePassword') }}</el-button>
                </el-form-item>
                <el-form-item :label="$t('profile.settings.notification')">
                  <el-switch v-model="settingsForm.emailNotification" :active-text="$t('profile.settings.emailNotification')" />
                  <el-switch v-model="settingsForm.smsNotification" :active-text="$t('profile.settings.smsNotification')" />
                </el-form-item>
                <el-form-item :label="$t('profile.settings.privacy')">
                  <el-switch v-model="settingsForm.profilePublic" :active-text="$t('profile.settings.publicProfile')" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="saveSettings">{{ $t('profile.settings.save') }}</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </div>
            </div>
          </transition>
        </div>
      </div>
    </div>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="showChangePassword" :title="$t('profile.settings.changePassword')" width="400px">
      <el-form :model="passwordForm" label-width="100px">
        <el-form-item :label="$t('profile.password.old')">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item :label="$t('profile.password.new')">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item :label="$t('profile.password.confirm')">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showChangePassword = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="changePassword">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <!-- 行程详情对话框 -->
    <el-dialog v-model="showPlanDetail" :title="$t('profile.planDetail.title')" width="800px">
      <div v-if="currentPlanDetail" class="plan-detail-content">
        <div class="plan-detail-header">
          <h3>{{ currentPlanDetail.title }}</h3>
          <div class="plan-detail-meta">
            <el-tag type="success">{{ currentPlanDetail.days }}{{ $t('profile.plans.daysSuffix') }}</el-tag>
            <span>{{ $t('profile.plans.estimatedCost') }}：¥{{ currentPlanDetail.totalCost }}</span>
            <span>{{ $t('profile.planDetail.startDate') }}：{{ formatDate(currentPlanDetail.startDate) }}</span>
          </div>
        </div>
        <div class="plan-detail-description">
          <p>{{ currentPlanDetail.description }}</p>
        </div>
        <div v-if="parsedPlanDetails" class="plan-detail-timeline">
          <el-timeline>
            <el-timeline-item
              v-for="(day, dayIndex) in parsedPlanDetails.days"
              :key="dayIndex"
              :timestamp="`${$t('plan.dayTitle', { day: dayIndex + 1 })} - ${day.date}`"
              placement="top"
            >
              <el-card>
                <div class="day-schedule">
                  <div
                    v-for="(item, itemIndex) in (day.schedule || [])"
                    :key="itemIndex"
                    class="schedule-item"
                  >
                    <div class="time">{{ item.time }}</div>
                    <div class="content">
                      <h4>{{ item.title || item.location || '未命名活动' }}</h4>
                      <p v-if="item.description">{{ item.description }}</p>
                    </div>
                  </div>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
      <template #footer>
        <el-button @click="showPlanDetail = false">{{ $t('common.close') || $t('profile.action.close') }}</el-button>
        <el-button type="primary" @click="editPlanFromDetail">{{ $t('common.edit') }}</el-button>
      </template>
    </el-dialog>

    <!-- 编辑行程对话框 -->
    <el-dialog v-model="showEditPlan" :title="$t('profile.editPlan.title')" width="600px">
      <el-form :model="editPlanForm" label-width="100px">
        <el-form-item :label="$t('profile.editPlan.labelTitle')">
          <el-input v-model="editPlanForm.title" />
        </el-form-item>
        <el-form-item :label="$t('profile.editPlan.labelDescription')">
          <el-input v-model="editPlanForm.description" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item :label="$t('profile.editPlan.labelDetails')">
          <el-input v-model="editPlanForm.planDetails" type="textarea" :rows="6" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditPlan = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="savePlanEdit">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <!-- 订单详情对话框 -->
    <el-dialog v-model="showOrderDetail" :title="$t('profile.orderDetail.title')" width="600px">
      <div v-if="currentOrderDetail" class="order-detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item :label="$t('profile.orders.orderId')">{{ currentOrderDetail.id }}</el-descriptions-item>
          <el-descriptions-item :label="$t('profile.orderDetail.status')">
            <el-tag :type="getStatusType(currentOrderDetail.status)">
              {{ getStatusText(currentOrderDetail.status) }}
            </el-tag>
          </el-descriptions-item>
          
          <el-descriptions-item :label="$t('profile.orderDetail.merchant')">
            {{ currentOrderDetail.scenicSpot?.name || currentOrderDetail.merchant?.shopName || $t('common.noData') }}
          </el-descriptions-item>
          
          <el-descriptions-item :label="$t('profile.orderDetail.amount')">
            ¥{{ currentOrderDetail.totalPrice || 0 }}
          </el-descriptions-item>

          <template v-if="currentOrderDetail.roomType">
            <el-descriptions-item :label="$t('profile.orderDetail.roomType')">
              {{ currentOrderDetail.roomType.name }}
            </el-descriptions-item>
            <el-descriptions-item :label="$t('profile.orderDetail.roomQuantity')">
              {{ currentOrderDetail.quantity }} {{ $t('profile.orderDetail.roomUnit') }}
            </el-descriptions-item>
            <el-descriptions-item :label="$t('profile.orderDetail.checkInTime')" :span="2">
              {{ currentOrderDetail.checkInDate }} {{ $t('detail.to') }} {{ currentOrderDetail.checkOutDate }}
            </el-descriptions-item>
          </template>

          <el-descriptions-item :label="$t('profile.orderDetail.note')" :span="2" v-if="currentOrderDetail.reservationNote">
            {{ currentOrderDetail.reservationNote }}
          </el-descriptions-item>
          
          <el-descriptions-item :label="$t('profile.orderDetail.createTime')" :span="2">
            {{ formatDateTime(currentOrderDetail.orderTime || currentOrderDetail.createTime) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="showOrderDetail = false">{{ $t('common.close') || $t('profile.action.close') }}</el-button>
      </template>
    </el-dialog>

    <Footer />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowLeft, 
  Upload, 
  User, 
  Star, 
  ChatDotRound, 
  ShoppingBag, 
  Calendar, 
  Setting,
  Edit,
  Male,
  Female,
  Message,
  Iphone
} from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'
import realtimeSync from '../../utils/websocket'
import { getReviewList, createReview, deleteReview as deleteReviewApi, getReviewsByUserId } from '../../api/review'
import { getFavoriteList } from '../../api/scenic'
import { getMerchantFavorites } from '../../api/merchantFavorite'
import { getMarketplaceFavorites } from '../../api/marketplace'
import { getUserPlans, getPlanById, updatePlan, deletePlan as deletePlanApi } from '../../api/plan'
import { getUserOrders, cancelOrder as cancelOrderApi, getOrderDetail } from '../../api/order'
import { getUserGroupBuyOrders } from '../../api/groupBuy'
import { changePassword as changePasswordApi, updateUserInfo } from '../../api/auth'
import Header from '../../components/Layout/Header.vue'
import Footer from '../../components/Layout/Footer.vue'
import ScenicCard from '../../components/ScenicCard.vue'
import { FAVORITE_TYPE } from '../../utils/favoriteType'

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('info')
const editMode = ref(false)
const loading = ref(false)
const saving = ref(false)
const showChangePassword = ref(false)
const userFormRef = ref(null)
const showPlanDetail = ref(false)
const showEditPlan = ref(false)
const showOrderDetail = ref(false)
const currentPlanDetail = ref(null)
const currentOrderDetail = ref(null)
const editPlanForm = ref({
  id: null,
  title: '',
  description: '',
  planDetails: ''
})

// 头像上传相关
const uploadAction = computed(() => {
  const API_BASE_URL = import.meta?.env?.VITE_API_BASE_URL || '/api'
  return `${API_BASE_URL}/upload`
})

const uploadHeaders = computed(() => {
  return {
    Authorization: `Bearer ${userStore.token}`
  }
})

const userForm = ref({
  username: '',
  phone: '',
  email: '',
  bio: ''
})

const settingsForm = ref({
  emailNotification: true,
  smsNotification: false,
  profilePublic: true
})

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const favorites = ref([])
const reviews = ref([])
const orders = ref([])
const groupBuyOrders = ref([])
const plans = ref([])

const handleMenuSelect = (key) => {
  activeTab.value = key
  if (key === 'favorites') {
    loadFavorites()
  } else if (key === 'reviews') {
    loadReviews()
  } else if (key === 'orders') {
    loadOrders()
    loadGroupBuyOrders()
  } else if (key === 'plans') {
    loadPlans()
  }
}

const loadFavorites = async () => {
  loading.value = true
  try {
    const userId = userStore.user?.id
    if (userId) {
      const response = await getFavoriteList(userId)
      const scenicList = (response.data || response || []).map(item => ({
        ...item,
        type: FAVORITE_TYPE.SCENIC,
        category: 'SCENIC'
      }))
      const hotelRes = await getMerchantFavorites(userId)
      const hotelsRaw = hotelRes?.data || hotelRes || []
      const hotelList = (Array.isArray(hotelsRaw) ? hotelsRaw : []).map(m => ({
        id: m.id,
        name: m.shopName,
        shopName: m.shopName,
        image: m.avatar,
        rating: m.rating,
        price: m.startPrice,
        type: FAVORITE_TYPE.MERCHANT,
        category: m.category || 'HOTEL',
        description: ''
      }))

      const marketplaceRes = await getMarketplaceFavorites(userId)
      const marketplacesRaw = marketplaceRes?.data || marketplaceRes || []
      const marketplaceList = (Array.isArray(marketplacesRaw) ? marketplacesRaw : []).map(m => ({
        id: m.id,
        name: m.name,
        image: m.coverImage || m.carouselImages?.split(',')[0] || '',
        rating: null,
        price: null,
        type: 'MARKETPLACE',
        category: 'MARKETPLACE',
        description: m.description || ''
      }))

      favorites.value = [...scenicList, ...hotelList, ...marketplaceList]
    }
  } catch (error) {
    console.error('加载收藏失败:', error)
  } finally {
    loading.value = false
  }
}

const loadReviews = async () => {
  loading.value = true
  try {
    const userId = userStore.user?.id
    if (userId) {
      const response = await getReviewsByUserId(userId)
      reviews.value = response.data || response || []
    } else {
      reviews.value = []
    }
  } catch (error) {
    console.error('加载评价失败:', error)
    ElMessage.error('加载评价失败')
    reviews.value = []
  } finally {
    loading.value = false
  }
}

const loadOrders = async () => {
  loading.value = true
  try {
    const userId = userStore.user?.id
    if (userId) {
      const response = await getUserOrders(userId)
      orders.value = response.data || response || []
    } else {
      orders.value = []
    }
  } catch (error) {
    console.error('加载订单失败:', error)
    orders.value = []
  } finally {
    loading.value = false
  }
}

// 加载团购订单
const loadGroupBuyOrders = async () => {
  const userId = userStore.user?.id
  if (!userId) { groupBuyOrders.value = []; return }
  try {
    const res = await getUserGroupBuyOrders(userId)
    groupBuyOrders.value = res?.data || res || []
  } catch (e) {
    console.error('加载团购订单失败:', e)
    groupBuyOrders.value = []
  }
}

// 房间预订订单（从 orders 中过滤有 roomType 的）
const hotelOrders = computed(() => orders.value.filter(o => o.roomType))

const getGroupBuyStatusType = (status) => {
  const map = { PAID: 'success', USED: 'info', CANCELLED: 'danger', REFUNDED: 'warning' }
  return map[status] || 'info'
}

const getGroupBuyStatusText = (status) => {
  const map = { PAID: '已支付', USED: '已使用', CANCELLED: '已取消', REFUNDED: '已退款' }
  return map[status] || status
}

const loadPlans = async () => {
  loading.value = true
  try {
    const userId = userStore.user?.id
    if (userId) {
      const response = await getUserPlans(userId)
      plans.value = response.data || response
    }
  } catch (error) {
    console.error('加载行程失败:', error)
  } finally {
    loading.value = false
  }
}

const saveUserInfo = async () => {
  // 表单验证
  if (userFormRef.value) {
    try {
      await userFormRef.value.validate()
    } catch (error) {
      ElMessage.warning(t('profile.message.checkForm'))
      return
    }
  }
  
  saving.value = true
  try {
    const response = await updateUserInfo(userForm.value)
    if (response.success) {
      // 更新store中的用户信息
      userStore.user = { ...userStore.user, ...response.data }
      userStore.saveUser()
      editMode.value = false
      ElMessage.success(t('common.success'))
    } else {
      ElMessage.error(response.message || t('profile.message.saveInfoFailed'))
    }
  } catch (error) {
    console.error('保存用户信息失败:', error)
    const errorMsg = error.response?.data?.message || error.message || t('profile.message.saveInfoFailed')
    ElMessage.error(errorMsg)
  } finally {
    saving.value = false
  }
}

// 获取头像URL（处理相对路径和绝对路径）
const getAvatarUrl = (avatar) => {
  if (!avatar) return ''
  // 如果是完整的URL（http或https开头），直接返回
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return avatar
  }
  // 如果是相对路径（以/开头），直接返回（vite代理会处理）
  if (avatar.startsWith('/')) {
    return avatar
  }
  // 其他情况直接返回
  return avatar
}

// 头像上传前的验证
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error(t('profile.message.imageOnly'))
    return false
  }
  if (!isLt5M) {
    ElMessage.error(t('profile.message.imageSizeLimit'))
    return false
  }
  return true
}

// 头像上传成功
const handleAvatarSuccess = async (response, file) => {
  try {
    if (response.success && response.url) {
      // 更新用户头像
      const updateResponse = await updateUserInfo({ avatar: response.url })
      if (updateResponse.success) {
        // 更新store中的用户信息
        userStore.user = { ...userStore.user, avatar: response.url }
        userStore.saveUser()
        ElMessage.success(t('profile.message.avatarUploaded'))
      } else {
        ElMessage.error(updateResponse.message || t('profile.message.avatarUpdateFailed'))
      }
    } else {
      ElMessage.error(response.message || t('common.failed'))
    }
  } catch (error) {
    console.error('更新头像失败:', error)
    ElMessage.error(error.message || t('profile.message.avatarUpdateFailed'))
  }
}

// 头像上传失败
const handleAvatarError = (error) => {
  console.error('头像上传失败:', error)
  ElMessage.error(t('profile.message.avatarUploadRetry'))
}

const cancelEdit = () => {
  editMode.value = false
  // 重置表单数据为当前用户信息
  userForm.value = {
    username: userStore.user?.username || '',
    phone: userStore.user?.phone || '',
    email: userStore.user?.email || '',
    bio: userStore.user?.bio || ''
  }
}

const deleteReview = async (reviewId) => {
  try {
    await deleteReviewApi(reviewId)
    reviews.value = reviews.value.filter(r => r.id !== reviewId)
    ElMessage.success(t('profile.message.deleteSuccess'))
  } catch (error) {
    ElMessage.error(t('profile.message.deleteFailed'))
  }
}

const getStatusText = (status) => {
  const normalizedStatus = (status || '').toString().toLowerCase()
  const texts = {
    pending: t('profile.orderStatus.pending'),
    confirmed: t('profile.orderStatus.confirmed'),
    cancelled: t('profile.orderStatus.cancelled')
  }
  return texts[normalizedStatus] || status
}

const getStatusType = (status) => {
  const normalizedStatus = (status || '').toString().toLowerCase()
  const types = {
    pending: 'warning',
    confirmed: 'success',
    cancelled: 'danger'
  }
  return types[normalizedStatus] || 'info'
}

const viewOrder = async (orderId) => {
  try {
    loading.value = true
    const response = await getOrderDetail(orderId)
    currentOrderDetail.value = response.data || response
    showOrderDetail.value = true
  } catch (error) {
    console.error('查看订单失败:', error)
    ElMessage.error(t('common.failed'))
  } finally {
    loading.value = false
  }
}

const cancelOrder = async (orderId) => {
  try {
    await cancelOrderApi(orderId)
    ElMessage.success(t('profile.message.orderCancelled'))
    loadOrders()
  } catch (error) {
    ElMessage.error(t('profile.message.cancelFailed'))
  }
}

const parsedPlanDetails = computed(() => {
  if (!currentPlanDetail.value || !currentPlanDetail.value.planDetails) {
    return null
  }
  try {
    return JSON.parse(currentPlanDetail.value.planDetails)
  } catch (e) {
    console.error('解析行程详情失败:', e)
    return null
  }
})

const viewPlan = async (planId) => {
  try {
    loading.value = true
    const response = await getPlanById(planId)
    currentPlanDetail.value = response.data || response
    showPlanDetail.value = true
  } catch (error) {
    console.error('加载行程详情失败:', error)
    ElMessage.error(t('profile.message.loadPlanDetailFailed'))
  } finally {
    loading.value = false
  }
}

const editPlanFromDetail = () => {
  if (currentPlanDetail.value) {
    goPlanEditor(currentPlanDetail.value.id)
  }
}

// 跳转到行程规划页进行编辑
const goPlanEditor = (planId) => {
  router.push({ path: '/plan', query: { planId: String(planId) } })
}

const savePlanEdit = async () => {
  try {
    loading.value = true
    await updatePlan(editPlanForm.value.id, {
      title: editPlanForm.value.title,
      description: editPlanForm.value.description,
      planDetails: editPlanForm.value.planDetails
    })
    ElMessage.success(t('profile.message.planUpdated'))
    showEditPlan.value = false
    // 重新加载行程列表
    await loadPlans()
  } catch (error) {
    console.error('更新行程失败:', error)
    ElMessage.error(t('profile.message.planUpdateFailed'))
  } finally {
    loading.value = false
  }
}

const deletePlan = async (planId) => {
  try {
    await ElMessageBox.confirm(t('profile.message.confirmDeletePlan'), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    
    loading.value = true
    await deletePlanApi(planId)
    ElMessage.success(t('profile.message.deleteSuccess'))
    // 重新加载行程列表
    await loadPlans()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除行程失败:', error)
      ElMessage.error(t('profile.message.planDeleteFailed'))
    }
  } finally {
    loading.value = false
  }
}

const saveSettings = async () => {
  try {
    // 账户设置（通知设置、隐私设置）暂时保存到本地存储
    // 如果后端有对应的API，可以在这里调用
    localStorage.setItem('userSettings', JSON.stringify(settingsForm.value))
    ElMessage.success(t('profile.message.settingsSaved'))
  } catch (error) {
    console.error('保存设置失败:', error)
    ElMessage.error(t('profile.message.settingsSaveFailed'))
  }
}

// 计划卡片描述：如果后端把 planDetails 放进了 description（或 description 是 JSON），则生成易读摘要
const getPlanCardDesc = (plan) => {
  if (!plan) return ''
  const desc = plan.description || ''
  const text = typeof desc === 'string' ? desc.trim() : ''
  const looksJson = text.startsWith('{') || text.startsWith('[')
  if (looksJson) {
    try {
      const obj = JSON.parse(text)
      const days = Array.isArray(obj.days) ? obj.days : []
      const activityCount = days.reduce((sum, d) => {
        const arr = Array.isArray(d.schedule) ? d.schedule : []
        return sum + arr.length
      }, 0)
      return `包含 ${days.length} 天，共 ${activityCount} 项安排`
    } catch {
      return '行程数据'
    }
  }
  return desc || 'DIY空白行程，等待您填写'
}

const changePassword = async () => {
  if (!passwordForm.value.oldPassword || !passwordForm.value.newPassword) {
    ElMessage.error(t('profile.validation.fillAll'))
    return
  }
  
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.error(t('profile.validation.passwordMismatch'))
    return
  }
  
  if (passwordForm.value.newPassword.length < 6) {
    ElMessage.error(t('profile.validation.passwordLength'))
    return
  }
  
  try {
    await changePasswordApi({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    ElMessage.success(t('profile.message.passwordChanged'))
    showChangePassword.value = false
    passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } catch (error) {
    console.error('密码修改失败:', error)
    const errorMsg = error.response?.data?.message || error.message || t('profile.message.passwordChangeFailed')
    ElMessage.error(errorMsg)
  }
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString()
}

// 带时间的格式化
const formatDateTime = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString()
}

const goBack = () => {
  router.go(-1)
}

onMounted(() => {
  // 初始化表单数据
  userForm.value = {
    username: userStore.user?.username || '',
    phone: userStore.user?.phone || '',
    email: userStore.user?.email || '',
    bio: userStore.user?.bio || ''
  }
  const savedSettings = localStorage.getItem('userSettings')
  if (savedSettings) {
    try {
      settingsForm.value = { ...settingsForm.value, ...JSON.parse(savedSettings) }
    } catch (e) {
      console.error('加载设置失败:', e)
    }
  }
  // 默认加载一次订单（如果初始就是订单 tab 也能看到）
  if (activeTab.value === 'orders') {
    loadOrders()
    loadGroupBuyOrders()
  }
  
  // 启动实时数据同步
  if (!realtimeSync.isConnected) {
    realtimeSync.connect()
  }
  realtimeSync.subscribe('order', handleRealtimeOrderUpdate)
})

onUnmounted(() => {
  realtimeSync.unsubscribe('order', handleRealtimeOrderUpdate)
})

const handleRealtimeOrderUpdate = (payload) => {
  const { operation, data } = payload
  // 确保数据有效且属于当前用户
  if (!data || (data.userId && data.userId !== userStore.user?.id)) {
    return
  }

  if (operation === 'update') {
    // 更新列表中的订单状态
    const index = orders.value.findIndex(o => o.id === data.id)
    if (index !== -1) {
      // 保留原有对象结构（如关联的 merchant/scenicSpot 对象），仅更新变动字段
      orders.value[index] = { ...orders.value[index], ...data }
      
      // 如果状态变为 confirmed，显示提示
      if (data.status === 'CONFIRMED' && orders.value[index].status !== 'CONFIRMED') {
        ElMessage.success(t('profile.message.orderConfirmed', { id: data.id }))
      }
    }
    
    // 如果当前正在查看该订单详情，也同步更新
    if (currentOrderDetail.value && currentOrderDetail.value.id === data.id) {
      currentOrderDetail.value = { ...currentOrderDetail.value, ...data }
    }
  } else if (operation === 'create') {
    // 如果是新订单，重新加载列表以确保获取完整的关联信息（如商家名称等）
    loadOrders()
  }
}
</script>

<style scoped>
.profile {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.container {
  flex: 1;
  max-width: 1600px;
  margin: 0 auto;
  padding: 40px;
  width: 100%;
}

.back-button-container {
  margin-bottom: 20px;
}

.profile-content {
  display: flex;
  gap: 30px;
  margin-top: 30px;
}

.profile-sidebar {
  width: 320px;
  flex-shrink: 0;
}

.user-card {
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  border-radius: 12px;
}

.user-info {
  text-align: center;
  padding: 0 20px 30px;
  margin-top: -50px;
  position: relative;
  z-index: 1;
}

.user-card-header {
  height: 100px;
  background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
}

.avatar-container {
  position: relative;
  display: inline-block;
  margin-bottom: 12px;
}

.user-avatar {
  width: 100px;
  height: 100px;
  border: 4px solid #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  background: #fff;
}

.avatar-uploader {
  position: absolute;
  bottom: 0;
  right: 0;
}

.avatar-uploader :deep(.el-upload) {
  border: none;
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  overflow: visible;
}

.upload-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: #409eff;
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.upload-btn:hover {
  background-color: #66b1ff;
  transform: scale(1.1);
}

.upload-btn :deep(.el-icon) {
  color: #fff;
  font-size: 16px;
}

.user-info h3 {
  margin: 16px 0 8px 0;
  color: #303133;
  font-size: 20px;
  font-weight: 600;
}

.user-info p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.user-role {
  margin-top: 4px;
}

.profile-menu {
  border-right: none;
  background: transparent;
  margin-top: 20px;
  box-shadow: none;
}

.profile-menu :deep(.el-menu-item) {
  border-radius: 8px;
  margin-bottom: 8px;
  height: 50px;
  line-height: 50px;
  color: #606266;
  border-left: 4px solid transparent;
  padding-left: 20px !important;
  transition: all 0.3s;
}

.profile-menu :deep(.el-menu-item:hover) {
  background-color: rgba(24, 144, 255, 0.08);
  color: #1890ff;
}

.profile-menu :deep(.el-menu-item.is-active) {
  background-color: #e6f7ff;
  color: #1890ff;
  border-left-color: #1890ff;
  font-weight: 500;
}

.profile-menu :deep(.el-icon) {
  font-size: 18px;
  margin-right: 12px;
  vertical-align: middle;
}

/* Transitions */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease-out;
}

.slide-up-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.slide-up-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

.card-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
}

.card-title::before {
  content: '';
  width: 4px;
  height: 16px;
  background: #1890ff;
  margin-right: 8px;
  border-radius: 2px;
}

.profile-main {
  flex: 1;
}

.tab-content {
  min-height: 600px;
}

.tab-content :deep(.el-card__body) {
  padding: 30px;
}

.tab-content :deep(.el-card) {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}

.tab-content :deep(.el-card__header) {
  padding: 20px 30px;
  font-size: 18px;
  font-weight: 600;
  border-bottom: 1px solid #f0f0f0;
}

.info-display {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
  font-size: 15px;
}

.info-item label {
  width: 120px;
  color: #606266;
  font-weight: 500;
  font-size: 15px;
}

.info-item span {
  color: #303133;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-item {
  padding: 24px;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  margin-bottom: 16px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.review-header h4 {
  margin: 0;
  color: #303133;
}

.review-content {
  color: #606266;
  margin-bottom: 12px;
}

.review-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.review-time {
  color: #909399;
  font-size: 12px;
}

.plan-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.plan-item {
  padding: 28px;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  transition: box-shadow 0.3s;
  margin-bottom: 20px;
}

.plan-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.plan-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.plan-header h4 {
  margin: 0;
  color: #303133;
}

.plan-info p {
  color: #606266;
  margin: 0 0 8px 0;
}

.plan-meta {
  display: flex;
  gap: 20px;
  color: #909399;
  font-size: 12px;
}

.plan-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.plan-detail-content {
  padding: 10px 0;
}

.plan-detail-header {
  margin-bottom: 20px;
}

.plan-detail-header h3 {
  margin: 0 0 12px 0;
  color: #303133;
}

.plan-detail-meta {
  display: flex;
  gap: 16px;
  align-items: center;
  color: #606266;
  font-size: 14px;
}

.plan-detail-description {
  margin-bottom: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.plan-detail-description p {
  margin: 0;
  color: #606266;
  line-height: 1.6;
}

.plan-detail-timeline {
  margin-top: 20px;
}

.day-schedule {
  margin-top: 12px;
}

.schedule-item {
  display: flex;
  margin-bottom: 16px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
}

.schedule-item .time {
  min-width: 80px;
  font-weight: 600;
  color: #e6a23c;
  margin-right: 16px;
}

.schedule-item .content h4 {
  margin: 0 0 4px 0;
  color: #303133;
}

.schedule-item .content p {
  margin: 0;
  color: #606266;
  font-size: 12px;
}

@media (max-width: 768px) {
  .profile-content {
    flex-direction: column;
  }
  
  .profile-sidebar {
    width: 100%;
  }
  
  .profile-menu {
    display: flex;
    overflow-x: auto;
  }
  
  .profile-menu .el-menu-item {
    white-space: nowrap;
  }
}
</style>
