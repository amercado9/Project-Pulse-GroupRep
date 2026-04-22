<template>
  <div class="join-page">
    <v-card class="join-card" rounded="lg">

      <!-- Already registered -->
      <template v-if="tokenState === 'already-registered'">
        <v-card-text class="pa-8 text-center">
          <v-icon size="64" color="info" class="mb-4">mdi-account-check</v-icon>
          <h2 class="mb-2">Already Registered</h2>
          <p class="text-medium-emphasis mb-2">You already have an account. Redirecting you to login...</p>
          <v-progress-linear indeterminate color="primary" class="mb-4" />
          <v-btn color="primary" @click="router.replace({ name: 'login' })">Go to Login</v-btn>
        </v-card-text>
      </template>

      <!-- Invalid / expired token -->
      <template v-else-if="tokenState === 'invalid'">
        <v-card-text class="pa-8 text-center">
          <v-icon size="64" color="error" class="mb-4">mdi-link-off</v-icon>
          <h2 class="mb-2">Invalid Invite Link</h2>
          <p class="text-medium-emphasis mb-6">This invite link is invalid or has expired. Please ask your admin for a new link.</p>
          <v-btn color="primary" @click="router.push({ name: 'login' })">Go to Login</v-btn>
        </v-card-text>
      </template>

      <!-- Loading -->
      <template v-else-if="tokenState === 'loading'">
        <v-card-text class="pa-8 text-center">
          <v-progress-circular indeterminate color="primary" class="mb-4" />
          <p class="text-medium-emphasis">Validating your invite link...</p>
        </v-card-text>
      </template>

      <!-- Registration form -->
      <template v-else>
        <v-card-title class="pt-6 px-6">
          <h2>Set Up Your Account</h2>
          <p class="subtitle">You've been invited to join <strong>{{ sectionName }}</strong>.</p>
        </v-card-title>

        <v-card-text class="px-6">

          <!-- Step 1: Enter details -->
          <v-form v-if="step === 1" ref="formRef" @submit.prevent>
            <v-text-field
              v-model="form.firstName"
              label="First Name"
              variant="outlined"
              density="comfortable"
              class="mb-3"
              :rules="[(v) => !!v || 'First name is required.']"
            />
            <v-text-field
              v-model="form.lastName"
              label="Last Name"
              variant="outlined"
              density="comfortable"
              class="mb-3"
              :rules="[(v) => !!v || 'Last name is required.']"
            />
            <v-text-field
              v-model="email"
              label="Email"
              variant="outlined"
              density="comfortable"
              class="mb-3"
              readonly
              disabled
            />
            <v-text-field
              v-model="form.password"
              label="Password"
              :type="showPwd ? 'text' : 'password'"
              :append-inner-icon="showPwd ? 'mdi-eye-off' : 'mdi-eye'"
              @click:append-inner="showPwd = !showPwd"
              variant="outlined"
              density="comfortable"
              class="mb-3"
              :rules="[(v) => !!v && v.length >= 6 || 'Password must be at least 6 characters.']"
            />
            <v-text-field
              v-model="form.confirmPassword"
              label="Confirm Password"
              :type="showConfirm ? 'text' : 'password'"
              :append-inner-icon="showConfirm ? 'mdi-eye-off' : 'mdi-eye'"
              @click:append-inner="showConfirm = !showConfirm"
              variant="outlined"
              density="comfortable"
              class="mb-4"
              :rules="[
                (v) => !!v || 'Please confirm your password.',
                (v) => v === form.password || 'Passwords do not match.'
              ]"
            />
            <v-btn block color="primary" size="large" @click="goToPreview">Preview</v-btn>
          </v-form>

          <!-- Step 2: Preview & confirm -->
          <template v-else>
            <v-alert type="info" variant="tonal" class="mb-4">
              Please review your details before confirming.
            </v-alert>
            <v-list density="compact" class="mb-4">
              <v-list-item title="First Name" :subtitle="form.firstName" />
              <v-list-item title="Last Name" :subtitle="form.lastName" />
              <v-list-item title="Email" :subtitle="email" />
              <v-list-item title="Section" :subtitle="sectionName ?? '—'" />
            </v-list>
            <v-row>
              <v-col>
                <v-btn block variant="outlined" @click="step = 1">Modify</v-btn>
              </v-col>
              <v-col>
                <v-btn block color="primary" :loading="submitting" @click="confirmRegister">
                  Confirm
                </v-btn>
              </v-col>
            </v-row>
          </template>

        </v-card-text>
      </template>

    </v-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { validateJoinToken, registerWithToken } from '@/features/auth/services/authService'
import { useNotifyStore } from '@/stores/notify'

const route = useRoute()
const router = useRouter()
const notify = useNotifyStore()

type TokenState = 'loading' | 'valid' | 'invalid' | 'already-registered'

const tokenState = ref<TokenState>('loading')
const token = ref('')
const email = ref('')
const sectionName = ref<string | null>(null)
const step = ref(1)
const submitting = ref(false)
const showPwd = ref(false)
const showConfirm = ref(false)
const formRef = ref()

const form = ref({ firstName: '', lastName: '', password: '', confirmPassword: '' })

onMounted(async () => {
  token.value = (route.query.token as string) ?? ''
  if (!token.value) {
    tokenState.value = 'invalid'
    return
  }
  try {
    const res = await validateJoinToken(token.value) as any
    if (res.flag) {
      email.value = res.data.email
      sectionName.value = res.data.sectionName
      tokenState.value = 'valid'
    } else {
      tokenState.value = 'invalid'
    }
  } catch (err: any) {
    if (err?.response?.status === 409) {
      tokenState.value = 'already-registered'
      setTimeout(() => router.replace({ name: 'login' }), 3000)
    } else {
      tokenState.value = 'invalid'
    }
  }
})

async function goToPreview() {
  const { valid } = await formRef.value.validate()
  if (valid) step.value = 2
}

async function confirmRegister() {
  submitting.value = true
  try {
    const res = await registerWithToken({
      token: token.value,
      firstName: form.value.firstName,
      lastName: form.value.lastName,
      password: form.value.password,
    }) as any
    if (res.flag) {
      notify.success('Account created! Please log in.')
      router.push({ name: 'login' })
    } else {
      notify.error(res.message)
      step.value = 1
    }
  } catch (err: any) {
    const msg = err?.response?.data?.message || 'An error occurred. Please try again.'
    notify.error(msg)
    step.value = 1
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.join-page {
  min-height: 100vh;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;

  .join-card {
    width: 100%;
    max-width: 520px;

    h2 { margin: 0 0 4px; font-size: 22px; font-weight: 700; color: #1a2744; }
    .subtitle { margin: 0; color: #909399; font-size: 14px; }
  }
}
</style>
