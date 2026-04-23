export interface TeamSummary {
  teamId: number
  sectionId: number
  sectionName: string
  teamName: string
  teamDescription: string | null
  teamWebsiteUrl: string | null
  teamMemberNames: string[]
  instructorNames: string[]
}

export interface TeamDetail {
  teamId: number
  sectionId: number
  sectionName: string
  teamName: string
  teamDescription: string | null
  teamWebsiteUrl: string | null
  teamMembers: TeamMemberDetail[]
  teamMemberNames: string[]
  instructorNames: string[]
}

export interface TeamMemberDetail {
  studentId: number
  fullName: string
  email: string
}

export interface CreateTeamRequest {
  sectionId: number
  teamName: string
  teamDescription: string | null
  teamWebsiteUrl: string | null
}

export interface UpdateTeamRequest {
  teamName: string
  teamDescription: string | null
  teamWebsiteUrl: string | null
}

export interface StudentAssignmentCandidate {
  studentId: number
  fullName: string
  email: string
}

export interface TeamStudentAssignmentTeam {
  teamId: number
  teamName: string
  assignedStudents: StudentAssignmentCandidate[]
}

export interface TeamStudentAssignmentWorkspace {
  sectionId: number
  sectionName: string
  teams: TeamStudentAssignmentTeam[]
  students: StudentAssignmentCandidate[]
}

export interface TeamStudentAssignmentInput {
  teamId: number
  studentIds: number[]
}

export interface UpdateTeamStudentAssignmentsRequest {
  sectionId: number
  assignments: TeamStudentAssignmentInput[]
}

export interface FindTeamsParams {
  sectionId?: number
  sectionName?: string
  teamName?: string
  instructor?: string
}
