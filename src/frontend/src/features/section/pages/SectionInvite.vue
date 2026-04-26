<template>
  <v-container max-width="720">
    <v-btn variant="text" prepend-icon="mdi-arrow-left" class="mb-4"
           @click="router.push({ name: 'section-detail', params: { id: sectionId } })">
      Back to Section
    </v-btn>

    <h2 class="text-h5 font-weight-bold mb-6">Invite Students</h2>

    <!-- Step 1: Enter emails -->
    <v-card v-if="step === 1" variant="outlined">
      <v-card-title class="text-subtitle-1 font-weight-bold pa-4 pb-2">
        Step 1 of 2 — Enter Student Emails
      </v-card-title>
      <v-card-text>
        <p class="text-body-2 text-medium-emphasis mb-4">
          Enter one or more student email addresses separated by semicolons.
          Spaces between emails are ignored.
        </p>
        <v-textarea
          v-model="emailsInput"
          label="Student emails"
          placeholder="alice@example.com; bob@example.com; carol@example.com"
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
              class="mr-1 mb-1"
            >{{ email }}</v-chip>
          </div>

          <div class="text-caption text-medium-emphasis mb-1">Email Message</div>
          <v-sheet
            rounded="lg"
            border
            class="pa-4 text-body-2"
            style="white-space: pre-wrap; font-family: monospace; background: #f8f9fa;"
          >
            <div class="mb-1"><strong>Subject:</strong> {{ preview.subject }}</div>
            <v-divider class="my-2" />
            {{ preview.body }}
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
  </v-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useNotifyStore } from '@/stores/notify'
import { previewInvites, sendInvites } from '../services/inviteService'
import type { InvitePreview } from '../services/inviteService'

const route = useRoute()
const router = useRouter()
const notifyStore = useNotifyStore()

const sectionId = computed(() => Number(route.params.id))

const step = ref<1 | 2>(1)
const emailsInput = ref('')
const preview = ref<InvitePreview | null>(null)
const loading = ref(false)
const inputError = ref('')

async function handlePreview() {
  if (!emailsInput.value.trim()) {
    inputError.value = 'Please enter at least one email address.'
    return
  }
  loading.value = true
  try {
    const res = await previewInvites(sectionId.value, emailsInput.value) as any
    if (res.flag) {
      preview.value = res.data
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
    const res = await sendInvites(sectionId.value, preview.value.emails) as any
    if (res.flag) {
      notifyStore.success(`${res.data.sentCount} invitation(s) sent successfully.`)
      router.push({ name: 'section-detail', params: { id: sectionId.value } })
    } else {
      notifyStore.error(res.message)
    }
  } catch {
    notifyStore.error('Failed to send invitations. Please try again.')
  } finally {
    loading.value = false
  }
}
</script>
