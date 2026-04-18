<template>
  <div class="auth-page">
    <v-card class="auth-card" rounded="lg">
      <v-card-title class="pt-6 px-6">
        <h2>Forgot Password</h2>
        <p class="subtitle">Enter your email and we'll send you a reset link.</p>
      </v-card-title>

      <v-card-text class="px-6 pb-6">
        <v-form ref="form" @submit.prevent="submit">
          <v-text-field
            v-model="formData.email"
            label="Email address"
            prepend-inner-icon="mdi-email"
            variant="outlined"
            density="comfortable"
            class="mb-4"
            :rules="[
              (v) => !!v || 'Email is required.',
              (v) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v) || 'Enter a valid email.'
            ]"
          />
          <v-btn block color="primary" size="large" :loading="submitting" @click="submit">
            Send Reset Link
          </v-btn>
          <div class="back-link mt-4 text-center">
            <v-btn variant="text" color="primary" @click="$router.push({ name: 'login' })">
              ← Back to Login
            </v-btn>
          </div>
        </v-form>
      </v-card-text>
    </v-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useNotifyStore } from '@/stores/notify'
import { sendResetPasswordLink } from '@/features/auth/services/authService'

const notifyStore = useNotifyStore()
const submitting = ref(false)
const formData = ref({ email: '' })
const form = ref()

async function submit() {
  const { valid } = await form.value.validate()
  if (!valid) return
  submitting.value = true
  try {
    await sendResetPasswordLink(formData.value.email)
    notifyStore.success('Reset link sent! Check your inbox.')
  } catch {
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.auth-page {
  min-height: 100vh;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;

  .auth-card {
    width: 100%;
    max-width: 420px;
    h2 { margin: 0 0 4px; font-size: 22px; font-weight: 700; color: #1a2744; }
    .subtitle { margin: 0; color: #909399; font-size: 14px; }
  }
}
</style>
