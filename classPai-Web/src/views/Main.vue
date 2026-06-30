<template>
  <div class="main-layout">
    <header class="top-nav">
      <div class="nav-inner">
        <div class="nav-left">
          <img class="brand-logo" src="/logo.png" alt="ClassPai" />
          <button
            class="classroom-link"
            :class="{ active: activeTab === 'classroom' }"
            @click="activeTab = 'classroom'"
          >
            我的课堂
          </button>
        </div>

        <div class="nav-actions">
          <button
            v-for="tab in rightTabs"
            :key="tab.key"
            class="nav-icon-btn"
            :class="{ active: activeTab === tab.key }"
            :title="tab.label"
            :aria-label="tab.label"
            @click="activeTab = tab.key"
          >
            <svg v-if="tab.key === 'messages'" viewBox="0 0 24 24" width="22" height="22" aria-hidden="true">
              <path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M10 21h4" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            <svg v-else viewBox="0 0 24 24" width="22" height="22" aria-hidden="true">
              <circle cx="12" cy="8" r="4" fill="none" stroke="currentColor" stroke-width="2"/>
              <path d="M4 21a8 8 0 0 1 16 0" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            <span v-if="tab.key === 'messages' && unreadMessageCount > 0" class="nav-badge">{{ unreadMessageCount > 99 ? '99+' : unreadMessageCount }}</span>
          </button>
        </div>
      </div>
    </header>

    <main class="content">
      <Profile v-if="activeTab === 'profile'" @logout="$emit('logout')" />
      <Classroom v-else-if="activeTab === 'classroom'" />
      <Messages v-else-if="activeTab === 'messages'" @markRead="unreadMessageCount = 0" />
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import Profile from './Profile.vue'
import Classroom from './Classroom.vue'
import Messages from './Messages.vue'
import { api } from '../api/request.js'

defineEmits(['logout'])

const validTabs = ['classroom', 'messages', 'profile']
const savedTab = sessionStorage.getItem('mainTab')
const activeTab = ref(validTabs.includes(savedTab) ? savedTab : 'classroom')
const unreadMessageCount = ref(0)

const rightTabs = [
  { key: 'messages', label: '私信' },
  { key: 'profile',  label: '我的' }
]

async function fetchUnreadCount() {
  try {
    const res = await api.getUnreadMessageCount()
    unreadMessageCount.value = res.data || 0
  } catch (e) {
    console.error('获取未读消息数失败:', e)
  }
}

onMounted(fetchUnreadCount)

// 每次切回主页（非私信tab）时刷新未读数
watch(activeTab, (val) => {
  sessionStorage.setItem('mainTab', val)
  if (val !== 'messages') fetchUnreadCount()
})
</script>

<style scoped>
.main-layout {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f6fa;
}

.top-nav {
  flex-shrink: 0;
  height: 70px;
  background: #fff;
  border-bottom: 1px solid #e9edf5;
  box-shadow: 0 1px 8px rgba(15, 23, 42, .04);
}

.nav-inner {
  width: 100%;
  height: 100%;
  padding: 0 36px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.nav-left {
  display: inline-flex;
  align-items: center;
  gap: 18px;
  min-width: 0;
}

.brand-logo {
  width: 200px;
  height:150px;
  object-fit: contain;
  flex-shrink: 0;
}

.classroom-link {
  height: 42px;
  padding: 0 14px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #2377e4;
  font-size: 18px;
  font-weight: 700;
  white-space: nowrap;
  cursor: pointer;
  transition: background .15s;
}

.classroom-link:hover,
.classroom-link.active {
  background: #eef4ff;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.nav-icon-btn {
  position: relative;
  width: 42px;
  height: 42px;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: #64748b;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background .15s, color .15s;
}

.nav-icon-btn:hover,
.nav-icon-btn.active {
  background: #eef4ff;
  color: #2377e4;
}

.nav-badge {
  position: absolute;
  top: 1px;
  right: -1px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background: #e74c3c;
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

.content {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}

@media (max-width: 480px) {
  .top-nav {
    height: 64px;
  }

  .nav-inner {
    padding: 0 12px;
    gap: 8px;
  }

  .nav-left {
    gap: 8px;
  }

  .brand-logo {
    width: 46px;
    height: 46px;
  }

  .classroom-link {
    height: 36px;
    padding: 0 8px;
    font-size: 16px;
  }

  .nav-actions {
    gap: 4px;
  }

  .nav-icon-btn {
    width: 38px;
    height: 38px;
  }
}
</style>
