import request from '@/shared/utils/request'
import type {
  LoginData,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
  ChangePasswordRequest,
  ChangePasswordResponse,
  SendResetPasswordEmailResponse,
  ResetUserPasswordResponse
} from './authTypes'

enum API {
  AUTH = '/auth',
  USERS = '/users'
}

export const loginUser = (loginData: LoginData) =>
  request.post<any, LoginResponse>(`${API.AUTH}/login`, {}, { auth: loginData })

export const registerUser = (payload: RegisterRequest) =>
  request.post<any, RegisterResponse>(`${API.AUTH}/register`, payload)

export const sendResetPasswordLink = (email: string) =>
  request.post<any, SendResetPasswordEmailResponse>(`${API.AUTH}/forget-password/${email}`)

export const resetUserPassword = (email: string, token: string, newPassword: string) =>
  request.patch<any, ResetUserPasswordResponse>(`${API.AUTH}/reset-password`, {
    email,
    token,
    newPassword
  })

export const changePassword = (payload: ChangePasswordRequest) =>
  request.patch<any, ChangePasswordResponse>(`${API.USERS}/change-password`, payload)

export const validateJoinToken = (token: string) =>
  request.get<any>(`${API.AUTH}/join`, { params: { token } })

export const registerWithToken = (payload: { token: string; firstName: string; lastName: string; password: string }) =>
  request.post<any>(`${API.AUTH}/join`, payload)
