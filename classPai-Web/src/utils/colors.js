const COLORS = ['#667eea', '#764ba2', '#f093fb', '#4facfe', '#43e97b', '#fa709a', '#fee140', '#30cfd0']

/** 根据 ID 取色，用于课程封面/头像 */
export function coverColor(id) {
  return COLORS[(id || 0) % COLORS.length]
}
