<template>
  <template v-for="menuItem in menuRoute.children" :key="menuItem.path">
    <!-- Leaf menu item -->
    <v-list-item
      v-if="!menuItem.children && hasPermission(menuItem.meta.requiresPermissions) && menuItem.meta.isMenuItem"
      :to="menuItem.path"
      :prepend-icon="menuItem.meta.icon"
      :title="menuItem.meta.title"
      rounded="lg"
      active-class="active-item"
    />

    <!-- Sub-menu with children -->
    <v-list-group
      v-if="menuItem.children && hasPermission(menuItem.meta.requiresPermissions) && menuItem.meta.isMenuItem"
      :value="menuItem.path"
    >
      <template #activator="{ props }">
        <v-list-item
          v-bind="props"
          :prepend-icon="menuItem.meta.icon"
          :title="menuItem.meta.title"
          rounded="lg"
        />
      </template>
      <MenuItems :menuRoute="menuItem" />
    </v-list-group>
  </template>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useUserInfoStore } from '@/stores/userInfo'

defineProps(['menuRoute'])

const userInfoStore = useUserInfoStore()
const userPermissions = computed(() => userInfoStore.roleList)

function hasPermission(requiredPermissions: string[] | undefined) {
  if (requiredPermissions === undefined) return true
  return requiredPermissions.some((p) => userPermissions.value.includes(p.toLowerCase()))
}
</script>
