<template>
  <div class="msg-page">
    <div class="msg-header">
      <button class="msg-back-btn" title="返回" @click="$emit('back')">
        <svg viewBox="0 0 24 24" width="20" height="20">
          <path d="M15 18l-6-6 6-6" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </button>
      <h2>私信</h2>
    </div>

    <div v-if="loading" class="msg-status">加载中...</div>
    <div v-else-if="error" class="msg-status msg-error">{{ error }}</div>
    <div v-else-if="messages.length === 0" class="msg-empty">
      <svg viewBox="0 0 120 120" width="80" height="80" fill="none">
        <rect x="25" y="30" width="70" height="60" rx="8" stroke="#ddd" stroke-width="2" fill="#f9f9f9"/>
        <path d="M30 40h60M30 52h50M30 64h40" stroke="#e0e0e0" stroke-width="2" stroke-linecap="round"/>
      </svg>
      <p>暂无消息</p>
    </div>
    <div v-else class="msg-list">
      <div
        v-for="msg in messages"
        :key="msg.msgId"
        class="msg-item"
        :class="{ unread: !msg.isRead, sent: isSent(msg) }"
      >
        <div class="msg-avatar" :class="{ sent: isSent(msg) }">{{ isSent(msg) ? '我' : (msg.senderName || '').charAt(0) }}</div>
        <div class="msg-body">
          <div class="msg-meta">
          <span class="msg-sender">{{ isSent(msg) ? '发给 ' + msg.receiverName : msg.senderName }}</span>
            <span class="msg-time">{{ fmtTime(msg.createTime) }}</span>
          </div>
          <p class="msg-content">{{ msg.content }}</p>
        </div>
        <span v-if="!msg.isRead && !isSent(msg)" class="msg-dot"></span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { api } from '../api/request.js'
import { getCurrentUserId } from '../utils/format.js'

const loading = ref(true)
const error = ref('')
const messages = ref([])
const currentUserId = ref(null)

const emit = defineEmits(['markRead', 'back'])

function isSent(msg) {
  return msg.senderId === currentUserId.value
}

function fmtTime(t) {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return (d.getMonth() + 1) + '/' + d.getDate() + ' ' +
    String(d.getHours()).padStart(2, '0') + ':' + String(d.getMinutes()).padStart(2, '0')
}

async function loadMessages() {
  loading.value = true
  error.value = ''
  try {
    const res = await api.getMessages()
    messages.value = res.data || []
  } catch (e) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  currentUserId.value = await getCurrentUserId(api)
  await loadMessages()
  // 标记已读
  try {
    await api.markMessagesRead()
    emit('markRead')
  } catch (_) {}
})
</script>

<style scoped>
.msg-page {
  min-height: 100vh;
  background: #f5f6fa;
}

.msg-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 20px;
  background: #fff;
  border-bottom: 1px solid #edf0f5;
}
.msg-back-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #555;
  cursor: pointer;
  transition: background .15s;
}
.msg-back-btn:hover {
  background: #f0f0f0;
}
.msg-header h2 {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
}

.msg-status {
  text-align: center;
  padding: 80px 0;
  color: #999;
  font-size: 14px;
}
.msg-error { color: #e74c3c; }

.msg-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 0;
  color: #bbb;
  font-size: 14px;
  gap: 12px;
}

.msg-list {
  padding: 0;
}

.msg-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px 20px;
  background: #fff;
  border-bottom: 1px solid #f0f0f5;
  position: relative;
}
.msg-item.unread {
  background: #f8faff;
}

.msg-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #e8f0fe;
  color: #2377E4;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.msg-body {
  flex: 1;
  min-width: 0;
}

.msg-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.msg-sender {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
}

.msg-time {
  font-size: 12px;
  color: #bbb;
}

.msg-content {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  word-break: break-all;
}

.msg-dot {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #e74c3c;
}

/* 已发送的消息 */
.msg-item.sent {
  flex-direction: row-reverse;
  text-align: right;
  background: #f0fdf4;
}
.msg-item.sent.unread {
  background: #f0fdf4;
}
.msg-avatar.sent {
  background: #dcfce7;
  color: #16a34a;
}
.msg-item.sent .msg-sender {
  color: #16a34a;
}
.msg-item.sent .msg-content {
  color: #166534;
}
</style>
