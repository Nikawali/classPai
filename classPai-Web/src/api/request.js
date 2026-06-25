const BASE_URL = '/api'

function getToken() {
  return sessionStorage.getItem('token')
}

async function request(url, options = {}) {
  const token = getToken()
  const config = {
    headers: { 'Content-Type': 'application/json' },
    ...options
  }
  if (token) {
    config.headers['Authorization'] = 'Bearer ' + token
  }
  const res = await fetch(BASE_URL + url, config)
  const data = await res.json()
  if (data.code !== 200) {
    throw new Error(data.message || '请求失败')
  }
  return data
}

/** 独立登录接口：不走 /api 前缀，直接请求 /auth/login */
async function authRequest(url, options = {}) {
  const config = {
    headers: { 'Content-Type': 'application/json' },
    ...options
  }
  const res = await fetch(url, config)
  const data = await res.json()
  if (data.code !== 200) {
    throw new Error(data.message || '请求失败')
  }
  return data
}

export const api = {
  /** 登录 */
  login(body) {
    return authRequest('/auth/login', {
      method: 'POST',
      body: JSON.stringify(body)
    })
  },

  /** 获取当前用户信息 */
  getProfile(token) {
    return authRequest('/auth/me', {
      method: 'GET',
      headers: {
        'Authorization': 'Bearer ' + token,
        'Content-Type': 'application/json'
      }
    })
  },

  sendCode(phone) {
    return request(`/user/send-code?phone=${phone}`)
  },
  register(body) {
    return request('/user/register', {
      method: 'POST',
      body: JSON.stringify(body)
    })
  },
  getSchools() {
    return request('/school/list')
  },

  // ========== 课程 ==========
  createCourse(body) {
    return request('/course', {
      method: 'POST',
      body: JSON.stringify(body)
    })
  },
  joinCourse(courseCode) {
    return request(`/course/join?courseCode=${encodeURIComponent(courseCode)}`, {
      method: 'POST'
    })
  },

  // ========== 课程数据（一次获取全部） ==========
  getAllCourses() {
    return request('/course/all')
  },

  // ========== 置顶 ==========
  togglePin(courseId) {
    return request(`/course/${courseId}/pin`, {
      method: 'POST'
    })
  },
  updatePinnedOrder(courseIds) {
    return request('/course/pinned/order', {
      method: 'PUT',
      body: JSON.stringify(courseIds)
    })
  },

  // ========== 搜索 ==========
  searchCourses(keyword) {
    return request(`/course/search?keyword=${encodeURIComponent(keyword)}`)
  },

  // ========== 个人信息修改 ==========
  sendCodeForProfile(token, phone) {
    return request(`/user/send-code-profile?phone=${phone}`, {
      headers: {
        'Authorization': 'Bearer ' + token
      }
    })
  },
  updateProfile(token, body) {
    return request('/user/profile', {
      method: 'PUT',
      headers: {
        'Authorization': 'Bearer ' + token,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(body)
    })
  }
}
