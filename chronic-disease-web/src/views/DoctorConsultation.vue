<script setup>
import { ref, nextTick, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getConsultationPage, markConsultationRead } from '../api/user.js'

const route = useRoute()
const router = useRouter()

const patientId = route.params.patientId
const patientName = ref(route.query.patientName || '患者')
const online = ref(false)

// 消息列表
const messages = ref([])
const listRef = ref(null)
const inputText = ref('')
const sending = ref(false)

// WebSocket
let ws = null
const token = localStorage.getItem('token')

function connectWs() {
  if (ws && ws.readyState === WebSocket.OPEN) return
  const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:'
  const wsUrl = import.meta.env.DEV
    ? `ws://localhost:9000/consultation-ws?token=${token}`
    : `${protocol}//${location.host}/api/consultation-ws?token=${token}`
  ws = new WebSocket(wsUrl)

  ws.onopen = () => {
    console.log('WebSocket 已连接')
    // 上线后标记已读
    markRead()
  }

  ws.onmessage = (event) => {
    try {
      const msg = JSON.parse(event.data)
      if (msg.type === 'new_message') {
        // 只处理当前聊天对象的消息
        if (`${msg.patientId}` === `${patientId}` || `${msg.doctorId}` === `${patientId}`) {
          messages.value.push({
            senderId: msg.senderId,
            senderRole: msg.senderRole,
            content: msg.content,
            createTime: msg.createTime
          })
          scrollBottom()
          markRead()
        } else {
          ElMessage.info('收到新消息')
        }
      } else if (msg.type === 'unread_count') {
        // 未读计数，暂不处理
      } else if (msg.type === 'ack') {
        // 消息回执
      }
    } catch (e) {
      console.error('消息解析失败', e)
    }
  }

  ws.onclose = () => {
    console.log('WebSocket 已断开，3秒后重连')
    setTimeout(connectWs, 3000)
  }

  ws.onerror = (err) => {
    console.error('WebSocket 错误', err)
  }
}

// 加载历史消息
async function loadHistory() {
  try {
    const res = await getConsultationPage({
      patientId: patientId,
      doctorId: getDoctorId(),
      pageNum: 1,
      pageSize: 50
    })
    // res 是 Map 格式: { "2026.07.29": [...], "2026.07.28": [...] }
    const list = []
    Object.entries(res).forEach(([date, msgs]) => {
      list.push({ type: 'date', date })
      msgs.forEach(msg => list.push(msg))
    })
    messages.value = list
    await nextTick()
    scrollBottom()
  } catch (e) {
    ElMessage.error('加载历史消息失败')
  }
}

// 标记已读
async function markRead() {
  try {
    await markConsultationRead(patientId)
  } catch {}
}

// 获取医生ID（从 sessionStorage）
function getDoctorId() {
  try {
    const userInfo = JSON.parse(sessionStorage.getItem('userInfo') || '{}')
    return userInfo.id
  } catch { return 0 }
}

// 发送消息
async function sendMessage() {
  const text = inputText.value.trim()
  if (!text) return
  sending.value = true
  try {
    const msg = JSON.stringify({
      patientId: patientId,
      doctorId: getDoctorId(),
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
  router.push('/admin/patients')
}

function goBack() {
  router.push('/admin/patients')
}

function handleKeydown(e) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

// 格式化时间
function fmtTime(t) {
  if (!t) return ''
  if (t.length <= 5) return t
  const d = new Date(t.replace(' ', 'T'))
  return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
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
  <div class="dc-root">
    <!-- 顶栏 -->
    <div class="dc-header">
      <span class="dc-back" @click="handleBack">
        <el-icon :size="18"><ArrowLeft /></el-icon>
      </span>
      <div class="dc-header-info">
        <span class="dc-header-name">{{ patientName }}</span>
        <span class="dc-header-status" :class="{ online }">
          {{ online ? '在线' : '离线' }}
        </span>
      </div>
    </div>

    <!-- 消息区 -->
    <div class="dc-msg-list" ref="listRef">
      <template v-for="(item, idx) in messages" :key="idx">
        <div v-if="item.type === 'date'" class="msg-date-sep">{{ item.date }}</div>
        <div
          v-else
          :class="['msg-row', item.senderRole === 'DOCTOR' ? 'msg-right' : 'msg-left']"
        >
          <span v-if="item.senderRole !== 'DOCTOR'" class="msg-sender">{{ patientName }}</span>
          <div class="msg-bubble">
            <div class="msg-content">{{ item.content }}</div>
          </div>
          <div class="msg-time">{{ fmtTime(item.createTime) }}</div>
        </div>
      </template>

      <div v-if="messages.length === 0" class="msg-empty">
        <p class="msg-empty-text">暂无消息，开始沟通吧</p>
      </div>
    </div>

    <!-- 输入区 -->
    <div class="dc-input-area">
      <textarea
        v-model="inputText"
        class="dc-textarea"
        placeholder="输入消息..."
        @keydown="handleKeydown"
        rows="1"
      ></textarea>
      <button class="dc-send-btn" :disabled="!inputText.trim() || sending" @click="sendMessage">
        {{ sending ? '发送中' : '发送' }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.dc-root {
  display: flex; flex-direction: column;
  height: 100%; max-width: 750px;
  margin: 0 auto; width: 100%;
  background: #fff;
  border-left: 1px solid #fef3c7;
  border-right: 1px solid #fef3c7;
}

/* ---- 顶栏 ---- */
.dc-header {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 20px;
  border-bottom: 1px solid #fef3c7;
  background: #fff;
  flex-shrink: 0;
}
.dc-back {
  cursor: pointer; color: #78716c; padding: 4px;
  border-radius: 8px; transition: all 0.2s;
}
.dc-back:hover { background: #fef3c7; color: #c2410c; }
.dc-header-info { display: flex; align-items: center; gap: 8px; }
.dc-header-name { font-size: 15px; font-weight: 700; color: #431407; }
.dc-header-status {
  font-size: 12px; color: #a8a29e;
  padding: 1px 8px; border-radius: 8px;
  background: #f5f5f4;
}
.dc-header-status.online {
  color: #16a34a; background: #f0fdf4;
}

/* ---- 消息区 ---- */
.dc-msg-list {
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
.msg-sender {
  font-size: 11px; color: #a8a29e;
  margin-bottom: 2px; padding: 0 4px;
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

.msg-date-sep {
  text-align: center; font-size: 12px; color: #a8a29e;
  padding: 8px 0;
  position: relative;
}
.msg-date-sep::before,
.msg-date-sep::after {
  content: ''; position: absolute; top: 50%;
  width: 35%; height: 1px; background: #e7e5e4;
}
.msg-date-sep::before { left: 0; }
.msg-date-sep::after { right: 0; }

.msg-empty {
  flex: 1; display: flex; align-items: center; justify-content: center;
}
.msg-empty-text { font-size: 14px; color: #d6d3d1; }

/* ---- 输入区 ---- */
.dc-input-area {
  display: flex; align-items: flex-end; gap: 10px;
  padding: 14px 20px;
  border-top: 1px solid #fef3c7;
  background: #fff;
  flex-shrink: 0;
}
.dc-textarea {
  flex: 1; min-height: 38px; max-height: 100px;
  border: 1px solid #fde68a; border-radius: 14px;
  padding: 8px 14px; font-size: 14px; outline: none;
  resize: none; font-family: inherit;
  background: #fffbeb; color: #44403c;
  line-height: 1.5;
}
.dc-textarea:focus { border-color: #f59e0b; background: #fff; }
.dc-send-btn {
  height: 38px; padding: 0 22px;
  border-radius: 14px; border: none;
  background: linear-gradient(135deg, #f97316, #ea580c);
  color: #fff; font-size: 14px; font-weight: 600;
  cursor: pointer; transition: all 0.2s;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(234,88,12,0.25);
}
.dc-send-btn:hover { background: linear-gradient(135deg, #ea580c, #c2410c); }
.dc-send-btn:disabled {
  opacity: 0.5; cursor: not-allowed;
  box-shadow: none;
}
</style>
