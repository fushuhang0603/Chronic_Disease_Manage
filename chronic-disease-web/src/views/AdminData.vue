<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getUserHealthRecords, getDictPage } from '../api/user.js'

// ====== 指标字典 ======
const indicators = ref([])
async function loadIndicators() {
  try {
    const data = await getDictPage({ pageNum: 1, pageSize: 100, termType: 'indicator' })
    indicators.value = (data.records || []).filter(d => d.status === 1).map(d => ({
      code: d.indexCode, name: d.indexName, unit: d.unit
    }))
  } catch { /* ignore */ }
}

// ====== 患者搜索 ======
const patientSearch = ref('')
const selectedPatient = ref(null)
const patientSearching = ref(false)

async function searchPatient() {
  const name = patientSearch.value.trim()
  if (!name) return
  patientSearching.value = true
  try {
    const data = await getUserHealthRecords({ patientName: name, pageNum: 1, pageSize: 1000 })
    if (!data.records || data.records.length === 0) {
      ElMessage.warning('未找到该患者的数据')
      return
    }
    selectedPatient.value = { patientName: name }
    buildChartData(data.records)
  } catch (e) {
    ElMessage.error(e.message || '查询失败')
  } finally { patientSearching.value = false }
}

function buildChartData(records) {
  const grouped = {}
  const sorted = records.sort((a, b) => (a.recordTime || '').localeCompare(b.recordTime || ''))
  for (const r of sorted) {
    if (!grouped[r.indexCode]) grouped[r.indexCode] = []
    grouped[r.indexCode].push(r)
  }
  chartData.value = grouped
  const codes = Object.keys(grouped)
  selectedIndicator.value = codes.length > 0 ? codes[0] : null
  nextTick(renderChart)
}

function clearPatient() {
  selectedPatient.value = null
  chartData.value = {}
  selectedIndicator.value = null
  trendDays.value = 30
  if (chartInstance) { chartInstance.dispose(); chartInstance = null }
  loadAllRecords(1)
}

// ====== 全部数据模式（默认） ======
const allLoading = ref(false)
const allRecords = ref([])
const allTotal = ref(0)
const allPage = ref(1)
const allPageSize = ref(10)
const allIndexCode = ref('')

async function loadAllRecords(page) {
  allLoading.value = true
  try {
    const params = { pageNum: page || allPage.value, pageSize: allPageSize.value }
    if (allIndexCode.value) params.indexCode = allIndexCode.value
    const data = await getUserHealthRecords(params)
    allRecords.value = data.records || []
    allTotal.value = data.total || 0
    allPage.value = page || allPage.value
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally { allLoading.value = false }
}

// ====== 图表 ======
const chartLoading = ref(false)
const chartData = ref({})
const selectedIndicator = ref(null)
const trendDays = ref(30)
let chartInstance = null

function getUnit(code) {
  const ind = indicators.value.find(i => i.code === code)
  return ind ? ind.unit : ''
}

function renderChart() {
  const code = selectedIndicator.value
  if (!code) return
  const records = (chartData.value[code] || []).filter(r => {
    if (trendDays.value <= 0) return true
    const cutoff = new Date()
    cutoff.setDate(cutoff.getDate() - trendDays.value)
    return new Date(r.recordTime) >= cutoff
  })
  const dom = document.getElementById('trend-chart')
  if (!dom) return
  if (chartInstance) chartInstance.dispose()
  chartInstance = echarts.init(dom)

  const dates = records.map(r => (r.recordTime || '').substring(0, 10))
  const values = records.map(r => Number(r.indexValue || 0))
  const unit = getUnit(code)

  chartInstance.setOption({
    tooltip: { trigger: 'axis', formatter: p => {
      if (!p || p.length === 0) return ''
      return `${p[0].axisValue}<br/>${p[0].value} ${unit}`
    }},
    grid: { top: 20, right: 30, bottom: 30, left: 60 },
    xAxis: { type: 'category', data: dates, axisLabel: { fontSize: 11, color: '#94a3b8' } },
    yAxis: {
      type: 'value', name: unit,
      nameTextStyle: { fontSize: 11, color: '#94a3b8' },
      axisLabel: { fontSize: 11, color: '#94a3b8' }
    },
    series: [{
      type: 'line', data: values, smooth: true,
      symbol: 'circle', symbolSize: 5,
      lineStyle: { color: '#3b82f6', width: 2 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(59,130,246,0.25)' },
          { offset: 1, color: 'rgba(59,130,246,0.02)' }
        ])
      },
      itemStyle: { color: '#3b82f6' }
    }]
  })
}

watch(selectedIndicator, () => nextTick(renderChart))
watch(trendDays, () => nextTick(renderChart))

// ====== 患者详情表格 ======
const patientTablePage = ref(1)
const patientTableSize = ref(10)

const patientFilteredRecords = computed(() => {
  if (!selectedIndicator.value) return []
  let records = (chartData.value[selectedIndicator.value] || [])
  if (trendDays.value > 0) {
    const cutoff = new Date()
    cutoff.setDate(cutoff.getDate() - trendDays.value)
    records = records.filter(r => new Date(r.recordTime) >= cutoff)
  }
  return records.slice().reverse()
})

const patientPagedRecords = computed(() => {
  const from = (patientTablePage.value - 1) * patientTableSize.value
  return patientFilteredRecords.value.slice(from, from + patientTableSize.value)
})

watch(selectedIndicator, () => { patientTablePage.value = 1 })
watch(trendDays, () => { patientTablePage.value = 1 })

function handleAllPageChange(p) { loadAllRecords(p) }
function handleAllIndexFilter() { loadAllRecords(1) }
function handlePatientTablePage(p) { patientTablePage.value = p }

function fmtTime(t) { return t ? t.replace('T', ' ').substring(0, 16) : '-' }
function indName(code) {
  const ind = indicators.value.find(i => i.code === code)
  return ind ? ind.name : code
}

onMounted(() => {
  loadIndicators()
  loadAllRecords(1)
  window.addEventListener('resize', () => chartInstance?.resize())
})
</script>

<template>
  <div class="admin-data">
    <!-- 搜索栏（仅选中患者时显示） -->
    <div class="search-card" v-if="selectedPatient">
      <div class="search-row">
        <div class="search-box">
          <div class="selected-patient">
            <span class="sp-name">{{ selectedPatient.patientName }}</span>
            <button class="sp-close" @click="clearPatient">✕ 返回全部数据</button>
          </div>
        </div>
      </div>
    </div>

    <!-- ========== 患者详情：图表 + 明细 ========== -->
    <template v-if="selectedPatient">
      <!-- 指标选择 + 时间范围 -->
      <div class="toolbar-card" v-if="Object.keys(chartData).length > 0">
        <div class="tb-left">
          <span class="tb-label">指标：</span>
          <el-select v-model="selectedIndicator" placeholder="选择指标" size="default" style="width: 180px">
            <el-option v-for="code in Object.keys(chartData)" :key="code" :label="indName(code)" :value="code" />
          </el-select>
        </div>
        <div class="tb-right">
          <span class="tb-label">范围：</span>
          <button :class="['tb-chip', { active: trendDays === 7 }]" @click="trendDays = 7">近7天</button>
          <button :class="['tb-chip', { active: trendDays === 14 }]" @click="trendDays = 14">近14天</button>
          <button :class="['tb-chip', { active: trendDays === 30 }]" @click="trendDays = 30">近30天</button>
          <button :class="['tb-chip', { active: trendDays === 0 }]" @click="trendDays = 0">全部</button>
        </div>
      </div>

      <!-- 图表 -->
      <div v-loading="chartLoading" class="chart-card">
        <div v-if="selectedIndicator" id="trend-chart" class="chart-wrap"></div>
        <div v-else class="empty-msg">请选择一项指标查看趋势图</div>
      </div>

      <!-- 记录明细表格 -->
      <div class="table-card" v-if="selectedIndicator">
        <div class="card-title">{{ indName(selectedIndicator) }} 记录明细</div>
        <table class="data-table" v-if="patientPagedRecords.length > 0">
          <thead>
            <tr>
              <th>记录时间</th>
              <th>数值</th>
              <th>单位</th>
              <th>备注</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in patientPagedRecords" :key="r.id">
              <td>{{ fmtTime(r.recordTime) }}</td>
              <td><span class="value-num">{{ r.indexValue }}</span></td>
              <td>{{ r.unit || '-' }}</td>
              <td>{{ r.remark || '-' }}</td>
            </tr>
          </tbody>
        </table>
        <div v-else class="empty-msg">暂无记录</div>

        <div v-if="patientFilteredRecords.length > patientTableSize" class="page-wrap">
          <el-pagination background layout="prev, pager, next" :total="patientFilteredRecords.length"
            :page-size="patientTableSize" :current-page="patientTablePage" @current-change="handlePatientTablePage" />
        </div>
      </div>
    </template>

    <!-- ========== 全部数据列表（默认） ========== -->
    <template v-else>
      <div class="toolbar-card">
        <div class="filter-row">
          <el-input
            v-model="patientSearch"
            placeholder="输入患者姓名搜索"
            :prefix-icon="Search"
            clearable
            size="default"
            style="width: 240px"
            @keyup.enter="searchPatient"
          />
          <el-select v-model="allIndexCode" @change="handleAllIndexFilter" clearable placeholder="全部指标" size="default" style="width: 180px">
            <el-option label="全部指标" value="" />
            <el-option v-for="ind in indicators" :key="ind.code" :label="ind.name" :value="ind.code" />
          </el-select>
          <el-button type="primary" :icon="Search" size="default" @click="searchPatient" :loading="patientSearching">搜索</el-button>
        </div>
      </div>

      <!-- 全部数据表格 -->
      <div v-loading="allLoading" class="table-card">
        <div class="card-title">全部患者指标记录</div>
        <table class="data-table" v-if="allRecords.length > 0">
          <thead>
            <tr>
              <th>患者姓名</th>
              <th>指标名称</th>
              <th>数值</th>
              <th>单位</th>
              <th>记录时间</th>
              <th>备注</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in allRecords" :key="r.id">
              <td><span class="patient-name">{{ r.patientName || '-' }}</span></td>
              <td>{{ indName(r.indexCode) }}</td>
              <td><span class="value-num">{{ r.indexValue }}</span></td>
              <td>{{ r.unit || '-' }}</td>
              <td>{{ fmtTime(r.recordTime) }}</td>
              <td>{{ r.remark || '-' }}</td>
            </tr>
          </tbody>
        </table>
        <div v-else class="empty-msg">暂无数据</div>

        <div v-if="allTotal > allPageSize" class="page-wrap">
          <el-pagination background layout="prev, pager, next" :total="allTotal"
            :page-size="allPageSize" :current-page="allPage" @current-change="handleAllPageChange" />
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.admin-data {
  display: flex; flex-direction: column; gap: 16px;
  max-width: 1100px; margin: 0 auto; width: 100%; padding-bottom: 32px;
}

/* 搜索 */
.search-card {
  background: #fff; border-radius: 16px; padding: 20px 24px;
  border: 1px solid #f1f5f9; box-shadow: 0 1px 4px rgba(0,0,0,0.03);
}
.search-row { position: relative; }
.search-box {
  display: flex; align-items: center; gap: 8px;
  height: 44px; padding: 0 14px; border-radius: 12px;
  border: 1.5px solid #e2e8f0; background: #fafbfc;
}
.filter-row {
  display: flex; align-items: center; gap: 12px;
}
.search-wrap { position: relative; }
.search-box:focus-within { border-color: #3b82f6; background: #fff; }
.search-icon { color: #94a3b8; flex-shrink: 0; }
.search-input {
  width: 100%; border: none; background: transparent; outline: none;
  font-size: 14px; color: #1e293b;
}
.search-input::placeholder { color: #94a3b8; }

.selected-patient {
  display: flex; align-items: center; gap: 8px; width: 100%;
}
.sp-name { font-size: 14px; font-weight: 700; color: #1e40af; }
.sp-info { font-size: 12px; color: #64748b; }
.sp-close {
  margin-left: auto; padding: 4px 12px; border-radius: 7px;
  border: 1px solid #fecaca; background: #fef2f2; color: #dc2626;
  font-size: 12px; cursor: pointer; white-space: nowrap;
}
.sp-close:hover { background: #fee2e2; }

/* 患者下拉 */
.patient-dropdown {
  position: absolute; top: 100%; left: 0; z-index: 10;
  margin-top: 6px; width: 100%;
  border: 1.5px solid #e2e8f0; border-radius: 12px;
  background: #fff; box-shadow: 0 8px 24px rgba(0,0,0,0.1);
  overflow: hidden;
}
.pd-item {
  padding: 12px 16px; display: flex; align-items: center; gap: 12px;
  cursor: pointer; transition: background 0.1s; border-bottom: 1px solid #f8fafc;
}
.pd-item:last-child { border-bottom: none; }
.pd-item:hover { background: #eff6ff; }
.pd-name { font-weight: 600; color: #1e293b; font-size: 14px; }
.pd-phone { font-size: 12px; color: #94a3b8; }

/* 工具栏 */
.toolbar-card {
  background: #fff; border-radius: 16px; padding: 14px 20px;
  border: 1px solid #f1f5f9; box-shadow: 0 1px 4px rgba(0,0,0,0.03);
  display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 10px;
}
.tb-left, .tb-right { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
.tb-label { font-size: 12px; color: #94a3b8; font-weight: 600; }
.tb-chip {
  padding: 4px 12px; border-radius: 7px; font-size: 12px; font-weight: 600;
  cursor: pointer; border: 1.5px solid #e2e8f0; background: #fff; color: #475569;
  transition: all 0.15s;
}
.tb-chip:hover { border-color: #3b82f6; color: #3b82f6; }
.tb-chip.active { background: #eff6ff; border-color: #3b82f6; color: #2563eb; }

/* 图表 */
.chart-card {
  background: #fff; border-radius: 16px; padding: 20px 24px;
  border: 1px solid #f1f5f9; box-shadow: 0 1px 4px rgba(0,0,0,0.03);
  min-height: 280px;
}
.chart-wrap { width: 100%; height: 300px; }

/* 表格 */
.table-card {
  background: #fff; border-radius: 16px; padding: 20px 24px;
  border: 1px solid #f1f5f9; box-shadow: 0 1px 4px rgba(0,0,0,0.03);
}
.card-title { font-size: 14px; font-weight: 700; color: #1e293b; margin-bottom: 12px; }

.data-table { width: 100%; border-collapse: collapse; }
.data-table th {
  text-align: left; padding: 10px 14px; font-size: 11px; font-weight: 700;
  color: #94a3b8; text-transform: uppercase; letter-spacing: 0.5px;
  border-bottom: 1px solid #f1f5f9; background: #fafbfc;
}
.data-table td {
  padding: 10px 14px; font-size: 13px; color: #334155;
  border-bottom: 1px solid #f8fafc;
}
.data-table tbody tr:hover { background: #f8fafc; }
.value-num { font-weight: 600; color: #1e293b; }
.patient-name { font-weight: 600; color: #1e40af; }

.page-wrap { display: flex; justify-content: center; margin-top: 14px; }

.empty-msg {
  display: flex; align-items: center; justify-content: center;
  padding: 60px 20px; color: #94a3b8; font-size: 14px;
}
</style>
