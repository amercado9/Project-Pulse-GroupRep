<template>
  <v-container>
    <v-btn variant="text" prepend-icon="mdi-arrow-left" class="mb-4" @click="router.push({ name: 'teams' })">
      Back to Teams
    </v-btn>

    <v-row class="mb-4" align="center">
      <v-col>
        <h2 class="text-h5 font-weight-bold">Assign Students to Senior Design Teams</h2>
        <div class="text-body-2 text-medium-emphasis">
          Select a section, stage assignments, review them, then confirm the final roster.
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
              {{ workspace.students.length }} eligible students
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
      Select a section to load its teams and student roster.
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
                  {{ assignedCount }}/{{ workspace.students.length }} assigned
                </v-chip>
              </v-card-title>
              <v-card-text>
                <v-alert v-if="workspace.teams.length === 0" type="warning" variant="tonal">
                  At least one team must exist in the selected section before assigning students.
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
                          {{ team.assignedStudents.length }} students
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
                        <div v-if="team.assignedStudents.length === 0" class="text-medium-emphasis">
                          No students assigned yet.
                        </div>
                        <div v-else class="assigned-list">
                          <v-chip
                            v-for="student in team.assignedStudents"
                            :key="student.studentId"
                            class="mr-2 mb-2"
                            color="primary"
                            variant="tonal"
                          >
                            {{ student.fullName }}
                            <template #append>
                              <v-btn
                                icon="mdi-close"
                                size="x-small"
                                variant="text"
                                class="ml-1"
                                @click.stop="removeStudent(team.teamId, student.studentId)"
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
              <v-card-title class="pa-4 pb-2">Student Pool</v-card-title>
              <v-card-text>
                <v-alert type="info" variant="tonal" density="compact" class="mb-4">
                  Select one team, then choose students to assign or move. Reassigning a student removes them from the prior team automatically.
                </v-alert>

                <v-alert v-if="workspace.students.length === 0" type="warning" variant="tonal">
                  At least one eligible student must exist in the selected section before assignments can be saved.
                </v-alert>

                <v-list v-else lines="two" density="compact">
                  <v-list-item
                    v-for="student in workspace.students"
                    :key="student.studentId"
                    :active="selectedStudentIds.includes(student.studentId)"
                    @click="toggleStudentSelection(student.studentId)"
                  >
                    <template #prepend>
                      <v-checkbox-btn
                        :model-value="selectedStudentIds.includes(student.studentId)"
                        @click.stop
                        @update:model-value="toggleStudentSelection(student.studentId)"
                      />
                    </template>

                    <v-list-item-title>{{ student.fullName }}</v-list-item-title>
                    <v-list-item-subtitle>{{ student.email }}</v-list-item-subtitle>

                    <template #append>
                      <v-chip
                        v-if="assignedTeamNameByStudentId(student.studentId)"
                        size="small"
                        color="primary"
                        variant="tonal"
                      >
                        {{ assignedTeamNameByStudentId(student.studentId) }}
                      </v-chip>
                      <v-chip v-else size="small" color="warning" variant="tonal">
                        Unassigned
                      </v-chip>
                    </template>
                  </v-list-item>
                </v-list>

                <v-divider class="my-4" />

                <v-btn
                  block
                  color="primary"
                  prepend-icon="mdi-account-arrow-right"
                  :disabled="!canAssignSelectedStudents"
                  @click="assignSelectedStudents"
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
                  Unassigned students:
                  <strong>{{ unassignedCount }}</strong>
                </div>
                <div class="text-body-2">
                  Selected students:
                  <strong>{{ selectedStudentIds.length }}</strong>
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
          <v-card-title class="pa-4 pb-2">Confirm Student Assignments</v-card-title>
          <v-card-text>
            <v-alert type="info" variant="tonal" class="mb-4">
              Please review the team assignments before confirming. The save will replace the current memberships for this section.
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
                  <th class="text-left">Students Assigned</th>
                  <td>{{ assignedCount }}</td>
                </tr>
              </tbody>
            </v-table>

            <v-row>
              <v-col v-for="team in stagedTeams" :key="team.teamId" cols="12" md="6">
                <v-card variant="outlined" class="h-100">
                  <v-card-title class="text-subtitle-1 font-weight-bold">{{ team.teamName }}</v-card-title>
                  <v-card-text>
                    <div v-if="team.assignedStudents.length === 0" class="text-medium-emphasis">No students assigned.</div>
                    <v-chip
                      v-for="student in team.assignedStudents"
                      :key="student.studentId"
                      size="small"
                      variant="tonal"
                      color="primary"
                      class="mr-1 mb-1"
                    >
                      {{ student.fullName }}
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
              After saving, use this list to notify students of their assigned team.
            </div>
            <v-table density="compact">
              <thead>
                <tr>
                  <th>Student</th>
                  <th>Email</th>
                  <th>Assigned Team</th>
                  <th>Section</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in previewNotificationSummary" :key="item.studentId">
                  <td>{{ item.fullName }}</td>
                  <td>{{ item.email }}</td>
                  <td>{{ item.teamName }}</td>
                  <td>{{ item.sectionName }}</td>
                </tr>
              </tbody>
            </v-table>
          </v-card-text>
        </v-card>
      </template>

      <v-card v-if="savedNotificationSummary.length" variant="outlined" class="mt-4">
        <v-card-title class="pa-4 pb-2">Notify Students</v-card-title>
        <v-card-text>
          <div class="text-body-2 mb-3">
            Assignments were saved successfully. Use this summary to notify students manually.
          </div>
          <v-table density="compact">
            <thead>
              <tr>
                <th>Student</th>
                <th>Email</th>
                <th>Assigned Team</th>
                <th>Section</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in savedNotificationSummary" :key="item.studentId">
                <td>{{ item.fullName }}</td>
                <td>{{ item.email }}</td>
                <td>{{ item.teamName }}</td>
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
  getTeamStudentAssignmentWorkspace,
  updateTeamStudentAssignments
} from '../services/teamService'
import type {
  StudentAssignmentCandidate,
  TeamStudentAssignmentTeam,
  TeamStudentAssignmentWorkspace,
  UpdateTeamStudentAssignmentsRequest
} from '../services/teamTypes'

interface NotificationSummaryItem {
  studentId: number
  fullName: string
  email: string
  teamName: string
  sectionName: string
}

const router = useRouter()

const sections = ref<SectionSummary[]>([])
const selectedSectionId = ref<number | null>(null)
const workspace = ref<TeamStudentAssignmentWorkspace | null>(null)
const stagedTeams = ref<TeamStudentAssignmentTeam[]>([])
const selectedTeamId = ref<number | null>(null)
const selectedStudentIds = ref<number[]>([])
const sectionsLoading = ref(false)
const workspaceLoading = ref(false)
const saving = ref(false)
const step = ref(1)
const validationMessage = ref('')
const savedNotificationSummary = ref<NotificationSummaryItem[]>([])
const snackbar = ref({ show: false, message: '', color: 'success' })

const assignedCount = computed(() =>
  stagedTeams.value.reduce((total, team) => total + team.assignedStudents.length, 0)
)

const unassignedCount = computed(() => {
  const totalStudents = workspace.value?.students.length ?? 0
  return Math.max(totalStudents - assignedCount.value, 0)
})

const selectedTeamName = computed(() =>
  stagedTeams.value.find((team) => team.teamId === selectedTeamId.value)?.teamName ?? 'None selected'
)

const canAssignSelectedStudents = computed(() =>
  selectedTeamId.value !== null && selectedStudentIds.value.length > 0
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
  selectedStudentIds.value = []

  if (!selectedSectionId.value) {
    workspace.value = null
    stagedTeams.value = []
    selectedTeamId.value = null
    return
  }

  workspaceLoading.value = true
  try {
    const res = await getTeamStudentAssignmentWorkspace(selectedSectionId.value) as any
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

function toggleStudentSelection(studentId: number) {
  if (selectedStudentIds.value.includes(studentId)) {
    selectedStudentIds.value = selectedStudentIds.value.filter((id) => id !== studentId)
    return
  }
  selectedStudentIds.value = [...selectedStudentIds.value, studentId]
}

function assignSelectedStudents() {
  if (!selectedTeamId.value || !workspace.value) {
    return
  }

  const selectedStudents = workspace.value.students.filter((student) =>
    selectedStudentIds.value.includes(student.studentId)
  )

  stagedTeams.value = stagedTeams.value.map((team) => ({
    ...team,
    assignedStudents: team.assignedStudents.filter(
      (student) => !selectedStudentIds.value.includes(student.studentId)
    )
  }))

  const teamIndex = stagedTeams.value.findIndex((team) => team.teamId === selectedTeamId.value)
  if (teamIndex >= 0) {
    const currentTeam = stagedTeams.value[teamIndex]
    if (!currentTeam) {
      return
    }

    const mergedStudents = [
      ...currentTeam.assignedStudents,
      ...selectedStudents.filter((student) =>
        !currentTeam.assignedStudents.some(
          (assignedStudent) => assignedStudent.studentId === student.studentId
        )
      )
    ].sort(compareStudents)

    stagedTeams.value[teamIndex] = {
      ...currentTeam,
      assignedStudents: mergedStudents
    }
  }

  selectedStudentIds.value = []
  validationMessage.value = ''
}

function removeStudent(teamId: number, studentId: number) {
  stagedTeams.value = stagedTeams.value.map((team) => {
    if (team.teamId !== teamId) {
      return team
    }
    return {
      ...team,
      assignedStudents: team.assignedStudents.filter((student) => student.studentId !== studentId)
    }
  })
}

function resetStagedAssignments() {
  if (!workspace.value) {
    return
  }

  stagedTeams.value = cloneTeams(workspace.value.teams)
  selectedTeamId.value = stagedTeams.value[0]?.teamId ?? null
  selectedStudentIds.value = []
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
    const payload: UpdateTeamStudentAssignmentsRequest = {
      sectionId: workspace.value.sectionId,
      assignments: stagedTeams.value.map((team) => ({
        teamId: team.teamId,
        studentIds: team.assignedStudents.map((student) => student.studentId)
      }))
    }

    const res = await updateTeamStudentAssignments(payload) as any
    if (res.flag && res.data) {
      workspace.value = res.data
      stagedTeams.value = cloneTeams(res.data.teams)
      selectedTeamId.value = stagedTeams.value[0]?.teamId ?? null
      selectedStudentIds.value = []
      savedNotificationSummary.value = buildNotificationSummary(res.data.teams, res.data.sectionName)
      validationMessage.value = ''
      step.value = 1
      snackbar.value = { show: true, message: 'Student team assignments saved successfully.', color: 'success' }
      return
    }

    snackbar.value = {
      show: true,
      message: res.message || 'Failed to save student assignments.',
      color: 'error'
    }
  } catch (error: any) {
    const message = error?.response?.data?.message || 'Failed to save student assignments.'
    snackbar.value = { show: true, message, color: 'error' }
  } finally {
    saving.value = false
  }
}

function assignedTeamNameByStudentId(studentId: number) {
  return stagedTeams.value.find((team) =>
    team.assignedStudents.some((student) => student.studentId === studentId)
  )?.teamName ?? null
}

function validateBeforePreview() {
  if (!selectedSectionId.value || !workspace.value) {
    return 'Section is required.'
  }
  if (workspace.value.teams.length === 0) {
    return 'At least one team must exist in the selected section.'
  }
  if (workspace.value.students.length === 0) {
    return 'At least one eligible student must exist in the selected section.'
  }

  const assignedStudentIds = stagedTeams.value.flatMap((team) =>
    team.assignedStudents.map((student) => student.studentId)
  )

  if (assignedStudentIds.length !== workspace.value.students.length) {
    return 'Every eligible student in the selected section must be assigned to exactly one team before preview.'
  }

  const uniqueStudentIds = new Set(assignedStudentIds)
  if (uniqueStudentIds.size !== assignedStudentIds.length) {
    return 'Each student may only be assigned to one team.'
  }

  return ''
}

function cloneTeams(teams: TeamStudentAssignmentTeam[]) {
  return teams.map((team) => ({
    ...team,
    assignedStudents: [...team.assignedStudents].sort(compareStudents)
  }))
}

function compareStudents(left: StudentAssignmentCandidate, right: StudentAssignmentCandidate) {
  return left.fullName.localeCompare(right.fullName, undefined, { sensitivity: 'base' })
    || left.email.localeCompare(right.email, undefined, { sensitivity: 'base' })
}

function buildNotificationSummary(teams: TeamStudentAssignmentTeam[], sectionName: string) {
  return teams
    .flatMap((team) => team.assignedStudents.map((student) => ({
      studentId: student.studentId,
      fullName: student.fullName,
      email: student.email,
      teamName: team.teamName,
      sectionName
    })))
    .sort((left, right) =>
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
</style>
