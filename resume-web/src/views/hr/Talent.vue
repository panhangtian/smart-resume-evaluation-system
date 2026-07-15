<template>
  <div>
    <h3 class="page-title">人才推荐</h3>

    <div v-if="!jobs.length" class="empty-state">
      <div class="empty-icon">📋</div>
      <p>暂无已发布的岗位，请先发布岗位</p>
      <el-button type="primary" @click="router.push('/hr/jobs')">去发布岗位</el-button>
    </div>

    <div v-for="job in jobs" :key="job.id" class="job-section">
      <div class="job-section-header">
        <div>
          <h4 class="job-section-title">{{ job.title }}</h4>
          <span class="job-section-company">@ {{ job.company }}</span>
        </div>
        <el-button size="small" type="primary" :loading="loadingMap[job.id]" @click="loadCandidates(job.id)">
          {{ loadingMap[job.id] ? '匹配中…' : '匹配候选' }}
        </el-button>
      </div>

      <div v-if="errorMap[job.id]" class="talent-error">{{ errorMap[job.id] }}</div>

      <!-- 候选人卡片列表 -->
      <div v-if="candidateMap[job.id] && candidateMap[job.id].length" class="candidate-grid">
        <div v-for="(cand, idx) in candidateMap[job.id]" :key="cand.jobId" class="candidate-card" @click="viewCandidate(cand)">
          <div class="cand-rank">{{ idx + 1 }}</div>
          <div class="cand-body">
            <div class="cand-name">{{ cand.jobTitle }}</div>
            <div class="cand-tags">
              <el-tag v-for="s in parsedSkills(cand.company).slice(0, 4)" :key="s" size="small" class="skill-tag">{{ s }}</el-tag>
              <span v-if="parsedSkills(cand.company).length > 4" class="skill-more">+{{ parsedSkills(cand.company).length - 4 }}</span>
            </div>
            <div class="cand-meta">
              <span v-if="cand.experienceYears != null">🧑‍💼 {{ cand.experienceYears }}年经验</span>
              <span v-if="cand.location">@ {{ cand.location }}</span>
              <span>📞 {{ contactText(cand) || '暂无联系方式' }}</span>
            </div>
          </div>
          <div class="cand-score" :style="{ color: matchColor(cand.matchScore) }">
            <div class="score-ring">
              <svg viewBox="0 0 44 44" width="44" height="44">
                <circle cx="22" cy="22" r="18" fill="none" stroke="#F0EEEA" stroke-width="4" />
                <circle cx="22" cy="22" r="18" fill="none" :stroke="matchColor(cand.matchScore)" stroke-width="4"
                  :stroke-dasharray="`${113 * cand.matchScore / 100} 113`"
                  stroke-linecap="round" transform="rotate(-90,22,22)" />
              </svg>
              <span class="score-num">{{ cand.matchScore }}</span>
            </div>
            <div class="score-label" :style="{ color: matchColor(cand.matchScore) }">{{ scoreLabel(cand.matchScore) }}</div>
          </div>
        </div>
      </div>

      <div v-else-if="loadedMap[job.id]" class="talent-empty">
        <p>暂无匹配候选人</p>
        <p class="talent-hint">可等待求职者上传并解析简历，或调整岗位的技能要求标签</p>
      </div>
    </div>

    <!-- 候选人详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="detail?.jobTitle || '候选人详情'" width="500px">
      <template v-if="detail">
        <div class="detail-header">
          <div class="detail-avatar">{{ detail.jobTitle?.charAt(0) || '?' }}</div>
          <div>
            <div class="detail-name">{{ detail.jobTitle }}</div>
            <div class="detail-username" v-if="detail.location">@ {{ detail.location }}</div>
          </div>
          <div class="detail-big-score" :style="{ color: matchColor(detail.matchScore) }">
            <span class="big-num">{{ detail.matchScore }}</span>
            <span class="big-unit">分</span>
          </div>
        </div>
        <el-divider />
        <div class="detail-section">
          <div class="detail-label">🎯 匹配度</div>
          <el-progress :percentage="detail.matchScore" :color="matchColor(detail.matchScore)" :stroke-width="10" />
        </div>
        <div class="detail-section">
          <div class="detail-label">🧑‍💼 工作年限</div>
          <div>{{ detail.experienceYears != null ? detail.experienceYears + ' 年' : '未填写' }}</div>
        </div>
        <div class="detail-section">
          <div class="detail-label">📞 联系方式</div>
          <div>{{ contactText(detail) || '暂无联系方式' }}</div>
        </div>
        <div class="detail-section">
          <div class="detail-label">🔧 技能标签</div>
          <div class="detail-tags">
            <el-tag v-for="s in parsedSkills(detail.company)" :key="s" size="small" class="skill-tag">{{ s }}</el-tag>
            <span v-if="!detail.company" style="color:var(--color-text-muted)">暂无技能信息</span>
          </div>
        </div>
        <div class="detail-section">
          <div class="detail-label">📝 能力简介</div>
          <div class="detail-ability">{{ detail.abilitySummary || '暂无能力简介' }}</div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyJobs } from '@/api/job'
import { recommendCandidates } from '@/api/recommend'
import { useRouter } from 'vue-router'

const router = useRouter()
const jobs = ref([])
const candidateMap = ref({})
const loadingMap = ref({})
const errorMap = ref({})
const loadedMap = ref({})

const detailVisible = ref(false)
const detail = ref(null)

onMounted(load)
async function load() {
  const res = await getMyJobs()
  jobs.value = res.data || []
}

async function loadCandidates(jobId) {
  loadingMap.value[jobId] = true
  errorMap.value[jobId] = ''
  try {
    const res = await recommendCandidates(jobId, 10)
    candidateMap.value[jobId] = res.data || []
    loadedMap.value[jobId] = true
  } catch {
    errorMap.value[jobId] = '获取候选人失败，请稍后重试'
    candidateMap.value[jobId] = []
    loadedMap.value[jobId] = true
  } finally {
    loadingMap.value[jobId] = false
  }
}

function parsedSkills(skills) {
  if (!skills) return []
  return skills.split(',').map(s => s.trim()).filter(Boolean)
}

// 联系方式：优先展示邮箱/电话；后端推荐接口当前未返回这两项时显示空
function contactText(cand) {
  if (!cand) return ''
  const parts = []
  if (cand.email) parts.push('邮箱：' + cand.email)
  if (cand.phone) parts.push('电话：' + cand.phone)
  return parts.join('　')
}

function viewCandidate(cand) {
  detail.value = cand
  detailVisible.value = true
}

function matchColor(score) {
  if (score >= 80) return '#5D9B71'
  if (score >= 60) return '#D4943F'
  return '#DF5A3F'
}

function scoreLabel(score) {
  if (score >= 85) return '非常匹配'
  if (score >= 70) return '比较匹配'
  if (score >= 60) return '一般匹配'
  if (score >= 40) return '略微匹配'
  return '较低匹配'
}
</script>

<style scoped>
.job-section {
  background: var(--color-bg-card);
  border: 1px solid #EDEDF0;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
}
.job-section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 12px;
}
.job-section-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--color-text-primary);
}
.job-section-company {
  font-size: 13px;
  font-weight: 400;
  color: #909399;
}

.talent-error {
  background: #FEF6F4;
  border: 1px solid rgba(223,90,63,0.2);
  border-radius: 8px;
  padding: 10px 14px;
  margin-top: 12px;
  font-size: 13px;
  color: #DF5A3F;
}
.talent-empty {
  text-align: center;
  padding: 24px;
  color: var(--color-text-muted);
  font-size: 13px;
}
.talent-hint {
  font-size: 12px;
  margin-top: 4px;
  opacity: 0.7;
}

/* 候选人卡片网格 */
.candidate-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 12px;
  margin-top: 16px;
}
.candidate-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  border: 1px solid #EDEDF0;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: #FAFAF8;
}
.candidate-card:hover {
  border-color: var(--el-color-primary-light-5);
  box-shadow: 0 4px 12px rgba(74,144,179,0.10);
  transform: translateY(-2px);
}
.cand-rank {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--el-color-primary-light-8);
  color: var(--el-color-primary);
  font-size: 13px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.cand-body {
  flex: 1;
  min-width: 0;
}
.cand-name {
  font-size: 14px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: 4px;
}
.cand-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 4px;
}
.skill-tag {
  background: var(--color-accent-bg) !important;
  border-color: transparent !important;
  color: var(--color-accent) !important;
  font-size: 11px;
}
.skill-more {
  font-size: 11px;
  color: var(--color-text-muted);
  line-height: 22px;
}
.cand-meta {
  display: flex;
  gap: 10px;
  font-size: 12px;
  color: var(--color-text-muted);
}
.cand-score {
  text-align: center;
  flex-shrink: 0;
}
.score-ring {
  position: relative;
  width: 44px;
  height: 44px;
  margin: 0 auto;
}
.score-num {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 800;
}
.score-label {
  font-size: 10px;
  font-weight: 600;
  margin-top: 2px;
}

/* 详情弹窗 */
.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
}
.detail-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--el-color-primary-light-8);
  color: var(--el-color-primary);
  font-size: 22px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.detail-name {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-text-primary);
}
.detail-username {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-top: 2px;
}
.detail-big-score {
  margin-left: auto;
  text-align: center;
}
.big-num {
  font-size: 32px;
  font-weight: 800;
}
.big-unit {
  font-size: 14px;
  font-weight: 400;
  opacity: 0.6;
}
.detail-section {
  margin-bottom: 16px;
}
.detail-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-secondary);
  margin-bottom: 6px;
}
.detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
.detail-ability {
  font-size: 13px;
  line-height: 1.7;
  color: var(--color-text-primary);
  background: #FAFAF8;
  border: 1px solid #EDEDF0;
  border-radius: 8px;
  padding: 12px;
  white-space: pre-wrap;
}
</style>
