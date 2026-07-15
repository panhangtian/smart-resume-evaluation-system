<template>
  <div class="login-wrapper">
    <!-- 背景装饰：漂浮的纸张卡片 -->
    <div class="bg-decor">
      <span class="paper-shape p1"></span>
      <span class="paper-shape p2"></span>
      <span class="paper-shape p3"></span>
    </div>

    <div class="login-card">
      <div class="brand">
        <div class="brand-logo">📄</div>
        <div class="login-title">简历评估<span>·</span>推荐</div>
        <div class="login-subtitle">智能简历评估与岗位推荐系统</div>
      </div>

      <el-tabs v-model="tab" class="login-tabs">
        <el-tab-pane label="登录" name="login" />
        <el-tab-pane label="注册" name="register" />
      </el-tabs>

      <el-form v-if="tab === 'login'" :model="loginForm" label-width="auto" @keyup.enter="handleLogin">
        <el-form-item label="账号"><el-input v-model="loginForm.username" placeholder="请输入账号" :prefix-icon="User" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="loginForm.password" type="password" show-password placeholder="请输入密码" :prefix-icon="Lock" /></el-form-item>
        <el-form-item><el-button type="primary" class="submit-btn" :loading="loading" @click="handleLogin">登 录</el-button></el-form-item>
      </el-form>

      <el-form v-else :model="regForm" label-width="auto">
        <el-form-item label="账号"><el-input v-model="regForm.username" placeholder="设置账号" :prefix-icon="User" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="regForm.password" type="password" show-password placeholder="设置密码" :prefix-icon="Lock" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="regForm.role" style="width:100%">
            <el-option label="求职者" value="JOBSEEKER" />
            <el-option label="HR" value="HR" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="昵称"><el-input v-model="regForm.nickname" placeholder="怎么称呼你" :prefix-icon="Avatar" /></el-form-item>
        <el-form-item><el-button type="primary" class="submit-btn" :loading="loading" @click="handleRegister">注 册</el-button></el-form-item>
      </el-form>

      <div class="login-footer">Paper &amp; Highlight · 简历智能评估</div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { login, register } from '@/api/auth'
import { ElMessage } from 'element-plus'
import { User, Lock, Avatar } from '@element-plus/icons-vue'

const router = useRouter()
const store = useUserStore()
const tab = ref('login')
const loading = ref(false)

const loginForm = ref({ username: '', password: '' })
const regForm = ref({ username: '', password: '', role: 'JOBSEEKER', nickname: '' })

async function handleLogin() {
  loading.value = true
  try {
    const res = await login(loginForm.value)
    store.setLogin(res.data.token, res.data.userId, res.data.nickname, res.data.role)
    router.push('/')
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  loading.value = true
  try {
    const res = await register(regForm.value)
    store.setLogin(res.data.token, res.data.userId, res.data.nickname, res.data.role)
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* ============ 登录页美化（布局不变） ============ */
.login-wrapper {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(1200px 600px at 15% 10%, rgba(74,144,179,0.18), transparent 55%),
    radial-gradient(900px 500px at 85% 90%, rgba(223,90,63,0.16), transparent 50%),
    linear-gradient(135deg, #24242A 0%, #2B2B31 45%, #34333C 100%);
  position: relative;
  overflow: hidden;
}
/* 顶部高亮笔签名条 */
.login-wrapper::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, transparent, var(--color-accent), transparent);
  opacity: 0.7;
  z-index: 2;
}
/* 细网格纹理，纸感底纹 */
.login-wrapper::after {
  content: '';
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255,255,255,0.025) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,255,255,0.025) 1px, transparent 1px);
  background-size: 32px 32px;
  pointer-events: none;
}

/* ---- 背景漂浮纸片装饰 ---- */
.bg-decor { position: absolute; inset: 0; z-index: 0; }
.paper-shape {
  position: absolute;
  display: block;
  border-radius: 14px;
  background: rgba(255,255,255,0.04);
  border: 1px solid rgba(255,255,255,0.06);
  backdrop-filter: blur(2px);
}
.paper-shape.p1 { width: 180px; height: 230px; top: 12%; left: 12%; transform: rotate(-12deg); animation: float1 9s ease-in-out infinite; }
.paper-shape.p2 { width: 140px; height: 180px; bottom: 14%; right: 14%; transform: rotate(10deg); animation: float2 11s ease-in-out infinite; }
.paper-shape.p3 { width: 90px;  height: 120px; top: 60%;  left: 22%; transform: rotate(6deg);  animation: float1 13s ease-in-out infinite; }
@keyframes float1 { 0%,100% { transform: translateY(0) rotate(-12deg); } 50% { transform: translateY(-18px) rotate(-9deg); } }
@keyframes float2 { 0%,100% { transform: translateY(0) rotate(10deg); }  50% { transform: translateY(16px) rotate(13deg); } }

/* ---- 登录卡片 ---- */
.login-card {
  position: relative;
  z-index: 1;
  width: 420px;
  padding: 40px 40px 28px;
  background: rgba(255,255,255,0.98);
  border-radius: 18px;
  box-shadow:
    0 24px 60px rgba(0,0,0,0.35),
    0 2px 8px rgba(0,0,0,0.15);
  border: 1px solid rgba(255,255,255,0.6);
  animation: cardIn 0.5s cubic-bezier(0.22, 1, 0.36, 1);
}
/* 卡片顶部签名高亮条 */
.login-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 32px;
  right: 32px;
  height: 4px;
  border-radius: 0 0 3px 3px;
  background: linear-gradient(90deg, var(--color-accent), var(--el-color-primary));
  opacity: 0.85;
}
@keyframes cardIn { from { opacity: 0; transform: translateY(16px); } to { opacity: 1; transform: translateY(0); } }

/* ---- 品牌标题区 ---- */
.brand { text-align: center; margin-bottom: 20px; }
.brand-logo {
  width: 56px;
  height: 56px;
  margin: 0 auto 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  border-radius: 16px;
  background: var(--color-accent-bg);
  border: 1px solid var(--color-accent-soft);
  box-shadow: 0 4px 12px rgba(223,90,63,0.12);
}
.login-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text-primary);
  letter-spacing: 0.5px;
}
.login-title span { color: var(--color-accent); margin: 0 2px; }
.login-subtitle {
  margin-top: 6px;
  font-size: 13px;
  color: var(--color-text-muted);
  letter-spacing: 0.3px;
}

/* ---- Tabs ---- */
.login-tabs { margin-bottom: 20px; }
.login-tabs :deep(.el-tabs__nav-wrap::after) { height: 1px; background: #EDEDF0; }
.login-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-secondary);
  padding: 0 20px;
}
.login-tabs :deep(.el-tabs__item.is-active) { color: var(--el-color-primary); }
.login-tabs :deep(.el-tabs__active-bar) {
  height: 3px;
  border-radius: 2px;
  background: var(--color-accent);
}

/* ---- 表单输入 ---- */
.login-card :deep(.el-form-item) { margin-bottom: 20px; }
.login-card :deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 2px 12px;
  box-shadow: 0 0 0 1px #E3E1DD inset;
  transition: box-shadow 0.2s ease;
}
.login-card :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--el-color-primary-light-5) inset;
}
.login-card :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--el-color-primary) inset;
}
.login-card :deep(.el-input__inner) { height: 40px; }

/* ---- 提交按钮 ---- */
.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  border-radius: 10px;
  margin-top: 4px;
  box-shadow: 0 6px 16px rgba(74,144,179,0.28);
  transition: transform 0.15s ease, box-shadow 0.2s ease;
}
.submit-btn:hover { transform: translateY(-1px); box-shadow: 0 8px 20px rgba(74,144,179,0.36); }
.submit-btn:active { transform: translateY(0); }

/* ---- 页脚 ---- */
.login-footer {
  text-align: center;
  margin-top: 4px;
  font-size: 12px;
  color: var(--color-text-muted);
  letter-spacing: 0.5px;
}
</style>
