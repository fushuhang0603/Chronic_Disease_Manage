<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDoctorList, bindDoctor, unbindDoctor, getMyDoctor } from '../api/user.js'

const loading = ref(true)
const bindingId = ref(null)
const doctors = ref([])
const myDoctorId = ref(null)

onMounted(async () => {
  try {
    const [docList, myDoc] = await Promise.all([
      getDoctorList(),
      getMyDoctor().catch(() => null),
    ])
    doctors.value = docList || []
    if (myDoc) myDoctorId.value = myDoc.doctorId
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
})

function getInitial(name) {
  return (name || '医')[0]
}

async function handleBind(doc) {
  if (bindingId.value) return
  try {
    await ElMessageBox.confirm(
      `确认绑定「${doc.realName}」为您的主治医生吗？`,
      '确认绑定',
      { confirmButtonText: '确认绑定', cancelButtonText: '取消', type: 'info' }
    )
    bindingId.value = doc.doctorId
    await bindDoctor(doc.doctorId)
    myDoctorId.value = doc.doctorId
    ElMessage.success(`已绑定医生：${doc.realName}`)
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '绑定失败')
  } finally {
    bindingId.value = null
  }
}

async function handleUnbind() {
  try {
    await ElMessageBox.confirm('确定要解除绑定吗？', '确认解绑', {
      confirmButtonText: '确认解绑', cancelButtonText: '取消', type: 'warning'
    })
    await unbindDoctor()
    myDoctorId.value = null
    ElMessage.success('已解绑')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '解绑失败')
  }
}
</script>

<template>
  <div class="pd-root" v-loading="loading">
    <div class="pd-content">
      <!-- 页面标题卡片 -->
      <div class="um-card">
        <div class="card-title-row">
          <span class="card-icon" style="background:linear-gradient(135deg,#0d9488,#0f766e)">
            <el-icon :size="18"><UserFilled /></el-icon>
          </span>
          <span class="card-label">医生团队</span>
          <span class="card-tip">共 {{ doctors.length }} 位医生</span>
          <span v-if="myDoctorId" class="bound-badge">已绑定专属医生</span>
        </div>
        <p class="card-subtitle">选择一位医生作为您的主治医生，以便更好地管理您的健康</p>
      </div>

      <!-- 医生卡片网格 -->
      <div class="pd-grid">
        <div
          v-for="doc in doctors"
          :key="doc.doctorId"
          :class="['pd-card', { bound: myDoctorId === doc.doctorId }]"
        >
          <!-- 顶部：头像 + 姓名 -->
          <div class="pd-card-top">
            <div class="pd-avatar">{{ getInitial(doc.realName) }}</div>
            <div class="pd-card-head">
              <div class="pd-name">{{ doc.realName }}</div>
              <span class="pd-title-badge">{{ doc.title }}</span>
            </div>
            <el-icon v-if="myDoctorId === doc.doctorId" class="pd-check" :size="20"><CircleCheckFilled /></el-icon>
          </div>

          <!-- 执业信息 -->
          <div class="pd-info-row">
            <div class="pd-info-item">
              <el-icon :size="14"><OfficeBuilding /></el-icon>
              <span>{{ doc.hospital }}</span>
            </div>
            <div class="pd-info-item">
              <el-icon :size="14"><Grid /></el-icon>
              <span>{{ doc.department }}</span>
            </div>
          </div>

          <!-- 擅长 -->
          <div class="pd-specialty" v-if="doc.specialty">
            <span class="pd-specialty-label">擅长</span>
            <span class="pd-specialty-text">{{ doc.specialty }}</span>
          </div>

          <!-- 简介 -->
          <div class="pd-intro" v-if="doc.introduction">{{ doc.introduction }}</div>

          <!-- 操作 -->
          <div class="pd-actions">
            <el-button
              v-if="myDoctorId !== doc.doctorId"
              type="primary"
              class="btn-primary"
              :loading="bindingId === doc.doctorId"
              @click="handleBind(doc)"
            >
              绑定医生
            </el-button>
            <el-button v-else class="btn-unbind" @click="handleUnbind">解除绑定</el-button>
          </div>
        </div>

        <div v-if="doctors.length === 0 && !loading" class="pd-empty">
          <el-icon :size="48"><UserFilled /></el-icon>
          <p>暂无医生信息</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.pd-root { width: 100%; }

.pd-content {
  max-width: 960px;
  margin: 0 auto;
  padding: 20px 24px 40px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ====== 卡片容器 ====== */
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
  margin-bottom: 8px;
}

.card-icon {
  width: 34px; height: 34px;
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.card-icon .el-icon { color: #fff; }
.card-label { font-size: 16px; font-weight: 700; color: #1e3a5f; }
.card-tip { font-size: 13px; color: #94a3b8; margin-left: auto; }

.bound-badge {
  font-size: 12px; font-weight: 600; color: #059669;
  background: #ecfdf5; padding: 4px 12px; border-radius: 20px;
  border: 1px solid #a7f3d0;
}

.card-subtitle {
  font-size: 13px; color: #94a3b8; margin: 0;
  line-height: 1.6;
}

/* ====== 卡片网格 ====== */
.pd-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

/* ====== 医生卡片 ====== */
.pd-card {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 4px 24px rgba(59,130,246,0.06);
  border: 1px solid #e2e8f0;
  transition: all 0.25s;
  position: relative;
  overflow: hidden;
}
.pd-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(59,130,246,0.1);
}
.pd-card.bound {
  border-color: #3b82f6;
  box-shadow: 0 4px 24px rgba(59,130,246,0.1);
}

/* 顶部行 */
.pd-card-top {
  display: flex; align-items: center; gap: 12px;
  margin-bottom: 14px;
}
.pd-avatar {
  width: 44px; height: 44px;
  border-radius: 12px;
  background: linear-gradient(135deg, #60a5fa, #3b82f6);
  display: flex; align-items: center; justify-content: center;
  color: #fff;
  font-size: 18px; font-weight: 700;
  flex-shrink: 0;
}
.pd-card-head { flex: 1; }
.pd-name { font-size: 16px; font-weight: 700; color: #1e3a5f; }
.pd-title-badge {
  display: inline-block;
  margin-top: 4px;
  padding: 2px 10px; border-radius: 20px;
  font-size: 12px; font-weight: 600;
  background: rgba(59,130,246,0.1); color: #2563eb;
}
.pd-check { color: #3b82f6; flex-shrink: 0; }

/* 执业信息 */
.pd-info-row {
  display: flex; gap: 18px; margin-bottom: 10px;
}
.pd-info-item {
  display: flex; align-items: center; gap: 6px;
  font-size: 13px; color: #64748b;
}
.pd-info-item .el-icon { color: #94a3b8; }

/* 擅长 */
.pd-specialty {
  display: flex; align-items: flex-start; gap: 6px;
  margin-bottom: 8px;
}
.pd-specialty-label {
  font-size: 11px; font-weight: 600; color: #10b981;
  background: rgba(16,185,129,0.1);
  padding: 2px 8px; border-radius: 6px;
  white-space: nowrap; flex-shrink: 0;
}
.pd-specialty-text {
  font-size: 13px; color: #475569; line-height: 1.5;
}

/* 简介 */
.pd-intro {
  font-size: 13px; color: #94a3b8;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 14px;
}

/* 按钮 */
.pd-actions { display: flex; justify-content: flex-end; }
.btn-primary {
  height: 36px; border-radius: 10px;
  background: linear-gradient(135deg, #60a5fa, #3b82f6);
  border: none;
  box-shadow: 0 2px 8px rgba(59,130,246,0.2);
  transition: all 0.3s;
}
.btn-primary:hover {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  box-shadow: 0 4px 16px rgba(59,130,246,0.3);
  transform: translateY(-1px);
}
.btn-unbind {
  height: 36px; border-radius: 10px;
  border: 1px solid #fca5a5; color: #dc2626;
  background: #fff;
}
.btn-unbind:hover { background: #fef2f2; border-color: #f87171; }

/* 空状态 */
.pd-empty {
  grid-column: 1 / -1;
  display: flex; flex-direction: column; align-items: center;
  padding: 80px 0; color: #94a3b8;
}
.pd-empty p { margin-top: 12px; font-size: 15px; }
</style>
