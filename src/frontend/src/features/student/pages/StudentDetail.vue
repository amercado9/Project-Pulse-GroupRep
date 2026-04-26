<template>
  <v-container>
    <v-btn variant="text" prepend-icon="mdi-arrow-left" class="mb-4"
           @click="router.push({ name: 'students' })">
      Back to Students
    </v-btn>

    <v-row v-if="loading">
      <v-col class="text-center"><v-progress-circular indeterminate /></v-col>
    </v-row>

    <template v-else-if="student">
      <!-- Header -->
      <v-row class="mb-2" align="center">
        <v-col>
          <h2 class="text-h5 font-weight-bold">{{ student.firstName }} {{ student.lastName }}</h2>
        </v-col>
        <v-col cols="auto" class="d-flex align-center ga-2">
          <v-chip :color="student.enabled ? 'success' : 'default'" variant="tonal">
            {{ student.enabled ? 'Active' : 'Inactive' }}
          </v-chip>
          <v-btn color="error" prepend-icon="mdi-delete-outline" variant="outlined" size="small"
                 @click="openDeleteDialog">
            Delete Student
          </v-btn>
        </v-col>
      </v-row>

      <!-- Student Info -->
      <v-card variant="outlined" class="mb-4">
        <v-card-title class="text-subtitle-1 font-weight-bold pa-4 pb-2">Student Info</v-card-title>
        <v-card-text>
          <v-row>
            <v-col cols="12" sm="4">
              <div class="text-caption text-medium-emphasis">First Name</div>
              <div>{{ student.firstName }}</div>
            </v-col>
            <v-col cols="12" sm="4">
              <div class="text-caption text-medium-emphasis">Last Name</div>
              <div>{{ student.lastName }}</div>
            </v-col>
            <v-col cols="12" sm="4">
              <div class="text-caption text-medium-emphasis">Email</div>
              <div>{{ student.email }}</div>
            </v-col>
            <v-col cols="12" sm="4">
              <div class="text-caption text-medium-emphasis">Section</div>
              <div>{{ student.sectionName ?? '—' }}</div>
            </v-col>
            <v-col cols="12" sm="4">
              <div class="text-caption text-medium-emphasis">Status</div>
              <div>
                <v-chip :color="student.enabled ? 'success' : 'default'" size="x-small" variant="tonal">
                  {{ student.enabled ? 'Active' : 'Inactive' }}
                </v-chip>
              </div>
            </v-col>
          </v-row>
        </v-card-text>
      </v-card>

      <!-- Teams -->
      <v-card variant="outlined">
        <v-card-title class="text-subtitle-1 font-weight-bold pa-4 pb-2">Teams</v-card-title>
        <v-card-text>
          <v-alert v-if="student.teamNames.length === 0" type="info" variant="tonal" density="compact">
            This student is not assigned to any team.
          </v-alert>
          <div v-else>
            <v-chip
              v-for="team in student.teamNames"
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

    <!-- Delete Confirmation Dialog -->
    <v-dialog v-model="deleteDialog" max-width="640" persistent>
      <v-card>
        <v-card-title class="pa-4">
          {{ deleteStep === 1 ? 'Delete Student' : 'Confirm Student Deletion' }}
        </v-card-title>
        <v-divider />

        <!-- Step 1: Consequences warning -->
        <v-card-text v-if="deleteStep === 1" class="pa-4">
          <v-alert type="warning" variant="tonal" class="mb-4">
            Deleting this student is permanent and cannot be undone.
          </v-alert>

          <div class="text-body-2 mb-4">
            If you continue, the system will permanently delete this student and all associated data.
            Specifically, deleting this student will automatically delete:
          </div>

          <v-list density="compact" class="mb-4">
            <v-list-item prepend-icon="mdi-clipboard-text-clock-outline" title="All WAR (Weekly Activity Report) submissions by this student" />
            <v-list-item prepend-icon="mdi-account-star-outline" title="All peer evaluation submissions by this student" />
            <v-list-item prepend-icon="mdi-account-star-outline" title="All peer evaluation entries received by this student" />
            <v-list-item prepend-icon="mdi-account-group" title="All team memberships for this student" />
          </v-list>

          <v-table density="compact" class="mb-4">
            <tbody>
              <tr>
                <th class="text-left" style="width: 160px">Student</th>
                <td>{{ student?.firstName }} {{ student?.lastName }}</td>
              </tr>
              <tr>
                <th class="text-left">Email</th>
                <td>{{ student?.email }}</td>
              </tr>
              <tr>
                <th class="text-left">Section</th>
                <td>{{ student?.sectionName ?? '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Deletion Strategy</th>
                <td>Physical delete (permanent)</td>
              </tr>
            </tbody>
          </v-table>
        </v-card-text>

        <!-- Step 2: Confirm -->
        <v-card-text v-else class="pa-4">
          <v-alert type="warning" variant="tonal" class="mb-4">
            Please review the details before confirming the deletion.
          </v-alert>

          <v-table density="compact" class="mb-4">
            <tbody>
              <tr>
                <th class="text-left" style="width: 160px">Student</th>
                <td>{{ student?.firstName }} {{ student?.lastName }}</td>
              </tr>
              <tr>
                <th class="text-left">Email</th>
                <td>{{ student?.email }}</td>
              </tr>
              <tr>
                <th class="text-left">Section</th>
                <td>{{ student?.sectionName ?? '—' }}</td>
              </tr>
            </tbody>
          </v-table>

          <div class="text-subtitle-2 font-weight-bold mb-2">Teams to be removed from</div>
          <v-alert
            v-if="!student?.teamNames.length"
            type="info"
            variant="tonal"
            density="compact"
            class="mb-4"
          >
            This student is not assigned to any team.
          </v-alert>
          <div v-else class="mb-4">
            <v-chip
              v-for="team in student.teamNames"
              :key="team"
              size="small"
              class="mr-1 mb-1"
            >{{ team }}</v-chip>
          </div>

          <v-alert type="error" variant="tonal">
            This action cannot be undone. All WARs and peer evaluations for this student will also be permanently deleted.
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
            @click="deleteStep === 1 ? deleteStep = 2 : confirmDelete()"
          >
            {{ deleteStep === 1 ? 'Preview' : 'Confirm Delete' }}
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
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getStudent, deleteStudent } from '../services/studentService'
import { useStudentNotificationsStore } from '../stores/studentNotifications'
import type { StudentDetail } from '../services/studentTypes'

const route = useRoute()
const router = useRouter()
const studentNotificationsStore = useStudentNotificationsStore()

const student = ref<StudentDetail | null>(null)
const loading = ref(false)

const deleteDialog = ref(false)
const deleteStep = ref(1)
const deleteSaving = ref(false)

const snackbar = ref({ show: false, message: '', color: 'success' })

onMounted(async () => {
  loading.value = true
  try {
    const res = await getStudent(Number(route.params.id)) as any
    if (res.flag) student.value = res.data
    else router.replace({ name: 'not-found' })
  } catch {
    router.replace({ name: 'not-found' })
  } finally {
    loading.value = false
  }
})

function openDeleteDialog() {
  deleteStep.value = 1
  deleteDialog.value = true
}

function cancelDelete() {
  deleteDialog.value = false
  deleteStep.value = 1
}

async function confirmDelete() {
  if (!student.value) return
  deleteSaving.value = true
  try {
    const res = await deleteStudent(student.value.studentId) as any
    if (res.flag) {
      studentNotificationsStore.setDeletedStudentNotification({
        studentId: student.value.studentId,
        fullName: `${student.value.firstName} ${student.value.lastName}`,
        email: student.value.email,
        sectionName: student.value.sectionName,
        teamNames: student.value.teamNames
      })
      await router.push({ name: 'students' })
    } else {
      snackbar.value = { show: true, message: res.message || 'Failed to delete student.', color: 'error' }
    }
  } catch (err: any) {
    snackbar.value = {
      show: true,
      message: err?.response?.data?.message || 'Failed to delete student.',
      color: 'error'
    }
  } finally {
    deleteSaving.value = false
  }
}
</script>
