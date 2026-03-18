<template>
  <el-card>
    <template #header>题库管理</template>
    <el-form inline>
      <el-form-item label="题型"><el-select style="width:120px"><el-option label="单选" value="SINGLE" /></el-select></el-form-item>
      <el-form-item><el-button type="primary">新增题目</el-button></el-form-item>
    </el-form>
  </el-card>
</template>
