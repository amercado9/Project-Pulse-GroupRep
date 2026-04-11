export interface PeerEvaluation {
  evaluationId?: number
  week: string
  evaluatorId?: number
  evaluatorName?: string
  evaluateeId: number
  evaluateeName?: string
  ratings: Rating[]
  totalScore?: number
  publicComment?: string
  privateComment?: string
  createdAt?: string
  updatedAt?: string
}

export interface Rating {
  ratingId?: number; criterionId: number; criterion: string; actualScore: number
}

export interface PeerEvaluationAverage {
  studentId: number; week: string; firstName: string; lastName: string
  email: string; teamName: string; averageTotalScore: number
  publicComments: string[]; privateComments: string[]
  ratingAverages: RatingAverage[]
}

export interface RatingAverage { criterionId: number; averageScore: number }
export interface PeriodParams { startWeek: string; endWeek: string }

export interface WeeklyPeerEvaluationReportForOneSection {
  sectionName: string; week: string
  peerEvaluationAverages: PeerEvaluationAverage[]
  studentsMissingPeerEvaluations: string[]
}

export interface FetchPeerEvaluationsResponse { flag: boolean; code: number; message: string; data: PeerEvaluation[] }
export interface SubmitPeerEvaluationResponse { flag: boolean; code: number; message: string; data: PeerEvaluation }
export interface UpdatePeerEvaluationResponse { flag: boolean; code: number; message: string; data: PeerEvaluation }
export interface GeneratePeerEvaluationAveragesForOneStudentResponse { flag: boolean; code: number; message: string; data: PeerEvaluationAverage[] }
export interface GenerateWeeklyPeerEvaluationAveragesForSectionResponse { flag: boolean; code: number; message: string; data: WeeklyPeerEvaluationReportForOneSection }
export interface GetWeeklyPeerEvaluationsForStudentResponse { flag: boolean; code: number; message: string; data: PeerEvaluation[] }
