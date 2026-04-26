import request from '@/shared/utils/request'
import type { ApiResponse } from '@/shared/types/api'

export interface InstructorInvitePreview {
  emails: string[]
  emailCount: number
  subject: string
  body: string
}

export interface InstructorInviteSendResult {
  sentCount: number
}

export interface InstructorInviteSendPayload {
  emails: string[]
  subject: string
  body: string
}

export const previewInstructorInvites = (emailsInput: string) =>
  request.post<ApiResponse<InstructorInvitePreview>>('/instructors/invites/preview', { emailsInput })

export const sendInstructorInvites = (payload: InstructorInviteSendPayload) =>
  request.post<ApiResponse<InstructorInviteSendResult>>('/instructors/invites/send', payload)
