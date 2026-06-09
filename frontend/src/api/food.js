import request from '../utils/request'

// 模拟美食数据（作为降级方案）
const mockFoodData = [
  {
    id: 1,
    name: '景德镇特色小吃街',
    description: '汇集景德镇各类传统小吃，包括冷粉、碱水粑、饺子粑等',
    image: 'https://picsum.photos/400/300?random=1',
    rating: 4.8,
    reviewCount: 256,
    price: 50,
    address: '景德镇市珠山区',
    category: '小吃',
    tags: '传统,小吃,特色'
  },
  {
    id: 2,
    name: '景德镇大酒店',
    description: '四星级酒店，提供正宗赣菜和景德镇地方菜',
    image: 'https://picsum.photos/400/300?random=2',
    rating: 4.6,
    reviewCount: 189,
    price: 150,
    address: '景德镇市昌江区',
    category: '餐厅',
    tags: '酒店,赣菜,高档'
  },
  {
    id: 3,
    name: '陶溪川美食广场',
    description: '位于陶溪川文创区，提供各类美食和饮品',
    image: 'https://picsum.photos/400/300?random=3',
    rating: 4.5,
    reviewCount: 312,
    price: 80,
    address: '景德镇市珠山区陶溪川',
    category: '美食广场',
    tags: '文创,多样,休闲'
  },
  {
    id: 4,
    name: '景德镇冷粉店',
    description: '景德镇特色冷粉，口感爽滑，配菜丰富',
    image: 'https://picsum.photos/400/300?random=4',
    rating: 4.7,
    reviewCount: 445,
    price: 15,
    address: '景德镇市珠山区',
    category: '小吃',
    tags: '特色,传统,实惠'
  },
  {
    id: 5,
    name: '陶瓷主题餐厅',
    description: '以陶瓷文化为主题的特色餐厅，环境优雅',
    image: 'https://picsum.photos/400/300?random=5',
    rating: 4.9,
    reviewCount: 178,
    price: 120,
    address: '景德镇市昌江区',
    category: '主题餐厅',
    tags: '主题,文化,环境'
  }
]

// 获取美食推荐列表
export function getRecommendFood(params = {}) {
  return request.get('/food/recommend', { params })
    .catch(error => {
      // 如果后端API失败，返回模拟数据作为降级方案
      console.warn('美食推荐API调用失败，使用模拟数据:', error)
      return Promise.resolve({
        success: true,
        data: mockFoodData,
        total: mockFoodData.length
      })
    })
}

// 获取美食详情
export function getFoodDetail(id) {
  return request.get(`/food/${id}`)
    .catch(error => {
      // 如果后端API失败，从模拟数据中查找
      console.warn('美食详情API调用失败，使用模拟数据:', error)
      const food = mockFoodData.find(f => f.id === id)
      if (food) {
        return Promise.resolve({
          success: true,
          data: food
        })
      }
      return Promise.reject(error)
    })
}

// 搜索美食
export function searchFood(keyword) {
  return request.get('/food/recommend', { params: { keyword } })
    .catch(error => {
      // 如果后端API失败，从模拟数据中搜索
      console.warn('美食搜索API调用失败，使用模拟数据:', error)
      const filtered = mockFoodData.filter(food => 
        food.name.includes(keyword) || 
        food.description.includes(keyword) ||
        food.category.includes(keyword)
      )
      return Promise.resolve({
        success: true,
        data: filtered,
        total: filtered.length
      })
    })
}

// 删除美食
export function deleteFood(id) {
  return request.delete(`/food/${id}`)
}

