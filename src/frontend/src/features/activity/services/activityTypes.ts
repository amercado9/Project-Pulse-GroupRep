export type ActivityCategory =
  | 'DEVELOPMENT'
  | 'TESTING'
  | 'BUGFIX'
  | 'COMMUNICATION'
  | 'DOCUMENTATION'
  | 'DESIGN'
  | 'PLANNING'
  | 'LEARNING'
  | 'DEPLOYMENT'
  | 'SUPPORT'
  | 'MISCELLANEOUS'

export type ActivityStatus = 'IN_PROGRESS' | 'UNDER_TESTING' | 'DONE'

export interface ActivityWeekOption {
  week: string
  label: string
  activeWeek: boolean
}

export interface ActivityDetail {
  activityId: number
  week: string
  category: ActivityCategory
  plannedActivity: string
  description: string
  plannedHours: number
  actualHours: number
  status: ActivityStatus
}

export interface ActivityWorkspace {
  sectionId: number | null
  sectionName: string | null
  teamId: number | null
  teamName: string | null
  selectedWeek: string | null
  availableWeeks: ActivityWeekOption[]
  activities: ActivityDetail[]
  canManageActivities: boolean
  availabilityMessage: string | null
}

export interface CreateActivityRequest {
  week: string
  category: ActivityCategory | null
  plannedActivity: string
  description: string
  plannedHours: number | null
  actualHours: number | null
  status: ActivityStatus | null
}

export interface UpdateActivityRequest {
  category: ActivityCategory | null
  plannedActivity: string
  description: string
  plannedHours: number | null
  actualHours: number | null
  status: ActivityStatus | null
}
