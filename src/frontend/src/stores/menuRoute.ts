import { defineStore } from 'pinia'
import { ref } from 'vue'
import { routes } from '@/router/routes'
import type { RouteRecordRaw } from 'vue-router'

export const useMenuRouteStore = defineStore(
  'menuRoute',
  () => {
    const menuRoute = ref<RouteRecordRaw>(routes.find((route) => route.name === 'dashboard')!)
    return { menuRoute }
  },
  { persist: true }
)
