const BASE_URL = '/api'

function getToken() {
  return sessionStorage.getItem('token')
}

async function request(url, options = {}) {
  const token = getToken()
  const headers = {}
  if (token) {
    headers['Authorization'] = 'Bearer ' + token
  }
  // FormData 不设置 Content-Type，浏览器会自动设置 multipart/form-data + boundary
  if (!(options.body instanceof FormData)) {
    headers['Content-Type'] = 'application/json'
  }
  const config = {
    headers,
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
  /** 获取单个课程详情 */
  getCourseDetail(courseId) {
    return request(`/course/${courseId}`)
  },
  /** 获取课程所有成员 */
  getCourseMembers(courseId) {
    return request(`/course/${courseId}/members`)
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

  // ========== 课程详情页各 Tab（预留，后端开发后启用） ==========
  getCourseChapters(courseId) {
    return request(`/course/${courseId}/chapters`)
  },
  getCourseCourseware(courseId) {
    return request(`/course/${courseId}/courseware`)
  },
  getCourseHomeworks(courseId) {
    return request(`/course/${courseId}/homeworks`)
  },
  getCourseTests(courseId) {
    return request(`/course/${courseId}/tests`)
  },
  getCourseMaterials(courseId) {
    return request(`/course/${courseId}/materials`)
  },
  getCourseNotices(courseId) {
    return request(`/course/${courseId}/notices`)
  },
  getCourseTopics(courseId) {
    return request(`/course/${courseId}/topics`)
  },

  // ========== 作业 ==========
  /** 获取课程作业列表 */
  getHomeworkList(courseId, page = 1, pageSize = 10) {
    return request(`/homework/course/${courseId}?page=${page}&pageSize=${pageSize}`)
  },
  /** 获取单个作业 */
  getHomework(hwId) {
    return request(`/homework/${hwId}`)
  },
  /** 获取某个作业的文件列表 */
  getHomeworkFiles(hwId) {
    return request(`/homework/${hwId}/files`)
  },
  /** 教师批阅页：获取作业所有学生提交状态 */
  getHomeworkGrading(hwId) {
    return request(`/homework/${hwId}/grading`)
  },
  /** 学生端：获取作业详情（含提交记录） */
  getHomeworkStudentDetail(hwId) {
    return request(`/homework/${hwId}/student`)
  },
  /** 学生端：获取提交页数据 */
  getSubmitPageData(hwId) {
    return request(`/homework/${hwId}/submit-page`)
  },
  /** 教师端：发布作业（支持文件上传） */
  createHomework(courseId, fd) {
    return request(`/homework/course/${courseId}`, {
      method: 'POST',
      body: fd
    })
  },
  /** 教师端：批阅打分 */
  gradeHomework(submitId, body) {
    return request(`/homework/submission/${submitId}/grade`, {
      method: 'PUT',
      body: JSON.stringify(body)
    })
  },
  /** 学生端：提交作业（支持文件上传） */
  submitHomework(hwId, content, files) {
    const formData = new FormData()
    if (content) formData.append('content', content)
    if (files && files.length) {
      files.forEach(f => formData.append('files', f))
    }
    return request(`/homework/${hwId}/submit`, {
      method: 'POST',
      body: formData
    })
  },
  // ========== 课程功能按钮（预留） ==========
  getCourseAttendance(courseId) {
    return request(`/course/${courseId}/attendance`)
  },
  getCoursePerformance(courseId) {
    return request(`/course/${courseId}/performance`)
  },
  getCourseGrades(courseId) {
    return request(`/course/${courseId}/grades`)
  },
  getCourseAnalysis(courseId) {
    return request(`/course/${courseId}/analysis`)
  },
  getCourseErrors(courseId) {
    return request(`/course/${courseId}/errors`)
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
