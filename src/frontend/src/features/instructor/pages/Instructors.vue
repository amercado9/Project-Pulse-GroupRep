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
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="instructor in instructors" :key="instructor.instructorId">
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
              <td>
                <v-tooltip text="Deactivate" location="top" v-if="instructor.enabled">
                  <template #activator="{ props }">
                    <v-btn
                      v-bind="props"
                      icon="mdi-account-remove"
                      variant="text"
                      size="small"
                      color="warning"
                      @click="showDeactivateDialog(instructor)"
                    />
                  </template>
                </v-tooltip>
              </td>
            </tr>
          </tbody>
        </v-table>
      </v-col>
    </v-row>

    <!-- Deactivate Dialog -->
    <v-dialog v-model="deactivateDialog" max-width="480" persistent>
      <v-card title="Deactivate Instructor">
        <v-card-text>
          <v-alert
            type="warning"
            variant="tonal"
            class="mb-4"
            title="Important Notice"
          >
            The instructor will no longer have access to the System. But the instructor’s information is kept in the System.
            Deactivation will NOT remove the instructor from the System and the instructor’s account can be recovered in the future.
          </v-alert>
          <p class="mb-4">Are you sure you want to deactivate <strong>{{ selectedInstructor?.firstName }} {{ selectedInstructor?.lastName }}</strong>?</p>
          <v-textarea
            v-model="deactivationReason"
            label="Reason for deactivation"
            variant="outlined"
            rows="3"
            required
          ></v-textarea>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="deactivateDialog = false">Cancel</v-btn>
          <v-btn color="warning" variant="flat" :loading="deactivating" @click="confirmDeactivate">Deactivate</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { findInstructors, deactivateInstructor } from '../services/instructorService'
import type { InstructorSummary } from '../services/instructorService'
import { useNotifyStore } from '@/stores/notify'

const router = useRouter()
const notifyStore = useNotifyStore()

const instructors = ref<InstructorSummary[]>([])
const loading = ref(false)
const searched = ref(false)

const deactivateDialog = ref(false)
const deactivating = ref(false)
const deactivationReason = ref('')
const selectedInstructor = ref<InstructorSummary | null>(null)

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

function showDeactivateDialog(instructor: InstructorSummary) {
  selectedInstructor.value = instructor
  deactivationReason.value = ''
  deactivateDialog.value = true
}

async function confirmDeactivate() {
  if (!selectedInstructor.value) return
  if (!deactivationReason.value.trim()) {
    notifyStore.warning('Please enter a reason for deactivation.')
    return
  }

  deactivating.value = true
  try {
    const res = await deactivateInstructor(selectedInstructor.value.instructorId, deactivationReason.value)
    if (res.flag) {
      notifyStore.success('Instructor deactivated successfully.')
      deactivateDialog.value = false
      search()
    } else {
      notifyStore.error(res.message || 'Failed to deactivate instructor.')
    }
  } catch (err) {
    notifyStore.error('An error occurred while deactivating the instructor.')
  } finally {
    deactivating.value = false
  }
}
</script>

<style scoped lang="scss">
.chip-group {
  min-width: 160px;
}
</style>
