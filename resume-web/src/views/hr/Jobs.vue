<template>
  <div>
    <h3 class="page-title">岗位管理</h3>

    <!-- 统计概览 -->
    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="8" v-for="stat in hrStats" :key="stat.label">
        <div class="stat-card">
          <div class="stat-number" :style="{ color: stat.color }">{{ stat.count }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 岗位列表 -->
    <div class="section-card">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
        <h4 style="font-weight:600;font-size:15px;color:var(--color-text-primary)">📋 岗位列表</h4>
        <el-button type="primary" @click="showCreate">
          <el-icon style="margin-right:4px"><Plus /></el-icon>发布岗位
        </el-button>
      </div>
      <el-table :data="jobs" v-loading="loading" style="width:100%">
        <el-table-column prop="title" label="岗位" min-width="150" />
        <el-table-column prop="company" label="公司" width="120" />
        <el-table-column prop="location" label="地点" width="120" />
        <el-table-column label="薪资(K)" width="120" align="center">
          <template #default="{row}">{{ row.salaryMin }}-{{ row.salaryMax }}K</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{row}">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="dark" round>
              {{ row.status === 1 ? '已发布' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="editRow(row)">编辑</el-button>
            <el-button size="small" @click="toggleRow(row)">{{ row.status === 1 ? '下架' : '上架' }}</el-button>
            <el-button size="small" type="primary" @click="viewApplications(row.id)">投递</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 岗位编辑弹窗 -->
    <el-dialog v-model="formVisible" :title="editing ? '编辑岗位' : '发布岗位'" width="600px" :close-on-click-modal="false">
      <el-form :model="form" label-width="80px">
        <el-form-item label="岗位名称" required><el-input v-model="form.title" placeholder="eg. Java后端开发工程师" /></el-form-item>
        <el-form-item label="公司" required><el-input v-model="form.company" placeholder="公司名称" /></el-form-item>
        <el-form-item label="地点"><el-input v-model="form.location" placeholder="eg. 深圳" /></el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="薪资下限"><el-input-number v-model="form.salaryMin" :min="0" :max="200" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="薪资上限"><el-input-number v-model="form.salaryMax" :min="0" :max="200" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="4" placeholder="岗位职责描述" /></el-form-item>
        <el-form-item label="技能要求"><el-input v-model="form.requirementTags" placeholder="逗号分隔，如：Java,Spring Boot,MySQL" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="saveJob" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 投递列表弹窗 -->
    <el-dialog v-model="appVisible" title="投递列表" width="700px">
      <template v-if="applications.length">
        <el-table :data="applications" style="width:100%" v-loading="appLoading">
          <el-table-column prop="userId" label="用户ID" width="80" />
          <el-table-column prop="status" label="状态" width="130" align="center">
            <template #default="{row}">
              <el-select :model-value="row.status" @change="(v) => updateAppStatus(row.id, v)" size="small">
                <el-option :value="0" label="待处理" />
                <el-option :value="1" label="已查看" />
                <el-option :value="2" label="面试" />
                <el-option :value="3" label="通过" />
                <el-option :value="4" label="不通过" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="投递时间" width="180" />
        </el-table>
      </template>
      <div v-else class="empty-state">
        <div class="empty-icon">📭</div>
        <p>暂无投递</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getMyJobs, createJob, updateJob, toggleJob } from '@/api/job'
import { getJobApplications, updateApplicationStatus } from '@/api/application'
import { ElMessage } from 'element-plus'

const jobs = ref([])
const loading = ref(false)
const saving = ref(false)
const formVisible = ref(false)
const editing = ref(false)
const form = ref({ title: '', company: '', location: '', salaryMin: 0, salaryMax: 0, description: '', requirementTags: '' })
const currentEditId = ref(null)
const appVisible = ref(false)
const applications = ref([])
const appLoading = ref(false)

const hrStats = computed(() => {
  const list = jobs.value
  return [
    { label: '全部岗位', count: list.length, color: 'var(--el-color-primary)' },
    { label: '已发布', count: list.filter(i => i.status === 1).length, color: '#5D9B71' },
    { label: '已下架', count: list.filter(i => i.status === 0).length, color: '#A0A0AC' }
  ]
})

onMounted(load)
async function load() {
  loading.value = true
  try {
    const res = await getMyJobs()
    jobs.value = res.data || []
  } finally { loading.value = false }
}

function showCreate() {
  editing.value = false
  currentEditId.value = null
  form.value = { title: '', company: '', location: '', salaryMin: 0, salaryMax: 0, description: '', requirementTags: '' }
  formVisible.value = true
}

function editRow(row) {
  editing.value = true
  currentEditId.value = row.id
  form.value = { ...row }
  formVisible.value = true
}

async function saveJob() {
  saving.value = true
  try {
    if (editing.value && currentEditId.value) {
      await updateJob(currentEditId.value, form.value)
      ElMessage.success('岗位已更新')
    } else {
      await createJob(form.value)
      ElMessage.success('岗位已发布，AI 自动生成匹配向量')
    }
    formVisible.value = false
    load()
  } finally { saving.value = false }
}

async function toggleRow(row) {
  const s = row.status === 1 ? 0 : 1
  await toggleJob(row.id, s)
  ElMessage.success(s === 1 ? '已上架' : '已下架')
  load()
}

async function viewApplications(jobId) {
  appVisible.value = true
  appLoading.value = true
  try {
    const res = await getJobApplications(jobId)
    applications.value = res.data || []
  } finally { appLoading.value = false }
}

async function updateAppStatus(id, status) {
  await updateApplicationStatus(id, status)
  ElMessage.success('状态已更新')
}
</script>
