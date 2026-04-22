import request from '@/shared/utils/request'
import type { ApiResponse } from '@/shared/types/api'
import type { CreateSectionRequest, SectionDetail, SectionSummary, UpdateSectionRequest } from './sectionTypes'

const BASE = '/sections'

export const createSection = (payload: CreateSectionRequest) =>
  request.post<ApiResponse<SectionDetail>>(BASE, payload)

export const findSections = (name?: string) =>
  request.get<ApiResponse<SectionSummary[]>>(BASE, { params: name ? { name } : {} })

export const updateSection = (id: number, payload: UpdateSectionRequest) =>
  request.put<ApiResponse<SectionDetail>>(`${BASE}/${id}`, payload)

export const getSection = (id: number) =>
  request.get<ApiResponse<SectionDetail>>(`${BASE}/${id}`)
