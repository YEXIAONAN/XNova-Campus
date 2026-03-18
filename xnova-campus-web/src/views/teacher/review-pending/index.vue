<template>
  <el-card>
    <template #header>未批改试卷</template>
    <XSearchForm :model="query" :items="items" @search="fetchList" @reset="onReset" />
    <XTable :data="list" :loading="loading">
      <el-table-column prop="examName" label="考试" min-width="180" />
      <el-table-column prop="studentName" label="学生" width="120" />
      <el-table-column prop="className" label="班级" width="120" />
      <el-table-column prop="submitTime" label="提交时间" width="180" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button link type="primary" @click="toReview(row.submissionId)">去批改</el-button>
        </template>
      </el-table-column>
    </XTable>
    <XPagination v-model:pageNum="query.pageNum" v-model:pageSize="query.pageSize" :total="total" @change="fetchList" />
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { pendingReviewsApi } from '@/api/teacher-review'
import XSearchForm from '@/components/XSearchForm/index.vue'
import XTable from '@/components/XTable/index.vue'
import XPagination from '@/components/XPagination/index.vue'

const router = useRouter()
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, publishId: '', className: '', studentName: '' })
const items = [
  { prop: 'publishId', label: '考试ID', type: 'input' },
  { prop: 'className', label: '班级', type: 'input' },
  { prop: 'studentName', label: '学生姓名', type: 'input' }
]

const fetchList = async () => {
  loading.value = true
  try {
    const data = await pendingReviewsApi(query)
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const onReset = () => {
  query.publishId = ''
  query.className = ''
  query.studentName = ''
  query.pageNum = 1
  fetchList()
}

const toReview = (submissionId: number) => {
  router.push(`/teacher/review?submissionId=${submissionId}`)
}

onMounted(fetchList)
</script>
