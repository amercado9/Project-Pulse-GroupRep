<template>
  <v-container>
    <v-row class="mb-4" align="center">
      <v-col><h2 class="text-h5 font-weight-bold">Students</h2></v-col>
    </v-row>

    <v-row class="mb-4">
      <v-col cols="12" md="4">
        <v-text-field
          v-model="filters.firstName"
          label="First name"
          prepend-inner-icon="mdi-account"
          clearable
          hide-details
          @keyup.enter="search"
        />
      </v-col>
      <v-col cols="12" md="4">
        <v-text-field
          v-model="filters.lastName"
          label="Last name"
          prepend-inner-icon="mdi-account"
          clearable
          hide-details
          @keyup.enter="search"
        />
      </v-col>
      <v-col cols="12" md="4">
        <v-text-field
          v-model="filters.email"
          label="Email"
          prepend-inner-icon="mdi-email"
          clearable
          hide-details
          @keyup.enter="search"
        />
      </v-col>
      <v-col cols="12" md="4">
        <v-text-field
          v-model="filters.sectionName"
          label="Section name"
          prepend-inner-icon="mdi-school"
          clearable
          hide-details
          @keyup.enter="search"
        />
      </v-col>
      <v-col cols="12" md="4">
        <v-text-field
          v-model="filters.teamName"
          label="Team name"
          prepend-inner-icon="mdi-account-group"
          clearable
          hide-details
          @keyup.enter="search"
        />
      </v-col>
      <v-col cols="auto" class="d-flex align-center ga-2">
        <v-btn color="primary" @click="search">Search</v-btn>
        <v-btn variant="text" @click="clearFilters">Clear</v-btn>
      </v-col>
    </v-row>

    <v-row v-if="loading">
      <v-col class="text-center">
        <v-progress-circular indeterminate />
      </v-col>
    </v-row>

    <template v-else-if="searched">
      <v-row v-if="students.length === 0">
        <v-col>
          <v-alert type="info" variant="tonal" class="mb-4">
            No matching students found.
          </v-alert>
          <div v-if="isAdmin" class="d-flex align-center ga-3">
            <span class="text-body-2 text-medium-emphasis">Want to add students to a section?</span>
            <v-btn color="primary" prepend-icon="mdi-email-plus" @click="goToInvite">
              Invite Students
            </v-btn>
          </div>
        </v-col>
      </v-row>

      <v-row v-else>
        <v-col cols="12">
          <v-table>
            <thead>
              <tr>
                <th>First Name</th>
                <th>Last Name</th>
                <th>Email</th>
                <th>Section</th>
                <th>Team(s)</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="student in students" :key="student.id">
                <td>{{ student.firstName }}</td>
                <td>{{ student.lastName }}</td>
                <td>{{ student.email }}</td>
                <td>{{ student.sectionName ?? '—' }}</td>
                <td>
                  <div v-if="student.teamNames.length" class="chip-group">
                    <v-chip
                      v-for="name in student.teamNames"
                      :key="name"
                      size="small"
                      variant="tonal"
                      class="mr-1 mb-1"
                    >{{ name }}</v-chip>
                  </div>
                  <span v-else class="text-medium-emphasis">—</span>
                </td>
                <td>
                  <v-btn
                    size="small"
                    variant="text"
                    color="primary"
                    @click="viewStudent(student.id)"
                  >
                    View
                  </v-btn>
                </td>
              </tr>
            </tbody>
          </v-table>
        </v-col>
      </v-row>
    </template>

    <v-snackbar v-model="snackbar.show" :color="snackbar.color" timeout="3000">
      {{ snackbar.message }}
    </v-snackbar>
  </v-container>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserInfoStore } from '@/stores/userInfo'
import { findStudents } from '../services/studentService'
import type { FindStudentsParams, StudentSummary } from '../services/studentTypes'

const router = useRouter()
const userInfoStore = useUserInfoStore()

const students = ref<StudentSummary[]>([])
const loading = ref(false)
const searched = ref(false)
const filters = ref<FindStudentsParams>({
  firstName: '',
  lastName: '',
  email: '',
  sectionName: '',
  teamName: ''
})
const snackbar = ref({ show: false, message: '', color: 'success' })

const isAdmin = computed(() => userInfoStore.isAdmin)

async function search() {
  loading.value = true
  try {
    const res = await findStudents({
      firstName: filters.value.firstName?.trim() || undefined,
      lastName: filters.value.lastName?.trim() || undefined,
      email: filters.value.email?.trim() || undefined,
      sectionName: filters.value.sectionName?.trim() || undefined,
      teamName: filters.value.teamName?.trim() || undefined
    }) as any
    students.value = res.flag ? (res.data ?? []) : []
    searched.value = true
  } catch {
    students.value = []
    searched.value = true
    snackbar.value = { show: true, message: 'Failed to load students.', color: 'error' }
  } finally {
    loading.value = false
  }
}

function clearFilters() {
  filters.value = { firstName: '', lastName: '', email: '', sectionName: '', teamName: '' }
  students.value = []
  searched.value = false
}

function viewStudent(id: number) {
  router.push({ name: 'student-detail', params: { id } })
}

function goToInvite() {
  router.push({ name: 'sections' })
}
</script>

<style scoped lang="scss">
.chip-group {
  min-width: 140px;
}
</style>
