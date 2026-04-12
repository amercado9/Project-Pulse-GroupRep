<template>
  <v-card rounded="lg">
    <v-card-title class="d-flex align-center justify-space-between pa-4">
      <span class="text-h6">Rubric Management</span>
      <v-btn color="primary" prepend-icon="mdi-plus" @click="openCreate">Create Rubric</v-btn>
    </v-card-title>

    <!-- Search -->
    <v-card-text class="pb-0">
      <v-row dense align="center">
        <v-col cols="12" sm="5">
          <v-text-field
            v-model="searchName"
            label="Search by rubric name"
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

    <!-- Table -->
    <v-card-text>
      <v-data-table
        :headers="headers"
        :items="rubrics"
        :loading="loading"
        item-value="rubricId"
        show-expand
      >
        <!-- Criteria count -->
        <template #item.criteria="{ item }">
          <v-chip size="small" color="primary" variant="tonal">
            {{ item.criteria?.length ?? 0 }} criteria
          </v-chip>
        </template>

        <!-- Actions -->
        <template #item.actions="{ item }">
          <v-btn icon="mdi-pencil" size="small" variant="text" color="primary" @click="openEdit(item)" />
        </template>

        <!-- Expanded row: assigned criteria -->
        <template #expanded-row="{ item }">
          <tr>
            <td :colspan="headers.length + 1" class="pa-0">
              <v-card flat color="grey-lighten-4" rounded="0">
                <v-card-text>
                  <div class="d-flex align-center justify-space-between mb-3">
                    <span class="text-subtitle-2 font-weight-bold">Assigned Criteria</span>
                    <v-btn
                      size="small"
                      color="primary"
                      variant="tonal"
                      prepend-icon="mdi-plus"
                      @click="openAssign(item)"
                    >
                      Assign Criterion
                    </v-btn>
                  </div>
                  <div v-if="!item.criteria?.length" class="text-medium-emphasis text-body-2">
                    No criteria assigned yet.
                  </div>
                  <v-table v-else density="compact">
                    <thead>
                      <tr>
                        <th>Criterion</th>
                        <th>Description</th>
                        <th>Max Score</th>
                        <th class="text-right">Remove</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="c in item.criteria" :key="c.criterionId">
                        <td>{{ c.criterion }}</td>
                        <td class="text-medium-emphasis">{{ c.description || '—' }}</td>
                        <td><v-chip size="x-small" variant="tonal" color="primary">{{ c.maxScore }} pts</v-chip></td>
                        <td class="text-right">
                          <v-btn
                            icon="mdi-close"
                            size="x-small"
                            variant="text"
                            color="error"
                            @click="removeCriterion(item, c)"
                          />
                        </td>
                      </tr>
                    </tbody>
                  </v-table>
                </v-card-text>
              </v-card>
            </td>
          </tr>
        </template>

        <template #no-data>
          <div class="text-center py-8 text-medium-emphasis">
            <v-icon icon="mdi-check-circle-outline" size="48" class="mb-3" />
            <p>No rubrics found.</p>
          </div>
        </template>
      </v-data-table>
    </v-card-text>

    <!-- Create / Edit rubric dialog -->
    <v-dialog v-model="rubricDialog" max-width="460" persistent>
      <v-card rounded="lg">
        <v-card-title class="pa-4">{{ editingId ? 'Edit Rubric' : 'Create Rubric' }}</v-card-title>
        <v-divider />
        <v-card-text class="pt-4">
          <v-form ref="rubricFormRef">
            <v-text-field
              v-model="rubricForm.rubricName"
              label="Rubric Name"
              variant="outlined"
              density="comfortable"
              :rules="[v => !!v || 'Name is required.']"
            />
          </v-form>
        </v-card-text>
        <v-card-actions class="pa-4 pt-0">
          <v-spacer />
          <v-btn variant="text" @click="rubricDialog = false">Cancel</v-btn>
          <v-btn color="primary" variant="flat" :loading="saving" @click="saveRubric">
            {{ editingId ? 'Save Changes' : 'Create' }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Assign criterion dialog -->
    <v-dialog v-model="assignDialog" max-width="500" persistent>
      <v-card rounded="lg">
        <v-card-title class="pa-4">Assign Criterion</v-card-title>
        <v-divider />
        <v-card-text class="pt-4">
          <p class="text-body-2 text-medium-emphasis mb-3">
            Select a criterion to add to <strong>{{ assignTarget?.rubricName }}</strong>.
          </p>
          <v-autocomplete
            v-model="selectedCriterionId"
            :items="availableCriteria"
            item-title="criterion"
            item-value="criterionId"
            label="Criterion"
            variant="outlined"
            density="comfortable"
            :loading="loadingCriteria"
          />
        </v-card-text>
        <v-card-actions class="pa-4 pt-0">
          <v-spacer />
          <v-btn variant="text" @click="assignDialog = false">Cancel</v-btn>
          <v-btn color="primary" variant="flat" :loading="saving" :disabled="!selectedCriterionId" @click="doAssign">
            Assign
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  searchRubrics, createRubric, updateRubric,
  assignCriterionToRubric, unassignCriterionFromRubric,
  searchEvaluationCriteria
} from '@/apis/rubric'
import type { Rubric, Criterion } from '@/apis/rubric/types'
import { useNotifyStore } from '@/stores/notify'

const notifyStore = useNotifyStore()

const searchName = ref('')
const loading = ref(false)
const saving = ref(false)
const rubrics = ref<Rubric[]>([])

// Rubric form
const rubricDialog = ref(false)
const rubricFormRef = ref()
const editingId = ref<number | null>(null)
const rubricForm = ref<Rubric>({ rubricName: '' })

// Assign criterion
const assignDialog = ref(false)
const assignTarget = ref<Rubric | null>(null)
const availableCriteria = ref<Criterion[]>([])
const selectedCriterionId = ref<number | null>(null)
const loadingCriteria = ref(false)

const headers = [
  { title: 'Rubric Name', key: 'rubricName', sortable: true },
  { title: 'Criteria', key: 'criteria', sortable: false },
  { title: 'Actions', key: 'actions', sortable: false, align: 'end' as const },
]

async function load() {
  loading.value = true
  try {
    const res = await searchRubrics({ rubricName: searchName.value || undefined })
    rubrics.value = res.data.content
  } catch { /* interceptor handles */ } finally { loading.value = false }
}

function runSearch() { load() }
function clearSearch() { searchName.value = ''; load() }

function openCreate() {
  editingId.value = null
  rubricForm.value = { rubricName: '' }
  rubricDialog.value = true
}

function openEdit(r: Rubric) {
  editingId.value = r.rubricId ?? null
  rubricForm.value = { ...r }
  rubricDialog.value = true
}

async function saveRubric() {
  const { valid } = await rubricFormRef.value.validate()
  if (!valid) return
  saving.value = true
  try {
    if (editingId.value) {
      await updateRubric({ ...rubricForm.value, rubricId: editingId.value })
      notifyStore.success('Rubric updated.')
    } else {
      await createRubric(rubricForm.value)
      notifyStore.success('Rubric created.')
    }
    rubricDialog.value = false
    load()
  } catch { /* interceptor handles */ } finally { saving.value = false }
}

async function openAssign(rubric: Rubric) {
  assignTarget.value = rubric
  selectedCriterionId.value = null
  assignDialog.value = true
  loadingCriteria.value = true
  try {
    const res = await searchEvaluationCriteria({})
    const assigned = new Set(rubric.criteria?.map(c => c.criterionId))
    availableCriteria.value = res.data.content.filter(c => !assigned.has(c.criterionId))
  } catch { /* interceptor handles */ } finally { loadingCriteria.value = false }
}

async function doAssign() {
  if (!assignTarget.value?.rubricId || !selectedCriterionId.value) return
  saving.value = true
  try {
    await assignCriterionToRubric(assignTarget.value.rubricId, selectedCriterionId.value)
    notifyStore.success('Criterion assigned.')
    assignDialog.value = false
    load()
  } catch { /* interceptor handles */ } finally { saving.value = false }
}

async function removeCriterion(rubric: Rubric, criterion: Criterion) {
  if (!rubric.rubricId || !criterion.criterionId) return
  try {
    await unassignCriterionFromRubric(rubric.rubricId, criterion.criterionId)
    notifyStore.success('Criterion removed.')
    load()
  } catch { /* interceptor handles */ }
}

onMounted(load)
</script>
