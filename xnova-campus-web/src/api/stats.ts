import request from '@/utils/request'

export const teacherStatsApi = (params: any) => request.get('/teacher/stats/exam-overview', { params })
