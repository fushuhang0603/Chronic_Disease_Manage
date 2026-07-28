<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getDoctorPatients } from '../api/user.js'

const router = useRouter()
const loading = ref(true)
const patients = ref([])
const total = ref(0)

const avatarColors = [
  { bg: 'linear-gradient(135deg, #fb923c, #f97316)' },
  { bg: 'linear-gradient(135deg, #f59e0b, #d97706)' },
  { bg: 'linear-gradient(135deg, #f87171, #dc2626)' },
  { bg: 'linear-gradient(135deg, #a78bfa, #7c3aed)' },
  { bg: 'linear-gradient(135deg, #38bdf8, #0ea5e9)' },
  { bg: 'linear-gradient(135deg, #34d399, #059669)' },
]
function avatarColor(id) { return avatarColors[(id || 0) % avatarColors.length].bg }
function getInitial(name) { return (name || '患')[0] }

async function fetchData() {
  loading.value = true
  try {
    const data = await getDoctorPatients()
    patients.value = data || {}
    total.value = Object.keys(patients.value).length
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function goChat(patient) {
  router.push({
    name: 'DoctorConsultation',
    params: { patientId: patient.patientId },
    query: { patientName: patient.patientName }
  })
}

onMounted(fetchData)
</script>

<template>
  <div class="dp-root">
    <!-- 欢迎区 -->
    <div class="welcome-card">
      <div class="welcome-left">
        <h2 class="welcome-title">我的患者</h2>
        <p class="welcome-desc">与您的患者在线沟通，提供健康指导</p>
      </div>
      <div class="welcome-stats">
        <div class="wstat-item">
          <span class="wstat-num">{{ total }}</span>
          <span class="wstat-unit">位患者</span>
        </div>
      </div>
    </div>

    <!-- 患者列表 -->
    <div class="patient-list" v-loading="loading">
      <div
        v-for="(p, patientId) in patients"
        :key="patientId"
        class="patient-card"
        @click="goChat(p)"
      >
        <div class="pc-avatar" :style="{ background: avatarColor(p.patientId) }">
          {{ getInitial(p.patientName) }}
        </div>

        <div class="pc-body">
          <div class="pc-top">
            <span class="pc-name">{{ p.patientName }}</span>
            <span class="pc-dot" :class="{ online: p.online }"></span>
          </div>
          <p class="pc-latest" v-if="p.latestContent">{{ p.latestContent }}</p>
          <p class="pc-latest empty" v-else>暂无消息</p>
        </div>

        <div class="pc-right">
          <span class="pc-time" v-if="p.latestTime">{{ p.latestTime }}</span>
          <span class="pc-unread" v-if="p.unreadCount > 0">{{ p.unreadCount > 99 ? '99+' : p.unreadCount }}</span>
        </div>
      </div>

      <div v-if="Object.keys(patients).length === 0 && !loading" class="empty-state">
        <div class="empty-icon">
          <el-icon :size="40"><UserFilled /></el-icon>
        </div>
        <p class="empty-title">暂无患者</p>
        <p class="empty-sub">暂未有患者绑定您为主治医生</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dp-root {
  max-width: 800px;
  margin: 0 auto;
  width: 100%;
  min-height: 100%;
  padding: 24px 24px 40px;
  display: flex;
  flex-direction: column;
  gap: 22px;
  box-sizing: border-box;
}

/* ==================== 欢迎区 ==================== */
.welcome-card {
  display: flex; align-items: center; justify-content: space-between;
  padding: 28px 32px; gap: 20px;
  border-radius: 20px;
  background: #fff;
  border: 1px solid #fde68a;
  box-shadow: 0 2px 12px rgba(249,115,22,0.08);
}
.welcome-left { flex: 1; }
.welcome-title {
  font-size: 24px; font-weight: 800; color: #7c2d12; margin: 0 0 6px 0;
}
.welcome-desc {
  font-size: 14px; color: #9a3412; margin: 0; line-height: 1.5;
}
.welcome-stats { flex-shrink: 0; }
.wstat-item {
  text-align: center;
  background: rgba(255,255,255,0.65);
  border-radius: 16px;
  padding: 14px 28px;
  border: 1px solid #fcd34d;
}
.wstat-num { font-size: 32px; font-weight: 800; color: #c2410c; display: block; line-height: 1; }
.wstat-unit { font-size: 13px; color: #9a3412; margin-top: 4px; display: block; }

/* ==================== 患者列表 ==================== */
.patient-list { display: flex; flex-direction: column; gap: 8px; }

.patient-card {
  display: flex; align-items: center; gap: 14px;
  background: #fff; border-radius: 16px;
  padding: 16px 20px;
  border: 1px solid #fef3c7;
  cursor: pointer;
  transition: all 0.2s;
}
.patient-card:hover {
  border-color: #fbbf24;
  box-shadow: 0 4px 16px rgba(249,115,22,0.08);
  transform: translateY(-1px);
}

.pc-avatar {
  width: 48px; height: 48px;
  border-radius: 14px;
  color: #fff; font-size: 18px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 3px 8px rgba(0,0,0,0.1);
}

.pc-body { flex: 1; min-width: 0; }
.pc-top { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.pc-name { font-size: 15px; font-weight: 600; color: #431407; }
.pc-dot {
  width: 8px; height: 8px; border-radius: 50%;
  background: #d6d3d1; flex-shrink: 0;
}
.pc-dot.online { background: #22c55e; box-shadow: 0 0 6px rgba(34,197,94,0.4); }
.pc-latest {
  font-size: 13px; color: #78716c; margin: 0;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.pc-latest.empty { color: #d6d3d1; font-style: italic; }

.pc-right {
  display: flex; flex-direction: column; align-items: flex-end; gap: 6px;
  flex-shrink: 0;
}
.pc-time { font-size: 12px; color: #a8a29e; }
.pc-unread {
  background: #ef4444; color: #fff;
  font-size: 11px; font-weight: 700;
  min-width: 20px; height: 20px; line-height: 20px;
  text-align: center; border-radius: 10px;
  padding: 0 6px;
}

/* ==================== 空状态 ==================== */
.empty-state {
  display: flex; flex-direction: column; align-items: center;
  padding: 80px 0; gap: 8px;
}
.empty-icon {
  width: 80px; height: 80px; border-radius: 50%;
  background: #fef3c7;
  display: flex; align-items: center; justify-content: center;
  color: #d6d3d1;
}
.empty-title { font-size: 16px; font-weight: 600; color: #a8a29e; margin: 0; }
.empty-sub { font-size: 13px; color: #d6d3d1; margin: 0; }
</style>
