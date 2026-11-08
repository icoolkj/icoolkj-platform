<template>
  <el-switch
    v-if="isNativeSwitch(field.componentType)"
    :model-value="Boolean(modelValue)"
    :disabled="disabled"
    @update:model-value="emit('update:modelValue', $event)"
  />
  <GenFormField
    v-else
    :model-value="modelValue"
    :config="config"
    mode="form"
    :get-dict="getDict"
    :placeholder="placeholder"
    :disabled="disabled"
    @update:model-value="emit('update:modelValue', $event)"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import GenFormField from '@/components/gen/fields/GenFormField.vue'
import {
  isNativeSwitch,
  toGenFormConfig,
} from '@/components/form-designer/runtime/formFieldCore'
import type { FormFieldDef, FormRuntimeHostContext } from '@/types/form-designer/schema'
import type { DictOption } from '@/types/system/dict'

const props = defineProps<{
  field: FormFieldDef
  modelValue: unknown
  placeholder: string
  disabled?: boolean
  host?: FormRuntimeHostContext
  getDict: (dictType: string) => DictOption[]
}>()

const emit = defineEmits<{
  'update:modelValue': [value: unknown]
}>()

const config = computed(() => toGenFormConfig(props.field, props.host))
</script>
