import request from '@/utils/request'

export const createQuestionApi = (data: any) => request.post('/teacher/questions', data)
export const getQuestionApi = (id: number) => request.get(`/teacher/questions/${id}`)
export const createPaperApi = (data: any) => request.post('/teacher/papers', data)
export const addPaperQuestionsApi = (paperId: number, data: any) => request.post(`/teacher/papers/${paperId}/questions`, data)
export const getPaperDetailApi = (paperId: number) => request.get(`/teacher/papers/${paperId}`)
export const importPreviewApi = (formData: FormData) => request.post('/teacher/question-import/preview', formData)
export const importConfirmApi = (data: any) => request.post('/teacher/question-import/confirm', data)
