import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { StudentDeletionNotification } from '../services/studentTypes'

export const useStudentNotificationsStore = defineStore('studentNotifications', () => {
  const deletedStudentNotification = ref<StudentDeletionNotification | null>(null)

  function setDeletedStudentNotification(notification: StudentDeletionNotification) {
    deletedStudentNotification.value = notification
  }

  function clearDeletedStudentNotification() {
    deletedStudentNotification.value = null
  }

  return {
    deletedStudentNotification,
    setDeletedStudentNotification,
    clearDeletedStudentNotification
  }
})
