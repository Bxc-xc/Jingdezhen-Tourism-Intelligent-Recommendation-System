/**
 * 智能收藏功能实时同步测试
 * 先清理可能存在的收藏，然后测试添加和删除
 */

const WebSocket = require('ws');
const axios = require('axios');

class SmartFavoriteSyncTester {
    constructor() {
        this.baseUrl = 'http://localhost:8889';
        this.wsUrl = 'ws://localhost:8889/ws/data-sync';
        this.ws = null;
        this.receivedMessages = [];
        this.testResults = {
            connection: false,
            favoriteAdd: false,
            favoriteDataSync: false
        };
        this.testUserId = 2; // 使用不同的用户ID
        this.testScenicId = 2; // 使用不同的景点ID
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
                }
            }
        }
    }

    // 清理可能存在的收藏（静默处理）
    async cleanupExistingFavorite() {
        console.log(`\n🧹 清理可能存在的收藏 (用户${this.testUserId}, 景点${this.testScenicId})...`);
        
        try {
            await axios.delete(`${this.baseUrl}/api/scenic/favorite/${this.testScenicId}?userId=${this.testUserId}`);
            console.log('🗑️  清理完成');
        } catch (error) {
            console.log('ℹ️  无需清理或清理失败（正常情况）');
        }
        
        // 等待一段时间让清理操作完成
        await new Promise(resolve => setTimeout(resolve, 500));
    }

    // 测试添加收藏
    async testAddFavorite() {
        console.log(`\n🧪 测试添加收藏 (用户${this.testUserId}, 景点${this.testScenicId})...`);
        
        const favoriteData = {
            userId: this.testUserId,
            scenicId: this.testScenicId
        };
        
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
        console.log('🚀 开始智能收藏功能实时同步测试\n');
        
        try {
            // 1. 连接WebSocket
            await this.connectWebSocket();
            
            // 2. 清理可能存在的收藏
            await this.cleanupExistingFavorite();
            
            // 3. 测试添加收藏
            await this.testAddFavorite();
            
            // 等待一段时间确保所有消息都收到
            await new Promise(resolve => setTimeout(resolve, 2000));
            
            // 4. 输出测试结果
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
        console.log('\n📊 智能收藏功能实时同步测试结果:');
        console.log('==========================================');
        
        const results = [
            { name: 'WebSocket连接', status: this.testResults.connection },
            { name: '收藏数据实时同步', status: this.testResults.favoriteDataSync },
            { name: '添加收藏同步', status: this.testResults.favoriteAdd }
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
                    const { dataType, operation, data } = msg.data;
                    console.log(`  ${index + 1}. ${msg.type}: ${dataType}-${operation}`);
                    if (dataType === 'favorite') {
                        console.log(`     数据: userId=${data.userId}, scenicId=${data.scenicId}, id=${data.id}`);
                    }
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
                console.log('🎉 收藏操作实时同步验证成功！');
            } else {
                console.log('⚠️  WebSocket连接正常，但未收到收藏数据同步消息');
                console.log('   可能原因：');
                console.log('   1. 收藏操作失败');
                console.log('   2. 实时同步服务调用失败');
                console.log('   3. JSON序列化问题');
            }
        } else {
            console.log('❌ WebSocket连接失败');
        }
        
        // 总结
        if (this.testResults.connection && this.testResults.favoriteDataSync) {
            console.log('\n🎊 结论: 收藏功能实时同步完全正常工作！');
        } else if (this.testResults.connection) {
            console.log('\n⚠️  结论: WebSocket基础设施正常，但收藏业务层实时同步需要检查');
        } else {
            console.log('\n❌ 结论: 收藏功能实时同步存在基础设施问题');
        }
    }
}

// 运行测试
async function main() {
    const tester = new SmartFavoriteSyncTester();
    await tester.runAllTests();
}

// 如果直接运行此脚本
if (require.main === module) {
    main().catch(console.error);
}

module.exports = SmartFavoriteSyncTester;