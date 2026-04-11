<template>
  <v-card rounded="lg">
    <v-card-title class="d-flex align-center justify-space-between pa-4">
      <span>Section Management</span>
      <v-btn v-if="userInfoStore.isAdmin" color="primary" prepend-icon="mdi-plus">Add Section</v-btn>
    </v-card-title>
    <v-card-text>
      <!-- UC-2: Find sections, UC-3: View section, UC-4: Create section, UC-5: Edit section, UC-6: Set up active weeks -->
      <div class="text-center py-12 text-medium-emphasis">
        <v-icon icon="mdi-school-outline" size="64" class="mb-4" />
        <p>UC-2 to UC-6: Section management — implementation goes here.</p>
      </div>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { useUserInfoStore } from '@/stores/userInfo'
const userInfoStore = useUserInfoStore()
</script>
