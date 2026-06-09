import request from '../utils/request'

export function favoriteMerchant(userId, merchantId) {
  return request.post('/merchant/favorite', { userId, merchantId })
}

export function unfavoriteMerchant(userId, merchantId) {
  return request.delete(`/merchant/favorite/${merchantId}`, { params: { userId } })
}

export function getMerchantFavorites(userId) {
  return request.get('/merchant/favorites', { params: { userId } })
}

export function checkFavoriteMerchant(userId, merchantId) {
  return request.get(`/merchant/favorite/${merchantId}`, { params: { userId } })
}

