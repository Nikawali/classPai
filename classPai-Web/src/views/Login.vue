<template>
  <div class="login-wrapper">
    <div class="login-card">
      <!-- 标题 -->
      <div class="header">
        <h1 class="title">欢迎回来</h1>
        <p class="subtitle">登录 ClassPai 课堂管理平台</p>
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
      <form class="form" @submit.prevent="handleLogin">
        <!-- 账号 -->
        <div class="field">
          <label for="userAccount">账号</label>
          <div class="input-wrapper">
            <svg class="input-icon" viewBox="0 0 24 24" width="18" height="18">
              <circle cx="12" cy="8" r="4" fill="none" stroke="currentColor" stroke-width="2"/>
              <path d="M4 20c0-4 4-7 8-7s8 3 8 7" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            <input
              id="userAccount"
              v-model="form.userAccount"
              type="text"
              placeholder="请输入手机号或学号"
              class="input"
              :class="{ 'input-error': fieldErrors.userAccount }"
              autocomplete="userAccount"
              @input="clearFieldError('userAccount')"
            />
          </div>
          <span v-if="fieldErrors.userAccount" class="field-err">{{ fieldErrors.userAccount }}</span>
        </div>

        <!-- 密码 -->
        <div class="field">
          <label for="password">密码</label>
          <div class="input-wrapper">
            <svg class="input-icon" viewBox="0 0 24 24" width="18" height="18">
              <rect x="3" y="11" width="18" height="11" rx="2" fill="none" stroke="currentColor" stroke-width="2"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            <input
              id="password"
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="请输入密码"
              class="input"
              :class="{ 'input-error': fieldErrors.password }"
              autocomplete="current-password"
              @input="clearFieldError('password')"
            />
            <button
              type="button"
              class="toggle-pwd"
              :aria-label="showPassword ? '隐藏密码' : '显示密码'"
              @click="showPassword = !showPassword"
            >
              <!-- 眼睛图标 -->
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

        <!-- 提交按钮 -->
        <button type="submit" class="btn-submit" :disabled="loading">
          <span v-if="loading" class="spinner"></span>
          <span v-else>登 录</span>
        </button>

        <p class="switch-link">
          没有账号？<a href="#" @click.prevent="$emit('goRegister')">立即注册</a>
        </p>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { api } from '../api/request.js'

const emit = defineEmits(['goRegister', 'loginSuccess'])

// ========== 表单 ==========
const form = reactive({ userAccount: '', password: '' })
const showPassword = ref(false)
const loading = ref(false)
const errorMsg = ref('')
const fieldErrors = reactive({ userAccount: '', password: '' })

function clearFieldError(field) {
  fieldErrors[field] = ''
  errorMsg.value = ''
}

// ========== 提交 ==========
async function handleLogin() {
  // 清除旧错误
  errorMsg.value = ''
  fieldErrors.userAccount = ''
  fieldErrors.password = ''

  // 前端校验
  let valid = true
  if (!form.userAccount.trim()) {
    fieldErrors.userAccount = '请输入手机号或学号'
    valid = false
  }
  if (!form.password) {
    fieldErrors.password = '请输入密码'
    valid = false
  }
  if (!valid) return

  loading.value = true
  try {
    const res = await api.login({
      userAccount: form.userAccount.trim(),
      password: form.password
    })
    const data = res.data
    // token 存入 sessionStorage，关闭页面即失效
    sessionStorage.setItem('token', data.token)
    sessionStorage.setItem('role', data.role)
    alert(`登录成功！\n用户：${data.userName}\n角色：${data.role === 'TEACHER' ? '教师' : '学生'}`)
    emit('loginSuccess')
  } catch (e) {
    errorMsg.value = e.message
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrapper {
  width: 100%;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #667eea20 0%, #764ba220 100%);
}

.login-card {
  width: 400px;
  max-width: 100%;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0,0,0,.08);
  padding: 44px 36px 36px;
}

/* ====== 头部 ====== */
.header {
  text-align: center;
  margin-bottom: 32px;
}
.title {
  font-size: 26px;
  color: #1a1a1a;
  font-weight: 700;
  margin-bottom: 8px;
}
.subtitle {
  font-size: 14px;
  color: #999;
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
  margin-bottom: 20px;
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
  gap: 20px;
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

/* 输入框行 */
.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}
.input-icon {
  position: absolute;
  left: 12px;
  color: #bbb;
  pointer-events: none;
}
.input {
  width: 100%;
  height: 44px;
  border: 1.5px solid #e0e0e0;
  border-radius: 10px;
  padding: 0 42px 0 38px;
  font-size: 14px;
  outline: none;
  transition: border-color .2s, box-shadow .2s;
  box-sizing: border-box;
  background: #fafafa;
}
.input:focus {
  border-color: #4a90d9;
  box-shadow: 0 0 0 3px rgba(74,144,217,.12);
  background: #fff;
}
.input-error {
  border-color: #e74c3c;
}
.input-error:focus {
  box-shadow: 0 0 0 3px rgba(231,76,60,.12);
}

/* 密码可见性切换 */
.toggle-pwd {
  position: absolute;
  right: 8px;
  border: none;
  background: none;
  color: #bbb;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  transition: color .15s;
}
.toggle-pwd:hover { color: #666; }

.field-err {
  font-size: 12px;
  color: #e74c3c;
  padding-left: 2px;
}

/* 提交按钮 */
.btn-submit {
  width: 100%;
  height: 46px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea, #764ba2);
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
  transform: translateY(-1px);
}
.btn-submit:disabled {
  opacity: .6;
  cursor: not-allowed;
  transform: none;
}

/* 注册跳转 */
.switch-link {
  text-align: center;
  font-size: 13px;
  color: #999;
  margin-top: 4px;
}
.switch-link a {
  color: #4a90d9;
  text-decoration: none;
}

/* 加载动画 */
.spinner {
  width: 22px;
  height: 22px;
  border: 2.5px solid rgba(255,255,255,.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin .6s linear infinite;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ========== 响应式 ========== */
@media (max-width: 480px) {
  .login-wrapper {
    padding: 16px;
    align-items: flex-start;
    padding-top: 60px;
    background: #fff;
  }
  .login-card {
    box-shadow: none;
    padding: 0;
    border-radius: 0;
  }
  .title { font-size: 22px; }
}
</style>
