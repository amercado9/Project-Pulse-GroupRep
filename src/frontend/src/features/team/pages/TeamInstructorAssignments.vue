<template>
  <v-container>
    <v-btn variant="text" prepend-icon="mdi-arrow-left" class="mb-4" @click="router.push({ name: 'teams' })">
      Back to Teams
    </v-btn>

    <v-row class="mb-4" align="center">
      <v-col>
        <h2 class="text-h5 font-weight-bold">Assign Instructors to Senior Design Teams</h2>
        <div class="text-body-2 text-medium-emphasis">
          Select a section, stage instructor assignments, review them, then confirm the final roster.
        </div>
      </v-col>
    </v-row>

    <v-card variant="outlined" class="mb-4">
      <v-card-text class="pa-4">
        <v-row align="center">
          <v-col cols="12" md="6">
            <v-select
              v-model="selectedSectionId"
              :items="sections"
              item-title="sectionName"
              item-value="sectionId"
              label="Section"
              :loading="sectionsLoading"
              :disabled="sectionsLoading || saving"
              clearable
              @update:model-value="loadWorkspace"
            />
          </v-col>
          <v-col cols="12" md="6" class="text-md-right">
            <v-chip v-if="workspace" color="primary" variant="tonal" class="mr-2">
              {{ workspace.teams.length }} teams
            </v-chip>
            <v-chip v-if="workspace" color="secondary" variant="tonal">
              {{ workspace.instructors.length }} enabled instructors
            </v-chip>
          </v-col>
        </v-row>

        <v-alert v-if="validationMessage" type="warning" variant="tonal" class="mt-2">
          {{ validationMessage }}
        </v-alert>
      </v-card-text>
    </v-card>

    <v-row v-if="workspaceLoading">
      <v-col class="text-center">
        <v-progress-circular indeterminate />
      </v-col>
    </v-row>

    <v-alert v-else-if="!selectedSectionId" type="info" variant="tonal">
      Select a section to load its teams and instructor roster.
    </v-alert>

    <template v-else-if="workspace">
      <template v-if="step === 1">
        <v-row>
          <v-col cols="12" lg="8">
            <v-card variant="outlined" class="mb-4">
              <v-card-title class="pa-4 pb-2 d-flex align-center">
                <span>Teams in {{ workspace.sectionName }}</span>
                <v-spacer />
                <v-chip color="primary" variant="tonal">
                  {{ teamsWithInstructorsCount }}/{{ workspace.teams.length }} staffed
                </v-chip>
              </v-card-title>
              <v-card-text>
                <v-alert v-if="workspace.teams.length === 0" type="warning" variant="tonal">
                  At least one team must exist in the selected section.
                </v-alert>
                <v-row v-else>
                  <v-col v-for="team in stagedTeams" :key="team.teamId" cols="12">
                    <v-card
                      variant="outlined"
                      :class="['team-card', { 'team-card--selected': team.teamId === selectedTeamId }]"
                    >
                      <v-card-title class="pa-4 pb-2 d-flex align-center">
                        <span>{{ team.teamName }}</span>
                        <v-spacer />
                        <v-chip size="small" variant="tonal" color="primary" class="mr-2">
                          {{ team.assignedInstructors.length }} instructors
                        </v-chip>
                        <v-btn
                          size="small"
                          color="primary"
                          variant="text"
                          @click="selectedTeamId = team.teamId"
                        >
                          {{ team.teamId === selectedTeamId ? 'Selected' : 'Select Team' }}
                        </v-btn>
                      </v-card-title>
                      <v-card-text>
                        <div v-if="team.assignedInstructors.length === 0" class="text-medium-emphasis">
                          No instructors assigned yet.
                        </div>
                        <div v-else class="assigned-list">
                          <v-chip
                            v-for="instructor in team.assignedInstructors"
                            :key="instructor.instructorId"
                            class="mr-2 mb-2"
                            color="primary"
                            variant="tonal"
                          >
                            {{ instructor.fullName }}
                            <template #append>
                              <v-btn
                                icon="mdi-close"
                                size="x-small"
                                variant="text"
                                class="ml-1"
                                @click.stop="removeInstructor(team.teamId, instructor.instructorId)"
                              />
                            </template>
                          </v-chip>
                        </div>
                      </v-card-text>
                    </v-card>
                  </v-col>
                </v-row>
              </v-card-text>
            </v-card>
          </v-col>

          <v-col cols="12" lg="4">
            <v-card variant="outlined" class="mb-4">
              <v-card-title class="pa-4 pb-2">Instructor Pool</v-card-title>
              <v-card-text>
                <v-alert type="info" variant="tonal" density="compact" class="mb-4">
                  Select one team, then choose instructors to assign. Instructors can supervise multiple teams.
                </v-alert>

                <v-alert v-if="workspace.instructors.length === 0" type="warning" variant="tonal">
                  At least one enabled instructor must exist before assignments can be saved.
                </v-alert>

                <v-list v-else lines="two" density="compact">
                  <v-list-item
                    v-for="instructor in workspace.instructors"
                    :key="instructor.instructorId"
                    :active="selectedInstructorIds.includes(instructor.instructorId)"
                    @click="toggleInstructorSelection(instructor.instructorId)"
                  >
                    <template #prepend>
                      <v-checkbox-btn
                        :model-value="selectedInstructorIds.includes(instructor.instructorId)"
                        @click.stop
                        @update:model-value="toggleInstructorSelection(instructor.instructorId)"
                      />
                    </template>

                    <v-list-item-title>{{ instructor.fullName }}</v-list-item-title>
                    <v-list-item-subtitle>{{ instructor.email }}</v-list-item-subtitle>

                    <template #append>
                      <div class="chip-group text-right">
                        <v-chip
                          v-for="teamName in assignedTeamNamesByInstructorId(instructor.instructorId)"
                          :key="`${instructor.instructorId}-${teamName}`"
                          size="small"
                          color="primary"
                          variant="tonal"
                          class="mr-1 mb-1"
                        >
                          {{ teamName }}
                        </v-chip>
                        <v-chip
                          v-if="assignedTeamNamesByInstructorId(instructor.instructorId).length === 0"
                          size="small"
                          color="warning"
                          variant="tonal"
                        >
                          Unassigned
                        </v-chip>
                      </div>
                    </template>
                  </v-list-item>
                </v-list>

                <v-divider class="my-4" />

                <v-btn
                  block
                  color="primary"
                  prepend-icon="mdi-account-arrow-right"
                  :disabled="!canAssignSelectedInstructors"
                  @click="assignSelectedInstructors"
                >
                  Assign to Selected Team
                </v-btn>
              </v-card-text>
            </v-card>

            <v-card variant="outlined">
              <v-card-title class="pa-4 pb-2">Current Status</v-card-title>
              <v-card-text>
                <div class="text-body-2 mb-2">
                  Selected team:
                  <strong>{{ selectedTeamName }}</strong>
                </div>
                <div class="text-body-2 mb-2">
                  Teams with zero instructors:
                  <strong>{{ teamsWithoutInstructorsCount }}</strong>
                </div>
                <div class="text-body-2">
                  Selected instructors:
                  <strong>{{ selectedInstructorIds.length }}</strong>
                </div>
              </v-card-text>
              <v-card-actions class="pa-4 pt-0">
                <v-btn variant="text" @click="resetStagedAssignments">Reset</v-btn>
                <v-spacer />
                <v-btn color="primary" @click="goToPreview">Preview</v-btn>
              </v-card-actions>
            </v-card>
          </v-col>
        </v-row>
      </template>

      <template v-else>
        <v-card variant="outlined" class="mb-4">
          <v-card-title class="pa-4 pb-2">Confirm Instructor Assignments</v-card-title>
          <v-card-text>
            <v-alert type="info" variant="tonal" class="mb-4">
              Please review the instructor assignments before confirming. The save will replace the current team instructor roster for this section.
            </v-alert>

            <v-table density="compact" class="mb-4">
              <tbody>
                <tr>
                  <th class="text-left">Section</th>
                  <td>{{ workspace.sectionName }}</td>
                </tr>
                <tr>
                  <th class="text-left">Teams</th>
                  <td>{{ stagedTeams.length }}</td>
                </tr>
                <tr>
                  <th class="text-left">Unique Instructors Used</th>
                  <td>{{ uniqueAssignedInstructorCount }}</td>
                </tr>
                <tr>
                  <th class="text-left">Total Team Assignments</th>
                  <td>{{ totalInstructorAssignments }}</td>
                </tr>
              </tbody>
            </v-table>

            <v-row>
              <v-col v-for="team in stagedTeams" :key="team.teamId" cols="12" md="6">
                <v-card variant="outlined" class="h-100">
                  <v-card-title class="text-subtitle-1 font-weight-bold">{{ team.teamName }}</v-card-title>
                  <v-card-text>
                    <div v-if="team.assignedInstructors.length === 0" class="text-medium-emphasis">No instructors assigned.</div>
                    <v-chip
                      v-for="instructor in team.assignedInstructors"
                      :key="instructor.instructorId"
                      size="small"
                      variant="tonal"
                      color="primary"
                      class="mr-1 mb-1"
                    >
                      {{ instructor.fullName }}
                    </v-chip>
                  </v-card-text>
                </v-card>
              </v-col>
            </v-row>
          </v-card-text>
          <v-divider />
          <v-card-actions class="pa-4">
            <v-btn variant="text" @click="cancelPreview">Cancel</v-btn>
            <v-spacer />
            <v-btn variant="outlined" @click="step = 1">Modify</v-btn>
            <v-btn color="primary" :loading="saving" @click="confirmAssignments">Confirm</v-btn>
          </v-card-actions>
        </v-card>

        <v-card variant="outlined">
          <v-card-title class="pa-4 pb-2">Manual Notification Preview</v-card-title>
          <v-card-text>
            <div class="text-body-2 mb-3">
              After saving, use this list to notify instructors of their assigned teams.
            </div>
            <v-table density="compact">
              <thead>
                <tr>
                  <th>Instructor</th>
                  <th>Email</th>
                  <th>Assigned Teams</th>
                  <th>Section</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in previewNotificationSummary" :key="item.instructorId">
                  <td>{{ item.fullName }}</td>
                  <td>{{ item.email }}</td>
                  <td>{{ item.teamNames.join(', ') }}</td>
                  <td>{{ item.sectionName }}</td>
                </tr>
              </tbody>
            </v-table>
          </v-card-text>
        </v-card>
      </template>

      <v-card v-if="savedNotificationSummary.length" variant="outlined" class="mt-4">
        <v-card-title class="pa-4 pb-2">Notify Instructors</v-card-title>
        <v-card-text>
          <div class="text-body-2 mb-3">
            Assignments were saved successfully. Use this summary to notify instructors manually.
          </div>
          <v-table density="compact">
            <thead>
              <tr>
                <th>Instructor</th>
                <th>Email</th>
                <th>Assigned Teams</th>
                <th>Section</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in savedNotificationSummary" :key="item.instructorId">
                <td>{{ item.fullName }}</td>
                <td>{{ item.email }}</td>
                <td>{{ item.teamNames.join(', ') }}</td>
                <td>{{ item.sectionName }}</td>
              </tr>
            </tbody>
          </v-table>
        </v-card-text>
      </v-card>
    </template>

    <v-snackbar v-model="snackbar.show" :color="snackbar.color" timeout="3500">
      {{ snackbar.message }}
    </v-snackbar>
  </v-container>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { findSections } from '@/features/section/services/sectionService'
import type { SectionSummary } from '@/features/section/services/sectionTypes'
import {
  getTeamInstructorAssignmentWorkspace,
  updateTeamInstructorAssignments
} from '../services/teamService'
import type {
  InstructorAssignmentCandidate,
  TeamInstructorAssignmentInstructor,
  TeamInstructorAssignmentTeam,
  TeamInstructorAssignmentWorkspace,
  UpdateTeamInstructorAssignmentsRequest
} from '../services/teamTypes'

interface NotificationSummaryItem {
  instructorId: number
  fullName: string
  email: string
  teamNames: string[]
  sectionName: string
}

const router = useRouter()

const sections = ref<SectionSummary[]>([])
const selectedSectionId = ref<number | null>(null)
const workspace = ref<TeamInstructorAssignmentWorkspace | null>(null)
const stagedTeams = ref<TeamInstructorAssignmentTeam[]>([])
const selectedTeamId = ref<number | null>(null)
const selectedInstructorIds = ref<number[]>([])
const sectionsLoading = ref(false)
const workspaceLoading = ref(false)
const saving = ref(false)
const step = ref(1)
const validationMessage = ref('')
const savedNotificationSummary = ref<NotificationSummaryItem[]>([])
const snackbar = ref({ show: false, message: '', color: 'success' })

const teamsWithInstructorsCount = computed(() =>
  stagedTeams.value.filter((team) => team.assignedInstructors.length > 0).length
)

const teamsWithoutInstructorsCount = computed(() =>
  stagedTeams.value.filter((team) => team.assignedInstructors.length === 0).length
)

const totalInstructorAssignments = computed(() =>
  stagedTeams.value.reduce((total, team) => total + team.assignedInstructors.length, 0)
)

const uniqueAssignedInstructorCount = computed(() => {
  const uniqueInstructorIds = new Set(
    stagedTeams.value.flatMap((team) => team.assignedInstructors.map((instructor) => instructor.instructorId))
  )
  return uniqueInstructorIds.size
})

const selectedTeamName = computed(() =>
  stagedTeams.value.find((team) => team.teamId === selectedTeamId.value)?.teamName ?? 'None selected'
)

const canAssignSelectedInstructors = computed(() =>
  selectedTeamId.value !== null && selectedInstructorIds.value.length > 0
)

const previewNotificationSummary = computed(() =>
  buildNotificationSummary(stagedTeams.value, workspace.value?.sectionName ?? '')
)

onMounted(async () => {
  await loadSections()
})

async function loadSections() {
  sectionsLoading.value = true
  try {
    const res = await findSections() as any
    sections.value = res.flag ? (res.data ?? []) : []

    const onlySection = sections.value[0]
    if (sections.value.length === 1 && onlySection) {
      selectedSectionId.value = onlySection.sectionId
      await loadWorkspace()
    }
  } catch {
    sections.value = []
    snackbar.value = { show: true, message: 'Failed to load sections.', color: 'error' }
  } finally {
    sectionsLoading.value = false
  }
}

async function loadWorkspace() {
  validationMessage.value = ''
  savedNotificationSummary.value = []
  step.value = 1
  selectedInstructorIds.value = []

  if (!selectedSectionId.value) {
    workspace.value = null
    stagedTeams.value = []
    selectedTeamId.value = null
    return
  }

  workspaceLoading.value = true
  try {
    const res = await getTeamInstructorAssignmentWorkspace(selectedSectionId.value) as any
    if (res.flag && res.data) {
      workspace.value = res.data
      stagedTeams.value = cloneTeams(res.data.teams)
      selectedTeamId.value = stagedTeams.value[0]?.teamId ?? null
      return
    }

    workspace.value = null
    stagedTeams.value = []
    selectedTeamId.value = null
    snackbar.value = { show: true, message: res.message || 'Failed to load assignments.', color: 'error' }
  } catch (error: any) {
    workspace.value = null
    stagedTeams.value = []
    selectedTeamId.value = null
    const message = error?.response?.data?.message || 'Failed to load assignments.'
    snackbar.value = { show: true, message, color: 'error' }
  } finally {
    workspaceLoading.value = false
  }
}

function toggleInstructorSelection(instructorId: number) {
  if (selectedInstructorIds.value.includes(instructorId)) {
    selectedInstructorIds.value = selectedInstructorIds.value.filter((id) => id !== instructorId)
    return
  }
  selectedInstructorIds.value = [...selectedInstructorIds.value, instructorId]
}

function assignSelectedInstructors() {
  if (!selectedTeamId.value || !workspace.value) {
    return
  }

  const selectedInstructors = workspace.value.instructors
    .filter((instructor) => selectedInstructorIds.value.includes(instructor.instructorId))
    .map(toAssignedInstructor)

  const teamIndex = stagedTeams.value.findIndex((team) => team.teamId === selectedTeamId.value)
  if (teamIndex >= 0) {
    const currentTeam = stagedTeams.value[teamIndex]
    if (!currentTeam) {
      return
    }

    const mergedInstructors = [
      ...currentTeam.assignedInstructors,
      ...selectedInstructors.filter((instructor) =>
        !currentTeam.assignedInstructors.some(
          (assignedInstructor) => assignedInstructor.instructorId === instructor.instructorId
        )
      )
    ].sort(compareInstructors)

    stagedTeams.value[teamIndex] = {
      ...currentTeam,
      assignedInstructors: mergedInstructors
    }
  }

  selectedInstructorIds.value = []
  validationMessage.value = ''
}

function removeInstructor(teamId: number, instructorId: number) {
  stagedTeams.value = stagedTeams.value.map((team) => {
    if (team.teamId !== teamId) {
      return team
    }
    return {
      ...team,
      assignedInstructors: team.assignedInstructors.filter((instructor) => instructor.instructorId !== instructorId)
    }
  })
}

function resetStagedAssignments() {
  if (!workspace.value) {
    return
  }

  stagedTeams.value = cloneTeams(workspace.value.teams)
  selectedTeamId.value = stagedTeams.value[0]?.teamId ?? null
  selectedInstructorIds.value = []
  validationMessage.value = ''
}

function goToPreview() {
  const message = validateBeforePreview()
  if (message) {
    validationMessage.value = message
    return
  }

  validationMessage.value = ''
  step.value = 2
}

function cancelPreview() {
  resetStagedAssignments()
  step.value = 1
}

async function confirmAssignments() {
  if (!workspace.value) {
    return
  }

  saving.value = true
  try {
    const payload: UpdateTeamInstructorAssignmentsRequest = {
      sectionId: workspace.value.sectionId,
      assignments: stagedTeams.value.map((team) => ({
        teamId: team.teamId,
        instructorIds: team.assignedInstructors.map((instructor) => instructor.instructorId)
      }))
    }

    const res = await updateTeamInstructorAssignments(payload) as any
    if (res.flag && res.data) {
      workspace.value = res.data
      stagedTeams.value = cloneTeams(res.data.teams)
      selectedTeamId.value = stagedTeams.value[0]?.teamId ?? null
      selectedInstructorIds.value = []
      savedNotificationSummary.value = buildNotificationSummary(res.data.teams, res.data.sectionName)
      validationMessage.value = ''
      step.value = 1
      snackbar.value = { show: true, message: 'Instructor team assignments saved successfully.', color: 'success' }
      return
    }

    snackbar.value = {
      show: true,
      message: res.message || 'Failed to save instructor assignments.',
      color: 'error'
    }
  } catch (error: any) {
    const message = error?.response?.data?.message || 'Failed to save instructor assignments.'
    snackbar.value = { show: true, message, color: 'error' }
  } finally {
    saving.value = false
  }
}

function assignedTeamNamesByInstructorId(instructorId: number) {
  return stagedTeams.value
    .filter((team) => team.assignedInstructors.some((instructor) => instructor.instructorId === instructorId))
    .map((team) => team.teamName)
    .sort((left, right) => left.localeCompare(right, undefined, { sensitivity: 'base' }))
}

function validateBeforePreview() {
  if (!selectedSectionId.value || !workspace.value) {
    return 'Section is required.'
  }
  if (workspace.value.teams.length === 0) {
    return 'At least one team must exist in the selected section.'
  }
  if (workspace.value.instructors.length === 0) {
    return 'At least one enabled instructor must exist before assignments can be saved.'
  }
  if (stagedTeams.value.some((team) => team.assignedInstructors.length === 0)) {
    return 'Each team in the selected section must be assigned at least one instructor.'
  }

  return ''
}

function cloneTeams(teams: TeamInstructorAssignmentTeam[]) {
  return teams.map((team) => ({
    ...team,
    assignedInstructors: [...team.assignedInstructors].sort(compareInstructors)
  }))
}

function compareInstructors(left: TeamInstructorAssignmentInstructor, right: TeamInstructorAssignmentInstructor) {
  return left.fullName.localeCompare(right.fullName, undefined, { sensitivity: 'base' })
    || left.email.localeCompare(right.email, undefined, { sensitivity: 'base' })
}

function toAssignedInstructor(instructor: InstructorAssignmentCandidate): TeamInstructorAssignmentInstructor {
  return {
    instructorId: instructor.instructorId,
    fullName: instructor.fullName,
    email: instructor.email
  }
}

function buildNotificationSummary(teams: TeamInstructorAssignmentTeam[], sectionName: string) {
  const itemsByInstructorId = new Map<number, NotificationSummaryItem>()

  for (const team of teams) {
    for (const instructor of team.assignedInstructors) {
      const existingItem = itemsByInstructorId.get(instructor.instructorId)
      if (existingItem) {
        existingItem.teamNames.push(team.teamName)
        existingItem.teamNames.sort((left, right) => left.localeCompare(right, undefined, { sensitivity: 'base' }))
        continue
      }

      itemsByInstructorId.set(instructor.instructorId, {
        instructorId: instructor.instructorId,
        fullName: instructor.fullName,
        email: instructor.email,
        teamNames: [team.teamName],
        sectionName
      })
    }
  }

  return [...itemsByInstructorId.values()].sort((left, right) =>
    left.fullName.localeCompare(right.fullName, undefined, { sensitivity: 'base' })
    || left.email.localeCompare(right.email, undefined, { sensitivity: 'base' })
  )
}
</script>

<style scoped lang="scss">
.team-card {
  border-color: rgba(var(--v-theme-outline), 0.5);
}

.team-card--selected {
  border-color: rgb(var(--v-theme-primary));
  box-shadow: 0 0 0 1px rgba(var(--v-theme-primary), 0.2);
}

.assigned-list {
  display: flex;
  flex-wrap: wrap;
}

.chip-group {
  max-width: 260px;
}
</style>
