<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()

const navItems = [
  { path: '/patient/home', label: '首页', icon: 'HomeFilled' },
  { path: '/patient/archive', label: '健康档案', icon: 'Folder' },
  { path: '/patient/data', label: '数据监测', icon: 'DataAnalysis' },
  { path: '/patient/remind', label: '用药提醒', icon: 'AlarmClock' },
  { path: '/patient/article', label: '健康资讯', icon: 'Document' },
]

const activeNav = computed(() => route.path)

function goPage(path) {
  if (route.path !== path) router.push(path)
}

function handleLogout() {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
    .then(() => {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      router.push('/login')
    })
    .catch(() => {})
}
</script>

<template>
  <div class="patient-shell">
    <!-- 顶部导航栏 -->
    <header class="top-nav">
      <div class="nav-brand" @click="goPage('/patient/home')">
        <span class="brand-icon">
          <el-icon :size="20"><Monitor /></el-icon>
        </span>
        <span class="brand-text">慢病管理</span>
      </div>

      <nav class="nav-links">
        <span
          v-for="item in navItems"
          :key="item.path"
          :class="['nav-link', { active: activeNav === item.path }]"
          @click="goPage(item.path)"
        >
          <el-icon :size="16"><component :is="item.icon" /></el-icon>
          {{ item.label }}
        </span>
      </nav>

      <div class="nav-actions">
        <span class="user-tag">患者</span>
        <el-button type="danger" text size="small" @click="handleLogout">退出</el-button>
      </div>
    </header>

    <!-- 主体内容 -->
    <main class="main-area">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.patient-shell {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f7fa;
}

/* ---- 顶部导航 ---- */
.top-nav {
  height: 56px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  padding: 0 24px;
  gap: 32px;
  flex-shrink: 0;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  flex-shrink: 0;
}
.brand-icon {
  width: 32px; height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #60a5fa, #3b82f6);
  display: flex; align-items: center; justify-content: center;
}
.brand-icon .el-icon { color: #fff; }
.brand-text { font-size: 16px; font-weight: 700; color: #1e3a5f; }

.nav-links {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
}
.nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}
.nav-link:hover { background: #f1f5f9; color: #1e3a5f; }
.nav-link.active { background: #eff6ff; color: #2563eb; font-weight: 600; }

.nav-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}
.user-tag {
  background: #eff6ff; color: #2563eb;
  padding: 2px 12px; border-radius: 20px;
  font-size: 12px; font-weight: 600;
}

/* ---- 主体 ---- */
.main-area {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}
</style>
