<template>
  <v-container>
    <v-row class="mb-4" align="center">
      <v-col><h2 class="text-h5 font-weight-bold">Senior Design Teams</h2></v-col>
    </v-row>

    <v-row class="mb-4">
      <v-col cols="12" md="4">
        <v-text-field
          v-model="filters.sectionName"
          label="Search by section"
          prepend-inner-icon="mdi-school"
          clearable
          hide-details
          @keyup.enter="search"
        />
      </v-col>
      <v-col cols="12" md="4">
        <v-text-field
          v-model="filters.teamName"
          label="Search by team name"
          prepend-inner-icon="mdi-account-group"
          clearable
          hide-details
          @keyup.enter="search"
        />
      </v-col>
      <v-col cols="12" md="4">
        <v-text-field
          v-model="filters.instructor"
          label="Search by instructor"
          prepend-inner-icon="mdi-account-tie"
          clearable
          hide-details
          @keyup.enter="search"
        />
      </v-col>
      <v-col cols="auto">
        <v-btn color="primary" @click="search">Search</v-btn>
      </v-col>
      <v-col cols="auto">
        <v-btn variant="text" @click="clearFilters">Clear</v-btn>
      </v-col>
    </v-row>

    <v-row v-if="loading">
      <v-col class="text-center">
        <v-progress-circular indeterminate />
      </v-col>
    </v-row>

    <v-row v-else-if="teams.length === 0">
      <v-col>
        <v-alert type="info" variant="tonal">
          No matching senior design teams found.
        </v-alert>
      </v-col>
    </v-row>

    <v-row v-else>
      <v-col cols="12">
        <v-table>
          <thead>
            <tr>
              <th>Section</th>
              <th>Team Name</th>
              <th>Description</th>
              <th>Website</th>
              <th>Team Members</th>
              <th>Instructors</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="team in teams" :key="team.teamId">
              <td>{{ team.sectionName }}</td>
              <td>{{ team.teamName }}</td>
              <td>{{ team.teamDescription || '—' }}</td>
              <td>
                <a
                  v-if="team.teamWebsiteUrl"
                  :href="team.teamWebsiteUrl"
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  Open
                </a>
                <span v-else class="text-medium-emphasis">—</span>
              </td>
              <td>
                <div v-if="team.teamMemberNames.length" class="chip-group">
                  <v-chip
                    v-for="member in team.teamMemberNames"
                    :key="member"
                    size="small"
                    variant="tonal"
                    class="mr-1 mb-1"
                  >{{ member }}</v-chip>
                </div>
                <span v-else class="text-medium-emphasis">—</span>
              </td>
              <td>
                <div v-if="team.instructorNames.length" class="chip-group">
                  <v-chip
                    v-for="instructor in team.instructorNames"
                    :key="instructor"
                    size="small"
                    variant="tonal"
                    color="primary"
                    class="mr-1 mb-1"
                  >{{ instructor }}</v-chip>
                </div>
                <span v-else class="text-medium-emphasis">—</span>
              </td>
              <td>
                <v-btn
                  size="small"
                  variant="text"
                  color="primary"
                  @click="viewTeam(team.teamId)"
                >
                  View
                </v-btn>
              </td>
            </tr>
          </tbody>
        </v-table>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { findTeams } from '../services/teamService'
import type { FindTeamsParams, TeamSummary } from '../services/teamTypes'

const router = useRouter()
const teams = ref<TeamSummary[]>([])
const loading = ref(false)
const filters = ref<FindTeamsParams>({
  sectionName: '',
  teamName: '',
  instructor: ''
})

onMounted(() => search())

async function search() {
  loading.value = true
  try {
    const res = await findTeams({
      sectionName: filters.value.sectionName?.trim() || undefined,
      teamName: filters.value.teamName?.trim() || undefined,
      instructor: filters.value.instructor?.trim() || undefined
    }) as any

    teams.value = res.flag ? (res.data ?? []) : []
  } catch {
    teams.value = []
  } finally {
    loading.value = false
  }
}

function clearFilters() {
  filters.value = { sectionName: '', teamName: '', instructor: '' }
  search()
}

function viewTeam(teamId: number) {
  router.push({ name: 'team-detail', params: { id: teamId } })
}
</script>

<style scoped lang="scss">
.chip-group {
  min-width: 180px;
}
</style>
