<script setup lang="ts">
import { computed } from 'vue'
import { buildAdminLoginUrl } from '@/config/portal'
import { resolveTenantFromLocation } from '@/tenant/resolveTenant'

const tenant = resolveTenantFromLocation()
const loginUrl = computed(() => buildAdminLoginUrl(tenant.tenantCode))
</script>

<template>
  <section class="page">
    <h1>进入控制台</h1>
    <p>跳转管理端登录页；若已解析租户，将附带 <code>tenantCode</code> 查询参数（需 Admin 登录页后续支持读取）。</p>
    <p>
      <a class="btn" :href="loginUrl" target="_blank" rel="noopener noreferrer">打开控制台登录</a>
    </p>
    <p class="hint">目标地址：{{ loginUrl }}</p>
  </section>
</template>

<style scoped>
.btn {
  display: inline-flex;
  padding: 10px 16px;
  border-radius: 8px;
  background: #1d39c4;
  color: #fff;
  font-weight: 600;
}
</style>
