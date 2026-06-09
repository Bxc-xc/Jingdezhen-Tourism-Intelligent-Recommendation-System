import request from '../utils/request'

const randomBetween = (min, max) => Math.floor(Math.random() * (max - min + 1)) + min

const formatDateTag = (date) => {
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${month}-${day}`
}

const buildTrendData = (days, userBase = 1400, orderBase = 190) => {
  const today = new Date()
  const dates = []
  const orderData = []
  const userData = []
  let userSeed = userBase
  let orderSeed = orderBase

  for (let i = days - 1; i >= 0; i--) {
    const date = new Date(today)
    date.setDate(today.getDate() - i)
    const day = date.getDay()
    const weekendFactor = day === 0 || day === 6 ? 1.16 : 1
    const campaignFactor = i === 3 || i === 10 ? 1.22 : 1

    const dailyUsers = Math.max(8, Math.round((userSeed + randomBetween(-20, 24)) * weekendFactor / 26))
    const dailyOrders = Math.max(40, Math.round((orderSeed + randomBetween(-38, 45)) * weekendFactor * campaignFactor))

    dates.push(formatDateTag(date))
    userData.push(dailyUsers)
    orderData.push(dailyOrders)
    userSeed += randomBetween(1, 4)
    orderSeed += randomBetween(-3, 4)
  }

  return { dates, userData, orderData }
}

const buildMerchantApplications = () => {
  const shopTypes = ['民宿', '餐饮', '陶瓷工坊', '旅行社', '文创市集']
  return Array.from({ length: 5 }).map((_, index) => ({
    id: index + 1,
    shopName: `商家申请-${100 + index}`,
    type: shopTypes[randomBetween(0, shopTypes.length - 1)],
    submittedAt: `${randomBetween(1, 23)}小时前`,
    status: 'PENDING'
  }))
}

const buildMockDashboardData = () => {
  const trend7 = buildTrendData(7)
  const trend30 = buildTrendData(30)
  const todayOrders = trend7.orderData[trend7.orderData.length - 1]
  const todayUsers = trend7.userData[trend7.userData.length - 1]
  const totalUsers = 26000 + trend30.userData.reduce((sum, val) => sum + val, 0)
  const totalOrders = 138000 + trend30.orderData.reduce((sum, val) => sum + val, 0)

  const prevOrder = trend7.orderData[trend7.orderData.length - 2] || todayOrders
  const prevUser = trend7.userData[trend7.userData.length - 2] || todayUsers

  const orderTrend = Number((((todayOrders - prevOrder) / Math.max(prevOrder, 1)) * 100).toFixed(1))
  const userTrend = Number((((todayUsers - prevUser) / Math.max(prevUser, 1)) * 100).toFixed(1))

  return {
    statCards: {
      totalUsers,
      totalOrders,
      todayNewUsers: todayUsers,
      todayOrders,
      userTrend,
      orderTrend
    },
    orderTrend7d: trend7,
    orderTrend30d: trend30,
    userTrend30d: {
      dates: trend30.dates,
      userData: trend30.userData
    },
    merchantApplications: buildMerchantApplications()
  }
}

export async function getDashboardData() {
  try {
    const response = await request.get('/admin/dashboard')
    if (response?.success && response?.data) {
      return response.data
    }
    return buildMockDashboardData()
  } catch (error) {
    // 接口未就绪时，自动回退到 mock，保证仪表盘可用
    return buildMockDashboardData()
  }
}

export async function getDashboardDataFromApi() {
  return request.get('/admin/dashboard')
}
