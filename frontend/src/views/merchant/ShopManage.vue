<template>
  <div class="shop-manage">
    <!-- 返回按钮 -->
    <div class="back-button-container">
      <el-button @click="goBack" :icon="ArrowLeft" circle />
    </div>
    
    <div class="page-header">
      <h2>店铺管理</h2>
      <el-button type="primary" @click="openEditDialog">
        <el-icon><Edit /></el-icon>
        编辑店铺信息
      </el-button>
    </div>

    <!-- 新商家引导提示 -->
    <el-alert
      v-if="showNewMerchantGuide"
      :title="guideTitle"
      type="info"
      :closable="true"
      @close="dismissGuide"
      show-icon
      class="guide-alert"
    >
      <template #default>
        <div class="guide-content">
          <p><strong>{{ guideMainText }}</strong></p>
          <ul>
            <li><strong>{{ guideNameLabel }}和{{ guideDescLabel }}</strong>：{{ guideNameDescHint }}</li>
            <li><strong>店铺图片</strong>：{{ guideImageHint }}</li>
            <li><strong>地址和电话</strong>：{{ guideContactHint }}</li>
            <li><strong>{{ guideTimeLabel }}</strong>：{{ guideTimeHint }}</li>
            <li v-if="merchantInfo?.category === 'FOOD'"><strong>人均消费和餐饮类型</strong>：帮助游客了解您的价格定位和店铺特色</li>
            <li v-if="merchantInfo?.category === 'HOTEL'"><strong>酒店星级和房间数</strong>：帮助游客了解您的酒店档次和规模</li>
          </ul>
          <p class="guide-tip">{{ guideTip }}</p>
        </div>
      </template>
    </el-alert>

    <!-- 店铺信息展示 -->
    <el-card class="shop-info-card">
      <div class="shop-info">
        <div class="shop-avatar">
          <el-avatar :size="80" :src="shopInfo.avatar">
            {{ shopInfo.name?.charAt(0) }}
          </el-avatar>
        </div>
        <div class="shop-details">
          <h3>{{ shopInfo.name || '未设置店铺名称' }}</h3>
          <p class="shop-description">{{ shopInfo.description || '暂无店铺描述' }}</p>
          <div class="shop-meta">
            <div class="meta-item">
              <el-icon><Location /></el-icon>
              <span>{{ shopInfo.address || '未设置地址' }}</span>
            </div>
            <div class="meta-item">
              <el-icon><Phone /></el-icon>
              <span>{{ shopInfo.phone || '未设置电话' }}</span>
            </div>
            <div class="meta-item">
              <el-icon><Clock /></el-icon>
              <span>{{ shopInfo.openTime || '未设置营业时间' }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 店铺统计 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><View /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ shopStats.views }}</div>
              <div class="stat-label">店铺浏览量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><Star /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ shopStats.rating }}</div>
              <div class="stat-label">平均评分</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ shopStats.orders }}</div>
              <div class="stat-label">总订单数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ shopStats.revenue }}</div>
              <div class="stat-label">总收入</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 活动管理快捷入口 -->
    <el-card class="activities-link-card">
      <div class="activities-link-content">
        <div class="link-info">
          <el-icon class="link-icon"><Present /></el-icon>
          <div class="link-text">
            <h3>营销活动管理</h3>
            <p>发布和管理您的营销活动，吸引更多顾客</p>
          </div>
        </div>
        <el-button type="primary" @click="goToActivityManage">
          <el-icon><ArrowRight /></el-icon>
          前往活动管理
        </el-button>
      </div>
    </el-card>

    <!-- 编辑店铺信息对话框 -->
    <el-dialog v-model="showEditDialog" :title="dialogTitle" width="700px">
      <el-form :model="editForm" label-width="120px">
        <!-- 店铺名称 -->
        <el-form-item :label="nameLabel" required>
          <el-input 
            v-model="editForm.name" 
            :placeholder="namePlaceholder"
            maxlength="100"
            show-word-limit
          />
          <div class="field-hint">{{ nameHint }}</div>
        </el-form-item>
        
        <!-- 店铺描述 -->
        <el-form-item :label="descriptionLabel" required>
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="5"
            :placeholder="descriptionPlaceholder"
            maxlength="500"
            show-word-limit
          />
          <div class="field-hint">{{ descriptionHint }}</div>
        </el-form-item>
        
        <!-- 美食商家专属字段 -->
        <template v-if="merchantInfo?.category === 'FOOD'">
          <el-form-item label="人均消费">
            <el-input 
              v-model="editForm.avgPrice" 
              placeholder="请输入人均消费，如：50-100"
              maxlength="50"
            />
            <div class="field-hint">此信息会显示在游客端的美食推荐卡片中</div>
          </el-form-item>
          <el-form-item label="餐饮类型">
            <el-select 
              v-model="editForm.cuisineType" 
              placeholder="请选择餐饮类型"
              clearable
              :popper-options="{
                strategy: 'fixed',
                modifiers: [
                  { name: 'offset', options: { offset: [0, 4] } },
                  { name: 'computeStyles', options: { adaptive: true, gpuAcceleration: true } }
                ]
              }"
            >
              <el-option label="小吃" value="SNACK" />
              <el-option label="奶茶饮品" value="BEVERAGE" />
              <el-option label="甜品" value="DESSERT" />
              <el-option label="快餐" value="FAST_FOOD" />
              <el-option label="火锅" value="HOTPOT" />
              <el-option label="烧烤" value="BBQ" />
              <el-option label="面食" value="NOODLE" />
              <el-option label="烘焙" value="BAKERY" />
              <el-option label="咖啡" value="COFFEE" />
              <el-option label="轻食" value="LIGHT_MEAL" />
              <el-option label="地方特色" value="LOCAL" />
              <el-option label="其他" value="OTHER" />
            </el-select>
            <div class="field-hint">选择您的餐饮类型，帮助游客快速了解您的店铺特色</div>
          </el-form-item>
        </template>
        
        <!-- 酒店商家专属字段 -->
        <template v-if="merchantInfo?.category === 'HOTEL'">
          <el-divider content-position="left">基础信息</el-divider>
          <el-form-item label="酒店星级">
            <el-select 
              v-model="editForm.starRating" 
              placeholder="请选择酒店星级"
              clearable
              :popper-options="{
                strategy: 'fixed',
                modifiers: [
                  { name: 'offset', options: { offset: [0, 4] } },
                  { name: 'computeStyles', options: { adaptive: true, gpuAcceleration: true } }
                ]
              }"
            >
              <el-option label="经济型" value="ECONOMY" />
              <el-option label="三星级" value="3_STAR" />
              <el-option label="四星级" value="4_STAR" />
              <el-option label="五星级" value="5_STAR" />
              <el-option label="豪华型" value="LUXURY" />
            </el-select>
            <div class="field-hint">选择您的酒店星级标准</div>
          </el-form-item>
          <el-form-item label="房间数量">
            <el-input-number 
              v-model="editForm.roomCount" 
              :min="1" 
              :max="1000"
              placeholder="请输入房间总数"
            />
            <div class="field-hint">酒店可提供的房间总数</div>
          </el-form-item>
          
          <el-divider content-position="left">设施与标签</el-divider>
          <el-form-item label="酒店设施">
            <el-select
              v-model="editForm.facilities"
              multiple
              collapse-tags
              placeholder="请选择酒店设施"
              style="width: 100%"
              :popper-options="{
                strategy: 'fixed',
                modifiers: [
                  { name: 'offset', options: { offset: [0, 4] } },
                  { name: 'computeStyles', options: { adaptive: true, gpuAcceleration: true } }
                ]
              }"
            >
              <el-option label="免费WiFi" value="免费WiFi" />
              <el-option label="停车场" value="停车场" />
              <el-option label="游泳池" value="游泳池" />
              <el-option label="健身房" value="健身房" />
              <el-option label="餐厅" value="餐厅" />
              <el-option label="接送服务" value="接送服务" />
              <el-option label="行李寄存" value="行李寄存" />
              <el-option label="24小时前台" value="24小时前台" />
            </el-select>
            <div class="field-hint">可多选，游客端将展示为“酒店设施”模块</div>
          </el-form-item>
          <el-form-item label="酒店标签">
            <el-select
              v-model="editForm.tags"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="输入或选择标签，如：亲子友好、交通便利"
              style="width: 100%"
            >
              <el-option label="位置优越" value="位置优越" />
              <el-option label="亲子友好" value="亲子友好" />
              <el-option label="干净卫生" value="干净卫生" />
              <el-option label="服务周到" value="服务周到" />
              <el-option label="海景房" value="海景房" />
            </el-select>
            <div class="field-hint">将用于游客端“评价标签/亮点”展示</div>
          </el-form-item>
        </template>
        <el-form-item label="店铺图片" required>
          <div class="image-upload-section">
            <!-- 图片列表展示区域（支持拖拽排序） -->
            <div class="image-list-container">
              <div 
                class="image-list"
                v-if="editForm.images && editForm.images.length > 0"
              >
                <div
                  v-for="(img, index) in editForm.images"
                  :key="index"
                  class="image-item"
                  :class="{ 'is-cover': index === 0 }"
                  draggable="true"
                  @dragstart="handleDragStart(index, $event)"
                  @dragover.prevent="handleDragOver(index, $event)"
                  @drop="handleDrop(index, $event)"
                  @dragend="handleDragEnd"
                >
                  <div class="image-thumbnail">
                    <img :src="img" :alt="`店铺图片 ${index + 1}`" />
                    <div class="image-overlay">
                      <div class="image-badges">
                        <el-tag v-if="index === 0" type="success" size="small">封面</el-tag>
                      </div>
                      <div class="image-actions">
                        <el-button
                          v-if="index !== 0"
                          type="primary"
                          size="small"
                          circle
                          @click.stop="setAsCover(index)"
                          title="设为封面"
                        >
                          <el-icon><Star /></el-icon>
                        </el-button>
                        <el-button
                          type="danger"
                          size="small"
                          circle
                          @click.stop="removeImage(index)"
                          title="删除"
                        >
                          <el-icon><Delete /></el-icon>
                        </el-button>
                      </div>
                    </div>
                  </div>
                  <div class="image-index">{{ index + 1 }}</div>
                </div>
              </div>
              
              <!-- 上传按钮 -->
              <el-upload
                class="image-upload-btn"
                :http-request="customAvatarUpload"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :on-error="handleAvatarError"
                :before-upload="beforeAvatarUpload"
                accept="image/*"
                multiple
              >
                <div class="upload-placeholder">
                  <el-icon class="upload-icon"><Plus /></el-icon>
                  <div class="upload-text">添加图片</div>
                </div>
              </el-upload>
            </div>
            
            <div class="image-info">
              <p class="image-hint">
                <el-icon><InfoFilled /></el-icon>
                图片说明：
              </p>
              <ul class="image-usage-list">
                <li>{{ imageHint1 }}</li>
                <li>{{ imageHint2 }}</li>
                <li>可以拖拽图片调整顺序，第一张自动成为封面</li>
                <li>建议尺寸：800x600px 或更高，支持 JPG、PNG 格式</li>
              </ul>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="店铺地址">
          <el-input 
            v-model="editForm.address" 
            placeholder="请输入详细地址，如：景德镇市珠山区XX路XX号"
            maxlength="255"
            show-word-limit
          />
          <div class="field-hint">{{ addressHint }}</div>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input 
            v-model="editForm.phone" 
            placeholder="请输入联系电话，如：0798-1234567 或 13800138000"
            maxlength="50"
          />
          <div class="field-hint">{{ phoneHint }}</div>
        </el-form-item>
        <el-form-item :label="openTimeLabel">
          <el-input 
            v-model="editForm.openTime" 
            :placeholder="openTimePlaceholder"
            maxlength="100"
          />
          <div class="field-hint">{{ openTimeHint }}</div>
        </el-form-item>
        <el-form-item :label="startPriceLabel">
          <el-input-number 
            v-model="editForm.startPrice" 
            :min="0" 
            :precision="2" 
            :step="10"
            :placeholder="startPricePlaceholder"
            style="width: 100%"
          />
          <div class="field-hint">{{ startPriceHint }}</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="saveShopInfo">保存</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import { ArrowLeft, InfoFilled, Plus, Delete, Star, Present, ArrowRight } from '@element-plus/icons-vue'
import { getMerchantByUserId, updateMerchantBasicInfo } from '../../api/merchant'
import request from '../../utils/request'
import realtimeSync from '../../utils/websocket'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const merchantInfo = ref(null)

const goBack = () => {
  router.go(-1)
}

const showEditDialog = ref(false)

const goToActivityManage = () => {
  router.push('/merchant/activities')
}

// 打开编辑对话框时，同步当前店铺信息到编辑表单
const openEditDialog = () => {
  // 处理图片数组：支持数组、字符串（逗号分隔）、单个字符串等多种格式
  let imagesArray = []
  if (Array.isArray(shopInfo.value.images)) {
    imagesArray = [...shopInfo.value.images]
  } else if (shopInfo.value.images) {
    // 如果是字符串，尝试按逗号分割
    if (typeof shopInfo.value.images === 'string') {
      imagesArray = shopInfo.value.images
        .split(',')
        .map(s => s.trim())
        .filter(Boolean)
    } else {
      imagesArray = [shopInfo.value.images].filter(Boolean)
    }
  }
  
  // 如果 avatar 存在但不在 images 中，确保 images 包含 avatar
  if (shopInfo.value.avatar && !imagesArray.includes(shopInfo.value.avatar)) {
    imagesArray.unshift(shopInfo.value.avatar)
  }
  
  // 将当前的 shopInfo 数据同步到 editForm，确保编辑过的字段都被保留
  Object.assign(editForm, {
    name: shopInfo.value.name || '',
    description: shopInfo.value.description || '',
    address: shopInfo.value.address || '',
    phone: shopInfo.value.phone || '',
    openTime: shopInfo.value.openTime || '',
    avatar: shopInfo.value.avatar || '',
    images: imagesArray,
    startPrice: shopInfo.value.startPrice || merchantInfo.value?.startPrice || 0,
    // 美食商家专属字段
    avgPrice: shopInfo.value.avgPrice || merchantInfo.value?.avgPrice || '',
    cuisineType: shopInfo.value.cuisineType || merchantInfo.value?.cuisineType || '',
    // 酒店商家专属字段
    starRating: shopInfo.value.starRating || merchantInfo.value?.starRating || '',
    roomCount: shopInfo.value.roomCount || merchantInfo.value?.roomCount || null,
    facilities: Array.isArray(shopInfo.value.facilities) ? shopInfo.value.facilities
      : (shopInfo.value.facilities ? String(shopInfo.value.facilities).split(',').map(s => s.trim()).filter(Boolean) : []),
    tags: Array.isArray(shopInfo.value.tags) ? shopInfo.value.tags
      : (typeof shopInfo.value.tags === 'string' ? shopInfo.value.tags.split(',').map(s => s && s.trim()).filter(Boolean) : [])
  })
  showEditDialog.value = true
}
// 新商家引导提示显示状态
const showNewMerchantGuide = ref(false)

// 检查是否需要显示新商家引导
const checkShowGuide = () => {
  if (!userStore.user?.id) return false
  const guideKey = `merchant_guide_shown_${userStore.user.id}`
  const hasShown = localStorage.getItem(guideKey)
  
  // 如果店铺信息不完整，显示引导
  const isIncomplete = !shopInfo.value.name || 
                       !shopInfo.value.description || 
                       !shopInfo.value.avatar ||
                       shopInfo.value.name === '未设置店铺名称'
  
  return !hasShown && isIncomplete
}

// 关闭引导提示
const dismissGuide = () => {
  if (userStore.user?.id) {
    const guideKey = `merchant_guide_shown_${userStore.user.id}`
    localStorage.setItem(guideKey, 'true')
  }
  showNewMerchantGuide.value = false
}

// 从后端 / localStorage / 用户信息中组合出当前店铺信息
const buildInitialShopInfo = (merchant, user, ignoreLocal = false) => {
  const localKey = user?.id ? `shopInfo_${user.id}` : null
  
  // 仅在非强制模式下读取本地缓存
  if (!ignoreLocal) {
    const savedShopInfo = localKey ? localStorage.getItem(localKey) : null
    if (savedShopInfo) {
      return JSON.parse(savedShopInfo)
    }
  }

  // 解析多张图片，优先使用后端的 shopImages 字段，其次是用户信息中的 shopImages
  const parseImages = (src) => {
    if (!src) return []
    if (Array.isArray(src)) return src
    if (typeof src === 'string') {
      return src
        .split(',')
        .map(s => s.trim())
        .filter(Boolean)
    }
    return []
  }

  const images = parseImages(merchant?.shopImages || user?.shopImages)
  
  // 判断是否有商家信息（以ID存在为准）
  // 如果有商家信息，严格使用商家字段，不再回退到用户字段（防止旧用户数据干扰）
  const hasMerchant = !!(merchant && merchant.id);

  return {
    name: hasMerchant ? (merchant.shopName || '未设置店铺名称') : (user?.shopName || (user?.username ? user.username + '的店铺' : '我的店铺')),
    description: hasMerchant ? (merchant.description || '暂无店铺描述') : (user?.shopDescription || '暂无店铺描述'),
    address: hasMerchant ? (merchant.address || '未设置地址') : (user?.shopAddress || '未设置地址'),
    phone: hasMerchant ? (merchant.phone || '未设置电话') : (user?.shopPhone || user?.phone || '未设置电话'),
    openTime: hasMerchant ? (merchant.openTime || '9:00-18:00') : (user?.shopOpenTime || '9:00-18:00'),
    startPrice: hasMerchant ? (merchant.startPrice || 0) : (user?.startPrice || 0),
    // 如果后端没有单独的 avatar，则默认使用第一张图片作为封面
    avatar: hasMerchant ? (merchant.avatar || (images[0] || '')) : (user?.shopAvatar || (images[0] || '')),
    images,
    // 美食商家专属字段
    avgPrice: hasMerchant ? (merchant.avgPrice || '') : (user?.avgPrice || ''),
    cuisineType: hasMerchant ? (merchant.cuisineType || '') : (user?.cuisineType || ''),
    // 酒店商家专属字段
    starRating: hasMerchant ? (merchant.starRating || '') : (user?.starRating || ''),
    roomCount: hasMerchant ? (merchant.roomCount || null) : (user?.roomCount || null),
    facilities: hasMerchant
      ? (Array.isArray(merchant.facilities) ? merchant.facilities : (merchant.facilities ? String(merchant.facilities).split(',').map(s => s.trim()).filter(Boolean) : []))
      : [],
    tags: hasMerchant
      ? (Array.isArray(merchant.tags) ? merchant.tags : (merchant.tags ? String(merchant.tags).split(',').map(s => s.trim()).filter(Boolean) : []))
      : []
  }
}

const shopInfo = ref(buildInitialShopInfo(null, userStore.user))

const shopStats = ref({
  views: 1234,
  rating: 4.8,
  orders: 89,
  revenue: 15680
})

const editForm = reactive({
  name: '',
  description: '',
  address: '',
  phone: '',
  openTime: '',
  avatar: '',
  startPrice: 0,
  // 多张店铺图片（用于酒店详情轮播），数组形式存储 URL
  images: [],
  // 美食商家专属字段
  avgPrice: '',
  cuisineType: '',
  // 酒店商家专属字段
  starRating: '',
  roomCount: null,
  facilities: [],
  tags: []
})

// 根据商家分类动态显示对话框标题
const dialogTitle = computed(() => {
  const category = merchantInfo.value?.category
  if (category === 'FOOD') {
    return '编辑餐厅信息'
  } else if (category === 'HOTEL') {
    return '编辑酒店信息'
  } else if (category === 'CERAMIC') {
    return '编辑体验店信息'
  }
  return '编辑店铺信息'
})

// 店铺名称标签和提示
const nameLabel = computed(() => {
  const category = merchantInfo.value?.category
  if (category === 'FOOD') return '餐厅名称'
  if (category === 'HOTEL') return '酒店名称'
  if (category === 'CERAMIC') return '体验店名称'
  return '店铺名称'
})

const namePlaceholder = computed(() => {
  const category = merchantInfo.value?.category
  if (category === 'FOOD') return '请输入餐厅名称，如：景德镇特色餐厅'
  if (category === 'HOTEL') return '请输入酒店名称，如：景德镇精品酒店'
  if (category === 'CERAMIC') return '请输入体验店名称'
  return '请输入店铺名称'
})

const nameHint = computed(() => {
  const category = merchantInfo.value?.category
  if (category === 'FOOD') return '此名称会显示在游客端的美食推荐卡片和详情页'
  if (category === 'HOTEL') return '此名称会显示在游客端的酒店推荐卡片和详情页'
  if (category === 'CERAMIC') return '此名称会显示在游客端的陶瓷体验推荐中'
  if (category === 'SCENIC') return '此名称会显示在游客端的景点推荐卡片和详情页'
  return '此名称会显示在游客端的推荐卡片和详情页'
})

// 店铺描述标签和提示
const descriptionLabel = computed(() => {
  const category = merchantInfo.value?.category
  if (category === 'FOOD') return '餐厅介绍'
  if (category === 'HOTEL') return '酒店介绍'
  if (category === 'CERAMIC') return '体验店介绍'
  if (category === 'SCENIC') return '景点介绍'
  return '店铺描述'
})

const descriptionPlaceholder = computed(() => {
  const category = merchantInfo.value?.category
  if (category === 'FOOD') return '请输入餐厅介绍，建议包含菜品特色、招牌菜、用餐环境等信息'
  if (category === 'HOTEL') return '请输入酒店介绍，建议包含酒店特色、设施服务、周边环境等信息'
  if (category === 'CERAMIC') return '请输入体验店介绍，建议包含体验项目、特色工艺等信息'
  if (category === 'SCENIC') return '请输入景点介绍，建议包含景点特色、历史文化、游玩项目等信息'
  return '请输入店铺描述，建议包含店铺特色、服务内容等信息'
})

const descriptionHint = computed(() => {
  const category = merchantInfo.value?.category
  if (category === 'FOOD') return '此介绍会显示在游客端的美食推荐卡片和详情页，建议详细描述菜品特色和用餐体验'
  if (category === 'HOTEL') return '此介绍会显示在游客端的酒店推荐卡片和详情页，建议详细描述酒店设施和服务特色'
  if (category === 'CERAMIC') return '此介绍会显示在游客端的陶瓷体验推荐中，建议详细描述体验项目和工艺特色'
  if (category === 'SCENIC') return '此介绍会显示在游客端的景点推荐卡片和详情页，建议详细描述景点特色和游玩体验'
  return '此描述会显示在游客端的推荐卡片和详情页，建议详细描述店铺特色'
})

// 图片提示
const imageHint1 = computed(() => {
  const category = merchantInfo.value?.category
  if (category === 'FOOD') return '第一张图片将作为封面图显示在美食推荐列表和详情页'
  if (category === 'HOTEL') return '第一张图片将作为封面图显示在酒店推荐列表和详情页'
  if (category === 'SCENIC') return '第一张图片将作为封面图显示在景点推荐列表和详情页'
  return '第一张图片将作为封面图显示在推荐列表和详情页'
})

const imageHint2 = computed(() => {
  const category = merchantInfo.value?.category
  if (category === 'FOOD') return '所有图片会在餐厅详情页以轮播图形式展示，建议上传菜品图片和用餐环境图片'
  if (category === 'HOTEL') return '所有图片会在酒店详情页以轮播图形式展示，建议上传房间图片和设施图片'
  if (category === 'SCENIC') return '所有图片会在景点详情页以轮播图形式展示，建议上传景点风光和游玩项目图片'
  return '所有图片会在详情页以轮播图形式展示'
})

// 地址提示
const addressHint = computed(() => {
  const category = merchantInfo.value?.category
  if (category === 'FOOD') return '此地址会显示在游客端的餐厅详情页，方便游客导航到店用餐'
  if (category === 'HOTEL') return '此地址会显示在游客端的酒店详情页，方便游客导航到店入住'
  if (category === 'SCENIC') return '此地址会显示在游客端的景点详情页，方便游客导航到景点游玩'
  return '此地址会显示在游客端的详情页，方便游客导航'
})

// 电话提示
const phoneHint = computed(() => {
  const category = merchantInfo.value?.category
  if (category === 'FOOD') return '此电话会显示在游客端的餐厅详情页，方便游客预订或咨询'
  if (category === 'HOTEL') return '此电话会显示在游客端的酒店详情页，方便游客预订或咨询'
  if (category === 'SCENIC') return '此电话会显示在游客端的景点详情页，方便游客咨询或购票'
  return '此电话会显示在游客端的详情页，方便游客联系'
})

// 营业时间标签和提示
const openTimeLabel = computed(() => {
  const category = merchantInfo.value?.category
  if (category === 'HOTEL') return '服务时间'
  if (category === 'SCENIC') return '开放时间'
  return '营业时间'
})

const openTimePlaceholder = computed(() => {
  const category = merchantInfo.value?.category
  if (category === 'FOOD') return '如：11:00-14:00, 17:00-21:00'
  if (category === 'HOTEL') return '如：24小时服务 或 7:00-23:00'
  if (category === 'SCENIC') return '如：8:00-17:30 (17:00停止入园)'
  return '如：9:00-18:00 或 全天24小时'
})

const openTimeHint = computed(() => {
  const category = merchantInfo.value?.category
  if (category === 'FOOD') return '此信息会显示在游客端的餐厅详情页，帮助游客了解用餐时间'
  if (category === 'HOTEL') return '此信息会显示在游客端的酒店详情页，通常酒店为24小时服务'
  if (category === 'SCENIC') return '此信息会显示在游客端的景点详情页，帮助游客了解开放时间'
  return '此信息会显示在游客端的详情页'
})

const startPriceLabel = computed(() => {
  const category = merchantInfo.value?.category
  if (category === 'FOOD') return '人均消费'
  if (category === 'HOTEL') return '起步价'
  if (category === 'SCENIC') return '门票价格'
  if (category === 'CERAMIC') return '体验价格'
  return '起步价'
})

const startPricePlaceholder = computed(() => {
  const category = merchantInfo.value?.category
  if (category === 'FOOD') return '请输入人均消费金额'
  if (category === 'HOTEL') return '请输入最低房型价格'
  if (category === 'SCENIC') return '请输入门票价格'
  if (category === 'CERAMIC') return '请输入体验课程起步价'
  return '请输入金额'
})

const startPriceHint = computed(() => {
  const category = merchantInfo.value?.category
  if (category === 'FOOD') return '此价格将作为人均消费显示'
  if (category === 'HOTEL') return '此价格将作为酒店起步价显示'
  if (category === 'SCENIC') return '此价格将作为景点门票价格显示'
  return '此价格将显示在推荐卡片上'
})

// 引导提示相关计算属性
const guideTitle = computed(() => {
  const category = merchantInfo.value?.category
  if (category === 'FOOD') return '新餐厅提示'
  if (category === 'HOTEL') return '新酒店提示'
  if (category === 'CERAMIC') return '新体验店提示'
  if (category === 'SCENIC') return '新景点提示'
  return '新商家提示'
})

const guideMainText = computed(() => {
  const category = merchantInfo.value?.category
  if (category === 'FOOD') return '完善餐厅信息，让更多游客看到您的餐厅！'
  if (category === 'HOTEL') return '完善酒店信息，让更多游客看到您的酒店！'
  if (category === 'CERAMIC') return '完善体验店信息，让更多游客看到您的体验店！'
  if (category === 'SCENIC') return '完善景点信息，让更多游客看到您的景点！'
  return '完善店铺信息，让更多游客看到您的店铺！'
})

const guideNameLabel = computed(() => {
  const category = merchantInfo.value?.category
  if (category === 'FOOD') return '餐厅名称'
  if (category === 'HOTEL') return '酒店名称'
  if (category === 'SCENIC') return '景点名称'
  return '店铺名称'
})

const guideDescLabel = computed(() => {
  const category = merchantInfo.value?.category
  if (category === 'FOOD') return '餐厅介绍'
  if (category === 'HOTEL') return '酒店介绍'
  if (category === 'SCENIC') return '景点介绍'
  return '店铺描述'
})

const guideNameDescHint = computed(() => {
  const category = merchantInfo.value?.category
  if (category === 'FOOD') return '会显示在游客端的美食推荐卡片和详情页'
  if (category === 'HOTEL') return '会显示在游客端的酒店推荐卡片和详情页'
  if (category === 'SCENIC') return '会显示在游客端的景点推荐卡片和详情页'
  return '会显示在游客端的推荐卡片和详情页'
})

const guideImageHint = computed(() => {
  const category = merchantInfo.value?.category
  if (category === 'FOOD') return '会作为封面图显示在美食推荐列表和详情页，建议上传菜品图片和用餐环境图片'
  if (category === 'HOTEL') return '会作为封面图显示在酒店推荐列表和详情页，建议上传房间图片和设施图片'
  if (category === 'SCENIC') return '会作为封面图显示在景点推荐列表和详情页，建议上传景点风光图片'
  return '会作为封面图显示在推荐列表和详情页'
})

const guideContactHint = computed(() => {
  const category = merchantInfo.value?.category
  if (category === 'FOOD') return '方便游客联系和导航到您的餐厅'
  if (category === 'HOTEL') return '方便游客联系和导航到您的酒店'
  if (category === 'SCENIC') return '方便游客联系和导航到您的景点'
  return '方便游客联系和导航到您的店铺'
})

const guideTimeLabel = computed(() => {
  const category = merchantInfo.value?.category
  if (category === 'HOTEL') return '服务时间'
  if (category === 'SCENIC') return '开放时间'
  return '营业时间'
})

const guideTimeHint = computed(() => {
  const category = merchantInfo.value?.category
  if (category === 'FOOD') return '帮助游客了解您的用餐时间'
  if (category === 'HOTEL') return '帮助游客了解您的服务时间'
  if (category === 'SCENIC') return '帮助游客了解您的开放时间'
  return '帮助游客了解您的服务时间'
})

const guideTip = computed(() => {
  const category = merchantInfo.value?.category
  if (category === 'FOOD') {
    return '💡 提示：请确保在商家资质审核时选择了"美食"分类，这样才能出现在游客端的美食推荐中。'
  }
  if (category === 'HOTEL') {
    return '💡 提示：请确保在商家资质审核时选择了"酒店"分类，这样才能出现在游客端的酒店推荐中。'
  }
  if (category === 'CERAMIC') {
    return '💡 提示：请确保在商家资质审核时选择了"陶瓷"分类，这样才能出现在游客端的陶瓷体验推荐中。'
  }
  return '💡 提示：请确保在商家资质审核时选择了正确的分类，这样才能出现在游客端的相应推荐中。'
})


const beforeAvatarUpload = (file) => {
  // 检查文件类型
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  
  // 检查文件大小（5MB）
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB！')
    return false
  }
  
  return true
}

// 自定义头像上传方法
const customAvatarUpload = async (options) => {
  const { file, onSuccess, onError } = options
  
  try {
    const formData = new FormData()
    formData.append('file', file.raw || file)
    
    // 使用 request 工具，确保携带 token
    // 注意：request 的 baseURL 是 '/api'，后端接口是 '/api/upload'，所以这里用 '/upload'
    const response = await request.post('/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    // 处理响应（request 拦截器已经处理了响应格式）
    // 后端返回格式：{ success: true, url: "/uploads/xxx.jpg", message: "..." }
    // request 拦截器处理后，如果 success=true，会返回整个 data 对象
    const url = response.url || response.data?.url
    if (response.success && url) {
      // 上传成功，更新图片列表
      if (!Array.isArray(editForm.images)) {
        editForm.images = []
      }
      // 如果图片已存在，先移除
      const existingIndex = editForm.images.indexOf(url)
      if (existingIndex !== -1) {
        editForm.images.splice(existingIndex, 1)
      }
      // 如果还没有封面，则第一张图片作为封面，并添加到第一位
      if (!editForm.avatar) {
        editForm.avatar = url
        editForm.images.unshift(url)
      } else {
        // 如果已有封面，添加到列表末尾
        editForm.images.push(url)
      }
      onSuccess({ url: url })
      ElMessage.success('图片上传成功')
    } else {
      // 响应格式异常
      const errorMsg = response.message || '上传失败，服务器未返回图片地址'
      throw new Error(errorMsg)
    }
  } catch (error) {
    console.error('图片上传失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '图片上传失败，请重试'
    ElMessage.error(errorMsg)
    
    // 上传失败时，使用本地预览（临时显示）
    const reader = new FileReader()
    reader.onload = (e) => {
      // 仅用于预览，不保存到表单（因为保存时会过滤掉 base64）
      const previewUrl = e.target.result
      // 临时显示预览，但提示用户需要重新上传
      ElMessage.warning('图片上传失败，已使用本地预览。请检查网络连接后重新上传，否则保存时图片将丢失。')
      // 不调用 onSuccess，让用户知道上传失败
      onError(error)
    }
    reader.onerror = () => {
      onError(error)
    }
    reader.readAsDataURL(file.raw || file)
  }
}

const handleAvatarError = (error, file) => {
  console.error('头像上传错误:', error)
  ElMessage.error('图片上传失败，请重试')
}

const handleAvatarSuccess = (response) => {
  // 响应已经在 customAvatarUpload 中处理了
  // 这里只做兼容处理
  if (response && response.url && !editForm.avatar) {
    editForm.avatar = response.url
  }
}

// 拖拽排序相关
const draggedIndex = ref(null)

const handleDragStart = (index, event) => {
  draggedIndex.value = index
  event.dataTransfer.effectAllowed = 'move'
  event.dataTransfer.setData('text/html', event.target.outerHTML)
  event.target.style.opacity = '0.5'
}

const handleDragOver = (index, event) => {
  event.preventDefault()
  event.dataTransfer.dropEffect = 'move'
  
  // 添加视觉反馈
  const target = event.currentTarget
  if (draggedIndex.value !== null && draggedIndex.value !== index) {
    target.classList.add('drag-over')
  }
}

const handleDrop = (index, event) => {
  event.preventDefault()
  
  // 移除视觉反馈
  document.querySelectorAll('.image-item').forEach(item => {
    item.classList.remove('drag-over')
  })
  
  if (draggedIndex.value === null || draggedIndex.value === index) {
    return
  }
  
  // 执行数组元素移动
  const images = [...editForm.images]
  const draggedItem = images[draggedIndex.value]
  images.splice(draggedIndex.value, 1)
  images.splice(index, 0, draggedItem)
  
  editForm.images = images
  // 更新封面为第一张
  if (images.length > 0) {
    editForm.avatar = images[0]
  }
  
  draggedIndex.value = null
}

const handleDragEnd = (event) => {
  event.target.style.opacity = '1'
  document.querySelectorAll('.image-item').forEach(item => {
    item.classList.remove('drag-over')
  })
  draggedIndex.value = null
}

// 删除单张图片
const removeImage = (index) => {
  if (!Array.isArray(editForm.images) || editForm.images.length === 0) {
    return
  }
  
  editForm.images.splice(index, 1)
  
  // 如果删除的是第一张，更新封面
  if (index === 0 && editForm.images.length > 0) {
    editForm.avatar = editForm.images[0]
  } else if (editForm.images.length === 0) {
    editForm.avatar = ''
  }
  
  ElMessage.success('图片已删除')
}

// 设置某张图片为封面（移动到第一位）
const setAsCover = (index) => {
  if (!Array.isArray(editForm.images) || index === 0) {
    return
  }
  
  const images = [...editForm.images]
  const targetImage = images[index]
  images.splice(index, 1)
  images.unshift(targetImage)
  
  editForm.images = images
  editForm.avatar = targetImage
  
  ElMessage.success('已设置为封面图')
}

const saveShopInfo = async () => {
  if (!merchantInfo.value || !merchantInfo.value.id) {
    // 如果还没有商家信息（例如资质未通过），仅在本地保存，避免报错
    Object.assign(shopInfo.value, editForm)
    if (userStore.user?.id) {
      localStorage.setItem(`shopInfo_${userStore.user.id}`, JSON.stringify(shopInfo.value))
    }
    showEditDialog.value = false
    ElMessage.success('店铺信息已在本地保存，商家认证通过后将支持云端保存')
    return
  }

  try {
    // 检查必填字段
    if (!editForm.name || editForm.name.trim() === '') {
      ElMessage.warning('请输入店铺名称')
      return
    }
    
    // 避免将超长的 base64 数据写入数据库：
    // 如果头像是 data URL（本地预览），提示用户需要重新上传
    let avatarForServer = editForm.avatar
    if (avatarForServer && typeof avatarForServer === 'string' && avatarForServer.startsWith('data:')) {
      ElMessage.warning('检测到图片未成功上传到服务器，请重新上传图片后再保存')
      return
    }

    // 确保 images 数组包含 avatar（如果存在）
    let imagesForServer = Array.isArray(editForm.images) ? [...editForm.images] : []
    // 如果 avatar 存在但不在 images 中，则添加到第一位
    if (avatarForServer && !imagesForServer.includes(avatarForServer)) {
      imagesForServer.unshift(avatarForServer)
    }
    // 如果 images 为空但 avatar 存在，则使用 avatar
    if (imagesForServer.length === 0 && avatarForServer) {
      imagesForServer = [avatarForServer]
    }

    const payload = {
      shopName: editForm.name,
      description: editForm.description,
      address: editForm.address,
      phone: editForm.phone,
      openTime: editForm.openTime,
      startPrice: editForm.startPrice,
      avatar: avatarForServer || null,
      images: imagesForServer,
      // 美食商家专属字段（始终传递，后端按分类处理）
      avgPrice: editForm.avgPrice || null,
      cuisineType: editForm.cuisineType || null,
      // 酒店商家专属字段（始终传递，后端按分类处理）
      starRating: editForm.starRating || null,
      roomCount: editForm.roomCount || null,
      facilities: Array.isArray(editForm.facilities) ? editForm.facilities : [],
      tags: Array.isArray(editForm.tags) ? editForm.tags : (editForm.tags ? String(editForm.tags).split(',').map(s => s && s.trim()).filter(Boolean) : [])
    }
    const res = await updateMerchantBasicInfo(merchantInfo.value.id, payload)

    // 兼容不同响应结构
    let data = res?.data?.data || res?.data || res?.data?.merchant || res?.merchant || null
    if (!data && res?.success && res?.data) {
      data = res.data
    }

    // 更新 merchantInfo，以便后续使用
    if (data) {
      merchantInfo.value = { ...merchantInfo.value, ...data }
    }

    // 直接将编辑表单的数据同步到展示数据，确保编辑过的字段都被保留
    // 这样用户再次打开编辑对话框时，所有编辑过的字段都会显示
    shopInfo.value = {
      name: editForm.name,
      description: editForm.description,
      address: editForm.address,
      phone: editForm.phone,
      openTime: editForm.openTime,
      startPrice: editForm.startPrice,
      avatar: avatarForServer || editForm.avatar,
      images: imagesForServer.length > 0 ? imagesForServer : (Array.isArray(editForm.images) ? editForm.images : []),
      // 美食商家专属字段
      avgPrice: editForm.avgPrice || '',
      cuisineType: editForm.cuisineType || '',
      // 酒店商家专属字段
      starRating: editForm.starRating || '',
      roomCount: editForm.roomCount || null,
      facilities: Array.isArray(editForm.facilities) ? editForm.facilities : [],
      tags: Array.isArray(editForm.tags) ? editForm.tags : []
    }

    // 编辑表单保持不变，这样如果用户不关闭对话框继续编辑，数据不会丢失
    // 注意：editForm 已经包含了用户编辑的所有字段，不需要重新赋值

    // 更新本地缓存
    if (userStore.user?.id) {
      localStorage.setItem(`shopInfo_${userStore.user.id}`, JSON.stringify(shopInfo.value))
    }

    showEditDialog.value = false
    ElMessage.success('店铺信息保存成功')
  } catch (error) {
    console.error('更新店铺信息失败:', error)
    ElMessage.error('店铺信息保存失败，请稍后再试')
  }
}

const loadMerchantInfo = async () => {
  try {
    if (!userStore.user?.id) return
    const response = await getMerchantByUserId(userStore.user.id)
    
    // 经过 axios 封装后，response 就是后端的 { success, data, message } 对象
    if (response.success && response.data) {
      merchantInfo.value = response.data
    } else {
      // 兼容旧格式
      const raw = response?.data?.data || response?.data || response?.merchant || null
      if (raw) {
        merchantInfo.value = raw
      }
    }

    // 根据后端商家信息重新构建店铺展示信息和编辑表单
    if (merchantInfo.value) {
      const merged = buildInitialShopInfo(merchantInfo.value, userStore.user)
      shopInfo.value = merged
      Object.assign(editForm, merged)
      
      // 从商家信息中获取真实评分数据（后端已经通过 enrichMerchantWithRating 计算好）
      const ratingValue = merchantInfo.value.rating
      console.log('店铺管理 - 商家评分数据:', { ratingValue, merchantInfo: merchantInfo.value })
      
      if (ratingValue != null && ratingValue !== undefined) {
        const rating =
          typeof ratingValue === 'number'
            ? ratingValue
            : parseFloat(ratingValue) || 0
        // 保留1位小数，与游客端保持一致
        shopStats.value.rating = rating > 0 ? parseFloat(rating.toFixed(1)) : 0
        console.log('更新店铺管理评分为:', shopStats.value.rating)
      } else {
        // 如果没有评分，使用默认值 0
        shopStats.value.rating = 0
        console.log('商家没有评分数据，设置为0')
      }
    }
  } catch (e) {
    console.error('加载商家信息失败:', e)
    // 静默处理，使用默认OTHER分类
  }
}

// 处理实时数据更新
const handleRealtimeUpdate = async (updateInfo) => {
  console.log('ShopManage received update:', updateInfo)
  const { operation, data: updateData } = updateInfo
  
  // 调试日志
  console.log('收到商家实时更新消息:', updateInfo)
  
  // 确保 merchantInfo 已加载
  if (!merchantInfo.value) {
    console.warn('MerchantInfo not loaded yet, skipping update')
    return
  }

  console.log('Current Merchant ID:', merchantInfo.value.id, 'Update ID:', updateData?.id)

  // 仅处理更新操作，且ID必须匹配当前商家ID
  if (operation === 'update') {
    // 强制转换为字符串比较
    const currentId = String(merchantInfo.value.id)
    const updateId = updateData?.id ? String(updateData.id) : null
    
    console.log(`🔍 ID检查: 本地ID=${currentId}, 更新ID=${updateId}`)

    if (updateId === currentId) {
      console.log('✅ 商家ID匹配，准备执行数据同步', updateData)
      
      ElNotification({
        title: '数据同步',
        message: '店铺信息已实时同步',
        type: 'success',
        position: 'bottom-right'
      })
      
      // 1. 优先尝试从 API 重新加载完整数据，确保数据一致性
      try {
        await loadMerchantInfo()
        // loadMerchantInfo 内部已经更新了 shopStats.rating，这里不需要再次更新
      } catch (e) {
        console.error('重新加载商家信息失败，将使用WebSocket数据:', e)
        // 如果API失败，降级使用WS数据
        merchantInfo.value = { ...merchantInfo.value, ...updateData }
        // 尝试从 WebSocket 数据中获取评分
        if (updateData.rating != null && updateData.rating !== undefined) {
          const rating = typeof updateData.rating === 'number' 
            ? updateData.rating 
            : parseFloat(updateData.rating) || 0
          shopStats.value.rating = rating > 0 ? parseFloat(rating.toFixed(1)) : 0
        }
      }
      
      // 2. 重新构建店铺展示信息 (传入 true 强制忽略本地旧缓存)
      const merged = buildInitialShopInfo(merchantInfo.value, userStore.user, true)
      console.log('Merged Shop Info (Fresh):', merged)
      
      // 同步更新本地缓存，防止刷新页面后数据回退
      if (userStore.user?.id) {
        localStorage.setItem(`shopInfo_${userStore.user.id}`, JSON.stringify(merged))
      }
      
      // 使用 nextTick 确保响应式
      nextTick(() => {
        shopInfo.value = merged
        
        // 强制更新 editForm
        if (showEditDialog.value) {
          ElMessage.info('管理员更新了店铺信息，已自动同步')
          
          const newImages = [...merged.images]
          editForm.images = []
          
          setTimeout(() => {
            Object.assign(editForm, merged)
            editForm.images = newImages
            console.log('🔄 editForm 已强制刷新')
          }, 50)
        } else {
          Object.assign(editForm, merged)
        }
      })
    } else {
      console.warn(`❌ ID不匹配: Current=${currentId}, Update=${updateId}`)
      ElNotification({
        title: '忽略更新',
        message: `收到其他商家更新消息 (ID: ${updateId})`,
        type: 'warning',
        duration: 3000,
        position: 'bottom-right'
      })
    }
  }
}

onMounted(async () => {
  // 初始化编辑表单
  Object.assign(editForm, shopInfo.value)

  // 加载商家信息
  await loadMerchantInfo()
  
  // 检查是否需要显示新商家引导
  showNewMerchantGuide.value = checkShowGuide()

  // 连接WebSocket并订阅商家更新
  if (!realtimeSync.isConnected) {
    realtimeSync.connect()
  }
  realtimeSync.subscribe('merchant', handleRealtimeUpdate)
})

onUnmounted(() => {
  // 取消订阅
  realtimeSync.unsubscribe('merchant', handleRealtimeUpdate)
})
</script>

<style scoped>
.shop-manage {
  padding: 20px;
  background: white;
  min-height: 100%;
  max-width: 1400px;
  margin: 0 auto;
}

.back-button-container {
  margin-bottom: 20px;
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

.shop-info-card {
  margin-bottom: 20px;
}

.shop-info {
  display: flex;
  gap: 20px;
}

.shop-avatar {
  flex-shrink: 0;
}

.shop-details {
  flex: 1;
}

.shop-details h3 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 24px;
}

.shop-description {
  color: #606266;
  margin: 0 0 16px 0;
  line-height: 1.6;
}

.shop-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
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
  background: rgba(24, 144, 255, 0.1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1890ff;
  font-size: 24px;
  margin-right: 16px;
  transition: all 0.3s;
}

.stat-card:hover .stat-icon {
  background: #1890ff;
  color: white;
  transform: scale(1.05);
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

.activities-link-card {
  margin-bottom: 20px;
}

.activities-link-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
}

.link-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.link-icon {
  font-size: 32px;
  color: #409eff;
}

.link-text h3 {
  margin: 0 0 4px 0;
  font-size: 18px;
  color: #303133;
}

.link-text p {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 新商家引导提示样式 */
.guide-alert {
  margin-bottom: 20px;
}

.guide-content {
  line-height: 1.8;
}

.guide-content ul {
  margin: 8px 0;
  padding-left: 20px;
}

.guide-content li {
  margin: 4px 0;
}

.guide-tip {
  margin-top: 12px;
  padding: 8px;
  background: #f0f9ff;
  border-left: 3px solid #409eff;
  border-radius: 4px;
}

/* 字段提示样式 */
.field-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.5;
}

/* 图片上传区域样式 */
.image-upload-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 图片列表容器 */
.image-list-container {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: flex-start;
}

/* 图片列表 */
.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  flex: 1;
}

/* 单个图片项 */
.image-item {
  position: relative;
  width: 150px;
  height: 150px;
  cursor: move;
  transition: all 0.3s;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid transparent;
}

.image-item:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.image-item.is-cover {
  border-color: #67c23a;
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.3);
}

.image-item.drag-over {
  border-color: #409eff;
  background-color: #ecf5ff;
  transform: scale(1.05);
}

.image-thumbnail {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
  border-radius: 6px;
}

.image-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 8px;
  opacity: 0;
  transition: opacity 0.3s;
}

.image-item:hover .image-overlay {
  opacity: 1;
}

.image-badges {
  display: flex;
  justify-content: flex-start;
}

.image-actions {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.image-index {
  position: absolute;
  top: 4px;
  right: 4px;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 600;
}

.image-item.is-cover .image-index {
  background: rgba(103, 194, 58, 0.8);
}

/* 上传按钮 */
.image-upload-btn {
  width: 150px;
  height: 150px;
}

.image-upload-btn :deep(.el-upload) {
  width: 100%;
  height: 100%;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.image-upload-btn :deep(.el-upload):hover {
  border-color: #409eff;
  background-color: #f5f7fa;
}

.upload-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #fafafa;
  color: #8c939d;
}

.upload-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.upload-text {
  font-size: 14px;
}

.image-info {
  margin-top: 8px;
}

.image-hint {
  font-size: 14px;
  color: #606266;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 4px;
}

.image-usage-list {
  margin: 8px 0;
  padding-left: 20px;
  color: #606266;
  font-size: 13px;
}

.image-usage-list li {
  margin: 4px 0;
}

@media (max-width: 768px) {
  .shop-info {
    flex-direction: column;
    text-align: center;
  }
  
  .stats-row .el-col {
    margin-bottom: 16px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
}
</style>
