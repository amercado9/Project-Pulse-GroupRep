<template>
  <v-container>
    <v-btn variant="text" prepend-icon="mdi-arrow-left" class="mb-4"
           @click="router.push({ name: 'students' })">
      Back to Students
    </v-btn>

    <v-row v-if="loading">
      <v-col class="text-center"><v-progress-circular indeterminate /></v-col>
    </v-row>

    <template v-else-if="student">
      <!-- Header -->
      <v-row class="mb-2" align="center">
        <v-col>
          <h2 class="text-h5 font-weight-bold">
            {{ student.firstName }} {{ student.lastName }}
          </h2>
          <p class="text-body-2 text-medium-emphasis mt-1">{{ student.email }}</p>
        </v-col>
      </v-row>

      <!-- Team & Section memberships -->
      <v-card variant="outlined" class="mb-4">
        <v-card-title class="text-subtitle-1 font-weight-bold pa-4 pb-2">
          Section &amp; Team
        </v-card-title>
        <v-card-text>
          <v-alert v-if="student.teams.length === 0" type="info" variant="tonal" density="compact">
            This student is not assigned to any team.
          </v-alert>
          <v-table v-else density="compact">
            <thead>
              <tr>
                <th>Section</th>
                <th>Team</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="team in student.teams" :key="team.teamId">
                <td>{{ team.sectionName }}</td>
                <td>{{ team.teamName }}</td>
              </tr>
            </tbody>
          </v-table>
        </v-card-text>
      </v-card>

      <!-- Peer Evaluations -->
      <v-card variant="outlined" class="mb-4">
        <v-card-title class="text-subtitle-1 font-weight-bold pa-4 pb-2">
          Peer Evaluations
        </v-card-title>
        <v-card-text>
          <span class="text-medium-emphasis">No peer evaluations yet.</span>
        </v-card-text>
      </v-card>

      <!-- Weekly Activity Reports -->
      <v-card variant="outlined">
        <v-card-title class="text-subtitle-1 font-weight-bold pa-4 pb-2">
          Weekly Activity Reports (WARs)
        </v-card-title>
        <v-card-text>
          <span class="text-medium-emphasis">No weekly activity reports yet.</span>
        </v-card-text>
      </v-card>
    </template>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getStudent } from '../services/studentService'
import type { StudentDetail } from '../services/studentTypes'

const route = useRoute()
const router = useRouter()

const student = ref<StudentDetail | null>(null)
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const res = await getStudent(Number(route.params.id)) as any
    if (res.flag) student.value = res.data
    else router.replace({ name: 'not-found' })
  } catch {
    router.replace({ name: 'not-found' })
  } finally {
    loading.value = false
  }
})
</script>
