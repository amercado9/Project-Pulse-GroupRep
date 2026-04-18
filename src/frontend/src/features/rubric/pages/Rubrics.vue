<template>
  <v-container>
    <v-row class="mb-4" align="center">
      <v-col><h2 class="text-h5 font-weight-bold">Rubrics</h2></v-col>
      <v-col cols="auto">
        <v-btn color="primary" prepend-icon="mdi-plus" @click="openDialog">Create Rubric</v-btn>
      </v-col>
    </v-row>

    <!-- Rubric list -->
    <v-row v-if="rubrics.length === 0">
      <v-col><v-alert type="info" variant="tonal">No rubrics found.</v-alert></v-col>
    </v-row>
    <v-row v-else>
      <v-col v-for="rubric in rubrics" :key="rubric.rubricId" cols="12" md="6">
        <v-card variant="outlined">
          <v-card-title>{{ rubric.rubricName }}</v-card-title>
          <v-card-subtitle>{{ rubric.criteria.length }} criteria</v-card-subtitle>
          <v-card-text>
            <v-chip
              v-for="c in rubric.criteria"
              :key="c.criterionId"
              class="mr-1 mb-1"
              size="small"
              variant="tonal"
            >{{ c.criterion }} ({{ c.maxScore }})</v-chip>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- Create Rubric Dialog -->
    <v-dialog v-model="dialog" max-width="700" persistent>
      <v-card>
        <v-card-title class="pa-4">
          {{ step === 1 ? 'Create Rubric' : 'Confirm Rubric' }}
        </v-card-title>
        <v-divider />

        <!-- Step 1: Enter details -->
        <v-card-text v-if="step === 1" class="pa-4">
          <v-text-field
            v-model="form.rubricName"
            label="Rubric Name"
            placeholder="e.g. Peer Eval Rubric v1"
            :error-messages="errors.rubricName"
            class="mb-4"
          />

          <div v-for="(criterion, i) in form.criteria" :key="i" class="mb-4">
            <v-row align="center" class="mb-1">
              <v-col><span class="text-subtitle-2">Criterion {{ i + 1 }}</span></v-col>
              <v-col cols="auto">
                <v-btn
                  v-if="form.criteria.length > 1"
                  icon="mdi-delete"
                  size="small"
                  variant="text"
                  color="error"
                  @click="removeCriterion(i)"
                />
              </v-col>
            </v-row>
            <v-text-field
              v-model="criterion.criterion"
              label="Criterion Name"
              placeholder="e.g. Quality of work"
              :error-messages="criterionErrors[i]?.criterion"
              density="compact"
              class="mb-2"
            />
            <v-textarea
              v-model="criterion.description"
              label="Description"
              placeholder="e.g. How do you rate the quality of this teammate's work?"
              density="compact"
              rows="2"
              class="mb-2"
            />
            <v-text-field
              v-model.number="criterion.maxScore"
              label="Max Score"
              type="number"
              min="0.1"
              step="0.1"
              :error-messages="criterionErrors[i]?.maxScore"
              density="compact"
            />
            <v-divider v-if="i < form.criteria.length - 1" class="mt-3" />
          </div>

          <v-btn variant="tonal" prepend-icon="mdi-plus" @click="addCriterion" class="mt-2">
            Add Criterion
          </v-btn>
        </v-card-text>

        <!-- Step 2: Preview & confirm -->
        <v-card-text v-else class="pa-4">
          <v-alert type="info" variant="tonal" class="mb-4">
            Please review the rubric details before confirming.
          </v-alert>
          <p class="text-subtitle-1 font-weight-bold mb-3">{{ form.rubricName }}</p>
          <v-table density="compact">
            <thead>
              <tr>
                <th>Criterion</th>
                <th>Description</th>
                <th>Max Score</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(c, i) in form.criteria" :key="i">
                <td>{{ c.criterion }}</td>
                <td>{{ c.description }}</td>
                <td>{{ c.maxScore }}</td>
              </tr>
            </tbody>
          </v-table>
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
import { ref, onMounted } from 'vue'
import { createRubric, getAllRubrics } from '../services/rubricService'
import type { Rubric, CriterionRequest } from '../services/rubricTypes'

const rubrics = ref<Rubric[]>([])
const dialog = ref(false)
const step = ref(1)
const saving = ref(false)

const emptyForm = () => ({
  rubricName: '',
  criteria: [{ criterion: '', description: '', maxScore: 10 }] as CriterionRequest[],
})

const form = ref(emptyForm())
const errors = ref<{ rubricName?: string }>({})
const criterionErrors = ref<{ criterion?: string; maxScore?: string }[]>([])

const snackbar = ref({ show: false, message: '', color: 'success' })

onMounted(loadRubrics)

async function loadRubrics() {
  try {
    const res = await getAllRubrics()
    if (res.data.flag) rubrics.value = res.data.data ?? []
  } catch {
    // leave list empty
  }
}

function openDialog() {
  form.value = emptyForm()
  errors.value = {}
  criterionErrors.value = []
  step.value = 1
  dialog.value = true
}

function cancel() {
  dialog.value = false
}

function addCriterion() {
  form.value.criteria.push({ criterion: '', description: '', maxScore: 10 })
}

function removeCriterion(index: number) {
  form.value.criteria.splice(index, 1)
}

function validate(): boolean {
  errors.value = {}
  criterionErrors.value = form.value.criteria.map(() => ({}))
  let valid = true

  if (!form.value.rubricName.trim()) {
    errors.value.rubricName = 'Rubric name is required.'
    valid = false
  }

  form.value.criteria.forEach((c, i) => {
    if (!c.criterion.trim()) {
      criterionErrors.value[i].criterion = 'Criterion name is required.'
      valid = false
    }
    if (!c.maxScore || c.maxScore <= 0) {
      criterionErrors.value[i].maxScore = 'Max score must be a positive number.'
      valid = false
    }
  })

  return valid
}

function goToPreview() {
  if (validate()) step.value = 2
}

async function confirmCreate() {
  saving.value = true
  try {
    const res = await createRubric(form.value)
    if (res.data.flag) {
      rubrics.value.push(res.data.data!)
      snackbar.value = { show: true, message: 'Rubric created successfully.', color: 'success' }
      dialog.value = false
    } else {
      snackbar.value = { show: true, message: res.data.message, color: 'error' }
      step.value = 1
    }
  } catch {
    snackbar.value = { show: true, message: 'An error occurred. Please try again.', color: 'error' }
    step.value = 1
  } finally {
    saving.value = false
  }
}
</script>
