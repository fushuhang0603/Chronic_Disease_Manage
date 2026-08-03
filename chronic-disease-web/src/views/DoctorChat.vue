<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getChatRecords, markChatRead } from '../api/user.js'

const route = useRoute()
const chatEl = ref(null)
const inputText = ref('')
const connected = ref(false)
const historyLoading = ref(false)
const noMore = ref(false)

const patientId = route.query.userId
const patientName = route.query.userName || '患者'

const doctorName = (() => {
  try {
    const raw = sessionStorage.getItem('userInfo')
    return raw ? (JSON.parse(raw).realName || JSON.parse(raw).username || '医生') : '医生'
  } catch { return '医生' }
})()
const messages = ref([])
let ws = null
let pageNum = 1 // 已加载到第几页

onMounted(async () => {
  if (!patientId) { ElMessage.error('缺少患者信息'); return }
  // 先拉历史消息，再连 WebSocket，避免漏消息
  await loadHistory()
  markChatRead(patientId).catch(() => {})
  connectWs()
})

// 分页加载历史消息：每次加载下一批更早的消息（时间倒序，最新的在底部）
async function loadHistory() {
  if (historyLoading.value || noMore.value) return
  historyLoading.value = true
  try {
    const res = await getChatRecords({ otherUserId: patientId, pageNum, pageSize: 20 })
    const list = res?.records || []
    // 已加载条数达到总条数 → 没有更多了
    if (messages.value.length + list.length >= (res?.total || 0)) noMore.value = true
    const newMsgs = list.map(m => ({
      id: m.id,
      content: m.content,
      time: formatTime(m.time),
      senderRole: m.senderRole,
      self: m.senderRole === 'DOCTOR',
    }))
    if (pageNum === 1) {
      // 第一页：最新的在列表最底部
      messages.value = [...newMsgs].reverse()
      nextTick(() => scrollBottom())
    } else {
      // 后面的页：更早的消息插到顶部
      messages.value = [...newMsgs].reverse().concat(messages.value)
    }
    pageNum++
  } catch (e) {
    console.error('[DoctorChat] 历史消息加载失败:', e)
  } finally {
    historyLoading.value = false
  }
}

function connectWs() {
  const token = sessionStorage.getItem('token')
  if (!token) {
    console.error('[DoctorChat] Token 不存在，无法连接 WebSocket')
    return
  }
  const httpUrl = import.meta.env.DEV
    ? `ws://localhost:9000/ws/chat?token=${encodeURIComponent(token)}`
    : `ws://${location.host}/ws/chat?token=${encodeURIComponent(token)}`
  console.log('[DoctorChat] WebSocket 连接中:', httpUrl)
  ws = new WebSocket(httpUrl)
  ws.onopen = () => { console.log('[DoctorChat] WebSocket 已连接'); connected.value = true }
  ws.onmessage = (e) => {
    try {
      const msg = JSON.parse(e.data)
      if (msg.type === 'error') { ElMessage.error(msg.message); return }
      const atBottom = isNearBottom()
      messages.value.push({
        id: 0,
        rawTime: msg.time,
        content: msg.content,
        time: formatTime(msg.time),
        senderRole: msg.senderRole,
        self: msg.senderRole === 'DOCTOR',
      })
      if (atBottom) scrollBottom()
    } catch (err) { console.error('[DoctorChat] 消息解析失败:', err) }
  }
  ws.onclose = (e) => { console.log('[DoctorChat] WebSocket 已断开, code:', e.code); connected.value = false }
  ws.onerror = (e) => { console.error('[DoctorChat] WebSocket 连接失败'); connected.value = false }
}

function sendMessage() {
  const text = inputText.value.trim()
  if (!text || !ws || ws.readyState !== WebSocket.OPEN) return
  ws.send(JSON.stringify({
    toUserId: String(patientId), content: text,
    patientName, doctorName, senderName: '', senderRole: 'DOCTOR',
  }))
  const t = fullNow()
  messages.value.push({ id: 0, content: text, time: formatTime(t), senderRole: 'DOCTOR', self: true })
  inputText.value = ''
  scrollBottom()
}

// 向上滚动接近顶部时加载更早的消息
function handleScroll(e) {
  if (e.target.scrollTop < 30) loadHistory()
}

function isNearBottom() {
  const el = chatEl.value
  if (!el) return true
  return el.scrollHeight - el.scrollTop - el.clientHeight < 80
}

function scrollBottom() {
  nextTick(() => {
    if (chatEl.value) chatEl.value.scrollTop = chatEl.value.scrollHeight
  })
}

function fullNow() {
  const d = new Date()
  const p = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(String(t).replace(/-/g, '/'))
  if (isNaN(d.getTime())) return t
  const now = new Date()
  const sameDay = d.getFullYear() === now.getFullYear() && d.getMonth() === now.getMonth() && d.getDate() === now.getDate()
  const p = n => String(n).padStart(2, '0')
  const hm = `${p(d.getHours())}:${p(d.getMinutes())}`
  return sameDay ? hm : `${d.getMonth() + 1}月${d.getDate()}日 ${hm}`
}

function handleKeydown(e) { if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); sendMessage() } }
onUnmounted(() => { if (ws) ws.close() })
</script>

<template>
  <div class="chat-root">
    <div class="chat-header">
      <div class="ch-avatar"><span>{{ (patientName || '患')[0] }}</span></div>
      <div class="ch-info">
        <h4 class="ch-name">{{ patientName }}</h4>
        <div class="ch-subtitle">在线问诊</div>
      </div>
      <span :class="['ch-status', { online: connected }]">{{ connected ? '已连接' : '连接中...' }}</span>
    </div>

    <div ref="chatEl" class="chat-body" @scroll="handleScroll">
      <div v-if="historyLoading" class="chat-loading">加载历史消息...</div>
      <div v-for="(m, i) in messages" :key="i" :class="['msg-row', m.self ? 'right' : 'left']">
        <div class="msg-bubble" :class="{ self: m.self }">
          <p class="msg-text">{{ m.content }}</p>
          <span class="msg-time">{{ m.time }}</span>
        </div>
      </div>
      <div v-if="messages.length === 0" class="chat-empty">
        <el-icon :size="40" color="#d6d3d1"><ChatDotRound /></el-icon>
        <p>发送消息开始问诊</p>
      </div>
    </div>

    <div class="chat-input">
      <textarea v-model="inputText" class="ci-textarea" placeholder="输入消息..." rows="2" @keydown="handleKeydown" :disabled="!connected"></textarea>
      <el-button type="primary" class="ci-btn" :disabled="!inputText.trim() || !connected" @click="sendMessage">发送</el-button>
    </div>
  </div>
</template>

<style scoped>
.chat-root {
  display: flex; flex-direction: column;
  height: calc(100vh - 64px);
  max-width: 800px; margin: 0 auto;
  background: #fff; border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 20px rgba(249,115,22,0.06);
  border: 1px solid #fed7aa;
}

/* ==== 头部 ==== */
.chat-header {
  display: flex; align-items: center; gap: 14px;
  padding: 16px 22px;
  background: linear-gradient(135deg, #fff7ed, #fffbeb);
  border-bottom: 1px solid #fde68a;
}
.ch-avatar {
  width: 44px; height: 44px; border-radius: 14px;
  background: linear-gradient(135deg, #fb923c, #ea580c);
  color: #fff; font-size: 18px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 3px 10px rgba(249,115,22,0.25);
  flex-shrink: 0;
}
.ch-info { flex: 1; min-width: 0; }
.ch-name {
  font-size: 16px; font-weight: 700; color: #431407; margin: 0;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.ch-subtitle { font-size: 12px; color: #9a3412; margin-top: 1px; }
.ch-status {
  font-size: 11px; font-weight: 600; padding: 4px 12px; border-radius: 12px;
  background: #fef3c7; color: #92400e; flex-shrink: 0;
}
.ch-status.online { background: #dcfce7; color: #16a34a; }

/* ==== 消息区 ==== */
.chat-body {
  flex: 1; overflow-y: auto; padding: 20px;
  display: flex; flex-direction: column; gap: 16px;
  background: #fffdf7;
}
.chat-empty {
  display: flex; flex-direction: column; align-items: center;
  justify-content: center; height: 100%; gap: 8px;
  color: #a8a29e; font-size: 14px;
}
.chat-empty p { margin: 0; }
.chat-loading {
  text-align: center; font-size: 12px; color: #a8a29e;
  padding: 4px 0;
}

.msg-row { display: flex; align-items: flex-end; gap: 8px; }
.msg-row.right { justify-content: flex-end; }
.msg-row.left { justify-content: flex-start; }

.msg-bubble {
  max-width: 72%; padding: 10px 14px; border-radius: 16px;
  background: #fff7ed; border: 1px solid #fed7aa;
  border-bottom-left-radius: 4px;
}
.msg-bubble.self {
  background: linear-gradient(135deg, #f97316, #ea580c);
  color: #fff; border: none;
  border-bottom-right-radius: 4px;
}
.msg-text { margin: 0; font-size: 14px; line-height: 1.6; word-break: break-word; }
.msg-time { font-size: 10px; color: #a8a29e; margin-top: 4px; display: block; }
.msg-bubble.self .msg-time { color: rgba(255,255,255,0.65); }

/* ==== 输入区 ==== */
.chat-input {
  display: flex; align-items: flex-end; gap: 10px;
  padding: 14px 20px; border-top: 1px solid #fed7aa;
  background: #fff;
}
.ci-textarea {
  flex: 1; resize: none; border: 1px solid #e7e5e4;
  border-radius: 14px; padding: 10px 16px; font-size: 14px;
  outline: none; font-family: inherit; line-height: 1.5;
  background: #fffdf7;
  transition: border-color 0.2s;
}
.ci-textarea:focus { border-color: #f97316; background: #fff; }
.ci-btn {
  height: 46px; padding: 0 24px; border-radius: 14px;
  background: linear-gradient(135deg, #f97316, #ea580c);
  border: none; font-weight: 700; font-size: 14px;
  box-shadow: 0 3px 12px rgba(234,88,12,0.3);
  transition: all 0.2s;
}
.ci-btn:hover { transform: translateY(-1px); box-shadow: 0 6px 20px rgba(234,88,12,0.4); }
.ci-btn:disabled { background: #e7e5e4; color: #a8a29e; box-shadow: none; transform: none; }
</style>
