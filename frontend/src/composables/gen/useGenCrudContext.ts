import { inject } from 'vue'
import { GEN_CRUD_CTX, type GenCrudContext } from './genCrudContext'

export function useGenCrudContext(): GenCrudContext {
  const ctx = inject(GEN_CRUD_CTX)
  if (!ctx) {
    throw new Error('[GenCrud] useGenCrudContext must be used inside a Gen pattern (e.g. GenPatternStandardCrud)')
  }
  return ctx
}
