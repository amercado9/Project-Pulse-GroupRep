<template>
  <v-container>
    <v-btn variant="text" prepend-icon="mdi-arrow-left" class="mb-4" @click="router.push({ name: 'teams' })">
      Back to Teams
    </v-btn>

    <v-row v-if="loading">
      <v-col class="text-center"><v-progress-circular indeterminate /></v-col>
    </v-row>

    <template v-else-if="team">
      <v-row class="mb-2" align="center">
        <v-col>
          <h2 class="text-h5 font-weight-bold">{{ team.teamName }}</h2>
        </v-col>
        <v-col cols="auto">
          <v-chip color="primary" variant="tonal">
            {{ team.sectionName }}
          </v-chip>
        </v-col>
      </v-row>

      <v-card variant="outlined" class="mb-4">
        <v-card-title class="text-subtitle-1 font-weight-bold pa-4 pb-2">Team Info</v-card-title>
        <v-card-text>
          <v-row>
            <v-col cols="12" md="6">
              <div class="text-caption text-medium-emphasis">Description</div>
              <div>{{ team.teamDescription ?? '—' }}</div>
            </v-col>
            <v-col cols="12" md="6">
              <div class="text-caption text-medium-emphasis">Website</div>
              <a
                v-if="team.teamWebsiteUrl"
                :href="team.teamWebsiteUrl"
                target="_blank"
                rel="noopener noreferrer"
              >
                {{ team.teamWebsiteUrl }}
              </a>
              <div v-else>—</div>
            </v-col>
          </v-row>
        </v-card-text>
      </v-card>

      <v-row>
        <v-col cols="12" md="6">
          <v-card variant="outlined" class="fill-height">
            <v-card-title class="text-subtitle-1 font-weight-bold pa-4 pb-2">Team Members</v-card-title>
            <v-card-text>
              <v-alert v-if="team.teamMemberNames.length === 0" type="info" variant="tonal" density="compact">
                No team members assigned.
              </v-alert>
              <div v-else>
                <v-chip
                  v-for="member in team.teamMemberNames"
                  :key="member"
                  size="small"
                  class="mr-1 mb-1"
                >
                  {{ member }}
                </v-chip>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" md="6">
          <v-card variant="outlined" class="fill-height">
            <v-card-title class="text-subtitle-1 font-weight-bold pa-4 pb-2">Instructors</v-card-title>
            <v-card-text>
              <v-alert v-if="team.instructorNames.length === 0" type="info" variant="tonal" density="compact">
                No instructors assigned.
              </v-alert>
              <div v-else>
                <v-chip
                  v-for="instructor in team.instructorNames"
                  :key="instructor"
                  size="small"
                  color="primary"
                  variant="tonal"
                  class="mr-1 mb-1"
                >
                  {{ instructor }}
                </v-chip>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </template>
  </v-container>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getTeam } from '../services/teamService'
import type { TeamDetail } from '../services/teamTypes'

const route = useRoute()
const router = useRouter()

const team = ref<TeamDetail | null>(null)
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const teamId = Number(route.params.id)
    const res = await getTeam(teamId) as any
    if (res.flag && res.data) {
      team.value = res.data
    } else {
      router.replace({ name: 'not-found' })
    }
  } catch {
    router.replace({ name: 'not-found' })
  } finally {
    loading.value = false
  }
})
</script>
