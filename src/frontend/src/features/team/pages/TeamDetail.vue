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
          <v-btn color="primary" prepend-icon="mdi-pencil" @click="openEditDialog">Edit Team</v-btn>
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
            <v-card-title class="text-subtitle-1 font-weight-bold pa-4 pb-2">Team Members</v-card-title>
            <v-card-text>
              <v-alert v-if="team.teamMemberNames.length === 0" type="info" variant="tonal" density="compact">
                No team members assigned.
              </v-alert>
              <div v-else>
                <v-chip
                  v-for="member in team.teamMemberNames"
                  :key="member"
                  size="small"
                  class="mr-1 mb-1"
                >
                  {{ member }}
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
    </template>

    <v-dialog v-model="dialog" max-width="700" persistent>
      <v-card>
        <v-card-title class="pa-4">
          {{ step === 1 ? 'Edit Team' : 'Confirm Team Changes' }}
        </v-card-title>
        <v-divider />

        <v-card-text v-if="step === 1" class="pa-4">
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
          <v-btn v-if="step === 2" variant="outlined" @click="step = 1">Modify</v-btn>
          <v-btn
            color="primary"
            :loading="saving"
            @click="step === 1 ? goToPreview() : confirmEdit()"
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
import { useRoute, useRouter } from 'vue-router'
import { useUserInfoStore } from '@/stores/userInfo'
import { getTeam, updateTeam } from '../services/teamService'
import type { TeamDetail, UpdateTeamRequest } from '../services/teamTypes'

const route = useRoute()
const router = useRouter()
const userInfoStore = useUserInfoStore()

const team = ref<TeamDetail | null>(null)
const loading = ref(false)
const dialog = ref(false)
const step = ref(1)
const saving = ref(false)
const errors = ref<{ teamName?: string; teamWebsiteUrl?: string }>({})
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
  errors.value = {}
  step.value = 1
  dialog.value = true
}

function cancelEdit() {
  dialog.value = false
}

function validate(): boolean {
  errors.value = {}
  let valid = true

  if (!form.value.teamName.trim()) {
    errors.value.teamName = 'Team name is required.'
    valid = false
  }

  if (form.value.teamWebsiteUrl.trim()) {
    try {
      const url = new URL(form.value.teamWebsiteUrl.trim())
      if (url.protocol !== 'http:' && url.protocol !== 'https:') {
        errors.value.teamWebsiteUrl = 'Team website URL must start with http:// or https://.'
        valid = false
      }
    } catch {
      errors.value.teamWebsiteUrl = 'Team website URL must be a valid absolute URL.'
      valid = false
    }
  }

  return valid
}

function goToPreview() {
  if (!validate()) return
  step.value = 2
}

async function confirmEdit() {
  if (!team.value) return

  saving.value = true
  try {
    const res = await updateTeam(team.value.teamId, previewPayload.value) as any
    if (res.flag && res.data) {
      team.value = res.data
      dialog.value = false
      snackbar.value = { show: true, message: 'Team updated successfully.', color: 'success' }
    }
  } catch (error: any) {
    const message = error?.response?.data?.message
    if (typeof message === 'string') {
      if (message.toLowerCase().includes('team name')) {
        errors.value.teamName = message
        step.value = 1
      } else if (message.toLowerCase().includes('website')) {
        errors.value.teamWebsiteUrl = message
        step.value = 1
      }
    }
  } finally {
    saving.value = false
  }
}

function normalizeOptional(value: string): string | null {
  const trimmed = value.trim()
  return trimmed ? trimmed : null
}
</script>
