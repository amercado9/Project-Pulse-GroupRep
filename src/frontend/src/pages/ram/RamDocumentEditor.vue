<template>
  <v-card rounded="lg">
    <v-card-title class="d-flex align-center justify-space-between pa-4">
      <span>Document Editor</span>
      <v-btn color="success" prepend-icon="mdi-check">Save</v-btn>
    </v-card-title>
    <v-card-text>
      <!-- RAM: Rich-text collaborative document editor -->
      <div class="text-center py-12 text-medium-emphasis">
        <v-icon icon="mdi-pencil-outline" size="64" class="mb-4" />
        <p>Requirements document editor — implementation goes here.</p>
      </div>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
const route = useRoute()
const documentId = route.params.documentId
</script>
