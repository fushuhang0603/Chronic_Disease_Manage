<script setup>
import { ref, nextTick, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getConsultationPage, markConsultationRead } from '../api/user.js'

const route = useRoute()
const router = useRouter()

const doctorId = route.params.doctorId
const doctorName = ref(route.query.doctorName || '医生')

// 消息列表
const messages = ref([])
const listRef = ref(null)
const inputText = ref('')
const sending = ref(false)

// WebSocket
let ws = null
const token = localStorage.getItem('token')

function getPatientId() {
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    return userInfo.id
  } catch { return 0 }
}

function connectWs() {
  if (ws && ws.readyState === WebSocket.OPEN) return
  const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:'
  const wsUrl = import.meta.env.DEV
    ? `ws://localhost:9000/consultation-ws?token=${token}`
    : `${protocol}//${location.host}/api/consultation-ws?token=${token}`
  ws = new WebSocket(wsUrl)

  ws.onopen = () => {
    console.log('WebSocket 已连接')
    markRead()
  }

  ws.onmessage = (event) => {
    try {
      const msg = JSON.parse(event.data)
      if (msg.type === 'new_message') {
        messages.value.push({
          senderId: msg.senderId,
          senderRole: msg.senderRole,
          content: msg.content,
          createTime: msg.createTime
        })
        scrollBottom()
        markRead()
      }
    } catch (e) {
      console.error('消息解析失败', e)
    }
  }

  ws.onclose = () => {
    console.log('WebSocket 已断开，3秒后重连')
    setTimeout(connectWs, 3000)
  }
}

async function loadHistory() {
  try {
    const res = await getConsultationPage({
      patientId: getPatientId(),
      doctorId: doctorId,
      pageNum: 1,
      pageSize: 50
    })
    messages.value = (res.records || []).reverse()
    totalMsg.value = res.total || 0
    await nextTick()
    scrollBottom()
  } catch (e) {
    ElMessage.error('加载历史消息失败')
  }
}

async function markRead() {
  try {
    await markConsultationRead(getPatientId(), doctorId)
  } catch {}
}

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text) return
  sending.value = true
  try {
    const msg = JSON.stringify({
      patientId: getPatientId(),
      doctorId: doctorId,
      content: text
    })
    ws.send(msg)
    inputText.value = ''
  } catch (e) {
    ElMessage.error('发送失败')
  } finally {
    sending.value = false
  }
}

function scrollBottom() {
  nextTick(() => {
    if (listRef.value) {
      listRef.value.scrollTop = listRef.value.scrollHeight
    }
  })
}

function handleBack() {
  router.push('/patient/doctors')
}

function handleKeydown(e) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

function fmtTime(t) {
  if (!t) return ''
  const d = new Date(t.replace(' ', 'T'))
  const now = new Date()
  const isToday = d.toDateString() === now.toDateString()
  if (isToday) {
    return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  return d.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

onMounted(async () => {
  await loadHistory()
  connectWs()
})

onUnmounted(() => {
  if (ws) ws.close()
})
</script>

<template>
  <div class="pc-root">
    <!-- 顶栏 -->
    <div class="pc-header">
      <span class="pc-back" @click="handleBack">
        <el-icon :size="18"><ArrowLeft /></el-icon>
      </span>
      <div class="pc-header-info">
        <span class="pc-header-name">{{ doctorName }}</span>
        <span class="pc-header-label">在线咨询</span>
      </div>
    </div>

    <!-- 消息区 -->
    <div class="pc-msg-list" ref="listRef">
      <div
        v-for="(msg, idx) in messages"
        :key="idx"
        :class="['msg-row', msg.senderRole === 'PATIENT' ? 'msg-right' : 'msg-left']"
      >
        <div class="msg-bubble">
          <div class="msg-content">{{ msg.content }}</div>
        </div>
        <div class="msg-time">{{ fmtTime(msg.createTime) }}</div>
      </div>

      <div v-if="messages.length === 0" class="msg-empty">
        <p class="msg-empty-text">暂无消息，向您的医生咨询吧</p>
      </div>
    </div>

    <!-- 输入区 -->
    <div class="pc-input-area">
      <textarea
        v-model="inputText"
        class="pc-textarea"
        placeholder="输入消息..."
        @keydown="handleKeydown"
        rows="1"
      ></textarea>
      <button class="pc-send-btn" :disabled="!inputText.trim() || sending" @click="sendMessage">
        {{ sending ? '发送中' : '发送' }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.pc-root {
  display: flex; flex-direction: column;
  height: 100%; max-width: 750px;
  margin: 0 auto; width: 100%;
  background: #fff;
  border-left: 1px solid #fef3c7;
  border-right: 1px solid #fef3c7;
}

/* ---- 顶栏 ---- */
.pc-header {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 20px;
  border-bottom: 1px solid #fef3c7;
  background: #fff;
  flex-shrink: 0;
}
.pc-back {
  cursor: pointer; color: #78716c; padding: 4px;
  border-radius: 8px; transition: all 0.2s;
}
.pc-back:hover { background: #fef3c7; color: #c2410c; }
.pc-header-info { display: flex; align-items: center; gap: 8px; }
.pc-header-name { font-size: 15px; font-weight: 700; color: #431407; }
.pc-header-label {
  font-size: 12px; color: #c2410c;
  background: #fff7ed; padding: 2px 10px; border-radius: 8px;
}

/* ---- 消息区 ---- */
.pc-msg-list {
  flex: 1; overflow-y: auto;
  padding: 20px;
  display: flex; flex-direction: column;
  gap: 16px;
  background: #fafaf9;
}

.msg-row { display: flex; flex-direction: column; max-width: 75%; }
.msg-left { align-self: flex-start; align-items: flex-start; }
.msg-right { align-self: flex-end; align-items: flex-end; }

.msg-bubble {
  padding: 10px 14px; border-radius: 14px;
  font-size: 14px; line-height: 1.6;
  word-break: break-word;
}
.msg-left .msg-bubble {
  background: #fff; border: 1px solid #fef3c7;
  border-bottom-left-radius: 4px;
}
.msg-right .msg-bubble {
  background: linear-gradient(135deg, #fb923c, #f97316);
  color: #fff;
  border-bottom-right-radius: 4px;
}

.msg-time {
  font-size: 11px; color: #a8a29e;
  margin-top: 4px; padding: 0 4px;
}

.msg-empty {
  flex: 1; display: flex; align-items: center; justify-content: center;
}
.msg-empty-text { font-size: 14px; color: #d6d3d1; }

/* ---- 输入区 ---- */
.pc-input-area {
  display: flex; align-items: flex-end; gap: 10px;
  padding: 14px 20px;
  border-top: 1px solid #fef3c7;
  background: #fff;
  flex-shrink: 0;
}
.pc-textarea {
  flex: 1; min-height: 38px; max-height: 100px;
  border: 1px solid #fde68a; border-radius: 14px;
  padding: 8px 14px; font-size: 14px; outline: none;
  resize: none; font-family: inherit;
  background: #fffbeb; color: #44403c;
  line-height: 1.5;
}
.pc-textarea:focus { border-color: #f59e0b; background: #fff; }
.pc-send-btn {
  height: 38px; padding: 0 22px;
  border-radius: 14px; border: none;
  background: linear-gradient(135deg, #f97316, #ea580c);
  color: #fff; font-size: 14px; font-weight: 600;
  cursor: pointer; transition: all 0.2s;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(234,88,12,0.25);
}
.pc-send-btn:hover { background: linear-gradient(135deg, #ea580c, #c2410c); }
.pc-send-btn:disabled { opacity: 0.5; cursor: not-allowed; box-shadow: none; }
</style>
