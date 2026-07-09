<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '../api/user.js'

const router = useRouter()
const formRef = ref(null)
const form = reactive({ username: '', nickname: '', password: '', phone: '' })
const loading = ref(false)

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await register(form)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    ElMessage.error(e.message || '注册失败')
  } finally {
    loading.value = false
  }
}

const features = {
  monitor: {
    title: '数据监测',
    color: '#3b82f6',
    desc: '全方位健康数据追踪，支持血糖、血压、血脂、心率等多项指标录入与监测。自动生成健康趋势图表，异常指标智能预警，帮助您随时掌握身体状况变化。',
    items: ['多维度指标录入', '健康趋势图表', '异常智能预警', '历史数据对比']
  },
  remind: {
    title: '用药提醒',
    color: '#f59e0b',
    desc: '智能用药提醒系统，支持自定义用药计划、剂量和频次。通过消息推送及时提醒服药，记录用药历史，避免漏服或重复用药，让慢病用药管理更省心。',
    items: ['自定义用药计划', '准时消息推送', '用药记录追踪', '漏服智能提醒']
  },
  consult: {
    title: '医生指导',
    color: '#10b981',
    desc: '在线连接专业医生团队，支持图文咨询与健康评估。医生可查看您的健康数据，提供个性化治疗建议和生活方式指导，让专业医疗触手可及。',
    items: ['在线图文咨询', '健康数据共享', '个性化建议', '定期随访管理']
  },
  article: {
    title: '健康资讯',
    color: '#8b5cf6',
    desc: '汇聚权威慢病科普内容，涵盖饮食营养、运动康复、药物知识、心理调适等方面。根据您的病种和兴趣，精准推送个性化健康资讯，助力科学自我管理。',
    items: ['权威科普文章', '饮食运动指导', '个性化推送', '疾病知识库']
  }
}

const modalVisible = ref(false)
const currentFeature = ref(features.monitor)

function openFeature(key) {
  currentFeature.value = features[key]
  modalVisible.value = true
}
</script>

<template>
  <div class="page">
    <!-- 左侧 -->
    <div class="left-side">
      <!-- 图片上方：品牌 + 功能 -->
      <div class="top-intro">
        <h1 class="title">慢病健康管理</h1>
        <p class="subtitle">科学监测 · 专业守护 · 健康生活</p>
        <div class="feat-grid">
          <div class="feat-bar" @click="openFeature('monitor')">
            <span class="feat-icon icon-blue"><el-icon :size="22"><TrendCharts /></el-icon></span>
            <span class="feat-text">数据监测</span>
          </div>
          <div class="feat-bar" @click="openFeature('remind')">
            <span class="feat-icon icon-orange"><el-icon :size="22"><AlarmClock /></el-icon></span>
            <span class="feat-text">用药提醒</span>
          </div>
          <div class="feat-bar" @click="openFeature('consult')">
            <span class="feat-icon icon-green"><el-icon :size="22"><ChatDotRound /></el-icon></span>
            <span class="feat-text">医生指导</span>
          </div>
          <div class="feat-bar" @click="openFeature('article')">
            <span class="feat-icon icon-purple"><el-icon :size="22"><Document /></el-icon></span>
            <span class="feat-text">健康资讯</span>
          </div>
        </div>
      </div>

      <!-- 医生图片 -->
      <div class="image-box">
        <img src="/doctor.jpg" alt="doctor" />
      </div>

      <!-- 图片下方：描述 + 标签 -->
      <div class="bottom-intro">
        <p class="desc">
          专注于慢性病全周期管理，提供血糖血压监测、用药智能提醒、在线医生指导、个性化健康资讯等一站式服务。
        </p>
        <div class="tag-row">
          <span class="tag blue">高血压</span>
          <span class="tag orange">糖尿病</span>
          <span class="tag green">痛风</span>
          <span class="tag-suffix">等常见慢病</span>
        </div>
      </div>
    </div>

    <!-- 右侧：注册卡片 -->
    <div class="right-side">
      <div class="card">
        <div class="card-head">
          <span class="card-icon"><el-icon :size="20"><UserFilled /></el-icon></span>
          <span class="card-title">创建账号</span>
        </div>
        <p class="card-sub">开启您的健康管理之旅</p>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名" size="large" clearable>
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="form.nickname" placeholder="请输入昵称" size="large" clearable>
              <template #prefix><el-icon><EditPen /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号" size="large" clearable maxlength="11">
              <template #prefix><el-icon><Phone /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" type="password" placeholder="至少6位密码" size="large" show-password>
              <template #prefix><el-icon><Lock /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button :loading="loading" @click="handleRegister" class="btn" size="large">注 册</el-button>
          </el-form-item>
        </el-form>

        <div class="footer">
          已有账号？<router-link to="/login" class="link">立即登录</router-link>
        </div>
      </div>
    </div>

    <!-- 功能介绍弹窗 -->
    <el-dialog v-model="modalVisible" :title="currentFeature.title" width="460px" center>
      <div class="modal-body">
        <div class="modal-icon" :style="{ background: currentFeature.color + '15' }">
          <el-icon :size="36" :color="currentFeature.color"><TrendCharts /></el-icon>
        </div>
        <p class="modal-desc">{{ currentFeature.desc }}</p>
        <div class="modal-items">
          <div class="modal-item" v-for="item in currentFeature.items" :key="item">
            <el-icon :size="16" :color="currentFeature.color"><Check /></el-icon>
            <span>{{ item }}</span>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  background: #e8f2fc;
}

/* ====== 左侧 ====== */
.left-side {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #dce9fa 0%, #e8f2fc 30%, #e8f2fc 70%, #dce9fa 100%);
}

/* 图片上方 */
.top-intro {
  padding: 36px 40px 16px;
  text-align: center;
  flex-shrink: 0;
}
.title { font-size: 32px; font-weight: 700; color: #1e3a5f; margin: 0 0 4px; letter-spacing: 3px; }
.subtitle { font-size: 14px; color: #4a6fa5; margin: 0 0 20px; }
.feat-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}
.feat-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: rgba(255,255,255,0.6);
  border-radius: 12px;
  transition: transform 0.2s, box-shadow 0.2s;
  cursor: pointer;
}
.feat-bar:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59,130,246,0.1);
}
.feat-icon {
  width: 40px; height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.icon-blue   .el-icon { color: #3b82f6; } .icon-blue   { background: rgba(59,130,246,0.1); }
.icon-orange .el-icon { color: #f59e0b; } .icon-orange { background: rgba(245,158,11,0.1); }
.icon-green  .el-icon { color: #10b981; } .icon-green  { background: rgba(16,185,129,0.1); }
.icon-purple .el-icon { color: #8b5cf6; } .icon-purple { background: rgba(139,92,246,0.1); }
.feat-text {
  font-size: 15px;
  font-weight: 600;
  color: #334155;
}

/* 医生图片 */
.image-box {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px 32px;
  min-height: 0;
}
.image-box img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

/* 图片下方 */
.bottom-intro {
  padding: 8px 40px 32px;
  text-align: center;
  flex-shrink: 0;
}
.desc {
  font-size: 16px; color: #5b7a9e;
  line-height: 1.8; margin: 0 0 16px;
}
.tag-row {
  display: flex; align-items: center; gap: 10px;
  justify-content: center; flex-wrap: wrap;
}
.tag {
  display: inline-block; padding: 5px 16px;
  border-radius: 20px; font-size: 15px; font-weight: 600;
}
.tag.blue   { background: rgba(59,130,246,0.12); color: #1d4ed8; }
.tag.orange { background: rgba(245,158,11,0.12); color: #b45309; }
.tag.green  { background: rgba(16,185,129,0.12); color: #065f46; }
.tag-suffix { font-size: 15px; color: #5b7a9e; }

/* ====== 右侧卡片 ====== */
.right-side {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 48px 40px 24px;
}

.card {
  width: 100%;
  max-width: 440px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 4px 24px rgba(59,130,246,0.06);
  padding: 40px 44px;
}

.card-head {
  display: flex; align-items: center; justify-content: center;
  gap: 8px; margin-bottom: 4px;
}
.card-icon {
  width: 36px; height: 36px; border-radius: 10px;
  background: linear-gradient(135deg, #93c5fd, #60a5fa);
  display: flex; align-items: center; justify-content: center;
}
.card-icon .el-icon { color: #fff; }
.card-title { font-size: 20px; font-weight: 700; color: #1e3a5f; }
.card-sub { text-align: center; color: #94a3b8; font-size: 13px; margin-bottom: 28px; }

:deep(.el-form-item__label) { font-size: 13px; font-weight: 600; color: #475569; padding-bottom: 4px; }
:deep(.el-input__wrapper) { border-radius: 10px; box-shadow: 0 0 0 1px #e2e8f0; background: #fff; }
:deep(.el-input__wrapper:hover) { box-shadow: 0 0 0 1px #93c5fd; }
:deep(.el-input.is-focus .el-input__wrapper) { box-shadow: 0 0 0 1px #60a5fa; }

.btn {
  width: 100%; height: 46px; font-size: 15px; letter-spacing: 6px;
  border-radius: 12px;
  background: linear-gradient(135deg, #60a5fa, #3b82f6);
  border: none; color: #fff; margin-top: 4px;
  box-shadow: 0 4px 16px rgba(59,130,246,0.25);
  transition: all 0.3s ease;
}
.btn:hover {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  box-shadow: 0 6px 24px rgba(59,130,246,0.35);
  transform: translateY(-2px);
}
.btn:active { transform: translateY(0); }

.footer { text-align: center; font-size: 13px; color: #94a3b8; margin-top: 10px; }
.link { color: #3b82f6; text-decoration: none; font-weight: 600; margin-left: 4px; }
.link:hover { text-decoration: underline; }

/* ====== 弹窗 ====== */
.modal-body { text-align: center; padding: 8px 0 16px; }
.modal-icon {
  width: 72px; height: 72px; border-radius: 20px;
  display: flex; align-items: center; justify-content: center;
  margin: 0 auto 20px;
}
.modal-desc {
  font-size: 15px; color: #475569;
  line-height: 1.8; margin: 0 0 24px;
}
.modal-items { text-align: left; }
.modal-item {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 16px; margin-bottom: 8px;
  background: #f8fafc; border-radius: 10px;
  font-size: 14px; color: #334155;
}

@media (max-width: 768px) {
  .page { flex-direction: column; }
  .left-side { display: none; }
  .right-side { padding: 24px 16px; }
  .card { padding: 32px 28px; }
}
</style>
