// 详细测试登录接口
const axios = require('axios');

async function testLogin() {
  const baseURL = 'http://localhost:8889/api';
  
  console.log('🔍 测试登录接口...\n');
  
  // 测试1: 正确的登录请求
  console.log('1. 测试正确的登录请求:');
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
    console.log('响应状态:', response.status);
    console.log('响应数据:', JSON.stringify(response.data, null, 2));
  } catch (error) {
    console.log('❌ 登录失败');
    console.log('错误状态:', error.response?.status);
    console.log('错误数据:', error.response?.data);
  }
  
  // 测试2: 错误的用户名
  console.log('\n2. 测试错误的用户名:');
  try {
    const response = await axios.post(`${baseURL}/auth/login`, {
      username: 'wronguser',
      password: '123456'
    }, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
    console.log('✅ 登录成功');
  } catch (error) {
    console.log('❌ 登录失败 (预期)');
    console.log('错误状态:', error.response?.status);
    console.log('错误数据:', error.response?.data);
  }
  
  // 测试3: 错误的密码
  console.log('\n3. 测试错误的密码:');
  try {
    const response = await axios.post(`${baseURL}/auth/login`, {
      username: 'tourist1',
      password: 'wrongpassword'
    }, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
    console.log('✅ 登录成功');
  } catch (error) {
    console.log('❌ 登录失败 (预期)');
    console.log('错误状态:', error.response?.status);
    console.log('错误数据:', error.response?.data);
  }
  
  // 测试4: 缺少字段
  console.log('\n4. 测试缺少字段:');
  try {
    const response = await axios.post(`${baseURL}/auth/login`, {
      username: 'tourist1'
      // 缺少password字段
    }, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
    console.log('✅ 登录成功');
  } catch (error) {
    console.log('❌ 登录失败 (预期)');
    console.log('错误状态:', error.response?.status);
    console.log('错误数据:', error.response?.data);
  }
  
  // 测试5: 空字段
  console.log('\n5. 测试空字段:');
  try {
    const response = await axios.post(`${baseURL}/auth/login`, {
      username: '',
      password: ''
    }, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
    console.log('✅ 登录成功');
  } catch (error) {
    console.log('❌ 登录失败 (预期)');
    console.log('错误状态:', error.response?.status);
    console.log('错误数据:', error.response?.data);
  }
}

testLogin();