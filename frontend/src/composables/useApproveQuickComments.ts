import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { getResolvedConfig } from '@/api/system/config'
import {
  DEFAULT_QUICK_COMMENTS,
  WORKFLOW_APPROVE_QUICK_COMMENTS_KEY,
  parseQuickCommentsConfig,
  resolveQuickCommentLabel,
  type QuickCommentItem,
  type QuickCommentsConfig,
} from '@/constants/workflowQuickComments'

export function useApproveQuickComments() {
  const { locale } = useI18n()
  const config = ref<QuickCommentsConfig>({ ...DEFAULT_QUICK_COMMENTS })
  const loaded = ref(false)

  async function load() {
    try {
      const res = await getResolvedConfig(WORKFLOW_APPROVE_QUICK_COMMENTS_KEY)
      const parsed = parseQuickCommentsConfig(res.data.data?.configValue)
      if (parsed) config.value = parsed
    } catch {
      config.value = { ...DEFAULT_QUICK_COMMENTS }
    } finally {
      loaded.value = true
    }
  }

  function labelOf(item: QuickCommentItem) {
    return resolveQuickCommentLabel(item, locale.value)
  }

  const passItems = computed(() =>
    config.value.pass.map((item) => ({ id: item.id, text: labelOf(item) })).filter((x) => !!x.text),
  )
  const rejectItems = computed(() =>
    config.value.reject
      .map((item) => ({ id: item.id, text: labelOf(item) }))
      .filter((x) => !!x.text),
  )

  onMounted(load)

  return { passItems, rejectItems, loaded, reload: load }
}
