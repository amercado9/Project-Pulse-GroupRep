import request from '@/shared/utils/request'
import type { ApiResponse } from '@/shared/types/api'

const base = (sectionId: number) => `/sections/${sectionId}/invites`

export interface InvitePreview {
  emails: string[]
  emailCount: number
  subject: string
  body: string
}

export interface InviteSendResult {
  sentCount: number
}

export const previewInvites = (sectionId: number, emailsInput: string) =>
  request.post<ApiResponse<InvitePreview>>(`${base(sectionId)}/preview`, { emailsInput })

export const sendInvites = (sectionId: number, emails: string[]) =>
  request.post<ApiResponse<InviteSendResult>>(`${base(sectionId)}/send`, { emails })
