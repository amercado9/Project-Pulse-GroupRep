<template>
  <v-container>
    <v-btn variant="text" prepend-icon="mdi-arrow-left" class="mb-4" @click="router.push({ name: 'sections' })">
      Back to Sections
    </v-btn>

    <v-row v-if="loading">
      <v-col class="text-center"><v-progress-circular indeterminate /></v-col>
    </v-row>

    <template v-else-if="section">
      <!-- Header -->
      <v-row class="mb-2" align="center">
        <v-col>
          <h2 class="text-h5 font-weight-bold">{{ section.sectionName }}</h2>
        </v-col>
        <v-col cols="auto">
          <v-chip :color="section.active ? 'success' : 'default'" variant="tonal" class="mr-2">
            {{ section.active ? 'Active' : 'Inactive' }}
          </v-chip>
          <v-btn color="primary" prepend-icon="mdi-pencil" variant="outlined" @click="openEditDialog">
            Edit
          </v-btn>
        </v-col>
      </v-row>

      <!-- Basic Info -->
      <v-card variant="outlined" class="mb-4">
        <v-card-title class="text-subtitle-1 font-weight-bold pa-4 pb-2">Section Info</v-card-title>
        <v-card-text>
          <v-row>
            <v-col cols="12" sm="4">
              <div class="text-caption text-medium-emphasis">Start Date</div>
              <div>{{ section.startDate ?? '—' }}</div>
            </v-col>
            <v-col cols="12" sm="4">
              <div class="text-caption text-medium-emphasis">End Date</div>
              <div>{{ section.endDate ?? '—' }}</div>
            </v-col>
            <v-col cols="12" sm="4">
              <div class="text-caption text-medium-emphasis">Rubric</div>
              <div>{{ section.rubricName ?? '—' }}</div>
            </v-col>
          </v-row>
        </v-card-text>
      </v-card>

      <!-- Teams -->
      <v-card variant="outlined" class="mb-4">
        <v-card-title class="text-subtitle-1 font-weight-bold pa-4 pb-2">Teams</v-card-title>
        <v-card-text>
          <v-alert v-if="section.teams.length === 0" type="info" variant="tonal" density="compact">
            No teams assigned to this section yet.
          </v-alert>
          <div v-else>
            <v-expansion-panels variant="accordion">
              <v-expansion-panel v-for="team in section.teams" :key="team.teamId" :title="team.teamName">
                <v-expansion-panel-text>
                  <div class="mb-2">
                    <div class="text-caption text-medium-emphasis mb-1">Members</div>
                    <span v-if="team.members.length === 0" class="text-medium-emphasis">—</span>
                    <v-chip v-for="m in team.members" :key="m" size="small" class="mr-1 mb-1">{{ m }}</v-chip>
                  </div>
                  <div>
                    <div class="text-caption text-medium-emphasis mb-1">Instructors</div>
                    <span v-if="team.instructors.length === 0" class="text-medium-emphasis">—</span>
                    <v-chip v-for="i in team.instructors" :key="i" size="small" color="primary" variant="tonal" class="mr-1 mb-1">{{ i }}</v-chip>
                  </div>
                </v-expansion-panel-text>
              </v-expansion-panel>
            </v-expansion-panels>
          </div>
        </v-card-text>
      </v-card>

      <!-- Unassigned -->
      <v-row>
        <v-col cols="12" md="6">
          <v-card variant="outlined">
            <v-card-title class="text-subtitle-1 font-weight-bold pa-4 pb-2">Unassigned Students</v-card-title>
            <v-card-text>
              <span v-if="section.unassignedStudents.length === 0" class="text-medium-emphasis">None</span>
              <v-chip v-for="s in section.unassignedStudents" :key="s" size="small" class="mr-1 mb-1">{{ s }}</v-chip>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" md="6">
          <v-card variant="outlined">
            <v-card-title class="text-subtitle-1 font-weight-bold pa-4 pb-2">Unassigned Instructors</v-card-title>
            <v-card-text>
              <span v-if="section.unassignedInstructors.length === 0" class="text-medium-emphasis">None</span>
              <v-chip v-for="i in section.unassignedInstructors" :key="i" size="small" color="primary" variant="tonal" class="mr-1 mb-1">{{ i }}</v-chip>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </template>

    <!-- Edit Section Dialog -->
    <v-dialog v-model="editDialog" max-width="600" persistent>
      <v-card>
        <v-card-title class="pa-4">
          {{ editStep === 1 ? 'Edit Section' : 'Confirm Changes' }}
        </v-card-title>
        <v-divider />

        <!-- Step 1: Edit fields -->
        <v-card-text v-if="editStep === 1" class="pa-4">
          <v-text-field
            v-model="editForm.sectionName"
            label="Section Name"
            :error-messages="editErrors.sectionName"
            class="mb-4"
          />
          <v-row>
            <v-col cols="12" md="6">
              <v-text-field
                v-model="editForm.startDate"
                label="Start Date"
                type="date"
                :error-messages="editErrors.startDate"
              />
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field
                v-model="editForm.endDate"
                label="End Date"
                type="date"
                :error-messages="editErrors.endDate"
              />
            </v-col>
          </v-row>
          <v-select
            v-model="editForm.rubricId"
            :items="rubrics"
            item-title="rubricName"
            item-value="rubricId"
            label="Rubric (optional)"
            clearable
            class="mt-2"
          />
        </v-card-text>

        <!-- Step 2: Preview changes -->
        <v-card-text v-else class="pa-4">
          <v-alert type="info" variant="tonal" class="mb-4">
            Please review the changes before confirming.
          </v-alert>
          <v-list density="compact">
            <v-list-item title="Section Name" :subtitle="editForm.sectionName" />
            <v-list-item title="Start Date" :subtitle="editForm.startDate || '—'" />
            <v-list-item title="End Date" :subtitle="editForm.endDate || '—'" />
            <v-list-item title="Rubric" :subtitle="selectedRubricName || '—'" />
          </v-list>
        </v-card-text>

        <v-divider />
        <v-card-actions class="pa-4">
          <v-btn variant="text" @click="editDialog = false">Cancel</v-btn>
          <v-spacer />
          <v-btn v-if="editStep === 2" variant="outlined" @click="editStep = 1">Modify</v-btn>
          <v-btn
            color="primary"
            :loading="saving"
            @click="editStep === 1 ? goToPreview() : confirmEdit()"
          >
            {{ editStep === 1 ? 'Preview' : 'Confirm' }}
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
import { useRoute, useRouter } from 'vue-router'
import { getSection, updateSection } from '../services/sectionService'
import { getAllRubrics } from '@/features/rubric/services/rubricService'
import type { SectionDetail } from '../services/sectionTypes'
import type { Rubric } from '@/features/rubric/services/rubricTypes'

const route = useRoute()
const router = useRouter()

const section = ref<SectionDetail | null>(null)
const loading = ref(false)

const editDialog = ref(false)
const editStep = ref(1)
const saving = ref(false)
const rubrics = ref<Rubric[]>([])

const editForm = ref({
  sectionName: '',
  startDate: '',
  endDate: '',
  rubricId: null as number | null,
})
const editErrors = ref<{ sectionName?: string; startDate?: string; endDate?: string }>({})

const snackbar = ref({ show: false, message: '', color: 'success' })

const selectedRubricName = computed(() =>
  rubrics.value.find(r => r.rubricId === editForm.value.rubricId)?.rubricName ?? null
)

onMounted(async () => {
  loading.value = true
  try {
    const res = await getSection(Number(route.params.id)) as any
    if (res.flag) section.value = res.data
    else router.replace({ name: 'not-found' })
  } catch {
    router.replace({ name: 'not-found' })
  } finally {
    loading.value = false
  }
  loadRubrics()
})

async function loadRubrics() {
  try {
    const res = await getAllRubrics() as any
    if (res.flag) rubrics.value = res.data ?? []
  } catch {
    // leave empty
  }
}

function openEditDialog() {
  if (!section.value) return
  editForm.value = {
    sectionName: section.value.sectionName,
    startDate: section.value.startDate ?? '',
    endDate: section.value.endDate ?? '',
    rubricId: section.value.rubricId ?? null,
  }
  editErrors.value = {}
  editStep.value = 1
  editDialog.value = true
}

function validate(): boolean {
  editErrors.value = {}
  let valid = true

  if (!editForm.value.sectionName.trim()) {
    editErrors.value.sectionName = 'Section name is required.'
    valid = false
  }

  if (editForm.value.startDate && editForm.value.endDate && editForm.value.endDate < editForm.value.startDate) {
    editErrors.value.endDate = 'End date must be after start date.'
    valid = false
  }

  return valid
}

function goToPreview() {
  if (validate()) editStep.value = 2
}

async function confirmEdit() {
  saving.value = true
  try {
    const payload = {
      sectionName: editForm.value.sectionName,
      startDate: editForm.value.startDate || null,
      endDate: editForm.value.endDate || null,
      rubricId: editForm.value.rubricId,
    }
    const res = await updateSection(Number(route.params.id), payload) as any
    if (res.flag) {
      section.value = res.data
      snackbar.value = { show: true, message: 'Section updated successfully.', color: 'success' }
      editDialog.value = false
    } else {
      snackbar.value = { show: true, message: res.message, color: 'error' }
      editStep.value = 1
    }
  } catch (err: any) {
    const msg = err?.response?.data?.message || 'An error occurred. Please try again.'
    snackbar.value = { show: true, message: msg, color: 'error' }
    editStep.value = 1
  } finally {
    saving.value = false
  }
}
</script>
