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

export interface FindTeamsParams {
  sectionId?: number
  sectionName?: string
  teamName?: string
  instructor?: string
}
