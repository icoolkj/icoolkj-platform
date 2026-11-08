<!--
  Gen 框架统一入口：按 pageKey 加载 Git 三元组 + 解析 CRUD 资源注册表 + 路由形态积木。
  标准页：<GenRuntimePage page-key="post" />；扩展 props 见 GenRuntimePageProps。
-->
<template>
  <div v-if="loadError" class="gen-page-shell gen-runtime-page--error">
    <el-alert type="error" :title="loadError" show-icon :closable="false" />
  </div>
  <div v-else-if="resourceError" class="gen-page-shell gen-runtime-page--error">
    <el-alert type="error" :title="resourceError" show-icon :closable="false" />
  </div>
  <div v-else-if="!ready" v-loading="true" class="gen-page-shell gen-runtime-page--loading" />
  <div v-else-if="genPageBind" class="gen-page-shell">
    <GenPage
      ref="innerPageRef"
      v-bind="genPageBind"
      @selection-change="emit('selection-change', $event)"
      @open-form="emit('open-form', $event)"
    >
      <template v-for="(_, slotName) in slots" #[slotName]="slotData">
        <slot :name="slotName" v-bind="slotData ?? {}" />
      </template>
    </GenPage>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, useSlots, watchEffect } from 'vue'
import GenPage from '@/components/gen/pages/GenPage.vue'
import { createGenPageExpose } from '@/components/gen/patterns/expose'
import type {
  GenMasterDetailApi,
  GenPageEmits,
  GenPagePatternProps,
  GenRuntimePageProps,
} from '@/components/gen/patterns/types'
import type { GenCrudApi } from '@/types/gen/crudApi'
import { resolveGenCrudResource, resolveResourceKey } from '@/components/gen/registry/crud-resources/registry'
import { useGenPageBundle } from '@/composables/gen/useGenPageBundle'

defineOptions({ inheritAttrs: false })

const props = defineProps<GenRuntimePageProps>()

const emit = defineEmits<GenPageEmits>()

const slots = useSlots()
const innerPageRef = ref<InstanceType<typeof GenPage>>()

function mergeApi(
  base: GenCrudApi | GenMasterDetailApi,
  override?: Partial<GenCrudApi> | Partial<GenMasterDetailApi>,
): GenCrudApi | GenMasterDetailApi {
  if (!override) return base
  if ('master' in base) {
    const b = base as GenMasterDetailApi
    const o = override as Partial<GenMasterDetailApi>
    return {
      master: { ...b.master, ...o.master },
      detail: { ...b.detail, ...o.detail },
    }
  }
  return { ...(base as GenCrudApi), ...(override as Partial<GenCrudApi>) }
}

const { bundle, ready, error: loadError } = useGenPageBundle(() => props.pageKey)

if (import.meta.env.DEV) {
  watchEffect(() => {
    const b = bundle.value
    if (!b) return
    if (!b.i18nFiles['zh-CN']) {
      console.warn(
        `[GenRuntimePage] missing i18n/gen/${props.pageKey}/zh-CN.json — labels may fall back to keys`,
      )
    }
    if (!b.i18nFiles['en-US']) {
      console.warn(`[GenRuntimePage] missing i18n/gen/${props.pageKey}/en-US.json`)
    }
  })
}

const resource = computed(() => {
  if (!bundle.value) return null
  try {
    return resolveGenCrudResource(bundle.value.view.meta)
  } catch {
    return null
  }
})

const resourceError = computed(() => {
  if (loadError.value || !bundle.value) return null
  if (resource.value) return null
  const key = resolveResourceKey(bundle.value.view.meta)
  return `GenRuntimePage: unregistered resourceKey "${key}" for pageKey "${props.pageKey}"`
})

const genPageBind = computed((): GenPagePatternProps | null => {
  const b = bundle.value
  const r = resource.value
  if (!b || !r) return null

  const {
    pageKey: _pageKey,
    apiOverride,
    viewOverride,
    readOnly,
    hideHeader,
    detailPerm,
    treeSources,
    selectable,
    customForm,
    statusChange,
    actionHandlers,
    rowActionVisible,
    createDefaults,
    getAddChildDefaults,
    fixedQuery,
    omitQueryFields,
    omitTableFields,
    treeRowFilter,
    dropOrphanRows,
    clearQueryOnFixedChange,
    getFieldOptions,
  } = props

  return {
    fileView: viewOverride ? viewOverride(b.view) : b.view,
    businessName: b.view.meta.businessName,
    apiModule: b.view.meta.apiModule,
    i18nFiles: b.i18nFiles,
    perm: r.perm,
    api: mergeApi(r.api, apiOverride),
    readOnly: readOnly ?? b.view.readOnly,
    hideHeader,
    detailPerm,
    treeSources,
    selectable,
    customForm,
    statusChange,
    actionHandlers,
    rowActionVisible,
    createDefaults,
    getAddChildDefaults,
    fixedQuery,
    omitQueryFields,
    omitTableFields,
    treeRowFilter,
    dropOrphanRows,
    clearQueryOnFixedChange,
    getFieldOptions,
  }
})

defineExpose(
  createGenPageExpose({
    openForm: (row?: Record<string, unknown>) => innerPageRef.value?.openForm(row),
    openAddChild: (parent) => innerPageRef.value?.openAddChild(parent),
    loadPage: () => innerPageRef.value?.loadPage(),
    loadMasterPage: () => innerPageRef.value?.loadMasterPage(),
    loadDetailPage: () => innerPageRef.value?.loadDetailPage(),
    rebuildTree: () => innerPageRef.value?.rebuildTree(),
    clearSelection: () => innerPageRef.value?.clearSelection(),
    get form() {
      return innerPageRef.value?.form
    },
    get queryParams() {
      return innerPageRef.value?.queryParams
    },
    get selectedRows() {
      return innerPageRef.value?.selectedRows
    },
  }),
)
</script>

<style scoped>
.gen-runtime-page--loading {
  min-height: 200px;
}

.gen-runtime-page--error {
  padding: 16px;
}
</style>
