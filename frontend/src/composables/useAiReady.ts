import { computed, onMounted, ref } from 'vue'
import { getAiRuntimeStatus, type AiRuntimeStatusVO } from '@/api/tool/ai/catalog'

export function useAiReady(capability = 'chat') {
  const loading = ref(false)
  const status = ref<AiRuntimeStatusVO | null>(null)

  const enabled = computed(() => !!status.value?.enabled)
  const apiKeyConfigured = computed(() => !!status.value?.apiKeyConfigured)
  const aiReady = computed(() => enabled.value && apiKeyConfigured.value)

  async function load() {
    loading.value = true
    try {
      const res = await getAiRuntimeStatus(capability)
      status.value = res.data.data ?? null
    } catch {
      status.value = null
    } finally {
      loading.value = false
    }
  }

  onMounted(load)

  return {
    loading,
    status,
    enabled,
    apiKeyConfigured,
    aiReady,
    load,
  }
}
