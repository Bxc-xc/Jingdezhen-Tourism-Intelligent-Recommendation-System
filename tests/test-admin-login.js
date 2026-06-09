// 专门测试admin登录
const axios = require('axios');

async function testAdminLogin() {
  const baseURL = 'http://localhost:8889/api';
  
  console.log('🔍 专门测试admin登录...\n');
  
  // 测试admin登录
  console.log('测试admin登录:');
  try {
    const response = await axios.post(`${baseURL}/auth/login`, {
      username: 'admin',
      password: 'admin123'
    }, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
    console.log('✅ admin登录成功');
    console.log('响应数据:', JSON.stringify(response.data, null, 2));
  } catch (error) {
    console.log('❌ admin登录失败');
    console.log('错误状态:', error.response?.status);
    console.log('错误信息:', error.response?.data);
    console.log('完整错误:', error.message);
  }
  
  // 测试其他用户对比
  console.log('\n测试tourist1登录对比:');
  try {
    const response = await axios.post(`${baseURL}/auth/login`, {
      username: 'tourist1',
      password: '123456'
    }, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
    console.log('✅ tourist1登录成功');
  } catch (error) {
    console.log('❌ tourist1登录失败');
    console.log('错误状态:', error.response?.status);
    console.log('错误信息:', error.response?.data);
  }
}

testAdminLogin();