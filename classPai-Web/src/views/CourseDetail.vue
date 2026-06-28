<template>
  <div class="course-detail-page" v-if="course">
    <!-- ==================== 成员管理页 ==================== -->
    <CourseMembers
      v-if="showMembers"
      :courseId="course.courseId"
      @back="backFromMembers"
    />

    <!-- ==================== 教师作业批阅页 ==================== -->
    <TeacherHomework
      v-if="showGrading"
      :hwId="gradingHwId"
      @back="backFromGrading"
    />

    <!-- ==================== 教师发布作业页 ==================== -->
    <CourseHomework
      v-if="showAddHomework"
      :courseId="course.courseId"
      :courseName="course.courseName"
      :courseCode="course.courseCode"
      @back="backFromAddHomework"
    />

    <!-- ==================== 学生作业详情页 ==================== -->
    <StudentHomeworkDetail
      v-else-if="selectedHomeworkId && !showSubmit"
      :courseId="course.courseId"
      :homeworkId="selectedHomeworkId"
      @back="backFromHomework"
      @submit="onStudentSubmit"
    />

    <!-- ==================== 学生作业提交页 ==================== -->
    <StudentHomeworkSubmit
      v-else-if="showSubmit"
      :courseId="course.courseId"
      :homeworkId="selectedHomeworkId"
      @back="backFromSubmit"
    />

    <!-- ==================== 课程详情主页 ==================== -->
    <template v-else>
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
        <div class="stat-item stat-clickable" @click="enterMembers">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none">
            <circle cx="12" cy="9" r="3.5" stroke="rgba(255,255,255,.7)" stroke-width="1.5"/>
            <path d="M5 20c0-3.9 3.1-7 7-7s7 3.1 7 7" stroke="rgba(255,255,255,.7)" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <span>{{ course.studentCount || 0 }} 成员</span>
          <svg class="stat-arrow" viewBox="0 0 24 24" width="10" height="10" fill="none">
            <path d="M8 6l8 6-8 6" fill="none" stroke="rgba(255,255,255,.5)" stroke-width="2" stroke-linecap="round"/>
          </svg>
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
        <div class="homework-header">
          <h3 class="homework-title">作业列表</h3>
          <button v-if="isTeacher" class="add-hw-btn" @click="addHomework">+ 添加作业</button>
        </div>

        <div v-if="hwLoading" class="homework-empty">
          <p class="homework-empty-text">加载中...</p>
        </div>
        <div v-else-if="homeworks.length === 0" class="homework-empty">
          <svg class="homework-empty-icon" viewBox="0 0 120 120" fill="none">
            <rect x="25" y="20" width="70" height="80" rx="6" stroke="#ddd" stroke-width="2" fill="#f9f9f9"/>
            <line x1="40" y1="42" x2="80" y2="42" stroke="#e0e0e0" stroke-width="2.5" stroke-linecap="round"/>
            <line x1="40" y1="54" x2="70" y2="54" stroke="#e8e8e8" stroke-width="2.5" stroke-linecap="round"/>
            <line x1="40" y1="66" x2="75" y2="66" stroke="#e8e8e8" stroke-width="2.5" stroke-linecap="round"/>
          </svg>
          <p class="homework-empty-text">暂无作业</p>
        </div>
        <div v-else class="homework-list">
          <div v-for="hw in homeworks" :key="hw.hwId" class="homework-card" @click="onHomeworkClick(hw)">
            <div class="hw-card-top">
              <h4 class="hw-title">{{ hw.title }}</h4>
              <!-- 学生视角：提交状态 -->
              <span v-if="!isTeacher" class="hw-status" :class="hw.submitted ? 'submitted' : 'unsubmitted'">
                {{ hw.submitted ? '已提交' : '未提交' }}
              </span>
              <!-- 教师视角：已交/已批 -->
              <span v-if="isTeacher" class="hw-stats">
                已交 {{ hw.submitCount || 0 }}/{{ hw.totalStudents || 0 }}
                已批 {{ hw.gradedCount || 0 }}/{{ hw.submitCount || 0 }}
              </span>
              <span class="hw-deadline">{{ fmtDeadline(hw.deadline) }}</span>
            </div>
            <div class="hw-card-bottom">
              <p class="hw-content">{{ hw.content }}</p>
              <button class="hw-file-btn" title="查看文件" @click.stop="toggleFiles(hw.hwId)">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none">
                  <path d="M13 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V9z" stroke="currentColor" stroke-width="1.5"/>
                  <polyline points="13 2 13 9 20 9" stroke="currentColor" stroke-width="1.5"/>
                </svg>
              </button>
            </div>
            <!-- 文件列表 -->
            <div v-if="expandedHwId === hw.hwId" class="hw-files">
              <div v-if="fileLoadingHwId === hw.hwId" class="hw-files-loading">加载中...</div>
              <div v-else-if="!hwFiles[hw.hwId] || hwFiles[hw.hwId].length === 0" class="hw-files-empty">该作业暂无文件</div>
              <a v-else v-for="f in hwFiles[hw.hwId]" :key="f.fileId"
                 class="hw-file-link" :href="f.filePath" target="_blank" rel="noopener">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none">
                  <path d="M13 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V9z" stroke="currentColor" stroke-width="1.5"/>
                  <polyline points="13 2 13 9 20 9" stroke="currentColor" stroke-width="1.5"/>
                </svg>
                <span>{{ hwFileLabel(f) }}</span>
              </a>
            </div>
          </div>
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
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { api } from '../api/request.js'
import CourseMembers from './CourseMembers.vue'
import TeacherHomework from './TeacherHomework.vue'
import CourseHomework from './CourseHomework.vue'
import StudentHomeworkDetail from './StudentHomeworkDetail.vue'
import StudentHomeworkSubmit from './StudentHomeworkSubmit.vue'
import { coverColor } from '../utils/colors.js'
import { fmtDeadline } from '../utils/format.js'

const props = defineProps({
  courseId: { type: [Number, String], required: true }
})
defineEmits(['back'])

const course = ref(null)
const loading = ref(true)
const error = ref('')
const activeTab = ref('catalog')

// ============ 成员管理导航 ============
const showMembers = ref(false)

function enterMembers() {
  showMembers.value = true
  location.hash = '#/course/' + props.courseId + '/members'
}

function backFromMembers() {
  showMembers.value = false
  location.hash = '#/course/' + props.courseId
}

// ============ 作业详情导航 ============
const selectedHomeworkId = ref(null)
const showSubmit = ref(false)

function onStudentSubmit({ courseId, homeworkId }) {
  showSubmit.value = true
}

function backFromSubmit() {
  showSubmit.value = false
  location.hash = '#/course/' + props.courseId + '/homework/' + selectedHomeworkId.value
}

// ============ 教师批阅导航 ============
const showGrading = ref(false)
const gradingHwId = ref(null)
const showAddHomework = ref(false)

function isGradingHash() {
  const m = location.hash.match(/^#\/course\/(\d+)\/homework\/(\d+)\/grading$/)
  if (m && Number(m[1]) === Number(props.courseId)) {
    gradingHwId.value = Number(m[2])
    return true
  }
  return false
}

function enterGrading(hw) {
  showMembers.value = false
  showGrading.value = true
  gradingHwId.value = hw.hwId
  location.hash = '#/course/' + props.courseId + '/homework/' + hw.hwId + '/grading'
}

function backFromGrading() {
  showGrading.value = false
  gradingHwId.value = null
  location.hash = '#/course/' + props.courseId
}

function parseHash() {
  const m = location.hash.match(/^#\/course\/(\d+)\/homework\/(\d+)$/)
  if (m && Number(m[1]) === Number(props.courseId)) {
    return Number(m[2])
  }
  return null
}

function isMembersHash() {
  const m = location.hash.match(/^#\/course\/(\d+)\/members$/)
  return m && Number(m[1]) === Number(props.courseId)
}

function onHomeworkClick(hw) {
  if (isTeacher.value) {
    enterGrading(hw)
  } else {
    enterHomework(hw.hwId)
  }
}

function enterHomework(hwId) {
  showMembers.value = false
  selectedHomeworkId.value = hwId
  location.hash = '#/course/' + props.courseId + '/homework/' + hwId
}

function backFromHomework() {
  selectedHomeworkId.value = null
  location.hash = '#/course/' + props.courseId
}

function onHashChange() {
  if (isMembersHash()) {
    showMembers.value = true
    showGrading.value = false
    selectedHomeworkId.value = null
  } else if (isGradingHash()) {
    showMembers.value = false
    showGrading.value = true
    selectedHomeworkId.value = null
  } else {
    showMembers.value = false
    showGrading.value = false
    selectedHomeworkId.value = parseHash()
  }
}

onMounted(() => {
  if (isMembersHash()) {
    showMembers.value = true
  } else if (isGradingHash()) {
    showGrading.value = true
  } else {
    selectedHomeworkId.value = parseHash()
  }
  window.addEventListener('hashchange', onHashChange)
})

onUnmounted(() => {
  window.removeEventListener('hashchange', onHashChange)
})

// ============ 作业 ============
const homeworks = ref([])
const hwLoading = ref(false)

async function loadHomeworks() {
  hwLoading.value = true
  try {
    const res = await api.getHomeworkList(props.courseId)
    homeworks.value = res.data?.records || []
  } catch (e) {
    console.error('加载作业列表失败:', e)
  } finally {
    hwLoading.value = false
  }
}

// ============ 作业文件 ============
const hwFiles = ref({})
const expandedHwId = ref(null)
const fileLoadingHwId = ref(null)

async function toggleFiles(hwId) {
  if (expandedHwId.value === hwId) {
    expandedHwId.value = null
    return
  }
  expandedHwId.value = hwId
  if (hwFiles.value[hwId]) return // 已加载过
  fileLoadingHwId.value = hwId
  try {
    const res = await api.getHomeworkFiles(hwId)
    hwFiles.value[hwId] = res.data || []
  } catch (e) {
    console.error('加载作业文件失败:', e)
    hwFiles.value[hwId] = []
  } finally {
    fileLoadingHwId.value = null
  }
}

/** 从文件路径和类型生成展示标签 */
function hwFileLabel(f) {
  const path = f.filePath || ''
  const type = f.fileType || ''
  const name = path.split('/').pop() || '文件'
  return type ? `${decodeURIComponent(name)} (${type})` : decodeURIComponent(name)
}

// 切换到作业 Tab 时加载数据
watch(activeTab, (val) => {
  if (val === 'homework') loadHomeworks()
})

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
  showAddHomework.value = true
}

function backFromAddHomework() {
  showAddHomework.value = false
  loadHomeworks()
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
  padding-left: 44px;
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
.stat-clickable {
  cursor: pointer;
  padding: 4px 8px;
  margin: -4px -8px;
  border-radius: 8px;
  transition: background .15s;
}
.stat-clickable:hover { background: rgba(255,255,255,.1); }
.stat-arrow { flex-shrink: 0; }

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
.homework-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.homework-title {
  font-size: 16px;
  font-weight: 600;
  color: #111;
}
.add-hw-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: 34px;
  padding: 0 16px;
  border: none;
  border-radius: 8px;
  background: #2377E4;
  color: #fff;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity .15s;
}
.add-hw-btn:hover { opacity: .9; }
.add-hw-btn:active { transform: scale(.97); }

/* 作业列表 */
.homework-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.homework-card {
  background: #f9f9fb;
  border-radius: 10px;
  padding: 14px 16px;
  cursor: pointer;
  transition: background .15s;
}
.homework-card:hover { background: #f0f0f5; }
.hw-card-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 8px;
}
.hw-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
  flex: 1;
}
.hw-deadline {
  font-size: 11px;
  color: #e74c3c;
  white-space: nowrap;
  padding: 2px 8px;
  background: #fef2f2;
  border-radius: 4px;
  flex-shrink: 0;
}
.hw-status {
  font-size: 11px;
  white-space: nowrap;
  padding: 2px 8px;
  border-radius: 4px;
  flex-shrink: 0;
}
.hw-status.submitted { color: #059669; background: #ecfdf5; }
.hw-status.unsubmitted { color: #e74c3c; background: #fef2f2; }
.hw-stats {
  font-size: 11px;
  color: #666;
  white-space: nowrap;
  flex-shrink: 0;
}
.hw-card-bottom {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}
.hw-content {
  flex: 1;
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 文件按钮 */
.hw-file-btn {
  flex-shrink: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 6px;
  background: #eef0f5;
  color: #888;
  cursor: pointer;
  transition: background .15s, color .15s;
}
.hw-file-btn:hover { background: #2377E4; color: #fff; }

/* 展开的文件列表 */
.hw-files {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #eee;
}
.hw-files-loading,
.hw-files-empty {
  font-size: 12px;
  color: #bbb;
}
.hw-file-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 10px;
  border-radius: 6px;
  background: #fff;
  font-size: 13px;
  color: #2377E4;
  text-decoration: none;
  transition: background .15s;
}
.hw-file-link + .hw-file-link { margin-top: 4px; }
.hw-file-link:hover { background: #eef2ff; }

/* 作业空状态 */
.homework-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 0;
}
.homework-empty-icon {
  width: 100px;
  height: 100px;
  margin-bottom: 12px;
}
.homework-empty-text {
  font-size: 13px;
  color: #bbb;
}

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

/* ==================== 作业详情占位 ==================== */
.hw-detail-placeholder {
  min-height: 100vh;
  background: #f5f6fa;
}
.hw-detail-topbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}
.hw-detail-back {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 50%;
  background: #f0f0f0;
  color: #333;
  cursor: pointer;
  transition: background .15s;
}
.hw-detail-back:hover { background: #e0e0e0; }
.hw-detail-title {
  font-size: 17px;
  font-weight: 600;
  color: #1a1a1a;
}
.hw-detail-body {
  padding: 40px 20px;
  text-align: center;
}
.hw-detail-info {
  font-size: 14px;
  color: #888;
  margin-bottom: 8px;
}
.hw-detail-placeholder-text {
  font-size: 14px;
  color: #bbb;
  margin-top: 24px;
}
</style>
