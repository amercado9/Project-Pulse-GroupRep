<template>
  <v-container>
    <v-row class="mb-4" align="center">
      <v-col><h2 class="text-h5 font-weight-bold">Senior Design Sections</h2></v-col>
      <v-col cols="auto">
        <v-btn color="primary" prepend-icon="mdi-plus" @click="openDialog">Create Section</v-btn>
      </v-col>
    </v-row>

    <!-- Search bar -->
    <v-row class="mb-4">
      <v-col cols="12" md="6">
        <v-text-field
          v-model="searchName"
          label="Search by section name"
          prepend-inner-icon="mdi-magnify"
          clearable
          hide-details
          @keyup.enter="search"
          @click:clear="clearSearch"
        />
      </v-col>
      <v-col cols="auto">
        <v-btn color="primary" @click="search">Search</v-btn>
      </v-col>
    </v-row>

    <!-- Results -->
    <v-row v-if="loading">
      <v-col class="text-center"><v-progress-circular indeterminate /></v-col>
    </v-row>

    <v-row v-else-if="sections.length === 0">
      <v-col>
        <v-alert type="info" variant="tonal">
          No matching sections found.
        </v-alert>
      </v-col>
    </v-row>

    <v-row v-else>
      <v-col cols="12">
        <v-table>
          <thead>
            <tr>
              <th>Section Name</th>
              <th>Teams</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="section in sections"
              :key="section.sectionId"
              class="cursor-pointer"
              @click="router.push({ name: 'section-detail', params: { id: section.sectionId } })"
            >
              <td>{{ section.sectionName }}</td>
              <td>
                <span v-if="section.teamNames.length === 0" class="text-medium-emphasis">—</span>
                <v-chip
                  v-for="team in section.teamNames"
                  :key="team"
                  size="small"
                  variant="tonal"
                  class="mr-1"
                >{{ team }}</v-chip>
              </td>
            </tr>
          </tbody>
        </v-table>
      </v-col>
    </v-row>

    <!-- Create Section Dialog -->
    <v-dialog v-model="dialog" max-width="600" persistent>
      <v-card>
        <v-card-title class="pa-4">
          {{ step === 1 ? 'Create Section' : 'Confirm Section' }}
        </v-card-title>
        <v-divider />

        <!-- Step 1: Enter details -->
        <v-card-text v-if="step === 1" class="pa-4">
          <v-text-field
            v-model="form.sectionName"
            label="Section Name"
            placeholder="e.g. Senior Design Spring 2026"
            :error-messages="errors.sectionName"
            class="mb-4"
          />
          <v-row>
            <v-col cols="12" md="6">
              <v-text-field
                v-model="form.startDate"
                label="Start Date"
                type="date"
                :error-messages="errors.startDate"
              />
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field
                v-model="form.endDate"
                label="End Date"
                type="date"
                :error-messages="errors.endDate"
              />
            </v-col>
          </v-row>
          <v-select
            v-model="form.rubricId"
            :items="rubrics"
            item-title="rubricName"
            item-value="rubricId"
            label="Rubric (optional)"
            clearable
            class="mt-2"
          />
        </v-card-text>

        <!-- Step 2: Preview & confirm -->
        <v-card-text v-else class="pa-4">
          <v-alert type="info" variant="tonal" class="mb-4">
            Please review the section details before confirming.
          </v-alert>
          <v-list density="compact">
            <v-list-item title="Section Name" :subtitle="form.sectionName" />
            <v-list-item title="Start Date" :subtitle="form.startDate || '—'" />
            <v-list-item title="End Date" :subtitle="form.endDate || '—'" />
            <v-list-item
              title="Rubric"
              :subtitle="selectedRubricName || '—'"
            />
          </v-list>
        </v-card-text>

        <v-divider />
        <v-card-actions class="pa-4">
          <v-btn variant="text" @click="cancel">Cancel</v-btn>
          <v-spacer />
          <v-btn v-if="step === 2" variant="outlined" @click="step = 1">Modify</v-btn>
          <v-btn
            color="primary"
            :loading="saving"
            @click="step === 1 ? goToPreview() : confirmCreate()"
          >
            {{ step === 1 ? 'Preview' : 'Confirm' }}
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { createSection, findSections } from '../services/sectionService'
import { getAllRubrics } from '@/features/rubric/services/rubricService'
import type { SectionSummary } from '../services/sectionTypes'
import type { Rubric } from '@/features/rubric/services/rubricTypes'

const router = useRouter()

const searchName = ref('')
const sections = ref<SectionSummary[]>([])
const loading = ref(false)

const dialog = ref(false)
const step = ref(1)
const saving = ref(false)
const rubrics = ref<Rubric[]>([])

const emptyForm = () => ({
  sectionName: '',
  startDate: '',
  endDate: '',
  rubricId: null as number | null,
})

const form = ref(emptyForm())
const errors = ref<{ sectionName?: string; startDate?: string; endDate?: string }>({})

const snackbar = ref({ show: false, message: '', color: 'success' })

const selectedRubricName = computed(() =>
  rubrics.value.find(r => r.rubricId === form.value.rubricId)?.rubricName ?? null
)

onMounted(() => {
  search()
  loadRubrics()
})

async function search() {
  loading.value = true
  try {
    const res = await findSections(searchName.value || undefined) as any
    if (res.flag) sections.value = res.data ?? []
    else sections.value = []
  } catch {
    sections.value = []
  } finally {
    loading.value = false
  }
}

function clearSearch() {
  searchName.value = ''
  search()
}

async function loadRubrics() {
  try {
    const res = await getAllRubrics() as any
    if (res.flag) rubrics.value = res.data ?? []
  } catch {
    // leave list empty
  }
}

function openDialog() {
  form.value = emptyForm()
  errors.value = {}
  step.value = 1
  dialog.value = true
}

function cancel() {
  dialog.value = false
}

function validate(): boolean {
  errors.value = {}
  let valid = true

  if (!form.value.sectionName.trim()) {
    errors.value.sectionName = 'Section name is required.'
    valid = false
  }

  if (form.value.startDate && form.value.endDate && form.value.endDate < form.value.startDate) {
    errors.value.endDate = 'End date must be after start date.'
    valid = false
  }

  return valid
}

function goToPreview() {
  if (validate()) step.value = 2
}

async function confirmCreate() {
  saving.value = true
  try {
    const payload = {
      sectionName: form.value.sectionName,
      startDate: form.value.startDate || null,
      endDate: form.value.endDate || null,
      rubricId: form.value.rubricId,
    }
    const res = await createSection(payload) as any
    if (res.flag) {
      await search()
      snackbar.value = { show: true, message: 'Section created successfully.', color: 'success' }
      dialog.value = false
    } else {
      snackbar.value = { show: true, message: res.message, color: 'error' }
      step.value = 1
    }
  } catch (err: any) {
    const msg = err?.response?.data?.message || 'An error occurred. Please try again.'
    snackbar.value = { show: true, message: msg, color: 'error' }
    step.value = 1
  } finally {
    saving.value = false
  }
}
</script>
