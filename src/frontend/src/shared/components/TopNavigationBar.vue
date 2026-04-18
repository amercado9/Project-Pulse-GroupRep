<template>
  <v-app-bar flat border="b" color="white" height="56">
    <v-app-bar-nav-icon @click="toggleCollapse" />

    <v-breadcrumbs :items="breadcrumbItems" density="compact" class="ml-0 px-0">
      <template #item="{ item }">
        <v-breadcrumbs-item :to="(item as any).to" :disabled="item.disabled">
          <v-icon :icon="(item as any).icon" size="16" class="mr-1" />
          {{ item.title }}
        </v-breadcrumbs-item>
      </template>
    </v-breadcrumbs>

    <v-spacer />

    <v-tooltip text="Refresh" location="bottom">
      <template #activator="{ props }">
        <v-btn v-bind="props" icon="mdi-refresh" variant="text" size="small" @click="refresh" />
      </template>
    </v-tooltip>

    <v-tooltip text="Full Screen" location="bottom">
      <template #activator="{ props }">
        <v-btn v-bind="props" icon="mdi-fullscreen" variant="text" size="small" @click="goToFullScreen" />
      </template>
    </v-tooltip>

    <!-- User dropdown -->
    <v-menu location="bottom end" min-width="180">
      <template #activator="{ props }">
        <v-btn v-bind="props" variant="text" class="user-btn mx-2">
          <v-avatar :image="avatar" size="28" class="mr-2" />
          <span class="username">{{ userInfoStore.userInfo?.firstName }}</span>
          <v-chip
            v-if="userInfoStore.isAdmin"
            color="error"
            size="x-small"
            variant="outlined"
            class="ml-2"
          >Admin</v-chip>
          <v-chip
            v-else-if="userInfoStore.isInstructor"
            color="warning"
            size="x-small"
            variant="outlined"
            class="ml-2"
          >Instructor</v-chip>
          <v-chip
            v-else-if="userInfoStore.isStudent"
            color="primary"
            size="x-small"
            variant="outlined"
            class="ml-2"
          >Student</v-chip>
          <v-icon icon="mdi-chevron-down" size="16" class="ml-1" />
        </v-btn>
      </template>

      <v-list density="compact" nav>
        <v-list-item
          prepend-icon="mdi-account-circle"
          title="My Profile"
          @click="router.push({ name: 'user-profile' })"
        />
        <v-list-item
          prepend-icon="mdi-lock-reset"
          title="Reset Password"
          @click="router.push({ name: 'user-reset-password' })"
        />
        <v-divider class="my-1" />
        <v-list-item
          prepend-icon="mdi-logout"
          title="Log Out"
          @click="logoutDialog = true"
        />
      </v-list>
    </v-menu>
  </v-app-bar>

  <!-- Logout confirmation dialog -->
  <v-dialog v-model="logoutDialog" max-width="360" persistent>
    <v-card title="Confirm Logout">
      <v-card-text>Are you sure you want to log out?</v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn variant="text" @click="logoutDialog = false">Cancel</v-btn>
        <v-btn color="primary" variant="flat" @click="confirmLogout">Log Out</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { inject, computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTokenStore } from '@/stores/token'
import { useUserInfoStore } from '@/stores/userInfo'
import { useSettingsStore } from '@/stores/settings'
import { useNotifyStore } from '@/stores/notify'
import avatar from '@/assets/default.svg'

const { isCollapse, toggleCollapse } = inject('menuCollapse') as any
const route = useRoute()
const router = useRouter()
const tokenStore = useTokenStore()
const userInfoStore = useUserInfoStore()
const settingsStore = useSettingsStore()
const notifyStore = useNotifyStore()
const logoutDialog = ref(false)

const refresh = () => router.go(0)

function goToFullScreen() {
  const el = document.documentElement
  if (el.requestFullscreen) el.requestFullscreen()
}

const breadcrumbItems = computed(() =>
  route.matched
    .filter((r) => r.meta?.title)
    .map((r, i, arr) => ({
      title: r.meta.title as string,
      icon: r.meta.icon as string,
      to: { name: r.name, params: route.params },
      disabled: i === arr.length - 1
    }))
)

function confirmLogout() {
  logoutDialog.value = false
  tokenStore.removeToken()
  userInfoStore.removeUserInfo()
  settingsStore.removeDefaultSectionId()
  router.push({ name: 'login' })
  notifyStore.success('Logged out successfully.')
}
</script>

<style lang="scss" scoped>
.user-btn {
  text-transform: none;
  letter-spacing: 0;

  .username {
    font-weight: 600;
    font-size: 14px;
    color: #303133;
  }
}
</style>
