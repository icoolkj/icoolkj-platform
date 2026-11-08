<!-- 绑定策略 — GenRuntimePage + 自定义表单（平台级） -->
<template>
  <GenRuntimePage
    ref="pageRef"
    page-key="workflowBizBindingPolicy"
    custom-form
    @open-form="handleOpenForm"
  />

  <WorkflowBizBindingPolicyFormDialog
    v-model="formVisible"
    :policy-id="editingId"
    :gp="gp"
    :gf="gf"
    :gv="gv"
    @success="pageRef?.loadPage()"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { GenRuntimePage } from '@/components/gen'
import WorkflowBizBindingPolicyFormDialog from '@/views/workflow/biz-binding-policy/WorkflowBizBindingPolicyFormDialog.vue'
import { useGenPageShellI18n } from '@/composables/gen'

const { gp, gf, gv } = useGenPageShellI18n('workflowBizBindingPolicy')

const pageRef = ref<InstanceType<typeof GenRuntimePage>>()
const formVisible = ref(false)
const editingId = ref<string>()

function handleOpenForm(row?: Record<string, unknown>) {
  editingId.value = row?.id ? String(row.id) : undefined
  formVisible.value = true
}
</script>

