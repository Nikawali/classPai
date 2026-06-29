<template>
  <div class="teacher-hw-page">
    <!-- 加载/错误 -->
    <div v-if="loading" class="th-status">加载中...</div>
    <div v-else-if="error" class="th-status th-error">{{ error }}</div>

    <template v-else>
      <!-- ========== 作业详情 Tab ========== -->
      <StudentHomeworkDetail
        v-if="activeTab === 'detail'"
        :courseId="courseId"
        :homeworkId="hwId"
        role="teacher"
        @back="activeTab = 'students'"
      />

      <!-- ========== 学生作业 Tab ========== -->
      <template v-if="activeTab === 'students'">
      <!-- ========== 顶部导航栏 ========== -->
      <div class="th-topbar">
        <button class="th-back" @click="$emit('back')">
          <svg viewBox="0 0 24 24" width="18" height="18">
            <path d="M15 18l-6-6 6-6" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </button>
        <div class="th-tabs">
          <button class="th-tab" :class="{ active: activeTab === 'students' }" @click="activeTab = 'students'">学生作业</button>
          <button class="th-tab" :class="{ active: activeTab === 'detail' }" @click="activeTab = 'detail'">作业详情</button>
        </div>
      </div>

      <!-- ========== 标题行 ========== -->
      <div class="th-header">
        <h1 class="th-title">{{ homework.title }}</h1>
      </div>

      <!-- ========== 操作栏：截止时间 + 操作 + 搜索 ========== -->
      <div class="th-actions">
        <div class="th-time-group">
          <div class="th-time-tag start">
            <svg viewBox="0 0 24 24" width="12" height="12" fill="none">
              <polyline points="5,12 10,17 19,8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            <template v-if="editingStartTime">
              <input type="datetime-local" class="th-time-input" v-model="editStartTimeValue"
                @blur="saveStartTime" @keyup.enter="saveStartTime" />
            </template>
            <template v-else>
              <span>开始 {{ fmtDeadline(homework.startTime) }}</span>
            </template>
          </div>
          <div class="th-time-tag end">
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none">
              <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.5"/>
              <polyline points="12,7 12,12 16,14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <template v-if="editingDeadline">
              <input type="datetime-local" class="th-time-input" v-model="editDeadlineValue"
                @blur="saveDeadline" @keyup.enter="saveDeadline" />
            </template>
            <template v-else>
              <span>截止 {{ fmtDeadline(homework.deadline) }}</span>
            </template>
          </div>
        </div>
        <div class="th-actions-right">
          <!-- 设置下拉 -->
          <div class="th-dropdown-wrapper" ref="dropdownRef">
            <button class="th-btn th-btn-outline" @click="showSettings = !showSettings">
              <svg viewBox="0 0 24 24" width="14" height="14" fill="none">
                <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.5"/>
                <circle cx="5" cy="12" r="1.5" fill="currentColor"/>
                <circle cx="19" cy="12" r="1.5" fill="currentColor"/>
              </svg>
            </button>
            <div v-if="showSettings" class="th-dropdown">
              <button v-if="hasNotStarted" class="th-dropdown-item" @click="startEditStartTime">修改开始时间</button>
              <button class="th-dropdown-item" @click="startEditDeadline">修改截止时间</button>
              <button class="th-dropdown-item" @click="showSettings = false">编辑作业内容</button>
              <button class="th-dropdown-item danger" @click="showSettings = false">删除作业</button>
            </div>
          </div>
          <!-- 批量成绩 -->
          <button class="th-btn th-btn-primary">
            对学生批量成绩
          </button>
        </div>
      </div>

      <!-- 搜索框 -->
      <div class="th-search-bar">
        <svg class="th-search-icon" viewBox="0 0 24 24" width="14" height="14">
          <circle cx="11" cy="11" r="7" fill="none" stroke="currentColor" stroke-width="2"/>
          <line x1="16.5" y1="16.5" x2="21" y2="21" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
        <input
          v-model="keyword"
          type="text"
          placeholder="搜索学号或姓名"
          class="th-search-input"
        />
        <button v-if="keyword" class="th-search-clear" @click="keyword = ''">
          <svg viewBox="0 0 24 24" width="12" height="12">
            <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </button>
      </div>

      <!-- ========== 统计摘要 ========== -->
      <div class="th-stats">
        <div class="th-stat">
          <span class="th-stat-num">{{ students.length }}</span>
          <span class="th-stat-label">学生总数</span>
        </div>
        <div class="th-stat">
          <span class="th-stat-num green">{{ submittedCount }}</span>
          <span class="th-stat-label">已提交</span>
        </div>
        <div class="th-stat">
          <span class="th-stat-num blue">{{ gradedCount }}</span>
          <span class="th-stat-label">已批改</span>
        </div>
        <div class="th-stat">
          <span class="th-stat-num red">{{ unsubmittedCount }}</span>
          <span class="th-stat-label">未提交</span>
        </div>
      </div>

      <!-- ========== 学生列表 ========== -->
      <div class="th-table">
        <div class="th-table-head">
          <span class="th-col-id">学号</span>
          <span class="th-col-name">姓名</span>
          <span class="th-col-score">成绩</span>
          <span class="th-col-time">提交时间</span>
          <span class="th-col-action">操作</span>
        </div>

        <div
          v-for="s in filteredStudents"
          :key="s.studentId"
          class="th-row"
          :class="{ 'row-unsubmitted': !s.submitted }"
        >
          <span class="th-col-id">{{ s.studentId }}</span>
          <span class="th-col-name">{{ s.userName || '未知' }}</span>

          <!-- 成绩列 -->
          <span class="th-col-score">
            <template v-if="!s.submitted">
              <span class="th-tag danger">未交</span>
            </template>
            <template v-else-if="s.score != null">
              <span class="th-score-val">{{ s.score }}</span>
              <span class="th-score-total">/{{ s.totalScore || 100 }}</span>
            </template>
            <template v-else>
              <span class="th-tag warn">未批</span>
            </template>
          </span>

          <!-- 时间列 -->
          <span class="th-col-time">
            {{ s.submitTime ? formatTime(s.submitTime) : '-' }}
          </span>

          <!-- 操作列 -->
          <span class="th-col-action">
            <template v-if="hasNotStarted">
              <span class="th-not-started-tip">未开始</span>
            </template>
            <template v-else>
              <template v-if="!s.submitted">
                <button class="th-action-btn warn" @click="handleUrge(s)">催交</button>
              </template>
              <template v-else-if="s.score == null">
                <button class="th-action-btn primary" @click="openGrade(s)">进入批阅</button>
              </template>
              <template v-else>
                <button class="th-action-btn ghost" @click="openGrade(s)">查看</button>
              </template>
            </template>
            <span v-if="s.files && s.files.length" class="th-file-badge">
              <svg viewBox="0 0 24 24" width="11" height="11"><path d="M13 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z" fill="none" stroke="currentColor" stroke-width="2"/><polyline points="13 2 13 9 20 9" fill="none" stroke="currentColor" stroke-width="2"/></svg>
              {{ s.files.length }}个文件
            </span>
          </span>
          </div>

        <div v-if="filteredStudents.length === 0" class="th-empty">
          无匹配学生
        </div>
      </div>

      <!-- ========== 底部操作栏 ========== -->
      <div v-if="!hasNotStarted" class="th-footer">
        <button class="th-footer-btn" @click="handleAIGradeAll" :disabled="gradingAI">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none">
            <path d="M12 2L2 7l10 5 10-5-10-5z" stroke="currentColor" stroke-width="1.5"/>
            <path d="M2 17l10 5 10-5" stroke="currentColor" stroke-width="1.5"/>
            <path d="M2 12l10 5 10-5" stroke="currentColor" stroke-width="1.5"/>
          </svg>
          {{ gradingAI ? '批改中...' : 'AI 一键批改' }}
        </button>
        <button class="th-footer-btn warn" @click="handleUrgeAll" :disabled="urgingAll">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none">
            <path d="M22 2L11 13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <path d="M22 2l-7 20-4-9-9-4 20-7z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          {{ urgingAll ? '发送中...' : '一键催交' }}
        </button>
      </div>

      <!-- ========== 评分弹窗 ========== -->
      <Teleport to="body">
        <div v-if="showGrade" class="th-overlay" @click.self="showGrade = false">
          <div class="th-dialog">
            <div class="th-dialog-header">
              <h3>批阅 — {{ gradeTarget?.userName || gradeTarget?.studentId }}</h3>
              <button class="th-dialog-close" @click="showGrade = false">&times;</button>
            </div>
            <form class="th-dialog-body" @submit.prevent="handleGrade">
              <div class="th-field">
                <label>学生提交内容</label>
                <div class="th-submit-content">{{ gradeTarget?.submitContent || '无文本内容' }}</div>
              </div>
              <div v-if="gradeTarget?.files?.length" class="th-field">
                <label>提交的文件</label>
                <div class="th-file-list">
                  <a v-for="(f, fi) in gradeTarget.files" :key="fi" :href="f.filePath" target="_blank" class="th-file-link">
                    <svg viewBox="0 0 24 24" width="14" height="14"><path d="M13 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z" fill="none" stroke="currentColor" stroke-width="2"/><polyline points="13 2 13 9 20 9" fill="none" stroke="currentColor" stroke-width="2"/></svg>
                    {{ f.fileName }}
                  </a>
                </div>
              </div>
              <div class="th-field">
                <label>分数 <span class="th-req">*</span>（满分 {{ gradeTarget?.totalScore || 100 }}）</label>
                <input v-model.number="gradeForm.score" type="number" min="0" :max="gradeTarget?.totalScore || 100" placeholder="请输入分数" class="th-input" required />
              </div>
              <div class="th-field">
                <label>评语</label>
                <textarea v-model="gradeForm.comment" rows="3" placeholder="可选..." class="th-input th-textarea"></textarea>
              </div>
              <p v-if="gradeError" class="th-grade-error">{{ gradeError }}</p>
              <div class="th-dialog-actions">
                <button type="button" class="th-btn-cancel" @click="showGrade = false">取消</button>
                <button type="submit" class="th-btn-submit" :disabled="gradingSubmitting">
                  {{ gradingSubmitting ? '提交中...' : '确认评分' }}
                </button>
              </div>
            </form>
          </div>
        </div>
      </Teleport>
      </template>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { api } from '../api/request.js'
import { fmtDeadline } from '../utils/format.js'
import StudentHomeworkDetail from './StudentHomeworkDetail.vue'

const props = defineProps({
  courseId: { type: [Number, String], required: true },
  hwId: { type: [Number, String], required: true }
})

const emit = defineEmits(['back'])

const activeTab = ref('students')

const loading = ref(true)
const error = ref('')
const homework = ref({})
const students = ref([])
const keyword = ref('')
const showSettings = ref(false)
const urgingAll = ref(false)
const gradingAI = ref(false)

// 作业是否尚未开始
const hasNotStarted = computed(() => {
  if (!homework.value.startTime) return false
  return new Date(homework.value.startTime) > new Date()
})

// ========== 修改截止时间 ==========
const editingDeadline = ref(false)
const editDeadlineValue = ref('')

function startEditDeadline() {
  showSettings.value = false
  setEditValue(homework.value.deadline, editDeadlineValue)
  editingDeadline.value = true
}

async function saveDeadline() {
  editingDeadline.value = false
  if (!editDeadlineValue.value) return
  const ts = Math.floor(new Date(editDeadlineValue.value).getTime() / 1000)
  try {
    await api.updateHomeworkTime(props.hwId, null, ts)
    homework.value.deadline = new Date(ts * 1000).toISOString()
  } catch (e) {
    alert(e.message || '修改失败')
  }
}

// ========== 修改开始时间 ==========
const editingStartTime = ref(false)
const editStartTimeValue = ref('')

function startEditStartTime() {
  showSettings.value = false
  setEditValue(homework.value.startTime, editStartTimeValue)
  editingStartTime.value = true
}

async function saveStartTime() {
  editingStartTime.value = false
  if (!editStartTimeValue.value) return
  const ts = Math.floor(new Date(editStartTimeValue.value).getTime() / 1000)
  try {
    await api.updateHomeworkTime(props.hwId, ts, null)
    homework.value.startTime = new Date(ts * 1000).toISOString()
  } catch (e) {
    alert(e.message || '修改失败')
  }
}

function setEditValue(dateVal, refVal) {
  if (dateVal) {
    const d = new Date(dateVal)
    d.setMinutes(d.getMinutes() - d.getTimezoneOffset())
    refVal.value = d.toISOString().slice(0, 16)
  } else {
    refVal.value = ''
  }
}

// ========== 评分 ==========
const showGrade = ref(false)
const gradeTarget = ref(null)
const gradingSubmitting = ref(false)
const gradeError = ref('')
const gradeForm = reactive({ score: '', comment: '' })

function openGrade(s) {
  gradeTarget.value = s
  gradeForm.score = s.score != null ? String(s.score) : ''
  gradeForm.comment = s.comment || ''
  gradeError.value = ''
  showGrade.value = true
}

async function handleGrade() {
  if (gradeForm.score === '' || gradeForm.score == null) {
    gradeError.value = '请输入分数'
    return
  }
  gradingSubmitting.value = true
  gradeError.value = ''
  try {
    await api.gradeHomework(gradeTarget.value.submitId, {
      score: Number(gradeForm.score),
      comment: gradeForm.comment || ''
    })
    showGrade.value = false
    // 刷新数据
    await loadData()
  } catch (e) {
    gradeError.value = e.message
  } finally {
    gradingSubmitting.value = false
  }
}

function handleUrge(s) {
  if (!confirm('确定向 ' + (s.userName || s.studentId) + ' 发送催交通知？')) return
  api.urgeHomework(props.hwId, s.studentId).then(() => {
    alert('催交通知已发送')
  }).catch(e => {
    alert('发送失败：' + e.message)
  })
}

async function handleUrgeAll() {
  if (!confirm('确定向所有未提交学生发送催交通知？')) return
  urgingAll.value = true
  try {
    const res = await api.urgeAllHomework(props.hwId)
    alert(res.message || '催交已发送')
  } catch (e) {
    alert('发送失败：' + e.message)
  } finally {
    urgingAll.value = false
  }
}

async function handleAIGradeAll() {
  const ungraded = students.value.filter(s => s.submitted && s.score == null).length
  if (ungraded === 0) {
    alert('没有待批改的提交')
    return
  }
  if (!confirm('确定对 ' + ungraded + ' 份未批改提交进行AI自动批改？')) return
  gradingAI.value = true
  try {
    const res = await api.gradeAIBatch(props.hwId)
    alert(res.data || res.message || '批改完成')
    await loadData()
  } catch (e) {
    alert('批改失败：' + e.message)
  } finally {
    gradingAI.value = false
  }
}

const filteredStudents = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return students.value
  return students.value.filter(s => {
    const id = String(s.studentId || '')
    const name = (s.userName || '').toLowerCase()
    return id.includes(kw) || name.includes(kw)
  })
})

const submittedCount = computed(() => students.value.filter(s => s.submitted).length)
const gradedCount = computed(() => students.value.filter(s => s.score != null).length)
const unsubmittedCount = computed(() => students.value.filter(s => !s.submitted).length)

function formatDeadline(d) {
  if (!d) return '无截止日期'
  const dt = new Date(d)
  return `${dt.getMonth() + 1}月${dt.getDate()}日 ${String(dt.getHours()).padStart(2, '0')}:${String(dt.getMinutes()).padStart(2, '0')}`
}

function formatTime(d) {
  if (!d) return '-'
  const dt = new Date(d)
  return `${dt.getMonth() + 1}/${dt.getDate()} ${String(dt.getHours()).padStart(2, '0')}:${String(dt.getMinutes()).padStart(2, '0')}`
}

async function loadData() {
  loading.value = true
  error.value = ''
  try {
    const [gradingRes, hwRes] = await Promise.all([
      api.getHomeworkGrading(props.hwId),
      api.getHomework(props.hwId)
    ])
    students.value = gradingRes.data || []
    homework.value = hwRes.data || {}
  } catch (e) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(() => { loadData() })
</script>

<style scoped>
.teacher-hw-page {
  width: 100%;
  max-width: 740px;
  margin: 0 auto;
  min-height: 100vh;
  background: #f8f9fb;
  padding-bottom: 32px;
}

/* 状态 */
.th-status { text-align: center; padding: 80px 0; color: #999; font-size: 14px; }
.th-error { color: #e74c3c; }

/* ====== 顶部导航 ====== */
.th-topbar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 10px 16px;
  background: #fff;
  border-bottom: 1px solid #edf0f5;
}
.th-back {
  width: 30px; height: 30px;
  display: flex; align-items: center; justify-content: center;
  border: none; border-radius: 8px;
  background: #f0f2f7; color: #555;
  cursor: pointer; flex-shrink: 0;
}
.th-back:hover { background: #e4e7ed; }
.th-tabs { display: flex; gap: 6px; }
.th-tab {
  padding: 6px 14px; border: none; border-radius: 6px;
  background: transparent; font-size: 13px; color: #888;
  cursor: pointer; transition: all .15s;
}
.th-tab.active {
  background: #e8f0fe; color: #2377E4; font-weight: 500;
}

/* ====== 标题 ====== */
.th-header { padding: 20px 20px 4px; }
.th-title { font-size: 22px; font-weight: 700; color: #1a1a1a; }

/* ====== 操作栏 ====== */
.th-actions {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 20px; flex-wrap: wrap; gap: 10px;
}
.th-time-group { display: flex; align-items: center; gap: 8px; }
.th-time-tag {
  display: flex; align-items: center; gap: 4px;
  font-size: 12px; padding: 4px 10px; border-radius: 6px;
}
.th-time-tag.start {
  color: #059669; background: #ecfdf5;
}
.th-time-tag.end {
  color: #e74c3c; background: #fef2f2;
}
.th-time-input {
  width: 160px; padding: 2px 6px; font-size: 12px;
  border: 1px solid #e74c3c; border-radius: 4px; outline: none;
  background: #fff;
}
.th-time-input:focus { box-shadow: 0 0 0 2px rgba(231,76,60,.2); }
.th-actions-right { display: flex; align-items: center; gap: 8px; }

.th-dropdown-wrapper { position: relative; }
.th-dropdown {
  position: absolute; right: 0; top: 36px;
  background: #fff; border-radius: 10px;
  box-shadow: 0 4px 20px rgba(0,0,0,.1);
  padding: 6px; z-index: 20; min-width: 140px;
}
.th-dropdown-item {
  display: block; width: 100%;
  padding: 10px 14px; border: none; border-radius: 8px;
  background: transparent; font-size: 13px; color: #333;
  cursor: pointer; text-align: left; transition: background .1s;
}
.th-dropdown-item:hover { background: #f5f6fa; }
.th-dropdown-item.danger { color: #e74c3c; }

.th-btn {
  height: 34px; padding: 0 14px; border: none; border-radius: 8px;
  font-size: 13px; font-weight: 500; cursor: pointer;
  display: flex; align-items: center; gap: 5px;
  transition: all .15s;
}
.th-btn-outline {
  background: #fff; color: #555; border: 1px solid #e0e0e0;
}
.th-btn-outline:hover { background: #f5f6fa; }
.th-btn-primary {
  background: #2377E4; color: #fff;
}
.th-btn-primary:hover { background: #1a5fc4; }

/* 搜索栏 */
.th-search-bar {
  display: flex; align-items: center; gap: 8px;
  margin: 0 20px 12px; padding: 0 12px;
  height: 38px; background: #fff; border-radius: 8px;
  border: 1px solid #edf0f5;
}
.th-search-icon { flex-shrink: 0; color: #bbb; }
.th-search-input {
  flex: 1; border: none; outline: none;
  font-size: 13px; color: #333; background: transparent;
}
.th-search-input::placeholder { color: #ccc; }
.th-search-clear {
  width: 20px; height: 20px; display: flex; align-items: center; justify-content: center;
  border: none; border-radius: 50%; background: #e0e0e0; color: #666; cursor: pointer;
}

/* ====== 统计 ====== */
.th-stats {
  display: flex; gap: 0; margin: 0 20px 16px;
  background: #fff; border-radius: 10px;
  overflow: hidden; border: 1px solid #edf0f5;
}
.th-stat {
  flex: 1; text-align: center; padding: 14px 8px;
  border-right: 1px solid #edf0f5;
}
.th-stat:last-child { border-right: none; }
.th-stat-num { font-size: 20px; font-weight: 700; color: #333; display: block; }
.th-stat-num.green { color: #059669; }
.th-stat-num.blue { color: #2377E4; }
.th-stat-num.red { color: #e74c3c; }
.th-stat-label { font-size: 11px; color: #999; margin-top: 2px; display: block; }

/* ====== 表格 ====== */
.th-table {
  margin: 0 20px;
  background: #fff; border-radius: 10px;
  border: 1px solid #edf0f5;
  overflow: hidden;
}
.th-table-head {
  display: flex; align-items: center;
  padding: 10px 16px; background: #fafbfc;
  border-bottom: 1px solid #edf0f5;
  font-size: 12px; color: #999; font-weight: 500;
}
.th-row {
  display: flex; align-items: center;
  padding: 12px 16px; border-bottom: 1px solid #f5f5f5;
  transition: background .1s;
}
.th-row:last-child { border-bottom: none; }
.th-row:hover { background: #fafbfc; }
.th-row.row-unsubmitted { background: #fefdf6; }

.th-col-id { width: 90px; font-size: 13px; color: #666; flex-shrink: 0; }
.th-col-name { flex: 1; font-size: 13px; color: #333; font-weight: 500; min-width: 0; }
.th-col-score { width: 80px; text-align: center; flex-shrink: 0; }
.th-col-time { width: 100px; text-align: center; font-size: 12px; color: #999; flex-shrink: 0; }
.th-col-action { width: 82px; text-align: right; flex-shrink: 0; }

.th-score-val { font-size: 14px; font-weight: 600; color: #059669; }
.th-score-total { font-size: 12px; color: #bbb; }

.th-tag {
  display: inline-block; font-size: 11px; padding: 2px 8px; border-radius: 4px; font-weight: 500;
}
.th-tag.danger { color: #e74c3c; background: #fef2f2; }
.th-tag.warn { color: #b45309; background: #fffbeb; }

.th-action-btn {
  height: 28px; padding: 0 10px; border: none; border-radius: 6px;
  font-size: 11px; font-weight: 500; cursor: pointer; transition: all .15s;
}
.th-action-btn.primary { background: #e8f0fe; color: #2377E4; }
.th-action-btn.primary:hover { background: #2377E4; color: #fff; }
.th-action-btn.warn { background: #fef2f2; color: #e74c3c; }
.th-action-btn.warn:hover { background: #e74c3c; color: #fff; }
.th-action-btn.ghost { background: #f8fafc; color: #64748b; }
.th-action-btn.ghost:hover { background: #f1f5f9; }

.th-file-badge {
  display: inline-flex; align-items: center; gap: 4px;
  font-size: 11px; color: #3b82f6; margin-top: 4px; cursor: default;
}

.th-not-started-tip {
  font-size: 11px;
  color: #9ca3af;
  font-style: italic;
}

.th-empty {
  text-align: center; padding: 32px 0; color: #bbb; font-size: 13px;
}

/* ====== 底部 ====== */
.th-footer {
  display: flex; gap: 12px; padding: 20px;
}
.th-footer-btn {
  flex: 1; height: 44px; display: flex; align-items: center; justify-content: center; gap: 8px;
  border: none; border-radius: 10px; font-size: 14px; font-weight: 500; cursor: pointer;
  transition: all .15s;
  background: #e8f0fe; color: #2377E4;
}
.th-footer-btn:hover { background: #d0e2fd; }
.th-footer-btn.warn { background: #fef2f2; color: #e74c3c; }
.th-footer-btn.warn:hover { background: #fde2e2; }
</style>

<!-- ===== 批阅弹窗样式（非 scoped，Teleport 到 body） ===== -->
<style>
.th-overlay {
  position: fixed; inset: 0; background: rgba(15,23,42,.5);
  display: flex; justify-content: center; align-items: flex-start;
  padding: 30px 20px; z-index: 1000; overflow-y: auto;
}
.th-dialog {
  background: #fff; border-radius: 14px; width: 560px; max-width: 95vw;
  box-shadow: 0 25px 60px rgba(0,0,0,.18);
}
.th-dialog-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 20px 28px; border-bottom: 1px solid #e2e8f0;
}
.th-dialog-header h3 { font-size: 18px; font-weight: 700; color: #1e293b; }
.th-dialog-close {
  width: 34px; height: 34px; display: flex; align-items: center; justify-content: center;
  background: none; border: none; font-size: 22px; color: #94a3b8; cursor: pointer; border-radius: 8px;
}
.th-dialog-close:hover { background: #f8fafc; color: #1e293b; }
.th-dialog-body { padding: 24px 28px; max-height: 60vh; overflow-y: auto; }
.th-dialog-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 20px; }
.th-field { margin-bottom: 16px; }
.th-field label { display: block; font-size: 13px; font-weight: 600; color: #1e293b; margin-bottom: 6px; }
.th-req { color: #ef4444; }
.th-input {
  width: 100%; padding: 9px 14px; border: 1px solid #e2e8f0; border-radius: 8px;
  font-size: 13px; color: #1e293b; outline: none; transition: border-color .2s; background: #fff; box-sizing: border-box;
}
.th-input:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,.1); }
.th-textarea { resize: vertical; font-family: inherit; }
.th-submit-content {
  padding: 12px; background: #f8fafc; border-radius: 8px; border: 1px solid #e2e8f0;
  font-size: 13px; color: #475569; white-space: pre-wrap; max-height: 200px; overflow-y: auto;
}
.th-grade-error { color: #ef4444; font-size: 13px; margin-top: 8px; }
.th-btn-cancel {
  padding: 10px 24px; background: #f8fafc; border: none; border-radius: 8px; font-size: 14px; color: #64748b; cursor: pointer;
}
.th-btn-submit {
  padding: 10px 28px; background: #3b82f6; color: #fff; border: none; border-radius: 8px; font-size: 14px; font-weight: 700; cursor: pointer; transition: background .2s;
}
.th-btn-submit:hover { background: #2563eb; }
.th-btn-submit:disabled { background: #a5b4fc; cursor: not-allowed; }

.th-file-list {
  display: flex; flex-direction: column; gap: 8px;
}
.th-file-link {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 8px 12px; background: #f0f9ff; border: 1px solid #bae6fd; border-radius: 8px;
  font-size: 13px; color: #0369a1; text-decoration: none; transition: background .15s; word-break: break-all;
}
.th-file-link:hover { background: #e0f2fe; }
</style>
