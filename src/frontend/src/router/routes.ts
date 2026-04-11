import type { RouteRecordRaw } from 'vue-router'

export const routes = [
  // ─── Public / Auth Routes ────────────────────────────────────────────────
  {
    path: '/login',
    component: () => import('@/pages/Login.vue'),
    name: 'login',
    meta: { requiresAuth: false, visitorOnly: true }
  },
  {
    path: '/register',
    component: () => import('@/pages/UserRegistration.vue'),
    name: 'register',
    meta: { requiresAuth: false, visitorOnly: true }
  },
  {
    path: '/forget-password',
    component: () => import('@/pages/ForgetPassword.vue'),
    name: 'forget-password',
    meta: { requiresAuth: false, visitorOnly: true }
  },
  {
    path: '/reset-password',
    component: () => import('@/pages/ResetPassword.vue'),
    name: 'reset-password',
    meta: { requiresAuth: false, visitorOnly: true }
  },

  // ─── Main App (authenticated) ────────────────────────────────────────────
  {
    path: '/',
    component: () => import('@/pages/Dashboard.vue'),
    name: 'dashboard',
    redirect: '/home',
    children: [
      // Home
      {
        path: '/home',
        component: () => import('@/pages/Home.vue'),
        name: 'home',
        meta: { title: 'Home', icon: 'mdi-home', isMenuItem: true, requiresAuth: true }
      },

      // ── Student: Weekly Activity Reports (UC-27, UC-32) ──────────────────
      {
        path: '/activities',
        name: 'activities',
        meta: {
          title: 'Weekly Activity Reports',
          icon: 'mdi-note-text',
          isMenuItem: true,
          requiresAuth: true,
          requiresPermissions: ['student']
        },
        redirect: '/activities/my-activities',
        children: [
          {
            path: '/activities/my-activities',
            component: () => import('@/pages/activities/MyActivities.vue'),
            name: 'my-activities',
            meta: { title: 'My Activities', icon: 'mdi-file-document', isMenuItem: true, requiresAuth: true }
          },
          {
            path: '/activities/team-activities',
            component: () => import('@/pages/activities/TeamsActivities.vue'),
            name: 'team-activities',
            meta: { title: "Team's Activities", icon: 'mdi-file-multiple', isMenuItem: true, requiresAuth: true }
          }
        ]
      },

      // ── Student: Peer Evaluations (UC-28, UC-29) ─────────────────────────
      {
        path: '/evaluations',
        name: 'evaluations',
        meta: {
          title: 'Peer Evaluations',
          icon: 'mdi-chart-bar',
          isMenuItem: true,
          requiresAuth: true,
          requiresPermissions: ['student']
        },
        redirect: '/evaluations/my-evaluations',
        children: [
          {
            path: '/evaluations/my-evaluations',
            component: () => import('@/pages/evaluations/MyEvaluations.vue'),
            name: 'my-evaluations',
            meta: { title: 'My Evaluations', icon: 'mdi-medal', isMenuItem: true, requiresAuth: true }
          },
          {
            path: '/evaluations/submit-evaluations',
            component: () => import('@/pages/evaluations/SubmitTeamsEvaluations.vue'),
            name: 'team-evaluations',
            meta: { title: 'Submit Evaluations', icon: 'mdi-trophy', isMenuItem: true, requiresAuth: true }
          }
        ]
      },

      // ── Instructor/Admin: Course & Section Management (UC-2 to UC-6) ─────
      {
        path: '/courses',
        component: () => import('@/pages/courses/Courses.vue'),
        name: 'courses',
        meta: {
          title: 'Courses',
          icon: 'mdi-office-building',
          isMenuItem: true,
          requiresAuth: true,
          requiresPermissions: ['instructor']
        }
      },
      {
        path: '/sections',
        component: () => import('@/pages/sections/Sections.vue'),
        name: 'sections',
        meta: {
          title: 'Sections',
          icon: 'mdi-school',
          isMenuItem: true,
          requiresAuth: true,
          requiresPermissions: ['instructor']
        }
      },

      // ── Admin/Instructor: Team Management (UC-7 to UC-14) ────────────────
      {
        path: '/teams',
        component: () => import('@/pages/teams/Teams.vue'),
        name: 'teams',
        meta: {
          title: 'Teams',
          icon: 'mdi-account-group',
          isMenuItem: true,
          requiresAuth: true,
          requiresPermissions: ['admin', 'instructor']
        }
      },

      // ── Admin/Instructor: Student Management (UC-11 to UC-17) ────────────
      {
        path: '/students',
        component: () => import('@/pages/students/Students.vue'),
        name: 'students',
        meta: {
          title: 'Students',
          icon: 'mdi-account',
          isMenuItem: true,
          requiresAuth: true,
          requiresPermissions: ['instructor']
        }
      },
      {
        path: '/students/:studentId',
        component: () => import('@/pages/students/StudentPerformanceDashboard.vue'),
        name: 'student-performance-dashboard',
        meta: {
          title: 'Performance Dashboard',
          icon: 'mdi-view-dashboard',
          isMenuItem: false,
          requiresAuth: true,
          requiresPermissions: ['instructor']
        }
      },

      // ── Admin: Instructor Management (UC-18 to UC-24) ────────────────────
      {
        path: '/instructors',
        component: () => import('@/pages/instructors/Instructors.vue'),
        name: 'instructors',
        meta: {
          title: 'Instructors',
          icon: 'mdi-account-tie',
          isMenuItem: true,
          requiresAuth: true,
          requiresPermissions: ['admin']
        }
      },

      // ── Instructor/Admin: Reporting (UC-31 to UC-34) ─────────────────────
      {
        path: '/section-activities',
        component: () => import('@/pages/activities/admin/SectionsActivities.vue'),
        name: 'section-activities',
        meta: {
          title: "Section's Activities",
          icon: 'mdi-clipboard-list',
          isMenuItem: true,
          requiresAuth: true,
          requiresPermissions: ['instructor']
        }
      },
      {
        path: '/section-evaluations',
        component: () => import('@/pages/evaluations/admin/SectionsEvaluations.vue'),
        name: 'section-evaluations',
        meta: {
          title: "Section's Evaluations",
          icon: 'mdi-chart-box',
          isMenuItem: true,
          requiresAuth: true,
          requiresPermissions: ['instructor']
        }
      },

      // ── Admin: Rubric Management (UC-1) ───────────────────────────────────
      {
        path: '/rubrics-criteria',
        name: 'rubrics-criteria',
        meta: {
          title: 'Rubrics',
          icon: 'mdi-speedometer',
          isMenuItem: true,
          requiresAuth: true,
          requiresPermissions: ['admin']
        },
        redirect: '/rubrics-criteria/rubrics',
        children: [
          {
            path: '/rubrics-criteria/rubrics',
            component: () => import('@/pages/rubrics/Rubrics.vue'),
            name: 'rubrics',
            meta: { title: 'Rubrics', icon: 'mdi-check-circle', isMenuItem: true, requiresAuth: true }
          },
          {
            path: '/rubrics-criteria/criteria',
            component: () => import('@/pages/rubrics/Criteria.vue'),
            name: 'criteria',
            meta: { title: 'Criteria', icon: 'mdi-file-check', isMenuItem: true, requiresAuth: true }
          }
        ]
      },

      // ── Requirements & Artifacts (RAM) ────────────────────────────────────
      {
        path: '/ram',
        name: 'ram',
        meta: {
          title: 'Requirements',
          icon: 'mdi-file-document-outline',
          isMenuItem: true,
          requiresAuth: true,
          requiresPermissions: ['student', 'instructor']
        },
        redirect: '/ram/documents',
        children: [
          {
            path: '/ram/documents',
            component: () => import('@/pages/ram/RamDocuments.vue'),
            name: 'ram-documents',
            meta: {
              title: 'Documents',
              icon: 'mdi-folder-multiple',
              isMenuItem: true,
              requiresAuth: true,
              requiresPermissions: ['student', 'instructor']
            }
          },
          {
            path: '/ram/documents/:documentId',
            component: () => import('@/pages/ram/RamDocumentEditor.vue'),
            name: 'ram-document-editor',
            meta: {
              title: 'Document Editor',
              icon: 'mdi-pencil',
              isMenuItem: false,
              requiresAuth: true,
              requiresPermissions: ['student', 'instructor']
            }
          },
          {
            path: '/ram/glossary/:documentId',
            component: () => import('@/pages/ram/RamGlossary.vue'),
            name: 'ram-glossary',
            meta: {
              title: 'Glossary',
              icon: 'mdi-book-open-variant',
              isMenuItem: false,
              requiresAuth: true,
              requiresPermissions: ['student', 'instructor']
            }
          },
          {
            path: '/ram/use-cases',
            component: () => import('@/pages/ram/RamUseCases.vue'),
            name: 'ram-use-cases',
            meta: {
              title: 'Use Cases',
              icon: 'mdi-ticket-confirmation',
              isMenuItem: true,
              requiresAuth: true,
              requiresPermissions: ['student', 'instructor']
            }
          }
        ]
      },

      // ── Current User Profile (UC-25, UC-26, UC-30) ───────────────────────
      {
        path: '/user',
        name: 'current-user',
        meta: { title: 'My Profile', icon: 'mdi-cog', isMenuItem: true, requiresAuth: true },
        redirect: '/user/profile',
        children: [
          {
            path: '/user/profile',
            component: () => import('@/pages/currentUser/UserInfo.vue'),
            name: 'user-profile',
            meta: { title: 'User Profile', icon: 'mdi-account-circle', isMenuItem: true, requiresAuth: true }
          },
          {
            path: '/user/reset-password',
            component: () => import('@/pages/currentUser/UserResetPassword.vue'),
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
    component: () => import('@/pages/errors/Forbidden.vue'),
    name: 'forbidden'
  },
  {
    path: '/:pathMatch(.*)*',
    component: () => import('@/pages/errors/NotFound.vue'),
    name: 'not-found'
  }
] as RouteRecordRaw[]
