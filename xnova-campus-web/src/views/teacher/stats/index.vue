<template>
  <el-row :gutter="12">
    <el-col :span="6" v-for="card in cards" :key="card.label">
      <el-card>
        <div class="label">{{ card.label }}</div>
        <div class="value">{{ card.value }}</div>
      </el-card>
    </el-col>
  </el-row>
  <el-card style="margin-top:12px">
    <div ref="chartRef" style="height: 360px"></div>
  </el-card>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import * as echarts from 'echarts'

const chartRef = ref<HTMLDivElement>()
const cards = ref([
  { label: '平均分', value: '--' },
  { label: '最高分', value: '--' },
  { label: '最低分', value: '--' },
  { label: '及格率', value: '--' }
])

onMounted(async () => {
  await nextTick()
  if (!chartRef.value) return
  const chart = echarts.init(chartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: ['班级A', '班级B', '班级C', '班级D'] },
    yAxis: { type: 'value' },
    series: [{
      name: '平均分',
      type: 'bar',
      data: [81, 76, 88, 73],
      itemStyle: { color: '#2f6fed' }
    }]
  })
})
</script>

<style scoped>
.label { color: #667085; }
.value { margin-top: 4px; font-size: 24px; font-weight: 700; }
</style>
