<template>
  <div>
    <h3 class="page-title">管理看板</h3>
    <div class="section-card">
    <el-row :gutter="20" v-if="data">
      <el-col :span="6" v-for="item in cards" :key="item.key">
        <div class="stat-card">
          <div class="stat-number">{{ data[item.key] || 0 }}</div>
          <div class="stat-label">{{ item.label }}</div>
        </div>
      </el-col>
    </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDashboard } from '@/api/admin'

const data = ref(null)
const cards = [
  { key: 'totalUsers', label: '用户总数' },
  { key: 'totalHr', label: 'HR' },
  { key: 'totalJobseekers', label: '求职者' },
  { key: 'totalResumes', label: '简历总数' },
  { key: 'parsedResumes', label: '已解析简历' },
  { key: 'totalJobs', label: '岗位总数' },
  { key: 'activeJobs', label: '在招岗位' },
  { key: 'totalEvaluations', label: '评估记录' },
  { key: 'totalRecommendations', label: '推荐记录' }
]

onMounted(async () => {
  const res = await getDashboard()
  data.value = res.data
})
</script>
