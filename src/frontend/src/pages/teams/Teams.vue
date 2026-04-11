<template>
  <v-card rounded="lg">
    <v-card-title class="d-flex align-center justify-space-between pa-4">
      <span>Team Management</span>
      <v-btn v-if="userInfoStore.isAdmin" color="primary" prepend-icon="mdi-plus">Create Team</v-btn>
    </v-card-title>
    <v-card-text>
      <!--
        UC-7:  Find senior design teams
        UC-8:  View a senior design team
        UC-9:  Create a senior design team
        UC-10: Edit a senior design team
        UC-12: Assign students to teams
        UC-13: Remove a student from a team
        UC-14: Delete a senior design team
        UC-19: Assign instructors to teams
        UC-20: Remove an instructor from a team
      -->
      <div class="text-center py-12 text-medium-emphasis">
        <v-icon icon="mdi-account-group-outline" size="64" class="mb-4" />
        <p>UC-7 to UC-14, UC-19, UC-20: Team management — implementation goes here.</p>
      </div>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { useUserInfoStore } from '@/stores/userInfo'
const userInfoStore = useUserInfoStore()
</script>
