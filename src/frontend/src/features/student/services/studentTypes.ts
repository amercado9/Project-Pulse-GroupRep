export interface StudentSummary {
  id: number
  firstName: string
  lastName: string
  email: string
  sectionName: string | null
  teamNames: string[]
}

export interface FindStudentsParams {
  firstName?: string
  lastName?: string
  email?: string
  sectionName?: string
  teamName?: string
}
