// IMPORTANT:
// This locale file intentionally uses ONLY ASCII characters (via Unicode escapes)
// to avoid any build/runtime encoding issues on Windows environments that may turn
// CJK characters into '?' during resource processing.

import zhCNLocale from 'element-plus/dist/locale/zh-cn.mjs'

// Deep clone enough for our override needs (locale is a plain object)
const safe = {
  ...zhCNLocale,
  el: {
    ...(zhCNLocale.el || {}),
    pagination: {
      ...((zhCNLocale.el && zhCNLocale.el.pagination) || {}),
      // 上一页 / 下一页 / 前往 / 页 / 条/页
      prev: '\u4e0a\u4e00\u9875',
      next: '\u4e0b\u4e00\u9875',
      goto: '\u524d\u5f80',
      pagesize: '\u6761/\u9875',
      total: '\u5171 {total} \u6761',
      pageClassifier: '\u9875'
    }
  }
}

export default safe

