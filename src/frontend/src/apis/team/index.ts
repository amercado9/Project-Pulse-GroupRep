import request from '@/utils/request'
import type {
  PaginationParams, Team, TeamSearchCriteria, CreateTeamResponse, FindTeamByIdResponse,
  SearchTeamByCriteriaResponse, UpdateTeamResponse, AssignStudentToTeamResponse,
  RemoveStudentFromTeamResponse, AssignInstructorToTeamResponse, TransferTeamRequest, TransferTeamResponse
} from './types'

enum API { SEARCH_TEAMS = '/teams/search', TEAMS = '/teams' }

export const searchTeams = (params: PaginationParams, criteria: TeamSearchCriteria) =>
  request.post<any, SearchTeamByCriteriaResponse>(API.SEARCH_TEAMS, criteria, { params })

export const findTeamById = (id: number) =>
  request.get<any, FindTeamByIdResponse>(`${API.TEAMS}/${id}`)

export const createTeam = (team: Team) =>
  request.post<any, CreateTeamResponse>(API.TEAMS, team)

export const updateTeam = (team: Team) =>
  request.put<any, UpdateTeamResponse>(`${API.TEAMS}/${team.teamId}`, team)

export const assignStudentToTeam = (teamId: number, studentId: number) =>
  request.put<any, AssignStudentToTeamResponse>(`${API.TEAMS}/${teamId}/students/${studentId}`)

export const removeStudentFromTeam = (teamId: number, studentId: number) =>
  request.delete<any, RemoveStudentFromTeamResponse>(`${API.TEAMS}/${teamId}/students/${studentId}`)

export const assignInstructorToTeam = (teamId: number, instructorId: number) =>
  request.put<any, AssignInstructorToTeamResponse>(`${API.TEAMS}/${teamId}/instructors/${instructorId}`)

export const transferTeam = (teamId: number, req: TransferTeamRequest) =>
  request.patch<any, TransferTeamResponse>(`${API.TEAMS}/${teamId}/section`, req)
