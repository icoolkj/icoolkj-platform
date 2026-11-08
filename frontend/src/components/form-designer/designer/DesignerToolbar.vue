<template>
  <div class="designer-toolbar">
    <div class="designer-toolbar__group">
      <el-button-group>
        <el-button :disabled="!canUndo" :title="gp('undo')" @click="emit('undo')">
          <el-icon><RefreshLeft /></el-icon>
        </el-button>
        <el-button :disabled="!canRedo" :title="gp('redo')" @click="emit('redo')">
          <el-icon><RefreshRight /></el-icon>
        </el-button>
      </el-button-group>
      <el-button @click="emit('clear')">{{ gp('clear') }}</el-button>
      <el-button v-if="!bound" @click="emit('demo')">{{ gp('demo') }}</el-button>
    </div>

    <div class="designer-toolbar__group">
      <el-radio-group :model-value="mode" @update:model-value="onMode">
        <el-radio-button value="design">{{ gp('modeDesign') }}</el-radio-button>
        <el-radio-button value="preview">{{ gp('modePreview') }}</el-radio-button>
      </el-radio-group>
      <el-radio-group :model-value="device" @update:model-value="onDevice">
        <el-radio-button value="pc">
          <el-icon><Monitor /></el-icon>
          <span class="designer-toolbar__label">{{ gp('devicePc') }}</span>
        </el-radio-button>
        <el-radio-button value="tablet">
          <el-icon><Notebook /></el-icon>
          <span class="designer-toolbar__label">{{ gp('deviceTablet') }}</span>
        </el-radio-button>
        <el-radio-button value="mobile">
          <el-icon><Cellphone /></el-icon>
          <span class="designer-toolbar__label">{{ gp('deviceMobile') }}</span>
        </el-radio-button>
      </el-radio-group>
    </div>

    <div class="designer-toolbar__group designer-toolbar__group--end">
      <el-button v-if="!bound" @click="emit('import')">{{ gp('importJson') }}</el-button>
      <el-button @click="emit('export')">{{ gp('exportJson') }}</el-button>
      <el-button type="primary" @click="emit('toggleStudio')">
        <el-icon class="designer-toolbar__fs-icon">
          <ScaleToOriginal v-if="studio" />
          <FullScreen v-else />
        </el-icon>
        {{ studio ? gp('exitStudio') : gp('enterStudio') }}
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  Cellphone,
  FullScreen,
  Monitor,
  Notebook,
  RefreshLeft,
  RefreshRight,
  ScaleToOriginal,
} from '@element-plus/icons-vue'
import type { FormPreviewDevice } from '@/types/form-designer/schema'

withDefaults(
  defineProps<{
    device: FormPreviewDevice
    mode: 'design' | 'preview'
    canUndo: boolean
    canRedo: boolean
    studio: boolean
    gp: (key: string) => string
    bound?: boolean
  }>(),
  { bound: false },
)

const emit = defineEmits<{
  'update:device': [value: FormPreviewDevice]
  'update:mode': [value: 'design' | 'preview']
  undo: []
  redo: []
  clear: []
  demo: []
  import: []
  export: []
  toggleStudio: []
}>()

function onMode(value: string | number | boolean | undefined) {
  if (value === 'design' || value === 'preview') {
    emit('update:mode', value)
  }
}

function onDevice(value: string | number | boolean | undefined) {
  if (value === 'pc' || value === 'tablet' || value === 'mobile') {
    emit('update:device', value)
  }
}
</script>

<style scoped>
.designer-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  flex-shrink: 0;
  gap: 16px;
  min-height: 56px;
  padding: 10px 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color);
}
.designer-toolbar__group {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}
.designer-toolbar__group--end {
  margin-left: auto;
}
.designer-toolbar__label {
  margin-left: 6px;
}
.designer-toolbar__fs-icon {
  margin-right: 6px;
}
.designer-toolbar :deep(.el-radio-button__inner) {
  display: inline-flex;
  align-items: center;
}
</style>
