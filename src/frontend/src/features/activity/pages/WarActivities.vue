<template>
  <v-container>
    <v-row class="mb-4" align="center">
      <v-col>
        <h2 class="text-h5 font-weight-bold">Weekly Activity Report</h2>
        <div class="text-body-2 text-medium-emphasis">
          Manage your weekly activities, review changes, then confirm updates to your WAR.
        </div>
      </v-col>
      <v-col cols="auto">
        <v-btn
          color="primary"
          prepend-icon="mdi-plus"
          :disabled="!workspace?.canManageActivities || !workspace?.selectedWeek"
          @click="openCreateDialog"
        >
          Add Activity
        </v-btn>
      </v-col>
    </v-row>

    <v-card variant="outlined" class="mb-4">
      <v-card-text class="pa-4">
        <v-row align="center">
          <v-col cols="12" md="4">
            <v-select
              v-model="selectedWeek"
              :items="workspace?.availableWeeks ?? []"
              item-title="label"
              item-value="week"
              label="Week"
              :loading="loading"
              :disabled="loading || !(workspace?.availableWeeks.length)"
              @update:model-value="handleWeekChange"
            />
          </v-col>
          <v-col cols="12" md="8" class="d-flex flex-wrap justify-md-end ga-2">
            <v-chip v-if="workspace?.sectionName" color="primary" variant="tonal">{{ workspace.sectionName }}</v-chip>
            <v-chip v-if="workspace?.teamName" color="secondary" variant="tonal">{{ workspace.teamName }}</v-chip>
            <v-chip v-if="selectedWeekOption" :color="selectedWeekOption.activeWeek ? 'success' : 'warning'" variant="tonal">
              {{ selectedWeekOption.activeWeek ? 'Active Week' : 'Inactive Week' }}
            </v-chip>
            <v-chip v-if="selectedWeek" variant="outlined">{{ getIsoWeekRangeLabel(selectedWeek) }}</v-chip>
          </v-col>
        </v-row>

        <v-alert v-if="workspace?.availabilityMessage" type="info" variant="tonal" class="mt-2">
          {{ workspace.availabilityMessage }}
        </v-alert>
      </v-card-text>
    </v-card>

    <v-row v-if="loading">
      <v-col class="text-center">
        <v-progress-circular indeterminate />
      </v-col>
    </v-row>

    <template v-else-if="workspace">
      <v-card variant="outlined">
        <v-card-title class="pa-4 pb-2 d-flex align-center">
          <span>My Activities</span>
          <v-spacer />
          <v-chip v-if="selectedWeek" variant="tonal" color="primary">
            {{ selectedWeekLabel }}
          </v-chip>
        </v-card-title>
        <v-card-text>
          <v-alert v-if="workspace.activities.length === 0" type="info" variant="tonal">
            No activities added for this week yet.
          </v-alert>

          <v-table v-else density="compact">
            <thead>
              <tr>
                <th>Category</th>
                <th>Planned Activity</th>
                <th>Description</th>
                <th>Planned Hours</th>
                <th>Actual Hours</th>
                <th>Status</th>
                <th class="text-right">Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="activity in workspace.activities" :key="activity.activityId">
                <td>
                  <v-chip
                    size="small"
                    variant="tonal"
                    :color="categoryChipColor(activity.category)"
                  >
                    {{ formatCategory(activity.category) }}
                  </v-chip>
                </td>
                <td>{{ activity.plannedActivity }}</td>
                <td class="description-cell">{{ activity.description }}</td>
                <td>{{ formatHours(activity.plannedHours) }}</td>
                <td>{{ formatHours(activity.actualHours) }}</td>
                <td>
                  <v-chip
                    size="small"
                    variant="tonal"
                    :color="statusChipColor(activity.status)"
                  >
                    {{ formatStatus(activity.status) }}
                  </v-chip>
                </td>
                <td class="text-right">
                  <v-btn size="small" variant="text" color="primary" @click="openEditDialog(activity)">Edit</v-btn>
                  <v-btn size="small" variant="text" color="error" @click="openDeleteDialog(activity)">Delete</v-btn>
                </td>
              </tr>
            </tbody>
          </v-table>
        </v-card-text>
      </v-card>
    </template>

    <v-dialog v-model="formDialog" max-width="760" persistent>
      <v-card>
        <v-card-title class="pa-4">
          {{ formMode === 'create' ? (formStep === 1 ? 'Add Activity' : 'Confirm Activity') : (formStep === 1 ? 'Edit Activity' : 'Confirm Activity Change') }}
        </v-card-title>
        <v-divider />

        <v-card-text v-if="formStep === 1" class="pa-4">
          <v-alert type="info" variant="tonal" class="mb-4">
            {{ selectedWeek ? `Selected week: ${selectedWeekLabel}` : 'Select a week before managing activities.' }}
          </v-alert>

          <v-select
            v-model="activityForm.category"
            :items="categoryOptions"
            item-title="label"
            item-value="value"
            label="Activity Category"
            :error-messages="formErrors.category"
            class="mb-4"
          />

          <v-text-field
            v-model="activityForm.plannedActivity"
            label="Planned Activity"
            :error-messages="formErrors.plannedActivity"
            class="mb-4"
          />

          <v-textarea
            v-model="activityForm.description"
            label="Activity Description"
            rows="4"
            :error-messages="formErrors.description"
            class="mb-4"
          />

          <v-row>
            <v-col cols="12" md="6">
              <v-text-field
                v-model="activityForm.plannedHours"
                label="Planned Hours"
                type="number"
                min="0"
                step="0.25"
                :error-messages="formErrors.plannedHours"
              />
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field
                v-model="activityForm.actualHours"
                label="Actual Hours"
                type="number"
                min="0"
                step="0.25"
                :error-messages="formErrors.actualHours"
              />
            </v-col>
          </v-row>

          <v-select
            v-model="activityForm.status"
            :items="statusOptions"
            item-title="label"
            item-value="value"
            label="Status"
            :error-messages="formErrors.status"
          />
        </v-card-text>

        <v-card-text v-else class="pa-4">
          <v-alert type="info" variant="tonal" class="mb-4">
            Please review the activity details before confirming.
          </v-alert>
          <v-table density="compact">
            <tbody>
              <tr>
                <th class="text-left">Week</th>
                <td>{{ selectedWeekLabel }}</td>
              </tr>
              <tr>
                <th class="text-left">Section</th>
                <td>{{ workspace?.sectionName ?? '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Team</th>
                <td>{{ workspace?.teamName ?? '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Category</th>
                <td>{{ activityForm.category ? formatCategory(activityForm.category) : '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Planned Activity</th>
                <td>{{ activityForm.plannedActivity || '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Description</th>
                <td>{{ activityForm.description || '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Planned Hours</th>
                <td>{{ activityForm.plannedHours || '0' }}</td>
              </tr>
              <tr>
                <th class="text-left">Actual Hours</th>
                <td>{{ activityForm.actualHours || '0' }}</td>
              </tr>
              <tr>
                <th class="text-left">Status</th>
                <td>{{ activityForm.status ? formatStatus(activityForm.status) : '—' }}</td>
              </tr>
            </tbody>
          </v-table>
        </v-card-text>

        <v-divider />
        <v-card-actions class="pa-4">
          <v-btn variant="text" @click="closeFormDialog">Cancel</v-btn>
          <v-spacer />
          <v-btn v-if="formStep === 2" variant="outlined" @click="formStep = 1">Modify</v-btn>
          <v-btn color="primary" :loading="saving" @click="formStep === 1 ? goToFormPreview() : submitForm()">
            {{ formStep === 1 ? 'Preview' : 'Confirm' }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="deleteDialog" max-width="680" persistent>
      <v-card>
        <v-card-title class="pa-4">Delete Activity</v-card-title>
        <v-divider />
        <v-card-text class="pa-4">
          <v-alert type="warning" variant="tonal" class="mb-4">
            This activity will be removed from your WAR for the selected week.
          </v-alert>
          <v-table density="compact">
            <tbody>
              <tr>
                <th class="text-left">Week</th>
                <td>{{ selectedWeekLabel }}</td>
              </tr>
              <tr>
                <th class="text-left">Category</th>
                <td>{{ deleteTarget ? formatCategory(deleteTarget.category) : '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Planned Activity</th>
                <td>{{ deleteTarget?.plannedActivity ?? '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Status</th>
                <td>{{ deleteTarget ? formatStatus(deleteTarget.status) : '—' }}</td>
              </tr>
            </tbody>
          </v-table>
        </v-card-text>
        <v-divider />
        <v-card-actions class="pa-4">
          <v-btn variant="text" @click="closeDeleteDialog">Cancel</v-btn>
          <v-spacer />
          <v-btn color="error" :loading="saving" @click="confirmDelete">Confirm</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-snackbar v-model="snackbar.show" :color="snackbar.color" timeout="3000">
      {{ snackbar.message }}
    </v-snackbar>
  </v-container>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { createActivity, deleteActivity, getActivityWorkspace, updateActivity } from '../services/activityService'
import type {
  ActivityCategory,
  ActivityDetail,
  ActivityStatus,
  ActivityWeekOption,
  ActivityWorkspace,
  CreateActivityRequest,
  UpdateActivityRequest
} from '../services/activityTypes'
import { formatIsoWeekLabel, getIsoWeekRangeLabel } from '@/shared/utils/week'

type FormMode = 'create' | 'edit'

interface ActivityFormState {
  category: ActivityCategory | null
  plannedActivity: string
  description: string
  plannedHours: string
  actualHours: string
  status: ActivityStatus | null
}

const loading = ref(false)
const saving = ref(false)
const workspace = ref<ActivityWorkspace | null>(null)
const selectedWeek = ref<string | null>(null)

const formDialog = ref(false)
const formStep = ref(1)
const formMode = ref<FormMode>('create')
const editTarget = ref<ActivityDetail | null>(null)
const deleteDialog = ref(false)
const deleteTarget = ref<ActivityDetail | null>(null)

const activityForm = ref<ActivityFormState>(defaultForm())
const formErrors = ref<Record<string, string[]>>({})

const snackbar = ref({ show: false, message: '', color: 'success' })

const categoryOptions = [
  { label: 'Development', value: 'DEVELOPMENT' },
  { label: 'Testing', value: 'TESTING' },
  { label: 'Bugfix', value: 'BUGFIX' },
  { label: 'Communication', value: 'COMMUNICATION' },
  { label: 'Documentation', value: 'DOCUMENTATION' },
  { label: 'Design', value: 'DESIGN' },
  { label: 'Planning', value: 'PLANNING' },
  { label: 'Learning', value: 'LEARNING' },
  { label: 'Deployment', value: 'DEPLOYMENT' },
  { label: 'Support', value: 'SUPPORT' },
  { label: 'Miscellaneous', value: 'MISCELLANEOUS' }
] as const

const statusOptions = [
  { label: 'In progress', value: 'IN_PROGRESS' },
  { label: 'Under testing', value: 'UNDER_TESTING' },
  { label: 'Done', value: 'DONE' }
] as const

const selectedWeekOption = computed<ActivityWeekOption | null>(() =>
  workspace.value?.availableWeeks.find((option) => option.week === selectedWeek.value) ?? null
)

const selectedWeekLabel = computed(() =>
  selectedWeekOption.value?.label ?? (selectedWeek.value ? formatIsoWeekLabel(selectedWeek.value) : '—')
)

onMounted(() => {
  loadWorkspace()
})

async function loadWorkspace(week?: string) {
  loading.value = true
  try {
    const response = await getActivityWorkspace(week) as any
    workspace.value = response.flag ? response.data : null
    selectedWeek.value = response.flag ? response.data?.selectedWeek ?? null : null
  } catch (err: any) {
    snackbar.value = {
      show: true,
      message: err?.response?.data?.message || 'An error occurred.',
      color: 'error'
    }
  } finally {
    loading.value = false
  }
}

async function handleWeekChange() {
  await loadWorkspace(selectedWeek.value ?? undefined)
}

function openCreateDialog() {
  formMode.value = 'create'
  editTarget.value = null
  activityForm.value = defaultForm()
  formErrors.value = {}
  formStep.value = 1
  formDialog.value = true
}

function openEditDialog(activity: ActivityDetail) {
  formMode.value = 'edit'
  editTarget.value = activity
  activityForm.value = {
    category: activity.category,
    plannedActivity: activity.plannedActivity,
    description: activity.description,
    plannedHours: String(activity.plannedHours),
    actualHours: String(activity.actualHours),
    status: activity.status
  }
  formErrors.value = {}
  formStep.value = 1
  formDialog.value = true
}

function closeFormDialog() {
  formDialog.value = false
  formStep.value = 1
  editTarget.value = null
  activityForm.value = defaultForm()
  formErrors.value = {}
}

function openDeleteDialog(activity: ActivityDetail) {
  deleteTarget.value = activity
  deleteDialog.value = true
}

function closeDeleteDialog() {
  deleteDialog.value = false
  deleteTarget.value = null
}

function goToFormPreview() {
  if (!validateForm()) {
    return
  }
  formStep.value = 2
}

async function submitForm() {
  if (!selectedWeek.value) {
    snackbar.value = { show: true, message: 'Week is required.', color: 'error' }
    return
  }

  saving.value = true
  try {
    if (formMode.value === 'create') {
      const payload: CreateActivityRequest = {
        week: selectedWeek.value,
        category: activityForm.value.category,
        plannedActivity: activityForm.value.plannedActivity.trim(),
        description: activityForm.value.description.trim(),
        plannedHours: parseNumber(activityForm.value.plannedHours),
        actualHours: parseNumber(activityForm.value.actualHours),
        status: activityForm.value.status
      }
      await createActivity(payload)
    } else if (editTarget.value) {
      const payload: UpdateActivityRequest = {
        category: activityForm.value.category,
        plannedActivity: activityForm.value.plannedActivity.trim(),
        description: activityForm.value.description.trim(),
        plannedHours: parseNumber(activityForm.value.plannedHours),
        actualHours: parseNumber(activityForm.value.actualHours),
        status: activityForm.value.status
      }
      await updateActivity(editTarget.value.activityId, payload)
    }

    closeFormDialog()
    await loadWorkspace(selectedWeek.value)
    snackbar.value = { show: true, message: 'WAR has been updated.', color: 'success' }
  } catch (err: any) {
    snackbar.value = {
      show: true,
      message: err?.response?.data?.message || 'An error occurred.',
      color: 'error'
    }
    formStep.value = 1
  } finally {
    saving.value = false
  }
}

async function confirmDelete() {
  if (!deleteTarget.value) {
    return
  }

  saving.value = true
  try {
    await deleteActivity(deleteTarget.value.activityId)
    closeDeleteDialog()
    await loadWorkspace(selectedWeek.value ?? undefined)
    snackbar.value = { show: true, message: 'WAR has been updated.', color: 'success' }
  } catch (err: any) {
    snackbar.value = {
      show: true,
      message: err?.response?.data?.message || 'An error occurred.',
      color: 'error'
    }
  } finally {
    saving.value = false
  }
}

function validateForm() {
  const errors: Record<string, string[]> = {}

  if (!activityForm.value.category) {
    errors.category = ['Activity category is required.']
  }
  if (!activityForm.value.plannedActivity.trim()) {
    errors.plannedActivity = ['Planned activity is required.']
  }
  if (activityForm.value.plannedActivity.trim().length > 255) {
    errors.plannedActivity = ['Planned activity must be 255 characters or fewer.']
  }
  if (!activityForm.value.description.trim()) {
    errors.description = ['Activity description is required.']
  }
  if (activityForm.value.description.trim().length > 2000) {
    errors.description = ['Activity description must be 2000 characters or fewer.']
  }

  validateHoursField(activityForm.value.plannedHours, 'plannedHours', 'Planned hours', errors)
  validateHoursField(activityForm.value.actualHours, 'actualHours', 'Actual hours', errors)

  if (!activityForm.value.status) {
    errors.status = ['Status is required.']
  }

  formErrors.value = errors
  return Object.keys(errors).length === 0
}

function validateHoursField(
  value: string,
  key: string,
  label: string,
  errors: Record<string, string[]>
) {
  if (value === '') {
    errors[key] = [`${label} are required.`]
    return
  }

  const parsed = Number(value)
  if (Number.isNaN(parsed)) {
    errors[key] = [`${label} must be a valid number.`]
    return
  }
  if (parsed < 0) {
    errors[key] = [`${label} must be zero or greater.`]
    return
  }
  if (parsed > 999.99) {
    errors[key] = [`${label} must be 999.99 or fewer.`]
  }
}

function parseNumber(value: string): number | null {
  if (value === '') return null
  const parsed = Number(value)
  return Number.isNaN(parsed) ? null : parsed
}

function defaultForm(): ActivityFormState {
  return {
    category: null,
    plannedActivity: '',
    description: '',
    plannedHours: '',
    actualHours: '',
    status: null
  }
}

function formatCategory(category: ActivityCategory) {
  return categoryOptions.find((option) => option.value === category)?.label ?? category
}

function formatStatus(status: ActivityStatus) {
  return statusOptions.find((option) => option.value === status)?.label ?? status
}

function categoryChipColor(category: ActivityCategory) {
  const colors: Record<ActivityCategory, string> = {
    DEVELOPMENT: 'primary',
    TESTING: 'info',
    BUGFIX: 'warning',
    COMMUNICATION: 'teal',
    DOCUMENTATION: 'indigo',
    DESIGN: 'deep-purple',
    PLANNING: 'orange-darken-2',
    LEARNING: 'cyan-darken-2',
    DEPLOYMENT: 'blue-grey-darken-1',
    SUPPORT: 'brown',
    MISCELLANEOUS: 'secondary'
  }

  return colors[category]
}

function statusChipColor(status: ActivityStatus) {
  const colors: Record<ActivityStatus, string> = {
    IN_PROGRESS: 'primary',
    UNDER_TESTING: 'warning',
    DONE: 'success'
  }

  return colors[status]
}

function formatHours(value: number) {
  return Number(value).toString()
}
</script>

<style scoped lang="scss">
.description-cell {
  max-width: 340px;
  white-space: normal;
}
</style>
