<script setup>
import { ref, nextTick, onUnmounted, computed } from 'vue'
import { patientChatStream } from '../api/agent.js'
import { marked } from 'marked'

// 配置 marked
marked.setOptions({ breaks: true, gfm: true })

function renderMd(text) {
  if (!text) return ''
  return marked(text)
}

const chatEl = ref(null)
const inputText = ref('')
const loading = ref(false)
const messages = ref([])
const currentStreamMsg = ref(null)
let abortCtrl = null

const quickQuestions = [
  '我患有糖尿病，推荐内分泌科的医生',
  '高血压平时饮食要注意什么？',
  '最近血糖不太稳定，怎么办？',
  '帮我解读一下检查报告',
]

function sendMessage(text) {
  const msg = (text || inputText.value).trim()
  if (!msg) return
  messages.value.push({ role: 'user', content: msg, time: fmtNow() })
  inputText.value = ''
  loading.value = true

  const aiMsg = { role: 'assistant', content: '', time: '', streaming: true }
  messages.value.push(aiMsg)
  currentStreamMsg.value = aiMsg
  scrollBottom()

  abortCtrl = patientChatStream(msg, {
    onToken(chunk) {
      if (currentStreamMsg.value) {
        currentStreamMsg.value.content += chunk
        nextTick(() => scrollBottom())
      }
    },
    onDone() {
      if (currentStreamMsg.value) {
        currentStreamMsg.value.time = fmtNow()
        currentStreamMsg.value.streaming = false
      }
      currentStreamMsg.value = null
      loading.value = false
      scrollBottom()
    },
    onError(err) {
      if (currentStreamMsg.value) {
        currentStreamMsg.value.content = '抱歉，服务暂时不可用，请稍后重试。'
        currentStreamMsg.value.time = fmtNow()
        currentStreamMsg.value.streaming = false
        currentStreamMsg.value.error = true
      }
      currentStreamMsg.value = null
      loading.value = false
    },
  })
}

function scrollBottom() {
  nextTick(() => {
    if (chatEl.value) chatEl.value.scrollTop = chatEl.value.scrollHeight
  })
}

function handleKeydown(e) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

function fmtNow() {
  const d = new Date()
  const p = n => String(n).padStart(2, '0')
  return `${p(d.getHours())}:${p(d.getMinutes())}`
}

onUnmounted(() => {
  if (abortCtrl) abortCtrl.abort()
})
</script>

<template>
  <div class="assistant-root">
    <!-- 背景装饰 -->
    <div class="bg-decor">
      <div class="bg-blob blob-1"></div>
      <div class="bg-blob blob-2"></div>
      <div class="bg-blob blob-3"></div>
      <div class="bg-dots"></div>
    </div>

    <div class="assistant-body">
      <!-- 头部 -->
      <div class="as-header">
        <div class="as-header-bg"></div>
        <div class="as-header-content">
          <div class="as-avatar-ring">
            <span class="as-avatar">
              <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="8" r="4"/>
                <path d="M5 21c0-3.9 3.1-7 7-7s7 3.1 7 7"/>
              </svg>
            </span>
          </div>
          <div>
            <h2 class="as-title">智能医助</h2>
            <p class="as-subtitle">
              <span class="as-dot-live"></span>
              AI 在线 · 随时为你解答
            </p>
          </div>
        </div>
      </div>

      <!-- 消息区 -->
      <div ref="chatEl" class="as-chat">
        <div v-if="messages.length === 0" class="as-welcome">
          <div class="as-welcome-icon">
            <svg viewBox="0 0 24 24" width="40" height="40" fill="none" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="8" r="5"/>
              <path d="M4 22c0-4.4 3.6-8 8-8s8 3.6 8 8"/>
              <path d="M12 1v3"/>
              <circle cx="19" cy="4" r="3" fill="currentColor" stroke="none" opacity="0.2"/>
              <circle cx="5" cy="19" r="2" fill="currentColor" stroke="none" opacity="0.15"/>
              <circle cx="21" cy="14" r="1.5" fill="currentColor" stroke="none" opacity="0.15"/>
            </svg>
          </div>
          <h3 class="as-welcome-title">你好，有什么可以帮你的？</h3>
          <p class="as-welcome-sub">我是你的 AI 健康助手，可以帮你解答慢病管理问题</p>
          <div class="as-quick-row">
            <span
              v-for="q in quickQuestions"
              :key="q"
              class="as-quick-tag"
              @click="sendMessage(q)"
            >{{ q }}</span>
          </div>
        </div>

        <template v-for="(m, i) in messages" :key="i">
          <div v-if="m.role === 'user'" class="as-msg-row right">
            <div class="as-bubble user">
              <p class="as-msg-text">{{ m.content }}</p>
            </div>
            <span class="as-msg-avatar user-avatar">
              <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
            </span>
          </div>
          <div v-else class="as-msg-row left">
            <span class="as-msg-avatar ai-avatar">
              <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="8" r="4"/>
                <path d="M5 21c0-3.9 3.1-7 7-7s7 3.1 7 7"/>
              </svg>
            </span>
            <div class="as-bubble ai" :class="{ error: m.error }">
              <div
                class="as-msg-text"
                v-html="renderMd(m.content) + (m.streaming && i === messages.length - 1 ? '<span class=as-cursor>|</span>' : '')"
              ></div>
            </div>
          </div>
        </template>
      </div>

      <!-- 输入区 -->
      <div class="as-input-area">
        <div class="as-input-ring">
          <textarea
            v-model="inputText"
            class="as-textarea"
            placeholder="输入你的健康问题..."
            rows="1"
            @keydown="handleKeydown"
            :disabled="loading"
          ></textarea>
          <button
            class="as-send-btn"
            :class="{ loading: loading }"
            :disabled="!inputText.trim() || loading"
            @click="sendMessage()"
          >
            <svg v-if="!loading" viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="22" y1="2" x2="11" y2="13"/>
              <polygon points="22 2 15 22 11 13 2 9 22 2"/>
            </svg>
            <span v-else class="as-send-dots">
              <span class="as-dot"></span>
              <span class="as-dot"></span>
              <span class="as-dot"></span>
            </span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.assistant-root {
  height: calc(100vh - 56px);
  display: flex;
  background: linear-gradient(180deg, #f0f7ff 0%, #eff6ff 40%, #f8fafc 100%);
  position: relative;
  overflow: hidden;
}

/* ====== 背景装饰 ====== */
.bg-decor { position: absolute; inset: 0; pointer-events: none; z-index: 0; overflow: hidden; }
.bg-blob {
  position: absolute; border-radius: 50%;
  filter: blur(60px); opacity: 0.25;
}
.blob-1 {
  width: 300px; height: 300px;
  top: -80px; right: -60px;
  background: rgba(147, 197, 253, 0.5);
}
.blob-2 {
  width: 200px; height: 200px;
  bottom: 10%; left: -40px;
  background: rgba(96, 165, 250, 0.4);
}
.blob-3 {
  width: 160px; height: 160px;
  top: 40%; right: -30px;
  background: rgba(191, 219, 254, 0.5);
}
.bg-dots {
  position: absolute; inset: 0;
  background-image: radial-gradient(circle, rgba(59, 130, 246, 0.08) 1px, transparent 1px);
  background-size: 24px 24px;
}

/* ====== 主体 ====== */
.assistant-body {
  position: relative; z-index: 1;
  display: flex; flex-direction: column;
  width: 100%; max-width: 780px;
  margin: 16px auto;
  background: rgba(255,255,255,0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 28px;
  box-shadow: 0 8px 40px rgba(59, 130, 246, 0.08), 0 1px 3px rgba(0,0,0,0.04);
}

/* ====== 头部 ====== */
.as-header {
  position: relative;
  padding: 24px 28px 20px;
  flex-shrink: 0; overflow: hidden;
}
.as-header-bg {
  position: absolute; inset: 0;
  background: linear-gradient(135deg, #dbeafe 0%, #eff6ff 60%, #f0f9ff 100%);
  border-radius: 28px 28px 0 0;
  opacity: 0.7;
}
.as-header-content {
  position: relative; z-index: 1;
  display: flex; align-items: center; gap: 16px;
}
.as-avatar-ring {
  width: 52px; height: 52px; border-radius: 50%;
  background: linear-gradient(135deg, #93c5fd, #60a5fa);
  padding: 3px;
  animation: as-pulse 2.5s ease-in-out infinite;
}
@keyframes as-pulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(59, 130, 246, 0.3); }
  50% { box-shadow: 0 0 0 12px rgba(59, 130, 246, 0); }
}
.as-avatar {
  width: 46px; height: 46px; border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  display: flex; align-items: center; justify-content: center;
  color: #fff;
}
.as-title {
  font-size: 20px; font-weight: 700; color: #1e3a5f; margin: 0; line-height: 1.3;
}
.as-subtitle {
  font-size: 12px; color: #64748b; margin: 3px 0 0;
  display: flex; align-items: center; gap: 6px;
}
.as-dot-live {
  width: 7px; height: 7px; border-radius: 50%;
  background: #22c55e;
  box-shadow: 0 0 6px rgba(34, 197, 94, 0.5);
  animation: as-dot-pulse 2s ease-in-out infinite;
}
@keyframes as-dot-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

/* ====== 消息区 ====== */
.as-chat {
  flex: 1; overflow-y: auto;
  padding: 24px 28px;
  display: flex; flex-direction: column;
  gap: 20px;
}

/* 欢迎区 */
.as-welcome {
  flex: 1;
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  padding: 40px 0;
}
.as-welcome-icon {
  width: 80px; height: 80px; border-radius: 50%;
  background: linear-gradient(135deg, #eff6ff, #dbeafe);
  display: flex; align-items: center; justify-content: center;
  color: #3b82f6; margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(59, 130, 246, 0.1);
}
.as-welcome-title {
  font-size: 22px; font-weight: 700; color: #1e3a5f; margin: 0 0 8px;
}
.as-welcome-sub {
  font-size: 14px; color: #94a3b8; margin: 0 0 28px;
}
.as-quick-row {
  display: flex; flex-wrap: wrap; gap: 10px; justify-content: center;
  max-width: 520px;
}
.as-quick-tag {
  font-size: 13px; padding: 10px 20px;
  border-radius: 24px;
  background: rgba(255,255,255,0.7);
  color: #475569; cursor: pointer;
  transition: all 0.25s;
  box-shadow: 0 1px 3px rgba(59, 130, 246, 0.06);
  border: 1px solid rgba(147, 197, 253, 0.3);
}
.as-quick-tag:hover {
  background: #3b82f6; color: #fff;
  border-color: #3b82f6;
  box-shadow: 0 4px 16px rgba(59, 130, 246, 0.25);
  transform: translateY(-1px);
}

/* 消息气泡 */
.as-msg-row { display: flex; align-items: flex-end; gap: 10px; }
.as-msg-row.right { justify-content: flex-end; }
.as-msg-row.left  { justify-content: flex-start; }

.as-msg-avatar {
  width: 32px; height: 32px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.user-avatar {
  background: linear-gradient(135deg, #93c5fd, #60a5fa);
  color: #fff;
}
.ai-avatar {
  background: linear-gradient(135deg, #e0f2fe, #bae6fd);
  color: #3b82f6;
}

.as-bubble {
  max-width: 72%; padding: 12px 20px;
  font-size: 14px; line-height: 1.7;
  position: relative;
}
.as-bubble.user {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: #fff;
  border-radius: 24px 24px 4px 24px;
  box-shadow: 0 4px 14px rgba(37, 99, 235, 0.25);
}
.as-bubble.ai {
  background: #fff;
  color: #334155;
  border-radius: 24px 24px 24px 4px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.04);
  border: 1px solid rgba(147, 197, 253, 0.2);
}
.as-bubble.ai.error {
  background: #fef2f2; border-color: #fecaca; color: #991b1b;
}
.as-msg-text {
  margin: 0; word-break: break-word;
}
.as-msg-text :deep(p) { margin: 0 0 6px; }
.as-msg-text :deep(p:last-child) { margin-bottom: 0; }
.as-msg-text :deep(ul), .as-msg-text :deep(ol) { margin: 4px 0; padding-left: 18px; }
.as-msg-text :deep(li) { margin-bottom: 2px; }
.as-msg-text :deep(strong) { font-weight: 700; }
.as-msg-text :deep(em) { font-style: italic; }
.as-msg-text :deep(code) {
  background: rgba(0,0,0,0.06); padding: 1px 5px; border-radius: 4px;
  font-size: 0.92em;
}
.as-msg-text :deep(pre) {
  background: rgba(0,0,0,0.04); padding: 10px 14px; border-radius: 8px;
  overflow-x: auto; font-size: 13px; margin: 6px 0;
}
.as-msg-text :deep(h1), .as-msg-text :deep(h2), .as-msg-text :deep(h3),
.as-msg-text :deep(h4), .as-msg-text :deep(h5), .as-msg-text :deep(h6) {
  margin: 8px 0 4px; font-weight: 700; line-height: 1.4;
}
.as-msg-text :deep(h3) { font-size: 15px; }
.as-msg-text :deep(h4) { font-size: 14px; }
.as-msg-text :deep(blockquote) {
  border-left: 3px solid #93c5fd; padding-left: 12px;
  margin: 6px 0; color: #64748b;
}
.as-msg-text :deep(hr) { border: none; border-top: 1px solid #e2e8f0; margin: 8px 0; }
.as-msg-text :deep(table) {
  border-collapse: collapse; margin: 6px 0; font-size: 13px;
}
.as-msg-text :deep(th), .as-msg-text :deep(td) {
  border: 1px solid #e2e8f0; padding: 4px 10px; text-align: left;
}
.as-msg-text :deep(th) { background: #f1f5f9; font-weight: 600; }
.as-msg-text :deep(a) { color: #2563eb; text-decoration: underline; }

/* 光标 */
.as-cursor {
  animation: as-blink 1s step-end infinite;
  font-weight: 300; color: #3b82f6;
}
@keyframes as-blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

/* ====== 输入区 ====== */
.as-input-area {
  padding: 12px 24px 20px;
  flex-shrink: 0;
}
.as-input-ring {
  display: flex; align-items: center; gap: 8px;
  padding: 6px 6px 6px 20px;
  border-radius: 32px;
  background: #fff;
  box-shadow: 0 2px 16px rgba(59, 130, 246, 0.08), 0 0 0 1px rgba(147, 197, 253, 0.25);
  transition: box-shadow 0.25s;
}
.as-input-ring:focus-within {
  box-shadow: 0 2px 20px rgba(59, 130, 246, 0.15), 0 0 0 2px rgba(59, 130, 246, 0.3);
}
.as-textarea {
  flex: 1; resize: none; border: none;
  padding: 8px 0; font-size: 14px; outline: none;
  font-family: inherit; line-height: 1.5;
  background: transparent; color: #1e293b;
  max-height: 100px;
}
.as-textarea::placeholder { color: #94a3b8; }
.as-textarea:disabled { color: #94a3b8; }
.as-send-btn {
  width: 44px; height: 44px; border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  border: none; color: #fff; cursor: pointer;
  flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  transition: all 0.25s;
  box-shadow: 0 3px 10px rgba(37, 99, 235, 0.3);
}
.as-send-btn:hover:not(:disabled) {
  transform: scale(1.08);
  box-shadow: 0 6px 20px rgba(37, 99, 235, 0.4);
}
.as-send-btn:disabled {
  background: #e2e8f0; color: #94a3b8;
  box-shadow: none; transform: none; cursor: not-allowed;
}
.as-send-btn.loading {
  background: #93c5fd;
}

.as-send-dots { display: flex; gap: 3px; align-items: center; }
.as-dot {
  width: 4px; height: 4px; border-radius: 50%; background: #fff;
  animation: as-bounce 1.4s infinite ease-in-out both;
}
.as-dot:nth-child(1) { animation-delay: -0.32s; }
.as-dot:nth-child(2) { animation-delay: -0.16s; }
@keyframes as-bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}
</style>
