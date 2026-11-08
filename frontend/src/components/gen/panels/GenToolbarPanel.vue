<template>
  <el-button
    v-if="!readOnly && perm.add"
    type="primary"
    :icon="Plus"
    v-hasPermi="[perm.add]"
    @click="openForm()"
  >
    {{ t('common.add') }}
  </el-button>
  <GenExcelToolbarActions
    :actions="excelToolbarActions"
    :read-only="readOnly"
    :perm="perm"
    :api="api"
    :api-module="apiModule"
    :business-name="businessName"
    :export-query="queryParams"
    :label-resolver="gp"
    :resolve-field-validation="resolveFieldValidation"
    @imported="loadPage"
  />
  <el-button
    v-for="action in batchDeleteToolbarActions"
    :key="action.id"
    v-show="!readOnly && batchDeletePerm(action).length"
    type="danger"
    plain
    :disabled="!selectedDeletableIds.length"
    v-hasPermi="batchDeletePerm(action)"
    @click="handleBatchDelete(action)"
  >
    {{ batchDeleteLabel(action) }}
  </el-button>
</template>

<script setup lang="ts">
import { Plus } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import GenExcelToolbarActions from '@/components/gen/excel/GenExcelToolbarActions.vue'
import { useGenCrudContext } from '@/composables/gen/useGenCrudContext'

const { t } = useI18n()
const {
  readOnly,
  perm,
  api,
  apiModule,
  businessName,
  list,
  form,
  ext,
  gp,
  resolveFieldValidation,
} = useGenCrudContext()

const queryParams = list.queryParams
const excelToolbarActions = ext.excelToolbarActions
const batchDeleteToolbarActions = ext.batchDeleteToolbarActions
const selectedDeletableIds = list.selectedDeletableIds
const batchDeletePerm = ext.batchDeletePerm
const batchDeleteLabel = ext.batchDeleteLabel
const handleBatchDelete = ext.handleBatchDelete
const loadPage = list.loadPage
const openForm = form.openForm
</script>
