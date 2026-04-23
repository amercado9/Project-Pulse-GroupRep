<template>
  <v-container>
    <v-btn variant="text" prepend-icon="mdi-arrow-left" class="mb-4" @click="router.push({ name: 'teams' })">
      Back to Teams
    </v-btn>

    <v-row v-if="loading">
      <v-col class="text-center"><v-progress-circular indeterminate /></v-col>
    </v-row>

    <template v-else-if="team">
      <v-row class="mb-2" align="center">
        <v-col>
          <h2 class="text-h5 font-weight-bold">{{ team.teamName }}</h2>
        </v-col>
        <v-col v-if="isAdmin" cols="auto">
          <div class="admin-actions">
            <v-btn color="primary" prepend-icon="mdi-pencil" @click="openEditDialog">Edit Team</v-btn>
            <v-btn color="error" variant="outlined" prepend-icon="mdi-delete" @click="openDeleteDialog">
              Delete Team
            </v-btn>
          </div>
        </v-col>
        <v-col cols="auto">
          <v-chip color="primary" variant="tonal">
            {{ team.sectionName }}
          </v-chip>
        </v-col>
      </v-row>

      <v-card variant="outlined" class="mb-4">
        <v-card-title class="text-subtitle-1 font-weight-bold pa-4 pb-2">Team Info</v-card-title>
        <v-card-text>
          <v-row>
            <v-col cols="12" md="6">
              <div class="text-caption text-medium-emphasis">Description</div>
              <div>{{ team.teamDescription ?? '—' }}</div>
            </v-col>
            <v-col cols="12" md="6">
              <div class="text-caption text-medium-emphasis">Website</div>
              <a
                v-if="team.teamWebsiteUrl"
                :href="team.teamWebsiteUrl"
                target="_blank"
                rel="noopener noreferrer"
              >
                {{ team.teamWebsiteUrl }}
              </a>
              <div v-else>—</div>
            </v-col>
          </v-row>
        </v-card-text>
      </v-card>

      <v-row>
        <v-col cols="12" md="6">
          <v-card variant="outlined" class="fill-height">
            <v-card-title class="text-subtitle-1 font-weight-bold pa-4 pb-2 d-flex align-center">
              <span>Team Members</span>
              <v-spacer />
              <v-btn
                v-if="isAdmin && team.teamMembers.length > 0"
                size="small"
                color="error"
                variant="outlined"
                prepend-icon="mdi-account-remove"
                @click="openRemoveDialog"
              >
                Remove Student
              </v-btn>
            </v-card-title>
            <v-card-text>
              <v-alert v-if="team.teamMembers.length === 0" type="info" variant="tonal" density="compact">
                No team members assigned.
              </v-alert>
              <div v-else>
                <v-chip
                  v-for="member in team.teamMembers"
                  :key="member.studentId"
                  size="small"
                  class="mr-1 mb-1"
                >
                  {{ member.fullName }}
                </v-chip>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" md="6">
          <v-card variant="outlined" class="fill-height">
            <v-card-title class="text-subtitle-1 font-weight-bold pa-4 pb-2">Instructors</v-card-title>
            <v-card-text>
              <v-alert v-if="team.instructorNames.length === 0" type="info" variant="tonal" density="compact">
                No instructors assigned.
              </v-alert>
              <div v-else>
                <v-chip
                  v-for="instructor in team.instructorNames"
                  :key="instructor"
                  size="small"
                  color="primary"
                  variant="tonal"
                  class="mr-1 mb-1"
                >
                  {{ instructor }}
                </v-chip>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <v-card v-if="removedStudentNotification" variant="outlined" class="mt-4">
        <v-card-title class="text-subtitle-1 font-weight-bold pa-4 pb-2">Notify Student</v-card-title>
        <v-card-text>
          <div class="text-body-2 mb-3">
            Use this summary to notify the student manually about the team removal.
          </div>
          <v-table density="compact">
            <tbody>
              <tr>
                <th class="text-left">Student</th>
                <td>{{ removedStudentNotification.fullName }}</td>
              </tr>
              <tr>
                <th class="text-left">Email</th>
                <td>{{ removedStudentNotification.email }}</td>
              </tr>
              <tr>
                <th class="text-left">Previous Team</th>
                <td>{{ removedStudentNotification.teamName }}</td>
              </tr>
              <tr>
                <th class="text-left">Section</th>
                <td>{{ removedStudentNotification.sectionName }}</td>
              </tr>
              <tr>
                <th class="text-left">Status</th>
                <td>{{ removedStudentNotification.status }}</td>
              </tr>
            </tbody>
          </v-table>
        </v-card-text>
      </v-card>
    </template>

    <v-dialog v-model="editDialog" max-width="700" persistent>
      <v-card>
        <v-card-title class="pa-4">
          {{ editStep === 1 ? 'Edit Team' : 'Confirm Team Changes' }}
        </v-card-title>
        <v-divider />

        <v-card-text v-if="editStep === 1" class="pa-4">
          <v-text-field
            v-model="form.teamName"
            label="Team Name"
            placeholder="e.g. Peer Evaluation Tool team"
            :error-messages="editErrors.teamName"
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
            :error-messages="editErrors.teamWebsiteUrl"
          />
        </v-card-text>

        <v-card-text v-else class="pa-4">
          <v-alert type="info" variant="tonal" class="mb-4">
            Please review the team changes before confirming.
          </v-alert>
          <v-table density="compact">
            <tbody>
              <tr>
                <th class="text-left">Section</th>
                <td>{{ team?.sectionName ?? '—' }}</td>
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
          <v-btn variant="text" @click="cancelEdit">Cancel</v-btn>
          <v-spacer />
          <v-btn v-if="editStep === 2" variant="outlined" @click="editStep = 1">Modify</v-btn>
          <v-btn
            color="primary"
            :loading="editSaving"
            @click="editStep === 1 ? goToEditPreview() : confirmEdit()"
          >
            {{ editStep === 1 ? 'Preview' : 'Confirm' }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="removeDialog" max-width="720" persistent>
      <v-card>
        <v-card-title class="pa-4">
          {{ removeStep === 1 ? 'Remove Student' : 'Confirm Student Removal' }}
        </v-card-title>
        <v-divider />

        <v-card-text v-if="removeStep === 1" class="pa-4">
          <v-select
            v-model="selectedStudentId"
            :items="team?.teamMembers ?? []"
            item-title="fullName"
            item-value="studentId"
            label="Student to Remove"
            :error-messages="removeErrors.studentId"
          />

          <v-card v-if="selectedTeamMember" variant="outlined" class="mt-4">
            <v-card-text>
              <div class="text-caption text-medium-emphasis">Selected Student Email</div>
              <div>{{ selectedTeamMember.email }}</div>
            </v-card-text>
          </v-card>
        </v-card-text>

        <v-card-text v-else class="pa-4">
          <v-alert type="warning" variant="tonal" class="mb-4">
            Please review the student removal before confirming.
          </v-alert>

          <v-table density="compact" class="mb-4">
            <tbody>
              <tr>
                <th class="text-left">Section</th>
                <td>{{ team?.sectionName ?? '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Team</th>
                <td>{{ team?.teamName ?? '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Student</th>
                <td>{{ selectedTeamMember?.fullName ?? '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Email</th>
                <td>{{ selectedTeamMember?.email ?? '—' }}</td>
              </tr>
            </tbody>
          </v-table>

          <div class="text-subtitle-2 font-weight-bold mb-2">Remaining Team Members</div>
          <v-alert
            v-if="remainingTeamMembers.length === 0"
            type="info"
            variant="tonal"
            density="compact"
            class="mb-4"
          >
            No team members will remain after this removal.
          </v-alert>
          <div v-else class="mb-4">
            <v-chip
              v-for="member in remainingTeamMembers"
              :key="member.studentId"
              size="small"
              class="mr-1 mb-1"
            >
              {{ member.fullName }}
            </v-chip>
          </div>

          <div class="text-subtitle-2 font-weight-bold mb-2">Manual Notification Preview</div>
          <v-table density="compact">
            <tbody>
              <tr>
                <th class="text-left">Student</th>
                <td>{{ selectedTeamMember?.fullName ?? '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Email</th>
                <td>{{ selectedTeamMember?.email ?? '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Previous Team</th>
                <td>{{ team?.teamName ?? '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Section</th>
                <td>{{ team?.sectionName ?? '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Status</th>
                <td>Removed from team</td>
              </tr>
            </tbody>
          </v-table>
        </v-card-text>

        <v-divider />
        <v-card-actions class="pa-4">
          <v-btn variant="text" @click="cancelRemove">Cancel</v-btn>
          <v-spacer />
          <v-btn v-if="removeStep === 2" variant="outlined" @click="removeStep = 1">Modify</v-btn>
          <v-btn
            color="error"
            :loading="removeSaving"
            @click="removeStep === 1 ? goToRemovePreview() : confirmRemove()"
          >
            {{ removeStep === 1 ? 'Preview' : 'Confirm' }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="deleteDialog" max-width="760" persistent>
      <v-card>
        <v-card-title class="pa-4">
          {{ deleteStep === 1 ? 'Delete Team' : 'Confirm Team Deletion' }}
        </v-card-title>
        <v-divider />

        <v-card-text v-if="deleteStep === 1" class="pa-4">
          <v-alert type="warning" variant="tonal" class="mb-4">
            Deleting this senior design team is permanent and cannot be undone.
          </v-alert>

          <div class="text-body-2 mb-4">
            If you continue, the system will physically delete this team. Students and instructors will be removed
            from the team automatically. Associated WARs and peer evaluations would also be deleted if those
            records existed.
          </div>

          <v-table density="compact" class="mb-4">
            <tbody>
              <tr>
                <th class="text-left">Section</th>
                <td>{{ team?.sectionName ?? '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Team</th>
                <td>{{ team?.teamName ?? '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Deletion Strategy</th>
                <td>Physical delete</td>
              </tr>
              <tr>
                <th class="text-left">Current Scope</th>
                <td>Existing team and membership data in the current system</td>
              </tr>
            </tbody>
          </v-table>

          <v-alert type="info" variant="tonal">
            WAR and peer-evaluation modules are not present in this repo yet, so this implementation deletes the
            existing team and membership records now and defers future related-record cleanup until those modules exist.
          </v-alert>
        </v-card-text>

        <v-card-text v-else class="pa-4">
          <v-alert type="warning" variant="tonal" class="mb-4">
            Please review the deletion consequences before confirming.
          </v-alert>

          <v-table density="compact" class="mb-4">
            <tbody>
              <tr>
                <th class="text-left">Section</th>
                <td>{{ team?.sectionName ?? '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Team</th>
                <td>{{ team?.teamName ?? '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Status</th>
                <td>Permanently delete this team</td>
              </tr>
            </tbody>
          </v-table>

          <div class="text-subtitle-2 font-weight-bold mb-2">Students to Remove</div>
          <v-alert
            v-if="!team?.teamMembers.length"
            type="info"
            variant="tonal"
            density="compact"
            class="mb-4"
          >
            No students are currently assigned to this team.
          </v-alert>
          <div v-else class="mb-4">
            <v-chip
              v-for="member in team.teamMembers"
              :key="member.studentId"
              size="small"
              class="mr-1 mb-1"
            >
              {{ member.fullName }}
            </v-chip>
          </div>

          <div class="text-subtitle-2 font-weight-bold mb-2">Instructors to Remove</div>
          <v-alert
            v-if="!team?.instructorNames.length"
            type="info"
            variant="tonal"
            density="compact"
            class="mb-4"
          >
            No instructors are currently assigned to this team.
          </v-alert>
          <div v-else class="mb-4">
            <v-chip
              v-for="instructor in team.instructorNames"
              :key="instructor"
              size="small"
              color="primary"
              variant="tonal"
              class="mr-1 mb-1"
            >
              {{ instructor }}
            </v-chip>
          </div>

          <v-alert type="error" variant="tonal">
            This action cannot be undone.
          </v-alert>
        </v-card-text>

        <v-divider />
        <v-card-actions class="pa-4">
          <v-btn variant="text" @click="cancelDelete">Cancel</v-btn>
          <v-spacer />
          <v-btn v-if="deleteStep === 2" variant="outlined" @click="deleteStep = 1">Modify</v-btn>
          <v-btn
            color="error"
            :loading="deleteSaving"
            @click="deleteStep === 1 ? goToDeletePreview() : confirmDelete()"
          >
            {{ deleteStep === 1 ? 'Preview' : 'Confirm' }}
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
import { useRoute, useRouter } from 'vue-router'
import { useUserInfoStore } from '@/stores/userInfo'
import { deleteTeam, getTeam, removeStudentFromTeam, updateTeam } from '../services/teamService'
import { useTeamNotificationsStore } from '../stores/teamNotifications'
import type { TeamDeletionNotification, TeamDetail, TeamMemberDetail, UpdateTeamRequest } from '../services/teamTypes'

interface RemovedStudentNotification {
  fullName: string
  email: string
  teamName: string
  sectionName: string
  status: string
}

const route = useRoute()
const router = useRouter()
const userInfoStore = useUserInfoStore()
const teamNotificationsStore = useTeamNotificationsStore()

const team = ref<TeamDetail | null>(null)
const loading = ref(false)
const editDialog = ref(false)
const editStep = ref(1)
const editSaving = ref(false)
const editErrors = ref<{ teamName?: string; teamWebsiteUrl?: string }>({})
const removeDialog = ref(false)
const removeStep = ref(1)
const removeSaving = ref(false)
const removeErrors = ref<{ studentId?: string }>({})
const selectedStudentId = ref<number | null>(null)
const deleteDialog = ref(false)
const deleteStep = ref(1)
const deleteSaving = ref(false)
const removedStudentNotification = ref<RemovedStudentNotification | null>(null)
const snackbar = ref({ show: false, message: '', color: 'success' })

const emptyForm = () => ({
  teamName: '',
  teamDescription: '',
  teamWebsiteUrl: ''
})

const form = ref(emptyForm())

const isAdmin = computed(() => userInfoStore.isAdmin)

const previewPayload = computed<UpdateTeamRequest>(() => ({
  teamName: form.value.teamName.trim(),
  teamDescription: normalizeOptional(form.value.teamDescription),
  teamWebsiteUrl: normalizeOptional(form.value.teamWebsiteUrl)
}))

const selectedTeamMember = computed<TeamMemberDetail | null>(() =>
  team.value?.teamMembers.find((member) => member.studentId === selectedStudentId.value) ?? null
)

const remainingTeamMembers = computed(() =>
  team.value?.teamMembers.filter((member) => member.studentId !== selectedStudentId.value) ?? []
)

onMounted(async () => {
  loading.value = true
  try {
    const teamId = Number(route.params.id)
    const res = await getTeam(teamId) as any
    if (res.flag && res.data) {
      team.value = res.data
    } else {
      router.replace({ name: 'not-found' })
    }
  } catch {
    router.replace({ name: 'not-found' })
  } finally {
    loading.value = false
  }
})

function openEditDialog() {
  if (!team.value) return

  form.value = {
    teamName: team.value.teamName,
    teamDescription: team.value.teamDescription ?? '',
    teamWebsiteUrl: team.value.teamWebsiteUrl ?? ''
  }
  editErrors.value = {}
  editStep.value = 1
  editDialog.value = true
}

function cancelEdit() {
  editDialog.value = false
}

function openRemoveDialog() {
  selectedStudentId.value = null
  removeErrors.value = {}
  removeStep.value = 1
  removeDialog.value = true
}

function cancelRemove() {
  removeDialog.value = false
  removeErrors.value = {}
  selectedStudentId.value = null
  removeStep.value = 1
}

function openDeleteDialog() {
  deleteStep.value = 1
  deleteDialog.value = true
}

function cancelDelete() {
  deleteDialog.value = false
  deleteStep.value = 1
}

function validateEdit(): boolean {
  editErrors.value = {}
  let valid = true

  if (!form.value.teamName.trim()) {
    editErrors.value.teamName = 'Team name is required.'
    valid = false
  }

  if (form.value.teamWebsiteUrl.trim()) {
    try {
      const url = new URL(form.value.teamWebsiteUrl.trim())
      if (url.protocol !== 'http:' && url.protocol !== 'https:') {
        editErrors.value.teamWebsiteUrl = 'Team website URL must start with http:// or https://.'
        valid = false
      }
    } catch {
      editErrors.value.teamWebsiteUrl = 'Team website URL must be a valid absolute URL.'
      valid = false
    }
  }

  return valid
}

function validateRemove(): boolean {
  removeErrors.value = {}
  if (!selectedStudentId.value) {
    removeErrors.value.studentId = 'Student is required.'
    return false
  }
  return true
}

function goToEditPreview() {
  if (!validateEdit()) return
  editStep.value = 2
}

function goToRemovePreview() {
  if (!validateRemove()) return
  removeStep.value = 2
}

function goToDeletePreview() {
  deleteStep.value = 2
}

async function confirmEdit() {
  if (!team.value) return

  editSaving.value = true
  try {
    const res = await updateTeam(team.value.teamId, previewPayload.value) as any
    if (res.flag && res.data) {
      team.value = res.data
      editDialog.value = false
      snackbar.value = { show: true, message: 'Team updated successfully.', color: 'success' }
    }
  } catch (error: any) {
    const message = error?.response?.data?.message
    if (typeof message === 'string') {
      if (message.toLowerCase().includes('team name')) {
        editErrors.value.teamName = message
        editStep.value = 1
      } else if (message.toLowerCase().includes('website')) {
        editErrors.value.teamWebsiteUrl = message
        editStep.value = 1
      }
    }
  } finally {
    editSaving.value = false
  }
}

async function confirmRemove() {
  if (!team.value || !selectedTeamMember.value) return

  removeSaving.value = true
  const removedStudent = selectedTeamMember.value

  try {
    const res = await removeStudentFromTeam(team.value.teamId, removedStudent.studentId) as any
    if (res.flag && res.data) {
      const updatedTeam = res.data as TeamDetail
      team.value = updatedTeam
      removedStudentNotification.value = {
        fullName: removedStudent.fullName,
        email: removedStudent.email,
        teamName: updatedTeam.teamName,
        sectionName: updatedTeam.sectionName,
        status: 'Removed from team'
      }
      removeDialog.value = false
      removeErrors.value = {}
      selectedStudentId.value = null
      removeStep.value = 1
      snackbar.value = { show: true, message: 'Student removed from team successfully.', color: 'success' }
      return
    }

    snackbar.value = { show: true, message: res.message || 'Failed to remove student from team.', color: 'error' }
  } catch (error: any) {
    const message = error?.response?.data?.message || 'Failed to remove student from team.'
    snackbar.value = { show: true, message, color: 'error' }
  } finally {
    removeSaving.value = false
  }
}

async function confirmDelete() {
  if (!team.value) return

  deleteSaving.value = true
  const notification = buildDeletedTeamNotification(team.value)

  try {
    const res = await deleteTeam(team.value.teamId) as any
    if (res.flag) {
      teamNotificationsStore.setDeletedTeamNotification(notification)
      await router.push({ name: 'teams' })
      return
    }

    snackbar.value = { show: true, message: res.message || 'Failed to delete team.', color: 'error' }
  } catch (error: any) {
    const message = error?.response?.data?.message || 'Failed to delete team.'
    snackbar.value = { show: true, message, color: 'error' }
  } finally {
    deleteSaving.value = false
  }
}

function buildDeletedTeamNotification(currentTeam: TeamDetail): TeamDeletionNotification {
  return {
    teamId: currentTeam.teamId,
    teamName: currentTeam.teamName,
    sectionName: currentTeam.sectionName,
    studentNotifications: currentTeam.teamMembers.map((member) => ({
      fullName: member.fullName,
      email: member.email
    })),
    instructorNotifications: currentTeam.instructorNames.map((fullName) => ({ fullName })),
    status: 'Team deleted'
  }
}

function normalizeOptional(value: string): string | null {
  const trimmed = value.trim()
  return trimmed ? trimmed : null
}
</script>

<style scoped lang="scss">
.admin-actions {
  display: flex;
  gap: 12px;
}
</style>
