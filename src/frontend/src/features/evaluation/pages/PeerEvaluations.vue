<template>
  <v-container>
    <v-row class="mb-4" align="center">
      <v-col>
        <h2 class="text-h5 font-weight-bold">Peer Evaluations</h2>
        <div class="text-body-2 text-medium-emphasis">
          Complete one peer evaluation for the previous week for every teammate, including yourself.
        </div>
      </v-col>
      <v-col cols="auto" v-if="workspace && isEditableWorkspace">
        <v-btn color="primary" :loading="saving" @click="openPreviewDialog">
          {{ workspace.submitted ? 'Preview Update' : 'Preview Submission' }}
        </v-btn>
      </v-col>
    </v-row>

    <v-card variant="outlined" class="mb-4">
      <v-card-text class="pa-4">
        <v-row>
          <v-col cols="12" md="8" class="d-flex flex-wrap ga-2">
            <v-chip v-if="workspace?.sectionName" color="primary" variant="tonal">
              {{ workspace.sectionName }}
            </v-chip>
            <v-chip v-if="workspace?.teamName" color="secondary" variant="tonal">
              {{ workspace.teamName }}
            </v-chip>
            <v-chip v-if="workspace?.weekLabel" color="success" variant="tonal">
              {{ workspace.weekLabel }}
            </v-chip>
            <v-chip v-if="workspace?.weekRangeLabel" variant="outlined">
              {{ workspace.weekRangeLabel }}
            </v-chip>
            <v-chip
              v-if="workspace"
              :color="isEditableWorkspace ? 'success' : 'warning'"
              variant="tonal"
            >
              {{ isEditableWorkspace ? 'Editable' : 'Read only' }}
            </v-chip>
          </v-col>
          <v-col cols="12" md="4" class="text-md-right">
            <div class="text-caption text-medium-emphasis">Submission deadline</div>
            <div class="text-body-2 font-weight-medium">{{ dueAtLabel }}</div>
          </v-col>
        </v-row>

        <v-alert v-if="workspace?.availabilityMessage" type="info" variant="tonal" class="mt-4">
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
      <v-alert
        v-if="!workspace.members.length"
        type="info"
        variant="tonal"
      >
        No peer-evaluation draft is available for the selected week.
      </v-alert>

      <v-row v-else>
        <v-col
          v-for="member in evaluationForm.evaluations"
          :key="member.studentId"
          cols="12"
        >
          <v-card variant="outlined">
            <v-card-title class="pa-4 pb-2 d-flex flex-wrap align-center ga-2">
              <span>{{ member.fullName }}</span>
              <v-chip v-if="member.self" size="small" color="primary" variant="tonal">Self</v-chip>
            </v-card-title>

            <v-card-text class="pt-0">
              <v-row>
                <v-col
                  v-for="criterion in workspace.criteria"
                  :key="criterion.criterionId"
                  cols="12"
                  md="6"
                >
                  <v-text-field
                    v-if="isEditableWorkspace"
                    :model-value="getScoreValue(member.studentId, criterion.criterionId)"
                    :label="criterionLabel(criterion)"
                    type="number"
                    min="1"
                    :max="criterion.maxScore"
                    step="1"
                    :hint="criterion.description || `Score from 1 to ${criterion.maxScore}`"
                    persistent-hint
                    :error-messages="errorMessages(scoreErrorKey(member.studentId, criterion.criterionId))"
                    @update:model-value="setScoreValue(member.studentId, criterion.criterionId, $event)"
                  />
                  <div v-else class="readonly-field">
                    <div class="text-caption text-medium-emphasis">{{ criterionLabel(criterion) }}</div>
                    <div class="text-body-1 font-weight-medium">
                      {{ displayScore(member.studentId, criterion.criterionId) }}
                    </div>
                    <div v-if="criterion.description" class="text-caption text-medium-emphasis">
                      {{ criterion.description }}
                    </div>
                  </div>
                </v-col>
              </v-row>

              <v-textarea
                v-if="isEditableWorkspace"
                v-model="member.publicComment"
                label="Public Comments"
                rows="3"
                counter="2000"
                :error-messages="errorMessages(publicCommentErrorKey(member.studentId))"
                class="mt-2"
              />
              <div v-else class="readonly-field mt-3">
                <div class="text-caption text-medium-emphasis">Public Comments</div>
                <div class="text-body-2 preserve-lines">{{ member.publicComment?.trim() || 'No public comments.' }}</div>
              </div>

              <v-textarea
                v-if="isEditableWorkspace"
                v-model="member.privateComment"
                label="Private Comments"
                rows="3"
                counter="2000"
                :error-messages="errorMessages(privateCommentErrorKey(member.studentId))"
                class="mt-2"
              />
              <div v-else class="readonly-field mt-3">
                <div class="text-caption text-medium-emphasis">Private Comments</div>
                <div class="text-body-2 preserve-lines">{{ member.privateComment?.trim() || 'No private comments.' }}</div>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </template>

    <v-dialog v-model="previewDialog" max-width="960" persistent scrollable>
      <v-card v-if="workspace">
        <v-card-title class="pa-4">
          {{ workspace.submitted ? 'Confirm Peer Evaluation Update' : 'Confirm Peer Evaluation Submission' }}
        </v-card-title>
        <v-divider />

        <v-card-text class="pa-4">
          <v-alert type="info" variant="tonal" class="mb-4">
            Review every score and comment before confirming.
          </v-alert>

          <v-table density="compact" class="mb-4">
            <tbody>
              <tr>
                <th class="text-left">Section</th>
                <td>{{ workspace.sectionName || '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Team</th>
                <td>{{ workspace.teamName || '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Week</th>
                <td>{{ workspace.weekRangeLabel || workspace.weekLabel || '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Deadline</th>
                <td>{{ dueAtLabel }}</td>
              </tr>
            </tbody>
          </v-table>

          <v-card
            v-for="member in evaluationForm.evaluations"
            :key="`preview-${member.studentId}`"
            variant="outlined"
            class="mb-4"
          >
            <v-card-title class="pa-4 pb-2 d-flex flex-wrap align-center ga-2">
              <span>{{ member.fullName }}</span>
              <v-chip v-if="member.self" size="small" color="primary" variant="tonal">Self</v-chip>
            </v-card-title>
            <v-card-text class="pt-0">
              <v-table density="compact" class="mb-3">
                <tbody>
                  <tr v-for="criterion in workspace.criteria" :key="`preview-score-${member.studentId}-${criterion.criterionId}`">
                    <th class="text-left">{{ criterion.criterion }}</th>
                    <td>{{ scoreField(member.studentId, criterion.criterionId).score || '—' }}</td>
                  </tr>
                </tbody>
              </v-table>

              <div class="readonly-field mb-3">
                <div class="text-caption text-medium-emphasis">Public Comments</div>
                <div class="text-body-2 preserve-lines">{{ member.publicComment.trim() || 'No public comments.' }}</div>
              </div>

              <div class="readonly-field">
                <div class="text-caption text-medium-emphasis">Private Comments</div>
                <div class="text-body-2 preserve-lines">{{ member.privateComment.trim() || 'No private comments.' }}</div>
              </div>
            </v-card-text>
          </v-card>
        </v-card-text>

        <v-divider />
        <v-card-actions class="pa-4">
          <v-btn variant="text" @click="previewDialog = false">Cancel</v-btn>
          <v-spacer />
          <v-btn variant="outlined" @click="previewDialog = false">Modify</v-btn>
          <v-btn color="primary" :loading="saving" @click="submitEvaluation">
            {{ workspace.submitted ? 'Confirm Update' : 'Confirm Submission' }}
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
import { computed, onMounted, ref } from 'vue'
import { createPeerEvaluation, getPeerEvaluationWorkspace, updatePeerEvaluation } from '../services/evaluationService'
import type {
  EvaluationCriterion,
  EvaluationWorkspace,
  PeerEvaluationSubmissionRequest
} from '../services/evaluationTypes'

interface EvaluationCriterionFormState {
  criterionId: number
  score: string
}

interface EvaluationMemberFormState {
  studentId: number
  fullName: string
  self: boolean
  publicComment: string
  privateComment: string
  scores: EvaluationCriterionFormState[]
}

interface EvaluationFormState {
  evaluations: EvaluationMemberFormState[]
}

const loading = ref(false)
const saving = ref(false)
const previewDialog = ref(false)
const workspace = ref<EvaluationWorkspace | null>(null)
const evaluationForm = ref<EvaluationFormState>({ evaluations: [] })
const formErrors = ref<Record<string, string[]>>({})
const snackbar = ref({ show: false, message: '', color: 'success' })

const isEditableWorkspace = computed(() =>
  Boolean(workspace.value?.editable && workspace.value?.canSubmit && workspace.value?.members.length)
)

const dueAtLabel = computed(() => formatDueAt(workspace.value?.dueAt ?? null))

onMounted(() => {
  loadWorkspace()
})

async function loadWorkspace() {
  loading.value = true
  try {
    const response = await getPeerEvaluationWorkspace()
    workspace.value = response.flag ? response.data : null
    syncFormWithWorkspace()
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

function syncFormWithWorkspace() {
  if (!workspace.value) {
    evaluationForm.value = { evaluations: [] }
    return
  }

  evaluationForm.value = {
    evaluations: workspace.value.members.map((member) => ({
      studentId: member.studentId,
      fullName: member.fullName,
      self: member.self,
      publicComment: member.publicComment ?? '',
      privateComment: member.privateComment ?? '',
      scores: workspace.value?.criteria.map((criterion) => ({
        criterionId: criterion.criterionId,
        score: String(member.scores.find((score) => score.criterionId === criterion.criterionId)?.score ?? '')
      })) ?? []
    }))
  }
  formErrors.value = {}
}

function scoreField(studentId: number, criterionId: number) {
  return evaluationForm.value.evaluations
    .find((member) => member.studentId === studentId)
    ?.scores.find((score) => score.criterionId === criterionId) ?? { criterionId, score: '' }
}

function getScoreValue(studentId: number, criterionId: number) {
  return scoreField(studentId, criterionId).score
}

function setScoreValue(studentId: number, criterionId: number, value: string | number | null) {
  const member = evaluationForm.value.evaluations.find((item) => item.studentId === studentId)
  const score = member?.scores.find((item) => item.criterionId === criterionId)

  if (score) {
    score.score = value == null ? '' : String(value)
  }
}

function openPreviewDialog() {
  if (!validateForm()) {
    return
  }
  previewDialog.value = true
}

async function submitEvaluation() {
  if (!workspace.value) {
    return
  }

  saving.value = true
  const wasSubmitted = workspace.value.submitted
  try {
    const payload = buildPayload()
    const response = workspace.value.submitted && workspace.value.submissionId
      ? await updatePeerEvaluation(workspace.value.submissionId, payload)
      : await createPeerEvaluation(payload)

    previewDialog.value = false
    applySavedSubmission(response.data.submissionId)
    snackbar.value = {
      show: true,
      message: response.message || (wasSubmitted ? 'Peer evaluation has been updated.' : 'Peer evaluation has been submitted.'),
      color: 'success'
    }
  } catch (err: any) {
    previewDialog.value = false
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
  const currentWorkspace = workspace.value
  const errors: Record<string, string[]> = {}

  if (!currentWorkspace) {
    snackbar.value = { show: true, message: 'Peer evaluation workspace is unavailable.', color: 'error' }
    return false
  }

  if (evaluationForm.value.evaluations.length !== currentWorkspace.members.length) {
    snackbar.value = { show: true, message: 'Every team member must be evaluated exactly once.', color: 'error' }
    return false
  }

  for (const member of evaluationForm.value.evaluations) {
    for (const criterion of currentWorkspace.criteria) {
      const scoreValue = scoreField(member.studentId, criterion.criterionId).score.trim()
      const key = scoreErrorKey(member.studentId, criterion.criterionId)

      if (!scoreValue) {
        errors[key] = ['A score is required.']
        continue
      }
      if (!/^-?\d+$/.test(scoreValue)) {
        errors[key] = ['Scores must be integers.']
        continue
      }

      const numericScore = Number(scoreValue)
      if (numericScore < 1 || numericScore > criterion.maxScore) {
        errors[key] = [`Score must be between 1 and ${criterion.maxScore}.`]
      }
    }

    if (member.publicComment.trim().length > 2000) {
      errors[publicCommentErrorKey(member.studentId)] = ['Public comments must be 2000 characters or fewer.']
    }
    if (member.privateComment.trim().length > 2000) {
      errors[privateCommentErrorKey(member.studentId)] = ['Private comments must be 2000 characters or fewer.']
    }
  }

  formErrors.value = errors
  if (Object.keys(errors).length > 0) {
    snackbar.value = { show: true, message: 'Fix the highlighted evaluation fields before continuing.', color: 'error' }
    return false
  }
  return true
}

function buildPayload(): PeerEvaluationSubmissionRequest {
  return {
    evaluations: evaluationForm.value.evaluations.map((member) => ({
      evaluateeStudentId: member.studentId,
      publicComment: normalizeComment(member.publicComment),
      privateComment: normalizeComment(member.privateComment),
      criterionScores: member.scores.map((score) => ({
        criterionId: score.criterionId,
        score: Number(score.score)
      }))
    }))
  }
}

function applySavedSubmission(submissionId: number) {
  if (!workspace.value) {
    return
  }

  workspace.value = {
    ...workspace.value,
    submissionId,
    submitted: true
  }
}

function normalizeComment(value: string) {
  const normalized = value.trim()
  return normalized ? normalized : null
}

function displayScore(studentId: number, criterionId: number) {
  return scoreField(studentId, criterionId).score || '—'
}

function criterionLabel(criterion: EvaluationCriterion) {
  return `${criterion.criterion} (1-${criterion.maxScore})`
}

function formatDueAt(value: string | null) {
  if (!value) {
    return '—'
  }

  const parsed = new Date(value)
  if (Number.isNaN(parsed.getTime())) {
    return value
  }

  return new Intl.DateTimeFormat(undefined, {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: 'numeric',
    minute: '2-digit'
  }).format(parsed)
}

function scoreErrorKey(studentId: number, criterionId: number) {
  return `score-${studentId}-${criterionId}`
}

function publicCommentErrorKey(studentId: number) {
  return `public-comment-${studentId}`
}

function privateCommentErrorKey(studentId: number) {
  return `private-comment-${studentId}`
}

function errorMessages(key: string) {
  return formErrors.value[key] ?? []
}
</script>

<style scoped lang="scss">
.readonly-field {
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 12px;
  padding: 12px 14px;
  background: rgba(0, 0, 0, 0.02);
}

.preserve-lines {
  white-space: pre-line;
}
</style>
