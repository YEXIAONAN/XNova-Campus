<template>
  <el-card>
    <template #header>可参加考试</template>
    <XSearchForm :model="query" :items="searchItems" @search="fetchList" @reset="onReset" />
    <XTable :data="list" :loading="loading">
      <el-table-column prop="examName" label="考试名称" min-width="200" />
      <el-table-column prop="startTime" label="开始时间" width="180" />
      <el-table-column prop="endTime" label="结束时间" width="180" />
      <el-table-column prop="examStatus" label="状态" width="120" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button type="primary" link @click="toDetail(row.publishId)">进入</el-button>
        </template>
      </el-table-column>
    </XTable>
    <XPagination v-model:pageNum="query.pageNum" v-model:pageSize="query.pageSize" :total="total" @change="fetchList" />
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { studentExamsApi } from '@/api/student-exam'
import XSearchForm from '@/components/XSearchForm/index.vue'
import XTable from '@/components/XTable/index.vue'
import XPagination from '@/components/XPagination/index.vue'

const router = useRouter()
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)

const query = reactive({ pageNum: 1, pageSize: 10, status: '' })
const searchItems = [{ prop: 'status', label: '状态', type: 'select', options: [
  { label: '可参加', value: 'AVAILABLE' },
  { label: '已完成', value: 'FINISHED' },
  { label: '已错过', value: 'MISSED' }
]}]

const fetchList = async () => {
  loading.value = true
  try {
    const data = await studentExamsApi(query)
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const onReset = () => {
  query.status = ''
  query.pageNum = 1
  fetchList()
}

const toDetail = (publishId: number) => router.push(`/student/exams/${publishId}`)

onMounted(fetchList)
</script>
