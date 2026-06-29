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
  if (!res.ok) {
    throw new Error(`请求失败 (${res.status})`)
  }
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
  if (!res.ok) {
    throw new Error(`请求失败 (${res.status})`)
  }
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
  /** 修改成员角色 */
  changeMemberRole(courseId, userId, role) {
    return request(`/course/${courseId}/members/${userId}/role?role=${role}`, {
      method: 'PUT'
    })
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

  // ========== 退课 ==========
  quitCourse(courseId) {
    return request(`/course/${courseId}/quit`, {
      method: 'DELETE'
    })
  },

  // ========== 归档 ==========
  getArchivedCourses() {
    return request('/course/archived')
  },
  archiveCourse(courseId) {
    return request(`/course/${courseId}/archive`, {
      method: 'POST'
    })
  },
  unarchiveCourse(courseId) {
    return request(`/course/${courseId}/unarchive`, {
      method: 'POST'
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

  // ========== 资料 ==========
  /** 列出资料（可指定 parentId，null=根目录） */
  getMaterials(courseId, parentId = null) {
    const params = parentId != null ? `?parentId=${parentId}` : ''
    return request(`/material/course/${courseId}${params}`)
  },
  /** 创建文件夹 */
  createMaterialFolder(courseId, name, parentId = null) {
    const fd = new FormData()
    fd.append('name', name)
    if (parentId != null) fd.append('parentId', parentId)
    return request(`/material/course/${courseId}/folder`, { method: 'POST', body: fd })
  },
  /** 上传附件 */
  uploadMaterialFile(courseId, files, parentId = null) {
    const fd = new FormData()
    for (const f of files) fd.append('files', f)
    if (parentId != null) fd.append('parentId', parentId)
    return request(`/material/course/${courseId}/file`, { method: 'POST', body: fd })
  },
  /** 添加外链 */
  addMaterialLink(courseId, name, url, parentId = null) {
    const fd = new FormData()
    fd.append('name', name)
    fd.append('url', url)
    if (parentId != null) fd.append('parentId', parentId)
    return request(`/material/course/${courseId}/link`, { method: 'POST', body: fd })
  },
  /** 删除资料（包含文件夹级联删除） */
  deleteMaterial(materialId) {
    return request(`/material/${materialId}`, { method: 'DELETE' })
  },
  /** 移动资料 */
  moveMaterial(materialId, targetParentId) {
    const fd = new FormData()
    if (targetParentId != null) fd.append('targetParentId', targetParentId)
    return request(`/material/${materialId}/move`, { method: 'PUT', body: fd })
  },
  /** 重命名 */
  renameMaterial(materialId, name) {
    const fd = new FormData()
    fd.append('name', name)
    return request(`/material/${materialId}/rename`, { method: 'PUT', body: fd })
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
  /** 教师端：获取作业详情（含文件，不含学生提交） */
  getHomeworkTeacherDetail(hwId) {
    return request(`/homework/${hwId}/detail`)
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
  /** 教师催交单个学生 */
  urgeHomework(hwId, studentId) {
    return request(`/homework/${hwId}/urge/${studentId}`, { method: 'POST' })
  },
  /** 教师一键催交所有未提交学生 */
  urgeAllHomework(hwId) {
    return request(`/homework/${hwId}/urge-all`, { method: 'POST' })
  },
  /** 教师 AI 一键批改 */
  gradeAIBatch(hwId) {
    return request(`/homework/${hwId}/grade-ai-batch`, { method: 'POST' })
  },
  /** 教师修改作业时间 */
  updateHomeworkTime(hwId, startTime, deadline) {
    return request(`/homework/${hwId}/time?startTime=${startTime || ''}&deadline=${deadline || ''}`, {
      method: 'PUT'
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
  },

  // ========== 私信 ==========
  getMessages() {
    return request('/message')
  },
  getUnreadMessageCount() {
    return request('/message/unread-count')
  },
  markMessagesRead() {
    return request('/message/read', { method: 'POST' })
  },

  // ========== 话题讨论 ==========
  getTopics(courseId) {
    return request(`/topic/course/${courseId}`)
  },
  createTopic(courseId, title, content) {
    const fd = new FormData()
    fd.append('title', title)
    if (content) fd.append('content', content)
    return request(`/topic/course/${courseId}`, { method: 'POST', body: fd })
  },
  updateTopic(topicId, title, content) {
    const fd = new FormData()
    if (title) fd.append('title', title)
    if (content !== undefined) fd.append('content', content)
    return request(`/topic/${topicId}`, { method: 'PUT', body: fd })
  },
  deleteTopic(topicId) {
    return request(`/topic/${topicId}`, { method: 'DELETE' })
  },
  togglePinTopic(topicId) {
    return request(`/topic/${topicId}/pin`, { method: 'PUT' })
  },
  replyTopic(topicId, content, isAnonymous) {
    const fd = new FormData()
    fd.append('content', content)
    fd.append('isAnonymous', isAnonymous ? 'true' : 'false')
    return request(`/topic/${topicId}/reply`, { method: 'POST', body: fd })
  },
  getReplies(topicId) {
    return request(`/topic/${topicId}/replies`)
  },
  toggleDiscussion(courseId) {
    return request(`/topic/course/${courseId}/toggle`, { method: 'PUT' })
  },
}
