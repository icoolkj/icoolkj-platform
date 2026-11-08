<template>
  <GenFieldAttachmentWidget
    :ctx="ctx"
    mode="single"
    variant="image"
    @update:model-value="emit('update:modelValue', $event)"
  />
</template>

<script setup lang="ts">
import GenFieldAttachmentWidget from './GenFieldAttachmentWidget.vue'
import type { GenFieldWidgetContext, GenFieldWidgetEmits } from '@/components/gen/registry/field-widgets/types'

defineProps<{ ctx: GenFieldWidgetContext }>()
const emit = defineEmits<GenFieldWidgetEmits>()
</script>
