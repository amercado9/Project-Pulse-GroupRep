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
