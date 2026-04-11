import type { Activity } from '../activity/types'
import type { Student } from '../student/types'

export interface Team {
  teamId?: number
  teamName: string
  description: string
  teamWebsiteUrl: string
  sectionId: number
  sectionName?: string
  students?: Student[]
  activitiesInAWeek?: Activity[]
  studentsMissingActivity?: Student[]
}

export interface TeamSearchCriteria {
  teamId?: number; teamName?: string; sectionId?: number; sectionName?: string
}

export interface PaginationParams { page: number; size: number }

export interface SearchTeamByCriteriaResponse {
  flag: boolean; code: number; message: string
  data: { content: Team[]; totalElements: number; totalPages: number }
}

export interface FindTeamByIdResponse { flag: boolean; code: number; message: string; data: Team }
export interface CreateTeamResponse { flag: boolean; code: number; message: string; data: Team }
export interface UpdateTeamResponse { flag: boolean; code: number; message: string; data: Team }
export interface AssignStudentToTeamResponse { flag: boolean; code: number; message: string }
export interface RemoveStudentFromTeamResponse { flag: boolean; code: number; message: string }
export interface AssignInstructorToTeamResponse { flag: boolean; code: number; message: string }
export interface TransferTeamRequest { sectionId: number }
export interface TransferTeamResponse {
  flag: boolean; code: number; message: string
  data: { teamId: number; teamName: string; oldSectionId: number; newSectionId: number }
}
