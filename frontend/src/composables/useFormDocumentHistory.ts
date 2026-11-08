import { computed, ref } from 'vue'
import { cloneFormDocument } from '@/components/form-designer/schemaUtils'
import type { FormDocument } from '@/types/form-designer/schema'

const DEFAULT_MAX = 50

/**
 * Undo/redo stack for FormDocument structural edits.
 * Call `push(current)` before applying a mutation; `undo`/`redo` return the document to restore.
 */
export function useFormDocumentHistory(max = DEFAULT_MAX) {
  const past = ref<FormDocument[]>([])
  const future = ref<FormDocument[]>([])

  const canUndo = computed(() => past.value.length > 0)
  const canRedo = computed(() => future.value.length > 0)

  function push(doc: FormDocument) {
    past.value.push(cloneFormDocument(doc))
    if (past.value.length > max) {
      past.value.shift()
    }
    future.value = []
  }

  function undo(current: FormDocument): FormDocument | null {
    if (!past.value.length) {
      return null
    }
    const prev = past.value.pop()!
    future.value.push(cloneFormDocument(current))
    return prev
  }

  function redo(current: FormDocument): FormDocument | null {
    if (!future.value.length) {
      return null
    }
    const next = future.value.pop()!
    past.value.push(cloneFormDocument(current))
    return next
  }

  function reset() {
    past.value = []
    future.value = []
  }

  return { canUndo, canRedo, push, undo, redo, reset }
}
