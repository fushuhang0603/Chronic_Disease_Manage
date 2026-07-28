<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDoctorProfilePage, bindDoctor, unbindDoctor, getMyDoctor } from '../api/user.js'

const router = useRouter()

const loading = ref(true)
const bindingId = ref(null)
const doctors = ref([])
const myDoctorId = ref(null)
const myDoctorName = ref('')
const total = ref(0)

const query = reactive({
  doctorName: '',
  hospital: '',
  department: '',
  pageNum: 1,
  pageSize: 6,
})

const showDetail = ref(false)
const detailDoc = ref(null)

function openDetail(doc) {
  detailDoc.value = doc
  showDetail.value = true
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getDoctorProfilePage({
      doctorName: query.doctorName || undefined,
      hospital: query.hospital || undefined,
      department: query.department || undefined,
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    })
    doctors.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
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

onMounted(async () => {
  try {
    const myDoc = await getMyDoctor().catch(() => null)
    if (myDoc) {
      myDoctorId.value = myDoc.doctorId
      myDoctorName.value = myDoc.realName
    }
  } catch {}
  fetchData()
})

function getInitial(name) { return (name || '医')[0] }

// 暖色系头像背景
const avatarColors = [
  { bg: 'linear-gradient(135deg, #fb923c, #f97316)' },
  { bg: 'linear-gradient(135deg, #f59e0b, #d97706)' },
  { bg: 'linear-gradient(135deg, #f87171, #dc2626)' },
  { bg: 'linear-gradient(135deg, #a78bfa, #7c3aed)' },
  { bg: 'linear-gradient(135deg, #38bdf8, #0ea5e9)' },
]
function avatarIdx(id) { return (id || 0) % avatarColors.length }

async function handleBind() {
  const doc = detailDoc.value
  if (!doc || bindingId.value) return
  try {
    await ElMessageBox.confirm(
      `确认绑定「${doc.realName}」为您的主治医生吗？`,
      '绑定确认',
      { confirmButtonText: '确认', cancelButtonText: '取消', type: 'info' }
    )
    bindingId.value = doc.doctorId
    await bindDoctor(doc.doctorId)
    myDoctorId.value = doc.doctorId
    myDoctorName.value = doc.realName
    showDetail.value = false
    ElMessage.success('绑定成功')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '绑定失败')
  } finally {
    bindingId.value = null
  }
}

async function handleUnbind() {
  try {
    await ElMessageBox.confirm('确定要解除绑定吗？', '确认解绑', {
      confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning'
    })
    await unbindDoctor()
    myDoctorId.value = null
    myDoctorName.value = ''
    showDetail.value = false
    ElMessage.success('已解绑')
  } catch {}
}
</script>

<template>
  <div class="pd-root">

    <!-- ==== 欢迎区 ==== -->
    <div class="welcome-card">
      <div class="welcome-left">
        <h2 class="welcome-title">医生团队</h2>
        <p class="welcome-desc">
          <template v-if="myDoctorId">
            您的主治医生：<em>{{ myDoctorName }}</em>
          </template>
          <template v-else>
            选择一位专业医生，为您提供个性化的慢病管理指导
          </template>
        </p>
      </div>
      <div class="welcome-stats">
        <div class="wstat-item">
          <span class="wstat-num">{{ total }}</span>
          <span class="wstat-unit">位医生</span>
        </div>
      </div>
    </div>

    <!-- ==== 筛选区 ==== -->
    <div class="filter-bar">
      <div class="filter-inputs">
        <el-input v-model="query.doctorName" placeholder="医生姓名" clearable @keyup.enter="handleSearch" class="fi-input">
          <template #prefix><el-icon :size="15"><Search /></el-icon></template>
        </el-input>
        <el-input v-model="query.hospital" placeholder="医院名称" clearable @keyup.enter="handleSearch" class="fi-input">
          <template #prefix><el-icon :size="15"><OfficeBuilding /></el-icon></template>
        </el-input>
        <el-input v-model="query.department" placeholder="科室" clearable @keyup.enter="handleSearch" class="fi-input-sm">
          <template #prefix><el-icon :size="15"><Grid /></el-icon></template>
        </el-input>
      </div>
      <div class="filter-actions">
        <el-button class="btn-filter-reset" @click="handleReset">重置</el-button>
        <el-button type="primary" class="btn-filter-search" @click="handleSearch">查询</el-button>
      </div>
    </div>

    <!-- ==== 医生列表 ==== -->
    <div class="doc-grid" v-loading="loading">
      <div
        v-for="doc in doctors"
        :key="doc.doctorId"
        :class="['doc-card', { bound: myDoctorId === doc.doctorId }]"
        @click="openDetail(doc)"
      >
        <div class="dc-top">
          <div class="dc-avatar" :style="{ background: avatarColors[avatarIdx(doc.doctorId)].bg }">
            {{ getInitial(doc.realName) }}
          </div>
          <div class="dc-badges" v-if="myDoctorId === doc.doctorId">
            <span class="dc-bound-badge">已绑定</span>
          </div>
        </div>

        <div class="dc-body">
          <h4 class="dc-name">{{ doc.realName }}</h4>
          <span class="dc-title">{{ doc.title }}</span>
          <div class="dc-meta">
            <span class="dc-meta-item">{{ doc.hospital }}</span>
            <span class="dc-meta-sep">|</span>
            <span class="dc-meta-item">{{ doc.department }}</span>
          </div>
          <p class="dc-specialty" v-if="doc.specialty">{{ doc.specialty }}</p>
        </div>

        <div class="dc-footer">
          <span class="dc-view">查看详情</span>
          <el-icon :size="14"><ArrowRight /></el-icon>
        </div>
      </div>

      <div v-if="doctors.length === 0 && !loading" class="empty-state">
        <div class="empty-icon">
          <el-icon :size="40"><UserFilled /></el-icon>
        </div>
        <p class="empty-title">暂无医生信息</p>
        <p class="empty-sub">换个筛选条件试试</p>
      </div>
    </div>

    <!-- ==== 分页 ==== -->
    <div class="pag-wrap" v-if="total > query.pageSize">
      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[6, 12, 18]"
        layout="total, prev, pager, next, sizes"
        background
        @current-change="handlePage"
        @size-change="handleSize"
      />
    </div>

    <!-- ==== 详情弹窗 ==== -->
    <el-dialog v-model="showDetail" width="480px" :close-on-click-modal="false" destroy-on-close>
      <template #header>
        <span class="dlg-header-title">医生详情</span>
      </template>

      <div class="dlg-root" v-if="detailDoc">
        <div class="dlg-profile">
          <div class="dlg-avatar" :style="{ background: avatarColors[avatarIdx(detailDoc.doctorId)].bg }">
            {{ getInitial(detailDoc.realName) }}
          </div>
          <div class="dlg-profile-info">
            <h3 class="dlg-name">{{ detailDoc.realName }}</h3>
            <span class="dlg-title-tag">{{ detailDoc.title }}</span>
          </div>
          <el-icon v-if="myDoctorId === detailDoc.doctorId" :size="22" color="#f59e0b"><CircleCheckFilled /></el-icon>
        </div>

        <div class="dlg-info-cards">
          <div class="dlg-info-card">
            <el-icon :size="18" color="#f59e0b"><OfficeBuilding /></el-icon>
            <div>
              <span class="dic-label">所属医院</span>
              <span class="dic-value">{{ detailDoc.hospital }}</span>
            </div>
          </div>
          <div class="dlg-info-card">
            <el-icon :size="18" color="#f59e0b"><Grid /></el-icon>
            <div>
              <span class="dic-label">科室</span>
              <span class="dic-value">{{ detailDoc.department }}</span>
            </div>
          </div>
        </div>

        <div class="dlg-section" v-if="detailDoc.specialty">
          <h4 class="dlg-section-title">擅长领域</h4>
          <p class="dlg-section-text">{{ detailDoc.specialty }}</p>
        </div>

        <div class="dlg-section" v-if="detailDoc.introduction">
          <h4 class="dlg-section-title">医生简介</h4>
          <p class="dlg-section-text">{{ detailDoc.introduction }}</p>
        </div>

        <div class="dlg-bound-tip" v-if="myDoctorId === detailDoc.doctorId">
          这是您当前绑定的主治医生
        </div>
      </div>

      <template #footer>
        <div class="dlg-btns">
          <template v-if="myDoctorId !== detailDoc?.doctorId">
            <el-button class="btn-dlg-cancel" @click="showDetail = false">关闭</el-button>
            <el-button type="primary" class="btn-dlg-confirm" :loading="bindingId === detailDoc?.doctorId" @click="handleBind">
              绑定为主治医生
            </el-button>
          </template>
          <template v-else>
            <el-button class="btn-dlg-danger" @click="handleUnbind">解除绑定</el-button>
            <el-button class="btn-dlg-cancel" @click="showDetail = false">关闭</el-button>
          </template>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.pd-root {
  max-width: 1080px;
  margin: 0 auto;
  width: 100%;
  min-height: 100%;
  padding: 24px 24px 40px;
  display: flex;
  flex-direction: column;
  gap: 22px;
  box-sizing: border-box;
  background: linear-gradient(180deg, #fff7ed 0%, #fef3c7 60%, #fffbeb 100%);
}

/* ==================== 欢迎区 ==================== */
.welcome-card {
  display: flex; align-items: center; justify-content: space-between;
  padding: 28px 32px; gap: 20px;
  border-radius: 20px;
  background: #fff;
  border: 1px solid #fde68a;
  box-shadow: 0 2px 12px rgba(249,115,22,0.08);
}
.welcome-left { flex: 1; }
.welcome-title {
  font-size: 24px; font-weight: 800; color: #7c2d12; margin: 0 0 6px 0;
  letter-spacing: -0.3px;
}
.welcome-desc {
  font-size: 14px; color: #9a3412; margin: 0; line-height: 1.5;
}
.welcome-desc em {
  font-style: normal; font-weight: 700; color: #c2410c;
  background: rgba(249,115,22,0.12); padding: 1px 8px; border-radius: 6px;
}
.welcome-stats { flex-shrink: 0; }
.wstat-item {
  text-align: center;
  background: rgba(255,255,255,0.65);
  border-radius: 16px;
  padding: 14px 28px;
  border: 1px solid #fcd34d;
}
.wstat-num {
  font-size: 32px; font-weight: 800; color: #c2410c; display: block; line-height: 1;
}
.wstat-unit { font-size: 13px; color: #9a3412; margin-top: 4px; display: block; }

/* ==================== 筛选区 ==================== */
.filter-bar {
  display: flex; align-items: center; justify-content: space-between;
  background: #fff; border: 1px solid #fde68a;
  border-radius: 16px; padding: 12px 20px;
  gap: 12px;
}
.filter-inputs { display: flex; align-items: center; gap: 10px; flex: 1; }
.fi-input { width: 180px; }
.fi-input-sm { width: 130px; }
.filter-bar :deep(.el-input__wrapper) {
  border-radius: 10px; background: #fffbeb;
  box-shadow: 0 0 0 1px #fde68a;
}
.filter-bar :deep(.el-input__wrapper:hover) { box-shadow: 0 0 0 1px #fbbf24; }
.filter-bar :deep(.el-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 1px #f59e0b; background: #fff;
}
.filter-actions { display: flex; gap: 8px; flex-shrink: 0; }
.btn-filter-reset {
  height: 36px; border-radius: 10px;
  border: 1px solid #fde68a; color: #92400e; background: #fff;
}
.btn-filter-reset:hover { background: #fff7ed; }
.btn-filter-search {
  height: 36px; border-radius: 10px; padding: 0 20px;
  background: linear-gradient(135deg, #f97316, #ea580c);
  border: none; font-weight: 600;
  box-shadow: 0 2px 8px rgba(234,88,12,0.25);
}
.btn-filter-search:hover {
  background: linear-gradient(135deg, #ea580c, #c2410c);
}

/* ==================== 卡片网格 ==================== */
.doc-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
}

/* ==================== 医生卡片 ==================== */
.doc-card {
  background: #fff;
  border-radius: 18px;
  border: 1px solid #fef3c7;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}
.doc-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 28px rgba(249,115,22,0.12);
  border-color: #fbbf24;
}
.doc-card.bound {
  border-color: #fbbf24;
  background: #fffdf7;
}

.dc-top {
  display: flex; align-items: flex-start;
  padding: 18px 20px 0;
  gap: 0;
  position: relative;
}
.dc-avatar {
  width: 52px; height: 52px;
  border-radius: 16px;
  color: #fff; font-size: 20px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 3px 10px rgba(0,0,0,0.12);
}
.dc-badges {
  flex: 1; display: flex; justify-content: flex-end;
}
.dc-bound-badge {
  font-size: 11px; font-weight: 600; color: #d97706;
  background: #fef3c7; padding: 3px 10px; border-radius: 10px;
}

.dc-body {
  padding: 12px 20px 8px;
  display: flex; flex-direction: column;
  gap: 6px;
}
.dc-name {
  font-size: 16px; font-weight: 700; color: #431407; margin: 0;
}
.dc-title {
  font-size: 12px; font-weight: 600; color: #c2410c;
  background: #fff7ed; padding: 2px 10px; border-radius: 6px;
  display: inline-block; width: fit-content;
}
.dc-meta {
  display: flex; align-items: center; gap: 6px;
  font-size: 12px; color: #78716c;
}
.dc-meta-sep { color: #d6d3d1; }
.dc-specialty {
  font-size: 12px; color: #a8a29e; margin: 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.dc-footer {
  display: flex; align-items: center; justify-content: center; gap: 4px;
  padding: 10px 20px 16px;
  border-top: 1px solid #fef3c7;
  margin: 0 12px;
  transition: color 0.2s;
}
.doc-card:hover .dc-footer { color: #ea580c; }
.dc-view { font-size: 13px; color: #a8a29e; }
.doc-card:hover .dc-view { color: #ea580c; }

/* ==================== 空状态 ==================== */
.empty-state {
  grid-column: 1 / -1;
  display: flex; flex-direction: column; align-items: center;
  padding: 80px 0; gap: 8px;
}
.empty-icon {
  width: 80px; height: 80px; border-radius: 50%;
  background: #fef3c7;
  display: flex; align-items: center; justify-content: center;
  color: #d6d3d1;
}
.empty-title { font-size: 16px; font-weight: 600; color: #a8a29e; margin: 0; }
.empty-sub { font-size: 13px; color: #d6d3d1; margin: 0; }

.pag-wrap { display: flex; justify-content: center; }

/* ==================== 详情弹窗 ==================== */
.dlg-header-title { font-size: 18px; font-weight: 700; color: #431407; }

.dlg-root { display: flex; flex-direction: column; gap: 20px; }

.dlg-profile {
  display: flex; align-items: center; gap: 14px;
  padding-bottom: 16px;
  border-bottom: 1px solid #fef3c7;
}
.dlg-avatar {
  width: 60px; height: 60px; border-radius: 18px;
  color: #fff; font-size: 24px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 3px 12px rgba(0,0,0,0.1);
}
.dlg-profile-info { flex: 1; }
.dlg-name { font-size: 20px; font-weight: 700; color: #431407; margin: 0; }
.dlg-title-tag {
  display: inline-block; margin-top: 4px;
  font-size: 13px; font-weight: 600; color: #c2410c;
  background: #fff7ed; padding: 2px 12px; border-radius: 8px;
}

.dlg-info-cards {
  display: grid; grid-template-columns: 1fr 1fr; gap: 10px;
}
.dlg-info-card {
  display: flex; align-items: center; gap: 10px;
  background: #fffbeb; border-radius: 12px; padding: 12px 14px;
  border: 1px solid #fef3c7;
}
.dlg-info-card > div { display: flex; flex-direction: column; gap: 2px; }
.dic-label { font-size: 11px; color: #a8a29e; }
.dic-value { font-size: 13px; color: #44403c; font-weight: 500; }

.dlg-section { display: flex; flex-direction: column; gap: 6px; }
.dlg-section-title {
  font-size: 13px; font-weight: 600; color: #78716c; margin: 0;
}
.dlg-section-text {
  font-size: 14px; color: #44403c; line-height: 1.7; margin: 0;
}

.dlg-bound-tip {
  padding: 12px 16px; border-radius: 10px;
  background: #fef3c7; color: #92400e;
  font-size: 13px; font-weight: 500; text-align: center;
}

.dlg-btns { display: flex; justify-content: flex-end; gap: 10px; }

.btn-dlg-cancel {
  height: 38px; border-radius: 10px;
  border: 1px solid #e7e5e4; color: #78716c;
}
.btn-dlg-confirm {
  height: 38px; border-radius: 10px; padding: 0 22px;
  background: linear-gradient(135deg, #f97316, #ea580c);
  border: none; font-weight: 600;
  box-shadow: 0 2px 8px rgba(234,88,12,0.25);
}
.btn-dlg-confirm:hover {
  background: linear-gradient(135deg, #ea580c, #c2410c);
}
.btn-dlg-danger {
  height: 38px; border-radius: 10px;
  border: 1px solid #fca5a5; color: #dc2626; background: #fff;
}
.btn-dlg-danger:hover { background: #fef2f2; }

/* ==================== 响应式 ==================== */
@media (max-width: 960px) {
  .doc-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 640px) {
  .doc-grid { grid-template-columns: 1fr; }
  .welcome-card { flex-direction: column; text-align: center; }
  .filter-bar { flex-direction: column; }
  .filter-inputs { flex-wrap: wrap; width: 100%; }
  .fi-input, .fi-input-sm { width: 100%; flex: 1 1 100%; }
}
</style>
