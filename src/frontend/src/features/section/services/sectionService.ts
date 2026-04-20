import request from '@/shared/utils/request'
import type { ApiResponse } from '@/shared/types/api'
import type { SectionDetail, SectionSummary } from './sectionTypes'

const BASE = '/sections'

export const findSections = (name?: string) =>
  request.get<ApiResponse<SectionSummary[]>>(BASE, { params: name ? { name } : {} })

export const getSection = (id: number) =>
  request.get<ApiResponse<SectionDetail>>(`${BASE}/${id}`)
