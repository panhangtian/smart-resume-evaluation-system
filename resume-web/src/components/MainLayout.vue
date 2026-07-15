<template>
  <el-container style="height: 100vh">
    <!-- 侧边栏 -->
    <el-aside width="220px" class="app-aside">
      <div class="sidebar-brand">
        <div class="brand-icon">✦</div>
        <div class="brand-text">
          <div class="brand-title">简历评估推荐</div>
          <div class="brand-sub">Smart Resume</div>
        </div>
      </div>

      <el-menu :default-active="activeMenu" router>
        <!-- 首页（通用） -->
        <el-menu-item index="/"><el-icon><HomeFilled /></el-icon>首页</el-menu-item>

        <!-- 求职者 -->
        <template v-if="store.isJobseeker">
          <div class="menu-group-label">求职者</div>
          <el-menu-item index="/profile"><el-icon><UserFilled /></el-icon>个人中心</el-menu-item>
          <el-menu-item index="/resumes"><el-icon><Upload /></el-icon>我的简历</el-menu-item>
          <el-menu-item index="/templates"><el-icon><Files /></el-icon>简历模板</el-menu-item>
          <el-menu-item index="/jobs"><el-icon><Search /></el-icon>浏览岗位</el-menu-item>
          <el-menu-item index="/recommend"><el-icon><Star /></el-icon>智能推荐</el-menu-item>
          <el-menu-item index="/evaluate"><el-icon><DataAnalysis /></el-icon>能力评估</el-menu-item>
          <el-menu-item index="/optimize"><el-icon><MagicStick /></el-icon>智能优化</el-menu-item>
          <el-menu-item index="/applications"><el-icon><Document /></el-icon>投递记录</el-menu-item>
        </template>

        <!-- HR -->
        <template v-if="store.isHr">
          <div class="menu-group-label">HR 管理</div>
          <el-menu-item index="/hr/jobs"><el-icon><Briefcase /></el-icon>岗位管理</el-menu-item>
          <el-menu-item index="/hr/talent"><el-icon><User /></el-icon>人才推荐</el-menu-item>
          <el-menu-item index="/hr/applications"><el-icon><Document /></el-icon>投递管理</el-menu-item>
        </template>

        <!-- 管理员 -->
        <template v-if="store.isAdmin">
          <div class="menu-group-label">系统管理</div>
          <el-menu-item index="/admin"><el-icon><Monitor /></el-icon>管理看板</el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container direction="vertical">
      <!-- 顶栏：右侧放"页面名 + 角色 + 用户名 + 退出" -->
      <header class="app-header">
        <div class="header-right">
          <span class="user-badge">{{ roleBadge }}</span>
          <span class="header-divider">/</span>
          <span class="header-page">{{ currentPageName }}</span>
          <span class="header-divider">/</span>
          <span class="user-name">{{ store.nickname || store.role }}</span>
          <el-button type="danger" size="small" plain round @click="handleLogout">退出</el-button>
        </div>
      </header>

      <!-- 内容（居中） -->
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { HomeFilled, Files, MagicStick, Document, Briefcase, User } from '@element-plus/icons-vue'

const route = useRoute()
const store = useUserStore()
const router = useRouter()

// 刷新后从 token 恢复角色/昵称，避免侧边栏与角色化页面空白
onMounted(() => {
  if (store.isLoggedIn && !store.role) {
    store.loadInfo()
  }
})

const pageNameMap = {
  '/': '首页',
  '/profile': '个人中心',
  '/resumes': '我的简历',
  '/templates': '简历模板',
  '/jobs': '浏览岗位',
  '/recommend': '智能推荐',
  '/evaluate': '能力评估',
  '/optimize': '智能优化',
  '/applications': '投递记录',
  '/hr/jobs': '岗位管理',
  '/hr/talent': '人才推荐',
  '/hr/applications': '投递管理',
  '/admin': '管理看板'
}

const currentPageName = computed(() => pageNameMap[route.path] || (route.path === '/' ? '首页' : route.path))

const roleBadge = computed(() => {
  const map = { JOBSEEKER: '求职者', HR: 'HR', ADMIN: '管理员' }
  return map[store.role] || store.role
})

function handleLogout() {
  store.logout()
  router.push('/login')
}
</script>

<style scoped>
/* 侧边栏 */
.app-aside {
  background: var(--color-bg-sidebar) !important;
  overflow-y: auto;
  overflow-x: hidden;
}
.app-aside::-webkit-scrollbar {
  width: 4px;
}
.app-aside::-webkit-scrollbar-thumb {
  background: rgba(255,255,255,0.1);
  border-radius: 2px;
}

/* 侧边栏品牌区 */
.sidebar-brand {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 18px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
  background: #24242A;
}
.brand-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, var(--color-accent), var(--el-color-primary));
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  flex-shrink: 0;
}
.brand-text {
  flex: 1;
  overflow: hidden;
}
.brand-title {
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.3px;
  white-space: nowrap;
}
.brand-sub {
  color: rgba(255,255,255,0.4);
  font-size: 11px;
  margin-top: 1px;
  white-space: nowrap;
}

/* 菜单分组标签 */
.menu-group-label {
  padding: 14px 18px 6px;
  font-size: 11px;
  font-weight: 600;
  color: rgba(255,255,255,0.25);
  text-transform: uppercase;
  letter-spacing: 1px;
}

/* 顶栏：横跨主区域，右侧信息靠右 */
.app-header {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  background: var(--color-bg-card);
  border-bottom: 1px solid #EDEDF0;
  padding: 0 24px;
  height: 52px;
  flex-shrink: 0;
  width: 100%;
  box-sizing: border-box;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}
.user-badge {
  font-size: 11px;
  font-weight: 600;
  background: var(--color-accent-bg);
  color: var(--color-accent);
  padding: 3px 10px;
  border-radius: 10px;
}
.header-page {
  font-size: 14px;
  font-weight: 700;
  color: var(--color-text-primary);
}
.header-divider {
  color: var(--color-text-muted);
  font-size: 13px;
}
.user-name {
  color: var(--color-text-secondary);
  font-size: 13px;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 主内容 */
.app-main {
  background: var(--color-bg-page);
  overflow-y: auto;
  flex: 1;
}
</style>
