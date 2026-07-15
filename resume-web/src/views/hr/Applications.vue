<template>
  <div>
    <h3 class="page-title">📬 投递管理</h3>

    <!-- 统计概览 -->
    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="6" v-for="stat in appStats" :key="stat.label">
        <div class="stat-card">
          <div class="stat-number" :style="{ color: stat.color }">{{ stat.count }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 筛选 -->
    <div class="section-card" style="margin-bottom:16px">
      <div style="display:flex;gap:12px;align-items:center;flex-wrap:wrap">
        <span style="font-size:13px;font-weight:600;color:var(--color-text-secondary)">📋 筛选岗位</span>
        <el-select v-model="filterJobId" placeholder="全部岗位" clearable style="width:220px" @change="applyFilter">
          <el-option v-for="j in hrJobs" :key="j.id" :label="j.title" :value="j.id" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width:140px" @change="applyFilter">
          <el-option label="待处理" :value="0" />
          <el-option label="已查看" :value="1" />
          <el-option label="面试" :value="2" />
          <el-option label="通过" :value="3" />
          <el-option label="不通过" :value="4" />
        </el-select>
        <el-button size="small" :loading="loading" @click="load">刷新</el-button>
      </div>
    </div>

    <!-- 投递列表 -->
    <div class="section-card">
      <div v-if="loading" style="text-align:center;padding:40px 0;color:var(--color-text-muted)">加载中…</div>
      <div v-else-if="!apps.length" class="empty-state" style="padding:40px 0">
        <div class="empty-icon">📭</div>
        <p>暂无投递记录</p>
      </div>
      <template v-else>
        <div v-for="(group, gIdx) in groupedApps" :key="gIdx" style="margin-bottom:20px">
          <div class="job-group-header">
            <span class="job-group-title">{{ group.jobTitle }}</span>
            <el-tag size="small">{{ group.apps.length }} 份投递</el-tag>
          </div>
          <el-table :data="group.apps" stripe style="width:100%" size="small">
            <el-table-column label="候选人" width="130">
              <template #default="{ row }">
                <span>{{ row.candidateName || '求职者 #' + row.userId }}</span>
              </template>
            </el-table-column>
            <el-table-column label="投递时间" width="170">
              <template #default="{ row }">{{ row.createTime }}</template>
            </el-table-column>
            <el-table-column label="简历来源" width="120">
              <template #default="{ row }">简历 #{{ row.resumeId }}</template>
            </el-table-column>
            <el-table-column label="当前状态" width="120">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)" size="small" effect="dark" round>
                  {{ statusMap[row.status] || '待处理' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="280">
              <template #default="{ row }">
                <el-button-group>
                  <el-button v-if="row.status === 0" size="small" type="primary" plain @click="updateStatus(row, 1)">标记已查看</el-button>
                  <el-button v-if="row.status < 2" size="small" type="success" plain @click="updateStatus(row, 2)">邀约面试</el-button>
                  <el-button v-if="row.status < 3 && row.status >= 1" size="small" type="warning" plain @click="updateStatus(row, 3)">通过</el-button>
                  <el-button v-if="row.status < 4 && row.status >= 1" size="small" type="danger" plain @click="updateStatus(row, 4)">不通过</el-button>
                </el-button-group>
              </template>
            </el-table-column>
            <el-table-column label="查看简历" width="90">
              <template #default="{ row }">
                <el-button size="small" type="info" plain @click="viewResume(row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </template>
    </div>

    <!-- 简历查看弹窗 -->
    <el-dialog v-model="resumeVisible" title="候选人简历" width="700px" top="5vh">
      <template v-if="resumeData">
        <pre class="resume-preview">{{ resumeData }}</pre>
      </template>
      <div v-else-if="loadingResume" style="text-align:center;padding:20px">加载中…</div>
      <div v-else style="text-align:center;padding:20px;color:var(--color-text-muted)">暂无简历数据</div>
      <template #footer>
        <el-button @click="resumeVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getMyJobs } from '@/api/job'
import { getJobApplications, updateApplicationStatus } from '@/api/application'
import { getResume } from '@/api/resume'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const hrJobs = ref([])
const rawApps = ref([])       // 全部投递（未筛选）
const apps = ref([])           // 筛选后投递
const filterJobId = ref(null)
const filterStatus = ref(null)
const resumeVisible = ref(false)
const resumeData = ref(null)
const loadingResume = ref(false)
const resumeCache = ref({})    // resumeId → parsedJson

const statusMap = { 0: '待处理', 1: '已查看', 2: '面试', 3: '通过', 4: '不通过' }

const appStats = computed(() => {
  const list = rawApps.value
  return [
    { label: '全部投递', count: list.length, color: 'var(--el-color-primary)' },
    { label: '待处理', count: list.filter(i => i.status === 0).length, color: '#D4943F' },
    { label: '面试中', count: list.filter(i => i.status === 1 || i.status === 2).length, color: '#4A90B3' },
    { label: '已通过', count: list.filter(i => i.status === 3).length, color: '#5D9B71' }
  ]
})

onMounted(load)

async function load() {
  loading.value = true
  try {
    const jobRes = await getMyJobs()
    hrJobs.value = jobRes.data || []
    const jobMap = {}
    hrJobs.value.forEach(j => { jobMap[j.id] = j })

    // 逐个岗位获取投递
    const all = []
    for (const job of hrJobs.value) {
      try {
        const appRes = await getJobApplications(job.id)
        const jobApps = (appRes.data || []).map(a => ({
          ...a,
          jobTitle: job.title,
          jobCompany: job.company,
        }))
        all.push(...jobApps)

        // 异步获取候选人姓名
        for (const app of jobApps) {
          if (app.resumeId && !resumeCache.value[app.resumeId]) {
            try {
              const r = await getResume(app.resumeId)
              const data = r.data || {}
              resumeCache.value[app.resumeId] = data.parsedJson || JSON.stringify(data, null, 2)
            } catch { resumeCache.value[app.resumeId] = null }
          }
        }
      } catch {}
    }

    rawApps.value = all
    applyFilter()
  } finally { loading.value = false }
}

function applyFilter() {
  let list = rawApps.value
  if (filterJobId.value) list = list.filter(a => a.jobId === filterJobId.value)
  if (filterStatus.value !== null) list = list.filter(a => a.status === filterStatus.value)
  // 按岗位分组后再按时间排序
  list.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
  apps.value = list
}

const groupedApps = computed(() => {
  const groups = {}
  apps.value.forEach(a => {
    const key = a.jobId
    if (!groups[key]) groups[key] = { jobId: key, jobTitle: a.jobTitle || '岗位 #' + a.jobId, apps: [] }
    groups[key].apps.push(a)
  })
  return Object.values(groups)
})

async function updateStatus(app, newStatus) {
  const statusNames = { 0: '待处理', 1: '已查看', 2: '面试', 3: '通过', 4: '不通过' }
  try {
    await ElMessageBox.confirm(`确认将「${app.jobTitle}」投递状态改为「${statusNames[newStatus]}」？`)
    await updateApplicationStatus(app.id, newStatus)
    app.status = newStatus
    ElMessage.success('状态已更新')
  } catch {}
}

async function viewResume(app) {
  resumeVisible.value = true
  resumeData.value = null
  loadingResume.value = true
  try {
    const cached = resumeCache.value[app.resumeId]
    if (cached) {
      resumeData.value = cached
    } else {
      const r = await getResume(app.resumeId)
      const data = r.data || {}
      resumeData.value = data.parsedJson || JSON.stringify(data, null, 2)
    }
  } catch {
    resumeData.value = '无法加载简历数据'
  } finally { loadingResume.value = false }
}

function statusTagType(status) {
  const map = { 0: 'warning', 1: 'primary', 2: 'primary', 3: 'success', 4: 'danger' }
  return map[status] || 'info'
}
</script>

<style scoped>
.job-group-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
  padding: 8px 10px;
  background: var(--color-bg-card);
  border: 1px solid #EDEDF0;
  border-radius: 8px;
}
.job-group-title {
  font-weight: 700;
  font-size: 14px;
  color: var(--color-text-primary);
}
.resume-preview {
  white-space: pre-wrap;
  font-size: 13px;
  line-height: 1.6;
  color: var(--color-text-primary);
  background: #F8F7F4;
  border-radius: 8px;
  padding: 16px;
  max-height: 60vh;
  overflow-y: auto;
  font-family: inherit;
  margin: 0;
}
</style>
