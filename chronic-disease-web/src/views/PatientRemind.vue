<script setup>
</script>

<template>
  <div class="um-card">
    <div class="card-title-row">
      <span class="card-icon" style="background:linear-gradient(135deg,#c4b5fd,#8b5cf6)">
        <el-icon :size="18"><AlarmClock /></el-icon>
      </span>
      <span class="card-label">用药提醒</span>
    </div>
    <div class="empty-box">
      <el-icon :size="64" color="#94a3b8"><AlarmClock /></el-icon>
      <p class="empty-text">用药提醒功能开发中，敬请期待</p>
    </div>
  </div>
</template>

<style scoped>
.um-card {
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 4px 24px rgba(59,130,246,0.06);
  padding: 24px 28px;
}
.card-title-row {
  display: flex; align-items: center; gap: 10px; margin-bottom: 20px;
}
.card-icon {
  width: 34px; height: 34px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.card-icon .el-icon { color: #fff; }
.card-label { font-size: 16px; font-weight: 700; color: #1e3a5f; }
.empty-box {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  padding: 80px 0; gap: 16px;
}
.empty-text { color: #94a3b8; font-size: 14px; margin: 0; }
</style>
