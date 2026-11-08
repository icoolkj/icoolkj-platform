import type { App, Directive } from 'vue'
import { useUserStore } from '@/stores/user'
import { hasPermission } from '@/utils/permission'

const hasPermi: Directive = {
  mounted(el, binding) {
    const { value } = binding
    if (!value) return
    const store = useUserStore()
    if (!hasPermission(store.permissions, value)) {
      el.parentNode?.removeChild(el)
    }
  },
}

export function setupDirectives(app: App) {
  app.directive('hasPermi', hasPermi)
}
