// 测试景点去重功能
const axios = require('axios');

const BASE_URL = 'http://localhost:8889';

async function testScenicDeduplication() {
  console.log('🧪 测试景点去重功能...\n');
  
  try {
    // 1. 获取热门景点
    console.log('1. 获取热门景点');
    const popularResponse = await axios.get(`${BASE_URL}/api/scenic`);
    const popularScenics = popularResponse.data.data || popularResponse.data;
    console.log(`✅ 热门景点数量: ${popularScenics.length}`);
    console.log('热门景点列表:');
    popularScenics.forEach((scenic, index) => {
      console.log(`  ${index + 1}. ${scenic.name} (ID: ${scenic.id})`);
    });
    
    // 2. 获取推荐景点
    console.log('\n2. 获取推荐景点');
    const recommendResponse = await axios.get(`${BASE_URL}/api/scenic/recommend`);
    const recommendScenics = recommendResponse.data.data || recommendResponse.data;
    console.log(`✅ 推荐景点数量: ${recommendScenics.length}`);
    console.log('推荐景点列表:');
    recommendScenics.forEach((scenic, index) => {
      console.log(`  ${index + 1}. ${scenic.name} (ID: ${scenic.id})`);
    });
    
    // 3. 检查重复
    console.log('\n3. 检查重复景点');
    const popularIds = new Set(popularScenics.map(scenic => scenic.id));
    const recommendIds = new Set(recommendScenics.map(scenic => scenic.id));
    
    const duplicates = popularScenics.filter(scenic => recommendIds.has(scenic.id));
    
    if (duplicates.length === 0) {
      console.log('✅ 没有发现重复景点！去重功能正常工作。');
    } else {
      console.log(`❌ 发现 ${duplicates.length} 个重复景点:`);
      duplicates.forEach(scenic => {
        console.log(`  - ${scenic.name} (ID: ${scenic.id})`);
      });
    }
    
    // 4. 统计信息
    console.log('\n4. 统计信息');
    console.log(`热门景点总数: ${popularScenics.length}`);
    console.log(`推荐景点总数: ${recommendScenics.length}`);
    console.log(`重复景点数: ${duplicates.length}`);
    console.log(`去重后总景点数: ${popularScenics.length + recommendScenics.length - duplicates.length}`);
    
  } catch (error) {
    console.error('❌ 测试失败:', error.response?.data || error.message);
  }
}

// 运行测试
testScenicDeduplication().catch(console.error);