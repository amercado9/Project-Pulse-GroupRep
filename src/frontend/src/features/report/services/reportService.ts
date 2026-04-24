import request from '@/shared/utils/request'
import type { TeamWarReportResponse } from './reportTypes'

const BASE = '/teams'

export const getTeamWarReport = (teamId: number, week: string) =>
  request.get<any>(`${BASE}/${teamId}/war-report`, { params: { week } })
