<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 健康知识轮播
const carouselItems = [
  {
    title: '高血压患者夏季血压管理',
    desc: '夏天气温升高，血管扩张，血压可能出现"假性正常"。切勿自行停药，应遵医嘱调整用药，每日早晚各测一次血压并记录。',
    tag: '疾病管理',
    bg: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  },
  {
    title: '糖尿病饮食"手掌法则"',
    desc: '每餐主食一拳头、蛋白质一手掌、蔬菜一捧、脂肪一大拇指。科学控糖从量化饮食开始。',
    tag: '饮食健康',
    bg: 'linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%)',
  },
  {
    title: '运动是最好的降压药',
    desc: '每周≥5天、每次30分钟的有氧运动（快走、游泳、太极），可使收缩压下降4~9mmHg。',
    tag: '运动康复',
    bg: 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
  },
  {
    title: '脑卒中识别：牢记"120"口诀',
    desc: '"1"看脸不对称、"2"查双臂无力、"0"听言语不清——出现任一症状立即拨打120。',
    tag: '急救科普',
    bg: 'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)',
  },
]

// 功能入口
const featureCards = [
  { title: '健康档案', desc: '管理个人健康信息，记录病史与过敏史', icon: 'Folder', color: '#3b82f6', bg: '#eff6ff', path: '/patient/archive' },
  { title: '数据监测', desc: '记录血压/血糖/血脂，生成趋势图表', icon: 'DataAnalysis', color: '#10b981', bg: '#ecfdf5', path: '/patient/data' },
  { title: '用药提醒', desc: '设置用药计划，准时推送防漏服', icon: 'AlarmClock', color: '#f59e0b', bg: '#fffbeb', path: '/patient/remind' },
  { title: '健康资讯', desc: '权威慢病科普，科学管理每一天', icon: 'Document', color: '#8b5cf6', bg: '#f5f3ff', path: '/patient/article' },
]

// 慢病指标参考
const indicatorRefs = [
  { label: '理想血压', value: '< 120 / 80', unit: 'mmHg', status: 'normal', desc: '收缩压 < 120 且 舒张压 < 80' },
  { label: '正常高值', value: '120~139 / 80~89', unit: 'mmHg', status: 'warn', desc: '需关注，改善生活方式' },
  { label: '高血压', value: '≥ 140 / 90', unit: 'mmHg', status: 'danger', desc: '请及时就医，规范用药' },
  { label: '空腹血糖', value: '3.9 ~ 6.1', unit: 'mmol/L', status: 'normal', desc: '空腹 ≥ 8小时' },
  { label: '餐后2h血糖', value: '< 7.8', unit: 'mmol/L', status: 'normal', desc: '从第一口饭开始计时' },
  { label: '糖尿病诊断', value: '≥ 11.1', unit: 'mmol/L', status: 'danger', desc: '任意时间血糖 ≥ 11.1' },
]

// 用药知识
const medKnowledge = [
  { title: '降压药', desc: '不可随意停药，即使血压正常也需维持用药。漏服后勿加倍补服。', icon: 'FirstAidKit', color: '#3b82f6' },
  { title: '降糖药', desc: '磺脲类餐前30min服用；二甲双胍餐中或餐后服，减少胃肠反应。', icon: 'Timer', color: '#10b981' },
  { title: '降脂药', desc: '他汀类建议睡前服用，肝功能异常者需定期监测转氨酶。', icon: 'Moon', color: '#8b5cf6' },
]

// 健康贴士
const tips = [
  { icon: 'Dish', text: '低盐饮食：每日食盐 < 6g（约一啤酒瓶盖），少吃腌制品' },
  { icon: 'Sunny', text: '每天30分钟户外活动，晒太阳有助于维生素D合成' },
  { icon: 'Clock', text: '早晨起床后1小时内测量血压，测前静坐5分钟' },
  { icon: 'WarningFilled', text: '出现胸闷、头痛剧烈、视物模糊等症状请立即就医' },
]

function goPage(path) { router.push(path) }
</script>

<template>
  <div class="home-root">
    <!-- ====== 轮播区 ====== -->
    <div class="carousel-section">
      <el-carousel :interval="5000" arrow="hover" height="200px" indicator-position="none">
        <el-carousel-item v-for="(item, idx) in carouselItems" :key="idx">
          <div class="carousel-card" :style="{ background: item.bg }">
            <div class="carousel-text">
              <span class="carousel-tag">{{ item.tag }}</span>
              <h3 class="carousel-title">{{ item.title }}</h3>
              <p class="carousel-desc">{{ item.desc }}</p>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- ====== 功能入口 ====== -->
    <div class="section-card">
      <div class="card-hd">
        <span class="hd-accent" style="background:#3b82f6"></span>
        <span class="hd-title">健康服务</span>
      </div>
      <div class="feature-grid">
        <div v-for="card in featureCards" :key="card.path" class="feature-card" @click="goPage(card.path)">
          <div class="feature-icon" :style="{ background: card.bg, color: card.color }">
            <el-icon :size="22"><component :is="card.icon" /></el-icon>
          </div>
          <div class="feature-info">
            <span class="feature-title">{{ card.title }}</span>
            <span class="feature-desc">{{ card.desc }}</span>
          </div>
          <el-icon class="feature-arrow" :size="14"><ArrowRight /></el-icon>
        </div>
      </div>
    </div>

    <!-- ====== 慢病指标参考 ====== -->
    <div class="section-card">
      <div class="card-hd">
        <span class="hd-accent" style="background:#10b981"></span>
        <span class="hd-title">慢病指标参考值</span>
      </div>
      <div class="ref-source">参考标准：《中国高血压防治指南》《中国2型糖尿病防治指南》</div>

      <div class="ref-split">
        <!-- 血压 -->
        <div class="ref-group">
          <div class="ref-group-title">血压指标</div>
          <div class="ref-row" v-for="row in indicatorRefs.slice(0, 3)" :key="row.label">
            <div class="ref-left">
              <span class="ref-name">{{ row.label }}</span>
              <span class="ref-desc">{{ row.desc }}</span>
            </div>
            <div class="ref-right">
              <span class="ref-value">{{ row.value }}</span>
              <span class="ref-unit">{{ row.unit }}</span>
            </div>
          </div>
        </div>
        <!-- 血糖 -->
        <div class="ref-group">
          <div class="ref-group-title">血糖指标</div>
          <div class="ref-row" v-for="row in indicatorRefs.slice(3)" :key="row.label">
            <div class="ref-left">
              <span class="ref-name">{{ row.label }}</span>
              <span class="ref-desc">{{ row.desc }}</span>
            </div>
            <div class="ref-right">
              <span class="ref-value">{{ row.value }}</span>
              <span class="ref-unit">{{ row.unit }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ====== 用药知识 + 贴士（双栏） ====== -->
    <div class="dual-section">
      <!-- 用药知识 -->
      <div class="section-card flex-1">
        <div class="card-hd">
          <span class="hd-accent" style="background:#f59e0b"></span>
          <span class="hd-title">用药小课堂</span>
        </div>
        <div class="med-list">
          <div v-for="(m, idx) in medKnowledge" :key="idx" class="med-item">
            <span class="med-icon" :style="{ background: m.color + '15', color: m.color }">
              <el-icon :size="18"><component :is="m.icon" /></el-icon>
            </span>
            <div class="med-body">
              <span class="med-title">{{ m.title }}</span>
              <span class="med-desc">{{ m.desc }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 健康贴士 -->
      <div class="section-card flex-1">
        <div class="card-hd">
          <span class="hd-accent" style="background:#8b5cf6"></span>
          <span class="hd-title">每日提醒</span>
        </div>
        <div class="tips-list">
          <div v-for="(tip, idx) in tips" :key="idx" class="tip-item">
            <el-icon :size="17" color="#3b82f6"><component :is="tip.icon" /></el-icon>
            <span>{{ tip.text }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-root {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px 20px 48px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* ====== 通用分区卡片 ====== */
.section-card {
  background: #dbeafe;
  border: 1px solid #b4c8e0;
  border-radius: 16px;
  padding: 24px 28px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.card-hd {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 18px;
}
.hd-accent {
  width: 6px; height: 6px; border-radius: 50%; flex-shrink: 0;
}
.hd-title {
  font-size: 16px; font-weight: 600; color: #1e293b; letter-spacing: 0.5px;
}
.hd-note {
  font-size: 11px; color: #94a3b8; margin-left: auto;
}

/* ====== 轮播 ====== */
.carousel-section {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  border: 1px solid #b4c8e0;
}
.carousel-section :deep(.el-carousel__container) { border-radius: 16px; }
.carousel-card {
  height: 100%; display: flex; align-items: flex-end;
  padding: 28px 36px; box-sizing: border-box;
}
.carousel-text { display: flex; flex-direction: column; gap: 6px; }
.carousel-tag {
  display: inline-block; background: rgba(255,255,255,0.22);
  color: #fff; font-size: 11px; padding: 3px 12px;
  border-radius: 20px; width: fit-content; font-weight: 500;
  letter-spacing: 0.5px;
}
.carousel-title { margin: 0; font-size: 21px; font-weight: 700; color: #fff; line-height: 1.3; }
.carousel-desc {
  margin: 0; font-size: 13px; color: rgba(255,255,255,0.85);
  line-height: 1.65; max-width: 540px;
}

/* ====== 功能卡片 ====== */
.feature-grid {
  display: grid; grid-template-columns: 1fr 1fr; gap: 14px;
}
.feature-card {
  display: flex; align-items: center; gap: 16px;
  padding: 18px 20px; border-radius: 14px; cursor: pointer;
  transition: all 0.2s ease;
  background: #dbeafe;
  border: 1px solid #b4c8e0;
}
.feature-card:hover {
  border-color: #89aac8;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
  transform: translateY(-2px);
  background: #fff;
}
.feature-icon {
  width: 52px; height: 52px; border-radius: 14px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
  box-shadow: 0 2px 6px rgba(0,0,0,0.06);
}
.feature-info { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 4px; }
.feature-title { font-size: 15px; font-weight: 600; color: #1e293b; }
.feature-desc { font-size: 12px; color: #94a3b8; line-height: 1.5; }
.feature-arrow { flex-shrink: 0; color: #cbd5e1; transition: all 0.2s; }
.feature-card:hover .feature-arrow { color: #64748b; transform: translateX(4px); }

/* ====== 指标参考 ====== */
.ref-source {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 18px;
  padding-bottom: 14px;
  border-bottom: 1px solid #b4c8e0;
}

.ref-split {
  display: flex;
  gap: 32px;
}
.ref-group {
  flex: 1;
  min-width: 0;
}
.ref-group-title {
  font-size: 13px;
  font-weight: 700;
  color: #64748b;
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid #b4c8e0;
}

.ref-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 0;
  border-bottom: 1px solid #cbd5e1;
}
.ref-row:last-child { border-bottom: none; }

.ref-left {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.ref-name {
  font-size: 14px;
  font-weight: 600;
  color: #334155;
}
.ref-desc {
  font-size: 12px;
  color: #94a3b8;
  line-height: 1.5;
}

.ref-right {
  display: flex;
  align-items: baseline;
  gap: 4px;
  flex-shrink: 0;
  text-align: right;
}
.ref-value {
  font-size: 17px;
  font-weight: 700;
  color: #1e293b;
  white-space: nowrap;
}
.ref-unit {
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
  color: #64748b;
}

/* ====== 双栏布局 ====== */
.dual-section { display: flex; gap: 24px; }
.flex-1 { flex: 1; min-width: 0; }

/* ====== 用药知识 ====== */
.med-list { display: flex; flex-direction: column; gap: 16px; }
.med-item { 
  display: flex; gap: 14px; 
  padding: 14px 16px;
  border-radius: 12px;
  background: #dbeafe;
  border: 1px solid #b4c8e0;
  transition: background 0.15s;
}
.med-item:hover { background: #fff; }
.med-icon {
  width: 42px; height: 42px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.med-body { display: flex; flex-direction: column; gap: 4px; }
.med-title { font-size: 14px; font-weight: 600; color: #334155; }
.med-desc { font-size: 12px; color: #64748b; line-height: 1.65; }

/* ====== 每日提醒 ====== */
.tips-list { display: flex; flex-direction: column; gap: 10px; }
.tip-item {
  display: flex; align-items: flex-start; gap: 10px;
  font-size: 13px; color: #475569; line-height: 1.7;
  padding: 12px 14px; border-radius: 10px;
  background: #dbeafe;
  border: 1px solid #b4c8e0;
  transition: background 0.15s;
}
.tip-item:hover { background: #fff; }
.tip-item .el-icon { flex-shrink: 0; margin-top: 2px; }

/* ====== 响应式 ====== */
@media (max-width: 700px) {
  .feature-grid { grid-template-columns: 1fr; }
  .dual-section { flex-direction: column; }
  .ref-split { flex-direction: column; gap: 20px; }
}
</style>
