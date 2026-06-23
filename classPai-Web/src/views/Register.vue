<template>
  <div class="register-card">
    <h2 class="title">注册 ClassPai</h2>

    <div class="form">
      <!-- 学号/工号 -->
      <div class="field">
        <label>学号/工号</label>
        <input v-model="form.userId" type="number" placeholder="请输入学号或工号"
          class="input" />
      </div>

      <!-- 姓名 -->
      <div class="field">
        <label>姓名</label>
        <input v-model="form.userName" type="text" placeholder="请输入真实姓名"
          class="input" />
      </div>

      <!-- 手机号 -->
      <div class="field">
        <label>手机号</label>
        <input v-model="form.phone" type="tel" placeholder="请输入手机号"
          maxlength="11" class="input" />
      </div>

      <!-- 密码 -->
      <div class="field">
        <label>密码</label>
        <input v-model="form.password" type="password" placeholder="请输入密码"
          class="input" />
      </div>

      <!-- 性别 -->
      <div class="field">
        <label>性别</label>
        <div class="radio-group">
          <label class="radio" :class="{ active: form.gender === 'male' }">
            <input v-model="form.gender" type="radio" value="male" />
            男
          </label>
          <label class="radio" :class="{ active: form.gender === 'female' }">
            <input v-model="form.gender" type="radio" value="female" />
            女
          </label>
        </div>
      </div>

      <!-- 角色选择 -->
      <div class="field">
        <label>角色</label>
        <div class="radio-group">
          <label class="radio" :class="{ active: form.role === 'student' }">
            <input v-model="form.role" type="radio" value="student" />
            学生
          </label>
          <label class="radio" :class="{ active: form.role === 'teacher' }">
            <input v-model="form.role" type="radio" value="teacher" />
            教师
          </label>
        </div>
      </div>

      <!-- 学校下拉框 -->
      <div class="field">
        <label>学校</label>
        <div class="dropdown-wrapper" ref="dropdownRef">
          <input v-model="schoolSearch" type="text" placeholder="搜索或选择学校"
            class="input" @focus="showDropdown = true" @input="onSchoolInput" />
          <ul v-if="showDropdown && filteredSchools.length" class="dropdown">
            <li v-for="s in filteredSchools" :key="s.id"
              @click="selectSchool(s)">
              {{ s.schoolName }}
            </li>
          </ul>
        </div>
      </div>

      <!-- 学生专属：学院 + 专业 -->
      <template v-if="form.role === 'student'">
        <div class="field">
          <label>学院</label>
          <input v-model="form.college" type="text" placeholder="请输入学院名称"
            class="input" />
        </div>
        <div class="field">
          <label>专业</label>
          <input v-model="form.major" type="text" placeholder="请输入专业名称"
            class="input" />
        </div>
      </template>

      <!-- 验证码 -->
      <div class="field">
        <label>验证码</label>
        <div class="code-row">
          <input v-model="form.code" type="text" placeholder="输入验证码"
            maxlength="6" class="input code-input" />
          <button class="btn-code" :disabled="codeBtnDisabled"
            @click="sendCode">
            {{ codeBtnText }}
          </button>
        </div>
      </div>

      <!-- 提交 -->
      <button class="btn-submit" @click="handleRegister">注册</button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { api } from '../api/request.js'

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

// 学校下拉框
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
}

function onSchoolInput() {
  showDropdown.value = true
}

// 点击外部关闭下拉
function onClickOutside(e) {
  if (dropdownRef.value && !dropdownRef.value.contains(e.target)) {
    showDropdown.value = false
  }
}
onMounted(() => document.addEventListener('click', onClickOutside))
onUnmounted(() => document.removeEventListener('click', onClickOutside))

// 加载学校列表
onMounted(async () => {
  try {
    const res = await api.getSchools()
    schools.value = res.data
  } catch (e) {
    console.error('加载学校列表失败:', e)
  }
})

// 验证码
const codeCountdown = ref(0)
let timer = null

const codeBtnDisabled = computed(() => codeCountdown.value > 0 || !form.phone)
const codeBtnText = computed(() => {
  if (codeCountdown.value > 0) return `${codeCountdown.value}s`
  return '获取验证码'
})

async function sendCode() {
  if (!form.phone) return alert('请先输入手机号')
  try {
    await api.sendCode(form.phone)
    alert('验证码已发送')
    codeCountdown.value = 60
    timer = setInterval(() => {
      codeCountdown.value--
      if (codeCountdown.value <= 0) {
        clearInterval(timer)
        timer = null
      }
    }, 1000)
  } catch (e) {
    alert(e.message)
  }
}
onUnmounted(() => { if (timer) clearInterval(timer) })

// 注册
async function handleRegister() {
  if (!form.userId) return alert('请输入学号/工号')
  if (!form.userName) return alert('请输入姓名')
  if (!form.phone)   return alert('请输入手机号')
  if (!form.password) return alert('请输入密码')
  if (!form.school)  return alert('请选择学校')
  if (!form.code)    return alert('请输入验证码')

  const body = {
    userId: Number(form.userId),
    userName: form.userName,
    phone: form.phone,
    password: form.password,
    gender: form.gender,
    role: form.role,
    school: form.school,
    college: form.role === 'student' ? form.college : null,
    major: form.role === 'student' ? form.major : null,
    code: form.code
  }

  try {
    await api.register(body)
    alert('注册成功！')
  } catch (e) {
    alert(e.message)
  }
}
</script>

<style scoped>
.register-card {
  width: 420px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 16px rgba(0,0,0,.08);
  padding: 40px 36px;
}
.title {
  text-align: center;
  font-size: 22px;
  color: #1a1a1a;
  margin-bottom: 28px;
}
.form { display: flex; flex-direction: column; gap: 18px; }
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
  height: 40px;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 0 12px;
  font-size: 14px;
  outline: none;
  transition: border-color .2s;
}
.input:focus { border-color: #4a90d9; }

/* 性别/角色 按钮组 */
.radio-group {
  display: flex;
  gap: 12px;
}
.radio {
  flex: 1;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #ddd;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all .2s;
}
.radio input { display: none; }
.radio.active {
  border-color: #4a90d9;
  color: #4a90d9;
  background: #f0f6ff;
}

/* 下拉 */
.dropdown-wrapper { position: relative; }
.dropdown {
  position: absolute;
  top: 44px;
  left: 0;
  right: 0;
  max-height: 200px;
  overflow-y: auto;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,.1);
  z-index: 10;
  list-style: none;
  padding: 4px 0;
}
.dropdown li {
  padding: 8px 12px;
  font-size: 13px;
  color: #333;
  cursor: pointer;
}
.dropdown li:hover { background: #f0f6ff; }

/* 验证码行 */
.code-row { display: flex; gap: 10px; }
.code-input { flex: 1; }
.btn-code {
  width: 110px;
  height: 40px;
  border: none;
  border-radius: 8px;
  background: #4a90d9;
  color: #fff;
  font-size: 13px;
  cursor: pointer;
  white-space: nowrap;
  transition: background .2s;
}
.btn-code:hover { background: #3a7bc8; }
.btn-code:disabled {
  background: #ccc;
  cursor: not-allowed;
}

/* 提交 */
.btn-submit {
  width: 100%;
  height: 44px;
  border: none;
  border-radius: 8px;
  background: #4a90d9;
  color: #fff;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  margin-top: 4px;
  transition: background .2s;
}
.btn-submit:hover { background: #3a7bc8; }
</style>
