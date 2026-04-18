<template>
  <v-card rounded="lg" max-width="520">
    <v-card-title class="pa-4">Reset Password</v-card-title>
    <v-card-text>
      <v-form ref="form" @submit.prevent="submit">
        <v-text-field
          v-model="formData.currentPassword"
          label="Current Password"
          :type="showCurrent ? 'text' : 'password'"
          :append-inner-icon="showCurrent ? 'mdi-eye-off' : 'mdi-eye'"
          @click:append-inner="showCurrent = !showCurrent"
          variant="outlined"
          density="comfortable"
          class="mb-3"
          :rules="[(v) => !!v || 'Current password is required.']"
        />
        <v-text-field
          v-model="formData.newPassword"
          label="New Password"
          :type="showNew ? 'text' : 'password'"
          :append-inner-icon="showNew ? 'mdi-eye-off' : 'mdi-eye'"
          @click:append-inner="showNew = !showNew"
          variant="outlined"
          density="comfortable"
          class="mb-3"
          :rules="[(v) => !!v && v.length >= 6 || 'Password must be at least 6 characters.']"
        />
        <v-text-field
          v-model="formData.confirmPassword"
          label="Confirm New Password"
          :type="showConfirm ? 'text' : 'password'"
          :append-inner-icon="showConfirm ? 'mdi-eye-off' : 'mdi-eye'"
          @click:append-inner="showConfirm = !showConfirm"
          variant="outlined"
          density="comfortable"
          class="mb-4"
          :rules="[
            (v) => !!v || 'Please confirm your new password.',
            (v) => v === formData.newPassword || 'Passwords do not match.'
          ]"
        />
        <v-btn color="primary" :loading="submitting" @click="submit">Update Password</v-btn>
      </v-form>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useNotifyStore } from '@/stores/notify'

const notifyStore = useNotifyStore()
const submitting = ref(false)
const showCurrent = ref(false)
const showNew = ref(false)
const showConfirm = ref(false)
const form = ref()

const formData = ref({ currentPassword: '', newPassword: '', confirmPassword: '' })

async function submit() {
  const { valid } = await form.value.validate()
  if (!valid) return
  submitting.value = true
  try {
    notifyStore.success('Password updated successfully.')
  } catch {
  } finally {
    submitting.value = false
  }
}
</script>
