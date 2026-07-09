<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import { getArchivePage, deleteArchive } from '../api/user.js'

const loading = ref(false)
const total = ref(0)
const records = ref([])
const currentArchive = ref(null)

const query = reactive({
  patientName: '',
  idCard: '',
  pageNum: 1,
  pageSize: 12,
})

const genderMap = { 1: '男', 2: '女' }

function calcAge(birthDate) {
  if (!birthDate) return null
  const now = new Date()
  const birth = new Date(birthDate)
  let age = now.getFullYear() - birth.getFullYear()
  const m = now.getMonth() - birth.getMonth()
  if (m < 0 || (m === 0 && now.getDate() < birth.getDate())) age--
  return age
}

async function fetchPage() {
  loading.value = true
  try {
    const data = await getArchivePage(query)
    records.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    ElMessage.error(e.message || '查询失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.pageNum = 1
  fetchPage()
}

function handleReset() {
  query.patientName = ''
  query.idCard = ''
  query.pageNum = 1
  fetchPage()
}

function handlePageChange(page) {
  query.pageNum = page
  fetchPage()
}

function openDetail(archive) {
  currentArchive.value = archive
}

function backToList() {
  currentArchive.value = null
}

async function handleDelete(archive) {
  try {
    await ElMessageBox.confirm(
      `确定删除「${archive.patientName}」的档案吗？删除后可在数据库中恢复。`,
      '删除确认',
      { type: 'warning' }
    )
    await deleteArchive(archive.id)
    ElMessage.success('删除成功')
    if (records.value.length === 1 && query.pageNum > 1) {
      query.pageNum--
    }
    fetchPage()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '删除失败')
    }
  }
}

function maskIdCard(idCard) {
  if (!idCard || idCard.length < 8) return idCard || '—'
  return idCard.slice(0, 4) + '****' + idCard.slice(-4)
}

onMounted(() => fetchPage())
</script>

<template>
  <div class="am-root">

    <!-- ========== 列表模式 ========== -->
    <template v-if="!currentArchive">
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="query.patientName"
          placeholder="患者姓名（模糊）"
          clearable
          size="large"
          class="search-input"
          @keyup.enter="handleSearch"
        />
        <el-input
          v-model="query.idCard"
          placeholder="身份证号（模糊）"
          clearable
          size="large"
          class="search-input"
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" size="large" class="search-btn" @click="handleSearch">搜索</el-button>
        <el-button size="large" class="reset-btn" @click="handleReset">重置</el-button>
      </div>

      <!-- 卡片列表 -->
      <div v-loading="loading" class="card-grid">
        <div
          v-for="item in records"
          :key="item.id"
          class="arc-card"
          @click="openDetail(item)"
        >
          <div class="card-top">
            <div class="card-avatar">
              <el-icon :size="22"><User /></el-icon>
            </div>
            <div class="card-name">{{ item.patientName || '未填写' }}</div>
            <el-tag
              :type="item.chronicType === '糖尿病' ? 'danger' : item.chronicType === '高血压' ? 'warning' : 'info'"
              size="small"
              effect="plain"
            >
              {{ item.chronicType || '未知' }}
            </el-tag>
          </div>

          <div class="card-body">
            <div class="card-row">
              <span class="card-label">性别</span>
              <span class="card-value">{{ genderMap[item.gender] || '—' }}</span>
            </div>
            <div class="card-row">
              <span class="card-label">年龄</span>
              <span class="card-value">{{ calcAge(item.birthDate) || '—' }}岁</span>
            </div>
            <div class="card-row">
              <span class="card-label">手机</span>
              <span class="card-value">{{ item.phone || '—' }}</span>
            </div>
            <div class="card-row">
              <span class="card-label">身份证</span>
              <span class="card-value">{{ maskIdCard(item.idCard) }}</span>
            </div>
          </div>

          <div class="card-foot">
            <span class="card-time">建档：{{ item.createTime?.slice(0, 10) || '—' }}</span>
            <el-button
              type="danger"
              text
              size="small"
              @click.stop="handleDelete(item)"
            >删除</el-button>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && records.length === 0" class="empty-state">
        <el-icon :size="56" color="#cbd5e1"><DocumentCopy /></el-icon>
        <p class="empty-text">暂无档案记录</p>
      </div>

      <!-- 分页 -->
      <div v-if="total > query.pageSize" class="pagination-wrap">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="query.pageSize"
          :current-page="query.pageNum"
          @current-change="handlePageChange"
        />
      </div>
    </template>

    <!-- ========== 详情模式 ========== -->
    <template v-else>
      <div class="detail-root">
        <!-- 顶部返回栏 -->
        <div class="detail-topbar">
          <el-button text class="back-btn" @click="backToList">
            <el-icon><ArrowLeft /></el-icon>
            <span>返回列表</span>
          </el-button>
          <span class="detail-title">档案详情</span>
        </div>

        <!-- 档案卡片（复用 ArchivePatient 查看模式样式） -->
        <div class="archive-card">
          <div class="card-header">
            <div class="header-left">
              <span class="header-icon">
                <el-icon :size="24"><User /></el-icon>
              </span>
              <div class="header-info">
                <span class="patient-name">{{ currentArchive.patientName || '未填写' }}</span>
                <span class="patient-meta">
                  {{ genderMap[currentArchive.gender] || '未知' }} &nbsp;·&nbsp;
                  {{ calcAge(currentArchive.birthDate) || '—' }}岁 &nbsp;·&nbsp;
                  {{ currentArchive.bloodType || '血型未知' }}
                </span>
              </div>
            </div>
          </div>

          <div class="record-no">档案编号：{{ currentArchive.id }}</div>

          <div class="info-grid">
            <div class="info-row">
              <span class="info-label">身份证号</span>
              <span class="info-value">{{ currentArchive.idCard || '—' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">出生日期</span>
              <span class="info-value">{{ currentArchive.birthDate || '—' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">联系电话</span>
              <span class="info-value">{{ currentArchive.phone || '—' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">家庭住址</span>
              <span class="info-value">{{ currentArchive.address || '—' }}</span>
            </div>
          </div>

          <div class="divider" />

          <div class="info-grid">
            <div class="info-row">
              <span class="info-label">慢病类型</span>
              <span class="info-value highlight">{{ currentArchive.chronicType || '—' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">首次确诊</span>
              <span class="info-value">{{ currentArchive.diagnosisDate || '—' }}</span>
            </div>
          </div>

          <div class="divider" />
          <div class="text-section">
            <div class="text-title">既往病史 / 手术史</div>
            <p class="text-content">{{ currentArchive.medicalHistory || '—' }}</p>
          </div>
          <div class="text-section">
            <div class="text-title">家族遗传病史</div>
            <p class="text-content">{{ currentArchive.familyHistory || '—' }}</p>
          </div>
          <div class="text-section">
            <div class="text-title">药物 / 食物过敏史</div>
            <p class="text-content" :class="{ warn: currentArchive.allergyHistory }">{{ currentArchive.allergyHistory || '无过敏史' }}</p>
          </div>
          <div class="text-section">
            <div class="text-title">生活习惯</div>
            <p class="text-content">{{ currentArchive.lifeHabit || '—' }}</p>
          </div>

          <div class="divider" />
          <div class="info-grid">
            <div class="info-row">
              <span class="info-label">紧急联系人</span>
              <span class="info-value">{{ currentArchive.emergencyName || '—' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">紧急联系电话</span>
              <span class="info-value">{{ currentArchive.emergencyPhone || '—' }}</span>
            </div>
          </div>
        </div>
      </div>
    </template>

  </div>
</template>

<style scoped>
.am-root { max-width: 1100px; margin: 0 auto; }

/* ===== 搜索栏 ===== */
.search-bar {
  display: flex; gap: 14px; align-items: center;
  margin-bottom: 24px;
}
.search-input { width: 220px; }
.search-btn {
  height: 40px; border-radius: 8px; padding: 0 24px;
  background: linear-gradient(135deg, #60a5fa, #3b82f6);
  border: none; font-weight: 600;
}
.reset-btn { height: 40px; border-radius: 8px; padding: 0 20px; }

/* ===== 卡片网格 ===== */
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
  min-height: 120px;
}

.arc-card {
  background: #fff;
  border-radius: 14px;
  border: 1px solid #e2e8f0;
  padding: 20px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex; flex-direction: column; gap: 14px;
}
.arc-card:hover {
  box-shadow: 0 4px 16px rgba(59,130,246,0.12);
  border-color: #93c5fd;
  transform: translateY(-2px);
}

.card-top {
  display: flex; align-items: center; gap: 10px;
}
.card-avatar {
  width: 38px; height: 38px; border-radius: 10px;
  background: linear-gradient(135deg, #93c5fd, #60a5fa);
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.card-avatar .el-icon { color: #fff; }
.card-name {
  font-size: 16px; font-weight: 700; color: #1e293b;
  flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}

.card-body {
  display: flex; flex-direction: column; gap: 6px;
  padding: 10px 0;
  border-top: 1px solid #f1f5f9;
  border-bottom: 1px solid #f1f5f9;
}
.card-row { display: flex; justify-content: space-between; align-items: center; }
.card-label { font-size: 12px; color: #94a3b8; }
.card-value { font-size: 13px; color: #334155; font-weight: 500; }

.card-foot {
  display: flex; justify-content: space-between; align-items: center;
}
.card-time { font-size: 12px; color: #94a3b8; }

/* ===== 空状态 ===== */
.empty-state {
  display: flex; flex-direction: column; align-items: center;
  padding: 60px 0; gap: 12px;
}
.empty-text { font-size: 15px; color: #94a3b8; }

/* ===== 分页 ===== */
.pagination-wrap {
  display: flex; justify-content: center; margin-top: 28px;
}

/* ===== 详情模式 ===== */
.detail-root { max-width: 860px; margin: 0 auto; }

.detail-topbar {
  display: flex; align-items: center; gap: 16px;
  margin-bottom: 20px;
}
.back-btn {
  font-size: 14px; color: #3b82f6; padding: 6px 12px;
}
.back-btn:hover { background: #eff6ff; }
.back-btn .el-icon { margin-right: 4px; }
.detail-title {
  font-size: 16px; font-weight: 700; color: #1e293b;
}

/* ===== 档案详情卡片（与 ArchivePatient 一致） ===== */
.archive-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.06);
  padding: 32px;
  border: 1px solid #e2e8f0;
}

.card-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 20px;
}
.header-left { display: flex; align-items: center; gap: 16px; }
.header-icon {
  width: 52px; height: 52px; border-radius: 14px;
  background: linear-gradient(135deg, #60a5fa, #3b82f6);
  display: flex; align-items: center; justify-content: center;
}
.header-icon .el-icon { color: #fff; }
.header-info { display: flex; flex-direction: column; gap: 2px; }
.patient-name { font-size: 20px; font-weight: 700; color: #1e293b; }
.patient-meta { font-size: 13px; color: #64748b; }

.record-no {
  font-size: 12px; color: #94a3b8;
  margin-bottom: 20px; padding-bottom: 16px;
  border-bottom: 1px dashed #e2e8f0;
}

.info-grid {
  display: grid; grid-template-columns: 1fr 1fr;
  gap: 14px 40px;
}
.info-row {
  display: flex; align-items: baseline; gap: 12px;
}
.info-label {
  font-size: 13px; color: #64748b; white-space: nowrap;
  min-width: 70px;
}
.info-value {
  font-size: 14px; color: #1e293b; font-weight: 500;
}
.info-value.highlight {
  color: #dc2626; font-weight: 600;
}

.divider {
  height: 1px; background: #f1f5f9;
  margin: 16px 0;
}

.text-section { margin-bottom: 12px; }
.text-title {
  font-size: 13px; color: #64748b;
  margin-bottom: 4px;
}
.text-content {
  font-size: 14px; color: #1e293b; line-height: 1.7;
  margin: 0; white-space: pre-wrap;
}
.text-content.warn { color: #dc2626; font-weight: 600; }
</style>
