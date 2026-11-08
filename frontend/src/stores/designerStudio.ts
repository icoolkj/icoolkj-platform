import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

/**
 * Designer Studio Mode: hide BasicLayout chrome so Form Designer can fill the viewport.
 * Cleared automatically when leaving the designer host page.
 */
export const useDesignerStudioStore = defineStore('designerStudio', () => {
  const active = ref(false)

  const isStudio = computed(() => active.value)

  function enter() {
    active.value = true
  }

  function exit() {
    active.value = false
  }

  function toggle() {
    active.value = !active.value
  }

  return { active, isStudio, enter, exit, toggle }
})
