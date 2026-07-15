<template>
  <div>
    <h3 class="page-title">我的简历</h3>

    <!-- 统计概览 -->
    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="6" v-for="stat in stats" :key="stat.label">
        <div class="stat-card">
          <div class="stat-number" :style="{ color: stat.color }">{{ stat.count }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 上传区 -->
    <div class="section-card">
      <el-upload
        drag
        action="#"
        :auto-upload="false"
        :show-file-list="false"
        :on-change="handleUpload"
        accept=".pdf,.doc,.docx,.txt"
      >
        <el-icon class="el-icon--upload" size="48"><UploadFilled /></el-icon>
        <div class="el-upload__text">拖拽或点击 <em>上传简历</em></div>
        <template #tip><div class="el-upload__tip">支持 PDF / DOC / DOCX / TXT，不超过 10MB</div></template>
      </el-upload>
    </div>

    <!-- 简历列表 -->
    <div class="section-card" style="margin-top:16px">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
        <h4 style="font-weight:600;font-size:15px;color:var(--color-text-primary)">📄 简历列表</h4>
        <el-button size="small" @click="load" :icon="'Refresh'">刷新</el-button>
      </div>
      <el-table :data="resumes" v-loading="loading" style="width:100%">
        <el-table-column prop="fileName" label="文件名" min-width="200">
          <template #default="{row}">
            <div style="display:flex;align-items:center;gap:8px">
              <span style="font-size:18px">{{ fileIcon(row.fileName) }}</span>
              <span>{{ row.fileName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="解析状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type" effect="dark" round>
              {{ statusMap[row.status]?.label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="yearsOfExperience" label="工作年限" width="100" align="center" />
        <el-table-column prop="skills" label="技能标签" min-width="180" show-overflow-tooltip />
        <el-table-column prop="createTime" label="上传时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewDetail(row)">详情</el-button>
            <el-button size="small" @click="reparse(row.id)" :disabled="row.status === 1">重解析</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 简历详情弹窗 -->
    <el-dialog v-model="detailVisible" title="简历详情" width="700px">
      <template v-if="current">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="文件名">{{ current.fileName }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusMap[current.status]?.type" size="small">{{ statusMap[current.status]?.label }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="能力画像">{{ current.abilityProfile || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="技能标签">{{ current.skills || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="工作年限">{{ current.yearsOfExperience ?? '未知' }}</el-descriptions-item>
        </el-descriptions>
        <el-divider />
        <div style="font-weight:600;margin-bottom:8px">AI 解析详情</div>
        <pre style="white-space: pre-wrap; background: #F9F8F6; padding: 16px; border-radius: 8px; max-height: 400px; overflow: auto; font-size:13px; line-height:1.6">{{ formatJson(current.parsedJson) }}</pre>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getMyResumes, uploadResume, getResume, reparseResume } from '@/api/resume'
import { ElMessage } from 'element-plus'

const resumes = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const current = ref(null)

const statusMap = {
  0: { label: '待解析', type: 'info' },
  1: { label: '解析中', type: 'warning' },
  2: { label: '已解析', type: 'success' },
  3: { label: '解析失败', type: 'danger' }
}

const stats = computed(() => {
  const list = resumes.value
  return [
    { label: '全部简历', count: list.length, color: 'var(--el-color-primary)' },
    { label: '已解析', count: list.filter(i => i.status === 2).length, color: '#5D9B71' },
    { label: '解析中', count: list.filter(i => i.status === 1).length, color: '#D4943F' },
    { label: '解析失败', count: list.filter(i => i.status === 3).length, color: '#DF5A3F' }
  ]
})

onMounted(load)
async function load() {
  loading.value = true
  try {
    const res = await getMyResumes()
    resumes.value = res.data || []
  } finally { loading.value = false }
}

async function handleUpload(file) {
  const formData = new FormData()
  formData.append('file', file.raw)
  loading.value = true
  try {
    await uploadResume(formData)
    ElMessage.success('上传成功，后台正在解析')
    load()
  } finally { loading.value = false }
}

async function viewDetail(row) {
  const res = await getResume(row.id)
  current.value = res.data
  detailVisible.value = true
}

async function reparse(id) {
  await reparseResume(id)
  ElMessage.success('已重新触发解析')
  load()
}

function formatJson(json) {
  if (!json) return '暂无数据'
  try { return JSON.stringify(JSON.parse(json), null, 2) } catch { return json }
}

function fileIcon(name) {
  if (!name) return '📄'
  const lower = name.toLowerCase()
  if (lower.endsWith('.pdf')) return '📕'
  if (lower.endsWith('.doc') || lower.endsWith('.docx')) return '📘'
  if (lower.endsWith('.txt')) return '📃'
  return '📄'
}
</script>
