export interface EvaluationCriterion {
  criterionId: number
  criterion: string
  description: string | null
  maxScore: number
}

export interface EvaluationCriterionDraftScore {
  criterionId: number
  score: number | null
}

export interface EvaluationMemberDraft {
  studentId: number
  fullName: string
  self: boolean
  publicComment: string | null
  privateComment: string | null
  scores: EvaluationCriterionDraftScore[]
}

export interface EvaluationWorkspace {
  sectionId: number | null
  sectionName: string | null
  teamId: number | null
  teamName: string | null
  week: string | null
  weekLabel: string | null
  weekRangeLabel: string | null
  submissionId: number | null
  submitted: boolean
  editable: boolean
  canSubmit: boolean
  availabilityMessage: string | null
  dueAt: string | null
  criteria: EvaluationCriterion[]
  members: EvaluationMemberDraft[]
}

export interface PeerEvaluationCriterionScoreSubmission {
  criterionId: number
  score: number | null
}

export interface PeerEvaluationMemberSubmission {
  evaluateeStudentId: number
  publicComment: string | null
  privateComment: string | null
  criterionScores: PeerEvaluationCriterionScoreSubmission[]
}

export interface PeerEvaluationSubmissionRequest {
  evaluations: PeerEvaluationMemberSubmission[]
}

export interface PeerEvaluationSubmissionDetail {
  submissionId: number
  week: string
  submittedAt: string
  updatedAt: string
}
