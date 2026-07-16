<template>
  <div class="admin-article">
    <div class="page-card">
      <div class="page-head">
        <div class="head-left">
          <span class="page-title">健康资讯管理</span>
          <span class="page-desc">发布和管理慢病健康科普内容</span>
        </div>
        <button class="create-btn" @click="openDialog(null)">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          新建资讯
        </button>
      </div>

      <!-- 筛选栏 -->
      <div class="filter-row">
        <select v-model="filterCategory" class="status-select" @change="handleSearch">
          <option value="">全部分类</option>
          <option value="饮食">饮食</option>
          <option value="运动">运动</option>
          <option value="用药">用药</option>
          <option value="慢病常识">慢病常识</option>
          <option value="并发症预防">并发症预防</option>
        </select>
        <select v-model="filterStatus" class="status-select" @change="handleSearch">
          <option value="">全部状态</option>
          <option value="1">已上架</option>
          <option value="0">已下架</option>
        </select>
        <button class="search-btn" @click="handleSearch">搜索</button>
      </div>

      <!-- 表格 -->
      <div v-loading="loading" class="table-wrap">
        <table class="data-table" v-if="records.length > 0">
          <thead>
            <tr>
              <th>标题</th>
              <th style="width:80px">分类</th>
              <th style="width:70px">浏览量</th>
              <th style="width:70px">状态</th>
              <th style="width:130px">发布时间</th>
              <th style="width:180px">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in records" :key="item.id" class="data-row" @click="viewDetail(item)">
              <td class="title-td">
                <span class="article-title">{{ item.title }}</span>
              </td>
              <td><span class="category-tag">{{ item.category }}</span></td>
              <td>{{ item.viewCount || 0 }}</td>
              <td>
                <span class="status-dot" :class="item.status === 1 ? 'on' : 'off'">
                  {{ item.status === 1 ? '上架' : '下架' }}
                </span>
              </td>
              <td class="time-text">{{ fmtTime(item.publishingTime) }}</td>
              <td>
                <div class="action-btns" @click.stop>
                  <button class="act-btn act-edit" @click="openDialog(item)">编辑</button>
                  <button v-if="item.status === 1" class="act-btn act-off" @click="handleToggleStatus(item)">下架</button>
                  <button v-else class="act-btn act-on" @click="handleToggleStatus(item)">上架</button>
                  <button class="act-btn act-delete" @click="handleDelete(item)">删除</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-else-if="!loading" class="empty-box">
          <p class="empty-text">暂无资讯数据</p>
        </div>
      </div>

      <div v-if="total > pageSize" class="page-wrap">
        <el-pagination background layout="total, prev, pager, next" :total="total" :page-size="pageSize" :current-page="pageNum" @current-change="handlePageChange" />
      </div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="showDetail" title="资讯详情" width="700px" :close-on-click-modal="false">
      <div class="detail-wrap" v-if="detailItem">
        <h2 class="detail-title">{{ detailItem.title }}</h2>
        <div class="detail-meta">
          <span class="category-tag">{{ detailItem.category }}</span>
          <span class="detail-meta-text">{{ detailItem.viewCount || 0 }} 阅读</span>
          <span class="detail-meta-text">{{ fmtTime(detailItem.publishingTime) }} 发布</span>
          <span class="status-dot" :class="detailItem.status === 1 ? 'on' : 'off'">
            {{ detailItem.status === 1 ? '已上架' : '已下架' }}
          </span>
        </div>
        <div class="detail-content">{{ detailItem.content }}</div>
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
import { getArticlePage, addArticle, editArticle, deleteArticle, updateArticleStatus } from '../api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitting = ref(false)
const records = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
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
    const res = await getArticlePage({
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
  } catch { /* 取消 */ }
}

async function handleDelete(item) {
  try {
    await ElMessageBox.confirm(`确定要删除「${item.title}」吗？`, '确认删除', { type: 'error' })
    await deleteArticle(item.id)
    ElMessage.success('已删除')
    fetchRecords()
  } catch { /* 取消 */ }
}

onMounted(() => fetchRecords())
</script>

<style scoped>
.admin-article { max-width: 1100px; margin: 0 auto; width: 100%; padding-bottom: 32px; }
.page-card {
  background: #fff; border-radius: 20px;
  box-shadow: 0 2px 20px rgba(0,0,0,0.05);
  padding: 28px 32px; border: 1px solid #f1f5f9;
}

.page-head { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 18px; }
.head-left { display: flex; flex-direction: column; gap: 4px; }
.page-title { font-size: 18px; font-weight: 700; color: #1e293b; }
.page-desc { font-size: 13px; color: #94a3b8; }
.create-btn {
  display: flex; align-items: center; gap: 6px;
  height: 38px; padding: 0 18px; border-radius: 10px;
  border: 1.5px solid #cbd5e1; background: #fff;
  color: #334155; font-size: 13px; font-weight: 600; cursor: pointer;
}
.create-btn:hover { border-color: #3b82f6; color: #3b82f6; background: #eff6ff; }

.filter-row { display: flex; align-items: center; gap: 10px; margin-bottom: 18px; }
.status-select {
  height: 36px; padding: 0 10px; border-radius: 8px;
  border: 1.5px solid #cbd5e1; background: #fff;
  font-size: 13px; color: #334155; outline: none; cursor: pointer;
}
.search-btn {
  height: 36px; padding: 0 16px; border-radius: 8px;
  border: none; cursor: pointer; font-size: 13px; font-weight: 600;
  background: #3b82f6; color: #fff;
}

.table-wrap { min-height: 200px; }
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
.data-row { cursor: pointer; }
.article-title { font-weight: 600; color: #1e293b; }
.category-tag {
  display: inline-block; padding: 2px 8px; border-radius: 5px;
  background: #eff6ff; color: #1d4ed8; font-size: 12px; font-weight: 600;
}
.time-text { font-size: 12px; color: #94a3b8; }

.status-dot {
  display: inline-block; padding: 2px 8px; border-radius: 5px; font-size: 12px; font-weight: 600;
}
.status-dot.on { background: #ecfdf5; color: #059669; }
.status-dot.off { background: #fef2f2; color: #dc2626; }

.action-btns { display: flex; gap: 6px; }
.act-btn {
  padding: 4px 12px; border-radius: 6px; font-size: 12px; font-weight: 600;
  cursor: pointer; border: 1.5px solid transparent;
}
.act-edit { color: #334155; background: #f8fafc; border-color: #e2e8f0; }
.act-edit:hover { background: #f1f5f9; }
.act-on { color: #059669; background: #ecfdf5; border-color: #a7f3d0; }
.act-on:hover { background: #d1fae5; }
.act-off { color: #d97706; background: #fffbeb; border-color: #fde68a; }
.act-off:hover { background: #fef3c7; }
.act-delete { color: #dc2626; background: #fef2f2; border-color: #fecaca; }
.act-delete:hover { background: #fee2e2; }

.empty-box { display: flex; justify-content: center; padding: 64px 0; }
.empty-text { color: #94a3b8; font-size: 15px; }

.page-wrap { display: flex; justify-content: center; margin-top: 18px; }

/* 弹窗 */
.form-wrap { padding: 4px 0; }
.form-item { margin-bottom: 14px; }
.form-item label { display: block; font-size: 13px; color: #64748b; margin-bottom: 4px; font-weight: 500; }
.form-row { display: flex; gap: 12px; }
.form-item.half { flex: 1; }
.text-input {
  width: 100%; height: 42px; border-radius: 10px; border: 1.5px solid #cbd5e1;
  padding: 0 14px; font-size: 14px; color: #1e293b; outline: none; box-sizing: border-box;
  background: #fff;
}
.text-input:focus { border-color: #3b82f6; }
.text-area {
  width: 100%; border-radius: 10px; border: 1.5px solid #cbd5e1;
  padding: 12px 14px; font-size: 14px; color: #1e293b; outline: none;
  box-sizing: border-box; resize: vertical; font-family: inherit;
}
.text-area:focus { border-color: #3b82f6; }

:deep(.el-dialog) { border-radius: 16px; }
:deep(.el-dialog__header) { padding: 24px 28px 0; }
:deep(.el-dialog__title) { font-size: 17px; font-weight: 700; }
:deep(.el-dialog__body) { padding: 16px 28px; }
:deep(.el-dialog__footer) { padding: 0 28px 24px; }

/* 详情弹窗 */
.detail-wrap { padding: 4px 0; }
.detail-title { font-size: 20px; font-weight: 700; color: #1e293b; margin: 0 0 14px 0; line-height: 1.4; }
.detail-meta { display: flex; align-items: center; gap: 12px; margin-bottom: 18px; flex-wrap: wrap; }
.detail-meta-text { font-size: 12px; color: #94a3b8; }
.detail-content {
  font-size: 14px; color: #334155; line-height: 1.9; white-space: pre-wrap;
  background: #f8fafc; border-radius: 12px; padding: 20px 24px;
  border: 1px solid #f1f5f9; min-height: 120px; max-height: 460px; overflow-y: auto;
}
</style>
