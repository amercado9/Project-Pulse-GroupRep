export type DocumentType = 'VISION_SCOPE' | 'USE_CASES' | 'USER_STORIES' | 'BUSINESS_RULES' | 'SRS' | 'GLOSSARY'
export type DocumentStatus = 'DRAFT' | 'SUBMITTED' | 'RETURNED'

export interface PaginationParams { page: number; size: number }

export interface PeerEvaluationUserRef { id: number; name: string }

export interface RequirementDocumentSectionLock {
  locked: boolean; documentSectionId: number
  lockedBy?: PeerEvaluationUserRef | null
  lockedAt?: string | null; expiresAt?: string | null; reason?: string | null
}

export interface RequirementArtifactSummary {
  id?: number; type: string; artifactKey?: string | null; title: string
  content: string; priority?: string | null; sourceSectionId?: number | null; notes?: string | null
}

export interface RequirementArtifact extends RequirementArtifactSummary {
  createdAt?: string | null; updatedAt?: string | null
  createdBy?: PeerEvaluationUserRef | null; updatedBy?: PeerEvaluationUserRef | null
}

export interface RequirementDocumentSection {
  id: number; sectionKey: string; type: string; title: string; content?: string | null
  requirementArtifacts?: RequirementArtifactSummary[]
  guidance?: string | null; createdAt?: string | null; updatedAt?: string | null
  version?: number | null; lock?: RequirementDocumentSectionLock | null
}

export interface RequirementDocumentSummary {
  id: number; type: DocumentType; teamId: number; documentKey: string
  title: string; status: DocumentStatus; version: number
}

export interface RequirementDocument {
  id: number; type: DocumentType; teamId: number; documentKey: string
  title: string; sections: RequirementDocumentSection[]; status: DocumentStatus; version: number
}

export interface Condition {
  id?: number; condition: string; type: string; priority?: string | null; notes?: string | null
}

export interface UseCaseExtensionStep { id?: number; actor: string; actionText: string }

export interface UseCaseExtension {
  id?: number; conditionText: string
  kind: 'ALTERNATE' | 'EXCEPTION'; exit: 'RESUME' | 'END_SUCCESS' | 'END_FAILURE'
  steps: UseCaseExtensionStep[]
}

export interface UseCaseMainStep {
  id?: number; actor: string; actionText: string; extensions: UseCaseExtension[]
}

export interface UseCaseLock {
  locked: boolean; useCaseId: number
  lockedBy?: PeerEvaluationUserRef | null
  lockedAt?: string | null; expiresAt?: string | null; reason?: string | null
}

export interface UseCase {
  id?: number; artifactKey?: string | null; title: string; description: string
  teamId: number; primaryActorId: number; secondaryActorIds?: number[]
  trigger: string; preconditions?: Condition[]; postconditions?: Condition[]
  mainSteps: UseCaseMainStep[]; priority?: string | null; notes?: string | null
  createdAt?: string | null; updatedAt?: string | null
  createdBy?: PeerEvaluationUserRef | null; updatedBy?: PeerEvaluationUserRef | null
  version?: number | null; lock?: UseCaseLock | null
}

export interface UseCaseLockRequest { reason?: string }

export interface SearchDocumentsResponse { flag: boolean; code: number; message: string; data: any }
export interface FindDocumentByIdResponse { flag: boolean; code: number; message: string; data: RequirementDocument }
export interface FindDocumentSectionByIdResponse { flag: boolean; code: number; message: string; data: RequirementDocumentSection }
export interface CreateRequirementDocumentRequest { type: DocumentType }
export interface CreateDocumentResponse { flag: boolean; code: number; message: string; data: RequirementDocument }
export interface UpdateDocumentSectionRequest { type?: string; content?: string; requirementArtifacts?: RequirementArtifactSummary[]; version?: number | null }
export interface UpdateDocumentSectionResponse { flag: boolean; code: number; message: string; data: RequirementDocumentSection }
export interface LockDocumentSectionRequest { reason?: string }
export interface GetDocumentSectionLockResponse { flag: boolean; code: number; message: string; data: RequirementDocumentSectionLock }
export interface LockDocumentSectionResponse { flag: boolean; code: number; message: string; data: RequirementDocumentSectionLock }
export interface SearchRequirementArtifactsResponse { flag: boolean; code: number; message: string; data: any }
export interface UseCaseResponse { flag: boolean; code: number; message: string; data: UseCase }
export interface UseCaseLockResponse { flag: boolean; code: number; message: string; data: UseCaseLock }
export interface GlossaryTermResponse { flag: boolean; code: number; message: string; data: RequirementArtifact }
