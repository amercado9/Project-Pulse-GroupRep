<template>
  <div class="login-page">
    <div class="login-left">
      <div class="branding">
        <h1 class="brand-title">Project Pulse</h1>
        <p class="brand-desc">
          A streamlined platform for TCU senior design students to track weekly contributions,
          submit peer evaluations, and collaborate on project requirements.
        </p>
        <div class="feature-list">
          <div class="feature-item">
            <v-icon icon="mdi-note-text" color="white" size="20" />
            Weekly Activity Reports
          </div>
          <div class="feature-item">
            <v-icon icon="mdi-chart-bar" color="white" size="20" />
            Peer Evaluations
          </div>
          <div class="feature-item">
            <v-icon icon="mdi-file-document" color="white" size="20" />
            Requirements Management
          </div>
        </div>
      </div>
    </div>

    <div class="login-right">
      <div class="form-box">
        <h2>Sign In</h2>
        <p class="form-sub">Welcome back! Please enter your credentials.</p>

        <v-form ref="loginForm" @submit.prevent="login">
          <v-text-field
            v-model="loginData.username"
            label="Email address"
            prepend-inner-icon="mdi-account"
            variant="outlined"
            density="comfortable"
            class="mb-3"
            :rules="rules.username"
            @keydown.enter="login"
          />
          <v-text-field
            v-model="loginData.password"
            label="Password"
            prepend-inner-icon="mdi-lock"
            :type="showPassword ? 'text' : 'password'"
            :append-inner-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
            @click:append-inner="showPassword = !showPassword"
            variant="outlined"
            density="comfortable"
            class="mb-1"
            :rules="rules.password"
            @keydown.enter="login"
          />
          <div class="form-row mb-4">
            <v-checkbox label="Remember me" density="compact" hide-details />
            <v-btn
              variant="text"
              color="primary"
              size="small"
              @click="goToForgotPassword"
            >Forgot password?</v-btn>
          </div>
          <v-btn
            block
            color="primary"
            size="large"
            :loading="isLoading"
            @click="login"
          >Sign In</v-btn>
        </v-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { loginUser } from '@/apis/login'
import type { LoginData } from '@/apis/login/types'
import { useTokenStore } from '@/stores/token'
import { useUserInfoStore } from '@/stores/userInfo'
import { useSettingsStore } from '@/stores/settings'
import { useNotifyStore } from '@/stores/notify'
import type { Instructor } from '@/apis/instructor/types'
import type { Student } from '@/apis/student/types'

const router = useRouter()
const route = useRoute()
const tokenStore = useTokenStore()
const userInfoStore = useUserInfoStore()
const settingsStore = useSettingsStore()
const notifyStore = useNotifyStore()

const loginData = ref<LoginData>({ username: '', password: '' })
const isLoading = ref(false)
const showPassword = ref(false)
const loginForm = ref()

const rules = {
  username: [
    (v: string) => !!v || 'Email is required.',
    (v: string) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v) || 'Enter a valid email address.'
  ],
  password: [(v: string) => !!v || 'Password is required.']
}

async function login() {
  const { valid } = await loginForm.value.validate()
  if (!valid) return

  isLoading.value = true
  try {
    const result = await loginUser(loginData.value)
    tokenStore.setToken(result.data.token)
    userInfoStore.setUserInfo(result.data.userInfo)

    if (userInfoStore.isInstructor) {
      const instructor = userInfoStore.userInfo as Instructor
      settingsStore.setDefaultCourseId(instructor.defaultCourseId ?? NaN)
      settingsStore.setDefaultSectionId(instructor.defaultSectionId ?? NaN)
    }
    if (userInfoStore.isStudent) {
      const student = userInfoStore.userInfo as Student
      settingsStore.setDefaultSectionId(student.sectionId ?? NaN)
    }

    notifyStore.success('Logged in successfully!')
    const redirect = route.query.redirect as string | undefined
    router.push({ path: redirect || '/' })
  } catch {
    // error handled by request interceptor
  } finally {
    isLoading.value = false
  }
}

function goToForgotPassword() {
  router.push({ name: 'forget-password' })
}
</script>

<style lang="scss" scoped>
.login-page {
  height: 100vh;
  display: flex;

  .login-left {
    flex: 1;
    background: linear-gradient(135deg, #1a2744 0%, #1565c0 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 60px;

    .branding {
      color: #fff;
      max-width: 420px;

      .brand-title {
        font-size: 3rem;
        font-weight: 800;
        margin: 0 0 16px;
        letter-spacing: -1px;
      }

      .brand-desc {
        font-size: 16px;
        opacity: 0.85;
        line-height: 1.6;
        margin-bottom: 32px;
      }

      .feature-list {
        display: flex;
        flex-direction: column;
        gap: 12px;

        .feature-item {
          display: flex;
          align-items: center;
          gap: 10px;
          font-size: 15px;
          opacity: 0.9;
        }
      }
    }
  }

  .login-right {
    width: 420px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #fff;
    padding: 40px;

    .form-box {
      width: 100%;

      h2 {
        font-size: 28px;
        font-weight: 700;
        margin: 0 0 8px;
        color: #1a2744;
      }

      .form-sub {
        color: #909399;
        margin: 0 0 28px;
        font-size: 14px;
      }

      .form-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
    }
  }
}

@media (max-width: 768px) {
  .login-page {
    .login-left { display: none; }
    .login-right { width: 100%; }
  }
}
</style>
