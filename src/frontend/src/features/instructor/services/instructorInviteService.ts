import request from '@/shared/utils/request'
import type { ApiResponse } from '@/shared/types/api'

export interface InstructorInvitePreview {
  emails: string[]
  emailCount: number
  subject: string
  body: string
}

export interface InstructorInviteLink {
  email: string
  link: string
}

export interface InstructorInviteLinksResult {
  links: InstructorInviteLink[]
}

export const previewInstructorInvites = (emailsInput: string) =>
  request.post<ApiResponse<InstructorInvitePreview>>('/instructors/invites/preview', { emailsInput })

export const generateInstructorInviteLinks = (emails: string[]) =>
  request.post<ApiResponse<InstructorInviteLinksResult>>('/instructors/invites/send', { emails })
