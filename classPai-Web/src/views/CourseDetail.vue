<template>
  <div class="course-detail-page" v-if="course">
    <!-- ==================== 顶部 Banner ==================== -->
    <div class="banner" :style="{ background: bannerColor }">
      <!-- 渐变红底 + 装饰叶片 -->
      <div class="banner-bg">
        <svg class="banner-leaf leaf-1" viewBox="0 0 120 160" fill="none">
          <path d="M60 0 C30 40 0 80 10 130 C20 160 50 160 60 130 C70 160 100 160 110 130 C120 80 90 40 60 0Z" fill="rgba(255,255,255,.08)"/>
          <line x1="60" y1="10" x2="60" y2="140" stroke="rgba(255,255,255,.06)" stroke-width="1"/>
          <path d="M60 140 C60 100 40 60 10 40" fill="none" stroke="rgba(255,255,255,.05)" stroke-width="1"/>
          <path d="M60 140 C60 100 80 60 110 40" fill="none" stroke="rgba(255,255,255,.05)" stroke-width="1"/>
        </svg>
        <svg class="banner-leaf leaf-2" viewBox="0 0 100 140" fill="none">
          <path d="M50 10 C25 45 5 80 12 120 C18 140 40 140 50 115 C60 140 85 140 90 120 C98 80 75 45 50 10Z" fill="rgba(255,255,255,.06)"/>
        </svg>
      </div>

      <!-- 返回按钮 -->
      <button class="banner-back" @click="$emit('back')">
        <svg viewBox="0 0 24 24" width="20" height="20">
          <path d="M15 18l-6-6 6-6" fill="none" stroke="#fff" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </button>

      <!-- 左侧信息 -->
      <div class="banner-main">
        <p class="banner-class">{{ classText }}</p>
        <h1 class="banner-title">{{ course.courseName }}</h1>
        <div class="banner-code-row">
          <span class="banner-code">加课码 {{ course.courseCode }}</span>
          <button class="banner-code-copy" @click.stop="copyCode">
            <svg viewBox="0 0 24 24" width="14" height="14">
              <rect x="8" y="2" width="12" height="16" rx="2" fill="none" stroke="currentColor" stroke-width="1.5"/>
              <path d="M16 6V4C16 2.9 15.1 2 14 2H4C2.9 2 2 2.9 2 4V16C2 17.1 2.9 18 4 18H6" fill="none" stroke="currentColor" stroke-width="1.5"/>
            </svg>
            复制
          </button>
        </div>
      </div>

      <!-- 右侧统计 -->
      <div class="banner-stats">
        <div class="stat-item">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none">
            <circle cx="12" cy="9" r="3.5" stroke="rgba(255,255,255,.7)" stroke-width="1.5"/>
            <path d="M5 20c0-3.9 3.1-7 7-7s7 3.1 7 7" stroke="rgba(255,255,255,.7)" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <span>{{ course.studentCount || 0 }} 成员</span>
        </div>
        <div class="stat-item">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none">
            <rect x="2" y="3" width="8" height="8" rx="1.5" stroke="rgba(255,255,255,.7)" stroke-width="1.5"/>
            <rect x="14" y="3" width="8" height="8" rx="1.5" stroke="rgba(255,255,255,.7)" stroke-width="1.5"/>
            <rect x="2" y="14" width="8" height="8" rx="1.5" stroke="rgba(255,255,255,.7)" stroke-width="1.5"/>
            <rect x="14" y="14" width="8" height="8" rx="1.5" stroke="rgba(255,255,255,.7)" stroke-width="1.5"/>
          </svg>
          <span>0 分组</span>
        </div>
        <div class="stat-item">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none">
            <circle cx="12" cy="12" r="9" stroke="rgba(255,255,255,.7)" stroke-width="1.5"/>
            <circle cx="9.5" cy="9.5" r="1" fill="rgba(255,255,255,.5)"/>
            <circle cx="14.5" cy="9.5" r="1" fill="rgba(255,255,255,.5)"/>
            <path d="M7 14c.8-1.5 2.8-2.5 5-2.5s4.2 1 5 2.5" stroke="rgba(255,255,255,.7)" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <span>{{ course.courseIntro ? '有' : '暂无' }}介绍</span>
        </div>
      </div>
    </div>

    <!-- ==================== 功能按钮模块 ==================== -->
    <div class="func-buttons">
      <button
        v-for="btn in funcButtons"
        :key="btn.type"
        class="func-btn"
        @click="handleFunc(btn.type)"
      >
        <span class="func-btn-circle">
          <!-- 考勤 -->
          <svg v-if="btn.icon === 'attendance'" viewBox="0 0 24 24" width="20" height="20" fill="none">
            <rect x="3" y="5" width="18" height="14" rx="2" stroke="currentColor" stroke-width="1.5"/>
            <path d="M3 10h18" stroke="currentColor" stroke-width="1.5"/>
            <line x1="8" y1="2" x2="8" y2="7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            <line x1="16" y1="2" x2="16" y2="7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            <circle cx="8.5" cy="13.5" r="1.2" fill="currentColor"/>
            <circle cx="13" cy="13.5" r="1.2" fill="currentColor"/>
            <circle cx="17.5" cy="13.5" r="1.2" fill="currentColor"/>
          </svg>
          <!-- 表现 -->
          <svg v-else-if="btn.icon === 'performance'" viewBox="0 0 24 24" width="20" height="20" fill="none">
            <path d="M11 4H4C2.9 4 2 4.9 2 6v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8l-6-4h-5z" stroke="currentColor" stroke-width="1.5"/>
            <path d="M14 4v5h5" stroke="currentColor" stroke-width="1.5"/>
            <circle cx="8.5" cy="14" r="1.2" fill="currentColor"/>
            <circle cx="12" cy="17" r="1.2" fill="currentColor"/>
            <circle cx="16" cy="12" r="1.2" fill="currentColor"/>
            <line x1="8.5" y1="14" x2="12" y2="17" stroke="currentColor" stroke-width="1.2"/>
            <line x1="12" y1="17" x2="16" y2="12" stroke="currentColor" stroke-width="1.2"/>
          </svg>
          <!-- 成绩 -->
          <svg v-else-if="btn.icon === 'grades'" viewBox="0 0 24 24" width="20" height="20" fill="none">
            <rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="1.5"/>
            <line x1="8" y1="10" x2="16" y2="10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            <line x1="8" y1="14" x2="14" y2="14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            <line x1="8" y1="18" x2="12" y2="18" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            <circle cx="17" cy="11" r="2" stroke="currentColor" stroke-width="1.2" fill="none"/>
            <path d="M16 11l.7 1.4 1.5.2-1.1 1 .3 1.4-1.4-.7-1.4.7.3-1.4-1.1-1 1.5-.2z" fill="currentColor"/>
          </svg>
          <!-- 分析 -->
          <svg v-else-if="btn.icon === 'analysis'" viewBox="0 0 24 24" width="20" height="20" fill="none">
            <path d="M3 3v16c0 1.1.9 2 2 2h15" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            <path d="M7 14l3-5 3 3 4-6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <!-- 错题集 -->
          <svg v-else-if="btn.icon === 'errors'" viewBox="0 0 24 24" width="20" height="20" fill="none">
            <path d="M12 2L2 20h20L12 2z" stroke="currentColor" stroke-width="1.5"/>
            <line x1="12" y1="10" x2="12" y2="14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            <circle cx="12" cy="17.5" r="1" fill="currentColor"/>
            <line x1="9" y1="7" x2="15" y2="7" stroke="currentColor" stroke-width="1" stroke-linecap="round" opacity=".4"/>
            <line x1="8" y1="5" x2="15" y2="5" stroke="currentColor" stroke-width="1" stroke-linecap="round" opacity=".2"/>
          </svg>
          <!-- 课程教学 -->
          <svg v-else-if="btn.icon === 'teaching'" viewBox="0 0 24 24" width="20" height="20" fill="none">
            <path d="M2 3h7c1.1 0 2 .9 2 2v14c0 1.1-.9 2-2 2H2V3z" stroke="currentColor" stroke-width="1.5"/>
            <path d="M10 3h12v18H10" stroke="currentColor" stroke-width="1.5"/>
            <line x1="6" y1="8" x2="8" y2="8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            <line x1="6" y1="12" x2="8" y2="12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            <line x1="14" y1="8" x2="18" y2="8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            <line x1="14" y1="12" x2="18" y2="12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            <line x1="14" y1="16" x2="16" y2="16" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <!-- 课程达成度 -->
          <svg v-else-if="btn.icon === 'achievement'" viewBox="0 0 24 24" width="20" height="20" fill="none">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="1.5"/>
            <circle cx="12" cy="12" r="6" stroke="currentColor" stroke-width="1.5"/>
            <circle cx="12" cy="12" r="2" fill="currentColor"/>
            <path d="M12 2v4M12 18v4M2 12h4M18 12h4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </span>
        <span class="func-btn-label">{{ btn.label }}</span>
      </button>
    </div>

    <!-- ==================== Tab 标签栏 ==================== -->
    <div class="tab-bar-wrapper">
      <div class="tab-bar" ref="tabBarRef">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          class="tab-item"
          :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key"
        >{{ tab.label }}</button>
      </div>
    </div>

    <!-- ==================== 内容区域 ==================== -->
    <div class="content-area">
      <!-- 目录 Tab -->
      <div v-if="activeTab === 'catalog'" class="catalog-panel">
        <div class="catalog-header">
          <h3 class="catalog-title">课程内容</h3>
          <span class="catalog-stats">0个章节 0个活动</span>
        </div>
        <div class="catalog-empty">
          <svg class="catalog-empty-icon" viewBox="0 0 120 120" fill="none">
            <rect x="25" y="20" width="70" height="80" rx="6" stroke="#ddd" stroke-width="2" fill="#f9f9f9"/>
            <line x1="40" y1="42" x2="80" y2="42" stroke="#e0e0e0" stroke-width="2.5" stroke-linecap="round"/>
            <line x1="40" y1="54" x2="70" y2="54" stroke="#e8e8e8" stroke-width="2.5" stroke-linecap="round"/>
            <line x1="40" y1="66" x2="75" y2="66" stroke="#e8e8e8" stroke-width="2.5" stroke-linecap="round"/>
            <circle cx="60" cy="90" r="14" fill="#f0f0f0"/>
            <path d="M56 90h8M60 86v8" stroke="#ccc" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <p class="catalog-empty-text">该课程暂无章节</p>
        </div>
      </div>

      <!-- 作业 Tab -->
      <div v-else-if="activeTab === 'homework'" class="homework-panel">
        <button v-if="isTeacher" class="add-hw-btn" @click="addHomework">+ 添加作业</button>
        <div v-else class="tab-placeholder">
          <svg class="tab-placeholder-icon" viewBox="0 0 100 100" fill="none">
            <circle cx="50" cy="45" r="22" stroke="#e0e0e0" stroke-width="2" fill="#fafafa"/>
            <path d="M42 45h16M50 37v16" stroke="#ccc" stroke-width="2.5" stroke-linecap="round"/>
          </svg>
          <p class="tab-placeholder-text">作业功能开发中，接口已预留</p>
        </div>
      </div>

      <!-- 其他 Tab 占位 -->
      <div v-else class="tab-placeholder">
        <svg class="tab-placeholder-icon" viewBox="0 0 100 100" fill="none">
          <circle cx="50" cy="45" r="22" stroke="#e0e0e0" stroke-width="2" fill="#fafafa"/>
          <path d="M42 45h16M50 37v16" stroke="#ccc" stroke-width="2.5" stroke-linecap="round"/>
        </svg>
        <p class="tab-placeholder-text">{{ currentTabLabel }}功能开发中，接口已预留</p>
      </div>
    </div>

    <!-- 加载 -->
    <div v-if="loading" class="page-loading">
      <p>加载中...</p>
    </div>
    <div v-else-if="error" class="page-error">
      <p>{{ error }}</p>
      <button @click="loadDetail">重试</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { api } from '../api/request.js'

const props = defineProps({
  courseId: { type: [Number, String], required: true }
})
defineEmits(['back'])

const course = ref(null)
const loading = ref(true)
const error = ref('')
const activeTab = ref('catalog')

const tabs = [
  { key: 'catalog',     label: '目录' },
  { key: 'courseware',  label: '互动课件' },
  { key: 'homework',    label: '作业' },
  { key: 'test',        label: '测试' },
  { key: 'material',    label: '资料' },
  { key: 'meeting',     label: '腾讯会议' },
  { key: 'notice',      label: '公告' },
  { key: 'topic',       label: '话题' },
  { key: 'qa',          label: '互动答题' },
  { key: 'case',        label: '案例学习' }
]

const isTeacher = computed(() => course.value?.userRole === 'teacher')

const studentFuncButtons = [
  { type: 'attendance',  label: '考勤',   icon: 'attendance' },
  { type: 'performance', label: '表现',   icon: 'performance' },
  { type: 'grades',      label: '成绩',   icon: 'grades' },
  { type: 'analysis',    label: '学习分析', icon: 'analysis' },
  { type: 'errors',      label: '错题集',  icon: 'errors' }
]
const teacherFuncButtons = [
  { type: 'teaching',    label: '课程教学', icon: 'teaching' },
  { type: 'analysis',    label: '学情分析', icon: 'analysis' },
  { type: 'grades',      label: '成绩管理', icon: 'grades' },
  { type: 'achievement', label: '课程达成度', icon: 'achievement' }
]
const funcButtons = computed(() => isTeacher.value ? teacherFuncButtons : studentFuncButtons)

const currentTabLabel = computed(() => {
  const t = tabs.find(t => t.key === activeTab.value)
  return t ? t.label : ''
})

const coverColors = ['#667eea', '#764ba2', '#f093fb', '#4facfe', '#43e97b', '#fa709a', '#fee140', '#30cfd0']
function coverColor(id) {
  return coverColors[id % coverColors.length]
}
const bannerColor = computed(() => {
  const id = course.value?.courseId
  return id != null ? coverColor(id) : '#667eea'
})

const classText = computed(() => {
  const parts = []
  if (course.value?.semester) parts.push(course.value.semester)
  if (course.value?.courseIntro) parts.push(course.value.courseIntro)
  return parts.join(' · ') || '班级信息'
})

// ============ 功能按钮（预留接口） ============
function handleFunc(type) {
  const map = {
    attendance:  { api: '/api/course/{id}/attendance', label: '考勤' },
    performance: { api: '/api/course/{id}/performance', label: '表现' },
    grades:      { api: '/api/course/{id}/grades', label: '成绩' },
    analysis:    { api: '/api/course/{id}/analysis', label: '分析' },
    errors:      { api: '/api/course/{id}/errors', label: '错题集' },
    teaching:    { api: '/api/course/{id}/teaching', label: '课程教学' },
    achievement: { api: '/api/course/{id}/achievement', label: '课程达成度' }
  }
  const info = map[type]
  console.log(`[预留接口] ${info.label}: ${info.api.replace('{id}', props.courseId)}`)
}

function addHomework() {
  console.log(`[预留接口] 添加作业: /api/course/${props.courseId}/homework (POST)`)
}

function copyCode() {
  if (!course.value?.courseCode) return
  navigator.clipboard.writeText(course.value.courseCode).then(() => {
    alert('加课码已复制')
  }).catch(() => {
    const input = document.createElement('input')
    input.value = course.value.courseCode
    document.body.appendChild(input)
    input.select()
    document.execCommand('copy')
    document.body.removeChild(input)
    alert('加课码已复制')
  })
}

async function loadDetail() {
  loading.value = true
  error.value = ''
  try {
    const res = await api.getCourseDetail(props.courseId)
    course.value = res.data
  } catch (e) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(() => { loadDetail() })
</script>

<style scoped>
.course-detail-page {
  width: 100%;
  max-width: 100%;
  min-height: 100vh;
  background: #fff;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* ==================== Banner ==================== */
.banner {
  position: relative;
  padding: 16px 16px 20px;
  min-height: 180px;
  overflow: hidden;
}

.banner-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}
.banner-leaf {
  position: absolute;
  opacity: .4;
}
.leaf-1 { right: -10px; top: -30px; width: 160px; height: 200px; transform: rotate(15deg); }
.leaf-2 { right: 30px; top: -20px; width: 130px; height: 170px; transform: rotate(-20deg); }

.banner-back {
  position: absolute;
  top: 12px;
  left: 12px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 50%;
  background: rgba(255,255,255,.15);
  cursor: pointer;
  z-index: 2;
  transition: background .15s;
}
.banner-back:hover { background: rgba(255,255,255,.25); }

.banner-main {
  position: relative;
  z-index: 1;
  padding-top: 8px;
}
.banner-class {
  font-size: 13px;
  color: rgba(255,255,255,.75);
  margin-bottom: 6px;
  line-height: 1.4;
}
.banner-title {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  line-height: 1.3;
  margin-bottom: 12px;
  letter-spacing: .5px;
}
.banner-code-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.banner-code {
  font-size: 13px;
  color: rgba(255,255,255,.8);
  letter-spacing: 1px;
}
.banner-code-copy {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border: 1px solid rgba(255,255,255,.3);
  border-radius: 4px;
  background: rgba(255,255,255,.1);
  color: #fff;
  font-size: 11px;
  cursor: pointer;
  transition: background .15s;
}
.banner-code-copy:hover { background: rgba(255,255,255,.2); }

.banner-stats {
  position: relative;
  z-index: 1;
  display: flex;
  gap: 20px;
  margin-top: 18px;
}
.stat-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: rgba(255,255,255,.7);
}

/* ==================== 功能按钮 ==================== */
.func-buttons {
  display: flex;
  justify-content: space-around;
  padding: 16px 12px;
  background: #fff;
}
.func-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  border: none;
  background: none;
  cursor: pointer;
  padding: 4px;
  transition: transform .15s;
}
.func-btn:active { transform: scale(.95); }
.func-btn-circle {
  width: 46px;
  height: 46px;
  border-radius: 50%;
  background: #2377E4;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  transition: background .15s;
}
.func-btn:hover .func-btn-circle { background: #1a5fc4; }
.func-btn-label {
  font-size: 11px;
  color: #555;
  white-space: nowrap;
}

/* ==================== Tab 栏 ==================== */
.tab-bar-wrapper {
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}
.tab-bar {
  display: flex;
  overflow-x: auto;
  padding: 0 4px;
  gap: 0;
  -webkit-overflow-scrolling: touch;
}
.tab-bar::-webkit-scrollbar { display: none; }
.tab-item {
  flex-shrink: 0;
  padding: 12px 14px;
  border: none;
  background: none;
  font-size: 13px;
  color: #777;
  cursor: pointer;
  position: relative;
  white-space: nowrap;
  transition: color .15s;
}
.tab-item.active {
  color: #2377E4;
  font-weight: 600;
}
.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 24px;
  height: 3px;
  border-radius: 2px;
  background: #2377E4;
}

/* ==================== 内容区域 ==================== */
.content-area {
  background: #fff;
  min-height: 400px;
}

/* 目录面板 */
.catalog-panel {
  padding: 16px;
}
.catalog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 40px;
}
.catalog-title {
  font-size: 16px;
  font-weight: 600;
  color: #111;
}
.catalog-stats {
  font-size: 12px;
  color: #999;
}
.catalog-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 0 60px;
}
.catalog-empty-icon {
  width: 120px;
  height: 120px;
  margin-bottom: 16px;
}
.catalog-empty-text {
  font-size: 14px;
  color: #aaa;
}

/* 作业面板 */
.homework-panel {
  padding: 16px;
}
.add-hw-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: 36px;
  padding: 0 18px;
  border: none;
  border-radius: 8px;
  background: #2377E4;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity .15s;
}
.add-hw-btn:hover { opacity: .9; }
.add-hw-btn:active { transform: scale(.97); }

/* 占位 Tab */
.tab-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 0;
}
.tab-placeholder-icon {
  width: 80px;
  height: 80px;
  margin-bottom: 14px;
}
.tab-placeholder-text {
  font-size: 13px;
  color: #bbb;
}

/* 加载/错误 */
.page-loading, .page-error {
  text-align: center;
  padding: 80px 0;
  color: #999;
  font-size: 14px;
}
.page-error button {
  margin-top: 10px;
  padding: 8px 20px;
  border: none;
  border-radius: 6px;
  background: #2377E4;
  color: #fff;
  font-size: 13px;
  cursor: pointer;
}
</style>
