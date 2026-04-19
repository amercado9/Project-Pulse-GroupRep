<template>
  <v-container>
    <v-row class="mb-4" align="center">
      <v-col><h2 class="text-h5 font-weight-bold">Senior Design Sections</h2></v-col>
    </v-row>

    <!-- Search bar -->
    <v-row class="mb-4">
      <v-col cols="12" md="6">
        <v-text-field
          v-model="searchName"
          label="Search by section name"
          prepend-inner-icon="mdi-magnify"
          clearable
          hide-details
          @keyup.enter="search"
          @click:clear="clearSearch"
        />
      </v-col>
      <v-col cols="auto">
        <v-btn color="primary" @click="search">Search</v-btn>
      </v-col>
    </v-row>

    <!-- Results -->
    <v-row v-if="loading">
      <v-col class="text-center"><v-progress-circular indeterminate /></v-col>
    </v-row>

    <v-row v-else-if="sections.length === 0">
      <v-col>
        <v-alert type="info" variant="tonal">
          No matching sections found.
        </v-alert>
      </v-col>
    </v-row>

    <v-row v-else>
      <v-col cols="12">
        <v-table>
          <thead>
            <tr>
              <th>Section Name</th>
              <th>Teams</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="section in sections" :key="section.sectionId">
              <td>{{ section.sectionName }}</td>
              <td>
                <span v-if="section.teamNames.length === 0" class="text-medium-emphasis">—</span>
                <v-chip
                  v-for="team in section.teamNames"
                  :key="team"
                  size="small"
                  variant="tonal"
                  class="mr-1"
                >{{ team }}</v-chip>
              </td>
            </tr>
          </tbody>
        </v-table>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { findSections } from '../services/sectionService'
import type { SectionSummary } from '../services/sectionTypes'

const searchName = ref('')
const sections = ref<SectionSummary[]>([])
const loading = ref(false)

onMounted(() => search())

async function search() {
  loading.value = true
  try {
    const res = await findSections(searchName.value || undefined) as any
    if (res.flag) sections.value = res.data ?? []
    else sections.value = []
  } catch {
    sections.value = []
  } finally {
    loading.value = false
  }
}

function clearSearch() {
  searchName.value = ''
  search()
}
</script>
