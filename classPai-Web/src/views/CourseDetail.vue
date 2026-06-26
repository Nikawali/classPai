<template>
  <div class="course-detail-page" v-if="course">
    <!-- ==================== 椤堕儴 Banner ==================== -->
    <div class="banner" :style="{ background: bannerColor }">
      <!-- 娓愬彉绾㈠簳 + 瑁呴グ鍙剁墖 -->
      <div class="banner-bg">
        <svg class="banner-leaf leaf-1" viewBox="0 0 120 160" fill="none">
          <path d="M60 0 C30 40 0 80 10 130 C20 160 50 160 60 130 C70 160 100 160 110 130 C120 80 90 40 60 0Z" fill="rgba(255,255,255,.08)"/>
          <line x1="60" y1="10" x2="60" y2="140" stroke="rgba(255,255,255,.06)" stroke-width="1"/>
          <path d="M60 140 C60 100 40 60 10 40" fill="none" stroke="rgba(255,255,255,.05)" stroke-width="1"/>
          <path d="M60 140 C60 100 80 60 110 40" fill="none" stroke="rgba(255,255,255,.05)" stroke-width="1"/>
        </svg>
        <svg class="banner-leaf leaf-2" viewBox="0 0 100 140" fill="none">
          <path d="M50 10 C25 45 5 80 12 120 C18 140 40 140 50 115 C60 140 85 140 90 120 C98 80 75 45 50 10Z" fill="rgba(255,255,255,.06)"/>
        </svg>
      </div>

      <!-- 杩斿洖鎸夐挳 -->
      <button class="banner-back" @click="$emit('back')">
        <svg viewBox="0 0 24 24" width="20" height="20">
          <path d="M15 18l-6-6 6-6" fill="none" stroke="#fff" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </button>

      <!-- 宸︿晶淇℃伅 -->
      <div class="banner-main">
        <p class="banner-class">{{ classText }}</p>
        <h1 class="banner-title">{{ course.courseName }}</h1>
        <div class="banner-code-row">
          <span class="banner-code">鍔犺鐮?{{ course.courseCode }}</span>
          <button class="banner-code-copy" @click.stop="copyCode">
            <svg viewBox="0 0 24 24" width="14" height="14">
              <rect x="8" y="2" width="12" height="16" rx="2" fill="none" stroke="currentColor" stroke-width="1.5"/>
              <path d="M16 6V4C16 2.9 15.1 2 14 2H4C2.9 2 2 2.9 2 4V16C2 17.1 2.9 18 4 18H6" fill="none" stroke="currentColor" stroke-width="1.5"/>
            </svg>
            澶嶅埗
          </button>
        </div>
      </div>

      <!-- 鍙充晶缁熻 -->
      <div class="banner-stats">
        <div class="stat-item">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none">
            <circle cx="12" cy="9" r="3.5" stroke="rgba(255,255,255,.7)" stroke-width="1.5"/>
            <path d="M5 20c0-3.9 3.1-7 7-7s7 3.1 7 7" stroke="rgba(255,255,255,.7)" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <span>{{ course.studentCount || 0 }} 鎴愬憳</span>
        </div>
        <div class="stat-item">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none">
            <rect x="2" y="3" width="8" height="8" rx="1.5" stroke="rgba(255,255,255,.7)" stroke-width="1.5"/>
            <rect x="14" y="3" width="8" height="8" rx="1.5" stroke="rgba(255,255,255,.7)" stroke-width="1.5"/>
            <rect x="2" y="14" width="8" height="8" rx="1.5" stroke="rgba(255,255,255,.7)" stroke-width="1.5"/>
            <rect x="14" y="14" width="8" height="8" rx="1.5" stroke="rgba(255,255,255,.7)" stroke-width="1.5"/>
          </svg>
          <span>0 鍒嗙粍</span>
        </div>
        <div class="stat-item">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none">
            <circle cx="12" cy="12" r="9" stroke="rgba(255,255,255,.7)" stroke-width="1.5"/>
            <circle cx="9.5" cy="9.5" r="1" fill="rgba(255,255,255,.5)"/>
            <circle cx="14.5" cy="9.5" r="1" fill="rgba(255,255,255,.5)"/>
            <path d="M7 14c.8-1.5 2.8-2.5 5-2.5s4.2 1 5 2.5" stroke="rgba(255,255,255,.7)" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <span>{{ course.courseIntro ? '鏈? : '鏆傛棤' }}浠嬬粛</span>
        </div>
      </div>
    </div>

    <!-- ==================== 鍔熻兘鎸夐挳妯″潡 ==================== -->
    <div class="func-buttons">
      <button
        v-for="btn in funcButtons"
        :key="btn.type"
        class="func-btn"
        @click="handleFunc(btn.type)"
      >
        <span class="func-btn-circle">
          <!-- 鑰冨嫟 -->
          <svg v-if="btn.icon === 'attendance'" viewBox="0 0 24 24" width="20" height="20" fill="none">
            <rect x="3" y="5" width="18" height="14" rx="2" stroke="currentColor" stroke-width="1.5"/>
            <path d="M3 10h18" stroke="currentColor" stroke-width="1.5"/>
            <line x1="8" y1="2" x2="8" y2="7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            <line x1="16" y1="2" x2="16" y2="7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            <circle cx="8.5" cy="13.5" r="1.2" fill="currentColor"/>
            <circle cx="13" cy="13.5" r="1.2" fill="currentColor"/>
            <circle cx="17.5" cy="13.5" r="1.2" fill="currentColor"/>
          </svg>
          <!-- 琛ㄧ幇 -->
          <svg v-else-if="btn.icon === 'performance'" viewBox="0 0 24 24" width="20" height="20" fill="none">
            <path d="M11 4H4C2.9 4 2 4.9 2 6v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8l-6-4h-5z" stroke="currentColor" stroke-width="1.5"/>
            <path d="M14 4v5h5" stroke="currentColor" stroke-width="1.5"/>
            <circle cx="8.5" cy="14" r="1.2" fill="currentColor"/>
            <circle cx="12" cy="17" r="1.2" fill="currentColor"/>
            <circle cx="16" cy="12" r="1.2" fill="currentColor"/>
            <line x1="8.5" y1="14" x2="12" y2="17" stroke="currentColor" stroke-width="1.2"/>
            <line x1="12" y1="17" x2="16" y2="12" stroke="currentColor" stroke-width="1.2"/>
          </svg>
          <!-- 鎴愮哗 -->
          <svg v-else-if="btn.icon === 'grades'" viewBox="0 0 24 24" width="20" height="20" fill="none">
            <rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="1.5"/>
            <line x1="8" y1="10" x2="16" y2="10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            <line x1="8" y1="14" x2="14" y2="14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            <line x1="8" y1="18" x2="12" y2="18" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            <circle cx="17" cy="11" r="2" stroke="currentColor" stroke-width="1.2" fill="none"/>
            <path d="M16 11l.7 1.4 1.5.2-1.1 1 .3 1.4-1.4-.7-1.4.7.3-1.4-1.1-1 1.5-.2z" fill="currentColor"/>
          </svg>
          <!-- 鍒嗘瀽 -->
          <svg v-else-if="btn.icon === 'analysis'" viewBox="0 0 24 24" width="20" height="20" fill="none">
            <path d="M3 3v16c0 1.1.9 2 2 2h15" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            <path d="M7 14l3-5 3 3 4-6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <!-- 閿欓闆?-->
          <svg v-else-if="btn.icon === 'errors'" viewBox="0 0 24 24" width="20" height="20" fill="none">
            <path d="M12 2L2 20h20L12 2z" stroke="currentColor" stroke-width="1.5"/>
            <line x1="12" y1="10" x2="12" y2="14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            <circle cx="12" cy="17.5" r="1" fill="currentColor"/>
            <line x1="9" y1="7" x2="15" y2="7" stroke="currentColor" stroke-width="1" stroke-linecap="round" opacity=".4"/>
            <line x1="8" y1="5" x2="15" y2="5" stroke="currentColor" stroke-width="1" stroke-linecap="round" opacity=".2"/>
          </svg>
          <!-- 璇剧▼鏁欏 -->
          <svg v-else-if="btn.icon === 'teaching'" viewBox="0 0 24 24" width="20" height="20" fill="none">
            <path d="M2 3h7c1.1 0 2 .9 2 2v14c0 1.1-.9 2-2 2H2V3z" stroke="currentColor" stroke-width="1.5"/>
            <path d="M10 3h12v18H10" stroke="currentColor" stroke-width="1.5"/>
            <line x1="6" y1="8" x2="8" y2="8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            <line x1="6" y1="12" x2="8" y2="12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            <line x1="14" y1="8" x2="18" y2="8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            <line x1="14" y1="12" x2="18" y2="12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            <line x1="14" y1="16" x2="16" y2="16" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <!-- 璇剧▼杈炬垚搴?-->
          <svg v-else-if="btn.icon === 'achievement'" viewBox="0 0 24 24" width="20" height="20" fill="none">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="1.5"/>
            <circle cx="12" cy="12" r="6" stroke="currentColor" stroke-width="1.5"/>
            <circle cx="12" cy="12" r="2" fill="currentColor"/>
            <path d="M12 2v4M12 18v4M2 12h4M18 12h4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </span>
        <span class="func-btn-label">{{ btn.label }}</span>
      </button>
    </div>

    <!-- ==================== Tab 鏍囩鏍?==================== -->
    <div class="tab-bar-wrapper">
      <div class="tab-bar" ref="tabBarRef">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          class="tab-item"
          :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key"
        >{{ tab.label }}</button>
      </div>
    </div>

    <!-- ==================== 鍐呭鍖哄煙 ==================== -->
    <div class="content-area">
      <!-- 鐩綍 Tab -->
      <div v-if="activeTab === 'catalog'" class="catalog-panel">
        <div class="catalog-header">
          <h3 class="catalog-title">璇剧▼鍐呭</h3>
          <span class="catalog-stats">0涓珷鑺?0涓椿鍔?/span>
        </div>
        <div class="catalog-empty">
          <svg class="catalog-empty-icon" viewBox="0 0 120 120" fill="none">
            <rect x="25" y="20" width="70" height="80" rx="6" stroke="#ddd" stroke-width="2" fill="#f9f9f9"/>
            <line x1="40" y1="42" x2="80" y2="42" stroke="#e0e0e0" stroke-width="2.5" stroke-linecap="round"/>
            <line x1="40" y1="54" x2="70" y2="54" stroke="#e8e8e8" stroke-width="2.5" stroke-linecap="round"/>
            <line x1="40" y1="66" x2="75" y2="66" stroke="#e8e8e8" stroke-width="2.5" stroke-linecap="round"/>
            <circle cx="60" cy="90" r="14" fill="#f0f0f0"/>
            <path d="M56 90h8M60 86v8" stroke="#ccc" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <p class="catalog-empty-text">璇ヨ绋嬫殏鏃犵珷鑺?/p>
        </div>
      </div>

      <!-- 浣滀笟 Tab -->
      <div v-else-if="activeTab === 'homework'" class="homework-panel">
        <div class="homework-header">
          <h3 class="homework-title">浣滀笟鍒楄〃</h3>
          <button v-if="isTeacher" class="add-hw-btn" @click="addHomework">+ 娣诲姞浣滀笟</button>
        </div>

        <div v-if="hwLoading" class="homework-empty">
          <p class="homework-empty-text">鍔犺浇涓?..</p>
        </div>
        <div v-else-if="homeworks.length === 0" class="homework-empty">
          <svg class="homework-empty-icon" viewBox="0 0 120 120" fill="none">
            <rect x="25" y="20" width="70" height="80" rx="6" stroke="#ddd" stroke-width="2" fill="#f9f9f9"/>
            <line x1="40" y1="42" x2="80" y2="42" stroke="#e0e0e0" stroke-width="2.5" stroke-linecap="round"/>
            <line x1="40" y1="54" x2="70" y2="54" stroke="#e8e8e8" stroke-width="2.5" stroke-linecap="round"/>
            <line x1="40" y1="66" x2="75" y2="66" stroke="#e8e8e8" stroke-width="2.5" stroke-linecap="round"/>
          </svg>
          <p class="homework-empty-text">鏆傛棤浣滀笟</p>
        </div>
        <div v-else class="homework-list">
          <div v-for="hw in homeworks" :key="hw.hwId" class="homework-card">
            <div class="hw-card-top">
              <h4 class="hw-title">{{ hw.title }}</h4>
              <span class="hw-deadline">{{ formatDate(hw.deadline) }}</span>
            </div>
            <div class="hw-card-bottom">
              <p class="hw-content">{{ hw.content }}</p>
              <button class="hw-file-btn" title="鏌ョ湅鏂囦欢" @click="toggleFiles(hw.hwId)">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none">
                  <path d="M13 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V9z" stroke="currentColor" stroke-width="1.5"/>
                  <polyline points="13 2 13 9 20 9" stroke="currentColor" stroke-width="1.5"/>
                </svg>
              </button>
            </div>
            <!-- 鏂囦欢鍒楄〃 -->
            <div v-if="expandedHwId === hw.hwId" class="hw-files">
              <div v-if="fileLoadingHwId === hw.hwId" class="hw-files-loading">鍔犺浇涓?..</div>
              <div v-else-if="!hwFiles[hw.hwId] || hwFiles[hw.hwId].length === 0" class="hw-files-empty">璇ヤ綔涓氭殏鏃犳枃浠?/div>
              <a v-else v-for="f in hwFiles[hw.hwId]" :key="f.fileId"
                 class="hw-file-link" :href="f.filePath" target="_blank" rel="noopener">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none">
                  <path d="M13 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V9z" stroke="currentColor" stroke-width="1.5"/>
                  <polyline points="13 2 13 9 20 9" stroke="currentColor" stroke-width="1.5"/>
                </svg>
                <span>{{ hwFileLabel(f) }}</span>
              </a>
            </div>
          </div>
        </div>
      </div>

      <!-- 鍏朵粬 Tab 鍗犱綅 -->
      <div v-else class="tab-placeholder">
        <svg class="tab-placeholder-icon" viewBox="0 0 100 100" fill="none">
          <circle cx="50" cy="45" r="22" stroke="#e0e0e0" stroke-width="2" fill="#fafafa"/>
          <path d="M42 45h16M50 37v16" stroke="#ccc" stroke-width="2.5" stroke-linecap="round"/>
        </svg>
        <p class="tab-placeholder-text">{{ currentTabLabel }}鍔熻兘寮€鍙戜腑锛屾帴鍙ｅ凡棰勭暀</p>
      </div>
    </div>

    <!-- 鍔犺浇 -->
    <div v-if="loading" class="page-loading">
      <p>鍔犺浇涓?..</p>
    </div>
    <div v-else-if="error" class="page-error">
      <p>{{ error }}</p>
      <button @click="loadDetail">閲嶈瘯</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { api } from '../api/request.js'

const props = defineProps({
  courseId: { type: [Number, String], required: true }
})
defineEmits(['back'])

const course = ref(null)
const loading = ref(true)
const error = ref('')
const activeTab = ref('catalog')

// ============ 浣滀笟 ============
const homeworks = ref([])
const hwLoading = ref(false)

async function loadHomeworks() {
  hwLoading.value = true
  try {
    const res = await api.getHomeworkList(props.courseId)
    homeworks.value = res.data?.records || []
  } catch (e) {
    console.error('鍔犺浇浣滀笟鍒楄〃澶辫触:', e)
  } finally {
    hwLoading.value = false
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '鏃犳埅姝㈡棩鏈?
  const d = new Date(dateStr)
  const month = d.getMonth() + 1
  const day = d.getDate()
  const hour = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${month}鏈?{day}鏃?${hour}:${min}`
}

// ============ 浣滀笟鏂囦欢 ============
const hwFiles = ref({})
const expandedHwId = ref(null)
const fileLoadingHwId = ref(null)

async function toggleFiles(hwId) {
  if (expandedHwId.value === hwId) {
    expandedHwId.value = null
    return
  }
  expandedHwId.value = hwId
  if (hwFiles.value[hwId]) return // 宸插姞杞借繃
  fileLoadingHwId.value = hwId
  try {
    const res = await api.getHomeworkFiles(hwId)
    hwFiles.value[hwId] = res.data || []
  } catch (e) {
    console.error('鍔犺浇浣滀笟鏂囦欢澶辫触:', e)
    hwFiles.value[hwId] = []
  } finally {
    fileLoadingHwId.value = null
  }
}

/** 浠庢枃浠惰矾寰勫拰绫诲瀷鐢熸垚灞曠ず鏍囩 */
function hwFileLabel(f) {
  const path = f.filePath || ''
  const type = f.fileType || ''
  const name = path.split('/').pop() || '鏂囦欢'
  return type ? `${decodeURIComponent(name)} (${type})` : decodeURIComponent(name)
}

// 鍒囨崲鍒颁綔涓?Tab 鏃跺姞杞芥暟鎹?watch(activeTab, (val) => {
  if (val === 'homework') loadHomeworks()
})

const tabs = [
  { key: 'catalog',     label: '鐩綍' },
  { key: 'courseware',  label: '浜掑姩璇句欢' },
  { key: 'homework',    label: '浣滀笟' },
  { key: 'test',        label: '娴嬭瘯' },
  { key: 'material',    label: '璧勬枡' },
  { key: 'meeting',     label: '鑵捐浼氳' },
  { key: 'notice',      label: '鍏憡' },
  { key: 'topic',       label: '璇濋' },
  { key: 'qa',          label: '浜掑姩绛旈' },
  { key: 'case',        label: '妗堜緥瀛︿範' }
]

const isTeacher = computed(() => course.value?.userRole === 'teacher')

const studentFuncButtons = [
  { type: 'attendance',  label: '鑰冨嫟',   icon: 'attendance' },
  { type: 'performance', label: '琛ㄧ幇',   icon: 'performance' },
  { type: 'grades',      label: '鎴愮哗',   icon: 'grades' },
  { type: 'analysis',    label: '瀛︿範鍒嗘瀽', icon: 'analysis' },
  { type: 'errors',      label: '閿欓闆?,  icon: 'errors' }
]
const teacherFuncButtons = [
  { type: 'teaching',    label: '璇剧▼鏁欏', icon: 'teaching' },
  { type: 'analysis',    label: '瀛︽儏鍒嗘瀽', icon: 'analysis' },
  { type: 'grades',      label: '鎴愮哗绠＄悊', icon: 'grades' },
  { type: 'achievement', label: '璇剧▼杈炬垚搴?, icon: 'achievement' }
]
const funcButtons = computed(() => isTeacher.value ? teacherFuncButtons : studentFuncButtons)

const currentTabLabel = computed(() => {
  const t = tabs.find(t => t.key === activeTab.value)
  return t ? t.label : ''
})

const coverColors = ['#667eea', '#764ba2', '#f093fb', '#4facfe', '#43e97b', '#fa709a', '#fee140', '#30cfd0']
function coverColor(id) {
  return coverColors[id % coverColors.length]
}
const bannerColor = computed(() => {
  const id = course.value?.courseId
  return id != null ? coverColor(id) : '#667eea'
})

const classText = computed(() => {
  const parts = []
  if (course.value?.semester) parts.push(course.value.semester)
  if (course.value?.courseIntro) parts.push(course.value.courseIntro)
  return parts.join(' 路 ') || '鐝骇淇℃伅'
})

// ============ 鍔熻兘鎸夐挳锛堥鐣欐帴鍙ｏ級 ============
function handleFunc(type) {
  const map = {
    attendance:  { api: '/api/course/{id}/attendance', label: '鑰冨嫟' },
    performance: { api: '/api/course/{id}/performance', label: '琛ㄧ幇' },
    grades:      { api: '/api/course/{id}/grades', label: '鎴愮哗' },
    analysis:    { api: '/api/course/{id}/analysis', label: '鍒嗘瀽' },
    errors:      { api: '/api/course/{id}/errors', label: '閿欓闆? },
    teaching:    { api: '/api/course/{id}/teaching', label: '璇剧▼鏁欏' },
    achievement: { api: '/api/course/{id}/achievement', label: '璇剧▼杈炬垚搴? }
  }
  const info = map[type]
  console.log(`[棰勭暀鎺ュ彛] ${info.label}: ${info.api.replace('{id}', props.courseId)}`)
}

function addHomework() {
  // TODO: 寮瑰嚭鍒涘缓浣滀笟琛ㄥ崟
  console.log(`[棰勭暀鎺ュ彛] 鍒涘缓浣滀笟: POST /api/homework/course/${props.courseId}`)
}

function copyCode() {
  if (!course.value?.courseCode) return
  navigator.clipboard.writeText(course.value.courseCode).then(() => {
    alert('鍔犺鐮佸凡澶嶅埗')
  }).catch(() => {
    const input = document.createElement('input')
    input.value = course.value.courseCode
    document.body.appendChild(input)
    input.select()
    document.execCommand('copy')
    document.body.removeChild(input)
    alert('鍔犺鐮佸凡澶嶅埗')
  })
}

async function loadDetail() {
  loading.value = true
  error.value = ''
  try {
    const res = await api.getCourseDetail(props.courseId)
    course.value = res.data
  } catch (e) {
    error.value = e.message || '鍔犺浇澶辫触'
  } finally {
    loading.value = false
  }
}

onMounted(() => { loadDetail() })
</script>

<style scoped>
.course-detail-page {
  width: 100%;
  max-width: 100%;
  min-height: 100vh;
  background: #fff;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* ==================== Banner ==================== */
.banner {
  position: relative;
  padding: 16px 16px 20px;
  min-height: 180px;
  overflow: hidden;
}

.banner-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}
.banner-leaf {
  position: absolute;
  opacity: .4;
}
.leaf-1 { right: -10px; top: -30px; width: 160px; height: 200px; transform: rotate(15deg); }
.leaf-2 { right: 30px; top: -20px; width: 130px; height: 170px; transform: rotate(-20deg); }

.banner-back {
  position: absolute;
  top: 12px;
  left: 12px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 50%;
  background: rgba(255,255,255,.15);
  cursor: pointer;
  z-index: 2;
  transition: background .15s;
}
.banner-back:hover { background: rgba(255,255,255,.25); }

.banner-main {
  position: relative;
  z-index: 1;
  padding-top: 8px;
}
.banner-class {
  font-size: 13px;
  color: rgba(255,255,255,.75);
  margin-bottom: 6px;
  line-height: 1.4;
}
.banner-title {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  line-height: 1.3;
  margin-bottom: 12px;
  letter-spacing: .5px;
}
.banner-code-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.banner-code {
  font-size: 13px;
  color: rgba(255,255,255,.8);
  letter-spacing: 1px;
}
.banner-code-copy {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border: 1px solid rgba(255,255,255,.3);
  border-radius: 4px;
  background: rgba(255,255,255,.1);
  color: #fff;
  font-size: 11px;
  cursor: pointer;
  transition: background .15s;
}
.banner-code-copy:hover { background: rgba(255,255,255,.2); }

.banner-stats {
  position: relative;
  z-index: 1;
  display: flex;
  gap: 20px;
  margin-top: 18px;
}
.stat-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: rgba(255,255,255,.7);
}

/* ==================== 鍔熻兘鎸夐挳 ==================== */
.func-buttons {
  display: flex;
  justify-content: space-around;
  padding: 16px 12px;
  background: #fff;
}
.func-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  border: none;
  background: none;
  cursor: pointer;
  padding: 4px;
  transition: transform .15s;
}
.func-btn:active { transform: scale(.95); }
.func-btn-circle {
  width: 46px;
  height: 46px;
  border-radius: 50%;
  background: #2377E4;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  transition: background .15s;
}
.func-btn:hover .func-btn-circle { background: #1a5fc4; }
.func-btn-label {
  font-size: 11px;
  color: #555;
  white-space: nowrap;
}

/* ==================== Tab 鏍?==================== */
.tab-bar-wrapper {
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}
.tab-bar {
  display: flex;
  overflow-x: auto;
  padding: 0 4px;
  gap: 0;
  -webkit-overflow-scrolling: touch;
}
.tab-bar::-webkit-scrollbar { display: none; }
.tab-item {
  flex-shrink: 0;
  padding: 12px 14px;
  border: none;
  background: none;
  font-size: 13px;
  color: #777;
  cursor: pointer;
  position: relative;
  white-space: nowrap;
  transition: color .15s;
}
.tab-item.active {
  color: #2377E4;
  font-weight: 600;
}
.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 24px;
  height: 3px;
  border-radius: 2px;
  background: #2377E4;
}

/* ==================== 鍐呭鍖哄煙 ==================== */
.content-area {
  background: #fff;
  min-height: 400px;
}

/* 鐩綍闈㈡澘 */
.catalog-panel {
  padding: 16px;
}
.catalog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 40px;
}
.catalog-title {
  font-size: 16px;
  font-weight: 600;
  color: #111;
}
.catalog-stats {
  font-size: 12px;
  color: #999;
}
.catalog-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 0 60px;
}
.catalog-empty-icon {
  width: 120px;
  height: 120px;
  margin-bottom: 16px;
}
.catalog-empty-text {
  font-size: 14px;
  color: #aaa;
}

/* 浣滀笟闈㈡澘 */
.homework-panel {
  padding: 16px;
}
.homework-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.homework-title {
  font-size: 16px;
  font-weight: 600;
  color: #111;
}
.add-hw-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: 34px;
  padding: 0 16px;
  border: none;
  border-radius: 8px;
  background: #2377E4;
  color: #fff;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity .15s;
}
.add-hw-btn:hover { opacity: .9; }
.add-hw-btn:active { transform: scale(.97); }

/* 浣滀笟鍒楄〃 */
.homework-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.homework-card {
  background: #f9f9fb;
  border-radius: 10px;
  padding: 14px 16px;
}
.hw-card-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 8px;
}
.hw-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
  flex: 1;
}
.hw-deadline {
  font-size: 11px;
  color: #e74c3c;
  white-space: nowrap;
  padding: 2px 8px;
  background: #fef2f2;
  border-radius: 4px;
}
.hw-card-bottom {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}
.hw-content {
  flex: 1;
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 鏂囦欢鎸夐挳 */
.hw-file-btn {
  flex-shrink: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 6px;
  background: #eef0f5;
  color: #888;
  cursor: pointer;
  transition: background .15s, color .15s;
}
.hw-file-btn:hover { background: #2377E4; color: #fff; }

/* 灞曞紑鐨勬枃浠跺垪琛?*/
.hw-files {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #eee;
}
.hw-files-loading,
.hw-files-empty {
  font-size: 12px;
  color: #bbb;
}
.hw-file-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 10px;
  border-radius: 6px;
  background: #fff;
  font-size: 13px;
  color: #2377E4;
  text-decoration: none;
  transition: background .15s;
}
.hw-file-link + .hw-file-link { margin-top: 4px; }
.hw-file-link:hover { background: #eef2ff; }

/* 浣滀笟绌虹姸鎬?*/
.homework-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 0;
}
.homework-empty-icon {
  width: 100px;
  height: 100px;
  margin-bottom: 12px;
}
.homework-empty-text {
  font-size: 13px;
  color: #bbb;
}

/* 鍗犱綅 Tab */
.tab-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 0;
}
.tab-placeholder-icon {
  width: 80px;
  height: 80px;
  margin-bottom: 14px;
}
.tab-placeholder-text {
  font-size: 13px;
  color: #bbb;
}

/* 鍔犺浇/閿欒 */
.page-loading, .page-error {
  text-align: center;
  padding: 80px 0;
  color: #999;
  font-size: 14px;
}
.page-error button {
  margin-top: 10px;
  padding: 8px 20px;
  border: none;
  border-radius: 6px;
  background: #2377E4;
  color: #fff;
  font-size: 13px;
  cursor: pointer;
}
</style>
