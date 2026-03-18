import request from '@/utils/request'

export const pendingReviewsApi = (params: any) => request.get('/teacher/reviews/pending', { params })
export const reviewDetailApi = (submissionId: number) => request.get(`/teacher/reviews/${submissionId}`)
export const gradeSubmissionApi = (submissionId: number, data: any) => request.post(`/teacher/reviews/${submissionId}/grade`, data)
export const teacherScoresApi = (params: any) => request.get('/teacher/scores', { params })
export const examOverviewApi = (params: any) => request.get('/teacher/stats/exam-overview', { params })
