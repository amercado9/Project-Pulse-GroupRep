export interface UpdateSectionRequest {
  sectionName: string
  startDate: string | null
  endDate: string | null
  rubricId: number | null
}

export interface CreateSectionRequest {
  sectionName: string
  startDate: string | null
  endDate: string | null
  rubricId: number | null
}

export interface SectionSummary {
  sectionId: number
  sectionName: string
  teamNames: string[]
}

export interface TeamInfo {
  teamId: number
  teamName: string
  members: string[]
  instructors: string[]
}

export interface SectionDetail {
  sectionId: number
  sectionName: string
  startDate: string | null
  endDate: string | null
  active: boolean
  rubricId: number | null
  rubricName: string | null
  teams: TeamInfo[]
  unassignedStudents: string[]
  unassignedInstructors: string[]
}
