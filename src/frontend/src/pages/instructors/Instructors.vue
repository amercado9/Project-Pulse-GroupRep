<template>
  <v-card rounded="lg">
    <v-card-title class="d-flex align-center justify-space-between pa-4">
      <span>Instructor Management</span>
      <v-btn color="primary" prepend-icon="mdi-plus" @click="showInviteDialog">
        Invite Instructor
      </v-btn>
    </v-card-title>

    <!-- Search -->
    <v-card-text class="pt-0">
      <v-row dense class="mb-2">
        <v-col cols="12" sm="4">
          <v-text-field
            v-model="search.firstName"
            label="First Name"
            clearable
            density="compact"
            variant="outlined"
            hide-details
          />
        </v-col>
        <v-col cols="12" sm="4">
          <v-text-field
            v-model="search.lastName"
            label="Last Name"
            clearable
            density="compact"
            variant="outlined"
            hide-details
          />
        </v-col>
        <v-col cols="12" sm="auto">
          <v-btn variant="outlined" @click="resetSearch">Reset</v-btn>
        </v-col>
      </v-row>

      <!-- Table -->
      <v-data-table
        :headers="headers"
        :items="filtered"
        :loading="loading"
        item-value="id"
        density="compact"
        class="elevation-0 border rounded"
        fixed-header
        height="520"
      >
        <template #item.roles="{ item }">
          <v-chip
            v-for="role in parseRoles(item.roles)"
            :key="role"
            :color="role === 'admin' ? 'error' : 'primary'"
            size="x-small"
            class="mr-1"
            variant="flat"
          >{{ role }}</v-chip>
        </template>

        <template #item.enabled="{ item }">
          <v-chip
            :color="item.enabled ? 'success' : 'error'"
            size="x-small"
            variant="flat"
          >{{ item.enabled ? 'Active' : 'Inactive' }}</v-chip>
        </template>

        <template #item.actions="{ item }">
          <v-tooltip text="View" location="top">
            <template #activator="{ props }">
              <v-btn
                v-bind="props"
                icon="mdi-eye"
                variant="text"
                size="small"
                color="info"
                @click="viewInstructor(item)"
              />
            </template>
          </v-tooltip>
          <v-tooltip :text="item.enabled ? 'Deactivate' : 'Reactivate'" location="top">
            <template #activator="{ props }">
              <v-btn
                v-bind="props"
                :icon="item.enabled ? 'mdi-account-remove' : 'mdi-account-check'"
                variant="text"
                size="small"
                :color="item.enabled ? 'warning' : 'success'"
                @click="toggleStatus(item)"
              />
            </template>
          </v-tooltip>
        </template>

        <template #no-data>
          <div class="text-center py-8 text-medium-emphasis">No instructors found.</div>
        </template>
      </v-data-table>
    </v-card-text>

    <!-- Invite Dialog -->
    <v-dialog v-model="inviteDialogVisible" max-width="480" persistent>
      <v-card title="Invite Instructor">
        <v-card-text>
          <InviteUsersForm @close-dialog="closeInvite" />
        </v-card-text>
      </v-card>
    </v-dialog>

    <!-- View Dialog -->
    <v-dialog v-model="viewDialogVisible" max-width="420">
      <v-card v-if="selected" title="Instructor Details">
        <v-card-text>
          <v-table density="compact">
            <tbody>
              <tr><td class="font-weight-medium">ID</td><td>{{ selected.id }}</td></tr>
              <tr><td class="font-weight-medium">First Name</td><td>{{ selected.firstName }}</td></tr>
              <tr><td class="font-weight-medium">Last Name</td><td>{{ selected.lastName }}</td></tr>
              <tr><td class="font-weight-medium">Email</td><td>{{ selected.email }}</td></tr>
              <tr>
                <td class="font-weight-medium">Roles</td>
                <td>
                  <v-chip
                    v-for="role in parseRoles(selected.roles)"
                    :key="role"
                    :color="role === 'admin' ? 'error' : 'primary'"
                    size="x-small"
                    class="mr-1"
                    variant="flat"
                  >{{ role }}</v-chip>
                </td>
              </tr>
              <tr>
                <td class="font-weight-medium">Status</td>
                <td>
                  <v-chip
                    :color="selected.enabled ? 'success' : 'error'"
                    size="x-small"
                    variant="flat"
                  >{{ selected.enabled ? 'Active' : 'Inactive' }}</v-chip>
                </td>
              </tr>
            </tbody>
          </v-table>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="viewDialogVisible = false">Close</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Toggle Status Confirm Dialog -->
    <v-dialog v-model="confirmDialogVisible" max-width="360" persistent>
      <v-card :title="`Confirm ${confirmAction}`">
        <v-card-text>
          {{ confirmAction }} {{ confirmTarget?.firstName }} {{ confirmTarget?.lastName }}?
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="confirmDialogVisible = false">Cancel</v-btn>
          <v-btn color="warning" variant="flat" @click="executeToggle">{{ confirmAction }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-card>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { fetchInstructors } from '@/apis/instructor'
import { useNotifyStore } from '@/stores/notify'
import type { Instructor } from '@/apis/instructor/types'
import InviteUsersForm from '@/pages/sections/InviteUsersForm.vue'

const notifyStore = useNotifyStore()
const loading = ref(false)
const instructors = ref<Instructor[]>([])
const inviteDialogVisible = ref(false)
const viewDialogVisible = ref(false)
const confirmDialogVisible = ref(false)
const selected = ref<Instructor | null>(null)
const confirmTarget = ref<Instructor | null>(null)
const confirmAction = ref('')
const search = ref({ firstName: '', lastName: '' })

const headers = [
  { title: 'ID', key: 'id', width: 70 },
  { title: 'First Name', key: 'firstName' },
  { title: 'Last Name', key: 'lastName' },
  { title: 'Email', key: 'email' },
  { title: 'Roles', key: 'roles', sortable: false },
  { title: 'Status', key: 'enabled', width: 100, align: 'center' as const },
  { title: 'Actions', key: 'actions', width: 120, sortable: false, align: 'center' as const }
]

const filtered = computed(() => {
  const fn = search.value.firstName.toLowerCase()
  const ln = search.value.lastName.toLowerCase()
  return instructors.value.filter(
    (i) =>
      (!fn || i.firstName.toLowerCase().includes(fn)) &&
      (!ln || i.lastName.toLowerCase().includes(ln))
  )
})

async function loadInstructors() {
  loading.value = true
  try {
    const res = await fetchInstructors()
    instructors.value = res.data ?? []
  } catch {
    notifyStore.error('Failed to load instructors.')
  } finally {
    loading.value = false
  }
}

function resetSearch() {
  search.value = { firstName: '', lastName: '' }
}

function parseRoles(roles?: string) {
  return roles?.split(/\s+/).filter(Boolean) ?? []
}

function showInviteDialog() {
  inviteDialogVisible.value = true
}

function closeInvite() {
  inviteDialogVisible.value = false
  loadInstructors()
}

function viewInstructor(row: Instructor) {
  selected.value = row
  viewDialogVisible.value = true
}

function toggleStatus(row: Instructor) {
  confirmTarget.value = row
  confirmAction.value = row.enabled ? 'Deactivate' : 'Reactivate'
  confirmDialogVisible.value = true
}

function executeToggle() {
  confirmDialogVisible.value = false
  // UC-23 (deactivate) / UC-24 (reactivate): API call goes here
  notifyStore.info(`${confirmAction.value} not yet implemented.`)
}

onMounted(loadInstructors)
</script>
