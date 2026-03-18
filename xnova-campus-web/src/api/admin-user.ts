import request from '@/utils/request'

export const pageUsersApi = (params: Record<string, any>) => request.get('/admin/users', { params })
export const createStudentApi = (data: any) => request.post('/admin/users/students', data)
export const createTeacherApi = (data: any) => request.post('/admin/users/teachers', data)
export const updateStudentApi = (data: any) => request.put('/admin/users/students', data)
export const updateTeacherApi = (data: any) => request.put('/admin/users/teachers', data)
export const deleteUserApi = (userId: number) => request.delete(`/admin/users/${userId}`)
export const updateUserStatusApi = (data: any) => request.put('/admin/users/status', data)
export const resetPasswordApi = (data: any) => request.put('/admin/users/reset-password', data)
