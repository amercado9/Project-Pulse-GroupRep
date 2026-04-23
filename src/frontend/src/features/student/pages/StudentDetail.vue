<template>
  <v-container>
    <v-row v-if="loading">
      <v-col class="text-center"><v-progress-circular indeterminate /></v-col>
    </v-row>

    <template v-else-if="student">
      <v-row class="mb-4" align="center">
        <v-col>
          <h2 class="text-h5 font-weight-bold">{{ student.firstName }} {{ student.lastName }}</h2>
          <div class="text-body-1 text-medium-emphasis">{{ student.email }}</div>
        </v-col>
        <v-col cols="auto">
          <v-btn variant="text" prepend-icon="mdi-arrow-left" @click="router.back()">Back</v-btn>
        </v-col>
      </v-row>

      <v-row>
        <v-col cols="12" md="6">
          <v-card variant="outlined">
            <v-card-title class="pa-4 text-subtitle-1 font-weight-bold">Section &amp; Team</v-card-title>
            <v-divider />
            <v-card-text class="pa-4">
              <div v-if="student.sectionName" class="mb-2">
                <span class="font-weight-medium">Section:</span> {{ student.sectionName }}
              </div>
              <div v-if="student.teamNames.length">
                <span class="font-weight-medium">Team(s):</span>
                <v-chip
                  v-for="name in student.teamNames"
                  :key="name"
                  size="small"
                  variant="tonal"
                  class="ml-2 mb-1"
                >{{ name }}</v-chip>
              </div>
              <div v-if="!student.sectionName && !student.teamNames.length" class="text-medium-emphasis">
                Not assigned to a section or team.
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </template>

    <v-row v-else-if="notFound">
      <v-col>
        <v-alert type="error" variant="tonal">Student not found.</v-alert>
        <v-btn class="mt-4" variant="text" prepend-icon="mdi-arrow-left" @click="router.back()">Back</v-btn>
      </v-col>
    </v-row>

    <v-snackbar v-model="snackbar.show" :color="snackbar.color" timeout="3000">
      {{ snackbar.message }}
    </v-snackbar>
  </v-container>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { findStudents } from '../services/studentService'
import type { StudentSummary } from '../services/studentTypes'

const route = useRoute()
const router = useRouter()

const student = ref<StudentSummary | null>(null)
const loading = ref(false)
const notFound = ref(false)
const snackbar = ref({ show: false, message: '', color: 'error' })

onMounted(async () => {
  const id = Number(route.params.id)
  loading.value = true
  try {
    const res = await findStudents() as any
    const found = (res.data ?? []).find((s: StudentSummary) => s.id === id)
    if (found) {
      student.value = found
    } else {
      notFound.value = true
    }
  } catch {
    notFound.value = true
    snackbar.value = { show: true, message: 'Failed to load student.', color: 'error' }
  } finally {
    loading.value = false
  }
})
</script>
