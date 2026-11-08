<template>
  <div class="form-designer" :class="{ 'form-designer--studio': studioActive }">
    <DesignerToolbar
      v-model:device="device"
      v-model:mode="mode"
      :can-undo="canUndo"
      :can-redo="canRedo"
      :studio="studioActive"
      :bound="isBoundHost"
      :gp="gp"
      @undo="undo"
      @redo="redo"
      @clear="clearAll"
      @demo="loadDemo"
      @import="importOpen = true"
      @export="exportJson"
      @toggle-studio="toggleStudio"
    />

    <div class="form-designer__body">
      <DesignerPalette
        :gp="gp"
        :bound="isBoundHost"
        :unused-fields="unusedPoolFields"
        @add="addFromPalette"
        @add-field="addPoolField"
      />

      <section class="form-designer__main">
        <DesignerCanvas
          :document="document"
          :device="device"
          :mode="mode"
          :selected-node-id="selectedNodeId"
          :selected-field-id="selectedFieldId"
          :preview-model="previewModel"
          :gp="gp"
          :node-label="nodeLabel"
          :protect-locked="isBoundHost"
          @select="onSelect"
          @select-field="onSelectField"
          @move="move"
          @move-child="onMoveChild"
          @remove="remove"
          @remove-child="onRemoveChild"
          @reorder="reorder"
          @drop-payload="onDropPayload"
          @update:preview-model="previewModel = $event"
        />
      </section>

      <DesignerProps
        v-model:props-tab="propsTab"
        :document="document"
        :selected-field-id="selectedFieldId"
        :selected-node-id="selectedNodeId"
        :bound="isBoundHost"
        :gp="gp"
        @patch-form="patchForm"
        @patch-field="patchField"
        @patch-node="patchNode"
      />
    </div>

    <el-dialog
      v-model="importOpen"
      :title="gp('importJson')"
      width="640px"
      destroy-on-close
      append-to-body
    >
      <el-input v-model="importText" type="textarea" :rows="14" />
      <template #footer>
        <el-button @click="importOpen = false">{{ gp('cancel') }}</el-button>
        <el-button type="primary" @click="applyImport">{{ gp('confirm') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="exportOpen"
      :title="gp('exportJson')"
      width="640px"
      destroy-on-close
      append-to-body
    >
      <el-input :model-value="exportText" type="textarea" :rows="14" readonly />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus'
import DesignerToolbar from '@/components/form-designer/designer/DesignerToolbar.vue'
import DesignerPalette from '@/components/form-designer/designer/DesignerPalette.vue'
import DesignerCanvas from '@/components/form-designer/designer/DesignerCanvas.vue'
import DesignerProps from '@/components/form-designer/designer/DesignerProps.vue'
import { findPaletteItem, type PaletteItem } from '@/components/form-designer/palette'
import { useGenI18n } from '@/composables/useGenI18n'
import { useFormDocumentHistory } from '@/composables/useFormDocumentHistory'
import { useFullscreen } from '@/composables/useFullscreen'
import formDesignerZhCN from '@/i18n/gen/formDesigner/zh-CN.json'
import formDesignerEnUS from '@/i18n/gen/formDesigner/en-US.json'
import type { GenI18nMessages } from '@/types/tool/genI18n'
import {
  appendFieldPreferContainer,
  applyFieldPatch,
  buildDemoFormDocument,
  cloneFormDocument,
  createEmptyFormDocument,
  createFieldDef,
  createModelFromDocument,
  findFieldById,
  findFieldContainerId,
  findLayoutNode,
  insertLayoutNode,
  moveChildInContainer,
  moveNode,
  nextFormNodeId,
  nodeHasLockedField,
  parseFormDocument,
  removeChildFromContainer,
  removeNode,
  reorderNodes,
} from '@/components/form-designer/schemaUtils'
import {
  composeBoundDocument,
  oaFieldToFormField,
  persistLayout,
  unusedFieldsInPool,
} from '@/components/form-designer/oaBridge'
import { useDesignerStudioStore } from '@/stores/designerStudio'
import type {
  FormDesignerDragPayload,
  FormDocument,
  FormFieldDef,
  FormLayoutNode,
  FormMeta,
  FormPreviewDevice,
} from '@/types/form-designer/schema'
import type { OaFieldDefVO } from '@/types/oa/oaPlatformTemplate'

const props = withDefaults(
  defineProps<{
    modelValue?: FormDocument
    /** sandbox = free palette; bound = OA field pool */
    hostMode?: 'sandbox' | 'bound'
    fieldPool?: OaFieldDefVO[]
  }>(),
  {
    hostMode: 'sandbox',
    fieldPool: () => [],
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: FormDocument]
}>()

const isBoundHost = computed(() => props.hostMode === 'bound')

const { gp } = useGenI18n('formDesigner', 'tool', {
  'zh-CN': formDesignerZhCN as GenI18nMessages,
  'en-US': formDesignerEnUS as GenI18nMessages,
})

const studioStore = useDesignerStudioStore()
const { active: studioActive } = storeToRefs(studioStore)
const { isFullscreen, toggle: toggleBrowserFullscreen } = useFullscreen()
const {
  canUndo,
  canRedo,
  push: pushHistory,
  undo: undoHistory,
  redo: redoHistory,
  reset: resetHistory,
} = useFormDocumentHistory()

/** Last JSON published to v-model — blocks echo that resetHistory + re-assigned nodes (lag / stale props). */
let lastPublishedJson = ''
/** First live span drag ticks push history once. */
let fieldPatchSession = false

const document = reactive<FormDocument>(
  cloneFormDocument(props.modelValue ?? createEmptyFormDocument()),
)
lastPublishedJson = JSON.stringify(cloneFormDocument(document))
const unusedPoolFields = computed(() =>
  isBoundHost.value ? unusedFieldsInPool(document, props.fieldPool) : [],
)
const device = ref<FormPreviewDevice>('pc')
const mode = ref<'design' | 'preview'>('design')
const propsTab = ref('form')
const selectedNodeId = ref<string>()
const selectedFieldId = ref<string>()
const previewModel = ref<Record<string, unknown>>(createModelFromDocument(document))
const importOpen = ref(false)
const exportOpen = ref(false)
const importText = ref('')
const exportText = ref('')
const applyingHistory = ref(false)

watch(
  () => props.modelValue,
  (val) => {
    if (!val || applyingHistory.value) {
      return
    }
    const json = JSON.stringify(val)
    if (json === lastPublishedJson) {
      return
    }
    lastPublishedJson = json
    syncDoc(cloneFormDocument(val))
    previewModel.value = createModelFromDocument(document)
    resetHistory()
    fieldPatchSession = false
  },
)

watch(
  document,
  () => {
    const cloned = cloneFormDocument(document)
    const json = JSON.stringify(cloned)
    if (json === lastPublishedJson) {
      return
    }
    lastPublishedJson = json
    emit('update:modelValue', cloned)
    const base = createModelFromDocument(document)
    previewModel.value = { ...base, ...previewModel.value }
  },
  { deep: true },
)

function syncDoc(next: FormDocument) {
  document.schemaVersion = next.schemaVersion
  document.form = next.form
  document.nodes = next.nodes
}

function commit(next: FormDocument) {
  pushHistory(cloneFormDocument(document))
  syncDoc(next)
}

function onSelect(nodeId: string) {
  selectedNodeId.value = nodeId
  const node = findLayoutNode(document, nodeId)
  if (node?.type === 'field') {
    selectedFieldId.value = node.field.id
    propsTab.value = 'field'
  } else if (node?.type === 'row' || node?.type === 'card') {
    // Select container itself — do not auto-focus first child (keeps chrome highlight correct).
    selectedFieldId.value = undefined
    propsTab.value = 'field'
  } else {
    selectedFieldId.value = undefined
    propsTab.value = 'field'
  }
}

function onSelectField(nodeId: string, fieldId: string) {
  selectedNodeId.value = nodeId
  selectedFieldId.value = fieldId
  propsTab.value = 'grid'
}

function onMoveChild(containerId: string, fieldId: string, delta: -1 | 1) {
  const next = moveChildInContainer(document, containerId, fieldId, delta)
  if (JSON.stringify(next.nodes) === JSON.stringify(document.nodes)) {
    return
  }
  commit(next)
}

function takeUnused(count: number): FormFieldDef[] {
  return unusedPoolFields.value.slice(0, count).map((f) => {
    const field = oaFieldToFormField(f)
    field.span = 8
    return field
  })
}

/**
 * Place field as root unless containerId is explicit (drop into row/card body).
 * Do NOT infer container from selection — that trapped every add inside the selected layout.
 */
function appendFieldToContainerOrRoot(
  field: FormFieldDef,
  atIndex?: number,
  containerId?: string,
) {
  const before = document.nodes.length
  const next = appendFieldPreferContainer(document, field, containerId, atIndex)
  commit(next)
  const placed = findFieldById(next, field.id) || flattenLastByCode(next, field.fieldCode)
  if (placed) {
    selectedFieldId.value = placed.id
    const cid = findFieldContainerId(next, placed.id)
    selectedNodeId.value =
      cid || next.nodes.find((n) => n.type === 'field' && n.field.id === placed.id)?.id
    propsTab.value = cid ? 'grid' : 'field'
  } else if (containerId) {
    selectedNodeId.value = containerId
    propsTab.value = 'field'
  } else if (next.nodes.length > before) {
    selectedNodeId.value = next.nodes[next.nodes.length - 1]?.id
    propsTab.value = 'field'
  }
}

function flattenLastByCode(doc: FormDocument, code: string): FormFieldDef | undefined {
  const all = doc.nodes.flatMap((n) => {
    if (n.type === 'field') return [n.field]
    if (n.type === 'row' || n.type === 'card') return n.children
    return []
  })
  return [...all].reverse().find((f) => f.fieldCode === code)
}

function addPoolField(oa: OaFieldDefVO, atIndex?: number, containerId?: string) {
  if (!isBoundHost.value) return
  appendFieldToContainerOrRoot(oaFieldToFormField(oa), atIndex, containerId)
}

function createLayoutNode(item: PaletteItem): FormLayoutNode | null {
  if (item.layoutKind === 'divider') {
    return { type: 'divider', id: nextFormNodeId('node'), title: gp('paletteDivider') }
  }
  if (item.layoutKind === 'card') {
    const children = isBoundHost.value ? takeUnused(3) : []
    return {
      type: 'card',
      id: nextFormNodeId('node'),
      title: gp('paletteCard'),
      children,
    }
  }
  if (item.layoutKind === 'row' || item.key === 'row') {
    if (isBoundHost.value) {
      const children = takeUnused(2)
      if (!children.length) {
        ElMessage.warning(gp('fieldPoolEmpty'))
        return null
      }
      return { type: 'row', id: nextFormNodeId('node'), children }
    }
    return {
      type: 'row',
      id: nextFormNodeId('node'),
      children: [
        createFieldDef({
          fieldCode: `field_${document.nodes.length + 1}a`,
          fieldName: gp('paletteInput'),
          componentType: 'input',
          span: 8,
        }),
        createFieldDef({
          fieldCode: `field_${document.nodes.length + 1}b`,
          fieldName: gp('paletteInput'),
          componentType: 'input',
          span: 8,
        }),
      ],
    }
  }
  return null
}

function addFromPalette(item: PaletteItem, atIndex?: number) {
  if (item.category === 'layout' || item.layoutKind) {
    const node = createLayoutNode(item)
    if (!node) return
    commit(insertLayoutNode(document, node, atIndex))
    selectedNodeId.value = node.id
    selectedFieldId.value = undefined
    propsTab.value = 'field'
    return
  }
  if (isBoundHost.value) {
    ElMessage.warning(gp('boundNoFreeControls'))
    return
  }
  if (!item.componentType) {
    return
  }
  const field = createFieldDef({
    fieldCode: `field_${document.nodes.length + 1}`,
    fieldName: gp(item.labelKey),
    componentType: item.componentType,
    fieldType: item.fieldType,
  })
  appendFieldToContainerOrRoot(field, atIndex)
}

function addFromPaletteAt(
  item: PaletteItem,
  atIndex?: number,
  containerId?: string,
) {
  if (item.category === 'layout' || item.layoutKind) {
    addFromPalette(item, atIndex)
    return
  }
  if (isBoundHost.value) {
    ElMessage.warning(gp('boundNoFreeControls'))
    return
  }
  if (!item.componentType) return
  const field = createFieldDef({
    fieldCode: `field_${document.nodes.length + 1}`,
    fieldName: gp(item.labelKey),
    componentType: item.componentType,
    fieldType: item.fieldType,
  })
  appendFieldToContainerOrRoot(field, atIndex, containerId)
}

function onDropPayload(payload: FormDesignerDragPayload, atIndex: number) {
  if (payload.kind === 'palette') {
    const item = findPaletteItem(payload.key)
    if (!item) return
    addFromPaletteAt(item, atIndex, payload.containerId)
    return
  }
  if (payload.kind === 'pool') {
    const oa = unusedPoolFields.value.find((f) => f.fieldCode === payload.fieldCode)
    if (!oa) {
      ElMessage.warning(gp('fieldPoolEmpty'))
      return
    }
    addPoolField(oa, atIndex, payload.containerId)
  }
}

function nodeLabel(node: FormLayoutNode): string {
  if (node.type === 'row') {
    const codes = node.children.map((c) => c.fieldCode).join(' + ')
    return codes ? `${gp('paletteRow')}: ${codes}` : gp('paletteRow')
  }
  if (node.type === 'card') {
    return `${gp('paletteCard')}: ${node.title || '—'}`
  }
  if (node.type === 'divider') {
    return `${gp('paletteDivider')}: ${node.title || '—'}`
  }
  return `${node.field.fieldName} (${node.field.fieldCode})`
}

function move(nodeId: string, delta: -1 | 1) {
  const next = moveNode(document, nodeId, delta)
  if (JSON.stringify(next.nodes) === JSON.stringify(document.nodes)) {
    return
  }
  commit(next)
}

function remove(nodeId: string) {
  const node = findLayoutNode(document, nodeId)
  if (!node) return
  if (isBoundHost.value && nodeHasLockedField(node)) {
    ElMessage.warning(gp('lockedCannotRemove'))
    return
  }
  commit(removeNode(document, nodeId))
  if (selectedNodeId.value === nodeId) {
    selectedNodeId.value = undefined
    selectedFieldId.value = undefined
  }
}

function onRemoveChild(containerId: string, fieldId: string) {
  const field = findFieldById(document, fieldId)
  if (!field) return
  if (isBoundHost.value && field.platformLocked) {
    ElMessage.warning(gp('lockedCannotRemove'))
    return
  }
  const next = removeChildFromContainer(document, containerId, fieldId)
  commit(next)
  if (selectedFieldId.value === fieldId) {
    const container = findLayoutNode(next, containerId)
    if (!container) {
      selectedNodeId.value = undefined
      selectedFieldId.value = undefined
    } else {
      selectedNodeId.value = containerId
      selectedFieldId.value = undefined
      propsTab.value = 'field'
    }
  }
}

function reorder(fromIndex: number, toIndex: number) {
  const next = reorderNodes(document, fromIndex, toIndex)
  if (JSON.stringify(next.nodes) === JSON.stringify(document.nodes)) {
    return
  }
  commit(next)
}

function patchForm(patch: Partial<FormMeta>) {
  pushHistory(cloneFormDocument(document))
  document.form = { ...document.form, ...patch }
  fieldPatchSession = false
}

function patchField(
  patch: Partial<FormFieldDef>,
  options?: { recordHistory?: boolean; endSession?: boolean },
) {
  const fieldId = selectedFieldId.value
  if (!fieldId) {
    return
  }
  const field = findFieldById(document, fieldId)
  if (!field) {
    return
  }
  let next: Partial<FormFieldDef> = patch
  if (isBoundHost.value) {
    next = {}
    if (patch.span !== undefined) next.span = patch.span
    if (patch.placeholder !== undefined) next.placeholder = patch.placeholder
    if (!Object.keys(next).length) {
      return
    }
  }
  const wantHistory = options?.recordHistory !== false
  if (wantHistory) {
    pushHistory(cloneFormDocument(document))
    fieldPatchSession = false
  } else if (!fieldPatchSession) {
    pushHistory(cloneFormDocument(document))
    fieldPatchSession = true
  }
  applyFieldPatch(field, next)
  if (options?.endSession) {
    fieldPatchSession = false
  }
}

function patchNode(patch: { title?: string }, options?: { endSession?: boolean }) {
  if (!selectedNodeId.value) return
  const node = findLayoutNode(document, selectedNodeId.value)
  if (!node || (node.type !== 'card' && node.type !== 'divider')) {
    return
  }
  if (patch.title !== undefined) {
    if (!fieldPatchSession) {
      pushHistory(cloneFormDocument(document))
      fieldPatchSession = true
    }
    node.title = patch.title
  }
  if (options?.endSession) {
    fieldPatchSession = false
  }
}

function clearAll() {
  if (isBoundHost.value) {
    commit(composeBoundDocument({ fields: props.fieldPool, layoutJson: null }))
  } else {
    commit(createEmptyFormDocument(document.form))
  }
  selectedNodeId.value = undefined
  selectedFieldId.value = undefined
  previewModel.value = createModelFromDocument(document)
}

function demoLabels() {
  return {
    basic: gp('demoBasic'),
    title: gp('demoTitle'),
    details: gp('demoDetails'),
    amount: gp('demoAmount'),
    applyDate: gp('demoApplyDate'),
    priority: gp('demoPriority'),
    reason: gp('demoReason'),
    urgent: gp('demoUrgent'),
  }
}

function loadDemo() {
  commit(buildDemoFormDocument(demoLabels()))
  selectedNodeId.value = document.nodes[0]?.id
  selectedFieldId.value = undefined
  previewModel.value = createModelFromDocument(document)
  fieldPatchSession = false
}

function exportJson() {
  exportText.value = JSON.stringify(cloneFormDocument(document), null, 2)
  exportOpen.value = true
}

function applyImport() {
  try {
    const parsed = parseFormDocument(JSON.parse(importText.value))
    commit(parsed)
    previewModel.value = createModelFromDocument(document)
    importOpen.value = false
    ElMessage.success(gp('importOk'))
  } catch {
    ElMessage.error(gp('importFail'))
  }
}

function undo() {
  const prev = undoHistory(cloneFormDocument(document))
  if (!prev) {
    return
  }
  applyingHistory.value = true
  syncDoc(prev)
  previewModel.value = createModelFromDocument(document)
  applyingHistory.value = false
}

function redo() {
  const next = redoHistory(cloneFormDocument(document))
  if (!next) {
    return
  }
  applyingHistory.value = true
  syncDoc(next)
  previewModel.value = createModelFromDocument(document)
  applyingHistory.value = false
}

async function toggleStudio() {
  if (studioActive.value) {
    studioStore.exit()
    if (isFullscreen.value) {
      await toggleBrowserFullscreen()
    }
  } else {
    studioStore.enter()
  }
}

function onKeydown(event: KeyboardEvent) {
  const target = event.target as HTMLElement | null
  const tag = target?.tagName?.toLowerCase()
  const typing = tag === 'input' || tag === 'textarea' || target?.isContentEditable

  if (event.key === 'Escape' && studioActive.value) {
    event.preventDefault()
    void toggleStudio()
    return
  }

  if (typing) {
    return
  }

  const mod = event.ctrlKey || event.metaKey
  if (!mod) {
    return
  }
  const key = event.key.toLowerCase()
  if (key === 'z' && !event.shiftKey) {
    event.preventDefault()
    undo()
  } else if (key === 'y' || (key === 'z' && event.shiftKey)) {
    event.preventDefault()
    redo()
  }
}

onMounted(() => {
  window.addEventListener('keydown', onKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', onKeydown)
  studioStore.exit()
})

defineExpose({
  getDocument: () => cloneFormDocument(document),
  /** Bound Save Gate: presentation-only layout_json */
  getLayoutJson: () => persistLayout(document),
})
</script>

<style scoped>
.form-designer {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  overflow: hidden;
}
.form-designer--studio {
  position: fixed;
  inset: 0;
  z-index: 2000;
  height: 100vh;
  min-height: 100vh;
  border-radius: 0;
  border: none;
}
.form-designer__body {
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr) 300px;
  flex: 1 1 0;
  min-height: 0;
  overflow: hidden;
}
.form-designer__main {
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
  overflow: hidden;
}
</style>
