import { createRouter, createWebHashHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue')
  },
  {
    path: '/',
    component: () => import('@/components/MainLayout.vue'),
    redirect: '/',
    children: [
      // 首页（通用）
      { path: '', name: 'Home', component: () => import('@/views/Home.vue') },
      // 求职者
      { path: '/profile', name: 'Profile', component: () => import('@/views/jobseeker/Profile.vue') },
      { path: '/resumes', name: 'Resume', component: () => import('@/views/jobseeker/Resume.vue') },
      { path: '/templates', name: 'Templates', component: () => import('@/views/jobseeker/Templates.vue') },
      { path: '/evaluate', name: 'Evaluate', component: () => import('@/views/jobseeker/Evaluate.vue') },
      { path: '/optimize', name: 'Optimize', component: () => import('@/views/jobseeker/Optimize.vue') },
      { path: '/recommend', name: 'Recommend', component: () => import('@/views/jobseeker/Recommend.vue') },
      { path: '/applications', name: 'JobseekerApp', component: () => import('@/views/jobseeker/Applications.vue') },
      { path: '/jobs', name: 'JobList', component: () => import('@/views/jobseeker/JobList.vue') },
      // HR
      { path: '/hr/jobs', name: 'HrJobs', component: () => import('@/views/hr/Jobs.vue') },
      { path: '/hr/talent', name: 'Talent', component: () => import('@/views/hr/Talent.vue') },
      { path: '/hr/applications', name: 'HrApplications', component: () => import('@/views/hr/Applications.vue') },
      // 管理员
      { path: '/admin', name: 'Admin', component: () => import('@/views/admin/Dashboard.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const store = useUserStore()
  if (to.path !== '/login' && !store.isLoggedIn) {
    return next('/login')
  }
  next()
})

export default router
