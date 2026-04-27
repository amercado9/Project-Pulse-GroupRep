import request from '@/shared/utils/request'
import type { ApiResponse } from '@/shared/types/api'

export interface InstructorSummary {
  instructorId: number
  firstName: string
  lastName: string
  email: string
  enabled: boolean
  teamNames: string[]
  academicYear: number | null
}

export interface FindInstructorsParams {
  firstName?: string
  lastName?: string
  enabled?: boolean
  teamName?: string
}

export const findInstructors = (params?: FindInstructorsParams) =>
  request.get<ApiResponse<InstructorSummary[]>>('/users/instructors', { params })

export const getInstructor = (id: number) =>
  request.get<ApiResponse<InstructorSummary>>(`/users/instructors/${id}`)

export const reactivateInstructor = (id: number) =>
  request.put<ApiResponse<void>>(`/users/instructors/${id}/reactivate`)

export const deactivateInstructor = (id: number) =>
  request.put<ApiResponse<void>>(`/users/instructors/${id}/deactivate`)
