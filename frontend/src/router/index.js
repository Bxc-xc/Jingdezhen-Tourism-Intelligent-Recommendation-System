import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

// NProgress 配置
NProgress.configure({ 
  showSpinner: false,
  easing: 'ease',
  speed: 500
})

// 游客端路由
const touristRoutes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/tourist/Home.vue'),
    meta: { title: '首页', role: 'TOURIST' }
  },
  {
    path: '/scenic/:id',
    name: 'ScenicDetail',
    component: () => import('../views/tourist/ScenicDetail.vue'),
    meta: { title: '景点详情', role: 'TOURIST' }
  },
  {
    path: '/recommend',
    name: 'Recommend',
    component: () => import('../views/tourist/Recommend.vue'),
    meta: { title: '推荐景点', role: 'TOURIST' }
  },
  {
    path: '/ceramic-experience',
    name: 'CeramicExperience',
    component: () => import('../views/tourist/CeramicExperience.vue'),
    meta: { title: '陶瓷体验', role: 'TOURIST' }
  },
  {
    path: '/marketplace/:id',
    name: 'MarketplaceDetail',
    component: () => import('../views/tourist/MarketplaceDetail.vue'),
    meta: { title: '市集详情', role: 'TOURIST' }
  },
  {
    path: '/ceramic-workshop/:id',
    name: 'CeramicWorkshopDetail',
    component: () => import('../views/tourist/CeramicWorkshopDetail.vue'),
    meta: { title: '陶瓷工坊详情', role: 'TOURIST' }
  },
  {
    path: '/hotel/:id',
    name: 'HotelDetail',
    component: () => import('../views/tourist/HotelDetail.vue'),
    meta: { title: '酒店详情', role: 'TOURIST' }
  },
  {
    path: '/food/:id',
    name: 'FoodDetail',
    component: () => import('../views/tourist/FoodDetail.vue'),
    meta: { title: '美食详情', role: 'TOURIST' }
  },
  {
    path: '/route/:id',
    name: 'RouteDetail',
    component: () => import('../views/tourist/RouteDetail.vue'),
    meta: { title: '路线详情', role: 'TOURIST' }
  },
  {
    path: '/plan',
    name: 'Plan',
    component: () => import('../views/tourist/Plan.vue'),
    meta: { title: '行程规划', role: 'TOURIST' }
  },
  {
    path: '/plan/:id',
    name: 'PlanDetail',
    component: () => import('../views/tourist/PlanDetail.vue'),
    meta: { title: '行程详情', role: 'TOURIST' }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/tourist/Profile.vue'),
    meta: { title: '个人中心', role: 'TOURIST', requiresAuth: true }
  }
]

// 商家端路由
const merchantRoutes = [
  {
    path: '/merchant',
    name: 'MerchantHome',
    component: () => import('../views/merchant/Home.vue'),
    meta: { title: '商家中心', role: 'MERCHANT', requiresAuth: true }
  },
  {
    path: '/merchant/shop',
    name: 'MerchantShop',
    component: () => import('../views/merchant/ShopManage.vue'),
    meta: { title: '店铺管理', role: 'MERCHANT', requiresAuth: true }
  },
  {
    path: '/merchant/rooms',
    name: 'MerchantRooms',
    component: () => import('../views/merchant/RoomTypeManage.vue'),
    meta: { title: '房型管理', role: 'MERCHANT', requiresAuth: true }
  },
  {
    path: '/merchant/room-calendar',
    name: 'MerchantRoomCalendar',
    component: () => import('../views/merchant/RoomCalendar.vue'),
    meta: { title: '房态管理', role: 'MERCHANT', requiresAuth: true }
  },
  {
    path: '/merchant/orders',
    name: 'MerchantOrders',
    component: () => import('../views/merchant/OrderManage.vue'),
    meta: { title: '订单管理', role: 'MERCHANT', requiresAuth: true }
  },
  {
    path: '/merchant/reviews',
    name: 'MerchantReviews',
    component: () => import('../views/merchant/ReviewManage.vue'),
    meta: { title: '评价管理', role: 'MERCHANT', requiresAuth: true }
  },
  {
    path: '/merchant/qualification',
    name: 'MerchantQualification',
    component: () => import('../views/merchant/QualificationApply.vue'),
    meta: { title: '资质申请', role: 'MERCHANT', requiresAuth: true }
  },
  {
    path: '/merchant/statistics',
    name: 'MerchantStatistics',
    component: () => import('../views/merchant/Statistics.vue'),
    meta: { title: '数据统计', role: 'MERCHANT', requiresAuth: true }
  },
  {
    path: '/merchant/activities',
    name: 'MerchantActivityManage',
    component: () => import('../views/merchant/ActivityManage.vue'),
    meta: { title: '活动管理', role: 'MERCHANT', requiresAuth: true }
  },
  {
    path: '/merchant/profile',
    name: 'MerchantProfile',
    component: () => import('../views/merchant/Profile.vue'),
    meta: { title: '个人资料', role: 'MERCHANT', requiresAuth: true }
  },
  {
    path: '/merchant/group-buy',
    name: 'MerchantGroupBuy',
    component: () => import('../views/merchant/GroupBuyManage.vue'),
    meta: { title: '团购管理', role: 'MERCHANT', requiresAuth: true }
  }
]

// 管理员端路由
const adminRoutes = [
  {
    path: '/admin',
    name: 'AdminHome',
    component: () => import('../views/admin/Home.vue'),
    meta: { title: '管理后台', role: 'ADMIN', requiresAuth: true }
  },
  {
    path: '/admin/audit',
    name: 'AdminAudit',
    component: () => import('../views/admin/AuditManage.vue'),
    meta: { title: '审核管理', role: 'ADMIN', requiresAuth: true }
  },
  {
    path: '/admin/statistics',
    name: 'AdminStatistics',
    component: () => import('../views/admin/Statistics.vue'),
    meta: { title: '统计分析', role: 'ADMIN', requiresAuth: true }
  },
  {
    path: '/admin/system',
    name: 'AdminSystem',
    component: () => import('../views/admin/SystemManage.vue'),
    meta: { title: '系统管理', role: 'ADMIN', requiresAuth: true }
  },
  {
    path: '/admin/review',
    name: 'AdminReview',
    component: () => import('../views/admin/ReviewManage.vue'),
    meta: { title: '评价管理', role: 'ADMIN', requiresAuth: true }
  },
  {
    path: '/admin/room-types',
    name: 'AdminRoomTypes',
    component: () => import('../views/admin/RoomTypeManage.vue'),
    meta: { title: '房型管理', role: 'ADMIN', requiresAuth: true }
  },
  {
    path: '/admin/logs',
    name: 'AdminLogs',
    component: () => import('../views/admin/OperationLog.vue'),
    meta: { title: '操作日志', role: 'ADMIN', requiresAuth: true }
  },
  {
    path: '/admin/profile',
    name: 'AdminProfile',
    component: () => import('../views/admin/Profile.vue'),
    meta: { title: '个人资料', role: 'ADMIN', requiresAuth: true }
  },
  {
    path: '/admin/group-buy',
    name: 'AdminGroupBuy',
    component: () => import('../views/admin/GroupBuyManage.vue'),
    meta: { title: '团购管理', role: 'ADMIN', requiresAuth: true }
  },
  {
    path: '/admin/activities',
    name: 'AdminActivities',
    component: () => import('../views/admin/ActivityManage.vue'),
    meta: { title: '店铺活动管理', role: 'ADMIN', requiresAuth: true }
  }
]

// 认证相关路由
const authRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/auth/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/auth/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('../views/auth/ForgotPassword.vue'),
    meta: { title: '忘记密码' }
  }
]

const routes = [
  ...touristRoutes,
  ...merchantRoutes,
  ...adminRoutes,
  ...authRoutes,
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    return { top: 0, left: 0, behavior: 'instant' }
  }
})

// 路由守卫
router.beforeEach((to, from, next) => {
  NProgress.start() // 开启进度条
  const userStore = useUserStore()
  
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 景德镇旅游智能推荐系统`
  }
  
  // 检查是否需要认证（requiresAuth 才是真正的访问控制，meta.role 仅作归属标识）
  if (to.meta.requiresAuth) {
    if (!userStore.token) {
      next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
      return
    }
  }
  
  next()
})

router.afterEach(() => {
  NProgress.done() // 结束进度条
})

export default router
