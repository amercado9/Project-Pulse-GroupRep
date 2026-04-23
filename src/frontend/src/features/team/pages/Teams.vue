<template>
  <v-container>
    <v-row class="mb-4" align="center">
      <v-col><h2 class="text-h5 font-weight-bold">Senior Design Teams</h2></v-col>
      <v-col v-if="isAdmin" cols="auto">
        <div class="header-actions">
          <v-btn variant="outlined" prepend-icon="mdi-account-switch" @click="openAssignmentsPage">
            Assign Students
          </v-btn>
          <v-btn color="primary" prepend-icon="mdi-plus" @click="openCreateDialog">Create Team</v-btn>
        </div>
      </v-col>
    </v-row>

    <v-row v-if="deletedTeamNotification" class="mb-4">
      <v-col cols="12">
        <v-card variant="outlined">
          <v-card-title class="pa-4 pb-2 d-flex align-center">
            <span>Notify Affected Users</span>
            <v-spacer />
            <v-btn variant="text" size="small" @click="dismissDeletedTeamNotification">Dismiss</v-btn>
          </v-card-title>
          <v-card-text class="pt-0">
            <div class="text-body-2 mb-3">
              Use this summary to notify affected students and instructors manually about the deleted team.
            </div>

            <v-table density="compact" class="mb-4">
              <tbody>
                <tr>
                  <th class="text-left">Team</th>
                  <td>{{ deletedTeamNotification.teamName }}</td>
                </tr>
                <tr>
                  <th class="text-left">Section</th>
                  <td>{{ deletedTeamNotification.sectionName }}</td>
                </tr>
                <tr>
                  <th class="text-left">Status</th>
                  <td>{{ deletedTeamNotification.status }}</td>
                </tr>
              </tbody>
            </v-table>

            <div class="text-subtitle-2 font-weight-bold mb-2">Affected Students</div>
            <v-alert
              v-if="deletedTeamNotification.studentNotifications.length === 0"
              type="info"
              variant="tonal"
              density="compact"
              class="mb-4"
            >
              No students were assigned to the deleted team.
            </v-alert>
            <v-table v-else density="compact" class="mb-4">
              <thead>
                <tr>
                  <th class="text-left">Student</th>
                  <th class="text-left">Email</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="student in deletedTeamNotification.studentNotifications" :key="student.email ?? student.fullName">
                  <td>{{ student.fullName }}</td>
                  <td>{{ student.email ?? '—' }}</td>
                </tr>
              </tbody>
            </v-table>

            <div class="text-subtitle-2 font-weight-bold mb-2">Affected Instructors</div>
            <v-alert
              v-if="deletedTeamNotification.instructorNotifications.length === 0"
              type="info"
              variant="tonal"
              density="compact"
            >
              No instructors were assigned to the deleted team.
            </v-alert>
            <v-table v-else density="compact">
              <thead>
                <tr>
                  <th class="text-left">Instructor</th>
                  <th class="text-left">Email</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="instructor in deletedTeamNotification.instructorNotifications" :key="instructor.fullName">
                  <td>{{ instructor.fullName }}</td>
                  <td>{{ instructor.email ?? '—' }}</td>
                </tr>
              </tbody>
            </v-table>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <v-row class="mb-4">
      <v-col cols="12" md="4">
        <v-text-field
          v-model="filters.sectionName"
          label="Search by section"
          prepend-inner-icon="mdi-school"
          clearable
          hide-details
          @keyup.enter="search"
        />
      </v-col>
      <v-col cols="12" md="4">
        <v-text-field
          v-model="filters.teamName"
          label="Search by team name"
          prepend-inner-icon="mdi-account-group"
          clearable
          hide-details
          @keyup.enter="search"
        />
      </v-col>
      <v-col cols="12" md="4">
        <v-text-field
          v-model="filters.instructor"
          label="Search by instructor"
          prepend-inner-icon="mdi-account-tie"
          clearable
          hide-details
          @keyup.enter="search"
        />
      </v-col>
      <v-col cols="auto">
        <v-btn color="primary" @click="search">Search</v-btn>
      </v-col>
      <v-col cols="auto">
        <v-btn variant="text" @click="clearFilters">Clear</v-btn>
      </v-col>
    </v-row>

    <v-row v-if="loading">
      <v-col class="text-center">
        <v-progress-circular indeterminate />
      </v-col>
    </v-row>

    <v-row v-else-if="teams.length === 0">
      <v-col>
        <v-alert type="info" variant="tonal">
          No matching senior design teams found.
        </v-alert>
        <v-btn v-if="isAdmin" color="primary" class="mt-4" prepend-icon="mdi-plus" @click="openCreateDialog">
          Create Team
        </v-btn>
      </v-col>
    </v-row>

    <v-row v-else>
      <v-col cols="12">
        <v-table>
          <thead>
            <tr>
              <th>Section</th>
              <th>Team Name</th>
              <th>Description</th>
              <th>Website</th>
              <th>Team Members</th>
              <th>Instructors</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="team in teams" :key="team.teamId">
              <td>{{ team.sectionName }}</td>
              <td>{{ team.teamName }}</td>
              <td>{{ team.teamDescription || '—' }}</td>
              <td>
                <a
                  v-if="team.teamWebsiteUrl"
                  :href="team.teamWebsiteUrl"
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  Open
                </a>
                <span v-else class="text-medium-emphasis">—</span>
              </td>
              <td>
                <div v-if="team.teamMemberNames.length" class="chip-group">
                  <v-chip
                    v-for="member in team.teamMemberNames"
                    :key="member"
                    size="small"
                    variant="tonal"
                    class="mr-1 mb-1"
                  >{{ member }}</v-chip>
                </div>
                <span v-else class="text-medium-emphasis">—</span>
              </td>
              <td>
                <div v-if="team.instructorNames.length" class="chip-group">
                  <v-chip
                    v-for="instructor in team.instructorNames"
                    :key="instructor"
                    size="small"
                    variant="tonal"
                    color="primary"
                    class="mr-1 mb-1"
                  >{{ instructor }}</v-chip>
                </div>
                <span v-else class="text-medium-emphasis">—</span>
              </td>
              <td>
                <v-btn
                  size="small"
                  variant="text"
                  color="primary"
                  @click="viewTeam(team.teamId)"
                >
                  View
                </v-btn>
              </td>
            </tr>
          </tbody>
        </v-table>
      </v-col>
    </v-row>

    <v-dialog v-model="dialog" max-width="700" persistent>
      <v-card>
        <v-card-title class="pa-4">
          {{ step === 1 ? 'Create Team' : 'Confirm Team' }}
        </v-card-title>
        <v-divider />

        <v-card-text v-if="step === 1" class="pa-4">
          <v-select
            v-model="form.sectionId"
            :items="sections"
            item-title="sectionName"
            item-value="sectionId"
            label="Section"
            :loading="sectionsLoading"
            :error-messages="errors.sectionId"
            class="mb-4"
          />

          <v-text-field
            v-model="form.teamName"
            label="Team Name"
            placeholder="e.g. Peer Evaluation Tool team"
            :error-messages="errors.teamName"
            class="mb-4"
          />

          <v-textarea
            v-model="form.teamDescription"
            label="Team Description"
            rows="3"
            class="mb-4"
          />

          <v-text-field
            v-model="form.teamWebsiteUrl"
            label="Team Website URL"
            placeholder="https://example.com"
            :error-messages="errors.teamWebsiteUrl"
          />
        </v-card-text>

        <v-card-text v-else class="pa-4">
          <v-alert type="info" variant="tonal" class="mb-4">
            Please review the team details before confirming.
          </v-alert>
          <v-table density="compact">
            <tbody>
              <tr>
                <th class="text-left">Section</th>
                <td>{{ selectedSectionName }}</td>
              </tr>
              <tr>
                <th class="text-left">Team Name</th>
                <td>{{ previewPayload.teamName }}</td>
              </tr>
              <tr>
                <th class="text-left">Description</th>
                <td>{{ previewPayload.teamDescription ?? '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Website</th>
                <td>{{ previewPayload.teamWebsiteUrl ?? '—' }}</td>
              </tr>
            </tbody>
          </v-table>
        </v-card-text>

        <v-divider />
        <v-card-actions class="pa-4">
          <v-btn variant="text" @click="cancelCreate">Cancel</v-btn>
          <v-spacer />
          <v-btn v-if="step === 2" variant="outlined" @click="step = 1">Modify</v-btn>
          <v-btn
            color="primary"
            :loading="saving"
            @click="step === 1 ? goToPreview() : confirmCreate()"
          >
            {{ step === 1 ? 'Preview' : 'Confirm' }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-snackbar v-model="snackbar.show" :color="snackbar.color" timeout="3000">
      {{ snackbar.message }}
    </v-snackbar>
  </v-container>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { findSections } from '@/features/section/services/sectionService'
import { useUserInfoStore } from '@/stores/userInfo'
import { useTeamNotificationsStore } from '../stores/teamNotifications'
import { createTeam, findTeams } from '../services/teamService'
import type { SectionSummary } from '@/features/section/services/sectionTypes'
import type { CreateTeamRequest, FindTeamsParams, TeamSummary } from '../services/teamTypes'

const router = useRouter()
const userInfoStore = useUserInfoStore()
const teamNotificationsStore = useTeamNotificationsStore()
const teams = ref<TeamSummary[]>([])
const loading = ref(false)
const filters = ref<FindTeamsParams>({
  sectionName: '',
  teamName: '',
  instructor: ''
})
const dialog = ref(false)
const step = ref(1)
const saving = ref(false)
const sectionsLoading = ref(false)
const sections = ref<SectionSummary[]>([])

const emptyForm = () => ({
  sectionId: null as number | null,
  teamName: '',
  teamDescription: '',
  teamWebsiteUrl: ''
})

const form = ref(emptyForm())
const errors = ref<{ sectionId?: string; teamName?: string; teamWebsiteUrl?: string }>({})
const snackbar = ref({ show: false, message: '', color: 'success' })

const isAdmin = computed(() => userInfoStore.isAdmin)
const deletedTeamNotification = computed(() => teamNotificationsStore.deletedTeamNotification)

const previewPayload = computed<CreateTeamRequest>(() => ({
  sectionId: form.value.sectionId ?? 0,
  teamName: form.value.teamName.trim(),
  teamDescription: normalizeOptional(form.value.teamDescription),
  teamWebsiteUrl: normalizeOptional(form.value.teamWebsiteUrl)
}))

const selectedSectionName = computed(() => {
  return sections.value.find((section) => section.sectionId === form.value.sectionId)?.sectionName ?? '—'
})

onMounted(async () => {
  if (deletedTeamNotification.value) {
    snackbar.value = { show: true, message: 'Team deleted successfully.', color: 'success' }
  }
  await search()
})

async function search() {
  loading.value = true
  try {
    const res = await findTeams({
      sectionName: filters.value.sectionName?.trim() || undefined,
      teamName: filters.value.teamName?.trim() || undefined,
      instructor: filters.value.instructor?.trim() || undefined
    }) as any

    teams.value = res.flag ? (res.data ?? []) : []
  } catch {
    teams.value = []
  } finally {
    loading.value = false
  }
}

function clearFilters() {
  filters.value = { sectionName: '', teamName: '', instructor: '' }
  search()
}

function viewTeam(teamId: number) {
  router.push({ name: 'team-detail', params: { id: teamId } })
}

function dismissDeletedTeamNotification() {
  teamNotificationsStore.clearDeletedTeamNotification()
}

function openAssignmentsPage() {
  router.push({ name: 'team-student-assignments' })
}

async function openCreateDialog() {
  form.value = emptyForm()
  errors.value = {}
  step.value = 1
  dialog.value = true
  await loadSections()
}

function cancelCreate() {
  dialog.value = false
}

async function loadSections() {
  sectionsLoading.value = true
  try {
    const res = await findSections() as any
    sections.value = res.flag ? (res.data ?? []) : []
  } catch {
    sections.value = []
    snackbar.value = { show: true, message: 'Failed to load sections.', color: 'error' }
  } finally {
    sectionsLoading.value = false
  }
}

function validate(): boolean {
  errors.value = {}
  let valid = true

  if (!form.value.sectionId) {
    errors.value.sectionId = 'Section is required.'
    valid = false
  }

  if (!form.value.teamName.trim()) {
    errors.value.teamName = 'Team name is required.'
    valid = false
  }

  const website = normalizeOptional(form.value.teamWebsiteUrl)
  if (website && !isValidAbsoluteHttpUrl(website)) {
    errors.value.teamWebsiteUrl = 'Team website URL must be a valid http:// or https:// URL.'
    valid = false
  }

  return valid
}

function goToPreview() {
  if (validate()) {
    step.value = 2
  }
}

async function confirmCreate() {
  saving.value = true
  try {
    const res = await createTeam(previewPayload.value) as any
    if (res.flag && res.data) {
      snackbar.value = { show: true, message: 'Team created successfully.', color: 'success' }
      dialog.value = false
      router.push({ name: 'team-detail', params: { id: res.data.teamId } })
      return
    }

    step.value = 1
    snackbar.value = { show: true, message: res.message || 'Failed to create team.', color: 'error' }
  } catch (error: any) {
    step.value = 1
    const message = error?.response?.data?.message || 'An error occurred. Please try again.'
    snackbar.value = { show: true, message, color: 'error' }
  } finally {
    saving.value = false
  }
}

function normalizeOptional(value: string | null | undefined) {
  const trimmed = value?.trim() ?? ''
  return trimmed ? trimmed : null
}

function isValidAbsoluteHttpUrl(value: string) {
  try {
    const url = new URL(value)
    return url.protocol === 'http:' || url.protocol === 'https:'
  } catch {
    return false
  }
}
</script>

<style scoped lang="scss">
.chip-group {
  min-width: 180px;
}

.header-actions {
  display: flex;
  gap: 12px;
}
</style>
