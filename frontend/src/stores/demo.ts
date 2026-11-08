import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

export const useDemoStore = defineStore('demo', () => {
  const demoMode = ref(false)

  const isDemo = computed(() => demoMode.value)

  function setDemoMode(value: boolean) {
    demoMode.value = value
  }

  return { demoMode, isDemo, setDemoMode }
})
