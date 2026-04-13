import request from '@/utils/request'
import type { RegisterRequest, RegisterResponse, ChangePasswordRequest, ChangePasswordResponse } from './types'

enum API { USERS = '/users' }

export const registerUser = (payload: RegisterRequest) =>
  request.post<any, RegisterResponse>(`${API.USERS}/register`, payload)
export const changePassword = (payload: ChangePasswordRequest) =>
  request.patch<any, ChangePasswordResponse>(`${API.USERS}/change-password`, payload)
