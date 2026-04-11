import request from '@/utils/request'
import type {
  Rubric, FindRubricByIdResponse, CreateRubricResponse, UpdateRubricResponse,
  RubricSearchCriteria, SearchRubricByCriteriaResponse, Criterion,
  SearchEvaluationCriteriaByCriteriaResponse, FindCriterionByIdResponse,
  CreateCriterionResponse, UpdateCriterionResponse, EvaluationCriterionSearchCriteria,
  AssignCriterionToRubricResponse, UnassignCriterionFromRubricResponse
} from './types'

enum API { CRITERIA = '/criteria', SEARCH_CRITERIA = '/criteria/search', RUBRICS = '/rubrics', SEARCH_RUBRICS = '/rubrics/search' }

export const searchEvaluationCriteria = (criteria: EvaluationCriterionSearchCriteria) =>
  request.post<any, SearchEvaluationCriteriaByCriteriaResponse>(API.SEARCH_CRITERIA, criteria)
export const findCriterionById = (id: number) => request.get<any, FindCriterionByIdResponse>(`${API.CRITERIA}/${id}`)
export const createCriterion = (c: Criterion) => request.post<any, CreateCriterionResponse>(API.CRITERIA, c)
export const updateCriterion = (c: Criterion) => request.put<any, UpdateCriterionResponse>(`${API.CRITERIA}/${c.criterionId}`, c)
export const searchRubrics = (criteria: RubricSearchCriteria) => request.post<any, SearchRubricByCriteriaResponse>(API.SEARCH_RUBRICS, criteria)
export const findRubricById = (id: number) => request.get<any, FindRubricByIdResponse>(`${API.RUBRICS}/${id}`)
export const createRubric = (r: Rubric) => request.post<any, CreateRubricResponse>(API.RUBRICS, r)
export const updateRubric = (r: Rubric) => request.put<any, UpdateRubricResponse>(`${API.RUBRICS}/${r.rubricId}`, r)
export const assignCriterionToRubric = (rubricId: number, criterionId: number) =>
  request.put<any, AssignCriterionToRubricResponse>(`${API.RUBRICS}/${rubricId}/criteria/${criterionId}`, {})
export const unassignCriterionFromRubric = (rubricId: number, criterionId: number) =>
  request.delete<any, UnassignCriterionFromRubricResponse>(`${API.RUBRICS}/${rubricId}/criteria/${criterionId}`)
