const COLORS = ['#667eea', '#764ba2', '#f093fb', '#4facfe', '#43e97b', '#fa709a', '#fee140', '#30cfd0']

/** 根据 ID 取色，用于课程封面/头像 */
export function coverColor(id) {
  return COLORS[(id || 0) % COLORS.length]
}

const COVER_IMAGES = [
  '/courseBG/01.jpg',
  '/courseBG/02.jpg',
  '/courseBG/03.jpg',
  '/courseBG/04.jpg'
]

/** 根据课程ID返回随机封面图片（乘法哈希确保分布均匀） */
export function coverImage(id) {
  const hash = ((id * 2654435761) >>> 0) % COVER_IMAGES.length
  return COVER_IMAGES[hash]
}
