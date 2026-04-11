export interface Activity {
  activityId?: number
  studentId?: number
  studentName?: string
  week: string
  teamId?: number
  teamName?: string
  category: ActivityCategory
  activity: string
  description: string
  plannedHours: number
  actualHours: number
  status: ActivityStatus
  comments?: string
  createdAt?: string
  updatedAt?: string
}

export enum ActivityCategory {
  DEVELOPMENT = 'DEVELOPMENT',
  TESTING = 'TESTING',
  BUGFIX = 'BUGFIX',
  COMMUNICATION = 'COMMUNICATION',
  DOCUMENTATION = 'DOCUMENTATION',
  DESIGN = 'DESIGN',
  PLANNING = 'PLANNING',
  LEARNING = 'LEARNING',
  DEPLOYMENT = 'DEPLOYMENT',
  SUPPORT = 'SUPPORT',
  MISCELLANEOUS = 'MISCELLANEOUS'
}

export enum ActivityStatus {
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED'
}

export interface PaginationParams { page: number; size: number; sort?: string }

export interface ActivitySearchCriteria {
  studentId?: number; teamId?: number; week?: string
  category?: ActivityCategory; status?: ActivityStatus
  startWeek?: string; endWeek?: string
}

export interface SearchActivityByCriteriaResponse {
  flag: boolean; code: number; message: string
  data: { content: Activity[]; totalElements: number; totalPages: number }
}

export interface FindActivityByIdResponse { flag: boolean; code: number; message: string; data: Activity }
export interface CreateActivityResponse { flag: boolean; code: number; message: string; data: Activity }
export interface UpdateActivityResponse { flag: boolean; code: number; message: string; data: Activity }
export interface DeleteActivityResponse { flag: boolean; code: number; message: string; data: null }
export interface CommentActivityResponse { flag: boolean; code: number; message: string; data: null }
