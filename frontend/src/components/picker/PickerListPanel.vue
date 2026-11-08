<template>
  <section class="picker-panel" :class="{ 'picker-panel--grow': grow, 'picker-panel--tree': tree }">
    <div v-if="$slots.header || title" class="picker-panel__header">
      <slot name="header">
        <span class="picker-panel__title">{{ title }}</span>
      </slot>
    </div>
    <div class="picker-panel__body" :class="{ 'picker-panel__body--flush': flush }">
      <slot />
    </div>
    <div v-if="$slots.footer" class="picker-panel__footer">
      <slot name="footer" />
    </div>
  </section>
</template>

<script setup lang="ts">
defineProps<{
  title?: string
  grow?: boolean
  tree?: boolean
  flush?: boolean
}>()
</script>

<style scoped lang="scss">
@use './picker.scss';

.picker-panel__title {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
