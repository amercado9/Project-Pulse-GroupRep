import request from '@/utils/request'
import type {
  PaginationParams, Section, SectionSearchCriteria,
  CreateSectionResponse, FindSectionByIdResponse, SearchSectionByCriteriaResponse,
  UpdateSectionResponse, AssignRubricToSectionResponse, GetSectionWeeksResponse,
  SetUpActiveWeeksResponse, SendEmailInvitationsResponse, InviteOrAddInstructorsResponse,
  GetInstructorsResponse, RemoveInstructorResponse
} from './types'

enum API { SEARCH_SECTIONS = '/sections/search', SECTIONS = '/sections' }

export const searchSections = (params: PaginationParams, criteria: SectionSearchCriteria) =>
  request.post<any, SearchSectionByCriteriaResponse>(API.SEARCH_SECTIONS, criteria, { params })
export const findSectionById = (id: number) => request.get<any, FindSectionByIdResponse>(`${API.SECTIONS}/${id}`)
export const createSection = (section: Section) => request.post<any, CreateSectionResponse>(API.SECTIONS, section)
export const updateSection = (section: Section) => request.put<any, UpdateSectionResponse>(`${API.SECTIONS}/${section.sectionId}`, section)
export const assignRubricToSection = (sectionId: number, rubricId: number) =>
  request.put<any, AssignRubricToSectionResponse>(`${API.SECTIONS}/${sectionId}/rubrics/${rubricId}`)
export const getSectionWeeks = (sectionId: number) =>
  request.get<any, GetSectionWeeksResponse>(`${API.SECTIONS}/${sectionId}/weeks`)
export const setUpActiveWeeks = (sectionId: number, activeWeeks: string[]) =>
  request.post<any, SetUpActiveWeeksResponse>(`${API.SECTIONS}/${sectionId}/weeks`, activeWeeks)
export const sendEmailInvitationsToStudents = (courseId: number, sectionId: number, emails: string[]) =>
  request.post<any, SendEmailInvitationsResponse>(`${API.SECTIONS}/${sectionId}/students/email-invitations`, emails, { params: { courseId } })
export const inviteOrAddInstructors = (courseId: number, sectionId: number, emails: string[]) =>
  request.post<any, InviteOrAddInstructorsResponse>(`${API.SECTIONS}/${sectionId}/instructors/invite-or-add`, emails, { params: { courseId } })
export const getInstructors = (sectionId: number) =>
  request.get<any, GetInstructorsResponse>(`${API.SECTIONS}/${sectionId}/instructors`)
export const removeInstructorFromSection = (sectionId: number, instructorId: number) =>
  request.delete<any, RemoveInstructorResponse>(`${API.SECTIONS}/${sectionId}/instructors/${instructorId}`)
