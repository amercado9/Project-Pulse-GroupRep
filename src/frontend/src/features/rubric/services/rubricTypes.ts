export interface CriterionRequest {
  criterion: string
  description: string
  maxScore: number
}

export interface CreateRubricRequest {
  rubricName: string
  criteria: CriterionRequest[]
}

export interface Criterion {
  criterionId: number
  criterion: string
  description: string
  maxScore: number
}

export interface Rubric {
  rubricId: number
  rubricName: string
  criteria: Criterion[]
}
