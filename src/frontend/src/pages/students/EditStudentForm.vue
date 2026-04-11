<template>
  <div>
    <!-- UC-26: Edit student account details -->
    <div class="text-center py-8 text-medium-emphasis">
      <p>UC-26: Edit student account — implementation goes here.</p>
    </div>
    <div class="d-flex justify-end gap-2 mt-4">
      <v-btn variant="text" @click="$emit('close-dialog')">Cancel</v-btn>
      <v-btn color="primary">Save</v-btn>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{ studentId?: number }>()
defineEmits(['close-dialog'])
</script>
