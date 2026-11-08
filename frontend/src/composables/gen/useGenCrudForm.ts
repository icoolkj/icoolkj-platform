import { computed, ref, type ComputedRef } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { GenCrudApi } from '@/types/gen/crudApi'
import type { GenViewFormField, GenViewSchema } from '@/types/tool/genView'
import { provideRichEditorRegistry } from '@/composables/useRichEditorRegistry'
import type GenEntityForm from '@/components/gen/fields/GenEntityForm.vue'

export type UseGenCrudFormOptions = {
  api: GenCrudApi
  view: ComputedRef<GenViewSchema>
  gp: (key: string, params?: Record<string, string | number>) => string
  customForm: ComputedRef<boolean>
  loadPage: () => Promise<void>
  onOpenForm?: (row?: Record<string, unknown>) => void
}

export function useGenCrudForm(options: UseGenCrudFormOptions) {
  const { t } = useI18n()
  const richEditorRegistry = provideRichEditorRegistry()

  const formVisible = ref(false)
  const submitting = ref(false)
  const form = ref<Record<string, unknown>>({})
  const entityFormRef = ref<InstanceType<typeof GenEntityForm>>()

  const formConfig = computed(() => options.view.value.form ?? {})
  const isPageFormMode = computed(() => formConfig.value.mode === 'page')
  const formTitle = computed(() => (form.value.id ? options.gp('editTitle') : options.gp('addTitle')))

  const visibleFormFields = computed(() => {
    const fields = options.view.value.form?.fields ?? []
    const modeFields = form.value.id
      ? fields.filter((f) => f.edit !== false && !f.insertOnly)
      : fields.filter((f) => f.insert !== false)
    return modeFields.filter((f) => {
      if (!f.showWhen) return true
      const current = form.value[f.showWhen.field]
      return f.showWhen.values.includes(String(current ?? ''))
    })
  })

  function resetForm() {
    form.value = {}
    entityFormRef.value?.resetFields()
  }

  function applyFormDefaults() {
    const createDefaults = options.view.value.form?.createDefaults ?? {}
    Object.assign(form.value, createDefaults)
    for (const field of options.view.value.form?.fields ?? []) {
      if (form.value[field.field] === undefined && field.defaultValue !== undefined) {
        form.value[field.field] = field.defaultValue
      }
    }
  }

  function closeForm() {
    formVisible.value = false
  }

  function openForm(row?: Record<string, unknown>) {
    if (options.customForm.value) {
      options.onOpenForm?.(row)
      return
    }
    resetForm()
    if (row?.id) {
      if (!options.api.get) return
      options.api.get(String(row.id)).then((raw) => {
        const res = raw as { data: { data: Record<string, unknown> } }
        form.value = { ...res.data.data }
        formVisible.value = true
      })
      return
    }
    applyFormDefaults()
    formVisible.value = true
  }

  async function submitForm() {
    if (!entityFormRef.value) return
    richEditorRegistry.flushAll()
    const valid = await entityFormRef.value.validate()
    if (!valid) return
    submitting.value = true
    try {
      if (form.value.id) {
        if (!options.api.update) return
        await options.api.update(form.value)
      } else {
        if (!options.api.create) return
        await options.api.create(form.value)
      }
      ElMessage.success(t('common.success'))
      closeForm()
      await options.loadPage()
    } finally {
      submitting.value = false
    }
  }

  async function handleDelete(row: Record<string, unknown>) {
    if (!row.id) return
    if (!options.api.delete) return
    await ElMessageBox.confirm(options.gp('deleteConfirm', { count: 1 }), t('common.tip'))
    await options.api.delete(String(row.id))
    ElMessage.success(t('common.success'))
    await options.loadPage()
  }

  function isFieldDisabled(field: GenViewFormField) {
    return Boolean(field.disabledOnEdit && form.value.id)
  }

  return {
    formVisible,
    submitting,
    form,
    entityFormRef,
    formConfig,
    isPageFormMode,
    formTitle,
    visibleFormFields,
    openForm,
    closeForm,
    submitForm,
    handleDelete,
    isFieldDisabled,
  }
}

export type GenCrudFormReturn = ReturnType<typeof useGenCrudForm>
