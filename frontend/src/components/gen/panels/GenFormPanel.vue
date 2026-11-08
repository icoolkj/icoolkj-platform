<template>
  <GenFormShell
    v-if="variant === 'dialog'"
    v-model="formVisible"
    :title="formTitle"
    :width="formConfig.dialogWidth ?? 860"
    :close-on-click-modal="formConfig.closeOnClickModal ?? false"
    :close-on-press-escape="formConfig.closeOnPressEscape ?? false"
  >
    <GenEntityForm
      ref="entityFormRef"
      :model="formModel"
      :fields="visibleFormFields"
      :rules="formRules"
      :label-width="labelWidth"
      :gf="gf"
      :gp="gp"
      :get-dict="getDict"
      :tree-sources="treeSources"
      :is-field-disabled="isFieldDisabled"
    />
    <template #footer>
      <el-button @click="closeForm">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" :loading="submitting" @click="submitForm">
        {{ t('common.confirm') }}
      </el-button>
    </template>
  </GenFormShell>
  <GenEntityForm
    v-else
    ref="entityFormRef"
    :model="formModel"
    :fields="visibleFormFields"
    :rules="formRules"
    :label-width="labelWidth"
    :gf="gf"
    :gp="gp"
    :get-dict="getDict"
    :tree-sources="treeSources"
    :is-field-disabled="isFieldDisabled"
  />
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import GenEntityForm from '@/components/gen/fields/GenEntityForm.vue'
import GenFormShell from '@/components/gen/layout/GenFormShell.vue'
import { useGenCrudContext } from '@/composables/gen/useGenCrudContext'

defineProps<{
  variant: 'inline' | 'dialog'
}>()

const { t } = useI18n()
const { form, formRules, gf, gp, getDict, treeSources } = useGenCrudContext()

const formVisible = form.formVisible
const formModel = form.form
const formTitle = form.formTitle
const formConfig = form.formConfig
const visibleFormFields = form.visibleFormFields
const submitting = form.submitting
const closeForm = form.closeForm
const submitForm = form.submitForm
const isFieldDisabled = form.isFieldDisabled
const bindEntityFormRef = form.bindEntityFormRef

const entityFormRef = ref<InstanceType<typeof GenEntityForm>>()
const labelWidth = computed(() => `${formConfig.value.labelWidth ?? 160}px`)

watch(entityFormRef, (r) => bindEntityFormRef(r), { immediate: true })
onBeforeUnmount(() => bindEntityFormRef(undefined))
</script>
