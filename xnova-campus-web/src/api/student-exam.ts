import request from '@/utils/request'

export const studentExamsApi = (params: any) => request.get('/student/exams', { params })
export const studentExamDetailApi = (publishId: number) => request.get(`/student/exams/${publishId}`)
export const submitExamApi = (publishId: number, data: any) => request.post(`/student/exams/${publishId}/submit`, data)
export const studentExamResultApi = (publishId: number) => request.get(`/student/exams/${publishId}/result`)
export const studentExamRankingApi = (publishId: number, params: any) => request.get(`/student/exams/${publishId}/ranking`, { params })
