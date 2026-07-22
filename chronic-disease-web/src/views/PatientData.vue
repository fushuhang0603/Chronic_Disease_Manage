<script setup>
import { ref, computed, onMounted, nextTick, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import {
  addHealthIndex, getHealthIndexPage, getHealthIndexTrendV2, deleteHealthIndex,
  addMedicine, getMedicinePage, deleteMedicine,
  addRecheck, getRecheckPage, deleteRecheck,
  getDictPage
} from '../api/user.js'

// ====== 通用 ======
const activeTab = ref(localStorage.getItem('patientActiveTab') || 'index')
function switchTab(tab) {
  activeTab.value = tab
  localStorage.setItem('patientActiveTab', tab)
  nextTick(() => {
    if (tab === 'index' && !indexLoaded.value) fetchIndexRecords()
    if (tab === 'medicine' && !medLoaded.value) fetchMedRecords()
    if (tab === 'recheck' && !recLoaded.value) fetchRecRecords()
    if (tab === 'trend' && !trendLoaded.value) fetchTrend()
  })
}

// ====== 指标记录 ======
const indexLoading = ref(false)
const indexSubmitting = ref(false)
const indicators = ref([])
const selectedIndex = ref(null)
const indexLoaded = ref(false)

const indexInputValue = ref(null)
const indexRecordTime = ref('')
const indexRemark = ref('')

const indexRecords = ref([])
const indexTotal = ref(0)
const indexPageNum = ref(1)
const indexPageSize = ref(10)
const filterIndexCode = ref('')
const dateRange = ref([])

// 搜索指标
const searchKeyword = ref('')
const filteredIndicators = computed(() => {
  if (!searchKeyword.value.trim()) return indicators.value
  const kw = searchKeyword.value.trim().toLowerCase()
  return indicators.value.filter(d => d.name.toLowerCase().includes(kw) || d.code.toLowerCase().includes(kw))
})

// 趋势图
const trendChartEl = ref(null)
let trendChartInstance = null
const trendLoading = ref(false)
const trendLoaded = ref(false)
const selectedTrendCode = ref('')
const trendDays = ref(7)
const trendDataMap = ref({})
const trendGranularity = ref('DAY')

function fmtTime(val) {
  if (!val) return ''
  return val.replace('T', ' ').substring(0, 19)
}
function nowLocal() {
  return new Date().toLocaleString('sv-SE').replace('T', ' ')
}
function resolveRecordTime(val) {
  if (!val) return nowLocal()
  if (val.endsWith('00:00:00')) return val.substring(0, 10) + nowLocal().substring(10)
  return val
}

const cardColors = [
  { bg: '#eff6ff', border: '#93c5fd', icon: '#3b82f6', iconBg: '#dbeafe' },
  { bg: '#ecfdf5', border: '#6ee7b7', icon: '#10b981', iconBg: '#d1fae5' },
  { bg: '#fefce8', border: '#fde047', icon: '#eab308', iconBg: '#fef9c3' },
  { bg: '#fef2f2', border: '#fca5a5', icon: '#ef4444', iconBg: '#fee2e2' },
  { bg: '#f5f3ff', border: '#c4b5fd', icon: '#8b5cf6', iconBg: '#ede9fe' },
  { bg: '#fdf2f8', border: '#f9a8d4', icon: '#ec4899', iconBg: '#fce7f3' },
  { bg: '#ecfeff', border: '#67e8f9', icon: '#06b6d4', iconBg: '#cffafe' },
  { bg: '#fff7ed', border: '#fdba74', icon: '#f97316', iconBg: '#ffedd5' },
]
function cardStyle(i) { return cardColors[i % cardColors.length] }
function firstChar(name) { return name ? name.charAt(0) : '?' }

async function fetchIndicators() {
  try {
    const data = await getDictPage({ pageNum: 1, pageSize: 100, termType: 'indicator' })
    indicators.value = (data.records || []).filter(d => d.status === 1).map(d => ({
      code: d.indexCode, name: d.indexName, unit: d.unit || '',
    }))
  } catch (e) { /* ignore */ }
}

async function fetchIndexRecords() {
  indexLoading.value = true
  try {
    const params = { pageNum: indexPageNum.value, pageSize: indexPageSize.value }
    if (filterIndexCode.value) params.indexCode = filterIndexCode.value
    if (dateRange.value?.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    const data = await getHealthIndexPage(params)
    indexRecords.value = data.records || []
    indexTotal.value = data.total || 0
    indexLoaded.value = true
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    indexLoading.value = false
  }
}

function selectIndex(item) {
  selectedIndex.value = item.code === selectedIndex.value?.code ? null : item
  indexInputValue.value = null; indexRecordTime.value = ''; indexRemark.value = ''
}

async function handleIndexSubmit() {
  if (!selectedIndex.value) { ElMessage.warning('请先选择指标'); return }
  if (!indexInputValue.value && indexInputValue.value !== 0) { ElMessage.warning('请输入数值'); return }
  indexSubmitting.value = true
  try {
    await addHealthIndex({
      indexCode: selectedIndex.value.code,
      indexValue: indexInputValue.value,
      unit: selectedIndex.value.unit,
      recordTime: resolveRecordTime(indexRecordTime.value),
      remark: indexRemark.value,
    })
    ElMessage.success(`${selectedIndex.value.name} 录入成功`)
    indexInputValue.value = null; indexRecordTime.value = ''; indexRemark.value = ''
    indexPageNum.value = 1
    indexLoaded.value = false; trendLoaded.value = false
    if (activeTab.value === 'index') fetchIndexRecords()
    if (activeTab.value === 'trend') fetchTrend()
  } catch (e) {
    ElMessage.error(e.message || '录入失败')
  } finally {
    indexSubmitting.value = false
  }
}

async function handleIndexDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除「${getIndicatorName(row.indexCode)} ${row.indexValue}${row.unit || ''}」吗？`, '删除确认', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning',
    })
    await deleteHealthIndex(row.id)
    ElMessage.success('删除成功')
    fetchIndexRecords()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

function getIndicatorName(code) {
  return (indicators.value.find(d => d.code === code) || {}).name || code
}
function getIndicatorUnit(code) {
  return (indicators.value.find(d => d.code === code) || {}).unit || ''
}

function handleIndexPageChange(p) { indexPageNum.value = p; fetchIndexRecords() }
function handleIndexSizeChange(s) { indexPageSize.value = s; indexPageNum.value = 1; fetchIndexRecords() }

// ====== 趋势图 ======
async function fetchTrend() {
  trendLoading.value = true
  try {
    const data = await getHealthIndexTrendV2({ days: trendDays.value, granularity: trendGranularity.value })
    trendDataMap.value = data || {}
    trendLoaded.value = true
    if (selectedTrendCode.value && trendDataMap.value[selectedTrendCode.value]) {
      renderChart(selectedTrendCode.value)
    }
  } catch (e) {
    ElMessage.error(e.message || '加载趋势失败')
  } finally { trendLoading.value = false }
}

function renderChart(code) {
  const points = trendDataMap.value[code] || []
  if (!trendChartEl.value || points.length === 0) {
    if (trendChartInstance) trendChartInstance.clear()
    return
  }
  if (!trendChartInstance) trendChartInstance = echarts.init(trendChartEl.value)
  const indicator = indicators.value.find(d => d.code === code)
  const unit = indicator?.unit || ''
  const labels = points.map(p => p.timeLabel)
  const values = points.map(p => p.avgValue)
  trendChartInstance.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        if (!params || params.length === 0) return ''
        const p = points[params[0].dataIndex]
        if (!p) return ''
        let html = `<b>${p.timeLabel}</b><br/>`
        html += `日均值: <b>${p.avgValue}</b> ${unit}<br/>`
        html += `最高: ${p.maxValue} &nbsp; 最低: ${p.minValue}<br/>`
        html += `测量次数: ${p.recordCount} 次`
        if (p.details && p.details.length > 0) {
          html += '<br/><hr style="margin:4px 0;border-color:#e2e8f0"/>'
          p.details.forEach(d => {
            html += `<span style="color:#94a3b8">${(d.recordTime || '').substring(11, 16)}</span> &nbsp; ${d.indexValue} ${d.unit || unit}<br/>`
          })
        }
        return html
      }
    },
    grid: { top: 20, right: 30, bottom: 30, left: 50 },
    xAxis: { type: 'category', data: labels, axisLabel: { fontSize: 11, color: '#94a3b8' } },
    yAxis: { type: 'value', name: unit, nameTextStyle: { fontSize: 11, color: '#94a3b8' }, axisLabel: { fontSize: 11, color: '#94a3b8' }, splitLine: { lineStyle: { color: '#f1f5f9' } } },
    series: [{
      type: 'line', data: values, smooth: true, symbol: 'circle', symbolSize: 6,
      lineStyle: { color: '#3b82f6', width: 2 }, itemStyle: { color: '#3b82f6' },
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(59,130,246,0.2)' }, { offset: 1, color: 'rgba(59,130,246,0.02)' }]) },
    }],
  })
}

function handleTrendIndicatorChange(code) {
  selectedTrendCode.value = code
  if (code && trendDataMap.value[code]) renderChart(code)
  else if (trendChartInstance) trendChartInstance.clear()
}
function handleTrendDaysChange(days) { trendDays.value = days; trendLoaded.value = false; fetchTrend() }
function handleTrendGranularityChange(g) { trendGranularity.value = g; trendLoaded.value = false; fetchTrend() }
function quickToTrend() {
  selectedTrendCode.value = selectedIndex.value.code
  switchTab('trend')
  nextTick(() => { document.querySelector('.pd-scroll')?.scrollIntoView({ behavior: 'smooth' }) })
}

onBeforeUnmount(() => {
  if (trendChartInstance) { trendChartInstance.dispose(); trendChartInstance = null }
})

// ====== 用药记录 ======
const medLoading = ref(false)
const medSubmitting = ref(false)
const medLoaded = ref(false)
const drugOptions = ref([])
const medRecords = ref([])
const medTotal = ref(0)
const medPageNum = ref(1)
const medPageSize = ref(10)
const filterDrugCode = ref('')
const medForm = ref({ drugCode: '', dosage: '', frequency: '', startDate: '', stopDate: '', remark: '' })

async function fetchDrugDict() {
  try {
    const data = await getDictPage({ pageNum: 1, pageSize: 200, termType: 'medicine' })
    drugOptions.value = (data.records || []).filter(d => d.status === 1).map(d => ({
      code: d.indexCode, name: d.indexName,
    }))
  } catch (e) { /* ignore */ }
}
function getDrugName(code) { return (drugOptions.value.find(d => d.code === code) || {}).name || code }

async function fetchMedRecords() {
  medLoading.value = true
  try {
    const params = { pageNum: medPageNum.value, pageSize: medPageSize.value }
    if (filterDrugCode.value) params.drugCode = filterDrugCode.value
    const data = await getMedicinePage(params)
    medRecords.value = data.records || []
    medTotal.value = data.total || 0
    medLoaded.value = true
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally { medLoading.value = false }
}

async function handleMedSubmit() {
  if (!medForm.value.drugCode) { ElMessage.warning('请选择药品'); return }
  if (!medForm.value.dosage) { ElMessage.warning('请填写剂量'); return }
  if (!medForm.value.frequency) { ElMessage.warning('请填写频次'); return }
  if (!medForm.value.startDate) { ElMessage.warning('请选择开始日期'); return }
  medSubmitting.value = true
  try {
    await addMedicine({ ...medForm.value })
    ElMessage.success('用药记录录入成功')
    medForm.value = { drugCode: '', dosage: '', frequency: '', startDate: '', stopDate: '', remark: '' }
    medPageNum.value = 1; medLoaded.value = false
    fetchMedRecords()
  } catch (e) {
    ElMessage.error(e.message || '录入失败')
  } finally { medSubmitting.value = false }
}

async function handleMedDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除「${getDrugName(row.drugCode)} ${row.dosage}」吗？`, '删除确认', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    await deleteMedicine(row.id)
    ElMessage.success('删除成功')
    fetchMedRecords()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}
function handleMedPageChange(p) { medPageNum.value = p; fetchMedRecords() }
function handleMedSizeChange(s) { medPageSize.value = s; medPageNum.value = 1; fetchMedRecords() }

// ====== 复查记录 ======
const recLoading = ref(false)
const recSubmitting = ref(false)
const recLoaded = ref(false)
const itemOptions = ref([])
const recRecords = ref([])
const recTotal = ref(0)
const recPageNum = ref(1)
const recPageSize = ref(10)
const filterRecheckCode = ref('')
const recForm = ref({ hospitalName: '', recheckItemCode: '', recheckResult: '', doctorAdvice: '', actualRecheckTime: '', planNextTime: '' })

async function fetchRecheckDict() {
  try {
    const data = await getDictPage({ pageNum: 1, pageSize: 200, termType: 'indicator' })
    itemOptions.value = (data.records || []).filter(d => d.status === 1).map(d => ({
      code: d.indexCode, name: d.indexName,
    }))
  } catch (e) { /* ignore */ }
}
function getItemName(code) { return (itemOptions.value.find(d => d.code === code) || {}).name || code }

async function fetchRecRecords() {
  recLoading.value = true
  try {
    const params = { pageNum: recPageNum.value, pageSize: recPageSize.value }
    if (filterRecheckCode.value) params.recheckItemCode = filterRecheckCode.value
    const data = await getRecheckPage(params)
    recRecords.value = data.records || []
    recTotal.value = data.total || 0
    recLoaded.value = true
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally { recLoading.value = false }
}

async function handleRecSubmit() {
  if (!recForm.value.hospitalName) { ElMessage.warning('请填写医院名称'); return }
  if (!recForm.value.recheckItemCode) { ElMessage.warning('请选择复查项目'); return }
  if (!recForm.value.actualRecheckTime) { ElMessage.warning('请选择复查日期'); return }
  recSubmitting.value = true
  try {
    await addRecheck({ ...recForm.value })
    ElMessage.success('复查记录录入成功')
    recForm.value = { hospitalName: '', recheckItemCode: '', recheckResult: '', doctorAdvice: '', actualRecheckTime: '', planNextTime: '' }
    recPageNum.value = 1; recLoaded.value = false
    fetchRecRecords()
  } catch (e) {
    ElMessage.error(e.message || '录入失败')
  } finally { recSubmitting.value = false }
}

async function handleRecDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除「${row.hospitalName} ${getItemName(row.recheckItemCode)}」吗？`, '删除确认', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    await deleteRecheck(row.id)
    ElMessage.success('删除成功')
    fetchRecRecords()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}
function handleRecPageChange(p) { recPageNum.value = p; fetchRecRecords() }
function handleRecSizeChange(s) { recPageSize.value = s; recPageNum.value = 1; fetchRecRecords() }

onMounted(() => {
  fetchIndicators()
  fetchDrugDict()
  fetchRecheckDict()
  // 恢复刷新前所在 Tab 的数据
  const tab = activeTab.value
  if (tab === 'index') fetchIndexRecords()
  if (tab === 'medicine') fetchMedRecords()
  if (tab === 'recheck') fetchRecRecords()
  if (tab === 'trend') fetchTrend()
})
</script>

<template>
  <div class="pd-root pd-scroll">
    <!-- ====== Tab 导航 ====== -->
    <div class="main-tabs">
      <button :class="['main-tab', { active: activeTab === 'index' }]" @click="switchTab('index')">
        <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
        指标录入
      </button>
      <button :class="['main-tab', { active: activeTab === 'trend' }]" @click="switchTab('trend')">
        <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="20" x2="12" y2="10"/><line x1="18" y1="20" x2="18" y2="4"/><line x1="6" y1="20" x2="6" y2="16"/></svg>
        趋势图表
      </button>
      <button :class="['main-tab', { active: activeTab === 'medicine' }]" @click="switchTab('medicine')">
        <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>
        用药记录
      </button>
      <button :class="['main-tab', { active: activeTab === 'recheck' }]" @click="switchTab('recheck')">
        <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
        复查记录
      </button>
    </div>

    <!-- ========================================== -->
    <!-- 指标录入 -->
    <!-- ========================================== -->
    <div v-show="activeTab === 'index'" class="tab-body">
      <div class="input-card">
        <div class="card-head">
          <span class="head-label">录入健康指标</span>
          <span class="head-hint">搜索指标，点击选择，输入数值即完成记录</span>
        </div>
        <div class="search-box">
          <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
          </svg>
          <input v-model="searchKeyword" class="search-input" placeholder="搜索指标名称，如血糖、血压..." />
        </div>
        <div class="card-grid">
          <div v-for="(item, i) in filteredIndicators" :key="item.code" class="indicator-card" :class="{ selected: selectedIndex?.code === item.code }"
            :style="selectedIndex?.code === item.code ? { background: cardStyle(i).iconBg, borderColor: cardStyle(i).icon } : { background: '#fff', borderColor: '#e2e8f0' }"
            @click="selectIndex(item)">
            <span class="card-icon" :style="selectedIndex?.code === item.code ? { background: cardStyle(i).icon, color: '#fff' } : { background: cardStyle(i).iconBg, color: cardStyle(i).icon }">{{ firstChar(item.name) }}</span>
            <span class="card-name">{{ item.name }}</span>
          </div>
          <div v-if="filteredIndicators.length === 0 && searchKeyword" class="no-match">未找到匹配的指标</div>
        </div>

        <Transition name="slide">
          <div v-if="selectedIndex" class="input-panel" :style="{ borderColor: cardStyle(indicators.findIndex(d => d.code === selectedIndex.code) % 8).border }">
            <div class="panel-header">
              <span class="panel-label">{{ selectedIndex.name }}</span>
              <span v-if="selectedIndex.unit" class="panel-unit">{{ selectedIndex.unit }}</span>
              <button class="trend-quick-link" @click="quickToTrend">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>查看趋势
              </button>
            </div>
            <div class="panel-fields">
              <div class="value-input-wrap">
                <input id="quick-input" v-model="indexInputValue" type="number" step="0.1" min="0" class="value-input" placeholder="输入数值" @keyup.enter="handleIndexSubmit" />
                <span v-if="selectedIndex.unit" class="unit-suffix">{{ selectedIndex.unit }}</span>
              </div>
              <el-date-picker v-model="indexRecordTime" type="datetime" placeholder="选择日期时间" style="width:210px"
                format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DD HH:mm:ss" class="time-picker" />
              <input v-model="indexRemark" class="remark-input" placeholder="备注（选填）" maxlength="100" />
              <button class="save-btn" :disabled="indexSubmitting" @click="handleIndexSubmit">
                {{ indexSubmitting ? '保存中...' : '保存' }}
              </button>
            </div>
          </div>
        </Transition>
      </div>

      <!-- 指标历史记录 -->
      <div class="list-card">
        <div class="card-head"><span class="head-label">历史记录</span></div>
        <div class="filter-row">
          <el-select v-model="filterIndexCode" placeholder="全部指标" size="default" style="width:160px" clearable @change="handleIndexPageChange(1)">
            <el-option v-for="opt in indicators" :key="opt.code" :label="opt.name" :value="opt.code" />
          </el-select>
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期"
            size="default" style="width:260px" value-format="YYYY-MM-DD" @change="handleIndexPageChange(1)" />
        </div>
        <el-table :data="indexRecords" v-loading="indexLoading" stripe empty-text="暂无数据">
          <el-table-column label="指标" min-width="110">
            <template #default="{ row }"><span class="index-tag">{{ getIndicatorName(row.indexCode) }}</span></template>
          </el-table-column>
          <el-table-column label="数值" width="130" align="center">
            <template #default="{ row }"><span class="value-cell">{{ row.indexValue }}<span v-if="row.unit" class="value-unit">{{ row.unit }}</span></span></template>
          </el-table-column>
          <el-table-column label="测量时间" width="170" align="center">
            <template #default="{ row }"><span>{{ row.recordTime || '—' }}</span></template>
          </el-table-column>
          <el-table-column label="备注" min-width="100">
            <template #default="{ row }"><span style="color:#94a3b8">{{ row.remark || '—' }}</span></template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center" fixed="right">
            <template #default="{ row }"><el-button type="danger" link size="small" @click="handleIndexDelete(row)">删除</el-button></template>
          </el-table-column>
        </el-table>
        <div class="pagination-wrap">
          <el-pagination background layout="total, sizes, prev, pager, next" :total="indexTotal" :page-size="indexPageSize"
            :current-page="indexPageNum" :page-sizes="[10, 20, 50]" @size-change="handleIndexSizeChange" @current-change="handleIndexPageChange" />
        </div>
      </div>
    </div>

    <!-- ========================================== -->
    <!-- 趋势图表 -->
    <!-- ========================================== -->
    <div v-show="activeTab === 'trend'" class="tab-body">
      <div class="list-card">
        <div class="card-head"><span class="head-label">指标趋势</span></div>
        <div class="trend-controls">
          <el-select v-model="selectedTrendCode" placeholder="选择指标" size="default" style="width:160px" @change="handleTrendIndicatorChange" clearable>
            <el-option v-for="opt in indicators" :key="opt.code" :label="opt.name" :value="opt.code" />
          </el-select>
          <el-radio-group v-model="trendGranularity" size="small" @change="handleTrendGranularityChange">
            <el-radio-button value="DAY">按日</el-radio-button>
            <el-radio-button value="WEEK">按周</el-radio-button>
            <el-radio-button value="MONTH">按月</el-radio-button>
          </el-radio-group>
          <el-radio-group v-model="trendDays" size="small" @change="handleTrendDaysChange">
            <el-radio-button :value="7">近7天</el-radio-button>
            <el-radio-button :value="14">近14天</el-radio-button>
            <el-radio-button :value="30">近30天</el-radio-button>
          </el-radio-group>
        </div>
        <div v-loading="trendLoading" class="chart-wrap">
          <div v-if="!selectedTrendCode" class="chart-placeholder">请选择一个指标查看趋势</div>
          <div ref="trendChartEl" class="chart-box"></div>
        </div>
      </div>
    </div>

    <!-- ========================================== -->
    <!-- 用药记录 -->
    <!-- ========================================== -->
    <div v-show="activeTab === 'medicine'" class="tab-body">
      <div class="input-card">
        <div class="card-head"><span class="head-label">录入用药记录</span></div>
        <div class="form-grid">
          <div class="field">
            <span class="field-label">药品名称</span>
            <el-select v-model="medForm.drugCode" placeholder="选择药品" filterable>
              <el-option v-for="opt in drugOptions" :key="opt.code" :label="opt.name" :value="opt.code" />
            </el-select>
          </div>
          <div class="field">
            <span class="field-label">剂量</span>
            <el-input v-model="medForm.dosage" placeholder="如 0.5g/次" />
          </div>
          <div class="field">
            <span class="field-label">频次</span>
            <el-input v-model="medForm.frequency" placeholder="如 每日2次" />
          </div>
          <div></div>
          <div class="field">
            <span class="field-label">开始日期</span>
            <el-date-picker v-model="medForm.startDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" />
          </div>
          <div class="field">
            <span class="field-label">停药日期</span>
            <el-date-picker v-model="medForm.stopDate" type="date" placeholder="选填" value-format="YYYY-MM-DD" />
          </div>
          <div class="field field-wide">
            <span class="field-label">备注</span>
            <el-input v-model="medForm.remark" placeholder="选填" maxlength="200" />
          </div>
          <div class="field field-btn">
            <button class="save-btn" :disabled="medSubmitting" @click="handleMedSubmit">
              {{ medSubmitting ? '保存中...' : '保存' }}
            </button>
          </div>
        </div>
      </div>

      <div class="list-card">
        <div class="card-head"><span class="head-label">用药记录</span></div>
        <div class="filter-row">
          <el-select v-model="filterDrugCode" placeholder="全部药品" style="width:160px" clearable @change="handleMedPageChange(1)">
            <el-option v-for="opt in drugOptions" :key="opt.code" :label="opt.name" :value="opt.code" />
          </el-select>
        </div>
        <el-table :data="medRecords" v-loading="medLoading" stripe empty-text="暂无用药记录">
          <el-table-column label="药品" min-width="140">
            <template #default="{ row }"><span class="med-tag">{{ getDrugName(row.drugCode) }}</span></template>
          </el-table-column>
          <el-table-column prop="dosage" label="剂量" width="120" align="center" />
          <el-table-column prop="frequency" label="频次" width="140" align="center" />
          <el-table-column prop="startDate" label="开始日期" width="120" align="center" />
          <el-table-column label="停药日期" width="120" align="center">
            <template #default="{ row }"><span :style="{ color: row.stopDate ? '#ef4444' : '#10b981' }">{{ row.stopDate || '服用中' }}</span></template>
          </el-table-column>
          <el-table-column label="备注" min-width="100">
            <template #default="{ row }"><span style="color:#94a3b8">{{ row.remark || '—' }}</span></template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center" fixed="right">
            <template #default="{ row }"><el-button type="danger" link size="small" @click="handleMedDelete(row)">删除</el-button></template>
          </el-table-column>
        </el-table>
        <div class="pagination-wrap">
          <el-pagination background layout="total, sizes, prev, pager, next" :total="medTotal" :page-size="medPageSize"
            :current-page="medPageNum" :page-sizes="[5, 10, 20]" @size-change="handleMedSizeChange" @current-change="handleMedPageChange" />
        </div>
      </div>
    </div>

    <!-- ========================================== -->
    <!-- 复查记录 -->
    <!-- ========================================== -->
    <div v-show="activeTab === 'recheck'" class="tab-body">
      <div class="input-card">
        <div class="card-head"><span class="head-label">录入复查记录</span></div>
        <div class="form-grid">
          <div class="field">
            <span class="field-label">医院名称</span>
            <el-input v-model="recForm.hospitalName" placeholder="如 人民医院" />
          </div>
          <div class="field">
            <span class="field-label">复查项目</span>
            <el-select v-model="recForm.recheckItemCode" placeholder="选择项目" filterable>
              <el-option v-for="opt in itemOptions" :key="opt.code" :label="opt.name" :value="opt.code" />
            </el-select>
          </div>
          <div class="field">
            <span class="field-label">复查日期</span>
            <el-date-picker v-model="recForm.actualRecheckTime" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" />
          </div>
          <div class="field">
            <span class="field-label">下次复查</span>
            <el-date-picker v-model="recForm.planNextTime" type="date" placeholder="选填" value-format="YYYY-MM-DD" />
          </div>
          <div class="field">
            <span class="field-label">检查结果</span>
            <el-input v-model="recForm.recheckResult" placeholder="选填" maxlength="500" />
          </div>
          <div class="field">
            <span class="field-label">医嘱建议</span>
            <el-input v-model="recForm.doctorAdvice" placeholder="选填" maxlength="500" />
          </div>
          <div class="field field-btn">
            <button class="save-btn" :disabled="recSubmitting" @click="handleRecSubmit">
              {{ recSubmitting ? '保存中...' : '保存' }}
            </button>
          </div>
        </div>
      </div>

      <div class="list-card">
        <div class="card-head"><span class="head-label">复查记录</span></div>
        <div class="filter-row">
          <el-select v-model="filterRecheckCode" placeholder="全部项目" style="width:160px" clearable @change="handleRecPageChange(1)">
            <el-option v-for="opt in itemOptions" :key="opt.code" :label="opt.name" :value="opt.code" />
          </el-select>
        </div>
        <el-table :data="recRecords" v-loading="recLoading" stripe empty-text="暂无复查记录">
          <el-table-column prop="hospitalName" label="医院" min-width="120" />
          <el-table-column label="复查项目" width="140">
            <template #default="{ row }"><span class="rec-tag">{{ getItemName(row.recheckItemCode) }}</span></template>
          </el-table-column>
          <el-table-column prop="actualRecheckTime" label="复查日期" width="120" align="center" />
          <el-table-column label="下次复查" width="120" align="center">
            <template #default="{ row }"><span :style="{ color: row.planNextTime ? '#3b82f6' : '#94a3b8' }">{{ row.planNextTime || '未计划' }}</span></template>
          </el-table-column>
          <el-table-column label="检查结果" min-width="130">
            <template #default="{ row }"><span style="color:#334155">{{ row.recheckResult || '—' }}</span></template>
          </el-table-column>
          <el-table-column label="医嘱建议" min-width="130">
            <template #default="{ row }"><span style="color:#64748b">{{ row.doctorAdvice || '—' }}</span></template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center" fixed="right">
            <template #default="{ row }"><el-button type="danger" link size="small" @click="handleRecDelete(row)">删除</el-button></template>
          </el-table-column>
        </el-table>
        <div class="pagination-wrap">
          <el-pagination background layout="total, sizes, prev, pager, next" :total="recTotal" :page-size="recPageSize"
            :current-page="recPageNum" :page-sizes="[5, 10, 20]" @size-change="handleRecSizeChange" @current-change="handleRecPageChange" />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.pd-root { display: flex; flex-direction: column; gap: 20px; max-width: 980px; margin: 0 auto; width: 100%; }

/* ===== Tab 导航 ===== */
.main-tabs {
  display: flex; gap: 4px; background: #f1f5f9; border-radius: 12px; padding: 4px;
  width: fit-content;
}
.main-tab {
  display: flex; align-items: center; gap: 6px; padding: 10px 20px;
  border: none; background: transparent; font-size: 14px; font-weight: 500;
  color: #64748b; cursor: pointer; border-radius: 10px; transition: all 0.2s;
}
.main-tab:hover:not(.active) { color: #334155; }
.main-tab.active { background: #fff; color: #1e293b; font-weight: 600; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }

.tab-body { display: flex; flex-direction: column; gap: 20px; }

/* ===== 卡片通用 ===== */
.input-card, .list-card {
  background: #fff; border-radius: 20px; box-shadow: 0 2px 20px rgba(0,0,0,0.05);
  padding: 28px 32px; border: 1px solid #f1f5f9;
}
.card-head { display: flex; align-items: baseline; gap: 12px; margin-bottom: 18px; }
.head-label { font-size: 17px; font-weight: 700; color: #1e293b; }
.head-hint { font-size: 13px; color: #94a3b8; }

/* ===== 搜索框 ===== */
.search-box { position: relative; margin-bottom: 18px; }
.search-icon { position: absolute; left: 14px; top: 50%; transform: translateY(-50%); width: 18px; height: 18px; color: #94a3b8; }
.search-input {
  width: 100%; height: 44px; border-radius: 12px; border: 1.5px solid #e2e8f0;
  padding: 0 44px; font-size: 14px; color: #1e293b; outline: none;
  background: #f8fafc; transition: all 0.2s; box-sizing: border-box;
}
.search-input:focus { border-color: #3b82f6; background: #fff; box-shadow: 0 0 0 3px rgba(59,130,246,0.08); }
.search-input::placeholder { color: #94a3b8; }

/* ===== 指标卡片网格 ===== */
.card-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; max-height: 260px; overflow-y: auto; }
.card-grid::-webkit-scrollbar { width: 4px; }
.card-grid::-webkit-scrollbar-thumb { background: #e2e8f0; border-radius: 2px; }
.indicator-card {
  display: flex; align-items: center; gap: 10px;
  padding: 12px 14px; border-radius: 12px; border: 1.5px solid #e2e8f0;
  cursor: pointer; transition: all 0.2s; user-select: none;
}
.indicator-card:hover { border-color: #94a3b8; transform: translateY(-1px); box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.indicator-card.selected { font-weight: 600; box-shadow: 0 4px 12px rgba(0,0,0,0.1); transform: translateY(-1px); }
.card-icon {
  width: 34px; height: 34px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  font-size: 16px; font-weight: 700; flex-shrink: 0; transition: all 0.2s;
}
.card-name { font-size: 13px; color: #334155; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.no-match { grid-column: 1 / -1; text-align: center; padding: 32px 0; color: #94a3b8; font-size: 14px; }

/* ===== 输入面板 ===== */
.input-panel { margin-top: 18px; padding: 18px 22px; background: #f8fafc; border-radius: 14px; border: 1.5px solid #e2e8f0; }
.panel-header { margin-bottom: 12px; display: flex; align-items: center; gap: 8px; }
.panel-label { font-size: 14px; font-weight: 600; color: #334155; }
.panel-unit { font-size: 12px; color: #64748b; background: #e2e8f0; padding: 2px 8px; border-radius: 4px; }
.trend-quick-link {
  margin-left: auto; display: flex; align-items: center; gap: 4px;
  padding: 4px 10px; border-radius: 8px; border: 1px solid #cbd5e1;
  background: #fff; color: #3b82f6; font-size: 12px; font-weight: 500; cursor: pointer; transition: all 0.2s;
}
.trend-quick-link:hover { background: #eff6ff; border-color: #3b82f6; }
.panel-fields { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }

.value-input-wrap { position: relative; display: flex; align-items: center; }
.unit-suffix { position: absolute; right: 12px; font-size: 13px; color: #64748b; pointer-events: none; }
.value-input {
  width: 160px; height: 42px; border-radius: 10px; border: 1.5px solid #cbd5e1;
  padding: 0 14px; font-size: 18px; font-weight: 600; color: #1e293b;
  outline: none; background: #fff; transition: border-color 0.2s;
}
.value-input:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }
.value-input::placeholder { font-size: 14px; font-weight: 400; color: #94a3b8; }
.remark-input {
  width: 180px; height: 42px; border-radius: 10px; border: 1.5px solid #cbd5e1;
  padding: 0 14px; font-size: 14px; color: #1e293b; outline: none; background: #fff; transition: border-color 0.2s;
}
.remark-input:focus { border-color: #3b82f6; }
.remark-input::placeholder { color: #94a3b8; }

.time-picker {
  --el-border-radius-base: 10px; --el-input-border-color: #cbd5e1;
}
.time-picker :deep(.el-input__wrapper) {
  border-radius: 10px !important; border: 1.5px solid #cbd5e1 !important; box-shadow: none !important;
  padding: 0 14px; height: 42px;
}
.time-picker :deep(.el-input__wrapper):hover { border-color: #3b82f6 !important; }
.time-picker :deep(.is-focus .el-input__wrapper) { border-color: #3b82f6 !important; box-shadow: 0 0 0 3px rgba(59,130,246,0.1) !important; }

.save-btn {
  height: 42px; padding: 0 28px; border-radius: 10px; font-size: 14px; font-weight: 600;
  border: none; cursor: pointer; transition: all 0.2s;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: #fff; box-shadow: 0 4px 12px rgba(37,99,235,0.3);
}
.save-btn:hover { box-shadow: 0 6px 16px rgba(37,99,235,0.4); transform: translateY(-1px); }
.save-btn:disabled { opacity: 0.5; cursor: not-allowed; transform: none; }

/* ===== 通用表单行（用药/复查） ===== */
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px 24px;
}
.form-grid .field {
  display: flex;
  align-items: center;
  gap: 10px;
}
.form-grid .field-label {
  font-size: 13px;
  color: #475569;
  font-weight: 500;
  white-space: nowrap;
  min-width: 60px;
  text-align: right;
}
.form-grid .field-btn {
  grid-column: 1 / -1;
  justify-content: flex-end;
  padding-top: 4px;
}
.form-grid .field-wide {
  grid-column: 1 / -1;
}
.form-grid :deep(.el-select) { flex: 1; }
.form-grid :deep(.el-input__wrapper) { border-radius: 10px; box-shadow: 0 0 0 1.5px #e2e8f0; }

/* ===== 动画 ===== */
.slide-enter-active, .slide-leave-active { transition: all 0.3s ease; }
.slide-enter-from, .slide-leave-to { opacity: 0; transform: translateY(-10px); }

/* ===== 历史记录 ===== */
.filter-row { display: flex; gap: 12px; margin-bottom: 16px; }
.filter-row :deep(.el-input__wrapper) { border-radius: 10px; box-shadow: 0 0 0 1.5px #e2e8f0; }

.index-tag { display: inline-block; padding: 2px 12px; border-radius: 6px; background: #eff6ff; color: #3b82f6; font-size: 13px; font-weight: 500; }
.med-tag { display: inline-block; padding: 2px 12px; border-radius: 6px; background: #eff6ff; color: #2563eb; font-size: 13px; font-weight: 500; }
.rec-tag { display: inline-block; padding: 2px 12px; border-radius: 6px; background: #fdf2f8; color: #be185d; font-size: 13px; font-weight: 500; }

.value-cell { font-size: 16px; font-weight: 700; color: #1e293b; }
.value-unit { font-size: 12px; font-weight: 400; color: #64748b; margin-left: 4px; }
.pagination-wrap { display: flex; justify-content: center; margin-top: 20px; padding-top: 16px; border-top: 1px solid #f1f5f9; }

.value-input::-webkit-outer-spin-button,
.value-input::-webkit-inner-spin-button { opacity: 1; }

/* ===== 趋势图 ===== */
.trend-controls { display: flex; align-items: center; gap: 16px; margin-bottom: 12px; }
.chart-wrap { position: relative; min-height: 200px; }
.chart-placeholder { display: flex; align-items: center; justify-content: center; height: 200px; color: #94a3b8; font-size: 14px; }
.chart-box { width: 100%; height: 300px; }
.chart-wrap :deep(.el-loading-mask) { border-radius: 14px; }
.trend-controls :deep(.el-input__wrapper),
.trend-controls :deep(.el-radio-button__inner) { border-radius: 10px; }
.trend-controls :deep(.el-radio-group) { box-shadow: none; }
</style>

<style>
.pd-root .time-picker { display: inline-flex; align-items: center; vertical-align: middle; }
.pd-root .time-picker .el-input__wrapper {
  border-radius: 10px; border: 1.5px solid #cbd5e1; box-shadow: none;
  padding: 0 14px; height: 42px;
}
.pd-root .time-picker .el-input__wrapper:hover { border-color: #3b82f6; }
</style>
