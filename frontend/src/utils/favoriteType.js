const MERCHANT_CATEGORIES = new Set(['HOTEL', 'FOOD', 'CERAMIC', 'MARKETPLACE'])

export const FAVORITE_TYPE = {
  SCENIC: 'SCENIC',
  MERCHANT: 'MERCHANT'
}

export function resolveFavoriteType(item = {}) {
  const type = String(item.type || '').toUpperCase()
  const category = String(item.category || '').toUpperCase()
  if (type === FAVORITE_TYPE.MERCHANT || MERCHANT_CATEGORIES.has(type) || MERCHANT_CATEGORIES.has(category)) {
    return FAVORITE_TYPE.MERCHANT
  }
  return FAVORITE_TYPE.SCENIC
}

