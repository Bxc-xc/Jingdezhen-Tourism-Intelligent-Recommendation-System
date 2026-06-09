import { favoriteScenic, unfavoriteScenic, checkFavoriteScenic } from './scenic'
import { favoriteMerchant, unfavoriteMerchant, checkFavoriteMerchant } from './merchantFavorite'
import { FAVORITE_TYPE } from '../utils/favoriteType'

export async function addFavorite({ userId, targetId, targetType }) {
  if (targetType === FAVORITE_TYPE.MERCHANT) {
    return favoriteMerchant(userId, targetId)
  }
  return favoriteScenic(userId, targetId)
}

export async function removeFavorite({ userId, targetId, targetType }) {
  if (targetType === FAVORITE_TYPE.MERCHANT) {
    return unfavoriteMerchant(userId, targetId)
  }
  return unfavoriteScenic(userId, targetId)
}

export async function checkFavorite({ userId, targetId, targetType }) {
  if (targetType === FAVORITE_TYPE.MERCHANT) {
    return checkFavoriteMerchant(userId, targetId)
  }
  return checkFavoriteScenic(userId, targetId)
}

