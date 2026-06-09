/**
 * 收藏功能实时同步测试
 * 专门测试收藏操作的实时同步功能
 */

const WebSocket = require('ws');
const axios = require('axios');

class FavoriteSyncTester {
    constructor() {
        this.baseUrl = 'http://localhost:8889';
        this.wsUrl = 'ws://localhost:8889/ws/data-sync';
        this.ws = null;
        this.receivedMessages = [];
        this.testResults = {
            connection: false,
            favoriteAdd: false,
            favoriteRemove: false,
            favoriteDataSync: false
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
                    console.log('📨 收到WebSocket消息:', JSON.stringify(message, null, 2));
                    this.receivedMessages.push(message);
                    this.analyzeMessage(message);
                } catch (error) {
                    console.error('❌ 解析WebSocket消息失败:', error);
                }
            });
            
            this.ws.on('error', (error) => {
                console.error('❌ WebSocket错误:', error);
                reject(error);
            });
            
            this.ws.on('close', () => {
                console.log('🔌 WebSocket连接关闭');
            });
            
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
            
            const { dataType, operation } = message.data;
            console.log(`📊 数据类型: ${dataType}, 操作: ${operation}`);
            
            if (dataType === 'favorite') {
                this.testResults.favoriteDataSync = true;
                console.log('🎯 收藏数据实时同步成功');
                
                if (operation === 'create') {
                    this.testResults.favoriteAdd = true;
                    console.log('✨ 添加收藏实时同步成功');
                } else if (operation === 'delete') {
                    this.testResults.favoriteRemove = true;
                    console.log('✨ 删除收藏实时同步成功');
                }
            }
        }
    }

    // 测试添加收藏
    async testAddFavorite() {
        console.log('\n🧪 测试添加收藏...');
        
        // 使用随机的景点ID来避免重复收藏
        const scenicId = Math.floor(Math.random() * 10) + 1;
        const favoriteData = {
            userId: 1,
            scenicId: scenicId
        };
        
        console.log(`📍 测试收藏景点ID: ${scenicId}`);
        
        try {
            const response = await axios.post(`${this.baseUrl}/api/scenic/favorite`, favoriteData);
            
            if (response.data.success) {
                console.log('✅ 添加收藏成功');
                
                // 等待WebSocket消息
                await this.waitForMessage('favorite', 3000);
            } else {
                console.log('❌ 添加收藏失败:', response.data.message);
            }
            
        } catch (error) {
            console.error('❌ 添加收藏请求失败:', error.response?.data || error.message);
        }
    }

    // 测试删除收藏
    async testRemoveFavorite() {
        console.log('\n🧪 测试删除收藏...');
        
        try {
            // 使用正确的API格式：DELETE /api/scenic/favorite/{scenicId}?userId={userId}
            const response = await axios.delete(`${this.baseUrl}/api/scenic/favorite/1?userId=1`);
            
            if (response.data.success) {
                console.log('✅ 删除收藏成功');
                
                // 等待WebSocket消息
                await this.waitForMessage('favorite', 3000);
            } else {
                console.log('❌ 删除收藏失败:', response.data.message);
            }
            
        } catch (error) {
            console.error('❌ 删除收藏请求失败:', error.response?.data || error.message);
        }
    }

    // 等待特定类型的消息
    async waitForMessage(dataType, timeout = 5000) {
        return new Promise((resolve) => {
            const startTime = Date.now();
            const initialCount = this.receivedMessages.filter(msg => 
                msg.type === 'data_update' && msg.data.dataType === dataType
            ).length;
            
            const checkMessage = () => {
                const currentCount = this.receivedMessages.filter(msg => 
                    msg.type === 'data_update' && msg.data.dataType === dataType
                ).length;
                
                if (currentCount > initialCount) {
                    console.log(`✅ 收到新的${dataType}类型实时同步消息`);
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

    // 运行所有测试
    async runAllTests() {
        console.log('🚀 开始收藏功能实时同步测试\n');
        
        try {
            // 1. 连接WebSocket
            await this.connectWebSocket();
            
            // 2. 测试添加收藏
            await this.testAddFavorite();
            
            // 3. 等待一段时间
            await new Promise(resolve => setTimeout(resolve, 1000));
            
            // 4. 测试删除收藏
            await this.testRemoveFavorite();
            
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
        console.log('\n📊 收藏功能实时同步测试结果:');
        console.log('==========================================');
        
        const results = [
            { name: 'WebSocket连接', status: this.testResults.connection },
            { name: '收藏数据实时同步', status: this.testResults.favoriteDataSync },
            { name: '添加收藏同步', status: this.testResults.favoriteAdd },
            { name: '删除收藏同步', status: this.testResults.favoriteRemove }
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
            console.log(`\n📨 收到的WebSocket消息 (${this.receivedMessages.length} 条):`);
            this.receivedMessages.forEach((msg, index) => {
                if (msg.type === 'data_update') {
                    const { dataType, operation } = msg.data;
                    console.log(`  ${index + 1}. ${msg.type}: ${dataType}-${operation}`);
                } else {
                    console.log(`  ${index + 1}. ${msg.type}: ${JSON.stringify(msg.data)}`);
                }
            });
        }
        
        // 收藏功能实时同步评估
        console.log('\n🎯 收藏功能实时同步评估:');
        if (this.testResults.connection) {
            console.log('✅ WebSocket连接功能正常');
            
            if (this.testResults.favoriteDataSync) {
                console.log('✅ 收藏数据实时同步功能正常');
                
                if (this.testResults.favoriteAdd || this.testResults.favoriteRemove) {
                    console.log('🎉 收藏操作实时同步验证成功！');
                } else {
                    console.log('⚠️  收到收藏同步消息，但操作类型可能不完整');
                }
            } else {
                console.log('⚠️  WebSocket连接正常，但未收到收藏数据同步消息');
            }
        } else {
            console.log('❌ WebSocket连接失败');
        }
        
        // 总结
        if (this.testResults.connection && this.testResults.favoriteDataSync) {
            console.log('\n🎊 结论: 收藏功能实时同步正常工作！');
        } else if (this.testResults.connection) {
            console.log('\n⚠️  结论: WebSocket正常，但收藏实时同步可能需要进一步检查');
        } else {
            console.log('\n❌ 结论: 收藏功能实时同步存在问题');
        }
    }
}

// 运行测试
async function main() {
    const tester = new FavoriteSyncTester();
    await tester.runAllTests();
}

// 如果直接运行此脚本
if (require.main === module) {
    main().catch(console.error);
}

module.exports = FavoriteSyncTester;