import router from './index'
import { useAuthStore } from '@/stores/auth'
import { usePermissionStore } from '@/stores/permission'

const whiteList = ['/login']

router.beforeEach(async (to, _from, next) => {
  const auth = useAuthStore()
  const permission = usePermissionStore()

  if (!auth.token) {
    if (whiteList.includes(to.path)) return next()
    return next('/login')
  }

  if (to.path === '/login') {
    return next('/dashboard')
  }

  if (!permission.loaded) {
    if (!auth.userLoaded) {
      await auth.fetchMe()
    }
    permission.buildRoutes(auth.roles)
    return next({ ...to, replace: true })
  }

  next()
})
