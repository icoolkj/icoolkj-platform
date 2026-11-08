import type { DeptVO } from '@/types/system/dept'
import type { TenantVO } from '@/types/tenant/tenant'

export const TENANT_NODE_PREFIX = 'tenant:'

export type ScopedDeptVO = DeptVO & {
  tenantId?: string
  isTenantNode?: boolean
  disabled?: boolean
  children?: ScopedDeptVO[]
}

export function isTenantTreeNode(id?: string) {
  return !!id && id.startsWith(TENANT_NODE_PREFIX)
}

export function tenantIdFromTreeNode(node: ScopedDeptVO) {
  if (node.isTenantNode && node.tenantId) return node.tenantId
  if (node.id && isTenantTreeNode(node.id)) return node.id.slice(TENANT_NODE_PREFIX.length)
  return node.tenantId
}

export function buildDeptTree(list: DeptVO[], tenantId?: string): ScopedDeptVO[] {
  const map = new Map<string, ScopedDeptVO>()
  list.forEach((item) => {
    if (item.id) map.set(item.id, { ...item, tenantId, children: [] })
  })
  const roots: ScopedDeptVO[] = []
  map.forEach((node) => {
    if (node.parentId && map.has(node.parentId)) {
      map.get(node.parentId)!.children!.push(node)
    } else {
      roots.push(node)
    }
  })
  return roots
}

export function buildMultiTenantDeptTree(
  tenants: TenantVO[],
  deptsByTenant: Map<string, DeptVO[]>,
): ScopedDeptVO[] {
  return tenants
    .filter((tenant) => tenant.id)
    .map((tenant) => ({
      id: `${TENANT_NODE_PREFIX}${tenant.id}`,
      deptName: formatTenantLabel(tenant),
      tenantId: tenant.id,
      isTenantNode: true,
      disabled: true,
      children: buildDeptTree(deptsByTenant.get(tenant.id!) ?? [], tenant.id),
    }))
}

export function formatTenantLabel(tenant: TenantVO) {
  const name = tenant.tenantName || tenant.tenantCode || tenant.id || ''
  return tenant.tenantCode ? `${name} (${tenant.tenantCode})` : name
}
