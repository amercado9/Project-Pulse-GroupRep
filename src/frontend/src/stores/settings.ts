import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useSettingsStore = defineStore(
  'settings',
  () => {
    const defaultSectionId = ref<number>(NaN)
    const setDefaultSectionId = (id: number) => { defaultSectionId.value = id }
    const removeDefaultSectionId = () => { defaultSectionId.value = NaN }

    const defaultCourseId = ref<number>(NaN)
    const setDefaultCourseId = (id: number) => { defaultCourseId.value = id }
    const setDefaultCourseIdAndResetSection = (id: number) => {
      defaultCourseId.value = id
      removeDefaultSectionId()
    }
    const removeDefaultCourseId = () => { defaultCourseId.value = NaN }

    return {
      defaultSectionId,
      setDefaultSectionId,
      removeDefaultSectionId,
      defaultCourseId,
      setDefaultCourseId,
      setDefaultCourseIdAndResetSection,
      removeDefaultCourseId
    }
  },
  { persist: true }
)
