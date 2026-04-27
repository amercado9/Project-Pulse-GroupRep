import request from '@/shared/utils/request'
import type { ApiResponse } from '@/shared/types/api'
import type { StudentDetail, StudentSummary, StudentUpdateDto } from './studentTypes'

const BASE = '/users/students'

export const findAllStudents = () =>
  request.get<ApiResponse<StudentSummary[]>>(BASE)

export const getStudent = (id: number) =>
  request.get<ApiResponse<StudentDetail>>(`${BASE}/${id}`)

export const deleteStudent = (id: number) =>
  request.delete<ApiResponse<null>>(`${BASE}/${id}`)

export const updateStudent = (id: number, data: StudentUpdateDto) =>
  request.put<ApiResponse<null>>(`${BASE}/${id}`, data)
