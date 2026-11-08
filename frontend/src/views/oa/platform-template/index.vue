<!-- OA 平台审批模板 — 配置字段 / 发布 主路径 -->
<template>
  <GenRuntimePage
    ref="pageRef"
    page-key="oaPlatformTemplate"
    custom-form
    :view-override="viewOverride"
    :action-handlers="actionHandlers"
    @open-form="handleOpenForm"
  />

  <OaPlatformTemplateFormDialog
    v-model="formVisible"
    :template-id="editingId"
    :gp="gp"
    :gf="gf"
    :gv="gv"
    @success="pageRef?.loadPage()"
  />

  <OaFieldDefEditor
    v-model="fieldVisible"
    :title="gp('configureAction')"
    :version-id="activeVersionId"
    :version-no="activeVersionNo"
    :version-status="activeVersionStatus"
    :fields="activeFields"
    :layout-json="activeLayoutJson"
    :show-publish="true"
    :saving="saving"
    :publishing="publishing"
    @save="saveFields"
    @save-and-publish="saveAndPublish"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { GenRuntimePage } from '@/components/gen'
import type { GenRowActionHandler } from '@/components/gen/registry/extension-blocks/registry'
import OaFieldDefEditor from '@/views/oa/components/OaFieldDefEditor.vue'
import OaPlatformTemplateFormDialog from '@/views/oa/platform-template/OaPlatformTemplateFormDialog.vue'
import { useGenPageShellI18n } from '@/composables/gen'
import type { GenViewSchema } from '@/types/tool/genView'
import {
  createOaPlatformDraftVersion,
  getOaPlatformTemplate,
  getOaPlatformTemplateVersion,
  publishOaPlatformVersion,
  saveOaPlatformFieldDefs,
} from '@/api/oa/oaPlatformTemplate'
import type { OaFieldDefVO } from '@/types/oa/oaPlatformTemplate'

const { gp, gf, gv } = useGenPageShellI18n('oaPlatformTemplate')
const { t } = useI18n()

function viewOverride(view: GenViewSchema): GenViewSchema {
  return {
    ...view,
    table: {
      ...view.table,
      actions: { edit: false, delete: false, width: 220 },
    },
  }
}

const pageRef = ref<InstanceType<typeof GenRuntimePage>>()
const formVisible = ref(false)
const editingId = ref<string>()
const fieldVisible = ref(false)
const saving = ref(false)
const publishing = ref(false)
const activeVersionId = ref<string>()
const activeVersionNo = ref<number>()
const activeVersionStatus = ref<string>()
const activeFields = ref<OaFieldDefVO[]>([])
const activeLayoutJson = ref<string | null>(null)

function handleOpenForm(row?: Record<string, unknown>) {
  editingId.value = row?.id ? String(row.id) : undefined
  formVisible.value = true
}

async function ensureDraft(templateId: string) {
  let detail = (await getOaPlatformTemplate(templateId)).data.data
  if (!detail.draftVersion) {
    await createOaPlatformDraftVersion({ templateId })
    ElMessage.success(gp('draftCreated'))
    detail = (await getOaPlatformTemplate(templateId)).data.data
  }
  const draft = detail.draftVersion
  if (!draft) {
    ElMessage.warning(gp('noDraft'))
    return null
  }
  return draft
}

async function openConfigure(row: Record<string, unknown>) {
  if (!row.id) return
  const draft = await ensureDraft(String(row.id))
  if (!draft) return
  const ver = await getOaPlatformTemplateVersion(draft.id)
  activeVersionId.value = draft.id
  activeVersionNo.value = draft.versionNo
  activeVersionStatus.value = draft.versionStatus
  activeFields.value = ver.data.data.fields ?? []
  activeLayoutJson.value = ver.data.data.layoutJson ?? null
  fieldVisible.value = true
  pageRef.value?.loadPage()
}

async function saveFields(payload: { fields: OaFieldDefVO[]; layoutJson: string }) {
  if (!activeVersionId.value) return
  saving.value = true
  try {
    await saveOaPlatformFieldDefs({
      versionId: activeVersionId.value,
      fields: payload.fields,
      layoutJson: payload.layoutJson,
    })
    ElMessage.success(t('common.success'))
    fieldVisible.value = false
    pageRef.value?.loadPage()
  } finally {
    saving.value = false
  }
}

async function saveAndPublish(payload: { fields: OaFieldDefVO[]; layoutJson: string }) {
  if (!activeVersionId.value) return
  await ElMessageBox.confirm(gp('publishConfirm'), t('common.tip'), { type: 'warning' })
  publishing.value = true
  try {
    await saveOaPlatformFieldDefs({
      versionId: activeVersionId.value,
      fields: payload.fields,
      layoutJson: payload.layoutJson,
    })
    await publishOaPlatformVersion({ versionId: activeVersionId.value })
    ElMessage.success(gp('published'))
    fieldVisible.value = false
    pageRef.value?.loadPage()
  } finally {
    publishing.value = false
  }
}

async function publishDraft(row: Record<string, unknown>) {
  if (!row.id) return
  const d = await getOaPlatformTemplate(String(row.id))
  const draft = d.data.data.draftVersion
  if (!draft) {
    ElMessage.warning(gp('noDraft'))
    return
  }
  await ElMessageBox.confirm(gp('publishConfirm'), t('common.tip'), { type: 'warning' })
  await publishOaPlatformVersion({ versionId: draft.id })
  ElMessage.success(gp('published'))
  pageRef.value?.loadPage()
}

const actionHandlers: Record<string, GenRowActionHandler> = {
  'oaPlatformTemplate.configure': async ({ row }) => openConfigure(row),
  'oaPlatformTemplate.publish': async ({ row }) => publishDraft(row),
}
</script>

