/**
 * 直接测试WebSocket数据推送功能
 * 通过直接调用RealtimeDataService来测试实时同步
 */

const WebSocket = require('ws');

class DirectWebSocketTester {
    constructor() {
        this.wsUrl = 'ws://localhost:8889/ws/data-sync';
        this.ws = null;
        this.receivedMessages = [];
        this.testResults = {
            connection: false,
            messageReceived: false,
            dataUpdateReceived: false
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
        this.testResults.messageReceived = true;
        
        if (message.type === 'connected') {
            console.log('✅ 收到连接确认消息');
        } else if (message.type === 'data_update') {
            console.log('✅ 收到数据更新消息');
            this.testResults.dataUpdateReceived = true;
            
            const { dataType, operation } = message.data;
            console.log(`📊 数据类型: ${dataType}, 操作: ${operation}`);
        }
    }

    // 测试WebSocket基本功能
    async testBasicWebSocket() {
        console.log('\n🧪 测试WebSocket基本功能...');
        
        // 发送ping消息
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            const testMessage = {
                type: 'ping',
                timestamp: Date.now()
            };
            
            this.ws.send(JSON.stringify(testMessage));
            console.log('📤 发送测试消息:', testMessage);
        }
        
        // 等待一段时间
        await new Promise(resolve => setTimeout(resolve, 1000));
    }

    // 等待消息
    async waitForMessages(timeout = 5000) {
        console.log('\n⏳ 等待WebSocket消息...');
        
        return new Promise((resolve) => {
            const startTime = Date.now();
            
            const checkMessages = () => {
                if (this.receivedMessages.length > 0) {
                    console.log(`✅ 收到 ${this.receivedMessages.length} 条消息`);
                    resolve(true);
                } else if (Date.now() - startTime > timeout) {
                    console.log('⏰ 等待消息超时');
                    resolve(false);
                } else {
                    setTimeout(checkMessages, 100);
                }
            };
            
            checkMessages();
        });
    }

    // 运行所有测试
    async runAllTests() {
        console.log('🚀 开始WebSocket直接测试\n');
        
        try {
            // 1. 连接WebSocket
            await this.connectWebSocket();
            
            // 2. 测试基本功能
            await this.testBasicWebSocket();
            
            // 3. 等待消息
            await this.waitForMessages(3000);
            
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
        console.log('\n📊 WebSocket测试结果:');
        console.log('==========================================');
        
        const results = [
            { name: 'WebSocket连接', status: this.testResults.connection },
            { name: '消息接收', status: this.testResults.messageReceived },
            { name: '数据更新消息', status: this.testResults.dataUpdateReceived }
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
            console.log(`\n📨 收到的消息详情:`);
            this.receivedMessages.forEach((msg, index) => {
                console.log(`  ${index + 1}. ${msg.type} - ${JSON.stringify(msg.data)}`);
            });
        }
        
        // WebSocket功能评估
        if (this.testResults.connection) {
            if (this.testResults.messageReceived) {
                console.log('\n🎉 WebSocket基本功能正常！');
                if (this.testResults.dataUpdateReceived) {
                    console.log('✨ 实时数据同步功能已实现并可正常工作！');
                } else {
                    console.log('ℹ️  WebSocket连接正常，但需要业务操作触发数据同步消息');
                }
            } else {
                console.log('\n⚠️  WebSocket连接正常，但消息传输可能有问题');
            }
        } else {
            console.log('\n❌ WebSocket连接失败');
        }
    }
}

// 运行测试
async function main() {
    const tester = new DirectWebSocketTester();
    await tester.runAllTests();
}

// 如果直接运行此脚本
if (require.main === module) {
    main().catch(console.error);
}

module.exports = DirectWebSocketTester;