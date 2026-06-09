// 端到端用例：登录 → 浏览 → 收藏 → 评论
const axios = require('axios')

const BASE_URL = 'http://localhost:8889/api'

async function e2e() {
  const log = (title, data) => {
    console.log(`\n=== ${title} ===`)
    if (data !== undefined) {
      console.log(typeof data === 'string' ? data : JSON.stringify(data, null, 2))
    }
  }

  try {
    // 1) 登录
    log('步骤1 登录(游客账号)', 'POST /auth/login')
    const loginRes = await axios.post(`${BASE_URL}/auth/login`, {
      username: 'tourist1',
      password: '123456'
    })
    if (!loginRes.data?.success) throw new Error('登录失败')
    const token = loginRes.data.token
    const user = loginRes.data.data
    log('登录成功，关键信息', { user, token: token?.slice(0, 16) + '...' })

    // 2) 浏览景点（热门景点 + 详情）
    log('步骤2 获取热门景点', 'GET /scenic/popular')
    const popularRes = await axios.get(`${BASE_URL}/scenic/popular`)
    if (!popularRes.data?.success || popularRes.data.data.length === 0) throw new Error('热门景点为空')
    const scenic = popularRes.data.data[0]
    log('热门景点样例', { id: scenic.id, name: scenic.name })

    log('获取景点详情', `GET /scenic/${scenic.id}`)
    const detailRes = await axios.get(`${BASE_URL}/scenic/${scenic.id}`)
    if (!detailRes.data?.success) throw new Error('获取景点详情失败')
    log('景点详情摘要', { id: detailRes.data.data.id, name: detailRes.data.data.name, price: detailRes.data.data.price })

    // 3) 收藏景点
    log('步骤3 收藏景点', 'POST /scenic/favorite')
    const favRes = await axios.post(`${BASE_URL}/scenic/favorite`, {
      userId: user.id,
      scenicId: scenic.id
    })
    if (!favRes.data?.success) throw new Error('收藏失败')
    log('收藏结果', favRes.data)

    log('校验收藏列表', `GET /scenic/favorites?userId=${user.id}`)
    const favListRes = await axios.get(`${BASE_URL}/scenic/favorites`, { params: { userId: user.id } })
    const isInFav = (favListRes.data?.data || []).some(s => String(s.id) === String(scenic.id))
    log('收藏校验', { total: favListRes.data?.data?.length || 0, containsTarget: isInFav })

    // 4) 发表评论
    log('步骤4 发表评论', 'POST /reviews')
    const reviewRes = await axios.post(`${BASE_URL}/reviews`, {
      userId: user.id,
      scenicSpotId: scenic.id,
      content: `自动化评论：风景很棒 ${new Date().toISOString()}`,
      rating: 5
    })
    if (!reviewRes.data?.success) throw new Error('评论失败')
    log('评论创建结果', { id: reviewRes.data.data.id, rating: reviewRes.data.data.rating })

    log('拉取景点评价', `GET /reviews/scenic/${scenic.id}`)
    const reviewListRes = await axios.get(`${BASE_URL}/reviews/scenic/${scenic.id}`)
    const createdExists = (reviewListRes.data?.data || []).some(r => String(r.id) === String(reviewRes.data.data.id))
    log('评论校验', { count: reviewListRes.data?.data?.length || 0, containsCreated: createdExists })

    console.log('\n✅ E2E 用例完成: 登录 → 浏览 → 收藏 → 评论')
  } catch (err) {
    console.error('\n❌ E2E 用例失败', {
      message: err.message,
      status: err.response?.status,
      data: err.response?.data
    })
    process.exit(1)
  }
}

if (require.main === module) {
  e2e()
}

module.exports = { e2e }


