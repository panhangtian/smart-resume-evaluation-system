<template>
  <div>
    <h3 class="page-title">能力评估</h3>

    <!-- 选择区 -->
    <div class="section-card">
      <p style="color:var(--color-text-muted);margin-bottom:16px;font-size:13px">
        选择简历和岗位，AI 将自动进行多维度匹配评分
      </p>
      <el-row :gutter="16" style="margin-bottom:16px">
        <el-col :span="8">
          <el-select v-model="selectedResume" placeholder="选择简历" style="width:100%" @change="loadEvals">
            <el-option v-for="r in resumes" :key="r.id" :label="r.fileName" :value="r.id" />
          </el-select>
        </el-col>
        <el-col :span="8">
          <el-select v-model="selectedJob" placeholder="选择岗位" style="width:100%">
            <el-option v-for="j in jobs" :key="j.id" :label="j.title" :value="j.id" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="handleEvaluate" :loading="evaluating">
            <el-icon style="margin-right:4px"><Lightning /></el-icon>开始评估
          </el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 评分展示区 -->
    <div v-if="selectedEval" class="eval-result-area">
      <!-- 综合评分大卡片 -->
      <div class="overall-card">
        <div class="overall-ring">
          <svg viewBox="0 0 120 120" width="120" height="120">
            <circle cx="60" cy="60" r="54" fill="none" stroke="#F0EEEA" stroke-width="8" />
            <circle cx="60" cy="60" r="54" fill="none" stroke="currentColor" stroke-width="8"
              :stroke-dasharray="`${overallCircumference * selectedEval.overall / 100} ${overallCircumference}`"
              stroke-linecap="round" transform="rotate(-90, 60, 60)"
              :style="{ color: scoreColor(selectedEval.overall) }" />
          </svg>
          <div class="overall-number">{{ selectedEval.overall }}</div>
        </div>
        <div class="overall-info">
          <div class="overall-label">综合匹配度</div>
          <div class="overall-sub" :style="{ color: scoreColor(selectedEval.overall) }">
            {{ scoreLabel(selectedEval.overall) }}
          </div>
          <div class="overall-comment">{{ selectedEval.comment }}</div>
        </div>
      </div>

      <!-- 维度评分条 -->
      <el-row :gutter="16" style="margin-top:16px">
        <el-col :span="8" v-for="dim in dimensions" :key="dim.key">
          <div class="dim-card">
            <div class="dim-header">
              <span class="dim-icon">{{ dim.icon }}</span>
              <span class="dim-label">{{ dim.label }}</span>
            </div>
            <div class="dim-number" :style="{ color: scoreColor(selectedEval[dim.key]) }">
              {{ selectedEval[dim.key] }}<span class="dim-unit">分</span>
            </div>
            <el-progress :percentage="selectedEval[dim.key]" :color="scoreColor(selectedEval[dim.key])"
              :stroke-width="8" />
            <div class="dim-desc">{{ dim.desc }}</div>
          </div>
        </el-col>
      </el-row>

      <!-- 优势 & 短板 -->
      <el-row :gutter="16" style="margin-top:16px">
        <el-col :span="12">
          <div class="strength-card">
            <div class="tag-header">
              <span class="tag-icon">✓</span> 优势
            </div>
            <div v-if="strengthsList.length">
              <el-tag v-for="(s, i) in strengthsList" :key="i" style="margin:4px;background:var(--color-accent-bg);border-color:transparent;color:var(--color-accent)">
                {{ s }}
              </el-tag>
            </div>
            <div v-else class="empty-tags">暂无数据</div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="gap-card">
            <div class="tag-header">
              <span class="tag-icon">△</span> 待提升
            </div>
            <div v-if="gapsList.length">
              <el-tag v-for="(g, i) in gapsList" :key="i" style="margin:4px;background:#FFF5F5;border-color:transparent;color:#E53E3E">
                {{ g }}
              </el-tag>
            </div>
            <div v-else class="empty-tags">暂无数据</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 评估记录表格 -->
    <div class="section-card" style="margin-top:24px">
      <h4 style="margin-bottom:16px;font-weight:600;font-size:15px;color:var(--color-text-primary)">📋 历史评估记录</h4>
      <el-table :data="evaluations" v-loading="loading" style="width:100%" @row-click="viewEval">
        <el-table-column label="岗位" min-width="160">
          <template #default="{row}">{{ row.jobTitle || ('岗位 #' + row.jobId) }}</template>
        </el-table-column>
        <el-table-column label="综合分" width="100" align="center">
          <template #default="{row}">
            <el-tag :type="overallTagType(row.overall)" effect="dark" round>{{ row.overall }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="技能" width="70" align="center">
          <template #default="{row}">{{ row.skillMatch }}</template>
        </el-table-column>
        <el-table-column label="经验" width="70" align="center">
          <template #default="{row}">{{ row.experienceMatch }}</template>
        </el-table-column>
        <el-table-column label="学历" width="70" align="center">
          <template #default="{row}">{{ row.educationMatch }}</template>
        </el-table-column>
        <el-table-column prop="comment" label="评语" show-overflow-tooltip min-width="200" />
        <el-table-column prop="createTime" label="评估时间" width="170" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getMyResumes } from '@/api/resume'
import { getAvailableJobs } from '@/api/job'
import { evaluate, getMyEvaluations } from '@/api/evaluate'
import { ElMessage } from 'element-plus'

const resumes = ref([])
const jobs = ref([])
const evaluations = ref([])
const selectedResume = ref('')
const selectedJob = ref('')
const loading = ref(false)
const evaluating = ref(false)
const selectedEval = ref(null)

const overallCircumference = 2 * Math.PI * 54

const dimensions = [
  { key: 'skillMatch', label: '技能匹配', icon: '⚙', desc: '简历技能与岗位要求的重合度' },
  { key: 'experienceMatch', label: '经验匹配', icon: '📅', desc: '工作年限与岗位预期的匹配度' },
  { key: 'educationMatch', label: '学历匹配', icon: '🎓', desc: '学历背景与岗位标准的匹配度' }
]

const strengthsList = computed(() => parseArray(selectedEval.value?.strengths))
const gapsList = computed(() => parseArray(selectedEval.value?.gaps))

onMounted(async () => {
  const [r, j] = await Promise.all([getMyResumes(), getAvailableJobs()])
  resumes.value = (r.data || []).filter(i => i.status === 2)
  jobs.value = j.data || []
  if (resumes.value.length) {
    selectedResume.value = resumes.value[0].id
    await loadEvals()
    // 自动选中最新一条记录
    if (evaluations.value.length) selectedEval.value = evaluations.value[0]
  }
})

async function loadEvals() {
  if (!selectedResume.value) return
  loading.value = true
  selectedEval.value = null
  try {
    const res = await getMyEvaluations(selectedResume.value)
    evaluations.value = (res.data || []).map(e => ({
      ...e,
      strengths: parseRaw(e.strengths),
      gaps: parseRaw(e.gaps)
    }))
  } finally { loading.value = false }
}

async function handleEvaluate() {
  if (!selectedResume.value || !selectedJob.value) {
    ElMessage.warning('请先选择简历和岗位')
    return
  }
  evaluating.value = true
  try {
    const res = await evaluate(selectedResume.value, selectedJob.value)
    ElMessage.success('✅ 评估完成，综合分：' + res.data.overall)
    await loadEvals()
    // 选中最新评估
    const latest = evaluations.value[0]
    if (latest) {
      selectedEval.value = latest
    }
  } finally { evaluating.value = false }
}

function viewEval(row) {
  selectedEval.value = row
  // 滚动到评分区域
  setTimeout(() => {
    const el = document.querySelector('.eval-result-area')
    if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }, 100)
}

function scoreColor(score) {
  if (score >= 80) return '#5D9B71'
  if (score >= 60) return '#D4943F'
  return '#DF5A3F'
}

function scoreLabel(score) {
  if (score >= 85) return '非常匹配'
  if (score >= 70) return '比较匹配'
  if (score >= 60) return '一般匹配'
  if (score >= 40) return '不太匹配'
  return '不匹配'
}

function overallTagType(score) {
  if (score >= 80) return 'success'
  if (score >= 60) return 'warning'
  return 'danger'
}

function parseArray(val) {
  if (!val) return []
  try { return JSON.parse(val) } catch { return [val] }
}

function parseRaw(val) {
  if (!val) return '[]'
  try { return JSON.parse(val) } catch { return JSON.stringify([val]) }
}
</script>

<style scoped>
/* 综合评分大卡片 */
.eval-result-area {
  animation: fadeIn 0.3s ease;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.overall-card {
  background: var(--color-bg-card);
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 24px;
  border: 1px solid #EDEDF0;
  box-shadow: 0 1px 3px rgba(43,43,49,0.06);
}
.overall-ring {
  position: relative;
  width: 120px;
  height: 120px;
  flex-shrink: 0;
}
.overall-ring svg {
  color: var(--el-color-primary);
}
.overall-number {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: 800;
  color: var(--color-text-primary);
}
.overall-info {
  flex: 1;
}
.overall-label {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-text-primary);
}
.overall-sub {
  font-size: 15px;
  font-weight: 600;
  margin-top: 4px;
}
.overall-comment {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-top: 8px;
  line-height: 1.6;
}

/* 维度卡片 */
.dim-card {
  background: var(--color-bg-card);
  border: 1px solid #EDEDF0;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.2s ease;
}
.dim-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(43,43,49,0.08);
}
.dim-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}
.dim-icon {
  font-size: 20px;
}
.dim-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
}
.dim-number {
  font-size: 28px;
  font-weight: 800;
  margin-bottom: 10px;
}
.dim-unit {
  font-size: 14px;
  font-weight: 400;
  opacity: 0.5;
}
.dim-desc {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-top: 8px;
}

/* 优势/短板卡片 */
.strength-card, .gap-card {
  background: var(--color-bg-card);
  border: 1px solid #EDEDF0;
  border-radius: 12px;
  padding: 20px;
  min-height: 80px;
}
.tag-header {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--color-text-primary);
}
.tag-icon {
  display: inline-block;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  text-align: center;
  line-height: 20px;
  font-size: 12px;
  margin-right: 6px;
}
.strength-card .tag-icon {
  background: var(--color-accent-bg);
  color: var(--color-accent);
}
.gap-card .tag-icon {
  background: #FFF5F5;
  color: #E53E3E;
}
.empty-tags {
  color: var(--color-text-muted);
  font-size: 13px;
  padding: 8px 0;
}
</style>
