import axios from 'axios'
import type { AxiosError, AxiosResponse } from 'axios'
import { useTokenStore } from '@/stores/token'
import { useUserInfoStore } from '@/stores/userInfo'
import { useNotifyStore } from '@/stores/notify'
import router from '@/router'

const devBaseURL = import.meta.env.VITE_SERVER_URL as string | undefined
const baseURL = import.meta.env.DEV ? devBaseURL : '/api/v1'
const requestInstance = axios.create({ baseURL, timeout: 10_000 })

let lastToastAt = 0

function toastOnce(msg: string, cooldownMs = 1500) {
  const now = Date.now()
  if (now - lastToastAt > cooldownMs) {
    useNotifyStore().error(msg)
    lastToastAt = now
  }
}

function hardLogout() {
  const tokenStore = useTokenStore()
  const userInfoStore = useUserInfoStore()
  tokenStore.removeToken()
  userInfoStore.removeUserInfo()
}

function isOnRoute(name: string): boolean {
  return router.currentRoute?.value?.name === name
}

requestInstance.interceptors.request.use(
  (config) => {
    const tokenStore = useTokenStore()
    if (tokenStore.token) {
      config.headers = config.headers ?? {}
      ;(config.headers as any).Authorization = `Bearer ${tokenStore.token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

requestInstance.interceptors.response.use(
  (response: AxiosResponse) => response.data,
  (error) => {
    if (!axios.isAxiosError(error) || !error.response) {
      toastOnce(error instanceof Error ? error.message : 'Request failed')
      return Promise.reject(error)
    }

    const { status, data } = error.response
    const msg = (data && (data.message || data.error)) || `HTTP ${status}: Something went wrong`

    if (status !== 401) toastOnce(msg)

    switch (status) {
      case 401:
        hardLogout()
        if (!isOnRoute('login')) {
          router.push({ name: 'login', query: { redirect: router.currentRoute.value.fullPath } })
        }
        break
      case 403:
        if (!isOnRoute('forbidden')) router.push({ name: 'forbidden' })
        break
      case 404:
        if (!isOnRoute('not-found')) router.push({ name: 'not-found' })
        break
      case 408:
      case 504:
        toastOnce('The request timed out. Please try again.')
        break
    }
    return Promise.reject(error)
  }
)

export default requestInstance
