<template>
  <div class="x-pagination">
    <el-pagination
      background
      layout="total, sizes, prev, pager, next"
      :current-page="pageNum"
      :page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50, 100]"
      @update:current-page="(v) => $emit('update:pageNum', v)"
      @update:page-size="(v) => $emit('update:pageSize', v)"
      @current-change="$emit('change')"
      @size-change="$emit('change')"
    />
  </div>
</template>

<script setup lang="ts">
defineProps<{ pageNum: number; pageSize: number; total: number }>()
defineEmits(['update:pageNum', 'update:pageSize', 'change'])
</script>

<style scoped>
.x-pagination {
  display: flex;
  justify-content: flex-end;
  padding: 12px 0;
}
</style>
