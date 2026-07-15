<template>
  <div class="page-wrapper">
    <h3 class="page-title">智能简历优化</h3>
    <p class="page-sub">上传或选择简历、目标岗位，AI 将评估匹配度并一键生成优化后的可下载新简历</p>

    <!-- AI 模型配置（可折叠） -->
    <div class="section-card no-print" style="margin-bottom:12px;padding:12px 16px">
      <div style="display:flex;align-items:center;justify-content:space-between;cursor:pointer" @click="aiConfigVisible = !aiConfigVisible">
        <span style="font-weight:600;font-size:13px">
          🤖 AI 模型配置 <span v-if="aiConfigured" style="color:#5D9B71;font-weight:400">已配置</span>
          <span v-else style="color:#D4943F;font-weight:400">未配置（将使用规则引擎）</span>
        </span>
        <el-icon :class="{ 'is-open': aiConfigVisible }" style="transition:transform .2s"><ArrowDown /></el-icon>
      </div>
      <div v-show="aiConfigVisible" style="margin-top:10px;display:flex;flex-direction:column;gap:8px">
        <el-input v-model="aiConfig.apiKey" placeholder="API Key（如：sk-xxx）" size="small" show-password />
        <div style="display:flex;gap:8px">
          <el-input v-model="aiConfig.baseUrl" placeholder="API 地址（默认 https://api.deepseek.com）" size="small" style="flex:2" />
          <el-input v-model="aiConfig.model" placeholder="模型名（默认 deepseek-chat）" size="small" style="flex:1" />
        </div>
        <div style="display:flex;gap:6px;align-items:center">
          <el-button size="small" type="primary" @click="saveAiConfig">保存配置</el-button>
          <el-button size="small" @click="testAiConnection">测试连接</el-button>
          <span v-if="aiTestResult" :style="{color: aiTestResult.ok ? '#5D9B71' : '#DF5A3F', fontSize:'12px'}">{{ aiTestResult.msg }}</span>
        </div>
      </div>
    </div>

    <!-- 选择区 -->
    <div class="section-card no-print">
      <p v-if="!hasResume" style="color:#E53E3E;font-size:13px;margin-bottom:12px">
        ⚠️ 你还没有已完成解析的简历，请先到「我的简历」上传并等待解析完成。
      </p>
      <el-row :gutter="16" style="margin-bottom:8px">
        <el-col :span="8">
          <el-select v-model="selectedResume" placeholder="选择已有简历" style="width:100%" @change="onResumeChange">
            <el-option v-for="r in resumes" :key="r.id" :label="r.fileName" :value="r.id" />
          </el-select>
        </el-col>
        <el-col :span="8">
          <el-select v-model="selectedJob" placeholder="选择目标岗位" style="width:100%" filterable clearable>
            <el-option-group label="🏢 后端岗位（系统发布）" v-if="backendJobs.length">
              <el-option v-for="j in backendJobs" :key="'b'+j.id" :label="`${j.title} · ${j.company}`" :value="j.id" />
            </el-option-group>
            <el-option-group label="🌐 外部岗位（猎聘/Boss/拉勾/智联/51job/牛客）" v-if="externalJobs.length">
              <el-option v-for="j in externalJobs" :key="'e'+j.id" :label="`${j.title} · ${j.company}  [${j.source || '外源'}]`" :value="j.id" />
            </el-option-group>
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" :loading="optimizing" :disabled="!hasResume || !selectedJob" @click="handleOptimize">
            <el-icon style="margin-right:4px"><MagicStick /></el-icon>开始评估
          </el-button>
        </el-col>
      </el-row>

      <div v-if="currentResume" class="resume-hint">
        当前简历技能：<el-tag v-for="s in resumeSkills" :key="s" size="small" style="margin:2px">{{ s }}</el-tag>
        <span v-if="!resumeSkills.length" style="color:var(--color-text-muted)">暂无技能标签 · 可到「个人中心」补充</span>
      </div>
    </div>

    <!-- 评估结果区 -->
    <div v-if="result" class="opt-result">
      <!-- 综合 + 维度 -->
      <div class="overall-card">
        <div class="overall-ring">
          <svg viewBox="0 0 120 120" width="120" height="120">
            <circle cx="60" cy="60" r="54" fill="none" stroke="#F0EEEA" stroke-width="8" />
            <circle cx="60" cy="60" r="54" fill="none" :stroke="scoreColor(result.overall)" stroke-width="8"
              :stroke-dasharray="`${circ * result.overall / 100} ${circ}`" stroke-linecap="round" transform="rotate(-90,60,60)" />
          </svg>
          <div class="overall-number">{{ result.overall }}</div>
        </div>
        <div class="overall-info">
          <div class="overall-label">综合匹配度</div>
          <div class="overall-sub" :style="{ color: scoreColor(result.overall) }">{{ scoreLabel(result.overall) }}</div>
          <div class="overall-comment">{{ result.comment }}</div>
        </div>
        <div class="overall-actions">
          <el-button type="primary" @click="handleAutoImprove" :loading="improving" :disabled="!resumeContent" style="width:220px">
            <el-icon style="margin-right:4px"><MagicStick /></el-icon>AI 一键优化
          </el-button>
          <el-button @click="copyAll" style="width:220px">
            <el-icon style="margin-right:4px"><CopyDocument /></el-icon>复制全部建议
          </el-button>
        </div>
      </div>

      <el-row :gutter="16" style="margin-top:16px">
        <el-col :span="8" v-for="dim in dimensions" :key="dim.key">
          <div class="dim-card">
            <div class="dim-header"><span class="dim-icon">{{ dim.icon }}</span><span class="dim-label">{{ dim.label }}</span></div>
            <div class="dim-number" :style="{ color: scoreColor(result[dim.key]) }">{{ result[dim.key] }}<span class="dim-unit">分</span></div>
            <el-progress :percentage="result[dim.key]" :color="scoreColor(result[dim.key])" :stroke-width="8" />
          </div>
        </el-col>
      </el-row>

      <!-- ===== AI 优化后预览 + 下载 ===== -->
      <div v-if="improvedContent" class="section-card improved-section" style="margin-top:16px">
        <div class="blk-title">✨ 优化后简历预览</div>
        <p class="blk-sub">以下是基于 AI 建议自动应用改写后的完整简历内容，可下载为 HTML / 打印为 PDF</p>

        <!-- 改写摘要 -->
        <div v-if="changes.length" class="change-summary">
          <div class="change-summary-title">📝 本次优化改动了 {{ changes.length }} 处：</div>
          <div class="change-pills">
            <span v-for="(c, i) in changes" :key="i" class="change-pill">
              <span class="change-pill-icon">{{ sectionIcon(c.section) }}</span>
              <span class="change-pill-text">{{ c.section }}</span>
              <span class="change-pill-action">{{ c.action }}</span>
            </span>
          </div>
        </div>

        <div class="improve-row">
          <!-- 左：预览 -->
          <div class="pv-paper">
            <div class="resume-doc tpl-modern" :style="{ '--t-accent': '#4A90B3' }">
              <header class="rd-head">
                <div class="rd-name">{{ improvedContent.name || '你的姓名' }}</div>
                <div class="rd-title">{{ improvedContent.title || (currentJob?.title || '求职意向') }}</div>
                <div class="rd-contact" v-if="improvedContent.phone || improvedContent.email || improvedContent.location">
                  {{ [improvedContent.phone, improvedContent.email, improvedContent.location].filter(Boolean).join(' · ') }}
                </div>
              </header>

              <section v-if="improvedContent.summary" class="rd-sec">
                <h3>个人简介</h3>
                <p class="rd-summary">{{ improvedContent.summary }}</p>
              </section>

              <section v-if="improvedContent.educationList?.filter(e => e.school).length" class="rd-sec">
                <h3>教育背景</h3>
                <div v-for="(e, i) in improvedContent.educationList.filter(x => x.school)" :key="'pe' + i" class="rd-item">
                  <div class="rd-item-h"><span>{{ e.school }}<em v-if="e.major"> · {{ e.major }}</em></span><span class="rd-mut">{{ e.period }}</span></div>
                  <div class="rd-mut" v-if="e.degree">{{ e.degree }}</div>
                </div>
              </section>

              <section v-if="improvedContent.workList?.filter(w => w.company).length" class="rd-sec">
                <h3>工作经历</h3>
                <div v-for="(w, i) in improvedContent.workList.filter(x => x.company)" :key="'pw' + i" class="rd-item">
                  <div class="rd-item-h"><span>{{ w.company }}<em v-if="w.title"> · {{ w.title }}</em></span><span class="rd-mut">{{ w.duration }}</span></div>
                  <div class="rd-item-d" v-if="w.description">{{ w.description }}</div>
                </div>
              </section>

              <section v-if="improvedContent.projectList?.filter(p => p.name).length" class="rd-sec">
                <h3>项目经历</h3>
                <div v-for="(p, i) in improvedContent.projectList.filter(x => x.name)" :key="'pp' + i" class="rd-item">
                  <div class="rd-item-h"><span>{{ p.name }}</span></div>
                  <div class="rd-item-d" v-if="p.description">{{ p.description }}</div>
                </div>
              </section>

              <section v-if="improvedContent.skillList?.length" class="rd-sec">
                <h3>专业技能</h3>
                <div class="rd-skills">
                  <span v-for="(s, i) in improvedContent.skillList" :key="'ps' + i" class="rd-skill">{{ s }}</span>
                </div>
              </section>
            </div>
          </div>

          <!-- 右：下载操作 -->
          <div class="download-panel">
            <div class="dp-title">📥 下载优化后的简历</div>
            <div class="dp-sub">将优化后的内容生成为独立文件，可直接用于投递</div>
            <div class="dp-actions">
              <el-button type="primary" size="default" @click="downloadHtml" style="width:100%">
                <el-icon style="margin-right:6px"><Download /></el-icon>下载 HTML 简历
              </el-button>
              <el-button type="success" size="default" @click="downloadDocx" style="width:100%">
                <el-icon style="margin-right:6px"><Document /></el-icon>下载 Word 简历
              </el-button>
              <el-button size="default" @click="printResume" style="width:100%">
                <el-icon style="margin-right:6px"><Printer /></el-icon>打印 / 导出 PDF
              </el-button>
            </div>
            <div class="dp-tips">
              <div class="tip-title">💡 使用提示</div>
              <ul>
                <li>HTML 文件可用浏览器打开，<b>Ctrl+P 打印为 PDF</b></li>
                <li>下载后可继续在「简历模板」中切换其他风格</li>
                <li>本优化基于岗位 JD 关键词改写，建议人工微调后投递</li>
              </ul>
            </div>
            <el-button size="small" type="success" plain @click="applyImprovedToResume" style="width:100%;margin-top:8px" v-if="selectedResume">
              <el-icon style="margin-right:4px"><Check /></el-icon>同步到我的简历
            </el-button>
          </div>
        </div>
      </div>

      <!-- ===== 内容分析与润色建议 ===== -->
      <div v-if="contentSuggestions.length" class="section-card" style="margin-top:16px">
        <div class="blk-title">📝 简历内容润色建议（逐段分析）</div>
        <p class="blk-sub">以下是对你简历各板块具体内容的文本级分析，含当前问题与改写示例</p>
        <div class="content-sug-list">
          <div v-for="(cs, i) in contentSuggestions" :key="'cs'+i" class="content-sug-card" :class="'lv-' + cs.level">
            <div class="cs-header">
              <span class="cs-section-icon">{{ sectionIcon(cs.section) }}</span>
              <span class="cs-section">{{ cs.section }}</span>
              <el-tag size="small" :type="cs.level === 'high' ? 'danger' : cs.level === 'mid' ? 'warning' : 'success'" class="cs-badge">
                {{ levelLabel(cs.level) }}
              </el-tag>
            </div>
            <div class="cs-issue">{{ cs.issue }}</div>
            <div v-if="cs.current" class="cs-block cs-current">
              <div class="cs-block-label">📎 目前这样：</div>
              <div class="cs-block-text">{{ cs.current }}</div>
            </div>
            <div v-if="cs.example" class="cs-block cs-example-block">
              <div class="cs-block-label">✏️ 建议改成这样：</div>
              <div class="cs-block-text cs-improved">{{ cs.example }}</div>
            </div>
            <div class="cs-actions">
              <el-button size="small" @click="copyText(cs.example, cs.section)">📋 复制示例</el-button>
              <el-button size="small" type="primary" link @click="goEdit">→ 去简历编辑页修改</el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 优化建议（gap-based） -->
      <div class="section-card" style="margin-top:16px">
        <div class="blk-title">💡 技能与维度建议（按优先级）</div>
        <div class="sug-list">
          <div v-for="(s, i) in suggestions" :key="i" class="sug-card" :class="'lv-' + s.level">
            <span class="sug-icon">{{ sugIcon(s.type) }}</span>
            <div class="sug-body">
              <div class="sug-head">
                <span class="sug-tag">{{ sugTypeLabel(s.type) }}</span>
                <span class="sug-level" :class="'lv-' + s.level">{{ levelLabel(s.level) }}</span>
              </div>
              <div class="sug-text">{{ s.text }}</div>
            </div>
          </div>
        </div>
        <div v-if="!suggestions.length" class="empty-tags">暂无建议，匹配度已经很优秀 🎉</div>
      </div>

      <!-- 优势 / 待提升 -->
      <el-row :gutter="16" style="margin-top:16px">
        <el-col :span="12">
          <div class="strength-card">
            <div class="tag-header"><span class="tag-icon">✓</span> AI 识别的优势</div>
            <div v-if="strengthsList.length">
              <el-tag v-for="(s, i) in strengthsList" :key="i" style="margin:4px;background:var(--color-accent-bg);border-color:transparent;color:var(--color-accent)">{{ s }}</el-tag>
            </div>
            <div v-else class="empty-tags">暂无数据</div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="gap-card">
            <div class="tag-header"><span class="tag-icon">△</span> 待提升项</div>
            <div v-if="gapsList.length">
              <el-tag v-for="(g, i) in gapsList" :key="i" style="margin:4px;background:#FFF5F5;border-color:transparent;color:#E53E3E">{{ g }}</el-tag>
            </div>
            <div v-else class="empty-tags">暂无数据</div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyResumes, getResume } from '@/api/resume'
import { getAvailableJobs } from '@/api/job'
import { evaluate } from '@/api/evaluate'
import { updateResumeData } from '@/api/profile'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MagicStick, Edit, CopyDocument, Download, Printer, Check, Document } from '@element-plus/icons-vue'

const router = useRouter()
const resumes = ref([])
const backendJobs = ref([])
const externalJobs = ref([])
const jobs = computed(() => [...backendJobs.value, ...externalJobs.value])
const selectedResume = ref('')
const selectedJob = ref('')
const optimizing = ref(false)
const result = ref(null)
const currentResume = ref(null)
const currentJob = computed(() => jobs.value.find(j => j.id === selectedJob.value))
const resumeSkills = ref([])
const resumeContent = ref(null)

const improvedContent = ref(null)
const changes = ref([])
const improving = ref(false)

const hasResume = computed(() => resumes.value.length > 0)
const circ = 2 * Math.PI * 54

const dimensions = [
  { key: 'skillMatch', label: '技能匹配', icon: '⚙' },
  { key: 'experienceMatch', label: '经验匹配', icon: '📅' },
  { key: 'educationMatch', label: '学历匹配', icon: '🎓' }
]

const strengthsList = computed(() => parseArray(result.value?.strengths))
const gapsList = computed(() => parseArray(result.value?.gaps))
const suggestions = ref([])
const contentSuggestions = ref([])

onMounted(async () => {
  const [r, j] = await Promise.all([getMyResumes(), getAvailableJobs()])
  resumes.value = (r.data || []).filter(i => i.status === 2)
  backendJobs.value = j.data || []
  // 拉取外源岗位（job-service 3001）
  try {
    const extRes = await fetch('http://localhost:3001/api/jobs?limit=200')
    const extData = await extRes.json()
    externalJobs.value = (extData.jobs || []).map(ex => ({ ...ex, _ext: true }))
  } catch {
    // 外源服务不可用时静默降级
    externalJobs.value = []
  }
  if (resumes.value.length) {
    selectedResume.value = resumes.value[0].id
    await onResumeChange(selectedResume.value)
  }
})

async function onResumeChange(id) {
  if (!id) return
  try {
    const res = await getResume(id)
    currentResume.value = res.data
    resumeSkills.value = (res.data?.skills || '').split(',').map(s => s.trim()).filter(Boolean)
    try { resumeContent.value = JSON.parse(res.data?.parsedJson || '{}') } catch { resumeContent.value = {} }
  } catch {
    currentResume.value = null
    resumeSkills.value = []
    resumeContent.value = null
  }
  // 切换简历时重置优化结果
  improvedContent.value = null
  changes.value = []
  result.value = null
}

async function handleOptimize() {
  if (!selectedResume.value || !selectedJob.value) {
    ElMessage.warning('请先选择简历和目标岗位')
    return
  }
  optimizing.value = true
  result.value = null
  suggestions.value = []
  contentSuggestions.value = []
  try {
    // 判断是否为外源岗位
    const selJob = jobs.value.find(j => j.id === selectedJob.value)
    if (selJob && selJob._ext) {
      // 外源岗位本地评估
      const r = currentResume.value
      if (!r) { ElMessage.warning('请先加载简历'); optimizing.value = false; return }
      const jobSkills = parseJobTags(selJob.requirementTags).skills.map(s => (''+s).toLowerCase().trim()).filter(Boolean)
      const resumeSkillsList = (r.skills || '').split(',').map(s => s.trim().toLowerCase()).filter(Boolean)
      const matched = jobSkills.filter(s => resumeSkillsList.includes(s))
      const missing = jobSkills.filter(s => !resumeSkillsList.includes(s))
      let skillScore = jobSkills.length > 0 ? Math.round((matched.length / jobSkills.length) * 70) : 0
      let expScore = (r.yearsOfExperience != null && r.yearsOfExperience > 0) ? 10 : 0
      let profileScore = (r.abilityProfile && r.abilityProfile.trim()) ? 10 : 0
      let overall = 10 + skillScore + expScore + profileScore
      if (overall > 99) overall = 99
      if (overall < 1) overall = 1
      const localResult = {
        overall,
        skillMatch: skillScore,
        experienceMatch: expScore * 10,
        educationMatch: 50,
        gaps: missing,
        strengths: matched,
        title: selJob.title,
        company: selJob.company,
        _ext: true
      }
      result.value = localResult
      buildSuggestions(localResult)
      ElMessage.success('✅ 本地评估完成，综合分：' + overall)
    } else {
      // 后端岗位走标准 API
      const res = await evaluate(selectedResume.value, selectedJob.value)
      result.value = res.data
      buildSuggestions(res.data)
      ElMessage.success('✅ 评估完成，综合分：' + res.data.overall)
    }
  } catch (e) {
    ElMessage.error('评估失败：' + (e?.response?.data?.message || e.message || '请确认简历已解析且岗位在线'))
  } finally {
    optimizing.value = false
  }
}

// ── AI 模型配置 ──
const aiConfigVisible = ref(false)
const aiTestResult = ref(null)
const aiConfig = ref({
  apiKey: localStorage.getItem('ai_api_key') || '',
  baseUrl: localStorage.getItem('ai_base_url') || 'https://api.deepseek.com',
  model: localStorage.getItem('ai_model') || 'deepseek-chat'
})
const aiConfigured = computed(() => !!aiConfig.value.apiKey)

function saveAiConfig() {
  localStorage.setItem('ai_api_key', aiConfig.value.apiKey)
  localStorage.setItem('ai_base_url', aiConfig.value.baseUrl)
  localStorage.setItem('ai_model', aiConfig.value.model)
  ElMessage.success('AI 配置已保存')
}

async function testAiConnection() {
  aiTestResult.value = null
  if (!aiConfig.value.apiKey) { aiTestResult.value = { ok: false, msg: '请先填写 API Key' }; return }
  try {
    const res = await fetch((aiConfig.value.baseUrl || 'https://api.deepseek.com') + '/chat/completions', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + aiConfig.value.apiKey },
      body: JSON.stringify({ model: aiConfig.value.model || 'deepseek-chat', messages: [{ role: 'user', content: '回复"OK"即可' }], max_tokens: 10 })
    })
    if (!res.ok) { const e = await res.json().catch(()=>({})); aiTestResult.value = { ok: false, msg: '失败：' + (e.error?.message || res.status) } }
    else aiTestResult.value = { ok: true, msg: '✅ 连接成功' }
  } catch (e) { aiTestResult.value = { ok: false, msg: '连接失败：' + e.message } }
}

// 调用 AI API（OpenAI 兼容）
async function callAi(prompt, system) {
  const url = (aiConfig.value.baseUrl || 'https://api.deepseek.com') + '/chat/completions'
  const key = aiConfig.value.apiKey
  const model = aiConfig.value.model || 'deepseek-chat'
  const res = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + key },
    body: JSON.stringify({
      model,
      messages: [{ role: 'system', content: system || '你是一位专业的简历优化助手，帮助求职者优化简历以匹配目标岗位。' }, { role: 'user', content: prompt }],
      temperature: 0.7,
      max_tokens: 4096
    })
  })
  const data = await res.json()
  if (!res.ok) throw new Error(data.error?.message || 'API 调用失败')
  return data.choices?.[0]?.message?.content || ''
}

// ── AI 一键优化（模型优先，规则引擎兜底）──
async function handleAutoImprove() {
  if (!resumeContent.value) {
    ElMessage.warning('请先选择简历')
    return
  }
  if (!currentJob.value) {
    ElMessage.warning('请先选择目标岗位')
    return
  }
  improving.value = true
  improvedContent.value = null
  changes.value = []

  try {
    if (aiConfigured.value) {
      // 有 AI 配置 → 调用模型优化
      const job = currentJob.value
      const resume = JSON.stringify(resumeContent.value, null, 2)
      const jobSkills = job.requirementTags || ''
      const prompt = `目标岗位：${job.title}（${job.company}）\n岗位技能要求：${jobSkills}\n岗位描述：${(job.description || '').substring(0,500)}\n\n当前简历内容（JSON）：\n${resume}\n\n请直接返回优化后的完整简历 JSON（保持原数据结构），并附上 3-5 条具体的优化说明（用 /* 优化说明 */ 包裹在 JSON 开头或先用文字说明再返回 JSON）。重点：1) 针对岗位技能匹配缺失项补充；2) 工作/项目经历描述更量化、更贴近岗位JD；3) 若无对应经历可合理占位。`
      const system = '你是一位专业的简历优化助手。请分析目标岗位JD与用户简历的匹配情况，优化简历内容使其更契合该岗位。直接返回JSON格式的优化后简历，保持原有数据字段结构不变。'
      const raw = await callAi(prompt, system)
      // 解析返回的 JSON
      let jsonStr = raw
      const codeMatch = raw.match(/```(?:json)?\s*([\s\S]*?)```/)
      if (codeMatch) jsonStr = codeMatch[1]
      const firstBrace = jsonStr.indexOf('{')
      const lastBrace = jsonStr.lastIndexOf('}')
      if (firstBrace >= 0 && lastBrace > firstBrace) jsonStr = jsonStr.slice(firstBrace, lastBrace + 1)
      const improved = JSON.parse(jsonStr)
      improvedContent.value = improved
      // 提取优化说明
      const notes = raw.replace(/```[\s\S]*?```/g, '').trim()
      changes.value = notes ? [{ section: 'AI 优化摘要', action: notes.substring(0, 500) }] : [{ section: '优化完成', action: 'AI 已根据岗位要求优化简历' }]
      ElMessage.success('✅ AI 优化完成')
    } else {
      // 无 AI 配置 → 规则引擎
      await new Promise(r => setTimeout(r, 600))
      const { improved, changeList } = autoImproveContent(
        JSON.parse(JSON.stringify(resumeContent.value)),
        currentJob.value,
        resumeSkills.value
      )
      improvedContent.value = improved
      changes.value = changeList
      if (changeList.length) {
        ElMessage.success(`✨ 已应用 ${changeList.length} 项优化，可下载新简历`)
      } else {
        ElMessage.info('简历已很完善，无需改动')
      }
    }
    // 滚到预览区
    setTimeout(() => {
      const el = document.querySelector('.improved-section')
      if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
    }, 200)
  } catch (e) {
    ElMessage.error('优化失败：' + (e.message || '未知错误'))
  } finally {
    improving.value = false
  }
}

// 核心：自动应用建议改写文本
function autoImproveContent(content, job, skills) {
  const c = content
  const changeList = []
  const jobSkills = (job?.requirementTags || '').split(',')
    .map(s => s.trim())
    .filter(s => s && !s.includes(':'))

  // 1. 个人简介
  const oldSummary = c.summary || ''
  if (!oldSummary || oldSummary.length < 30) {
    const years = c.yearsOfExperience || c.yearsOfExperienceNum || '3+'
    const topSkills = (c.skillList || skills || []).slice(0, 3).join('、') || '互联网产品研发'
    c.summary = `${years} 年互联网行业经验，专注 ${topSkills} 领域。主导过核心业务系统从 0 到 1 的架构设计与上线，具备扎实的工程化能力、性能调优经验与跨团队协作能力。在过往工作中持续推动技术演进与团队效率提升，善于将复杂业务问题拆解为可落地的技术方案。`
    changeList.push({ section: '个人简介', action: oldSummary ? '润色扩充' : '新增内容' })
  } else if (oldSummary.length < 80 && !/\d+%|\d+万|\d+倍|\d+ 年/.test(oldSummary)) {
    c.summary = oldSummary + ` 在过往项目中持续推动技术演进，主导过 3+ 个核心项目上线，系统性能平均提升 30%+，并建立团队工程化规范。`
    changeList.push({ section: '个人简介', action: '补充量化数据' })
  }

  // 2. 工作经历 - 规范化字段并扩充短描述
  const workList = c.workList || c.work || []
  if (Array.isArray(workList) && workList.length) {
    let workChanged = 0
    c.workList = workList.map(w => {
      const desc = w.description || w.desc || ''
      if (!desc || desc.length < 20) {
        const company = w.company || '相关公司'
        const title = w.title || '核心开发'
        w.description = `担任${title}，负责${company}核心业务系统的研发与维护。深入理解业务需求并设计高效的技术方案，通过持续的技术优化与代码重构，系统稳定性提升至 99.9%+，研发效率提高 30%。`
        workChanged++
      } else if (desc.length < 50 && !/\d+%|\d+万|\d+倍|\d+人/.test(desc)) {
        w.description = desc + ` 通过技术优化与团队协作，将核心指标提升 30%+，并建立了完整的工程化体系。`
        workChanged++
      }
      return w
    })
    if (workChanged) changeList.push({ section: '工作经历', action: `${workChanged} 段扩写` })
  } else if (!workList.length) {
    // 完全没有工作经历 - 新增占位
    c.workList = [{
      company: '某某科技有限公司',
      title: '核心研发工程师',
      duration: c.yearsOfExperienceNum ? `${new Date().getFullYear() - parseInt(c.yearsOfExperienceNum)} - 至今` : '近期',
      description: '负责核心业务系统的前后端研发，深入理解业务需求并设计高效的技术方案。通过持续的技术优化与代码重构，系统稳定性提升至 99.9%+，研发效率提高 30%。'
    }]
    changeList.push({ section: '工作经历', action: '新增占位' })
  }

  // 3. 项目经历
  const projList = c.projectList || c.projects || []
  if (!Array.isArray(projList) || projList.length === 0) {
    c.projectList = [{
      name: '核心业务平台',
      description: '主导核心业务平台从 0 到 1 的架构设计与实现，支撑日活 XX 万+用户访问，QPS 峰值 XX 万。引入微前端/微服务架构，团队开发效率提升 40%，首屏性能提升 50%+。'
    }]
    changeList.push({ section: '项目经历', action: '新增占位' })
  } else {
    let projChanged = 0
    c.projectList = projList.map(p => {
      if (!p.description || p.description.length < 20) {
        p.description = `${p.name || '该项目'}：负责核心模块的设计与实现，主导技术方案选型与落地。引入 XX 工具/框架，将研发效率提升 30%+，并完成 XX 关键指标的优化。`
        projChanged++
      }
      return p
    })
    if (projChanged) changeList.push({ section: '项目经历', action: `${projChanged} 个项目扩写` })
  }

  // 4. 技能
  const skillList = c.skillList || c.skills || []
  const oldSkillCount = skillList.length
  if (skillList.length < 5) {
    const newSkills = jobSkills
      .filter(s => !skillList.includes(s))
      .slice(0, Math.max(0, 6 - skillList.length))
    c.skillList = [...skillList, ...newSkills]
    if (c.skillList.length > oldSkillCount) {
      changeList.push({ section: '技能标签', action: `新增 ${c.skillList.length - oldSkillCount} 项（来自岗位）` })
    }
  }

  // 5. 教育背景（无则占位）
  const eduList = c.educationList || c.education || []
  if (!Array.isArray(eduList) || eduList.length === 0) {
    c.educationList = [{
      school: '某某大学',
      major: '计算机科学与技术',
      degree: '本科',
      period: ''
    }]
    changeList.push({ section: '教育背景', action: '新增占位（请手动补充）' })
  }

  return { improved: c, changeList }
}

// ── 下载新简历 ──
function buildOptimizedHtml() {
  const c = improvedContent.value
  if (!c) return ''
  const edu = (c.educationList || []).filter(e => e.school).map(e =>
    `<div class="it"><div class="ih"><span>${esc(e.school)}${e.major ? ' · ' + esc(e.major) : ''}</span><span class="mut">${esc(e.period)}</span></div>${e.degree ? `<div class="mut">${esc(e.degree)}</div>` : ''}</div>`).join('')
  const work = (c.workList || []).filter(w => w.company).map(w =>
    `<div class="it"><div class="ih"><span>${esc(w.company)}${w.title ? ' · ' + esc(w.title) : ''}</span><span class="mut">${esc(w.duration)}</span></div>${w.description ? `<div class="id">${esc(w.description)}</div>` : ''}</div>`).join('')
  const proj = (c.projectList || []).filter(p => p.name).map(p =>
    `<div class="it"><div class="ih"><span>${esc(p.name)}</span></div>${p.description ? `<div class="id">${esc(p.description)}</div>` : ''}</div>`).join('')
  const skills = (c.skillList || []).map(s => `<span class="sk">${esc(s)}</span>`).join('')
  const contact = [c.phone, c.email, c.location].filter(Boolean).map(esc).join(' · ')

  const css = `
    *{box-sizing:border-box;margin:0;padding:0}
    body{font-family:-apple-system,'Segoe UI','PingFang SC','Microsoft YaHei',sans-serif;color:#243B4A;background:#fff;padding:40px;line-height:1.7}
    .rd{max-width:780px;margin:0 auto;--t-accent:#4A90B3}
    .rd-head{border-bottom:1px solid #ccc;padding-bottom:14px;margin-bottom:20px}
    .rd-name{font-size:30px;font-weight:800;color:var(--t-accent)}
    .rd-title{font-size:16px;color:#444;margin-top:4px;font-weight:600}
    .rd-contact{font-size:13px;color:#777;margin-top:6px}
    .rd-sec{margin-bottom:20px}
    .rd-sec h3{font-size:15px;color:var(--t-accent);border-left:4px solid var(--t-accent);padding-left:8px;margin-bottom:12px}
    .rd-summary{font-size:14px;color:#444}
    .it{margin-bottom:14px}
    .ih{display:flex;justify-content:space-between;font-weight:600;font-size:14px}
    .ih em{font-style:normal;font-weight:400;color:#555}
    .mut{color:#888;font-size:12px;font-weight:400}
    .id{font-size:13px;color:#555;margin-top:4px;line-height:1.7}
    .rd-skills{display:flex;flex-wrap:wrap;gap:8px}
    .sk{background:var(--t-accent);color:#fff;font-size:12px;padding:4px 12px;border-radius:14px}
    .ai-badge{display:inline-block;background:linear-gradient(135deg,#5D9B71,#4A90B3);color:#fff;font-size:10px;padding:2px 8px;border-radius:8px;margin-left:8px;vertical-align:middle}
  `
  return `<!DOCTYPE html><html lang="zh-CN"><head><meta charset="utf-8"><title>${esc(c.name)}的简历（AI优化版）</title><style>${css}</style></head><body>
  <div class="rd">
    <header class="rd-head">
      <div class="rd-name">${esc(c.name) || '你的姓名'}<span class="ai-badge">AI 优化</span></div>
      <div class="rd-title">${esc(c.title) || (currentJob.value?.title || '求职意向')}</div>
      ${contact ? `<div class="rd-contact">${contact}</div>` : ''}
    </header>
    ${c.summary ? `<section class="rd-sec"><h3>个人简介</h3><p class="rd-summary">${esc(c.summary)}</p></section>` : ''}
    ${edu ? `<section class="rd-sec"><h3>教育背景</h3>${edu}</section>` : ''}
    ${work ? `<section class="rd-sec"><h3>工作经历</h3>${work}</section>` : ''}
    ${proj ? `<section class="rd-sec"><h3>项目经历</h3>${proj}</section>` : ''}
    ${skills ? `<section class="rd-sec"><h3>专业技能</h3><div class="rd-skills">${skills}</div></section>` : ''}
  </div></body></html>`
}

function downloadHtml() {
  if (!improvedContent.value) { ElMessage.warning('请先点击 AI 一键优化'); return }
  const html = buildOptimizedHtml()
  const blob = new Blob([html], { type: 'text/html;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `简历-AI优化版-${improvedContent.value.name || 'template'}.html`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
  ElMessage.success('已下载 AI 优化版简历')
}

async function downloadDocx() {
  if (!improvedContent.value) { ElMessage.warning('请先点击 AI 一键优化'); return }
  try {
    const { buildOptimizedDocxBlob } = await import('@/utils/docxBuilder')
    const blob = await buildOptimizedDocxBlob(improvedContent.value, currentJob.value?.title)
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `简历-AI优化版-${improvedContent.value.name || 'template'}.docx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    ElMessage.success('已下载 Word 简历（可用 Microsoft Word / WPS 打开编辑）')
  } catch (e) {
    ElMessage.error('Word 生成失败：' + e.message)
  }
}

function printResume() {
  if (!improvedContent.value) { ElMessage.warning('请先点击 AI 一键优化'); return }
  // 打开新窗口打印
  const html = buildOptimizedHtml()
  const w = window.open('', '_blank')
  if (w) {
    w.document.write(html)
    w.document.close()
    setTimeout(() => w.print(), 500)
  } else {
    ElMessage.warning('请允许弹窗以使用打印功能')
  }
}

// 同步到我的简历
async function applyImprovedToResume() {
  if (!improvedContent.value || !selectedResume.value) return
  try {
    await ElMessageBox.confirm('将优化后的内容写回你的简历（会覆盖当前内容），是否继续？', '同步确认', { type: 'warning' })
  } catch { return }
  try {
    const c = improvedContent.value
    await updateResumeData(selectedResume.value, {
      parsedJson: JSON.stringify({
        name: c.name,
        yearsOfExperience: c.yearsOfExperienceNum || c.yearsOfExperience || '',
        education: c.educationList || [],
        work: (c.workList || []).map(w => ({ company: w.company, title: w.title, duration: w.duration, description: w.description })),
        projects: c.projectList || [],
        skills: c.skillList || [],
        summary: c.summary
      }),
      abilityProfile: c.summary,
      skills: (c.skillList || []).join(','),
      yearsOfExperience: c.yearsOfExperienceNum || 0
    })
    ElMessage.success('✅ 已同步到「个人中心 → 简历编辑」')
  } catch (e) {
    ElMessage.error('同步失败：' + (e?.response?.data?.msg || e.message))
  }
}

function esc(s) { return (s || '').toString().replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;') }

// ── 简历内容分析（原有） ──
function analyzeResumeContent() {
  const list = []
  const rc = resumeContent.value || {}

  // 个人简介
  const summary = (rc.summary || '').trim()
  if (!summary) {
    list.push({ level: 'high', section: '个人简介', issue: '缺少自我评价 / 个人简介。', current: null, example: '示例：「8 年互联网行业后端与全栈开发经验，3 年团队管理经历，精通 Java/Go 体系与云原生架构，主导过日活千万级电商平台的整体升级。」' })
  } else if (summary.length < 25) {
    list.push({ level: 'high', section: '个人简介', issue: `仅 ${summary.length} 字，过于简短。`, current: summary, example: '扩充至 80-120 字，包含：年限 + 领域 + 技术栈 + 1-2 个量化成果。' })
  } else if (summary.length < 70 && !/\d/.test(summary)) {
    list.push({ level: 'mid', section: '个人简介', issue: '缺少量化数据支撑（数字+指标）。', current: summary, example: '加入：工作年限、项目规模、性能/效率提升百分比。' })
  }

  // 工作经历
  const workList = Array.isArray(rc.workList || rc.work) ? (rc.workList || rc.work) : []
  if (workList.length === 0) {
    list.push({ level: 'high', section: '工作经历', issue: '缺少工作经历板块。', current: null, example: '按时间倒序，使用 STAR 法则：场景 → 任务 → 行动 → 结果。' })
  } else {
    workList.forEach((w, idx) => {
      const comp = (w.company || '').trim() || `第 ${idx + 1} 段`
      const desc = (w.description || w.desc || '').trim()
      if (!desc || desc.length < 15) {
        list.push({ level: 'high', section: `工作经历：${comp}`, issue: desc ? `仅 ${desc.length} 字` : '缺少描述', current: desc || '（空）', example: '采用 STAR 法则 + 量化指标。' })
      } else if (!/\d+/.test(desc)) {
        list.push({ level: 'mid', section: `工作经历：${comp}`, issue: `描述有 ${desc.length} 字但缺数字`, current: desc, example: '加入至少一个量化指标：性能/规模/效率。' })
      }
    })
  }

  // 项目经历
  const projList = Array.isArray(rc.projectList || rc.projects) ? (rc.projectList || rc.projects) : []
  if (projList.length === 0) {
    list.push({ level: 'mid', section: '项目经历', issue: '缺少独立的「项目经历」板块。', current: null, example: '挑 2-3 个技术栈相关的项目，每个含名称、技术栈、职责、成果。' })
  }

  // 技能
  const skillList = Array.isArray(rc.skillList || rc.skills) ? (rc.skillList || rc.skills) : []
  if (skillList.length < 4) {
    list.push({ level: 'mid', section: '技能标签', issue: `仅 ${skillList.length} 个，建议补充到 6-10 个。`, current: skillList.join('、') || '（空）', example: '按类别组织：前端/后端/运维/工具，参考岗位 JD 关键词。' })
  }

  // 教育
  const eduList = Array.isArray(rc.educationList || rc.education) ? (rc.educationList || rc.education) : []
  if (eduList.length === 0) {
    list.push({ level: 'high', section: '教育背景', issue: '缺少教育背景信息。', current: null, example: '学校 · 专业 · 学历 · 时间。' })
  }

  contentSuggestions.value = list
}

function buildSuggestions(ev) {
  const list = []
  parseArray(ev.gaps).forEach(g => {
    list.push({ type: 'skill', level: 'high', text: `补充技能「${g}」：岗位明确要求，建议在简历技能区与项目经历中具体体现该能力。` })
  })
  if (ev.skillMatch < 60) list.push({ type: 'skill', level: 'mid', text: '技能匹配度偏低，建议系统梳理岗位 JD 中的技术栈，并逐一对应到你的项目经验中。' })
  if (ev.experienceMatch < 60) list.push({ type: 'exp', level: 'mid', text: '经验匹配度偏低，建议用数据量化工作业绩（如"性能提升 40%"），并突出与岗位相关的年限。' })
  if (ev.educationMatch < 60) list.push({ type: 'edu', level: 'low', text: '学历匹配度偏低，建议在简历中强化相关项目、认证与培训经历作为补充证明。' })
  if (ev.overall >= 80) list.push({ type: 'good', level: 'low', text: '综合匹配度优秀，重点打磨简历表达与关键词命中即可提升通过率。' })
  if (ev.overall >= 60 && ev.overall < 80 && parseArray(ev.gaps).length === 0) {
    list.push({ type: 'good', level: 'low', text: '核心技能已具备，建议在简历中增加量化成果与业务价值描述。' })
  }
  suggestions.value = list
  analyzeResumeContent()
}

function goEdit() {
  router.push('/profile')
  ElMessage.info('已跳转到个人中心，可在「简历编辑」中按建议修改')
}

async function copyAll() {
  let text = `简历优化建议\n═══\n`
  if (improvedContent.value) {
    text += '\n【✨ AI 一键优化已应用】\n'
    text += `本次优化改动：${changes.value.map(c => c.section).join('、')}\n`
  }
  if (contentSuggestions.value.length) {
    text += '\n【📝 内容润色建议】\n'
    contentSuggestions.value.forEach((cs, i) => {
      text += `\n${i + 1}. [${cs.section}] ${cs.issue}`
      if (cs.example) text += `\n   优化示例：${cs.example}`
    })
  }
  if (suggestions.value.length) {
    text += '\n\n【💡 技能与维度建议】\n'
    suggestions.value.forEach((s, i) => {
      text += `\n${i + 1}.【${sugTypeLabel(s.type)}·${levelLabel(s.level)}】${s.text}`
    })
  }
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('全部建议已复制到剪贴板')
  } catch {
    ElMessage.warning('复制失败，请手动选择文本复制')
  }
}

async function copyText(text, label) {
  if (!text) { ElMessage.info('无可复制内容'); return }
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success(`「${label}」的优化示例已复制`)
  } catch {
    ElMessage.warning('复制失败')
  }
}

function sectionIcon(sec) {
  if (sec.startsWith('个人简介')) return '✍️'
  if (sec.startsWith('工作经历')) return '💼'
  if (sec.startsWith('项目经历')) return '🚀'
  if (sec.startsWith('技能')) return '🏷️'
  if (sec.startsWith('教育')) return '🎓'
  return '📄'
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
function parseArray(val) {
  if (!val) return []
  try { return JSON.parse(val) } catch { return [val] }
}
function sugIcon(t) { return { skill: '⚙', exp: '📅', edu: '🎓', good: '✓' }[t] || '💡' }
function sugTypeLabel(t) { return { skill: '技能补充', exp: '经验表达', edu: '学历补充', good: '锦上添花' }[t] || '建议' }
function levelLabel(l) { return { high: '高优先级', mid: '中优先级', low: '可选' }[l] || l }
// 解析外源岗位 requirementTags（格式：来源:xx,学历:xx,经验:xx,类型:xx,技能1,技能2,…）
function parseJobTags(tags) {
  const out = { source: '', edu: '', exp: '', type: '', skills: [] }
  if (!tags) return out
  tags.split(',').forEach(t => {
    t = t.trim()
    if (t.startsWith('来源:')) out.source = t.slice(3)
    else if (t.startsWith('学历:')) out.edu = t.slice(3)
    else if (t.startsWith('经验:')) out.exp = t.slice(3)
    else if (t.startsWith('类型:')) out.type = t.slice(3)
    else if (t) out.skills.push(t)
  })
  return out
}
</script>

<style scoped>
.page-wrapper { width: 100%; max-width: 1280px; margin: 0 auto; padding: 24px 24px 32px; box-sizing: border-box; }
.page-sub { color: var(--color-text-muted); font-size: 13px; margin: -8px 0 16px; }
.resume-hint { font-size: 12px; color: var(--color-text-muted); margin-top: 4px; }
.blk-title { font-weight: 600; color: var(--color-text-primary); margin-bottom: 6px; }
.blk-sub { font-size: 12px; color: var(--color-text-muted); margin: 0 0 14px; }

/* 综合卡 */
.opt-result { animation: fadeIn 0.3s ease; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }
.overall-card {
  background: var(--color-bg-card);
  border: 1px solid #EDEDF0;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
  box-shadow: 0 1px 3px rgba(43, 43, 49, 0.06);
}
.overall-ring { position: relative; width: 120px; height: 120px; flex-shrink: 0; }
.overall-number { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; font-size: 32px; font-weight: 800; color: var(--color-text-primary); }
.overall-info { flex: 1; min-width: 200px; }
.overall-label { font-size: 18px; font-weight: 700; color: var(--color-text-primary); }
.overall-sub { font-size: 15px; font-weight: 600; margin-top: 4px; }
.overall-comment { font-size: 13px; color: var(--color-text-secondary); margin-top: 8px; line-height: 1.6; }
.overall-actions { display: flex; flex-direction: column; gap: 12px; width: 220px; flex-shrink: 0; align-items: stretch; margin-left: auto; }
.overall-actions .el-button {
  width: 100% !important;
  margin: 0 !important;
}

/* 维度卡 */
.dim-card { background: var(--color-bg-card); border: 1px solid #EDEDF0; border-radius: 12px; padding: 20px; }
.dim-header { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; }
.dim-icon { font-size: 20px; }
.dim-label { font-size: 14px; font-weight: 600; color: var(--color-text-primary); }
.dim-number { font-size: 28px; font-weight: 800; margin-bottom: 10px; }
.dim-unit { font-size: 14px; font-weight: 400; opacity: 0.5; }

/* ===== 优化后预览 ===== */
.improved-section { padding: 20px 24px; }
.change-summary {
  background: linear-gradient(135deg, rgba(93,155,113,0.06), rgba(74,144,179,0.06));
  border: 1px solid rgba(93,155,113,0.20);
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 16px;
}
.change-summary-title { font-size: 13px; font-weight: 600; color: var(--color-text-primary); margin-bottom: 8px; }
.change-pills { display: flex; flex-wrap: wrap; gap: 6px; }
.change-pill {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: #fff;
  border: 1px solid rgba(93,155,113,0.30);
  color: #2D6E4A;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 500;
}
.change-pill-icon { font-size: 12px; }
.change-pill-action { color: #4A90B3; }

.improve-row {
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: 20px;
}
.pv-paper {
  background: #F4F3EF;
  padding: 24px;
  border-radius: 10px;
  display: flex;
  justify-content: center;
}
.resume-doc {
  width: 100%;
  background: #fff;
  padding: 32px 36px;
  border-radius: 6px;
  box-shadow: 0 4px 20px rgba(43, 43, 49, 0.10);
  --t-accent: #4A90B3;
  color: #243B4A;
  font-size: 13.5px;
  line-height: 1.6;
}
.rd-head { border-bottom: 1px solid #ccc; padding-bottom: 12px; margin-bottom: 16px; }
.rd-name { font-size: 26px; font-weight: 800; color: var(--t-accent); }
.rd-title { font-size: 14px; color: #444; margin-top: 4px; font-weight: 600; }
.rd-contact { font-size: 12px; color: #777; margin-top: 5px; }
.rd-sec { margin-bottom: 16px; }
.rd-sec h3 {
  font-size: 14px; color: var(--t-accent);
  border-left: 4px solid var(--t-accent); padding-left: 8px; margin-bottom: 8px;
}
.rd-summary { color: #444; }
.rd-item { margin-bottom: 10px; }
.rd-item-h { display: flex; justify-content: space-between; font-weight: 600; font-size: 13.5px; }
.rd-item-h em { font-style: normal; font-weight: 400; color: #555; }
.rd-mut { color: #888; font-size: 12px; font-weight: 400; }
.rd-item-d { font-size: 12.5px; color: #555; margin-top: 3px; line-height: 1.7; }
.rd-skills { display: flex; flex-wrap: wrap; gap: 6px; }
.rd-skill {
  background: var(--t-accent); color: #fff;
  font-size: 11.5px; padding: 3px 10px; border-radius: 12px;
}
.tpl-modern .rd-head { border-bottom: 1px solid #ccc; }

/* 下载面板 */
.download-panel {
  background: linear-gradient(135deg, #FAF9F7 0%, #F4F1ED 100%);
  border: 1px solid #EDECE9;
  border-radius: 10px;
  padding: 20px;
  height: fit-content;
  position: sticky;
  top: 20px;
}
.dp-title { font-size: 15px; font-weight: 700; color: var(--color-text-primary); margin-bottom: 4px; }
.dp-sub { font-size: 12px; color: var(--color-text-muted); margin-bottom: 16px; line-height: 1.5; }
.dp-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 16px;
  align-items: stretch;
}
.dp-actions .el-button {
  width: 100% !important;
  margin: 0 !important;
}
.dp-tips {
  background: #fff;
  border: 1px solid #EDECE9;
  border-radius: 8px;
  padding: 10px 12px;
  font-size: 12px;
}
.tip-title { font-weight: 600; color: var(--color-text-primary); margin-bottom: 6px; }
.dp-tips ul { margin: 0; padding-left: 18px; color: var(--color-text-secondary); line-height: 1.8; }

/* ===== 内容润色建议 ===== */
.content-sug-list { display: flex; flex-direction: column; gap: 14px; }
.content-sug-card {
  border: 1px solid #EDECE9;
  border-radius: 12px;
  padding: 18px 20px;
  transition: box-shadow 0.15s ease;
}
.content-sug-card:hover { box-shadow: 0 4px 16px rgba(43, 43, 49, 0.08); }
.content-sug-card.lv-high { border-left: 4px solid #DF5A3F; background: #FEF6F4; }
.content-sug-card.lv-mid { border-left: 4px solid #D4943F; background: #FDF8F0; }
.content-sug-card.lv-low { border-left: 4px solid #5D9B71; background: #F4F9F5; }
.cs-header { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; }
.cs-section-icon { font-size: 18px; }
.cs-section { font-size: 15px; font-weight: 700; color: var(--color-text-primary); flex: 1; }
.cs-badge { flex-shrink: 0; }
.cs-issue { font-size: 13px; color: var(--color-text-secondary); line-height: 1.6; margin-bottom: 10px; }
.cs-block { margin-bottom: 8px; border-radius: 8px; overflow: hidden; }
.cs-block-label { font-size: 11px; font-weight: 600; color: var(--color-text-muted); padding: 6px 12px 0; }
.cs-block-text {
  font-size: 13px; padding: 6px 12px 10px;
  line-height: 1.7; white-space: pre-wrap; word-break: break-word;
}
.cs-current .cs-block-text {
  background: #fff; margin: 4px 12px 10px; padding: 10px 12px;
  border-radius: 6px; border: 1px dashed #D8D5CF; color: var(--color-text-secondary);
}
.cs-example-block .cs-block-text {
  background: rgba(93,155,113,0.04); margin: 4px 12px 10px; padding: 10px 12px;
  border-radius: 6px; border: 1px solid rgba(93,155,113,0.25);
}
.cs-improved { color: #2D6E4A; font-weight: 500; }
.cs-actions { display: flex; align-items: center; gap: 8px; margin-top: 6px; padding-left: 2px; }

/* 建议卡 (原有) */
.sug-list { display: flex; flex-direction: column; gap: 12px; }
.sug-card {
  display: flex; gap: 12px; padding: 14px 16px;
  border-radius: 10px; border-left: 4px solid #ccc; background: #FAFAF8;
}
.sug-card.lv-high { border-left-color: #DF5A3F; background: #FEF6F4; }
.sug-card.lv-mid { border-left-color: #D4943F; background: #FDF8F0; }
.sug-card.lv-low { border-left-color: #5D9B71; background: #F4F9F5; }
.sug-icon { font-size: 20px; flex-shrink: 0; }
.sug-body { flex: 1; }
.sug-head { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.sug-tag { font-size: 13px; font-weight: 700; color: var(--color-text-primary); }
.sug-level { font-size: 11px; padding: 1px 8px; border-radius: 8px; }
.sug-level.lv-high { background: #DF5A3F; color: #fff; }
.sug-level.lv-mid { background: #D4943F; color: #fff; }
.sug-level.lv-low { background: #5D9B71; color: #fff; }
.sug-text { font-size: 13px; color: var(--color-text-secondary); line-height: 1.6; }

/* 优势/短板 */
.strength-card, .gap-card { background: var(--color-bg-card); border: 1px solid #EDEDF0; border-radius: 12px; padding: 20px; min-height: 80px; }
.tag-header { font-size: 14px; font-weight: 600; margin-bottom: 12px; color: var(--color-text-primary); }
.tag-icon { display: inline-block; width: 20px; height: 20px; border-radius: 50%; text-align: center; line-height: 20px; font-size: 12px; margin-right: 6px; }
.strength-card .tag-icon { background: var(--color-accent-bg); color: var(--color-accent); }
.gap-card .tag-icon { background: #FFF5F5; color: #E53E3E; }
.empty-tags { color: var(--color-text-muted); font-size: 13px; padding: 8px 0; }

@media (max-width: 992px) {
  .improve-row { grid-template-columns: 1fr; }
  .download-panel { position: static; }
}
</style>

<style>
@media print {
  .app-aside, .app-header { display: none !important; }
  .el-main.app-main { padding: 0 !important; overflow: visible !important; }
  .no-print, .change-summary, .download-panel, .dp-actions, .dp-tips,
  .cs-actions, .overall-actions, .sug-card, .content-sug-card { display: none !important; }
  body { background: #fff !important; }
  .pv-paper { background: #fff !important; padding: 0 !important; }
  .resume-doc { box-shadow: none !important; max-width: 100% !important; }
}
</style>
