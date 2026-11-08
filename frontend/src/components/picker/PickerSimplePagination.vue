<template>
  <div v-if="total > 0" class="picker-simple-pagination">
    <el-button
      size="small"
      :disabled="page <= 1"
      class="picker-simple-pagination__btn"
      @click="goPrev"
    >
      ‹
    </el-button>
    <span class="picker-simple-pagination__info">{{ page }} / {{ pageCount }}</span>
    <el-button
      size="small"
      :disabled="page >= pageCount"
      class="picker-simple-pagination__btn"
      @click="goNext"
    >
      ›
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { PICKER_SELECTED_PAGE_SIZE } from './constants'

const props = withDefaults(
  defineProps<{
    total: number
    page: number
    pageSize?: number
  }>(),
  {
    pageSize: PICKER_SELECTED_PAGE_SIZE,
  },
)

const emit = defineEmits<{
  'update:page': [number]
}>()

const pageCount = computed(() => Math.max(1, Math.ceil(props.total / props.pageSize)))

function goPrev() {
  if (props.page > 1) emit('update:page', props.page - 1)
}

function goNext() {
  if (props.page < pageCount.value) emit('update:page', props.page + 1)
}
</script>

<style scoped lang="scss">
.picker-simple-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
}

.picker-simple-pagination__info {
  min-width: 48px;
  font-size: 12px;
  color: var(--el-text-color-regular);
  text-align: center;
  user-select: none;
}

.picker-simple-pagination__btn {
  min-width: 28px;
  padding: 0 6px;
  font-size: 16px;
  line-height: 1;
}
</style>
