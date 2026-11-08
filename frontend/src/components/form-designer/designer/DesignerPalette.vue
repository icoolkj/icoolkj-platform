<template>
  <aside class="designer-palette">
    <template v-if="bound">
      <div class="designer-palette__title">{{ gp('fieldPool') }}</div>
      <p class="designer-palette__hint">{{ gp('fieldPoolHint') }}</p>
      <div v-if="!unusedFields.length" class="designer-palette__empty">
        {{ gp('fieldPoolEmpty') }}
      </div>
      <div v-else class="designer-palette__grid">
        <button
          v-for="field in unusedFields"
          :key="field.fieldCode"
          type="button"
          class="designer-palette__item"
          draggable="true"
          @click="emit('addField', field)"
          @dragstart="onPoolDragStart(field, $event)"
          @dragend="onDragEnd"
        >
          <el-icon :size="18"><DocumentAdd /></el-icon>
          <span>{{ field.fieldName }}</span>
          <span class="designer-palette__code">{{ field.fieldCode }}</span>
        </button>
      </div>
      <div class="designer-palette__title designer-palette__title--spaced">
        {{ gp('paletteLayout') }}
      </div>
      <div class="designer-palette__grid">
        <button
          v-for="item in layoutPalette"
          :key="item.key"
          type="button"
          class="designer-palette__item"
          draggable="true"
          @click="emit('add', item)"
          @dragstart="onPaletteDragStart(item, $event)"
          @dragend="onDragEnd"
        >
          <el-icon :size="18">
            <component :is="resolveIconComponent(item.icon)" />
          </el-icon>
          <span>{{ gp(item.labelKey) }}</span>
        </button>
      </div>
    </template>
    <el-collapse v-else v-model="openGroups">
      <el-collapse-item name="basic">
        <template #title>
          <span class="designer-palette__title">{{ gp('paletteBasic') }}</span>
        </template>
        <div class="designer-palette__grid">
          <button
            v-for="item in basicPalette"
            :key="item.key"
            type="button"
            class="designer-palette__item"
            draggable="true"
            @click="emit('add', item)"
            @dragstart="onPaletteDragStart(item, $event)"
            @dragend="onDragEnd"
          >
            <el-icon :size="18">
              <component :is="resolveIconComponent(item.icon)" />
            </el-icon>
            <span>{{ gp(item.labelKey) }}</span>
          </button>
        </div>
      </el-collapse-item>
      <el-collapse-item name="layout">
        <template #title>
          <span class="designer-palette__title">{{ gp('paletteLayout') }}</span>
        </template>
        <div class="designer-palette__grid">
          <button
            v-for="item in layoutPalette"
            :key="item.key"
            type="button"
            class="designer-palette__item"
            draggable="true"
            @click="emit('add', item)"
            @dragstart="onPaletteDragStart(item, $event)"
            @dragend="onDragEnd"
          >
            <el-icon :size="18">
              <component :is="resolveIconComponent(item.icon)" />
            </el-icon>
            <span>{{ gp(item.labelKey) }}</span>
          </button>
        </div>
      </el-collapse-item>
    </el-collapse>
  </aside>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { DocumentAdd } from '@element-plus/icons-vue'
import { FORM_DESIGNER_PALETTE, type PaletteItem } from '@/components/form-designer/palette'
import { resolveIconComponent } from '@/utils/icon'
import {
  FORM_DESIGNER_DND_MIME,
  type FormDesignerDragPayload,
} from '@/types/form-designer/schema'
import type { OaFieldDefVO } from '@/types/oa/oaPlatformTemplate'

withDefaults(
  defineProps<{
    gp: (key: string) => string
    bound?: boolean
    unusedFields?: OaFieldDefVO[]
  }>(),
  {
    bound: false,
    unusedFields: () => [],
  },
)

const emit = defineEmits<{
  add: [item: PaletteItem]
  addField: [field: OaFieldDefVO]
}>()

const openGroups = ref(['basic', 'layout'])
const basicPalette = FORM_DESIGNER_PALETTE.filter((p) => p.category === 'basic')
const layoutPalette = FORM_DESIGNER_PALETTE.filter((p) => p.category === 'layout')

function writePayload(event: DragEvent, payload: FormDesignerDragPayload) {
  if (!event.dataTransfer) {
    return
  }
  event.dataTransfer.effectAllowed = 'copyMove'
  event.dataTransfer.setData(FORM_DESIGNER_DND_MIME, JSON.stringify(payload))
  event.dataTransfer.setData('text/plain', payload.kind)
}

function onPaletteDragStart(item: PaletteItem, event: DragEvent) {
  writePayload(event, { kind: 'palette', key: item.key })
}

function onPoolDragStart(field: OaFieldDefVO, event: DragEvent) {
  writePayload(event, { kind: 'pool', fieldCode: field.fieldCode })
}

function onDragEnd() {
  /* no-op; canvas clears its own highlight */
}
</script>

<style scoped>
.designer-palette {
  border-right: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color);
  min-height: 0;
  overflow: auto;
  padding: 8px 10px 16px;
}
.designer-palette__title {
  font-size: 13px;
  font-weight: 600;
}
.designer-palette__title--spaced {
  margin-top: 16px;
  margin-bottom: 8px;
}
.designer-palette__hint {
  margin: 6px 0 10px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.4;
}
.designer-palette__empty {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  padding: 12px 0;
}
.designer-palette__grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}
.designer-palette__item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  min-height: 64px;
  padding: 8px 4px;
  border: 1px solid var(--el-border-color);
  border-radius: 6px;
  background: var(--el-fill-color-blank);
  font-size: 12px;
  color: var(--el-text-color-regular);
  cursor: grab;
  user-select: none;
}
.designer-palette__item:active {
  cursor: grabbing;
}
.designer-palette__item:hover {
  border-color: var(--el-color-primary);
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}
.designer-palette__code {
  font-size: 10px;
  opacity: 0.7;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
}
.designer-palette :deep(.el-collapse-item__header) {
  height: 40px;
  line-height: 40px;
  border-bottom: none;
}
.designer-palette :deep(.el-collapse-item__wrap) {
  border-bottom: none;
}
.designer-palette :deep(.el-collapse-item__content) {
  padding-bottom: 12px;
}
</style>
