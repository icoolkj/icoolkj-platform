<template>
  <aside class="designer-props">
    <el-tabs :key="localeKey" :model-value="propsTab" @update:model-value="onTabChange">
      <el-tab-pane :label="gp('tabForm')" name="form">
        <el-form label-position="top" size="small">
          <el-form-item :label="gp('labelWidth')">
            <el-input-number
              :model-value="document.form.labelWidth"
              :min="60"
              :max="220"
              @update:model-value="emit('patchForm', { labelWidth: Number($event) || 100 })"
            />
          </el-form-item>
          <el-form-item :label="gp('labelPosition')">
            <el-radio-group
              :model-value="document.form.labelPosition"
              @update:model-value="emit('patchForm', { labelPosition: $event as 'left' | 'top' })"
            >
              <el-radio-button value="left">{{ gp('labelLeft') }}</el-radio-button>
              <el-radio-button value="top">{{ gp('labelTop') }}</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="document.form.labelPosition === 'left'" :label="gp('labelAlign')">
            <el-radio-group
              :model-value="document.form.labelAlign || 'right'"
              @update:model-value="emit('patchForm', { labelAlign: $event as 'left' | 'right' })"
            >
              <el-radio-button value="left">{{ gp('alignLeft') }}</el-radio-button>
              <el-radio-button value="right">{{ gp('alignRight') }}</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item :label="gp('formSize')">
            <el-radio-group
              :model-value="document.form.size || 'default'"
              @update:model-value="
                emit('patchForm', { size: $event as 'default' | 'small' | 'large' })
              "
            >
              <el-radio-button value="default">{{ gp('sizeDefault') }}</el-radio-button>
              <el-radio-button value="large">{{ gp('sizeLarge') }}</el-radio-button>
              <el-radio-button value="small">{{ gp('sizeSmall') }}</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item :label="gp('hideRequiredAsterisk')">
            <el-switch
              :model-value="Boolean(document.form.hideRequiredAsterisk)"
              @change="(v: string | number | boolean) => emit('patchForm', { hideRequiredAsterisk: Boolean(v) })"
            />
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane :label="gp('tabField')" name="field">
        <template v-if="selectedNode || selectedField">
          <el-form label-position="top" size="small">
            <template
              v-if="selectedNode && (selectedNode.type === 'card' || selectedNode.type === 'divider')"
            >
              <el-form-item :label="gp('nodeTitle')">
                <el-input
                  :model-value="selectedNode.title || ''"
                  @update:model-value="(v: string) => emit('patchNode', { title: v })"
                  @change="() => emit('patchNode', {}, { endSession: true })"
                />
              </el-form-item>
              <el-divider v-if="selectedField" />
            </template>

            <template v-if="selectedField">
              <el-form-item :label="gp('fieldCode')">
                <el-input
                  :model-value="selectedField.fieldCode"
                  :disabled="bound"
                  @update:model-value="(v: string) => patchFieldLive({ fieldCode: v })"
                  @change="(v: string) => patchFieldCommit({ fieldCode: v })"
                />
              </el-form-item>
              <el-form-item :label="gp('fieldName')">
                <el-input
                  :model-value="selectedField.fieldName"
                  :disabled="bound"
                  @update:model-value="(v: string) => patchFieldLive({ fieldName: v })"
                  @change="(v: string) => patchFieldCommit({ fieldName: v })"
                />
              </el-form-item>
              <el-form-item v-if="!bound" :label="gp('componentType')">
                <el-select
                  :model-value="selectedField.componentType"
                  @update:model-value="(v: string) => emit('patchField', { componentType: v as FormComponentType })"
                >
                  <el-option
                    v-for="item in basicPalette"
                    :key="item.componentType"
                    :label="gp(item.labelKey)"
                    :value="item.componentType"
                  />
                </el-select>
              </el-form-item>
              <el-form-item v-if="!bound" :label="gp('required')">
                <el-switch
                  :model-value="selectedField.required"
                  @update:model-value="(v: string | number | boolean) => emit('patchField', { required: Boolean(v) })"
                />
              </el-form-item>
              <el-form-item :label="gp('placeholder')">
                <el-input
                  :model-value="selectedField.placeholder"
                  @update:model-value="(v: string) => patchFieldLive({ placeholder: v })"
                  @change="(v: string) => patchFieldCommit({ placeholder: v })"
                />
              </el-form-item>
              <el-form-item
                v-if="!bound && needsDict(selectedField.componentType)"
                :label="gp('dictType')"
              >
                <el-select
                  :model-value="selectedField.dictType || ''"
                  filterable
                  clearable
                  :loading="dictLoading"
                  :placeholder="gp('dictTypePlaceholder')"
                  @update:model-value="(v: string) => emit('patchField', { dictType: v || undefined })"
                >
                  <el-option
                    v-for="opt in dictOptions"
                    :key="opt.dictType"
                    :label="`${opt.dictName} (${opt.dictType})`"
                    :value="opt.dictType"
                  />
                </el-select>
              </el-form-item>
              <el-form-item v-if="bound && selectedField.dictType" :label="gp('dictType')">
                <el-input :model-value="selectedField.dictType" disabled />
              </el-form-item>
              <el-form-item v-if="!bound && supportsMaxLength(selectedField.componentType)" :label="gp('maxLength')">
                <el-input-number
                  :model-value="selectedField.maxLength"
                  :min="0"
                  :max="5000"
                  @update:model-value="(v: number | undefined) => patchFieldLive({ maxLength: v ?? undefined })"
                  @change="(v: number | undefined) => patchFieldCommit({ maxLength: v ?? undefined })"
                />
              </el-form-item>
              <el-alert
                v-if="bound"
                type="info"
                :closable="false"
                show-icon
                :title="gp('boundContractHint')"
              />
            </template>
            <el-empty
              v-else-if="!selectedNode || selectedNode.type === 'row'"
              :description="gp('noFieldSelection')"
              :image-size="56"
            />
          </el-form>
        </template>
        <el-empty v-else :description="gp('noSelection')" :image-size="64" />
      </el-tab-pane>

      <el-tab-pane :label="gp('tabGrid')" name="grid" :disabled="!gridEnabled">
        <template v-if="selectedField && gridEnabled">
          <el-form label-position="top" size="small">
            <el-form-item :label="gp('gridSpan')">
              <el-radio-group
                :key="`span-preset-${selectedField.id}-${currentSpan}`"
                :model-value="currentSpanPreset"
                @change="onSpanPreset"
              >
                <el-radio-button
                  v-for="p in SPAN_PRESETS"
                  :key="p"
                  :value="p"
                >
                  {{ p }}
                </el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item :label="gp('gridSpanExact')">
              <el-slider
                :model-value="currentSpan"
                :min="1"
                :max="24"
                show-input
                :show-input-controls="true"
                @update:model-value="onSpanLive"
                @change="onSpanCommit"
              />
            </el-form-item>
            <el-alert
              v-if="rowSpanHint"
              :type="rowSpanHint.over ? 'warning' : 'info'"
              :closable="false"
              show-icon
              :title="rowSpanHint.text"
            />
          </el-form>
        </template>
        <el-empty v-else :description="gp('gridDisabledHint')" :image-size="56" />
      </el-tab-pane>
    </el-tabs>
  </aside>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { listDictTypeOptions } from '@/api/system/dict'
import { FORM_DESIGNER_PALETTE } from '@/components/form-designer/palette'
import {
  findFieldById,
  findFieldContainerId,
  findLayoutNode,
  resolveFieldSpan,
  sumChildrenSpan,
} from '@/components/form-designer/schemaUtils'
import type {
  FormComponentType,
  FormDocument,
  FormFieldDef,
  FormLayoutNode,
  FormMeta,
} from '@/types/form-designer/schema'
import type { DictTypeVO } from '@/types/system/dict'

const SPAN_PRESETS = [8, 12, 16, 24] as const

const props = withDefaults(
  defineProps<{
    document: FormDocument
    selectedFieldId?: string
    selectedNodeId?: string
    propsTab: string
    gp: (key: string, params?: Record<string, string | number>) => string
    bound?: boolean
  }>(),
  { bound: false },
)

const emit = defineEmits<{
  'update:propsTab': [value: string]
  patchForm: [patch: Partial<FormMeta>]
  patchField: [
    patch: Partial<FormFieldDef>,
    options?: { recordHistory?: boolean; endSession?: boolean },
  ]
  patchNode: [patch: { title?: string }, options?: { endSession?: boolean }]
}>()

const { locale } = useI18n()
const localeKey = computed(() => String(locale.value))

const basicPalette = FORM_DESIGNER_PALETTE.filter((p) => p.category === 'basic' && p.componentType)
const dictOptions = ref<DictTypeVO[]>([])
const dictLoading = ref(false)

/** Always resolve from live document — avoids stale field/container props. */
const selectedField = computed<FormFieldDef | undefined>(() =>
  props.selectedFieldId ? findFieldById(props.document, props.selectedFieldId) : undefined,
)

const selectedNode = computed<FormLayoutNode | undefined>(() =>
  props.selectedNodeId ? findLayoutNode(props.document, props.selectedNodeId) : undefined,
)

const fieldContainer = computed<FormLayoutNode | undefined>(() => {
  if (!props.selectedFieldId) return undefined
  const cid = findFieldContainerId(props.document, props.selectedFieldId)
  return cid ? findLayoutNode(props.document, cid) : undefined
})

const gridEnabled = computed(() => Boolean(selectedField.value))

const currentSpan = computed(() =>
  selectedField.value ? resolveFieldSpan(selectedField.value) : 8,
)

const currentSpanPreset = computed(() => {
  const s = currentSpan.value
  return (SPAN_PRESETS as readonly number[]).includes(s) ? s : undefined
})

const rowSpanHint = computed(() => {
  const container = fieldContainer.value
  if (!container || (container.type !== 'row' && container.type !== 'card')) {
    return null
  }
  const used = sumChildrenSpan(container.children)
  const over = used > 24
  return {
    over,
    text: props.gp(over ? 'gridSpanOver' : 'gridSpanUsed', { used }),
  }
})

function needsDict(componentType: FormComponentType) {
  return componentType === 'select' || componentType === 'radio' || componentType === 'checkbox'
}

function supportsMaxLength(componentType: FormComponentType) {
  return (
    componentType === 'input' ||
    componentType === 'textarea' ||
    componentType === 'richText'
  )
}

function onTabChange(tab: string | number) {
  emit('update:propsTab', String(tab))
}

/** Live typing / spinner: one undo step for the whole gesture. */
function patchFieldLive(patch: Partial<FormFieldDef>) {
  emit('patchField', patch, { recordHistory: false })
}

function patchFieldCommit(patch: Partial<FormFieldDef>) {
  emit('patchField', patch, { recordHistory: false, endSession: true })
}

function onSpanPreset(v: string | number | boolean | undefined) {
  emit('patchField', { span: Number(v) }, { recordHistory: true })
}

function onSpanLive(v: number | number[]) {
  emit('patchField', { span: Number(Array.isArray(v) ? v[0] : v) }, { recordHistory: false })
}

function onSpanCommit(v: number | number[]) {
  emit(
    'patchField',
    { span: Number(Array.isArray(v) ? v[0] : v) },
    { recordHistory: false, endSession: true },
  )
}

watch(
  () => [props.selectedFieldId, selectedNode.value?.type] as const,
  () => {
    if (selectedField.value && props.propsTab === 'form') {
      return
    }
    if (!selectedField.value && props.propsTab === 'grid') {
      emit('update:propsTab', 'field')
    }
  },
)

onMounted(async () => {
  dictLoading.value = true
  try {
    const res = await listDictTypeOptions()
    dictOptions.value = res.data.data ?? []
  } catch {
    dictOptions.value = []
  } finally {
    dictLoading.value = false
  }
})
</script>

<style scoped>
.designer-props {
  border-left: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color);
  min-height: 0;
  overflow: auto;
  padding: 4px 12px 16px;
}
.designer-props :deep(.el-tabs__header) {
  margin-bottom: 12px;
}
.designer-props :deep(.el-slider) {
  padding-right: 8px;
}
</style>
