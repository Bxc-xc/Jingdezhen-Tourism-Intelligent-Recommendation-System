import request from '../utils/request'

// 获取景点评论
export function getReviewList(scenicId, params = {}) {
  return request.get(`/reviews/scenic/${scenicId}`, { params })
}

// 发布评论（支持景点或商家）
export function createReview(reviewData) {
  return request.post('/reviews', reviewData)
}

// 发布商家评论
export function createMerchantReview(reviewData) {
  return request.post('/reviews/merchant', reviewData)
}

// 获取商家评论列表
export function getMerchantReviewList(merchantId, params = {}) {
  return request.get(`/reviews/merchant/${merchantId}`, { params })
}

// 获取商家平均评分
export function getMerchantAverageRating(merchantId) {
  return request.get(`/reviews/merchant/${merchantId}/average-rating`)
}

// 获取商家评论数量
export function getMerchantReviewCount(merchantId) {
  return request.get(`/reviews/merchant/${merchantId}/count`)
}

// 回复评论
export function replyReview(reviewId, content) {
  return request.post(`/reviews/${reviewId}/reply`, { content })
}

// 删除评论
export function deleteReview(reviewId) {
  return request.delete(`/reviews/${reviewId}`)
}

// 批量删除评论（管理员端使用）
export function batchDeleteReviews(ids) {
  return request.post('/reviews/batch-delete', { ids })
}

// 根据用户ID获取评论列表
export function getReviewsByUserId(userId) {
  return request.get(`/reviews/user/${userId}`)
}

// 获取所有评论（管理员端使用）
export function getAllReviews() {
  return request.get('/reviews')
}