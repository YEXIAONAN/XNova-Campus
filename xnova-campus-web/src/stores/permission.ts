import { defineStore } from 'pinia'
import type { RouteRecordRaw } from 'vue-router'
import router from '@/router'
import { asyncRoutes } from '@/router/async-routes'
import { filterRoutes } from '@/utils/permission'

export const usePermissionStore = defineStore('permission', {
  state: () => ({
    routes: [] as RouteRecordRaw[],
    menus: [] as RouteRecordRaw[],
    loaded: false
  }),
  actions: {
    reset() {
      this.routes = []
      this.menus = []
      this.loaded = false
    },
    buildRoutes(roles: string[]) {
      const accessRoutes = filterRoutes(asyncRoutes, roles)
      this.routes = accessRoutes
      this.menus = accessRoutes

      accessRoutes.forEach((route) => {
        if (!router.hasRoute(route.name as string)) {
          router.addRoute(route)
        }
      })
      this.loaded = true
    }
  }
})
