import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { TeamDeletionNotification } from '../services/teamTypes'

export const useTeamNotificationsStore = defineStore('teamNotifications', () => {
  const deletedTeamNotification = ref<TeamDeletionNotification | null>(null)

  function setDeletedTeamNotification(notification: TeamDeletionNotification) {
    deletedTeamNotification.value = notification
  }

  function clearDeletedTeamNotification() {
    deletedTeamNotification.value = null
  }

  return {
    deletedTeamNotification,
    setDeletedTeamNotification,
    clearDeletedTeamNotification
  }
})
