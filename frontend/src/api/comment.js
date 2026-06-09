import request from '../utils/request'

/**
 * 获取景点/店铺下评论及嵌套回复（含商家回复）
 * GET /api/comment/list?scenicId=
 */
export function getCommentList(scenicId) {
  return request.get('/comment/list', { params: { scenicId } })
}

/**
 * 商家回复（JWT 解析商家，无需传 merchantId）
 * POST /api/comment/reply
 * body: { reviewId, content, parentReplyId? }
 */
export function postCommentReply(body) {
  return request.post('/comment/reply', body)
}
