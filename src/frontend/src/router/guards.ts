import type { RouteLocationNormalized, Router } from 'vue-router'
import { useTokenStore } from '@/stores/token'
import { useUserInfoStore } from '@/stores/userInfo'
import { jwtDecode } from 'jwt-decode'

function setupNavigationGuards(router: Router) {
  router.beforeEach((to, from) => {
    if (!isLoggedIn()) {
      if (to.meta.requiresAuth) {
        return { name: 'login', query: { redirect: to.path } }
      }
    } else {
      if (isVisitorOnly(to)) {
        return from
      } else {
        if (!checkPermissions(to)) {
          return { name: 'forbidden' }
        }
      }
    }
  })
}

function isTokenValid(token: string | null): boolean {
  if (!token) return false
  try {
    const decoded = jwtDecode(token)
    return decoded.exp! * 1000 >= Date.now()
  } catch {
    return false
  }
}

function isLoggedIn(): boolean {
  return isTokenValid(useTokenStore().token)
}

function checkPermissions(to: RouteLocationNormalized): boolean {
  const userPermissions = useUserInfoStore().roleList
  if (!to.meta.roles) return true
  const required = to.meta.roles as string[]
  return required.some((p) => userPermissions.includes(p.toLowerCase()))
}

function isVisitorOnly(route: RouteLocationNormalized): boolean {
  return Boolean(route.meta.visitorOnly)
}

export default setupNavigationGuards
