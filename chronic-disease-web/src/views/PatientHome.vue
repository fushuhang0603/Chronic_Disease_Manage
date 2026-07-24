<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTopArticles, updateFavoriteStatus, getMyDoctor } from '../api/user'
import { ArrowRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()

// ====== 患者信息 ======
const userName = ref('')
try {
  const raw = localStorage.getItem('userInfo')
  if (raw) {
    const info = JSON.parse(raw)
    userName.value = info.name || info.userName || info.realName || ''
  }
} catch {}

// ====== 问候语 ======
function getGreeting() {
  const h = new Date().getHours()
  if (h < 9) return '早上好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
}
function todayStr() {
  const d = new Date()
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 星期${'日一二三四五六'[d.getDay()]}`
}

// ====== 功能入口 ======
const featureCards = [
  { title: '健康档案', desc: '管理个人信息与病史', color: '#3b82f6', bg: 'linear-gradient(135deg, #eff6ff, #dbeafe)', path: '/patient/archive' },
  { title: '健康监测', desc: '血压血糖趋势分析', color: '#10b981', bg: 'linear-gradient(135deg, #ecfdf5, #d1fae5)', path: '/patient/data' },
  { title: '用药提醒', desc: '准时推送服药计划', color: '#f59e0b', bg: 'linear-gradient(135deg, #fffbeb, #fef3c7)', path: '/patient/remind' },
  { title: '全部资讯', desc: '浏览慢病科普文章', color: '#8b5cf6', bg: 'linear-gradient(135deg, #f5f3ff, #ede9fe)', path: '/patient/article' },
]

// ====== 慢病指标 ======
const indicators = [
  { label: '理想血压', value: '<120/80', unit: 'mmHg', bg: 'linear-gradient(135deg, #eff6ff, #dbeafe)', color: '#2563eb' },
  { label: '高血压', value: '≥140/90', unit: 'mmHg', bg: 'linear-gradient(135deg, #f0f4ff, #dbeafe)', color: '#1d4ed8' },
  { label: '空腹血糖', value: '3.9~6.1', unit: 'mmol/L', bg: 'linear-gradient(135deg, #eff6ff, #dbeafe)', color: '#2563eb' },
  { label: '糖尿病', value: '≥11.1', unit: 'mmol/L', bg: 'linear-gradient(135deg, #f0f4ff, #dbeafe)', color: '#1d4ed8' },
]

// ====== 每日提醒 ======
const tipGradients = [
  'linear-gradient(135deg, #eff6ff, #dbeafe)',
  'linear-gradient(135deg, #ecfdf5, #d1fae5)',
  'linear-gradient(135deg, #fffbeb, #fef3c7)',
  'linear-gradient(135deg, #f5f3ff, #ede9fe)',
]
const tips = [
  { text: '低盐饮食：每日食盐 < 6g，少吃腌制品' },
  { text: '每天30分钟户外活动，多晒太阳' },
  { text: '晨起1小时内测血压，测前静坐5分钟' },
  { text: '胸闷、头痛剧烈、视物模糊，立即就医' },
]

// ====== 医生绑定 ======
const myDoctor = ref(null)

async function loadMyDoctor() {
  try {
    myDoctor.value = await getMyDoctor()
  } catch { myDoctor.value = null }
}

function goDoctors() { router.push('/patient/doctors') }

// ====== 资讯 ======
const articles = ref([])

async function loadArticles() {
  try {
    articles.value = await getTopArticles(5) || []
  } catch { /* 无数据不报错 */ }
}

function getSummary(content) {
  if (!content) return ''
  return content.replace(/\s+/g, ' ').substring(0, 60) + (content.length > 60 ? '...' : '')
}
function fmtTime(t) {
  return t ? t.replace('T', ' ').substring(0, 10) : '-'
}

// ====== 详情弹窗 ======
const showDetail = ref(false)
const detailItem = ref(null)

function openDetail(item) {
  detailItem.value = item
  showDetail.value = true
  try { fetch('/api/article/readHistory', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ articleId: item.id, readDuration: 5 }) }) } catch {}
}

async function handleFavorite(item) {
  try {
    const status = item.isFavorited ? 1 : 0
    await updateFavoriteStatus(item.id, status)
    item.isFavorited = !item.isFavorited
    ElMessage.success(item.isFavorited ? '已收藏' : '已取消收藏')
  } catch { ElMessage.error('操作失败') }
}

function goPage(path) { router.push(path) }
function goArticle() { router.push('/patient/article') }

onMounted(() => { loadArticles(); loadMyDoctor() })
</script>

<template>
  <div class="home-root">
    <!-- ====== 问候横幅 ====== -->
    <div class="hero-banner">
      <div class="hero-left">
        <div class="hero-top-row">
          <span class="hero-greeting">{{ getGreeting() }}<template v-if="userName">，{{ userName }}</template></span>
          <span class="hero-badge">今日</span>
        </div>
        <p class="hero-date">{{ todayStr() }}</p>
        <p class="hero-quote">科学管理慢病，享受品质生活</p>
        <div class="hero-tags">
          <span class="hero-tag" @click="goPage('/patient/remind')">用药提醒</span>
          <span class="hero-tag" @click="goPage('/patient/data')">记录指标</span>
          <span class="hero-tag" @click="goArticle">健康资讯</span>
        </div>
      </div>
      <div class="hero-right">
        <div class="hero-decor">
          <svg viewBox="0 0 140 120" width="120" height="104" fill="none">
            <circle cx="60" cy="44" r="38" fill="#dbeafe" opacity="0.6"/>
            <circle cx="82" cy="68" r="30" fill="#bfdbfe" opacity="0.5"/>
            <circle cx="100" cy="36" r="16" fill="#e0e7ff" opacity="0.6"/>
            <path d="M32 82 Q60 32 88 82" stroke="#93c5fd" stroke-width="2.5" fill="none" opacity="0.4"/>
            <path d="M40 90 Q60 52 80 90" stroke="#93c5fd" stroke-width="2" fill="none" opacity="0.35"/>
            <rect x="90" y="70" width="28" height="6" rx="3" fill="#60a5fa" opacity="0.3" transform="rotate(-12 104 73)"/>
            <rect x="96" y="80" width="20" height="4" rx="2" fill="#60a5fa" opacity="0.25" transform="rotate(-12 106 82)"/>
          </svg>
        </div>
        <div class="hero-stats">
          <div class="hero-stat" v-if="articles.length > 0">
            <span class="hs-num">{{ articles.length }}</span>
            <span class="hs-label">篇新资讯</span>
          </div>
          <div class="hero-stat">
            <span class="hs-num">{{ indicators.length }}</span>
            <span class="hs-label">项关键指标</span>
          </div>
        </div>
      </div>
    </div>

    <!-- ====== 功能入口 ====== -->
    <div class="feature-section">
      <div v-for="card in featureCards" :key="card.path" class="feature-card" :style="{ background: card.bg }" @click="goPage(card.path)">
        <span class="fc-label" :style="{ color: card.color }">{{ card.title }}</span>
        <span class="fc-desc">{{ card.desc }}</span>
        <el-icon class="fc-arrow" :size="15"><ArrowRight /></el-icon>
      </div>
      <!-- 我的医生 -->
      <div class="feature-card fc-doctor" :style="{ background: 'linear-gradient(135deg, #f0fdfa, #ccfbf1)' }" @click="goDoctors">
        <span class="fc-label" style="color:#0d9488">{{ myDoctor ? myDoctor.realName : '我的医生' }}</span>
        <span class="fc-desc">{{ myDoctor ? (myDoctor.title + ' · ' + myDoctor.hospital) : '绑定专属医生' }}</span>
        <el-icon class="fc-arrow" :size="15"><ArrowRight /></el-icon>
      </div>
    </div>

    <!-- ====== 健康资讯 ====== -->
    <div class="article-section" v-if="articles.length > 0">
      <div class="section-hd">
        <span class="shd-title">健康资讯</span>
        <span class="shd-more" @click="goArticle">查看全部 <el-icon :size="14"><ArrowRight /></el-icon></span>
      </div>
      <div class="article-row">
        <div v-for="item in articles.slice(0, 5)" :key="item.id" class="article-card" @click="openDetail(item)">
          <div class="ac-top">
            <span class="ac-cat">{{ item.category }}</span>
            <span class="ac-fav" :class="{ on: item.isFavorited }" @click.stop="handleFavorite(item)">
              {{ item.isFavorited ? '已收藏' : '收藏' }}
            </span>
          </div>
          <h3 class="ac-title">{{ item.title }}</h3>
          <p class="ac-desc">{{ getSummary(item.content) }}</p>
          <div class="ac-bottom">
            <span>{{ item.viewCount || 0 }} 阅读</span>
            <span>{{ fmtTime(item.publishingTime) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- ====== 指标 + 贴士 ====== -->
    <div class="dual-row">
      <!-- 指标参考 -->
      <div class="indicator-card">
        <div class="section-hd">
          <span class="shd-title">指标参考</span>
        </div>
        <div class="ind-grid">
          <div v-for="ind in indicators" :key="ind.label" class="ind-item" :style="{ background: ind.bg, borderColor: ind.color + '30' }">
            <span class="ind-label">{{ ind.label }}</span>
            <span class="ind-value" :style="{ color: ind.color }">{{ ind.value }}</span>
            <span class="ind-unit">{{ ind.unit }}</span>
          </div>
        </div>
      </div>

      <!-- 每日提醒 -->
      <div class="tips-card">
        <div class="section-hd">
          <span class="shd-title">每日提醒</span>
        </div>
        <div class="tips-list">
          <div v-for="(tip, idx) in tips" :key="idx" class="tip-item" :style="{ background: tipGradients[idx] }">
            <span class="tip-dot">{{ idx + 1 }}</span>
            <span>{{ tip.text }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- ====== 详情弹窗 ====== -->
    <el-dialog v-model="showDetail" :title="detailItem?.title" width="700px" :close-on-click-modal="false" destroy-on-close>
      <div class="detail-wrap" v-if="detailItem">
        <div class="detail-meta">
          <span class="detail-tag">{{ detailItem.category }}</span>
          <span>{{ detailItem.viewCount || 0 }} 阅读</span>
          <span>{{ fmtTime(detailItem.publishingTime) }}</span>
        </div>
        <div class="detail-content">{{ detailItem.content }}</div>
      </div>
      <template #footer>
        <el-button @click="showDetail = false">关闭</el-button>
        <el-button type="warning" v-if="detailItem" @click="handleFavorite(detailItem); detailItem.isFavorited = !detailItem.isFavorited">
          {{ detailItem.isFavorited ? '取消收藏' : '加入收藏' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.home-root {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  padding: 20px 24px 40px;
  display: flex;
  flex-direction: column;
  gap: 24px;
  box-sizing: border-box;
}

/* 大屏适配 */
@media (min-width: 1400px) {
  .home-root { padding: 24px 40px 48px; }
}

/* ====== 问候横幅 ====== */
.hero-banner {
  display: flex; align-items: center; justify-content: space-between;
  padding: 28px 32px; border-radius: 20px;
  background: linear-gradient(135deg, #eff6ff 0%, #f0f9ff 40%, #faf5ff 100%);
  border: 1px solid #e0e7ff; overflow: hidden; position: relative; gap: 20px;
}
.hero-left { position: relative; z-index: 1; flex: 1; }
.hero-top-row { display: flex; align-items: center; gap: 10px; margin-bottom: 4px; }
.hero-greeting { font-size: 22px; font-weight: 800; color: #1e293b; line-height: 1.3; }
.hero-badge {
  font-size: 11px; font-weight: 600; padding: 2px 10px; border-radius: 10px;
  background: linear-gradient(135deg, #dbeafe, #eff6ff); color: #2563eb;
  border: 1px solid #bfdbfe;
}
.hero-date { font-size: 13px; color: #64748b; margin: 0 0 6px 0; }
.hero-quote { font-size: 13px; color: #3b82f6; margin: 0 0 12px 0; font-weight: 500; }
.hero-tags { display: flex; gap: 8px; flex-wrap: wrap; }
.hero-tag {
  font-size: 12px; padding: 4px 14px; border-radius: 14px; cursor: pointer;
  background: rgba(255,255,255,0.7); color: #475569;
  border: 1px solid #e2e8f0; transition: all 0.15s; font-weight: 500;
}
.hero-tag:hover { background: #fff; border-color: #3b82f6; color: #2563eb; }
/* 右侧 */
.hero-right { display: flex; align-items: center; gap: 16px; flex-shrink: 0; z-index: 1; }
.hero-decor { opacity: 0.75; flex-shrink: 0; }
.hero-stats { display: flex; flex-direction: column; gap: 8px; }
.hero-stat {
  display: flex; align-items: center; gap: 8px;
  padding: 6px 12px; border-radius: 10px;
  background: rgba(255,255,255,0.6); border: 1px solid #e2e8f0;
  white-space: nowrap;
}
.hs-num { font-size: 18px; font-weight: 800; color: #3b82f6; }
.hs-label { font-size: 11px; color: #64748b; font-weight: 500; }

/* ====== 功能入口 ====== */
.feature-section {
  display: grid; grid-template-columns: repeat(5, 1fr); gap: 14px;
}
.feature-card {
  display: flex; flex-direction: column; gap: 8px;
  padding: 20px 18px; border-radius: 14px; cursor: pointer;
  border: 1px solid #e2e8f0; position: relative;
  transition: transform 0.2s, box-shadow 0.2s;
}
.feature-card:hover { transform: translateY(-2px); box-shadow: 0 6px 24px rgba(0,0,0,0.08); }
.fc-label { font-size: 15px; font-weight: 700; }
.fc-desc { font-size: 12px; color: #94a3b8; line-height: 1.5; }
.fc-arrow { position: absolute; right: 14px; top: 22px; color: #cbd5e1; }
.feature-card:hover .fc-arrow { color: #64748b; }

/* ====== 分区标题 ====== */
.section-hd {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 16px;
}
.shd-title { font-size: 16px; font-weight: 700; color: #1e293b; }
.shd-more {
  font-size: 13px; color: #3b82f6; cursor: pointer; font-weight: 500;
  display: flex; align-items: center; gap: 2px;
}
.shd-more:hover { color: #1d4ed8; }

/* ====== 健康资讯卡片横排 ====== */
.article-section {
  background: #fff; border: 1px solid #e2e8f0; border-radius: 16px;
  padding: 24px; box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.article-row { display: flex; gap: 12px; overflow-x: auto; padding-bottom: 4px; }
.article-card {
  flex: 1 0 180px; min-width: 160px;
  border: 1px solid #f1f5f9; border-radius: 12px; padding: 14px;
  cursor: pointer; transition: all 0.2s; display: flex; flex-direction: column;
  background: #fafcff;
}
.article-card:hover {
  border-color: #bfdbfe; box-shadow: 0 4px 16px rgba(59,130,246,0.08);
  background: #fff;
}
.ac-top {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px;
}
.ac-cat {
  font-size: 11px; font-weight: 600; padding: 2px 8px; border-radius: 5px;
  background: #eff6ff; color: #3b82f6;
}
.ac-fav {
  font-size: 11px; color: #94a3b8; cursor: pointer; transition: color 0.15s;
}
.ac-fav:hover { color: #3b82f6; }
.ac-fav.on { color: #f59e0b; font-weight: 600; }
.ac-title {
  font-size: 14px; font-weight: 700; color: #1e293b; margin: 0 0 8px 0;
  overflow: hidden; text-overflow: ellipsis; display: -webkit-box;
  -webkit-line-clamp: 2; -webkit-box-orient: vertical; line-height: 1.5;
}
.ac-desc {
  font-size: 12px; color: #94a3b8; line-height: 1.6; margin: 0 0 auto 0;
  overflow: hidden; text-overflow: ellipsis;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
}
.ac-bottom {
  display: flex; justify-content: space-between; font-size: 11px;
  color: #cbd5e1; margin-top: 12px; padding-top: 10px;
  border-top: 1px solid #f1f5f9;
}

/* ====== 指标 + 贴士双栏 ====== */
.dual-row { display: flex; gap: 20px; }
.indicator-card, .tips-card {
  flex: 1; background: #fff; border: 1px solid #e2e8f0;
  border-radius: 16px; padding: 24px; box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

/* 指标网格 */
.ind-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.ind-item {
  display: flex; flex-direction: column; align-items: center;
  padding: 14px 10px; border-radius: 10px; border: 1px solid transparent;
}
.ind-label { font-size: 12px; color: #64748b; margin-bottom: 4px; }
.ind-value { font-size: 20px; font-weight: 800; line-height: 1.3; }
.ind-unit { font-size: 11px; color: #94a3b8; margin-top: 2px; }

/* 每日提醒 */
.tips-list { display: flex; flex-direction: column; gap: 10px; }
.tip-item {
  display: flex; align-items: center; gap: 12px;
  font-size: 13px; color: #475569; line-height: 1.6;
  padding: 10px 14px; border-radius: 10px;
}
.tip-dot {
  width: 22px; height: 22px; border-radius: 50%;
  background: #eff6ff; color: #3b82f6; font-size: 11px; font-weight: 700;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}

/* ====== 详情弹窗 ====== */
.detail-wrap { padding: 4px 0; }
.detail-meta { display: flex; align-items: center; gap: 12px; margin-bottom: 18px; font-size: 12px; color: #94a3b8; }
.detail-tag {
  display: inline-block; padding: 2px 8px; border-radius: 5px;
  background: #eff6ff; color: #1d4ed8; font-size: 12px; font-weight: 600;
}
.detail-content {
  font-size: 15px; color: #334155; line-height: 2; white-space: pre-wrap;
  max-height: 480px; overflow-y: auto;
}
:deep(.el-dialog) { border-radius: 16px; }
:deep(.el-dialog__header) { padding: 24px 28px 0; }
:deep(.el-dialog__title) { font-size: 18px; font-weight: 700; max-width: 580px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; display: block; }
:deep(.el-dialog__body) { padding: 16px 28px; }
:deep(.el-dialog__footer) { padding: 0 28px 24px; }

/* ====== 响应式 ====== */
@media (max-width: 1100px) {
  .feature-section { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 900px) {
  .feature-section { grid-template-columns: 1fr 1fr; }
}
@media (max-width: 640px) {
  .feature-section { grid-template-columns: 1fr; }
  .dual-row { flex-direction: column; }
  .hero-banner { padding: 20px; flex-direction: column; align-items: flex-start; }
  .hero-right { flex-direction: row; width: 100%; justify-content: space-between; }
  .hero-greeting { font-size: 18px; }
}
</style>
