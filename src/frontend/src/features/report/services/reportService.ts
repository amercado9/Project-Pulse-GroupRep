import request from '@/shared/utils/request'
import type { ApiResponse } from '@/shared/types/api'
import type { StudentPeerEvaluationReportResponse, TeamWarReportResponse } from './reportTypes'

const BASE = '/teams'
const REPORT_BASE = '/reports'

export const getTeamWarReport = (teamId: number, week: string) =>
  request.get<any>(`${BASE}/${teamId}/war-report`, { params: { week } })

export const getMyPeerEvaluationReport = (week?: string) =>
  request.get<ApiResponse<StudentPeerEvaluationReportResponse>>(`${REPORT_BASE}/peer-evaluations/me`, {
    params: week ? { week } : undefined
  }) as unknown as Promise<ApiResponse<StudentPeerEvaluationReportResponse>>
