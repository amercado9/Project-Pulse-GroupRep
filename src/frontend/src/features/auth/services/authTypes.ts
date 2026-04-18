export interface UserInfo {
  id: number
  firstName: string
  lastName: string
  email: string
  roles: string
}

export interface LoginData {
  username: string
  password: string
}

export interface LoginResponse {
  flag: boolean
  code: number
  message: string
  data: {
    userInfo: UserInfo
    token: string
  }
}

export interface RegisterRequest {
  firstName: string
  lastName: string
  email: string
  password: string
}

export interface RegisterResponse {
  flag: boolean
  code: number
  message: string
}

export interface ChangePasswordRequest {
  currentPassword: string
  newPassword: string
}

export interface ChangePasswordResponse {
  flag: boolean
  code: number
  message: string
}

export interface SendResetPasswordEmailResponse {
  flag: boolean
  code: number
  message: string
}

export interface ResetUserPasswordResponse {
  flag: boolean
  code: number
  message: string
}
