<template>
  <div>
    <h3 class="page-title">投递记录</h3>

    <!-- 统计概览 -->
    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="6" v-for="stat in appStats" :key="stat.label">
        <div class="stat-card">
          <div class="stat-number" :style="{ color: stat.color }">{{ stat.count }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 投递列表 -->
    <div class="section-card">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
        <h4 style="font-weight:600;font-size:15px;color:var(--color-text-primary)">📬 投递记录</h4>
        <el-button size="small" @click="load" :icon="'Refresh'">刷新</el-button>
      </div>

      <div v-if="apps.length" class="app-timeline">
        <div v-for="app in apps" :key="app.id" class="app-item">
          <div class="app-dot" :class="statusDotClass(app.status)"></div>
          <div class="app-content">
            <div class="app-header">
              <span class="app-job">{{ app.jobTitle || '岗位 #' + app.jobId }}</span>
              <el-tag :type="statusTagType(app.status)" size="small" effect="dark" round>
                {{ statusMap[app.status] || '待处理' }}
              </el-tag>
            </div>
            <div class="app-meta">
              <span v-if="app.jobCompany" class="app-company">🏢 {{ app.jobCompany }}</span>
            </div>
            <div class="app-time">🕐 {{ app.createTime }}</div>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <div class="empty-icon">📭</div>
        <p>暂无投递记录，去浏览岗位投递吧</p>
        <el-button type="primary" style="margin-top:12px" @click="router.push('/jobs')">浏览岗位</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getMyApplications } from '@/api/application'
import { useRouter } from 'vue-router'

const router = useRouter()
const apps = ref([])
const loading = ref(false)

const statusMap = { 0: '待处理', 1: '已查看', 2: '面试', 3: '通过', 4: '不通过' }

const appStats = computed(() => {
  const list = apps.value
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
    const res = await getMyApplications()
    apps.value = res.data || []
  } finally { loading.value = false }
}

function statusTagType(status) {
  const map = { 0: 'warning', 1: 'primary', 2: 'primary', 3: 'success', 4: 'danger' }
  return map[status] || 'info'
}

function statusDotClass(status) {
  const map = { 0: 'dot-warning', 1: 'dot-primary', 2: 'dot-primary', 3: 'dot-success', 4: 'dot-danger' }
  return map[status] || 'dot-default'
}
</script>

<style scoped>
.app-timeline {
  position: relative;
  padding-left: 20px;
}
.app-timeline::before {
  content: '';
  position: absolute;
  left: 6px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: #EDEDF0;
}
.app-item {
  display: flex;
  gap: 16px;
  padding: 14px 0;
  position: relative;
}
.app-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 4px;
  z-index: 1;
}
.dot-warning { background: #D4943F; }
.dot-primary { background: #4A90B3; }
.dot-success { background: #5D9B71; }
.dot-danger { background: #DF5A3F; }
.dot-default { background: #A0A0AC; }
.app-content {
  flex: 1;
  background: var(--color-bg-card);
  border: 1px solid #F0EEEA;
  border-radius: 10px;
  padding: 14px 18px;
  transition: all 0.2s ease;
}
.app-content:hover {
  border-color: #DDD;
  box-shadow: 0 2px 8px rgba(43,43,49,0.04);
}
.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}
.app-job {
  font-weight: 600;
  font-size: 15px;
  color: var(--color-text-primary);
}
.app-meta {
  margin-bottom: 4px;
}
.app-company {
  font-size: 12px;
  color: var(--color-text-secondary);
}
.app-time {
  font-size: 12px;
  color: var(--color-text-muted);
}
</style>
