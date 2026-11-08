import { computed, onMounted, ref, type Ref } from 'vue'
import { registerAllDomainGenBlocks } from '@/components/gen/registry/plugins/registerAll'
import { getTreeSourceApi } from '@/components/gen/registry/tree-sources/registry'
import { buildGenTree } from '@/utils/genTree'
import { TREE_ROOT_PARENT_ID } from '@/constants/flags'
import type { GenViewSchema, GenViewTreeSourceConfig } from '@/types/tool/genView'

type TreeSourcesMap = Record<string, Record<string, unknown>[]>

function buildTreeFromFlat(
  rows: Record<string, unknown>[],
  config: GenViewTreeSourceConfig,
  rootLabel: string,
): Record<string, unknown>[] {
  const parentField = config.parentField ?? 'parentId'
  const rootValue = config.rootValue ?? TREE_ROOT_PARENT_ID
  const labelField = config.labelField
  const valueField = config.valueField ?? 'id'
  const tree = buildGenTree(rows, parentField, rootValue)
  if (config.wrapRoot === false) {
    return tree
  }
  return [
    {
      [valueField]: rootValue,
      [labelField]: rootLabel,
      children: tree,
    },
  ]
}

async function loadConfiguredSource(
  config: GenViewTreeSourceConfig,
  gp: (key: string) => string,
): Promise<Record<string, unknown>[]> {
  const loader = getTreeSourceApi(config.apiKey)
  if (!loader) {
    console.warn(`[useGenTreeSources] unregistered tree source apiKey: ${config.apiKey}`)
    return []
  }
  const pageSize = config.pageSize ?? 10000
  const rows = await loader({ pageSize })
  const rootLabel = config.rootLabelKey ? gp(config.rootLabelKey) : gp('treeRoot')
  return buildTreeFromFlat(rows, config, rootLabel)
}

/**
 * 从 view.json extensions.treeSources 加载树选择数据，并与 prop 传入的 treeSources 合并（prop 优先）。
 */
export function useGenTreeSources(
  view: Ref<GenViewSchema | null | undefined>,
  gp: (key: string) => string,
  propSources?: Ref<TreeSourcesMap | undefined>,
) {
  registerAllDomainGenBlocks()

  const loaded = ref<TreeSourcesMap>({})
  const ready = ref(false)

  async function reload() {
    const configs = view.value?.extensions?.treeSources ?? {}
    const entries = Object.entries(configs)
    if (entries.length === 0) {
      loaded.value = {}
      ready.value = true
      return
    }
    ready.value = false
    const next: TreeSourcesMap = {}
    await Promise.all(
      entries.map(async ([key, config]) => {
        try {
          next[key] = await loadConfiguredSource(config, gp)
        } catch (err) {
          console.warn(`[useGenTreeSources] failed to load "${key}":`, err)
          next[key] = []
        }
      }),
    )
    loaded.value = next
    ready.value = true
  }

  onMounted(() => {
    void reload()
  })

  const treeSources = computed<TreeSourcesMap>(() => ({
    ...loaded.value,
    ...(propSources?.value ?? {}),
  }))

  return { treeSources, ready, reload }
}
