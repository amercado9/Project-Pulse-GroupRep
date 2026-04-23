<template>
  <v-container>
    <v-row class="mb-4" align="center">
      <v-col><h2 class="text-h5 font-weight-bold">Students</h2></v-col>
    </v-row>

    <!-- Search filters -->
    <v-row class="mb-2">
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
          prepend-inner-icon="mdi-email-outline"
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

    <!-- Loading -->
    <v-row v-if="loading">
      <v-col class="text-center"><v-progress-circular indeterminate /></v-col>
    </v-row>

    <!-- Empty state -->
    <v-row v-else-if="students.length === 0">
      <v-col>
        <v-alert type="info" variant="tonal">No matching students found.</v-alert>
      </v-col>
    </v-row>

    <!-- Results table -->
    <v-row v-else>
      <v-col cols="12">
        <v-table>
          <thead>
            <tr>
              <th>First Name</th>
              <th>Last Name</th>
              <th>Team(s)</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="student in students" :key="student.id">
              <td>{{ student.firstName }}</td>
              <td>{{ student.lastName }}</td>
              <td>
                <span v-if="student.teamNames.length === 0" class="text-medium-emphasis">—</span>
                <v-chip
                  v-for="name in student.teamNames"
                  :key="name"
                  size="small"
                  variant="tonal"
                  class="mr-1 mb-1"
                >{{ name }}</v-chip>
              </td>
              <td>
                <v-btn
                  size="small"
                  variant="text"
                  color="primary"
                  @click="router.push({ name: 'student-detail', params: { id: student.id } })"
                >
                  View
                </v-btn>
              </td>
            </tr>
          </tbody>
        </v-table>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { findStudents } from '../services/studentService'
import type { FindStudentsParams, StudentSummary } from '../services/studentTypes'

const router = useRouter()
const students = ref<StudentSummary[]>([])
const loading = ref(false)
const filters = ref<FindStudentsParams>({
  firstName: '',
  lastName: '',
  email: '',
  sectionName: '',
  teamName: ''
})

onMounted(() => search())

async function search() {
  loading.value = true
  try {
    const params: FindStudentsParams = {}
    if (filters.value.firstName?.trim())   params.firstName   = filters.value.firstName.trim()
    if (filters.value.lastName?.trim())    params.lastName    = filters.value.lastName.trim()
    if (filters.value.email?.trim())       params.email       = filters.value.email.trim()
    if (filters.value.sectionName?.trim()) params.sectionName = filters.value.sectionName.trim()
    if (filters.value.teamName?.trim())    params.teamName    = filters.value.teamName.trim()

    const res = await findStudents(params) as any
    students.value = res.flag ? (res.data ?? []) : []
  } catch {
    students.value = []
  } finally {
    loading.value = false
  }
}

function clearFilters() {
  filters.value = { firstName: '', lastName: '', email: '', sectionName: '', teamName: '' }
  search()
}
</script>
