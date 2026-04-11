export interface Criterion {
  criterionId?: number; criterion: string; description: string; maxScore: number; courseId?: number
}
export interface EvaluationCriterionSearchCriteria { criterionId?: number; criterion?: string }
export interface Rubric { rubricId?: number; rubricName: string; criteria?: Criterion[]; courseId?: number }
export interface RubricSearchCriteria { rubricId?: number; rubricName?: string }

export interface SearchEvaluationCriteriaByCriteriaResponse { flag: boolean; code: number; message: string; data: { content: Criterion[] } }
export interface FindCriterionByIdResponse { flag: boolean; code: number; message: string; data: Criterion }
export interface CreateCriterionResponse { flag: boolean; code: number; message: string; data: Criterion }
export interface UpdateCriterionResponse { flag: boolean; code: number; message: string; data: Criterion }
export interface SearchRubricByCriteriaResponse { flag: boolean; code: number; message: string; data: { content: Rubric[] } }
export interface FindRubricByIdResponse { flag: boolean; code: number; message: string; data: Rubric }
export interface CreateRubricResponse { flag: boolean; code: number; message: string; data: Rubric }
export interface UpdateRubricResponse { flag: boolean; code: number; message: string; data: Rubric }
export interface AssignCriterionToRubricResponse { flag: boolean; code: number; message: string }
export interface UnassignCriterionFromRubricResponse { flag: boolean; code: number; message: string }
