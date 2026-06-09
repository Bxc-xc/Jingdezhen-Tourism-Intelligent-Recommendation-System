#!/usr/bin/env node
/**
 * 景德镇旅游系统 - 自动化测试入口
 * 用法: node tests/run-tests.js [--suite=auth|scenic|favorite|groupbuy|all]
 *
 * 依赖: npm install axios (在项目根目录或 tests/ 目录下)
 */

const axios = require('axios');

const BASE_URL = process.env.API_URL || 'http://localhost:8889/api';
const SUITE = (process.argv.find(a => a.startsWith('--suite=')) || '--suite=all').split('=')[1];

// ---- 工具函数 ----

const results = { passed: 0, failed: 0, errors: [] };

function pass(name) {
  results.passed++;
  console.log(`  ✅ ${name}`);
}

function fail(name, reason) {
  results.failed++;
  console.log(`  ❌ ${name}: ${reason}`);
  results.errors.push({ name, reason });
}

async function test(name, fn) {
  try {
    await fn();
    pass(name);
  } catch (e) {
    fail(name, e.response?.data?.message || e.message);
  }
}

function assert(condition, message) {
  if (!condition) throw new Error(message);
}

// ---- 测试套件 ----

// 1. 系统健康检查
async function suiteHealth() {
  console.log('\n🔌 系统健康检查');
  await test('后端API可达', async () => {
    const r = await axios.get(`${BASE_URL}/scenic`);
    assert(r.status === 200, `状态码 ${r.status}`);
  });
  await test('数据库有景点数据', async () => {
    const r = await axios.get(`${BASE_URL}/scenic`);
    assert(r.data.data?.length > 0, '景点列表为空');
  });
}

// 2. 用户认证
async function suiteAuth() {
  console.log('\n👤 用户认证');
  let token = null;

  await test('游客登录成功', async () => {
    const r = await axios.post(`${BASE_URL}/auth/login`, { username: 'tourist1', password: '123456' });
    assert(r.data.success, r.data.message);
    assert(r.data.token, 'token 为空');
    token = r.data.token;
  });

  await test('错误密码登录失败', async () => {
    try {
      await axios.post(`${BASE_URL}/auth/login`, { username: 'tourist1', password: 'wrong' });
      throw new Error('应该返回错误');
    } catch (e) {
      assert(e.response?.status === 400, '期望 400');
    }
  });

  await test('空用户名登录失败', async () => {
    try {
      await axios.post(`${BASE_URL}/auth/login`, { username: '', password: '123456' });
      throw new Error('应该返回错误');
    } catch (e) {
      assert(e.response?.status === 400, '期望 400');
    }
  });

  await test('Token 验证有效', async () => {
    if (!token) throw new Error('无 token，跳过');
    const r = await axios.post(`${BASE_URL}/auth/verify`, {}, {
      headers: { Authorization: `Bearer ${token}` }
    });
    assert(r.data.success, r.data.message);
  });

  await test('管理员登录成功', async () => {
    const r = await axios.post(`${BASE_URL}/auth/login`, { username: 'admin', password: '123456' });
    assert(r.data.success, r.data.message);
    assert(r.data.data?.role === 'ADMIN', `角色应为 ADMIN，实际: ${r.data.data?.role}`);
  });
}

// 3. 景点功能
async function suiteScenic() {
  console.log('\n🏛️  景点功能');

  await test('获取景点列表', async () => {
    const r = await axios.get(`${BASE_URL}/scenic`);
    assert(r.data.success, r.data.message);
    assert(Array.isArray(r.data.data), '返回值不是数组');
  });

  await test('获取景点详情', async () => {
    const list = await axios.get(`${BASE_URL}/scenic`);
    const id = list.data.data[0]?.id;
    assert(id, '没有景点可测试');
    const r = await axios.get(`${BASE_URL}/scenic/${id}`);
    assert(r.data.success, r.data.message);
    assert(r.data.data?.id === id, 'ID 不匹配');
  });

  await test('景点不存在返回 404', async () => {
    try {
      await axios.get(`${BASE_URL}/scenic/999999`);
      throw new Error('应该返回 404');
    } catch (e) {
      assert(e.response?.status === 404, `期望 404，实际 ${e.response?.status}`);
    }
  });

  await test('关键词搜索景点', async () => {
    const r = await axios.get(`${BASE_URL}/scenic/search`, { params: { keyword: '陶瓷' } });
    assert(r.data.success, r.data.message);
    assert(Array.isArray(r.data.data), '返回值不是数组');
  });

  await test('获取热门景点', async () => {
    const r = await axios.get(`${BASE_URL}/scenic/popular`);
    assert(r.data.success, r.data.message);
  });

  await test('获取推荐景点', async () => {
    const r = await axios.get(`${BASE_URL}/scenic/recommend`);
    assert(r.data.success, r.data.message);
  });
}

// 4. 收藏功能
async function suiteFavorite() {
  console.log('\n❤️  收藏功能');

  // 先登录获取 userId
  let userId = null;
  let scenicId = null;
  try {
    const loginRes = await axios.post(`${BASE_URL}/auth/login`, { username: 'tourist1', password: '123456' });
    userId = loginRes.data.data?.id;
    const scenicRes = await axios.get(`${BASE_URL}/scenic`);
    scenicId = scenicRes.data.data?.[0]?.id;
  } catch (e) {
    fail('收藏测试前置条件', '登录或获取景点失败: ' + e.message);
    return;
  }

  await test('检查收藏状态', async () => {
    const r = await axios.get(`${BASE_URL}/scenic/favorite/${scenicId}`, { params: { userId } });
    assert(r.data.success, r.data.message);
    assert(typeof r.data.data === 'boolean', '返回值应为 boolean');
  });

  // 先确保未收藏（忽略错误）
  await axios.delete(`${BASE_URL}/scenic/favorite/${scenicId}?userId=${userId}`).catch(() => {});

  await test('添加景点收藏', async () => {
    const r = await axios.post(`${BASE_URL}/scenic/favorite`, { userId, scenicId });
    assert(r.data.success, r.data.message);
  });

  await test('重复收藏应报错', async () => {
    try {
      await axios.post(`${BASE_URL}/scenic/favorite`, { userId, scenicId });
      throw new Error('应该报错');
    } catch (e) {
      assert(e.response?.status === 400, '期望 400');
    }
  });

  await test('取消景点收藏', async () => {
    const r = await axios.delete(`${BASE_URL}/scenic/favorite/${scenicId}?userId=${userId}`);
    assert(r.data.success, r.data.message);
  });

  await test('取消不存在的收藏应报错', async () => {
    try {
      await axios.delete(`${BASE_URL}/scenic/favorite/${scenicId}?userId=${userId}`);
      throw new Error('应该报错');
    } catch (e) {
      assert(e.response?.status === 400, '期望 400');
    }
  });

  await test('获取用户收藏列表', async () => {
    const r = await axios.get(`${BASE_URL}/scenic/favorites`, { params: { userId } });
    assert(r.data.success, r.data.message);
    assert(Array.isArray(r.data.data), '返回值应为数组');
  });
}

// 5. 团购功能
async function suiteGroupBuy() {
  console.log('\n🛒 团购功能');

  let merchantToken = null;
  let merchantId = null;
  let groupBuyId = null;

  try {
    const r = await axios.post(`${BASE_URL}/auth/login`, { username: 'merchant1', password: '123456' });
    merchantToken = r.data.token;
    merchantId = r.data.data?.merchantId || r.data.data?.id;
  } catch (e) {
    fail('团购测试前置条件', '商家登录失败: ' + e.message);
    return;
  }

  await test('获取已上架团购列表', async () => {
    const r = await axios.get(`${BASE_URL}/group-buy/approved`);
    assert(r.data.success, r.data.message);
    assert(Array.isArray(r.data.data), '返回值应为数组');
  });

  await test('获取所有团购（管理员）', async () => {
    const adminLogin = await axios.post(`${BASE_URL}/auth/login`, { username: 'admin', password: '123456' });
    const adminToken = adminLogin.data.token;
    const r = await axios.get(`${BASE_URL}/group-buy/admin/all`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    assert(r.data.success, r.data.message);
  });
}

// 6. 搜索功能
async function suiteSearch() {
  console.log('\n🔍 搜索功能');

  await test('综合搜索', async () => {
    const r = await axios.get(`${BASE_URL}/search`, { params: { keyword: '景德镇' } });
    assert(r.data.success, r.data.message);
  });

  await test('空关键词搜索', async () => {
    const r = await axios.get(`${BASE_URL}/search`, { params: { keyword: '' } });
    // 空关键词应返回成功（可能是空列表或全部）
    assert(r.status === 200, `状态码 ${r.status}`);
  });
}

// ---- 主入口 ----

async function main() {
  console.log('='.repeat(50));
  console.log('  景德镇旅游系统 - 自动化测试');
  console.log(`  目标: ${BASE_URL}`);
  console.log(`  套件: ${SUITE}`);
  console.log('='.repeat(50));

  const suites = {
    health:   suiteHealth,
    auth:     suiteAuth,
    scenic:   suiteScenic,
    favorite: suiteFavorite,
    groupbuy: suiteGroupBuy,
    search:   suiteSearch,
  };

  if (SUITE === 'all') {
    for (const fn of Object.values(suites)) {
      await fn();
    }
  } else if (suites[SUITE]) {
    await suites[SUITE]();
  } else {
    console.error(`未知套件: ${SUITE}，可选: ${Object.keys(suites).join(', ')}, all`);
    process.exit(1);
  }

  // 汇总
  const total = results.passed + results.failed;
  console.log('\n' + '='.repeat(50));
  console.log(`  结果: ${results.passed}/${total} 通过`);
  if (results.failed > 0) {
    console.log(`  失败项:`);
    results.errors.forEach(e => console.log(`    - ${e.name}: ${e.reason}`));
  }
  console.log('='.repeat(50));

  process.exit(results.failed > 0 ? 1 : 0);
}

main().catch(e => {
  console.error('测试运行异常:', e.message);
  process.exit(1);
});
