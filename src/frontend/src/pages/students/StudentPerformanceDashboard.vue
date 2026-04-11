<template>
  <v-card rounded="lg">
    <v-card-title class="pa-4">Student Performance Dashboard</v-card-title>
    <v-card-text>
      <!-- UC-16: View a student, UC-33: Peer eval report, UC-34: WAR report -->
      <div class="text-center py-12 text-medium-emphasis">
        <v-icon icon="mdi-view-dashboard-outline" size="64" class="mb-4" />
        <p>UC-16/UC-33/UC-34: Student performance dashboard — implementation goes here.</p>
      </div>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
const route = useRoute()
const studentId = route.params.studentId
</script>
