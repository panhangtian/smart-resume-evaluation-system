import { defineStore } from 'pinia'
import { getInfo } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userId: '',
    nickname: '',
    role: '',
    infoLoaded: false
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    isJobseeker: (state) => state.role === 'JOBSEEKER',
    isHr: (state) => state.role === 'HR',
    isAdmin: (state) => state.role === 'ADMIN'
  },
  actions: {
    setLogin(token, userId, nickname, role) {
      this.token = token
      this.userId = userId
      this.nickname = nickname
      this.role = role
      localStorage.setItem('token', token)
    },
    async loadInfo() {
      try {
        const res = await getInfo()
        this.userId = res.data.id
        this.nickname = res.data.nickname
        this.role = res.data.role
        this.infoLoaded = true
      } catch {
        this.logout()
      }
    },
    logout() {
      this.token = ''
      this.userId = ''
      this.nickname = ''
      this.role = ''
      this.infoLoaded = false
      localStorage.removeItem('token')
    }
  }
})
