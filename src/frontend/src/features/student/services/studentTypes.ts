export interface StudentSummary {
  studentId: number
  firstName: string
  lastName: string
  email: string
  enabled: boolean
  sectionId: number | null
  sectionName: string | null
}

export interface StudentDetail {
  studentId: number
  firstName: string
  lastName: string
  email: string
  enabled: boolean
  sectionId: number | null
  sectionName: string | null
  teamNames: string[]
}

export interface StudentUpdateDto {
  firstName: string
  lastName: string
  email: string
  password?: string
}

export interface StudentDeletionNotification {
  studentId: number
  fullName: string
  email: string
  sectionName: string | null
  teamNames: string[]
}
