/**
 * 智能医助 API — 调用 chronic-disease-agent 的对话接口（SSE 流式）
 */

const AGENT_BASE = '/api/agent'

/**
 * 患者端智能医助对话（SSE 流式）
 * @param {string} userInput - 用户输入
 * @param {object} callbacks - { onToken, onDone, onError }
 * @returns {AbortController} 用于取消请求
 */
export function patientChatStream(userInput, callbacks) {
  const controller = new AbortController()
  const url = `${AGENT_BASE}/patient/chat?userInput=${encodeURIComponent(userInput)}`

  fetch(url, {
    signal: controller.signal,
    headers: {
      Authorization: sessionStorage.getItem('token') || '',
    },
  })
    .then(async (response) => {
      if (!response.ok) {
        callbacks.onError?.(new Error(`请求失败: ${response.status}`))
        return
      }
      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      while (true) {
        const { done, value } = await reader.read()
        if (done) break
        const chunk = decoder.decode(value, { stream: true })
        callbacks.onToken?.(chunk)
      }
      callbacks.onDone?.()
    })
    .catch((err) => {
      if (err.name !== 'AbortError') {
        callbacks.onError?.(err)
      }
    })

  return controller
}
