<template>
  <v-container max-width="720">
    <v-btn variant="text" prepend-icon="mdi-arrow-left" class="mb-4" @click="router.push({ name: 'instructors' })">
      Back to Instructors
    </v-btn>

    <h2 class="text-h5 font-weight-bold mb-6">Invite Instructors</h2>

    <!-- Step 1: Enter emails -->
    <v-card v-if="step === 1" variant="outlined">
      <v-card-title class="text-subtitle-1 font-weight-bold pa-4 pb-2">
        Step 1 of 2 — Enter Instructor Emails
      </v-card-title>
      <v-card-text>
        <p class="text-body-2 text-medium-emphasis mb-4">
          Enter one or more instructor email addresses separated by semicolons.
          Spaces between emails are ignored. Duplicate emails are removed automatically.
        </p>
        <v-textarea
          v-model="emailsInput"
          label="Instructor emails"
          placeholder="ivy@tcu.edu; noah@tcu.edu; emma@tcu.edu"
          rows="5"
          variant="outlined"
          :error-messages="inputError"
          @input="inputError = ''"
        />
      </v-card-text>
      <v-card-actions class="pa-4 pt-0">
        <v-spacer />
        <v-btn color="primary" :loading="loading" @click="handlePreview">
          Next — Preview Email
        </v-btn>
      </v-card-actions>
    </v-card>

    <!-- Step 2: Preview & confirm -->
    <template v-else-if="step === 2 && preview">
      <v-card variant="outlined" class="mb-4">
        <v-card-title class="text-subtitle-1 font-weight-bold pa-4 pb-2">
          Step 2 of 2 — Confirm &amp; Send
        </v-card-title>
        <v-card-text>
          <v-alert type="info" variant="tonal" density="compact" class="mb-4">
            <strong>{{ preview.emailCount }}</strong>
            {{ preview.emailCount === 1 ? 'invitation' : 'invitations' }} will be sent.
          </v-alert>

          <div class="text-caption text-medium-emphasis mb-1">Recipients</div>
          <div class="mb-4">
            <v-chip
              v-for="email in preview.emails"
              :key="email"
              size="small"
              color="primary"
              variant="tonal"
              class="mr-1 mb-1"
            >{{ email }}</v-chip>
          </div>

          <div class="text-caption text-medium-emphasis mb-1">Email Subject</div>
          <v-text-field
            v-model="customSubject"
            variant="outlined"
            density="compact"
            class="mb-4"
          />

          <div class="text-caption text-medium-emphasis mb-1">
            Email Message
            <span class="ml-2 text-primary" style="cursor: pointer;" @click="editingBody = !editingBody">
              {{ editingBody ? 'Preview' : 'Customize' }}
            </span>
          </div>

          <v-textarea
            v-if="editingBody"
            v-model="customBody"
            variant="outlined"
            rows="10"
            class="mb-2"
            hint="Use [Registration link] as a placeholder — it will be replaced with the actual link."
            persistent-hint
          />
          <v-sheet
            v-else
            rounded="lg"
            border
            class="pa-4 text-body-2 mb-2"
            style="white-space: pre-wrap; font-family: monospace; background: #f8f9fa;"
          >
            {{ customBody }}
          </v-sheet>
        </v-card-text>

        <v-card-actions class="pa-4 pt-0">
          <v-btn variant="text" @click="step = 1">Modify Details</v-btn>
          <v-spacer />
          <v-btn color="primary" :loading="loading" @click="handleSend">
            Send Invitations
          </v-btn>
        </v-card-actions>
      </v-card>
    </template>

    <v-snackbar v-model="snackbar.show" :color="snackbar.color" timeout="3000">
      {{ snackbar.message }}
    </v-snackbar>
  </v-container>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  previewInstructorInvites,
  sendInstructorInvites
} from '../services/instructorInviteService'
import type { InstructorInvitePreview } from '../services/instructorInviteService'

const router = useRouter()

const step = ref<1 | 2>(1)
const emailsInput = ref('')
const preview = ref<InstructorInvitePreview | null>(null)
const customSubject = ref('')
const customBody = ref('')
const editingBody = ref(false)
const loading = ref(false)
const inputError = ref('')
const snackbar = ref({ show: false, message: '', color: 'success' })

async function handlePreview() {
  if (!emailsInput.value.trim()) {
    inputError.value = 'Please enter at least one email address.'
    return
  }
  loading.value = true
  try {
    const res = await previewInstructorInvites(emailsInput.value) as any
    if (res.flag) {
      preview.value = res.data
      customSubject.value = res.data.subject
      customBody.value = res.data.body
      step.value = 2
    } else {
      inputError.value = res.message
    }
  } catch (err: any) {
    const msg = err?.response?.data?.message
    inputError.value = msg || 'Failed to validate emails. Please try again.'
  } finally {
    loading.value = false
  }
}

async function handleSend() {
  if (!preview.value) return
  loading.value = true
  try {
    const res = await sendInstructorInvites({
      emails: preview.value.emails,
      subject: customSubject.value,
      body: customBody.value
    }) as any
    if (res.flag) {
      snackbar.value = {
        show: true,
        message: `${res.data.sentCount} invitation(s) sent successfully.`,
        color: 'success'
      }
      setTimeout(() => router.push({ name: 'home' }), 1500)
    } else {
      snackbar.value = { show: true, message: res.message, color: 'error' }
    }
  } catch {
    snackbar.value = { show: true, message: 'Failed to send invitations. Please try again.', color: 'error' }
  } finally {
    loading.value = false
  }
}
</script>
