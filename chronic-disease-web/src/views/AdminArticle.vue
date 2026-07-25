<template>
  <div class="admin-root">
    <!-- 顶部 -->
    <div class="page-header">
      <div class="header-info">
        <h2 class="header-title">健康资讯管理</h2>
        <p class="header-sub">发布和管理慢病健康科普内容</p>
      </div>
      <button class="btn-create" @click="openDialog(null)">
        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        新建资讯
      </button>
    </div>

    <!-- 双栏 -->
    <div class="content-cols">
      <!-- 左：文章列表 -->
      <div class="col-main">
        <!-- 筛选 -->
        <div class="filter-bar">
          <div class="filter-left">
            <select v-model="filterCategory" @change="handleSearch" class="flt-select">
              <option value="">全部分类</option>
              <option value="饮食">饮食</option>
              <option value="运动">运动</option>
              <option value="用药">用药</option>
              <option value="慢病常识">慢病常识</option>
              <option value="并发症预防">并发症预防</option>
            </select>
            <select v-model="filterStatus" @change="handleSearch" class="flt-select">
              <option value="">全部状态</option>
              <option value="1">已上架</option>
              <option value="0">已下架</option>
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
                <th class="col-sm">分类</th>
                <th class="col-sm">阅读</th>
                <th class="col-sm">状态</th>
                <th class="col-md">发布时间</th>
                <th class="col-lg">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in records" :key="item.id" @click="viewDetail(item)">
                <td><span class="link-title">{{ item.title }}</span></td>
                <td><span class="cat-badge">{{ item.category }}</span></td>
                <td class="num-cell">{{ item.viewCount || 0 }}</td>
                <td><span class="status-badge" :class="item.status === 1 ? 'on' : 'off'">{{ item.status === 1 ? '上架' : '下架' }}</span></td>
                <td class="time-cell">{{ fmtTime(item.publishingTime) }}</td>
                <td @click.stop>
                  <div class="row-actions">
                    <button class="btn-row" @click="openDialog(item)">编辑</button>
                    <button v-if="item.status === 1" class="btn-row warn" @click="handleToggleStatus(item)">下架</button>
                    <button v-else class="btn-row success" @click="handleToggleStatus(item)">上架</button>
                    <button class="btn-row danger" @click="handleDelete(item)">删除</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
          <div v-else-if="!loading" class="empty-state">
            <p>暂无资讯数据</p>
          </div>
        </div>

        <div class="pagination-wrap">
          <el-pagination background layout="total, sizes, prev, pager, next" :total="total" :page-size="pageSize" :page-sizes="[10, 20, 50]" :current-page="pageNum" @current-change="handlePageChange" @size-change="handleSizeChange" />
        </div>
      </div>

      <!-- 右：排行 -->
      <div class="col-side">
        <div class="side-card">
          <div class="side-card-head">
            <span class="side-card-title">日收藏排行</span>
            <input v-model="rankDate" type="date" class="date-input" @change="fetchRank" />
          </div>
          <div v-loading="rankLoading" class="rank-list">
            <div v-if="rankRecords.length === 0 && !rankLoading" class="empty-state small">
              <p>暂无排行数据</p>
              <p class="hint">收藏文章后次日可见</p>
            </div>
            <div v-for="(item, idx) in rankRecords" :key="item.id" class="rank-item" @click="viewDetail(item)">
              <span class="rank-idx" :class="{ 'top3': idx < 3 }">{{ idx + 1 }}</span>
              <div class="rank-info">
                <span class="rank-title">{{ item.title }}</span>
                <span class="rank-meta">{{ item.category }} · {{ item.favoriteCount || 0 }} 收藏 · {{ item.viewCount || 0 }} 阅读</span>
              </div>
              <span class="rank-status" :class="item.status === 1 ? 'on' : 'off'">{{ item.status === 1 ? '上架' : '下架' }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="showDetail" title="资讯详情" width="700px" :close-on-click-modal="false">
      <div class="detail-wrap" v-if="detailItem">
        <h2 class="detail-title">{{ detailItem.title }}</h2>
        <div class="detail-meta">
          <span class="cat-badge">{{ detailItem.category }}</span>
          <span class="detail-meta-item">{{ detailItem.viewCount || 0 }} 阅读</span>
          <span class="detail-meta-item">{{ fmtTime(detailItem.publishingTime) }} 发布</span>
          <span class="status-badge" :class="detailItem.status === 1 ? 'on' : 'off'">{{ detailItem.status === 1 ? '已上架' : '已下架' }}</span>
        </div>
        <div class="detail-body">{{ detailItem.content }}</div>
      </div>
      <template #footer>
        <el-button @click="showDetail = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="showDialog" :title="editId ? '编辑资讯' : '新建资讯'" width="700px" :close-on-click-modal="false" destroy-on-close>
      <div class="form-wrap">
        <div class="form-item">
          <label>标题</label>
          <input v-model="form.title" class="text-input" placeholder="资讯标题" maxlength="100" />
        </div>
        <div class="form-row">
          <div class="form-item half">
            <label>分类</label>
            <select v-model="form.category" class="text-input">
              <option value="">选择分类</option>
              <option value="饮食">饮食</option>
              <option value="运动">运动</option>
              <option value="用药">用药</option>
              <option value="慢病常识">慢病常识</option>
              <option value="并发症预防">并发症预防</option>
            </select>
          </div>
          <div class="form-item half">
            <label>状态</label>
            <select v-model="form.status" class="text-input">
              <option :value="1">上架</option>
              <option :value="0">下架（草稿）</option>
            </select>
          </div>
        </div>
        <div class="form-item">
          <label>发布时间</label>
          <el-date-picker v-model="form.publishingTime" type="datetime" size="large" style="width:100%" placeholder="选择发布时间" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DD HH:mm:ss" />
        </div>
        <div class="form-item">
          <label>正文内容</label>
          <textarea v-model="form.content" class="text-area" rows="10" placeholder="输入文章正文内容（支持换行）" maxlength="50000"></textarea>
        </div>
      </div>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSave">{{ editId ? '保存修改' : '发布资讯' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getArticlePageAdmin, addArticle, editArticle, deleteArticle, updateArticleStatus, getAdminRank } from '../api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitting = ref(false)
const records = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const rankLoading = ref(false)
const rankRecords = ref([])
const rankDate = ref(new Date().toISOString().substring(0, 10))
const filterCategory = ref('')
const filterStatus = ref('')
const showDialog = ref(false)
const editId = ref(null)
const showDetail = ref(false)
const detailItem = ref(null)

const form = reactive({
  title: '', category: '', status: 1, publishingTime: '', content: ''
})

function fmtTime(t) {
  return t ? t.replace('T', ' ').substring(0, 16) : '-'
}

function viewDetail(item) {
  detailItem.value = item
  showDetail.value = true
}

async function fetchRecords() {
  loading.value = true
  try {
    const res = await getArticlePageAdmin({
      pageNum: pageNum.value, pageSize: pageSize.value,
      category: filterCategory.value || undefined,
      status: filterStatus.value !== '' ? Number(filterStatus.value) : undefined
    })
    records.value = res.records || []
    total.value = res.total || 0
  } finally { loading.value = false }
}

function handleSearch() { pageNum.value = 1; fetchRecords() }
function handlePageChange(p) { pageNum.value = p; fetchRecords() }
function handleSizeChange(s) { pageSize.value = s; pageNum.value = 1; fetchRecords() }

function openDialog(item) {
  if (item) {
    editId.value = item.id
    form.title = item.title
    form.category = item.category
    form.status = item.status
    form.publishingTime = item.publishingTime
    form.content = item.content
  } else {
    editId.value = null
    form.title = ''; form.category = ''; form.status = 1
    form.publishingTime = ''; form.content = ''
  }
  showDialog.value = true
}

async function handleSave() {
  if (!form.title.trim()) { ElMessage.warning('请输入标题'); return }
  if (!form.category) { ElMessage.warning('请选择分类'); return }
  if (!form.content.trim()) { ElMessage.warning('请输入正文'); return }
  submitting.value = true
  try {
    const data = { ...form }
    if (editId.value) {
      data.id = editId.value
      await editArticle(data)
      ElMessage.success('编辑成功')
    } else {
      await addArticle(data)
      ElMessage.success('发布成功')
    }
    showDialog.value = false
    fetchRecords()
    fetchRank()
  } finally { submitting.value = false }
}

async function handleToggleStatus(item) {
  const newStatus = item.status === 1 ? 0 : 1
  const label = newStatus === 1 ? '上架' : '下架'
  try {
    await ElMessageBox.confirm(`确定要${label}「${item.title}」吗？`, '提示', { type: 'warning' })
    await updateArticleStatus(item.id, newStatus)
    ElMessage.success(`已${label}`)
    fetchRecords()
    fetchRank()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

async function handleDelete(item) {
  try {
    await ElMessageBox.confirm(`确定要删除「${item.title}」吗？`, '确认删除', { type: 'error' })
    await deleteArticle(item.id)
    ElMessage.success('已删除')
    fetchRecords()
    fetchRank()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error(e.message || '删除失败')
    }
  }
}

async function fetchRank() {
  rankLoading.value = true
  try {
    rankRecords.value = await getAdminRank(rankDate.value, 20) || []
  } catch { /* ignore */ }
  rankLoading.value = false
}

onMounted(() => { fetchRecords(); fetchRank() })
</script>

<style scoped>
.admin-root { max-width: 1280px; margin: 0 auto; width: 100%; padding: 24px 24px 40px; box-sizing: border-box; }

/* 顶部 */
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

/* 双栏 */
.content-cols {
  display: flex; gap: 20px; align-items: flex-start;
}
.col-main { flex: 1; min-width: 0; }
.col-side { width: 340px; flex-shrink: 0; }

/* 筛选 */
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

/* 表格 */
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
.col-md { width: 130px; }
.col-lg { width: 200px; }
.link-title { font-weight: 600; color: #7c2d12; }
.link-title:hover { color: #f97316; }
.cat-badge {
  display: inline-block; padding: 2px 8px; border-radius: 5px;
  background: #fff7ed; color: #f97316; font-size: 12px; font-weight: 600;
}
.num-cell { color: #78716c; font-variant-numeric: tabular-nums; }
.time-cell { font-size: 12px; color: #a8a29e; }
.status-badge {
  display: inline-block; padding: 2px 8px; border-radius: 5px; font-size: 12px; font-weight: 600;
}
.status-badge.on { background: #ecfdf5; color: #059669; }
.status-badge.off { background: #fef2f2; color: #dc2626; }

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
.empty-state .hint { font-size: 12px; margin-top: 4px; color: #d6d3d1; }
.empty-state.small { padding: 40px 20px; }

.pagination-wrap { display: flex; justify-content: center; margin-top: 20px; padding-top: 16px; border-top: 1px solid #fef3c7; }

/* 排行侧栏 */
.side-card {
  background: #fff; border-radius: 14px; border: 1px solid #fef3c7;
  box-shadow: 0 1px 4px rgba(249,115,22,0.04); overflow: hidden;
}
.side-card-head {
  display: flex; align-items: center; justify-content: space-between; gap: 8px;
  padding: 16px 18px; border-bottom: 1px solid #fffbeb;
}
.side-card-title { font-size: 14px; font-weight: 700; color: #7c2d12; }
.date-input {
  padding: 4px 8px; border: 1.5px solid #fde68a; border-radius: 6px;
  font-size: 12px; color: #78716c; outline: none; background: #fffbeb;
}
.date-input:focus { border-color: #f97316; }

.rank-list { padding: 6px 0; min-height: 100px; }
.rank-item {
  display: flex; align-items: center; gap: 12px; padding: 10px 18px;
  border-bottom: 1px solid #fffbeb; transition: background 0.1s;
}
.rank-item:last-child { border-bottom: none; }
.rank-item:hover { background: #fff7ed; }

.rank-idx {
  width: 22px; height: 22px; border-radius: 6px; display: flex; align-items: center;
  justify-content: center; font-size: 12px; font-weight: 700; color: #a8a29e;
  background: #fef3c7; flex-shrink: 0;
}
.rank-idx.top3 { background: linear-gradient(135deg, #fbbf24, #f59e0b); color: #fff; }

.rank-info { flex: 1; min-width: 0; }
.rank-title {
  display: block; font-size: 13px; font-weight: 600; color: #7c2d12;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.rank-meta {
  display: block; font-size: 11px; color: #a8a29e; margin-top: 2px;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.rank-status {
  font-size: 11px; font-weight: 600; flex-shrink: 0;
  padding: 2px 6px; border-radius: 4px;
}
.rank-status.on { color: #059669; background: #ecfdf5; }
.rank-status.off { color: #dc2626; background: #fef2f2; }

/* 弹窗 */
.form-wrap { padding: 4px 0; }
.form-item { margin-bottom: 14px; }
.form-item label { display: block; font-size: 13px; color: #78716c; margin-bottom: 4px; font-weight: 500; }
.form-row { display: flex; gap: 12px; }
.form-item.half { flex: 1; }
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
.detail-title { font-size: 20px; font-weight: 700; color: #7c2d12; margin: 0 0 14px 0; line-height: 1.4; }
.detail-meta { display: flex; align-items: center; gap: 10px; margin-bottom: 18px; flex-wrap: wrap; }
.detail-meta-item { font-size: 12px; color: #a8a29e; }
.detail-body {
  font-size: 14px; color: #431407; line-height: 1.9; white-space: pre-wrap;
  background: #fffbeb; border-radius: 12px; padding: 20px 24px;
  border: 1px solid #fef3c7; min-height: 120px; max-height: 460px; overflow-y: auto;
}

/* 响应式 */
@media (max-width: 960px) {
  .content-cols { flex-direction: column; }
  .col-side { width: 100%; }
}

::deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background: linear-gradient(135deg, #f97316, #ea580c);
  border-radius: 8px;
}
</style>
