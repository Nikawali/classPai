<template>
  <div class="create-overlay" @click.self="$emit('back')">
    <div class="create-card">
      <!-- 标题栏 -->
      <div class="create-header">
        <h2 class="create-title">创建课程</h2>
        <button class="create-close-btn" @click="$emit('back')">
          <svg viewBox="0 0 24 24" width="18" height="18">
            <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </button>
      </div>

      <div v-if="errorMsg" class="error-banner">
        <span>{{ errorMsg }}</span>
      </div>

      <form class="form" @submit.prevent="handleSubmit">
        <!-- 课程名称 -->
        <div class="field">
          <label for="courseName">课程名称</label>
          <input
            id="courseName"
            v-model="form.courseName"
            type="text"
            placeholder="例如：高等数学"
            class="input"
            :class="{ 'input-error': fieldErrors.courseName }"
            @input="clearFieldError('courseName')"
          />
          <span v-if="fieldErrors.courseName" class="field-err">{{ fieldErrors.courseName }}</span>
        </div>

        <!-- 课程介绍 -->
        <div class="field">
          <label for="courseIntro">课程介绍</label>
          <textarea
            id="courseIntro"
            v-model="form.courseIntro"
            placeholder="简要介绍课程内容..."
            class="input textarea"
            rows="3"
          ></textarea>
        </div>

        <!-- 学年 -->
        <div class="field">
          <label for="academicYear">学年</label>
          <select
            id="academicYear"
            v-model="form.academicYear"
            class="input select"
            :class="{ 'input-error': fieldErrors.academicYear }"
            @change="clearFieldError('academicYear')"
          >
            <option value="" disabled>请选择学年</option>
            <option v-for="y in yearOptions" :key="y" :value="y">
              {{ y }}-{{ y + 1 }}
            </option>
          </select>
          <span v-if="fieldErrors.academicYear" class="field-err">{{ fieldErrors.academicYear }}</span>
        </div>

        <!-- 学期 -->
        <div class="field">
          <label for="semester">学期</label>
          <select
            id="semester"
            v-model="form.semester"
            class="input select"
            :class="{ 'input-error': fieldErrors.semester }"
            @change="clearFieldError('semester')"
          >
            <option value="" disabled>请选择学期</option>
            <option value="第一学期">第一学期</option>
            <option value="第二学期">第二学期</option>
          </select>
          <span v-if="fieldErrors.semester" class="field-err">{{ fieldErrors.semester }}</span>
        </div>

        <!-- 提交 -->
        <button type="submit" class="btn-submit" :disabled="loading">
          <span v-if="loading" class="spinner"></span>
          <span v-else>创建课程</span>
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { api } from '../api/request.js'

const emit = defineEmits(['back', 'created'])

const yearOptions = Array.from({ length: 50 }, (_, i) => 2000 + i)

const form = reactive({
  courseName: '',
  courseIntro: '',
  academicYear: '',
  semester: ''
})

const loading = ref(false)
const errorMsg = ref('')
const fieldErrors = reactive({
  courseName: '',
  academicYear: '',
  semester: ''
})

function clearFieldError(field) {
  fieldErrors[field] = ''
  errorMsg.value = ''
}

async function handleSubmit() {
  errorMsg.value = ''
  Object.keys(fieldErrors).forEach(k => fieldErrors[k] = '')

  let valid = true
  if (!form.courseName.trim()) {
    fieldErrors.courseName = '请输入课程名称'
    valid = false
  }
  if (!form.academicYear) {
    fieldErrors.academicYear = '请选择学年'
    valid = false
  }
  if (!form.semester) {
    fieldErrors.semester = '请选择学期'
    valid = false
  }
  if (!valid) return

  loading.value = true
  try {
    const year = Number(form.academicYear)
    const body = {
      courseName: form.courseName.trim(),
      courseIntro: form.courseIntro.trim(),
      startDate: `${year}-09-01`,
      endDate: `${year + 1}-06-30`,
      semester: form.semester
    }
    await api.createCourse(body)
    emit('created')
  } catch (e) {
    errorMsg.value = e.message
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.create-overlay {
  position: fixed;
  inset: 0;
  z-index: 300;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.create-card {
  width: 460px;
  max-width: 100%;
  max-height: 85vh;
  overflow-y: auto;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  padding: 24px 28px 28px;
}

/* 标题栏 */
.create-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.create-title {
  font-size: 18px;
  font-weight: 600;
  color: #222;
}

.create-close-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #888;
  cursor: pointer;
  transition: background .15s, color .15s;
}
.create-close-btn:hover {
  background: #f0f0f0;
  color: #333;
}

.error-banner {
  padding: 10px 14px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  color: #dc2626;
  font-size: 13px;
  margin-bottom: 16px;
  animation: slideDown .25s ease;
}
@keyframes slideDown {
  from { opacity: 0; transform: translateY(-8px); }
  to   { opacity: 1; transform: translateY(0); }
}

.form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.field label {
  font-size: 13px;
  color: #555;
  font-weight: 500;
}

.input {
  width: 100%;
  height: 44px;
  border: 1.5px solid #e2e2e2;
  border-radius: 8px;
  padding: 0 14px;
  font-size: 14px;
  outline: none;
  transition: border-color .2s, box-shadow .2s;
  box-sizing: border-box;
  background: #fff;
}
.input:focus {
  border-color: #36B8F3;
  box-shadow: 0 0 0 3px rgba(54, 184, 243, 0.1);
}
.input-error {
  border-color: #e74c3c;
}
.textarea {
  height: auto;
  padding: 12px 14px;
  resize: vertical;
  font-family: inherit;
}
.select {
  cursor: pointer;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='8'%3E%3Cpath d='M1 1l5 5 5-5' fill='none' stroke='%23999' stroke-width='2' stroke-linecap='round'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 14px center;
  padding-right: 36px;
}

.field-err {
  font-size: 12px;
  color: #e74c3c;
  padding-left: 2px;
}

.btn-submit {
  width: 100%;
  height: 48px;
  border: none;
  border-radius: 8px;
  background: rgb(54, 184, 243);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  margin-top: 4px;
  transition: opacity .2s, transform .15s;
  display: flex;
  align-items: center;
  justify-content: center;
}
.btn-submit:hover {
  opacity: .92;
}
.btn-submit:disabled {
  opacity: .5;
  cursor: not-allowed;
}

.spinner {
  width: 22px;
  height: 22px;
  border: 2.5px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin .6s linear infinite;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
