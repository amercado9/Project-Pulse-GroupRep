export interface StudentTeamInfo {
  teamId: number
  teamName: string
  sectionId: number
  sectionName: string
}

export interface StudentSummary {
  id: number
  firstName: string
  lastName: string
  teamNames: string[]
}

export interface StudentDetail {
  id: number
  firstName: string
  lastName: string
  email: string
  teams: StudentTeamInfo[]
  peerEvaluations: unknown[]
  wars: unknown[]
}

export interface FindStudentsParams {
  firstName?: string
  lastName?: string
  email?: string
  teamId?: number
  teamName?: string
  sectionId?: number
  sectionName?: string
}
