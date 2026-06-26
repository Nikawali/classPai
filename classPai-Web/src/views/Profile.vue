<template>
  <div class="profile">
    <!-- ==================== 头像区（保留不变） ==================== -->
    <div class="avatar-section">
      <div class="avatar">{{ initial }}</div>
      <h2 class="name">{{ user.userName || '加载中...' }}</h2>
      <span class="role-tag">{{ roleText }}</span>
    </div>

    <!-- ==================== 信息卡片（保留不变） ==================== -->
    <div class="info-card">
      <div class="info-item">
        <span class="info-label">学号/工号</span>
        <span class="info-value">{{ user.userId }}</span>
      </div>
      <div class="info-item">
        <span class="info-label">手机号</span>
        <span class="info-value">{{ user.phone || '未绑定' }}</span>
      </div>
      <div class="info-item">
        <span class="info-label">角色</span>
        <span class="info-value">{{ roleText }}</span>
      </div>
      <div class="info-item">
        <span class="info-label">注册时间</span>
        <span class="info-value">{{ user.createTime || '-' }}</span>
      </div>
    </div>

    <!-- ==================== 编辑信息模块（优先展示） ==================== -->
    <div class="expand-section">
      <button class="expand-entry" @click="editExpanded = !editExpanded">
        <div class="expand-entry-left">
          <svg class="expand-entry-icon" viewBox="0 0 24 24" width="20" height="20">
            <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04a1 1 0 000-1.41l-2.34-2.34a1 1 0 00-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"
              fill="currentColor"/>
          </svg>
          <span class="expand-entry-text">账号更改</span>
        </div>
        <svg class="expand-arrow" :class="{ rotated: editExpanded }"
          viewBox="0 0 24 24" width="18" height="18">
          <path d="M8 10l4 4 4-4" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </button>

      <div class="expand-body" :class="{ expanded: editExpanded }">
        <div class="expand-body-inner">
          <!-- 操作结果提示 -->
          <div v-if="editMsg" class="toast" :class="editOk ? 'toast-ok' : 'toast-err'">
            {{ editMsg }}
          </div>

          <!-- 手机号 + 获取验证码 -->
          <div class="field">
            <label>新手机号</label>
            <div class="phone-row">
              <input v-model="editForm.phone" type="tel" class="input flex-input" maxlength="11"
                :class="{ 'input-err': editErrors.phone }"
                :placeholder="user.phone || '未绑定'"
                @input="editErrors.phone = ''" />
              <button type="button" class="btn-code"
                :disabled="codeCd > 0 || !editForm.phone"
                @click="sendSmsForEdit">
                {{ codeCd > 0 ? `${codeCd}s` : '获取验证码' }}
              </button>
            </div>
            <span v-if="editErrors.phone" class="field-err">{{ editErrors.phone }}</span>
          </div>

          <!-- 短信验证码 -->
          <div class="field">
            <label>短信验证码</label>
            <input v-model="editForm.smsCode" type="text" class="input" maxlength="6"
              :class="{ 'input-err': editErrors.smsCode }"
              placeholder="请输入6位验证码"
              @input="editErrors.smsCode = ''" />
            <span v-if="editErrors.smsCode" class="field-err">{{ editErrors.smsCode }}</span>
          </div>

          <!-- 新密码 -->
          <div class="field">
            <label>新密码</label>
            <input v-model="editForm.password" type="password" class="input" maxlength="20"
              :class="{ 'input-err': editErrors.password }"
              placeholder="≥6位"
              @input="editErrors.password = ''" />
            <span v-if="editErrors.password" class="field-err">{{ editErrors.password }}</span>
            <span v-else class="field-hint">≥6位</span>
          </div>

          <!-- 确认密码 -->
          <div class="field">
            <label>确认密码</label>
            <input v-model="editForm.confirmPwd" type="password" class="input" maxlength="20"
              :class="{ 'input-err': editErrors.confirmPwd }"
              placeholder="请再次输入新密码"
              @input="editErrors.confirmPwd = ''" />
            <span v-if="editErrors.confirmPwd" class="field-err">{{ editErrors.confirmPwd }}</span>
          </div>

          <!-- 提交按钮 -->
          <button class="btn-submit" :disabled="editSubmitting" @click="handleEditSubmit">
            <span v-if="editSubmitting" class="spinner"></span>
            <span v-else>保存修改</span>
          </button>
          <p class="submit-hint">
            <!-- TODO: 调用后端接口 POST /api/user/profile 更新手机号和密码 -->
          </p>
        </div>
      </div>
    </div>

    <!-- ==================== 身份认证模块 ==================== -->
    <div class="expand-section">
      <button class="expand-entry" @click="authExpanded = !authExpanded">
        <div class="expand-entry-left">
          <svg class="expand-entry-icon" viewBox="0 0 24 24" width="20" height="20">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 15l-4-4 1.41-1.41L11 14.17l5.59-5.59L18 10l-7 7z"
              fill="none" stroke="currentColor" stroke-width="2"/>
          </svg>
          <span class="expand-entry-text">身份认证</span>
        </div>
        <svg class="expand-arrow" :class="{ rotated: authExpanded }"
          viewBox="0 0 24 24" width="18" height="18">
          <path d="M8 10l4 4 4-4" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </button>

      <div class="expand-body" :class="{ expanded: authExpanded }">
        <div class="expand-body-inner">
          <div v-if="authMsg" class="toast" :class="authOk ? 'toast-ok' : 'toast-err'">
            {{ authMsg }}
          </div>

          <!-- 学校（只读） -->
          <div class="field">
            <label>学校</label>
            <input v-model="authForm.school" type="text" disabled class="input input-disabled"
              placeholder="数据加载中..." />
            <span class="field-hint">学校信息由管理员设置，不可修改</span>
          </div>

          <!-- 学院（可编辑） -->
          <div class="field">
            <label>学院</label>
            <input v-model="authForm.college" type="text" class="input" maxlength="50"
              :class="{ 'input-err': authErrors.college }"
              placeholder="请输入学院名称"
              @input="authErrors.college = ''" />
            <span v-if="authErrors.college" class="field-err">{{ authErrors.college }}</span>
            <span v-else class="field-hint">不超过50字符</span>
          </div>

          <!-- 专业（可编辑） -->
          <div class="field">
            <label>专业</label>
            <input v-model="authForm.major" type="text" class="input" maxlength="50"
              :class="{ 'input-err': authErrors.major }"
              placeholder="请输入专业名称"
              @input="authErrors.major = ''" />
            <span v-if="authErrors.major" class="field-err">{{ authErrors.major }}</span>
            <span v-else class="field-hint">不超过50字符</span>
          </div>

          <button class="btn-submit" :disabled="authSubmitting" @click="handleAuthSubmit">
            <span v-if="authSubmitting" class="spinner"></span>
            <span v-else>保存修改</span>
          </button>
          <p class="submit-hint">
            <!-- TODO: 调用后端接口 POST /api/user/auth-info 更新实名信息 -->
          </p>
        </div>
      </div>
    </div>

    <!-- 退出登录 -->
    <button class="btn-logout" @click="logout">退出登录</button>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'

const emit = defineEmits(['logout'])

// ==================== 用户数据 ====================
const user = ref({})

const initial = computed(() => {
  return user.value.userName ? user.value.userName.charAt(0).toUpperCase() : '?'
})
const roleText = computed(() => {
  const r = user.value.role
  if (r === 'teacher') return '教师'
  if (r === 'student') return '学生'
  return r || '-'
})

onMounted(async () => {
  try {
    const { api } = await import('../api/request.js')
    const token = sessionStorage.getItem('token')
    if (!token) { emit('logout'); return }
    const res = await api.getProfile(token)
    user.value = res.data
    editForm.phone = res.data.phone || ''
    authForm.school = res.data.school || ''
    authForm.college = res.data.college || ''
    authForm.major = res.data.major || ''
  } catch (e) {
    console.error('获取用户信息失败:', e.message)
  }
})

// ================================================================
//  「编辑信息」模块 — 手机号 + 密码修改（优先展示）
// ================================================================
const editExpanded = ref(false)

const editForm = reactive({
  phone: '',
  smsCode: '',
  password: '',
  confirmPwd: ''
})

const editErrors = reactive({
  phone: '',
  smsCode: '',
  password: '',
  confirmPwd: ''
})

const editSubmitting = ref(false)
const editMsg = ref('')
const editOk = ref(false)

// 验证码倒计时
const codeCd = ref(0)
let codeTimer = null

async function sendSmsForEdit() {
  if (!/^1\d{10}$/.test(editForm.phone)) {
    editErrors.phone = '请输入正确的11位手机号'
    return
  }
  editErrors.phone = ''

  try {
    const { api } = await import('../api/request.js')
    const token = sessionStorage.getItem('token')
    const res = await api.sendCodeForProfile(token, editForm.phone)
    editOk.value = true
    editMsg.value = '验证码已发送（演示：' + res.data + '）'
    codeCd.value = 60
    codeTimer = setInterval(() => {
      codeCd.value--
      if (codeCd.value <= 0) {
        clearInterval(codeTimer)
        codeTimer = null
      }
    }, 1000)
  } catch (e) {
    editErrors.phone = e.message || '验证码发送失败'
  }
}

function validateEditForm() {
  let valid = true

  // 手机号
  if (editForm.phone && !/^1\d{10}$/.test(editForm.phone)) {
    editErrors.phone = '请输入正确的11位手机号'
    valid = false
  }

  // 手机号或密码任一变更都需要验证码
  if ((editForm.phone && editForm.phone !== user.value.phone) || editForm.password) {
    if (!editForm.smsCode) {
      editErrors.smsCode = '修改手机号或密码需要输入验证码'
      valid = false
    } else if (!/^\d{6}$/.test(editForm.smsCode)) {
      editErrors.smsCode = '验证码为6位数字'
      valid = false
    }
  }

  // 密码
  if (editForm.password) {
    if (editForm.password.length < 6) {
      editErrors.password = '密码至少6位'
      valid = false
    }

    if (!editForm.confirmPwd) {
      editErrors.confirmPwd = '请确认新密码'
      valid = false
    } else if (editForm.password !== editForm.confirmPwd) {
      editErrors.confirmPwd = '两次输入的密码不一致'
      valid = false
    }
  }

  return valid
}

async function handleEditSubmit() {
  editMsg.value = ''
  Object.keys(editErrors).forEach(k => editErrors[k] = '')

  if (!validateEditForm()) return

  // 至少修改一项
  if (!editForm.phone && !editForm.password) {
    editOk.value = false
    editMsg.value = '请至少填写一项需要修改的信息'
    return
  }

  editSubmitting.value = true
  try {
    const { api } = await import('../api/request.js')
    const token = sessionStorage.getItem('token')
    const body = {
      phone: editForm.phone || undefined,
      password: editForm.password || undefined,
      smsCode: editForm.smsCode || undefined
    }
    await api.updateProfile(token, body)

    editOk.value = true
    editMsg.value = '信息修改成功'
    if (editForm.phone) user.value.phone = editForm.phone
    editForm.phone = ''
    editForm.password = ''
    editForm.confirmPwd = ''
    editForm.smsCode = ''
  } catch (e) {
    editOk.value = false
    editMsg.value = e.message || '修改失败，请重试'
  } finally {
    editSubmitting.value = false
  }
}

// ================================================================
//  「身份认证」模块 — 仅保留实名认证（学校/学院/专业）
// ================================================================
const authExpanded = ref(false)

const authForm = reactive({
  school: '',
  college: '',
  major: ''
})

const authErrors = reactive({
  college: '',
  major: ''
})

const authSubmitting = ref(false)
const authMsg = ref('')
const authOk = ref(false)

function validateAuthForm() {
  let valid = true
  if (authForm.college && authForm.college.length > 50) {
    authErrors.college = '学院名称不能超过50字符'
    valid = false
  }
  if (authForm.major && authForm.major.length > 50) {
    authErrors.major = '专业名称不能超过50字符'
    valid = false
  }
  return valid
}

async function handleAuthSubmit() {
  authMsg.value = ''
  Object.keys(authErrors).forEach(k => authErrors[k] = '')

  if (!validateAuthForm()) return

  authSubmitting.value = true
  try {
    const { api } = await import('../api/request.js')
    const token = sessionStorage.getItem('token')
    await api.updateProfile(token, {
      college: authForm.college || undefined,
      major: authForm.major || undefined
    })

    authOk.value = true
    authMsg.value = '认证信息更新成功'
  } catch (e) {
    authOk.value = false
    authMsg.value = e.message || '更新失败，请重试'
  } finally {
    authSubmitting.value = false
  }
}

function logout() {
  sessionStorage.removeItem('token')
  emit('logout')
}
</script>

<style scoped>
.profile {
  padding: 20px 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

/* ========== 头像区（保留） ========== */
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 28px 0 16px;
}
.avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-size: 30px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}
.name { font-size: 20px; color: #1a1a1a; font-weight: 600; }
.role-tag {
  font-size: 12px; padding: 3px 14px; border-radius: 20px;
  background: #f0f4ff; color: #4a6cf7; font-weight: 500;
}

/* ========== 信息卡片（保留） ========== */
.info-card {
  width: 100%; background: #fff; border-radius: 12px;
  overflow: hidden; box-shadow: 0 1px 6px rgba(0,0,0,.04);
}
.info-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16px 18px; border-bottom: 1px solid #f5f5f5;
}
.info-item:last-child { border-bottom: none; }
.info-label { font-size: 14px; color: #888; }
.info-value { font-size: 14px; color: #333; font-weight: 500; }

/* ========== 可展开模块（编辑信息 / 身份认证共用样式） ========== */
.expand-section { width: 100%; }

.expand-entry {
  width: 100%; display: flex; justify-content: space-between; align-items: center;
  padding: 16px 18px; background: #fff; border: none; border-radius: 12px;
  cursor: pointer; box-shadow: 0 1px 6px rgba(0,0,0,.04);
  transition: box-shadow .2s;
}
.expand-entry:hover { box-shadow: 0 2px 10px rgba(0,0,0,.08); }
.expand-entry-left { display: flex; align-items: center; gap: 10px; }
.expand-entry-icon { color: #4a6cf7; }
.expand-entry-text { font-size: 15px; color: #333; font-weight: 500; }
.expand-arrow { color: #bbb; transition: transform .25s; }
.expand-arrow.rotated { transform: rotate(180deg); }

.expand-body {
  max-height: 0; overflow: hidden; transition: max-height .35s ease;
}
.expand-body.expanded { max-height: 1000px; }
.expand-body-inner {
  background: #fff; border-radius: 0 0 12px 12px;
  padding: 0 18px 20px; margin-top: -6px;
  box-shadow: 0 1px 6px rgba(0,0,0,.04);
  display: flex; flex-direction: column; gap: 16px;
}

/* 提示条 */
.toast {
  padding: 10px 14px; border-radius: 8px; font-size: 13px;
  text-align: center; animation: fadeIn .25s ease;
}
.toast-ok { background: #f0fdf4; color: #16a34a; border: 1px solid #bbf7d0; }
.toast-err { background: #fef2f2; color: #dc2626; border: 1px solid #fecaca; }
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-6px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* 表单字段 */
.field { display: flex; flex-direction: column; gap: 4px; }
.field label { font-size: 13px; color: #555; font-weight: 500; }
.input {
  width: 100%; height: 42px; border: 1px solid #ddd; border-radius: 8px;
  padding: 0 12px; font-size: 14px; outline: none;
  transition: border-color .2s; box-sizing: border-box; background: #fafafa;
}
.input:focus { border-color: #4a6cf7; background: #fff; }
.input-err { border-color: #e74c3c; }
.input-err:focus { border-color: #e74c3c; }
.input-disabled { background: #f3f4f6; color: #999; cursor: not-allowed; }

/* 手机号 + 验证码按钮行 */
.phone-row { display: flex; gap: 10px; }
.flex-input { flex: 1; }
.btn-code {
  width: 110px; height: 42px; border: none; border-radius: 8px;
  background: #4a6cf7; color: #fff; font-size: 13px; cursor: pointer;
  white-space: nowrap; transition: background .2s; flex-shrink: 0;
}
.btn-code:hover { background: #3d5bd9; }
.btn-code:disabled { background: #ccc; cursor: not-allowed; }

.field-hint { font-size: 11px; color: #bbb; padding-left: 2px; }
.field-err { font-size: 12px; color: #e74c3c; padding-left: 2px; }

/* 提交按钮 */
.btn-submit {
  width: 100%; height: 44px; border: none; border-radius: 10px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff; font-size: 15px; font-weight: 600; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: opacity .2s;
}
.btn-submit:hover { opacity: .92; }
.btn-submit:disabled { opacity: .6; cursor: not-allowed; }

.submit-hint { font-size: 11px; color: #ccc; text-align: center; margin-top: -8px; font-style: italic; }

.spinner {
  width: 20px; height: 20px; border: 2.5px solid rgba(255,255,255,.3);
  border-top-color: #fff; border-radius: 50%; animation: spin .6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* 退出按钮 */
.btn-logout {
  width: 100%; height: 44px; border: none; border-radius: 10px;
  background: #fff; color: #e74c3c; font-size: 15px; cursor: pointer;
  box-shadow: 0 1px 4px rgba(0,0,0,.06); margin-top: 8px; transition: background .2s;
}
.btn-logout:hover { background: #fef2f2; }

/* ========== 响应式 ========== */
@media (min-width: 768px) {
  .profile { max-width: 500px; margin: 0 auto; padding: 32px 20px; }
}
@media (min-width: 1024px) {
  .profile { max-width: 540px; }
}
</style>
