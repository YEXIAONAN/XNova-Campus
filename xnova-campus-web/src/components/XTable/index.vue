<template>
  <el-table :data="data" border stripe v-loading="loading" style="width: 100%">
    <slot />
    <template #empty>
      <el-empty description="暂无数据" />
    </template>
  </el-table>
</template>

<script setup lang="ts">
defineProps<{ data: any[]; loading?: boolean }>()
</script>
