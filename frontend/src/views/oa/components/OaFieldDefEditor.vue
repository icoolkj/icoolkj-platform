<!-- 字段映射弹框（平台模板等场景）；内页请用 OaFieldDefEditorPanel -->
<template>
  <el-dialog
    v-model="visible"
    :title="title"
    width="1280px"
    top="4vh"
    destroy-on-close
    :close-on-click-modal="false"
    @closed="emit('closed')"
  >
    <el-tabs v-model="innerTab" class="oa-field-editor-tabs">
      <el-tab-pane :label="gp('tabFields') || '字段'" name="fields">
        <OaFieldDefEditorPanel
          ref="panelRef"
          :version-no="versionNo"
          :version-status="versionStatus"
          :fields="fields"
          :tenant-mode="tenantMode"
          :max-height="420"
        />
      </el-tab-pane>
      <el-tab-pane :label="gp('tabLayout') || '布局'" name="layout">
        <p class="oa-field-editor-tabs__hint">{{ gp('layoutHint') }}</p>
        <FormDesigner
          v-model="layoutDocument"
          host-mode="bound"
          :field-pool="liveFields"
          class="oa-field-editor-tabs__designer"
        />
      </el-tab-pane>
    </el-tabs>
    <template #footer>
      <el-button @click="visible = false">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" plain :loading="saving" @click="handleSave">
        {{ t('common.save') }}
      </el-button>
      <el-button
        v-if="showPublish"
        type="primary"
        :loading="publishing"
        @click="handleSaveAndPublish"
      >
        {{ publishLabel }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useGenPageShellI18n } from '@/composables/gen/useGenPageShellI18n'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import FormDesigner from '@/components/form-designer/FormDesigner.vue'
import { prepareBoundLayoutSave } from '@/components/form-designer/boundHost'
import { composeBoundDocument, persistLayout } from '@/components/form-designer/oaBridge'
import type { FormDocument } from '@/types/form-designer/schema'
import type { OaFieldDefVO } from '@/types/oa/oaPlatformTemplate'
import OaFieldDefEditorPanel from '@/views/oa/components/OaFieldDefEditorPanel.vue'
const { gp } = useGenPageShellI18n('oaPlatformTemplate')

const props = withDefaults(
  defineProps<{
    modelValue: boolean
    title?: string
    versionId?: string
    versionNo?: number
    versionStatus?: string
    fields?: OaFieldDefVO[]
    layoutJson?: string | null
    tenantMode?: boolean
    showPublish?: boolean
    saving?: boolean
    publishing?: boolean
  }>(),
  {
    title: '',
    showPublish: false,
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  save: [payload: { fields: OaFieldDefVO[]; layoutJson: string }]
  'save-and-publish': [payload: { fields: OaFieldDefVO[]; layoutJson: string }]
  closed: []
}>()

const { t } = useI18n()
const panelRef = ref<InstanceType<typeof OaFieldDefEditorPanel>>()
const innerTab = ref('fields')
const layoutDocument = ref<FormDocument>(
  composeBoundDocument({ fields: props.fields ?? [], layoutJson: props.layoutJson }),
)
const publishLabel = computed(() => gp('saveAndPublishAction') || '保存并发布')

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const liveFields = computed(() => panelRef.value?.getFields() ?? props.fields ?? [])

watch(
  () => [props.modelValue, props.fields, props.layoutJson] as const,
  ([open]) => {
    if (!open) return
    innerTab.value = 'fields'
    layoutDocument.value = composeBoundDocument({
      fields: props.fields ?? [],
      layoutJson: props.layoutJson,
    })
  },
)

watch(innerTab, (tab) => {
  if (tab === 'layout') {
    const fields = panelRef.value?.getFields() ?? props.fields ?? []
    layoutDocument.value = composeBoundDocument({
      fields,
      layoutJson: persistLayout(layoutDocument.value),
    })
  }
})

defineExpose({
  getFields: () => panelRef.value?.getFields() ?? [],
  getLayoutJson: () => {
    const fields = panelRef.value?.getFields() ?? []
    const prepared = prepareBoundLayoutSave({ fields, document: layoutDocument.value })
    return prepared.ok ? prepared.layoutJson : persistLayout(layoutDocument.value)
  },
})

function buildPayload(): { fields: OaFieldDefVO[]; layoutJson: string } | null {
  const fields = panelRef.value?.getFields() ?? []
  const prepared = prepareBoundLayoutSave({ fields, document: layoutDocument.value })
  if (!prepared.ok) {
    ElMessage.error(gp('layoutValidateFail') || prepared.reason)
    return null
  }
  layoutDocument.value = prepared.document
  return {
    fields,
    layoutJson: prepared.layoutJson,
  }
}

function handleSave() {
  const payload = buildPayload()
  if (!payload) return
  emit('save', payload)
}

function handleSaveAndPublish() {
  const payload = buildPayload()
  if (!payload) return
  emit('save-and-publish', payload)
}
</script>

<style scoped>
.oa-field-editor-tabs__hint {
  margin: 0 0 10px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
.oa-field-editor-tabs__designer {
  height: min(68vh, 640px);
  min-height: 420px;
}
</style>
