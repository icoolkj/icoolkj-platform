<!--
  P04 形态积木 — 主子双列表。
  框架映射：layout master-detail → 本组件。
-->
<template>
  <div
    v-loading="!genReady || !viewReady || !dictReady || masterLoading || detailLoading"
    class="page-container"
  >
    <PageHeader :title="gp('title')" :description="gp('description')" />

    <el-row :gutter="16">
      <el-col :span="masterSpan">
        <el-card shadow="never" class="table-card">
          <template #header>
            <div class="panel-header">
              <span>{{ gp('masterPanel') }}</span>
              <div class="panel-header__actions">
                <el-button
                  v-if="!readOnly"
                  type="primary"
                  :icon="Plus"
                  v-hasPermi="[perm.add]"
                  @click="openMasterForm()"
                >
                  {{ t('common.add') }}
                </el-button>
                <GenExcelToolbarActions
                  :actions="masterExcelToolbarActions"
                  :read-only="readOnly"
                  :perm="masterExcelPerm"
                  :api="api.master"
                  :api-module="apiModule"
                  :business-name="masterExcelBusinessName"
                  :export-query="masterQuery"
                  :label-resolver="gp"
                  :resolve-field-validation="resolveFieldValidation"
                  @imported="loadMasterPage"
                />
              </div>
            </div>
          </template>

          <el-form
            v-if="masterQueryFields.length"
            :model="masterQuery"
            inline
            @submit.prevent
            class="panel-search"
          >
            <el-form-item
              v-for="field in masterQueryFields"
              :key="field.field"
              :label="gf(field.field, 'label')"
            >
              <GenFormField
                v-model="masterQuery[field.field]"
                :config="field"
                mode="query"
                :placeholder="gf(field.field, 'placeholder')"
                :get-dict="getDict"
                @enter="loadMasterPage"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="Search" @click="loadMasterPage">
                {{ t('common.search') }}
              </el-button>
            </el-form-item>
          </el-form>

          <el-table
            :data="masterList"
            stripe
            border
            highlight-current-row
            @row-click="handleMasterSelect"
          >
            <el-table-column
              v-for="col in masterTableFields"
              :key="col.field"
              :label="gf(col.field, 'label')"
              :prop="col.component === 'text' ? col.field : undefined"
              :min-width="col.minWidth"
              :width="col.width"
              :align="col.align"
              :show-overflow-tooltip="col.showOverflowTooltip"
            >
              <template v-if="col.component === 'dict-tag'" #default="{ row }">
                <DictTag :options="getDict(col.dictType!)" :value="row[col.field]" />
              </template>
            </el-table-column>
            <el-table-column
              v-if="masterTableActions.edit || masterTableActions.delete"
              :label="t('common.actions')"
              align="center"
              :width="masterTableActions.width ?? 120"
              fixed="right"
            >
              <template #default="{ row }">
                <el-button
                  link
                  type="primary"
                  v-hasPermi="[perm.update]"
                  @click.stop="openMasterForm(row)"
                >
                  {{ t('common.edit') }}
                </el-button>
                <el-button
                  link
                  type="danger"
                  v-hasPermi="[perm.delete]"
                  @click.stop="handleMasterDelete(row)"
                >
                  {{ t('common.delete') }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="table-footer">
            <Pagination
              v-show="masterTotal > 0"
              v-model:page="masterQuery.pageNo"
              v-model:limit="masterQuery.pageSize"
              :total="masterTotal"
              @pagination="loadMasterPage"
            />
          </div>
        </el-card>
      </el-col>

      <el-col :span="detailSpan">
        <el-card shadow="never" class="table-card">
          <template #header>
            <div class="panel-header">
              <span>{{ gp('detailPanel') }}</span>
              <div class="panel-header__actions">
                <el-button
                  v-if="!readOnly"
                  type="primary"
                  :icon="Plus"
                  :disabled="!selectedMaster"
                  v-hasPermi="[perm.add]"
                  @click="openDetailForm()"
                >
                  {{ t('common.add') }}
                </el-button>
                <GenExcelToolbarActions
                  v-if="detailExcelReady && selectedMaster"
                  :actions="detailExcelToolbarActions"
                  :read-only="readOnly"
                  :perm="detailExcelPerm"
                  :api="api.detail"
                  :api-module="apiModule"
                  :business-name="detailExcelBusinessName!"
                  :export-query="detailExportQuery"
                  :label-resolver="detailExcelLabelResolver"
                  :resolve-field-validation="resolveFieldValidation"
                />
              </div>
            </div>
          </template>

          <el-empty v-if="!selectedMaster" :description="gp('selectMasterFirst')" />

          <template v-else>
            <el-table :data="detailList" stripe border highlight-current-row>
              <el-table-column
                v-for="col in detailTableFields"
                :key="col.field"
                :label="gf(col.field, 'label')"
                :prop="col.component === 'text' ? col.field : undefined"
                :min-width="col.minWidth"
                :width="col.width"
                :align="col.align"
                :show-overflow-tooltip="col.showOverflowTooltip"
              >
                <template v-if="col.component === 'dict-tag'" #default="{ row }">
                  <DictTag :options="getDict(col.dictType!)" :value="row[col.field]" />
                </template>
              </el-table-column>
              <el-table-column
                v-if="detailTableActions.edit || detailTableActions.delete"
                :label="t('common.actions')"
                align="center"
                :width="detailTableActions.width ?? 120"
                fixed="right"
              >
                <template #default="{ row }">
                  <el-button
                    link
                    type="primary"
                    v-hasPermi="[perm.update]"
                    @click="openDetailForm(row)"
                  >
                    {{ t('common.edit') }}
                  </el-button>
                  <el-button
                    link
                    type="danger"
                    v-hasPermi="[perm.delete]"
                    @click="handleDetailDelete(row)"
                  >
                    {{ t('common.delete') }}
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <div class="table-footer">
              <Pagination
                v-show="detailTotal > 0"
                v-model:page="detailQuery.pageNo"
                v-model:limit="detailQuery.pageSize"
                :total="detailTotal"
                @pagination="loadDetailPage"
              />
            </div>
          </template>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog
      v-model="masterFormVisible"
      :title="masterFormTitle"
      :width="`${masterFormConfig.dialogWidth ?? 520}px`"
      destroy-on-close
    >
      <el-form
        ref="masterFormRef"
        :model="masterForm"
        :rules="masterFormRules"
        :label-width="`${masterFormConfig.labelWidth ?? 100}px`"
      >
        <el-form-item
          v-for="field in visibleMasterFormFields"
          :key="field.field"
          :label="gf(field.field, 'label')"
          :prop="field.field"
        >
          <GenFormField
            v-model="masterForm[field.field]"
            :config="field"
            mode="form"
            :placeholder="gf(field.field, 'placeholder')"
            :disabled="isMasterFieldDisabled(field)"
            :get-dict="getDict"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="masterFormVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="masterSubmitting" @click="submitMasterForm">
          {{ t('common.confirm') }}
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="detailFormVisible"
      :title="detailFormTitle"
      :width="`${detailFormConfig.dialogWidth ?? 520}px`"
      destroy-on-close
    >
      <el-form
        ref="detailFormRef"
        :model="detailForm"
        :rules="detailFormRules"
        :label-width="`${detailFormConfig.labelWidth ?? 100}px`"
      >
        <el-form-item
          v-for="field in visibleDetailFormFields"
          :key="field.field"
          :label="gf(field.field, 'label')"
          :prop="field.field"
        >
          <GenFormField
            v-model="detailForm[field.field]"
            :config="field"
            mode="form"
            :placeholder="gf(field.field, 'placeholder')"
            :disabled="isDetailFieldDisabled(field)"
            :get-dict="getDict"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="detailFormVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="detailSubmitting" @click="submitDetailForm">
          {{ t('common.confirm') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import Pagination from '@/components/Pagination.vue'
import DictTag from '@/components/DictTag.vue'
import GenFormField from '@/components/gen/fields/GenFormField.vue'
import GenExcelToolbarActions from '@/components/gen/excel/GenExcelToolbarActions.vue'
import { useDict } from '@/composables/useDict'
import { useExcelImportExport } from '@/composables/useExcelImportExport'
import { useGenI18n } from '@/composables/useGenI18n'
import { collectDictTypes, useGenView } from '@/composables/useGenView'
import { useDemoStore } from '@/stores/demo'
import type { GenMasterDetailApi } from '@/types/gen/crudApi'
import type { GenFieldValidation } from '@/types/tool/genI18n'
import type { GenViewFormField } from '@/types/tool/genView'
import { createGenPageExpose } from '@/components/gen/patterns/expose'
import type { GenPatternMasterDetailProps } from '@/components/gen/patterns/types'

export type { GenMasterDetailApi }

const props = defineProps<GenPatternMasterDetailProps>()

const { t } = useI18n()
const demoStore = useDemoStore()
const {
  ready: genReady,
  gp,
  gf,
  gv,
  formRules,
} = useGenI18n(props.businessName, props.apiModule, props.i18nFiles)
const { ready: viewReady, view } = useGenView(props.businessName, props.apiModule, props.fileView)
const readOnly = computed(
  () => view.value.readOnly === true || props.readOnly === true || demoStore.isDemo,
)
function resolveFieldValidation(field: string, rule: string) {
  return gv(field, rule as keyof GenFieldValidation)
}
const dictTypes = computed(() => collectDictTypes(view.value))
const { getDict, ready: dictReady } = useDict(dictTypes)

const md = computed(() => view.value.masterDetail!)
const detailForeignKey = computed(() => md.value.detailForeignKey)
const masterSpan = computed(() => md.value.masterSpan ?? 10)
const detailSpan = computed(() => md.value.detailSpan ?? 14)

const masterLoading = ref(false)
const detailLoading = ref(false)
const masterSubmitting = ref(false)
const detailSubmitting = ref(false)
const masterList = ref<Record<string, unknown>[]>([])
const detailList = ref<Record<string, unknown>[]>([])
const masterTotal = ref(0)
const detailTotal = ref(0)
const selectedMaster = ref<Record<string, unknown> | null>(null)

const masterFormVisible = ref(false)
const detailFormVisible = ref(false)
const masterForm = ref<Record<string, unknown>>({})
const detailForm = ref<Record<string, unknown>>({})
const masterFormRef = ref<FormInstance>()
const detailFormRef = ref<FormInstance>()

const masterQuery = reactive<{ pageNo: number; pageSize: number; [key: string]: unknown }>({
  pageNo: 1,
  pageSize: 10,
})
const detailQuery = reactive<{ pageNo: number; pageSize: number; [key: string]: unknown }>({
  pageNo: 1,
  pageSize: 10,
})

const masterQueryFields = computed(() => md.value.master?.query?.fields ?? [])
const masterTableFields = computed(() => md.value.master?.table?.fields ?? [])
const detailTableFields = computed(() =>
  (md.value.detail?.table?.fields ?? []).filter((f) => f.field !== detailForeignKey.value),
)
const masterFormConfig = computed(() => md.value.master?.form ?? {})
const detailFormConfig = computed(() => md.value.detail?.form ?? {})

const masterToolbarActions = computed(
  () => md.value.master?.toolbarActions ?? view.value.extensions?.toolbarActions ?? [],
)
const masterExcelBusinessName = computed(
  () => md.value.master?.businessName ?? props.businessName,
)
const masterExcelToolbarActions = computed(() => {
  const fromView = masterToolbarActions.value
  if (fromView.some((a) => a.builtin === 'export' || a.builtin === 'import')) {
    return fromView
  }
  return useExcelImportExport({
    businessName: masterExcelBusinessName.value,
    apiModule: props.apiModule,
    api: props.api.master,
    perm: props.perm,
  }).actions
})
const detailToolbarActions = computed(() => md.value.detail?.toolbarActions ?? [])
/** Fail closed: never fall back to master businessName for detail Excel paths. */
const detailExcelBusinessName = computed(() => md.value.detail?.businessName)
const masterExcelPerm = computed(() => props.perm)
const detailExcelPerm = computed(() => props.detailPerm ?? props.perm)
/** Detail panel: export only (import cannot inject master FK into Excel rows). */
const detailExcelToolbarActions = computed(() => {
  const fromView = detailToolbarActions.value.filter((a) => a.builtin === 'export')
  if (import.meta.env.DEV && detailToolbarActions.value.some((a) => a.builtin === 'import')) {
    console.warn(
      '[GenMasterDetail] detail builtin:import is not supported (no FK injection); ignored.',
    )
  }
  if (fromView.length) {
    return fromView
  }
  // Export-only auto-discovery when detail.toolbarActions omits export but API surface exists.
  const detailBiz = detailExcelBusinessName.value
  if (!detailBiz) {
    return []
  }
  return useExcelImportExport({
    businessName: detailBiz,
    apiModule: props.apiModule,
    api: props.api.detail,
    perm: detailExcelPerm.value,
    builtins: ['export'],
  }).actions
})
/** Avoid master page.exportFileName leaking into detail downloads. */
function detailExcelLabelResolver(key: string) {
  if (key === 'exportFileName' || key === 'exportSheetName' || key === 'exportAsyncTaskName') {
    return key
  }
  return gp(key)
}
const detailExcelReady = computed(() => {
  if (!detailExcelToolbarActions.value.length) return false
  if (!detailExcelBusinessName.value) {
    if (import.meta.env.DEV) {
      console.warn(
        '[GenMasterDetail] detail.businessName is required when detail Excel export is configured.',
      )
    }
    return false
  }
  return true
})
const detailExportQuery = computed(() => {
  const q: Record<string, unknown> = { ...detailQuery }
  if (selectedMaster.value?.id) {
    q[detailForeignKey.value] = selectedMaster.value.id
  }
  return q
})

const masterTableActions = computed(() => ({
  edit: !readOnly.value && md.value.master?.table?.actions?.edit !== false,
  delete: !readOnly.value && md.value.master?.table?.actions?.delete !== false,
  width: md.value.master?.table?.actions?.width,
}))
const detailTableActions = computed(() => ({
  edit: !readOnly.value && md.value.detail?.table?.actions?.edit !== false,
  delete: !readOnly.value && md.value.detail?.table?.actions?.delete !== false,
  width: md.value.detail?.table?.actions?.width,
}))

const masterFormTitle = computed(() =>
  masterForm.value.id ? gp('masterEditTitle') : gp('masterAddTitle'),
)
const detailFormTitle = computed(() =>
  detailForm.value.id ? gp('detailEditTitle') : gp('detailAddTitle'),
)

const masterFormRules = formRules
const detailFormRules = formRules

const visibleMasterFormFields = computed(() =>
  filterFormFields(md.value.master?.form?.fields ?? [], masterForm.value),
)
const visibleDetailFormFields = computed(() =>
  filterFormFields(md.value.detail?.form?.fields ?? [], detailForm.value).filter(
    (f) => f.field !== detailForeignKey.value,
  ),
)

function filterFormFields(fields: GenViewFormField[], formData: Record<string, unknown>) {
  if (formData.id) {
    return fields.filter((f) => f.edit !== false && !f.insertOnly)
  }
  return fields.filter((f) => f.insert !== false)
}

function applyFormDefaults(
  formData: Record<string, unknown>,
  fields: GenViewFormField[] | undefined,
) {
  for (const field of fields ?? []) {
    if (formData[field.field] === undefined && field.defaultValue !== undefined) {
      formData[field.field] = field.defaultValue
    }
  }
}

async function loadMasterPage() {
  masterLoading.value = true
  try {
    const res = (await props.api.master.page(masterQuery)) as {
      data: { data: { list: Record<string, unknown>[]; total: number } }
    }
    masterList.value = res.data.data.list
    masterTotal.value = res.data.data.total
    if (selectedMaster.value?.id) {
      const still = masterList.value.find((r) => r.id === selectedMaster.value!.id)
      selectedMaster.value = still ?? null
      if (selectedMaster.value) {
        await loadDetailPage()
      } else {
        detailList.value = []
        detailTotal.value = 0
      }
    }
  } finally {
    masterLoading.value = false
  }
}

async function loadDetailPage() {
  if (!selectedMaster.value?.id) return
  detailLoading.value = true
  try {
    detailQuery[detailForeignKey.value] = selectedMaster.value.id
    const res = (await props.api.detail.page(detailQuery)) as {
      data: { data: { list: Record<string, unknown>[]; total: number } }
    }
    detailList.value = res.data.data.list
    detailTotal.value = res.data.data.total
  } finally {
    detailLoading.value = false
  }
}

function handleMasterSelect(row: Record<string, unknown>) {
  selectedMaster.value = row
  detailQuery.pageNo = 1
  loadDetailPage()
}

function openMasterForm(row?: Record<string, unknown>) {
  masterForm.value = {}
  masterFormRef.value?.resetFields()
  if (row?.id) {
    if (!props.api.master.get) return
    props.api.master.get(String(row.id)).then((raw) => {
      const res = raw as { data: { data: Record<string, unknown> } }
      masterForm.value = { ...res.data.data }
      masterFormVisible.value = true
    })
    return
  }
  applyFormDefaults(masterForm.value, md.value.master?.form?.fields)
  masterFormVisible.value = true
}

function openDetailForm(row?: Record<string, unknown>) {
  if (!selectedMaster.value?.id) return
  detailForm.value = { [detailForeignKey.value]: selectedMaster.value.id }
  detailFormRef.value?.resetFields()
  if (row?.id) {
    if (!props.api.detail.get) return
    props.api.detail.get(String(row.id)).then((raw) => {
      const res = raw as { data: { data: Record<string, unknown> } }
      detailForm.value = { ...res.data.data }
      detailFormVisible.value = true
    })
    return
  }
  applyFormDefaults(detailForm.value, md.value.detail?.form?.fields)
  detailFormVisible.value = true
}

async function submitMasterForm() {
  if (!masterFormRef.value) return
  const valid = await masterFormRef.value.validate().catch(() => false)
  if (!valid) return
  masterSubmitting.value = true
  try {
    if (masterForm.value.id) {
      if (!props.api.master.update) return
      await props.api.master.update(masterForm.value)
    } else {
      if (!props.api.master.create) return
      await props.api.master.create(masterForm.value)
    }
    ElMessage.success(t('common.success'))
    masterFormVisible.value = false
    await loadMasterPage()
  } finally {
    masterSubmitting.value = false
  }
}

async function submitDetailForm() {
  if (!detailFormRef.value) return
  const valid = await detailFormRef.value.validate().catch(() => false)
  if (!valid) return
  detailSubmitting.value = true
  try {
    if (detailForm.value.id) {
      if (!props.api.detail.update) return
      await props.api.detail.update(detailForm.value)
    } else {
      if (!props.api.detail.create) return
      await props.api.detail.create(detailForm.value)
    }
    ElMessage.success(t('common.success'))
    detailFormVisible.value = false
    await loadDetailPage()
  } finally {
    detailSubmitting.value = false
  }
}

async function handleMasterDelete(row: Record<string, unknown>) {
  if (!row.id) return
  if (!props.api.master.delete) return
  await ElMessageBox.confirm(gp('deleteConfirm', { count: 1 }), t('common.tip'))
  await props.api.master.delete(String(row.id))
  if (selectedMaster.value?.id === row.id) {
    selectedMaster.value = null
    detailList.value = []
    detailTotal.value = 0
  }
  ElMessage.success(t('common.success'))
  await loadMasterPage()
}

async function handleDetailDelete(row: Record<string, unknown>) {
  if (!row.id) return
  if (!props.api.detail.delete) return
  await ElMessageBox.confirm(gp('deleteConfirm', { count: 1 }), t('common.tip'))
  await props.api.detail.delete(String(row.id))
  ElMessage.success(t('common.success'))
  await loadDetailPage()
}

function isMasterFieldDisabled(field: GenViewFormField) {
  return Boolean(field.disabledOnEdit && masterForm.value.id)
}

function isDetailFieldDisabled(field: GenViewFormField) {
  return Boolean(field.disabledOnEdit && detailForm.value.id)
}

defineExpose(createGenPageExpose({ loadMasterPage, loadDetailPage }))

onMounted(loadMasterPage)
</script>

<style scoped>
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.panel-header__actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}
.panel-search {
  margin-bottom: 12px;
}
</style>
