<!--
  P01/P02/P08 形态积木 — 标准分页 CRUD / 只读审计 / customForm。
  框架映射：layout standard-crud | custom-form → 本组件。
-->
<template>
  <div v-loading="!genReady || !viewReady || !dictReady || !treeSourcesReady || loading">
    <GenListLayout
      v-if="isPageFormMode && formVisible"
      :title="formTitle"
      :description="gp('description')"
      :show-search="false"
      :show-right-toolbar="false"
    >
      <template #header-extra>
        <el-button @click="closeForm">{{ t('common.back') }}</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">
          {{ t('common.save') }}
        </el-button>
      </template>
      <GenFormPanel variant="inline" />
    </GenListLayout>

    <GenListLayout
      v-show="!(isPageFormMode && formVisible)"
      :title="gp('title')"
      :description="gp('description')"
      :hide-header="hideHeader"
      :show-search="showSearch"
      @refresh="loadPage"
      @toggle-search="showSearch = !showSearch"
    >
      <template #search>
        <GenQueryPanel />
      </template>

      <template v-if="$slots['table-top']" #table-top>
        <slot name="table-top" />
      </template>

      <template #toolbar>
        <GenToolbarPanel />
      </template>

      <template #toolbar-extra>
        <slot name="toolbar-extra" />
      </template>

      <GenTablePanel>
        <template #row-actions="slotProps">
          <slot name="row-actions" v-bind="slotProps" />
        </template>
      </GenTablePanel>

      <template #footer>
        <Pagination
          v-show="total > 0"
          v-model:page="queryParams.pageNo"
          v-model:limit="queryParams.pageSize"
          :total="total"
          @pagination="loadPage"
        />
      </template>
    </GenListLayout>

    <GenFormPanel v-if="!customForm && !isPageFormMode" variant="dialog" />

    <component
      :is="dialogComp"
      v-for="(dialogComp, dialogId) in mountedDialogs"
      :key="dialogId"
      v-model="dialogState[dialogId].visible"
      v-bind="{ ...dialogState[dialogId].props, ...dialogPageContext }"
    />
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { unref } from 'vue'
import Pagination from '@/components/Pagination.vue'
import GenListLayout from '@/components/gen/layout/GenListLayout.vue'
import GenFormPanel from '@/components/gen/panels/GenFormPanel.vue'
import GenQueryPanel from '@/components/gen/panels/GenQueryPanel.vue'
import GenTablePanel from '@/components/gen/panels/GenTablePanel.vue'
import GenToolbarPanel from '@/components/gen/panels/GenToolbarPanel.vue'
import { createGenPageExpose } from '@/components/gen/patterns/expose'
import type { GenPageEmits, GenPatternStandardCrudProps } from '@/components/gen/patterns/types'
import { useGenCrudPage } from '@/composables/gen/useGenCrudPage'

export type { GenCrudApi, GenCrudPerm } from '@/types/gen/crudApi'

const props = defineProps<GenPatternStandardCrudProps>()

const emit = defineEmits<GenPageEmits>()

const { t } = useI18n()
const ctx = useGenCrudPage(props, emit)

const {
  genReady,
  viewReady,
  dictReady,
  treeSourcesReady,
  gp,
  customForm,
  showSearch,
  list,
  form,
  ext,
  clearSelection,
} = ctx

const loading = list.loading
const total = list.total
const queryParams = list.queryParams
const loadPage = list.loadPage
const isPageFormMode = form.isPageFormMode
const formVisible = form.formVisible
const formTitle = form.formTitle
const submitting = form.submitting
const closeForm = form.closeForm
const submitForm = form.submitForm
const mountedDialogs = ext.mountedDialogs
const dialogState = ext.dialogState
const dialogPageContext = ext.dialogPageContext

defineExpose(
  createGenPageExpose({
    openForm: form.openForm,
    loadPage,
    clearSelection,
    get form() {
      return unref(form.form)
    },
    get queryParams() {
      return queryParams
    },
    get selectedRows() {
      return unref(list.selectedRows)
    },
  }),
)
</script>
