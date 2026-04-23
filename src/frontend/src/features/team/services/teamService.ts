import request from '@/shared/utils/request'
import type { ApiResponse } from '@/shared/types/api'
import type {
  CreateTeamRequest,
  FindTeamsParams,
  TeamDetail,
  TeamStudentAssignmentWorkspace,
  TeamSummary,
  UpdateTeamRequest,
  UpdateTeamStudentAssignmentsRequest
} from './teamTypes'

const BASE = '/teams'
const ASSIGNMENT_BASE = '/team-student-assignments'

export const findTeams = (params?: FindTeamsParams) =>
  request.get<ApiResponse<TeamSummary[]>>(BASE, { params: params ?? {} })

export const getTeam = (id: number) =>
  request.get<ApiResponse<TeamDetail>>(`${BASE}/${id}`)

export const createTeam = (payload: CreateTeamRequest) =>
  request.post<ApiResponse<TeamDetail>>(BASE, payload)

export const updateTeam = (id: number, payload: UpdateTeamRequest) =>
  request.put<ApiResponse<TeamDetail>>(`${BASE}/${id}`, payload)

export const removeStudentFromTeam = (teamId: number, studentId: number) =>
  request.delete<ApiResponse<TeamDetail>>(`${BASE}/${teamId}/students/${studentId}`)

export const getTeamStudentAssignmentWorkspace = (sectionId: number) =>
  request.get<ApiResponse<TeamStudentAssignmentWorkspace>>(ASSIGNMENT_BASE, { params: { sectionId } })

export const updateTeamStudentAssignments = (payload: UpdateTeamStudentAssignmentsRequest) =>
  request.put<ApiResponse<TeamStudentAssignmentWorkspace>>(ASSIGNMENT_BASE, payload)
