<template>
  <div class="admin-root">
    <!-- 顶部 -->
    <div class="page-header">
      <div class="header-info">
        <h2 class="header-title">公告管理</h2>
        <p class="header-sub">发布系统公告，患者端首页可见</p>
      </div>
      <button class="btn-create" @click="openDialog">
        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        新建公告
      </button>
    </div>

    <!-- 筛选 -->
    <div class="filter-bar">
      <div class="filter-left">
        <select v-model="filterStatus" @change="handleSearch" class="flt-select">
          <option value="">全部状态</option>
          <option :value="0">草稿</option>
          <option :value="1">已发布</option>
          <option :value="2">已下线</option>
        </select>
      </div>
      <button class="btn-search" @click="handleSearch">筛选</button>
    </div>

    <!-- 表格 -->
    <div v-loading="loading" class="table-area">
      <table class="data-table" v-if="records.length > 0">
        <thead>
          <tr>
            <th>标题</th>
            <th class="col-sm">范围</th>
            <th class="col-sm">发布人</th>
            <th class="col-sm">状态</th>
            <th class="col-md">发布时间</th>
            <th class="col-lg">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in records" :key="item.id" @click="viewDetail(item)">
            <td><span class="link-title">{{ item.title }}</span></td>
            <td><span class="cat-badge">{{ scopeText(item.scope) }}</span></td>
            <td class="num-cell">{{ item.publisherName || '-' }}</td>
            <td><span class="status-badge" :class="statusClass(item.status)">{{ statusText(item.status) }}</span></td>
            <td class="time-cell">{{ fmtTime(item.publishTime) }}</td>
            <td @click.stop>
              <div class="row-actions">
                <button v-if="item.status === 0" class="btn-row success" @click="handleToggleStatus(item)">发布</button>
                <button v-if="item.status === 1" class="btn-row warn" @click="handleToggleStatus(item)">下线</button>
                <button v-if="item.status === 2" class="btn-row success" @click="handleToggleStatus(item)">重新发布</button>
                <button class="btn-row danger" @click="handleDelete(item)">删除</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else-if="!loading" class="empty-state">
        <p>暂无公告数据</p>
      </div>
    </div>

    <div class="pagination-wrap">
      <el-pagination background layout="total, prev, pager, next" :total="total" :page-size="pageSize" :current-page="pageNum" @current-change="handlePageChange" />
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="showDetail" :title="detailItem?.title" width="700px" :close-on-click-modal="false">
      <div class="detail-wrap" v-if="detailItem">
        <div class="detail-meta">
          <span class="cat-badge">{{ scopeText(detailItem.scope) }}</span>
          <span class="detail-meta-item">发布人：{{ detailItem.publisherName || '-' }}</span>
          <span class="detail-meta-item">{{ fmtTime(detailItem.publishTime) }}</span>
          <span class="status-badge" :class="statusClass(detailItem.status)">{{ statusText(detailItem.status) }}</span>
        </div>
        <div class="detail-body">{{ detailItem.content || '暂无内容' }}</div>
      </div>
      <template #footer>
        <el-button @click="showDetail = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 新增弹窗 -->
    <el-dialog v-model="showDialog" title="新建公告" width="700px" :close-on-click-modal="false" destroy-on-close>
      <div class="form-wrap">
        <div class="form-item">
          <label>公告标题</label>
          <input v-model="form.title" class="text-input" placeholder="公告标题" maxlength="200" />
        </div>
        <div class="form-item">
          <label>可见范围</label>
          <select v-model="form.scope" class="text-input">
            <option value="all">全部用户</option>
            <option value="patient">仅患者</option>
            <option value="doctor">仅医生</option>
          </select>
        </div>
        <div class="form-item">
          <label>公告内容</label>
          <textarea v-model="form.content" class="text-area" rows="10" placeholder="输入公告内容（支持换行）" maxlength="2000"></textarea>
        </div>
      </div>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSave">保存（草稿）</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getNoticePage, addNotice, updateNoticeStatus, deleteNotice } from '../api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitting = ref(false)
const records = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const filterStatus = ref('')
const showDialog = ref(false)
const showDetail = ref(false)
const detailItem = ref(null)

const form = reactive({
  title: '', scope: 'all', content: ''
})

function fmtTime(t) {
  return t ? t.replace('T', ' ').substring(0, 16) : '-'
}

function scopeText(scope) {
  return { all: '全部', patient: '患者', doctor: '医生' }[scope] || '全部'
}

function statusText(status) {
  return { 0: '草稿', 1: '已发布', 2: '已下线' }[status] || '-'
}

function statusClass(status) {
  if (status === 1) return 'on'
  if (status === 2) return 'off'
  return 'draft'
}

function viewDetail(item) {
  detailItem.value = item
  showDetail.value = true
}

async function fetchRecords() {
  loading.value = true
  try {
    const res = await getNoticePage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      status: filterStatus.value !== '' ? Number(filterStatus.value) : undefined
    })
    records.value = res.records || []
    total.value = res.total || 0
  } finally { loading.value = false }
}

function handleSearch() { pageNum.value = 1; fetchRecords() }
function handlePageChange(p) { pageNum.value = p; fetchRecords() }

function openDialog() {
  form.title = ''; form.scope = 'all'; form.content = ''
  showDialog.value = true
}

async function handleSave() {
  if (!form.title.trim()) { ElMessage.warning('请输入公告标题'); return }
  if (!form.content.trim()) { ElMessage.warning('请输入公告内容'); return }
  submitting.value = true
  try {
    // 携带当前登录管理员姓名（后端发布人ID取登录态）
    const raw = sessionStorage.getItem('userInfo')
    let publisherName = ''
    try { publisherName = (raw && JSON.parse(raw).name) || '' } catch {}
    await addNotice({ ...form, publisherName })
    ElMessage.success('保存成功')
    showDialog.value = false
    fetchRecords()
  } finally { submitting.value = false }
}

async function handleToggleStatus(item) {
  // 0草稿 → 1发布；1发布 → 2下线；2下线 → 1重新发布
  const newStatus = item.status === 1 ? 2 : 1
  const label = newStatus === 1 ? '发布' : '下线'
  try {
    await ElMessageBox.confirm(`确定要${label}「${item.title}」吗？`, '提示', { type: 'warning' })
    await updateNoticeStatus(item.id, newStatus)
    ElMessage.success(`已${label}`)
    fetchRecords()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

async function handleDelete(item) {
  try {
    await ElMessageBox.confirm(`确定要删除「${item.title}」吗？`, '确认删除', { type: 'error' })
    await deleteNotice(item.id)
    ElMessage.success('已删除')
    fetchRecords()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error(e.message || '删除失败')
    }
  }
}

onMounted(() => { fetchRecords() })
</script>

<style scoped>
.admin-root { max-width: 1280px; margin: 0 auto; width: 100%; padding: 24px 24px 40px; box-sizing: border-box; }

.page-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 20px; gap: 16px;
}
.header-title { font-size: 20px; font-weight: 700; color: #7c2d12; margin: 0; }
.header-sub { font-size: 13px; color: #a8a29e; margin: 2px 0 0 0; }
.btn-create {
  display: inline-flex; align-items: center; gap: 6px; flex-shrink: 0;
  height: 38px; padding: 0 20px; border-radius: 10px; font-size: 13px; font-weight: 600;
  border: none; cursor: pointer; background: linear-gradient(135deg, #f97316, #ea580c); color: #fff; transition: all 0.15s;
}
.btn-create:hover { background: linear-gradient(135deg, #ea580c, #c2410c); }

.filter-bar {
  display: flex; align-items: center; justify-content: space-between; gap: 10px;
  padding: 14px 18px; margin-bottom: 12px;
  background: #fff; border-radius: 14px; border: 1px solid #fef3c7;
  box-shadow: 0 1px 4px rgba(249,115,22,0.04);
}
.filter-left { display: flex; gap: 10px; }
.flt-select {
  height: 34px; padding: 0 10px; border-radius: 8px;
  border: 1.5px solid #fde68a; background: #fffbeb;
  font-size: 13px; color: #78716c; outline: none; cursor: pointer;
}
.flt-select:focus { border-color: #f97316; }
.btn-search {
  height: 34px; padding: 0 18px; border-radius: 8px; font-size: 13px; font-weight: 600;
  border: none; cursor: pointer; background: #fef3c7; color: #78716c;
}
.btn-search:hover { background: #fde68a; }

.table-area {
  background: #fff; border-radius: 14px; border: 1px solid #fef3c7;
  min-height: 200px; overflow: hidden;
  box-shadow: 0 1px 4px rgba(249,115,22,0.04);
}
.data-table { width: 100%; border-collapse: collapse; }
.data-table th {
  text-align: left; padding: 13px 16px; font-size: 11px; font-weight: 700;
  color: #a8a29e; text-transform: uppercase; letter-spacing: 0.5px;
  border-bottom: 1px solid #fef3c7; background: #fffbeb;
}
.data-table td {
  padding: 13px 16px; font-size: 13px; color: #431407;
  border-bottom: 1px solid #fffbeb; vertical-align: middle;
}
.data-table tbody tr { cursor: pointer; transition: background 0.1s; }
.data-table tbody tr:hover { background: #fff7ed; }
.col-sm { width: 72px; }
.col-md { width: 150px; }
.col-lg { width: 200px; }
.link-title { font-weight: 600; color: #7c2d12; }
.link-title:hover { color: #f97316; }
.cat-badge {
  display: inline-block; padding: 2px 8px; border-radius: 5px;
  background: #fff7ed; color: #f97316; font-size: 12px; font-weight: 600;
}
.num-cell { color: #78716c; }
.time-cell { font-size: 12px; color: #a8a29e; }
.status-badge {
  display: inline-block; padding: 2px 8px; border-radius: 5px; font-size: 12px; font-weight: 600;
}
.status-badge.on { background: #ecfdf5; color: #059669; }
.status-badge.off { background: #fef2f2; color: #dc2626; }
.status-badge.draft { background: #fffbeb; color: #d97706; }

.row-actions { display: flex; gap: 6px; }
.btn-row {
  padding: 3px 10px; border-radius: 6px; font-size: 12px; font-weight: 600;
  cursor: pointer; border: 1.5px solid #fde68a; background: #fff; color: #78716c;
}
.btn-row:hover { background: #fff7ed; border-color: #f97316; }
.btn-row.success { color: #059669; border-color: #a7f3d0; background: #f0fdf4; }
.btn-row.success:hover { background: #dcfce7; }
.btn-row.warn { color: #d97706; border-color: #fde68a; background: #fffbeb; }
.btn-row.warn:hover { background: #fef3c7; }
.btn-row.danger { color: #dc2626; border-color: #fecaca; background: #fef2f2; }
.btn-row.danger:hover { background: #fee2e2; }

.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 80px 20px; color: #a8a29e; }
.empty-state p { font-size: 14px; margin: 0; }

.pagination-wrap { display: flex; justify-content: center; margin-top: 20px; padding-top: 16px; border-top: 1px solid #fef3c7; }

.form-wrap { padding: 4px 0; }
.form-item { margin-bottom: 14px; }
.form-item label { display: block; font-size: 13px; color: #78716c; margin-bottom: 4px; font-weight: 500; }
.text-input {
  width: 100%; height: 42px; border-radius: 10px; border: 1.5px solid #fde68a;
  padding: 0 14px; font-size: 14px; color: #431407; outline: none; box-sizing: border-box;
  background: #fffbeb;
}
.text-input:focus { border-color: #f97316; }
.text-area {
  width: 100%; border-radius: 10px; border: 1.5px solid #fde68a;
  padding: 12px 14px; font-size: 14px; color: #431407; outline: none;
  box-sizing: border-box; resize: vertical; font-family: inherit;
}
.text-area:focus { border-color: #f97316; }

:deep(.el-dialog) { border-radius: 16px; }
:deep(.el-dialog__header) { padding: 24px 28px 0; border-bottom: 1px solid #fef3c7; }
:deep(.el-dialog__title) { font-size: 17px; font-weight: 700; color: #7c2d12; }
:deep(.el-dialog__body) { padding: 16px 28px; }
:deep(.el-dialog__footer) { padding: 0 28px 24px; }

.detail-wrap { padding: 4px 0; }
.detail-meta { display: flex; align-items: center; gap: 10px; margin-bottom: 18px; flex-wrap: wrap; }
.detail-meta-item { font-size: 12px; color: #a8a29e; }
.detail-body {
  font-size: 14px; color: #431407; line-height: 1.9; white-space: pre-wrap;
  background: #fffbeb; border-radius: 12px; padding: 20px 24px;
  border: 1px solid #fef3c7; min-height: 120px; max-height: 460px; overflow-y: auto;
}

::deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background: linear-gradient(135deg, #f97316, #ea580c);
  border-radius: 8px;
}
</style>
