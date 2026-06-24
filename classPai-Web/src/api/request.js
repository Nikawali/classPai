const BASE_URL = '/api'

async function request(url, options = {}) {
  const config = {
    headers: { 'Content-Type': 'application/json' },
    ...options
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

  // ========== 课堂 ==========
  /** 置顶课程 */
  getTopCourses() {
    return request('/classroom/top')
  },
  /** 我学的课程（按学期） */
  getMyLearningCourses(semester) {
    return request(`/classroom/my-learning?semester=${encodeURIComponent(semester)}`)
  },
  /** 我协助的课程（按学期） */
  getMyAssistingCourses(semester) {
    return request(`/classroom/my-assisting?semester=${encodeURIComponent(semester)}`)
  },
  /** 加入课程 */
  joinCourse(code) {
    return request('/classroom/join', {
      method: 'POST',
      body: JSON.stringify({ code })
    })
  },
  /** 创建课程（教师） */
  createCourse(body) {
    return request('/classroom/create', {
      method: 'POST',
      body: JSON.stringify(body)
    })
  },
  /** 搜索课程 */
  searchCourses(keyword) {
    return request(`/classroom/search?keyword=${encodeURIComponent(keyword)}`)
  }
}
