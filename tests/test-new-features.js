// 测试新功能的脚本
const axios = require('axios');

const BASE_URL = 'http://localhost:8889';

// 测试数据
const testData = {
  // 商家资质申请测试数据
  merchantApplication: {
    userId: 3,
    businessLicense: '/uploads/test_business_license.jpg',
    identityCard: '/uploads/test_id_front.jpg,/uploads/test_id_back.jpg',
    shopPhotos: '["/uploads/test_shop1.jpg","/uploads/test_shop2.jpg"]',
    description: '测试商家资质申请，专业陶瓷制作体验服务'
  },
  
  // 预约测试数据
  reservation: {
    userId: 2,
    merchantId: 1,
    scenicId: 1,
    reservationDate: '2024-02-01',
    reservationTime: '10:00:00',
    participants: 2,
    remark: '测试预约'
  }
};

async function testMerchantApplicationAPI() {
  console.log('🧪 测试商家资质申请API...');
  
  try {
    // 1. 提交申请
    console.log('1. 提交商家资质申请');
    const submitResponse = await axios.post(`${BASE_URL}/api/merchant-application`, testData.merchantApplication);
    console.log('✅ 提交申请成功:', submitResponse.data);
    
    const applicationId = submitResponse.data.data?.id;
    
    // 2. 获取申请状态
    console.log('2. 获取申请状态');
    const statusResponse = await axios.get(`${BASE_URL}/api/merchant-application/user/${testData.merchantApplication.userId}`);
    console.log('✅ 获取状态成功:', statusResponse.data);
    
    // 3. 审核申请（管理员操作）
    if (applicationId) {
      console.log('3. 审核申请');
      const auditResponse = await axios.put(`${BASE_URL}/api/merchant-application/${applicationId}/audit`, {
        status: 'APPROVED',
        opinion: '申请材料齐全，审核通过'
      });
      console.log('✅ 审核成功:', auditResponse.data);
    }
    
  } catch (error) {
    console.error('❌ 商家资质申请API测试失败:', error.response?.data || error.message);
  }
}

async function testReservationAPI() {
  console.log('🧪 测试预约管理API...');
  
  try {
    // 1. 创建预约
    console.log('1. 创建预约');
    const createResponse = await axios.post(`${BASE_URL}/api/reservation`, testData.reservation);
    console.log('✅ 创建预约成功:', createResponse.data);
    
    const reservationId = createResponse.data.data?.id;
    
    // 2. 获取商家预约列表
    console.log('2. 获取商家预约列表');
    const listResponse = await axios.get(`${BASE_URL}/api/reservation/merchant/${testData.reservation.merchantId}`);
    console.log('✅ 获取预约列表成功:', listResponse.data);
    
    // 3. 确认预约
    if (reservationId) {
      console.log('3. 确认预约');
      const confirmResponse = await axios.put(`${BASE_URL}/api/reservation/${reservationId}/confirm`);
      console.log('✅ 确认预约成功:', confirmResponse.data);
    }
    
    // 4. 获取预约统计
    console.log('4. 获取预约统计');
    const statsResponse = await axios.get(`${BASE_URL}/api/reservation/merchant/${testData.reservation.merchantId}/stats`);
    console.log('✅ 获取统计成功:', statsResponse.data);
    
  } catch (error) {
    console.error('❌ 预约管理API测试失败:', error.response?.data || error.message);
  }
}

async function testFileUploadAPI() {
  console.log('🧪 测试文件上传API...');
  
  try {
    // 模拟文件上传（这里只是测试API是否可访问）
    console.log('1. 测试文件上传接口');
    const uploadResponse = await axios.get(`${BASE_URL}/api/upload`);
    console.log('✅ 文件上传接口可访问');
    
  } catch (error) {
    if (error.response?.status === 405) {
      console.log('✅ 文件上传接口存在（方法不允许是正常的）');
    } else {
      console.error('❌ 文件上传API测试失败:', error.response?.data || error.message);
    }
  }
}

async function runAllTests() {
  console.log('🚀 开始测试新功能...\n');
  
  await testMerchantApplicationAPI();
  console.log('');
  
  await testReservationAPI();
  console.log('');
  
  await testFileUploadAPI();
  console.log('');
  
  console.log('✅ 所有测试完成！');
}

// 运行测试
runAllTests().catch(console.error);