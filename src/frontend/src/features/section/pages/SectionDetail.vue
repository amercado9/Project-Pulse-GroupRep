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
          <v-chip :color="section.active ? 'success' : 'default'" variant="tonal">
            {{ section.active ? 'Active' : 'Inactive' }}
          </v-chip>
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
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getSection } from '../services/sectionService'
import type { SectionDetail } from '../services/sectionTypes'

const route = useRoute()
const router = useRouter()

const section = ref<SectionDetail | null>(null)
const loading = ref(false)

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
})
</script>
