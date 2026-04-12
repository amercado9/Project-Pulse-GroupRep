import type { Instructor } from '../instructor/types'

export type DayOfWeek = 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY'

export interface Section {
  sectionId?: number; sectionName: string; startDate: string; endDate: string
  rubricId?: number; rubricName?: string; activeWeeks?: string[]
  courseId?: number; isActive: boolean
  warWeeklyDueDay: DayOfWeek; warDueTime: string
  peerEvaluationWeeklyDueDay: DayOfWeek; peerEvaluationDueTime: string
}

export type WeekInfo = { weekNumber: string; monday: string; sunday: string; isActive: boolean }
export interface SectionSearchCriteria { sectionId?: number; sectionName?: string }
export interface PaginationParams { page: number; size: number }

export interface SearchSectionByCriteriaResponse {
  flag: boolean; code: number; message: string
  data: { content: Section[]; totalElements: number; totalPages: number }
}

export interface FindSectionByIdResponse { flag: boolean; code: number; message: string; data: Section }
export interface CreateSectionResponse { flag: boolean; code: number; message: string; data: Section }
export interface UpdateSectionResponse { flag: boolean; code: number; message: string; data: Section }
export interface AssignRubricToSectionResponse { flag: boolean; code: number; message: string }
export interface GetSectionWeeksResponse { flag: boolean; code: number; message: string; data: WeekInfo[] }
export interface SetUpActiveWeeksResponse { flag: boolean; code: number; message: string }
export interface SendEmailInvitationsResponse { flag: boolean; code: number; message: string }
export interface InviteOrAddInstructorsResponse {
  flag: boolean; code: number; message: string
  data: { added: string[]; invited: string[]; alreadyExists: string[] }
}
export interface GetInstructorsResponse { flag: boolean; code: number; message: string; data: Instructor[] }
export interface RemoveInstructorResponse { flag: boolean; code: number; message: string }
