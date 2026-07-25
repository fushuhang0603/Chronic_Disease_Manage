<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getDoctorProfilePage, addDoctorProfile, editDoctorProfile,
  getDoctorProfileById, deleteDoctorProfile, getDoctorUserList
} from '../api/user.js'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const query = reactive({
  doctorName: '',
  hospital: '',
  department: '',
  pageNum: 1,
  pageSize: 20,
})

const dialogVisible = ref(false)
const dialogMode = ref('add')
const formRef = ref(null)
const submitLoading = ref(false)
const editLoading = ref(false)
const doctorUserOptions = ref([])

const addForm = reactive({
  id: null,
  doctorId: null,
  realName: '',
  title: '',
  hospital: '',
  department: '',
  specialty: '',
  introduction: '',
})

const titleOptions = [
  { label: '主任医师', value: '主任医师' },
  { label: '副主任医师', value: '副主任医师' },
  { label: '主治医师', value: '主治医师' },
  { label: '住院医师', value: '住院医师' },
  { label: '医师', value: '医师' },
]

const addRules = reactive({
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  title: [{ required: true, message: '请选择职称', trigger: 'change' }],
  hospital: [{ required: true, message: '请输入所属医院', trigger: 'blur' }],
  department: [{ required: true, message: '请输入科室', trigger: 'blur' }],
  specialty: [{ required: true, message: '请输入擅长领域', trigger: 'blur' }],
})

async function fetchDoctorUsers() {
  try {
    const res = await getDoctorUserList()
    doctorUserOptions.value = (res || []).map(u => ({
      id: u.id,
      username: u.username,
      nickname: u.nickname,
      phone: u.phone,
      avatar: u.avatar,
    }))
  } catch (e) {
    console.error('获取医生列表失败', e)
  }
}

function handleAdd() {
  dialogMode.value = 'add'
  addForm.id = null
  addForm.doctorId = null
  addForm.realName = ''
  addForm.title = ''
  addForm.hospital = ''
  addForm.department = ''
  addForm.specialty = ''
  addForm.introduction = ''
  formRef.value?.resetFields()
  fetchDoctorUsers()
  dialogVisible.value = true
}

async function handleEdit(row) {
  dialogMode.value = 'edit'
  formRef.value?.resetFields()
  dialogVisible.value = true
  editLoading.value = true
  try {
    const profile = await getDoctorProfileById(row.doctorId)
    addForm.id = profile.id
    addForm.doctorId = profile.doctorId
    addForm.realName = profile.realName
    addForm.title = profile.title
    addForm.hospital = profile.hospital
    addForm.department = profile.department
    addForm.specialty = profile.specialty
    addForm.introduction = profile.introduction || ''
    doctorUserOptions.value = [{
      id: profile.doctorId,
      username: row.doctorName || '',
      nickname: profile.realName || '',
      phone: '',
      avatar: '',
    }]
  } catch (e) {
    ElMessage.error(e.message || '查询失败')
    dialogVisible.value = false
  } finally {
    editLoading.value = false
  }
}

async function handleSubmit() {
  if (!addForm.doctorId) {
    ElMessage.warning('请选择医生账号')
    return
  }
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    const payload = {
      id: addForm.id,
      doctorId: addForm.doctorId,
      realName: addForm.realName,
      title: addForm.title,
      hospital: addForm.hospital,
      department: addForm.department,
      specialty: addForm.specialty,
      introduction: addForm.introduction,
    }
    if (dialogMode.value === 'add') {
      await addDoctorProfile(payload)
      ElMessage.success('新增成功')
    } else {
      await editDoctorProfile(payload)
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
      doctorName: query.doctorName || undefined,
      hospital: query.hospital || undefined,
      department: query.department || undefined,
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    }
    const res = await getDoctorProfilePage(params)
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
  query.doctorName = ''
  query.hospital = ''
  query.department = ''
  query.pageNum = 1
  fetchData()
}
function handlePage(p) { query.pageNum = p; fetchData() }
function handleSize(s) { query.pageSize = s; query.pageNum = 1; fetchData() }

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定要删除医生「${row.realName || row.doctorName}」的资历信息吗？删除后不可恢复。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消' }
    )
    await deleteDoctorProfile(row.id)
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
        <span class="card-label">医生筛选</span>
      </div>
      <el-form :model="query" inline class="search-form">
        <el-form-item label="医生姓名">
          <el-input v-model="query.doctorName" placeholder="输入真实姓名搜索" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="所属医院">
          <el-input v-model="query.hospital" placeholder="医院名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="科室">
          <el-input v-model="query.department" placeholder="科室名称" clearable @keyup.enter="handleSearch" />
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
        <span class="card-icon" style="background:linear-gradient(135deg,#34d399,#10b981)">
          <el-icon :size="18"><UserFilled /></el-icon>
        </span>
        <span class="card-label">医生资历列表</span>
        <span class="card-tip">共 {{ total }} 条记录</span>
        <el-button type="primary" class="btn-add" @click="handleAdd">新增医生</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe border class="um-table">
        <el-table-column prop="doctorName" label="医生账号" width="140" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column label="职称" width="120" align="center">
          <template #default="{ row }">
            <span class="title-tag">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="hospital" label="所属医院" min-width="160" />
        <el-table-column prop="department" label="科室" width="120" />
        <el-table-column prop="specialty" label="擅长领域" min-width="180" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="170" align="center" />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-cell">
              <el-button size="small" class="btn-action" @click="handleEdit(row)">编辑</el-button>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :close-on-click-modal="false" width="560px" class="add-dialog">
      <template #header>
        <span class="dialog-title">{{ dialogMode === 'add' ? '新增医生资历' : '编辑医生资历' }}</span>
      </template>

      <el-form ref="formRef" :model="addForm" :rules="addRules" label-position="top" class="add-form" v-loading="editLoading">
        <div class="form-section">
          <div class="section-label">选择医生账号</div>
          <div class="doctor-card-grid" v-if="dialogMode === 'add'">
            <div
              v-for="doc in doctorUserOptions"
              :key="doc.id"
              :class="['doctor-card', { selected: addForm.doctorId === doc.id }]"
              @click="addForm.doctorId = doc.id"
            >
              <div class="doc-avatar-sm">{{ (doc.nickname || doc.username || '医')[0] }}</div>
              <span class="doc-name-sm">{{ doc.nickname || doc.username }}</span>
              <span class="doc-username-sm">{{ doc.username }}</span>
              <span class="doc-phone-sm" v-if="doc.phone">{{ doc.phone }}</span>
              <el-icon v-if="addForm.doctorId === doc.id" class="doc-check-sm" :size="16"><CircleCheckFilled /></el-icon>
            </div>
            <div v-if="doctorUserOptions.length === 0" class="doctor-empty">
              <el-icon :size="24"><User /></el-icon>
              <span>暂无可选的医生账号</span>
            </div>
          </div>
          <div v-if="dialogMode === 'edit'" class="doctor-card readonly">
            <div class="doc-avatar-sm">{{ (doctorUserOptions[0]?.nickname || doctorUserOptions[0]?.username || '医')[0] }}</div>
            <span class="doc-name-sm">{{ doctorUserOptions[0]?.nickname || doctorUserOptions[0]?.username }}</span>
            <span class="doc-username-sm">{{ doctorUserOptions[0]?.username }}</span>
          </div>
        </div>

        <div class="divider"></div>

        <div class="form-section">
          <div class="section-label">基本信息</div>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="真实姓名" prop="realName">
                <el-input v-model="addForm.realName" placeholder="请输入真实姓名" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="职称" prop="title">
                <el-select v-model="addForm.title" placeholder="请选择职称" style="width:100%">
                  <el-option v-for="o in titleOptions" :key="o.value" :label="o.label" :value="o.value" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <div class="form-section">
          <div class="section-label">执业信息</div>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="所属医院" prop="hospital">
                <el-input v-model="addForm.hospital" placeholder="如 北京协和医院" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="科室" prop="department">
                <el-input v-model="addForm.department" placeholder="如 内分泌科" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="擅长领域" prop="specialty">
            <el-input v-model="addForm.specialty" placeholder="如 糖尿病、高血压、甲状腺疾病" />
          </el-form-item>
          <el-form-item label="简介">
            <el-input
              v-model="addForm.introduction"
              type="textarea"
              :rows="3"
              placeholder="医生简介（选填）"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button class="btn-cancel" @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" class="btn-submit" :loading="submitLoading" @click="handleSubmit">确认</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.um-root { display: flex; flex-direction: column; gap: 20px; }

.um-card {
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 4px 24px rgba(249,115,22,0.06);
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
  background: linear-gradient(135deg, #fb923c, #f97316);
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.card-icon .el-icon { color: #fff; }
.card-label { font-size: 16px; font-weight: 700; color: #7c2d12; }
.card-tip { font-size: 13px; color: #a8a29e; margin-left: auto; }

.search-form { margin-bottom: 0; }
.search-form :deep(.el-form-item) { margin-bottom: 16px; }
.search-form :deep(.el-form-item__label) { font-size: 13px; font-weight: 600; color: #78716c; }
.search-form :deep(.el-input) { width: 180px; }
.search-form :deep(.el-select) { width: 160px; }
.search-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px #fde68a;
  background: #fffbeb;
}
.search-form :deep(.el-input__wrapper:hover) { box-shadow: 0 0 0 1px #fbbf24; }
.search-form :deep(.el-input.is-focus .el-input__wrapper) { box-shadow: 0 0 0 1px #f97316; }

.btn-search {
  height: 38px;
  border-radius: 10px;
  background: linear-gradient(135deg, #f97316, #ea580c);
  border: none;
  box-shadow: 0 2px 10px rgba(234,88,12,0.25);
  transition: all 0.3s;
}
.btn-search:hover {
  background: linear-gradient(135deg, #ea580c, #c2410c);
  box-shadow: 0 4px 16px rgba(234,88,12,0.35);
  transform: translateY(-1px);
}
.btn-reset {
  height: 38px; border-radius: 10px;
  border: 1px solid #fde68a; color: #78716c;
}

.btn-add {
  height: 34px; border-radius: 10px; margin-left: auto;
  background: linear-gradient(135deg, #f97316, #ea580c);
  border: none; box-shadow: 0 2px 8px rgba(234,88,12,0.25);
}
.btn-add:hover {
  background: linear-gradient(135deg, #ea580c, #c2410c);
}

.um-table { margin-bottom: 0; }
.um-table :deep(th) {
  background: #fffbeb;
  color: #78716c;
  font-weight: 600;
  font-size: 13px;
}
.um-table :deep(td) { font-size: 13px; color: #44403c; }
.um-table :deep(.el-table__cell) {
  border-right-color: #fef3c7;
  border-bottom-color: #fef3c7;
}
.um-table :deep(.el-table__header-wrapper th) {
  border-right-color: #fef3c7;
  border-bottom-color: #fde68a;
}

.title-tag {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  background: rgba(249,115,22,0.12); color: #c2410c;
}

.action-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}
.btn-action {
  height: 28px; border-radius: 8px; font-size: 12px; padding: 0 10px;
  border: 1px solid #fde68a; color: #78716c; background: #fff;
}
.btn-action:hover { border-color: #f97316; color: #c2410c; background: #fff7ed; }
.btn-action.btn-del { color: #dc2626; border-color: #fca5a5; }
.btn-action.btn-del:hover { background: #fef2f2; border-color: #f87171; color: #b91c1c; }

.pagination-wrap {
  display: flex; justify-content: center; margin-top: 16px;
}

.add-dialog :deep(.el-dialog__header) { border-bottom: 1px solid #fef3c7; padding: 20px 24px 16px; }
.add-dialog :deep(.el-dialog__body) { padding: 20px 24px 4px; }
.dialog-title { font-size: 17px; font-weight: 700; color: #7c2d12; }

.form-section { margin-bottom: 4px; }
.section-label {
  font-size: 12px; font-weight: 700; color: #a8a29e;
  text-transform: uppercase; letter-spacing: 1px;
  margin-bottom: 12px;
}
.divider {
  height: 1px; background: #fef3c7;
  margin: 8px 0 16px;
}

.doctor-card-grid {
  display: flex; flex-direction: column; gap: 4px;
  max-height: 200px; overflow-y: auto;
  padding-right: 4px;
}
.doctor-card-grid::-webkit-scrollbar { width: 4px; }
.doctor-card-grid::-webkit-scrollbar-track { background: transparent; }
.doctor-card-grid::-webkit-scrollbar-thumb { background: #d6d3d1; border-radius: 4px; }

.doctor-card {
  display: flex; align-items: center; gap: 8px;
  padding: 5px 10px;
  border-radius: 8px;
  border: 1.5px solid #fef3c7;
  background: #fff;
  cursor: pointer;
  transition: all 0.15s;
}
.doctor-card:hover { border-color: #fbbf24; background: #fffdf7; }
.doctor-card.selected {
  border-color: #f97316;
  background: #fff7ed;
}
.doctor-card.readonly { cursor: default; pointer-events: none; border-color: #fef3c7; background: #fffbeb; }

.doc-avatar-sm {
  width: 26px; height: 26px;
  border-radius: 6px;
  display: flex; align-items: center; justify-content: center;
  color: #fff;
  font-size: 12px; font-weight: 700;
  flex-shrink: 0;
  line-height: 1;
  background: linear-gradient(135deg, #fb923c, #f97316);
}
.doc-name-sm { font-size: 13px; font-weight: 600; color: #7c2d12; white-space: nowrap; }
.doc-username-sm { font-size: 11px; color: #a8a29e; }
.doc-phone-sm {
  font-size: 10px; color: #c2410c; font-weight: 500;
  background: rgba(249,115,22,0.08);
  padding: 1px 5px; border-radius: 5px;
}
.doc-check-sm { color: #f97316; flex-shrink: 0; margin-left: auto; }
.doctor-empty {
  display: flex; flex-direction: column; align-items: center; gap: 6px;
  padding: 20px 0; color: #a8a29e; font-size: 13px;
}

.add-form { margin-top: 8px; }
.add-form :deep(.el-form-item__label) { font-size: 13px; font-weight: 600; color: #78716c; }
.add-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px #fde68a;
}
.add-form :deep(.el-input__wrapper:hover) { box-shadow: 0 0 0 1px #fbbf24; }
.add-form :deep(.el-textarea__inner) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px #fde68a;
}
.add-form :deep(.el-textarea__inner:hover) { box-shadow: 0 0 0 1px #fbbf24; }

.dialog-footer { display: flex; gap: 10px; justify-content: flex-end; padding-top: 8px; }
.btn-cancel { height: 38px; border-radius: 10px; }
.btn-submit {
  height: 38px; border-radius: 10px;
  background: linear-gradient(135deg, #f97316, #ea580c);
  border: none; box-shadow: 0 2px 8px rgba(234,88,12,0.25);
}
.btn-submit:hover {
  background: linear-gradient(135deg, #ea580c, #c2410c);
}
</style>
