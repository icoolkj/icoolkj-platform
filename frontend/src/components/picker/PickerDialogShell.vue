<template>
  <el-dialog
    :model-value="modelValue"
    :title="title"
    :width="width"
    destroy-on-close
    :close-on-click-modal="false"
    class="picker-dialog-shell"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div class="picker-dialog-body">
      <slot />
    </div>
    <template #footer>
      <slot name="footer">
        <div class="picker-dialog-footer">
          <span v-if="showSelectedCount" class="picker-summary-inline">
            {{ selectedText }}: {{ selectedCount }}
          </span>
          <div class="picker-dialog-footer__actions">
            <el-button @click="emit('update:modelValue', false)">{{ cancelText }}</el-button>
            <el-button type="primary" @click="emit('confirm')">{{ confirmText }}</el-button>
          </div>
        </div>
      </slot>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { PICKER_DIALOG_WIDTH } from './constants'

withDefaults(
  defineProps<{
    modelValue: boolean
    title: string
    width?: string
    cancelText?: string
    confirmText?: string
    selectedText?: string
    selectedCount?: number
    showSelectedCount?: boolean
  }>(),
  {
    width: PICKER_DIALOG_WIDTH,
    cancelText: 'Cancel',
    confirmText: 'Confirm',
    selectedText: 'Selected',
    selectedCount: 0,
    showSelectedCount: true,
  },
)

const emit = defineEmits<{
  'update:modelValue': [boolean]
  confirm: []
}>()
</script>

<style scoped lang="scss">
@use './picker.scss';
</style>
