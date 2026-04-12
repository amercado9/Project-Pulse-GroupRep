<template>
  <v-card rounded="lg">
    <!-- Header -->
    <v-card-title class="d-flex align-center justify-space-between pa-4">
      <span class="text-h6">Section Management</span>
      <v-btn v-if="userInfoStore.isAdmin" color="primary" prepend-icon="mdi-plus" @click="openCreateDialog">
        Add Section
      </v-btn>
    </v-card-title>

    <!-- Search bar (UC-2) -->
    <v-card-text class="pb-0">
      <v-row dense align="center">
        <v-col cols="12" sm="5">
          <v-text-field
            v-model="searchName"
            label="Search by section name"
            prepend-inner-icon="mdi-magnify"
            variant="outlined"
            density="compact"
            clearable
            hide-details
            @keydown.enter="runSearch"
            @click:clear="clearSearch"
          />
        </v-col>
        <v-col cols="auto">
          <v-btn color="primary" variant="flat" @click="runSearch">Search</v-btn>
        </v-col>
        <v-col cols="auto">
          <v-btn variant="text" @click="clearSearch">Reset</v-btn>
        </v-col>
      </v-row>
    </v-card-text>

    <!-- Data table -->
    <v-card-text>
      <v-data-table-server
        v-model:items-per-page="pagination.size"
        :headers="headers"
        :items="sections"
        :items-length="totalElements"
        :loading="loading"
        :page="pagination.page + 1"
        item-value="sectionId"
        @update:page="onPageChange"
        @update:items-per-page="onSizeChange"
      >
        <!-- Status chip -->
        <template #item.isActive="{ item }">
          <v-chip :color="item.isActive ? 'success' : 'default'" size="small" variant="tonal">
            {{ item.isActive ? 'Active' : 'Inactive' }}
          </v-chip>
        </template>

        <!-- Date columns -->
        <template #item.startDate="{ item }">{{ item.startDate ?? '—' }}</template>
        <template #item.endDate="{ item }">{{ item.endDate ?? '—' }}</template>

        <!-- Actions -->
        <template #item.actions="{ item }">
          <v-btn icon="mdi-eye" size="small" variant="text" color="primary" @click="viewSection(item)" />
          <v-btn
            v-if="userInfoStore.isAdmin"
            icon="mdi-pencil"
            size="small"
            variant="text"
            color="secondary"
            @click="editSection(item)"
          />
        </template>

        <!-- Empty state -->
        <template #no-data>
          <div class="text-center py-8 text-medium-emphasis">
            <v-icon icon="mdi-school-outline" size="48" class="mb-3" />
            <p>No sections found.</p>
          </div>
        </template>
      </v-data-table-server>
    </v-card-text>

    <!-- View dialog -->
    <v-dialog v-model="viewDialog" max-width="560">
      <v-card v-if="selectedSection" rounded="lg">
        <v-card-title class="pa-4 d-flex align-center justify-space-between">
          <span>Section Details</span>
          <v-btn icon="mdi-close" variant="text" @click="viewDialog = false" />
        </v-card-title>
        <v-divider />
        <v-card-text class="pt-4">
          <v-row dense>
            <v-col cols="6">
              <div class="text-caption text-medium-emphasis">Section Name</div>
              <div class="text-body-1 font-weight-medium">{{ selectedSection.sectionName }}</div>
            </v-col>
            <v-col cols="6">
              <div class="text-caption text-medium-emphasis">Status</div>
              <v-chip :color="selectedSection.isActive ? 'success' : 'default'" size="small" variant="tonal" class="mt-1">
                {{ selectedSection.isActive ? 'Active' : 'Inactive' }}
              </v-chip>
            </v-col>
            <v-col cols="6" class="mt-2">
              <div class="text-caption text-medium-emphasis">Start Date</div>
              <div>{{ selectedSection.startDate ?? '—' }}</div>
            </v-col>
            <v-col cols="6" class="mt-2">
              <div class="text-caption text-medium-emphasis">End Date</div>
              <div>{{ selectedSection.endDate ?? '—' }}</div>
            </v-col>
            <v-col cols="6" class="mt-2">
              <div class="text-caption text-medium-emphasis">WAR Due Day</div>
              <div>{{ selectedSection.warWeeklyDueDay }} @ {{ selectedSection.warDueTime }}</div>
            </v-col>
            <v-col cols="6" class="mt-2">
              <div class="text-caption text-medium-emphasis">Peer Eval Due Day</div>
              <div>{{ selectedSection.peerEvaluationWeeklyDueDay }} @ {{ selectedSection.peerEvaluationDueTime }}</div>
            </v-col>
          </v-row>
        </v-card-text>
        <v-card-actions class="pa-4 pt-0">
          <v-spacer />
          <v-btn color="primary" variant="tonal" @click="viewDialog = false">Close</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { searchSections } from '@/apis/section'
import type { Section } from '@/apis/section/types'
import { useUserInfoStore } from '@/stores/userInfo'
import { useNotifyStore } from '@/stores/notify'

const userInfoStore = useUserInfoStore()
const notifyStore = useNotifyStore()

const searchName = ref('')
const loading = ref(false)
const sections = ref<Section[]>([])
const totalElements = ref(0)
const pagination = ref({ page: 0, size: 10 })

const viewDialog = ref(false)
const selectedSection = ref<Section | null>(null)

const headers = [
  { title: 'Section Name', key: 'sectionName', sortable: false },
  { title: 'Start Date', key: 'startDate', sortable: false },
  { title: 'End Date', key: 'endDate', sortable: false },
  { title: 'Status', key: 'isActive', sortable: false },
  { title: 'Actions', key: 'actions', sortable: false, align: 'end' as const },
]

async function loadSections() {
  loading.value = true
  try {
    const res = await searchSections(
      { page: pagination.value.page, size: pagination.value.size },
      { sectionName: searchName.value || undefined }
    )
    sections.value = res.data.content
    totalElements.value = res.data.totalElements
  } catch {
    // handled by request interceptor
  } finally {
    loading.value = false
  }
}

function runSearch() {
  pagination.value.page = 0
  loadSections()
}

function clearSearch() {
  searchName.value = ''
  pagination.value.page = 0
  loadSections()
}

function onPageChange(newPage: number) {
  pagination.value.page = newPage - 1
  loadSections()
}

function onSizeChange(newSize: number) {
  pagination.value.size = newSize
  pagination.value.page = 0
  loadSections()
}

function viewSection(section: Section) {
  selectedSection.value = section
  viewDialog.value = true
}

function editSection(section: Section) {
  notifyStore.info(`Edit section "${section.sectionName}" — coming soon.`)
}

function openCreateDialog() {
  notifyStore.info('Create section — coming soon.')
}

onMounted(loadSections)
</script>
