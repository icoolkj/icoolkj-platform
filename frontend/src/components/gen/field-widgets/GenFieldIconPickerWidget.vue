<template>
  <IconPicker
    :model-value="(ctx.modelValue as string) ?? ''"
    :placeholder="ctx.placeholder"
    :disabled="ctx.disabled"
    @update:model-value="emit('update:modelValue', $event)"
  />
</template>

<script setup lang="ts">
import IconPicker from '@/components/IconPicker.vue'
import type { GenFieldWidgetContext, GenFieldWidgetEmits } from '@/components/gen/registry/field-widgets/types'

defineProps<{ ctx: GenFieldWidgetContext }>()
const emit = defineEmits<GenFieldWidgetEmits>()
</script>
