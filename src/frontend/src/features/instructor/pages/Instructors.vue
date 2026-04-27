<template>
  <v-container>
    <v-row class="mb-4" align="center">
      <v-col><h2 class="text-h5 font-weight-bold">Instructors</h2></v-col>
      <v-col cols="auto">
        <v-btn
          color="primary"
          prepend-icon="mdi-email-plus-outline"
          @click="router.push({ name: 'instructor-invite' })"
        >
          Invite Instructors
        </v-btn>
      </v-col>
    </v-row>

    <!-- Search form -->
    <v-row class="mb-4">
      <v-col cols="12" md="3">
        <v-text-field
          v-model="filters.firstName"
          label="Search by first name"
          prepend-inner-icon="mdi-account"
          clearable
          hide-details
          @keyup.enter="search"
        />
      </v-col>
      <v-col cols="12" md="3">
        <v-text-field
          v-model="filters.lastName"
          label="Search by last name"
          prepend-inner-icon="mdi-account"
          clearable
          hide-details
          @keyup.enter="search"
        />
      </v-col>
      <v-col cols="12" md="3">
        <v-text-field
          v-model="filters.teamName"
          label="Search by team name"
          prepend-inner-icon="mdi-account-group"
          clearable
          hide-details
          @keyup.enter="search"
        />
      </v-col>
      <v-col cols="12" md="2">
        <v-select
          v-model="filters.enabledStr"
          :items="statusOptions"
          label="Status"
          clearable
          hide-details
        />
      </v-col>
      <v-col cols="auto" class="d-flex align-center gap-2">
        <v-btn color="primary" @click="search">Search</v-btn>
        <v-btn variant="text" @click="clearFilters">Clear</v-btn>
      </v-col>
    </v-row>

    <!-- Loading -->
    <v-row v-if="loading">
      <v-col class="text-center"><v-progress-circular indeterminate /></v-col>
    </v-row>

    <!-- Empty state (ext 4a) -->
    <v-row v-else-if="searched && instructors.length === 0">
      <v-col>
        <v-alert type="info" variant="tonal" class="mb-4">
          No matching instructors found.
        </v-alert>
        <v-btn
          color="primary"
          prepend-icon="mdi-email-plus-outline"
          class="mr-2"
          @click="router.push({ name: 'instructor-invite' })"
        >
          Invite Instructors
        </v-btn>
        <v-btn variant="text" @click="clearFilters">Modify Search</v-btn>
      </v-col>
    </v-row>

    <!-- Results table -->
    <v-row v-else-if="instructors.length > 0">
      <v-col cols="12">
        <v-table>
          <thead>
            <tr>
              <th>First Name</th>
              <th>Last Name</th>
              <th>Team Name(s)</th>
              <th>Status</th>
              <th>Academic Year</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="instructor in instructors"
              :key="instructor.instructorId"
              class="clickable-row"
              @click="router.push({ name: 'instructor-detail', params: { id: instructor.instructorId } })"
            >
              <td>{{ instructor.firstName }}</td>
              <td>{{ instructor.lastName }}</td>
              <td>
                <div v-if="instructor.teamNames.length" class="chip-group">
                  <v-chip
                    v-for="name in instructor.teamNames"
                    :key="name"
                    size="small"
                    variant="tonal"
                    class="mr-1 mb-1"
                  >{{ name }}</v-chip>
                </div>
                <span v-else class="text-medium-emphasis">—</span>
              </td>
              <td>
                <v-chip
                  :color="instructor.enabled ? 'success' : 'default'"
                  size="x-small"
                  variant="tonal"
                >
                  {{ instructor.enabled ? 'Active' : 'Inactive' }}
                </v-chip>
              </td>
              <td>{{ instructor.academicYear ?? '—' }}</td>
            </tr>
          </tbody>
        </v-table>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { findInstructors } from '../services/instructorService'
import type { InstructorSummary } from '../services/instructorService'

const router = useRouter()

const instructors = ref<InstructorSummary[]>([])
const loading = ref(false)
const searched = ref(false)

const statusOptions = [
  { title: 'Active', value: 'true' },
  { title: 'Inactive', value: 'false' }
]

const filters = ref({
  firstName: '',
  lastName: '',
  teamName: '',
  enabledStr: null as string | null
})

onMounted(() => search())

async function search() {
  loading.value = true
  searched.value = true
  try {
    const params: Record<string, string | boolean | undefined> = {}
    if (filters.value.firstName?.trim()) params.firstName = filters.value.firstName.trim()
    if (filters.value.lastName?.trim()) params.lastName = filters.value.lastName.trim()
    if (filters.value.teamName?.trim()) params.teamName = filters.value.teamName.trim()
    if (filters.value.enabledStr !== null && filters.value.enabledStr !== undefined) {
      params.enabled = filters.value.enabledStr === 'true'
    }

    const res = await findInstructors(params) as any
    instructors.value = res.flag ? (res.data ?? []) : []
  } catch {
    instructors.value = []
  } finally {
    loading.value = false
  }
}

function clearFilters() {
  filters.value = { firstName: '', lastName: '', teamName: '', enabledStr: null }
  search()
}
</script>

<style scoped lang="scss">
.clickable-row {
  cursor: pointer;
  &:hover {
    background-color: rgba(var(--v-theme-primary), 0.05);
  }
}
.chip-group {
  min-width: 160px;
}
</style>
