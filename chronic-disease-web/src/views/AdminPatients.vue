<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getMyPatients } from '../api/user.js'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const allPatients = ref([])
const searchName = ref('')

// 获取当前医生ID
const doctorId = (() => {
  try {
    const raw = sessionStorage.getItem('userInfo')
    if (raw) return JSON.parse(raw).id
  } catch {}
  return null
})()

// 客户端搜索过滤
const patients = computed(() => {
  if (!searchName.value) return allPatients.value
  const kw = searchName.value.toLowerCase()
  return allPatients.value.filter(p =>
    (p.realName || '').toLowerCase().includes(kw) ||
    (p.username || '').toLowerCase().includes(kw)
  )
})

onMounted(() => fetchPatients())

async function fetchPatients() {
  if (!doctorId) {
    ElMessage.error('获取医生信息失败')
    return
  }
  loading.value = true
  try {
    const data = await getMyPatients(doctorId)
    allPatients.value = data || []
  } catch (e) {
    ElMessage.error('加载患者列表失败')
  } finally {
    loading.value = false
  }
}

function goChat(p) {
  router.push({
    path: '/admin/patients/chat',
    query: { userId: p.id, userName: p.realName || p.username },
  })
}

function handleSearch() {
  // computed 自动响应，无需额外操作
}
</script>

<template>
  <div class="ap-root">
    <div class="ap-header">
      <h3 class="ap-title">医患沟通</h3>
      <div class="ap-search">
        <el-input v-model="searchName" placeholder="搜索患者姓名" clearable
          size="default" @keyup.enter="handleSearch" style="width: 220px">
          <template #prefix><el-icon :size="14"><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" class="ap-search-btn" @click="handleSearch">搜索</el-button>
      </div>
    </div>

    <div v-loading="loading" class="ap-list">
      <div v-for="p in patients" :key="p.id" class="ap-card" @click="goChat(p)">
        <div class="ap-avatar">{{ (p.realName || p.username || '患')[0] }}</div>
        <div class="ap-info">
          <span class="ap-name">{{ p.realName || p.username }}</span>
          <span class="ap-tip">点击开始沟通</span>
        </div>
        <el-icon :size="16" color="#a8a29e"><ChatDotRound /></el-icon>
      </div>
      <div v-if="!loading && patients.length === 0" class="ap-empty">
        暂无患者数据
      </div>
    </div>
  </div>
</template>

<style scoped>
.ap-root {
  padding: 20px 24px; max-width: 900px; margin: 0 auto;
}
.ap-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 16px;
}
.ap-title { font-size: 18px; font-weight: 700; color: #431407; margin: 0; }
.ap-search { display: flex; gap: 8px; }
.ap-search-btn {
  height: 34px; border-radius: 10px; padding: 0 16px;
  background: linear-gradient(135deg, #f97316, #ea580c);
  border: none; font-weight: 600;
}

.ap-list { display: flex; flex-direction: column; gap: 8px; }
.ap-card {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 16px; border-radius: 12px;
  background: #fff; border: 1px solid #fef3c7;
  cursor: pointer; transition: all 0.15s;
}
.ap-card:hover { background: #fffbeb; border-color: #fbbf24; }
.ap-avatar {
  width: 40px; height: 40px; border-radius: 12px;
  background: linear-gradient(135deg, #f97316, #ea580c);
  color: #fff; font-size: 16px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.ap-info { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.ap-name { font-size: 14px; font-weight: 600; color: #431407; }
.ap-tip { font-size: 12px; color: #a8a29e; }
.ap-empty { text-align: center; padding: 60px 0; color: #a8a29e; }
</style>
