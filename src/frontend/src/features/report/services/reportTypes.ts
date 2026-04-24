export interface WarReportRow {
  activityCategory: string
  plannedActivity: string
  description: string | null
  plannedHours: number | null
  actualHours: number | null
  status: string
}

export interface StudentWarReport {
  studentId: number
  studentName: string
  submitted: boolean
  activities: WarReportRow[]
}

export interface TeamWarReportResponse {
  teamId: number
  teamName: string
  week: string
  studentReports: StudentWarReport[]
}

export interface ReportWeekOption {
  week: string
  label: string
  rangeLabel: string
}

export interface StudentPeerEvaluationCriterionAverage {
  criterionId: number
  criterion: string
  description: string | null
  averageScore: number
  maxScore: number
}

export interface StudentPeerEvaluationReportResponse {
  sectionId: number | null
  sectionName: string | null
  teamId: number | null
  teamName: string | null
  studentId: number
  studentName: string
  selectedWeek: string
  selectedWeekLabel: string
  selectedWeekRangeLabel: string
  availableWeeks: ReportWeekOption[]
  reportAvailable: boolean
  availabilityMessage: string | null
  criterionAverages: StudentPeerEvaluationCriterionAverage[]
  publicComments: string[]
  overallGrade: number | null
  maxTotalScore: number | null
}
