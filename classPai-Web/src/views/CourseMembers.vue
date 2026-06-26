<template>
  <div class="members-page">
    <!-- 顶部搜索栏 -->
    <div class="members-topbar">
      <button class="members-back" @click="$emit('back')">
        <svg viewBox="0 0 24 24" width="20" height="20">
          <path d="M15 18l-6-6 6-6" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </button>
      <div class="members-search">
        <svg class="members-search-icon" viewBox="0 0 24 24" width="16" height="16">
          <circle cx="11" cy="11" r="7" fill="none" stroke="currentColor" stroke-width="2"/>
          <line x1="16.5" y1="16.5" x2="21" y2="21" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
        <input
          v-model="keyword"
          type="text"
          placeholder="搜索成员姓名或ID"
          class="members-search-input"
        />
        <button v-if="keyword" class="members-search-clear" @click="keyword = ''">
          <svg viewBox="0 0 24 24" width="14" height="14">
            <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </button>
      </div>
    </div>

    <!-- 加载/错误 -->
    <div v-if="loading" class="members-status">加载中...</div>
    <div v-else-if="error" class="members-status members-error">{{ error }}</div>

    <!-- 成员列表 -->
    <template v-else>
      <!-- 教师栏 -->
      <div v-if="filteredTeachers.length > 0" class="members-section">
        <div class="members-section-header">
          <span class="members-section-title">教师</span>
          <span class="members-section-count">{{ filteredTeachers.length }}人</span>
        </div>
        <div class="members-list">
          <div v-for="m in filteredTeachers" :key="m.userId" class="member-card">
            <div class="member-avatar" :style="{ background: coverColor(m.userId) }">
              {{ (m.userName || '?').charAt(0) }}
            </div>
            <div class="member-info">
              <p class="member-name">{{ m.userName || '未知' }}</p>
              <p class="member-id">ID: {{ m.userId }}</p>
            </div>
            <span class="member-role-tag teacher">教师</span>
          </div>
        </div>
      </div>

      <!-- 学生栏 -->
      <div class="members-section">
        <div class="members-section-header">
          <span class="members-section-title">学生</span>
          <span class="members-section-count">{{ filteredStudents.length }}人</span>
        </div>
        <div v-if="filteredStudents.length === 0" class="members-empty">
          {{ keyword ? '无匹配结果' : '暂无学生' }}
        </div>
        <div v-else class="members-list">
          <div v-for="m in filteredStudents" :key="m.userId" class="member-card">
            <div class="member-avatar" :style="{ background: coverColor(m.userId) }">
              {{ (m.userName || '?').charAt(0) }}
            </div>
            <div class="member-info">
              <p class="member-name">{{ m.userName || '未知' }}</p>
              <p class="member-id">ID: {{ m.userId }}</p>
            </div>
            <span class="member-role-tag student">学生</span>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { api } from '../api/request.js'
import { coverColor } from '../utils/colors.js'

const props = defineProps({
  courseId: { type: [Number, String], required: true }
})
defineEmits(['back'])

const members = ref([])
const loading = ref(true)
const error = ref('')
const keyword = ref('')

// 模糊搜索：根据 userName 或 userId 匹配
const filteredMembers = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return members.value
  return members.value.filter(m => {
    const name = (m.userName || '').toLowerCase()
    const id = String(m.userId || '')
    return name.includes(kw) || id.includes(kw)
  })
})

const teachers = computed(() => filteredMembers.value.filter(m => m.role === 'teacher'))
const students = computed(() => filteredMembers.value.filter(m => m.role === 'student'))

// 保持教师在上方的排序
const filteredTeachers = computed(() => teachers.value)
const filteredStudents = computed(() => students.value)

async function loadMembers() {
  loading.value = true
  error.value = ''
  try {
    const res = await api.getCourseMembers(props.courseId)
    members.value = res.data || []
  } catch (e) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(() => { loadMembers() })
</script>

<style scoped>
.members-page {
  width: 100%;
  max-width: 640px;
  margin: 0 auto;
  min-height: 100vh;
  background: #f5f6fa;
}

/* 顶部搜索栏 */
.members-topbar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  position: sticky;
  top: 0;
  z-index: 10;
}
.members-back {
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
  flex-shrink: 0;
  transition: background .15s;
}
.members-back:hover { background: #e0e0e0; }

.members-search {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  height: 36px;
  padding: 0 12px;
  background: #f5f6fa;
  border-radius: 10px;
}
.members-search-icon {
  flex-shrink: 0;
  color: #bbb;
}
.members-search-input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
  color: #333;
}
.members-search-input::placeholder { color: #bbb; }
.members-search-clear {
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 50%;
  background: #ddd;
  color: #666;
  cursor: pointer;
  flex-shrink: 0;
}

/* 状态 */
.members-status {
  text-align: center;
  padding: 60px 0;
  color: #999;
  font-size: 14px;
}
.members-error { color: #e74c3c; }

/* 区块 */
.members-section {
  background: #fff;
  margin: 12px 16px;
  border-radius: 12px;
  overflow: hidden;
}
.members-section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px 10px;
}
.members-section-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
}
.members-section-count {
  font-size: 12px;
  color: #aaa;
}

/* 成员卡片 */
.members-list {
  padding: 0 16px 4px;
}
.member-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-top: 1px solid #f5f5f5;
}
.member-card:first-child { border-top: none; }

.member-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  flex-shrink: 0;
}
.member-info {
  flex: 1;
  min-width: 0;
}
.member-name {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.member-id {
  font-size: 12px;
  color: #aaa;
  margin-top: 2px;
}
.member-role-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
  flex-shrink: 0;
}
.member-role-tag.teacher {
  color: #2377E4;
  background: rgba(35,119,228,.1);
}
.member-role-tag.student {
  color: #059669;
  background: #ecfdf5;
}

.members-empty {
  text-align: center;
  padding: 32px 0;
  color: #bbb;
  font-size: 13px;
}
</style>
