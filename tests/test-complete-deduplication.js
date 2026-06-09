// 全面测试景点去重功能
const axios = require('axios');

const BASE_URL = 'http://localhost:8889';

async function testCompleteDeduplication() {
  console.log('🧪 全面测试景点去重功能...\n');
  
  try {
    // 1. 测试热门景点API
    console.log('1. 测试热门景点API');
    const popularResponse = await axios.get(`${BASE_URL}/api/scenic/popular`);
    const popularScenics = popularResponse.data.data || popularResponse.data;
    console.log(`✅ 热门景点数量: ${popularScenics.length}`);
    console.log('热门景点列表:');
    popularScenics.forEach((scenic, index) => {
      console.log(`  ${index + 1}. ${scenic.name} (ID: ${scenic.id})`);
    });
    
    // 2. 测试推荐景点API
    console.log('\n2. 测试推荐景点API');
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
    
    // 4. 测试所有景点API
    console.log('\n4. 测试所有景点API');
    const allResponse = await axios.get(`${BASE_URL}/api/scenic`);
    const allScenics = allResponse.data.data || allResponse.data;
    console.log(`✅ 所有景点数量: ${allScenics.length}`);
    
    // 5. 验证数据完整性
    console.log('\n5. 验证数据完整性');
    const allIds = new Set(allScenics.map(scenic => scenic.id));
    const popularIdsSet = new Set(popularScenics.map(scenic => scenic.id));
    const recommendIdsSet = new Set(recommendScenics.map(scenic => scenic.id));
    
    // 检查热门景点是否都在所有景点中
    const popularInAll = popularScenics.every(scenic => allIds.has(scenic.id));
    console.log(`热门景点都在所有景点中: ${popularInAll ? '✅' : '❌'}`);
    
    // 检查推荐景点是否都在所有景点中
    const recommendInAll = recommendScenics.every(scenic => allIds.has(scenic.id));
    console.log(`推荐景点都在所有景点中: ${recommendInAll ? '✅' : '❌'}`);
    
    // 6. 统计信息
    console.log('\n6. 统计信息');
    console.log(`所有景点总数: ${allScenics.length}`);
    console.log(`热门景点数量: ${popularScenics.length}`);
    console.log(`推荐景点数量: ${recommendScenics.length}`);
    console.log(`重复景点数: ${duplicates.length}`);
    console.log(`去重后总景点数: ${popularScenics.length + recommendScenics.length - duplicates.length}`);
    
    // 7. 测试不同推荐算法
    console.log('\n7. 测试不同推荐算法');
    const algorithms = ['hot', 'personal', 'similar', 'nearby'];
    
    for (const algorithm of algorithms) {
      try {
        const algResponse = await axios.get(`${BASE_URL}/api/scenic/recommend`, {
          params: { algorithm }
        });
        const algScenics = algResponse.data.data || algResponse.data;
        const algIds = new Set(algScenics.map(scenic => scenic.id));
        const algDuplicates = popularScenics.filter(scenic => algIds.has(scenic.id));
        
        console.log(`${algorithm} 推荐: ${algScenics.length}个景点, 重复: ${algDuplicates.length}个`);
      } catch (error) {
        console.log(`${algorithm} 推荐: 测试失败 - ${error.message}`);
      }
    }
    
    // 8. 最终验证
    console.log('\n8. 最终验证结果');
    if (duplicates.length === 0 && popularInAll && recommendInAll) {
      console.log('🎉 所有测试通过！景点去重功能完全正常！');
    } else {
      console.log('❌ 部分测试失败，需要进一步检查。');
    }
    
  } catch (error) {
    console.error('❌ 测试失败:', error.response?.data || error.message);
  }
}

// 运行测试
testCompleteDeduplication().catch(console.error);