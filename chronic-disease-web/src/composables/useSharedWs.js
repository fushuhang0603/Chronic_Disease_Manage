import { ref } from 'vue'

/**
 * 全局共享 WebSocket 单例 —— 整个应用只建一个连接，
 * Layout.vue 和 DoctorChat.vue 等组件通过 onMessage() 各自监听感兴趣的消息类型。
 */
let ws = null
const listeners = new Set()
const connected = ref(false)

export function useSharedWs() {

  /** 建立 WebSocket 连接（幂等：已连接则跳过） */
  function connect(token) {
    if (ws && (ws.readyState === WebSocket.OPEN || ws.readyState === WebSocket.CONNECTING)) {
      return
    }
    const wsUrl = import.meta.env.DEV
      ? `ws://localhost:9000/ws/chat?token=${encodeURIComponent(token)}`
      : `ws://${location.host}/ws/chat?token=${encodeURIComponent(token)}`

    ws = new WebSocket(wsUrl)
    ws.onopen = () => { connected.value = true }
    ws.onmessage = (e) => {
      try {
        const msg = JSON.parse(e.data)
        listeners.forEach(fn => { try { fn(msg) } catch {} })
      } catch { /* ignore */ }
    }
    ws.onclose = () => { connected.value = false; ws = null }
    ws.onerror = () => { connected.value = false }
  }

  /** 关闭连接 */
  function disconnect() {
    if (ws) {
      ws.close()
      ws = null
      connected.value = false
    }
  }

  /** 发送 JSON 消息，返回是否发送成功 */
  function send(data) {
    if (ws && ws.readyState === WebSocket.OPEN) {
      const payload = typeof data === 'string' ? data : JSON.stringify(data)
      ws.send(payload)
      return true
    }
    return false
  }

  /** 注册消息监听器，返回取消注册函数 */
  function onMessage(fn) {
    listeners.add(fn)
    return () => listeners.delete(fn)
  }

  return { connect, disconnect, send, onMessage, connected }
}
