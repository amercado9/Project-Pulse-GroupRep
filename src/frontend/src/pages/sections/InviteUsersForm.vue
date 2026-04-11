<template>
  <div>
    <v-textarea
      v-model="emailInput"
      label="Email(s)"
      placeholder="Enter email addresses separated by semicolons"
      rows="3"
      variant="outlined"
      density="comfortable"
      hint="Separate multiple emails with semicolons."
      persistent-hint
      class="mb-3"
    />

    <div class="mb-4">
      <p class="text-body-2 mb-2">Invite as:</p>
      <v-btn-toggle v-model="inviteRole" mandatory density="compact" color="primary">
        <v-btn value="student">Student</v-btn>
        <v-btn value="instructor">Instructor</v-btn>
      </v-btn-toggle>
    </div>

    <div class="d-flex justify-end gap-2">
      <v-btn variant="text" @click="$emit('close-dialog')">Cancel</v-btn>
      <v-btn color="primary" :loading="submitting" @click="sendInvitations">Confirm</v-btn>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useNotifyStore } from '@/stores/notify'

const emit = defineEmits(['close-dialog'])
const notifyStore = useNotifyStore()
const emailInput = ref('')
const inviteRole = ref('student')
const submitting = ref(false)

async function sendInvitations() {
  const emails = emailInput.value
    .split(';')
    .map((e) => e.trim())
    .filter(Boolean)
  if (!emails.length) {
    notifyStore.warning('Please enter at least one email.')
    return
  }

  submitting.value = true
  try {
    // UC-11: invite students / UC-18: invite instructors — API call goes here
    notifyStore.success(`Invitations sent to ${emails.length} recipient(s).`)
    emit('close-dialog')
  } catch {
  } finally {
    submitting.value = false
  }
}
</script>
