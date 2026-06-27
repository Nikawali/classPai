<template>
  <!-- ================================================================
   学生端作业详情页 — StudentHomeworkDetail.vue
   复用 CourseDetail.vue 的样式变量、布局结构
   接口：GET /api/homework/{hwId}/student → StudentHomeworkVO
   ================================================================ -->
  <div class="shd-page" v-if="!loading && !error && detail">
    <!-- ========== 返回栏 ========== -->
    <div class="back-bar">
      <button class="btn-back" @click="goBack">
        <svg viewBox="0 0 24 24" width="20" height="20"><path d="M15 18l-6-6 6-6" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
        <span>返回课程</span>
      </button>
    </div>

    <!-- ========== 作业信息与状态栏 ========== -->
    <div class="shd-card">
      <div class="shd-card-header">
        <h2 class="shd-title">{{ detail.title }}</h2>
        <span class="shd-status" :class="'status-' + detail.submitStatus">{{ statusText }}</span>
      </div>
      <div class="shd-meta">
        <span class="shd-meta-item">
          <svg viewBox="0 0 24 24" width="14" height="14"><rect x="3" y="4" width="18" height="18" rx="2" fill="none" stroke="currentColor" stroke-width="2"/><line x1="16" y1="2" x2="16" y2="6" stroke="currentColor" stroke-width="2"/><line x1="8" y1="2" x2="8" y2="6" stroke="currentColor" stroke-width="2"/></svg>
          所属课程：{{ detail.courseName || '--' }}
        </span>
        <span class="shd-meta-item">
          <svg viewBox="0 0 24 24" width="14" height="14"><circle cx="12" cy="12" r="10" fill="none" stroke="currentColor" stroke-width="2"/><polyline points="12 6 12 12 16 14" fill="none" stroke="currentColor" stroke-width="2"/></svg>
          发布时间：{{ fmt(detail.startTime) }}
        </span>
        <span class="shd-meta-item deadline-item" :class="{ 'deadline-soon': isDeadlineSoon, 'deadline-over': isOverdue }">
          <svg viewBox="0 0 24 24" width="14" height="14"><circle cx="12" cy="12" r="10" fill="none" stroke="currentColor" stroke-width="2"/><polyline points="12 6 12 12 16 14" fill="none" stroke="currentColor" stroke-width="2"/></svg>
          截止时间：{{ fmt(detail.deadline) }}
          <span v-if="isDeadlineSoon && !isOverdue" class="tag-warn">即将截止</span>
          <span v-if="isOverdue" class="tag-over">已逾期</span>
        </span>
        <span v-if="detail.latestScore != null" class="shd-meta-item score-item">
          <svg viewBox="0 0 24 24" width="14" height="14"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26" fill="none" stroke="currentColor" stroke-width="2"/></svg>
          得分：<strong>{{ detail.latestScore }}分</strong>
        </span>
      </div>
    </div>

    <!-- ========== 作业内容与附件区 ========== -->
    <div class="shd-card">
      <h3 class="shd-section-title">作业内容</h3>
      <div class="shd-content">{{ detail.content || '暂无内容描述' }}</div>

      <div v-if="detail.files && detail.files.length" class="shd-files-section">
        <h3 class="shd-section-title">附件资料 ({{ detail.files.length }})</h3>
        <div class="file-list">
          <div v-for="f in detail.files" :key="f.fileId" class="file-item">
            <span class="file-icon">{{ fileIcon(f.originalName) }}</span>
            <span class="file-name" :title="f.originalName">{{ f.originalName }}</span>
            <span class="file-size">{{ fmtSize(f.fileSize) }}</span>
            <a v-if="f.filePath" :href="'/' + f.filePath" target="_blank" class="file-dl" title="下载">⬇</a>
          </div>
        </div>
      </div>
      <div v-else class="shd-empty-block">
        <span>暂无附件</span>
      </div>
    </div>

    <!-- ========== 历史提交记录区 ========== -->
    <div class="shd-card">
      <div class="shd-card-header">
        <h3 class="shd-section-title">我的提交记录</h3>
        <button v-if="canSubmit" class="btn-submit-hw" @click="goSubmit">
          <svg viewBox="0 0 24 24" width="16" height="16"><line x1="12" y1="5" x2="12" y2="19" stroke="currentColor" stroke-width="2"/><line x1="5" y1="12" x2="19" y2="12" stroke="currentColor" stroke-width="2"/></svg>
          提交作业
        </button>
      </div>
      <div v-if="detail.submissions && detail.submissions.length">
        <div v-for="sub in detail.submissions" :key="sub.submitId" class="sub-record">
          <div class="sub-record-header">
            <span class="sub-time">{{ fmt(sub.submitTime) }}</span>
            <span class="sub-status-tag" :class="{ 'sub-graded': sub.score != null }">
              {{ sub.score != null ? '已批改' : '已提交' }}
            </span>
          </div>
          <div v-if="sub.submitContent" class="sub-content">
            <span class="sub-label">提交内容：</span>{{ sub.submitContent }}
          </div>
          <div v-if="sub.score != null" class="sub-grade">
            <div class="sub-grade-item">
              <span class="sub-label">得分：</span><strong>{{ sub.score }}分</strong>
            </div>
            <div v-if="sub.comment" class="sub-grade-item">
              <span class="sub-label">评语：</span><span class="sub-comment">{{ sub.comment }}</span>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="shd-empty-block">
        <svg viewBox="0 0 24 24" width="36" height="36" fill="none" stroke="#cbd5e1" stroke-width="1.5">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
          <polyline points="14 2 14 8 20 8"/>
          <line x1="12" y1="12" x2="12" y2="18"/><line x1="9" y1="15" x2="15" y2="15"/>
        </svg>
        <span>暂无提交记录</span>
      </div>
    </div>
  </div>

  <!-- ========== 全局加载状态 ========== -->
  <div v-else-if="loading" class="shd-state">
    <div class="loading-spinner"></div>
    <p>加载中...</p>
  </div>

  <!-- ========== 全局错误状态（网络异常 / 接口报错 / 无权限等） ========== -->
  <div v-else-if="error" class="shd-state">
    <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="#ef4444" stroke-width="1.5">
      <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
    </svg>
    <p class="error-text">{{ error }}</p>
    <button class="btn-retry" @click="loadDetail">重试</button>
  </div>
</template>

<script setup>
/**
 * StudentHomeworkDetail.vue
 * 学生端作业详情页：展示作业信息、附件、历史提交记录、提交入口
 *
 * 数据来源：GET /api/homework/{hwId}/student → StudentHomeworkVO
 * 提交入口：复用课程详情页的提交逻辑（hash 跳转到 CourseDetail 作业区）
 */
import { ref, computed, onMounted } from 'vue'
import { api } from '../api/request.js'
import { fmt, fmtSize, fileIcon } from '../utils/format.js'

const props = defineProps({
  courseId:  { type: [String, Number], required: true },
  homeworkId:{ type: [String, Number], required: true }
})

const emit = defineEmits(['back', 'submit'])

// ========== 数据状态 ==========
const loading = ref(true)
const error = ref('')
const detail = ref(null)

// ========== 计算属性 ==========
const statusText = computed(() => {
  const m = { unsubmitted: '未提交', submitted: '已提交', overdue: '已逾期', graded: '已批改' }
  return m[detail.value?.submitStatus] || '--'
})
const isOverdue = computed(() => detail.value?.submitStatus === 'overdue')
const isDeadlineSoon = computed(() => {
  if (!detail.value?.deadline) return false
  const dl = new Date(detail.value.deadline).getTime()
  const now = Date.now()
  return (dl - now > 0 && dl - now <= 24 * 3600 * 1000)
})
const canSubmit = computed(() => {
  const s = detail.value?.submitStatus
  return s === 'unsubmitted' || s === 'submitted' || s === 'overdue'
})

// ========== 数据加载 ==========
async function loadDetail() {
  loading.value = true
  error.value = ''
  detail.value = null
  try {
    const res = await api.getHomeworkStudentDetail(props.homeworkId)
    if (res.code !== 200) {
      error.value = res.message || '加载失败'
      return
    }
    detail.value = res.data
  } catch (e) {
    error.value = e.message || '网络异常，请检查网络后重试'
  } finally {
    loading.value = false
  }
}

// ========== 操作 ==========
function goBack() {
  emit('back')
}

function goSubmit() {
  // 跳转到课程详情页的作业提交区（复用已有提交逻辑）
  location.hash = '#/course/' + props.courseId + '/homework/' + props.homeworkId
  emit('submit', { courseId: props.courseId, homeworkId: props.homeworkId })
}

onMounted(loadDetail)
</script>

<style scoped>
/* ===== 变量（与 CourseDetail 完全一致） ===== */
.shd-page {
  --primary: #3b82f6; --primary-dark: #2563eb; --primary-light: #eff6ff;
  --bg: #f8fafc; --card: #fff; --border: #e2e8f0; --shadow: 0 1px 3px rgba(0,0,0,.06);
  --text: #1e293b; --text-secondary: #64748b; --text-muted: #94a3b8;
  --danger: #ef4444; --success: #22c55e; --warn: #f59e0b;
  padding: 0 0 32px;
}

/* ===== 返回栏 ===== */
.back-bar { padding: 12px 28px; background: var(--card); border-bottom: 1px solid var(--border); }
.btn-back { display: flex; align-items: center; gap: 6px; background: none; border: none; font-size: 15px; color: var(--text); cursor: pointer; font-weight: 600; }
.btn-back:hover { color: var(--primary); }

/* ===== 卡片 ===== */
.shd-card {
  background: var(--card); border: 1px solid var(--border); border-radius: 12px;
  padding: 24px 28px; margin: 20px 28px 0; box-shadow: var(--shadow);
}
.shd-card-header {
  display: flex; justify-content: space-between; align-items: flex-start; gap: 12px; margin-bottom: 16px;
}
.shd-title { font-size: 20px; font-weight: 700; color: var(--text); flex: 1; }
.shd-section-title { font-size: 15px; font-weight: 700; color: var(--text); margin-bottom: 12px; }

/* ===== 状态标签 ===== */
.shd-status {
  padding: 5px 14px; border-radius: 14px; font-size: 13px; font-weight: 600; white-space: nowrap;
}
.status-unsubmitted { background: #fef3c7; color: #92400e; }
.status-submitted  { background: #dbeafe; color: #1d4ed8; }
.status-overdue    { background: #fee2e2; color: #dc2626; }
.status-graded     { background: #dcfce7; color: #15803d; }

/* ===== 元信息 ===== */
.shd-meta {
  display: flex; flex-wrap: wrap; gap: 16px;
}
.shd-meta-item {
  display: flex; align-items: center; gap: 5px; font-size: 13px; color: var(--text-secondary);
}
.deadline-soon { color: var(--warn); }
.deadline-over { color: var(--danger); font-weight: 600; }
.tag-warn {
  font-size: 11px; padding: 1px 8px; border-radius: 10px; background: #fef3c7; color: #92400e; margin-left: 6px;
}
.tag-over {
  font-size: 11px; padding: 1px 8px; border-radius: 10px; background: #fee2e2; color: #dc2626; margin-left: 6px;
}
.score-item { color: var(--primary); }
.score-item strong { font-size: 15px; }

/* ===== 内容 ===== */
.shd-content {
  font-size: 14px; line-height: 1.8; color: var(--text); white-space: pre-wrap;
  padding: 14px 16px; background: var(--bg); border-radius: 8px;
}

/* ===== 附件 ===== */
.shd-files-section { margin-top: 24px; border-top: 1px solid var(--border); padding-top: 20px; }
.file-list { display: flex; flex-direction: column; gap: 4px; }
.file-item {
  display: flex; align-items: center; gap: 10px; padding: 10px 14px;
  background: var(--bg); border-radius: 8px; transition: background .15s;
}
.file-item:hover { background: var(--primary-light); }
.file-icon { font-size: 18px; flex-shrink: 0; }
.file-name { flex: 1; font-size: 13px; color: var(--text); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.file-size { font-size: 12px; color: var(--text-muted); flex-shrink: 0; }
.file-dl {
  font-size: 14px; text-decoration: none; color: var(--primary); flex-shrink: 0;
  padding: 4px 8px; border-radius: 4px;
}
.file-dl:hover { background: var(--primary-light); }

/* ===== 空状态 ===== */
.shd-empty-block {
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  padding: 32px 0; color: var(--text-muted); font-size: 14px;
}

/* ===== 提交按钮 ===== */
.btn-submit-hw {
  display: flex; align-items: center; gap: 6px;
  padding: 9px 20px; background: var(--primary); color: #fff;
  border: none; border-radius: 8px; font-size: 14px; font-weight: 600; cursor: pointer; transition: background .2s;
}
.btn-submit-hw:hover { background: var(--primary-dark); }

/* ===== 提交记录 ===== */
.sub-record {
  padding: 16px; border: 1px solid var(--border); border-radius: 10px; margin-bottom: 12px;
  transition: box-shadow .15s;
}
.sub-record:hover { box-shadow: 0 2px 8px rgba(0,0,0,.04); }
.sub-record-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;
}
.sub-time { font-size: 13px; color: var(--text-secondary); }
.sub-status-tag {
  font-size: 12px; padding: 2px 10px; border-radius: 10px; background: #dbeafe; color: #1d4ed8; font-weight: 500;
}
.sub-graded { background: #dcfce7; color: #15803d; }
.sub-content { font-size: 13px; color: var(--text-secondary); margin-bottom: 6px; }
.sub-grade { margin-top: 10px; padding-top: 10px; border-top: 1px dashed var(--border); }
.sub-grade-item { font-size: 13px; margin-bottom: 4px; color: var(--text-secondary); }
.sub-grade-item strong { color: var(--primary); font-size: 16px; }
.sub-label { font-weight: 600; color: var(--text); }
.sub-comment { color: var(--text); }

/* ===== 加载 / 错误状态 ===== */
.shd-state {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 16px; padding: 80px 20px; color: var(--text-muted);
}
.error-text { color: var(--danger); text-align: center; max-width: 360px; }
.btn-retry {
  padding: 10px 28px; background: var(--primary); color: #fff; border: none;
  border-radius: 8px; font-size: 14px; font-weight: 600; cursor: pointer; transition: background .2s;
}
.btn-retry:hover { background: var(--primary-dark); }
.loading-spinner {
  width: 36px; height: 36px; border: 3px solid var(--border); border-top-color: var(--primary);
  border-radius: 50%; animation: spin .7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
</style>
