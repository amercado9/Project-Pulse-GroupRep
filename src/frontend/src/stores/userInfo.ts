import type { UserInfo } from '@/features/auth/services/authTypes'
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserInfoStore = defineStore(
  'userInfo',
  () => {
    const userInfo = ref<UserInfo | null>(null)

    const setUserInfo = (newUserInfo: UserInfo) => { userInfo.value = newUserInfo }
    const removeUserInfo = () => { userInfo.value = null }

    const roleList = computed<string[]>(() => {
      if (!userInfo.value) return []
      return userInfo.value.roles.split(/\s+/).map((r) => r.toLowerCase()).filter(Boolean)
    })

    const hasRole = (role: string) => roleList.value.includes(role.toLowerCase())

    const isAuthenticated = computed(() => userInfo.value !== null)
    const isAdmin = computed(() => hasRole('admin'))
    const isInstructor = computed(() => hasRole('instructor'))
    const isStudent = computed(() => hasRole('student'))

    return { userInfo, setUserInfo, removeUserInfo, isAuthenticated, roleList, hasRole, isAdmin, isInstructor, isStudent }
  },
  { persist: true }
)
