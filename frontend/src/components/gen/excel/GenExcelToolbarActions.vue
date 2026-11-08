<template>
  <template v-for="action in declaredActions" :key="action.id">
    <el-button
      v-if="toolbarActionVisible(action)"
      :type="toolbarActionType(action)"
      :icon="toolbarActionIcon(action)"
      :loading="toolbarActionLoading(action)"
      v-hasPermi="toolbarActionPerm(action)"
      @click="runToolbarAction(action)"
    >
      {{ toolbarActionLabel(action) }}
    </el-button>
  </template>

  <GenExcelExportDialog
    v-if="canChooseExportMode"
    v-model="exportDialogVisible"
    :confirming="exporting"
    default-mode="async"
    @confirm="confirmExport"
  />

  <GenExcelImportDialog
    v-if="hasImportAction"
    v-model="importDialogVisible"
    :action="pendingImportAction"
    :api-module="apiModule"
    :business-name="businessName"
    :api="api"
    :can-choose-exec-mode="canChooseImportMode"
    :label-resolver="labelResolver"
    :resolve-page-text="labelResolver"
    :resolve-field-validation="resolveFieldValidation"
    @success="emit('imported')"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import GenExcelExportDialog from '@/components/gen/excel/GenExcelExportDialog.vue'
import GenExcelImportDialog from '@/components/gen/excel/GenExcelImportDialog.vue'
import { useGenExcelToolbar } from '@/composables/useGenExcelToolbar'
import type { GenCrudApi, GenCrudPerm } from '@/types/gen/crudApi'
import type { GenViewToolbarAction } from '@/types/tool/genView'

const props = defineProps<{
  actions?: GenViewToolbarAction[]
  readOnly: boolean
  perm: GenCrudPerm
  api: GenCrudApi
  apiModule: string
  businessName: string
  exportQuery: Record<string, unknown>
  labelResolver?: (key: string) => string
  resolveFieldValidation?: (field: string, rule: string) => string
}>()

const emit = defineEmits<{
  imported: []
}>()

const {
  declaredActions,
  exporting,
  exportDialogVisible,
  canChooseExportMode,
  canChooseImportMode,
  importDialogVisible,
  pendingImportAction,
  toolbarActionVisible,
  toolbarActionPerm,
  toolbarActionLabel,
  toolbarActionIcon,
  toolbarActionType,
  toolbarActionLoading,
  runToolbarAction,
  confirmExport,
} = useGenExcelToolbar({
  apiModule: () => props.apiModule,
  businessName: () => props.businessName,
  api: () => props.api,
  perm: () => props.perm,
  readOnly: () => props.readOnly,
  exportQuery: () => props.exportQuery,
  resolveLabel: props.labelResolver,
  toolbarActions: () => props.actions,
})

const hasImportAction = computed(() => declaredActions.value.some((a) => a.builtin === 'import'))
</script>
