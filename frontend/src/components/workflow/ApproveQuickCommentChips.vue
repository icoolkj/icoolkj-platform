<template>
  <div v-if="visibleItems.length" class="approve-quick-comments">
    <div v-if="title" class="approve-quick-comments__title">{{ title }}</div>
    <div class="approve-quick-comments__chips">
      <button
        v-for="item in visibleItems"
        :key="item.id"
        type="button"
        class="approve-quick-comments__chip"
        :class="{ 'is-reject': tone === 'reject' }"
        @click="emit('select', item.text)"
      >
        {{ item.text }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    title?: string
    items: { id: string; text: string }[]
    tone?: 'pass' | 'reject' | 'neutral'
  }>(),
  { tone: 'neutral' },
)

const emit = defineEmits<{ select: [text: string] }>()

const visibleItems = computed(() => props.items.filter((i) => !!i.text))
</script>

<style scoped>
.approve-quick-comments {
  margin-bottom: 8px;
}

.approve-quick-comments__title {
  margin-bottom: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.approve-quick-comments__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.approve-quick-comments__chip {
  max-width: 100%;
  padding: 4px 10px;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  background: var(--el-fill-color-blank);
  color: var(--el-color-primary);
  font-size: 12px;
  line-height: 1.4;
  cursor: pointer;
  text-align: left;
  transition:
    border-color 0.15s ease,
    background 0.15s ease;
}

.approve-quick-comments__chip:hover {
  border-color: var(--el-color-primary-light-5);
  background: var(--el-color-primary-light-9);
}

.approve-quick-comments__chip.is-reject {
  color: var(--el-color-danger);
}

.approve-quick-comments__chip.is-reject:hover {
  border-color: var(--el-color-danger-light-5);
  background: var(--el-color-danger-light-9);
}
</style>
