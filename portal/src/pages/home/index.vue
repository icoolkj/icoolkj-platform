<script setup lang="ts">
import { portalConfig } from '@/config/portal'
import { resolveTenantFromLocation } from '@/tenant/resolveTenant'

const tenant = resolveTenantFromLocation()
</script>

<template>
  <section class="page">
    <h1>租户门户（骨架）</h1>
    <p v-if="tenant.tenantCode">
      当前解析租户：<strong>{{ tenant.tenantCode }}</strong>（来源：{{ tenant.source }}）
    </p>
    <p v-else>未解析到租户。本地可用 <code>?tenantCode=demo</code> 联调。</p>
    <p>
      <RouterLink to="/login-entry">进入控制台入口</RouterLink>
    </p>
    <p class="hint">
      品牌与内容后期按租户 bootstrap 加载；目录禁止按租户拆分。兜底名：
      {{ portalConfig.fallbackBrand }}
    </p>
  </section>
</template>
