import request from '@/shared/utils/request'
import type { ApiResponse } from '@/shared/types/api'
import type {
  EvaluationWorkspace,
  PeerEvaluationSubmissionDetail,
  PeerEvaluationSubmissionRequest
} from './evaluationTypes'

const BASE = '/peer-evaluations'

export const getPeerEvaluationWorkspace = () =>
  request.get<ApiResponse<EvaluationWorkspace>>(`${BASE}/workspace`) as unknown as Promise<ApiResponse<EvaluationWorkspace>>

export const createPeerEvaluation = (payload: PeerEvaluationSubmissionRequest) =>
  request.post<ApiResponse<PeerEvaluationSubmissionDetail>>(BASE, payload) as unknown as Promise<ApiResponse<PeerEvaluationSubmissionDetail>>

export const updatePeerEvaluation = (submissionId: number, payload: PeerEvaluationSubmissionRequest) =>
  request.put<ApiResponse<PeerEvaluationSubmissionDetail>>(`${BASE}/${submissionId}`, payload) as unknown as Promise<ApiResponse<PeerEvaluationSubmissionDetail>>
