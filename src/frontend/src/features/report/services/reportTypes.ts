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

export interface StudentWeekWarReport {
  week: string
  activities: WarReportRow[]
}

export interface StudentWarReportResponse {
  studentId: number
  studentName: string
  startWeek: string
  endWeek: string
  weekReports: StudentWeekWarReport[]
}

export interface EvalReportEntry {
  evaluatorName: string
  totalScore: number
  maxScore: number
  publicComment: string | null
  privateComment: string | null
}

export interface StudentPeerEvalReport {
  studentId: number
  studentName: string
  teamName: string
  submitted: boolean
  grade: number | null
  maxGrade: number | null
  evaluations: EvalReportEntry[]
}

export interface SectionPeerEvalReportResponse {
  sectionId: number
  sectionName: string
  week: string
  studentReports: StudentPeerEvalReport[]
}
