<template>
  <div class="picker-search-bar">
    <el-input
      :model-value="modelValue"
      clearable
      :placeholder="placeholder"
      @update:model-value="emit('update:modelValue', $event)"
      @keyup.enter="emit('search')"
    />
    <el-button type="primary" @click="emit('search')">{{ searchText }}</el-button>
    <el-button @click="emit('reset')">{{ resetText }}</el-button>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  modelValue: string
  placeholder?: string
  searchText?: string
  resetText?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [string]
  search: []
  reset: []
}>()
</script>

<style scoped lang="scss">
@use './picker.scss';
</style>
