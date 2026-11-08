import { computed, type Ref } from 'vue'
import type { TenantVO } from '@/types/tenant/tenant'
import { useUserStore } from '@/stores/user'

/** 解析选择器租户范围：显式 tenants/tenantId 优先，否则回退当前登录租户（便于租户管理员直接选用） */
export function usePickerTenants(props: { tenantId?: string; tenants?: TenantVO[] }): {
  resolvedTenants: Ref<TenantVO[]>
  multiTenant: Ref<boolean>
} {
  const userStore = useUserStore()

  const resolvedTenants = computed(() => {
    if (props.tenants?.length) {
      return props.tenants.filter((item) => item.id)
    }
    if (props.tenantId) {
      return [{ id: props.tenantId, tenantName: '', tenantCode: '' }] as TenantVO[]
    }
    const current = userStore.user?.tenantId
    if (current != null && String(current).length) {
      return [{ id: String(current), tenantName: '', tenantCode: '' }] as TenantVO[]
    }
    return [] as TenantVO[]
  })

  const multiTenant = computed(() => resolvedTenants.value.length > 1)

  return { resolvedTenants, multiTenant }
}
