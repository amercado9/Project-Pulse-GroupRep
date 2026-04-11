import request from '@/utils/request'
import type { ChangePasswordRequest, ChangePasswordResponse } from './types'

enum API { USERS = '/users' }

export const changePassword = (payload: ChangePasswordRequest) =>
  request.patch<any, ChangePasswordResponse>(`${API.USERS}/change-password`, payload)
