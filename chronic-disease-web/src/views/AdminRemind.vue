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
const pageSize = ref(10)
const searchName = ref('')

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

function fmtTime(val) {
  if (!val) return ''
  return val.replace('T', ' ').substring(0, 16)
}

// ====== 新建提醒弹窗 ======
function openCreateDialog() {
  form.title = ''
  form.remindType = 'custom'
  form.remindTime = ''
  form.repeatType = 'none'
  form.content = ''
  patientSearch.value = ''
  patientList.value = []
  selectedPatient.value = null
  showDialog.value = true
}

async function searchPatient() {
  if (!patientSearch.value.trim()) return
  patientSearching.value = true
  try {
    const data = await getArchivePage({ patientName: patientSearch.value.trim(), pageNum: 1, pageSize: 10 })
    patientList.value = data.records || []
  } catch (e) {
    ElMessage.error(e.message || '查询患者失败')
  } finally {
    patientSearching.value = false
  }
}

function selectPatient(p) {
  selectedPatient.value = p
  patientList.value = []
}

async function handleSubmit() {
  if (!selectedPatient.value) { ElMessage.warning('请先选择患者'); return }
  if (!form.title.trim()) { ElMessage.warning('请输入提醒标题'); return }
  if (!form.remindTime) { ElMessage.warning('请选择提醒时间'); return }
  submitting.value = true
  try {
    await addRemindForPatient({ userId: selectedPatient.value.userId, ...form })
    ElMessage.success('提醒创建成功')
    showDialog.value = false
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
        <button class="create-btn" @click="openCreateDialog">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          新建提醒
        </button>
      </div>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" class="search-icon"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
        <input
          v-model="searchName"
          class="search-input"
          placeholder="按患者姓名搜索提醒"
          @keyup.enter="handleSearch"
        />
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
      <div v-if="total > pageSize" class="page-wrap">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :total="total"
          :page-size="pageSize"
          :current-page="pageNum"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- ====== 新建提醒弹窗 ====== -->
    <el-dialog v-model="showDialog" title="为患者创建提醒" width="560px" :close-on-click-modal="false" destroy-on-close>
      <!-- 选择患者 -->
      <div class="dialog-section">
        <div class="section-label">选择患者</div>
        <div v-if="!selectedPatient" class="patient-pick">
          <div class="pick-search">
            <input
              v-model="patientSearch"
              class="text-input"
              placeholder="输入患者姓名搜索"
              @keyup.enter="searchPatient"
            />
            <button class="pick-btn" @click="searchPatient" :disabled="patientSearching">
              {{ patientSearching ? '搜索中...' : '搜索' }}
            </button>
          </div>
          <div v-if="patientList.length > 0" class="pick-results">
            <div
              v-for="p in patientList"
              :key="p.id"
              class="pick-item"
              @click="selectPatient(p)"
            >
              <span class="pick-name">{{ p.patientName }}</span>
              <span class="pick-info">{{ p.chronicType || '-' }} · {{ p.gender === 1 ? '男' : p.gender === 2 ? '女' : '-' }} · {{ p.phone || '-' }}</span>
            </div>
          </div>
        </div>
        <div v-else class="selected-patient">
          <span class="selected-label">已选择：</span>
          <span class="selected-name">{{ selectedPatient.patientName }}</span>
          <span class="selected-info">{{ selectedPatient.phone || '' }}</span>
          <button class="change-btn" @click="selectedPatient = null">更换</button>
        </div>
      </div>

      <!-- 提醒信息 -->
      <div class="dialog-section" v-if="selectedPatient">
        <div class="section-label">提醒信息</div>
        <div class="form-grid">
          <div class="form-item full">
            <label>提醒标题</label>
            <input v-model="form.title" class="text-input" placeholder="如：服用降压药" maxlength="50" />
          </div>
          <div class="form-item">
            <label>提醒类型</label>
            <el-select v-model="form.remindType" size="large" style="width:100%">
              <el-option label="用药" value="medicine" />
              <el-option label="复查" value="recheck" />
              <el-option label="自定义" value="custom" />
            </el-select>
          </div>
          <div class="form-item">
            <label>重复方式</label>
            <el-select v-model="form.repeatType" size="large" style="width:100%">
              <el-option label="不重复" value="none" />
              <el-option label="每天" value="daily" />
              <el-option label="每周" value="weekly" />
              <el-option label="每月" value="monthly" />
            </el-select>
          </div>
          <div class="form-item full">
            <label>提醒时间</label>
            <el-date-picker
              v-model="form.remindTime"
              type="datetime"
              size="large"
              style="width:100%"
              placeholder="选择提醒时间"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </div>
          <div class="form-item full">
            <label>备注详情</label>
            <input v-model="form.content" class="text-input" placeholder="选填" maxlength="200" />
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :disabled="!selectedPatient" :loading="submitting" @click="handleSubmit">创建提醒</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.admin-remind { height: 100%; }

.page-card {
  background: #fff; border-radius: 16px;
  box-shadow: 0 1px 12px rgba(0,0,0,0.04);
  padding: 28px 32px; height: 100%; display: flex; flex-direction: column;
}

/* 头部 */
.page-head { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 20px; }
.head-left { display: flex; flex-direction: column; gap: 4px; }
.page-title { font-size: 18px; font-weight: 700; color: #1e293b; }
.page-desc { font-size: 13px; color: #94a3b8; }

.create-btn {
  display: flex; align-items: center; gap: 6px;
  height: 38px; padding: 0 20px; border-radius: 10px;
  border: none; cursor: pointer; font-size: 13px; font-weight: 600;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: #fff; box-shadow: 0 4px 12px rgba(37,99,235,0.3);
  transition: all 0.2s; white-space: nowrap;
}
.create-btn:hover { box-shadow: 0 6px 18px rgba(37,99,235,0.4); transform: translateY(-1px); }

/* 搜索栏 */
.search-bar {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 14px; background: #f8fafc;
  border: 1.5px solid #e2e8f0; border-radius: 10px; margin-bottom: 18px;
}
.search-icon { color: #94a3b8; flex-shrink: 0; }
.search-input {
  flex: 1; border: none; background: transparent; outline: none;
  font-size: 14px; color: #1e293b;
}
.search-input::placeholder { color: #94a3b8; }
.search-btn {
  height: 32px; padding: 0 16px; border-radius: 7px;
  border: none; cursor: pointer; font-size: 13px; font-weight: 600;
  background: #3b82f6; color: #fff; transition: background 0.2s;
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
.page-wrap { display: flex; justify-content: flex-end; margin-top: 18px; }

/* ====== 弹窗 ====== */
.dialog-section { margin-bottom: 18px; }
.section-label { font-size: 14px; font-weight: 600; color: #1e293b; margin-bottom: 10px; }

/* 选择患者 */
.pick-search { display: flex; gap: 10px; }
.pick-btn {
  height: 42px; padding: 0 18px; border-radius: 10px;
  border: none; cursor: pointer; font-size: 13px; font-weight: 600;
  background: #3b82f6; color: #fff;
}
.pick-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.pick-results {
  margin-top: 10px; border: 1.5px solid #e2e8f0; border-radius: 10px;
  overflow: hidden;
}
.pick-item {
  padding: 10px 14px; display: flex; align-items: center; gap: 12px;
  cursor: pointer; transition: background 0.15s;
  border-bottom: 1px solid #f1f5f9;
}
.pick-item:last-child { border-bottom: none; }
.pick-item:hover { background: #eff6ff; }
.pick-name { font-weight: 600; color: #1e293b; }
.pick-info { font-size: 12px; color: #94a3b8; }

.selected-patient {
  display: flex; align-items: center; gap: 10px;
  padding: 12px 14px; background: #eff6ff; border-radius: 10px;
  border: 1.5px solid #bfdbfe;
}
.selected-label { font-size: 13px; color: #64748b; }
.selected-name { font-weight: 700; color: #1e40af; font-size: 14px; }
.selected-info { font-size: 12px; color: #64748b; }
.change-btn {
  margin-left: auto; padding: 4px 14px; border-radius: 6px;
  border: 1.5px solid #bfdbfe; background: #fff; color: #1e40af;
  font-size: 12px; font-weight: 600; cursor: pointer;
}
.change-btn:hover { background: #dbeafe; }

/* 表单 */
.form-grid { display: flex; flex-wrap: wrap; gap: 12px; }
.form-item { width: calc(50% - 6px); }
.form-item.full { width: 100%; }
.form-item label { display: block; font-size: 13px; color: #64748b; margin-bottom: 4px; font-weight: 500; }

.text-input {
  height: 42px; border-radius: 10px; border: 1.5px solid #cbd5e1;
  padding: 0 14px; font-size: 14px; color: #1e293b;
  outline: none; background: #fff; transition: border-color 0.2s;
  box-sizing: border-box; width: 100%;
}
.text-input:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }
.text-input::placeholder { color: #94a3b8; }

:deep(.el-select .el-input__wrapper) { height: 42px; border-radius: 10px; box-shadow: 0 0 0 1.5px #cbd5e1; }
:deep(.el-date-editor .el-input__wrapper) { height: 42px; border-radius: 10px; box-shadow: 0 0 0 1.5px #cbd5e1; }
:deep(.el-dialog) { border-radius: 16px; }
:deep(.el-dialog__header) { padding: 24px 28px 0; }
:deep(.el-dialog__title) { font-size: 17px; font-weight: 700; color: #1e293b; }
:deep(.el-dialog__body) { padding: 16px 28px; }
:deep(.el-dialog__footer) { padding: 0 28px 24px; }
</style>
