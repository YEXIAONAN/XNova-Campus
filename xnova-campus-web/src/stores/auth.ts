import { defineStore } from 'pinia'
import { clearToken, setToken as persistToken, getToken } from '@/utils/auth'
import { loginApi, meApi, logoutApi } from '@/api/auth'

export interface UserInfo {
  id: number
  username: string
  realName: string
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: getToken(),
    user: null as UserInfo | null,
    roles: [] as string[],
    permissions: [] as string[],
    userLoaded: false
  }),
  actions: {
    async login(payload: { username: string; password: string; loginType?: string }) {
      const data = await loginApi(payload)
      this.token = data.accessToken
      this.roles = data.roles || []
      this.permissions = data.permissions || []
      this.user = data.user || null
      this.userLoaded = false
      persistToken(this.token)
    },
    async fetchMe() {
      const data = await meApi()
      this.user = {
        id: data.id,
        username: data.username,
        realName: data.realName
      }
      this.roles = data.roles || this.roles
      this.permissions = data.permissions || this.permissions
      this.userLoaded = true
    },
    async logout() {
      try {
        await logoutApi()
      } catch (_) {
        // ignore
      }
      this.reset()
    },
    reset() {
      this.token = ''
      this.user = null
      this.roles = []
      this.permissions = []
      this.userLoaded = false
      clearToken()
    }
  },
  persist: {
    paths: ['roles', 'permissions']
  }
})
