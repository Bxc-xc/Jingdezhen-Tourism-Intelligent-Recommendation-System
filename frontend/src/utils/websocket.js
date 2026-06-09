// WebSocket实时数据同步
class RealtimeDataSync {
  constructor() {
    this.ws = null
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectInterval = 3000
    this.listeners = new Map()
    this.isConnected = false
  }

  // 连接WebSocket
  connect() {
    // 优先使用环境变量配置的完整WS地址
    const envWsUrl = import.meta?.env?.VITE_WS_URL
    
    let wsUrl
    if (envWsUrl) {
      wsUrl = envWsUrl
    } else {
      // 根据当前协议自动选择 ws/wss
      const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
      // 使用当前 host，让 Vite 代理（开发环境）或直接连接（生产环境）处理
      wsUrl = `${protocol}//${window.location.host}/ws/data-sync`
    }
    
    console.log('连接WebSocket:', wsUrl)
    console.log('WebSocket Client v1.1 initialized')
    
    this.ws = new WebSocket(wsUrl)
    
    this.ws.onopen = () => {
      console.log('WebSocket连接成功')
      this.isConnected = true
      this.reconnectAttempts = 0
      this.emit('connected')
    }
    
    this.ws.onmessage = (event) => {
      try {
        const message = JSON.parse(event.data)
        console.log('收到WebSocket消息:', message)
        this.handleMessage(message)
      } catch (error) {
        console.error('解析WebSocket消息失败:', error)
      }
    }
    
    this.ws.onclose = () => {
      console.log('WebSocket连接关闭')
      this.isConnected = false
      this.emit('disconnected')
      this.reconnect()
    }
    
    this.ws.onerror = (error) => {
      // WebSocket 连接错误不影响应用功能，只记录日志
      console.warn('WebSocket连接错误，将使用轮询作为备用方案:', error)
      // 不触发 error 事件，避免影响应用
      // this.emit('error', error)
    }
  }

  // 处理接收到的消息
  handleMessage(message) {
    const { type, data } = message
    
    switch (type) {
      case 'data_update':
        this.handleDataUpdate(data)
        break
      case 'notification':
        this.handleNotification(data)
        break
      case 'connected':
        console.log('WebSocket连接已建立/心跳:', data)
        this.isConnected = true
        this.emit('connected', data)
        break
      case 'pong':
        // 心跳响应，忽略
        break
      default:
        console.log('未知消息类型:', type)
    }
  }

  // 处理数据更新
  handleDataUpdate(data) {
    const { dataType, operation, data: updateData } = data
    
    // 触发对应的事件
    this.emit(`data_update_${dataType}`, {
      operation,
      data: updateData,
      timestamp: data.timestamp
    })
    
    // 触发通用数据更新事件
    this.emit('data_update', {
      dataType,
      operation,
      data: updateData,
      timestamp: data.timestamp
    })
  }

  // 处理通知
  handleNotification(data) {
    this.emit('notification', data)
  }

  // 重连机制
  reconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      console.log(`尝试重连WebSocket (${this.reconnectAttempts}/${this.maxReconnectAttempts})`)
      
      setTimeout(() => {
        this.connect()
      }, this.reconnectInterval)
    } else {
      console.warn('WebSocket重连失败，已达到最大重连次数。应用将使用轮询作为备用方案。')
      // 不再尝试重连，应用可以使用轮询机制
    }
  }

  // 发送消息
  send(message) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(message))
    } else {
      console.warn('WebSocket未连接，无法发送消息')
    }
  }

  // 订阅数据更新
  subscribe(dataType, callback) {
    const eventName = `data_update_${dataType}`
    this.addEventListener(eventName, callback)
  }

  // 取消订阅
  unsubscribe(dataType, callback) {
    const eventName = `data_update_${dataType}`
    this.removeEventListener(eventName, callback)
  }

  // 添加事件监听器
  addEventListener(event, callback) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, new Set())
    }
    this.listeners.get(event).add(callback)
  }

  // 移除事件监听器
  removeEventListener(event, callback) {
    if (this.listeners.has(event)) {
      this.listeners.get(event).delete(callback)
    }
  }

  // 触发事件
  emit(event, data) {
    if (this.listeners.has(event)) {
      this.listeners.get(event).forEach(callback => {
        try {
          callback(data)
        } catch (error) {
          console.error('事件回调执行失败:', error)
        }
      })
    }
  }

  // 断开连接
  disconnect() {
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
    this.isConnected = false
  }
}

// 创建全局实例
const realtimeSync = new RealtimeDataSync()

export default realtimeSync