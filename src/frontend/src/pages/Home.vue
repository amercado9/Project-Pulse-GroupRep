<template>
  <div class="home-page">
    <!-- Welcome banner -->
    <div class="welcome-banner mb-6">
      <h2>Welcome back, {{ userInfoStore.userInfo?.firstName }}</h2>
      <p class="subtitle">
        <v-chip :color="roleChipColor" size="small" variant="flat" class="mr-2">{{ roleLabel }}</v-chip>
        Project Pulse — Senior Design Management Platform
      </p>
    </div>

    <!-- Student view -->
    <template v-if="userInfoStore.isStudent">
      <v-row class="mb-4">
        <v-col cols="12" md="6">
          <v-card rounded="lg" hover>
            <v-card-title class="d-flex align-center gap-2">
              <v-icon icon="mdi-note-text" color="primary" />
              Weekly Activity Reports (WAR)
            </v-card-title>
            <v-card-text>
              <p>Log what you worked on each week so your instructor can track team contributions.</p>
              <ul>
                <li>Go to <strong>My Activities</strong> to add or update your activities for the current week.</li>
                <li>Use <strong>Team's Activities</strong> to see what your teammates are working on.</li>
                <li>Be specific about activity names and actual hours spent.</li>
              </ul>
            </v-card-text>
            <v-card-actions>
              <v-btn color="primary" variant="outlined" @click="$router.push({ name: 'my-activities' })">
                Go to My Activities
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-col>

        <v-col cols="12" md="6">
          <v-card rounded="lg" hover>
            <v-card-title class="d-flex align-center gap-2">
              <v-icon icon="mdi-chart-bar" color="success" />
              Peer Evaluations
            </v-card-title>
            <v-card-text>
              <p>Evaluate your teammates each week based on the provided rubric criteria.</p>
              <ul>
                <li>Go to <strong>Submit Evaluations</strong> to rate your teammates for the previous week.</li>
                <li>Evaluations are due weekly and <strong>cannot be made up</strong> if missed.</li>
                <li>View <strong>My Evaluations</strong> to see how your teammates rated you.</li>
                <li>Private comments are visible to instructors only.</li>
              </ul>
            </v-card-text>
            <v-card-actions>
              <v-btn color="success" variant="outlined" @click="$router.push({ name: 'team-evaluations' })">
                Submit Evaluations
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-col>
      </v-row>

      <v-row>
        <v-col cols="12" md="6">
          <v-card rounded="lg" hover>
            <v-card-title class="d-flex align-center gap-2">
              <v-icon icon="mdi-file-document" color="warning" />
              Requirements & Artifacts
            </v-card-title>
            <v-card-text>
              <p>Collaborate with your team on project documents — vision, scope, use cases, and glossary.</p>
            </v-card-text>
            <v-card-actions>
              <v-btn color="warning" variant="outlined" @click="$router.push({ name: 'ram-documents' })">
                View Documents
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-col>

        <v-col cols="12" md="6">
          <v-card rounded="lg" hover>
            <v-card-title class="d-flex align-center gap-2">
              <v-icon icon="mdi-cog" color="secondary" />
              My Profile
            </v-card-title>
            <v-card-text>
              <p>Update your account information and change your password at any time.</p>
            </v-card-text>
            <v-card-actions>
              <v-btn variant="outlined" @click="$router.push({ name: 'user-profile' })">
                My Profile
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-col>
      </v-row>
    </template>

    <!-- Instructor view -->
    <template v-else-if="userInfoStore.isInstructor">
      <v-row class="mb-4">
        <v-col cols="12" md="4">
          <v-card rounded="lg" hover>
            <v-card-title class="d-flex align-center gap-2">
              <v-icon icon="mdi-school" color="primary" />
              Section Management
            </v-card-title>
            <v-card-text>
              <p>Manage your senior design sections, configure active weeks, and assign rubrics.</p>
            </v-card-text>
            <v-card-actions>
              <v-btn color="primary" variant="outlined" @click="$router.push({ name: 'sections' })">
                Manage Sections
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-col>

        <v-col cols="12" md="4">
          <v-card rounded="lg" hover>
            <v-card-title class="d-flex align-center gap-2">
              <v-icon icon="mdi-clipboard-list" color="success" />
              WAR Reports
            </v-card-title>
            <v-card-text>
              <p>View and generate Weekly Activity Reports for teams and individual students.</p>
            </v-card-text>
            <v-card-actions>
              <v-btn color="success" variant="outlined" @click="$router.push({ name: 'section-activities' })">
                View WAR Reports
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-col>

        <v-col cols="12" md="4">
          <v-card rounded="lg" hover>
            <v-card-title class="d-flex align-center gap-2">
              <v-icon icon="mdi-chart-box" color="warning" />
              Evaluation Reports
            </v-card-title>
            <v-card-text>
              <p>Generate peer evaluation reports for the section, teams, or individual students.</p>
            </v-card-text>
            <v-card-actions>
              <v-btn color="warning" variant="outlined" @click="$router.push({ name: 'section-evaluations' })">
                View Evaluations
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-col>
      </v-row>

      <v-row>
        <v-col cols="12" md="4">
          <v-card rounded="lg" hover>
            <v-card-title class="d-flex align-center gap-2">
              <v-icon icon="mdi-account-group" color="deep-purple" />
              Team Management
            </v-card-title>
            <v-card-text>
              <p>View teams, manage student assignments, and track team performance.</p>
            </v-card-text>
            <v-card-actions>
              <v-btn variant="outlined" @click="$router.push({ name: 'teams' })">View Teams</v-btn>
            </v-card-actions>
          </v-card>
        </v-col>

        <v-col cols="12" md="4">
          <v-card rounded="lg" hover>
            <v-card-title class="d-flex align-center gap-2">
              <v-icon icon="mdi-account" color="secondary" />
              Students
            </v-card-title>
            <v-card-text>
              <p>Search students, view profiles, and access individual performance dashboards.</p>
            </v-card-text>
            <v-card-actions>
              <v-btn variant="outlined" @click="$router.push({ name: 'students' })">View Students</v-btn>
            </v-card-actions>
          </v-card>
        </v-col>

        <v-col cols="12" md="4">
          <v-card rounded="lg" hover>
            <v-card-title class="d-flex align-center gap-2">
              <v-icon icon="mdi-file-document-outline" color="primary" />
              Requirements
            </v-card-title>
            <v-card-text>
              <p>Review team project documents, use cases, and glossary entries.</p>
            </v-card-text>
            <v-card-actions>
              <v-btn variant="outlined" @click="$router.push({ name: 'ram-documents' })">View Documents</v-btn>
            </v-card-actions>
          </v-card>
        </v-col>
      </v-row>
    </template>

    <!-- Admin view -->
    <template v-else>
      <v-alert type="info" variant="tonal" rounded="lg">
        <strong>Admin Dashboard</strong> — You have full administrative access.
        Use the sidebar to manage sections, teams, students, instructors, and rubrics.
      </v-alert>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useUserInfoStore } from '@/stores/userInfo'

const userInfoStore = useUserInfoStore()

const roleLabel = computed(() => {
  if (userInfoStore.isAdmin) return 'Admin'
  if (userInfoStore.isInstructor) return 'Instructor'
  if (userInfoStore.isStudent) return 'Student'
  return 'User'
})

const roleChipColor = computed(() => {
  if (userInfoStore.isAdmin) return 'error'
  if (userInfoStore.isInstructor) return 'warning'
  return 'primary'
})
</script>

<style lang="scss" scoped>
.home-page {
  max-width: 1200px;

  .welcome-banner {
    background: linear-gradient(135deg, #1a2744 0%, #1565c0 100%);
    border-radius: 10px;
    padding: 28px 32px;
    color: #fff;

    h2 {
      margin: 0 0 8px;
      font-size: 24px;
      font-weight: 700;
    }

    .subtitle {
      margin: 0;
      opacity: 0.9;
      font-size: 14px;
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 4px;
    }
  }

  p { color: #606266; margin-bottom: 12px; font-size: 14px; }
  ul { color: #606266; font-size: 14px; padding-left: 20px; margin-bottom: 8px; }
}
</style>
