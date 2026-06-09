import request from '../utils/request'

/**
 * 全局模糊搜索
 * 覆盖：景点、陶瓷市集、陶瓷工坊、餐饮、酒店、路线
 */
export function globalSearch(keyword) {
  return request.get('/search', { params: { keyword } })
}
