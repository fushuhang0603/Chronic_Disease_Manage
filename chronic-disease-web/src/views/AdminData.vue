<script setup>
import { ref, computed, onMounted, nextTick, watch, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getUserHealthRecords, getMedicineRecords, getRecheckRecords, getDictPage, getAdminIndexTrend } from '../api/user.js'

const activeTab = ref(localStorage.getItem('adminActiveTab') || 'trend')
const patientSearch = ref('')
const selectedPatient = ref(null)
const patientSearching = ref(false)

// ====== 字典 ======
const indicators = ref([])
const drugDict = ref([])
const recheckItemDict = ref([])

async function loadIndicators() {
  try {
    const data = await getDictPage({ pageNum: 1, pageSize: 100, termType: 'indicator' })
    indicators.value = (data.records || []).filter(d => d.status === 1).map(d => ({
      code: d.indexCode, name: d.indexName, unit: d.unit || ''
    }))
  } catch { /* ignore */ }
}

async function loadDrugDict() {
  try {
    const data = await getDictPage({ pageNum: 1, pageSize: 200, termType: 'medicine' })
    drugDict.value = (data.records || []).filter(d => d.status === 1).map(d => ({
      code: d.indexCode, name: d.indexName
    }))
  } catch { /* ignore */ }
}

async function loadRecheckItemDict() {
  try {
    const data = await getDictPage({ pageNum: 1, pageSize: 200, termType: 'indicator' })
    recheckItemDict.value = (data.records || []).filter(d => d.status === 1).map(d => ({
      code: d.indexCode, name: d.indexName
    }))
  } catch { /* ignore */ }
}

function indName(code) {
  const ind = indicators.value.find(i => i.code === code)
  return ind ? ind.name : code
}

function drugName(code) {
  const d = drugDict.value.find(i => i.code === code)
  return d ? d.name : code
}

function recheckItemName(code) {
  const d = recheckItemDict.value.find(i => i.code === code)
  return d ? d.name : code
}

// ====== 趋势图表 Tab — 患者卡片列表 ======
const cardLoading = ref(false)
const patientCards = ref([])
const cardSearch = ref('')

const filteredCards = computed(() => {
  if (!cardSearch.value.trim()) return patientCards.value
  const kw = cardSearch.value.trim().toLowerCase()
  return patientCards.value.filter(c => c.patientName.toLowerCase().includes(kw))
})

async function loadPatientCards() {
  cardLoading.value = true
  try {
    const data = await getUserHealthRecords({ pageSize: 1000 })
    const records = data.records || []
    // 按患者姓名分组，统计每个患者的概要信息
    const map = {}
    records.forEach(r => {
      const name = r.patientName || '未知'
      if (!map[name]) {
        map[name] = {
          patientName: name,
          recordCount: 0,
          lastRecordTime: '',
          indicatorCodes: new Set()
        }
      }
      map[name].recordCount++
      if (!map[name].lastRecordTime || r.recordTime > map[name].lastRecordTime) {
        map[name].lastRecordTime = r.recordTime
      }
      map[name].indicatorCodes.add(r.indexCode)
    })
    patientCards.value = Object.values(map)
      .map(c => ({ ...c, indicatorCount: c.indicatorCodes.size }))
      .sort((a, b) => b.lastRecordTime.localeCompare(a.lastRecordTime))
  } catch (e) {
    ElMessage.error(e.message || '加载患者列表失败')
  } finally { cardLoading.value = false }
}

function openPatientChart(patientName) {
  selectedPatient.value = { patientName }
  patientSearch.value = patientName
  fetchTrendChart()
}

// ====== 趋势图表 Tab — 图表详情 ======
const trendLoading = ref(false)
const trendData = ref({})
const selectedTrendCode = ref('')
const trendDays = ref(30)
const trendGranularity = ref('DAY')
let chartInstance = null

async function fetchTrendChart() {
  const name = selectedPatient.value?.patientName
  if (!name) return
  trendLoading.value = true
  try {
    const data = await getAdminIndexTrend({ patientName: name, days: trendDays.value, granularity: trendGranularity.value })
    trendData.value = data || {}
    const codes = Object.keys(trendData.value)
    if (codes.length > 0 && !trendData.value[selectedTrendCode.value]) {
      selectedTrendCode.value = codes[0]
    }
    nextTick(renderChart)
  } catch (e) {
    ElMessage.error(e.message || '加载趋势图失败')
  } finally { trendLoading.value = false }
}

function renderChart() {
  const code = selectedTrendCode.value
  const dom = document.getElementById('admin-trend-chart')
  if (!dom || !code) return
  const points = trendData.value[code] || []
  if (chartInstance) chartInstance.dispose()
  chartInstance = echarts.init(dom)

  const indicator = indicators.value.find(i => i.code === code)
  const unit = indicator?.unit || ''
  const labels = points.map(p => p.timeLabel)
  const values = points.map(p => Number(p.avgValue || 0))

  chartInstance.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        if (!params || params.length === 0) return ''
        const p = points[params[0].dataIndex]
        if (!p) return ''
        let html = `<b>${indicator?.name || code}</b><br/>${p.timeLabel}<br/>`
        html += `均值：<b>${p.avgValue} ${unit}</b><br/>`
        html += `最高：${p.maxValue} &nbsp; 最低：${p.minValue}<br/>`
        html += `测量次数：${p.recordCount} 次`
        if (p.details && p.details.length > 0) {
          html += '<br/><hr style="margin:4px 0;border-color:#e2e8f0"/>'
          p.details.forEach(d => {
            html += `<span style="color:#94a3b8">${(d.recordTime || '').substring(11, 16)}</span> &nbsp; ${d.indexValue} ${d.unit || unit}<br/>`
          })
        }
        return html
      }
    },
    grid: { top: 20, right: 30, bottom: 30, left: 60 },
    xAxis: { type: 'category', data: labels, axisLabel: { fontSize: 11, color: '#94a3b8' } },
    yAxis: { type: 'value', name: unit, nameTextStyle: { fontSize: 11, color: '#94a3b8' }, axisLabel: { fontSize: 11, color: '#94a3b8' }, splitLine: { lineStyle: { color: '#f1f5f9' } } },
    series: [{
      type: 'line', data: values, smooth: true, symbol: 'circle', symbolSize: 6,
      lineStyle: { color: '#3b82f6', width: 2 }, itemStyle: { color: '#3b82f6' },
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(59,130,246,0.25)' }, { offset: 1, color: 'rgba(59,130,246,0.02)' }]) },
    }],
  })
}

watch(selectedTrendCode, () => nextTick(renderChart))
watch(trendDays, () => {
  if (selectedPatient.value) fetchTrendChart()
})
watch(trendGranularity, () => {
  if (selectedPatient.value) fetchTrendChart()
})

const trendTablePage = ref(1)
const trendTableSize = ref(10)
const trendTableRecords = computed(() => {
  if (!selectedTrendCode.value) return []
  const points = trendData.value[selectedTrendCode.value] || []
  // 扁平化所有明细记录用于表格展示
  const records = []
  points.forEach(p => {
    if (p.details && p.details.length > 0) {
      p.details.forEach(d => {
        records.push({ timeLabel: p.timeLabel, ...d })
      })
    }
  })
  return records.reverse()
})
const trendPagedRecords = computed(() => {
  const from = (trendTablePage.value - 1) * trendTableSize.value
  return trendTableRecords.value.slice(from, from + trendTableSize.value)
})
watch(selectedTrendCode, () => { trendTablePage.value = 1 })
watch(trendDays, () => { trendTablePage.value = 1 })

function clearTrend() {
  selectedPatient.value = null
  trendData.value = {}
  selectedTrendCode.value = ''
  if (chartInstance) { chartInstance.dispose(); chartInstance = null }
}

// ====== 全部数据 ======
const allLoading = ref(false)
const allRecords = ref([])
const allTotal = ref(0)
const allPage = ref(1)
const allPageSize = ref(10)
const allIndexCode = ref('')
const detailRecords = ref([])

function callTabApi(params) {
  const apiMap = {
    index: getUserHealthRecords,
    medicine: getMedicineRecords,
    recheck: getRecheckRecords
  }
  return apiMap[activeTab.value](params)
}

async function loadTabAllRecords() {
  allLoading.value = true
  try {
    const params = { pageNum: allPage.value, pageSize: allPageSize.value }
    if (activeTab.value === 'index' && allIndexCode.value) params.indexCode = allIndexCode.value
    const data = await callTabApi(params)
    allRecords.value = data.records || []
    allTotal.value = data.total || 0
  } catch (e) { ElMessage.error(e.message || '加载失败')
  } finally { allLoading.value = false }
}

async function searchPatient() {
  const name = patientSearch.value.trim()
  if (!name) return
  patientSearching.value = true
  try {
    const data = await callTabApi({ patientName: name, pageNum: 1, pageSize: 1000 })
    if (!data.records || data.records.length === 0) {
      ElMessage.warning('未找到该患者的数据')
      return
    }
    selectedPatient.value = { patientName: name }
    detailRecords.value = data.records
  } catch (e) { ElMessage.error(e.message || '查询失败')
  } finally { patientSearching.value = false }
}

function clearPatient() {
  selectedPatient.value = null
  detailRecords.value = []
  loadTabAllRecords()
}

function onTabChange(tab) {
  localStorage.setItem('adminActiveTab', tab)
  selectedPatient.value = null
  trendData.value = {}
  detailRecords.value = []
  if (tab === 'trend') {
    loadPatientCards()
  } else {
    allPage.value = 1
    loadTabAllRecords()
  }
}

function handleAllPageChange(p) { allPage.value = p; loadTabAllRecords() }
function handleAllIndexFilter() { allPage.value = 1; loadTabAllRecords() }

function fmtTime(t) { return t ? t.replace('T', ' ').substring(0, 16) : '-' }
function fmtDate(d) { return d || '-' }

onMounted(() => {
  loadIndicators()
  loadDrugDict()
  loadRecheckItemDict()
  if (activeTab.value === 'trend') {
    loadPatientCards()
  } else {
    loadTabAllRecords()
  }
  window.addEventListener('resize', () => chartInstance?.resize())
})

onBeforeUnmount(() => {
  if (chartInstance) { chartInstance.dispose(); chartInstance = null }
})
</script>

<template>
  <div class="admin-data">
    <el-tabs v-model="activeTab" @tab-change="onTabChange" class="main-tabs">
      <el-tab-pane label="趋势图表" name="trend" />
      <el-tab-pane label="健康指标" name="index" />
      <el-tab-pane label="用药记录" name="medicine" />
      <el-tab-pane label="复查记录" name="recheck" />
    </el-tabs>

    <!-- ========== 趋势图表 Tab ========== -->
    <template v-if="activeTab === 'trend'">
      <!-- 列表模式：患者卡片 -->
      <template v-if="!selectedPatient">
        <div class="toolbar-card">
          <div class="filter-row">
            <el-input v-model="cardSearch" placeholder="搜索患者姓名" :prefix-icon="Search" clearable size="default" style="width: 280px" />
          </div>
        </div>

        <div v-loading="cardLoading" class="card-grid">
          <div v-for="card in filteredCards" :key="card.patientName" class="tcard" @click="openPatientChart(card.patientName)">
            <div class="tcard-head">
              <div class="tcard-name">{{ card.patientName }}</div>
            </div>
            <div class="tcard-body">
              <div class="tcard-stat-row">
                <div class="tcard-stat-item">
                  <span class="tcard-num">{{ card.recordCount }}</span>
                  <span class="tcard-label">条记录</span>
                </div>
                <div class="tcard-stat-item">
                  <span class="tcard-num">{{ card.indicatorCount }}</span>
                  <span class="tcard-label">项指标</span>
                </div>
              </div>
              <div class="tcard-time">最近记录：{{ fmtDate(card.lastRecordTime?.substring(0, 10)) }}</div>
            </div>
            <div class="tcard-foot">查看趋势图 →</div>
          </div>
        </div>

        <div v-if="!cardLoading && filteredCards.length === 0 && patientCards.length > 0" class="empty-msg">未找到匹配的患者</div>
        <div v-if="!cardLoading && patientCards.length === 0" class="empty-msg">暂无患者指标数据</div>
      </template>

      <!-- 详情模式：图表 -->
      <template v-else>
        <div class="search-card">
          <div class="selected-patient">
            <span class="sp-name">{{ selectedPatient.patientName }}</span>
            <button class="sp-back" @click="clearTrend">
              <span class="sp-back-arrow">‹</span> 返回患者列表
            </button>
          </div>
        </div>

        <div class="toolbar-card" v-if="Object.keys(trendData).length > 0">
          <div class="tb-left">
            <span class="tb-label">指标：</span>
            <el-select v-model="selectedTrendCode" placeholder="选择指标" size="default" style="width: 200px">
              <el-option v-for="code in Object.keys(trendData)" :key="code" :label="indName(code)" :value="code" />
            </el-select>
          </div>
          <div class="tb-right">
            <span class="tb-label">粒度：</span>
            <button :class="['tb-chip', { active: trendGranularity === 'DAY' }]" @click="trendGranularity = 'DAY'">按日</button>
            <button :class="['tb-chip', { active: trendGranularity === 'WEEK' }]" @click="trendGranularity = 'WEEK'">按周</button>
            <button :class="['tb-chip', { active: trendGranularity === 'MONTH' }]" @click="trendGranularity = 'MONTH'">按月</button>
            <span class="tb-label" style="margin-left:16px">范围：</span>
            <button :class="['tb-chip', { active: trendDays === 7 }]" @click="trendDays = 7">近7天</button>
            <button :class="['tb-chip', { active: trendDays === 14 }]" @click="trendDays = 14">近14天</button>
            <button :class="['tb-chip', { active: trendDays === 30 }]" @click="trendDays = 30">近30天</button>
            <button :class="['tb-chip', { active: trendDays === 90 }]" @click="trendDays = 90">近90天</button>
          </div>
        </div>

        <div class="chart-card" v-if="selectedTrendCode">
          <div v-loading="trendLoading" id="admin-trend-chart" class="chart-wrap"></div>
        </div>

        <div class="table-card" v-if="selectedTrendCode && trendTableRecords.length > 0">
          <div class="card-title">{{ indName(selectedTrendCode) }} — 记录明细</div>
          <table class="data-table">
            <thead><tr><th>日期</th><th>时间</th><th>数值</th><th>单位</th></tr></thead>
            <tbody>
              <tr v-for="r in trendPagedRecords" :key="r.id">
                <td>{{ r.timeLabel }}</td>
                <td>{{ fmtTime(r.recordTime) }}</td>
                <td><span class="value-num">{{ r.indexValue }}</span></td>
                <td>{{ r.unit || '-' }}</td>
              </tr>
            </tbody>
          </table>
          <div class="pagination-wrap">
            <el-pagination background layout="total, sizes, prev, pager, next" :page-sizes="[10, 20, 50]" :total="trendTableRecords.length" :page-size="trendTableSize" :current-page="trendTablePage" @current-change="p => trendTablePage = p" @size-change="s => trendTableSize = s" />
          </div>
        </div>

        <div class="table-card empty-msg" v-if="!trendLoading && Object.keys(trendData).length === 0">该患者暂无指标数据</div>
      </template>
    </template>

    <!-- ========== 健康指标 Tab ========== -->
    <template v-if="activeTab === 'index'">
      <template v-if="selectedPatient">
        <div class="search-card">
          <div class="selected-patient">
            <span class="sp-name">{{ selectedPatient.patientName }}</span>
            <button class="sp-close" @click="clearPatient">✕ 返回全部数据</button>
          </div>
        </div>
        <div v-loading="allLoading" class="table-card">
          <div class="card-title">{{ selectedPatient.patientName }} 的指标记录</div>
          <table class="data-table" v-if="detailRecords.length > 0">
            <thead><tr><th>指标名称</th><th>数值</th><th>单位</th><th>记录时间</th><th>备注</th></tr></thead>
            <tbody>
              <tr v-for="r in detailRecords" :key="r.id">
                <td>{{ indName(r.indexCode) }}</td>
                <td><span class="value-num">{{ r.indexValue }}</span></td>
                <td>{{ r.unit || '-' }}</td>
                <td>{{ fmtTime(r.recordTime) }}</td>
                <td>{{ r.remark || '-' }}</td>
              </tr>
            </tbody>
          </table>
          <div v-else class="empty-msg">暂无数据</div>
        </div>
      </template>
      <template v-else>
        <div class="toolbar-card">
          <div class="filter-row">
            <el-input v-model="patientSearch" placeholder="输入患者姓名搜索" :prefix-icon="Search" clearable size="default" style="width: 240px" @keyup.enter="searchPatient" />
            <el-select v-model="allIndexCode" @change="handleAllIndexFilter" clearable placeholder="全部指标" size="default" style="width: 180px">
              <el-option label="全部指标" value="" />
              <el-option v-for="ind in indicators" :key="ind.code" :label="ind.name" :value="ind.code" />
            </el-select>
            <el-button type="primary" :icon="Search" size="default" @click="searchPatient" :loading="patientSearching">搜索</el-button>
          </div>
        </div>
        <div v-loading="allLoading" class="table-card">
          <div class="card-title">全部患者指标记录</div>
          <table class="data-table" v-if="allRecords.length > 0">
            <thead><tr><th>患者姓名</th><th>指标名称</th><th>数值</th><th>单位</th><th>记录时间</th><th>备注</th></tr></thead>
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
          <div class="pagination-wrap">
            <el-pagination background layout="total, sizes, prev, pager, next" :page-sizes="[10, 20, 50]" :total="allTotal" :page-size="allPageSize" :current-page="allPage" @current-change="handleAllPageChange" @size-change="s => { allPageSize = s; allPage = 1; loadTabAllRecords() }" />
          </div>
        </div>
      </template>
    </template>

    <!-- ========== 用药记录 Tab ========== -->
    <template v-if="activeTab === 'medicine'">
      <template v-if="selectedPatient">
        <div class="search-card">
          <div class="selected-patient">
            <span class="sp-name">{{ selectedPatient.patientName }}</span>
            <button class="sp-close" @click="clearPatient">✕ 返回全部数据</button>
          </div>
        </div>
        <div v-loading="allLoading" class="table-card">
          <div class="card-title">{{ selectedPatient.patientName }} 的用药记录</div>
          <table class="data-table" v-if="detailRecords.length > 0">
            <thead><tr><th>药品名称</th><th>剂量</th><th>频次</th><th>开始日期</th><th>停药日期</th><th>备注</th></tr></thead>
            <tbody>
              <tr v-for="r in detailRecords" :key="r.id">
                <td>{{ drugName(r.drugCode) }}</td>
                <td>{{ r.dosage || '-' }}</td>
                <td>{{ r.frequency || '-' }}</td>
                <td>{{ fmtDate(r.startDate) }}</td>
                <td>{{ fmtDate(r.stopDate) || '持续中' }}</td>
                <td>{{ r.remark || '-' }}</td>
              </tr>
            </tbody>
          </table>
          <div v-else class="empty-msg">暂无数据</div>
        </div>
      </template>
      <template v-else>
        <div class="toolbar-card">
          <div class="filter-row">
            <el-input v-model="patientSearch" placeholder="输入患者姓名搜索" :prefix-icon="Search" clearable size="default" style="width: 240px" @keyup.enter="searchPatient" />
            <el-button type="primary" :icon="Search" size="default" @click="searchPatient" :loading="patientSearching">搜索</el-button>
          </div>
        </div>
        <div v-loading="allLoading" class="table-card">
          <div class="card-title">全部患者用药记录</div>
          <table class="data-table" v-if="allRecords.length > 0">
            <thead><tr><th>患者姓名</th><th>药品名称</th><th>剂量</th><th>频次</th><th>开始日期</th><th>停药日期</th><th>备注</th></tr></thead>
            <tbody>
              <tr v-for="r in allRecords" :key="r.id">
                <td><span class="patient-name">{{ r.patientName || '-' }}</span></td>
                <td>{{ drugName(r.drugCode) }}</td>
                <td>{{ r.dosage || '-' }}</td>
                <td>{{ r.frequency || '-' }}</td>
                <td>{{ fmtDate(r.startDate) }}</td>
                <td>{{ fmtDate(r.stopDate) || '持续中' }}</td>
                <td>{{ r.remark || '-' }}</td>
              </tr>
            </tbody>
          </table>
          <div v-else class="empty-msg">暂无数据</div>
          <div class="pagination-wrap">
            <el-pagination background layout="total, sizes, prev, pager, next" :page-sizes="[10, 20, 50]" :total="allTotal" :page-size="allPageSize" :current-page="allPage" @current-change="handleAllPageChange" @size-change="s => { allPageSize = s; allPage = 1; loadTabAllRecords() }" />
          </div>
        </div>
      </template>
    </template>

    <!-- ========== 复查记录 Tab ========== -->
    <template v-if="activeTab === 'recheck'">
      <template v-if="selectedPatient">
        <div class="search-card">
          <div class="selected-patient">
            <span class="sp-name">{{ selectedPatient.patientName }}</span>
            <button class="sp-close" @click="clearPatient">✕ 返回全部数据</button>
          </div>
        </div>
        <div v-loading="allLoading" class="table-card">
          <div class="card-title">{{ selectedPatient.patientName }} 的复查记录</div>
          <table class="data-table" v-if="detailRecords.length > 0">
            <thead><tr><th>复查医院</th><th>复查项目</th><th>复查结果</th><th>医生建议</th><th>实际复查日期</th><th>计划下次复查</th></tr></thead>
            <tbody>
              <tr v-for="r in detailRecords" :key="r.id">
                <td>{{ r.hospitalName || '-' }}</td>
                <td>{{ recheckItemName(r.recheckItemCode) }}</td>
                <td>{{ r.recheckResult || '-' }}</td>
                <td>{{ r.doctorAdvice || '-' }}</td>
                <td>{{ fmtDate(r.actualRecheckTime) }}</td>
                <td>{{ fmtDate(r.planNextTime) }}</td>
              </tr>
            </tbody>
          </table>
          <div v-else class="empty-msg">暂无数据</div>
        </div>
      </template>
      <template v-else>
        <div class="toolbar-card">
          <div class="filter-row">
            <el-input v-model="patientSearch" placeholder="输入患者姓名搜索" :prefix-icon="Search" clearable size="default" style="width: 240px" @keyup.enter="searchPatient" />
            <el-button type="primary" :icon="Search" size="default" @click="searchPatient" :loading="patientSearching">搜索</el-button>
          </div>
        </div>
        <div v-loading="allLoading" class="table-card">
          <div class="card-title">全部患者复查记录</div>
          <table class="data-table" v-if="allRecords.length > 0">
            <thead><tr><th>患者姓名</th><th>复查医院</th><th>复查项目</th><th>复查结果</th><th>医生建议</th><th>实际复查日期</th><th>计划下次复查</th></tr></thead>
            <tbody>
              <tr v-for="r in allRecords" :key="r.id">
                <td><span class="patient-name">{{ r.patientName || '-' }}</span></td>
                <td>{{ r.hospitalName || '-' }}</td>
                <td>{{ recheckItemName(r.recheckItemCode) }}</td>
                <td>{{ r.recheckResult || '-' }}</td>
                <td>{{ r.doctorAdvice || '-' }}</td>
                <td>{{ fmtDate(r.actualRecheckTime) }}</td>
                <td>{{ fmtDate(r.planNextTime) }}</td>
              </tr>
            </tbody>
          </table>
          <div v-else class="empty-msg">暂无数据</div>
          <div class="pagination-wrap">
            <el-pagination background layout="total, sizes, prev, pager, next" :page-sizes="[10, 20, 50]" :total="allTotal" :page-size="allPageSize" :current-page="allPage" @current-change="handleAllPageChange" @size-change="s => { allPageSize = s; allPage = 1; loadTabAllRecords() }" />
          </div>
        </div>
      </template>
    </template>
  </div>
</template>

<style scoped>
.admin-data { display: flex; flex-direction: column; gap: 16px; max-width: 1100px; margin: 0 auto; width: 100%; padding-bottom: 32px; }

.main-tabs { background: #fff; border-radius: 16px; padding: 0 20px; border: 1px solid #f1f5f9; box-shadow: 0 1px 4px rgba(0,0,0,0.03); }
.main-tabs :deep(.el-tabs__header) { margin-bottom: 0; }
.main-tabs :deep(.el-tabs__nav-wrap::after) { height: 1px; }

.search-card { background: #fff; border-radius: 16px; padding: 16px 24px; border: 1px solid #f1f5f9; box-shadow: 0 1px 4px rgba(0,0,0,0.03); }
.selected-patient { display: flex; align-items: center; gap: 12px; }
.sp-name { font-size: 15px; font-weight: 700; color: #1e293b; }
.sp-back { display: inline-flex; align-items: center; gap: 4px; margin-left: auto; padding: 6px 16px 6px 12px; border-radius: 20px; border: 1px solid #e2e8f0; background: #fff; color: #64748b; font-size: 13px; cursor: pointer; transition: all .2s; }
.sp-back-arrow { font-size: 20px; font-weight: 300; line-height: 1; color: #94a3b8; transition: color .2s; }
.sp-back:hover { background: #f8fafc; border-color: #93c5fd; color: #3b82f6; }
.sp-back:hover .sp-back-arrow { color: #3b82f6; }

.toolbar-card { background: #fff; border-radius: 16px; padding: 14px 20px; border: 1px solid #f1f5f9; box-shadow: 0 1px 4px rgba(0,0,0,0.03); display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 10px; }
.filter-row { display: flex; align-items: center; gap: 12px; }
.tb-left, .tb-right { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
.tb-label { font-size: 12px; color: #94a3b8; font-weight: 600; }
.tb-chip { padding: 4px 12px; border-radius: 7px; font-size: 12px; font-weight: 600; cursor: pointer; border: 1.5px solid #e2e8f0; background: #fff; color: #475569; transition: all 0.15s; }
.tb-chip:hover { border-color: #3b82f6; color: #3b82f6; }
.tb-chip.active { background: #eff6ff; border-color: #3b82f6; color: #2563eb; }

/* ===== 患者卡片 ===== */
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
  min-height: 120px;
}

.tcard {
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.25s;
  display: flex;
  flex-direction: column;
  border: 1px solid #f1f5f9;
}
.tcard:hover {
  box-shadow: 0 8px 28px rgba(0,0,0,0.08);
  transform: translateY(-3px);
  border-color: #e2e8f0;
}

.tcard-head {
  padding: 18px 20px 10px;
  background: linear-gradient(135deg, #eff6ff 0%, #f0f9ff 50%, #ecfeff 100%);
  border-bottom: 1px solid #e0f2fe;
}
.tcard-name {
  font-size: 16px; font-weight: 700; color: #0f172a;
}

.tcard-body {
  padding: 16px 20px;
  flex: 1;
  display: flex; flex-direction: column; gap: 12px;
}

.tcard-stat-row {
  display: flex; gap: 24px;
}
.tcard-stat-item {
  display: flex; align-items: baseline; gap: 4px;
}
.tcard-num {
  font-size: 22px; font-weight: 700; color: #2563eb;
}
.tcard-label {
  font-size: 12px; color: #94a3b8;
}

.tcard-time {
  font-size: 12px; color: #94a3b8;
}

.tcard-foot {
  padding: 12px 20px;
  border-top: 1px solid #f8fafc;
  font-size: 13px; color: #2563eb; font-weight: 500;
  transition: all 0.2s;
}
.tcard:hover .tcard-foot {
  color: #1d4ed8;
}

/* ===== 图表 ===== */
.chart-card { background: #fff; border-radius: 16px; padding: 20px 24px; border: 1px solid #f1f5f9; box-shadow: 0 1px 4px rgba(0,0,0,0.03); min-height: 320px; }
.chart-wrap { width: 100%; height: 320px; }

/* ===== 表格 ===== */
.table-card { background: #fff; border-radius: 16px; padding: 20px 24px; border: 1px solid #f1f5f9; box-shadow: 0 1px 4px rgba(0,0,0,0.03); }
.card-title { font-size: 14px; font-weight: 700; color: #1e293b; margin-bottom: 12px; }

.data-table { width: 100%; border-collapse: collapse; }
.data-table th { text-align: left; padding: 10px 14px; font-size: 11px; font-weight: 700; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.5px; border-bottom: 1px solid #f1f5f9; background: #fafbfc; }
.data-table td { padding: 10px 14px; font-size: 13px; color: #334155; border-bottom: 1px solid #f8fafc; }
.data-table tbody tr:hover { background: #f8fafc; }
.value-num { font-weight: 600; color: #1e293b; }
.patient-name { font-weight: 600; color: #1e40af; }

.pagination-wrap { display: flex; justify-content: center; margin-top: 20px; padding-top: 16px; border-top: 1px solid #f1f5f9; }
:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background: linear-gradient(135deg, #60a5fa, #3b82f6);
  border-radius: 8px;
}
.empty-msg { display: flex; align-items: center; justify-content: center; padding: 60px 20px; color: #94a3b8; font-size: 14px; }
</style>
