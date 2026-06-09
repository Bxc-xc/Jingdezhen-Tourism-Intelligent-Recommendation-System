// 实时数据同步组合式函数
import { ref, onMounted, onUnmounted } from 'vue'
import realtimeSync from '../utils/websocket'

export function useRealtimeData(dataType, initialData = []) {
  const data = ref(initialData)
  const loading = ref(false)
  const error = ref(null)

  // 处理数据更新
  const handleDataUpdate = (updateInfo) => {
    const { operation, data: updateData } = updateInfo
    
    switch (operation) {
      case 'create':
        data.value.push(updateData)
        break
      case 'update':
        const updateIndex = data.value.findIndex(item => item.id === updateData.id)
        if (updateIndex !== -1) {
          data.value[updateIndex] = updateData
        }
        break
      case 'delete':
        data.value = data.value.filter(item => item.id !== updateData.id)
        break
      default:
        console.log('未知操作类型:', operation)
    }
  }

  // 处理通知
  const handleNotification = (notification) => {
    // 这里可以显示通知，比如使用Element Plus的Message组件
    console.log('收到通知:', notification)
  }

  onMounted(() => {
    // 连接WebSocket
    if (!realtimeSync.isConnected) {
      realtimeSync.connect()
    }

    // 订阅数据更新
    realtimeSync.subscribe(dataType, handleDataUpdate)
    
    // 订阅通知
    realtimeSync.addEventListener('notification', handleNotification)
  })

  onUnmounted(() => {
    // 取消订阅
    realtimeSync.unsubscribe(dataType, handleDataUpdate)
    realtimeSync.removeEventListener('notification', handleNotification)
  })

  return {
    data,
    loading,
    error,
    handleDataUpdate,
    handleNotification
  }
}