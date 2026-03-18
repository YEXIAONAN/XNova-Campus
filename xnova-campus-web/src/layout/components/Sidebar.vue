<template>
  <aside class="sidebar" :class="{ collapse: appStore.sidebarCollapsed }">
    <div class="logo">XNova Campus</div>
    <el-scrollbar class="menu-scroll">
      <el-menu :default-active="activePath" router :collapse="appStore.sidebarCollapsed">
        <template v-for="route in menuRoutes" :key="String(route.name)">
          <el-sub-menu v-if="route.children && route.children.length" :index="route.path">
            <template #title>
              <el-icon><Grid /></el-icon>
              <span>{{ route.meta?.title }}</span>
            </template>
            <el-menu-item
              v-for="child in route.children"
              :key="String(child.name)"
              :index="resolvePath(route.path, child.path || '')"
            >
              {{ child.meta?.title }}
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else :index="route.path">
            <el-icon><Grid /></el-icon>
            <span>{{ route.meta?.title }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-scrollbar>
  </aside>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { usePermissionStore } from '@/stores/permission'
import { useAppStore } from '@/stores/app'

const route = useRoute()
const permissionStore = usePermissionStore()
const appStore = useAppStore()

const activePath = computed(() => route.path)
const menuRoutes = computed(() => {
  const base = [
    {
      path: '/dashboard',
      name: 'Dashboard',
      meta: { title: '工作台' },
      children: [{ path: '', meta: { title: '工作台' } }]
    }
  ]
  return [...base, ...permissionStore.menus].map((route) => ({
    ...route,
    children: route.children?.filter((c) => !c.meta?.hidden)
  }))
})

function resolvePath(parent: string, child: string) {
  if (!child) return parent
  return `${parent}/${child}`.replace(/\/+/g, '/')
}
</script>

<style scoped>
.sidebar {
  width: 220px;
  background: #ffffff;
  border-right: 1px solid var(--line);
  transition: width 0.2s ease;
}

.sidebar.collapse {
  width: 64px;
}

.logo {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  border-bottom: 1px solid var(--line);
}

.menu-scroll {
  height: calc(100% - 56px);
}
</style>
