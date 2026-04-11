import request from '@/utils/request'
import type {
  PaginationParams, StudentSearchCriteria, SearchStudentByCriteriaResponse,
  CreateStudentResponse, DeleteStudentResponse, FindStudentByIdResponse,
  UpdateStudentResponse, Student, RegistrationParams, FindStudentByTeamIdResponse
} from './types'

enum API {
  STUDENTS = '/students',
  SEARCH_STUDENTS = '/students/search'
}

export const searchStudents = (params: PaginationParams, criteria: StudentSearchCriteria) =>
  request.post<any, SearchStudentByCriteriaResponse>(API.SEARCH_STUDENTS, criteria, { params })

export const findStudentById = (id: number) =>
  request.get<any, FindStudentByIdResponse>(`${API.STUDENTS}/${id}`)

export const findStudentsByTeamId = (teamId: number) =>
  request.get<any, FindStudentByTeamIdResponse>(`${API.STUDENTS}/teams/${teamId}`)

export const createStudent = (newStudent: Student, params: RegistrationParams) =>
  request.post<any, CreateStudentResponse>(API.STUDENTS, newStudent, { params })

export const updateStudent = (updatedStudent: Student) =>
  request.put<any, UpdateStudentResponse>(`${API.STUDENTS}/${updatedStudent.id}`, updatedStudent)

export const deleteStudent = (id: number) =>
  request.delete<any, DeleteStudentResponse>(`${API.STUDENTS}/${id}`)
