<template>
  <el-card>
    <template #header>用户管理</template>
    <XSearchForm :model="query" :items="items" @search="fetchList" @reset="onReset" />
    <div style="margin-bottom: 12px">
      <el-button type="primary">新增学生</el-button>
      <el-button>新增教师</el-button>
    </div>
    <XTable :data="list" :loading="loading">
      <el-table-column prop="username" label="账号" width="140" />
      <el-table-column prop="realName" label="姓名" width="120" />
      <el-table-column prop="roleCode" label="角色" width="110" />
      <el-table-column prop="major" label="专业" min-width="180" />
      <el-table-column prop="className" label="班级" width="120" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button link type="primary">编辑</el-button>
          <el-button link type="warning">重置密码</el-button>
          <el-button link type="danger">删除</el-button>
        </template>
      </el-table-column>
    </XTable>
    <XPagination v-model:pageNum="query.pageNum" v-model:pageSize="query.pageSize" :total="total" @change="fetchList" />
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { pageUsersApi } from '@/api/admin-user'
import XSearchForm from '@/components/XSearchForm/index.vue'
import XTable from '@/components/XTable/index.vue'
import XPagination from '@/components/XPagination/index.vue'

const loading = ref(false)
const total = ref(0)
const list = ref<any[]>([])
const query = reactive({ pageNum: 1, pageSize: 10, roleCode: '', realName: '', className: '', major: '', status: '' })
const items = [
  { prop: 'roleCode', label: '角色', type: 'select', options: [
    { label: '管理员', value: 'ADMIN' },
    { label: '教师', value: 'TEACHER' },
    { label: '学生', value: 'STUDENT' }
  ] },
  { prop: 'realName', label: '姓名', type: 'input' },
  { prop: 'className', label: '班级', type: 'input' },
  { prop: 'major', label: '专业', type: 'input' },
  { prop: 'status', label: '状态', type: 'select', options: [
    { label: '启用', value: 1 },
    { label: '禁用', value: 0 }
  ] }
]

const fetchList = async () => {
  loading.value = true
  try {
    const data = await pageUsersApi(query)
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const onReset = () => {
  Object.assign(query, { pageNum: 1, pageSize: 10, roleCode: '', realName: '', className: '', major: '', status: '' })
  fetchList()
}

onMounted(fetchList)
</script>
