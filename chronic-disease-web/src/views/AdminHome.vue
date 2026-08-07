<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminDashboard, getUserCount } from '../api/user.js'

const stats = reactive([
  { label: '患者总数', val: 0, icon: '👥', color: '#6366f1' },
  { label: '医生总数', val: 0, icon: '🩺', color: '#06b6d4' },
  { label: '异常指标', val: 0, icon: '⚠️', color: '#f43f5e' },
])

const abnormalRecords = ref([])
const loading = ref(false)

const abnormalTag = { 1: '偏高', 2: '偏低' }
const abnormalCls = { 1: 'tag-high', 2: 'tag-low' }

function fmtTime(v) {
  if (!v) return ''
  return v.replace('T', ' ').substring(5, 16)
}

async function loadCounts() {
  try {
    const res = await getUserCount()
    stats[0].val = res.patientCount || 0
    stats[1].val = res.doctorCount || 0
  } catch { /* ok */ }
}

async function loadDashboard() {
  loading.value = true
  try {
    const res = await getAdminDashboard()
    stats[2].val = res.abnormalPatientCount || 0
    abnormalRecords.value = res.latestRecords || []
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally { loading.value = false }
}

onMounted(() => { loadCounts(); loadDashboard() })
</script>

<template>
  <div class="home-root">
    <!-- 顶部统计 -->
    <div class="top-stats">
      <div v-for="s in stats" :key="s.label" class="stat-item" :style="{ '--ac': s.color }">
        <span class="stat-emoji">{{ s.icon }}</span>
        <div>
          <div class="stat-num">{{ s.val }}</div>
          <div class="stat-lbl">{{ s.label }}</div>
        </div>
      </div>
    </div>

    <!-- 异常指标表格 -->
    <div class="panel">
      <div class="panel-hd">
        <span class="hd-dot" style="background:#f43f5e"></span>
        <span class="hd-title">最新异常指标</span>
        <span class="hd-tip" v-if="abnormalRecords.length">共 {{ abnormalRecords.length }} 条</span>
      </div>

      <el-table :data="abnormalRecords" v-loading="loading" size="small" class="dash-table">
        <el-table-column prop="patientName" label="患者" width="100" />
        <el-table-column prop="indexCode" label="指标" width="110" />
        <el-table-column label="数值" width="80" align="center">
          <template #default="{ row }">
            <span :class="['val', abnormalCls[row.isAbnormal]]">{{ row.indexValue }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="70" align="center">
          <template #default="{ row }">
            <span :class="['tag', abnormalCls[row.isAbnormal]]">{{ abnormalTag[row.isAbnormal] }}</span>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="130" align="center">
          <template #default="{ row }">{{ fmtTime(row.recordTime) }}</template>
        </el-table-column>
      </el-table>

      <div v-if="!abnormalRecords.length && !loading" class="empty">暂无异常指标记录</div>
    </div>
  </div>
</template>

<style scoped>
.home-root { display: flex; flex-direction: column; gap: 16px; }

/* 顶部统计卡片 */
.top-stats {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px;
}
.stat-item {
  background: #fff; border-radius: 14px; padding: 16px 20px;
  display: flex; align-items: center; gap: 14px;
  border-top: 3px solid var(--ac);
  box-shadow: 0 1px 8px rgba(0,0,0,.04);
}
.stat-emoji { font-size: 28px; line-height: 1; }
.stat-num { font-size: 24px; font-weight: 800; color: #1e293b; }
.stat-lbl { font-size: 12px; color: #94a3b8; margin-top: 2px; }

/* 内容面板 */
.panel {
  background: #fff; border-radius: 14px;
  box-shadow: 0 1px 8px rgba(0,0,0,.04);
  padding: 16px 20px;
}
.panel-hd {
  display: flex; align-items: center; gap: 8px;
  padding-bottom: 12px; border-bottom: 1px solid #f1f5f9;
  margin-bottom: 8px;
}
.hd-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.hd-title { font-size: 14px; font-weight: 600; color: #334155; }
.hd-tip { font-size: 12px; color: #94a3b8; margin-left: auto; }

/* 表格 */
.dash-table { font-size: 13px; }
.dash-table :deep(th) { background: #f8fafc; color: #64748b; font-weight: 600; padding: 8px 0; }
.dash-table :deep(td) { padding: 7px 0; }

.val { font-weight: 700; }
.val.tag-high { color: #e11d48; }
.val.tag-low  { color: #2563eb; }

.tag {
  display: inline-block; padding: 0 8px; border-radius: 10px;
  font-size: 11px; font-weight: 600;
}
.tag.tag-high { background: #ffe4e6; color: #be123c; }
.tag.tag-low  { background: #dbeafe; color: #1d4ed8; }

.empty { text-align: center; padding: 32px 0; color: #94a3b8; font-size: 13px; }
</style>
