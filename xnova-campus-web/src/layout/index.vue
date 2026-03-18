<template>
  <div class="layout-wrap">
    <Sidebar />
    <div class="layout-main" :class="{ collapse: appStore.sidebarCollapsed }">
      <HeaderBar />
      <AppMain />
    </div>
  </div>
</template>

<script setup lang="ts">
import Sidebar from './components/Sidebar.vue'
import HeaderBar from './components/HeaderBar.vue'
import AppMain from './components/AppMain.vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
</script>

<style scoped>
.layout-wrap {
  display: flex;
  height: 100%;
}

.layout-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}
</style>
