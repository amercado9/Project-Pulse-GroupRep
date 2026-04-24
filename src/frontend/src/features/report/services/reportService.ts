import request from '@/shared/utils/request'

const BASE = '/teams'

export const getTeamWarReport = (teamId: number, week: string) =>
  request.get<any>(`${BASE}/${teamId}/war-report`, { params: { week } })

export const getStudentWarReport = (teamId: number, studentId: number, startWeek: string, endWeek: string) =>
  request.get<any>(`${BASE}/${teamId}/students/${studentId}/war-report`, { params: { startWeek, endWeek } })
