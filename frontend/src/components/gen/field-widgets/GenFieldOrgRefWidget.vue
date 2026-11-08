<template>
  <OrgRefField
    :mode="mode"
    :model-value="ctx.modelValue"
    :placeholder="ctx.placeholder"
    :disabled="ctx.disabled"
    @update:model-value="emit('update:modelValue', $event)"
  />
</template>

<script setup lang="ts">
import OrgRefField from '@/components/form-designer/runtime/OrgRefField.vue'
import type { GenFieldWidgetContext, GenFieldWidgetEmits } from '@/components/gen/registry/field-widgets/types'

defineProps<{
  ctx: GenFieldWidgetContext
  mode: 'user' | 'dept'
}>()

const emit = defineEmits<GenFieldWidgetEmits>()
</script>
