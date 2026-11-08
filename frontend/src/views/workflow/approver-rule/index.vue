<!-- 审批人规则 — GenRuntimePage + view.extensions；平台归属用表格 tag 列 -->
<template>
  <GenRuntimePage
    ref="pageRef"
    page-key="workflowApproverRule"
    custom-form
    :api-override="apiOverride"
    :row-action-visible="rowActionVisible"
    @open-form="handleOpenForm"
  />

  <WorkflowApproverRuleFormDialog
    v-model="formVisible"
    :rule-id="editingId"
    :gp="gp"
    :gf="gf"
    :gv="gv"
    @success="pageRef?.loadPage()"
  />
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { GenRuntimePage } from '@/components/gen'
import type { GenCrudApi } from '@/types/gen/crudApi'
import type { GenViewRowAction } from '@/types/tool/genView'
import WorkflowApproverRuleFormDialog from '@/views/workflow/approver-rule/WorkflowApproverRuleFormDialog.vue'
import { useGenPageShellI18n } from '@/composables/gen'
import { pageWorkflowApproverRule } from '@/api/workflow/approverRule'
import { isPlatformTenant } from '@/constants/flags'

const { gp, gf, gv } = useGenPageShellI18n('workflowApproverRule')

function isPlatformOwned(row: Record<string, unknown>) {
  return isPlatformTenant(String(row.tenantId ?? ''))
}

const apiOverride = computed<Partial<GenCrudApi>>(() => ({
  page: async (params) => {
    const res = await pageWorkflowApproverRule(params)
    const payload = res.data.data
    const list = (payload.list ?? []).map((row) => ({
      ...(row as Record<string, unknown>),
      ownerScope: isPlatformOwned(row as Record<string, unknown>) ? gp('platformOwned') : '',
    }))
    return {
      ...res,
      data: {
        ...res.data,
        data: {
          ...payload,
          list,
        },
      },
    }
  },
}))

const pageRef = ref<InstanceType<typeof GenRuntimePage>>()
const formVisible = ref(false)
const editingId = ref<string>()

function handleOpenForm(row?: Record<string, unknown>) {
  if (row && isPlatformOwned(row)) return
  editingId.value = row?.id ? String(row.id) : undefined
  formVisible.value = true
}

function rowActionVisible(action: GenViewRowAction, row: Record<string, unknown>) {
  if ((action.id === 'edit' || action.id === 'delete') && isPlatformOwned(row)) return false
  return undefined
}
</script>

