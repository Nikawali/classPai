<template>
  <!-- ================================================================
   教师端作业管理页 — CourseHomework.vue
   所有字段、接口完全对齐后端 HomeworkController / HomeworkService
   后端 Homework 实体字段：hwId, courseId, title, content, startTime,
     deadline, createTime, submitCount, gradedCount, totalStudents, totalScore
   后端接口：
     GET  /api/homework/course/{courseId}    列表
     GET  /api/homework/{hwId}               详情
     POST /api/homework/course/{courseId}    创建(FormData)
     GET  /api/homework/{hwId}/files         附件列表
     GET  /api/homework/{hwId}/grading       批阅数据
   ================================================================ -->
  <div class="course-homework">
    <!-- ========== 返回栏 ========== -->
    <div class="back-bar">
      <button class="btn-back" @click="$emit('back')">
        <svg viewBox="0 0 24 24" width="20" height="20"><path d="M15 18l-6-6 6-6" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
        <span>{{ courseName }}</span>
      </button>
    </div>

    <!-- ========== 课程 Tab 栏 ========== -->
    <div class="course-tabs">
      <div class="course-tabs-left">
        <button v-for="t in courseTabs" :key="t" class="course-tab" :class="{ active: activeCourseTab === t }" @click="activeCourseTab = t">{{ t }}</button>
      </div>
      <div class="course-tabs-right">
        <span class="course-code-label">课程码：{{ courseCode }}</span>
      </div>
    </div>

    <!-- ========== 作业列表区域 ========== -->
    <div v-if="!viewingDetail">
    <!-- ========== 操作栏 ========== -->
    <div class="hw-toolbar">
      <span class="hw-summary">共 {{ totalCount }} 份作业</span>
      <button class="btn-publish" @click="openCreate">
        <svg viewBox="0 0 24 24" width="16" height="16"><line x1="12" y1="5" x2="12" y2="19" stroke="currentColor" stroke-width="2" stroke-linecap="round"/><line x1="5" y1="12" x2="19" y2="12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
        发布作业
      </button>
    </div>

    <!-- ========== 作业列表表格 ========== -->
    <div class="hw-table-wrap">
      <table class="hw-table">
        <thead>
          <tr>
            <th style="width:30%">作业名称</th>
            <th style="width:15%">开始时间</th>
            <th style="width:15%">截止时间</th>
            <th style="width:18%">提交进度</th>
            <th style="width:22%">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="5" class="empty-cell"><div class="empty-state"><p>加载中...</p></div></td>
          </tr>
          <tr v-else-if="homeworkList.length === 0">
            <td colspan="5" class="empty-cell">
              <div class="empty-state">
                <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="#cbd5e1" stroke-width="1.5">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                  <line x1="12" y1="12" x2="12" y2="18"/><line x1="9" y1="15" x2="15" y2="15"/>
                </svg>
                <p>暂无作业，请点击"发布作业"创建</p>
              </div>
            </td>
          </tr>
          <tr v-for="hw in homeworkList" :key="hw.hwId" style="cursor:pointer" @click="detailHwId = hw.hwId; viewingDetail = true">
            <td><span class="hw-name">{{ hw.title }}</span></td>
            <td>{{ fmtShort(hw.startTime) }}</td>
            <td>{{ fmtShort(hw.deadline) }}</td>
            <td>
              <div class="progress-cell">
                <div class="progress-bar"><div class="progress-fill" :style="{width:progressPct(hw)+'%'}"></div></div>
                <span class="progress-text">已交{{ hw.submitCount || 0 }}/{{ hw.totalStudents || 0 }} &nbsp; 已批{{ hw.gradedCount || 0 }}</span>
              </div>
            </td>
            <td @click.stop>
              <div class="action-btns">
                <button class="act-btn" title="查看详情" @click="detailHwId = hw.hwId; viewingDetail = true"><svg viewBox="0 0 24 24" width="15" height="15"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" fill="none" stroke="currentColor" stroke-width="2"/><circle cx="12" cy="12" r="3" fill="none" stroke="currentColor" stroke-width="2"/></svg></button>
                <button class="act-btn" title="查看附件" @click="viewFiles(hw)"><svg viewBox="0 0 24 24" width="15" height="15"><path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg></button>
                <button class="act-btn" title="批阅" @click="viewGrading(hw)"><svg viewBox="0 0 24 24" width="15" height="15"><path d="M12 20h9" stroke="currentColor" stroke-width="2" stroke-linecap="round"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z" fill="none" stroke="currentColor" stroke-width="2"/></svg></button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- 分页 -->
      <div class="pagination" v-if="totalPages > 1">
        <button :disabled="page <= 1" @click="changePage(page-1)">上一页</button>
        <span v-for="p in totalPages" :key="p" class="page-num" :class="{active:p===page}" @click="changePage(p)">{{ p }}</span>
        <button :disabled="page >= totalPages" @click="changePage(page+1)">下一页</button>
      </div>
    </div>
    </div>

    <!-- ========== 创建作业弹窗 ========== -->
    <Teleport to="body">
      <div v-if="showCreate" class="cw-overlay" @click.self="showCreate=false">
        <div class="cw-dialog">
          <div class="cw-dialog-header">
            <h3>发布新作业</h3>
            <button class="cw-dialog-close" @click="showCreate=false">&times;</button>
          </div>
          <form class="cw-dialog-body" @submit.prevent="handleCreate">
            <div class="cw-field">
              <label>作业标题 <span class="cw-req">*</span></label>
              <input v-model="createForm.title" type="text" placeholder="请输入作业标题" class="cw-input" required />
            </div>
            <div class="cw-field-row">
              <div class="cw-field">
                <label>满分</label>
                <input v-model.number="createForm.totalScore" type="number" min="1" placeholder="默认100" class="cw-input" style="width:120px" />
              </div>
              <div class="cw-field">
                <label>最大提交次数</label>
                <input v-model.number="createForm.maxSubmissions" type="number" min="1" placeholder="不限制" class="cw-input" style="width:120px" />
              </div>
            </div>
            <div class="cw-field">
              <label>作业内容</label>
              <textarea v-model="createForm.content" rows="6" placeholder="请输入作业内容描述..." class="cw-input cw-textarea"></textarea>
            </div>
            <div class="cw-field-row">
              <div class="cw-field">
                <label>开始时间</label>
                <input v-model="createForm.startTime" type="datetime-local" class="cw-input" />
              </div>
              <div class="cw-field">
                <label>截止时间 <span class="cw-req">*</span></label>
                <input v-model="createForm.deadline" type="datetime-local" class="cw-input" placeholder="请选择截止时间" required />
              </div>
            </div>
            <div class="cw-field">
              <label>附件（选填，支持多文件）</label>
              <div class="cw-attach-area">
                <label class="cw-attach-btn">
                  <svg viewBox="0 0 24 24" width="16" height="16"><path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
                  选择文件
                  <input type="file" multiple hidden @change="onFilesSelected" />
                </label>
                <span class="cw-attach-hint" v-if="createForm.files.length === 0">支持文档、压缩包、图片等</span>
              </div>
              <ul v-if="createForm.files.length" class="cw-attach-list">
                <li v-for="(f,i) in createForm.files" :key="i" class="cw-attach-item">
                  <span class="cw-attach-name">{{ f.name }}</span>
                  <span class="cw-attach-size">{{ fmtSize(f.size) }}</span>
                  <button type="button" class="cw-attach-del" @click="createForm.files.splice(i,1)">&times;</button>
                </li>
              </ul>
            </div>
            <p v-if="createError" class="cw-error">{{ createError }}</p>
            <div class="cw-dialog-actions">
              <button type="button" class="cw-btn-cancel" @click="showCreate=false">取消</button>
              <button type="submit" class="cw-btn-submit" :disabled="submitting">{{ submitting ? '发布中...' : '发 布' }}</button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>

    <!-- ========== 教师作业详情（复用学生端组件） ========== -->
    <StudentHomeworkDetail
      v-if="viewingDetail"
      :courseId="courseId"
      :homeworkId="detailHwId"
      role="teacher"
      @back="viewingDetail = false"
    />

    <!-- ========== 附件列表弹窗 ========== -->
    <Teleport to="body">
      <div v-if="showFiles" class="cw-overlay" @click.self="showFiles=false">
        <div class="cw-dialog">
          <div class="cw-dialog-header">
            <h3>附件列表 — {{ filesHwTitle }}</h3>
            <button class="cw-dialog-close" @click="showFiles=false">&times;</button>
          </div>
          <div class="cw-dialog-body">
            <div v-if="filesLoading" class="cw-empty-state">加载中...</div>
            <div v-else-if="currentFiles.length===0" class="cw-empty-state">暂无附件</div>
            <ul v-else class="cw-file-list">
              <li v-for="f in currentFiles" :key="f.fileId" class="cw-file-item">
                <span>{{ f.originalName }}</span>
                <span class="cw-file-size">{{ fmtSize(f.fileSize) }}</span>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- ========== 批阅弹窗 ========== -->
    <Teleport to="body">
      <div v-if="showGrading" class="cw-overlay" @click.self="showGrading=false">
        <div class="cw-dialog cw-dialog-xwide">
          <div class="cw-dialog-header">
            <h3>批阅 — {{ gradingHwTitle }}</h3>
            <button class="cw-dialog-close" @click="showGrading=false">&times;</button>
          </div>
          <div class="cw-dialog-body">
            <div v-if="gradingLoading" class="cw-empty-state">加载中...</div>
            <div v-else-if="gradingList.length===0" class="cw-empty-state">暂无提交</div>
            <table v-else class="cw-grading-table">
              <thead>
                <tr><th>学生</th><th>提交时间</th><th>得分</th><th>操作</th></tr>
              </thead>
              <tbody>
                <tr v-for="g in gradingList" :key="g.submitId || g.studentId">
                  <td>{{ g.studentName || g.studentId }}</td>
                  <td>{{ fmtShort(g.submitTime) || '未提交' }}</td>
                  <td>{{ g.score != null ? g.score + '分' : '未批' }}</td>
                  <td>
                    <button v-if="g.submitId && g.score == null" class="act-btn" title="评分" @click="openGrade(g)">评分</button>
                    <span v-else-if="g.score == null">--</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- ========== 评分小弹窗 ========== -->
    <Teleport to="body">
      <div v-if="showGrade" class="cw-overlay" @click.self="showGrade=false">
        <div class="cw-dialog" style="width:400px">
          <div class="cw-dialog-header">
            <h3>评分</h3>
            <button class="cw-dialog-close" @click="showGrade=false">&times;</button>
          </div>
          <form class="cw-dialog-body" @submit.prevent="handleGrade">
            <div class="cw-field">
              <label>分数 <span class="cw-req">*</span></label>
              <input v-model.number="gradeForm.score" type="number" min="0" max="100" placeholder="0-100" class="cw-input" required />
            </div>
            <div class="cw-field">
              <label>评语</label>
              <textarea v-model="gradeForm.feedback" rows="3" placeholder="可选..." class="cw-input cw-textarea"></textarea>
            </div>
            <p v-if="gradeError" class="cw-error">{{ gradeError }}</p>
            <div class="cw-dialog-actions">
              <button type="button" class="cw-btn-cancel" @click="showGrade=false">取消</button>
              <button type="submit" class="cw-btn-submit" :disabled="gradingSubmitting">{{ gradingSubmitting ? '提交中...' : '确认' }}</button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
/**
 * CourseHomework.vue — 严格对齐后端 HomeworkController
 *
 * 数据模型（Homework 实体）：
 *   存储字段：hwId, courseId, title, content, startTime, deadline, createTime
 *   瞬态字段：submitCount, gradedCount, totalStudents, totalScore
 *
 * 后端 API 调用清单：
 *   GET  /api/homework/course/{courseId}?page=&pageSize=    → api.getHomeworkList
 *   POST /api/homework/course/{courseId}  (FormData)         → api.createHomework
 *   GET  /api/homework/{hwId}                                 → api.getHomework
 *   GET  /api/homework/{hwId}/files                           → api.getHomeworkFiles
 *   GET  /api/homework/{hwId}/grading                         → api.getHomeworkGrading
 *   PUT  /api/homework/submission/{submitId}/grade            → api.gradeHomework
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { api } from '../api/request.js'
import { fmtShort, fmtSize } from '../utils/format.js'
import StudentHomeworkDetail from './StudentHomeworkDetail.vue'

const props = defineProps({
  courseId:  { type: [String,Number], required: true },
  courseName:{ type: String, default: '课程名称' },
  courseCode:{ type: String, default: '' }
})
const emit = defineEmits(['back'])

// ========== Tab 状态 ==========
const courseTabs = ['作业', '资料', '公告', '话题', '成员']
const activeCourseTab = ref('作业')

// ========== 列表 ==========
const homeworkList = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = 10
const totalCount = ref(0)
const totalPages = computed(() => Math.ceil(totalCount.value / pageSize) || 1)

async function loadList() {
  loading.value = true
  try {
    const res = await api.getHomeworkList(props.courseId, page.value, pageSize)
    // 后端返回 Result<PageResult<Homework>>
    const dp = res.data || {}
    homeworkList.value = dp.records || []
    totalCount.value = dp.total || 0
  } catch (e) {
    console.error('获取作业列表失败:', e)
    homeworkList.value = []
    totalCount.value = 0
  } finally { loading.value = false }
}
function changePage(p) { page.value = p; loadList() }

// ========== 创建作业 ==========
const showCreate = ref(false)
const submitting = ref(false)
const createError = ref('')
const createForm = reactive({ title: '', content: '', startTime: '', deadline: '', maxSubmissions: '', totalScore: '', files: [] })

function openCreate() {
  createError.value = ''
  createForm.title = ''
  createForm.content = ''
  createForm.startTime = nowLocal()
  createForm.deadline = ''
  createForm.maxSubmissions = ''
  createForm.totalScore = ''
  createForm.files = []
  showCreate.value = true
}
function nowLocal() {
  const d = new Date()
  d.setMinutes(d.getMinutes() - d.getTimezoneOffset())
  return d.toISOString().slice(0, 16)
}
function onFilesSelected(e) {
  createForm.files = Array.from(e.target.files)
}
async function handleCreate() {
  if (!createForm.title.trim()) { createError.value = '请输入作业标题'; return }
  if (!createForm.deadline)     { createError.value = '请选择截止时间'; return }

  submitting.value = true
  createError.value = ''
  try {
    const fd = new FormData()
    fd.append('title',    createForm.title.trim())
    fd.append('content',  createForm.content.trim())
    if (createForm.startTime) {
      fd.append('startTime', String(Math.floor(new Date(createForm.startTime).getTime() / 1000)))
    }
    fd.append('deadline', String(Math.floor(new Date(createForm.deadline).getTime() / 1000)))
    if (createForm.maxSubmissions) fd.append('maxSubmissions', createForm.maxSubmissions)
    if (createForm.totalScore)     fd.append('totalScore', createForm.totalScore)
    createForm.files.forEach(f => fd.append('files', f))

    await api.createHomework(props.courseId, fd)
    showCreate.value = false
    loadList()
  } catch (e) {
    createError.value = e.message
  } finally { submitting.value = false }
}

// ========== 作业详情 ==========
const viewingDetail = ref(false)
const detailHwId = ref(null)

function viewDetail(hw) {
  detailHwId.value = hw.hwId
  viewingDetail.value = true
}

// ========== 附件列表 ==========
const showFiles = ref(false)
const filesHwTitle = ref('')
const currentFiles = ref([])
const filesLoading = ref(false)

async function viewFiles(hw) {
  filesHwTitle.value = hw.title
  showFiles.value = true
  filesLoading.value = true
  currentFiles.value = []
  try {
    const res = await api.getHomeworkFiles(hw.hwId)
    currentFiles.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    currentFiles.value = []
  } finally { filesLoading.value = false }
}

// ========== 批阅 ==========
const showGrading = ref(false)
const gradingHwTitle = ref('')
const gradingList = ref([])
const gradingLoading = ref(false)

async function viewGrading(hw) {
  gradingHwTitle.value = hw.title
  showGrading.value = true
  gradingLoading.value = true
  gradingList.value = []
  try {
    const res = await api.getHomeworkGrading(hw.hwId)
    gradingList.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    gradingList.value = []
  } finally { gradingLoading.value = false }
}

// ========== 评分 ==========
const showGrade = ref(false)
const gradingSubmitting = ref(false)
const gradeError = ref('')
const gradeTarget = ref(null)
const gradeForm = reactive({ score: '', feedback: '' })

function openGrade(gradingItem) {
  gradeTarget.value = gradingItem
  gradeForm.score = ''
  gradeForm.feedback = ''
  gradeError.value = ''
  showGrade.value = true
}
async function handleGrade() {
  if (gradeForm.score == null || gradeForm.score === '') { gradeError.value='请输入分数'; return }
  gradingSubmitting.value = true
  gradeError.value = ''
  try {
    await api.gradeHomework(gradeTarget.value.submitId, {
      score:    Number(gradeForm.score),
      feedback: gradeForm.feedback || ''
    })
    showGrade.value = false
    viewGrading({ hwId: gradeTarget.value.hwId, title: gradingHwTitle.value })
  } catch (e) {
    gradeError.value = e.message
  } finally { gradingSubmitting.value = false }
}

// ========== 工具 ==========
function progressPct(hw) {
  const total = hw.totalStudents || 0
  return total > 0 ? Math.round(((hw.submitCount || 0) / total) * 100) : 0
}

onMounted(loadList)
</script>

<style scoped>
/* ===== CSS 变量 ===== */
.course-homework {
  --primary: #3b82f6; --primary-dark: #2563eb; --primary-light: #eff6ff;
  --bg: #f8fafc; --card: #fff; --border: #e2e8f0;
  --text: #1e293b; --text-secondary: #64748b; --text-muted: #94a3b8;
  --danger: #ef4444; --success: #22c55e;
}

/* ===== 返回栏 ===== */
.back-bar { padding: 12px 28px; background: var(--card); border-bottom: 1px solid var(--border); }
.btn-back { display: flex; align-items: center; gap: 6px; background: none; border: none; font-size: 15px; color: var(--text); cursor: pointer; font-weight: 600; }
.btn-back:hover { color: var(--primary); }

/* ===== 课程 Tab ===== */
.course-tabs { display: flex; justify-content: space-between; align-items: center; padding: 0 28px; background: var(--card); border-bottom: 2px solid var(--border); height: 48px; }
.course-tabs-left { display: flex; }
.course-tab { padding: 12px 22px; background: none; border: none; font-size: 14px; color: var(--text-secondary); cursor: pointer; border-bottom: 2px solid transparent; margin-bottom: -2px; transition:all .2s; font-weight: 500; }
.course-tab:hover { color: var(--text); }
.course-tab.active { color: var(--primary); border-bottom-color: var(--primary); font-weight: 600; }
.course-code-label { font-size: 13px; color: var(--text-muted); }

/* ===== 工具栏 ===== */
.hw-toolbar { display: flex; justify-content: space-between; align-items: center; padding: 16px 28px; background: var(--bg); }
.hw-summary { font-size: 14px; color: var(--text-secondary); }
.btn-publish { display: flex; align-items: center; gap: 6px; padding: 9px 20px; background: var(--primary); color: #fff; border: none; border-radius: 8px; font-size: 14px; font-weight: 600; cursor: pointer; transition: background .2s; }
.btn-publish:hover { background: var(--primary-dark); }

/* ===== 表格 ===== */
.hw-table-wrap { padding: 0 28px 28px; }
.hw-table { width: 100%; border-collapse: collapse; background: var(--card); border-radius: 12px; overflow: hidden; box-shadow: 0 1px 3px rgba(0,0,0,.04); }
.hw-table thead { background: #f1f5f9; }
.hw-table th { padding: 14px 16px; font-size: 13px; font-weight: 600; color: var(--text-secondary); text-align: left; }
.hw-table td { padding: 14px 16px; font-size: 13px; color: var(--text); border-top: 1px solid var(--border); vertical-align: middle; }
.hw-table tbody tr:hover { background: var(--primary-light); }
.hw-name { font-weight: 600; }
.progress-cell { display: flex; align-items: center; gap: 8px; }
.progress-bar { width: 60px; height: 6px; background: #e2e8f0; border-radius: 3px; overflow: hidden; flex-shrink: 0; }
.progress-fill { height: 100%; background: var(--primary); border-radius: 3px; }
.progress-text { font-size: 12px; color: var(--text-secondary); white-space: nowrap; }
.action-btns { display: flex; gap: 4px; }
.act-btn { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; background: none; border: 1px solid var(--border); border-radius: 6px; cursor: pointer; color: var(--text-secondary); transition: all .15s; }
.act-btn:hover { border-color: var(--primary); color: var(--primary); background: var(--primary-light); }
.empty-cell { text-align: center; padding: 48px 0 !important; }
.empty-state { display: flex; flex-direction: column; align-items: center; gap: 12px; color: var(--text-muted); font-size: 14px; }

/* ===== 分页 ===== */
.pagination { display: flex; justify-content: center; align-items: center; gap: 6px; margin-top: 20px; }
.pagination button { padding: 6px 14px; border: 1px solid var(--border); border-radius: 6px; background: var(--card); cursor: pointer; font-size: 13px; color: var(--text-secondary); }
.pagination button:hover:not(:disabled) { border-color: var(--primary); color: var(--primary); }
.pagination button:disabled { opacity: .4; cursor: not-allowed; }
.page-num { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; border-radius: 6px; cursor: pointer; font-size: 13px; color: var(--text-secondary); }
.page-num.active { background: var(--primary); color: #fff; }
</style>

<!-- ===== 弹窗/表单样式（非 scoped，因为 Teleport 到 body） ===== -->
<style>
/* 复用 CourseHomework 的 CSS 变量（硬编码） */
.cw-overlay { position: fixed; inset: 0; background: rgba(15,23,42,.5); display: flex; justify-content: center; align-items: flex-start; padding: 30px 20px; z-index: 1000; overflow-y: auto; }
.cw-dialog { background: #fff; border-radius: 14px; width: 640px; max-width: 95vw; box-shadow: 0 25px 60px rgba(0,0,0,.18); }
.cw-dialog-wide { width: 700px; }
.cw-dialog-xwide { width: 780px; }
.cw-dialog-header { display: flex; justify-content: space-between; align-items: center; padding: 20px 28px; border-bottom: 1px solid #e2e8f0; }
.cw-dialog-header h3 { font-size: 18px; font-weight: 700; color: #1e293b; }
.cw-dialog-close { width: 34px; height: 34px; display: flex; align-items: center; justify-content: center; background: none; border: none; font-size: 22px; color: #94a3b8; cursor: pointer; border-radius: 8px; }
.cw-dialog-close:hover { background: #f8fafc; color: #1e293b; }
.cw-dialog-body { padding: 24px 28px; max-height: 60vh; overflow-y: auto; }
.cw-dialog-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 20px; }
.cw-field { margin-bottom: 16px; }
.cw-field label { display: block; font-size: 13px; font-weight: 600; color: #1e293b; margin-bottom: 6px; }
.cw-req { color: #ef4444; }
.cw-field-row { display: flex; gap: 20px; }
.cw-field-row .cw-field { flex: 1; }
.cw-input { width: 100%; padding: 9px 14px; border: 1px solid #e2e8f0; border-radius: 8px; font-size: 13px; color: #1e293b; outline: none; transition: border-color .2s; background: #fff; box-sizing: border-box; }
.cw-input:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,.1); }
.cw-textarea { resize: vertical; font-family: inherit; }
.cw-error { color: #ef4444; font-size: 13px; margin-top: 8px; }
.cw-btn-cancel { padding: 10px 24px; background: #f8fafc; border: none; border-radius: 8px; font-size: 14px; color: #64748b; cursor: pointer; }
.cw-btn-submit { padding: 10px 28px; background: #3b82f6; color: #fff; border: none; border-radius: 8px; font-size: 14px; font-weight: 700; cursor: pointer; transition: background .2s; }
.cw-btn-submit:hover { background: #2563eb; }
.cw-btn-submit:disabled { background: #a5b4fc; cursor: not-allowed; }
.cw-attach-area { display: flex; align-items: center; gap: 14px; }
.cw-attach-btn { display: flex; align-items: center; gap: 6px; padding: 8px 16px; border: 1px dashed #3b82f6; border-radius: 8px; font-size: 13px; color: #3b82f6; cursor: pointer; background: #eff6ff; }
.cw-attach-btn:hover { background: #dbeafe; }
.cw-attach-hint { font-size: 12px; color: #94a3b8; }
.cw-attach-list { list-style: none; margin-top: 10px; }
.cw-attach-item { display: flex; align-items: center; gap: 8px; padding: 8px 12px; background: #f8fafc; border-radius: 6px; margin-bottom: 6px; font-size: 13px; }
.cw-attach-name { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.cw-attach-size { color: #94a3b8; font-size: 12px; }
.cw-attach-del { background: none; border: none; font-size: 18px; color: #94a3b8; cursor: pointer; }
.cw-detail-meta { display: flex; flex-wrap: wrap; gap: 16px; padding-bottom: 14px; border-bottom: 1px solid #e2e8f0; margin-bottom: 14px; font-size: 13px; color: #64748b; }
.cw-detail-section { margin-bottom: 16px; }
.cw-detail-section h4 { font-size: 14px; font-weight: 600; color: #1e293b; margin-bottom: 8px; }
.cw-detail-content { font-size: 14px; line-height: 1.7; color: #1e293b; white-space: pre-wrap; }
.cw-file-list { list-style: none; }
.cw-file-item { display: flex; align-items: center; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #e2e8f0; font-size: 13px; color: #1e293b; }
.cw-file-size { color: #94a3b8; font-size: 12px; }
.cw-grading-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.cw-grading-table th { text-align: left; padding: 8px 10px; background: #f1f5f9; color: #64748b; font-weight: 600; }
.cw-grading-table td { padding: 10px; border-top: 1px solid #e2e8f0; color: #1e293b; }
.cw-empty-state { display: flex; flex-direction: column; align-items: center; gap: 12px; color: #94a3b8; font-size: 14px; }
</style>
