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
</script>

<template>
  <div class="register-page">
    <div class="deco-layer">
      <div class="glow g1"></div>
      <div class="glow g2"></div>
      <div class="glow g3"></div>
    </div>

    <div class="left-area">
      <div class="hero-text">
        <h1 class="hero-title">慢病健康管理</h1>
        <p class="hero-sub">科学监测 · 专业守护 · 健康生活</p>
      </div>

      <div class="data-cards">
        <div class="data-card">
          <div class="dc-icon dc-ic-blood">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2.69l5.66 5.66a8 8 0 1 1-11.31 0z"/></svg>
          </div>
          <div class="dc-info">
            <span class="dc-val">128/82</span>
            <span class="dc-label">血压 mmHg</span>
          </div>
        </div>
        <div class="data-card">
          <div class="dc-icon dc-ic-sugar">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
          </div>
          <div class="dc-info">
            <span class="dc-val">5.8</span>
            <span class="dc-label">空腹血糖 mmol/L</span>
          </div>
        </div>
        <div class="data-card">
          <div class="dc-icon dc-ic-heart">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
          </div>
          <div class="dc-info">
            <span class="dc-val">76</span>
            <span class="dc-label">静息心率 bpm</span>
          </div>
        </div>
        <div class="data-card">
          <div class="dc-icon dc-ic-pill">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2"><rect x="8" y="2" width="8" height="20" rx="4"/><line x1="12" y1="6" x2="12" y2="10"/><line x1="12" y1="14" x2="12" y2="16"/></svg>
          </div>
          <div class="dc-info">
            <span class="dc-val">3次</span>
            <span class="dc-label">今日用药提醒</span>
          </div>
        </div>
      </div>

      <div class="mini-chart">
        <svg viewBox="0 0 320 80" fill="none" xmlns="http://www.w3.org/2000/svg">
          <line x1="0" y1="20" x2="320" y2="20" stroke="rgba(249,115,22,0.06)" stroke-width="1" />
          <line x1="0" y1="40" x2="320" y2="40" stroke="rgba(249,115,22,0.06)" stroke-width="1" />
          <line x1="0" y1="60" x2="320" y2="60" stroke="rgba(249,115,22,0.06)" stroke-width="1" />
          <polyline points="0,55 30,50 60,42 90,44 120,38 150,35 180,32 210,34 240,28 270,26 300,22 320,24"
            stroke="rgba(249,115,22,0.35)" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round" />
          <polyline points="0,62 30,58 60,55 90,56 120,52 150,48 180,50 210,46 240,44 270,40 300,38 320,36"
            stroke="rgba(251,146,60,0.20)" stroke-width="1.5" fill="none" stroke-linecap="round" stroke-linejoin="round" />
          <circle cx="60" cy="42" r="3" fill="rgba(249,115,22,0.5)" />
          <circle cx="150" cy="35" r="3" fill="rgba(249,115,22,0.5)" />
          <circle cx="240" cy="28" r="3" fill="rgba(249,115,22,0.5)" />
          <circle cx="300" cy="22" r="3" fill="rgba(249,115,22,0.5)" />
        </svg>
        <div class="chart-labels">
          <span>周一</span><span>周二</span><span>周三</span><span>周四</span><span>周五</span><span>周六</span><span>周日</span>
        </div>
        <div class="chart-legend">
          <span class="lg-item"><span class="lg-dot l1"></span>血糖趋势</span>
          <span class="lg-item"><span class="lg-dot l2"></span>收缩压</span>
        </div>
      </div>
    </div>

    <div class="register-card">
      <h2 class="card-title">创建账号</h2>
      <p class="card-sub">开启您的健康管理之旅</p>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="register-form">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" clearable>
            <template #prefix>
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" size="large" clearable>
            <template #prefix>
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 3a2.85 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z"/></svg>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" size="large" clearable maxlength="11">
            <template #prefix>
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><rect x="5" y="2" width="14" height="20" rx="2" ry="2"/><line x1="12" y1="18" x2="12.01" y2="18"/></svg>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="至少6位密码" size="large" show-password>
            <template #prefix>
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <button type="button" class="register-btn" :disabled="loading" @click="handleRegister">
            <span v-if="!loading">注 册</span>
            <span v-else class="btn-loading">
              <svg class="spin" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83"/></svg>
              注册中...
            </span>
          </button>
        </el-form-item>
      </el-form>

      <div class="card-footer">
        <span>已有账号？</span>
        <router-link to="/login" class="footer-link">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-page {
  min-height: 100vh;
  position: relative;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  background: linear-gradient(160deg, #fef3c7 0%, #fffbeb 30%, #fff7ed 60%, #ffedd5 100%);
  overflow: hidden;
  padding: 40px 48px;
}

.deco-layer {
  position: absolute; inset: 0; pointer-events: none; overflow: hidden;
}

.glow {
  position: absolute; border-radius: 50%;
}
.g1 {
  width: 600px; height: 600px;
  top: -200px; left: -100px;
  background: radial-gradient(circle, rgba(251,146,60,0.10) 0%, transparent 55%);
}
.g2 {
  width: 400px; height: 400px;
  bottom: -100px; left: 30%;
  background: radial-gradient(circle, rgba(254,215,170,0.12) 0%, transparent 55%);
}
.g3 {
  width: 300px; height: 300px;
  top: 30%; right: 8%;
  background: radial-gradient(circle, rgba(251,191,36,0.06) 0%, transparent 55%);
}

.left-area {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 28px;
  flex: 1;
  max-width: 700px;
}

.hero-text {
}
.hero-title {
  font-size: 28px; font-weight: 800; color: #7c2d12;
  margin: 0 0 6px; letter-spacing: 4px;
}
.hero-sub {
  font-size: 14px; color: #a8a29e;
  margin: 0; letter-spacing: 2px;
}

.data-cards {
  display: flex; gap: 10px; flex-wrap: wrap;
}
.data-card {
  display: flex; align-items: center; gap: 10px;
  padding: 12px 16px;
  background: rgba(255,255,255,0.65);
  backdrop-filter: blur(6px);
  border-radius: 14px;
  border: 1px solid rgba(249,115,22,0.08);
  box-shadow: 0 2px 12px rgba(194,65,12,0.04);
}
.dc-icon {
  width: 38px; height: 38px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.dc-ic-blood { background: rgba(239,68,68,0.10); color: #ef4444; }
.dc-ic-sugar { background: rgba(249,115,22,0.10); color: #f97316; }
.dc-ic-heart { background: rgba(236,72,153,0.10); color: #ec4899; }
.dc-ic-pill  { background: rgba(34,197,94,0.10); color: #22c55e; }

.dc-info {
  display: flex; flex-direction: column; gap: 2px;
}
.dc-val {
  font-size: 18px; font-weight: 800; color: #7c2d12;
  line-height: 1;
}
.dc-label {
  font-size: 11px; color: #a8a29e;
  white-space: nowrap;
}

.mini-chart {
  width: 100%;
  padding: 24px 28px;
  background: rgba(255,255,255,0.65);
  backdrop-filter: blur(6px);
  border-radius: 16px;
  border: 1px solid rgba(249,115,22,0.08);
  box-shadow: 0 2px 12px rgba(194,65,12,0.04);
}
.mini-chart svg {
  width: 100%; height: 160px;
}
.chart-labels {
  display: flex; justify-content: space-between;
  padding: 0 4px; margin-top: 4px;
}
.chart-labels span {
  font-size: 10px; color: #a8a29e;
}
.chart-legend {
  display: flex; gap: 16px; margin-top: 6px;
  justify-content: center;
}
.lg-item {
  font-size: 11px; color: #78716c;
  display: flex; align-items: center; gap: 5px;
}
.lg-dot {
  width: 8px; height: 8px; border-radius: 2px; display: inline-block;
}
.l1 { background: rgba(249,115,22,0.5); }
.l2 { background: rgba(251,146,60,0.4); }

.register-card {
  flex-shrink: 0;
  width: 360px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 8px 40px rgba(194,65,12,0.10), 0 2px 8px rgba(194,65,12,0.06);
  padding: 36px 32px 32px;
  border: 1px solid #fef3c7;
  margin-top: 160px;
  margin-right: 120px;
}

.card-title {
  font-size: 22px; font-weight: 700; color: #431407;
  text-align: center; margin: 0 0 6px;
}
.card-sub {
  font-size: 14px; color: #a8a29e;
  text-align: center; margin: 0 0 28px;
}

.register-form :deep(.el-form-item) { margin-bottom: 18px; }
.register-form :deep(.el-form-item__label) {
  font-size: 13px; font-weight: 600; color: #78716c; padding-bottom: 6px;
}
.register-form :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 0 0 1.5px #fde68a; background: #fffbeb;
  padding: 4px 12px; transition: all 0.25s;
}
.register-form :deep(.el-input__wrapper:hover) { box-shadow: 0 0 0 1.5px #fbbf24; }
.register-form :deep(.el-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 2px rgba(249,115,22,0.20); background: #fff;
}
.register-form :deep(.el-input__prefix) { color: #a8a29e; margin-right: 6px; }
.register-form :deep(.el-input.is-focus .el-input__prefix) { color: #f97316; }

.register-btn {
  width: 100%; height: 48px; border-radius: 14px; border: none;
  font-size: 16px; font-weight: 700; letter-spacing: 8px;
  color: #fff; cursor: pointer;
  background: linear-gradient(135deg, #fb923c, #f97316);
  box-shadow: 0 4px 20px rgba(249,115,22,0.25);
  transition: all 0.3s ease; margin-top: 6px;
}
.register-btn:hover {
  background: linear-gradient(135deg, #f97316, #ea580c);
  box-shadow: 0 6px 28px rgba(249,115,22,0.35);
  transform: translateY(-2px);
}
.register-btn:active { transform: translateY(0); }
.register-btn:disabled { opacity: 0.7; cursor: not-allowed; transform: none; }

.btn-loading { display: inline-flex; align-items: center; gap: 8px; letter-spacing: 2px; }
.spin { animation: rotate 1s linear infinite; }
@keyframes rotate { to { transform: rotate(360deg); } }

.card-footer { text-align: center; margin-top: 6px; font-size: 14px; color: #a8a29e; }
.footer-link { color: #f97316; text-decoration: none; font-weight: 700; margin-left: 4px; }
.footer-link:hover { color: #c2410c; text-decoration: underline; }

@media (max-width: 768px) {
  .register-page { justify-content: center; padding: 24px 20px; }
  .left-area { display: none; }
  .register-card { width: 100%; max-width: 400px; padding: 32px 28px; }
}
</style>
