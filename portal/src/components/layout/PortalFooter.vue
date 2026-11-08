<script setup lang="ts">
import { portalConfig } from '@/config/portal'
</script>

<template>
  <footer class="portal-footer">
    <span>Powered by {{ portalConfig.fallbackBrand }}</span>
  </footer>
</template>

<style scoped>
.portal-footer {
  padding: 16px;
  text-align: center;
  font-size: 0.8125rem;
  color: #8c8c8c;
  border-top: 1px solid #e8e8e8;
  background: #fff;
}
</style>
