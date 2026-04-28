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
          Next — Confirm Recipients
        </v-btn>
      </v-card-actions>
    </v-card>

    <!-- Step 2: Confirm recipients -->
    <v-card v-else-if="step === 2 && preview" variant="outlined">
      <v-card-title class="text-subtitle-1 font-weight-bold pa-4 pb-2">
        Step 2 of 2 — Confirm &amp; Generate Links
      </v-card-title>
      <v-card-text>
        <v-alert type="info" variant="tonal" density="compact" class="mb-4">
          Registration links will be generated for
          <strong>{{ preview.emailCount }}</strong>
          {{ preview.emailCount === 1 ? 'student' : 'students' }}.
          Copy and send each link manually to the recipient.
        </v-alert>

        <div class="text-caption text-medium-emphasis mb-1">Recipients</div>
        <div class="mb-2">
          <v-chip
            v-for="email in preview.emails"
            :key="email"
            size="small"
            class="mr-1 mb-1"
          >{{ email }}</v-chip>
        </div>
      </v-card-text>
      <v-card-actions class="pa-4 pt-0">
        <v-btn variant="text" @click="step = 1">Modify Details</v-btn>
        <v-spacer />
        <v-btn color="primary" :loading="loading" @click="handleGenerate">
          Generate Links
        </v-btn>
      </v-card-actions>
    </v-card>

    <!-- Step 3: Show generated links -->
    <v-card v-else-if="step === 3" variant="outlined">
      <v-card-title class="text-subtitle-1 font-weight-bold pa-4 pb-2">
        Registration Links Generated
      </v-card-title>
      <v-card-text>
        <v-alert type="success" variant="tonal" density="compact" class="mb-4">
          Copy each link and send it to the corresponding student via email.
          Links expire in 30 days.
        </v-alert>

        <div v-for="item in links" :key="item.email" class="mb-4">
          <div class="text-caption text-medium-emphasis mb-1">{{ item.email }}</div>
          <div class="d-flex align-center" style="gap: 8px;">
            <v-text-field
              :model-value="item.link"
              variant="outlined"
              density="compact"
              readonly
              hide-details
              class="flex-grow-1"
            />
            <v-btn
              icon
              size="small"
              variant="tonal"
              @click="copyLink(item.link)"
            >
              <v-icon>mdi-content-copy</v-icon>
            </v-btn>
          </div>
        </div>
      </v-card-text>
      <v-card-actions class="pa-4 pt-0">
        <v-btn variant="text" @click="reset">Generate More Links</v-btn>
        <v-spacer />
        <v-btn color="primary" @click="router.push({ name: 'section-detail', params: { id: sectionId } })">Done</v-btn>
      </v-card-actions>
    </v-card>

    <v-snackbar v-model="snackbar.show" :color="snackbar.color" timeout="2000">
      {{ snackbar.message }}
    </v-snackbar>
  </v-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { previewInvites, generateInviteLinks } from '../services/inviteService'
import type { InvitePreview, InviteLink } from '../services/inviteService'

const route = useRoute()
const router = useRouter()

const sectionId = computed(() => Number(route.params.id))

const step = ref<1 | 2 | 3>(1)
const emailsInput = ref('')
const preview = ref<InvitePreview | null>(null)
const links = ref<InviteLink[]>([])
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

async function handleGenerate() {
  if (!preview.value) return
  loading.value = true
  try {
    const res = await generateInviteLinks(sectionId.value, preview.value.emails) as any
    if (res.flag) {
      links.value = res.data.links
      step.value = 3
    } else {
      snackbar.value = { show: true, message: res.message, color: 'error' }
    }
  } catch {
    snackbar.value = { show: true, message: 'Failed to generate links. Please try again.', color: 'error' }
  } finally {
    loading.value = false
  }
}

async function copyLink(link: string) {
  await navigator.clipboard.writeText(link)
  snackbar.value = { show: true, message: 'Link copied!', color: 'success' }
}

function reset() {
  step.value = 1
  emailsInput.value = ''
  preview.value = null
  links.value = []
}
</script>
