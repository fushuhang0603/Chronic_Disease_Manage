<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { getTopArticles, updateFavoriteStatus, getMyDoctor, getHomeNotices } from '../api/user'
import { ArrowRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()

// ====== 患者信息 ======
const userName = ref('')
try {
  const raw = sessionStorage.getItem('userInfo')
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

function goChat() {
  if (myDoctor.value) {
    router.push({ path: '/patient/chat', query: { doctorId: myDoctor.value.doctorId, doctorName: myDoctor.value.realName } })
  }
}

// ====== 资讯 ======
const articles = ref([])

async function loadArticles() {
  try {
    articles.value = await getTopArticles(5) || []
  } catch { /* 无数据不报错 */ }
}

// ====== 平台公告 ======
const notices = ref([])
const showNoticeDetail = ref(false)
const noticeDetail = ref(null)

async function loadNotices() {
  try {
    notices.value = await getHomeNotices() || []
  } catch { /* 无公告不报错 */ }
}

function openNotice(item) {
  noticeDetail.value = item
  showNoticeDetail.value = true
}

function goNotice() { router.push('/patient/notice') }

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
function goAssistant() { router.push('/patient/assistant') }

onMounted(() => { loadArticles(); loadMyDoctor(); loadNotices() })
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
            <circle cx="60" cy="44" r="38" fill="#fef3c7" opacity="0.7"/>
            <circle cx="82" cy="68" r="30" fill="#fde68a" opacity="0.6"/>
            <circle cx="100" cy="36" r="16" fill="#fef3c7" opacity="0.7"/>
            <path d="M32 82 Q60 32 88 82" stroke="#fbbf24" stroke-width="2.5" fill="none" opacity="0.4"/>
            <path d="M40 90 Q60 52 80 90" stroke="#fbbf24" stroke-width="2" fill="none" opacity="0.35"/>
            <rect x="90" y="70" width="28" height="6" rx="3" fill="#f59e0b" opacity="0.3" transform="rotate(-12 104 73)"/>
            <rect x="96" y="80" width="20" height="4" rx="2" fill="#f59e0b" opacity="0.25" transform="rotate(-12 106 82)"/>
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

    <!-- ====== 智能医助入口 ====== -->
    <div class="ai-entry-card" @click="goAssistant">
      <div class="ai-entry-glow"></div>
      <div class="ai-entry-orb orb-1"></div>
      <div class="ai-entry-orb orb-2"></div>
      <div class="ai-entry-content">
        <div class="ai-entry-icon-wrap">
          <svg viewBox="0 0 24 24" width="26" height="26" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="8" r="4"/>
            <path d="M5 21c0-3.9 3.1-7 7-7s7 3.1 7 7"/>
            <path d="M12 3v2"/>
            <circle cx="18" cy="5" r="2" fill="currentColor" stroke="none" opacity="0.3"/>
            <circle cx="6" cy="18" r="1.5" fill="currentColor" stroke="none" opacity="0.2"/>
            <circle cx="20" cy="15" r="1" fill="currentColor" stroke="none" opacity="0.25"/>
          </svg>
        </div>
        <div class="ai-entry-text">
          <h3 class="ai-entry-title">
            智能医助
            <span class="ai-entry-badge">AI</span>
          </h3>
          <p class="ai-entry-desc">你的专属健康顾问，随时在线解答</p>
        </div>
        <div class="ai-entry-btn">
          <span>开始对话</span>
          <el-icon :size="16"><ArrowRight /></el-icon>
        </div>
      </div>
    </div>

    <!-- ====== 平台公告 ====== -->
    <div class="notice-section" v-if="notices.length > 0">
      <div class="section-hd">
        <span class="shd-title">平台公告</span>
        <span class="shd-more" @click="goNotice">查看全部 <el-icon :size="14"><ArrowRight /></el-icon></span>
      </div>
      <div class="notice-list">
        <div v-for="n in notices" :key="n.id" class="notice-item" @click="openNotice(n)">
          <span class="notice-badge">公告</span>
          <span class="notice-title">{{ n.title }}</span>
          <span class="notice-time">{{ fmtTime(n.publishTime) }}</span>
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
      <div class="feature-card fc-doctor" :style="{ background: 'linear-gradient(135deg, #fffbeb, #fef3c7)' }" @click="goDoctors">
        <span class="fc-label" style="color:#c2410c">{{ myDoctor ? myDoctor.realName : '我的医生' }}</span>
        <span class="fc-desc">{{ myDoctor ? (myDoctor.title + ' · ' + myDoctor.hospital) : '绑定专属医生' }}</span>
        <el-icon class="fc-arrow" :size="15"><ArrowRight /></el-icon>
      </div>
    </div>

    <!-- ====== 在线问诊 ====== -->
    <div class="consult-section" v-if="myDoctor">
      <div class="consult-card">
        <!-- 左侧：医生头像 -->
        <div class="consult-avatar">
          <span class="consult-avatar-text">{{ (myDoctor.realName || '医')[0] }}</span>
          <div class="consult-avatar-dot"></div>
        </div>
        <!-- 中间：信息区 -->
        <div class="consult-body">
          <div class="consult-head">
            <h3 class="consult-title">在线问诊</h3>
            <span class="consult-tag">专属医生</span>
          </div>
          <p class="consult-desc">随时向您的主治医生发起在线咨询，获得专业健康指导</p>
          <div class="consult-doctor-row">
            <span class="cdr-name">{{ myDoctor.realName }}</span>
            <span class="cdr-sep"></span>
            <span class="cdr-meta">{{ myDoctor.title }}</span>
            <span class="cdr-sep"></span>
            <span class="cdr-meta">{{ myDoctor.hospital }}</span>
          </div>
        </div>
        <!-- 右侧：操作按钮 -->
        <button class="consult-btn" @click="goChat">
          <el-icon :size="18"><ChatDotRound /></el-icon>
          <span>开始问诊</span>
        </button>
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
        <el-button type="warning" v-if="detailItem" @click="handleFavorite(detailItem)">
          {{ detailItem.isFavorited ? '取消收藏' : '加入收藏' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- ====== 公告详情弹窗 ====== -->
    <el-dialog v-model="showNoticeDetail" :title="noticeDetail?.title" width="700px" :close-on-click-modal="false" destroy-on-close>
      <div class="detail-wrap" v-if="noticeDetail">
        <div class="detail-meta">
          <span class="detail-tag">公告</span>
          <span>发布人：{{ noticeDetail.publisherName || '管理员' }}</span>
          <span>{{ fmtTime(noticeDetail.publishTime) }}</span>
        </div>
        <div class="detail-content">{{ noticeDetail.content }}</div>
      </div>
      <template #footer>
        <el-button @click="showNoticeDetail = false">关闭</el-button>
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
  background: linear-gradient(135deg, #fff7ed 0%, #fef3c7 40%, #fffbeb 100%);
  border: 1px solid #fde68a; overflow: hidden; position: relative; gap: 20px;
}
.hero-left { position: relative; z-index: 1; flex: 1; }
.hero-top-row { display: flex; align-items: center; gap: 10px; margin-bottom: 4px; }
.hero-greeting { font-size: 22px; font-weight: 800; color: #431407; line-height: 1.3; }
.hero-badge {
  font-size: 11px; font-weight: 600; padding: 2px 10px; border-radius: 10px;
  background: linear-gradient(135deg, #fef3c7, #fde68a); color: #92400e;
  border: 1px solid #fbbf24;
}
.hero-date { font-size: 13px; color: #78716c; margin: 0 0 6px 0; }
.hero-quote { font-size: 13px; color: #c2410c; margin: 0 0 12px 0; font-weight: 500; }
.hero-tags { display: flex; gap: 8px; flex-wrap: wrap; }
.hero-tag {
  font-size: 12px; padding: 4px 14px; border-radius: 14px; cursor: pointer;
  background: rgba(255,255,255,0.7); color: #78716c;
  border: 1px solid #fde68a; transition: all 0.15s; font-weight: 500;
}
.hero-tag:hover { background: #fff; border-color: #f97316; color: #c2410c; }
/* 右侧 */
.hero-right { display: flex; align-items: center; gap: 16px; flex-shrink: 0; z-index: 1; }
.hero-decor { opacity: 0.75; flex-shrink: 0; }
.hero-stats { display: flex; flex-direction: column; gap: 8px; }
.hero-stat {
  display: flex; align-items: center; gap: 8px;
  padding: 6px 12px; border-radius: 10px;
  background: rgba(255,255,255,0.6); border: 1px solid #fde68a;
  white-space: nowrap;
}
.hs-num { font-size: 18px; font-weight: 800; color: #c2410c; }
.hs-label { font-size: 11px; color: #78716c; font-weight: 500; }

/* ====== 功能入口 ====== */
.feature-section {
  display: grid; grid-template-columns: repeat(5, 1fr); gap: 14px;
}
.feature-card {
  display: flex; flex-direction: column; gap: 8px;
  padding: 20px 18px; border-radius: 14px; cursor: pointer;
  border: 1px solid #fef3c7; position: relative; background: #fff;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  transition: transform 0.2s, box-shadow 0.2s;
}
.feature-card:hover { transform: translateY(-2px); box-shadow: 0 6px 24px rgba(249,115,22,0.1); }
.fc-label { font-size: 15px; font-weight: 700; }
.fc-desc { font-size: 12px; color: #a8a29e; line-height: 1.5; }
.fc-arrow { position: absolute; right: 14px; top: 22px; color: #d6d3d1; }
.feature-card:hover .fc-arrow { color: #78716c; }

/* ====== 分区标题 ====== */
.section-hd {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 16px;
}
.shd-title { font-size: 16px; font-weight: 700; color: #431407; }
.shd-more {
  font-size: 13px; color: #c2410c; cursor: pointer; font-weight: 500;
  display: flex; align-items: center; gap: 2px;
}
.shd-more:hover { color: #9a3412; }

/* ====== 平台公告 ====== */
.notice-section {
  background: #fff; border: 1px solid #fef3c7; border-radius: 16px;
  padding: 24px; box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.notice-list { display: flex; flex-direction: column; gap: 2px; }
.notice-item {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 14px; border-radius: 10px; cursor: pointer;
  transition: background 0.15s;
}
.notice-item:hover { background: #fff7ed; }
.notice-badge {
  flex-shrink: 0; font-size: 11px; font-weight: 700;
  padding: 2px 8px; border-radius: 5px;
  background: linear-gradient(135deg, #f97316, #ea580c); color: #fff;
}
.notice-title {
  flex: 1; min-width: 0; font-size: 14px; font-weight: 600; color: #431407;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.notice-time { flex-shrink: 0; font-size: 12px; color: #a8a29e; }

/* ====== 健康资讯卡片横排 ====== */
.article-section {
  background: #fff; border: 1px solid #fef3c7; border-radius: 16px;
  padding: 24px; box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.article-row { display: flex; gap: 12px; overflow-x: auto; padding-bottom: 4px; }
.article-card {
  flex: 1 0 180px; min-width: 160px;
  border: 1px solid #fef3c7; border-radius: 12px; padding: 14px;
  cursor: pointer; transition: all 0.2s; display: flex; flex-direction: column;
  background: #fffbeb;
}
.article-card:hover {
  border-color: #fbbf24; box-shadow: 0 4px 16px rgba(249,115,22,0.08);
  background: #fff;
}
.ac-top {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px;
}
.ac-cat {
  font-size: 11px; font-weight: 600; padding: 2px 8px; border-radius: 5px;
  background: #fef3c7; color: #c2410c;
}
.ac-fav {
  font-size: 11px; color: #a8a29e; cursor: pointer; transition: color 0.15s;
}
.ac-fav:hover { color: #c2410c; }
.ac-fav.on { color: #f59e0b; font-weight: 600; }
.ac-title {
  font-size: 14px; font-weight: 700; color: #431407; margin: 0 0 8px 0;
  overflow: hidden; text-overflow: ellipsis; display: -webkit-box;
  -webkit-line-clamp: 2; -webkit-box-orient: vertical; line-height: 1.5;
}
.ac-desc {
  font-size: 12px; color: #a8a29e; line-height: 1.6; margin: 0 0 auto 0;
  overflow: hidden; text-overflow: ellipsis;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
}
.ac-bottom {
  display: flex; justify-content: space-between; font-size: 11px;
  color: #d6d3d1; margin-top: 12px; padding-top: 10px;
  border-top: 1px solid #fef3c7;
}

/* ====== 指标 + 贴士双栏 ====== */
.dual-row { display: flex; gap: 20px; }
.indicator-card, .tips-card {
  flex: 1; background: #fff; border: 1px solid #fef3c7;
  border-radius: 16px; padding: 24px; box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

/* 指标网格 */
.ind-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.ind-item {
  display: flex; flex-direction: column; align-items: center;
  padding: 14px 10px; border-radius: 10px; border: 1px solid transparent;
}
.ind-label { font-size: 12px; color: #78716c; margin-bottom: 4px; }
.ind-value { font-size: 20px; font-weight: 800; line-height: 1.3; }
.ind-unit { font-size: 11px; color: #a8a29e; margin-top: 2px; }

/* 每日提醒 */
.tips-list { display: flex; flex-direction: column; gap: 10px; }
.tip-item {
  display: flex; align-items: center; gap: 12px;
  font-size: 13px; color: #44403c; line-height: 1.6;
  padding: 10px 14px; border-radius: 10px;
}
.tip-dot {
  width: 22px; height: 22px; border-radius: 50%;
  background: linear-gradient(135deg, #f97316, #ea580c);
  color: #fff; font-size: 11px; font-weight: 700;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}

/* ====== 在线问诊 ====== */
.consult-card {
  display: flex; align-items: center; gap: 24px;
  padding: 28px 32px; border-radius: 20px;
  background: linear-gradient(135deg, #fff7ed 0%, #fff1e6 30%, #fef9f0 65%, #fffdf7 100%);
  border: 1px solid #fed7aa;
  box-shadow: 0 2px 20px rgba(249,115,22,0.06);
  position: relative; overflow: hidden;
}
/* 背景装饰 */
.consult-card::before {
  content: '';
  position: absolute; right: -40px; top: -40px;
  width: 200px; height: 200px; border-radius: 50%;
  background: radial-gradient(circle, rgba(251,191,36,0.1) 0%, transparent 70%);
  pointer-events: none;
}

/* 头像 */
.consult-avatar {
  position: relative; flex-shrink: 0;
  width: 64px; height: 64px; border-radius: 20px;
  background: linear-gradient(135deg, #fb923c, #ea580c);
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 6px 20px rgba(249,115,22,0.3);
}
.consult-avatar-text {
  color: #fff; font-size: 24px; font-weight: 800; line-height: 1;
}
.consult-avatar-dot {
  position: absolute; bottom: -2px; right: -2px;
  width: 16px; height: 16px; border-radius: 50%;
  background: #22c55e; border: 3px solid #fff;
  box-shadow: 0 0 0 3px rgba(34,197,94,0.2);
}

/* 信息区 */
.consult-body { flex: 1; min-width: 0; z-index: 1; }
.consult-head {
  display: flex; align-items: center; gap: 10px; margin-bottom: 6px;
}
.consult-title {
  font-size: 20px; font-weight: 800; color: #7c2d12; margin: 0;
  letter-spacing: -0.3px;
}
.consult-tag {
  font-size: 11px; font-weight: 700; padding: 3px 10px; border-radius: 20px;
  background: linear-gradient(135deg, #fef3c7, #fde68a);
  color: #92400e; border: 1px solid #fbbf24;
}
.consult-desc {
  font-size: 13px; color: #78350f; margin: 0 0 10px 0; line-height: 1.6;
}
.consult-doctor-row {
  display: flex; align-items: center; gap: 8px; flex-wrap: wrap;
}
.cdr-name {
  font-size: 14px; font-weight: 700; color: #7c2d12;
  background: rgba(249,115,22,0.08); padding: 3px 12px; border-radius: 8px;
}
.cdr-sep {
  width: 3px; height: 3px; border-radius: 50%; background: #fbbf24; flex-shrink: 0;
}
.cdr-meta {
  font-size: 12px; color: #92400e; font-weight: 500;
}

/* 按钮 */
.consult-btn {
  display: flex; align-items: center; gap: 8px;
  padding: 14px 28px; border-radius: 16px; border: none;
  background: linear-gradient(135deg, #f97316, #ea580c);
  color: #fff; font-size: 15px; font-weight: 700;
  cursor: pointer; transition: all 0.25s; flex-shrink: 0; z-index: 1;
  box-shadow: 0 4px 18px rgba(234,88,12,0.35);
  position: relative; overflow: hidden;
}
.consult-btn::after {
  content: '';
  position: absolute; inset: 0;
  background: linear-gradient(135deg, rgba(255,255,255,0.12), transparent);
  pointer-events: none;
}
.consult-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 28px rgba(234,88,12,0.45);
}
.consult-btn:active { transform: translateY(0); }

/* 响应式 */
@media (max-width: 768px) {
  .consult-card { flex-direction: column; text-align: center; padding: 24px; }
  .consult-body { text-align: center; }
  .consult-head { justify-content: center; }
  .consult-doctor-row { justify-content: center; }
}

/* ====== 详情弹窗 ====== */
.detail-wrap { padding: 4px 0; }
.detail-meta { display: flex; align-items: center; gap: 12px; margin-bottom: 18px; font-size: 12px; color: #a8a29e; }
.detail-tag {
  display: inline-block; padding: 2px 8px; border-radius: 5px;
  background: #fef3c7; color: #c2410c; font-size: 12px; font-weight: 600;
}
.detail-content {
  font-size: 15px; color: #44403c; line-height: 2; white-space: pre-wrap;
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
/* ====== 智能医助入口卡片 ====== */
.ai-entry-card {
  position: relative;
  padding: 28px 32px;
  border-radius: 24px;
  cursor: pointer;
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 40%, #e0f2fe 100%);
  overflow: hidden;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 1px 3px rgba(59, 130, 246, 0.08);
}
.ai-entry-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(59, 130, 246, 0.18);
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 40%, #bae6fd 100%);
}
/* 光晕 */
.ai-entry-glow {
  position: absolute;
  top: -60px; right: -40px;
  width: 200px; height: 200px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(96, 165, 250, 0.25) 0%, transparent 70%);
  pointer-events: none;
  transition: all 0.35s;
}
.ai-entry-card:hover .ai-entry-glow {
  width: 260px; height: 260px;
  top: -80px; right: -60px;
}
/* 装饰圆点 */
.ai-entry-orb {
  position: absolute; border-radius: 50%; pointer-events: none;
}
.orb-1 {
  width: 48px; height: 48px;
  bottom: -12px; left: 20%;
  background: rgba(147, 197, 253, 0.35);
}
.orb-2 {
  width: 24px; height: 24px;
  top: 14px; right: 30%;
  background: rgba(96, 165, 250, 0.3);
}
.ai-entry-content {
  position: relative; z-index: 1;
  display: flex; align-items: center; gap: 20px;
}
/* 图标 */
.ai-entry-icon-wrap {
  width: 56px; height: 56px; border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  display: flex; align-items: center; justify-content: center;
  color: #fff; flex-shrink: 0;
  box-shadow: 0 6px 20px rgba(37, 99, 235, 0.3);
  animation: ai-float 3s ease-in-out infinite;
}
@keyframes ai-float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-4px); }
}
/* 文字 */
.ai-entry-text { flex: 1; min-width: 0; }
.ai-entry-title {
  font-size: 18px; font-weight: 700; color: #1e3a5f; margin: 0;
  display: flex; align-items: center; gap: 8px;
}
.ai-entry-badge {
  font-size: 10px; font-weight: 800; padding: 1px 7px;
  border-radius: 6px;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: #fff; letter-spacing: 0.5px;
}
.ai-entry-desc {
  font-size: 13px; color: #64748b; margin: 4px 0 0;
}
/* 按钮 */
.ai-entry-btn {
  display: flex; align-items: center; gap: 6px;
  padding: 10px 22px; border-radius: 30px;
  background: #fff; color: #2563eb;
  font-size: 14px; font-weight: 600;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.15);
  transition: all 0.25s; flex-shrink: 0;
  white-space: nowrap;
}
.ai-entry-card:hover .ai-entry-btn {
  background: #2563eb; color: #fff;
  box-shadow: 0 4px 16px rgba(37, 99, 235, 0.35);
}
</style>
