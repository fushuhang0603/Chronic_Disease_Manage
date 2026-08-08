<template>
  <div class="abnormal-container">
    <div class="abnormal-header">
      <div class="header-left">
        <h2 class="page-title">异常预警监控</h2>
        <span class="page-desc">实时查看所有患者的异常指标记录，及时干预</span>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <div class="search-input-wrap">
        <el-icon class="search-icon"><Search /></el-icon>
        <input
          v-model="patientSearch"
          placeholder="输入患者姓名筛选..."
          @keyup.enter="search"
          class="search-input"
        />
      </div>
      <button class="btn-search" @click="search">搜索</button>
      <button class="btn-reset" @click="resetSearch">重置</button>
    </div>

    <!-- 卡片网格 -->
    <div v-if="records.length > 0" class="abnormal-grid">
      <div
        v-for="r in records"
        :key="r.id"
        :class="['abnormal-card', r.isAbnormal === 1 ? 'card-high' : 'card-low']"
      >
        <div class="card-top">
          <div class="card-patient">{{ r.patientName || '-' }}</div>
          <span :class="['badge', r.isAbnormal === 1 ? 'badge-high' : 'badge-low']">
            {{ r.isAbnormal === 1 ? '偏高' : '偏低' }}
          </span>
        </div>
        <div class="card-value-row">
          <span class="card-value">{{ r.indexValue }}</span>
          <span class="card-unit">{{ r.unit || '' }}</span>
        </div>
        <div class="card-index-name">{{ indName(r.indexCode) }}</div>
        <div class="card-bottom">
          <span class="card-time">{{ formatDate(r.recordTime) }}</span>
          <button class="card-link" @click="goToPatient(r)">查看趋势 →</button>
        </div>
      </div>
    </div>

    <div v-else-if="!loading" class="empty-msg">暂无异常指标记录</div>
    <div v-if="loading" class="empty-msg">加载中...</div>

    <!-- 分页 -->
    <div v-if="total > pageSize" class="pagination-wrap">
      <button
        :disabled="pageNum <= 1"
        @click="pageNum--; fetchData()"
        class="btn-page"
      >上一页</button>
      <span class="page-info">第 {{ pageNum }} / {{ totalPages }} 页（共 {{ total }} 条）</span>
      <button
        :disabled="pageNum >= totalPages"
        @click="pageNum++; fetchData()"
        class="btn-page"
      >下一页</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAbnormalRecords, getDictPage } from '../api/user'

const router = useRouter()
const records = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(12)
const patientSearch = ref('')
const loading = ref(false)

const totalPages = computed(() => Math.ceil(total.value / pageSize.value) || 1)

// 指标字典映射：code -> name
const indicatorMap = ref({})

onMounted(async () => {
  await loadIndicators()
  fetchData()
})

async function loadIndicators() {
  try {
    const res = await getDictPage({ pageNum: 1, pageSize: 100, termType: 'indicator' })
    const list = res.records || []
    list.filter(d => d.status === 1).forEach(d => {
      indicatorMap.value[d.indexCode] = d.indexName
    })
  } catch { /* ignore */ }
}

function indName(code) {
  return indicatorMap.value[code] || code
}

function formatDate(t) {
  if (!t) return '-'
  const s = String(t)
  return s.length >= 16 ? s.substring(0, 16).replace('T', ' ') : s
}

async function fetchData() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (patientSearch.value.trim()) {
      params.patientName = patientSearch.value.trim()
    }
    const res = await getAbnormalRecords(params)
    if (res.data) {
      records.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch {
    ElMessage.error('加载异常记录失败')
  } finally {
    loading.value = false
  }
}

function search() {
  pageNum.value = 1
  fetchData()
}

function resetSearch() {
  patientSearch.value = ''
  pageNum.value = 1
  fetchData()
}

function goToPatient(r) {
  router.push({
    path: '/admin/data',
    query: {
      patientId: r.userId,
      patientName: r.patientName,
      recordId: r.id
    }
  })
}
</script>

<style scoped>
.abnormal-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px;
}

.abnormal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
}

.page-desc {
  display: block;
  margin-top: 4px;
  font-size: 13px;
  color: #94a3b8;
}

/* 搜索栏 */
.search-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  padding: 12px 16px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.03);
}

.search-input-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #f8fafc;
}

.search-icon { color: #94a3b8; font-size: 16px; }

.search-input {
  flex: 1;
  border: none;
  outline: none;
  padding: 8px 0;
  font-size: 14px;
  background: transparent;
  color: #334155;
}

.btn-search,
.btn-reset {
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  border: none;
  cursor: pointer;
  transition: opacity 0.15s;
}

.btn-search {
  background: #3b82f6;
  color: #fff;
}

.btn-reset {
  background: #f1f5f9;
  color: #64748b;
}

.btn-search:hover,
.btn-reset:hover { opacity: 0.85; }

/* 卡片网格 */
.abnormal-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}

.abnormal-card {
  padding: 16px 18px;
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  border-left: 4px solid;
  transition: transform 0.15s, box-shadow 0.15s;
  cursor: default;
}

.abnormal-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
}

.card-high {
  border-left-color: #dc2626;
}

.card-low {
  border-left-color: #2563eb;
}

.card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.card-patient {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
}

.badge {
  padding: 2px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
}

.badge-high {
  background: #fef2f2;
  color: #dc2626;
}

.badge-low {
  background: #eff6ff;
  color: #2563eb;
}

.card-value-row {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-bottom: 4px;
}

.card-value {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
}

.card-unit {
  font-size: 13px;
  color: #94a3b8;
}

.card-index-name {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 10px;
}

.card-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 10px;
  border-top: 1px solid #f1f5f9;
}

.card-time {
  font-size: 12px;
  color: #94a3b8;
}

.card-link {
  font-size: 12px;
  color: #3b82f6;
  background: none;
  border: none;
  cursor: pointer;
  font-weight: 500;
}

.card-link:hover { text-decoration: underline; }

/* 分页 */
.pagination-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-top: 24px;
}

.btn-page {
  padding: 8px 16px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  background: #fff;
  color: #334155;
  font-size: 14px;
  cursor: pointer;
}

.btn-page:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-info {
  font-size: 13px;
  color: #94a3b8;
}

.empty-msg {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: #94a3b8;
  font-size: 14px;
}
</style>
