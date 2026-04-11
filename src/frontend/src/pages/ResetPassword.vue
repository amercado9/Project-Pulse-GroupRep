<template>
  <div class="auth-page">
    <v-card class="auth-card" rounded="lg">
      <v-card-title class="pt-6 px-6">
        <h2>Reset Password</h2>
        <p class="subtitle">Enter your new password below.</p>
      </v-card-title>

      <v-card-text class="px-6 pb-6">
        <v-form ref="form" @submit.prevent="submit">
          <v-text-field
            v-model="formData.password"
            label="New Password"
            prepend-inner-icon="mdi-lock"
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
            label="Confirm New Password"
            prepend-inner-icon="mdi-lock-check"
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
          <v-btn block color="primary" size="large" :loading="submitting" @click="submit">
            Reset Password
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
import { resetUserPassword } from '@/apis/login'

const route = useRoute()
const router = useRouter()
const notifyStore = useNotifyStore()
const submitting = ref(false)
const showPwd = ref(false)
const showConfirm = ref(false)
const token = ref('')
const email = ref('')
const form = ref()

const formData = ref({ password: '', confirmPassword: '' })

onMounted(() => {
  token.value = (route.query.token as string) ?? ''
  email.value = (route.query.email as string) ?? ''
})

async function submit() {
  const { valid } = await form.value.validate()
  if (!valid) return
  submitting.value = true
  try {
    await resetUserPassword(email.value, token.value, formData.value.password)
    notifyStore.success('Password reset successfully! Please log in.')
    router.push({ name: 'login' })
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
