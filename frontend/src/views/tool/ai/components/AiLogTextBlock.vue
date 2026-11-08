<!-- @deprecated import from @/domains/tool/ai/components/AiLogTextBlock -->
<template>
  <AiLogTextBlock v-bind="$attrs" />
</template>

<script setup lang="ts">
import AiLogTextBlock from '@/domains/tool/ai/components/AiLogTextBlock.vue'

defineOptions({ inheritAttrs: false })
</script>
