<template>
  <v-container>
    <v-row class="mb-4" align="center">
      <v-col>
        <h2 class="text-h5 font-weight-bold">Students</h2>
      </v-col>
    </v-row>

    <!-- Deletion notification banner -->
    <v-row v-if="deletedStudentNotification" class="mb-4">
      <v-col cols="12">
        <v-card variant="outlined">
          <v-card-title class="pa-4 pb-2 d-flex align-center">
            <span>Student Deleted</span>
            <v-spacer />
            <v-btn variant="text" size="small" @click="studentNotificationsStore.clearDeletedStudentNotification()">
              Dismiss
            </v-btn>
          </v-card-title>
          <v-card-text class="pt-0">
            <div class="text-body-2 mb-3">
              The following student has been permanently deleted from the system.
            </div>
            <v-table density="compact" class="mb-2">
              <tbody>
                <tr>
                  <th class="text-left" style="width: 140px">Name</th>
                  <td>{{ deletedStudentNotification.fullName }}</td>
                </tr>
                <tr>
                  <th class="text-left">Email</th>
                  <td>{{ deletedStudentNotification.email }}</td>
                </tr>
                <tr>
                  <th class="text-left">Section</th>
                  <td>{{ deletedStudentNotification.sectionName ?? '—' }}</td>
                </tr>
                <tr>
                  <th class="text-left">Teams</th>
                  <td>{{ deletedStudentNotification.teamNames.length ? deletedStudentNotification.teamNames.join(', ') : '—' }}</td>
                </tr>
              </tbody>
            </v-table>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- Loading -->
    <v-row v-if="loading">
      <v-col class="text-center"><v-progress-circular indeterminate /></v-col>
    </v-row>

    <!-- Student table -->
    <template v-else>
      <v-card variant="outlined">
        <v-card-text class="pa-0">
          <v-table>
            <thead>
              <tr>
                <th class="text-left">Name</th>
                <th class="text-left">Email</th>
                <th class="text-left">Section</th>
                <th class="text-left">Status</th>
                <th class="text-left"></th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="students.length === 0">
                <td colspan="5" class="text-center text-medium-emphasis py-6">
                  No students found in the system.
                </td>
              </tr>
              <tr v-for="student in students" :key="student.studentId">
                <td class="font-weight-medium">{{ student.firstName }} {{ student.lastName }}</td>
                <td>{{ student.email }}</td>
                <td>{{ student.sectionName ?? '—' }}</td>
                <td>
                  <v-chip
                    :color="student.enabled ? 'success' : 'default'"
                    size="x-small"
                    variant="tonal"
                  >
                    {{ student.enabled ? 'Active' : 'Inactive' }}
                  </v-chip>
                </td>
                <td>
                  <v-btn
                    size="small"
                    variant="text"
                    @click="router.push({ name: 'student-detail', params: { id: student.studentId } })"
                  >
                    View
                  </v-btn>
                </td>
              </tr>
            </tbody>
          </v-table>
        </v-card-text>
      </v-card>
    </template>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { findAllStudents } from '../services/studentService'
import { useStudentNotificationsStore } from '../stores/studentNotifications'
import type { StudentSummary } from '../services/studentTypes'

const router = useRouter()
const studentNotificationsStore = useStudentNotificationsStore()

const students = ref<StudentSummary[]>([])
const loading = ref(false)

const deletedStudentNotification = computed(() => studentNotificationsStore.deletedStudentNotification)

onMounted(async () => {
  loading.value = true
  try {
    const res = await findAllStudents() as any
    if (res.flag) students.value = res.data ?? []
  } catch { /* handled by request interceptor */ } finally {
    loading.value = false
  }
})
</script>
