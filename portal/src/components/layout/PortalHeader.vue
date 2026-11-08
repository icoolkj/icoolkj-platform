<script setup lang="ts">
import { computed } from 'vue'
import { portalConfig } from '@/config/portal'
import { resolveTenantFromLocation } from '@/tenant/resolveTenant'

const tenant = resolveTenantFromLocation()
const brand = computed(() =>
  tenant.tenantCode ? `Tenant: ${tenant.tenantCode}` : portalConfig.fallbackBrand,
)
</script>

<template>
  <header class="portal-header">
    <RouterLink class="brand" to="/">{{ brand }}</RouterLink>
    <nav>
      <RouterLink to="/">首页</RouterLink>
      <RouterLink to="/login-entry">进入控制台</RouterLink>
    </nav>
  </header>
</template>

<style scoped>
.portal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 20px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
}

.brand {
  font-weight: 700;
}

nav {
  display: flex;
  gap: 16px;
  font-size: 0.95rem;
}

nav a.router-link-active {
  color: #1d39c4;
  font-weight: 600;
}
</style>
