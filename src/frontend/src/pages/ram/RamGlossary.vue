<template>
  <v-card rounded="lg">
    <v-card-title class="d-flex align-center justify-space-between pa-4">
      <span>Glossary</span>
      <v-btn color="primary" prepend-icon="mdi-plus">Add Term</v-btn>
    </v-card-title>
    <v-card-text>
      <!-- RAM: Glossary terms management -->
      <div class="text-center py-12 text-medium-emphasis">
        <v-icon icon="mdi-book-open-variant-outline" size="64" class="mb-4" />
        <p>Glossary management — implementation goes here.</p>
      </div>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
const route = useRoute()
const documentId = route.params.documentId
</script>
