<template>
  <!-- ================================================================
   学生端作业提交页 — StudentHomeworkSubmit.vue
   复用 StudentHomeworkDetail 的样式变量体系
   接口：
     GET  /api/homework/{hwId}/submit-page → StudentSubmitVO
     POST /api/homework/{hwId}/submit      → SubmissionVO (FormData)
   ================================================================ -->
  <div class="shs-page" v-if="!loading && !error && data">
    <!-- ========== 返回栏 ========== -->
    <div class="back-bar">
      <button class="btn-back" @click="$emit('back')">
        <svg viewBox="0 0 24 24" width="20" height="20"><path d="M15 18l-6-6 6-6" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
        <span>{{ data.courseName || '返回' }}</span>
      </button>
    </div>

    <!-- ========== 头部：标题 + 状态 ========== -->
    <div class="shs-card">
      <div class="shs-card-header">
        <h2 class="shs-title">{{ data.title }}</h2>
        <span class="shs-status" :class="'s-' + data.submitStatus">{{ statusLabel }}</span>
      </div>
      <div class="shs-meta">
        <span>满分：{{ data.totalScore || 100 }}分</span>
        <span>开始：{{ fmt(data.startTime) }}</span>
        <span class="meta-deadline" :class="{ 'deadline-soon': deadlineSoon, 'deadline-over': isOverdue }">
          截止：{{ fmt(data.deadline) }}
          <em v-if="deadlineSoon && !isOverdue" class="tag-warn">即将截止</em>
          <em v-if="isOverdue" class="tag-over">已逾期</em>
        </span>
        <span>已提交：{{ data.submittedCount }}次</span>
      </div>
    </div>

    <!-- ========== 上部：作业详情 + 教师附件 ========== -->
    <div class="shs-card">
      <h3 class="shs-section-title">作业要求</h3>
      <div class="shs-desc">{{ data.content || '暂无内容描述' }}</div>

      <div v-if="data.teacherFiles && data.teacherFiles.length" class="shs-files-section">
        <h4>教师附件 ({{ data.teacherFiles.length }})</h4>
        <div class="file-list">
          <div v-for="f in data.teacherFiles" :key="f.fileId" class="file-item">
            <span class="file-name" :title="f.fileName">{{ f.fileName }}</span>
            <span class="file-size">{{ fmtSize(f.fileSize) }}</span>
            <a v-if="f.filePath" :href="'/' + f.filePath" target="_blank" class="file-dl">⬇</a>
          </div>
        </div>
      </div>
    </div>

    <!-- ========== 中部：作答区 ========== -->
    <div class="shs-card">
      <h3 class="shs-section-title">作 答</h3>

      <!-- 文字作答 -->
      <div class="field">
        <label>作答内容</label>
        <textarea v-model="submitContent" rows="5" placeholder="请输入你的作答内容..."
          class="input" :disabled="!canSubmitNow"></textarea>
      </div>

      <!-- 附件上传 -->
      <div class="field">
        <label>上传附件</label>
        <div class="upload-row">
          <label class="upload-btn" :class="{ disabled: !canSubmitNow }">
            <svg viewBox="0 0 24 24" width="14" height="14"><path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
            选择文件
            <input type="file" multiple hidden @change="onFilesSelected" :disabled="!canSubmitNow" />
          </label>
        </div>
        <ul v-if="selectedFiles.length" class="upload-list">
          <li v-for="(f,i) in selectedFiles" :key="i" class="upload-item">
            <span>{{ f.name }}</span>
            <span class="f-size">{{ fmtSize(f.size) }}</span>
            <button type="button" @click="selectedFiles.splice(i,1)" :disabled="!canSubmitNow">&times;</button>
          </li>
        </ul>
      </div>

      <!-- 提交按钮 -->
      <div class="submit-bar">
        <span v-if="submitBtnMsg" class="submit-hint">{{ submitBtnMsg }}</span>
        <button class="btn-submit" :disabled="!canSubmitNow || submitting" @click="doSubmit">
          {{ submitting ? '提交中...' : submitBtnText }}
        </button>
      </div>
      <p v-if="submitError" class="error-text">{{ submitError }}</p>
    </div>

    <!-- ========== 下部：历史提交记录 ========== -->
    <div class="shs-card">
      <h3 class="shs-section-title">我的提交记录</h3>
      <div v-if="data.submissions && data.submissions.length">
        <div v-for="(sub, idx) in data.submissions" :key="sub.submitId"
          class="sub-record" :class="{ 'latest': idx === 0 }">
          <div class="sub-record-hd">
            <span class="sub-time">{{ fmt(sub.submitTime) }}</span>
            <span class="sub-tag" :class="{ graded: sub.score != null }">
              {{ sub.score != null ? '已批改' : '已提交' }}
            </span>
            <span v-if="idx === 0" class="sub-tag latest-tag">最新</span>
          </div>
          <div v-if="sub.submitContent" class="sub-content">{{ sub.submitContent }}</div>
          <div v-if="sub.files && sub.files.length" class="sub-files">
            <span v-for="sf in sub.files" :key="sf.sFileId" class="sub-file-chip">
              📎 {{ sf.fileName }}
            </span>
          </div>
          <div v-if="sub.score != null" class="sub-grade">
            <div><strong>得分：{{ sub.score }}分</strong></div>
            <div v-if="sub.comment" class="sub-comment">评语：{{ sub.comment }}</div>
          </div>
        </div>
      </div>
      <div v-else class="empty-block">暂无提交记录</div>
    </div>
  </div>

  <!-- ======== 加载中 ======== -->
  <div v-else-if="loading" class="shs-state">
    <div class="loading-spinner"></div>
    <p>加载中...</p>
  </div>

  <!-- ======== 错误 ======== -->
  <div v-else-if="error" class="shs-state">
    <p class="err-text">{{ error }}</p>
    <button class="btn-retry" @click="loadData">重试</button>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { api } from '../api/request.js'

const props = defineProps({
  courseId:  { type: [String,Number], required: true },
  homeworkId:{ type: [String,Number], required: true }
})
const emit = defineEmits(['back'])

const loading = ref(true)
const error = ref('')
const data = ref(null)

const submitContent = ref('')
const selectedFiles = ref([])
const submitting = ref(false)
const submitError = ref('')

// ===== 状态标签 =====
const statusLabel = computed(() => ({
  unsubmitted:'未提交', submitted:'已提交', unstarted:'未开始',
  overdue:'已逾期', graded:'已批改'
}[data.value?.submitStatus] || '--'))

const isOverdue = computed(() => data.value?.submitStatus === 'overdue')
const deadlineSoon = computed(() => {
  const dl = data.value?.deadline; if (!dl) return false
  const d = new Date(dl).getTime(); const n = Date.now()
  return d - n > 0 && d - n <= 24 * 3600 * 1000
})

// ===== 提交按钮逻辑 =====
const submitBtnMsg = computed(() => {
  const s = data.value?.submitStatus
  if (s === 'unstarted') return '作业未到提交时间'
  if (s === 'overdue' && !data.value?.allowLate) return '作业已截止，无法提交'
  if (s === 'graded') return '作业已批改，无法修改'
  if (data.value?.submitLimit > 0 && (data.value?.remainingCount ?? 0) <= 0)
    return '已达到最大提交次数'
  return ''
})
const submitBtnText = computed(() => {
  if (data.value?.submitStatus === 'graded') return '已批改'
  return '提交作业'
})
const canSubmitNow = computed(() => {
  const s = data.value?.submitStatus
  if (s === 'unstarted') return false
  if (s === 'graded') return false
  if (s === 'overdue' && !data.value?.allowLate) return false
  // 次数限制检查
  if (data.value?.submitLimit > 0 && (data.value?.remainingCount ?? 0) <= 0) return false
  return true
})

// ===== 数据加载 =====
async function loadData() {
  loading.value = true; error.value = ''
  try {
    const res = await api.getSubmitPageData(props.homeworkId)
    if (res.code !== 200) { error.value = res.message || '加载失败'; return }
    data.value = res.data
  } catch (e) {
    error.value = e.message || '网络异常，请检查网络后重试'
  } finally { loading.value = false }
}

// ===== 文件选择 =====
function onFilesSelected(e) {
  selectedFiles.value = [...selectedFiles.value, ...Array.from(e.target.files)]
  e.target.value = ''
}

// ===== 提交 =====
async function doSubmit() {
  if (!submitContent.value.trim() && selectedFiles.value.length === 0) {
    submitError.value = '请填写作答内容或上传附件'
    return
  }
  submitting.value = true; submitError.value = ''
  try {
    const fd = new FormData()
    fd.append('submitContent', submitContent.value.trim())
    selectedFiles.value.forEach(f => fd.append('files', f))
    await api.submitHomework(props.homeworkId, fd)
    // 成功后清空表单 + 重新加载
    submitContent.value = ''
    selectedFiles.value = []
    await loadData()
  } catch (e) {
    submitError.value = e.message
  } finally { submitting.value = false }
}

// ===== 工具 =====
function fmt(v) {
  if (!v) return '--'
  if (typeof v === 'number') return new Date(v * 1000).toLocaleString('zh-CN')
  return String(v).replace('T',' ').substring(0,16)
}
function fmtSize(b) {
  if (!b) return ''
  if (b < 1024) return b+'B'; if (b < 1048576) return (b/1024).toFixed(1)+'KB'
  return (b/1048576).toFixed(1)+'MB'
}

onMounted(loadData)
</script>

<style scoped>
/* ===== 变量（与 StudentHomeworkDetail 一致） ===== */
.shs-page {
  --primary:#3b82f6; --primary-dark:#2563eb; --primary-light:#eff6ff;
  --bg:#f8fafc; --card:white; --border:#e2e8f0; --shadow:0 1px 3px rgba(0,0,0,.06);
  --text:#1e293b; --text2:#64748b; --text3:#94a3b8;
  --danger:#ef4444; --success:#22c55e; --warn:#f59e0b;
  padding:0 0 32px;
}

.back-bar{padding:12px 28px;background:white;border-bottom:1px solid var(--border)}
.btn-back{display:flex;align-items:center;gap:6px;background:none;border:none;font-size:15px;color:var(--text);cursor:pointer;font-weight:600}
.btn-back:hover{color:var(--primary)}

.shs-card{background:white;border:1px solid var(--border);border-radius:12px;padding:24px 28px;margin:20px 28px 0;box-shadow:var(--shadow)}
.shs-card-header{display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:12px}
.shs-title{font-size:20px;font-weight:700;color:var(--text)}
.shs-section-title{font-size:15px;font-weight:700;color:var(--text);margin-bottom:12px}

/* 状态标签 */
.shs-status{padding:5px 14px;border-radius:14px;font-size:13px;font-weight:600;white-space:nowrap}
.s-unsubmitted,.s-unstarted{background:#fef3c7;color:#92400e}
.s-submitted{background:#dbeafe;color:#1d4ed8}
.s-overdue{background:#fee2e2;color:#dc2626}
.s-graded{background:#dcfce7;color:#15803d}

.shs-meta{display:flex;flex-wrap:wrap;gap:16px;font-size:13px;color:var(--text2)}
.deadline-soon{color:var(--warn)}
.deadline-over{color:var(--danger);font-weight:600}
.tag-warn{font-size:11px;padding:1px 8px;border-radius:10px;background:#fef3c7;color:#92400e;margin-left:6px;font-style:normal}
.tag-over{font-size:11px;padding:1px 8px;border-radius:10px;background:#fee2e2;color:#dc2626;margin-left:6px;font-style:normal}
.shs-desc{font-size:14px;line-height:1.8;color:var(--text);white-space:pre-wrap;padding:14px 16px;background:var(--bg);border-radius:8px}

/* 附件 */
.shs-files-section{margin-top:20px;border-top:1px solid var(--border);padding-top:16px}
.shs-files-section h4{font-size:14px;font-weight:600;color:var(--text);margin-bottom:8px}
.file-list{display:flex;flex-direction:column;gap:4px}
.file-item{display:flex;align-items:center;gap:8px;padding:8px 12px;background:var(--bg);border-radius:6px;font-size:13px}
.file-name{flex:1;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;color:var(--text)}
.file-size{font-size:12px;color:var(--text3)}
.file-dl{text-decoration:none;color:var(--primary);font-size:14px;padding:2px 6px}
.file-dl:hover{background:var(--primary-light);border-radius:4px}

/* 表单 */
.field{margin-bottom:16px}
.field label{display:block;font-size:13px;font-weight:600;color:var(--text);margin-bottom:6px}
.input{width:100%;padding:10px 14px;border:1px solid var(--border);border-radius:8px;font-size:13px;color:var(--text);outline:none;resize:vertical;background:#fff;box-sizing:border-box}
.input:focus{border-color:var(--primary);box-shadow:0 0 0 3px rgba(59,130,246,.1)}
.input:disabled{background:var(--bg);color:var(--text3)}
.upload-row{display:flex;align-items:center;gap:10px}
.upload-btn{display:inline-flex;align-items:center;gap:6px;padding:9px 18px;border:1px dashed var(--primary);border-radius:8px;font-size:13px;color:var(--primary);cursor:pointer;background:var(--primary-light)}
.upload-btn:hover{background:#dbeafe}
.upload-btn.disabled{opacity:.4;cursor:not-allowed}
.upload-list{list-style:none;margin-top:8px}
.upload-item{display:flex;align-items:center;gap:8px;padding:6px 12px;background:var(--bg);border-radius:6px;margin-bottom:4px;font-size:13px}
.f-size{font-size:12px;color:var(--text3)}
.upload-item button{background:none;border:none;font-size:16px;color:var(--text3);cursor:pointer;margin-left:auto}
.upload-item button:hover{color:var(--danger)}
.submit-bar{display:flex;justify-content:flex-end;align-items:center;gap:14px;margin-top:16px}
.submit-hint{font-size:13px;color:var(--danger)}
.btn-submit{padding:10px 32px;background:var(--primary);color:white;border:none;border-radius:8px;font-size:15px;font-weight:700;cursor:pointer;transition:background .2s}
.btn-submit:hover:not(:disabled){background:var(--primary-dark)}
.btn-submit:disabled{background:#a5b4fc;cursor:not-allowed}
.error-text{color:var(--danger);font-size:13px;margin-top:8px}

/* 提交记录 */
.sub-record{padding:16px;border:1px solid var(--border);border-radius:10px;margin-bottom:12px;transition:box-shadow .15s}
.sub-record:hover{box-shadow:0 2px 8px rgba(0,0,0,.04)}
.sub-record.latest{border-color:var(--primary);background:var(--primary-light)}
.sub-record-hd{display:flex;align-items:center;gap:8px;margin-bottom:8px}
.sub-time{font-size:13px;color:var(--text2);flex:1}
.sub-tag{font-size:12px;padding:2px 10px;border-radius:10px;background:#dbeafe;color:#1d4ed8;font-weight:500}
.sub-tag.graded{background:#dcfce7;color:#15803d}
.sub-tag.latest-tag{background:var(--primary);color:white}
.sub-content{font-size:13px;color:var(--text2);margin-bottom:6px;white-space:pre-wrap}
.sub-files{display:flex;flex-wrap:wrap;gap:6px;margin-bottom:6px}
.sub-file-chip{font-size:12px;padding:2px 10px;background:var(--bg);border-radius:6px;color:var(--text2)}
.sub-grade{margin-top:10px;padding-top:10px;border-top:1px dashed var(--border)}
.sub-grade strong{color:var(--primary)}
.sub-comment{color:var(--text2);font-size:13px;margin-top:4px}
.empty-block{display:flex;flex-direction:column;align-items:center;gap:8px;padding:32px 0;color:var(--text3);font-size:14px}

/* 加载/错误 */
.shs-state{display:flex;flex-direction:column;align-items:center;justify-content:center;gap:16px;padding:80px 20px;color:var(--text3)}
.err-text{color:var(--danger);text-align:center;max-width:360px}
.btn-retry{padding:10px 28px;background:var(--primary);color:white;border:none;border-radius:8px;font-size:14px;font-weight:600;cursor:pointer}
.btn-retry:hover{background:var(--primary-dark)}
.loading-spinner{width:36px;height:36px;border:3px solid var(--border);border-top-color:var(--primary);border-radius:50%;animation:spin .7s linear infinite}
@keyframes spin{to{transform:rotate(360deg)}}
</style>
