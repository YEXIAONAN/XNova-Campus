import request from '@/utils/request'

export interface LoginPayload {
  username: string
  password: string
  loginType?: string
}

export interface LoginResponse {
  tokenType: string
  accessToken: string
  expiresIn: number
  user: { id: number; username: string; realName: string }
  roles: string[]
  permissions: string[]
}

export interface MeResponse {
  id: number
  username: string
  realName: string
  roles: string[]
  permissions: string[]
}

export const loginApi = (data: LoginPayload) => request.post<any, LoginResponse>('/auth/login', data)
export const meApi = () => request.get<any, MeResponse>('/auth/me')
export const logoutApi = () => request.post('/auth/logout')
