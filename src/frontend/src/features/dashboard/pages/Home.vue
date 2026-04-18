<template>
  <div class="home-page">
    <div class="welcome-banner mb-6">
      <h2>Welcome back, {{ userInfoStore.userInfo?.firstName }}</h2>
      <p class="subtitle">
        <v-chip :color="roleChipColor" size="small" variant="flat" class="mr-2">{{ roleLabel }}</v-chip>
        Project Pulse — Senior Design Management Platform
      </p>
    </div>

    <v-alert type="info" variant="tonal" rounded="lg">
      Use the sidebar to navigate to your features.
    </v-alert>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useUserInfoStore } from '@/stores/userInfo'

const userInfoStore = useUserInfoStore()

const roleLabel = computed(() => {
  if (userInfoStore.isAdmin) return 'Admin'
  if (userInfoStore.isInstructor) return 'Instructor'
  if (userInfoStore.isStudent) return 'Student'
  return 'User'
})

const roleChipColor = computed(() => {
  if (userInfoStore.isAdmin) return 'error'
  if (userInfoStore.isInstructor) return 'warning'
  return 'primary'
})
</script>

<style lang="scss" scoped>
.home-page {
  max-width: 1200px;

  .welcome-banner {
    background: linear-gradient(135deg, #1a2744 0%, #1565c0 100%);
    border-radius: 10px;
    padding: 28px 32px;
    color: #fff;

    h2 {
      margin: 0 0 8px;
      font-size: 24px;
      font-weight: 700;
    }

    .subtitle {
      margin: 0;
      opacity: 0.9;
      font-size: 14px;
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 4px;
    }
  }
}
</style>
