<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDictPage, addDict, editDict, getDictById, deleteDict, updateDictStatus } from '../api/user.js'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const query = reactive({
  indexCode: '',
  indexName: '',
  termType: '',
  status: null,
  pageNum: 1,
  pageSize: 20,
})

const queryTermTypeOptions = [
  { label: '全部类型', value: '' },
  { label: '指标', value: 'indicator' },
  { label: '疾病', value: 'disease' },
  { label: '药品', value: 'medicine' },
]
const circleTypeOptions = [
  { label: '指标', value: 'indicator', color: '#3b82f6', bg: 'rgba(59,130,246,0.08)' },
  { label: '疾病', value: 'disease', color: '#ef4444', bg: 'rgba(239,68,68,0.08)' },
  { label: '药品', value: 'medicine', color: '#10b981', bg: 'rgba(16,185,129,0.08)' },
]
const termTypeLabelMap = {
  indicator: '指标',
  disease: '疾病',
  medicine: '药品',
}
function getTermTypeLabel(type) {
  return termTypeLabelMap[type] || type
}

const statusOptions = [
  { label: '全部状态', value: null },
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 },
]

// 新增/编辑弹窗
const dialogVisible = ref(false)
const dialogMode = ref('add')
const formRef = ref(null)
const submitLoading = ref(false)
const editLoading = ref(false)
const addForm = reactive({
  id: null, indexCode: '', indexName: '', termType: 'indicator',
  sort: 0, status: 1,
})
const addRules = reactive({
  indexCode: [{ required: true, message: '请输入术语编码', trigger: 'blur' }],
  indexName: [{ required: true, message: '请输入术语名称', trigger: 'blur' }],
  termType: [{ required: true, message: '请选择术语类型', trigger: 'change' }],
  sort: [{ required: true, message: '请输入排序值', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
})

function handleAdd() {
  dialogMode.value = 'add'
  addForm.id = null
  addForm.indexCode = ''; addForm.indexName = ''; addForm.termType = 'indicator'
  addForm.sort = 0; addForm.status = 1
  formRef.value?.resetFields()
  dialogVisible.value = true
}

async function handleEdit(row) {
  dialogMode.value = 'edit'
  formRef.value?.resetFields()
  dialogVisible.value = true
  editLoading.value = true
  try {
    const dict = await getDictById(row.id)
    addForm.id = dict.id; addForm.indexCode = dict.indexCode; addForm.indexName = dict.indexName
    addForm.termType = dict.termType || 'indicator'
    addForm.sort = dict.sort; addForm.status = dict.status
  } catch (e) {
    ElMessage.error(e.message || '查询失败')
    dialogVisible.value = false
  } finally {
    editLoading.value = false
  }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    const payload = { ...addForm }
    if (dialogMode.value === 'add') {
      await addDict(payload)
      ElMessage.success('新增成功')
    } else {
      await editDict(payload)
      ElMessage.success('编辑成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

async function fetchData() {
  loading.value = true
  try {
    const params = {
      indexCode: query.indexCode || undefined,
      indexName: query.indexName || undefined,
      termType: query.termType || undefined,
      status: query.status,
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    }
    const res = await getDictPage(params)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    ElMessage.error(e.message || '查询失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() { query.pageNum = 1; fetchData() }
function handleReset() {
  query.indexCode = ''; query.indexName = ''; query.termType = ''
  query.status = null; query.pageNum = 1
  fetchData()
}
function handlePage(p) { query.pageNum = p; fetchData() }
function handleSize(s) { query.pageSize = s; query.pageNum = 1; fetchData() }

async function handleStatus(row) {
  const act = row.status === 1 ? '禁用' : '启用'
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await ElMessageBox.confirm(`确定要${act}术语「${row.indexName}」吗？`, '提示', { type: 'warning' })
    await updateDictStatus(row.id, newStatus)
    ElMessage.success(`已${act}`)
    fetchData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '操作失败')
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定要删除术语「${row.indexName}」吗？删除后不可恢复。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消' }
    )
    await deleteDict(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

onMounted(() => fetchData())
</script>

<template>
  <div class="um-root">
    <!-- 搜索卡片 -->
    <div class="um-card">
      <div class="card-title-row">
        <span class="card-icon"><el-icon :size="18"><Search /></el-icon></span>
        <span class="card-label">术语筛选</span>
      </div>
      <el-form :model="query" inline class="search-form">
        <el-form-item label="术语编码">
          <el-input v-model="query.indexCode" placeholder="模糊搜索" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="术语名称">
          <el-input v-model="query.indexName" placeholder="模糊搜索" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="术语类型">
          <el-select v-model="query.termType" clearable>
            <el-option v-for="o in queryTermTypeOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable>
            <el-option v-for="o in statusOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="btn-search" @click="handleSearch">查询</el-button>
          <el-button class="btn-reset" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 表格卡片 -->
    <div class="um-card">
      <div class="card-title-row">
        <span class="card-icon" style="background:linear-gradient(135deg,#a78bfa,#7c3aed)">
          <el-icon :size="18"><Collection /></el-icon>
        </span>
        <span class="card-label">术语字典列表</span>
        <span class="card-tip">共 {{ total }} 条记录</span>
        <el-button type="primary" class="btn-add" @click="handleAdd">新增术语</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe border class="um-table">
        <el-table-column prop="indexCode" label="编码" width="160" />
        <el-table-column prop="indexName" label="术语名称" width="160" />
        <el-table-column label="术语类型" width="120" align="center">
          <template #default="{ row }">
            <span :class="['type-tag', row.termType]">
              {{ getTermTypeLabel(row.termType) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <span :class="['status-dot', row.status === 1 ? 'on' : 'off']">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" align="center" />
        <el-table-column label="操作" min-width="220" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-cell">
              <el-button size="small" class="btn-action" @click="handleEdit(row)">编辑</el-button>
              <el-button size="small" :class="['btn-action', row.status === 1 ? 'btn-off' : 'btn-on']" @click="handleStatus(row)">
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button size="small" class="btn-action btn-del" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="handlePage"
          @size-change="handleSize"
        />
      </div>
    </div>
  </div>

  <!-- 新增/编辑弹窗 -->
  <el-dialog v-model="dialogVisible" :close-on-click-modal="false" width="420px" class="add-dialog">
    <template #header>
      <span class="dialog-title">{{ dialogMode === 'add' ? '新增术语' : '编辑术语' }}</span>
    </template>

    <el-form ref="formRef" :model="addForm" :rules="addRules" label-position="top" class="add-form" v-loading="editLoading">
      <el-form-item label="术语编码" prop="indexCode">
        <el-input v-model="addForm.indexCode" placeholder="英文编码，如 systolic" />
      </el-form-item>
      <el-form-item label="术语名称" prop="indexName">
        <el-input v-model="addForm.indexName" placeholder="中文名称，如 收缩压" />
      </el-form-item>
      <el-form-item label="术语类型" prop="termType">
        <div class="segmented">
          <button
            v-for="o in circleTypeOptions" :key="o.value"
            :class="['seg-item', { picked: addForm.termType === o.value }]"
            :style="addForm.termType === o.value ? { background: o.color, borderColor: o.color } : {}"
            @click="addForm.termType = o.value"
            type="button"
          >{{ o.label }}</button>
        </div>
      </el-form-item>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="排序" prop="sort">
            <el-input-number v-model="addForm.sort" :min="0" :max="999" controls-position="right" style="width:100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态" prop="status">
            <el-select v-model="addForm.status" style="width:100%">
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button class="btn-cancel" @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" class="btn-submit" :loading="submitLoading" @click="handleSubmit">确认</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.um-root { display: flex; flex-direction: column; gap: 20px; }

.um-card {
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 4px 24px rgba(59,130,246,0.06);
  padding: 24px 28px;
}

.card-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}
.card-icon {
  width: 34px; height: 34px;
  border-radius: 10px;
  background: linear-gradient(135deg, #60a5fa, #3b82f6);
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.card-icon .el-icon { color: #fff; }
.card-label { font-size: 16px; font-weight: 700; color: #1e3a5f; }
.card-tip { font-size: 13px; color: #94a3b8; margin-left: auto; }

.search-form { margin-bottom: 0; }
.search-form :deep(.el-form-item) { margin-bottom: 16px; }
.search-form :deep(.el-form-item__label) { font-size: 13px; font-weight: 600; color: #475569; }
.search-form :deep(.el-input) { width: 170px; }
.search-form :deep(.el-select) { width: 140px; }
.search-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px #e2e8f0;
  background: #fff;
}
.search-form :deep(.el-input__wrapper:hover) { box-shadow: 0 0 0 1px #93c5fd; }
.search-form :deep(.el-input.is-focus .el-input__wrapper) { box-shadow: 0 0 0 1px #60a5fa; }

.btn-search {
  height: 38px;
  border-radius: 10px;
  background: linear-gradient(135deg, #60a5fa, #3b82f6);
  border: none;
  box-shadow: 0 2px 10px rgba(59,130,246,0.2);
  transition: all 0.3s;
}
.btn-search:hover {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  box-shadow: 0 4px 16px rgba(59,130,246,0.3);
  transform: translateY(-1px);
}
.btn-reset {
  height: 38px; border-radius: 10px;
  border: 1px solid #e2e8f0; color: #475569;
}

.btn-add {
  height: 34px; border-radius: 10px; margin-left: auto;
  background: linear-gradient(135deg, #60a5fa, #3b82f6);
  border: none; box-shadow: 0 2px 8px rgba(59,130,246,0.2);
}

.um-table { margin-bottom: 0; }
.um-table :deep(th) {
  background: #f8fafc;
  color: #475569;
  font-weight: 600;
  font-size: 13px;
}
.um-table :deep(td) { font-size: 13px; color: #334155; }
.um-table :deep(.el-table__cell) {
  border-right-color: #d0d7de;
  border-bottom-color: #d0d7de;
}
.um-table :deep(.el-table__header-wrapper th) {
  border-right-color: #d0d7de;
  border-bottom-color: #c0c8d0;
}

/* 术语类型标签 */
.type-tag {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}
.type-tag.indicator { background: rgba(59,130,246,0.12); color: #1d4ed8; }
.type-tag.disease   { background: rgba(239,68,68,0.12); color: #dc2626; }
.type-tag.medicine  { background: rgba(16,185,129,0.12); color: #059669; }

.status-dot {
  display: inline-flex; align-items: center; gap: 5px;
  font-size: 13px; font-weight: 500;
}
.status-dot::before {
  content: ''; width: 7px; height: 7px; border-radius: 50%; flex-shrink: 0;
}
.status-dot.on  { color: #059669; } .status-dot.on::before  { background: #10b981; }
.status-dot.off { color: #dc2626; } .status-dot.off::before { background: #ef4444; }

.pagination-wrap { display: flex; justify-content: center; margin-top: 20px; padding-top: 16px; border-top: 1px solid #f1f5f9; }

.action-cell { display: flex; gap: 8px; justify-content: center; }
.btn-action {
  height: 28px; padding: 0 12px; border-radius: 6px;
  font-size: 12px; font-weight: 500;
  border: none; color: #fff;
  transition: all 0.2s;
}
.btn-action { background: #3b82f6; }
.btn-action:hover { background: #2563eb; }
.btn-action.btn-on { background: #16a34a; }
.btn-action.btn-on:hover { background: #15803d; }
.btn-action.btn-off { background: #dc2626; }
.btn-action.btn-off:hover { background: #b91c1c; }
.btn-action.btn-del { background: #ef4444; }
.btn-action.btn-del:hover { background: #dc2626; }

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background: linear-gradient(135deg, #60a5fa, #3b82f6);
  border-radius: 8px;
}

/* ====== 弹窗 ====== */
.add-dialog :deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
}
.add-dialog :deep(.el-dialog__header) {
  padding: 24px 28px 0;
  margin: 0;
}
.add-dialog :deep(.el-dialog__headerbtn) {
  top: 22px; right: 24px;
  width: 32px; height: 32px; border-radius: 8px;
}
.add-dialog :deep(.el-dialog__headerbtn:hover) { background: #f1f5f9; }
.add-dialog :deep(.el-dialog__body) {
  padding: 20px 28px 4px;
}
.add-dialog :deep(.el-dialog__footer) {
  padding: 16px 28px 24px;
}

.dialog-title {
  font-size: 17px; font-weight: 600; color: #1e293b; letter-spacing: -0.2px;
}

/* ---- 分段选择器 ---- */
.segmented {
  display: flex;
  gap: 8px;
}
.seg-item {
  flex: 1;
  padding: 9px 0;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  background: #fff;
  color: #64748b;
  font-size: 14px; font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
  line-height: 1;
}
.seg-item:hover {
  border-color: #cbd5e1;
  color: #475569;
}
.seg-item.picked {
  color: #fff;
  font-weight: 600;
  border-color: transparent;
}

/* ---- 表单 ---- */
.add-form :deep(.el-form-item) {
  margin-bottom: 18px;
}
.add-form :deep(.el-form-item__label) {
  font-size: 13px; font-weight: 600; color: #334155;
  margin-bottom: 6px;
}
.add-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px #e2e8f0;
  background: #fff;
  transition: all 0.2s;
}
.add-form :deep(.el-input__wrapper:hover) { box-shadow: 0 0 0 1px #cbd5e1; }
.add-form :deep(.el-input.is-focus .el-input__wrapper) { box-shadow: 0 0 0 2px rgba(59,130,246,0.25); }
.add-form :deep(.el-input-number .el-input__wrapper) { border-radius: 10px; }
.add-form :deep(.el-select .el-input__wrapper) { border-radius: 10px; }

/* ---- 底部按钮 ---- */
.dialog-footer {
  display: flex; justify-content: flex-end; gap: 12px;
}
.btn-cancel {
  height: 40px; border-radius: 10px; padding: 0 24px;
  border: 1px solid #e2e8f0; color: #475569; font-size: 14px; background: #fff;
  font-weight: 500;
}
.btn-cancel:hover { border-color: #cbd5e1; background: #f8fafc; }
.btn-submit {
  height: 40px; border-radius: 10px; padding: 0 28px; font-size: 14px; font-weight: 500;
  background: #3b82f6; border: none; color: #fff;
}
.btn-submit:hover { background: #2563eb; }
</style>
