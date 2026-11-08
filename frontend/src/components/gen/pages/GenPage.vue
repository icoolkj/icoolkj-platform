<!--
  Gen 统一入口：由 view.page.layout 经 patterns/registry 映射形态积木。
  薄壳页挂载 GenRuntimePage；需手写三元组时可直接挂载 GenPage。
-->
<template>
  <component
    :is="patternBlock"
    ref="layoutRef"
    v-bind="patternProps"
    @selection-change="(rows: Record<string, unknown>[]) => emit('selection-change', rows)"
    @open-form="(row?: Record<string, unknown>) => emit('open-form', row)"
  >
    <template v-for="(_, slotName) in slots" #[slotName]="slotData">
      <slot :name="slotName" v-bind="slotData ?? {}" />
    </template>
  </component>
</template>

<script setup lang="ts">
import { computed, ref, useAttrs, useSlots } from 'vue'
import {
  buildPatternBlockProps,
  resolveLayoutFromView,
  resolvePatternBlock,
} from '@/components/gen/patterns/registry'
import { createGenPageExpose } from '@/components/gen/patterns/expose'
import type { GenPageEmits, GenPageExpose, GenPagePatternProps } from '@/components/gen/patterns/types'
import { warnUnroutedLayout } from '@/components/gen/registry/page-patterns'

const props = defineProps<GenPagePatternProps>()

defineOptions({ inheritAttrs: false })

const emit = defineEmits<GenPageEmits>()

const attrs = useAttrs()
const slots = useSlots()

const layout = computed(() => {
  const fromView = props.fileView.page?.layout
  warnUnroutedLayout(fromView, props.fileView.meta?.businessName)
  return resolveLayoutFromView(props.fileView)
})

const patternBlock = computed(() => resolvePatternBlock(layout.value))

/** 事件由模板显式转发，避免 attrs 与 emit 双触发 */
const patternProps = computed(() => ({
  ...buildPatternBlockProps(layout.value, props),
  ...nonEventAttrs(attrs),
}))

function nonEventAttrs(raw: Record<string, unknown>): Record<string, unknown> {
  return Object.fromEntries(
    Object.entries(raw).filter(([key]) => !(key.startsWith('on') && key.length > 2 && key[2] === key[2].toUpperCase())),
  )
}

const layoutRef = ref<Partial<GenPageExpose>>()

defineExpose(
  createGenPageExpose({
    openForm: (row?: Record<string, unknown>) => layoutRef.value?.openForm?.(row),
    openAddChild: (parent) => layoutRef.value?.openAddChild?.(parent),
    loadPage: () => layoutRef.value?.loadPage?.(),
    loadMasterPage: () => layoutRef.value?.loadMasterPage?.(),
    loadDetailPage: () => layoutRef.value?.loadDetailPage?.(),
    rebuildTree: () => layoutRef.value?.rebuildTree?.(),
    clearSelection: () => layoutRef.value?.clearSelection?.(),
    get form() {
      return layoutRef.value?.form
    },
    get queryParams() {
      return layoutRef.value?.queryParams
    },
    get selectedRows() {
      return layoutRef.value?.selectedRows
    },
  }),
)
</script>
