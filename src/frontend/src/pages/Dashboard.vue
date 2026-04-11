<template>
  <v-navigation-drawer
    v-model="drawerOpen"
    :rail="isCollapse"
    :width="220"
    :rail-width="64"
    permanent
    class="sidebar"
  >
    <!-- Logo area -->
    <div class="sidebar-logo" :class="{ collapsed: isCollapse }">
      <img
        :src="isCollapse ? logoSmall : logo"
        :alt="isCollapse ? '' : 'Project Pulse'"
        class="logo-img"
      />
    </div>

    <v-list density="compact" nav class="sidebar-menu">
      <MenuItems :menuRoute="menuRouteStore.menuRoute" />
    </v-list>
  </v-navigation-drawer>

  <TopNavigationBar />

  <v-main class="main-content">
    <RouterView />
  </v-main>

  <v-footer app height="40" color="white" border="t" class="app-footer">
    <span>Project Pulse &nbsp;·&nbsp; v{{ appVersion }}</span>
  </v-footer>
</template>

<script setup lang="ts">
import { ref, provide } from 'vue'
import MenuItems from '@/components/MenuItems.vue'
import TopNavigationBar from '@/components/TopNavigationBar.vue'
import { useMenuRouteStore } from '@/stores/menuRoute'
import logo from '@/assets/logo.svg'
import logoSmall from '@/assets/logo-small.svg'

const menuRouteStore = useMenuRouteStore()
const drawerOpen = ref(true)
const isCollapse = ref(false)

function toggleCollapse() {
  isCollapse.value = !isCollapse.value
}

provide('menuCollapse', { isCollapse, toggleCollapse })

const appVersion = __APP_VERSION__
</script>

<style lang="scss" scoped>
.sidebar {
  background: linear-gradient(180deg, #1a2744 0%, #0f1b38 100%) !important;

  .sidebar-logo {
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-bottom: 1px solid rgba(255, 255, 255, 0.08);
    padding: 0 12px;
    flex-shrink: 0;

    .logo-img {
      max-width: 160px;
      max-height: 44px;
      object-fit: contain;
      transition: max-width 0.3s ease;
    }

    &.collapsed .logo-img {
      max-width: 36px;
    }
  }

  .sidebar-menu {
    :deep(.v-list-item) {
      color: rgba(255, 255, 255, 0.85) !important;
      border-radius: 6px;
      margin: 2px 4px;

      &:hover { background-color: rgba(255, 255, 255, 0.08) !important; }
      &.v-list-item--active {
        background-color: rgba(96, 180, 255, 0.18) !important;
        color: #60b4ff !important;
      }
    }

    :deep(.v-list-group__items .v-list-item) {
      padding-left: 32px !important;
    }
  }
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
}

.app-footer {
  font-size: 12px;
  color: #909399;
  justify-content: center;
}
</style>
