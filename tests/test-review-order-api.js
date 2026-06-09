/**
 * 评价系统和订单系统API测试
 * 测试新实现的API端点：
 * - GET /api/reviews
 * - POST /api/reviews
 * - PUT /api/reviews/{id}
 * - GET /api/orders
 * - POST /api/orders
 * - PUT /api/orders/{id}
 */

const axios = require('axios');

const BASE_URL = 'http://localhost:8080';

// 测试数据
const testUser = {
    id: 1,
    username: 'testuser',
    password: '123456',
    email: 'test@example.com',
    role: 'TOURIST'
};

const testMerchant = {
    id: 1,
    name: '测试商家'
};

const testScenicSpot = {
    id: 1,
    name: '测试景点'
};

const testReview = {
    userId: 1,
    scenicSpotId: 1,
    content: '这是一个测试评价，景点非常好！',
    rating: 5
};

const testOrder = {
    userId: 1,
    merchantId: 1,
    scenicSpotId: 1
};

// HTTP请求配置
const axiosConfig = {
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json'
    }
};

async function testReviewAPI() {
    console.log('\n=== 评价系统API测试 ===');
    
    try {
        // 1. 测试 POST /api/reviews - 创建评价
        console.log('\n1. 测试创建评价 (POST /api/reviews)');
        const createReviewResponse = await axios.post(
            `${BASE_URL}/api/reviews`,
            testReview,
            axiosConfig
        );
        console.log('✅ 创建评价成功:', createReviewResponse.data);
        const reviewId = createReviewResponse.data.data.id;
        
        // 2. 测试 GET /api/reviews - 获取所有评价
        console.log('\n2. 测试获取所有评价 (GET /api/reviews)');
        const getAllReviewsResponse = await axios.get(
            `${BASE_URL}/api/reviews`,
            axiosConfig
        );
        console.log('✅ 获取所有评价成功:', {
            success: getAllReviewsResponse.data.success,
            count: getAllReviewsResponse.data.data.length,
            firstReview: getAllReviewsResponse.data.data[0] || null
        });
        
        // 3. 测试 PUT /api/reviews/{id} - 更新评价
        console.log('\n3. 测试更新评价 (PUT /api/reviews/{id})');
        const updatedReview = {
            content: '更新后的评价内容，体验更好了！',
            rating: 4
        };
        const updateReviewResponse = await axios.put(
            `${BASE_URL}/api/reviews/${reviewId}`,
            updatedReview,
            axiosConfig
        );
        console.log('✅ 更新评价成功:', updateReviewResponse.data);
        
        // 4. 测试获取特定景点的评价
        console.log('\n4. 测试获取特定景点评价 (GET /api/reviews/scenic/{scenicSpotId})');
        const scenicReviewsResponse = await axios.get(
            `${BASE_URL}/api/reviews/scenic/${testScenicSpot.id}`,
            axiosConfig
        );
        console.log('✅ 获取景点评价成功:', {
            success: scenicReviewsResponse.data.success,
            count: scenicReviewsResponse.data.data.length
        });
        
        // 5. 测试获取平均评分
        console.log('\n5. 测试获取平均评分 (GET /api/reviews/scenic/{scenicSpotId}/average-rating)');
        const avgRatingResponse = await axios.get(
            `${BASE_URL}/api/reviews/scenic/${testScenicSpot.id}/average-rating`,
            axiosConfig
        );
        console.log('✅ 获取平均评分成功:', avgRatingResponse.data);
        
        return reviewId;
        
    } catch (error) {
        console.error('❌ 评价API测试失败:', {
            message: error.message,
            status: error.response?.status,
            data: error.response?.data
        });
        throw error;
    }
}

async function testOrderAPI() {
    console.log('\n=== 订单系统API测试 ===');
    
    try {
        // 1. 测试 POST /api/orders - 创建订单
        console.log('\n1. 测试创建订单 (POST /api/orders)');
        const createOrderResponse = await axios.post(
            `${BASE_URL}/api/orders`,
            testOrder,
            axiosConfig
        );
        console.log('✅ 创建订单成功:', createOrderResponse.data);
        const orderId = createOrderResponse.data.data.id;
        
        // 2. 测试 GET /api/orders - 获取所有订单
        console.log('\n2. 测试获取所有订单 (GET /api/orders)');
        const getAllOrdersResponse = await axios.get(
            `${BASE_URL}/api/orders`,
            axiosConfig
        );
        console.log('✅ 获取所有订单成功:', {
            success: getAllOrdersResponse.data.success,
            count: getAllOrdersResponse.data.data.length,
            firstOrder: getAllOrdersResponse.data.data[0] || null
        });
        
        // 3. 测试 PUT /api/orders/{id} - 更新订单
        console.log('\n3. 测试更新订单 (PUT /api/orders/{id})');
        const updatedOrder = {
            status: 'CONFIRMED'
        };
        const updateOrderResponse = await axios.put(
            `${BASE_URL}/api/orders/${orderId}`,
            updatedOrder,
            axiosConfig
        );
        console.log('✅ 更新订单成功:', updateOrderResponse.data);
        
        // 4. 测试获取特定用户的订单
        console.log('\n4. 测试获取用户订单 (GET /api/orders/user/{userId})');
        const userOrdersResponse = await axios.get(
            `${BASE_URL}/api/orders/user/${testUser.id}`,
            axiosConfig
        );
        console.log('✅ 获取用户订单成功:', {
            success: userOrdersResponse.data.success,
            count: userOrdersResponse.data.data.length
        });
        
        // 5. 测试获取特定状态的订单
        console.log('\n5. 测试获取特定状态订单 (GET /api/orders/status/CONFIRMED)');
        const statusOrdersResponse = await axios.get(
            `${BASE_URL}/api/orders/status/CONFIRMED`,
            axiosConfig
        );
        console.log('✅ 获取状态订单成功:', {
            success: statusOrdersResponse.data.success,
            count: statusOrdersResponse.data.data.length
        });
        
        return orderId;
        
    } catch (error) {
        console.error('❌ 订单API测试失败:', {
            message: error.message,
            status: error.response?.status,
            data: error.response?.data
        });
        throw error;
    }
}

async function testAPIEndpoints() {
    console.log('🚀 开始API端点测试...');
    
    try {
        // 测试评价API
        const reviewId = await testReviewAPI();
        
        // 测试订单API
        const orderId = await testOrderAPI();
        
        console.log('\n=== API测试总结 ===');
        console.log('✅ 评价系统API测试完成');
        console.log('✅ 订单系统API测试完成');
        console.log(`📝 创建的测试评价ID: ${reviewId}`);
        console.log(`📝 创建的测试订单ID: ${orderId}`);
        
        console.log('\n🎉 所有API测试成功完成！');
        
    } catch (error) {
        console.error('\n💥 API测试过程中出现错误:', error.message);
        process.exit(1);
    }
}

// 如果直接运行此脚本
if (require.main === module) {
    testAPIEndpoints();
}

module.exports = {
    testReviewAPI,
    testOrderAPI,
    testAPIEndpoints
};