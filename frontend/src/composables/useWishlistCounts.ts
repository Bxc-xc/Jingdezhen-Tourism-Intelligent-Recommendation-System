import { ref } from 'vue'

// Lightweight mock of wishlist "popularity" counts (front-end only)
// Stable pseudo-random seed per id; persisted in localStorage for consistency.
const KEY = 'wishlist:counts:v1'

function seedForId(id: number | string): number {
  const s = String(id)
  let hash = 0
  for (let i = 0; i < s.length; i++) {
    hash = (hash << 5) - hash + s.charCodeAt(i)
    hash |= 0
  }
  return Math.abs(hash)
}

function initialCount(id: number | string): number {
  const base = (seedForId(id) % 90) + 10 // 10 - 99
  return base
}

type CountsMap = Record<string, number>

function load(): CountsMap {
  try {
    const raw = localStorage.getItem(KEY)
    if (!raw) return {}
    return JSON.parse(raw) as CountsMap
  } catch {
    return {}
  }
}

function save(map: CountsMap) {
  localStorage.setItem(KEY, JSON.stringify(map))
}

const countsMap = ref<CountsMap>(load())

export function useWishlistCounts() {
  function getCount(id: number | string): number {
    const k = String(id)
    if (!(k in countsMap.value)) {
      countsMap.value[k] = initialCount(k)
      save(countsMap.value)
    }
    return countsMap.value[k]
  }
  function increment(id: number | string) {
    const k = String(id)
    countsMap.value[k] = getCount(k) + 1
    save(countsMap.value)
  }
  function decrement(id: number | string) {
    const k = String(id)
    countsMap.value[k] = Math.max(0, getCount(k) - 1)
    save(countsMap.value)
  }
  return { getCount, increment, decrement, countsMap }
}

