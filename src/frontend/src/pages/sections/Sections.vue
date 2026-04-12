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
        <template #item.isActive="{ item }">
          <v-chip :color="item.isActive ? 'success' : 'default'" size="small" variant="tonal">
            {{ item.isActive ? 'Active' : 'Inactive' }}
          </v-chip>
        </template>

        <template #item.startDate="{ item }">{{ item.startDate ?? '—' }}</template>
        <template #item.endDate="{ item }">{{ item.endDate ?? '—' }}</template>

        <template #item.actions="{ item }">
          <v-btn
            icon="mdi-eye"
            size="small"
            variant="text"
            color="primary"
            @click="viewSection(item)"
          />
          <v-btn
            v-if="userInfoStore.isAdmin"
            icon="mdi-pencil"
            size="small"
            variant="text"
            color="secondary"
            @click="editSection(item)"
          />
        </template>

        <template #no-data>
          <div class="text-center py-8 text-medium-emphasis">
            <v-icon icon="mdi-school-outline" size="48" class="mb-3" />
            <p>No sections found.</p>
          </div>
        </template>
      </v-data-table-server>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { searchSections } from '@/apis/section'
import type { Section } from '@/apis/section/types'
import { useUserInfoStore } from '@/stores/userInfo'
import { useNotifyStore } from '@/stores/notify'

const router = useRouter()
const userInfoStore = useUserInfoStore()
const notifyStore = useNotifyStore()

const searchName = ref('')
const loading = ref(false)
const sections = ref<Section[]>([])
const totalElements = ref(0)
const pagination = ref({ page: 0, size: 10 })

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
  router.push({ name: 'section-detail', params: { sectionId: section.sectionId } })
}

function editSection(section: Section) {
  notifyStore.info(`Edit section "${section.sectionName}" — coming soon.`)
}

function openCreateDialog() {
  notifyStore.info('Create section — coming soon.')
}

onMounted(loadSections)
</script>
