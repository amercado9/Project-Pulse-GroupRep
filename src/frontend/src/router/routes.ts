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

  {
    path: '/join',
    component: () => import('@/features/auth/pages/JoinSection.vue'),
    name: 'join-section',
    meta: { requiresAuth: false }
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
      {
        path: '/war',
        component: () => import('@/features/activity/pages/WarActivities.vue'),
        name: 'war-activities',
        meta: {
          title: 'WAR',
          icon: 'mdi-clipboard-text-clock-outline',
          isMenuItem: true,
          requiresAuth: true,
          roles: ['student']
        }
      },
      {
        path: '/peer-evaluations',
        component: () => import('@/features/evaluation/pages/PeerEvaluations.vue'),
        name: 'peer-evaluations',
        meta: {
          title: 'Peer Evaluations',
          icon: 'mdi-account-star-outline',
          isMenuItem: true,
          requiresAuth: true,
          roles: ['student']
        }
      },

      // ── Sections ─────────────────────────────────────────────────────────
      {
        path: '/sections',
        component: () => import('@/features/section/pages/Sections.vue'),
        name: 'sections',
        meta: { title: 'Sections', icon: 'mdi-school', isMenuItem: true, requiresAuth: true, roles: ['admin'] }
      },
      {
        path: '/sections/:id',
        component: () => import('@/features/section/pages/SectionDetail.vue'),
        name: 'section-detail',
        meta: { requiresAuth: true, roles: ['admin'] }
      },

      // ── Teams ────────────────────────────────────────────────────────────
      {
        path: '/teams',
        component: () => import('@/features/team/pages/Teams.vue'),
        name: 'teams',
        meta: { title: 'Teams', icon: 'mdi-account-group', isMenuItem: true, requiresAuth: true, roles: ['admin', 'instructor'] }
      },
      {
        path: '/teams/assign-students',
        component: () => import('@/features/team/pages/TeamStudentAssignments.vue'),
        name: 'team-student-assignments',
        meta: { requiresAuth: true, roles: ['admin'] }
      },
      {
        path: '/teams/assign-instructors',
        component: () => import('@/features/team/pages/TeamInstructorAssignments.vue'),
        name: 'team-instructor-assignments',
        meta: { requiresAuth: true, roles: ['admin'] }
      },
      {
        path: '/teams/:id',
        component: () => import('@/features/team/pages/TeamDetail.vue'),
        name: 'team-detail',
        meta: { requiresAuth: true, roles: ['admin', 'instructor'] }
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
