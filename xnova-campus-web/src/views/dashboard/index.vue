<template>
  <div class="card-grid">
    <el-card>
      <template #header>欢迎使用 XNova Campus</template>
      <p>当前登录用户：{{ authStore.user?.realName }}（{{ authStore.roles.join(', ') }}）</p>
      <p>这里是统一工作台，可根据角色进入对应功能模块。</p>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
const authStore = useAuthStore()
</script>

<style scoped>
.card-grid { display: grid; gap: 12px; }
</style>
