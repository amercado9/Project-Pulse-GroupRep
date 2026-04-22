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
        <v-col cols="auto" class="d-flex align-center ga-2">
          <v-chip :color="section.active ? 'success' : 'default'" variant="tonal">
            {{ section.active ? 'Active' : 'Inactive' }}
          </v-chip>
          <v-btn color="primary" prepend-icon="mdi-pencil" variant="outlined" size="small" @click="openEditDialog">
            Edit
          </v-btn>
          <v-btn
            color="primary"
            prepend-icon="mdi-email-plus-outline"
            size="small"
            @click="router.push({ name: 'section-invite', params: { id: section.sectionId } })"
          >
            Invite Students
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

      <!-- Active Weeks -->
      <v-card variant="outlined" class="mb-4">
        <v-card-title class="text-subtitle-1 font-weight-bold pa-4 pb-2 d-flex align-center">
          <span>Active Weeks</span>
          <v-spacer />
          <v-btn
            v-if="section.startDate && section.endDate"
            size="small"
            color="primary"
            variant="outlined"
            prepend-icon="mdi-calendar-edit"
            @click="openWeeksDialog"
          >
            Set Up Active Weeks
          </v-btn>
        </v-card-title>
        <v-card-text>
          <v-alert v-if="!section.startDate || !section.endDate" type="warning" variant="tonal" density="compact">
            Set a start and end date on this section before configuring active weeks.
          </v-alert>
          <template v-else-if="section.activeWeeks.length === 0">
            <span class="text-medium-emphasis">No active weeks configured yet.</span>
          </template>
          <template v-else>
            <v-chip
              v-for="week in sortedActiveWeeks"
              :key="week"
              size="small"
              color="primary"
              variant="tonal"
              class="mr-1 mb-1"
            >{{ formatWeekLabel(week) }}</v-chip>
          </template>
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
          <v-btn color="primary" :loading="saving" @click="editStep === 1 ? goToEditPreview() : confirmEdit()">
            {{ editStep === 1 ? 'Preview' : 'Confirm' }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Active Weeks Dialog -->
    <v-dialog v-model="weeksDialog" max-width="700" persistent>
      <v-card>
        <v-card-title class="pa-4">
          {{ weeksStep === 1 ? 'Set Up Active Weeks' : 'Confirm Active Weeks' }}
        </v-card-title>
        <v-divider />

        <!-- Step 1: Select weeks -->
        <v-card-text v-if="weeksStep === 1" class="pa-4">
          <v-alert type="info" variant="tonal" density="compact" class="mb-4">
            Select the weeks during which students must submit WARs and peer evaluations.
          </v-alert>
          <div class="d-flex gap-2 mb-3">
            <v-btn size="small" variant="tonal" @click="selectAll">Select All</v-btn>
            <v-btn size="small" variant="tonal" @click="deselectAll">Deselect All</v-btn>
          </div>
          <v-row dense>
            <v-col
              v-for="week in allWeeks"
              :key="week.value"
              cols="6"
              sm="4"
              md="3"
            >
              <v-checkbox
                v-model="selectedWeeks"
                :value="week.value"
                :label="week.label"
                density="compact"
                hide-details
              />
            </v-col>
          </v-row>
        </v-card-text>

        <!-- Step 2: Preview active weeks -->
        <v-card-text v-else class="pa-4">
          <v-alert type="info" variant="tonal" class="mb-4">
            Please review the active weeks before confirming.
          </v-alert>
          <p class="text-body-2 mb-3">
            <strong>{{ selectedWeeks.length }}</strong> active weeks selected:
          </p>
          <template v-if="selectedWeeks.length === 0">
            <span class="text-medium-emphasis">No active weeks selected.</span>
          </template>
          <v-chip
            v-for="week in sortedSelectedWeeks"
            :key="week"
            size="small"
            color="primary"
            variant="tonal"
            class="mr-1 mb-1"
          >{{ formatWeekLabel(week) }}</v-chip>
        </v-card-text>

        <v-divider />
        <v-card-actions class="pa-4">
          <v-btn variant="text" @click="weeksDialog = false">Cancel</v-btn>
          <v-spacer />
          <v-btn v-if="weeksStep === 2" variant="outlined" @click="weeksStep = 1">Modify</v-btn>
          <v-btn
            color="primary"
            :loading="savingWeeks"
            @click="weeksStep === 1 ? weeksStep = 2 : confirmWeeks()"
          >
            {{ weeksStep === 1 ? 'Preview' : 'Confirm' }}
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
import { getSection, updateSection, setupActiveWeeks } from '../services/sectionService'
import { getAllRubrics } from '@/features/rubric/services/rubricService'
import type { SectionDetail } from '../services/sectionTypes'
import type { Rubric } from '@/features/rubric/services/rubricTypes'

const route = useRoute()
const router = useRouter()

const section = ref<SectionDetail | null>(null)
const loading = ref(false)
const rubrics = ref<Rubric[]>([])

// Edit dialog
const editDialog = ref(false)
const editStep = ref(1)
const saving = ref(false)
const editForm = ref({ sectionName: '', startDate: '', endDate: '', rubricId: null as number | null })
const editErrors = ref<{ sectionName?: string; startDate?: string; endDate?: string }>({})

// Active weeks dialog
const weeksDialog = ref(false)
const weeksStep = ref(1)
const savingWeeks = ref(false)
const allWeeks = ref<{ value: string; label: string }[]>([])
const selectedWeeks = ref<string[]>([])

const snackbar = ref({ show: false, message: '', color: 'success' })

const selectedRubricName = computed(() =>
  rubrics.value.find(r => r.rubricId === editForm.value.rubricId)?.rubricName ?? null
)

const sortedActiveWeeks = computed(() =>
  [...(section.value?.activeWeeks ?? [])].sort()
)

const sortedSelectedWeeks = computed(() =>
  [...selectedWeeks.value].sort()
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
  } catch { /* leave empty */ }
}

// ── Edit section ──────────────────────────────────────────────────────────────

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

function validateEdit(): boolean {
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

function goToEditPreview() {
  if (validateEdit()) editStep.value = 2
}

async function confirmEdit() {
  saving.value = true
  try {
    const res = await updateSection(Number(route.params.id), {
      sectionName: editForm.value.sectionName,
      startDate: editForm.value.startDate || null,
      endDate: editForm.value.endDate || null,
      rubricId: editForm.value.rubricId,
    }) as any
    if (res.flag) {
      section.value = res.data
      snackbar.value = { show: true, message: 'Section updated successfully.', color: 'success' }
      editDialog.value = false
    } else {
      snackbar.value = { show: true, message: res.message, color: 'error' }
      editStep.value = 1
    }
  } catch (err: any) {
    snackbar.value = { show: true, message: err?.response?.data?.message || 'An error occurred.', color: 'error' }
    editStep.value = 1
  } finally {
    saving.value = false
  }
}

// ── Active weeks ──────────────────────────────────────────────────────────────

function generateWeeks(startDate: string, endDate: string): { value: string; label: string }[] {
  const weeks: { value: string; label: string }[] = []
  const start = new Date(startDate)
  const end = new Date(endDate)

  // Move to Monday of the start week
  const day = start.getDay()
  const diff = day === 0 ? -6 : 1 - day
  start.setDate(start.getDate() + diff)

  while (start <= end) {
    const isoWeek = getISOWeek(start)
    weeks.push({ value: isoWeek, label: formatWeekLabel(isoWeek) })
    start.setDate(start.getDate() + 7)
  }
  return weeks
}

function getISOWeek(date: Date): string {
  const d = new Date(date)
  d.setHours(0, 0, 0, 0)
  d.setDate(d.getDate() + 3 - ((d.getDay() + 6) % 7))
  const week1 = new Date(d.getFullYear(), 0, 4)
  const weekNum = 1 + Math.round(((d.getTime() - week1.getTime()) / 86400000 - 3 + ((week1.getDay() + 6) % 7)) / 7)
  return `${d.getFullYear()}-W${String(weekNum).padStart(2, '0')}`
}

function formatWeekLabel(isoWeek: string): string {
  const [year, w] = isoWeek.split('-W') as [string, string]
  return `Week ${parseInt(w)} (${year})`
}

function openWeeksDialog() {
  if (!section.value || !section.value.startDate || !section.value.endDate) return
  allWeeks.value = generateWeeks(section.value.startDate, section.value.endDate)
  selectedWeeks.value = [...(section.value.activeWeeks ?? [])]
  weeksStep.value = 1
  weeksDialog.value = true
}

function selectAll() {
  selectedWeeks.value = allWeeks.value.map(w => w.value)
}

function deselectAll() {
  selectedWeeks.value = []
}

async function confirmWeeks() {
  savingWeeks.value = true
  try {
    const res = await setupActiveWeeks(Number(route.params.id), selectedWeeks.value) as any
    if (res.flag) {
      section.value = res.data
      snackbar.value = { show: true, message: 'Active weeks saved successfully.', color: 'success' }
      weeksDialog.value = false
    } else {
      snackbar.value = { show: true, message: res.message, color: 'error' }
    }
  } catch (err: any) {
    snackbar.value = { show: true, message: err?.response?.data?.message || 'An error occurred.', color: 'error' }
  } finally {
    savingWeeks.value = false
  }
}
</script>
