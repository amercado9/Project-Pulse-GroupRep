import request from '@/utils/request'
import type {
  PeerEvaluation, SubmitPeerEvaluationResponse, FetchPeerEvaluationsResponse,
  UpdatePeerEvaluationResponse, GeneratePeerEvaluationAveragesForOneStudentResponse,
  PeriodParams, GenerateWeeklyPeerEvaluationAveragesForSectionResponse,
  GetWeeklyPeerEvaluationsForStudentResponse
} from './types'

enum API { EVALUATIONS = '/evaluations' }

export const submitEvaluation = (evaluation: PeerEvaluation) =>
  request.post<any, SubmitPeerEvaluationResponse>(API.EVALUATIONS, evaluation)

export const getEvaluationsByEvaluatorIdAndWeek = (evaluatorId: number, week: string) =>
  request.get<any, FetchPeerEvaluationsResponse>(`${API.EVALUATIONS}/evaluators/${evaluatorId}/week/${week}`)

export const updateEvaluation = (evaluation: PeerEvaluation) =>
  request.put<any, UpdatePeerEvaluationResponse>(`${API.EVALUATIONS}/${evaluation.evaluationId}`, evaluation)

export const generateWeeklyPeerEvaluationAveragesForStudentWithinOnePeriod = (studentId: number, params: PeriodParams) =>
  request.get<any, GeneratePeerEvaluationAveragesForOneStudentResponse>(`${API.EVALUATIONS}/students/${studentId}`, { params })

export const generateWeeklyPeerEvaluationAveragesForSection = (sectionId: number, week: string) =>
  request.get<any, GenerateWeeklyPeerEvaluationAveragesForSectionResponse>(`${API.EVALUATIONS}/sections/${sectionId}/week/${week}`)

export const getWeeklyPeerEvaluationsForStudent = (studentId: number, week: string) =>
  request.get<any, GetWeeklyPeerEvaluationsForStudentResponse>(`${API.EVALUATIONS}/students/${studentId}/week/${week}/details`)

export const sendPeerEvaluationSubmissionConfirmationEmail = (week: string) =>
  request.post<any, void>(`${API.EVALUATIONS}/weeks/${week}/receipt`)
