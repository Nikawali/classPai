<template>
  <div class="teacher-hw-page">
    <!-- 加载/错误 -->
    <div v-if="loading" class="th-status">加载中...</div>
    <div v-else-if="error" class="th-status th-error">{{ error }}</div>

    <template v-else>
      <!-- ========== 顶部导航栏 ========== -->
      <div class="th-topbar">
        <button class="th-back" @click="$emit('back')">
          <svg viewBox="0 0 24 24" width="18" height="18">
            <path d="M15 18l-6-6 6-6" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </button>
        <div class="th-tabs">
          <button class="th-tab active">学生作业</button>
          <button class="th-tab">作业详情</button>
        </div>
      </div>

      <!-- ========== 标题行 ========== -->
      <div class="th-header">
        <h1 class="th-title">{{ homework.title }}</h1>
      </div>

      <!-- ========== 操作栏：截止时间 + 操作 + 搜索 ========== -->
      <div class="th-actions">
        <div class="th-deadline">
          <svg viewBox="0 0 24 24" width="14" height="14" fill="none">
            <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.5"/>
            <polyline points="12,7 12,12 16,14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <span>截止 {{ formatDeadline(homework.deadline) }}</span>
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
              <button class="th-dropdown-item" @click="showSettings = false">修改截止时间</button>
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
            <template v-if="!s.submitted">
              <button class="th-action-btn warn">催交</button>
            </template>
            <template v-else-if="s.score == null">
              <button class="th-action-btn primary">进入批阅</button>
            </template>
            <template v-else>
              <button class="th-action-btn ghost">查看</button>
            </template>
          </span>
        </div>

        <div v-if="filteredStudents.length === 0" class="th-empty">
          无匹配学生
        </div>
      </div>

      <!-- ========== 底部操作栏 ========== -->
      <div class="th-footer">
        <button class="th-footer-btn">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none">
            <path d="M12 2L2 7l10 5 10-5-10-5z" stroke="currentColor" stroke-width="1.5"/>
            <path d="M2 17l10 5 10-5" stroke="currentColor" stroke-width="1.5"/>
            <path d="M2 12l10 5 10-5" stroke="currentColor" stroke-width="1.5"/>
          </svg>
          AI 一键批改
        </button>
        <button class="th-footer-btn warn">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none">
            <path d="M22 2L11 13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <path d="M22 2l-7 20-4-9-9-4 20-7z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          一键催交
        </button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { api } from '../api/request.js'

const props = defineProps({
  hwId: { type: [Number, String], required: true }
})
defineEmits(['back'])

const loading = ref(true)
const error = ref('')
const homework = ref({})
const students = ref([])
const keyword = ref('')
const showSettings = ref(false)

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
.th-deadline {
  display: flex; align-items: center; gap: 5px;
  font-size: 12px; color: #e74c3c;
  background: #fef2f2; padding: 4px 10px; border-radius: 6px;
}
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
.th-action-btn.ghost { background: transparent; color: #999; }
.th-action-btn.ghost:hover { background: #f0f0f0; }

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
