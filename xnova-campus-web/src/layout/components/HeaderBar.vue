<template>
  <header class="header-bar">
    <div class="left">
      <el-button text @click="appStore.toggleSidebar">
        <el-icon><Fold /></el-icon>
      </el-button>
      <span class="title">{{ currentTitle }}</span>
    </div>
    <div class="right">
      <el-dropdown>
        <span class="user">{{ authStore.user?.realName || authStore.user?.username || '用户' }}</span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import { usePermissionStore } from '@/stores/permission'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const authStore = useAuthStore()
const permissionStore = usePermissionStore()

const currentTitle = computed(() => (route.meta?.title as string) || 'XNova Campus')

async function handleLogout() {
  await authStore.logout()
  permissionStore.reset()
  router.replace('/login')
}
</script>

<style scoped>
.header-bar {
  height: 56px;
  background: #fff;
  border-bottom: 1px solid var(--line);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
}

.left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.title {
  font-weight: 600;
}

.user {
  cursor: pointer;
}
</style>
