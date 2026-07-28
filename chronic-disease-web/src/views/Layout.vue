<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const isCollapse = ref(false)

const menuItems = [
  { path: '/admin/users', label: '用户管理', icon: 'User', color: '#3b82f6' },
  { path: '/admin/doctors', label: '医生管理', icon: 'Avatar', color: '#10b981' },
  { path: '/admin/archives', label: '健康档案', icon: 'Folder', color: '#10b981' },
  { path: '/admin/dicts', label: '术语字典', icon: 'Collection', color: '#8b5cf6' },
  { path: '/admin/data', label: '健康监测', icon: 'DataAnalysis', color: '#f59e0b' },
  { path: '/admin/remind', label: '用药提醒', icon: 'AlarmClock', color: '#8b5cf6' },
  { path: '/admin/article', label: '健康资讯', icon: 'Document', color: '#ef4444' },
  { path: '/admin/patients', label: '医患沟通', icon: 'ChatDotRound', color: '#06b6d4' },
]

const activeMenu = computed(() => route.path)

function handleMenu(path) {
  router.push(path)
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
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <aside :class="['aside', { collapsed: isCollapse }]">
      <div class="aside-head">
        <div class="head-icon">
          <el-icon :size="20"><Monitor /></el-icon>
        </div>
        <span v-show="!isCollapse" class="head-title">慢病管理系统</span>
      </div>

      <nav class="nav-list">
        <div
          v-for="item in menuItems"
          :key="item.path"
          :class="['nav-item', { active: activeMenu === item.path }]"
          @click="handleMenu(item.path)"
        >
          <span class="nav-icon" :style="{ background: item.color + '15', color: item.color }">
            <el-icon :size="18"><component :is="item.icon" /></el-icon>
          </span>
          <span v-show="!isCollapse" class="nav-label">{{ item.label }}</span>
        </div>
      </nav>
    </aside>

    <!-- 右侧主体 -->
    <div class="right-area">
      <!-- 顶栏 -->
      <header class="top-bar">
        <el-button :icon="isCollapse ? 'Expand' : 'Fold'" text @click="isCollapse = !isCollapse" />
        <div class="top-right">
          <span class="role-badge">管理员</span>
          <el-button type="danger" text size="small" @click="handleLogout">退出登录</el-button>
        </div>
      </header>

      <!-- 内容区 -->
      <main class="main-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped>
.admin-layout { display: flex; height: 100vh; background: #fff7ed; }

/* === 侧边栏 === */
.aside {
  width: 220px;
  background: #fff;
  border-right: 1px solid #fde68a;
  display: flex;
  flex-direction: column;
  transition: width 0.2s;
  overflow: hidden;
  flex-shrink: 0;
}
.aside.collapsed { width: 64px; }

.aside-head {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-bottom: 1px solid #fef3c7;
  padding: 0 16px;
  flex-shrink: 0;
}
.head-icon {
  width: 36px; height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #fb923c, #f97316);
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.head-icon .el-icon { color: #fff; }
.head-title { font-size: 16px; font-weight: 700; color: #7c2d12; white-space: nowrap; letter-spacing: 1px; }

.nav-list { flex: 1; padding: 12px 8px; display: flex; flex-direction: column; gap: 2px; }

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  color: #78716c;
}
.nav-item:hover { background: #fef3c7; color: #7c2d12; }
.nav-item.active { background: #fef3c7; color: #c2410c; font-weight: 600; }

.nav-icon {
  width: 34px; height: 34px;
  border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.nav-label { font-size: 14px; white-space: nowrap; }

/* === 右侧 === */
.right-area { flex: 1; display: flex; flex-direction: column; overflow: hidden; }

.top-bar {
  height: 56px;
  background: #fff;
  border-bottom: 1px solid #fde68a;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  flex-shrink: 0;
}
.top-right { display: flex; align-items: center; gap: 16px; }
.role-badge {
  background: #fef3c7; color: #c2410c;
  padding: 3px 14px; border-radius: 20px;
  font-size: 13px; font-weight: 600;
}

.main-content { flex: 1; padding: 24px; overflow-y: auto; background: #fff7ed; }
</style>
