<template>
  <v-card rounded="lg">
    <v-card-title class="d-flex align-center justify-space-between pa-4">
      <span>Course Management</span>
      <v-btn v-if="userInfoStore.isAdmin" color="primary" prepend-icon="mdi-plus">Add Course</v-btn>
    </v-card-title>
    <v-card-text>
      <!-- Supports UC-2 to UC-6 (section/course management) -->
      <div class="text-center py-12 text-medium-emphasis">
        <v-icon icon="mdi-office-building-outline" size="64" class="mb-4" />
        <p>Course management — implementation goes here.</p>
      </div>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { useUserInfoStore } from '@/stores/userInfo'
const userInfoStore = useUserInfoStore()
</script>
