<template>
  <div class="discussion">
    <!-- 教师控制栏 -->
    <div v-if="isTeacher" class="disc-ctrl">
      <button class="disc-ctrl-btn" @click="handleToggleDiscussion">
        {{ discussionLocked ? '开放讨论' : '禁止讨论' }}
      </button>
    </div>

    <!-- 发帖区 -->
    <div v-if="!showReplies && (isTeacher || !discussionLocked)" class="disc-create">
      <input v-model="newTitle" class="disc-input" placeholder="标题..."
        maxlength="200" @keyup.enter="handleCreate" />
      <textarea v-model="newContent" class="disc-textarea" placeholder="内容（可选）..."
        rows="2"></textarea>
      <button class="disc-btn" @click="handleCreate" :disabled="!newTitle.trim()">发布</button>
    </div>
    <div v-else-if="showReplies" class="disc-back">
      <button class="disc-back-btn" @click="showReplies = false; replies = []">&larr; 返回话题列表</button>
    </div>
    <div v-if="!isTeacher && discussionLocked && !showReplies" class="disc-locked">
      教师已禁止学生讨论
    </div>

    <!-- 话题列表 -->
    <div v-if="!showReplies" class="disc-list">
      <div v-if="loading">加载中...</div>
      <div v-else-if="topics.length === 0" class="disc-empty">暂无话题，快来发帖吧</div>
      <div v-for="t in topics" :key="t.topicId" class="disc-item" :class="{ pinned: t.isPinned }">
        <div class="disc-item-header">
          <span v-if="t.isPinned" class="disc-pin-badge">置顶</span>
          <span class="disc-title" @click="openReplies(t)">{{ t.title }}</span>
          <span class="disc-meta">
            <span class="disc-author">{{ t.userName || '未知' }}</span>
            <span class="disc-time">{{ fmtShort(t.createTime) }}</span>
            <span class="disc-reply-count">{{ t.replyCount || 0 }}回复</span>
          </span>
          <div class="disc-item-actions">
            <button v-if="isTeacher" class="disc-action-btn" @click.stop="handleTogglePin(t)">
              {{ t.isPinned ? '取消置顶' : '置顶' }}
            </button>
            <button v-if="canEdit(t)" class="disc-action-btn" @click.stop="startEdit(t)">编辑</button>
            <button v-if="canDelete(t)" class="disc-action-btn danger" @click.stop="handleDelete(t)">删除</button>
          </div>
        </div>
        <div v-if="t.content" class="disc-item-body">{{ t.content }}</div>
        <!-- 编辑中 -->
        <div v-if="editingId === t.topicId" class="disc-edit-area">
          <input v-model="editTitle" class="disc-input" maxlength="200" />
          <textarea v-model="editContent" class="disc-textarea" rows="2"></textarea>
          <div class="disc-edit-btns">
            <button class="disc-btn-sm" @click="saveEdit(t)">保存</button>
            <button class="disc-btn-sm cancel" @click="editingId = null">取消</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 回复列表 -->
    <div v-else class="disc-replies">
      <div class="disc-reply-item" v-for="r in replies" :key="r.replyId">
        <div class="disc-reply-header">
          <span class="disc-reply-author">{{ r.userName || '匿名' }}</span>
          <span class="disc-reply-time">{{ fmtShort(r.createTime) }}</span>
        </div>
        <div class="disc-reply-content">{{ r.content }}</div>
      </div>
      <div v-if="replies.length === 0" class="disc-empty">暂无回复</div>
      <!-- 回复输入区 -->
      <div v-if="isTeacher || !discussionLocked" class="disc-reply-create">
        <textarea v-model="replyContent" class="disc-textarea" placeholder="写回复..."
          rows="2"></textarea>
        <div class="disc-reply-bar">
          <label class="disc-anon-label">
            <input type="checkbox" v-model="replyAnonymous" /> 匿名回复
          </label>
          <button class="disc-btn" @click="handleReply" :disabled="!replyContent.trim()">回复</button>
        </div>
      </div>
      <div v-else class="disc-locked">教师已禁止学生讨论</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { api } from '../api/request.js'
import { fmtShort, getCurrentUserId } from '../utils/format.js'

const props = defineProps({
  courseId: { type: [Number, String], required: true },
  isTeacher: { type: Boolean, default: false },
  discussionLocked: { type: Boolean, default: false }
})

const emit = defineEmits(['discussion-toggled'])

const currentUserId = ref(null)

// 话题列表
const topics = ref([])
const loading = ref(true)

// 发帖
const newTitle = ref('')
const newContent = ref('')

// 编辑
const editingId = ref(null)
const editTitle = ref('')
const editContent = ref('')

// 回复
const showReplies = ref(false)
const currentTopic = ref(null)
const replies = ref([])
const replyContent = ref('')
const replyAnonymous = ref(false)

async function loadTopics() {
  loading.value = true
  try {
    const res = await api.getTopics(props.courseId)
    const data = res.data || []
    // 从话题数据中提取 discussionLocked（偷懒取第一个话题对应的课程状态）
    topics.value = data
  } catch (e) {
    console.error('加载话题失败', e)
  } finally {
    loading.value = false
  }
}

function canEdit(t) {
  if (props.isTeacher) return true
  return currentUserId.value && t.userId === currentUserId.value
}
function canDelete(t) { return canEdit(t) }

async function handleCreate() {
  if (!newTitle.value.trim()) return
  try {
    await api.createTopic(props.courseId, newTitle.value.trim(), newContent.value.trim())
    newTitle.value = ''
    newContent.value = ''
    loadTopics()
  } catch (e) {
    alert(e.message || '发帖失败')
  }
}

async function handleDelete(t) {
  if (!confirm('确定删除此话题及所有回复？')) return
  try {
    await api.deleteTopic(t.topicId)
    loadTopics()
  } catch (e) {
    alert(e.message || '删除失败')
  }
}

async function handleTogglePin(t) {
  try {
    await api.togglePinTopic(t.topicId)
    loadTopics()
  } catch (e) {
    alert(e.message || '操作失败')
  }
}

async function handleToggleDiscussion() {
  try {
    await api.toggleDiscussion(props.courseId)
    emit('discussion-toggled')
  } catch (e) {
    alert(e.message || '操作失败')
  }
}

function startEdit(t) {
  editingId.value = t.topicId
  editTitle.value = t.title
  editContent.value = t.content || ''
}

async function saveEdit(t) {
  try {
    await api.updateTopic(t.topicId, editTitle.value.trim(), editContent.value.trim())
    t.title = editTitle.value.trim()
    t.content = editContent.value.trim()
    editingId.value = null
  } catch (e) {
    alert(e.message || '编辑失败')
  }
}

async function openReplies(t) {
  currentTopic.value = t
  showReplies.value = true
  try {
    const res = await api.getReplies(t.topicId)
    replies.value = res.data || []
  } catch (e) {
    console.error('加载回复失败', e)
  }
}

async function handleReply() {
  if (!replyContent.value.trim()) return
  try {
    await api.replyTopic(currentTopic.value.topicId, replyContent.value.trim(), replyAnonymous.value)
    replyContent.value = ''
    replyAnonymous.value = false
    openReplies(currentTopic.value)
  } catch (e) {
    alert(e.message || '回复失败')
  }
}

onMounted(async () => {
  currentUserId.value = await getCurrentUserId(api)
  loadTopics()
})
</script>

<style scoped>
.discussion { padding: 0; }

.disc-ctrl { display: flex; justify-content: flex-end; margin-bottom: 12px; }
.disc-ctrl-btn {
  font-size: 12px; padding: 4px 12px; border: 1px solid #e74c3c; background: #fff;
  color: #e74c3c; border-radius: 4px; cursor: pointer;
}

.disc-create { display: flex; flex-direction: column; gap: 8px; margin-bottom: 16px; }
.disc-input, .disc-textarea {
  width: 100%; padding: 8px 12px; border: 1px solid var(--border, #e5e7eb);
  border-radius: 6px; font-size: 14px; outline: none; resize: vertical; box-sizing: border-box;
}
.disc-input:focus, .disc-textarea:focus { border-color: var(--primary, #4a6cf7); }
.disc-btn {
  align-self: flex-end; padding: 6px 20px;
  background: var(--primary, #4a6cf7); color: #fff; border: none;
  border-radius: 6px; font-size: 13px; cursor: pointer;
}
.disc-btn:disabled { opacity: .5; cursor: not-allowed; }

.disc-back { margin-bottom: 12px; }
.disc-back-btn { font-size: 13px; color: var(--primary, #4a6cf7); background: none; border: none; cursor: pointer; }

.disc-locked { text-align: center; padding: 24px; color: #9ca3af; font-size: 13px; }

.disc-list { display: flex; flex-direction: column; gap: 10px; }
.disc-empty { text-align: center; padding: 32px; color: #9ca3af; font-size: 13px; }
.disc-item {
  border: 1px solid var(--border, #e5e7eb); border-radius: 8px; padding: 12px 14px;
}
.disc-item.pinned { border-color: #f59e0b; background: #fffbeb; }
.disc-item-header { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.disc-pin-badge { font-size: 10px; background: #f59e0b; color: #fff; padding: 1px 6px; border-radius: 3px; }
.disc-title { font-weight: 600; font-size: 14px; cursor: pointer; color: var(--text, #1f2937); flex: 1; min-width: 0; }
.disc-title:hover { color: var(--primary, #4a6cf7); }
.disc-meta { display: flex; gap: 10px; font-size: 11px; color: #9ca3af; }
.disc-item-actions { display: flex; gap: 4px; }
.disc-action-btn {
  font-size: 11px; padding: 2px 8px; border: 1px solid #d1d5db; background: #fff;
  color: #6b7280; border-radius: 3px; cursor: pointer;
}
.disc-action-btn.danger { color: #ef4444; border-color: #fca5a5; }
.disc-item-body { margin-top: 8px; font-size: 13px; color: #4b5563; white-space: pre-wrap; word-break: break-all; }
.disc-edit-area { margin-top: 8px; display: flex; flex-direction: column; gap: 6px; }
.disc-edit-btns { display: flex; gap: 6px; }
.disc-btn-sm {
  padding: 4px 12px; border-radius: 4px; font-size: 12px; border: 1px solid var(--primary, #4a6cf7);
  background: var(--primary, #4a6cf7); color: #fff; cursor: pointer;
}
.disc-btn-sm.cancel { background: #fff; color: #6b7280; border-color: #d1d5db; }

.disc-replies { display: flex; flex-direction: column; gap: 10px; }
.disc-reply-item {
  padding: 10px 12px; border: 1px solid var(--border, #e5e7eb); border-radius: 6px;
  border-left: 3px solid var(--primary, #4a6cf7);
}
.disc-reply-header { display: flex; gap: 8px; font-size: 12px; color: #6b7280; margin-bottom: 4px; }
.disc-reply-author { font-weight: 500; }
.disc-reply-content { font-size: 13px; color: #374151; white-space: pre-wrap; word-break: break-all; }
.disc-reply-create { margin-top: 12px; display: flex; flex-direction: column; gap: 8px; }
.disc-reply-bar { display: flex; align-items: center; justify-content: space-between; }
.disc-anon-label { font-size: 12px; color: #6b7280; display: flex; align-items: center; gap: 4px; cursor: pointer; }
</style>
