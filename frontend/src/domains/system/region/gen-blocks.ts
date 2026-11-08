import { pageRegion } from '@/api/system/region'
import type { GenTreeSourceApiLoader } from '@/components/gen/registry/tree-sources/registry'

export const GEN_TREE_SOURCE_APIS: Record<string, GenTreeSourceApiLoader> = {
  pageRegion: async ({ pageSize }) => {
    const res = await pageRegion({ pageNo: 1, pageSize })
    return res.data.data.list as unknown as Record<string, unknown>[]
  },
}
