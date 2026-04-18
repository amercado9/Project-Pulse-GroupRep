export interface ApiResponse<T = null> {
  flag: boolean
  code: number
  message: string
  data: T
}
