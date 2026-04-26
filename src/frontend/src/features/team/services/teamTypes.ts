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
  teamInstructors: TeamInstructorDetail[]
  instructorNames: string[]
}

export interface TeamMemberDetail {
  studentId: number
  fullName: string
  email: string
}

export interface TeamInstructorDetail {
  instructorId: number
  fullName: string
  email: string
}

export interface TeamNotificationRecipient {
  fullName: string
  email?: string
}

export interface TeamDeletionNotification {
  teamId: number
  teamName: string
  sectionName: string
  studentNotifications: TeamNotificationRecipient[]
  instructorNotifications: TeamNotificationRecipient[]
  status: string
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

export interface InstructorAssignmentCandidate {
  instructorId: number
  fullName: string
  email: string
  assignedTeamNames: string[]
}

export interface TeamInstructorAssignmentInstructor {
  instructorId: number
  fullName: string
  email: string
}

export interface TeamInstructorAssignmentTeam {
  teamId: number
  teamName: string
  assignedInstructors: TeamInstructorAssignmentInstructor[]
}

export interface TeamInstructorAssignmentWorkspace {
  sectionId: number
  sectionName: string
  teams: TeamInstructorAssignmentTeam[]
  instructors: InstructorAssignmentCandidate[]
}

export interface TeamStudentAssignmentInput {
  teamId: number
  studentIds: number[]
}

export interface TeamInstructorAssignmentInput {
  teamId: number
  instructorIds: number[]
}

export interface UpdateTeamStudentAssignmentsRequest {
  sectionId: number
  assignments: TeamStudentAssignmentInput[]
}

export interface UpdateTeamInstructorAssignmentsRequest {
  sectionId: number
  assignments: TeamInstructorAssignmentInput[]
}

export interface FindTeamsParams {
  sectionId?: number
  sectionName?: string
  teamName?: string
  instructor?: string
}
