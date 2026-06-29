<template>
  <div class="material-panel">
    <!-- 面包屑导航 -->
    <div class="mat-breadcrumb">
      <span class="mat-bread-item" @click="navigateTo(null)">根目录</span>
      <template v-for="(b, i) in breadcrumbs" :key="b.materialId">
        <span class="mat-bread-sep">/</span>
        <span class="mat-bread-item" @click="navigateTo(b.materialId, i)">{{ b.name }}</span>
      </template>
    </div>

    <!-- 教师工具栏 -->
    <div v-if="isTeacher" class="mat-toolbar">
      <button class="mat-tb-btn" @click="showAddFolder = true">+ 新建文件夹</button>
      <label class="mat-tb-btn">
        + 上传文件
        <input type="file" multiple hidden @change="onFileChange" />
      </label>
      <button class="mat-tb-btn" @click="showAddLink = true">+ 添加外链</button>
    </div>

    <!-- 新建文件夹弹窗 -->
    <div v-if="showAddFolder" class="mat-overlay" @click.self="showAddFolder = false">
      <div class="mat-dialog">
        <h4>新建文件夹</h4>
        <input v-model="folderName" class="mat-input" placeholder="文件夹名称" @keyup.enter="createFolder" />
        <div class="mat-dialog-btns">
          <button class="mat-d-btn" @click="createFolder" :disabled="!folderName.trim()">确定</button>
          <button class="mat-d-btn cancel" @click="showAddFolder = false">取消</button>
        </div>
      </div>
    </div>

    <!-- 添加外链弹窗 -->
    <div v-if="showAddLink" class="mat-overlay" @click.self="showAddLink = false">
      <div class="mat-dialog">
        <h4>添加外链资源</h4>
        <input v-model="linkName" class="mat-input" placeholder="资源名称" />
        <input v-model="linkUrl" class="mat-input" placeholder="链接地址（https://...）" style="margin-top:8px" />
        <div class="mat-dialog-btns">
          <button class="mat-d-btn" @click="addLink" :disabled="!linkName.trim() || !linkUrl.trim()">确定</button>
          <button class="mat-d-btn cancel" @click="showAddLink = false">取消</button>
        </div>
      </div>
    </div>

    <!-- 重命名弹窗 -->
    <div v-if="showRename" class="mat-overlay" @click.self="showRename = false">
      <div class="mat-dialog">
        <h4>重命名</h4>
        <input v-model="renameName" class="mat-input" placeholder="新名称" @keyup.enter="doRename" />
        <div class="mat-dialog-btns">
          <button class="mat-d-btn" @click="doRename" :disabled="!renameName.trim()">确定</button>
          <button class="mat-d-btn cancel" @click="showRename = false">取消</button>
        </div>
      </div>
    </div>

    <!-- 移动到弹窗 -->
    <div v-if="showMove" class="mat-overlay" @click.self="showMove = false">
      <div class="mat-dialog">
        <h4>移动到</h4>
        <div class="mat-move-list">
          <div class="mat-move-item" @click="doMove(null)">根目录</div>
          <div v-for="f in allFolders" :key="f.materialId" class="mat-move-item" @click="doMove(f.materialId)">
            {{ f.name }}
          </div>
          <div v-if="allFolders.length === 0" style="color:#999;padding:8px">暂无可用文件夹</div>
        </div>
        <div class="mat-dialog-btns">
          <button class="mat-d-btn cancel" @click="showMove = false">取消</button>
        </div>
      </div>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="mat-empty">加载中...</div>

    <!-- 资料列表 -->
    <div v-else-if="items.length === 0" class="mat-empty">
      <p>此目录为空</p>
    </div>
    <div v-else class="mat-list">
      <div
        v-for="item in items" :key="item.materialId"
        class="mat-item"
        :class="{ 'mat-folder': item.type === 'folder' }"
      >
        <!-- 文件夹图标 -->
        <svg v-if="item.type === 'folder'" class="mat-icon" viewBox="0 0 24 24" width="20" height="20" fill="none">
          <path d="M2 6c0-1.1.9-2 2-2h5l2 2h9c1.1 0 2 .9 2 2v10c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6z" fill="#f59e0b" stroke="#d97706" stroke-width="1.2"/>
        </svg>
        <!-- 文件图标 -->
        <svg v-else-if="item.type === 'file'" class="mat-icon" viewBox="0 0 24 24" width="20" height="20" fill="none">
          <path d="M13 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V9z" fill="#e0e7ff" stroke="#6366f1" stroke-width="1.2"/>
          <polyline points="13 2 13 9 20 9" stroke="#6366f1" stroke-width="1.2"/>
        </svg>
        <!-- 链接图标 -->
        <svg v-else-if="item.type === 'link'" class="mat-icon" viewBox="0 0 24 24" width="20" height="20" fill="none">
          <path d="M10 13a5 5 0 007.5.5l2-2a5 5 0 00-7-7l-1.5 1.5" stroke="#0891b2" stroke-width="1.5" stroke-linecap="round"/>
          <path d="M14 11a5 5 0 00-7.5-.5l-2 2a5 5 0 007 7l1.5-1.5" stroke="#0891b2" stroke-width="1.5" stroke-linecap="round"/>
        </svg>

        <!-- 名称 -->
        <span
          class="mat-name"
          @click="onItemClick(item)"
          :title="item.type === 'folder' ? '进入文件夹' : item.type === 'link' ? '打开链接' : '下载文件'"
        >{{ item.name }}</span>

        <!-- 文件大小 -->
        <span v-if="item.type === 'file' && item.fileSize" class="mat-size">{{ formatSize(item.fileSize) }}</span>

        <!-- 教师操作按钮 -->
        <div v-if="isTeacher" class="mat-actions">
          <button class="mat-act-btn" @click.stop="startRename(item)">重命名</button>
          <button class="mat-act-btn" @click.stop="startMove(item)">移动</button>
          <button class="mat-act-btn danger" @click.stop="doDelete(item)">删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { api } from '../api/request.js'

const props = defineProps({
  courseId: { type: [Number, String], required: true },
  isTeacher: { type: Boolean, default: false }
})

const items = ref([])
const loading = ref(true)

// 当前路径：从根目录到当前文件夹的节点链
const currentParentId = ref(null)
const breadcrumbs = ref([])  // [{ materialId, name }]

// 弹窗状态
const showAddFolder = ref(false)
const folderName = ref('')

const showAddLink = ref(false)
const linkName = ref('')
const linkUrl = ref('')

const showRename = ref(false)
const renameTarget = ref(null)
const renameName = ref('')

const showMove = ref(false)
const moveTarget = ref(null)
const allFolders = ref([])

// 文件上传
const pendingFiles = ref(null)

async function loadMaterials() {
  loading.value = true
  try {
    const res = await api.getMaterials(props.courseId, currentParentId.value)
    items.value = res.data || []
  } catch (e) {
    console.error('加载资料失败', e)
  } finally {
    loading.value = false
  }
}

function navigateTo(parentId, breadIndex) {
  if (breadIndex != null) {
    // 点击面包屑：截断到该层
    breadcrumbs.value = breadcrumbs.value.slice(0, breadIndex + 1)
  } else {
    breadcrumbs.value = []
  }
  currentParentId.value = parentId
  loadMaterials()
}

// 进入文件夹（点击）
function enterFolder(item) {
  breadcrumbs.value.push({ materialId: item.materialId, name: item.name })
  currentParentId.value = item.materialId
  loadMaterials()
}

// 下载文件
function downloadFile(item) {
  window.open(item.filePath, '_blank')
}

// 打开外链
function openLink(item) {
  let url = item.url
  if (!/^https?:\/\//i.test(url)) {
    url = 'https://' + url
  }
  window.open(url, '_blank')
}

function onItemClick(item) {
  if (item.type === 'folder') enterFolder(item)
  else if (item.type === 'file') downloadFile(item)
  else if (item.type === 'link') openLink(item)
}

// ========== 教师操作 ==========

async function createFolder() {
  if (!folderName.value.trim()) return
  try {
    await api.createMaterialFolder(props.courseId, folderName.value.trim(), currentParentId.value)
    folderName.value = ''
    showAddFolder.value = false
    loadMaterials()
  } catch (e) {
    alert(e.message || '创建失败')
  }
}

async function onFileChange(e) {
  const files = e.target.files
  if (!files || files.length === 0) return
  try {
    await api.uploadMaterialFile(props.courseId, files, currentParentId.value)
    e.target.value = ''
    loadMaterials()
  } catch (err) {
    alert(err.message || '上传失败')
  }
}

async function addLink() {
  if (!linkName.value.trim() || !linkUrl.value.trim()) return
  try {
    await api.addMaterialLink(props.courseId, linkName.value.trim(), linkUrl.value.trim(), currentParentId.value)
    linkName.value = ''
    linkUrl.value = ''
    showAddLink.value = false
    loadMaterials()
  } catch (e) {
    alert(e.message || '添加失败')
  }
}

async function doDelete(item) {
  const msg = item.type === 'folder' ? '确定删除该文件夹及其所有内容？' : '确定删除？'
  if (!confirm(msg)) return
  try {
    await api.deleteMaterial(item.materialId)
    loadMaterials()
  } catch (e) {
    alert(e.message || '删除失败')
  }
}

function startRename(item) {
  renameTarget.value = item
  renameName.value = item.name
  showRename.value = true
}

async function doRename() {
  if (!renameName.value.trim()) return
  try {
    await api.renameMaterial(renameTarget.value.materialId, renameName.value.trim())
    showRename.value = false
    renameTarget.value = null
    loadMaterials()
  } catch (e) {
    alert(e.message || '重命名失败')
  }
}

async function startMove(item) {
  moveTarget.value = item
  // 加载所有文件夹（排除自身及其子文件夹）
  try {
    const res = await loadAllFolders(props.courseId)
    allFolders.value = res.filter(f => f.materialId !== item.materialId)
  } catch (e) {
    allFolders.value = []
  }
  showMove.value = true
}

/** 递归获取课程下所有文件夹 */
async function loadAllFolders(courseId, parentId = null) {
  const result = []
  try {
    const res = await api.getMaterials(courseId, parentId)
    const data = res.data || []
    for (const item of data) {
      if (item.type === 'folder') {
        result.push(item)
        const children = await loadAllFolders(courseId, item.materialId)
        result.push(...children)
      }
    }
  } catch (e) { /* ignore */ }
  return result
}

async function doMove(targetParentId) {
  try {
    await api.moveMaterial(moveTarget.value.materialId, targetParentId)
    showMove.value = false
    moveTarget.value = null
    loadMaterials()
  } catch (e) {
    alert(e.message || '移动失败')
  }
}

function formatSize(bytes) {
  if (!bytes) return ''
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

onMounted(() => { loadMaterials() })
</script>

<style scoped>
.material-panel { position: relative; }

/* 面包屑 */
.mat-breadcrumb {
  display: flex; align-items: center; gap: 4px; flex-wrap: wrap;
  margin-bottom: 14px; font-size: 13px;
}
.mat-bread-item {
  color: #2377E4; cursor: pointer;
}
.mat-bread-item:hover { text-decoration: underline; }
.mat-bread-sep { color: #ccc; }

/* 工具栏 */
.mat-toolbar {
  display: flex; gap: 8px; margin-bottom: 14px; flex-wrap: wrap;
}
.mat-tb-btn {
  display: inline-flex; align-items: center;
  padding: 6px 14px; border: 1px solid #2377E4; border-radius: 6px;
  background: #fff; color: #2377E4; font-size: 12px; cursor: pointer;
  transition: background .15s;
}
.mat-tb-btn:hover { background: #eef2ff; }
.mat-tb-btn input[type=file] { display: none; }

/* 资料列表 */
.mat-list { display: flex; flex-direction: column; gap: 4px; }
.mat-item {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 12px; border-radius: 8px;
  transition: background .15s;
}
.mat-item:hover { background: #f5f6fa; }
.mat-icon { flex-shrink: 0; }
.mat-name {
  flex: 1; min-width: 0; font-size: 14px; color: #1a1a1a;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  cursor: pointer;
}
.mat-folder .mat-name { font-weight: 500; }
.mat-name:hover { color: #2377E4; }
.mat-size { font-size: 11px; color: #999; white-space: nowrap; }

.mat-actions { display: flex; gap: 4px; flex-shrink: 0; }
.mat-act-btn {
  padding: 3px 8px; border: 1px solid #d1d5db; border-radius: 4px;
  background: #fff; color: #6b7280; font-size: 11px; cursor: pointer;
}
.mat-act-btn:hover { background: #f3f4f6; }
.mat-act-btn.danger { color: #ef4444; border-color: #fca5a5; }
.mat-act-btn.danger:hover { background: #fef2f2; }

.mat-empty { text-align: center; padding: 40px 0; color: #bbb; font-size: 13px; }

/* 弹窗 */
.mat-overlay {
  position: fixed; inset: 0; z-index: 100;
  background: rgba(0,0,0,.3); display: flex; align-items: center; justify-content: center;
}
.mat-dialog {
  background: #fff; border-radius: 12px; padding: 20px 24px;
  width: 90%; max-width: 360px; box-shadow: 0 8px 30px rgba(0,0,0,.15);
}
.mat-dialog h4 { margin: 0 0 12px; font-size: 15px; color: #111; }
.mat-input {
  width: 100%; padding: 8px 12px; border: 1px solid #e5e7eb; border-radius: 6px;
  font-size: 14px; outline: none; box-sizing: border-box;
}
.mat-input:focus { border-color: #2377E4; }
.mat-dialog-btns { display: flex; justify-content: flex-end; gap: 8px; margin-top: 14px; }
.mat-d-btn {
  padding: 6px 16px; border: none; border-radius: 6px;
  background: #2377E4; color: #fff; font-size: 13px; cursor: pointer;
}
.mat-d-btn:disabled { opacity: .5; cursor: not-allowed; }
.mat-d-btn.cancel { background: #f3f4f6; color: #6b7280; }

.mat-move-list {
  max-height: 200px; overflow-y: auto; margin-bottom: 8px;
  border: 1px solid #e5e7eb; border-radius: 6px;
}
.mat-move-item {
  padding: 8px 12px; cursor: pointer; font-size: 13px; color: #333;
  border-bottom: 1px solid #f0f0f0;
}
.mat-move-item:last-child { border-bottom: none; }
.mat-move-item:hover { background: #eef2ff; }
</style>
