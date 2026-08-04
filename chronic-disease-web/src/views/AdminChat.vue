<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminChatRecords, getChatDayRecords } from '../api/user.js'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const query = reactive({
  patientName: '',
  doctorName: '',
  content: '',
  pageNum: 1,
  pageSize: 20,
})

const senderRoleMap = { PATIENT: '患者', DOCTOR: '医生' }
function roleLabel(role) {
  return senderRoleMap[role] || role
}

async function fetchData() {
  loading.value = true
  try {
    const params = {
      patientName: query.patientName || undefined,
      doctorName: query.doctorName || undefined,
      content: query.content || undefined,
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    }
    const res = await getAdminChatRecords(params)
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
  query.patientName = ''; query.doctorName = ''; query.content = ''
  query.pageNum = 1
  fetchData()
}
function handlePage(p) { query.pageNum = p; fetchData() }
function handleSize(s) { query.pageSize = s; query.pageNum = 1; fetchData() }

function fmtTime(val) {
  if (!val) return ''
  return val.replace('T', ' ').substring(0, 19)
}

// ====== 明细弹窗 ======
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailRecords = ref([])
const detailTitle = ref('')
const detailForm = reactive({ patientId: null, patientName: '', doctorId: null, doctorName: '', date: '' })

function handleDetail(row) {
  detailForm.patientId = row.patientId
  detailForm.patientName = row.patientName
  detailForm.doctorId = row.doctorId
  detailForm.doctorName = row.doctorName
  detailForm.date = (row.createTime || '').substring(0, 10)
  detailVisible.value = true
  fetchDetail()
}

async function fetchDetail() {
  if (!detailForm.date) { ElMessage.warning('请选择日期'); return }
  detailLoading.value = true
  try {
    const res = await getChatDayRecords({
      patientId: detailForm.patientId,
      doctorId: detailForm.doctorId,
      consultationTime: detailForm.date + ' 00:00:00',
    })
    detailRecords.value = res || []
  } catch (e) {
    ElMessage.error(e.message || '查询失败')
  } finally {
    detailLoading.value = false
  }
}

function detailTime(val) {
  if (!val) return ''
  return val.replace('T', ' ').substring(11, 16)
}

onMounted(() => fetchData())
</script>

<template>
  <div class="ac-root">
    <!-- 搜索卡片 -->
    <div class="um-card">
      <div class="card-title-row">
        <span class="card-icon"><el-icon :size="18"><Search /></el-icon></span>
        <span class="card-label">聊天记录筛选</span>
      </div>
      <el-form :model="query" inline class="search-form">
        <el-form-item label="患者姓名">
          <el-input v-model="query.patientName" placeholder="模糊搜索" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="医生姓名">
          <el-input v-model="query.doctorName" placeholder="模糊搜索" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="消息内容">
          <el-input v-model="query.content" placeholder="关键词搜索" clearable @keyup.enter="handleSearch" />
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
        <span class="card-icon" style="background:linear-gradient(135deg,#06b6d4,#0891b2)">
          <el-icon :size="18"><ChatDotRound /></el-icon>
        </span>
        <span class="card-label">全部聊天记录</span>
        <span class="card-tip">共 {{ total }} 条记录</span>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe border class="um-table">
        <el-table-column prop="patientId" label="患者ID" width="100" align="center" />
        <el-table-column prop="patientName" label="患者" width="100" align="center" />
        <el-table-column prop="doctorId" label="医生ID" width="100" align="center" />
        <el-table-column prop="doctorName" label="医生" width="100" align="center" />
        <el-table-column prop="senderId" label="发送方ID" width="100" align="center" />
        <el-table-column label="发送方" width="80" align="center">
          <template #default="{ row }">
            <span :class="['role-tag', row.senderRole === 'doctor' ? 'doctor' : 'patient']">
              {{ roleLabel(row.senderRole) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="消息内容" min-width="200">
          <template #default="{ row }">
            <span class="content-text">{{ row.content }}</span>
          </template>
        </el-table-column>
        <el-table-column label="已读" width="70" align="center">
          <template #default="{ row }">
            <span :class="row.isRead === 1 ? 'read-yes' : 'read-no'">
              {{ row.isRead === 1 ? '已读' : '未读' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="发送时间" width="170" align="center">
          <template #default="{ row }">{{ fmtTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" class="btn-detail" @click="handleDetail(row)">明细</el-button>
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

  <!-- 明细弹窗 -->
  <el-dialog v-model="detailVisible" :close-on-click-modal="false" width="820px" class="detail-dialog" :show-close="true">
    <div class="chat-root-detail">
      <!-- 头部 -->
      <div class="chat-header-detail">
        <div class="ch-avatar"><span>{{ (detailForm.patientName || '患')[0] }}</span></div>
        <div class="ch-info">
          <h4 class="ch-name">
            <span class="ch-label">患者：</span>{{ detailForm.patientName }}
            <span class="ch-divider">|</span>
            <span class="ch-label">医生：</span>{{ detailForm.doctorName }}
          </h4>
          <div class="ch-subtitle">聊天记录明细</div>
        </div>
        <el-date-picker v-model="detailForm.date" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" size="small" @change="fetchDetail" class="ch-date-picker" />
      </div>

      <!-- 消息区 -->
      <div class="chat-body-detail" v-loading="detailLoading">
        <div v-if="detailRecords.length === 0 && !detailLoading" class="chat-empty">
          <el-icon :size="40" color="#d6d3d1"><ChatDotRound /></el-icon>
          <p>该日期暂无聊天记录</p>
        </div>
        <div v-for="(r, i) in detailRecords" :key="r.id || i" :class="['msg-row', r.senderRole === 'doctor' ? 'right' : 'left']">
          <div class="msg-avatar" v-if="r.senderRole !== 'doctor'">
            <span>{{ (detailForm.patientName || '患')[0] }}</span>
          </div>
          <div :class="['msg-bubble', { self: r.senderRole === 'doctor' }]">
            <p class="msg-text">{{ r.content }}</p>
            <span class="msg-time">{{ detailTime(r.createTime) }}</span>
          </div>
          <div class="msg-avatar" v-if="r.senderRole === 'doctor'">
            <span>{{ (detailForm.doctorName || '医')[0] }}</span>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<style scoped>
.ac-root { display: flex; flex-direction: column; gap: 20px; }

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
.search-form :deep(.el-input) { width: 170px; }
.search-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px #fde68a;
  background: #fffbeb;
}
.search-form :deep(.el-input__wrapper:hover) { box-shadow: 0 0 0 1px #f97316; }
.search-form :deep(.el-input.is-focus .el-input__wrapper) { box-shadow: 0 0 0 1px #f97316; }

.btn-search {
  height: 38px;
  border-radius: 10px;
  background: linear-gradient(135deg, #f97316, #ea580c);
  border: none;
  box-shadow: 0 2px 10px rgba(194,65,12,0.2);
  transition: all 0.3s;
}
.btn-search:hover {
  background: linear-gradient(135deg, #ea580c, #c2410c);
  box-shadow: 0 4px 16px rgba(194,65,12,0.3);
  transform: translateY(-1px);
}
.btn-reset {
  height: 38px; border-radius: 10px;
  border: 1px solid #fde68a; color: #78716c;
}

.um-table { margin-bottom: 0; }
.um-table :deep(th) {
  background: #fffbeb;
  color: #78716c;
  font-weight: 600;
  font-size: 13px;
}
.um-table :deep(td) { font-size: 13px; color: #431407; }
.um-table :deep(.el-table__cell) {
  border-right-color: #d0d7de;
  border-bottom-color: #d0d7de;
}
.um-table :deep(.el-table__header-wrapper th) {
  border-right-color: #d0d7de;
  border-bottom-color: #c0c8d0;
}

.content-text {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-all;
  line-height: 1.5;
}

.role-tag {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}
.role-tag.patient { background: rgba(249,115,22,0.12); color: #c2410c; }
.role-tag.doctor { background: rgba(6,182,212,0.12); color: #0e7490; }

.read-yes { color: #16a34a; font-weight: 500; }
.read-no { color: #dc2626; font-weight: 500; }

.btn-detail {
  height: 26px; padding: 0 10px; border-radius: 6px;
  font-size: 12px; background: #06b6d4; border: none; color: #fff;
}
.btn-detail:hover { background: #0891b2; }

/* ===== 明细弹窗 -- 聊天风格 ===== */
.detail-dialog :deep(.el-dialog) { border-radius: 16px; overflow: hidden; }
.detail-dialog :deep(.el-dialog__header) { display: none; }
.detail-dialog :deep(.el-dialog__body) { padding: 0; }

.chat-root-detail {
  display: flex; flex-direction: column;
  height: 560px;
  background: #fff;
}

.chat-header-detail {
  display: flex; align-items: center; gap: 14px;
  padding: 16px 22px;
  background: linear-gradient(135deg, #fff7ed, #fffbeb);
  border-bottom: 1px solid #fde68a;
  flex-shrink: 0;
}
.chat-header-detail .ch-avatar {
  width: 44px; height: 44px; border-radius: 14px;
  background: linear-gradient(135deg, #fb923c, #ea580c);
  color: #fff; font-size: 18px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 3px 10px rgba(249,115,22,0.25);
  flex-shrink: 0;
}
.chat-header-detail .ch-info { flex: 1; min-width: 0; }
.chat-header-detail .ch-name {
  font-size: 16px; font-weight: 700; color: #431407; margin: 0;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.chat-header-detail .ch-label { font-weight: 400; color: #9a3412; }
.chat-header-detail .ch-divider { margin: 0 8px; color: #d6d3d1; font-weight: 300; }
.chat-header-detail .ch-subtitle { font-size: 12px; color: #9a3412; }
.ch-date-picker { width: 150px; flex-shrink: 0; }

.chat-body-detail {
  flex: 1; overflow-y: auto; padding: 20px;
  display: flex; flex-direction: column; gap: 16px;
  background: #fffdf7;
}
.chat-body-detail .chat-empty {
  display: flex; flex-direction: column; align-items: center;
  justify-content: center; height: 100%; gap: 8px;
  color: #a8a29e; font-size: 14px;
}
.chat-body-detail .chat-empty p { margin: 0; }

.chat-body-detail .msg-row { display: flex; align-items: flex-end; gap: 8px; }
.chat-body-detail .msg-row.right { justify-content: flex-end; }
.chat-body-detail .msg-row.left { justify-content: flex-start; }

.chat-body-detail .msg-avatar {
  width: 34px; height: 34px; border-radius: 10px;
  background: #fef3c7; color: #92400e;
  font-size: 14px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.chat-body-detail .msg-row.right .msg-avatar {
  background: linear-gradient(135deg, #fb923c, #ea580c); color: #fff;
}

.chat-body-detail .msg-bubble {
  max-width: 68%; padding: 10px 14px; border-radius: 16px;
  background: #fff7ed; border: 1px solid #fed7aa;
  border-bottom-left-radius: 4px;
}
.chat-body-detail .msg-bubble.self {
  background: linear-gradient(135deg, #f97316, #ea580c);
  color: #fff; border: none;
  border-bottom-right-radius: 4px;
}
.chat-body-detail .msg-text { margin: 0; font-size: 14px; line-height: 1.6; word-break: break-word; }
.chat-body-detail .msg-time { font-size: 10px; color: #a8a29e; margin-top: 4px; display: block; }
.chat-body-detail .msg-bubble.self .msg-time { color: rgba(255,255,255,0.65); }

.pagination-wrap { display: flex; justify-content: center; margin-top: 20px; padding-top: 16px; border-top: 1px solid #fef3c7; }

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background: linear-gradient(135deg, #f97316, #ea580c);
  border-radius: 8px;
}
</style>
