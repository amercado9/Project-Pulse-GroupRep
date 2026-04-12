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

  <!-- UC-4: Create Section Dialog (3-step stepper) -->
  <v-dialog v-model="createDialog" max-width="620" persistent>
    <v-card rounded="lg">
      <v-card-title class="pa-4 d-flex align-center gap-2">
        <v-icon icon="mdi-school-outline" color="primary" />
        Create Senior Design Section
      </v-card-title>
      <v-divider />

      <v-stepper v-model="step" flat>
        <v-stepper-header>
          <v-stepper-item title="Details" :value="1" :complete="step > 1" />
          <v-divider />
          <v-stepper-item title="Rubric" :value="2" :complete="step > 2" />
          <v-divider />
          <v-stepper-item title="Confirm" :value="3" />
        </v-stepper-header>

        <v-stepper-window>
          <!-- Step 1: Section Details -->
          <v-stepper-window-item :value="1">
            <v-form ref="detailsFormRef" class="pa-4">
              <v-text-field
                v-model="form.sectionName"
                label="Section Name *"
                variant="outlined"
                density="comfortable"
                placeholder="e.g. Spring 2026 – Section A"
                :rules="[v => !!v?.trim() || 'Section name is required.']"
                class="mb-3"
              />
              <v-row dense>
                <v-col cols="12" sm="6">
                  <v-text-field
                    v-model="form.startDate"
                    label="Start Date *"
                    variant="outlined"
                    density="comfortable"
                    type="date"
                    :rules="[v => !!v || 'Start date is required.']"
                  />
                </v-col>
                <v-col cols="12" sm="6">
                  <v-text-field
                    v-model="form.endDate"
                    label="End Date *"
                    variant="outlined"
                    density="comfortable"
                    type="date"
                    :rules="[v => !!v || 'End date is required.', v => !form.startDate || v >= form.startDate || 'End date must be after start date.']"
                  />
                </v-col>
              </v-row>
              <v-row dense class="mt-1">
                <v-col cols="12" sm="6">
                  <v-select
                    v-model="form.warWeeklyDueDay"
                    :items="daysOfWeek"
                    label="WAR Due Day"
                    variant="outlined"
                    density="comfortable"
                  />
                </v-col>
                <v-col cols="12" sm="6">
                  <v-text-field
                    v-model="form.warDueTime"
                    label="WAR Due Time"
                    variant="outlined"
                    density="comfortable"
                    type="time"
                  />
                </v-col>
                <v-col cols="12" sm="6">
                  <v-select
                    v-model="form.peerEvaluationWeeklyDueDay"
                    :items="daysOfWeek"
                    label="Peer Eval Due Day"
                    variant="outlined"
                    density="comfortable"
                  />
                </v-col>
                <v-col cols="12" sm="6">
                  <v-text-field
                    v-model="form.peerEvaluationDueTime"
                    label="Peer Eval Due Time"
                    variant="outlined"
                    density="comfortable"
                    type="time"
                  />
                </v-col>
              </v-row>
            </v-form>
          </v-stepper-window-item>

          <!-- Step 2: Rubric Selection -->
          <v-stepper-window-item :value="2">
            <div class="pa-4">
              <p class="text-body-2 text-medium-emphasis mb-4">
                Select a rubric for peer evaluations. This is optional — you can assign one later.
              </p>
              <v-autocomplete
                v-model="selectedRubricId"
                :items="rubrics"
                item-title="rubricName"
                item-value="rubricId"
                label="Rubric (optional)"
                variant="outlined"
                density="comfortable"
                clearable
                :loading="loadingRubrics"
                class="mb-4"
                @update:model-value="onRubricSelected"
              />

              <template v-if="selectedRubric">
                <p class="text-subtitle-2 font-weight-bold mb-2">
                  Criteria in "{{ selectedRubric.rubricName }}"
                </p>
                <div v-if="!selectedRubric.criteria?.length" class="text-medium-emphasis text-body-2">
                  No criteria assigned to this rubric.
                </div>
                <v-table v-else density="compact">
                  <thead>
                    <tr>
                      <th>Criterion</th>
                      <th>Description</th>
                      <th>Max Score</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="c in selectedRubric.criteria" :key="c.criterionId">
                      <td class="font-weight-medium">{{ c.criterion }}</td>
                      <td class="text-medium-emphasis">{{ c.description || '—' }}</td>
                      <td><v-chip size="x-small" variant="tonal" color="primary">{{ c.maxScore }} pts</v-chip></td>
                    </tr>
                  </tbody>
                </v-table>
              </template>
            </div>
          </v-stepper-window-item>

          <!-- Step 3: Confirmation -->
          <v-stepper-window-item :value="3">
            <div class="pa-4">
              <p class="text-body-2 text-medium-emphasis mb-4">
                Review the details before creating the section.
              </p>
              <v-table density="compact">
                <tbody>
                  <tr>
                    <td class="text-medium-emphasis" style="width:45%">Section Name</td>
                    <td class="font-weight-medium">{{ form.sectionName }}</td>
                  </tr>
                  <tr>
                    <td class="text-medium-emphasis">Start Date</td>
                    <td>{{ form.startDate }}</td>
                  </tr>
                  <tr>
                    <td class="text-medium-emphasis">End Date</td>
                    <td>{{ form.endDate }}</td>
                  </tr>
                  <tr>
                    <td class="text-medium-emphasis">WAR Due</td>
                    <td>{{ form.warWeeklyDueDay }} at {{ form.warDueTime }}</td>
                  </tr>
                  <tr>
                    <td class="text-medium-emphasis">Peer Eval Due</td>
                    <td>{{ form.peerEvaluationWeeklyDueDay }} at {{ form.peerEvaluationDueTime }}</td>
                  </tr>
                  <tr>
                    <td class="text-medium-emphasis">Rubric</td>
                    <td>{{ selectedRubric?.rubricName ?? 'None' }}</td>
                  </tr>
                </tbody>
              </v-table>
            </div>
          </v-stepper-window-item>
        </v-stepper-window>
      </v-stepper>

      <v-divider />
      <v-card-actions class="pa-4">
        <v-btn variant="text" @click="cancelCreate">Cancel</v-btn>
        <v-spacer />
        <v-btn v-if="step > 1" variant="tonal" @click="step--">Back</v-btn>
        <v-btn v-if="step < 3" color="primary" variant="flat" @click="nextStep">Next</v-btn>
        <v-btn v-else color="primary" variant="flat" :loading="saving" @click="submitCreate">
          Create Section
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { searchSections, createSection } from '@/apis/section'
import type { Section, DayOfWeek } from '@/apis/section/types'
import { searchRubrics } from '@/apis/rubric'
import type { Rubric } from '@/apis/rubric/types'
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

const daysOfWeek: DayOfWeek[] = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY']

// ── Create dialog state ────────────────────────────────────────────────────
const createDialog = ref(false)
const step = ref(1)
const saving = ref(false)
const detailsFormRef = ref()

const emptyForm = (): Section => ({
  sectionName: '',
  startDate: '',
  endDate: '',
  isActive: true,
  warWeeklyDueDay: 'FRIDAY',
  warDueTime: '23:59',
  peerEvaluationWeeklyDueDay: 'FRIDAY',
  peerEvaluationDueTime: '23:59',
})
const form = ref<Section>(emptyForm())

// Rubric selection
const rubrics = ref<Rubric[]>([])
const loadingRubrics = ref(false)
const selectedRubricId = ref<number | null>(null)
const selectedRubric = ref<Rubric | null>(null)

// ── Table actions ──────────────────────────────────────────────────────────

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

// ── Create dialog ──────────────────────────────────────────────────────────

async function openCreateDialog() {
  form.value = emptyForm()
  step.value = 1
  selectedRubricId.value = null
  selectedRubric.value = null
  createDialog.value = true
  loadingRubrics.value = true
  try {
    const res = await searchRubrics({})
    rubrics.value = res.data.content
  } catch {
    // handled by request interceptor
  } finally {
    loadingRubrics.value = false
  }
}

function cancelCreate() {
  createDialog.value = false
}

async function nextStep() {
  if (step.value === 1) {
    const { valid } = await detailsFormRef.value.validate()
    if (!valid) return
  }
  step.value++
}

function onRubricSelected(id: number | null) {
  selectedRubric.value = id ? (rubrics.value.find(r => r.rubricId === id) ?? null) : null
  form.value.rubricId = id ?? undefined
}

async function submitCreate() {
  saving.value = true
  try {
    await createSection(form.value)
    notifyStore.success(`Section "${form.value.sectionName}" created.`)
    createDialog.value = false
    loadSections()
  } catch {
    // handled by request interceptor
  } finally {
    saving.value = false
  }
}

onMounted(loadSections)
</script>
