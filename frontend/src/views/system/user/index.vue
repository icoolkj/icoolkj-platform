<!-- AUTO-GENERATED 用户 — GenRuntimePage + view.json -->
<template>
  <GenRuntimePage page-key="user" :read-only="isPlatformTenantUser" />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { GenRuntimePage } from '@/components/gen'
import { isPlatformTenant } from '@/constants/flags'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const isPlatformTenantUser = computed(() => isPlatformTenant(userStore.user?.tenantId))
</script>

