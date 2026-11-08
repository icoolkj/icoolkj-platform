import { computed, ref, watch, type Ref } from 'vue'

export function useClientPageList<T>(source: Ref<T[]>, pageSize = 10) {
  const pageNo = ref(1)

  const total = computed(() => source.value.length)
  const pageCount = computed(() => Math.max(1, Math.ceil(total.value / pageSize) || 1))

  watch(total, () => {
    if (pageNo.value > pageCount.value) pageNo.value = pageCount.value
    if (pageNo.value < 1) pageNo.value = 1
  })

  const pageRows = computed(() => {
    if (!total.value) return []
    const start = (pageNo.value - 1) * pageSize
    return source.value.slice(start, start + pageSize)
  })

  function resetPage() {
    pageNo.value = 1
  }

  return { pageNo, pageRows, total, pageSize, pageCount, resetPage }
}
