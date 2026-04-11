import request from '@/utils/request'
import type {
  LoginData,
  LoginResponse,
  ResetUserPasswordResponse,
  SendResetPasswordEmailResponse
} from './types'

enum API {
  LOGIN_ENDPOINT = '/users/login',
  USERS_ENDPOINT = '/users',
  FORGET_PASSWORD_ENDPOINT = '/users/forget-password'
}

export const loginUser = (loginData: LoginData) =>
  request.post<any, LoginResponse>(API.LOGIN_ENDPOINT, {}, { auth: loginData })

export const sendResetPasswordLink = (email: string) =>
  request.post<any, SendResetPasswordEmailResponse>(`${API.FORGET_PASSWORD_ENDPOINT}/${email}`)

export const resetUserPassword = (email: string, token: string, newPassword: string) =>
  request.patch<any, ResetUserPasswordResponse>(`${API.USERS_ENDPOINT}/reset-password`, {
    email,
    token,
    newPassword
  })
