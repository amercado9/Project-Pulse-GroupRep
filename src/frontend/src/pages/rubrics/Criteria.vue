<template>
  <v-card rounded="lg">
    <v-card-title class="d-flex align-center justify-space-between pa-4">
      <span class="text-h6">Criteria Management</span>
      <v-btn color="primary" prepend-icon="mdi-plus" @click="openCreate">Add Criterion</v-btn>
    </v-card-title>

    <!-- Search -->
    <v-card-text class="pb-0">
      <v-row dense align="center">
        <v-col cols="12" sm="5">
          <v-text-field
            v-model="searchName"
            label="Search by criterion name"
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
        :items="criteria"
        :loading="loading"
        item-value="criterionId"
      >
        <template #item.maxScore="{ item }">
          <v-chip size="small" color="primary" variant="tonal">{{ item.maxScore }} pts</v-chip>
        </template>
        <template #item.description="{ item }">
          <span class="text-truncate d-inline-block" style="max-width:280px">{{ item.description || '—' }}</span>
        </template>
        <template #item.actions="{ item }">
          <v-btn icon="mdi-pencil" size="small" variant="text" color="primary" @click="openEdit(item)" />
        </template>
        <template #no-data>
          <div class="text-center py-8 text-medium-emphasis">
            <v-icon icon="mdi-file-check-outline" size="48" class="mb-3" />
            <p>No criteria found.</p>
          </div>
        </template>
      </v-data-table>
    </v-card-text>

    <!-- Create / Edit dialog -->
    <v-dialog v-model="dialog" max-width="520" persistent>
      <v-card rounded="lg">
        <v-card-title class="pa-4">{{ editingId ? 'Edit Criterion' : 'Add Criterion' }}</v-card-title>
        <v-divider />
        <v-card-text class="pt-4">
          <v-form ref="formRef">
            <v-text-field
              v-model="form.criterion"
              label="Criterion Name"
              variant="outlined"
              density="comfortable"
              class="mb-3"
              :rules="[v => !!v || 'Name is required.']"
            />
            <v-textarea
              v-model="form.description"
              label="Description"
              variant="outlined"
              density="comfortable"
              rows="3"
              class="mb-3"
            />
            <v-text-field
              v-model.number="form.maxScore"
              label="Max Score"
              type="number"
              variant="outlined"
              density="comfortable"
              :rules="[v => v > 0 || 'Must be greater than 0.']"
            />
          </v-form>
        </v-card-text>
        <v-card-actions class="pa-4 pt-0">
          <v-spacer />
          <v-btn variant="text" @click="closeDialog">Cancel</v-btn>
          <v-btn color="primary" variant="flat" :loading="saving" @click="save">
            {{ editingId ? 'Save Changes' : 'Add Criterion' }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { searchEvaluationCriteria, createCriterion, updateCriterion } from '@/apis/rubric'
import type { Criterion } from '@/apis/rubric/types'
import { useNotifyStore } from '@/stores/notify'

const notifyStore = useNotifyStore()

const searchName = ref('')
const loading = ref(false)
const saving = ref(false)
const criteria = ref<Criterion[]>([])
const dialog = ref(false)
const formRef = ref()
const editingId = ref<number | null>(null)

const blankForm = (): Criterion => ({ criterion: '', description: '', maxScore: 10 })
const form = ref<Criterion>(blankForm())

const headers = [
  { title: 'Criterion', key: 'criterion', sortable: true },
  { title: 'Description', key: 'description', sortable: false },
  { title: 'Max Score', key: 'maxScore', sortable: true },
  { title: 'Actions', key: 'actions', sortable: false, align: 'end' as const },
]

async function load() {
  loading.value = true
  try {
    const res = await searchEvaluationCriteria({ criterion: searchName.value || undefined })
    criteria.value = res.data.content
  } catch { /* interceptor handles */ } finally { loading.value = false }
}

function runSearch() { load() }
function clearSearch() { searchName.value = ''; load() }

function openCreate() {
  editingId.value = null
  form.value = blankForm()
  dialog.value = true
}

function openEdit(c: Criterion) {
  editingId.value = c.criterionId ?? null
  form.value = { ...c }
  dialog.value = true
}

function closeDialog() { dialog.value = false }

async function save() {
  const { valid } = await formRef.value.validate()
  if (!valid) return
  saving.value = true
  try {
    if (editingId.value) {
      await updateCriterion({ ...form.value, criterionId: editingId.value })
      notifyStore.success('Criterion updated.')
    } else {
      await createCriterion(form.value)
      notifyStore.success('Criterion added.')
    }
    dialog.value = false
    load()
  } catch { /* interceptor handles */ } finally { saving.value = false }
}

onMounted(load)
</script>
