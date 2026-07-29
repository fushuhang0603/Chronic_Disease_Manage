<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyArchive, addArchive, editArchive } from '../api/user.js'

const loading = ref(false)
const hasArchive = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  patientName: '',
  idCard: '',
  age: null,
  birthDate: '',
  gender: 1,
  phone: '',
  address: '',
  bloodType: '',
  chronicType: '',
  diagnosisDate: '',
  medicalHistory: '',
  familyHistory: '',
  allergyHistory: '',
  lifeHabit: '',
  emergencyName: '',
  emergencyPhone: '',
})

const rules = reactive({
  patientName: [{ required: true, message: '请输入患者姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  chronicType: [{ required: true, message: '请选择慢病类型', trigger: 'change' }],
})

const chronicOptions = ['高血压', '糖尿病', '冠心病', '慢性阻塞性肺疾病', '脑卒中', '其他']
const bloodOptions = ['A', 'B', 'AB', 'O']
const genderMap = { 1: '男', 2: '女' }

async function fetchArchive() {
  loading.value = true
  try {
    const data = await getMyArchive()
    if (data) {
      hasArchive.value = true
      Object.assign(form, {
        id: data.id,
        patientName: data.patientName || '',
        idCard: data.idCard || '',
        age: data.age,
        birthDate: data.birthDate || '',
        gender: data.gender || 1,
        phone: data.phone || '',
        address: data.address || '',
        bloodType: data.bloodType || '',
        chronicType: data.chronicType || '',
        diagnosisDate: data.diagnosisDate || '',
        medicalHistory: data.medicalHistory || '',
        familyHistory: data.familyHistory || '',
        allergyHistory: data.allergyHistory || '',
        lifeHabit: data.lifeHabit || '',
        emergencyName: data.emergencyName || '',
        emergencyPhone: data.emergencyPhone || '',
      })
    }
  } catch (e) {
    ElMessage.error(e.message || '加载健康档案失败')
  } finally {
    loading.value = false
  }
}

function handleEdit() {
  isEdit.value = true
  nextTick(() => formRef.value?.clearValidate())
}

function handleCancel() {
  isEdit.value = false
  fetchArchive()
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (hasArchive.value) {
      await editArchive(form)
      ElMessage.success('更新成功')
    } else {
      await addArchive(form)
      ElMessage.success('创建成功')
      hasArchive.value = true
    }
    isEdit.value = false
    await fetchArchive()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  }
}

import { nextTick } from 'vue'
onMounted(() => fetchArchive())
</script>

<template>
  <div class="ar-root">
    <!-- ========== 查看模式：档案卡片 ========== -->
    <template v-if="hasArchive && !isEdit">
      <div class="archive-card">
        <!-- 卡片头部 -->
        <div class="card-header">
          <div class="header-left">
            <span class="header-icon">
              <el-icon :size="24"><User /></el-icon>
            </span>
            <div class="header-info">
              <span class="patient-name">{{ form.patientName || '未填写' }}</span>
              <span class="patient-meta">
                {{ genderMap[form.gender] || '未知' }} &nbsp;·&nbsp;
                {{ form.age != null ? form.age + '岁' : '—岁' }} &nbsp;·&nbsp;
                {{ form.bloodType || '血型未知' }}
              </span>
            </div>
          </div>
          <el-button type="primary" class="edit-btn" @click="handleEdit">编辑档案</el-button>
        </div>

        <!-- 档案编号 -->
        <div class="record-no">档案编号：{{ form.id }}</div>

        <!-- 信息网格 -->
        <div class="info-grid">
          <div class="info-row">
            <span class="info-label">身份证号</span>
            <span class="info-value">{{ form.idCard || '—' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">出生日期</span>
            <span class="info-value">{{ form.birthDate || '—' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">联系电话</span>
            <span class="info-value">{{ form.phone || '—' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">家庭住址</span>
            <span class="info-value">{{ form.address || '—' }}</span>
          </div>
        </div>

        <div class="divider" />

        <div class="info-grid">
          <div class="info-row">
            <span class="info-label">慢病类型</span>
            <span class="info-value highlight">{{ form.chronicType || '—' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">首次确诊</span>
            <span class="info-value">{{ form.diagnosisDate || '—' }}</span>
          </div>
        </div>

        <div class="divider" />
        <div class="text-section">
          <div class="text-title">既往病史 / 手术史</div>
          <p class="text-content">{{ form.medicalHistory || '—' }}</p>
        </div>
        <div class="text-section">
          <div class="text-title">家族遗传病史</div>
          <p class="text-content">{{ form.familyHistory || '—' }}</p>
        </div>
        <div class="text-section">
          <div class="text-title">药物 / 食物过敏史</div>
          <p class="text-content" :class="{ warn: form.allergyHistory }">{{ form.allergyHistory || '无过敏史' }}</p>
        </div>
        <div class="text-section">
          <div class="text-title">生活习惯</div>
          <p class="text-content">{{ form.lifeHabit || '—' }}</p>
        </div>

        <div class="divider" />
        <div class="info-grid">
          <div class="info-row">
            <span class="info-label">紧急联系人</span>
            <span class="info-value">{{ form.emergencyName || '—' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">紧急联系电话</span>
            <span class="info-value">{{ form.emergencyPhone || '—' }}</span>
          </div>
        </div>
      </div>
    </template>

    <!-- ========== 新增 / 编辑模式：填表 ========== -->
    <template v-if="!hasArchive || isEdit">
      <div class="form-card">
        <div class="form-title">
          <span class="title-dot" />
          {{ hasArchive ? '编辑健康档案' : '新建健康档案' }}
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="right" label-width="110px" class="hos-form">
          <!-- 患者信息 -->
          <div class="form-section">患者信息</div>
          <el-form-item label="患者姓名" prop="patientName">
            <el-input v-model="form.patientName" size="large" placeholder="请输入真实姓名" maxlength="30" />
          </el-form-item>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="身份证号">
                <el-input v-model="form.idCard" size="large" placeholder="选填" maxlength="18" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="联系电话" prop="phone">
                <el-input v-model="form.phone" size="large" placeholder="请输入手机号" maxlength="11" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="性别" prop="gender">
                <el-radio-group v-model="form.gender" size="large">
                  <el-radio-button :value="1">男</el-radio-button>
                  <el-radio-button :value="2">女</el-radio-button>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="出生日期">
                <el-date-picker v-model="form.birthDate" type="date" size="large"
                  style="width:100%" placeholder="选择日期" value-format="YYYY-MM-DD" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="血型">
                <el-select v-model="form.bloodType" size="large" style="width:100%" placeholder="选填" clearable>
                  <el-option v-for="o in bloodOptions" :key="o" :label="o + ' 型'" :value="o" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="家庭住址">
            <el-input v-model="form.address" size="large" placeholder="选填" maxlength="200" />
          </el-form-item>

          <!-- 疾病信息 -->
          <div class="form-section">疾病信息</div>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="慢病类型" prop="chronicType">
                <el-select v-model="form.chronicType" size="large" style="width:100%" placeholder="请选择">
                  <el-option v-for="o in chronicOptions" :key="o" :label="o" :value="o" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="首次确诊日期">
                <el-date-picker v-model="form.diagnosisDate" type="date" size="large"
                  style="width:100%" placeholder="选择日期" value-format="YYYY-MM-DD" />
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 病史信息 -->
          <div class="form-section">病史信息</div>
          <el-form-item label="既往病史">
            <el-input v-model="form.medicalHistory" type="textarea" :rows="2" size="large"
              placeholder="请描述既往病史、手术史等" maxlength="500" show-word-limit />
          </el-form-item>
          <el-form-item label="家族病史">
            <el-input v-model="form.familyHistory" type="textarea" :rows="2" size="large"
              placeholder="请描述家族遗传病史" maxlength="500" show-word-limit />
          </el-form-item>
          <el-form-item label="过敏史">
            <el-input v-model="form.allergyHistory" type="textarea" :rows="2" size="large"
              placeholder="请描述药物、食物等过敏情况" maxlength="500" show-word-limit />
          </el-form-item>
          <el-form-item label="生活习惯">
            <el-input v-model="form.lifeHabit" type="textarea" :rows="2" size="large"
              placeholder="吸烟、饮酒、运动、饮食等习惯" maxlength="500" show-word-limit />
          </el-form-item>

          <!-- 紧急联系人 -->
          <div class="form-section">紧急联系人</div>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="联系人姓名">
                <el-input v-model="form.emergencyName" size="large" placeholder="选填" maxlength="30" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="联系人电话">
                <el-input v-model="form.emergencyPhone" size="large" placeholder="选填" maxlength="11" />
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 按钮 -->
          <div class="form-footer">
            <el-button size="large" class="btn-cancel" @click="handleCancel">取 消</el-button>
            <el-button size="large" type="primary" class="btn-submit" @click="handleSubmit">提 交</el-button>
          </div>
        </el-form>
      </div>
    </template>

    <!-- ========== 无档案时的空状态 ========== -->
    <template v-if="!hasArchive && !isEdit">
      <div class="empty-card">
        <el-icon :size="72" color="#cbd5e1"><DocumentAdd /></el-icon>
        <p class="empty-title">暂无健康档案</p>
        <p class="empty-desc">创建您的个人健康档案，方便医生了解您的健康状况</p>
        <el-button type="primary" size="large" class="start-btn" @click="isEdit = true">创建健康档案</el-button>
      </div>
    </template>
  </div>
</template>

<style scoped>
.ar-root { display: flex; flex-direction: column; gap: 20px; max-width: 860px; margin: 0 auto; width: 100%; }

/* ===== 档案卡片（查看模式） ===== */
.archive-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.06);
  padding: 32px;
  border: 1px solid #fef3c7;
}

.card-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 20px;
}
.header-left { display: flex; align-items: center; gap: 16px; }
.header-icon {
  width: 52px; height: 52px; border-radius: 14px;
  background: linear-gradient(135deg, #fb923c, #f97316);
  display: flex; align-items: center; justify-content: center;
}
.header-icon .el-icon { color: #fff; }
.header-info { display: flex; flex-direction: column; gap: 2px; }
.patient-name { font-size: 20px; font-weight: 700; color: #431407; }
.patient-meta { font-size: 13px; color: #78716c; }

.edit-btn {
  height: 38px; border-radius: 10px; padding: 0 20px;
  background: linear-gradient(135deg, #fb923c, #f97316);
  border: none; font-weight: 600; box-shadow: 0 2px 8px rgba(249,115,22,0.3);
}

.record-no {
  font-size: 12px; color: #a8a29e;
  margin-bottom: 20px; padding-bottom: 16px;
  border-bottom: 1px dashed #fef3c7;
}

.info-grid {
  display: grid; grid-template-columns: 1fr 1fr;
  gap: 14px 40px;
}
.info-row {
  display: flex; align-items: baseline; gap: 12px;
}
.info-label {
  font-size: 13px; color: #78716c; white-space: nowrap;
  min-width: 70px;
}
.info-value {
  font-size: 14px; color: #431407; font-weight: 500;
}
.info-value.highlight {
  color: #dc2626; font-weight: 600;
}

.divider {
  height: 1px; background: #fef3c7;
  margin: 16px 0;
}

.text-section { margin-bottom: 12px; }
.text-title {
  font-size: 13px; color: #78716c;
  margin-bottom: 4px;
}
.text-content {
  font-size: 14px; color: #431407; line-height: 1.7;
  margin: 0; white-space: pre-wrap;
}
.text-content.warn { color: #dc2626; font-weight: 600; }

/* ===== 填表卡片 ===== */
.form-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.06);
  padding: 32px;
  border: 1px solid #fef3c7;
}

.form-title {
  font-size: 18px; font-weight: 700; color: #431407;
  display: flex; align-items: center; gap: 10px;
  margin-bottom: 28px;
}
.title-dot {
  width: 5px; height: 22px; border-radius: 3px;
  background: linear-gradient(135deg, #fb923c, #f97316);
}

.form-section {
  font-size: 14px; font-weight: 700; color: #334155;
  margin: 24px 0 16px; padding: 8px 14px;
  background: #fff7ed; border-left: 3px solid #f97316;
  border-radius: 0 6px 6px 0;
}

/* 填表样式：清晰的大输入框 */
.hos-form :deep(.el-input__wrapper) {
  border-radius: 6px;
  box-shadow: 0 0 0 1.5px #fde68a;
  background: #fff;
}
.hos-form :deep(.el-input__wrapper:hover) { box-shadow: 0 0 0 1.5px #fb923c; }
.hos-form :deep(.el-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 2px #f97316;
}
.hos-form :deep(.el-textarea__inner) {
  border-radius: 6px;
  box-shadow: 0 0 0 1.5px #fde68a;
  font-size: 14px; line-height: 1.8;
}
.hos-form :deep(.el-textarea__inner:hover) { box-shadow: 0 0 0 1.5px #fb923c; }
.hos-form :deep(.el-textarea__inner:focus) { box-shadow: 0 0 0 2px #f97316; }
.hos-form :deep(.el-select .el-input__wrapper) { border-radius: 6px; }
.hos-form :deep(.el-form-item__label) {
  font-size: 14px; font-weight: 600; color: #334155;
}
.hos-form :deep(.el-radio-button__inner) {
  border-radius: 6px !important; padding: 8px 24px;
}

.form-footer {
  display: flex; justify-content: flex-end; gap: 12px;
  margin-top: 32px; padding-top: 20px; border-top: 1px solid #fef3c7;
}
.btn-cancel {
  height: 42px; border-radius: 8px; padding: 0 28px;
  border: 1.5px solid #fde68a; color: #78716c; font-size: 14px; font-weight: 600;
}
.btn-submit {
  height: 42px; border-radius: 8px; padding: 0 32px; font-size: 14px; font-weight: 600;
  background: linear-gradient(135deg, #fb923c, #f97316);
  border: none; box-shadow: 0 2px 8px rgba(249,115,22,0.3);
}

/* ===== 空状态 ===== */
.empty-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.06);
  padding: 60px 32px;
  display: flex; flex-direction: column; align-items: center;
  gap: 12px;
  border: 1px solid #fef3c7;
}
.empty-title { font-size: 18px; font-weight: 600; color: #334155; margin: 0; }
.empty-desc { font-size: 14px; color: #a8a29e; margin: 0; }
.start-btn {
  margin-top: 12px; height: 44px; border-radius: 10px; padding: 0 32px; font-size: 15px;
  background: linear-gradient(135deg, #fb923c, #f97316);
  border: none; box-shadow: 0 4px 14px rgba(249,115,22,0.3);
}
</style>
