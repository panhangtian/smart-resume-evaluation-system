<template>
  <div class="home">
    <!-- ===== Hero 横幅 ===== -->
    <section class="hero">
      <div class="hero-bg"></div>
      <div class="hero-float hero-float-1"></div>
      <div class="hero-float hero-float-2"></div>
      <div class="hero-float hero-float-3"></div>
      <div class="hero-inner">
        <div class="hero-badge">✨ Smart Resume · 智能简历平台</div>
        <h1 class="hero-title">
          你好，<span class="hl">{{ store.nickname || store.role }}</span>
          <br />让每份简历，遇见对的岗位
        </h1>
        <p class="hero-sub">{{ heroSubtitle }}</p>
        <div class="hero-actions">
          <el-button v-for="cta in heroCtAs" :key="cta.to" :type="cta.primary ? 'primary' : 'default'"
            size="large" round @click="go(cta.to)">
            <el-icon v-if="cta.icon" style="margin-right:6px"><component :is="cta.icon" /></el-icon>
            {{ cta.label }}
          </el-button>
        </div>
      </div>
    </section>

    <!-- ===== 数据概览 ===== -->
    <section class="block">
      <div class="section-head">
        <h2 class="block-title">数据概览</h2>
        <span class="block-sub">实时同步你的工作台</span>
      </div>
      <el-row :gutter="20">
        <el-col :span="6" v-for="s in stats" :key="s.label">
          <div class="stat-card home-stat" :style="{ cursor: s.to ? 'pointer' : 'default' }" @click="s.to && go(s.to)">
            <div class="stat-icon" :style="{ background: s.color + '1A', color: s.color }">
              <el-icon><component :is="s.icon" /></el-icon>
            </div>
            <div class="stat-number" :style="{ color: s.color }">{{ s.value }}</div>
            <div class="stat-label">{{ s.label }}</div>
          </div>
        </el-col>
      </el-row>
    </section>

    <!-- ===== 核心能力 ===== -->
    <section class="block">
      <div class="section-head">
        <h2 class="block-title">平台能力</h2>
        <span class="block-sub">AI 驱动的全流程人才匹配</span>
      </div>
      <el-row :gutter="20">
        <el-col :span="8" v-for="f in features" :key="f.title">
          <div class="feature-card" @click="go(f.to)">
            <div class="feature-icon" :style="{ background: f.color + '1A', color: f.color }">
              <el-icon><component :is="f.icon" /></el-icon>
            </div>
            <div class="feature-title">{{ f.title }}</div>
            <div class="feature-desc">{{ f.desc }}</div>
            <div class="feature-link">立即体验 <el-icon><ArrowRight /></el-icon></div>
          </div>
        </el-col>
      </el-row>
    </section>

    <!-- ===== 精选内容（角色化） ===== -->
    <section class="block" v-if="showcase.length">
      <div class="section-head">
        <h2 class="block-title">{{ showcaseTitle }}</h2>
        <el-button text type="primary" @click="go(showcaseMore)">查看更多 →</el-button>
      </div>
      <div class="showcase-grid">
        <div v-for="item in showcase" :key="item.id" class="job-card" @click="go(showcaseMore)">
          <div class="job-card-header">
            <div class="job-card-brand">{{ item.company?.charAt(0) || '🏢' }}</div>
            <div class="job-card-headtext">
              <div class="job-card-title">{{ item.title }}</div>
              <span v-if="metaOf(item.requirementTags).source[0]" class="source-pill">🔗 {{ metaOf(item.requirementTags).source[0] }}</span>
            </div>
          </div>
          <div class="job-card-meta">
            <span class="meta-item">{{ item.company }}</span>
            <span class="meta-item">📍 {{ item.location }}</span>
            <span class="meta-item salary">{{ item.salaryMin || item.salaryMax ? '💰 ' + (item.salaryMin || 0) + '-' + (item.salaryMax || 0) + 'K/月' : '💰 薪资面议' }}</span>
          </div>
          <div class="job-card-desc">{{ item.description || truncate(item.title, 50) }}</div>
          <div class="job-card-tags" v-if="metaOf(item.requirementTags).skills.length">
            <el-tag v-for="(s, idx) in metaOf(item.requirementTags).skills.slice(0, 3)" :key="idx" size="small" style="margin:2px;background:var(--color-accent-bg);border-color:transparent;color:var(--color-accent)">
              {{ s }}
            </el-tag>
          </div>
          <div class="job-card-actions">
            <el-button size="small" type="primary" plain @click.stop="quickApply(item)">立即投递</el-button>
            <el-button size="small" plain @click.stop="go(showcaseMore)">查看详情</el-button>
          </div>
        </div>
      </div>
    </section>

    <!-- ===== 内容→页脚过渡 ===== -->
    <div class="footer-fade"></div>

    <!-- ===== 底部页脚 ===== -->
    <footer class="site-footer">
      <div class="footer-inner">
        <div class="footer-col footer-brand">
          <div class="footer-logo">📄</div>
          <div class="footer-name">简历评估·推荐</div>
          <p class="footer-tag">连接简历与机会，<br />让每份才华都被温柔以待。</p>
        </div>
        <div class="footer-col">
          <div class="footer-h">快速导航</div>
          <ul class="footer-list">
            <li><a @click="go('/jobs')">浏览岗位</a></li>
            <li><a @click="go('/resumes')">我的简历</a></li>
            <li><a @click="go('/evaluate')">能力评估</a></li>
            <li><a @click="go('/recommend')">智能推荐</a></li>
          </ul>
        </div>
        <div class="footer-col">
          <div class="footer-h">关于我们</div>
          <ul class="footer-list">
            <li><a>平台使命</a></li>
            <li><a>数据隐私</a></li>
            <li><a>合作致谢</a></li>
            <li><a>加入我们</a></li>
          </ul>
        </div>
        <div class="footer-col">
          <div class="footer-h">联系方式</div>
          <ul class="footer-list footer-contact">
            <li><span class="ci">📧</span><span>support@smart-resume.cn</span></li>
            <li><span class="ci">📞</span><span>400-888-RESUME</span></li>
            <li><span class="ci">📍</span><span>上海市浦东新区张江高科</span></li>
          </ul>
        </div>
      </div>
      <div class="footer-bottom">
        <span>© 2026 简历评估·推荐 · Paper &amp; Highlight</span>
        <span class="footer-icp">沪 ICP 备 2026XXXXXX 号 · AI 驱动，规则辅助</span>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getAvailableJobs } from '@/api/job'
import { getMyResumes } from '@/api/resume'
import { recommendJobs } from '@/api/recommend'
import { getMyApplications } from '@/api/application'
import { getMyJobs } from '@/api/job'
import { getDashboard } from '@/api/admin'
import { getInfo } from '@/api/auth'
import {
  UserFilled, Upload, Search, Star, DataAnalysis, Document,
  Briefcase, User, Monitor, ArrowRight, OfficeBuilding, Trophy, Connection
} from '@element-plus/icons-vue'

const router = useRouter()
const store = useUserStore()

const loading = ref(false)
const jobsCount = ref(0)
const resumeCount = ref(0)
const recCount = ref(0)
const appCount = ref(0)
const myJobCount = ref(0)
const appReceived = ref(0)
const adminData = ref(null)
const showcase = ref([])

const isJobseeker = computed(() => store.isJobseeker)
const isHr = computed(() => store.isHr)
const isAdmin = computed(() => store.isAdmin)

const heroSubtitle = computed(() => {
  if (isJobseeker.value) return '上传简历，AI 为你解析能力画像，智能匹配最合适的工作机会。'
  if (isHr.value) return '发布岗位，AI 帮你从海量简历中精准筛选与推荐人才。'
  if (isAdmin.value) return '掌控平台全局数据，洞察招聘与求职的每一个环节。'
  return '智能简历评估与岗位推荐系统'
})

const heroCtAs = computed(() => {
  if (isJobseeker.value) return [
    { to: '/resumes', label: '上传简历', icon: Upload, primary: true },
    { to: '/jobs', label: '浏览岗位', icon: Search, primary: false }
  ]
  if (isHr.value) return [
    { to: '/hr/jobs', label: '发布岗位', icon: Briefcase, primary: true },
    { to: '/hr/talent', label: '人才推荐', icon: User, primary: false }
  ]
  if (isAdmin.value) return [
    { to: '/admin', label: '管理看板', icon: Monitor, primary: true }
  ]
  return []
})

const stats = computed(() => {
  if (isJobseeker.value) return [
    { label: '在招岗位', value: jobsCount.value, icon: Search, color: '#4A90B3' },
    { label: '我的简历', value: resumeCount.value, icon: Upload, color: '#DF5A3F' },
    { label: '智能推荐', value: recCount.value, icon: Star, color: '#5D9B71' },
    { label: '投递记录', value: appCount.value, icon: Document, color: '#D4943F' }
  ]
  if (isHr.value) return [
    { label: '我发布的岗位', value: myJobCount.value, icon: Briefcase, color: '#4A90B3' },
    { label: '收到投递', value: appReceived.value, icon: Document, color: '#DF5A3F', to: '/hr/applications' },
    { label: '在招岗位总数', value: jobsCount.value, icon: Search, color: '#5D9B71' },
    { label: '平台人才库', value: resumeCount.value, icon: User, color: '#D4943F' }
  ]
  if (isAdmin.value && adminData.value) return [
    { label: '用户总数', value: adminData.value.totalUsers || 0, icon: UserFilled, color: '#4A90B3' },
    { label: '岗位总数', value: adminData.value.totalJobs || 0, icon: Briefcase, color: '#DF5A3F' },
    { label: '简历总数', value: adminData.value.totalResumes || 0, icon: Upload, color: '#5D9B71' },
    { label: '在招岗位', value: adminData.value.activeJobs || 0, icon: Search, color: '#D4943F' }
  ]
  return []
})

const features = computed(() => {
  if (isHr.value) return [
    { title: '岗位发布与管理', desc: '快速发布岗位，设置薪资、地点与要求，一键上下架。', icon: Briefcase, color: '#4A90B3', to: '/hr/jobs' },
    { title: 'AI 人才推荐', desc: '基于简历与岗位向量匹配，精准推荐合适候选人。', icon: Connection, color: '#5D9B71', to: '/hr/talent' },
    { title: '简历解析', desc: '自动提取候选人能力画像与技能标签，省去人工初筛。', icon: Upload, color: '#DF5A3F', to: '/hr/talent' }
  ]
  if (isAdmin.value) return [
    { title: '全局数据看板', desc: '用户、岗位、简历、评估全维度统计一目了然。', icon: Monitor, color: '#4A90B3', to: '/admin' },
    { title: '平台运营洞察', desc: '掌握招聘与求职的活跃度与匹配效率。', icon: Trophy, color: '#5D9B71', to: '/admin' },
    { title: '系统配置', desc: '统一管理角色与权限，保障平台稳定运行。', icon: OfficeBuilding, color: '#DF5A3F', to: '/admin' }
  ]
  return [
    { title: '智能简历解析', desc: '上传 PDF/Word，AI 自动提取能力画像与技能标签。', icon: Upload, color: '#DF5A3F', to: '/resumes' },
    { title: '多维能力评估', desc: '从技能、经验、学历多维度评估与目标岗位的匹配度。', icon: DataAnalysis, color: '#4A90B3', to: '/evaluate' },
    { title: '个性化推荐', desc: '基于向量相似度为每份简历匹配最合适的岗位。', icon: Star, color: '#5D9B71', to: '/recommend' }
  ]
})

const showcaseTitle = computed(() => {
  if (isJobseeker.value) return '为你精选的岗位'
  if (isHr.value) return '我发布的岗位'
  if (isAdmin.value) return '平台概览'
  return ''
})
const showcaseMore = computed(() => {
  if (isJobseeker.value) return '/jobs'
  if (isHr.value) return '/hr/jobs'
  if (isAdmin.value) return '/admin'
  return '/jobs'
})

function go(to) { router.push(to) }

function quickApply(item) {
  if (item.id) {
    // 跳到岗位详情
    router.push({ path: '/jobs', query: { id: item.id } })
  } else {
    router.push('/jobs')
  }
}

function truncate(text, len) {
  if (!text) return ''
  return text.length > len ? text.slice(0, len) + '…' : text
}
function metaOf(tags) {
  const out = { source: [], skills: [] }
  if (!tags) return out
  tags.split(',').map(t => t.trim()).filter(Boolean).forEach(t => {
    if (t.startsWith('来源:')) out.source.push(t.slice(3))
    else if (!t.includes(':')) out.skills.push(t)
  })
  return out
}

onMounted(load)
async function load() {
  // 确保用户信息（角色/昵称）已加载，避免刷新后角色为空导致统计与精选不渲染
  if (!store.role && store.isLoggedIn) {
    try {
      const res = await getInfo()
      if (res.data) {
        store.nickname = res.data.nickname || store.nickname
        store.role = res.data.role
        store.userId = res.data.id
      }
    } catch {}
  }
  loading.value = true
  try {
    // 公共：在招岗位
    try {
      const jr = await getAvailableJobs()
      jobsCount.value = (jr.data || []).length
    } catch {}

    if (isJobseeker.value) {
      try { const rr = await getMyResumes(); resumeCount.value = (rr.data || []).length } catch {}
      try {
        const rec = await recommendJobs(6)
        recCount.value = (rec.data || []).length
        // 有推荐则展示推荐，否则降级展示最新在招岗位，保证首页饱满
        showcase.value = (rec.data || []).length ? (rec.data || []).slice(0, 3) : (await getAvailableJobs()).data.slice(0, 3)
      } catch {}
      try { const ap = await getMyApplications(); appCount.value = (ap.data || []).length } catch {}
    } else if (isHr.value) {
      try { const mj = await getMyJobs(); myJobCount.value = (mj.data || []).length; showcase.value = (mj.data || []).slice(0, 3) } catch {}
      // 收到投递：遍历我的岗位汇总
      try {
        const mj = await getMyJobs()
        const list = mj.data || []
        let total = 0
        await Promise.all(list.map(async (job) => {
          try {
            const ar = await import('@/api/application').then(m => m.getJobApplications(job.id))
            total += (ar.data || []).length
          } catch {}
        }))
        appReceived.value = total
      } catch {}
      // 不调用 getMyResumes — HR 无此权限
    } else if (isAdmin.value) {
      try { const d = await getDashboard(); adminData.value = d.data } catch {}
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.home { max-width: 1280px; margin: 0 auto; padding: 0 24px 40px; }

/* ===== Hero ===== */
.hero {
  position: relative;
  margin: 0 -24px 32px;
  padding: 56px 48px 64px;
  overflow: hidden;
  background: linear-gradient(135deg, #243B4A 0%, #2F5A70 45%, #4A90B3 100%);
  border-radius: 0 0 24px 24px;
}
.hero-bg {
  position: absolute; inset: 0;
  background-image:
    radial-gradient(circle at 15% 25%, rgba(223,90,63,0.18), transparent 40%),
    radial-gradient(circle at 85% 75%, rgba(157,199,225,0.22), transparent 45%);
  pointer-events: none;
}
.hero-float {
  position: absolute;
  background: rgba(255,255,255,0.08);
  border: 1px solid rgba(255,255,255,0.12);
  border-radius: 8px;
  animation: floaty 7s ease-in-out infinite;
}
.hero-float-1 { width: 120px; height: 150px; top: 30px; right: 12%; transform: rotate(8deg); }
.hero-float-2 { width: 90px; height: 120px; bottom: 20px; right: 28%; transform: rotate(-6deg); animation-delay: 1.5s; }
.hero-float-3 { width: 70px; height: 90px; top: 50px; right: 42%; transform: rotate(12deg); animation-delay: 3s; opacity: 0.6; }
@keyframes floaty {
  0%, 100% { transform: translateY(0) rotate(8deg); }
  50% { transform: translateY(-14px) rotate(8deg); }
}
.hero-inner { position: relative; z-index: 1; max-width: 720px; }
.hero-badge {
  display: inline-block;
  font-size: 13px;
  color: #fff;
  background: rgba(255,255,255,0.14);
  border: 1px solid rgba(255,255,255,0.2);
  padding: 5px 14px;
  border-radius: 999px;
  margin-bottom: 18px;
  backdrop-filter: blur(4px);
}
.hero-title {
  font-size: 38px;
  line-height: 1.3;
  font-weight: 700;
  color: #fff;
  margin: 0 0 16px;
  letter-spacing: 0.5px;
}
.hero-title .hl { color: #FFC9A8; }
.hero-sub {
  font-size: 16px;
  color: rgba(255,255,255,0.82);
  line-height: 1.7;
  margin: 0 0 28px;
  max-width: 560px;
}
.hero-actions { display: flex; gap: 14px; flex-wrap: wrap; }
.hero-actions :deep(.el-button) { padding: 12px 26px; font-size: 15px; font-weight: 600; }
.hero-actions :deep(.el-button--default) {
  background: rgba(255,255,255,0.12);
  border-color: rgba(255,255,255,0.25);
  color: #fff;
}
.hero-actions :deep(.el-button--default:hover) { background: rgba(255,255,255,0.2); }

/* ===== 区块 ===== */
.block { margin-bottom: 36px; }
.section-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 18px;
}
.block-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0;
  position: relative;
  padding-left: 14px;
}
.block-title::before {
  content: '';
  position: absolute;
  left: 0; top: 50%;
  transform: translateY(-50%);
  width: 4px; height: 18px;
  background: var(--color-accent);
  border-radius: 2px;
}
.block-sub { font-size: 13px; color: var(--color-text-muted); }

/* ===== 统计卡 ===== */
.home-stat {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 22px 24px;
}
.stat-icon {
  width: 44px; height: 44px;
  border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  font-size: 22px;
  margin-bottom: 14px;
}
.home-stat .stat-number { font-size: 34px; line-height: 1.1; }
.home-stat .stat-label { margin-top: 6px; }

/* ===== 特性卡 ===== */
.feature-card {
  background: var(--color-bg-card);
  border: 1px solid #EDEDF0;
  border-radius: 14px;
  padding: 26px 24px;
  cursor: pointer;
  transition: all 0.25s ease;
  height: 100%;
  box-sizing: border-box;
}
.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 28px rgba(43,43,49,0.10);
  border-color: var(--color-accent-soft);
}
.feature-icon {
  width: 52px; height: 52px;
  border-radius: 14px;
  display: flex; align-items: center; justify-content: center;
  font-size: 26px;
  margin-bottom: 16px;
}
.feature-title { font-size: 17px; font-weight: 700; color: var(--color-text-primary); margin-bottom: 8px; }
.feature-desc { font-size: 13px; color: var(--color-text-secondary); line-height: 1.7; min-height: 44px; }
.feature-link {
  margin-top: 14px;
  font-size: 13px;
  font-weight: 600;
  color: var(--el-color-primary);
  display: flex; align-items: center; gap: 4px;
}

/* ===== 精选网格 ===== */
.showcase-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}
.job-card {
  background: var(--color-bg-card);
  border: 1px solid #EDEDF0;
  border-radius: 12px;
  padding: 18px;
  cursor: pointer;
  transition: all 0.2s ease;
}
.job-card:hover { transform: translateY(-3px); box-shadow: 0 6px 20px rgba(43,43,49,0.10); }
.job-card-header { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.job-card-brand {
  width: 42px; height: 42px; border-radius: 10px;
  background: var(--color-accent-bg); color: var(--color-accent);
  display: flex; align-items: center; justify-content: center;
  font-size: 18px; font-weight: 700; flex-shrink: 0;
}
.job-card-title { font-size: 15px; font-weight: 700; color: var(--color-text-primary); }
.job-card-headtext { display: flex; flex-direction: column; gap: 4px; min-width: 0; }
.source-pill {
  align-self: flex-start;
  font-size: 11px; font-weight: 600;
  color: var(--el-color-primary);
  background: rgba(74,144,179,0.10);
  border: 1px solid rgba(74,144,179,0.25);
  padding: 1px 8px; border-radius: 999px;
}
.job-card-meta { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 10px; }
.meta-item { font-size: 12px; color: var(--color-text-secondary); background: #F5F4F2; padding: 2px 8px; border-radius: 4px; }
.meta-item.salary { color: var(--color-accent); font-weight: 600; }
.job-card-desc { font-size: 13px; color: var(--color-text-secondary); line-height: 1.5; min-height: 40px; }
.job-card-tags { margin-top: 8px; min-height: 22px; }
.job-card-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #F0EEEA;
}

/* ===== 内容→页脚过渡 ===== */
.footer-fade {
  margin: 56px -24px 0;
  height: 80px;
  background: linear-gradient(180deg, var(--color-bg-page, #FAF8F4) 0%, #E8E2D7 40%, #B8C5CD 80%, #243B4A 100%);
  position: relative;
}
.footer-fade::before {
  content: '';
  position: absolute;
  top: 0; left: 50%;
  transform: translateX(-50%);
  width: 60%;
  max-width: 400px;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--color-accent), transparent);
  opacity: 0.5;
}

/* ===== 底部页脚 ===== */
.site-footer {
  margin: 56px -24px 0;
  background: linear-gradient(180deg, #1A2730 0%, #243B4A 100%);
  color: #B8C5CD;
  position: relative;
}
.site-footer::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 3px;
  background: linear-gradient(90deg, #4A90B3 0%, #C75B39 50%, #4A90B3 100%);
  opacity: 0.7;
}
.footer-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 48px 24px 32px;
  display: grid;
  grid-template-columns: 1.4fr 1fr 1fr 1.2fr;
  gap: 32px;
}
.footer-col { min-width: 0; }
.footer-brand { padding-right: 20px; }
.footer-logo {
  font-size: 24px;
  margin-bottom: 8px;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.30));
}
.footer-name {
  font-size: 17px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 12px;
  letter-spacing: 1px;
}
.footer-tag {
  font-size: 12px;
  line-height: 1.8;
  color: #95A5B0;
  margin: 0;
}
.footer-h {
  font-size: 14px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 16px;
  position: relative;
  padding-left: 10px;
}
.footer-h::before {
  content: '';
  position: absolute;
  left: 0; top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 14px;
  background: var(--color-accent);
  border-radius: 2px;
}
.footer-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.footer-list li a,
.footer-list li span {
  font-size: 13px;
  color: #95A5B0;
  text-decoration: none;
  transition: color 0.15s ease;
  cursor: pointer;
}
.footer-list li a:hover { color: #FFC9A8; }
.footer-contact li {
  display: flex;
  align-items: center;
  gap: 8px;
}
.footer-contact li span:last-child {
  color: #B8C5CD;
  cursor: default;
}
.ci {
  width: 18px;
  text-align: center;
  font-size: 13px;
  opacity: 0.85;
  flex-shrink: 0;
}

.footer-bottom {
  border-top: 1px solid rgba(255,255,255,0.10);
  max-width: 1280px;
  margin: 0 auto;
  padding: 16px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: #6E7F8A;
  flex-wrap: wrap;
  gap: 8px;
}
.footer-icp { color: #6E7F8A; }

@media (max-width: 768px) {
  .footer-inner {
    grid-template-columns: 1fr 1fr;
    gap: 24px;
  }
  .footer-brand { grid-column: span 2; padding-right: 0; }
  .footer-bottom { flex-direction: column; align-items: flex-start; }
}
</style>
