<template>
  <div class="classroom-page">
    <!-- ==================== 置顶课程区 ==================== -->
    <div class="top-section">
      <div class="top-header">
        <h2 class="top-title">置顶课程</h2>
        <div class="btn-action-wrapper">
          <button class="btn-action" @click="showActionSheet = !showActionSheet">
            <svg viewBox="0 0 24 24" width="16" height="16" class="btn-action-icon">
              <line x1="12" y1="5" x2="12" y2="19" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <line x1="5" y1="12" x2="19" y2="12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            加入/创建课程
          </button>
          <!-- 下拉菜单 -->
          <div v-if="showActionSheet" class="action-sheet">
            <button class="action-sheet-item" @click="showJoinDialog = true; showActionSheet = false">
              <svg viewBox="0 0 24 24" width="18" height="18">
                <path d="M15 3h6v6M9 21H3v-6M21 3l-7 7M3 21l7-7" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              加入课堂
            </button>
            <button class="action-sheet-item" @click="showCreateDialog = true; showActionSheet = false">
              <svg viewBox="0 0 24 24" width="18" height="18">
                <rect x="3" y="3" width="18" height="18" rx="2" fill="none" stroke="currentColor" stroke-width="2"/>
                <line x1="9" y1="9" x2="9" y2="15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <line x1="12" y1="12" x2="12" y2="12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <line x1="12" y1="9" x2="12" y2="15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              创建课堂
            </button>
          </div>
        </div>
      </div>

      <!-- 横向滚动卡片（后端填充） -->
      <div class="top-cards-scroll">
        <div
          v-for="course in topCourses"
          :key="course.id"
          class="top-card"
          @click="enterCourse(course)"
        >
          <span class="top-card-tag">{{ isTeacher ? '教' : '学' }}</span>
          <div class="top-card-cover" :style="{ background: course.coverColor }"></div>
          <div class="top-card-body">
            <span class="top-card-code">{{ course.inviteCode }}</span>
            <p class="top-card-name">{{ course.courseName }}</p>
            <p class="top-card-class">{{ course.className }}</p>
          </div>
        </div>

        <div v-if="topCourses.length === 0" class="top-empty">
          <p>暂无置顶课程，点击右上角加入/创建课程</p>
        </div>
      </div>
    </div>

    <!-- ==================== 切换栏 ==================== -->
    <div class="tab-bar">
      <div v-if="isTeacher" class="tab-tabs">
        <span class="tab-btn active">我的课程</span>
      </div>
      <div v-else class="tab-tabs">
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'learning' }"
          @click="activeTab = 'learning'"
        >我学的</button>
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'assisting' }"
          @click="activeTab = 'assisting'"
        >我协助的</button>
      </div>
      <div class="tab-actions">
        <button class="tab-icon-btn" @click="showSearch = !showSearch">
          <svg viewBox="0 0 24 24" width="18" height="18">
            <circle cx="11" cy="11" r="7" fill="none" stroke="currentColor" stroke-width="2"/>
            <line x1="16.5" y1="16.5" x2="21" y2="21" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </button>
        <button class="tab-icon-btn">
          <svg viewBox="0 0 24 24" width="18" height="18">
            <circle cx="12" cy="5" r="1.5" fill="currentColor"/>
            <circle cx="12" cy="12" r="1.5" fill="currentColor"/>
            <circle cx="12" cy="19" r="1.5" fill="currentColor"/>
          </svg>
        </button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div v-if="showSearch" class="search-bar">
      <svg viewBox="0 0 24 24" width="16" height="16" class="search-icon">
        <circle cx="11" cy="11" r="7" fill="none" stroke="currentColor" stroke-width="2"/>
        <line x1="16.5" y1="16.5" x2="21" y2="21" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
      </svg>
      <input
        v-model="searchKeyword"
        type="text"
        placeholder="搜索课程名称"
        class="search-input"
        @keyup.enter="handleSearch"
      />
    </div>

    <!-- ==================== 学期折叠列表（后端填充） ==================== -->
    <div class="semester-list">
      <div
        v-for="semester in semesterData"
        :key="semester.name"
        class="semester-group"
      >
        <div class="semester-header" @click="toggleSemester(semester.name)">
          <svg
            class="semester-arrow"
            :class="{ expanded: semester.expanded }"
            viewBox="0 0 24 24" width="16" height="16"
          >
            <path d="M8 6l8 6-8 6" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <span class="semester-name">{{ semester.name }}</span>
          <span class="semester-count">{{ semester.courses.length }}门课程</span>
        </div>

        <div v-show="semester.expanded" class="semester-courses">
          <div
            v-for="course in semester.courses"
            :key="course.id"
            class="course-item"
            @click="enterCourse(course)"
          >
            <div class="course-thumb" :style="{ background: course.coverColor }">
              <span class="course-thumb-text">{{ course.courseName.charAt(0) }}</span>
            </div>
            <div class="course-info">
              <p class="course-name">{{ course.courseName }}</p>
              <p class="course-meta">
                <span>{{ course.className }}</span>
                <span class="meta-sep">·</span>
                <span>{{ course.studentCount }}人</span>
              </p>
            </div>
            <button class="course-menu-btn" @click.stop="handleCourseMenu(course)">
              <svg viewBox="0 0 24 24" width="18" height="18">
                <circle cx="12" cy="5" r="1.5" fill="#999"/>
                <circle cx="12" cy="12" r="1.5" fill="#999"/>
                <circle cx="12" cy="19" r="1.5" fill="#999"/>
              </svg>
            </button>
          </div>

          <div v-if="semester.courses.length === 0" class="course-empty">
            <p>暂无课程</p>
          </div>
        </div>
      </div>
    </div>

    <!-- ==================== 加入课程弹窗 ==================== -->
    <div v-if="showJoinDialog" class="dialog-overlay" @click.self="showJoinDialog = false">
      <div class="dialog-card">
        <h3 class="dialog-title">加入课程</h3>
        <p class="dialog-desc">输入老师分享的课程邀请码</p>
        <input v-model="joinCode" type="text" placeholder="请输入邀请码"
          class="dialog-input" @keyup.enter="handleJoinCourse" />
        <div class="dialog-actions">
          <button class="dialog-btn-cancel" @click="showJoinDialog = false">取消</button>
          <button class="dialog-btn-confirm" @click="handleJoinCourse" :disabled="!joinCode.trim()">加入</button>
        </div>
        <p v-if="joinError" class="dialog-error">{{ joinError }}</p>
      </div>
    </div>

    <!-- ==================== 创建课程弹窗 ==================== -->
    <div v-if="showCreateDialog" class="dialog-overlay" @click.self="showCreateDialog = false">
      <div class="dialog-card">
        <h3 class="dialog-title">创建课程</h3>
        <p class="dialog-desc">填写课程基本信息</p>
        <input v-model="createForm.courseName" type="text" placeholder="课程名称"
          class="dialog-input" style="margin-bottom:10px" />
        <input v-model="createForm.className" type="text" placeholder="班级名称（选填）"
          class="dialog-input" />
        <div class="dialog-actions">
          <button class="dialog-btn-cancel" @click="showCreateDialog = false">取消</button>
          <button class="dialog-btn-confirm" @click="handleCreateCourse" :disabled="!createForm.courseName.trim()">创建</button>
        </div>
        <p v-if="createError" class="dialog-error">{{ createError }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { api } from '../api/request.js'

// ==================== 角色 ====================
const userRole = ref(sessionStorage.getItem('role') || 'STUDENT')
const isTeacher = computed(() => userRole.value === 'TEACHER')

// ==================== 状态 ====================
const activeTab = ref('learning')
const showSearch = ref(false)
const searchKeyword = ref('')
const topCourses = ref([])
const semesterData = ref([])

// 加入课程弹窗
const showJoinDialog = ref(false)
const joinCode = ref('')
const joinError = ref('')

// 创建课程弹窗
const showCreateDialog = ref(false)
const createForm = ref({ courseName: '', className: '' })
const createError = ref('')

// 加入/创建 下拉菜单
const showActionSheet = ref(false)

// ==================== 方法 ====================
function toggleSemester(name) {
  const sem = semesterData.value.find(s => s.name === name)
  if (sem) sem.expanded = !sem.expanded
}

function enterCourse(course) {
  // TODO：跳转到课程详情页
}

function handleCourseMenu(course) {
  // TODO：弹出操作菜单
}

async function handleJoinCourse() {
  if (!joinCode.value.trim()) return
  joinError.value = ''
  try {
    await api.joinCourse(joinCode.value)
    showJoinDialog.value = false
    joinCode.value = ''
    loadCourses()
  } catch (e) {
    joinError.value = e.message
  }
}

async function handleCreateCourse() {
  if (!createForm.value.courseName.trim()) return
  createError.value = ''
  try {
    await api.createCourse({
      courseName: createForm.value.courseName.trim(),
      className: createForm.value.className.trim()
    })
    showCreateDialog.value = false
    createForm.value = { courseName: '', className: '' }
    loadCourses()
  } catch (e) {
    createError.value = e.message
  }
}

function handleSearch() {
  const kw = searchKeyword.value.trim()
  if (!kw) return
  api.searchCourses(kw).then(res => {
    // TODO：展示搜索结果
  }).catch(e => {
    console.error('搜索失败:', e)
  })
}

// ==================== 数据加载 ====================
async function loadCourses() {
  try {
    if (isTeacher.value) {
      const [topRes, listRes] = await Promise.all([
        api.getTopCourses(),
        api.getMyAssistingCourses()
      ])
      topCourses.value = topRes.data || []
      semesterData.value = listRes.data || []
    } else {
      const [topRes, listRes] = await Promise.all([
        api.getTopCourses(),
        api.getMyLearningCourses()
      ])
      topCourses.value = topRes.data || []
      semesterData.value = listRes.data || []
    }
  } catch (e) {
    console.error('加载课程失败:', e)
  }
}

onMounted(() => {
  loadCourses()
})
</script>

<style scoped>
/* ==================== 页面容器 ==================== */
.classroom-page {
  width: 100%;
  max-width: 640px;
  margin: 0 auto;
  padding: 20px 16px;
  min-height: 100%;
  background: #f5f6fa;
}

/* ==================== 置顶课程区 ==================== */
.top-section {
  margin-bottom: 24px;
}

.top-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.top-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.btn-action {
  display: flex;
  align-items: center;
  gap: 4px;
  height: 34px;
  padding: 0 14px;
  border: none;
  border-radius: 8px;
  background: #4a6cf7;
  color: #fff;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity .15s;
}
.btn-action:hover { opacity: .9; }
.btn-action-icon { flex-shrink: 0; }

/* 下拉菜单容器 */
.btn-action-wrapper {
  position: relative;
}

.action-sheet {
  position: absolute;
  right: 0;
  top: 38px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 4px 20px rgba(0,0,0,.12);
  padding: 6px;
  z-index: 50;
  min-width: 150px;
}

.action-sheet-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 10px 14px;
  border: none;
  border-radius: 8px;
  background: transparent;
  font-size: 14px;
  color: #333;
  cursor: pointer;
  white-space: nowrap;
  transition: background .15s;
}
.action-sheet-item:hover { background: #f5f6fa; }

/* 横向滚动卡片 */
.top-cards-scroll {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding-bottom: 4px;
  scroll-snap-type: x mandatory;
  -webkit-overflow-scrolling: touch;
}
.top-cards-scroll::-webkit-scrollbar { display: none; }

.top-card {
  flex-shrink: 0;
  width: 154px;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  scroll-snap-align: start;
  position: relative;
  transition: box-shadow .15s;
}
.top-card:hover { box-shadow: 0 2px 12px rgba(0,0,0,.08); }
.top-card:active { transform: scale(.98); }

.top-card-tag {
  position: absolute;
  top: 8px;
  left: 8px;
  font-size: 11px;
  color: #4a6cf7;
  background: rgba(74,108,247,.12);
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 500;
  z-index: 1;
}

.top-card-cover {
  width: 100%;
  height: 88px;
}

.top-card-body {
  padding: 10px 10px 12px;
}
.top-card-code {
  font-size: 11px;
  color: #aaa;
  display: block;
  margin-bottom: 4px;
}
.top-card-name {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 2px;
}
.top-card-class {
  font-size: 12px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.top-empty {
  width: 100%;
  text-align: center;
  padding: 28px 0;
  color: #bbb;
  font-size: 13px;
}

/* ==================== 切换栏 ==================== */
.tab-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
  padding: 0 2px;
}

.tab-tabs {
  display: flex;
  gap: 4px;
  background: #eef0f5;
  border-radius: 8px;
  padding: 3px;
}

.tab-btn {
  height: 32px;
  padding: 0 16px;
  border: none;
  border-radius: 6px;
  background: transparent;
  font-size: 13px;
  color: #888;
  cursor: pointer;
  transition: all .2s;
}
.tab-btn.active {
  background: #fff;
  color: #1a1a1a;
  font-weight: 500;
  box-shadow: 0 1px 3px rgba(0,0,0,.08);
}

.tab-actions {
  display: flex;
  gap: 6px;
}

.tab-icon-btn {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 8px;
  background: #fff;
  color: #888;
  cursor: pointer;
  transition: color .15s;
}
.tab-icon-btn:hover { color: #4a6cf7; }

/* 搜索栏 */
.search-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 12px;
  height: 40px;
  background: #fff;
  border-radius: 10px;
  margin-bottom: 16px;
}
.search-icon { flex-shrink: 0; color: #bbb; }
.search-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 13px;
  color: #333;
  background: transparent;
}
.search-input::placeholder { color: #bbb; }

/* ==================== 学期列表 ==================== */
.semester-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.semester-group {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}

.semester-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 16px;
  cursor: pointer;
  user-select: none;
  transition: background .15s;
}
.semester-header:hover { background: #fafafa; }
.semester-header:active { background: #f5f5f5; }

.semester-arrow {
  flex-shrink: 0;
  color: #bbb;
  transition: transform .2s;
}
.semester-arrow.expanded { transform: rotate(90deg); }

.semester-name {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
  flex: 1;
}

.semester-count {
  font-size: 12px;
  color: #aaa;
}

/* 课程条目 */
.semester-courses {
  border-top: 1px solid #f0f0f0;
}

.course-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background .1s;
}
.course-item:hover { background: #fafafa; }
.course-item:active { background: #f5f5f5; }
.course-item + .course-item {
  border-top: 1px solid #f5f5f5;
}

.course-thumb {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.course-thumb-text {
  font-size: 16px;
  font-weight: 700;
  color: #fff;
}

.course-info { flex: 1; min-width: 0; }
.course-name {
  font-size: 15px;
  font-weight: 500;
  color: #1a1a1a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 4px;
}
.course-meta { font-size: 12px; color: #999; }
.meta-sep { margin: 0 6px; color: #ddd; }

.course-menu-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 6px;
  background: transparent;
  cursor: pointer;
  flex-shrink: 0;
  transition: background .15s;
}
.course-menu-btn:hover { background: #f0f0f0; }
.course-menu-btn:active { background: #e8e8e8; }

.course-empty {
  text-align: center;
  padding: 24px 0;
  color: #ccc;
  font-size: 13px;
}

/* ==================== 弹窗 ==================== */
.dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  padding: 20px;
}

.dialog-card {
  width: 320px;
  max-width: 100%;
  background: #fff;
  border-radius: 14px;
  padding: 28px 24px 20px;
  text-align: center;
}

.dialog-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 6px;
}

.dialog-desc {
  font-size: 13px;
  color: #999;
  margin-bottom: 16px;
}

.dialog-input {
  width: 100%;
  height: 42px;
  border: 1.5px solid #e0e0e0;
  border-radius: 8px;
  padding: 0 12px;
  font-size: 14px;
  outline: none;
  box-sizing: border-box;
  text-align: center;
  letter-spacing: 2px;
  transition: border-color .2s;
}
.dialog-input:focus { border-color: #4a6cf7; }
.dialog-input::placeholder { color: #ccc; letter-spacing: 0; }

.dialog-actions {
  display: flex;
  gap: 10px;
  margin-top: 18px;
}

.dialog-btn-cancel,
.dialog-btn-confirm {
  flex: 1;
  height: 40px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  font-weight: 500;
  transition: opacity .15s;
}
.dialog-btn-cancel:active,
.dialog-btn-confirm:active { opacity: .85; }

.dialog-btn-cancel {
  background: #f0f0f0;
  color: #666;
}

.dialog-btn-confirm {
  background: #4a6cf7;
  color: #fff;
}
.dialog-btn-confirm:disabled {
  opacity: .5;
  cursor: not-allowed;
}

.dialog-error {
  margin-top: 12px;
  font-size: 12px;
  color: #e74c3c;
}

/* ==================== 响应式 ==================== */
@media (max-width: 480px) {
  .classroom-page {
    padding: 14px 12px;
  }
  .top-title { font-size: 16px; }
  .top-card { width: 136px; }
  .top-card-cover { height: 76px; }
  .dialog-card { padding: 24px 18px 18px; }
}
</style>
