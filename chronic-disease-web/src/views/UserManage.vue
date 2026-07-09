<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { getUserPage, addUser, editUser, getUserById, deleteUser, updateUserStatus } from '../api/user.js'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const query = reactive({
  username: '',
  nickname: '',
  phone: '',
  roleType: '',
  status: null,
  pageNum: 1,
  pageSize: 10,
})

const roleOptions = [
  { label: '全部角色', value: '' },
  { label: '超级管理员', value: 'admin' },
  { label: '医生', value: 'doctor' },
  { label: '患者', value: 'patient' },
]
const addRoleOptions = [
  { label: '超级管理员', value: 'admin' },
  { label: '医生', value: 'doctor' },
  { label: '患者', value: 'patient' },
]
const statusOptions = [
  { label: '全部状态', value: null },
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 },
]
const roleLabelMap = { admin: '管理员', doctor: '医生', patient: '患者' }

// 新增/编辑弹窗
const dialogVisible = ref(false)
const dialogMode = ref('add') // 'add' | 'edit'
const formRef = ref(null)
const submitLoading = ref(false)
const addForm = reactive({
  id: null,
  username: '',
  password: '',
  nickname: '',
  phone: '',
  roleType: 'patient',
  status: 1,
})
const addRules = reactive({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  roleType: [{ required: true, message: '请选择角色', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
})

function handleAdd() {
  dialogMode.value = 'add'
  addForm.id = null
  addForm.username = ''
  addForm.password = ''
  addForm.nickname = ''
  addForm.phone = ''
  addForm.roleType = 'patient'
  addForm.status = 1
  addRules.password = [{ required: true, message: '请输入密码', trigger: 'blur' }]
  formRef.value?.resetFields()
  dialogVisible.value = true
}

async function handleEdit(row) {
  dialogMode.value = 'edit'
  addRules.password = []
  formRef.value?.resetFields()
  const loading = ElLoading.service({ fullscreen: true, text: '加载中...' })
  try {
    const user = await getUserById(row.id)
    addForm.id = user.id
    addForm.username = user.username
    addForm.password = ''
    addForm.nickname = user.nickname
    addForm.phone = user.phone
    addForm.roleType = user.roleType
    addForm.status = user.status
    dialogVisible.value = true
  } catch (e) {
    ElMessage.error(e.message || '查询用户信息失败')
  } finally {
    loading.close()
  }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (dialogMode.value === 'add') {
      await addUser(addForm)
      ElMessage.success('新增用户成功')
    } else {
      const payload = { ...addForm }
      if (!payload.password) delete payload.password
      await editUser(payload)
      ElMessage.success('编辑用户成功')
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
      username: query.username || undefined,
      nickname: query.nickname || undefined,
      phone: query.phone || undefined,
      roleType: query.roleType || undefined,
      status: query.status,
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    }
    const res = await getUserPage(params)
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
  query.username = ''
  query.nickname = ''
  query.phone = ''
  query.roleType = ''
  query.status = null
  query.pageNum = 1
  fetchData()
}
function handlePage(p) { query.pageNum = p; fetchData() }
function handleSize(s) { query.pageSize = s; query.pageNum = 1; fetchData() }

async function handleStatus(row) {
  const act = row.status === 1 ? '禁用' : '启用'
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await ElMessageBox.confirm(
      `确定要${act}用户「${row.nickname || row.username}」吗？`,
      '提示',
      { type: 'warning' }
    )
    await updateUserStatus(row.id, newStatus)
    ElMessage.success(`已${act}`)
    fetchData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户「${row.nickname || row.username}」吗？删除后不可恢复。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消' }
    )
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '删除失败')
    }
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
        <span class="card-label">用户筛选</span>
      </div>
      <el-form :model="query" inline class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="query.username" placeholder="模糊搜索" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="query.nickname" placeholder="模糊搜索" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="query.phone" placeholder="模糊搜索" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="query.roleType" clearable>
            <el-option v-for="o in roleOptions" :key="o.value" :label="o.label" :value="o.value" />
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
        <span class="card-icon" style="background:linear-gradient(135deg,#93c5fd,#60a5fa)">
          <el-icon :size="18"><UserFilled /></el-icon>
        </span>
        <span class="card-label">用户列表</span>
        <span class="card-tip">共 {{ total }} 条记录</span>
        <el-button type="primary" class="btn-add" @click="handleAdd">新增用户</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe border class="um-table">
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="roleType" label="角色" width="110" align="center">
          <template #default="{ row }">
            <span :class="['role-tag', row.roleType]">
              {{ roleLabelMap[row.roleType] || row.roleType }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <span :class="['status-dot', row.status === 1 ? 'on' : 'off']">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" width="170" align="center" />
        <el-table-column prop="createTime" label="注册时间" width="170" align="center" />
        <el-table-column label="操作" width="240" align="center" fixed="right">
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
  <el-dialog v-model="dialogVisible" :close-on-click-modal="false" width="520px" class="add-dialog">
    <template #header>
      <div class="dialog-title">{{ dialogMode === 'add' ? '新增用户' : '编辑用户' }}</div>
    </template>

    <el-form ref="formRef" :model="addForm" :rules="addRules" label-position="top" class="add-form">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="addForm.username" placeholder="请输入用户名" size="large">
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="addForm.nickname" placeholder="请输入昵称" size="large">
              <template #prefix><el-icon><UserFilled /></el-icon></template>
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item :label="dialogMode === 'add' ? '密码' : '密码（不修改则不填）'" prop="password">
        <el-input v-model="addForm.password" type="password"
          :placeholder="dialogMode === 'add' ? '请输入密码' : '留空则不修改密码'"
          size="large" show-password>
          <template #prefix><el-icon><Lock /></el-icon></template>
        </el-input>
      </el-form-item>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="addForm.phone" placeholder="请输入手机号" size="large">
              <template #prefix><el-icon><Phone /></el-icon></template>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="角色" prop="roleType">
            <el-select v-model="addForm.roleType" size="large" style="width:100%">
              <el-option v-for="o in addRoleOptions" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="账号状态" prop="status">
        <el-radio-group v-model="addForm.status" class="status-radio">
          <el-radio-button :value="1">启 用</el-radio-button>
          <el-radio-button :value="0">禁 用</el-radio-button>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button size="large" class="btn-cancel" @click="dialogVisible = false">取 消</el-button>
        <el-button size="large" type="primary" class="btn-submit" :loading="submitLoading" @click="handleSubmit">确 定</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.um-root { display: flex; flex-direction: column; gap: 20px; }

/* === 卡片 === */
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

/* === 搜索表单 === */
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

/* 按钮 */
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

/* 新增按钮 */
.btn-add {
  height: 34px; border-radius: 10px; margin-left: auto;
  background: linear-gradient(135deg, #60a5fa, #3b82f6);
  border: none; box-shadow: 0 2px 8px rgba(59,130,246,0.2);
}

/* === 表格 === */
.um-table { margin-bottom: 0; }
.um-table :deep(th) {
  background: #f8fafc;
  color: #475569;
  font-weight: 600;
  font-size: 13px;
}
.um-table :deep(td) { font-size: 13px; color: #334155; }

/* 表格边框加深 */
.um-table :deep(.el-table__cell) {
  border-right-color: #d0d7de;
  border-bottom-color: #d0d7de;
}
.um-table :deep(.el-table__header-wrapper th) {
  border-right-color: #d0d7de;
  border-bottom-color: #c0c8d0;
}

/* 角色标签 */
.role-tag {
  display: inline-block;
  padding: 2px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}
.role-tag.admin  { background: rgba(239,68,68,0.12);  color: #dc2626; }
.role-tag.doctor { background: rgba(245,158,11,0.12); color: #b45309; }
.role-tag.patient{ background: rgba(59,130,246,0.12); color: #1d4ed8; }

/* 状态标签 */
.status-dot {
  display: inline-flex; align-items: center; gap: 5px;
  font-size: 13px; font-weight: 500;
}
.status-dot::before {
  content: ''; width: 7px; height: 7px; border-radius: 50%; flex-shrink: 0;
}
.status-dot.on  { color: #059669; } .status-dot.on::before  { background: #10b981; }
.status-dot.off { color: #dc2626; } .status-dot.off::before { background: #ef4444; }

/* 分页 */
.pagination-wrap { display: flex; justify-content: center; margin-top: 20px; padding-top: 16px; border-top: 1px solid #f1f5f9; }

/* 操作按钮 */
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

/* === 新增弹窗 === */
.add-dialog :deep(.el-dialog) {
  border-radius: 20px;
  overflow: hidden;
}
.dialog-title {
  display: flex; align-items: center; gap: 10px;
  font-size: 18px; font-weight: 700; color: #1e3a5f; padding-bottom: 8px;
}

.add-form :deep(.el-form-item__label) {
  font-size: 13px; font-weight: 600; color: #475569; padding-bottom: 4px;
}
.add-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px #e2e8f0;
  background: #fff;
}
.add-form :deep(.el-input__wrapper:hover) { box-shadow: 0 0 0 1px #93c5fd; }
.add-form :deep(.el-input.is-focus .el-input__wrapper) { box-shadow: 0 0 0 1px #3b82f6; }

.status-radio :deep(.el-radio-button__inner) {
  border-radius: 8px; padding: 8px 28px;
  border: 1px solid #e2e8f0; background: #fff; color: #475569;
  box-shadow: none;
}
.status-radio :deep(.el-radio-button.is-active .el-radio-button__inner) {
  background: linear-gradient(135deg, #60a5fa, #3b82f6);
  border-color: transparent; color: #fff;
  box-shadow: 0 2px 8px rgba(59,130,246,0.25);
}

.dialog-footer {
  display: flex; justify-content: flex-end; gap: 12px;
}
.btn-cancel {
  height: 42px; border-radius: 10px; padding: 0 24px;
  border: 1px solid #e2e8f0; color: #475569; font-size: 14px;
}
.btn-submit {
  height: 42px; border-radius: 10px; padding: 0 28px; font-size: 14px;
  background: linear-gradient(135deg, #60a5fa, #3b82f6);
  border: none; box-shadow: 0 2px 10px rgba(59,130,246,0.25);
}
.btn-submit:hover {
  box-shadow: 0 4px 16px rgba(59,130,246,0.35);
  transform: translateY(-1px);
}
</style>
