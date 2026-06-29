<template>
  <div class="main-layout">
    <!-- 上部内容区 -->
    <div class="content">
      <Profile v-if="activeTab === 'profile'" @logout="$emit('logout')" />
      <Todo v-else-if="activeTab === 'todo'" />
      <Classroom v-else-if="activeTab === 'classroom'" />
      <Messages v-else-if="activeTab === 'messages'" @markRead="unreadMessageCount = 0" />
    </div>

    <!-- 底部 Dock 栏 -->
    <nav class="dock">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        class="dock-item"
        :class="{ active: activeTab === tab.key }"
        @click="activeTab = tab.key"
      >
        <span class="dock-icon">{{ tab.icon }}</span>
        <span class="dock-label">{{ tab.label }}</span>
        <span v-if="tab.key === 'messages' && unreadMessageCount > 0" class="dock-badge">{{ unreadMessageCount > 99 ? '99+' : unreadMessageCount }}</span>
      </button>
    </nav>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import Profile from './Profile.vue'
import Todo from './Todo.vue'
import Classroom from './Classroom.vue'
import Messages from './Messages.vue'
import { api } from '../api/request.js'

defineEmits(['logout'])

const activeTab = ref(sessionStorage.getItem('mainTab') || 'classroom')
const unreadMessageCount = ref(0)

const tabs = [
  { key: 'todo',      label: '待办', icon: '📋' },
  { key: 'classroom', label: '课堂', icon: '📚' },
  { key: 'messages',  label: '私信', icon: '💬' },
  { key: 'profile',   label: '我的', icon: '👤' }
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

.content {
  flex: 1;
  overflow-y: auto;
}

/* ========== Dock 栏 ========== */
.dock {
  display: flex;
  background: #fff;
  border-top: 1px solid #eee;
  padding: 6px 0 env(safe-area-inset-bottom, 6px);
  flex-shrink: 0;
}

.dock-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 8px 0;
  border: none;
  background: none;
  cursor: pointer;
  color: #999;
  transition: color .15s;
  position: relative;
}

.dock-item.active {
  color: #4a6cf7;
}

.dock-icon {
  font-size: 22px;
  line-height: 1;
}

.dock-label {
  font-size: 11px;
}

.dock-badge {
  position: absolute;
  top: 2px;
  right: 50%;
  transform: translateX(22px);
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background: #e74c3c;
  color: #fff;
  font-size: 10px;
  font-weight: 600;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}
</style>
