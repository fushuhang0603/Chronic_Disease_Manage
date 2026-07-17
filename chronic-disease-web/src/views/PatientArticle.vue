<template>
  <div class="article-page">
    <!-- 分类 Tab -->
    <div class="filter-bar">
      <div class="bar-tags">
        <button :class="['tag-chip', { active: activeCategory === '' && !onlyFavorited }]" @click="switchTab('')">全部</button>
        <button v-for="c in categories" :key="c"
          :class="['tag-chip', { active: activeCategory === c && !onlyFavorited }]"
          @click="switchTab(c)">{{ c }}</button>
        <button :class="['tag-chip', { active: onlyFavorited }]" @click="switchTab('favorites')">我的收藏</button>
      </div>
    </div>

    <!-- 文章列表 -->
    <div v-loading="loading" class="article-list">
      <template v-if="records.length > 0">
        <div v-for="item in records" :key="item.id" class="article-card" @click="openDetail(item)">
          <div class="card-tag" :style="{ background: catBgs[catIdxMap[item.category] ?? 0], color: catColors[catIdxMap[item.category] ?? 0] }">{{ item.category }}</div>
          <h3 class="card-title">{{ item.title }}</h3>
          <p class="card-desc">{{ getSummary(item.content) }}</p>
          <div class="card-foot">
            <span>{{ fmtTime(item.publishingTime) }}</span>
            <span>{{ item.viewCount || 0 }} 阅读</span>
            <span class="card-fav" :class="{ on: item.isFavorited }" @click.stop="handleFavorite(item)">
              {{ item.isFavorited ? '已收藏' : '收藏' }}
            </span>
          </div>
        </div>
      </template>
      <div v-else-if="!loading" class="empty-box">暂无资讯</div>
    </div>

    <div v-if="total > pageSize" class="page-wrap">
      <el-pagination background layout="total, prev, pager, next" :total="total" :page-size="pageSize" :current-page="pageNum" @current-change="handlePageChange" />
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="showDetail" :title="detailItem?.title" width="700px" :close-on-click-modal="false" destroy-on-close>
      <div class="detail-wrap" v-if="detailItem">
        <div class="detail-meta">
          <span class="detail-tag" :style="{ background: catBgs[catIdxMap[detailItem.category] ?? 0], color: catColors[catIdxMap[detailItem.category] ?? 0] }">{{ detailItem.category }}</span>
          <span>{{ detailItem.viewCount || 0 }} 阅读</span>
          <span>{{ fmtTime(detailItem.publishingTime) }}</span>
        </div>
        <div class="detail-content">{{ detailItem.content }}</div>
      </div>
      <template #footer>
        <el-button @click="showDetail = false">关闭</el-button>
        <el-button class="fav-dialog-btn" v-if="detailItem" @click="handleFavorite(detailItem); detailItem.isFavorited = !detailItem.isFavorited">
          {{ detailItem.isFavorited ? '取消收藏' : '加入收藏' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getArticlePage, getFavoritesPage, updateFavoriteStatus } from '../api/user'
import { ElMessage } from 'element-plus'

const categories = ['饮食', '运动', '用药', '慢病常识', '并发症预防']
const catIdxMap = { '饮食': 0, '运动': 1, '用药': 2, '慢病常识': 3, '并发症预防': 4 }
const catColors = ['#d97706', '#d97706', '#d97706', '#d97706', '#d97706']
const catBgs = ['#fffbeb', '#fffbeb', '#fffbeb', '#fffbeb', '#fffbeb']

const loading = ref(false)
const records = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(7)
const activeCategory = ref('')
const onlyFavorited = ref(false)
const showDetail = ref(false)
const detailItem = ref(null)

function fmtTime(t) { return t ? t.replace('T', ' ').substring(0, 10) : '-' }
function getSummary(c) { if (!c) return ''; const s = c.replace(/\s+/g, ' '); return s.length > 60 ? s.substring(0, 60) + '...' : s }

async function fetchRecords() {
  loading.value = true
  try {
    const res = onlyFavorited.value
      ? await getFavoritesPage({ pageNum: pageNum.value, pageSize: pageSize.value })
      : await getArticlePage({ pageNum: pageNum.value, pageSize: pageSize.value, category: activeCategory.value || undefined })
    records.value = res.records || []
    total.value = res.total || 0
  } finally { loading.value = false }
}
function switchTab(cat) {
  if (cat === 'favorites') { onlyFavorited.value = true; activeCategory.value = '' }
  else { onlyFavorited.value = false; activeCategory.value = cat }
  pageNum.value = 1; fetchRecords()
}
async function openDetail(item) {
  detailItem.value = item; showDetail.value = true
  try { await fetch('/api/article/readHistory', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ articleId: item.id, readDuration: 5 }) }) } catch {}
}
async function handleFavorite(item) {
  const status = item.isFavorited ? 1 : 0
  try { await updateFavoriteStatus(item.id, status); item.isFavorited = !item.isFavorited; ElMessage.success(item.isFavorited ? '已收藏' : '已取消收藏') }
  catch { ElMessage.error('操作失败') }
}
function handlePageChange(p) { pageNum.value = p; fetchRecords() }
onMounted(() => fetchRecords())
</script>

<style scoped>
.article-page { max-width: 1200px; margin: 0 auto; width: 100%; padding: 12px 20px 28px; }

/* ====== 分类筛选 ====== */
.filter-bar { display: flex; align-items: center; margin-bottom: 16px; }
.bar-tags { display: flex; gap: 4px; background: #f1f5f9; border-radius: 10px; padding: 4px; }
.tag-chip {
  padding: 7px 18px; border-radius: 8px; border: none;
  background: transparent; color: #64748b; font-size: 13px; cursor: pointer;
  transition: all 0.2s; white-space: nowrap; font-weight: 500;
}
.tag-chip:hover { color: #334155; }
.tag-chip.active { background: #fff; color: #1e293b; font-weight: 600; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }

/* ====== 文章列表 ====== */
.article-list { min-height: 200px; display: flex; flex-direction: column; gap: 8px; }
.article-card {
  background: linear-gradient(135deg, #fafbfc, #f5f6f8);
  border: 1px solid #e9ecef; border-radius: 10px; cursor: pointer;
  padding: 12px 16px; transition: all 0.15s;
}
.article-card:hover { border-color: #cbd5e1; background: linear-gradient(135deg, #f5f6f8, #f0f1f3); box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.card-tag { display: inline-block; font-size: 10px; font-weight: 600; padding: 2px 8px; border-radius: 3px; margin-bottom: 6px; background: #e2e8f0; color: #64748b; }
.card-title { font-size: 14px; font-weight: 700; color: #1e293b; margin: 0 0 5px 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.card-desc { font-size: 12px; color: #94a3b8; line-height: 1.6; margin: 0 0 8px 0; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.card-foot { display: flex; align-items: center; gap: 14px; font-size: 11px; color: #cbd5e1; }
.card-fav { color: #64748b; font-weight: 500; cursor: pointer; margin-left: auto; transition: color 0.15s; }
.card-fav:hover { color: #334155; }
.card-fav.on { color: #d97706; }

/* ====== 详情弹窗 ====== */
.detail-wrap { padding: 4px 0; }
.detail-meta { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; font-size: 12px; color: #94a3b8; }
.detail-tag { font-size: 11px; font-weight: 600; padding: 2px 8px; border-radius: 3px; }
.detail-content { font-size: 14px; color: #475569; line-height: 1.9; white-space: pre-wrap; max-height: 460px; overflow-y: auto; }
.fav-dialog-btn {
  background: linear-gradient(135deg, #eff6ff, #dbeafe); color: #2563eb; border: 1px solid #93c5fd;
}
.fav-dialog-btn:hover { background: linear-gradient(135deg, #dbeafe, #bfdbfe); color: #1d4ed8; }

.empty-box { display: flex; justify-content: center; padding: 48px 0; color: #94a3b8; font-size: 14px; }
.page-wrap { display: flex; justify-content: center; padding: 10px 0; }

:deep(.el-dialog) { border-radius: 14px; resize: both; overflow: auto; min-width: 400px; min-height: 300px; background: linear-gradient(180deg, #fff 0%, #f0f6ff 100%); }
:deep(.el-dialog__header) { padding: 22px 24px 0; }
:deep(.el-dialog__title) { font-size: 17px; font-weight: 700; max-width: 580px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; display: block; }
:deep(.el-dialog__body) { padding: 14px 24px; }
:deep(.el-dialog__footer) { padding: 0 24px 22px; }
</style>
