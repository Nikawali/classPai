<template>
  <div class="classroom-page">
    <CourseDetail
      v-if="selectedCourseId"
      :courseId="selectedCourseId"
      @back="selectedCourseId = null"
    />
    <template v-else>
    <!-- ==================== 置顶课程区（拖动排序） ==================== -->
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

      <!-- 横向滚动卡片（支持拖动排序） -->
      <div
        ref="scrollContainer"
        class="top-cards-scroll"
        @dragover.prevent
        @drop.prevent="onDrop"
        @scroll="onCardsScroll"
      >
        <div
          v-for="(course, idx) in pinnedCourses"
          :key="course.courseId"
          class="top-card"
          :class="{ 'dragging': dragIndex === idx, 'drag-over': dragOverIndex === idx }"
          draggable="true"
          @dragstart="onDragStart(idx, $event)"
          @dragenter.prevent="onDragEnter(idx)"
          @dragover.prevent
          @dragend="onDragEnd"
          @click="enterCourse(course)"
        >
          <span class="top-card-tag">{{ course.userRole === 'teacher' ? '教' : '学' }}</span>
          <!-- 3 点菜单按钮 -->
          <button class="top-card-menu-btn" title="更多" @click.stop="openCardMenu(course)">
            <svg viewBox="0 0 24 24" width="14" height="14">
              <circle cx="12" cy="5" r="1.5" fill="currentColor"/>
              <circle cx="12" cy="12" r="1.5" fill="currentColor"/>
              <circle cx="12" cy="19" r="1.5" fill="currentColor"/>
            </svg>
          </button>
          <div class="top-card-cover" :style="{ background: coverColor(course.courseId) }">
            <span class="top-card-cover-text">{{ course.courseName.charAt(0) }}</span>
          </div>
          <div class="top-card-body">
            <span class="top-card-code">{{ course.courseCode }}</span>
            <p class="top-card-name">{{ course.courseName }}</p>
            <p class="top-card-intro">{{ course.courseIntro }}</p>
          </div>
        </div>

        <div v-if="pinnedCourses.length === 0" class="top-empty">
          <p>暂无置顶课程，点课程右侧菜单置顶</p>
        </div>
      </div>

      <!-- 自定义滚动条 -->
      <div
        v-if="pinnedCourses.length > 0 && scrollRatio < 1"
        ref="scrollbarTrack"
        class="custom-scrollbar"
        :class="{ active: isDraggingScrollbar }"
        @mousedown.prevent="onTrackDown"
        @touchstart.prevent="onTrackDown"
      >
        <div
          class="custom-scrollbar-thumb"
          :style="{ left: scrollLeftPercent + '%', width: scrollThumbWidth + '%' }"
          @mousedown.stop.prevent="onThumbDown"
          @touchstart.stop.prevent="onThumbDown"
        ></div>
      </div>
    </div>

    <!-- ==================== 切换栏 ==================== -->
    <div class="tab-bar">
      <div class="tab-tabs">
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'student' }"
          @click="switchTab('student')"
        >我学的</button>
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'teacher' }"
          @click="switchTab('teacher')"
        >我教的</button>
      </div>
      <div class="tab-actions">
        <button class="tab-icon-btn" @click="showSearch = !showSearch">
          <svg viewBox="0 0 24 24" width="18" height="18">
            <circle cx="11" cy="11" r="7" fill="none" stroke="currentColor" stroke-width="2"/>
            <line x1="16.5" y1="16.5" x2="21" y2="21" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
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

    <!-- ==================== 学年学期分组列表 ==================== -->
    <div v-if="groupedCourses.length === 0" class="course-empty">
      <p>{{ activeTab === 'student' ? '暂无学习的课程' : '暂无教学的课程' }}</p>
    </div>

    <div v-else class="group-list">
      <div
        v-for="group in groupedCourses"
        :key="group.groupName"
        class="group-item"
      >
        <div class="group-header" @click="toggleGroup(group.groupName)">
          <svg
            class="group-arrow"
            :class="{ expanded: expandedGroups.has(group.groupName) }"
            viewBox="0 0 24 24" width="16" height="16"
          >
            <path d="M8 6l8 6-8 6" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <span class="group-name">{{ group.groupName }}</span>
          <span class="group-count">{{ group.courses.length }}门课程</span>
        </div>

        <transition name="group-slide">
          <div v-show="expandedGroups.has(group.groupName)" class="group-courses">
            <div
              v-for="course in group.courses"
              :key="course.courseId"
              class="group-course-card"
              @click="enterCourse(course)"
            >
              <span class="group-card-tag">{{ activeTab === 'teacher' ? '教' : '学' }}</span>
              <div class="group-card-cover" :style="{ background: coverColor(course.courseId) }">
                <span class="group-card-cover-text">{{ course.courseName.charAt(0) }}</span>
              </div>
              <div class="group-card-body">
                <p class="group-card-name">{{ course.courseName }}</p>
                <p class="group-card-intro">{{ course.courseIntro }}</p>
                <p class="group-card-meta">{{ course.studentCount || 0 }} 名学生</p>
              </div>
              <button class="group-card-menu" title="更多" @click.stop="openCardMenu(course)">
                <svg viewBox="0 0 24 24" width="14" height="14">
                  <circle cx="12" cy="5" r="1.5" fill="currentColor"/>
                  <circle cx="12" cy="12" r="1.5" fill="currentColor"/>
                  <circle cx="12" cy="19" r="1.5" fill="currentColor"/>
                </svg>
              </button>
            </div>
          </div>
        </transition>
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

    <!-- ==================== 创建课程全屏页 ==================== -->
    <CourseCreate
      v-if="showCreateDialog"
      @back="showCreateDialog = false"
      @created="onCourseCreated"
    />

    <!-- ==================== 底部操作面板 ==================== -->
    <div v-if="bottomSheet.course" class="sheet-overlay" @click.self="closeBottomSheet">
      <div class="sheet-panel">
        <div class="sheet-header">
          <p class="sheet-course-name">{{ bottomSheet.course.courseName }}</p>
          <p class="sheet-course-code">{{ bottomSheet.course.courseCode }}</p>
        </div>
        <div class="sheet-actions">
          <button class="sheet-action-item" @click="handleSheetAction('quit')">退课</button>
          <button class="sheet-action-item" @click="handleSheetAction('archive')">归档</button>
          <button class="sheet-action-item" @click="handleSheetAction('pin')">{{ isSheetCoursePinned ? '取消置顶' : '置顶' }}</button>
        </div>
        <button class="sheet-cancel-btn" @click="closeBottomSheet">取消</button>
      </div>
    </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { api } from '../api/request.js'
import CourseCreate from './CourseCreate.vue'
import CourseDetail from './CourseDetail.vue'

// ==================== 状态 ====================
const activeTab = ref('student')
const selectedCourseId = ref(null)
const showSearch = ref(false)
const searchKeyword = ref('')
const pinnedCourses = ref([])

// 分组课程（全量数据 + 按标签页过滤）
const allGroupsData = ref([])
const expandedGroups = ref(new Set())
const groupedCourses = computed(() => {
  const targetRole = activeTab.value === 'teacher' ? 'teacher' : 'student'
  return allGroupsData.value
    .map(g => ({
      groupName: g.groupName,
      courses: (g.courses || []).filter(c => c.userRole === targetRole)
    }))
    .filter(g => g.courses.length > 0)
})

// 拖动排序状态
const dragIndex = ref(-1)
const dragOverIndex = ref(-1)
const dragged = ref(false)

// 自定义滚动条
const scrollContainer = ref(null)
const scrollbarTrack = ref(null)
const scrollLeftPercent = ref(0)
const scrollThumbWidth = ref(100)
const scrollRatio = ref(1)
const isDraggingScrollbar = ref(false)

function updateScrollbar() {
  const el = scrollContainer.value
  if (!el) return
  const w = el.scrollWidth - el.clientWidth
  scrollRatio.value = el.clientWidth / el.scrollWidth
  if (w <= 0) {
    scrollLeftPercent.value = 0
    scrollThumbWidth.value = 100
  } else {
    scrollLeftPercent.value = (el.scrollLeft / el.scrollWidth) * 100
    scrollThumbWidth.value = Math.max(scrollRatio.value * 100, 8)
  }
}

function onCardsScroll() {
  updateScrollbar()
}

function onThumbDown(e) {
  const el = scrollContainer.value
  const track = scrollbarTrack.value
  if (!el || !track) return

  isDraggingScrollbar.value = true
  const maxScroll = el.scrollWidth - el.clientWidth
  const trackWidth = track.getBoundingClientRect().width
  const startX = (e.touches ? e.touches[0].clientX : e.clientX)
  const startScroll = el.scrollLeft

  const move = (ev) => {
    const dx = (ev.touches ? ev.touches[0].clientX : ev.clientX) - startX
    el.scrollLeft = Math.max(0, Math.min(maxScroll, startScroll + dx * maxScroll / trackWidth))
  }
  const up = () => {
    isDraggingScrollbar.value = false
    document.removeEventListener('mousemove', move)
    document.removeEventListener('mouseup', up)
    document.removeEventListener('touchmove', move)
    document.removeEventListener('touchend', up)
  }

  document.addEventListener('mousemove', move)
  document.addEventListener('mouseup', up)
  document.addEventListener('touchmove', move, { passive: true })
  document.addEventListener('touchend', up)
}

function onTrackDown(e) {
  const el = scrollContainer.value
  const track = scrollbarTrack.value
  if (!el || !track) return

  isDraggingScrollbar.value = true
  const maxScroll = el.scrollWidth - el.clientWidth
  const rect = track.getBoundingClientRect()

  // 点击轨道 → 平滑滚到对应位置
  const jumpTo = (x) => {
    const ratio = Math.max(0, Math.min(1, x / rect.width))
    el.scrollTo({ left: ratio * maxScroll, behavior: 'smooth' })
  }

  let rafId = 0
  let targetX = 0
  let dragging = false

  const updateTarget = (ev) => {
    dragging = true
    targetX = (ev.touches ? ev.touches[0].clientX : ev.clientX) - rect.left
  }

  const tick = () => {
    if (dragging) {
      // 按住拖动 → 直接赋值
      el.scrollLeft = Math.max(0, Math.min(maxScroll, (targetX / rect.width) * maxScroll))
    }
    rafId = requestAnimationFrame(tick)
  }

  const move = (ev) => updateTarget(ev)
  const up = () => {
    isDraggingScrollbar.value = false
    cancelAnimationFrame(rafId)
    document.removeEventListener('mousemove', move)
    document.removeEventListener('mouseup', up)
    document.removeEventListener('touchmove', move)
    document.removeEventListener('touchend', up)
  }

  // 首次点击
  jumpTo((e.touches ? e.touches[0].clientX : e.clientX) - rect.left)

  document.addEventListener('mousemove', move)
  document.addEventListener('mouseup', up)
  document.addEventListener('touchmove', move, { passive: true })
  document.addEventListener('touchend', up)
  rafId = requestAnimationFrame(tick)
}

// 加入课程弹窗
const showJoinDialog = ref(false)
const joinCode = ref('')
const joinError = ref('')

// 创建课程
const showCreateDialog = ref(false)

// 加入/创建下拉菜单
const showActionSheet = ref(false)

// 底部操作面板
const bottomSheet = ref({ course: null })

// 当前打开的面板中课程的置顶状态
const isSheetCoursePinned = computed(() => {
  const id = bottomSheet.value.course?.courseId
  return id ? pinnedCourseIds.value.has(id) : false
})

// 当前置顶课程 ID 集合（用于快速判断）
const pinnedCourseIds = ref(new Set())

function openCardMenu(course) {
  bottomSheet.value = { course }
}
function closeBottomSheet() {
  bottomSheet.value = { course: null }
}
function handleSheetAction(action) {
  const name = bottomSheet.value.course?.courseName || ''
  const id = bottomSheet.value.course?.courseId
  closeBottomSheet()
  if (action === 'pin' && id) {
    handlePin(id)
  } else if (action === 'quit') {
    alert('退课功能开发中（课程：' + name + '）')
  } else if (action === 'archive') {
    alert('归档功能开发中（课程：' + name + '）')
  }
}

// ==================== 颜色 ====================
const coverColors = ['#667eea', '#764ba2', '#f093fb', '#4facfe', '#43e97b', '#fa709a', '#fee140', '#30cfd0']
function coverColor(id) {
  return coverColors[id % coverColors.length]
}

// ==================== 拖动排序 ====================
function onDragStart(idx, e) {
  dragIndex.value = idx
  dragged.value = true
  e.dataTransfer.effectAllowed = 'move'
  e.dataTransfer.setData('text/plain', idx)
}

function onDragEnter(idx) {
  dragOverIndex.value = idx
}

function onDragEnd() {
  dragIndex.value = -1
  dragOverIndex.value = -1
  // 拖动结束后短暂时间内忽略 click
  setTimeout(() => { dragged.value = false }, 100)
}

function onDrop(e) {
  const from = dragIndex.value
  const to = dragOverIndex.value
  if (from === -1 || to === -1 || from === to) {
    onDragEnd()
    return
  }

  const list = [...pinnedCourses.value]
  const [moved] = list.splice(from, 1)
  list.splice(to, 0, moved)
  pinnedCourses.value = list

  // 同步后端
  const ids = list.map(c => c.courseId)
  api.updatePinnedOrder(ids).catch(e => {
    console.error('排序同步失败:', e)
    loadAllCourses()
  })

  onDragEnd()
}

// ==================== 置顶/取消置顶 ====================
async function handlePin(courseId) {
  try {
    await api.togglePin(courseId)
    loadAllCourses()
  } catch (e) {
    console.error('置顶失败:', e)
  }
}

// ==================== 方法 ====================
function switchTab(role) {
  activeTab.value = role
  expandedGroups.value = new Set()
}

function toggleGroup(name) {
  const s = new Set(expandedGroups.value)
  if (s.has(name)) {
    s.delete(name)
  } else {
    s.add(name)
  }
  expandedGroups.value = s
}

function enterCourse(course) {
  if (dragged.value) return
  selectedCourseId.value = course.courseId
}

async function handleJoinCourse() {
  if (!joinCode.value.trim()) return
  joinError.value = ''
  try {
    await api.joinCourse(joinCode.value)
    showJoinDialog.value = false
    joinCode.value = ''
    loadAllCourses()
  } catch (e) {
    joinError.value = e.message
  }
}

function onCourseCreated() {
  showCreateDialog.value = false
  loadAllCourses()
}

function handleSearch() {
  const kw = searchKeyword.value.trim()
  if (!kw) return
  api.searchCourses(kw).then(res => {
    // TODO: 搜索后展示结果（后端接口待实现）
    console.log('搜索结果:', res)
  }).catch(e => console.error('搜索失败:', e))
}

// ==================== 数据加载 ====================
async function loadAllCourses() {
  try {
    const res = await api.getAllCourses()
    const data = res.data || {}
    pinnedCourses.value = data.pinnedCourses || []
    pinnedCourseIds.value = new Set(pinnedCourses.value.map(c => c.courseId))
    allGroupsData.value = data.groups || []
    await nextTick()
    updateScrollbar()
  } catch (e) {
    console.error('加载课程数据失败:', e)
  }
}

onMounted(() => {
  loadAllCourses()
  window.addEventListener('resize', updateScrollbar)
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
  overflow: hidden;
}

/* ==================== 置顶课程区 ==================== */
.top-section {
  margin-bottom: 24px;
  min-width: 0;
  overflow: hidden;
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
  flex-wrap: nowrap;
  gap: 12px;
  overflow-x: auto;
  overflow-y: hidden;
  padding-bottom: 4px;
  width: 100%;
  -webkit-overflow-scrolling: touch;
  scroll-behavior: auto;
}
.top-cards-scroll::-webkit-scrollbar { display: none; }

/* 自定义滚动条 */
.custom-scrollbar {
  height: 4px;
  background: #e8e8e8;
  border-radius: 2px;
  margin-top: 12px;
  cursor: pointer;
  position: relative;
  touch-action: none;
  transition: height .25s cubic-bezier(.4, 0, .2, 1),
              border-radius .25s cubic-bezier(.4, 0, .2, 1),
              background .25s cubic-bezier(.4, 0, .2, 1);
}

.custom-scrollbar-thumb {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  height: 100%;
  background: #c8c8c8;
  border-radius: 2px;
  min-width: 32px;
  transition: height .25s cubic-bezier(.4, 0, .2, 1),
              background .25s cubic-bezier(.4, 0, .2, 1),
              border-radius .25s cubic-bezier(.4, 0, .2, 1);
  pointer-events: auto;
}

/* 按下拖动时变粗——按住不放就算移开也保持粗 */
.custom-scrollbar.active {
  height: 12px;
  border-radius: 6px;
  background: #e0e0e0;
}
.custom-scrollbar.active .custom-scrollbar-thumb {
  height: 100%;
  background: #888;
  border-radius: 6px;
}

.top-card {
  flex-shrink: 0;
  width: 154px;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: grab;
  position: relative;
  transition: box-shadow .15s, transform .15s, opacity .15s;
  user-select: none;
  -webkit-user-select: none;
}
.top-card:active { cursor: grabbing; }
.top-card:hover { box-shadow: 0 2px 12px rgba(0,0,0,.08); }

.top-card.dragging {
  opacity: 0.4;
  transform: scale(0.95);
}

.top-card.drag-over {
  box-shadow: 0 0 0 2px #4a6cf7, 0 4px 16px rgba(74,108,247,.2);
  transform: scale(1.03);
}

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

.top-card-menu-btn {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 4px;
  background: rgba(0,0,0,.35);
  color: #fff;
  cursor: pointer;
  z-index: 2;
  opacity: 0;
  transition: opacity .15s;
}
.top-card:hover .top-card-menu-btn {
  opacity: 1;
}

/* 底部操作面板 */
.sheet-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,.35);
  z-index: 200;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  animation: overlayIn .2s ease;
}
@keyframes overlayIn {
  from { opacity: 0; }
  to   { opacity: 1; }
}

.sheet-panel {
  width: 100%;
  background: #fff;
  border-radius: 16px 16px 0 0;
  padding: 24px 20px 20px;
  animation: slideUp .25s ease;
}
@keyframes slideUp {
  from { transform: translateY(100%); }
  to   { transform: translateY(0); }
}

.sheet-header {
  text-align: center;
  margin-bottom: 20px;
}
.sheet-course-name {
  font-size: 17px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 4px;
}
.sheet-course-code {
  font-size: 12px;
  color: #aaa;
  letter-spacing: 1px;
}

.sheet-actions {
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin-bottom: 12px;
}

.sheet-action-item {
  width: 100%;
  height: 48px;
  border: none;
  border-radius: 10px;
  background: #f5f6fa;
  color: #333;
  font-size: 15px;
  cursor: pointer;
  transition: background .12s;
}
.sheet-action-item:hover { background: #eef0f5; }
.sheet-action-item:active { background: #e5e7ec; }

.sheet-cancel-btn {
  width: 100%;
  height: 44px;
  border: none;
  border-radius: 10px;
  background: #e8e8e8;
  color: #666;
  font-size: 14px;
  cursor: pointer;
  transition: background .12s;
}
.sheet-cancel-btn:hover { background: #ddd; }

.top-card-cover {
  width: 100%;
  height: 88px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.top-card-cover-text {
  font-size: 32px;
  font-weight: 700;
  color: rgba(255,255,255,.7);
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
}
.top-card-intro {
  font-size: 11px;
  color: #aaa;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-top: 3px;
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

.tab-actions { display: flex; gap: 6px; }

.tab-icon-btn {
  width: 34px; height: 34px;
  display: flex; align-items: center; justify-content: center;
  border: none; border-radius: 8px;
  background: #fff; color: #888;
  cursor: pointer; transition: color .15s;
}
.tab-icon-btn:hover { color: #4a6cf7; }

.search-bar {
  display: flex; align-items: center; gap: 8px;
  padding: 0 12px; height: 40px;
  background: #fff; border-radius: 10px; margin-bottom: 16px;
}
.search-icon { flex-shrink: 0; color: #bbb; }
.search-input {
  flex: 1; border: none; outline: none;
  font-size: 13px; color: #333; background: transparent;
}
.search-input::placeholder { color: #bbb; }

/* ==================== 学年学期分组 ==================== */
.course-empty {
  text-align: center;
  padding: 40px 0;
  color: #bbb;
  font-size: 13px;
}

.group-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.group-item {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}

.group-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 16px;
  cursor: pointer;
  user-select: none;
  transition: background .15s;
}
.group-header:hover { background: #fafafa; }
.group-header:active { background: #f5f5f5; }

.group-arrow {
  flex-shrink: 0;
  color: #bbb;
  transition: transform .25s ease;
}
.group-arrow.expanded { transform: rotate(90deg); }

.group-name {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
  flex: 1;
}

.group-count {
  font-size: 12px;
  color: #aaa;
}

.group-courses {
  border-top: 1px solid #f0f0f0;
  overflow: hidden;
}

/* 折叠过渡动画 */
.group-slide-enter-active,
.group-slide-leave-active {
  transition: all .25s ease;
  max-height: 2000px;
}
.group-slide-enter-from,
.group-slide-leave-to {
  max-height: 0;
  opacity: 0;
}

/* 课程卡片 -- 与置顶卡片风格一致 */
.group-course-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background .1s;
  position: relative;
}
.group-course-card:hover { background: #fafafa; }
.group-course-card:active { background: #f5f5f5; }
.group-course-card + .group-course-card {
  border-top: 1px solid #f5f5f5;
}

.group-card-cover {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.group-card-cover-text {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
}

.group-card-body {
  flex: 1;
  min-width: 0;
}
.group-card-name {
  font-size: 15px;
  font-weight: 500;
  color: #1a1a1a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 3px;
}
.group-card-intro {
  font-size: 12px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 4px;
}
.group-card-meta {
  font-size: 11px;
  color: #bbb;
}

/* 角色角标 */
.group-card-tag {
  position: absolute;
  top: 6px;
  left: 6px;
  font-size: 10px;
  color: #4a6cf7;
  background: rgba(74,108,247,.1);
  padding: 1px 5px;
  border-radius: 3px;
  font-weight: 600;
  z-index: 1;
}
.group-card-menu {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #bbb;
  cursor: pointer;
  flex-shrink: 0;
  transition: color .15s, background .15s;
}
.group-card-menu:hover { color: #666; background: #f0f0f0; }

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
