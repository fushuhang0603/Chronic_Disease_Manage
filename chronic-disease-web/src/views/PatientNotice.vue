<template>
  <div class="patient-root">
    <div class="page-header">
      <h2 class="header-title">公告中心</h2>
      <p class="header-sub">平台公告与重要通知</p>
    </div>

    <div v-loading="loading" class="notice-area">
      <div v-if="records.length > 0" class="notice-card-list">
        <div v-for="item in records" :key="item.id" class="notice-card" @click="viewDetail(item)">
          <span class="notice-badge">公告</span>
          <div class="notice-body">
            <h3 class="notice-title">{{ item.title }}</h3>
            <p class="notice-desc">{{ getSummary(item.content) }}</p>
            <div class="notice-meta">
              <span>发布人ID：{{ item.publisherId || '-' }}</span>
              <span>{{ fmtTime(item.publishTime) }}</span>
            </div>
          </div>
        </div>
      </div>
      <div v-else-if="!loading" class="empty-state">
        <p>暂无公告</p>
      </div>
    </div>

    <div class="pagination-wrap" v-if="total > 0">
      <el-pagination background layout="total, prev, pager, next" :total="total" :page-size="pageSize" :current-page="pageNum" @current-change="handlePageChange" />
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="showDetail" :title="detailItem?.title" width="700px" :close-on-click-modal="false">
      <div class="detail-wrap" v-if="detailItem">
        <div class="detail-meta">
          <span class="detail-tag">公告</span>
          <span>发布人ID：{{ detailItem.publisherId || '-' }}</span>
          <span>{{ fmtTime(detailItem.publishTime) }}</span>
        </div>
        <div class="detail-body">{{ detailItem.content || '暂无内容' }}</div>
      </div>
      <template #footer>
        <el-button @click="showDetail = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getNoticePage } from '../api/user'

const loading = ref(false)
const records = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const showDetail = ref(false)
const detailItem = ref(null)

function fmtTime(t) {
  return t ? t.replace('T', ' ').substring(0, 16) : '-'
}

function getSummary(content) {
  if (!content) return ''
  return content.replace(/\s+/g, ' ').substring(0, 80) + (content.length > 80 ? '...' : '')
}

function viewDetail(item) {
  detailItem.value = item
  showDetail.value = true
}

async function fetchRecords() {
  loading.value = true
  try {
    // 患者端只看已发布公告
    const res = await getNoticePage({ pageNum: pageNum.value, pageSize: pageSize.value, status: 1 })
    records.value = res.records || []
    total.value = res.total || 0
  } finally { loading.value = false }
}

function handlePageChange(p) { pageNum.value = p; fetchRecords() }

onMounted(() => { fetchRecords() })
</script>

<style scoped>
.patient-root { max-width: 1000px; margin: 0 auto; width: 100%; padding: 24px 24px 40px; box-sizing: border-box; }

.page-header { margin-bottom: 20px; }
.header-title { font-size: 20px; font-weight: 700; color: #7c2d12; margin: 0; }
.header-sub { font-size: 13px; color: #a8a29e; margin: 2px 0 0 0; }

.notice-area {
  background: #fff; border: 1px solid #fef3c7; border-radius: 16px;
  min-height: 200px; padding: 12px; box-shadow: 0 1px 4px rgba(249,115,22,0.04);
}
.notice-card-list { display: flex; flex-direction: column; gap: 8px; }
.notice-card {
  display: flex; align-items: flex-start; gap: 12px;
  padding: 16px 18px; border-radius: 12px; cursor: pointer;
  border: 1px solid #fffbeb; background: #fffbeb;
  transition: all 0.2s;
}
.notice-card:hover { border-color: #fbbf24; background: #fff7ed; }
.notice-badge {
  flex-shrink: 0; font-size: 11px; font-weight: 700; margin-top: 2px;
  padding: 3px 10px; border-radius: 6px;
  background: linear-gradient(135deg, #f97316, #ea580c); color: #fff;
}
.notice-body { flex: 1; min-width: 0; }
.notice-title { font-size: 15px; font-weight: 700; color: #431407; margin: 0 0 6px 0; }
.notice-desc {
  font-size: 13px; color: #a8a29e; line-height: 1.6; margin: 0 0 8px 0;
  overflow: hidden; text-overflow: ellipsis;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
}
.notice-meta { display: flex; gap: 16px; font-size: 12px; color: #d6d3d1; }

.empty-state { display: flex; align-items: center; justify-content: center; padding: 80px 20px; color: #a8a29e; }
.empty-state p { font-size: 14px; margin: 0; }

.pagination-wrap { display: flex; justify-content: center; margin-top: 20px; padding-top: 16px; border-top: 1px solid #fef3c7; }

:deep(.el-dialog) { border-radius: 16px; }
:deep(.el-dialog__header) { padding: 24px 28px 0; border-bottom: 1px solid #fef3c7; }
:deep(.el-dialog__title) { font-size: 17px; font-weight: 700; color: #7c2d12; }
:deep(.el-dialog__body) { padding: 16px 28px; }
:deep(.el-dialog__footer) { padding: 0 28px 24px; }

.detail-wrap { padding: 4px 0; }
.detail-meta { display: flex; align-items: center; gap: 12px; margin-bottom: 18px; font-size: 12px; color: #a8a29e; }
.detail-tag {
  display: inline-block; padding: 2px 8px; border-radius: 5px;
  background: #fef3c7; color: #c2410c; font-size: 12px; font-weight: 600;
}
.detail-body {
  font-size: 14px; color: #431407; line-height: 1.9; white-space: pre-wrap;
  background: #fffbeb; border-radius: 12px; padding: 20px 24px;
  border: 1px solid #fef3c7; min-height: 120px; max-height: 460px; overflow-y: auto;
}

::deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background: linear-gradient(135deg, #f97316, #ea580c);
  border-radius: 8px;
}
</style>
