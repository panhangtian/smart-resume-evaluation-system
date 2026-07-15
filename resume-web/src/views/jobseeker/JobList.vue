<template>
  <div class="page-wrapper">
    <h3 class="page-title">浏览岗位</h3>

    <!-- 搜索栏 -->
    <div class="section-card">
      <div style="display:flex;gap:12px;align-items:center">
        <el-input v-model="keyword" placeholder="搜索岗位名称、公司、技能要求…" style="flex:1" clearable @keyup.enter="search" />
        <el-button type="primary" @click="search"><el-icon style="margin-right:4px"><Search /></el-icon>搜索</el-button>
        <el-button :loading="extLoading" @click="refreshNow">
          <el-icon style="margin-right:4px"><Refresh /></el-icon>{{ extLoading ? '刷新中…' : '刷新岗位' }}
        </el-button>
      </div>
    </div>

    <!-- 筛选条 -->
    <div class="filter-bar" v-if="allJobs.length">
      <div class="filter-row">
        <span class="filter-label">🏷️ 行业</span>
        <div class="filter-pills">
          <span :class="['pill pill-industry', { active: !filterIndustry }]" @click="filterIndustry = ''">全部</span>
          <span v-for="ind in uniqueIndustries" :key="'i'+ind" :class="['pill pill-industry', { active: filterIndustry === ind }]" @click="filterIndustry = filterIndustry === ind ? '' : ind">
            {{ ind }}<i v-if="industryCount[ind]" class="pill-count">{{ industryCount[ind] }}</i>
          </span>
        </div>
      </div>
      <div class="filter-row">
        <span class="filter-label">📍 城市</span>
        <div class="filter-pills">
          <span :class="['pill', { active: !filterCity }]" @click="filterCity = ''">全部</span>
          <span v-for="c in uniqueCities" :key="'c'+c" :class="['pill', { active: filterCity === c }]" @click="filterCity = filterCity === c ? '' : c">{{ c }}</span>
        </div>
      </div>
      <div class="filter-row">
        <span class="filter-label">🔗 来源</span>
        <div class="filter-pills">
          <span :class="['pill', { active: !filterSource }]" @click="filterSource = ''">全部</span>
          <span v-for="s in uniqueSources" :key="'s'+s" :class="['pill', { active: filterSource === s }]" @click="filterSource = filterSource === s ? '' : s">{{ s }}</span>
        </div>
      </div>
      <div class="filter-row">
        <span class="filter-label">📋 类型</span>
        <div class="filter-pills">
          <span :class="['pill', { active: !filterType }]" @click="filterType = ''">全部</span>
          <span v-for="t in uniqueTypes" :key="'t'+t" :class="['pill', { active: filterType === t }]" @click="filterType = filterType === t ? '' : t">{{ t }}</span>
        </div>
      </div>
      <div class="filter-row">
        <span class="filter-label">🎓 学历</span>
        <div class="filter-pills">
          <span :class="['pill', { active: !filterEdu }]" @click="filterEdu = ''">全部</span>
          <span v-for="e in uniqueEdus" :key="'e'+e" :class="['pill', { active: filterEdu === e }]" @click="filterEdu = filterEdu === e ? '' : e">{{ e }}</span>
        </div>
      </div>
      <div class="filter-row">
        <span class="filter-label">💰 薪资</span>
        <div class="salary-range">
          <el-input-number v-model="salaryMinF" :min="0" :max="200" placeholder="最低K" size="small" controls-position="right" style="width:110px" @change="applyFilters" />
          <span style="margin:0 6px;color:var(--color-text-muted)">—</span>
          <el-input-number v-model="salaryMaxF" :min="0" :max="500" placeholder="最高K" size="small" controls-position="right" style="width:110px" @change="applyFilters" />
          <el-button size="small" text type="info" @click="salaryMinF=0; salaryMaxF=0; applyFilters()" style="margin-left:6px">清除</el-button>
        </div>
      </div>
    </div>

    <!-- 岗位数统计条 -->
    <div class="stats-bar">
      <span>共 <b>{{ allJobs.length }}</b> 个岗位</span>
      <span class="stats-detail">
        <span v-if="extJobs.length">外源 <b>{{ extJobs.length }}</b> 个 |
          <template v-for="(cnt, src) in extSourceStats" :key="src">
            {{ src }}:{{ cnt }}
          </template>
        </span>
        <span v-else-if="extLoaded && !extJobs.length" class="stats-fallback">外源暂不可用</span>
      </span>
      <span style="margin-left:auto;font-size:11px;color:var(--color-text-muted)" v-if="jobs.length !== allJobs.length">
        筛选后 <b>{{ jobs.length }}</b> 个
      </span>
    </div>

    <!-- 岗位卡片网格 -->
    <div v-if="displayedJobs.length" class="job-grid">
      <div v-for="job in displayedJobs" :key="job.id" class="job-card" @click="viewJob(job)">
        <div class="job-card-header">
          <div class="job-card-brand">{{ job.company?.charAt(0) || '🏢' }}</div>
          <div class="job-card-headtext">
            <div class="job-card-title">{{ job.title }}</div>
            <div class="job-card-badges">
              <span v-if="job.industry" class="industry-pill">{{ job.industry }}</span>
              <span v-if="isExternalJob(job)" class="source-pill">🏢 系统HR</span>
              <span v-else-if="metaOf(job.requirementTags).source[0]" class="source-pill">🔗 {{ metaOf(job.requirementTags).source[0] }}</span>
            </div>
          </div>
        </div>
        <div class="job-card-meta">
          <span class="meta-item">{{ job.company }}</span>
          <span class="meta-item">📍 {{ job.location }}</span>
          <span class="meta-item salary">{{ salaryText(job) }}</span>
          <span v-for="e in metaOf(job.requirementTags).edu" :key="'e'+e" class="meta-item">{{ e }}</span>
          <span v-for="e in metaOf(job.requirementTags).exp" :key="'x'+e" class="meta-item">{{ e }}</span>
          <span v-for="e in metaOf(job.requirementTags).type" :key="'t'+e" class="meta-item type">{{ e }}</span>
        </div>
        <div class="job-card-desc">{{ truncate(job.description, 64) }}</div>
        <div class="job-card-tags">
          <el-tag v-for="tag in metaOf(job.requirementTags).skills.slice(0,4)" :key="tag" size="small" style="margin:2px;background:var(--color-accent-bg);border-color:transparent;color:var(--color-accent)">
            {{ tag }}
          </el-tag>
        </div>
        <div class="job-card-actions">
          <el-button size="small" @click.stop="viewJob(job)">详情</el-button>
          <el-button size="small" type="primary" @click.stop="evaluateJob(job)">评估</el-button>
          <el-button v-if="!isExternalJob(job)" size="small" type="success" @click.stop="handleApply(job)">投递</el-button>
          <el-button v-else size="small" type="warning" @click.stop="openApplyGuide(job)">去{{ job.source || '原平台' }}投递</el-button>
        </div>
      </div>
      <!-- 无限滚动哨兵 -->
      <div ref="sentinel" class="scroll-sentinel" v-if="hasMore">
        <span v-if="loading">加载中…</span>
        <span v-else>下拉加载更多…</span>
      </div>
    </div>

    <div v-else class="empty-state">
      <div class="empty-icon">🔍</div>
      <p v-if="loading">加载中…</p>
      <p v-else-if="allJobs.length && !jobs.length">没有匹配的岗位，试试调整筛选条件</p>
      <p v-else>暂无岗位数据，请先以 HR 账号登录发布岗位</p>
    </div>

    <!-- 岗位详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="current?.title" width="600px">
      <template v-if="current">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="公司">{{ current.company }}</el-descriptions-item>
          <el-descriptions-item v-if="current.industry" label="行业">{{ current.industry }}</el-descriptions-item>
          <el-descriptions-item label="地点">{{ current.location }}</el-descriptions-item>
          <el-descriptions-item label="薪资(月/千元)">{{ salaryText(current) }}</el-descriptions-item>
          <el-descriptions-item label="发布者">
            <span class="source-pill">{{ isExternalJob(current) ? '🏢 系统HR' : '🏢 ' + (current.company || 'HR') }}</span>
          </el-descriptions-item>
          <el-descriptions-item v-if="metaOf(current.requirementTags).edu[0]" label="学历要求">{{ metaOf(current.requirementTags).edu.join(' / ') }}</el-descriptions-item>
          <el-descriptions-item v-if="metaOf(current.requirementTags).exp[0]" label="经验要求">{{ metaOf(current.requirementTags).exp.join(' / ') }}</el-descriptions-item>
          <el-descriptions-item v-if="metaOf(current.requirementTags).type[0]" label="岗位类型">{{ metaOf(current.requirementTags).type.join(' / ') }}</el-descriptions-item>
          <el-descriptions-item label="描述">{{ current.description }}</el-descriptions-item>
          <el-descriptions-item label="技能要求">
            <el-tag v-for="tag in metaOf(current.requirementTags).skills" :key="tag" size="small" style="margin:2px;background:var(--color-accent-bg);border-color:transparent;color:var(--color-accent)">{{ tag }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <div style="display:flex;gap:12px;margin-top:20px;justify-content:flex-end">
          <el-button type="primary" @click="evaluateJob(current)">AI 评估匹配度</el-button>
          <el-button v-if="!isExternalJob(current)" type="success" @click="handleApply(current)">立即投递</el-button>
          <el-button v-else type="warning" @click="openApplyGuide(current)">前往{{ current.source || '原平台' }}投递</el-button>
        </div>
      </template>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 外源岗位投递引导弹窗 -->
    <el-dialog v-model="applyGuideVisible" title="前往原平台投递" width="480px">
      <template v-if="applyGuideJob">
        <div class="apply-guide">
          <div class="apply-guide-icon">🔗</div>
          <div class="apply-guide-title">该岗位来自 <b>{{ applyGuideJob.source || '外部招聘平台' }}</b> 聚合</div>
          <div class="apply-guide-job">
            <div><span class="lbl">岗位</span>{{ applyGuideJob.title }}</div>
            <div><span class="lbl">公司</span>{{ applyGuideJob.company }}</div>
            <div><span class="lbl">地点</span>{{ applyGuideJob.location }}</div>
            <div><span class="lbl">薪资</span>{{ salaryText(applyGuideJob) }}</div>
          </div>
          <div class="apply-guide-hint">
            <p>💡 <b>请前往</b>「{{ applyGuideJob.source || '原招聘平台' }}」<b>平台</b>，搜索以下关键词找到该岗位并投递：</p>
            <div class="apply-guide-keywords">{{ applyGuideJob.title }} {{ applyGuideJob.company }}</div>
          </div>
          <div class="apply-guide-tip">本系统暂未对接 {{ applyGuideJob.source || '该平台' }} 的 API，因此无法直接代为投递。可点击下方"我知道了"关闭此提示。</div>
        </div>
      </template>
      <template #footer>
        <el-button @click="applyGuideVisible = false">我知道了</el-button>
      </template>
    </el-dialog>

    <!-- 外源岗位本地评估结果 -->
    <el-dialog v-model="extEvalVisible" title="本地评估 · 外源岗位" width="520px">
      <div v-if="extEvalLoading" style="text-align:center;padding:30px 0">
        <el-icon class="is-loading" style="font-size:32px;color:var(--el-color-primary)"><Loading /></el-icon>
        <p style="margin-top:12px;font-size:14px;color:var(--color-text-muted)">正在分析匹配度…</p>
      </div>
      <template v-else-if="extEvalResult">
        <div v-if="extEvalResult.error" style="text-align:center;padding:20px;color:#DF5A3F">
          {{ extEvalResult.msg }}
        </div>
        <template v-else>
          <div class="exteval-header">
            <div class="exteval-score-ring">
              <svg viewBox="0 0 80 80" width="80" height="80">
                <circle cx="40" cy="40" r="34" fill="none" stroke="#F0EEEA" stroke-width="6" />
                <circle cx="40" cy="40" r="34" fill="none" :stroke="extEvalMatchColor(extEvalResult.overall)" stroke-width="6"
                  :stroke-dasharray="`${214 * extEvalResult.overall / 100} 214`"
                  stroke-linecap="round" transform="rotate(-90 40 40)" />
              </svg>
              <span class="exteval-score-num">{{ extEvalResult.overall }}</span>
            </div>
            <div class="exteval-meta">
              <div class="exteval-title">{{ extEvalResult.jobTitle }}</div>
              <div class="exteval-company">{{ extEvalResult.company }}</div>
              <el-tag size="small" type="warning" style="margin-top:6px">外源岗位 · 本地评估</el-tag>
            </div>
          </div>
          <el-divider />
          <div class="exteval-breakdown">
            <div class="exteval-row">
              <span class="exteval-label">🔧 技能匹配</span>
              <el-progress :percentage="extEvalResult.skillScore" :stroke-width="8" :color="extEvalMatchColor(extEvalResult.skillScore*100/70)" style="flex:1" />
              <span class="exteval-num">{{ extEvalResult.skillScore }}</span>
            </div>
            <div class="exteval-row">
              <span class="exteval-label">📅 经验加值</span>
              <el-progress :percentage="extEvalResult.expScore*10" :stroke-width="8" color="#D4943F" style="flex:1" />
              <span class="exteval-num">{{ extEvalResult.expScore }}</span>
            </div>
            <div class="exteval-row">
              <span class="exteval-label">📝 画像完整</span>
              <el-progress :percentage="extEvalResult.profileScore*10" :stroke-width="8" color="#5D9B71" style="flex:1" />
              <span class="exteval-num">{{ extEvalResult.profileScore }}</span>
            </div>
          </div>
          <el-divider />
          <div class="exteval-analysis">
            <div class="exteval-label">📊 分析</div>
            <p>{{ extEvalResult.analysis }}</p>
          </div>
          <div class="exteval-skills" v-if="extEvalResult.matchedSkills.length">
            <span class="exteval-tag-label">✅ 已匹配</span>
            <el-tag v-for="s in extEvalResult.matchedSkills" :key="s" size="small" effect="plain" type="success" style="margin:2px">{{ s }}</el-tag>
          </div>
          <div class="exteval-skills" v-if="extEvalResult.missingSkills.length">
            <span class="exteval-tag-label">⚠️ 待提升</span>
            <el-tag v-for="s in extEvalResult.missingSkills" :key="s" size="small" effect="plain" type="danger" style="margin:2px">{{ s }}</el-tag>
          </div>
        </template>
      </template>
      <template #footer>
        <el-button @click="extEvalVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, onUnmounted, nextTick } from 'vue'
import { getAvailableJobs } from '@/api/job'
import { evaluate } from '@/api/evaluate'
import { applyJob } from '@/api/application'
import { getMyResumes, getResume } from '@/api/resume'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const allJobs = ref([])
const jobs = ref([])
const extJobs = ref([])
const extLoading = ref(false)
const extLoaded = ref(false)
const loading = ref(false)
const keyword = ref('')
const detailVisible = ref(false)
const current = ref(null)
const applyGuideVisible = ref(false)
const applyGuideJob = ref(null)

// ---- 外源岗位本地评估 ----
const extEvalVisible = ref(false)
const extEvalLoading = ref(false)
const extEvalResult = ref(null)

// ---- 无限滚动 ----
const displayCount = ref(20)
const PAGE_SIZE = 20
const sentinel = ref(null)

// ---- 自动刷新轮询 ----
let pollTimer = null

// ---- 筛选状态 ----
const filterCity = ref('')
const filterSource = ref('')
const filterType = ref('')
const filterEdu = ref('')
const filterIndustry = ref('')
const salaryMinF = ref(0)
const salaryMaxF = ref(0)

// ---- 从全量数据提取唯一值 ----
const uniqueIndustries = computed(() => {
  const set = new Set()
  allJobs.value.forEach(j => { if (j.industry) set.add(j.industry) })
  return [...set].sort()
})
const industryCount = computed(() => {
  const stats = {}
  allJobs.value.forEach(j => { if (j.industry) stats[j.industry] = (stats[j.industry] || 0) + 1 })
  return stats
})
const uniqueCities = computed(() => {
  const set = new Set()
  allJobs.value.forEach(j => { if (j.location) set.add(j.location) })
  return [...set].sort()
})
const uniqueSources = computed(() => {
  const set = new Set()
  allJobs.value.forEach(j => metaOf(j.requirementTags).source.forEach(s => set.add(s)))
  return [...set].sort()
})
const uniqueTypes = computed(() => {
  const set = new Set()
  allJobs.value.forEach(j => metaOf(j.requirementTags).type.forEach(t => set.add(t)))
  return [...set].sort()
})
const uniqueEdus = computed(() => {
  const set = new Set()
  allJobs.value.forEach(j => metaOf(j.requirementTags).edu.forEach(e => set.add(e)))
  return [...set].sort()
})
const extSourceStats = computed(() => {
  const stats = {}
  extJobs.value.forEach(j => { const s = j.source || '未知'; stats[s] = (stats[s] || 0) + 1 })
  return stats
})

// ---- 应用筛选 ----
function applyFilters() {
  let list = allJobs.value
  if (filterIndustry.value) list = list.filter(j => j.industry === filterIndustry.value)
  if (filterCity.value) list = list.filter(j => j.location === filterCity.value)
  if (filterSource.value) list = list.filter(j => metaOf(j.requirementTags).source.includes(filterSource.value))
  if (filterType.value) list = list.filter(j => metaOf(j.requirementTags).type.includes(filterType.value))
  if (filterEdu.value) list = list.filter(j => metaOf(j.requirementTags).edu.includes(filterEdu.value))
  if (salaryMinF.value > 0) list = list.filter(j => (j.salaryMax || 0) >= salaryMinF.value)
  if (salaryMaxF.value > 0) list = list.filter(j => (j.salaryMin || 0) <= salaryMaxF.value)
  jobs.value = list
  displayCount.value = PAGE_SIZE
}

function salaryText(job) {
  if (!job.salaryMin && !job.salaryMax) return '💰 薪资面议'
  const min = job.salaryMin || 0
  const max = job.salaryMax || 0
  if (min === max) return `💰 ${min}K/月`
  return `💰 ${min}-${max}K/月`
}

// ---- 无限滚动 ----
const displayedJobs = computed(() => jobs.value.slice(0, displayCount.value))
const hasMore = computed(() => displayCount.value < jobs.value.length)

function loadMore() {
  if (!hasMore.value) return
  displayCount.value += PAGE_SIZE
}

// ---- IntersectionObserver ----
let observer = null
function setupObserver() {
  if (observer) observer.disconnect()
  if (!sentinel.value) return
  observer = new IntersectionObserver((entries) => {
    if (entries[0].isIntersecting && hasMore.value) {
      loadMore()
    }
  }, { rootMargin: '100px' })
  observer.observe(sentinel.value)
}

watch([displayedJobs], () => {
  nextTick(() => {
    if (sentinel.value && observer) {
      observer.disconnect()
      observer.observe(sentinel.value)
    }
  })
})

// 监听筛选变化自动应用
watch([filterIndustry, filterCity, filterSource, filterType, filterEdu], () => applyFilters())

onMounted(async () => {
  await load()
  await nextTick()
  setupObserver()
  startPolling()
})

onUnmounted(() => {
  stopPolling()
  if (observer) { observer.disconnect(); observer = null }
})

async function load() {
  loading.value = true
  try {
    const [backendRes] = await Promise.all([
      getAvailableJobs(keyword.value),
      fetchExtJobs()
    ])
    mergeJobs(backendRes)
  } finally { loading.value = false }
}

// ---- 自动刷新轮询(60s) ----
function startPolling() {
  stopPolling()
  pollTimer = setInterval(() => {
    fetchExtJobs().then(() => {
      getAvailableJobs(keyword.value).then(backendRes => mergeJobs(backendRes))
    })
  }, 60000)
}
function stopPolling() {
  if (pollTimer) { clearInterval(pollTimer); pollTimer = null }
}

async function search() { load() }

// ---- 从外挂服务获取岗位 ----
const EXT_URL = 'http://localhost:3001/api/jobs'
async function fetchExtJobs() {
  extLoading.value = true
  try {
    const resp = await fetch(EXT_URL)
    if (!resp.ok) throw new Error('HTTP ' + resp.status)
    const data = await resp.json()
    extJobs.value = data.jobs || []
    extLoaded.value = true
  } catch {
    // job-service 不可用时保留已有外源数据
    if (!extLoaded.value) extJobs.value = []
    extLoaded.value = false
  } finally {
    extLoading.value = false
  }
}

function mergeJobs(backendRes) {
  allJobs.value = (backendRes.data || []).concat(extJobs.value)
  applyFilters()
}

// 手动刷新
async function refreshNow() {
  await fetchExtJobs()
  const backendRes = await getAvailableJobs(keyword.value)
  mergeJobs(backendRes)
  if (extLoaded.value) {
    ElMessage.success('已刷新岗位数据，共 ' + allJobs.value.length + ' 个岗位')
  } else {
    ElMessage.warning('外源岗位服务暂不可用，仅显示系统岗位')
  }
}

function viewJob(row) {
  current.value = row
  detailVisible.value = true
}

async function evaluateJob(row) {
  // 外源岗位走本地评估（后端只支持 Long 型 jobId）
  if (isExternalJob(row)) {
    detailVisible.value = false
    evaluateExternalJob(row)
    return
  }
  const res = await getMyResumes()
  const list = res.data || []
  if (list.length === 0) {
    ElMessage.warning('请先上传简历')
    router.push('/resumes')
    return
  }
  const r = list.find(i => i.status === 2) || list[0]
  try {
    const ev = await evaluate(r.id, row.id)
    ElMessage.success('✅ 评估完成，综合分：' + ev.data.overall)
    router.push('/evaluate')
  } catch (e) {
    ElMessage.error('评估失败：' + (e.message || '未知错误'))
  }
}

// 外源岗位本地评估：提取岗位技能 + 简历技能，计算匹配度
async function evaluateExternalJob(job) {
  // 1. 找到已解析简历
  const res = await getMyResumes()
  const list = res.data || []
  const matched = list.find(i => i.status === 2)
  if (!matched) { ElMessage.warning('请先上传并完成解析的简历'); router.push('/resumes'); return }

  extEvalLoading.value = true
  extEvalResult.value = null
  extEvalVisible.value = true

  try {
    // 2. 取简历详情（skills / abilityProfile / yearsOfExperience）
    const { data: r } = await getResume(matched.id)
    // 3. 岗位要求技能
    const jobSkills = metaOf(job.requirementTags).skills.map(s => (''+s).toLowerCase().trim()).filter(Boolean)
    // 4. 简历技能
    const resumeSkills = (r.skills || '').split(',').map(s => s.trim().toLowerCase()).filter(Boolean)
    // 5. 技能匹配
    const matchedList = jobSkills.filter(s => resumeSkills.includes(s))
    const missingList = jobSkills.filter(s => !resumeSkills.includes(s))
    const extraList = resumeSkills.filter(s => !jobSkills.includes(s) && s.length > 0)
    // 6. 匹配分
    let skillScore = 0
    if (jobSkills.length > 0) {
      skillScore = Math.round((matchedList.length / jobSkills.length) * 70)
    }
    // 经验分 (有年限加 10)
    let expScore = (r.yearsOfExperience != null && r.yearsOfExperience > 0) ? 10 : 0
    // 能力画像存在加 10
    let profileScore = (r.abilityProfile && r.abilityProfile.trim()) ? 10 : 0
    // 基础分 10
    let overall = 10 + skillScore + expScore + profileScore
    if (overall > 99) overall = 99
    if (overall < 1) overall = 1

    const analysis = []
    if (matchedList.length > 0) analysis.push(`匹配 ${matchedList.length} 项技能：${matchedList.join('、')}`)
    if (missingList.length > 0) analysis.push(`缺少 ${missingList.length} 项技能：${missingList.join('、')}`)
    if (r.yearsOfExperience != null && r.yearsOfExperience > 0) analysis.push(`具备 ${r.yearsOfExperience} 年工作经验`)
    if (r.abilityProfile && r.abilityProfile.trim()) analysis.push(`能力画像已完善`)
    if (extraList.length > 0) analysis.push(`额外掌握：${extraList.slice(0,6).join('、')}${extraList.length>6?'…':''}`)

    extEvalResult.value = {
      jobTitle: job.title,
      company: job.company,
      overall,
      skillScore,
      expScore,
      profileScore,
      matchedSkills: matchedList,
      missingSkills: missingList,
      extraSkills: extraList,
      analysis: analysis.length ? analysis.join('；') : '技能匹配度偏低，建议补充相关技能'
    }
  } catch (e) {
    extEvalResult.value = { error: true, msg: e.message || '评估失败' }
  } finally {
    extEvalLoading.value = false
  }
}

async function handleApply(row) {
  try {
    await ElMessageBox.confirm('确认投递「' + row.title + '」？')
    const res = await getMyResumes()
    const list = res.data || []
    const valid = list.find(i => i.status === 2)
    if (!valid) { ElMessage.warning('请先上传且完成解析的简历'); return }
    await applyJob(valid.id, row.id)
    ElMessage.success('🎉 投递成功')
    router.push('/applications')
  } catch {}
}

function openApplyGuide(job) {
  applyGuideJob.value = job
  applyGuideVisible.value = true
}

function isExternalJob(job) {
  return typeof job.id === 'string' && job.id.startsWith('ext-')
}

function truncate(text, len) {
  if (!text) return ''
  return text.length > len ? text.slice(0, len) + '…' : text
}

function parseTags(tags) {
  if (!tags) return []
  return tags.split(',').map(t => t.trim()).filter(Boolean).slice(0, 5)
}

// 将 requirementTags 拆分为：来源 / 学历 / 经验 / 类型 / 技能
function metaOf(tags) {
  const out = { source: [], edu: [], exp: [], type: [], skills: [] }
  if (!tags) return out
  tags.split(',').map(t => t.trim()).filter(Boolean).forEach(t => {
    if (t.startsWith('来源:')) out.source.push(t.slice(3))
    else if (t.startsWith('学历:')) out.edu.push(t.slice(3))
    else if (t.startsWith('经验:')) out.exp.push(t.slice(3))
    else if (t.startsWith('类型:')) out.type.push(t.slice(3))
    else out.skills.push(t)
  })
  return out
}

// 外源评估匹配度颜色
function extEvalMatchColor(score) {
  if (score >= 80) return '#5D9B71'
  if (score >= 60) return '#D4943F'
  return '#DF5A3F'
}
</script>

<style scoped>
/* 页面容器：自身居中 */
.page-wrapper {
  width: 100%;
  max-width: 1280px;
  margin: 0 auto;
  padding: 24px 24px 32px;
  box-sizing: border-box;
}

/* 筛选条 */
.filter-bar {
  background: var(--color-bg-card);
  border: 1px solid #EDEDF0;
  border-radius: 12px;
  padding: 14px 18px;
  margin-top: 14px;
  margin-bottom: 0;
}
.filter-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}
.filter-row:last-child { margin-bottom: 0; }
.filter-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text-secondary);
  min-width: 44px;
  flex-shrink: 0;
}
.filter-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}
.pill {
  font-size: 12px;
  padding: 2px 10px;
  border-radius: 999px;
  background: #F5F4F2;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all 0.15s ease;
  user-select: none;
  white-space: nowrap;
}
.pill:hover { background: #EBE9E5; }
.pill.active {
  background: var(--el-color-primary-light-8);
  color: var(--el-color-primary);
  font-weight: 600;
}
.pill-industry {
  background: #EAF2F6;
  color: #2C5E74;
}
.pill-industry:hover { background: #DCE9EF; }
.pill-industry.active {
  background: var(--el-color-primary);
  color: #fff;
}
.pill-count {
  font-style: normal;
  font-size: 10px;
  margin-left: 5px;
  padding: 0 5px;
  border-radius: 999px;
  background: rgba(0,0,0,0.08);
}
.pill-industry.active .pill-count { background: rgba(255,255,255,0.28); }

.job-card-badges {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
  margin-top: 4px;
}
.industry-pill {
  font-size: 11px;
  font-weight: 600;
  color: #2C5E74;
  background: #EAF2F6;
  border: 1px solid rgba(44,94,116,0.25);
  padding: 1px 8px;
  border-radius: 999px;
  line-height: 1.6;
}
.salary-range {
  display: flex;
  align-items: center;
}

/* 统计条 */
.stats-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: var(--color-text-secondary);
  padding: 10px 0 6px;
  flex-wrap: wrap;
}
.stats-bar b { color: var(--color-text-primary); }
.stats-detail { font-size: 11px; color: var(--color-text-muted); }
.stats-fallback { color: #c0a060; }

.job-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 14px;
  margin-top: 16px;
}
.job-card {
  background: var(--color-bg-card);
  border: 1px solid #EDEDF0;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  overflow: hidden;
}
.job-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(90deg, var(--color-accent), var(--el-color-primary));
  opacity: 0.4;
}
.job-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(43,43,49,0.10);
}
.job-card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.job-card-brand {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  background: var(--color-accent-bg);
  color: var(--color-accent);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 700;
  flex-shrink: 0;
}
.job-card-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-text-primary);
}
.job-card-headtext {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}
.source-pill {
  align-self: flex-start;
  font-size: 11px;
  font-weight: 600;
  color: var(--el-color-primary);
  background: rgba(74,144,179,0.10);
  border: 1px solid rgba(74,144,179,0.25);
  padding: 1px 8px;
  border-radius: 999px;
  line-height: 1.6;
}
.meta-item.salary { color: var(--color-accent); font-weight: 600; }
.meta-item.type { color: var(--el-color-primary); }
.job-card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}
.meta-item {
  font-size: 12px;
  color: var(--color-text-secondary);
  background: #F5F4F2;
  padding: 2px 8px;
  border-radius: 4px;
}
.job-card-desc {
  font-size: 13px;
  color: var(--color-text-secondary);
  line-height: 1.5;
  margin-bottom: 12px;
}
.job-card-tags {
  margin-bottom: 14px;
}
.job-card-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  border-top: 1px solid #F0EEEA;
  padding-top: 12px;
}
.scroll-sentinel {
  grid-column: 1 / -1;
  text-align: center;
  padding: 16px 0;
  font-size: 13px;
  color: var(--color-text-muted);
}

/* 外源投递引导弹窗 */
.apply-guide {
  text-align: center;
  padding: 8px 4px;
}
.apply-guide-icon {
  font-size: 48px;
  margin-bottom: 12px;
}
.apply-guide-title {
  font-size: 16px;
  color: var(--color-text-primary);
  margin-bottom: 16px;
}
.apply-guide-job {
  background: #F8F7F4;
  border-radius: 10px;
  padding: 14px 18px;
  text-align: left;
  margin-bottom: 16px;
  font-size: 14px;
  line-height: 2;
  color: var(--color-text-primary);
}
.apply-guide-job .lbl {
  display: inline-block;
  min-width: 48px;
  font-weight: 600;
  color: var(--color-text-secondary);
  font-size: 13px;
}
.apply-guide-hint {
  background: #FFF8E6;
  border: 1px solid #F0E6C0;
  border-radius: 10px;
  padding: 14px 18px;
  margin-bottom: 14px;
  text-align: left;
  font-size: 13px;
  line-height: 1.7;
  color: #7A6A30;
}
.apply-guide-hint p { margin: 0 0 8px; }
.apply-guide-keywords {
  display: inline-block;
  background: #FFF;
  border: 1px dashed #D4C896;
  border-radius: 6px;
  padding: 6px 14px;
  font-weight: 600;
  font-size: 15px;
  color: #5A4A20;
}
.apply-guide-tip {
  font-size: 12px;
  color: var(--color-text-muted);
  line-height: 1.5;
  padding: 0 8px;
}

/* ─── 外源岗位本地评估弹窗 ─── */
.exteval-header {
  display: flex;
  align-items: center;
  gap: 20px;
}
.exteval-score-ring {
  position: relative;
  flex-shrink: 0;
}
.exteval-score-num {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: 800;
}
.exteval-meta { flex: 1; }
.exteval-title { font-size: 17px; font-weight: 700; color: var(--color-text-primary); }
.exteval-company { font-size: 13px; color: var(--color-text-muted); margin-top: 2px; }

.exteval-breakdown {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.exteval-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.exteval-label {
  width: 90px;
  flex-shrink: 0;
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-secondary);
}
.exteval-num {
  width: 32px;
  flex-shrink: 0;
  text-align: right;
  font-size: 13px;
  font-weight: 700;
  color: var(--color-text-primary);
}
.exteval-analysis {
  font-size: 13px;
  color: var(--color-text-primary);
  line-height: 1.7;
}
.exteval-skills {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 2px;
  margin-top: 10px;
}
.exteval-tag-label {
  font-size: 12px;
  font-weight: 600;
  margin-right: 6px;
  color: var(--color-text-secondary);
}
</style>
