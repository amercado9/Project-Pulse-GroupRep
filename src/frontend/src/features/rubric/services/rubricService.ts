import request from '@/shared/utils/request'
import type { ApiResponse } from '@/shared/types/api'
import type { CreateRubricRequest, Rubric } from './rubricTypes'

const BASE = '/rubrics'

export const createRubric = (payload: CreateRubricRequest) =>
  request.post<ApiResponse<Rubric>>(BASE, payload)

export const getAllRubrics = () =>
  request.get<ApiResponse<Rubric[]>>(BASE)

export const getRubricById = (id: number) =>
  request.get<ApiResponse<Rubric>>(`${BASE}/${id}`)
