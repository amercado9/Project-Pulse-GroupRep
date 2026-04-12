<template>
  <div>
    <!-- Back button + header -->
    <div class="d-flex align-center mb-4 gap-3">
      <v-btn
        icon="mdi-arrow-left"
        variant="text"
        @click="$router.push({ name: 'sections' })"
      />
      <div>
        <h2 class="text-h6 font-weight-bold ma-0">
          {{ section?.sectionName ?? 'Section Detail' }}
        </h2>
        <span class="text-caption text-medium-emphasis">UC-3 — View Senior Design Section</span>
      </div>
      <v-spacer />
      <v-chip
        v-if="section"
        :color="section.isActive ? 'success' : 'default'"
        variant="tonal"
        size="small"
      >
        {{ section.isActive ? 'Active' : 'Inactive' }}
      </v-chip>
      <v-btn
        v-if="userInfoStore.isAdmin && section"
        color="primary"
        prepend-icon="mdi-pencil"
        variant="tonal"
        size="small"
        @click="notifyStore.info('Edit section — coming soon.')"
      >
        Edit
      </v-btn>
    </div>

    <!-- Loading skeleton -->
    <template v-if="loading">
      <v-row>
        <v-col v-for="n in 4" :key="n" cols="12" sm="6">
          <v-skeleton-loader type="card" rounded="lg" />
        </v-col>
      </v-row>
    </template>

    <template v-else-if="section">
      <!-- Row 1: Overview + Schedule -->
      <v-row class="mb-2">
        <!-- Overview -->
        <v-col cols="12" md="6">
          <v-card rounded="lg" height="100%">
            <v-card-title class="d-flex align-center gap-2 pb-2">
              <v-icon icon="mdi-information-outline" color="primary" size="20" />
              Overview
            </v-card-title>
            <v-divider />
            <v-card-text>
              <v-table density="compact">
                <tbody>
                  <tr>
                    <td class="text-medium-emphasis" style="width:40%">Section Name</td>
                    <td class="font-weight-medium">{{ section.sectionName }}</td>
                  </tr>
                  <tr>
                    <td class="text-medium-emphasis">Start Date</td>
                    <td>{{ section.startDate ?? '—' }}</td>
                  </tr>
                  <tr>
                    <td class="text-medium-emphasis">End Date</td>
                    <td>{{ section.endDate ?? '—' }}</td>
                  </tr>
                  <tr>
                    <td class="text-medium-emphasis">Status</td>
                    <td>
                      <v-chip
                        :color="section.isActive ? 'success' : 'default'"
                        size="x-small"
                        variant="tonal"
                      >
                        {{ section.isActive ? 'Active' : 'Inactive' }}
                      </v-chip>
                    </td>
                  </tr>
                  <tr>
                    <td class="text-medium-emphasis">Rubric</td>
                    <td>{{ section.rubricName ?? (section.rubricId ? `Rubric #${section.rubricId}` : 'Not assigned') }}</td>
                  </tr>
                </tbody>
              </v-table>
            </v-card-text>
          </v-card>
        </v-col>

        <!-- Schedule -->
        <v-col cols="12" md="6">
          <v-card rounded="lg" height="100%">
            <v-card-title class="d-flex align-center gap-2 pb-2">
              <v-icon icon="mdi-calendar-clock" color="warning" size="20" />
              Due Dates Schedule
            </v-card-title>
            <v-divider />
            <v-card-text>
              <v-table density="compact">
                <tbody>
                  <tr>
                    <td class="text-medium-emphasis" style="width:55%">WAR Due Day</td>
                    <td class="font-weight-medium">{{ section.warWeeklyDueDay }}</td>
                  </tr>
                  <tr>
                    <td class="text-medium-emphasis">WAR Due Time</td>
                    <td>{{ section.warDueTime }}</td>
                  </tr>
                  <tr>
                    <td class="text-medium-emphasis">Peer Eval Due Day</td>
                    <td class="font-weight-medium">{{ section.peerEvaluationWeeklyDueDay }}</td>
                  </tr>
                  <tr>
                    <td class="text-medium-emphasis">Peer Eval Due Time</td>
                    <td>{{ section.peerEvaluationDueTime }}</td>
                  </tr>
                </tbody>
              </v-table>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Row 2: Active Weeks + Teams placeholder -->
      <v-row>
        <!-- Active Weeks -->
        <v-col cols="12" md="6">
          <v-card rounded="lg">
            <v-card-title class="d-flex align-center gap-2 pb-2">
              <v-icon icon="mdi-calendar-check" color="success" size="20" />
              Active Weeks
            </v-card-title>
            <v-divider />
            <v-card-text>
              <template v-if="section.activeWeeks && section.activeWeeks.length">
                <div class="d-flex flex-wrap gap-2">
                  <v-chip
                    v-for="week in section.activeWeeks"
                    :key="week"
                    size="small"
                    color="primary"
                    variant="tonal"
                  >
                    Week {{ week }}
                  </v-chip>
                </div>
              </template>
              <div v-else class="text-medium-emphasis text-body-2">
                No active weeks configured.
              </div>
            </v-card-text>
          </v-card>
        </v-col>

        <!-- Teams -->
        <v-col cols="12" md="6">
          <v-card rounded="lg">
            <v-card-title class="d-flex align-center gap-2 pb-2">
              <v-icon icon="mdi-account-group" color="deep-purple" size="20" />
              Teams
            </v-card-title>
            <v-divider />
            <v-card-text>
              <div class="text-center py-6 text-medium-emphasis">
                <v-icon icon="mdi-account-group-outline" size="40" class="mb-2" />
                <p class="text-body-2">Team management available in UC-7 to UC-14.</p>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </template>

    <!-- Error state -->
    <v-alert v-else type="error" variant="tonal" rounded="lg">
      Section not found or failed to load.
    </v-alert>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { findSectionById } from '@/apis/section'
import type { Section } from '@/apis/section/types'
import { useUserInfoStore } from '@/stores/userInfo'
import { useNotifyStore } from '@/stores/notify'

const route = useRoute()
const userInfoStore = useUserInfoStore()
const notifyStore = useNotifyStore()

const section = ref<Section | null>(null)
const loading = ref(false)

async function loadSection() {
  loading.value = true
  try {
    const id = Number(route.params.sectionId)
    const res = await findSectionById(id)
    section.value = res.data
  } catch {
    // handled by request interceptor
  } finally {
    loading.value = false
  }
}

onMounted(loadSection)
</script>
