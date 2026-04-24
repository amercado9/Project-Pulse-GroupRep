<template>
  <v-container>
    <v-row class="mb-4" align="center">
      <v-col>
        <h2 class="text-h5 font-weight-bold">My Peer Evaluation Report</h2>
        <div class="text-body-2 text-medium-emphasis">
          Review your averaged peer evaluation scores, public comments, and overall grade for an active week.
        </div>
      </v-col>
    </v-row>

    <v-card variant="outlined" class="mb-4">
      <v-card-text class="pa-4">
        <v-row align="center" class="ga-0">
          <v-col cols="12" md="4">
            <v-select
              v-model="selectedWeek"
              :items="weekOptions"
              item-title="title"
              item-value="value"
              label="Active Week"
              :disabled="loading || !report?.availableWeeks.length"
            />
          </v-col>
          <v-col cols="12" md="8" class="text-md-right">
            <v-btn color="primary" :loading="loading" :disabled="!selectedWeek" @click="loadReport(selectedWeek, true)">
              Generate Report
            </v-btn>
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>

    <v-row v-if="loading">
      <v-col class="text-center">
        <v-progress-circular indeterminate />
      </v-col>
    </v-row>

    <template v-else-if="report">
      <v-card variant="outlined" class="mb-4">
        <v-card-text class="pa-4">
          <v-row>
            <v-col cols="12" md="8" class="d-flex flex-wrap ga-2">
              <v-chip v-if="report.sectionName" color="primary" variant="tonal">
                {{ report.sectionName }}
              </v-chip>
              <v-chip v-if="report.teamName" color="secondary" variant="tonal">
                {{ report.teamName }}
              </v-chip>
              <v-chip color="success" variant="tonal">
                {{ report.selectedWeekLabel }}
              </v-chip>
              <v-chip variant="outlined">
                {{ report.selectedWeekRangeLabel }}
              </v-chip>
            </v-col>
            <v-col cols="12" md="4" class="text-md-right">
              <div class="text-caption text-medium-emphasis">Student</div>
              <div class="text-body-1 font-weight-medium">{{ report.studentName }}</div>
            </v-col>
          </v-row>
        </v-card-text>
      </v-card>

      <v-alert
        v-if="!report.reportAvailable"
        type="info"
        variant="tonal"
        class="mb-4"
      >
        {{ report.availabilityMessage || 'No peer evaluation data is available for the selected week.' }}
      </v-alert>

      <template v-else>
        <v-card variant="outlined" class="mb-4">
          <v-card-text class="pa-4">
            <div class="text-caption text-medium-emphasis mb-1">Overall Grade</div>
            <div class="text-h5 font-weight-bold">
              {{ overallGradeLabel }}
            </div>
          </v-card-text>
        </v-card>

        <v-card variant="outlined" class="mb-4">
          <v-card-title class="pa-4 pb-2">Criterion Averages</v-card-title>
          <v-card-text class="pt-0">
            <v-row>
              <v-col
                v-for="criterion in report.criterionAverages"
                :key="criterion.criterionId"
                cols="12"
                md="6"
              >
                <v-card variant="tonal" color="surface" class="criterion-card">
                  <v-card-text class="criterion-card__body">
                    <div class="criterion-card__title text-subtitle-1 font-weight-medium">{{ criterion.criterion }}</div>
                    <div v-if="criterion.description" class="criterion-card__description text-body-2 mb-3">
                      {{ criterion.description }}
                    </div>
                    <div class="criterion-card__score text-h6">
                      {{ formatDecimal(criterion.averageScore) }} / {{ formatMaxScore(criterion.maxScore) }}
                    </div>
                  </v-card-text>
                </v-card>
              </v-col>
            </v-row>
          </v-card-text>
        </v-card>

        <v-card variant="outlined">
          <v-card-title class="pa-4 pb-2">Public Comments</v-card-title>
          <v-card-text class="pt-0">
            <v-list v-if="report.publicComments.length" lines="two">
              <v-list-item
                v-for="(comment, index) in report.publicComments"
                :key="`${report.selectedWeek}-${index}`"
              >
                <template #prepend>
                  <v-icon icon="mdi-comment-text-outline" />
                </template>
                <v-list-item-title class="preserve-lines">{{ comment }}</v-list-item-title>
              </v-list-item>
            </v-list>
            <div v-else class="text-body-2 text-medium-emphasis">
              No public comments were provided for this week.
            </div>
          </v-card-text>
        </v-card>
      </template>
    </template>

    <v-snackbar v-model="snackbar.show" :color="snackbar.color" timeout="3000">
      {{ snackbar.message }}
    </v-snackbar>
  </v-container>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getMyPeerEvaluationReport } from '../services/reportService'
import type { StudentPeerEvaluationReportResponse } from '../services/reportTypes'

const loading = ref(false)
const selectedWeek = ref<string>('')
const report = ref<StudentPeerEvaluationReportResponse | null>(null)
const snackbar = ref({ show: false, message: '', color: 'success' })

const weekOptions = computed(() =>
  (report.value?.availableWeeks ?? []).map((week) => ({
    title: `${week.label} — ${week.rangeLabel}`,
    value: week.week
  }))
)

const overallGradeLabel = computed(() => {
  if (!report.value || report.value.overallGrade == null || report.value.maxTotalScore == null) {
    return '—'
  }
  return `${formatDecimal(report.value.overallGrade)} / ${report.value.maxTotalScore}`
})

onMounted(async () => {
  await loadReport(undefined, false)
})

async function loadReport(week?: string, userInitiated = false) {
  loading.value = true
  try {
    const response = await getMyPeerEvaluationReport(week)
    report.value = response.flag ? response.data : null
    if (report.value) {
      selectedWeek.value = report.value.selectedWeek
      if (userInitiated) {
        snackbar.value = {
          show: true,
          message: report.value.reportAvailable
            ? 'Report refreshed.'
            : (report.value.availabilityMessage || 'No peer evaluation data is available for the selected week.'),
          color: report.value.reportAvailable ? 'success' : 'info'
        }
      }
    }
  } catch (err: any) {
    snackbar.value = {
      show: true,
      message: err?.response?.data?.message || 'An error occurred generating the report.',
      color: 'error'
    }
  } finally {
    loading.value = false
  }
}

function formatDecimal(value: number) {
  return Number(value).toString()
}

function formatMaxScore(value: number) {
  return Number.isInteger(value) ? String(value) : Number(value).toString()
}
</script>

<style scoped>
.preserve-lines {
  white-space: pre-line;
}

.criterion-card {
  background: #f3f6fb !important;
}

.criterion-card__body {
  color: #1f2937;
}

.criterion-card__title {
  color: #1f2937;
}

.criterion-card__description {
  color: #4b5563;
}

.criterion-card__score {
  color: #0f172a;
  font-weight: 700;
}
</style>
