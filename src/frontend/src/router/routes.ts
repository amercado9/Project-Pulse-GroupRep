import type { RouteRecordRaw } from 'vue-router'

export const routes = [
  // ─── Public / Auth Routes ────────────────────────────────────────────────
  {
    path: '/login',
    component: () => import('@/features/auth/pages/Login.vue'),
    name: 'login',
    meta: { requiresAuth: false, visitorOnly: true }
  },
  {
    path: '/register',
    component: () => import('@/features/auth/pages/UserRegistration.vue'),
    name: 'register',
    meta: { requiresAuth: false, visitorOnly: true }
  },
  {
    path: '/forget-password',
    component: () => import('@/features/auth/pages/ForgetPassword.vue'),
    name: 'forget-password',
    meta: { requiresAuth: false, visitorOnly: true }
  },
  {
    path: '/reset-password',
    component: () => import('@/features/auth/pages/ResetPassword.vue'),
    name: 'reset-password',
    meta: { requiresAuth: false, visitorOnly: true }
  },

  // ─── Main App (authenticated) ────────────────────────────────────────────
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    name: 'dashboard',
    redirect: '/home',
    children: [
      {
        path: '/home',
        component: () => import('@/features/dashboard/pages/Home.vue'),
        name: 'home',
        meta: { title: 'Home', icon: 'mdi-home', isMenuItem: true, requiresAuth: true }
      },

      // ── Sections ─────────────────────────────────────────────────────────
      {
        path: '/sections',
        component: () => import('@/features/section/pages/Sections.vue'),
        name: 'sections',
        meta: { title: 'Sections', icon: 'mdi-school', isMenuItem: true, requiresAuth: true, roles: ['admin'] }
      },

      // ── Rubrics ──────────────────────────────────────────────────────────
      {
        path: '/rubrics',
        component: () => import('@/features/rubric/pages/Rubrics.vue'),
        name: 'rubrics',
        meta: { title: 'Rubrics', icon: 'mdi-clipboard-list', isMenuItem: true, requiresAuth: true, roles: ['admin'] }
      },

      // ── Current User Profile ─────────────────────────────────────────────
      {
        path: '/user',
        name: 'current-user',
        meta: { title: 'My Profile', icon: 'mdi-cog', isMenuItem: true, requiresAuth: true },
        redirect: '/user/profile',
        children: [
          {
            path: '/user/profile',
            component: () => import('@/features/user/pages/UserInfo.vue'),
            name: 'user-profile',
            meta: { title: 'User Profile', icon: 'mdi-account-circle', isMenuItem: true, requiresAuth: true }
          },
          {
            path: '/user/reset-password',
            component: () => import('@/features/user/pages/UserResetPassword.vue'),
            name: 'user-reset-password',
            meta: { title: 'Reset Password', icon: 'mdi-lock-reset', isMenuItem: true, requiresAuth: true }
          }
        ]
      }
    ]
  },

  // ─── Error Pages ─────────────────────────────────────────────────────────
  {
    path: '/403',
    component: () => import('@/shared/pages/errors/Forbidden.vue'),
    name: 'forbidden'
  },
  {
    path: '/:pathMatch(.*)*',
    component: () => import('@/shared/pages/errors/NotFound.vue'),
    name: 'not-found'
  }
] as RouteRecordRaw[]
