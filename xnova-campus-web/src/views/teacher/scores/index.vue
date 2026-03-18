<template>
  <el-card>
    <template #header>成绩管理</template>
    <XSearchForm :model="query" :items="items" @search="fetchList" @reset="onReset" />
    <XTable :data="list" :loading="loading">
      <el-table-column prop="examName" label="考试" min-width="180" />
      <el-table-column prop="studentName" label="学生" width="120" />
      <el-table-column prop="className" label="班级" width="120" />
      <el-table-column prop="totalScore" label="总分" width="100" />
      <el-table-column prop="reviewStatus" label="批改状态" width="140" />
      <el-table-column prop="scoreStatus" label="成绩状态" width="120" />
    </XTable>
    <XPagination v-model:pageNum="query.pageNum" v-model:pageSize="query.pageSize" :total="total" @change="fetchList" />
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { teacherScoresApi } from '@/api/teacher-review'
import XSearchForm from '@/components/XSearchForm/index.vue'
import XTable from '@/components/XTable/index.vue'
import XPagination from '@/components/XPagination/index.vue'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, publishId: '', className: '', studentName: '', reviewStatus: '' })

const items = [
  { prop: 'publishId', label: '考试ID', type: 'input' },
  { prop: 'className', label: '班级', type: 'input' },
  { prop: 'studentName', label: '学生姓名', type: 'input' },
  { prop: 'reviewStatus', label: '批改状态', type: 'select', options: [
    { label: '待批改', value: 'PENDING_REVIEW' },
    { label: '已完成', value: 'REVIEW_FINISHED' }
  ] }
]

const fetchList = async () => {
  loading.value = true
  try {
    const data = await teacherScoresApi(query)
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const onReset = () => {
  Object.assign(query, { pageNum: 1, pageSize: 10, publishId: '', className: '', studentName: '', reviewStatus: '' })
  fetchList()
}

onMounted(fetchList)
</script>
