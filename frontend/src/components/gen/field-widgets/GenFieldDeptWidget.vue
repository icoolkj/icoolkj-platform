<template>
  <GenFieldOrgRefWidget :ctx="ctx" mode="dept" @update:model-value="emit('update:modelValue', $event)" />
</template>

<script setup lang="ts">
import GenFieldOrgRefWidget from './GenFieldOrgRefWidget.vue'
import type { GenFieldWidgetContext, GenFieldWidgetEmits } from '@/components/gen/registry/field-widgets/types'

defineProps<{ ctx: GenFieldWidgetContext }>()
const emit = defineEmits<GenFieldWidgetEmits>()
</script>
