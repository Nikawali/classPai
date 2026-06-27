/**
 * 共享格式化工具函数
 */

/** 日期时间格式化（完整） */
export function fmt(v) {
  if (!v) return '--'
  if (typeof v === 'number') return new Date(v * 1000).toLocaleString('zh-CN')
  return String(v).replace('T', ' ').substring(0, 16)
}

/** 日期时间格式化（短格式：MM/DD HH:mm） */
export function fmtShort(v) {
  if (!v) return '--'
  if (typeof v === 'number') return new Date(v * 1000).toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
  return String(v).replace('T', ' ').substring(5, 16)
}

/** 截止日期格式化：M月D日 HH:mm */
export function fmtDeadline(v) {
  if (!v) return '无截止日期'
  const d = new Date(v)
  const month = d.getMonth() + 1
  const day = d.getDate()
  const hour = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${month}月${day}日 ${hour}:${min}`
}

/** 文件大小格式化 */
export function fmtSize(b) {
  if (!b) return ''
  if (b < 1024) return b + ' B'
  if (b < 1048576) return (b / 1024).toFixed(1) + ' KB'
  return (b / 1048576).toFixed(1) + ' MB'
}

/** 文件类型 → emoji 图标 */
export function fileIcon(name) {
  const ext = (name || '').split('.').pop()?.toLowerCase()
  if (['jpg', 'jpeg', 'png', 'gif', 'svg'].includes(ext)) return '\uD83D\uDDBC' // 🖼
  if (['pdf'].includes(ext)) return '\uD83D\uDCC4' // 📄
  if (['zip', 'rar', '7z', 'gz'].includes(ext)) return '\uD83D\uDDDC' // 🗜
  if (['doc', 'docx'].includes(ext)) return '\uD83D\uDCDD' // 📝
  return '\uD83D\uDCCE' // 📎
}
