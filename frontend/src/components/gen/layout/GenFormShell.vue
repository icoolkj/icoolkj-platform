<template>
  <el-dialog
    v-if="formMode === 'dialog'"
    v-model="visible"
    :title="title"
    :width="dialogWidth"
    :close-on-click-modal="closeOnClickModal"
    :close-on-press-escape="closeOnPressEscape"
    destroy-on-close
  >
    <slot />
    <template v-if="$slots.footer" #footer>
      <slot name="footer" />
    </template>
  </el-dialog>

  <el-drawer
    v-else
    v-model="visible"
    :title="title"
    :size="drawerSize"
    :close-on-click-modal="closeOnClickModal"
    :close-on-press-escape="closeOnPressEscape"
    destroy-on-close
  >
    <slot />
    <template v-if="$slots.footer" #footer>
      <slot name="footer" />
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useLayoutStore } from '@/stores/layout'

const props = withDefaults(
  defineProps<{
    modelValue: boolean
    title: string
    width?: number
    closeOnClickModal?: boolean
    closeOnPressEscape?: boolean
  }>(),
  {
    width: 860,
    closeOnClickModal: false,
    closeOnPressEscape: false,
  },
)

const emit = defineEmits<{ 'update:modelValue': [boolean] }>()

const layoutStore = useLayoutStore()
const { formMode } = storeToRefs(layoutStore)

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
})

const dialogWidth = computed(() => `${props.width}px`)
const drawerSize = computed(() => `${Math.min(props.width, 960)}px`)
</script>
