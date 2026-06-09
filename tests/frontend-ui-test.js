// 前端界面功能测试脚本
const axios = require('axios');

class FrontendUITester {
  constructor() {
    this.frontendURL = 'http://localhost:5173';
    this.backendURL = 'http://localhost:8889/api';
    this.testResults = {
      passed: 0,
      failed: 0,
      warnings: 0,
      issues: []
    };
  }

  logResult(testName, status, message = '') {
    if (status === 'pass') {
      this.testResults.passed++;
      console.log(`✅ ${testName}: 通过`);
    } else if (status === 'fail') {
      this.testResults.failed++;
      console.log(`❌ ${testName}: 失败 - ${message}`);
      this.testResults.issues.push({ test: testName, type: 'error', message });
    } else if (status === 'warn') {
      this.testResults.warnings++;
      console.log(`⚠️ ${testName}: 警告 - ${message}`);
      this.testResults.issues.push({ test: testName, type: 'warning', message });
    }
  }

  // 测试前端服务可访问性
  async testFrontendAccessibility() {
    console.log('\n🌐 === 前端服务可访问性测试 ===');
    
    try {
      const response = await axios.get(this.frontendURL, { timeout: 5000 });
      this.logResult('前端服务访问', response.status === 200 ? 'pass' : 'fail');
    } catch (error) {
      this.logResult('前端服务访问', 'fail', error.message);
    }
  }

  // 测试API代理配置
  async testAPIProxy() {
    console.log('\n🔗 === API代理配置测试 ===');
    
    // 检查Vite配置
    try {
      const fs = require('fs');
      const viteConfigPath = 'frontend/vite.config.js';
      
      if (fs.existsSync(viteConfigPath)) {
        const viteConfig = fs.readFileSync(viteConfigPath, 'utf8');
        const hasProxy = viteConfig.includes('proxy') && viteConfig.includes('/api');
        this.logResult('Vite代理配置', hasProxy ? 'pass' : 'warn', 
          hasProxy ? '' : '建议配置API代理以避免跨域问题');
      } else {
        this.logResult('Vite配置文件', 'warn', '未找到vite.config.js文件');
      }
    } catch (error) {
      this.logResult('Vite配置检查', 'fail', error.message);
    }
  }

  // 测试前端路由配置
  async testRouterConfiguration() {
    console.log('\n🛣️ === 前端路由配置测试 ===');
    
    try {
      const fs = require('fs');
      const routerPath = 'frontend/src/router/index.js';
      
      if (fs.existsSync(routerPath)) {
        const routerConfig = fs.readFileSync(routerPath, 'utf8');
        
        // 检查主要路由
        const routes = [
          { name: '首页路由', pattern: /path.*['"]\// },
          { name: '登录路由', pattern: /path.*['"]\/login/ },
          { name: '推荐路由', pattern: /path.*['"]\/recommend/ },
          { name: '景点详情路由', pattern: /path.*['"]\/scenic/ },
          { name: '个人中心路由', pattern: /path.*['"]\/profile/ }
        ];
        
        routes.forEach(route => {
          const hasRoute = route.pattern.test(routerConfig);
          this.logResult(route.name, hasRoute ? 'pass' : 'warn', 
            hasRoute ? '' : '建议添加此路由');
        });
        
        // 检查路由守卫
        const hasGuard = routerConfig.includes('beforeEach') || routerConfig.includes('meta');
        this.logResult('路由守卫配置', hasGuard ? 'pass' : 'warn', 
          hasGuard ? '' : '建议添加路由守卫进行权限控制');
          
      } else {
        this.logResult('路由配置文件', 'fail', '未找到router/index.js文件');
      }
    } catch (error) {
      this.logResult('路由配置检查', 'fail', error.message);
    }
  }

  // 测试状态管理配置
  async testStateManagement() {
    console.log('\n📦 === 状态管理配置测试 ===');
    
    try {
      const fs = require('fs');
      const storesPath = 'frontend/src/stores';
      
      if (fs.existsSync(storesPath)) {
        const storeFiles = fs.readdirSync(storesPath);
        
        // 检查主要store文件
        const requiredStores = ['user.js', 'scenic.js'];
        requiredStores.forEach(store => {
          const hasStore = storeFiles.includes(store);
          this.logResult(`${store} Store`, hasStore ? 'pass' : 'warn', 
            hasStore ? '' : '建议创建此Store');
        });
        
        // 检查用户Store的功能
        if (storeFiles.includes('user.js')) {
          const userStore = fs.readFileSync(`${storesPath}/user.js`, 'utf8');
          const hasLogin = userStore.includes('login');
          const hasLogout = userStore.includes('logout');
          const hasToken = userStore.includes('token');
          
          this.logResult('用户Store功能完整性', 
            (hasLogin && hasLogout && hasToken) ? 'pass' : 'warn',
            (hasLogin && hasLogout && hasToken) ? '' : '缺少部分用户管理功能');
        }
        
      } else {
        this.logResult('状态管理目录', 'fail', '未找到stores目录');
      }
    } catch (error) {
      this.logResult('状态管理检查', 'fail', error.message);
    }
  }

  // 测试组件结构
  async testComponentStructure() {
    console.log('\n🧩 === 组件结构测试 ===');
    
    try {
      const fs = require('fs');
      const componentsPath = 'frontend/src/components';
      const viewsPath = 'frontend/src/views';
      
      // 检查组件目录
      if (fs.existsSync(componentsPath)) {
        const components = fs.readdirSync(componentsPath, { recursive: true });
        const hasLayout = components.some(c => c.includes('Layout') || c.includes('Header') || c.includes('Footer'));
        const hasCards = components.some(c => c.includes('Card'));
        
        this.logResult('布局组件', hasLayout ? 'pass' : 'warn', 
          hasLayout ? '' : '建议创建Header/Footer等布局组件');
        this.logResult('卡片组件', hasCards ? 'pass' : 'warn', 
          hasCards ? '' : '建议创建景点卡片等展示组件');
      } else {
        this.logResult('组件目录', 'warn', '未找到components目录');
      }
      
      // 检查页面目录
      if (fs.existsSync(viewsPath)) {
        const views = fs.readdirSync(viewsPath, { recursive: true });
        
        const requiredViews = [
          { name: '游客页面', pattern: /tourist/ },
          { name: '商家页面', pattern: /merchant/ },
          { name: '管理员页面', pattern: /admin/ },
          { name: '认证页面', pattern: /auth|login/ }
        ];
        
        requiredViews.forEach(view => {
          const hasView = views.some(v => view.pattern.test(v));
          this.logResult(view.name, hasView ? 'pass' : 'warn', 
            hasView ? '' : '建议创建此类页面');
        });
      } else {
        this.logResult('页面目录', 'fail', '未找到views目录');
      }
      
    } catch (error) {
      this.logResult('组件结构检查', 'fail', error.message);
    }
  }

  // 测试样式和UI框架
  async testStylingFramework() {
    console.log('\n🎨 === 样式和UI框架测试 ===');
    
    try {
      const fs = require('fs');
      const packagePath = 'frontend/package.json';
      
      if (fs.existsSync(packagePath)) {
        const packageJson = JSON.parse(fs.readFileSync(packagePath, 'utf8'));
        const dependencies = { ...packageJson.dependencies, ...packageJson.devDependencies };
        
        // 检查UI框架
        const hasElementPlus = dependencies['element-plus'];
        const hasVueRouter = dependencies['vue-router'];
        const hasPinia = dependencies['pinia'];
        const hasAxios = dependencies['axios'];
        
        this.logResult('Element Plus UI框架', hasElementPlus ? 'pass' : 'warn', 
          hasElementPlus ? '' : '建议使用UI框架提升开发效率');
        this.logResult('Vue Router路由', hasVueRouter ? 'pass' : 'fail', 
          hasVueRouter ? '' : 'Vue Router是必需的');
        this.logResult('Pinia状态管理', hasPinia ? 'pass' : 'warn', 
          hasPinia ? '' : '建议使用Pinia进行状态管理');
        this.logResult('Axios HTTP客户端', hasAxios ? 'pass' : 'fail', 
          hasAxios ? '' : 'Axios是API调用必需的');
          
        // 检查图表库
        const hasECharts = dependencies['echarts'] || dependencies['vue-echarts'];
        this.logResult('图表库', hasECharts ? 'pass' : 'warn', 
          hasECharts ? '' : '建议添加图表库用于数据可视化');
          
      } else {
        this.logResult('前端包配置', 'fail', '未找到frontend/package.json文件');
      }
    } catch (error) {
      this.logResult('依赖检查', 'fail', error.message);
    }
  }

  // 测试API调用配置
  async testAPIConfiguration() {
    console.log('\n🔌 === API调用配置测试 ===');
    
    try {
      const fs = require('fs');
      const apiPath = 'frontend/src/api';
      const utilsPath = 'frontend/src/utils';
      
      // 检查API目录
      if (fs.existsSync(apiPath)) {
        const apiFiles = fs.readdirSync(apiPath);
        
        const requiredAPIs = ['auth.js', 'scenic.js', 'order.js'];
        requiredAPIs.forEach(api => {
          const hasAPI = apiFiles.includes(api);
          this.logResult(`${api} API模块`, hasAPI ? 'pass' : 'warn', 
            hasAPI ? '' : '建议创建此API模块');
        });
      } else {
        this.logResult('API目录', 'warn', '建议创建api目录组织API调用');
      }
      
      // 检查请求工具
      if (fs.existsSync(utilsPath)) {
        const utilFiles = fs.readdirSync(utilsPath);
        const hasRequest = utilFiles.includes('request.js');
        
        this.logResult('请求工具配置', hasRequest ? 'pass' : 'warn', 
          hasRequest ? '' : '建议创建统一的请求工具');
          
        if (hasRequest) {
          const requestConfig = fs.readFileSync(`${utilsPath}/request.js`, 'utf8');
          const hasInterceptor = requestConfig.includes('interceptors');
          const hasBaseURL = requestConfig.includes('baseURL');
          
          this.logResult('请求拦截器', hasInterceptor ? 'pass' : 'warn', 
            hasInterceptor ? '' : '建议添加请求/响应拦截器');
          this.logResult('基础URL配置', hasBaseURL ? 'pass' : 'warn', 
            hasBaseURL ? '' : '建议配置API基础URL');
        }
      }
      
    } catch (error) {
      this.logResult('API配置检查', 'fail', error.message);
    }
  }

  // 测试响应式设计
  async testResponsiveDesign() {
    console.log('\n📱 === 响应式设计测试 ===');
    
    try {
      const fs = require('fs');
      const viewsPath = 'frontend/src/views';
      
      if (fs.existsSync(viewsPath)) {
        // 检查主要页面文件
        const homeView = 'frontend/src/views/tourist/Home.vue';
        if (fs.existsSync(homeView)) {
          const homeContent = fs.readFileSync(homeView, 'utf8');
          
          // 检查响应式相关代码
          const hasElRow = homeContent.includes('el-row');
          const hasElCol = homeContent.includes('el-col');
          const hasResponsiveProps = homeContent.includes(':xs=') || homeContent.includes(':sm=') || homeContent.includes(':md=');
          const hasMediaQuery = homeContent.includes('@media');
          
          this.logResult('栅格布局系统', hasElRow && hasElCol ? 'pass' : 'warn', 
            hasElRow && hasElCol ? '' : '建议使用栅格系统实现响应式布局');
          this.logResult('响应式断点', hasResponsiveProps ? 'pass' : 'warn', 
            hasResponsiveProps ? '' : '建议配置不同屏幕尺寸的断点');
          this.logResult('CSS媒体查询', hasMediaQuery ? 'pass' : 'warn', 
            hasMediaQuery ? '' : '建议添加CSS媒体查询优化移动端显示');
        }
      }
      
    } catch (error) {
      this.logResult('响应式设计检查', 'fail', error.message);
    }
  }

  // 执行所有前端测试
  async runAllTests() {
    console.log('🎨 开始执行前端界面功能测试\n');
    console.log('=' .repeat(60));
    
    await this.testFrontendAccessibility();
    await this.testAPIProxy();
    await this.testRouterConfiguration();
    await this.testStateManagement();
    await this.testComponentStructure();
    await this.testStylingFramework();
    await this.testAPIConfiguration();
    await this.testResponsiveDesign();
    
    this.generateReport();
  }

  // 生成测试报告
  generateReport() {
    console.log('\n' + '=' .repeat(60));
    console.log('📊 === 前端测试报告 ===');
    console.log(`✅ 通过: ${this.testResults.passed}`);
    console.log(`❌ 失败: ${this.testResults.failed}`);
    console.log(`⚠️ 警告: ${this.testResults.warnings}`);
    
    const total = this.testResults.passed + this.testResults.failed + this.testResults.warnings;
    const score = ((this.testResults.passed + this.testResults.warnings * 0.5) / total * 100).toFixed(1);
    console.log(`📈 综合评分: ${score}%`);
    
    if (this.testResults.issues.length > 0) {
      console.log('\n🔍 === 发现的问题 ===');
      this.testResults.issues.forEach((issue, index) => {
        const icon = issue.type === 'error' ? '❌' : '⚠️';
        console.log(`${index + 1}. ${icon} ${issue.test}: ${issue.message}`);
      });
    }
    
    console.log('\n💡 === 前端优化建议 ===');
    console.log('1. 确保所有页面都支持响应式设计');
    console.log('2. 添加加载状态和错误处理提示');
    console.log('3. 优化图片和资源加载性能');
    console.log('4. 添加用户操作的反馈提示');
    console.log('5. 考虑添加PWA支持提升用户体验');
    
    console.log('\n' + '=' .repeat(60));
  }
}

// 执行前端测试
const frontendTester = new FrontendUITester();
frontendTester.runAllTests().catch(console.error);