<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addRemind, getRemindPage, updateRemindStatus, deleteRemind,
} from '../api/user.js'

// ====== 状态 ======
const loading = ref(false)
const submitting = ref(false)
const showForm = ref(false)
const activeStatus = ref(0)

const records = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)

const form = reactive({
  title: '',
  remindType: 'custom',
  remindTime: '',
  repeatType: 'none',
  content: '',
})

// ====== 映射 ======
const typeMap = {
  medicine: { label: '用药', color: '#f59e0b', bg: '#fffbeb' },
  recheck: { label: '复查', color: '#f97316', bg: '#fff7ed' },
  custom: { label: '自定义', color: '#8b5cf6', bg: '#f5f3ff' },
}
const repeatMap = {
  none: '不重复',
  daily: '每天',
  weekly: '每周',
  monthly: '每月',
}
const statusTabs = [
  { status: null, label: '全部' },
  { status: 0, label: '待提醒' },
  { status: 2, label: '已读' },
  { status: 3, label: '已完成' },
  { status: 5, label: '已过期' },
  { status: 4, label: '已关闭' },
]

// ====== 方法 ======
async function fetchRecords() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (activeStatus.value !== null) params.remindStatus = activeStatus.value
    const data = await getRemindPage(params)
    records.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function switchTab(status) {
  activeStatus.value = status
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

async function handleSubmit() {
  if (!form.title.trim()) { ElMessage.warning('请输入提醒标题'); return }
  if (!form.remindTime) { ElMessage.warning('请选择提醒时间'); return }
  submitting.value = true
  try {
    await addRemind({ ...form })
    ElMessage.success('提醒创建成功')
    form.title = ''; form.content = ''; form.remindTime = ''
    form.remindType = 'custom'; form.repeatType = 'none'
    showForm.value = false
    pageNum.value = 1
    fetchRecords()
  } catch (e) {
    ElMessage.error(e.message || '创建失败')
  } finally {
    submitting.value = false
  }
}

async function handleRead(row) {
  try {
    await updateRemindStatus(row.id, 2)
    ElMessage.success('已标记为已读')
    fetchRecords()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

async function handleComplete(row) {
  try {
    await updateRemindStatus(row.id, 3)
    ElMessage.success('已完成提醒')
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

async function handleClose(row) {
  try {
    await updateRemindStatus(row.id, 4)
    ElMessage.success('提醒已关闭')
    fetchRecords()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

async function handleReopen(row) {
  try {
    await updateRemindStatus(row.id, 0)
    ElMessage.success('提醒已重新启用')
    fetchRecords()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

function fmtTime(val) {
  if (!val) return ''
  return val.replace('T', ' ').substring(0, 16)
}

onMounted(() => fetchRecords())
</script>

<template>
  <div class="remind-root">
    <!-- ====== 新建提醒入口 ====== -->
    <div class="input-card">
      <div class="card-head">
        <span class="head-label">用药提醒</span>
        <span class="head-hint">设置用药、复查提醒，不再遗忘</span>
        <button class="add-btn" @click="showForm = !showForm">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          {{ showForm ? '收起' : '新建提醒' }}
        </button>
      </div>

      <!-- 新建表单 -->
      <Transition name="slide">
        <div v-if="showForm" class="remind-form">
          <div class="form-row">
            <input
              v-model="form.title"
              class="text-input"
              style="width:260px"
              placeholder="提醒标题，如：服用二甲双胍"
              maxlength="50"
            />
            <el-select v-model="form.remindType" size="large" style="width:130px">
              <el-option label="用药" value="medicine" />
              <el-option label="复查" value="recheck" />
              <el-option label="自定义" value="custom" />
            </el-select>
            <el-date-picker
              v-model="form.remindTime"
              type="datetime"
              size="large"
              style="width:210px"
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
            <input
              v-model="form.content"
              class="text-input"
              style="flex:1"
              placeholder="备注详情（选填）"
              maxlength="200"
            />
          </div>
        </div>
      </Transition>
    </div>

    <!-- ====== 状态标签页 ====== -->
    <div class="list-card">
      <div class="card-head">
        <span class="head-label">提醒列表</span>
      </div>
      <div class="status-tabs">
        <button
          v-for="tab in statusTabs"
          :key="tab.status"
          :class="['status-tab', { active: activeStatus === tab.status }]"
          @click="switchTab(tab.status)"
        >
          {{ tab.label }}
        </button>
      </div>

      <!-- 提醒列表 -->
      <div v-loading="loading" class="remind-list">
        <div
          v-for="item in records"
          :key="item.id"
          class="remind-row"
          :class="{ closed: item.remindStatus === 4, expired: item.remindStatus === 5 }"
        >
          <div class="row-left">
            <div class="row-type-dot" :style="{ background: typeMap[item.remindType]?.color || '#78716c' }"></div>
            <div class="row-info">
              <div class="row-title-row">
                <span class="row-title">{{ item.title }}</span>
                <span class="row-type-label">{{ (typeMap[item.remindType] || typeMap.custom).label }}</span>
                <span v-if="item.repeatType !== 'none'" class="row-repeat-label">{{ repeatMap[item.repeatType] }}</span>
              </div>
              <div class="row-meta">
                <svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2" class="row-meta-icon"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                <span>{{ fmtTime(item.remindTime) }}</span>
                <span v-if="item.content" class="row-desc">— {{ item.content }}</span>
              </div>
            </div>
          </div>
          <div class="row-actions">
            <template v-if="item.remindStatus === 4">
              <button class="row-action-btn row-btn-blue" @click="handleReopen(item)">重新启用</button>
              <button class="row-action-btn row-btn-red" @click="handleDelete(item)">删除</button>
            </template>
            <template v-else-if="item.remindStatus === 5">
              <button class="row-action-btn row-btn-blue" @click="handleReopen(item)">重新启用</button>
              <button class="row-action-btn row-btn-gray" @click="handleClose(item)">关闭</button>
            </template>
            <template v-else>
              <button
                v-if="item.remindStatus === 0 || item.remindStatus === 1"
                class="row-action-btn row-btn-blue"
                @click="handleRead(item)"
              >标记已读</button>
              <button
                v-if="item.remindStatus === 2"
                class="row-action-btn row-btn-green"
                @click="handleComplete(item)"
              >完成</button>
              <button
                class="row-action-btn row-btn-gray"
                @click="handleClose(item)"
              >关闭</button>
            </template>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="!loading && records.length === 0" class="empty-box">
          <el-icon :size="64" color="#a8a29e"><AlarmClock /></el-icon>
          <p class="empty-text">暂无提醒</p>
          <p class="empty-sub">点击上方「新建提醒」添加用药或复查提醒</p>
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
.remind-root {
  display: flex; flex-direction: column; gap: 20px;
  max-width: 860px; margin: 0 auto; width: 100%; padding-bottom: 32px;
}

/* ===== 卡片通用 ===== */
.input-card, .list-card {
  background: #fff; border-radius: 20px;
  box-shadow: 0 2px 20px rgba(0,0,0,0.05);
  padding: 28px 32px; border: 1px solid #fef3c7;
}
.card-head { display: flex; align-items: baseline; gap: 12px; margin-bottom: 18px; }
.head-label { font-size: 17px; font-weight: 700; color: #431407; }
.head-hint { font-size: 13px; color: #a8a29e; flex: 1; }

.add-btn {
  display: flex; align-items: center; gap: 6px;
  height: 38px; padding: 0 18px; border-radius: 10px;
  border: 1.5px solid #a8a29e; background: #fff;
  color: #431407; font-size: 13px; font-weight: 600; cursor: pointer;
  transition: all 0.2s;
}
.add-btn:hover { border-color: #f97316; color: #f97316; background: #fff7ed; }

/* ===== 表单 ===== */
.remind-form {
  padding: 18px 22px; background: #fffbeb;
  border-radius: 14px; border: 1.5px solid #fef3c7;
}
.form-row { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.form-row :deep(.el-input__wrapper) {
  border-radius: 10px; box-shadow: 0 0 0 1.5px #fef3c7;
}
.form-row :deep(.el-select .el-input__wrapper) { height: 42px; }
.form-row :deep(.el-date-editor .el-input__wrapper) { height: 42px; }

.text-input {
  height: 42px; border-radius: 10px; border: 1.5px solid #a8a29e;
  padding: 0 14px; font-size: 14px; color: #431407;
  outline: none; background: #fff; transition: border-color 0.2s;
  box-sizing: border-box;
}
.text-input:focus { border-color: #f97316; box-shadow: 0 0 0 3px rgba(249,115,22,0.1); }
.text-input::placeholder { color: #a8a29e; }

.save-btn {
  height: 42px; padding: 0 28px; border-radius: 10px; font-size: 14px; font-weight: 600;
  border: none; cursor: pointer; transition: all 0.2s;
  background: linear-gradient(135deg, #f97316, #c2410c);
  color: #fff; box-shadow: 0 4px 12px rgba(194,65,12,0.3);
  white-space: nowrap;
}
.save-btn:hover { box-shadow: 0 6px 16px rgba(194,65,12,0.4); transform: translateY(-1px); }
.save-btn:disabled { opacity: 0.5; cursor: not-allowed; transform: none; }

/* ===== 状态标签页 ===== */
.status-tabs {
  display: flex; gap: 4px; background: #fef3c7;
  border-radius: 10px; padding: 3px; width: fit-content;
  margin-bottom: 18px;
}
.status-tab {
  padding: 6px 18px; border: none; background: transparent;
  font-size: 13px; font-weight: 500; color: #78716c;
  cursor: pointer; border-radius: 8px; transition: all 0.2s;
}
.status-tab:hover:not(.active) { color: #431407; }
.status-tab.active { background: #fff; color: #431407; font-weight: 600; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }

/* ===== 提醒列表（长条状） ===== */
.remind-list {
  min-height: 120px;
  display: flex; flex-direction: column; gap: 8px;
}

.remind-row {
  display: flex; align-items: center; justify-content: space-between; gap: 16px;
  padding: 14px 20px;
  background: #fff7ed; border-radius: 10px;
  border: 2px solid #fbbf24;
  transition: all 0.15s;
}
.remind-row:hover {
  background: #fff;
  border-color: #9a3412;
  box-shadow: 0 2px 12px rgba(194,65,12,0.15);
}

.row-left {
  display: flex; align-items: center; gap: 14px;
  flex: 1; min-width: 0;
}

.row-type-dot {
  width: 10px; height: 10px; border-radius: 50%;
  flex-shrink: 0;
}

.row-info {
  flex: 1; min-width: 0;
  display: flex; flex-direction: column; gap: 4px;
}

.row-title-row {
  display: flex; align-items: center; gap: 8px; flex-wrap: wrap;
}
.row-title {
  font-size: 14px; font-weight: 600; color: #431407;
}

.row-type-label {
  display: inline-block; padding: 0 8px; border-radius: 5px;
  font-size: 11px; font-weight: 600; color: #78716c; background: #fef3c7;
  line-height: 1.6;
}
.row-repeat-label {
  display: inline-block; padding: 0 6px; border-radius: 4px;
  font-size: 11px; color: #78716c; background: #fffbeb; border: 1px solid #fef3c7;
  font-weight: 500;
}
.row-meta {
  display: flex; align-items: center; gap: 5px;
  font-size: 12px; color: #a8a29e;
}
.row-meta-icon { color: #a8a29e; flex-shrink: 0; }
.row-desc {
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  max-width: 260px;
}

.row-actions {
  display: flex; gap: 8px; flex-shrink: 0;
}
.row-action-btn {
  padding: 5px 14px; border-radius: 7px;
  font-size: 12px; font-weight: 600; cursor: pointer;
  transition: all 0.15s; border: 1.5px solid transparent;
}
.row-btn-blue {
  color: #9a3412; background: #fff7ed; border-color: #fde68a;
}
.row-btn-blue:hover { background: #fef3c7; }
.row-btn-green {
  color: #d97706; background: #fffbeb; border-color: #fde68a;
}
.row-btn-green:hover { background: #fef3c7; }
.row-btn-red {
  color: #dc2626; background: #fef2f2; border-color: #fecaca;
}
.row-btn-red:hover { background: #fee2e2; }
.row-btn-gray {
  color: #78716c; background: #fffbeb; border-color: #fef3c7;
}
.row-btn-gray:hover { background: #fef3c7; }

/* ===== 已关闭状态 ===== */
.remind-row.closed {
  background: #fffbeb;
  border-color: #fef3c7;
  opacity: 0.7;
}
.remind-row.closed:hover {
  background: #fff;
  border-color: #a8a29e;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  opacity: 1;
}
.remind-row.closed .row-type-dot {
  background: #a8a29e !important;
}
.remind-row.closed .row-title {
  color: #a8a29e;
  text-decoration: line-through;
}
.remind-row.closed .row-meta {
  color: #a8a29e;
}

/* ===== 已过期状态 ===== */
.remind-row.expired {
  background: #fef2f2;
  border-color: #fecaca;
}
.remind-row.expired:hover {
  background: #fff;
  border-color: #dc2626;
  box-shadow: 0 2px 12px rgba(220,38,38,0.1);
}
.remind-row.expired .row-type-dot {
  background: #dc2626 !important;
}
.remind-row.expired .row-title {
  color: #991b1b;
}

/* ===== 空状态 ===== */
.empty-box {
  display: flex; flex-direction: column; align-items: center;
  padding: 64px 0; gap: 12px;
}
.empty-text { font-size: 16px; color: #a8a29e; font-weight: 500; margin: 0; }
.empty-sub { font-size: 13px; color: #a8a29e; margin: 0; }

/* ===== 分页 ===== */
.pagination-wrap { display: flex; justify-content: center; margin-top: 20px; padding-top: 16px; border-top: 1px solid #fef3c7; }

/* ===== 动画 ===== */
.slide-enter-active, .slide-leave-active { transition: all 0.3s ease; }
.slide-enter-from, .slide-leave-to { opacity: 0; transform: translateY(-10px); }

::deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background: linear-gradient(135deg, #fb923c, #f97316);
  border-radius: 8px;
}
</style>
