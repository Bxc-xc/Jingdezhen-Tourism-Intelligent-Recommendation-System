/**
 * 实时同步功能测试脚本
 * 测试WebSocket连接和数据同步功能
 */

const WebSocket = require('ws');
const axios = require('axios');

class RealtimeSyncTester {
    constructor() {
        this.baseUrl = 'http://localhost:8889';
        this.wsUrl = 'ws://localhost:8889/ws/data-sync';
        this.ws = null;
        this.receivedMessages = [];
        this.testResults = {
            connection: false,
            dataSync: false,
            userUpdate: false,
            favoriteUpdate: false,
            travelPlanUpdate: false
        };
    }

    // 连接WebSocket
    async connectWebSocket() {
        return new Promise((resolve, reject) => {
            console.log('🔗 正在连接WebSocket...');
            
            this.ws = new WebSocket(this.wsUrl);
            
            this.ws.on('open', () => {
                console.log('✅ WebSocket连接成功');
                this.testResults.connection = true;
                resolve();
            });
            
            this.ws.on('message', (data) => {
                try {
                    const message = JSON.parse(data.toString());
                    console.log('📨 收到消息:', JSON.stringify(message, null, 2));
                    this.receivedMessages.push(message);
                    this.analyzeMessage(message);
                } catch (error) {
                    console.error('❌ 解析消息失败:', error);
                }
            });
            
            this.ws.on('error', (error) => {
                console.error('❌ WebSocket错误:', error);
                reject(error);
            });
            
            this.ws.on('close', () => {
                console.log('🔌 WebSocket连接关闭');
            });
            
            // 设置超时
            setTimeout(() => {
                if (!this.testResults.connection) {
                    reject(new Error('WebSocket连接超时'));
                }
            }, 5000);
        });
    }

    // 分析收到的消息
    analyzeMessage(message) {
        if (message.type === 'connected') {
            console.log('✅ 收到连接确认消息');
        } else if (message.type === 'data_update') {
            console.log('✅ 收到数据更新消息');
            this.testResults.dataSync = true;
            
            const { dataType } = message.data;
            switch (dataType) {
                case 'user':
                    this.testResults.userUpdate = true;
                    console.log('✅ 用户数据更新同步正常');
                    break;
                case 'favorite':
                    this.testResults.favoriteUpdate = true;
                    console.log('✅ 收藏数据更新同步正常');
                    break;
                case 'travel_plan':
                    this.testResults.travelPlanUpdate = true;
                    console.log('✅ 行程规划数据更新同步正常');
                    break;
            }
        }
    }

    // 测试用户注册（触发实时同步）
    async testUserRegistration() {
        console.log('\n🧪 测试用户注册实时同步...');
        
        const timestamp = Date.now();
        const userData = {
            username: `test_user_${timestamp}`,
            password: 'test123456',
            email: `test${timestamp}@example.com`,
            phone: `138${timestamp.toString().slice(-8)}`
        };
        
        try {
            const response = await axios.post(`${this.baseUrl}/api/auth/register`, userData);
            console.log('✅ 用户注册成功:', response.data);
            
            // 等待WebSocket消息
            await this.waitForMessage('user', 3000);
            
        } catch (error) {
            console.error('❌ 用户注册失败:', error.response?.data || error.message);
        }
    }

    // 测试收藏功能（触发实时同步）
    async testFavoriteOperation() {
        console.log('\n🧪 测试收藏操作实时同步...');
        
        // 首先需要登录获取token
        const loginData = {
            username: 'admin',
            password: 'admin123'
        };
        
        try {
            const loginResponse = await axios.post(`${this.baseUrl}/api/auth/login`, loginData);
            const token = loginResponse.data.data.token;
            
            // 添加收藏
            const favoriteData = {
                userId: 1,
                scenicId: 1
            };
            
            const response = await axios.post(`${this.baseUrl}/api/scenic/favorite`, favoriteData, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            
            console.log('✅ 添加收藏成功:', response.data);
            
            // 等待WebSocket消息
            await this.waitForMessage('favorite', 3000);
            
        } catch (error) {
            console.error('❌ 收藏操作失败:', error.response?.data || error.message);
        }
    }

    // 等待特定类型的消息
    async waitForMessage(dataType, timeout = 5000) {
        return new Promise((resolve) => {
            const startTime = Date.now();
            
            const checkMessage = () => {
                const found = this.receivedMessages.some(msg => 
                    msg.type === 'data_update' && msg.data.dataType === dataType
                );
                
                if (found) {
                    console.log(`✅ 收到${dataType}类型的实时同步消息`);
                    resolve(true);
                } else if (Date.now() - startTime > timeout) {
                    console.log(`⏰ 等待${dataType}类型消息超时`);
                    resolve(false);
                } else {
                    setTimeout(checkMessage, 100);
                }
            };
            
            checkMessage();
        });
    }

    // 测试WebSocket连接状态
    async testConnectionStatus() {
        console.log('\n🧪 测试WebSocket连接状态...');
        
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            console.log('✅ WebSocket连接状态正常');
            
            // 发送测试消息
            this.ws.send(JSON.stringify({
                type: 'ping',
                timestamp: Date.now()
            }));
            
            console.log('📤 发送测试消息');
        } else {
            console.log('❌ WebSocket连接状态异常');
        }
    }

    // 运行所有测试
    async runAllTests() {
        console.log('🚀 开始实时同步功能测试\n');
        
        try {
            // 1. 连接WebSocket
            await this.connectWebSocket();
            
            // 2. 测试连接状态
            await this.testConnectionStatus();
            
            // 3. 测试用户注册同步
            await this.testUserRegistration();
            
            // 4. 测试收藏操作同步
            await this.testFavoriteOperation();
            
            // 等待一段时间确保所有消息都收到
            await new Promise(resolve => setTimeout(resolve, 2000));
            
            // 5. 输出测试结果
            this.printTestResults();
            
        } catch (error) {
            console.error('❌ 测试过程中出现错误:', error);
        } finally {
            // 关闭WebSocket连接
            if (this.ws) {
                this.ws.close();
            }
        }
    }

    // 输出测试结果
    printTestResults() {
        console.log('\n📊 测试结果汇总:');
        console.log('==========================================');
        
        const results = [
            { name: 'WebSocket连接', status: this.testResults.connection },
            { name: '数据同步功能', status: this.testResults.dataSync },
            { name: '用户数据同步', status: this.testResults.userUpdate },
            { name: '收藏数据同步', status: this.testResults.favoriteUpdate },
            { name: '行程规划同步', status: this.testResults.travelPlanUpdate }
        ];
        
        results.forEach(result => {
            const icon = result.status ? '✅' : '❌';
            const status = result.status ? '通过' : '失败';
            console.log(`${icon} ${result.name}: ${status}`);
        });
        
        console.log('==========================================');
        
        const passedTests = results.filter(r => r.status).length;
        const totalTests = results.length;
        
        console.log(`\n📈 测试通过率: ${passedTests}/${totalTests} (${Math.round(passedTests/totalTests*100)}%)`);
        
        if (this.receivedMessages.length > 0) {
            console.log(`\n📨 总共收到 ${this.receivedMessages.length} 条WebSocket消息`);
        }
        
        // 实时同步功能评估
        if (this.testResults.connection && this.testResults.dataSync) {
            console.log('\n🎉 实时同步功能基本正常！');
        } else if (this.testResults.connection) {
            console.log('\n⚠️  WebSocket连接正常，但数据同步可能有问题');
        } else {
            console.log('\n❌ 实时同步功能存在问题，请检查WebSocket配置');
        }
    }
}

// 运行测试
async function main() {
    const tester = new RealtimeSyncTester();
    await tester.runAllTests();
}

// 如果直接运行此脚本
if (require.main === module) {
    main().catch(console.error);
}

module.exports = RealtimeSyncTester;