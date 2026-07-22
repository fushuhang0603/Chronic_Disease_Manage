<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllRemindPage, addRemindForPatient, updateRemindStatus, deleteRemind, getArchivePage } from '../api/user.js'

// ====== 状态 ======
const loading = ref(false)
const submitting = ref(false)
const showDialog = ref(false)
const patientSearch = ref('')
const patientList = ref([])
const selectedPatient = ref(null)
const patientSearching = ref(false)

const records = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(7)
const searchName = ref('')
const activeStatus = ref('')

const form = reactive({
  title: '',
  remindType: 'custom',
  remindTime: '',
  repeatType: 'none',
  content: '',
})

// ====== 映射 ======
const typeMap = {
  medicine: { label: '用药', color: '#10b981' },
  recheck: { label: '复查', color: '#3b82f6' },
  custom: { label: '自定义', color: '#8b5cf6' },
}
const repeatMap = { none: '不重复', daily: '每天', weekly: '每周', monthly: '每月' }
const statusMap = {
  0: { label: '待提醒', color: '#64748b' },
  1: { label: '已推送', color: '#3b82f6' },
  2: { label: '已读', color: '#10b981' },
  3: { label: '已完成', color: '#059669' },
  4: { label: '已关闭', color: '#94a3b8' },
  5: { label: '已过期', color: '#ef4444' },
}

// ====== 方法 ======
async function fetchRecords() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (searchName.value.trim()) params.patientName = searchName.value.trim()
    if (activeStatus.value !== '') params.remindStatus = Number(activeStatus.value)
    const data = await getAllRemindPage(params)
    records.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  fetchRecords()
}

function handlePageChange(p) {
  pageNum.value = p
  fetchRecords()
}

function handleSizeChange(s) {
  pageSize.value = s
  pageNum.value = 1
  fetchRecords()
}

function fmtTime(val) {
  if (!val) return ''
  return val.replace('T', ' ').substring(0, 16)
}

// ====== 新建提醒 ======
let searchTimer = null

async function searchPatient() {
  if (!patientSearch.value.trim() || patientSearch.value.trim().length < 2) {
    patientList.value = []
    return
  }
  clearTimeout(searchTimer)
  searchTimer = setTimeout(async () => {
  patientSearching.value = true
  try {
    const data = await getArchivePage({ patientName: patientSearch.value.trim(), pageNum: 1, pageSize: 10 })
    patientList.value = data.records || []
  } catch (e) {
    ElMessage.error(e.message || '查询患者失败')
  } finally {
    patientSearching.value = false
  }
  }, 300)
}

function selectPatient(p) {
  selectedPatient.value = p
  patientList.value = []
}

async function handleSubmit() {
  if (!selectedPatient.value) { ElMessage.warning('请先搜索并选择患者'); return }
  if (!form.title.trim()) { ElMessage.warning('请输入提醒标题'); return }
  if (!form.remindTime) { ElMessage.warning('请选择提醒时间'); return }
  submitting.value = true
  try {
    await addRemindForPatient({ patientName: selectedPatient.value.patientName, ...form })
    ElMessage.success('提醒创建成功')
    showDialog.value = false
    form.title = ''; form.content = ''; form.remindTime = ''
    form.remindType = 'custom'; form.repeatType = 'none'
    patientSearch.value = ''; selectedPatient.value = null
    pageNum.value = 1
    fetchRecords()
  } catch (e) {
    ElMessage.error(e.message || '创建失败')
  } finally {
    submitting.value = false
  }
}

// ====== 操作 ======
async function handleClose(row) {
  try {
    await updateRemindStatus(row.id, 4)
    ElMessage.success('已关闭')
    fetchRecords()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

async function handleReopen(row) {
  try {
    await updateRemindStatus(row.id, 0)
    ElMessage.success('已重新启用')
    fetchRecords()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除「${row.title}」吗？`, '删除确认', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning',
    })
    await deleteRemind(row.id)
    ElMessage.success('删除成功')
    if (records.value.length === 1 && pageNum.value > 1) pageNum.value--
    fetchRecords()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

onMounted(() => fetchRecords())
</script>

<template>
  <div class="admin-remind">
    <div class="page-card">
      <!-- 头部 -->
      <div class="page-head">
        <div class="head-left">
          <span class="page-title">用药提醒管理</span>
          <span class="page-desc">管理所有患者的用药、复查提醒</span>
        </div>
        <button class="create-btn" @click="showDialog = !showDialog">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          {{ showDialog ? '收起' : '新建提醒' }}
        </button>
      </div>

      <!-- 新建表单（行内展开，与患者端一致） -->
      <Transition name="slide">
        <div v-if="showDialog" class="remind-form">
          <!-- 患者姓名 + 标题 + 类型 + 时间 + 重复 + 提交 -->
          <div class="form-row">
            <div style="position:relative">
              <input v-if="!selectedPatient" v-model="patientSearch" class="text-input" style="width:160px" placeholder="患者姓名" @input="searchPatient" />
              <div v-else class="selected-tag-inline">
                <span>{{ selectedPatient.patientName }}</span>
                <span class="selected-tag-phone">{{ selectedPatient.phone || '' }}</span>
                <button class="selected-tag-close" @click="selectedPatient = null; patientSearch = ''">&#10005;</button>
              </div>
              <div v-if="patientList.length > 0 && !selectedPatient" class="patient-dropdown">
                <div v-for="p in patientList" :key="p.id" class="patient-dropdown-item" @click="selectPatient(p)">
                  <span class="pick-name">{{ p.patientName }}</span>
                  <span class="pick-info">{{ p.phone || '-' }}</span>
                </div>
              </div>
            </div>
            <input v-model="form.title" class="text-input" style="width:200px" placeholder="提醒标题，如：服用降压药" maxlength="50" />
            <el-select v-model="form.remindType" size="large" style="width:110px">
              <el-option label="用药" value="medicine" />
              <el-option label="复查" value="recheck" />
              <el-option label="自定义" value="custom" />
            </el-select>
            <el-date-picker
              v-model="form.remindTime"
              type="datetime"
              size="large"
              style="width:190px"
              placeholder="提醒时间"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
            <el-select v-model="form.repeatType" size="large" style="width:110px">
              <el-option label="不重复" value="none" />
              <el-option label="每天" value="daily" />
              <el-option label="每周" value="weekly" />
              <el-option label="每月" value="monthly" />
            </el-select>
            <button class="save-btn" :disabled="submitting" @click="handleSubmit">
              {{ submitting ? '保存中...' : '创建提醒' }}
            </button>
          </div>
          <div class="form-row" style="margin-top:10px">
            <input v-model="form.content" class="text-input" style="flex:1" placeholder="备注详情（选填）" maxlength="200" />
          </div>
        </div>
      </Transition>
    </div>

    <!-- 列表卡片 -->
    <div class="list-card">
      <!-- 筛选栏 -->
      <div class="filter-row">
        <div class="search-bar">
          <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" class="search-icon"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
          <input v-model="searchName" class="search-input" placeholder="患者姓名" @keyup.enter="handleSearch" />
        </div>
        <select v-model="activeStatus" class="status-select" @change="handleSearch">
          <option value="">全部状态</option>
          <option value="0">待提醒</option>
          <option value="1">已推送</option>
          <option value="2">已读</option>
          <option value="3">已完成</option>
          <option value="5">已过期</option>
          <option value="4">已关闭</option>
        </select>
        <button class="search-btn" @click="handleSearch">搜索</button>
      </div>

      <!-- 表格 -->
      <div v-loading="loading" class="table-wrap">
        <table class="data-table" v-if="records.length > 0">
          <thead>
            <tr>
              <th style="width:100px">患者姓名</th>
              <th>提醒标题</th>
              <th style="width:80px">类型</th>
              <th style="width:150px">提醒时间</th>
              <th style="width:70px">重复</th>
              <th style="width:80px">状态</th>
              <th style="width:160px">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in records" :key="item.id" :class="{ 'row-closed': item.remindStatus === 4, 'row-expired': item.remindStatus === 5 }">
              <td><span class="patient-name">{{ item.patientName || '-' }}</span></td>
              <td>
                <span class="remind-title" :class="{ closed: item.remindStatus === 4 }">{{ item.title }}</span>
                <span v-if="item.content" class="remind-content">— {{ item.content }}</span>
              </td>
              <td>
                <span class="type-tag" :style="{ color: typeMap[item.remindType]?.color || '#475569', background: (typeMap[item.remindType]?.color || '#475569') + '15' }">
                  {{ (typeMap[item.remindType] || typeMap.custom).label }}
                </span>
              </td>
              <td><span class="time-text">{{ fmtTime(item.remindTime) }}</span></td>
              <td>{{ repeatMap[item.repeatType] }}</td>
              <td>
                <span class="status-tag" :style="{ color: statusMap[item.remindStatus]?.color, background: (statusMap[item.remindStatus]?.color || '#94a3b8') + '15' }">
                  {{ statusMap[item.remindStatus]?.label || '未知' }}
                </span>
              </td>
              <td>
                <div class="action-btns">
                  <template v-if="item.remindStatus === 4 || item.remindStatus === 5">
                    <button class="act-btn act-reopen" @click="handleReopen(item)">重新启用</button>
                    <button class="act-btn act-delete" @click="handleDelete(item)">删除</button>
                  </template>
                  <template v-else>
                    <button class="act-btn act-close" @click="handleClose(item)">关闭</button>
                    <button class="act-btn act-delete" @click="handleDelete(item)">删除</button>
                  </template>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-else-if="!loading" class="empty-box">
          <el-icon :size="56" color="#cbd5e1"><AlarmClock /></el-icon>
          <p class="empty-text">暂无提醒数据</p>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :total="total"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :current-page="pageNum"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-remind {
  display: flex; flex-direction: column; gap: 20px;
  max-width: 1100px; margin: 0 auto; width: 100%; padding-bottom: 32px;
}

.page-card, .list-card {
  background: #fff; border-radius: 20px;
  box-shadow: 0 2px 20px rgba(0,0,0,0.05);
  padding: 28px 32px; border: 1px solid #f1f5f9;
}

/* 头部 */
.page-head { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 0; }
.head-left { display: flex; flex-direction: column; gap: 4px; }
.page-title { font-size: 18px; font-weight: 700; color: #1e293b; }
.page-desc { font-size: 13px; color: #94a3b8; }

.create-btn {
  display: flex; align-items: center; gap: 6px;
  height: 38px; padding: 0 18px; border-radius: 10px;
  border: 1.5px solid #cbd5e1; background: #fff;
  color: #334155; font-size: 13px; font-weight: 600; cursor: pointer;
  transition: all 0.2s;
}
.create-btn:hover { border-color: #3b82f6; color: #3b82f6; background: #eff6ff; }

/* 筛选栏 */
.filter-row {
  display: flex; align-items: center; gap: 10px; margin-bottom: 18px;
}
.search-bar {
  display: flex; align-items: center; gap: 6px;
  height: 36px; box-sizing: border-box;
  padding: 0 10px; background: #fff;
  border: 1.5px solid #cbd5e1; border-radius: 8px;
  width: 200px;
}
.search-bar:focus-within { border-color: #3b82f6; }
.search-icon { color: #94a3b8; flex-shrink: 0; }
.search-input {
  width: 100%; border: none; background: transparent; outline: none;
  font-size: 12px; color: #1e293b;
}
.search-input::placeholder { color: #94a3b8; }
.search-btn {
  height: 36px; padding: 0 16px; border-radius: 8px;
  border: none; cursor: pointer; font-size: 13px; font-weight: 600;
  background: #3b82f6; color: #fff; transition: background 0.2s;
  white-space: nowrap;
}
.search-btn:hover { background: #2563eb; }

/* 表格 */
.table-wrap { flex: 1; min-height: 200px; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th {
  text-align: left; padding: 12px 14px; font-size: 12px; font-weight: 600;
  color: #64748b; border-bottom: 1.5px solid #e2e8f0;
}
.data-table td {
  padding: 12px 14px; font-size: 13px; color: #334155;
  border-bottom: 1px solid #f1f5f9; vertical-align: middle;
}
.data-table tbody tr:hover { background: #f8fafc; }
.data-table tbody tr.row-closed { opacity: 0.55; }
.data-table tbody tr.row-closed:hover { opacity: 0.8; }
.data-table tbody tr.row-expired { background: #fffbfb; }
.data-table tbody tr.row-expired:hover { background: #fff5f5; }

.patient-name { font-weight: 600; color: #1e293b; }
.remind-title { color: #1e293b; }
.remind-title.closed { text-decoration: line-through; color: #94a3b8; }
.remind-content { font-size: 12px; color: #94a3b8; margin-left: 4px; }

.type-tag {
  display: inline-block; padding: 2px 8px; border-radius: 5px;
  font-size: 12px; font-weight: 600;
}
.time-text { font-size: 12px; color: #64748b; }
.status-tag {
  display: inline-block; padding: 2px 8px; border-radius: 5px;
  font-size: 12px; font-weight: 600;
}

.action-btns { display: flex; gap: 6px; }
.act-btn {
  padding: 4px 12px; border-radius: 6px; font-size: 12px; font-weight: 600;
  cursor: pointer; border: 1.5px solid transparent; transition: all 0.15s;
}
.act-close { color: #64748b; background: #f8fafc; border-color: #e2e8f0; }
.act-close:hover { background: #f1f5f9; }
.act-reopen { color: #1e40af; background: #eff6ff; border-color: #bfdbfe; }
.act-reopen:hover { background: #dbeafe; }
.act-delete { color: #dc2626; background: #fef2f2; border-color: #fecaca; }
.act-delete:hover { background: #fee2e2; }

/* 空状态 */
.empty-box { display: flex; flex-direction: column; align-items: center; padding: 64px 0; gap: 12px; }
.empty-text { font-size: 15px; color: #94a3b8; font-weight: 500; margin: 0; }

/* 分页 */
.pagination-wrap { display: flex; justify-content: center; margin-top: 20px; padding-top: 16px; border-top: 1px solid #f1f5f9; }

/* ===== 行内表单（与患者端一致） ===== */
.remind-form {
  margin-top: 18px; padding: 18px 22px; background: #f8fafc;
  border-radius: 14px; border: 1.5px solid #e2e8f0;
}
.form-row { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.form-row :deep(.el-select .el-input__wrapper) {
  height: 42px; border-radius: 10px; box-shadow: 0 0 0 1.5px #e2e8f0;
}
.form-row :deep(.el-date-editor .el-input__wrapper) {
  height: 42px; border-radius: 10px; box-shadow: 0 0 0 1.5px #e2e8f0;
}

.text-input {
  height: 42px; border-radius: 10px; border: 1.5px solid #cbd5e1;
  padding: 0 14px; font-size: 14px; color: #1e293b;
  outline: none; background: #fff; transition: border-color 0.2s;
  box-sizing: border-box;
}
.text-input:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }
.text-input::placeholder { color: #94a3b8; }

.save-btn {
  height: 42px; padding: 0 28px; border-radius: 10px; font-size: 14px; font-weight: 600;
  border: none; cursor: pointer; transition: all 0.2s;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: #fff; box-shadow: 0 4px 12px rgba(37,99,235,0.3);
  white-space: nowrap;
}
.save-btn:hover { box-shadow: 0 6px 16px rgba(37,99,235,0.4); transform: translateY(-1px); }
.save-btn:disabled { opacity: 0.5; cursor: not-allowed; transform: none; }

/* 已选患者 */
.selected-tag-inline {
  display: inline-flex; align-items: center; gap: 8px;
  height: 42px; padding: 0 12px; background: #eff6ff;
  border-radius: 10px; border: 1.5px solid #bfdbfe;
  font-size: 14px; font-weight: 600; color: #1e40af;
}
.selected-tag-phone { font-size: 12px; color: #64748b; font-weight: 400; }
.selected-tag-close {
  margin-left: 4px; width: 20px; height: 20px; border-radius: 50%;
  border: none; background: #dbeafe; color: #1e40af;
  font-size: 11px; cursor: pointer; display: flex; align-items: center; justify-content: center;
}
.selected-tag-close:hover { background: #bfdbfe; }

/* 患者下拉 */
.patient-dropdown {
  position: absolute; top: 100%; left: 0; z-index: 10;
  margin-top: 4px; min-width: 260px;
  border: 1.5px solid #e2e8f0; border-radius: 10px;
  background: #fff; box-shadow: 0 6px 20px rgba(0,0,0,0.1);
  overflow: hidden;
}
.patient-dropdown-item {
  padding: 10px 14px; display: flex; align-items: center; gap: 12px;
  cursor: pointer; transition: background 0.15s;
  border-bottom: 1px solid #f1f5f9;
}
.patient-dropdown-item:last-child { border-bottom: none; }
.patient-dropdown-item:hover { background: #eff6ff; }
.pick-name { font-weight: 600; color: #1e293b; }
.pick-info { font-size: 12px; color: #94a3b8; }

/* 动画 */
.slide-enter-active, .slide-leave-active { transition: all 0.3s ease; }
.slide-enter-from, .slide-leave-to { opacity: 0; transform: translateY(-10px); }

::deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background: linear-gradient(135deg, #60a5fa, #3b82f6);
  border-radius: 8px;
}
</style>

<style>
/* 筛选栏状态下拉框 — 与搜索输入框样式一致 */
.status-select {
  width: 200px; height: 36px;
  padding: 0 8px; font-size: 13px; color: #1e293b;
  border: 1.5px solid #cbd5e1; border-radius: 8px;
  background: #fff; outline: none; cursor: pointer;
  box-sizing: border-box;
  appearance: auto;
}
.status-select:focus { border-color: #3b82f6; }
</style>
