import request from '@/utils/request'
import type {
  PaginationParams, ActivitySearchCriteria, Activity,
  SearchActivityByCriteriaResponse, CreateActivityResponse,
  UpdateActivityResponse, DeleteActivityResponse, FindActivityByIdResponse, CommentActivityResponse
} from './types'

enum API { SEARCH = '/activities/search', ACTIVITIES = '/activities' }

export const searchActivities = (params: PaginationParams, criteria: ActivitySearchCriteria) =>
  request.post<any, SearchActivityByCriteriaResponse>(API.SEARCH, criteria, { params })

export const findActivityById = (id: number) =>
  request.get<any, FindActivityByIdResponse>(`${API.ACTIVITIES}/${id}`)

export const createActivity = (activity: Activity) =>
  request.post<any, CreateActivityResponse>(API.ACTIVITIES, activity)

export const updateActivity = (activity: Activity) =>
  request.put<any, UpdateActivityResponse>(`${API.ACTIVITIES}/${activity.activityId}`, activity)

export const deleteActivity = (id: number) =>
  request.delete<any, DeleteActivityResponse>(`${API.ACTIVITIES}/${id}`)

export const commentActivity = (id: number, comment: string) =>
  request.patch<any, CommentActivityResponse>(`${API.ACTIVITIES}/${id}/comments`, { comment })
