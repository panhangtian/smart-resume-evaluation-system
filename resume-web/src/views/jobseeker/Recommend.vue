<template>
  <div>
    <h3 class="page-title">智能岗位推荐</h3>

    <!-- 推荐说明 -->
    <div class="section-card">
      <div style="display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:12px">
        <div>
          <div style="font-weight:600;font-size:15px;color:var(--color-text-primary);margin-bottom:4px">✨ AI 匹配推荐</div>
          <p style="color:var(--color-text-muted);font-size:13px;margin:0">
            基于简历向量与岗位矢量的余弦相似度，自动推荐最匹配的岗位
          </p>
        </div>
        <el-button type="primary" @click="loadRecommend" :loading="loading" :icon="'Refresh'">
          刷新推荐
        </el-button>
      </div>
    </div>

    <!-- 推荐卡片列表 -->
    <div v-if="recommends.length" class="rec-list">
      <div v-for="rec in recommends" :key="rec.jobId" class="rec-card">
        <div class="rec-match">
          <div class="match-ring">
            <svg viewBox="0 0 48 48" width="48" height="48">
              <circle cx="24" cy="24" r="20" fill="none" stroke="#F0EEEA" stroke-width="4" />
              <circle cx="24" cy="24" r="20" fill="none" stroke="currentColor" stroke-width="4"
                :stroke-dasharray="`${125.6 * rec.matchScore / 100} 125.6`"
                stroke-linecap="round" transform="rotate(-90, 24, 24)"
                :style="{ color: matchColor(rec.matchScore) }" />
            </svg>
            <div class="match-num">{{ rec.matchScore }}</div>
          </div>
          <div class="match-label" :style="{ color: matchColor(rec.matchScore) }">{{ matchText(rec.matchScore) }}</div>
        </div>
        <div class="rec-info">
          <div class="rec-title">{{ rec.jobTitle }}</div>
          <div class="rec-meta">
            <span>🏢 {{ rec.company }}</span>
            <span>📍 {{ rec.location }}</span>
            <span>💰 {{ rec.salary }}</span>
          </div>
        </div>
        <div class="rec-action">
          <el-button size="small" type="success" @click="handleApply(rec)">
            投递简历
          </el-button>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state" style="margin-top:24px">
      <div class="empty-icon">🎯</div>
      <p style="margin-bottom:12px">
        {{ noDataReason }}
      </p>
      <div style="display:flex;gap:12px;justify-content:center">
        <el-button type="primary" @click="loadRecommend">刷新试试</el-button>
        <el-button @click="router.push('/resumes')">上传简历</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { recommendJobs } from '@/api/recommend'
import { getMyResumes } from '@/api/resume'
import { applyJob } from '@/api/application'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const recommends = ref([])
const loading = ref(false)

const noDataReason = computed(() => {
  return '暂无推荐结果。请确保已上传并完成简历解析，且已有 HR 发布岗位。'
})

loadRecommend()
async function loadRecommend() {
  loading.value = true
  try {
    const res = await recommendJobs(20)
    recommends.value = res.data || []
    if (recommends.value.length === 0) {
      ElMessage.info('暂无推荐结果，请确认已上传简历且岗位数据存在')
    }
  } catch (e) {
    ElMessage.warning('获取推荐失败，请重试')
  } finally { loading.value = false }
}

async function handleApply(row) {
  const res = await getMyResumes()
  const list = res.data || []
  const valid = list.find(i => i.status === 2)
  if (!valid) { ElMessage.warning('请先上传且完成解析的简历'); return }
  try {
    await applyJob(valid.id, row.jobId)
    ElMessage.success('🎉 投递成功')
    router.push('/applications')
  } catch (e) {
    ElMessage.error('投递失败')
  }
}

function matchColor(score) {
  if (score >= 80) return '#5D9B71'
  if (score >= 60) return '#D4943F'
  return '#DF5A3F'
}

function matchText(score) {
  if (score >= 85) return '非常匹配'
  if (score >= 70) return '比较匹配'
  if (score >= 60) return '一般匹配'
  if (score >= 40) return '略微匹配'
  return '较低匹配'
}
</script>

<style scoped>
.rec-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
}
.rec-card {
  display: flex;
  align-items: center;
  gap: 20px;
  background: var(--color-bg-card);
  border: 1px solid #EDEDF0;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.2s ease;
}
.rec-card:hover {
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(43,43,49,0.06);
}
.rec-match {
  text-align: center;
  min-width: 60px;
}
.match-ring {
  position: relative;
  width: 48px;
  height: 48px;
  margin: 0 auto;
}
.match-num {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  color: var(--color-text-primary);
}
.match-label {
  font-size: 11px;
  font-weight: 600;
  margin-top: 4px;
}
.rec-info {
  flex: 1;
}
.rec-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: 6px;
}
.rec-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 13px;
  color: var(--color-text-secondary);
}
.rec-action {
  flex-shrink: 0;
}
</style>
