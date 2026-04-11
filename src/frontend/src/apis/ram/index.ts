import request from '@/utils/request'
import type {
  PaginationParams, SearchDocumentsResponse, CreateRequirementDocumentRequest,
  CreateDocumentResponse, FindDocumentByIdResponse, FindDocumentSectionByIdResponse,
  UpdateDocumentSectionResponse, LockDocumentSectionRequest, LockDocumentSectionResponse,
  GetDocumentSectionLockResponse, SearchRequirementArtifactsResponse,
  UseCaseResponse, UseCase, UseCaseLockRequest, UseCaseLockResponse,
  GlossaryTermResponse, RequirementArtifact, UpdateDocumentSectionRequest
} from './types'

enum API { TEAMS = '/teams' }

export const searchDocuments = (teamId: number, params: PaginationParams, criteria: Record<string, string>) =>
  request.post<any, SearchDocumentsResponse>(`${API.TEAMS}/${teamId}/documents/search`, criteria, { params })

export const createDocument = (teamId: number, body: CreateRequirementDocumentRequest) =>
  request.post<any, CreateDocumentResponse>(`${API.TEAMS}/${teamId}/documents`, body)

export const findDocumentById = (teamId: number, documentId: number) =>
  request.get<any, FindDocumentByIdResponse>(`${API.TEAMS}/${teamId}/documents/${documentId}`)

export const findDocumentSectionById = (teamId: number, documentId: number, sectionId: number) =>
  request.get<any, FindDocumentSectionByIdResponse>(`${API.TEAMS}/${teamId}/documents/${documentId}/document-sections/${sectionId}`)

export const searchRequirementArtifacts = (teamId: number, params: PaginationParams, criteria: Record<string, string>) =>
  request.post<any, SearchRequirementArtifactsResponse>(`${API.TEAMS}/${teamId}/requirement-artifacts/search`, criteria, { params })

export const updateDocumentSection = (teamId: number, documentId: number, sectionId: number, section: UpdateDocumentSectionRequest) =>
  request.put<any, UpdateDocumentSectionResponse>(`${API.TEAMS}/${teamId}/documents/${documentId}/document-sections/${sectionId}`, section)

export const getDocumentSectionLock = (teamId: number, documentId: number, sectionId: number) =>
  request.get<any, GetDocumentSectionLockResponse>(`${API.TEAMS}/${teamId}/documents/${documentId}/document-sections/${sectionId}/lock`)

export const lockDocumentSection = (teamId: number, documentId: number, sectionId: number, body: LockDocumentSectionRequest) =>
  request.put<any, LockDocumentSectionResponse>(`${API.TEAMS}/${teamId}/documents/${documentId}/document-sections/${sectionId}/lock`, body)

export const unlockDocumentSection = (teamId: number, documentId: number, sectionId: number) =>
  request.delete<any, LockDocumentSectionResponse>(`${API.TEAMS}/${teamId}/documents/${documentId}/document-sections/${sectionId}/lock`)

export const getUseCaseById = (teamId: number, useCaseId: number) =>
  request.get<any, UseCaseResponse>(`${API.TEAMS}/${teamId}/use-cases/${useCaseId}`)

export const createUseCase = (teamId: number, payload: UseCase) =>
  request.post<any, UseCaseResponse>(`${API.TEAMS}/${teamId}/use-cases`, payload)

export const updateUseCase = (teamId: number, useCaseId: number, payload: UseCase) =>
  request.put<any, UseCaseResponse>(`${API.TEAMS}/${teamId}/use-cases/${useCaseId}`, payload)

export const getUseCaseLock = (teamId: number, useCaseId: number) =>
  request.get<any, UseCaseLockResponse>(`${API.TEAMS}/${teamId}/use-cases/${useCaseId}/lock`)

export const lockUseCase = (teamId: number, useCaseId: number, body: UseCaseLockRequest) =>
  request.put<any, UseCaseLockResponse>(`${API.TEAMS}/${teamId}/use-cases/${useCaseId}/lock`, body)

export const unlockUseCase = (teamId: number, useCaseId: number) =>
  request.delete<any, UseCaseLockResponse>(`${API.TEAMS}/${teamId}/use-cases/${useCaseId}/lock`)

export const getGlossaryTermById = (teamId: number, glossaryTermId: number) =>
  request.get<any, GlossaryTermResponse>(`${API.TEAMS}/${teamId}/glossary-terms/${glossaryTermId}`)

export const createGlossaryTerm = (teamId: number, payload: RequirementArtifact) =>
  request.post<any, GlossaryTermResponse>(`${API.TEAMS}/${teamId}/glossary-terms`, payload)

export const updateGlossaryTermDefinition = (teamId: number, termId: number, payload: RequirementArtifact) =>
  request.patch<any, GlossaryTermResponse>(`${API.TEAMS}/${teamId}/glossary-terms/${termId}`, payload)

export const renameGlossaryTerm = (teamId: number, termId: number, payload: RequirementArtifact) =>
  request.patch<any, GlossaryTermResponse>(`${API.TEAMS}/${teamId}/glossary-terms/${termId}/rename`, payload)
