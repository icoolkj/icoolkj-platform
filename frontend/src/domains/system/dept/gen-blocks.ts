import { pageDept } from '@/api/system/dept'
import type { GenTreeSourceApiLoader } from '@/components/gen/registry/tree-sources/registry'

export const GEN_TREE_SOURCE_APIS: Record<string, GenTreeSourceApiLoader> = {
  pageDept: async ({ pageSize }) => {
    const res = await pageDept({ pageNo: 1, pageSize })
    return res.data.data.list as unknown as Record<string, unknown>[]
  },
}
