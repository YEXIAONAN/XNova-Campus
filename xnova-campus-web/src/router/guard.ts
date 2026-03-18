import type { Pinia } from 'pinia'
import type { Router } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { usePermissionStore } from '@/stores/permission'

const whiteList = ['/login']

export default function setupRouterGuard(router: Router, pinia: Pinia) {
  router.beforeEach(async (to, _from, next) => {
    const auth = useAuthStore(pinia)
    const permission = usePermissionStore(pinia)

    if (!auth.token) {
      if (whiteList.includes(to.path)) return next()
      return next('/login')
    }

    if (to.path === '/login') {
      return next('/dashboard')
    }

    if (!permission.loaded) {
      try {
        if (!auth.userLoaded) {
          await auth.fetchMe()
        }
        permission.buildRoutes(auth.roles)
        return next({ ...to, replace: true })
      } catch (_e) {
        auth.reset()
        permission.reset()
        return next('/login')
      }
    }

    next()
  })
}
