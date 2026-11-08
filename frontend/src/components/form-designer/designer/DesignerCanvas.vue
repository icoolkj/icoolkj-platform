<template>
  <div
    class="designer-canvas-wrap"
    :class="{ 'is-external-drag': externalDragOver }"
    @dragover.prevent="onWrapDragOver"
    @dragleave="onWrapDragLeave"
    @drop.prevent="onWrapDrop"
  >
    <div class="designer-canvas" :class="[`is-${device}`, { 'is-drop-active': externalDragOver }]">
      <template v-if="mode === 'preview'">
        <FormRuntime
          :model-value="previewModel"
          :document="document"
          :device="device"
          :empty-text="gp('empty')"
          @update:model-value="emit('update:previewModel', $event)"
        />
      </template>
      <template v-else>
        <div v-if="!document.nodes.length" class="designer-canvas__empty">
          <el-icon :size="40" class="designer-canvas__empty-icon"><EditPen /></el-icon>
          <p>{{ gp('empty') }}</p>
          <p class="designer-canvas__empty-sub">{{ gp('emptyHint') }}</p>
        </div>
        <div
          v-for="(node, index) in document.nodes"
          :key="node.id"
          class="designer-canvas__node"
          :class="{
            'is-active': isNodeChromeActive(node),
            'is-child-focus': Boolean(selectedChildMeta(node)),
            'is-dragging': dragFromIndex === index,
            'is-drop-target': dropHighlightIndex === index,
            'is-slot-drop': slotDropContainerId === node.id,
            [`is-kind-${node.type}`]: true,
          }"
          draggable="true"
          @click.stop="onSelectChrome(node.id)"
          @dragstart="onReorderDragStart(index, $event)"
          @dragover.prevent.stop="onNodeDragOver(index, $event)"
          @dragleave="onNodeDragLeave(index)"
          @drop.prevent.stop="onNodeDrop(index, $event)"
          @dragend="onDragEnd"
        >
          <div class="designer-canvas__node-bar">
            <span class="designer-canvas__drag" :title="gp('dragSort')">
              <el-icon><Rank /></el-icon>
              {{ nodeLabel(node) }}
              <template v-if="selectedChildMeta(node)">
                · {{ selectedChildMeta(node)!.fieldName }}
                · {{ selectedChildMeta(node)!.span }}/24
              </template>
            </span>
            <span class="designer-canvas__node-ops">
              <template v-if="selectedChildMeta(node)">
                <el-button
                  link
                  type="primary"
                  size="small"
                  @click.stop="emit('moveChild', node.id, selectedChildMeta(node)!.id, -1)"
                >
                  ←
                </el-button>
                <el-button
                  link
                  type="primary"
                  size="small"
                  @click.stop="emit('moveChild', node.id, selectedChildMeta(node)!.id, 1)"
                >
                  →
                </el-button>
                <el-button
                  v-if="canRemoveChild(selectedChildMeta(node)!)"
                  link
                  type="danger"
                  size="small"
                  @click.stop="emit('removeChild', node.id, selectedChildMeta(node)!.id)"
                >
                  {{ gp('removeField') }}
                </el-button>
                <el-tag v-else size="small" type="info" effect="plain">{{ gp('locked') }}</el-tag>
              </template>
              <el-button link type="primary" size="small" @click.stop="emit('move', node.id, -1)">
                ↑
              </el-button>
              <el-button link type="primary" size="small" @click.stop="emit('move', node.id, 1)">
                ↓
              </el-button>
              <el-button
                v-if="canRemoveNode(node)"
                link
                type="danger"
                size="small"
                @click.stop="emit('remove', node.id)"
              >
                {{ selectedChildMeta(node) ? gp('removeContainer') : gp('remove') }}
              </el-button>
              <el-tag
                v-else-if="!selectedChildMeta(node)"
                size="small"
                type="info"
                effect="plain"
              >
                {{ gp('locked') }}
              </el-tag>
            </span>
          </div>

          <!-- Same FormRuntime layout as preview (WYSIWYG) -->
          <div
            class="designer-canvas__body"
            :class="{ 'is-container': node.type === 'row' || node.type === 'card' }"
            @dragover.prevent.stop="
              node.type === 'row' || node.type === 'card' ? onSlotDragOver(node.id, $event) : undefined
            "
            @dragleave="
              node.type === 'row' || node.type === 'card' ? onSlotDragLeave(node.id, $event) : undefined
            "
            @drop.prevent.stop="
              node.type === 'row' || node.type === 'card' ? onSlotDrop(node.id, $event) : undefined
            "
          >
            <FormRuntime
              :model-value="previewModel"
              :document="singleNodeDoc(node)"
              :device="device"
              design
              :selected-field-id="selectedFieldId"
              :selected-node-id="selectedNodeId"
              empty-text=""
              @select-field="(_nid, fid) => onSelectFieldChrome(node.id, fid)"
              @select-node="onSelectChrome(node.id)"
              @update:model-value="emit('update:previewModel', $event)"
            />
            <div
              v-if="(node.type === 'row' || node.type === 'card') && !node.children.length"
              class="designer-canvas__slot-empty"
            >
              {{ gp('dropIntoContainer') }}
            </div>
            <div
              v-else-if="
                (node.type === 'row' || node.type === 'card') &&
                sumChildrenSpan(node.children) < 24
              "
              class="designer-canvas__slot-hint"
            >
              {{ gp('gridRemain', { n: 24 - sumChildrenSpan(node.children) }) }}
            </div>
          </div>
        </div>
        <div
          v-if="document.nodes.length"
          class="designer-canvas__tail"
          :class="{ 'is-drop-target': dropHighlightIndex === document.nodes.length }"
          @dragover.prevent.stop="onTailDragOver($event)"
          @dragleave="onTailDragLeave"
          @drop.prevent.stop="onTailDrop($event)"
        >
          {{ gp('dropHere') }}
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { EditPen, Rank } from '@element-plus/icons-vue'
import FormRuntime from '@/components/form-designer/FormRuntime.vue'
import {
  nodeHasLockedField,
  resolveFieldSpan,
  sumChildrenSpan,
} from '@/components/form-designer/schemaUtils'
import {
  FORM_DESIGNER_DND_MIME,
  type FormDesignerDragPayload,
  type FormDocument,
  type FormLayoutNode,
  type FormPreviewDevice,
} from '@/types/form-designer/schema'

const props = withDefaults(
  defineProps<{
    document: FormDocument
    device: FormPreviewDevice
    mode: 'design' | 'preview'
    selectedNodeId?: string
    selectedFieldId?: string
    previewModel: Record<string, unknown>
    gp: (key: string, params?: Record<string, string | number>) => string
    nodeLabel: (node: FormLayoutNode) => string
    protectLocked?: boolean
  }>(),
  { protectLocked: false },
)

const emit = defineEmits<{
  select: [nodeId: string]
  selectField: [nodeId: string, fieldId: string]
  move: [nodeId: string, delta: -1 | 1]
  moveChild: [containerId: string, fieldId: string, delta: -1 | 1]
  remove: [nodeId: string]
  removeChild: [containerId: string, fieldId: string]
  reorder: [fromIndex: number, toIndex: number]
  dropPayload: [payload: FormDesignerDragPayload, atIndex: number]
  'update:previewModel': [value: Record<string, unknown>]
}>()

const dragFromIndex = ref<number | null>(null)
const dropHighlightIndex = ref<number | null>(null)
const externalDragOver = ref(false)
const slotDropContainerId = ref<string | null>(null)

function clearDropHighlights() {
  dragFromIndex.value = null
  dropHighlightIndex.value = null
  externalDragOver.value = false
  slotDropContainerId.value = null
}

function onSelectChrome(nodeId: string) {
  clearDropHighlights()
  emit('select', nodeId)
}

function onSelectFieldChrome(nodeId: string, fieldId: string) {
  clearDropHighlights()
  emit('selectField', nodeId, fieldId)
}

function singleNodeDoc(node: FormLayoutNode): FormDocument {
  return {
    schemaVersion: 1,
    form: props.document.form,
    nodes: [node],
  }
}

function canRemoveNode(node: FormLayoutNode): boolean {
  if (!props.protectLocked) {
    return true
  }
  return !nodeHasLockedField(node)
}

/** Container chrome only when the container itself is selected — not when a child field is focused. */
function isNodeChromeActive(node: FormLayoutNode): boolean {
  if (node.type === 'row' || node.type === 'card') {
    return props.selectedNodeId === node.id && !props.selectedFieldId
  }
  if (node.type === 'field') {
    if (props.selectedFieldId) {
      return node.field.id === props.selectedFieldId
    }
    return props.selectedNodeId === node.id
  }
  return props.selectedNodeId === node.id
}

function selectedChildMeta(node: FormLayoutNode) {
  if (!props.selectedFieldId || (node.type !== 'row' && node.type !== 'card')) {
    return null
  }
  const child = node.children.find((c) => c.id === props.selectedFieldId)
  if (!child) return null
  return {
    id: child.id,
    fieldName: child.fieldName,
    span: resolveFieldSpan(child),
    platformLocked: Boolean(child.platformLocked),
  }
}

function canRemoveChild(child: { platformLocked: boolean }): boolean {
  if (!props.protectLocked) return true
  return !child.platformLocked
}

function readPayload(event: DragEvent): FormDesignerDragPayload | null {
  const raw = event.dataTransfer?.getData(FORM_DESIGNER_DND_MIME)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw) as FormDesignerDragPayload
  } catch {
    return null
  }
}

function onReorderDragStart(index: number, event: DragEvent) {
  dragFromIndex.value = index
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move'
    event.dataTransfer.setData(
      FORM_DESIGNER_DND_MIME,
      JSON.stringify({ kind: 'reorder', index } satisfies FormDesignerDragPayload),
    )
    event.dataTransfer.setData('text/plain', String(index))
  }
}

function onWrapDragOver(event: DragEvent) {
  if (props.mode !== 'design') return
  if (event.dataTransfer?.types.includes(FORM_DESIGNER_DND_MIME)) {
    externalDragOver.value = true
    event.dataTransfer.dropEffect = dragFromIndex.value === null ? 'copy' : 'move'
  }
}

function onWrapDragLeave(event: DragEvent) {
  const related = event.relatedTarget as Node | null
  if (related && (event.currentTarget as HTMLElement).contains(related)) {
    return
  }
  externalDragOver.value = false
  slotDropContainerId.value = null
}

function onWrapDrop(event: DragEvent) {
  if (props.mode !== 'design') return
  const payload = readPayload(event)
  clearDropHighlights()
  if (!payload) return
  if (payload.kind === 'reorder') {
    emit('reorder', payload.index, props.document.nodes.length - 1)
    return
  }
  emit('dropPayload', payload, props.document.nodes.length)
}

function onNodeDragOver(index: number, event: DragEvent) {
  dropHighlightIndex.value = index
  if (event.dataTransfer) {
    event.dataTransfer.dropEffect = dragFromIndex.value === null ? 'copy' : 'move'
  }
}

function onNodeDragLeave(index: number) {
  if (dropHighlightIndex.value === index) {
    dropHighlightIndex.value = null
  }
}

function onNodeDrop(toIndex: number, event: DragEvent) {
  const payload = readPayload(event)
  clearDropHighlights()
  if (!payload) {
    return
  }
  if (payload.kind === 'reorder') {
    if (payload.index !== toIndex) {
      emit('reorder', payload.index, toIndex)
    }
    return
  }
  // Drop on a node chrome / edge = insert as sibling at this index (root).
  // Drop into container body only via onSlotDrop (explicit containerId).
  emit('dropPayload', payload, toIndex)
}

function onSlotDragOver(containerId: string, event: DragEvent) {
  slotDropContainerId.value = containerId
  if (event.dataTransfer) {
    event.dataTransfer.dropEffect = 'copy'
  }
}

function onSlotDragLeave(containerId: string, event: DragEvent) {
  const related = event.relatedTarget as Node | null
  if (related && (event.currentTarget as HTMLElement).contains(related)) {
    return
  }
  if (slotDropContainerId.value === containerId) {
    slotDropContainerId.value = null
  }
}

function onSlotDrop(containerId: string, event: DragEvent) {
  const payload = readPayload(event)
  clearDropHighlights()
  if (!payload || payload.kind === 'reorder' || payload.kind === 'childReorder') {
    return
  }
  const idx = props.document.nodes.findIndex((n) => n.id === containerId)
  emit('dropPayload', { ...payload, containerId }, idx >= 0 ? idx : props.document.nodes.length)
}

function onTailDragOver(event: DragEvent) {
  dropHighlightIndex.value = props.document.nodes.length
  if (event.dataTransfer) {
    event.dataTransfer.dropEffect = dragFromIndex.value === null ? 'copy' : 'move'
  }
}

function onTailDragLeave() {
  if (dropHighlightIndex.value === props.document.nodes.length) {
    dropHighlightIndex.value = null
  }
}

function onTailDrop(event: DragEvent) {
  const payload = readPayload(event)
  clearDropHighlights()
  if (!payload) return
  if (payload.kind === 'reorder') {
    emit('reorder', payload.index, props.document.nodes.length - 1)
    return
  }
  emit('dropPayload', payload, props.document.nodes.length)
}

function onDragEnd() {
  clearDropHighlights()
}

onMounted(() => {
  // Palette dragend does not hit canvas nodes — clear sticky slot/drop chrome globally.
  window.addEventListener('dragend', clearDropHighlights)
  window.addEventListener('drop', clearDropHighlights)
})

onUnmounted(() => {
  window.removeEventListener('dragend', clearDropHighlights)
  window.removeEventListener('drop', clearDropHighlights)
})
</script>

<style scoped>
.designer-canvas-wrap {
  flex: 1 1 0;
  min-height: 0;
  overflow: auto;
  padding: 20px 16px;
  background:
    radial-gradient(circle at 1px 1px, var(--el-border-color-extra-light) 1px, transparent 0)
      0 0 / 16px 16px,
    var(--el-fill-color-light);
}
.designer-canvas-wrap.is-external-drag .designer-canvas {
  border-color: var(--el-color-primary);
}
.designer-canvas {
  margin: 0 auto;
  min-height: 420px;
  padding: 20px 16px;
  background: var(--el-bg-color);
  border: 1px dashed var(--el-color-primary-light-5);
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  transition:
    width 0.2s ease,
    border-color 0.15s ease,
    box-shadow 0.15s ease;
}
.designer-canvas.is-drop-active {
  box-shadow: 0 0 0 2px var(--el-color-primary-light-7);
}
.designer-canvas.is-pc {
  width: 100%;
  max-width: 920px;
}
.designer-canvas.is-tablet {
  width: 768px;
  max-width: 100%;
}
.designer-canvas.is-mobile {
  width: 375px;
  max-width: 100%;
}
.designer-canvas__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 320px;
  color: var(--el-text-color-secondary);
  gap: 6px;
}
.designer-canvas__empty-icon {
  opacity: 0.45;
}
.designer-canvas__empty p {
  margin: 0;
  font-size: 13px;
}
.designer-canvas__empty-sub {
  font-size: 12px;
  opacity: 0.85;
}
.designer-canvas__node {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  padding: 8px 10px 10px;
  margin-bottom: 10px;
  background: var(--el-fill-color-blank);
  cursor: grab;
  transition:
    border-color 0.15s ease,
    box-shadow 0.15s ease,
    background 0.15s ease;
}
.designer-canvas__node:hover {
  border-color: var(--el-color-primary-light-5);
}
.designer-canvas__node:active {
  cursor: grabbing;
}
.designer-canvas__node.is-active {
  border-style: solid;
  border-color: var(--el-color-primary);
  box-shadow: 0 0 0 1px var(--el-color-primary-light-7);
}
/* Child field focused: emphasize bar only — do not wash the whole container. */
.designer-canvas__node.is-child-focus:not(.is-active) .designer-canvas__node-bar {
  color: var(--el-color-primary);
}
.designer-canvas__node.is-dragging {
  opacity: 0.55;
}
.designer-canvas__node.is-drop-target {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}
.designer-canvas__node.is-slot-drop .designer-canvas__body.is-container {
  outline: 1px dashed var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}
.designer-canvas__node.is-kind-card {
  border-style: solid;
  border-color: var(--el-border-color);
}
.designer-canvas__node-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 8px;
}
.designer-canvas__drag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.designer-canvas__node-ops {
  display: inline-flex;
  gap: 2px;
  flex-shrink: 0;
}
.designer-canvas__body {
  min-width: 0;
}
.designer-canvas__body.is-container {
  border-radius: 6px;
  min-height: 40px;
}
.designer-canvas__slot-empty,
.designer-canvas__slot-hint {
  text-align: center;
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  padding: 6px 0 2px;
}
.designer-canvas__tail {
  margin-top: 4px;
  padding: 10px;
  text-align: center;
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  border: 1px dashed transparent;
  border-radius: 6px;
}
.designer-canvas__tail.is-drop-target {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}
</style>
