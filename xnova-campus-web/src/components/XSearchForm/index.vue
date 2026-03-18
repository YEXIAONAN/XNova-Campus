<template>
  <el-form :inline="true" :model="model" class="search-form">
    <template v-for="item in items" :key="item.prop">
      <el-form-item :label="item.label">
        <el-input v-if="item.type === 'input'" v-model="model[item.prop]" :placeholder="item.placeholder || '请输入'" clearable />
        <el-select v-else-if="item.type === 'select'" v-model="model[item.prop]" clearable :placeholder="item.placeholder || '请选择'" style="width: 160px">
          <el-option v-for="op in item.options || []" :key="String(op.value)" :label="op.label" :value="op.value" />
        </el-select>
      </el-form-item>
    </template>
    <el-form-item>
      <el-button type="primary" @click="$emit('search')">查询</el-button>
      <el-button @click="$emit('reset')">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
defineProps<{
  model: Record<string, any>
  items: Array<{ prop: string; label: string; type: 'input' | 'select'; placeholder?: string; options?: Array<{ label: string; value: any }> }>
}>()

defineEmits(['search', 'reset'])
</script>

<style scoped>
.search-form {
  padding: 12px;
  border: 1px solid var(--line);
  border-radius: 10px;
  background: #fff;
  margin-bottom: 12px;
}
</style>
