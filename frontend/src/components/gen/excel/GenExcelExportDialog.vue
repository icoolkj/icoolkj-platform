<template>
  <GenFormShell v-model="visible" :title="t('common.export')" :width="440">
    <el-form label-width="96px" @submit.prevent>
      <el-form-item :label="t('common.excelExecMode')" required>
        <el-radio-group v-model="execMode" :disabled="confirming">
          <el-radio value="async">{{ t('common.exportAsync') }}</el-radio>
          <el-radio value="sync">{{ t('common.exportSync') }}</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :disabled="confirming" @click="visible = false">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" :loading="confirming" @click="confirm">
        {{ execMode === 'async' ? t('common.exportAsync') : t('common.exportSync') }}
      </el-button>
    </template>
  </GenFormShell>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import GenFormShell from '@/components/gen/layout/GenFormShell.vue'

const props = defineProps<{
  modelValue: boolean
  confirming?: boolean
  defaultMode?: 'sync' | 'async'
}>()

const emit = defineEmits<{
  'update:modelValue': [boolean]
  confirm: [mode: 'sync' | 'async']
}>()

const { t } = useI18n()
const execMode = ref<'sync' | 'async'>('async')

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const confirming = computed(() => Boolean(props.confirming))

watch(
  () => props.modelValue,
  (open) => {
    if (open) {
      execMode.value = props.defaultMode ?? 'async'
    }
  },
)

function confirm() {
  if (confirming.value) return
  emit('confirm', execMode.value)
}
</script>
