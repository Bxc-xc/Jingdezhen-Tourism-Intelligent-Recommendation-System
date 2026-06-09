const axios = require('axios');

const BASE_URL = 'http://localhost:8889/api';

// 测试收藏功能
async function testFavorite() {
  console.log('=== 测试收藏功能 ===');
  
  try {
    // 1. 添加收藏
    console.log('1. 添加收藏...');
    const addResponse = await axios.post(`${BASE_URL}/scenic/favorite`, {
      userId: 1,
      scenicId: 1
    });
    console.log('添加收藏结果:', addResponse.data);
    
    // 2. 获取收藏列表
    console.log('2. 获取收藏列表...');
    const listResponse = await axios.get(`${BASE_URL}/scenic/favorites?userId=1`);
    console.log('收藏列表:', listResponse.data);
    
    // 3. 检查是否已收藏
    console.log('3. 检查是否已收藏...');
    const checkResponse = await axios.get(`${BASE_URL}/scenic/favorite/1?userId=1`);
    console.log('收藏状态:', checkResponse.data);
    
    // 4. 取消收藏
    console.log('4. 取消收藏...');
    const removeResponse = await axios.delete(`${BASE_URL}/scenic/favorite/1?userId=1`);
    console.log('取消收藏结果:', removeResponse.data);
    
  } catch (error) {
    console.error('收藏功能测试失败:', error.response?.data || error.message);
  }
}

// 测试行程规划功能
async function testTravelPlan() {
  console.log('\n=== 测试行程规划功能 ===');
  
  try {
    // 1. 生成行程规划
    console.log('1. 生成行程规划...');
    const generateResponse = await axios.post(`${BASE_URL}/plan/generate`, {
      userId: 1,
      days: 3,
      startDate: '2024-02-01',
      budget: 'medium',
      transport: 'car',
      interests: ['陶瓷文化', '历史古迹']
    });
    console.log('生成行程结果:', generateResponse.data);
    
    const planId = generateResponse.data.data?.id;
    if (planId) {
      // 2. 获取行程详情
      console.log('2. 获取行程详情...');
      const detailResponse = await axios.get(`${BASE_URL}/plan/${planId}`);
      console.log('行程详情:', detailResponse.data);
      
      // 3. 获取用户行程列表
      console.log('3. 获取用户行程列表...');
      const listResponse = await axios.get(`${BASE_URL}/plan/user/1`);
      console.log('行程列表:', listResponse.data);
      
      // 4. 更新行程
      console.log('4. 更新行程...');
      const updateResponse = await axios.put(`${BASE_URL}/plan/${planId}`, {
        title: '更新后的景德镇之旅',
        description: '这是一个更新后的行程描述'
      });
      console.log('更新行程结果:', updateResponse.data);
      
      // 5. 删除行程
      console.log('5. 删除行程...');
      const deleteResponse = await axios.delete(`${BASE_URL}/plan/${planId}`);
      console.log('删除行程结果:', deleteResponse.data);
    }
    
  } catch (error) {
    console.error('行程规划功能测试失败:', error.response?.data || error.message);
  }
}

// 运行测试
async function runTests() {
  console.log('开始测试收藏和行程规划功能...\n');
  
  await testFavorite();
  await testTravelPlan();
  
  console.log('\n测试完成！');
}

runTests().catch(console.error);