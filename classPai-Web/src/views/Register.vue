<template>
  <div class="register-page">
    <!-- 背景图层 -->
    <div class="register-bg" :style="{ backgroundImage: `url(${backgroundImgUrl})` }"></div>
    <!-- 深色半透明遮罩 -->
    <div class="register-overlay"></div>
    <!-- 注册弹窗 -->
    <div class="register-wrapper">
      <div class="register-card">
        <!-- 标题 -->
        <div class="header">
          <h1 class="title">创建账号</h1>
          <p class="subtitle">加入 ClassPai 课堂管理平台</p>
        </div>

        <!-- 错误提示区域 -->
        <div v-if="errorMsg" class="error-banner" role="alert">
          <svg class="error-icon" viewBox="0 0 24 24" width="18" height="18">
            <circle cx="12" cy="12" r="10" fill="none" stroke="currentColor" stroke-width="2"/>
            <line x1="12" y1="8" x2="12" y2="13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <circle cx="12" cy="16.5" r="1" fill="currentColor"/>
          </svg>
          <span>{{ errorMsg }}</span>
        </div>

        <!-- 表单 -->
        <form class="form" @submit.prevent="handleRegister">
          <!-- 学号/工号 -->
          <div class="field">
            <div class="input-wrapper">
              <svg class="input-icon" viewBox="0 0 24 24" width="18" height="18">
                <rect x="3" y="5" width="18" height="3" rx="1" fill="none" stroke="currentColor" stroke-width="2"/>
                <rect x="5" y="10" width="14" height="10" rx="2" fill="none" stroke="currentColor" stroke-width="2"/>
                <line x1="7" y1="14" x2="17" y2="14" stroke="currentColor" stroke-width="1.5"/>
              </svg>
              <input id="reg-userId" v-model="form.userId" type="number"
                placeholder="请输入学号或工号" class="input"
                :class="{ 'input-error': fieldErrors.userId }"
                @input="clearFieldError('userId')" />
            </div>
            <span v-if="fieldErrors.userId" class="field-err">{{ fieldErrors.userId }}</span>
          </div>

          <!-- 姓名 -->
          <div class="field">
            <div class="input-wrapper">
              <svg class="input-icon" viewBox="0 0 24 24" width="18" height="18">
                <circle cx="12" cy="8" r="4" fill="none" stroke="currentColor" stroke-width="2"/>
                <path d="M4 20c0-4 4-7 8-7s8 3 8 7" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              <input id="reg-userName" v-model="form.userName" type="text"
                placeholder="请输入真实姓名" class="input"
                :class="{ 'input-error': fieldErrors.userName }"
                @input="clearFieldError('userName')" />
            </div>
            <span v-if="fieldErrors.userName" class="field-err">{{ fieldErrors.userName }}</span>
          </div>

          <!-- 手机号 -->
          <div class="field">
            <div class="input-wrapper">
              <svg class="input-icon" viewBox="0 0 24 24" width="18" height="18">
                <rect x="6" y="1" width="12" height="20" rx="3" fill="none" stroke="currentColor" stroke-width="2"/>
                <circle cx="12" cy="18" r="1.5" fill="currentColor"/>
              </svg>
              <input id="reg-phone" v-model="form.phone" type="tel"
                placeholder="请输入11位手机号" maxlength="11" class="input"
                :class="{ 'input-error': fieldErrors.phone }"
                @input="clearFieldError('phone')" />
            </div>
            <span v-if="fieldErrors.phone" class="field-err">{{ fieldErrors.phone }}</span>
          </div>

          <!-- 密码 -->
          <div class="field">
            <div class="input-wrapper">
              <svg class="input-icon" viewBox="0 0 24 24" width="18" height="18">
                <rect x="3" y="11" width="18" height="11" rx="2" fill="none" stroke="currentColor" stroke-width="2"/>
                <path d="M7 11V7a5 5 0 0 1 10 0v4" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              <input id="reg-password" v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="请输入密码（6-16位）" class="input"
                :class="{ 'input-error': fieldErrors.password }"
                @input="clearFieldError('password')" />
              <button type="button" class="toggle-pwd"
                :aria-label="showPassword ? '隐藏密码' : '显示密码'"
                @click="showPassword = !showPassword">
                <svg v-if="showPassword" viewBox="0 0 24 24" width="20" height="20">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" fill="none" stroke="currentColor" stroke-width="2"/>
                  <circle cx="12" cy="12" r="3" fill="none" stroke="currentColor" stroke-width="2"/>
                  <line x1="1" y1="1" x2="23" y2="23" stroke="currentColor" stroke-width="2"/>
                </svg>
                <svg v-else viewBox="0 0 24 24" width="20" height="20">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" fill="none" stroke="currentColor" stroke-width="2"/>
                  <circle cx="12" cy="12" r="3" fill="none" stroke="currentColor" stroke-width="2"/>
                </svg>
              </button>
            </div>
            <span v-if="fieldErrors.password" class="field-err">{{ fieldErrors.password }}</span>
          </div>

          <!-- 性别 + 角色 -->
          <div class="field">
            <div class="radio-group">
              <label class="radio" :class="{ active: form.gender === 'male' }">
                <input v-model="form.gender" type="radio" value="male" /> 男
              </label>
              <label class="radio" :class="{ active: form.gender === 'female' }">
                <input v-model="form.gender" type="radio" value="female" /> 女
              </label>
              <label class="radio" :class="{ active: form.role === 'student' }">
                <input v-model="form.role" type="radio" value="student" /> 学生
              </label>
              <label class="radio" :class="{ active: form.role === 'teacher' }">
                <input v-model="form.role" type="radio" value="teacher" /> 教师
              </label>
            </div>
          </div>

          <!-- 学校下拉框 -->
          <div class="field">
            <div class="input-wrapper dropdown-wrapper" ref="dropdownRef">
              <svg class="input-icon" viewBox="0 0 24 24" width="18" height="18">
                <path d="M3 9l9 7 9-7v11a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" fill="none" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
              </svg>
              <input v-model="schoolSearch" type="text" placeholder="搜索或选择学校"
                class="input" style="padding-right:36px"
                @focus="showDropdown = true" @input="onSchoolInput" />
              <svg class="input-icon" style="left:auto;right:12px"
                viewBox="0 0 24 24" width="14" height="14">
                <path d="M8 10l4 4 4-4" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              <ul v-if="showDropdown && filteredSchools.length" class="dropdown">
                <li v-for="s in filteredSchools" :key="s.id"
                  @click="selectSchool(s)">{{ s.schoolName }}</li>
              </ul>
            </div>
            <span v-if="fieldErrors.school" class="field-err">{{ fieldErrors.school }}</span>
          </div>

          <!-- 学生专属：学院 + 专业 -->
          <template v-if="form.role === 'student'">
            <div class="field">
              <div class="input-wrapper">
                <svg class="input-icon" viewBox="0 0 24 24" width="18" height="18">
                  <path d="M2 22L12 2l10 20H2z" fill="none" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                  <line x1="8" y1="15" x2="16" y2="15" stroke="currentColor" stroke-width="1.5"/>
                </svg>
                <input id="reg-college" v-model="form.college" type="text"
                  placeholder="请输入学院名称" class="input"
                  @input="clearFieldError('college')" />
              </div>
            </div>
            <div class="field">
              <div class="input-wrapper">
                <svg class="input-icon" viewBox="0 0 24 24" width="18" height="18">
                  <rect x="4" y="2" width="16" height="20" rx="2" fill="none" stroke="currentColor" stroke-width="2"/>
                  <line x1="8" y1="7" x2="16" y2="7" stroke="currentColor" stroke-width="1.5"/>
                  <line x1="8" y1="11" x2="16" y2="11" stroke="currentColor" stroke-width="1.5"/>
                </svg>
                <input id="reg-major" v-model="form.major" type="text"
                  placeholder="请输入专业名称" class="input"
                  @input="clearFieldError('major')" />
              </div>
            </div>
          </template>

          <!-- 验证码 -->
          <div class="field">
            <div class="code-row">
              <div class="input-wrapper" style="flex:1">
                <svg class="input-icon" viewBox="0 0 24 24" width="18" height="18">
                  <rect x="4" y="6" width="4" height="12" rx="1" fill="none" stroke="currentColor" stroke-width="2"/>
                  <rect x="10" y="6" width="4" height="12" rx="1" fill="none" stroke="currentColor" stroke-width="2"/>
                  <rect x="16" y="6" width="4" height="12" rx="1" fill="none" stroke="currentColor" stroke-width="2"/>
                </svg>
                <input id="reg-code" v-model="form.code" type="text"
                  placeholder="输入验证码" maxlength="6" class="input"
                  :class="{ 'input-error': fieldErrors.code }"
                  @input="clearFieldError('code')" />
              </div>
              <button type="button" class="btn-code"
                :disabled="codeCountdown > 0 || !form.phone"
                @click="sendCode">
                {{ codeCountdown > 0 ? `${codeCountdown}s` : '获取验证码' }}
              </button>
            </div>
            <span v-if="fieldErrors.code" class="field-err">{{ fieldErrors.code }}</span>
          </div>

          <!-- 提交 -->
          <button type="submit" class="btn-submit" :disabled="loading">
            <span v-if="loading" class="spinner"></span>
            <span v-else>注册</span>
          </button>

          <p class="switch-link">
            已有账号？<a href="#" @click.prevent="$emit('goLogin')">返回登录</a>
          </p>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { api } from '../api/request.js'

const emit = defineEmits(['goLogin'])

// ========== 背景图片 ==========
const backgroundImgUrl = ref('/登陆注册背景图.png')

// ========== 表单数据 ==========
const form = reactive({
  userId: '',
  userName: '',
  phone: '',
  password: '',
  gender: 'male',
  role: 'student',
  school: '',
  college: '',
  major: '',
  code: ''
})

const showPassword = ref(false)
const loading = ref(false)
const errorMsg = ref('')
const fieldErrors = reactive({})

function clearFieldError(field) {
  fieldErrors[field] = ''
  errorMsg.value = ''
}

// ========== 学校下拉框 ==========
const schools = ref([])
const schoolSearch = ref('')
const showDropdown = ref(false)
const dropdownRef = ref(null)

const filteredSchools = computed(() => {
  if (!schoolSearch.value.trim()) return schools.value
  const kw = schoolSearch.value.toLowerCase()
  return schools.value.filter(s => s.schoolName.toLowerCase().includes(kw))
})

function selectSchool(school) {
  form.school = school.schoolName
  schoolSearch.value = school.schoolName
  showDropdown.value = false
  clearFieldError('school')
}

function onSchoolInput() {
  showDropdown.value = true
}

function onClickOutside(e) {
  if (dropdownRef.value && !dropdownRef.value.contains(e.target)) {
    showDropdown.value = false
  }
}
onMounted(() => document.addEventListener('click', onClickOutside))
onUnmounted(() => document.removeEventListener('click', onClickOutside))

onMounted(async () => {
  try {
    const res = await api.getSchools()
    schools.value = res.data
  } catch (e) {
    console.error('加载学校列表失败:', e)
  }
})

// ========== 验证码 ==========
const codeCountdown = ref(0)
let timer = null

async function sendCode() {
  if (!/^\d{11}$/.test(form.phone)) {
    errorMsg.value = '请先输入正确的11位手机号'
    return
  }
  try {
    const res = await api.sendCode(form.phone)
    form.code = res.data
    alert('模拟验证码：' + res.data)
    codeCountdown.value = 60
    timer = setInterval(() => {
      codeCountdown.value--
      if (codeCountdown.value <= 0) {
        clearInterval(timer)
        timer = null
      }
    }, 1000)
  } catch (e) {
    errorMsg.value = e.message
  }
}
onUnmounted(() => { if (timer) clearInterval(timer) })

// ========== 表单校验 ==========
function validateForm() {
  let valid = true
  if (!form.userId) { fieldErrors.userId = '请输入学号/工号'; valid = false }
  if (!form.userName.trim()) { fieldErrors.userName = '请输入姓名'; valid = false }
  if (!/^\d{11}$/.test(form.phone)) { fieldErrors.phone = '请输入正确的11位手机号'; valid = false }
  if (!form.password || form.password.length < 6) { fieldErrors.password = '密码至少6位'; valid = false }
  if (!form.school) { fieldErrors.school = '请选择学校'; valid = false }
  if (!form.code) { fieldErrors.code = '请输入验证码'; valid = false }
  return valid
}

// ========== 注册提交 ==========
async function handleRegister() {
  errorMsg.value = ''
  Object.keys(fieldErrors).forEach(k => fieldErrors[k] = '')
  if (!validateForm()) return

  loading.value = true
  try {
    const body = {
      userId: Number(form.userId),
      userName: form.userName.trim(),
      phone: form.phone,
      password: form.password,
      gender: form.gender,
      role: form.role,
      school: form.school,
      college: form.role === 'student' ? form.college : null,
      major: form.role === 'student' ? form.major : null,
      code: form.code
    }
    await api.register(body)
    alert('注册成功！即将返回登录')
    // 切回登录页
    emit('goLogin')
  } catch (e) {
    errorMsg.value = e.message
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* ========== 全屏背景 ========== */
.register-page {
  position: fixed;
  inset: 0;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}

.register-bg {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-color: #4a5568;
}

.register-overlay {
  position: absolute;
  inset: 0;
  background: transparent;
}

/* ========== 弹窗居中容器 ========== */
.register-wrapper {
  position: absolute;
  inset: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 24px;
  padding-left: 74px;
}

.register-card {
  width: 500px;
  max-width: 100%;
  background: #fff;
  border-radius: 0;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.18), 0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 58px 40px 50px;
  margin-left: 630px;
  box-sizing: border-box;
}

/* ====== 头部 ====== */
.header {
  text-align: center;
  margin-bottom: 30px;
}
.title {
  font-size: 28px;
  color: #1a1a1a;
  font-weight: 700;
  margin-bottom: 10px;
  letter-spacing: 0.5px;
}
.subtitle {
  font-size: 14px;
  color: #aaa;
}

/* ====== 错误横幅 ====== */
.error-banner {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  color: #dc2626;
  font-size: 13px;
  margin-bottom: 24px;
  animation: slideDown .25s ease;
}
.error-icon { flex-shrink: 0; }

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-8px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* ====== 表单 ====== */
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

/* 输入框行 */
.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}
.input-icon {
  position: absolute;
  left: 14px;
  color: #bbb;
  pointer-events: none;
  z-index: 1;
}
.input {
  width: 100%;
  height: 48px;
  border: 1.5px solid #e2e2e2;
  border-radius: 0;
  padding: 0 44px 0 42px;
  font-size: 14px;
  outline: none;
  transition: border-color .25s, box-shadow .25s;
  box-sizing: border-box;
  background: #fff;
  color: #1a1a1a;
}
.input::placeholder {
  color: #bbb;
}
.input:focus {
  border-color: #6c63ff;
  box-shadow: 0 0 0 3px rgba(108, 99, 255, 0.1);
}
.input-error {
  border-color: #e74c3c;
}
.input-error:focus {
  box-shadow: 0 0 0 3px rgba(231, 76, 60, 0.1);
}

.field-err {
  font-size: 12px;
  color: #e74c3c;
  padding-left: 2px;
}

/* 密码可见性切换 */
.toggle-pwd {
  position: absolute;
  right: 10px;
  border: none;
  background: none;
  color: #bbb;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  transition: color .15s;
  z-index: 1;
}
.toggle-pwd:hover { color: #555; }

/* ========== 性别/角色 按钮组 ========== */
.radio-group {
  display: flex;
  gap: 10px;
  padding-top: 2px;
}
.radio {
  flex: 1;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1.5px solid #e2e2e2;
  border-radius: 0;
  cursor: pointer;
  font-size: 13px;
  color: #888;
  transition: all .2s;
  background: #fff;
}
.radio input { display: none; }
.radio:hover { border-color: #6c63ff; color: #6c63ff; }
.radio.active {
  border-color: #6c63ff;
  color: #6c63ff;
  background: #f5f3ff;
}

/* ========== 下拉 ========== */
.dropdown-wrapper { position: relative; }
.dropdown {
  position: absolute;
  top: 52px;
  left: 0;
  right: 0;
  max-height: 200px;
  overflow-y: auto;
  background: #fff;
  border: 1.5px solid #e2e2e2;
  border-radius: 0;
  box-shadow: 0 4px 12px rgba(0,0,0,.1);
  z-index: 10;
  list-style: none;
  padding: 4px 0;
}
.dropdown li {
  padding: 10px 14px;
  font-size: 13px;
  color: #333;
  cursor: pointer;
  transition: background .15s;
}
.dropdown li:hover { background: #f5f3ff; }

/* ========== 验证码行 ========== */
.code-row { display: flex; gap: 10px; }
.btn-code {
  width: 110px;
  height: 48px;
  border: none;
  border-radius: 0;
  background: rgb(54, 184, 243);
  color: #fff;
  font-size: 13px;
  cursor: pointer;
  white-space: nowrap;
  transition: opacity .2s;
  flex-shrink: 0;
}
.btn-code:hover { opacity: .88; }
.btn-code:disabled {
  background: #ccc;
  cursor: not-allowed;
  opacity: 1;
}

/* ========== 提交按钮 ========== */
.btn-submit {
  width: 100%;
  height: 48px;
  border: none;
  border-radius: 0;
  background: rgb(54, 184, 243);
  color: rgb(255, 255, 255);
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  margin-top: 6px;
  transition: opacity .2s, transform .15s, box-shadow .2s;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 14px rgba(54, 184, 243, 0.35);
}
.btn-submit:hover {
  opacity: 0.92;
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(54, 184, 243, 0.45);
}
.btn-submit:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.switch-link {
  text-align: center;
  font-size: 13px;
  color: #aaa;
  margin-top: 6px;
}
.switch-link a {
  color: #4a90d9;
  text-decoration: none;
  font-weight: 500;
}
.switch-link a:hover {
  text-decoration: underline;
}

/* 加载动画 */
.spinner {
  width: 22px;
  height: 22px;
  border: 2.5px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin .6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* ========== 响应式 ========== */
@media (max-width: 480px) {
  .register-wrapper {
    padding: 16px;
    align-items: flex-start;
    padding-top: 80px;
  }
  .register-card {
    padding: 36px 24px 28px;
    border-radius: 0;
    width: 100%;
  }
  .title { font-size: 24px; }
}
</style>
