<script setup lang="ts">
import { computed } from 'vue'
import type { DictOption } from '@/types/system/dict'

const props = defineProps<{
  options: DictOption[]
  value?: string | number | null
}>()

const matched = computed(() =>
  props.options.find((opt) => String(opt.value) === String(props.value ?? '')),
)
</script>

<template>
  <el-tag v-if="matched" :type="(matched.tagType as any) || 'info'" size="small">
    {{ matched.label }}
  </el-tag>
  <span v-else-if="value !== undefined && value !== null && value !== ''">{{ value }}</span>
  <span v-else>—</span>
</template>
