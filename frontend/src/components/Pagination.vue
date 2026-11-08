<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  total: number
  page: number
  limit: number
}>()

const emit = defineEmits<{
  'update:page': [value: number]
  'update:limit': [value: number]
  pagination: []
}>()

const currentPage = computed({
  get: () => props.page,
  set: (val) => emit('update:page', val),
})

const pageSize = computed({
  get: () => props.limit,
  set: (val) => emit('update:limit', val),
})

function handleSizeChange() {
  emit('update:page', 1)
  emit('pagination')
}

function handleCurrentChange() {
  emit('pagination')
}
</script>

<template>
  <div class="pagination-wrap">
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      background
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<style scoped>
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
