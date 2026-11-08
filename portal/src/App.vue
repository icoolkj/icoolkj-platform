<script setup lang="ts">
import PortalHeader from '@/components/layout/PortalHeader.vue'
import PortalFooter from '@/components/layout/PortalFooter.vue'
</script>

<template>
  <div class="portal-shell">
    <PortalHeader />
    <main class="portal-main">
      <RouterView />
    </main>
    <PortalFooter />
  </div>
</template>
