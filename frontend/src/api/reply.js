import request from '../utils/request'

// 游客回复评论
export function createUserReply(reviewId, userId, content) {
  return request.post(`/replies/review/${reviewId}/user`, { userId, content })
}

// 商家回复评论
export function createMerchantReply(reviewId, merchantId, content) {
  return request.post(`/replies/review/${reviewId}/merchant`, { merchantId, content })
}

// 游客回复回复
export function createUserReplyToReply(reviewId, parentReplyId, userId, content) {
  return request.post(`/replies/review/${reviewId}/reply/${parentReplyId}/user`, { userId, content })
}

// 商家回复回复
export function createMerchantReplyToReply(reviewId, parentReplyId, merchantId, content) {
  return request.post(`/replies/review/${reviewId}/reply/${parentReplyId}/merchant`, { merchantId, content })
}

// 根据评论ID获取所有回复（树形结构）
export function getRepliesByReviewId(reviewId) {
  return request.get(`/replies/review/${reviewId}`)
}

// 根据父回复ID获取子回复
export function getChildReplies(parentReplyId) {
  return request.get(`/replies/reply/${parentReplyId}/children`)
}

// 删除回复
export function deleteReply(replyId) {
  return request.delete(`/replies/${replyId}`)
}

// 商家删除自己的回复（带权限校验）
export function deleteMerchantReply(replyId) {
  return request.delete(`/replies/${replyId}/merchant`)
}

// 更新回复
export function updateReply(replyId, content) {
  return request.put(`/replies/${replyId}`, { content })
}

