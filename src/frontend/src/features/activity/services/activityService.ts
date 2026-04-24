import request from '@/shared/utils/request'
import type { ApiResponse } from '@/shared/types/api'
import type { ActivityDetail, ActivityWorkspace, CreateActivityRequest, UpdateActivityRequest } from './activityTypes'

const BASE = '/activities'

export const getActivityWorkspace = (week?: string) =>
  request.get<ApiResponse<ActivityWorkspace>>(`${BASE}/workspace`, { params: week ? { week } : {} })

export const createActivity = (payload: CreateActivityRequest) =>
  request.post<ApiResponse<ActivityDetail>>(BASE, payload)

export const updateActivity = (activityId: number, payload: UpdateActivityRequest) =>
  request.put<ApiResponse<ActivityDetail>>(`${BASE}/${activityId}`, payload)

export const deleteActivity = (activityId: number) =>
  request.delete<ApiResponse<null>>(`${BASE}/${activityId}`)
