import request from '@/shared/utils/request'
import type { ApiResponse } from '@/shared/types/api'
import type { FindTeamsParams, TeamSummary } from './teamTypes'

const BASE = '/teams'

export const findTeams = (params?: FindTeamsParams) =>
  request.get<ApiResponse<TeamSummary[]>>(BASE, { params: params ?? {} })
