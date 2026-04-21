import request from '@/shared/utils/request'
import type { ApiResponse } from '@/shared/types/api'
import type { FindTeamsParams, TeamDetail, TeamSummary } from './teamTypes'

const BASE = '/teams'

export const findTeams = (params?: FindTeamsParams) =>
  request.get<ApiResponse<TeamSummary[]>>(BASE, { params: params ?? {} })

export const getTeam = (id: number) =>
  request.get<ApiResponse<TeamDetail>>(`${BASE}/${id}`)
