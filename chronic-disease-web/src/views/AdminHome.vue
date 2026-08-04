<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Search, Warning } from '@element-plus/icons-vue'
import { getAdminDashboard, getUserCount } from '../api/user.js'

const statCards = ref([
  { label: '患者总数', val: 0, unit: '人', icon: User, color: '#f97316' },
  { label: '医生总数', val: 0, unit: '人', icon: User, color: '#06b6d4' },
  { label: '异常指标', val: 0, unit: '人', icon: Warning, color: '#ef4444' },
])

const abnormalRecords = ref([])
const loading = ref(false)

const abnormalLabel = { 1: '偏高', 2: '偏低' }
const abnormalColor = { 1: '#dc2626', 2: '#2563eb' }

function fmtTime(val) {
  if (!val) return ''
  return val.replace('T', ' ').substring(0, 16)
}

async function loadCounts() {
  try {
    const res = await getUserCount()
    statCards.value[0].val = res.patientCount || 0
    statCards.value[1].val = res.doctorCount || 0
  } catch { /* 兼容老版本 */ }
}

async function loadDashboard() {
  loading.value = true
  try {
    const res = await getAdminDashboard()
    statCards.value[2].val = res.abnormalPatientCount || 0
    abnormalRecords.value = res.latestRecords || []
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCounts()
  loadDashboard()
})
</script>

<template>
  <div class="ah-root">
    <!-- 统计卡片 -->
    <div class="stat-row">
      <div v-for="c in statCards" :key="c.label" class="stat-card" :style="{ '--accent': c.color }">
        <div class="stat-icon">
          <el-icon :size="24"><component :is="c.icon" /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-label">{{ c.label }}</div>
          <div class="stat-value">
            <span class="val-num">{{ c.val }}</span>
            <span class="val-unit">{{ c.unit }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 最新异常指标 -->
    <div class="um-card">
      <div class="card-title-row">
        <span class="card-icon" style="background:linear-gradient(135deg,#ef4444,#dc2626)">
          <el-icon :size="18"><Warning /></el-icon>
        </span>
        <span class="card-label">最新异常指标</span>
        <span class="card-tip" v-if="abnormalRecords.length > 0">最近 10 条</span>
      </div>

      <el-table :data="abnormalRecords" v-loading="loading" stripe border class="um-table">
        <el-table-column prop="patientName" label="患者" width="120" align="center" />
        <el-table-column prop="indexCode" label="指标编码" width="120" align="center" />
        <el-table-column label="数值" width="100" align="center">
          <template #default="{ row }">
            <span :style="{ color: abnormalColor[row.isAbnormal] || '#431407', fontWeight: 700 }">
              {{ row.indexValue }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <span :class="['ab-tag', row.isAbnormal === 1 ? 'high' : 'low']">
              {{ abnormalLabel[row.isAbnormal] }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="记录时间" width="160" align="center">
          <template #default="{ row }">{{ fmtTime(row.recordTime) }}</template>
        </el-table-column>
      </el-table>

      <div v-if="abnormalRecords.length === 0 && !loading" class="empty-state">
        <el-icon :size="40" color="#d6d3d1"><Search /></el-icon>
        <p>暂无异常指标记录</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ah-root { display: flex; flex-direction: column; gap: 24px; }

.stat-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.stat-card {
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 4px 24px rgba(249,115,22,0.06);
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  border-left: 4px solid var(--accent);
  transition: box-shadow 0.3s, transform 0.2s;
}
.stat-card:hover { box-shadow: 0 6px 30px rgba(249,115,22,0.12); transform: translateY(-2px); }
.stat-icon {
  width: 52px; height: 52px; border-radius: 14px;
  background: color-mix(in srgb, var(--accent) 12%, #fff);
  color: var(--accent);
  display: flex; align-items: center; justify-content: center;
}
.stat-body { flex: 1; }
.stat-label { font-size: 13px; color: #78716c; margin-bottom: 4px; font-weight: 500; }
.stat-value { display: flex; align-items: baseline; gap: 4px; }
.val-num { font-size: 32px; font-weight: 800; color: #431407; }
.val-unit { font-size: 14px; color: #a8a29e; }

.um-card {
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 4px 24px rgba(249,115,22,0.06);
  padding: 24px 28px;
}

.card-title-row {
  display: flex; align-items: center; gap: 10px; margin-bottom: 20px;
}
.card-icon {
  width: 34px; height: 34px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.card-icon .el-icon { color: #fff; }
.card-label { font-size: 16px; font-weight: 700; color: #7c2d12; }
.card-tip { font-size: 13px; color: #a8a29e; margin-left: auto; }

.um-table { margin-bottom: 0; }
.um-table :deep(th) { background: #fffbeb; color: #78716c; font-weight: 600; font-size: 13px; }
.um-table :deep(td) { font-size: 13px; color: #431407; }

.ab-tag {
  display: inline-block; padding: 2px 10px; border-radius: 20px;
  font-size: 12px; font-weight: 600;
}
.ab-tag.high { background: #fef2f2; color: #dc2626; }
.ab-tag.low { background: #eff6ff; color: #2563eb; }

.empty-state {
  display: flex; flex-direction: column; align-items: center;
  padding: 40px 0; gap: 8px; color: #a8a29e; font-size: 14px;
}
</style>
