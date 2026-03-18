import type { RouteRecordRaw } from 'vue-router'

export function hasRole(roles: string[], route: RouteRecordRaw): boolean {
  const allowRoles = (route.meta?.roles || []) as string[]
  if (!allowRoles.length) return true
  return roles.some((r) => allowRoles.includes(r))
}

export function filterRoutes(routes: RouteRecordRaw[], roles: string[]): RouteRecordRaw[] {
  const res: RouteRecordRaw[] = []
  routes.forEach((route) => {
    const item = { ...route }
    if (hasRole(roles, item)) {
      if (item.children?.length) {
        item.children = filterRoutes(item.children, roles)
      }
      res.push(item)
    }
  })
  return res
}
