<template>
  <div class="main-layout">
    <!-- 上部内容区 -->
    <div class="content">
      <Profile v-if="activeTab === 'profile'" @logout="$emit('logout')" />
      <Todo v-else-if="activeTab === 'todo'" />
      <Classroom v-else-if="activeTab === 'classroom'" />
      <Messages v-else-if="activeTab === 'messages'" />
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
      </button>
    </nav>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import Profile from './Profile.vue'
import Todo from './Todo.vue'
import Classroom from './Classroom.vue'
import Messages from './Messages.vue'

defineEmits(['logout'])

const activeTab = ref('classroom')

const tabs = [
  { key: 'todo',      label: '待办', icon: '📋' },
  { key: 'classroom', label: '课堂', icon: '📚' },
  { key: 'messages',  label: '私信', icon: '💬' },
  { key: 'profile',   label: '我的', icon: '👤' }
]
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
</style>
