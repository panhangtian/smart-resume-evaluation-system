<template>
  <div>
    <!-- 页面标题 -->
    <h3 class="page-title">个人中心</h3>

    <el-row :gutter="20">
      <!-- ===== 左侧：个人资料 + 统计 + 快捷操作 ===== -->
      <el-col :xs="24" :lg="8">
        <!-- 资料卡 -->
        <div class="section-card profile-card">
          <div class="profile-cover"></div>
          <div class="profile-header">
            <el-upload
              :show-file-list="false"
              :auto-upload="false"
              accept="image/*"
              :on-change="handleAvatarChange"
              class="avatar-uploader"
            >
              <div class="avatar-ring">
                <img v-if="avatarUrl" :src="avatarUrl" class="avatar-img" />
                <div v-else class="avatar-circle">{{ initials }}</div>
                <div class="avatar-mask">
                  <el-icon><Camera /></el-icon>
                  <span>{{ avatarUrl ? '更换' : '上传' }}</span>
                </div>
                <div v-if="avatarUrl" class="avatar-remove" @click.stop="removeAvatar" title="移除头像">×</div>
              </div>
            </el-upload>
            <div class="profile-id">
              <div class="profile-name">{{ form.nickname || form.username }}</div>
              <div class="profile-role" :class="'role-' + (store.role || '').toLowerCase()">
                <span class="role-dot"></span>{{ roleLabel }}
              </div>
            </div>
          </div>

          <el-form :model="form" label-width="58px" size="default" style="margin-top:18px">
            <el-form-item label="账号">
              <el-input :model-value="form.username" disabled />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="form.nickname" placeholder="输入昵称" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="form.email" placeholder="输入邮箱" />
            </el-form-item>
            <el-form-item label="手机">
              <el-input v-model="form.phone" placeholder="输入手机号" />
            </el-form-item>
          </el-form>
          <el-button type="primary" :loading="saving" style="width:100%;margin-top:4px" @click="handleSaveProfile">
            💾 保存资料
          </el-button>
        </div>

        <!-- 统计卡（仅求职者） -->
        <div v-if="store.isJobseeker" class="section-card stats-card">
          <div class="stats-title">📊 我的数据</div>
          <div class="stats-grid">
            <div class="stat-item" @click="goResumes">
              <div class="stat-num">{{ stats.resumes }}</div>
              <div class="stat-lbl">简历</div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item" @click="goEvaluate">
              <div class="stat-num">{{ stats.evaluations }}</div>
              <div class="stat-lbl">评估</div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item" @click="goApplications">
              <div class="stat-num">{{ stats.applications }}</div>
              <div class="stat-lbl">投递</div>
            </div>
          </div>
        </div>

        <!-- 快捷操作（仅求职者） -->
        <div v-if="store.isJobseeker" class="section-card actions-card">
          <div class="stats-title">⚡ 快捷操作</div>
          <div class="action-list">
            <div class="action-item" @click="goResumes">
              <span class="act-icon">📤</span>
              <span class="act-text">上传新简历</span>
              <span class="act-arrow">›</span>
            </div>
            <div class="action-item" @click="goOptimize">
              <span class="act-icon">✨</span>
              <span class="act-text">AI 智能优化</span>
              <span class="act-arrow">›</span>
            </div>
            <div class="action-item" @click="goJobList">
              <span class="act-icon">🔍</span>
              <span class="act-text">浏览岗位</span>
              <span class="act-arrow">›</span>
            </div>
            <div class="action-item" @click="goTemplates">
              <span class="act-icon">📄</span>
              <span class="act-text">简历模板</span>
              <span class="act-arrow">›</span>
            </div>
          </div>
        </div>
      </el-col>

      <!-- ===== 右侧：简历编辑（仅求职者） ===== -->
      <el-col :xs="24" :lg="16">
        <div class="section-card" v-if="store.isJobseeker">
          <div class="resume-edit-head">
            <h4 style="margin:0;font-weight:600;color:var(--color-text-primary)">📄 简历编辑</h4>
            <p style="color:var(--color-text-muted);margin:4px 0 0;font-size:12px">
              选择简历后可编辑 AI 解析结果，修改后保存更新
            </p>
          </div>

          <!-- 简历选择 -->
          <el-select v-model="selectedResumeId" placeholder="选择要编辑的简历" style="width:100%;margin:16px 0" @change="loadResumeDetail">
            <el-option v-for="r in resumes" :key="r.id" :label="r.fileName" :value="r.id" />
          </el-select>

          <!-- 结构化编辑表单 -->
          <template v-if="editResume">
            <div class="resume-section">
              <div class="section-label"><span class="sec-icon">👤</span> 基本信息</div>
              <div class="section-body">
                <el-form :model="parsed" label-position="top" size="default">
                  <el-row :gutter="16">
                    <el-col :span="12">
                      <el-form-item label="姓名"><el-input v-model="parsed.name" placeholder="你的姓名" /></el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="工作年限"><el-input-number v-model="parsed.yearsOfExperienceNum" :min="0" :max="50" style="width:100%" /></el-form-item>
                    </el-col>
                  </el-row>
                </el-form>
              </div>
            </div>

            <div class="resume-section">
              <div class="section-label"><span class="sec-icon">✨</span> 能力画像</div>
              <div class="section-body">
                <el-input v-model="editForm.abilityProfile" type="textarea" :rows="3" placeholder="用一句话描述你的核心能力与职业定位…" />
              </div>
            </div>

            <div class="resume-section">
              <div class="section-label">
                <span class="sec-icon">🎓</span> 教育背景
                <el-button size="small" text type="primary" @click="addEducation">+ 添加</el-button>
              </div>
              <div class="section-body">
                <div v-for="(edu, idx) in parsed.educationList" :key="'edu'+idx" class="sub-card">
                  <div class="sub-card-head">
                    <span>{{ edu.school || '教育经历 ' + (idx+1) }}</span>
                    <el-button size="small" text type="danger" @click="removeEducation(idx)">删除</el-button>
                  </div>
                  <el-row :gutter="12">
                    <el-col :span="8"><el-input v-model="edu.school" placeholder="学校" /></el-col>
                    <el-col :span="8"><el-input v-model="edu.major" placeholder="专业" /></el-col>
                    <el-col :span="8"><el-input v-model="edu.degree" placeholder="学历" /></el-col>
                  </el-row>
                </div>
                <div v-if="!parsed.educationList.length" class="empty-hint">暂无教育背景，点击上方"添加"</div>
              </div>
            </div>

            <div class="resume-section">
              <div class="section-label">
                <span class="sec-icon">💼</span> 工作经历
                <el-button size="small" text type="primary" @click="addWork">+ 添加</el-button>
              </div>
              <div class="section-body">
                <div v-for="(w, idx) in parsed.workList" :key="'w'+idx" class="sub-card">
                  <div class="sub-card-head">
                    <span>{{ w.company || '工作经历 ' + (idx+1) }}</span>
                    <el-button size="small" text type="danger" @click="removeWork(idx)">删除</el-button>
                  </div>
                  <el-row :gutter="12">
                    <el-col :span="10"><el-input v-model="w.company" placeholder="公司名称" /></el-col>
                    <el-col :span="8"><el-input v-model="w.title" placeholder="职位" /></el-col>
                    <el-col :span="6"><el-input v-model="w.duration" placeholder="如 3年" /></el-col>
                  </el-row>
                  <el-input v-model="w.description" type="textarea" :rows="2" placeholder="工作描述、主要成就…" style="margin-top:8px" />
                </div>
                <div v-if="!parsed.workList.length" class="empty-hint">暂无工作经历，点击上方"添加"</div>
              </div>
            </div>

            <div class="resume-section">
              <div class="section-label">
                <span class="sec-icon">🚀</span> 项目经历
                <el-button size="small" text type="primary" @click="addProject">+ 添加</el-button>
              </div>
              <div class="section-body">
                <div v-for="(p, idx) in parsed.projectList" :key="'p'+idx" class="sub-card">
                  <div class="sub-card-head">
                    <span>{{ p.name || '项目 ' + (idx+1) }}</span>
                    <el-button size="small" text type="danger" @click="removeProject(idx)">删除</el-button>
                  </div>
                  <el-input v-model="p.name" placeholder="项目名称" />
                  <el-input v-model="p.description" type="textarea" :rows="2" placeholder="项目描述、技术栈、你的角色…" style="margin-top:6px" />
                </div>
                <div v-if="!parsed.projectList.length" class="empty-hint">暂无项目经历，点击上方"添加"</div>
              </div>
            </div>

            <div class="resume-section">
              <div class="section-label"><span class="sec-icon">🏷️</span> 技能标签</div>
              <div class="section-body">
                <div class="tag-input-area">
                  <el-tag v-for="(skill, idx) in parsed.skillList" :key="idx" closable @close="removeSkill(idx)" style="margin:3px;background:var(--color-accent-bg);border-color:transparent;color:var(--color-accent)">{{ skill }}</el-tag>
                  <el-input v-if="skillInputVisible" ref="skillInputRef" v-model="skillInputValue" size="small" style="width:100px;margin:3px" @keyup.enter="confirmAddSkill" @blur="confirmAddSkill" />
                  <el-button v-else size="small" @click="showSkillInput">+ 添加技能</el-button>
                </div>
              </div>
            </div>

            <el-button type="primary" :loading="resumeSaving" style="width:100%;margin-top:20px" size="large" @click="handleSaveResume">
              💾 保存简历修改
            </el-button>
          </template>
          <div v-else class="empty-state" style="padding:40px 0">
            <div class="empty-icon">📂</div>
            <p>请先选择一份简历进行编辑</p>
          </div>
        </div>

        <!-- HR/Admin 占位 -->
        <div class="section-card" v-if="!store.isJobseeker" style="text-align:center;padding:60px 0">
          <div style="font-size:48px;margin-bottom:16px;opacity:0.3">📊</div>
          <p style="color:var(--color-text-muted)">{{ store.isHr ? 'HR 角色，请使用岗位管理和人才推荐功能' : '管理员角色，请使用管理看板' }}</p>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { useRouter } from 'vue-router'
import { getInfo } from '@/api/auth'
import { updateProfile, updateResumeData } from '@/api/profile'
import { getMyResumes } from '@/api/resume'
import { getMyApplications } from '@/api/application'
import { ElMessage } from 'element-plus'
import { Camera } from '@element-plus/icons-vue'

const store = useUserStore()
const router = useRouter()

// ===== 个人资料 =====
const form = ref({
  username: '',
  nickname: '',
  email: '',
  phone: ''
})
const saving = ref(false)

// ===== 头像 (本地存储，base64) =====
const avatarUrl = ref('')
const AVATAR_KEY = () => `user_avatar_${store.userId || 'default'}`

function loadAvatar() {
  try { avatarUrl.value = localStorage.getItem(AVATAR_KEY()) || '' } catch { avatarUrl.value = '' }
}
function saveAvatar(dataUrl) {
  try { localStorage.setItem(AVATAR_KEY(), dataUrl) } catch {}
}
function clearAvatar() {
  avatarUrl.value = ''
  try { localStorage.removeItem(AVATAR_KEY()) } catch {}
}

function handleAvatarChange(file) {
  if (!file || !file.raw) return
  const raw = file.raw
  if (raw.size > 3 * 1024 * 1024) {
    ElMessage.warning('图片不能超过 3MB')
    return
  }
  if (!raw.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }
  const reader = new FileReader()
  reader.onload = (e) => {
    const dataUrl = e.target.result
    // 简单压缩：超过 800px 用 canvas 缩放
    compressImage(dataUrl, 800).then((finalUrl) => {
      avatarUrl.value = finalUrl
      saveAvatar(finalUrl)
      ElMessage.success('头像已更新')
    })
  }
  reader.readAsDataURL(raw)
}

function compressImage(dataUrl, maxSize) {
  return new Promise((resolve) => {
    const img = new Image()
    img.onload = () => {
      let { width, height } = img
      if (width <= maxSize && height <= maxSize) {
        resolve(dataUrl)
        return
      }
      if (width > height) {
        height = Math.round(height * maxSize / width)
        width = maxSize
      } else {
        width = Math.round(width * maxSize / height)
        height = maxSize
      }
      const canvas = document.createElement('canvas')
      canvas.width = width
      canvas.height = height
      const ctx = canvas.getContext('2d')
      ctx.drawImage(img, 0, 0, width, height)
      resolve(canvas.toDataURL('image/jpeg', 0.85))
    }
    img.onerror = () => resolve(dataUrl)
    img.src = dataUrl
  })
}

function removeAvatar() {
  clearAvatar()
  ElMessage.success('已移除头像')
}

const initials = computed(() => {
  const n = form.value.nickname || form.value.username || 'U'
  return n.charAt(0).toUpperCase()
})

const roleLabel = computed(() => {
  const map = { JOBSEEKER: '求职者', HR: 'HR 管理员', ADMIN: '系统管理员' }
  return map[store.role] || store.role
})

// ===== 统计 =====
const stats = reactive({ resumes: 0, evaluations: 0, applications: 0 })

async function loadStats() {
  try {
    const [r, a] = await Promise.all([
      getMyResumes().catch(() => ({ data: [] })),
      getMyApplications().catch(() => ({ data: [] }))
    ])
    stats.resumes = (r.data || []).length
    stats.applications = (a.data || []).length
    // 评估数：取所有简历的评估总数
    let evCount = 0
    const resumes = r.data || []
    const evApi = (await import('@/api/evaluate')).getMyEvaluations
    for (const re of resumes) {
      try {
        const ev = await evApi(re.id)
        evCount += (ev.data || []).length
      } catch {}
    }
    stats.evaluations = evCount
  } catch {}
}

// ===== 路由跳转 =====
const goResumes = () => router.push('/resumes')
const goOptimize = () => router.push('/optimize')
const goJobList = () => router.push('/jobs')
const goTemplates = () => router.push('/templates')
const goEvaluate = () => router.push('/evaluate')
const goApplications = () => router.push('/applications')

onMounted(async () => {
  loadAvatar()
  const res = await getInfo()
  if (res.data) {
    form.value.username = res.data.username || ''
    form.value.nickname = res.data.nickname || ''
    form.value.email = res.data.email || ''
    form.value.phone = res.data.phone || ''
  }
  if (store.isJobseeker) {
    loadResumes()
    loadStats()
  }
})

async function handleSaveProfile() {
  saving.value = true
  try {
    await updateProfile({
      nickname: form.value.nickname,
      email: form.value.email,
      phone: form.value.phone
    })
    store.nickname = form.value.nickname
    ElMessage.success('资料已更新')
  } finally {
    saving.value = false
  }
}

// ===== 简历编辑（结构化） =====
const resumes = ref([])
const selectedResumeId = ref('')
const editResume = ref(null)
const editForm = ref({ parsedJson: '', abilityProfile: '', skills: '', yearsOfExperience: 0 })
const resumeSaving = ref(false)

const parsed = ref({
  name: '',
  yearsOfExperienceNum: 0,
  educationList: [],
  workList: [],
  projectList: [],
  skillList: []
})

const skillInputVisible = ref(false)
const skillInputValue = ref('')
const skillInputRef = ref(null)

function parseToJson(jsonStr) {
  try {
    return JSON.parse(jsonStr) || {}
  } catch { return {} }
}

function serializeParsed() {
  const p = parsed.value
  return JSON.stringify({
    name: p.name || '',
    yearsOfExperience: String(p.yearsOfExperienceNum ?? 0),
    education: (p.educationList || []).map(e => ({
      degree: e.degree || '', school: e.school || '', major: e.major || ''
    })),
    work: (p.workList || []).map(w => ({
      company: w.company || '', title: w.title || '',
      duration: w.duration || '', description: w.description || ''
    })),
    projects: (p.projectList || []).map(pj => ({
      name: pj.name || '', description: pj.description || ''
    })),
    skills: p.skillList || [],
    summary: editForm.value.abilityProfile || ''
  }, null, 2)
}

async function loadResumes() {
  const res = await getMyResumes()
  resumes.value = res.data || []
  if (resumes.value.length) {
    selectedResumeId.value = resumes.value[0].id
    loadResumeDetail(resumes.value[0].id)
  }
}

async function loadResumeDetail(id) {
  const r = resumes.value.find(i => i.id === id)
  if (!r) return
  editResume.value = r
  editForm.value = {
    parsedJson: r.parsedJson || '',
    abilityProfile: r.abilityProfile || '',
    skills: r.skills || '',
    yearsOfExperience: r.yearsOfExperience || 0
  }
  const obj = parseToJson(r.parsedJson)
  parsed.value = {
    name: obj.name || '',
    yearsOfExperienceNum: parseInt(obj.yearsOfExperience) || r.yearsOfExperience || 0,
    educationList: Array.isArray(obj.education) ? obj.education.map(e => ({ ...e })) : [],
    workList: Array.isArray(obj.work) ? obj.work.map(w => ({ ...w })) : [],
    projectList: Array.isArray(obj.projects) ? obj.projects.map(p => ({ ...p })) : [],
    skillList: Array.isArray(obj.skills) ? [...obj.skills] : (r.skills ? r.skills.split(',').map(s=>s.trim()).filter(Boolean) : [])
  }
}

function addEducation() { parsed.value.educationList.push({ school: '', major: '', degree: '' }) }
function removeEducation(idx) { parsed.value.educationList.splice(idx, 1) }

function addWork() { parsed.value.workList.push({ company: '', title: '', duration: '', description: '' }) }
function removeWork(idx) { parsed.value.workList.splice(idx, 1) }

function addProject() { parsed.value.projectList.push({ name: '', description: '' }) }
function removeProject(idx) { parsed.value.projectList.splice(idx, 1) }

function showSkillInput() { skillInputVisible.value = true; skillInputValue.value = '' }
function confirmAddSkill() {
  const v = (skillInputValue.value || '').trim()
  if (v && !parsed.value.skillList.includes(v)) {
    parsed.value.skillList.push(v)
  }
  skillInputVisible.value = false
  skillInputValue.value = ''
}
function removeSkill(idx) { parsed.value.skillList.splice(idx, 1) }

async function handleSaveResume() {
  if (!selectedResumeId.value) return
  resumeSaving.value = true
  try {
    const newJson = serializeParsed()
    const newSkills = parsed.value.skillList.join(',')
    await updateResumeData(selectedResumeId.value, {
      parsedJson: newJson,
      abilityProfile: editForm.value.abilityProfile,
      skills: newSkills,
      yearsOfExperience: parsed.value.yearsOfExperienceNum
    })
    ElMessage.success('✅ 简历已更新')
    const res = await getMyResumes()
    resumes.value = res.data || []
    const updated = resumes.value.find(i => i.id === selectedResumeId.value)
    if (updated) editResume.value = updated
  } finally {
    resumeSaving.value = false
  }
}
</script>

<style scoped>
/* ===== 资料卡 ===== */
.profile-card {
  text-align: left;
  padding: 0;
  overflow: hidden;
}
.profile-cover {
  height: 60px;
  background: linear-gradient(135deg, #243B4A 0%, #4A90B3 50%, #6FAFC9 100%);
  position: relative;
}
.profile-cover::after {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 80% 30%, rgba(255,255,255,0.18) 0, transparent 40%),
    radial-gradient(circle at 20% 80%, rgba(199,91,57,0.18) 0, transparent 50%);
}
.profile-header {
  display: flex;
  align-items: flex-end;
  gap: 14px;
  padding: 0 20px;
  margin-top: -32px;
  margin-bottom: 8px;
  position: relative;
  z-index: 2;
}
/* ===== 头像上传 ===== */
.avatar-uploader { display: block; }
.avatar-uploader :deep(.el-upload) { display: block; cursor: pointer; }
.avatar-ring {
  position: relative;
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: #fff;
  padding: 3px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.10);
  flex-shrink: 0;
  cursor: pointer;
  overflow: hidden;
}
.avatar-circle {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: linear-gradient(135deg, #243B4A, #4A90B3);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 700;
  color: #fff;
}
.avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  display: block;
}
.avatar-mask {
  position: absolute;
  inset: 3px;
  border-radius: 50%;
  background: rgba(36,59,74,0.62);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  font-size: 11px;
  font-weight: 500;
  opacity: 0;
  transition: opacity 0.2s ease;
  backdrop-filter: blur(2px);
}
.avatar-mask .el-icon { font-size: 16px; }
.avatar-ring:hover .avatar-mask { opacity: 1; }
.avatar-remove {
  position: absolute;
  top: 0;
  right: 0;
  width: 20px;
  height: 20px;
  background: #C75B39;
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  line-height: 1;
  font-weight: 600;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s ease;
  z-index: 3;
  box-shadow: 0 2px 6px rgba(0,0,0,0.20);
}
.avatar-ring:hover .avatar-remove { opacity: 1; }
.avatar-remove:hover { background: #B14A2A; }

.profile-id { padding-bottom: 6px; }
.profile-name {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1.2;
}
.profile-role {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 11px;
  margin-top: 4px;
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(74,144,179,0.12);
  color: #4A90B3;
  font-weight: 500;
}
.profile-role.role-hr { background: rgba(199,91,57,0.12); color: #C75B39; }
.profile-role.role-admin { background: rgba(36,59,74,0.15); color: #243B4A; }
.role-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

/* ===== 统计卡 ===== */
.stats-card { padding: 16px 20px; }
.stats-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 14px;
}
.stats-grid {
  display: flex;
  align-items: stretch;
  background: linear-gradient(135deg, #FAF9F7 0%, #F4F1ED 100%);
  border-radius: 10px;
  padding: 14px 0;
}
.stat-item {
  flex: 1;
  text-align: center;
  cursor: pointer;
  transition: all 0.15s ease;
  border-radius: 8px;
  padding: 4px 0;
}
.stat-item:hover {
  background: rgba(74,144,179,0.08);
  transform: translateY(-1px);
}
.stat-num {
  font-size: 22px;
  font-weight: 800;
  color: var(--el-color-primary);
  line-height: 1;
}
.stat-lbl {
  font-size: 11px;
  color: var(--color-text-muted);
  margin-top: 4px;
}
.stat-divider {
  width: 1px;
  background: #D8D5CF;
  margin: 4px 0;
}

/* ===== 快捷操作 ===== */
.actions-card { padding: 16px 20px; }
.action-list { display: flex; flex-direction: column; gap: 2px; }
.action-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s ease;
}
.action-item:hover {
  background: #F5F4F2;
}
.act-icon { font-size: 18px; }
.act-text {
  flex: 1;
  font-size: 13px;
  color: var(--color-text-primary);
  font-weight: 500;
}
.act-arrow {
  color: var(--color-text-muted);
  font-size: 18px;
  line-height: 1;
}

/* ===== 简历编辑头部 ===== */
.resume-edit-head {
  padding-bottom: 0;
}

/* ===== 简历结构化编辑（沿用原样式） ===== */
.resume-section {
  background: #FAF9F7;
  border-radius: 10px;
  padding: 14px 16px;
  margin-bottom: 12px;
  border: 1px solid #EDECE9;
}
.section-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  font-size: 14px;
  color: var(--color-text-primary);
  margin-bottom: 10px;
}
.sec-icon { font-size: 15px; }
.section-body {}
.sub-card {
  background: #fff;
  border: 1px solid #E8E6E1;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 8px;
}
.sub-card:last-child { margin-bottom: 0; }
.sub-card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-weight: 600;
  font-size: 13px;
  color: var(--color-text-primary);
}
.empty-hint {
  text-align: center;
  color: var(--color-text-muted);
  font-size: 13px;
  padding: 16px 0;
  opacity: 0.6;
}
.tag-input-area {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}
</style>
