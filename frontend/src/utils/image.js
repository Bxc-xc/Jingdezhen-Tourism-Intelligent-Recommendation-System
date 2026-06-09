export function normalizeImageUrl(url) {
	if (!url) return ''
	const s = String(url).trim()
	// 如果是后端返回的绝对URL（含localhost或127.0.0.1），提取路径部分走代理
	if (s.startsWith('http://') || s.startsWith('https://')) {
		try {
			const parsed = new URL(s)
			const host = parsed.hostname
			if (host === 'localhost' || host === '127.0.0.1' || host === '0.0.0.0') {
				// 提取路径部分，让Vite代理处理
				return parsed.pathname
			}
		} catch {}
		return s
	}
	// prepend axios baseURL origin (strip trailing /api if present)
	const base = (import.meta?.env?.VITE_API_BASE_URL || '/api')
	try {
		// If base is absolute, use its origin; otherwise use window.location.origin
		const absolute = new URL(base, window.location.origin)
		// If base path ends with /api, strip it to map static '/uploads' from server root
		const origin = absolute.origin
		return s.startsWith('/')
			? origin + s
			: origin + '/' + s
	} catch {
		return s
	}
}

export function toSingleFileList(url) {
	const u = normalizeImageUrl(url)
	return u ? [{ url: u, name: 'image' }] : []
}

// 图片处理通用工具（前端）
const API_BASE_URL = (import.meta?.env?.VITE_API_BASE_URL) || '/api'
const CDN_BASE_URL = (import.meta?.env?.VITE_CDN_BASE_URL) || ''
export const FALLBACK_IMAGE = 'https://dummyimage.com/1200x400/eeeeee/aaaaaa.png&text=No+Image'

/**
 * 标准化图片URL
 * @param {string} url - 原始URL
 * @returns {string} - 标准化后的URL
 */
export function normalizeUrl(url) {
  // 1. 空值检查
  if (!url || typeof url !== 'string') {
    console.warn('normalizeUrl: 无效的URL输入', url)
    return FALLBACK_IMAGE
  }
  
  const u = url.trim()
  if (!u) {
    console.warn('normalizeUrl: 空URL')
    return FALLBACK_IMAGE
  }
  
  // 2. Base64图片直接返回
  if (u.startsWith('data:image/')) {
    return u
  }
  
  // 3. 完整URL：若是 localhost/127.0.0.1 则提取路径走 Vite 代理，否则直接返回
  if (u.startsWith('http://') || u.startsWith('https://')) {
    try {
      const parsed = new URL(u)
      const host = parsed.hostname
      if (host === 'localhost' || host === '127.0.0.1' || host === '0.0.0.0') {
        // 提取路径部分，交给 Vite 代理转发
        return parsed.pathname
      }
    } catch {}
    return u
  }

  // 4. 上传静态资源：后端挂载在站点根路径 `/uploads/**`
  // 目标：任何 `/uploads/...` 或 `uploads/...` 都直接走站点根路径
  if (u === '/uploads' || u.startsWith('/uploads/')) {
    return u
  }
  if (u === 'uploads' || u.startsWith('uploads/')) {
    return `/${u}`
  }

  // 5. 其他相对路径，拼接 CDN 或 API base URL
  const base = CDN_BASE_URL || API_BASE_URL
  if (u.startsWith('/')) {
    return `${base}${u}`
  }
  return `${base}/${u}`
}

/**
 * 标准化图片列表
 * @param {string|Array} input - 图片URL字符串（逗号分隔或JSON数组）或数组
 * @returns {Array<string>} - 标准化后的URL数组
 */
export function normalizeList(input) {
  const set = new Set()
  
  if (!input) {
    return []
  }
  
  // 如果已经是数组
  if (Array.isArray(input)) {
    input.forEach(i => { 
      if (i && typeof i === 'string' && i.trim()) {
        const normalized = normalizeUrl(i)
        if (normalized !== FALLBACK_IMAGE) {
          set.add(normalized)
        }
      }
    })
    return Array.from(set)
  }
  
  // 如果是字符串
  const s = String(input).trim()
  if (!s) {
    return []
  }
  
  // 尝试解析JSON数组
  try {
    if (s.startsWith('[') && s.endsWith(']')) {
      const arr = JSON.parse(s)
      if (Array.isArray(arr)) {
        arr.forEach(i => { 
          if (i && String(i).trim()) {
            const normalized = normalizeUrl(String(i))
            if (normalized !== FALLBACK_IMAGE) {
              set.add(normalized)
            }
          }
        })
        return Array.from(set)
      }
    }
  } catch (e) {
    console.warn('normalizeList: JSON解析失败，尝试逗号分隔', e)
  }
  
  // 逗号分隔的字符串
  s.split(',').forEach(i => { 
    if (i && i.trim()) {
      const normalized = normalizeUrl(i)
      if (normalized !== FALLBACK_IMAGE) {
        set.add(normalized)
      }
    }
  })
  
  return Array.from(set)
}

/**
 * 构建srcset（用于响应式图片）
 * @param {string} url - 图片URL
 * @returns {string} - srcset字符串
 */
export function buildSrcSet(url) {
  const u = normalizeUrl(url)
  // 简单 srcset（如果有 CDN，可在服务端按参数生成多规格图）
  return `${u} 1x, ${u} 2x`
}
