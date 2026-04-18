<template>
  <div class="register-page">
    <v-card class="register-card" rounded="lg">
      <v-card-title class="pt-6 px-6">
        <h2>Create Your Account</h2>
        <p class="subtitle">Complete your registration for Project Pulse.</p>
      </v-card-title>

      <v-card-text class="px-6">
        <v-form ref="registerForm" @submit.prevent="register">
          <v-text-field
            v-model="formData.firstName"
            label="First Name"
            variant="outlined"
            density="comfortable"
            class="mb-3"
            :rules="[(v) => !!v || 'First name is required.']"
          />
          <v-text-field
            v-model="formData.lastName"
            label="Last Name"
            variant="outlined"
            density="comfortable"
            class="mb-3"
            :rules="[(v) => !!v || 'Last name is required.']"
          />
          <v-text-field
            v-model="formData.email"
            label="Email"
            variant="outlined"
            density="comfortable"
            class="mb-3"
            readonly
            disabled
          />
          <v-text-field
            v-model="formData.password"
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
            v-model="formData.confirmPassword"
            label="Confirm Password"
            :type="showConfirm ? 'text' : 'password'"
            :append-inner-icon="showConfirm ? 'mdi-eye-off' : 'mdi-eye'"
            @click:append-inner="showConfirm = !showConfirm"
            variant="outlined"
            density="comfortable"
            class="mb-4"
            :rules="[
              (v) => !!v || 'Please confirm your password.',
              (v) => v === formData.password || 'Passwords do not match.'
            ]"
          />
          <v-btn block color="primary" size="large" :loading="submitting" @click="register">
            Create Account
          </v-btn>
        </v-form>
      </v-card-text>
    </v-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useNotifyStore } from '@/stores/notify'
import { registerUser } from '@/features/auth/services/authService'

const route = useRoute()
const router = useRouter()
const notifyStore = useNotifyStore()
const submitting = ref(false)
const showPwd = ref(false)
const showConfirm = ref(false)
const registerForm = ref()

const formData = ref({
  firstName: '',
  lastName: '',
  email: '',
  password: '',
  confirmPassword: ''
})

onMounted(() => {
  const email = route.query.email as string
  const alreadyRegistered = route.query.registered as string
  if (email) formData.value.email = email
  if (alreadyRegistered === 'true') {
    notifyStore.info('You already have an account. Please log in.')
    router.push({ name: 'login' })
  }
})

async function register() {
  const { valid } = await registerForm.value.validate()
  if (!valid) return
  submitting.value = true
  try {
    await registerUser({
      firstName: formData.value.firstName,
      lastName: formData.value.lastName,
      email: formData.value.email,
      password: formData.value.password
    })
    notifyStore.success('Account created! Please log in.')
    router.push({ name: 'login' })
  } catch (err: any) {
    if (err?.response?.status === 409) {
      notifyStore.warning('An account with this email already exists. Redirecting to login.')
      router.push({ name: 'login' })
    }
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;

  .register-card {
    width: 100%;
    max-width: 520px;

    h2 { margin: 0 0 4px; font-size: 22px; font-weight: 700; color: #1a2744; }
    .subtitle { margin: 0; color: #909399; font-size: 14px; }
  }
}
</style>
