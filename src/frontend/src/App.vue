<template>
  <v-app>
    <RouterView />
    <v-snackbar
      v-model="notifyStore.visible"
      :color="notifyStore.color"
      :timeout="notifyStore.timeout"
      location="bottom right"
      rounded="lg"
    >
      {{ notifyStore.message }}
      <template #actions>
        <v-btn variant="text" icon="mdi-close" @click="notifyStore.visible = false" />
      </template>
    </v-snackbar>
  </v-app>
</template>

<script setup lang="ts">
import { useNotifyStore } from '@/stores/notify'
const notifyStore = useNotifyStore()
</script>
