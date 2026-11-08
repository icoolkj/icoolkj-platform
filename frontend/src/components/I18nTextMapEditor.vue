<!-- 按语言配置表动态填写业务名称，产出 locale→text map（落库为 *_i18n JSON） -->
<template>
  <div class="i18n-text-map-editor">
    <el-form-item
      v-for="(loc, index) in localeRows"
      :key="loc.value"
      :label="itemLabel(loc)"
      :required="required && index === 0"
      :prop="propPath ? `${propPath}.${loc.value}` : undefined"
    >
      <el-input
        :model-value="modelValue[loc.value] ?? ''"
        :placeholder="placeholder"
        :maxlength="maxlength"
        :disabled="disabled"
        show-word-limit
        @update:model-value="(v: string) => setLocaleValue(loc.value, v)"
      />
    </el-form-item>
    <p v-if="!localeRows.length" class="i18n-text-map-editor__empty">
      {{ emptyHint }}
    </p>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useLocaleStore } from '@/stores/locale'
import type { I18nTextMap } from '@/utils/i18nTextMap'

const props = withDefaults(
  defineProps<{
    modelValue: I18nTextMap
    /** 表单项标签前缀，如「目录名称」→「目录名称（简体中文）」 */
    label?: string
    placeholder?: string
    maxlength?: number
    disabled?: boolean
    required?: boolean
    /** 嵌套校验 prop 前缀，如 nameI18n */
    propPath?: string
    emptyHint?: string
  }>(),
  {
    label: '',
    placeholder: '',
    maxlength: 80,
    disabled: false,
    required: false,
    emptyHint: '暂无启用语言，请先在「语言配置」中启用',
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: I18nTextMap]
}>()

const localeStore = useLocaleStore()

const localeRows = computed(() => localeStore.localeOptions)

function itemLabel(loc: { label: string; value: string }) {
  const base = props.label?.trim()
  const name = loc.label || loc.value
  return base ? `${base}（${name}）` : name
}

function setLocaleValue(code: string, value: string) {
  emit('update:modelValue', { ...props.modelValue, [code]: value })
}
</script>

<style scoped>
.i18n-text-map-editor__empty {
  margin: 0;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>
