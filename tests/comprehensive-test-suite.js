// 景德镇旅游智能推荐系统 - 全面功能测试套件
const axios = require('axios');

class TourismSystemTester {
  constructor() {
    this.baseURL = 'http://localhost:8889/api';
    this.testResults = {
      passed: 0,
      failed: 0,
      errors: []
    };
    this.testUser = null;
    this.testMerchant = null;
    this.testAdmin = null;
  }

  // 测试结果记录
  logResult(testName, success, message = '') {
    if (success) {
      this.testResults.passed++;
      console.log(`✅ ${testName}: 通过`);
    } else {
      this.testResults.failed++;
      console.log(`❌ ${testName}: 失败 - ${message}`);
      this.testResults.errors.push({ test: testName, error: message });
    }
  }

  // 1. 系统连接测试
  async testSystemConnection() {
    console.log('\n🔌 === 系统连接测试 ===');
    
    try {
      const response = await axios.get(`${this.baseURL}/scenic`);
      this.logResult('后端API连接', response.status === 200);
      
      // 测试数据库连接
      const hasData = response.data.data && response.data.data.length > 0;
      this.logResult('数据库连接', hasData, hasData ? '' : '没有景点数据');
      
    } catch (error) {
      this.logResult('后端API连接', false, error.message);
    }
  }

  // 2. 用户认证功能测试
  async testUserAuthentication() {
    console.log('\n👤 === 用户认证功能测试 ===');
    
    // 测试用户登录
    try {
      const loginResponse = await axios.post(`${this.baseURL}/auth/login`, {
        username: 'tourist1',
        password: '123456'
      });
      
      const loginSuccess = loginResponse.data.success && loginResponse.data.data;
      this.logResult('游客登录', loginSuccess);
      
      if (loginSuccess) {
        this.testUser = loginResponse.data.data;
      }
      
    } catch (error) {
      this.logResult('游客登录', false, error.message);
    }

    // 测试商家登录
    try {
      const merchantLogin = await axios.post(`${this.baseURL}/auth/login`, {
        username: 'merchant1',
        password: '123456'
      });
      
      const merchantSuccess = merchantLogin.data.success && merchantLogin.data.data;
      this.logResult('商家登录', merchantSuccess);
      
      if (merchantSuccess) {
        this.testMerchant = merchantLogin.data.data;
      }
      
    } catch (error) {
      this.logResult('商家登录', false, error.message);
    }

    // 测试管理员登录
    try {
      const adminLogin = await axios.post(`${this.baseURL}/auth/login`, {
        username: 'admin',
        password: '123456'
      });
      
      const adminSuccess = adminLogin.data.success && adminLogin.data.data;
      this.logResult('管理员登录', adminSuccess);
      
      if (adminSuccess) {
        this.testAdmin = adminLogin.data.data;
      }
      
    } catch (error) {
      this.logResult('管理员登录', false, error.message);
    }

    // 测试错误登录
    try {
      await axios.post(`${this.baseURL}/auth/login`, {
        username: 'wronguser',
        password: 'wrongpass'
      });
      this.logResult('错误登录处理', false, '应该返回错误但没有');
    } catch (error) {
      this.logResult('错误登录处理', error.response?.status === 400);
    }
  }

  // 3. 景点管理功能测试
  async testScenicSpotManagement() {
    console.log('\n🏞️ === 景点管理功能测试 ===');
    
    // 获取所有景点
    try {
      const allScenic = await axios.get(`${this.baseURL}/scenic`);
      const hasScenic = allScenic.data.data && allScenic.data.data.length > 0;
      this.logResult('获取景点列表', hasScenic);
      
      if (hasScenic) {
        const firstScenic = allScenic.data.data[0];
        
        // 获取景点详情
        try {
          const detail = await axios.get(`${this.baseURL}/scenic/${firstScenic.id}`);
          this.logResult('获取景点详情', detail.data.success);
        } catch (error) {
          this.logResult('获取景点详情', false, error.message);
        }
      }
      
    } catch (error) {
      this.logResult('获取景点列表', false, error.message);
    }

    // 测试景点搜索
    try {
      const searchResult = await axios.get(`${this.baseURL}/scenic/search/name?name=古窑`);
      this.logResult('景点名称搜索', searchResult.data.success);
    } catch (error) {
      this.logResult('景点名称搜索', false, error.message);
    }

    // 测试分类查询
    try {
      const categoryResult = await axios.get(`${this.baseURL}/scenic/category/博物馆`);
      this.logResult('景点分类查询', categoryResult.data.success);
    } catch (error) {
      this.logResult('景点分类查询', false, error.message);
    }

    // 测试价格范围查询
    try {
      const priceResult = await axios.get(`${this.baseURL}/scenic/search/price?minPrice=0&maxPrice=100`);
      this.logResult('价格范围查询', priceResult.data.success);
    } catch (error) {
      this.logResult('价格范围查询', false, error.message);
    }
  }

  // 4. 推荐算法功能测试
  async testRecommendationSystem() {
    console.log('\n🎯 === 推荐算法功能测试 ===');
    
    // 测试热门景点
    try {
      const popular = await axios.get(`${this.baseURL}/scenic/popular`);
      const hasPopular = popular.data.data && popular.data.data.length > 0;
      this.logResult('热门景点推荐', hasPopular);
      
      // 检查是否有重复
      if (hasPopular) {
        const names = popular.data.data.map(s => s.name);
        const uniqueNames = [...new Set(names)];
        this.logResult('热门景点去重', names.length === uniqueNames.length, 
          names.length !== uniqueNames.length ? '存在重复景点' : '');
      }
      
    } catch (error) {
      this.logResult('热门景点推荐', false, error.message);
    }

    // 测试个性化推荐
    try {
      const recommend = await axios.get(`${this.baseURL}/scenic/recommend`);
      const hasRecommend = recommend.data.data && recommend.data.data.length > 0;
      this.logResult('个性化推荐', hasRecommend);
      
      // 检查推荐景点去重
      if (hasRecommend) {
        const names = recommend.data.data.map(s => s.name);
        const uniqueNames = [...new Set(names)];
        this.logResult('推荐景点去重', names.length === uniqueNames.length);
      }
      
    } catch (error) {
      this.logResult('个性化推荐', false, error.message);
    }

    // 测试用户特定推荐
    if (this.testUser) {
      try {
        const userRecommend = await axios.get(`${this.baseURL}/scenic/recommend?userId=${this.testUser.id}`);
        this.logResult('用户特定推荐', userRecommend.data.success);
      } catch (error) {
        this.logResult('用户特定推荐', false, error.message);
      }
    }
  }

  // 5. 订单管理功能测试
  async testOrderManagement() {
    console.log('\n📋 === 订单管理功能测试 ===');
    
    // 获取订单列表
    try {
      const orders = await axios.get(`${this.baseURL}/order`);
      this.logResult('获取订单列表', orders.data.success);
      
      if (orders.data.data && orders.data.data.length > 0) {
        const firstOrder = orders.data.data[0];
        
        // 获取订单详情
        try {
          const orderDetail = await axios.get(`${this.baseURL}/order/${firstOrder.id}`);
          this.logResult('获取订单详情', orderDetail.data.success);
        } catch (error) {
          this.logResult('获取订单详情', false, error.message);
        }
      }
      
    } catch (error) {
      this.logResult('获取订单列表', false, error.message);
    }
  }

  // 6. 评价系统功能测试
  async testReviewSystem() {
    console.log('\n⭐ === 评价系统功能测试 ===');
    
    try {
      const reviews = await axios.get(`${this.baseURL}/review`);
      this.logResult('获取评价列表', reviews.data.success || reviews.status === 200);
    } catch (error) {
      // 评价接口可能有问题，记录但不算严重错误
      this.logResult('获取评价列表', false, `${error.message} (可能需要修复)`);
    }
  }

  // 7. 商家功能测试
  async testMerchantFunctions() {
    console.log('\n🏪 === 商家功能测试 ===');
    
    try {
      const merchants = await axios.get(`${this.baseURL}/merchant`);
      this.logResult('获取商家列表', merchants.data.success);
      
      if (merchants.data.data && merchants.data.data.length > 0) {
        const firstMerchant = merchants.data.data[0];
        
        try {
          const merchantDetail = await axios.get(`${this.baseURL}/merchant/${firstMerchant.id}`);
          this.logResult('获取商家详情', merchantDetail.data.success);
        } catch (error) {
          this.logResult('获取商家详情', false, error.message);
        }
      }
      
    } catch (error) {
      this.logResult('获取商家列表', false, error.message);
    }
  }

  // 8. 用户信息管理测试
  async testUserManagement() {
    console.log('\n👥 === 用户信息管理测试 ===');
    
    try {
      const users = await axios.get(`${this.baseURL}/user`);
      this.logResult('获取用户列表', users.data.success);
      
      if (this.testUser) {
        try {
          const userDetail = await axios.get(`${this.baseURL}/user/${this.testUser.id}`);
          this.logResult('获取用户详情', userDetail.data.success);
        } catch (error) {
          this.logResult('获取用户详情', false, error.message);
        }
      }
      
    } catch (error) {
      this.logResult('获取用户列表', false, error.message);
    }
  }

  // 9. API错误处理测试
  async testErrorHandling() {
    console.log('\n🚫 === API错误处理测试 ===');
    
    // 测试不存在的景点
    try {
      await axios.get(`${this.baseURL}/scenic/99999`);
      this.logResult('不存在景点处理', false, '应该返回404错误');
    } catch (error) {
      this.logResult('不存在景点处理', error.response?.status === 404);
    }

    // 测试不存在的用户
    try {
      await axios.get(`${this.baseURL}/user/99999`);
      this.logResult('不存在用户处理', false, '应该返回404错误');
    } catch (error) {
      this.logResult('不存在用户处理', error.response?.status === 404);
    }

    // 测试无效的搜索参数
    try {
      await axios.get(`${this.baseURL}/scenic/search/price?minPrice=abc&maxPrice=def`);
      this.logResult('无效参数处理', false, '应该返回400错误');
    } catch (error) {
      this.logResult('无效参数处理', error.response?.status === 400);
    }
  }

  // 10. 数据一致性测试
  async testDataConsistency() {
    console.log('\n🔍 === 数据一致性测试 ===');
    
    try {
      // 检查景点数据完整性
      const scenic = await axios.get(`${this.baseURL}/scenic`);
      if (scenic.data.data) {
        const hasRequiredFields = scenic.data.data.every(spot => 
          spot.id && spot.name && spot.price !== undefined
        );
        this.logResult('景点数据完整性', hasRequiredFields);
      }

      // 检查用户数据完整性
      const users = await axios.get(`${this.baseURL}/user`);
      if (users.data.data) {
        const hasRequiredFields = users.data.data.every(user => 
          user.id && user.username && user.role
        );
        this.logResult('用户数据完整性', hasRequiredFields);
      }

    } catch (error) {
      this.logResult('数据一致性检查', false, error.message);
    }
  }

  // 执行所有测试
  async runAllTests() {
    console.log('🧪 开始执行景德镇旅游智能推荐系统全面功能测试\n');
    console.log('=' .repeat(60));
    
    await this.testSystemConnection();
    await this.testUserAuthentication();
    await this.testScenicSpotManagement();
    await this.testRecommendationSystem();
    await this.testOrderManagement();
    await this.testReviewSystem();
    await this.testMerchantFunctions();
    await this.testUserManagement();
    await this.testErrorHandling();
    await this.testDataConsistency();
    
    this.generateTestReport();
  }

  // 生成测试报告
  generateTestReport() {
    console.log('\n' + '=' .repeat(60));
    console.log('📊 === 测试报告 ===');
    console.log(`✅ 通过测试: ${this.testResults.passed}`);
    console.log(`❌ 失败测试: ${this.testResults.failed}`);
    console.log(`📈 成功率: ${((this.testResults.passed / (this.testResults.passed + this.testResults.failed)) * 100).toFixed(1)}%`);
    
    if (this.testResults.errors.length > 0) {
      console.log('\n🔍 === 需要修复的问题 ===');
      this.testResults.errors.forEach((error, index) => {
        console.log(`${index + 1}. ${error.test}: ${error.error}`);
      });
    }
    
    console.log('\n💡 === 测试建议 ===');
    if (this.testResults.failed === 0) {
      console.log('🎉 恭喜！所有功能测试都通过了！');
    } else if (this.testResults.failed <= 3) {
      console.log('⚠️ 系统基本功能正常，但有少量问题需要修复');
    } else {
      console.log('🚨 系统存在较多问题，建议优先修复核心功能');
    }
    
    console.log('\n📋 === 后续测试建议 ===');
    console.log('1. 性能测试：测试高并发访问情况');
    console.log('2. 安全测试：SQL注入、XSS攻击防护');
    console.log('3. 兼容性测试：不同浏览器和设备');
    console.log('4. 用户体验测试：界面响应速度和易用性');
    console.log('5. 数据备份恢复测试');
    
    console.log('\n' + '=' .repeat(60));
  }
}

// 执行测试
const tester = new TourismSystemTester();
tester.runAllTests().catch(console.error);