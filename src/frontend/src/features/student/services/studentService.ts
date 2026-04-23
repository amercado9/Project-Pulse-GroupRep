import request from '@/shared/utils/request'
import type { ApiResponse } from '@/shared/types/api'
import type { FindStudentsParams, StudentDetail, StudentSummary } from './studentTypes'

const BASE = '/users/students'

export const findStudents = (params?: FindStudentsParams) =>
  request.get<ApiResponse<StudentSummary[]>>(BASE, { params })

export const getStudent = (id: number) =>
  request.get<ApiResponse<StudentDetail>>(`${BASE}/${id}`)
