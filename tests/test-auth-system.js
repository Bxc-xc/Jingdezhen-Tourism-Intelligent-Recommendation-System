const axios = require('axios');
const bcrypt = require('bcrypt');

const API_BASE = 'http://localhost:8889/api';

// 测试用户数据
const testUsers = [
  {
    username: 'admin',
    password: '123456',
    role: 'ADMIN',
    phone: '13800138000'
  },
  {
    username: 'merchant1',
    password: '123456',
    role: 'MERCHANT',
    phone: '13800138001'
  },
  {
    username: 'tourist1',
    password: '123456',
    role: 'TOURIST',
    phone: '13800138002'
  }
];

// 测试注册功能
async function testRegister() {
  console.log('\n=== 测试用户注册 ===');
  
  for (const userData of testUsers) {
    try {
      const response = await axios.post(`${API_BASE}/auth/register`, userData);
      console.log(`✅ 注册成功: ${userData.username} (${userData.role})`);
      console.log('响应数据:', response.data);
    } catch (error) {
      if (error.response?.data?.message?.includes('已存在')) {
        console.log(`ℹ️  用户已存在: ${userData.username}`);
      } else {
        console.log(`❌ 注册失败: ${userData.username}`);
        console.log('错误:', error.response?.data?.message || error.message);
      }
    }
  }
}

// 测试登录功能
async function testLogin() {
  console.log('\n=== 测试用户登录 ===');
  
  for (const userData of testUsers) {
    try {
      const loginData = {
        username: userData.username,
        password: userData.password
      };
      
      console.log(`\n尝试登录: ${userData.username}`);
      const response = await axios.post(`${API_BASE}/auth/login`, loginData);
      
      console.log(`✅ 登录成功: ${userData.username}`);
      console.log('用户信息:', response.data.data);
      console.log('Token长度:', response.data.token?.length || 0);
      
      // 测试Token验证
      if (response.data.token) {
        await testTokenVerification(response.data.token, userData.username);
      }
      
    } catch (error) {
      console.log(`❌ 登录失败: ${userData.username}`);
      console.log('错误:', error.response?.data?.message || error.message);
    }
  }
}

// 测试Token验证
async function testTokenVerification(token, username) {
  try {
    const response = await axios.post(`${API_BASE}/auth/verify`, {}, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    console.log(`✅ Token验证成功: ${username}`);
    console.log('Token信息:', response.data.data);
  } catch (error) {
    console.log(`❌ Token验证失败: ${username}`);
    console.log('错误:', error.response?.data?.message || error.message);
  }
}

// 测试错误场景
async function testErrorScenarios() {
  console.log('\n=== 测试错误场景 ===');
  
  // 测试错误的用户名密码
  try {
    await axios.post(`${API_BASE}/auth/login`, {
      username: 'nonexistent',
      password: 'wrongpassword'
    });
  } catch (error) {
    console.log('✅ 错误用户名密码测试通过');
    console.log('错误信息:', error.response?.data?.message);
  }
  
  // 测试空用户名密码
  try {
    await axios.post(`${API_BASE}/auth/login`, {
      username: '',
      password: ''
    });
  } catch (error) {
    console.log('✅ 空用户名密码测试通过');
    console.log('错误信息:', error.response?.data?.message);
  }
  
  // 测试无效Token
  try {
    await axios.post(`${API_BASE}/auth/verify`, {}, {
      headers: {
        'Authorization': 'Bearer invalid-token'
      }
    });
  } catch (error) {
    console.log('✅ 无效Token测试通过');
    console.log('错误信息:', error.response?.data?.message);
  }
}

// 主测试函数
async function runTests() {
  console.log('🚀 开始测试用户认证系统');
  console.log('后端地址:', API_BASE);
  
  try {
    await testRegister();
    await testLogin();
    await testErrorScenarios();
    
    console.log('\n🎉 所有测试完成');
  } catch (error) {
    console.error('❌ 测试过程中发生错误:', error.message);
  }
}

// 运行测试
if (require.main === module) {
  runTests();
}

module.exports = {
  testRegister,
  testLogin,
  testTokenVerification,
  testErrorScenarios
};