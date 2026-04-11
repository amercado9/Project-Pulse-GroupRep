export interface Student {
  id?: number
  username: string
  password?: string
  enabled?: boolean
  roles?: string
  firstName: string
  lastName: string
  email: string
  sectionId?: number
  sectionName?: string
  teamId?: number
  teamName?: string
}

export interface PaginationParams { page: number; size: number }

export interface StudentSearchCriteria {
  firstName?: string
  lastName?: string
  email?: string
  sectionId?: number
  sectionName?: string
  teamId?: number
}

export interface RegistrationParams {
  courseId: number
  sectionId?: number
  registrationToken: string
  role: string
}

export interface SearchStudentByCriteriaResponse {
  flag: boolean; code: number; message: string
  data: {
    content: Student[]
    totalElements: number; totalPages: number
    pageNumber: number; pageSize: number
  }
}

export interface FindStudentByIdResponse { flag: boolean; code: number; message: string; data: Student }
export interface FindStudentByTeamIdResponse { flag: boolean; code: number; message: string; data: Student[] }
export interface CreateStudentResponse { flag: boolean; code: number; message: string; data: Student }
export interface UpdateStudentResponse { flag: boolean; code: number; message: string; data: Student }
export interface DeleteStudentResponse { flag: boolean; code: number; message: string; data: null }
