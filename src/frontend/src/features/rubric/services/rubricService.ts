import request from '@/shared/utils/request'
import type { CreateRubricRequest, Rubric } from './rubricTypes'

const BASE = '/rubrics'

export const createRubric = (payload: CreateRubricRequest) =>
  request.post<Rubric>(BASE, payload)

export const getAllRubrics = () =>
  request.get<Rubric[]>(BASE)

export const getRubricById = (id: number) =>
  request.get<Rubric>(`${BASE}/${id}`)
