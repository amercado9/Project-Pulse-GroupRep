<template>
  <v-container>
    <v-btn variant="text" prepend-icon="mdi-arrow-left" class="mb-4"
           @click="router.push({ name: 'instructors' })">
      Back to Instructors
    </v-btn>

    <v-row v-if="loading">
      <v-col class="text-center"><v-progress-circular indeterminate /></v-col>
    </v-row>

    <template v-else-if="instructor">
      <!-- Header -->
      <v-row class="mb-2" align="center">
        <v-col>
          <h2 class="text-h5 font-weight-bold">{{ instructor.firstName }} {{ instructor.lastName }}</h2>
        </v-col>
        <v-col cols="auto" class="d-flex align-center ga-2">
          <v-chip :color="instructor.enabled ? 'success' : 'default'" variant="tonal">
            {{ instructor.enabled ? 'Active' : 'Inactive' }}
          </v-chip>
          <v-btn
            v-if="!instructor.enabled"
            color="success"
            prepend-icon="mdi-account-check-outline"
            variant="outlined"
            size="small"
            @click="openReactivateDialog"
          >
            Reactivate Instructor
          </v-btn>
          <v-btn
            v-if="instructor.enabled"
            color="error"
            prepend-icon="mdi-account-off-outline"
            variant="outlined"
            size="small"
            @click="openDeactivateDialog"
          >
            Deactivate Instructor
          </v-btn>
        </v-col>
      </v-row>

      <!-- Instructor Info -->
      <v-card variant="outlined" class="mb-4">
        <v-card-title class="text-subtitle-1 font-weight-bold pa-4 pb-2">Instructor Info</v-card-title>
        <v-card-text>
          <v-row>
            <v-col cols="12" sm="4">
              <div class="text-caption text-medium-emphasis">First Name</div>
              <div>{{ instructor.firstName }}</div>
            </v-col>
            <v-col cols="12" sm="4">
              <div class="text-caption text-medium-emphasis">Last Name</div>
              <div>{{ instructor.lastName }}</div>
            </v-col>
            <v-col cols="12" sm="4">
              <div class="text-caption text-medium-emphasis">Email</div>
              <div>{{ instructor.email }}</div>
            </v-col>
            <v-col cols="12" sm="4">
              <div class="text-caption text-medium-emphasis">Status</div>
              <div>
                <v-chip :color="instructor.enabled ? 'success' : 'default'" size="x-small" variant="tonal">
                  {{ instructor.enabled ? 'Active' : 'Inactive' }}
                </v-chip>
              </div>
            </v-col>
            <v-col cols="12" sm="4">
              <div class="text-caption text-medium-emphasis">Academic Year</div>
              <div>{{ instructor.academicYear ?? '—' }}</div>
            </v-col>
          </v-row>
        </v-card-text>
      </v-card>

      <!-- Supervised Teams -->
      <v-card variant="outlined">
        <v-card-title class="text-subtitle-1 font-weight-bold pa-4 pb-2">Supervised Teams</v-card-title>
        <v-card-text>
          <v-alert v-if="instructor.teamNames.length === 0" type="info" variant="tonal" density="compact">
            This instructor is not assigned to any team.
          </v-alert>
          <div v-else>
            <v-chip
              v-for="team in instructor.teamNames"
              :key="team"
              size="small"
              color="primary"
              variant="tonal"
              class="mr-1 mb-1"
            >{{ team }}</v-chip>
          </div>
        </v-card-text>
      </v-card>
    </template>

    <!-- Reactivate Confirmation Dialog -->
    <v-dialog v-model="reactivateDialog" max-width="500">
      <v-card>
        <v-card-title class="pa-4">Confirm Reactivation</v-card-title>
        <v-divider />
        <v-card-text class="pa-4">
          Are you sure you want to reactivate the instructor <strong>{{ instructor?.firstName }} {{ instructor?.lastName }}</strong>?
          <br><br>
          This will restore the instructor's access to the System. The instructor will be notified that her account has been reactivated.
        </v-card-text>
        <v-divider />
        <v-card-actions class="pa-4">
          <v-btn variant="text" @click="reactivateDialog = false">Cancel</v-btn>
          <v-spacer />
          <v-btn color="success" :loading="saving" @click="confirmReactivate">Confirm Reactivation</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Deactivate Confirmation Dialog -->
    <v-dialog v-model="deactivateDialog" max-width="500">
      <v-card>
        <v-card-title class="pa-4">Confirm Deactivation</v-card-title>
        <v-divider />
        <v-card-text class="pa-4">
          Are you sure you want to deactivate the instructor <strong>{{ instructor?.firstName }} {{ instructor?.lastName }}</strong>?
          <br><br>
          The instructor will no longer have access to the System.
        </v-card-text>
        <v-divider />
        <v-card-actions class="pa-4">
          <v-btn variant="text" @click="deactivateDialog = false">Cancel</v-btn>
          <v-spacer />
          <v-btn color="error" :loading="saving" @click="confirmDeactivate">Confirm Deactivation</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-snackbar v-model="snackbar.show" :color="snackbar.color" timeout="3000">
      {{ snackbar.message }}
    </v-snackbar>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getInstructor, reactivateInstructor, deactivateInstructor } from '../services/instructorService'
import type { InstructorSummary } from '../services/instructorService'

const route = useRoute()
const router = useRouter()

const instructor = ref<InstructorSummary | null>(null)
const loading = ref(false)
const saving = ref(false)

const reactivateDialog = ref(false)
const deactivateDialog = ref(false)
const snackbar = ref({ show: false, message: '', color: 'success' })

onMounted(fetchInstructor)

async function fetchInstructor() {
  loading.value = true
  try {
    const res = await getInstructor(Number(route.params.id)) as any
    if (res.flag) instructor.value = res.data
    else router.replace({ name: 'not-found' })
  } catch {
    router.replace({ name: 'not-found' })
  } finally {
    loading.value = false
  }
}

function openReactivateDialog() {
  reactivateDialog.value = true
}

async function confirmReactivate() {
  if (!instructor.value) return
  saving.value = true
  try {
    const res = await reactivateInstructor(instructor.value.instructorId) as any
    if (res.flag) {
      snackbar.value = { show: true, message: 'Instructor reactivated successfully.', color: 'success' }
      reactivateDialog.value = false
      await fetchInstructor()
    } else {
      snackbar.value = { show: true, message: res.message || 'Failed to reactivate instructor.', color: 'error' }
    }
  } catch (err: any) {
    snackbar.value = {
      show: true,
      message: err?.response?.data?.message || 'Failed to reactivate instructor.',
      color: 'error'
    }
  } finally {
    saving.value = false
  }
}

function openDeactivateDialog() {
  deactivateDialog.value = true
}

async function confirmDeactivate() {
  if (!instructor.value) return
  saving.value = true
  try {
    const res = await deactivateInstructor(instructor.value.instructorId) as any
    if (res.flag) {
      snackbar.value = { show: true, message: 'Instructor deactivated successfully.', color: 'success' }
      deactivateDialog.value = false
      await fetchInstructor()
    } else {
      snackbar.value = { show: true, message: res.message || 'Failed to deactivate instructor.', color: 'error' }
    }
  } catch (err: any) {
    snackbar.value = {
      show: true,
      message: err?.response?.data?.message || 'Failed to deactivate instructor.',
      color: 'error'
    }
  } finally {
    saving.value = false
  }
}
</script>
