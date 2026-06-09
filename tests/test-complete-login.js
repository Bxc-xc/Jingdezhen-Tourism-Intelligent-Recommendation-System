// 完整测试登录流程
const axios = require('axios');

async function testCompleteLogin() {
  const baseURL = 'http://localhost:8889/api';
  
  console.log('🔍 完整测试登录流程...\n');
  
  // 测试1: 正确格式（只有username和password）
  console.log('1. 测试正确格式（只有username和password）:');
  try {
    const response = await axios.post(`${baseURL}/auth/login`, {
      username: 'tourist1',
      password: '123456'
    }, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
    console.log('✅ 登录成功');
    console.log('响应数据:', JSON.stringify(response.data, null, 2));
  } catch (error) {
    console.log('❌ 登录失败');
    console.log('错误状态:', error.response?.status);
    console.log('错误信息:', error.response?.data);
  }
  
  // 测试2: 包含额外字段（remember）
  console.log('\n2. 测试包含额外字段（remember）:');
  try {
    const response = await axios.post(`${baseURL}/auth/login`, {
      username: 'tourist1',
      password: '123456',
      remember: false
    }, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
    console.log('✅ 登录成功');
    console.log('响应数据:', JSON.stringify(response.data, null, 2));
  } catch (error) {
    console.log('❌ 登录失败');
    console.log('错误状态:', error.response?.status);
    console.log('错误信息:', error.response?.data);
  }
  
  // 测试3: 测试所有角色
  console.log('\n3. 测试所有角色登录:');
  const accounts = [
    { username: 'admin', password: '123456', role: 'ADMIN' },
    { username: 'tourist1', password: '123456', role: 'TOURIST' },
    { username: 'merchant1', password: '123456', role: 'MERCHANT' }
  ];
  
  for (const account of accounts) {
    try {
      const response = await axios.post(`${baseURL}/auth/login`, {
        username: account.username,
        password: account.password
      });
      console.log(`✅ ${account.role} 登录成功:`, response.data.data.username);
    } catch (error) {
      console.log(`❌ ${account.role} 登录失败:`, error.response?.data?.message);
    }
  }
}

testCompleteLogin();