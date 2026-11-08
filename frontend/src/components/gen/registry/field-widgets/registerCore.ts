/**
 * 内核 + 平台字段 Widget 注册（应用启动一次）。
 * 基础控件走 GenFormFieldLegacy 适配；平台/重 Widget 独立组件，可后续在 domains 扩展。
 */
import { defineComponent, h, type Component } from 'vue'
import { registerFieldWidget } from './registry'
import GenFormFieldLegacy from '@/components/gen/field-widgets/GenFormFieldLegacy.vue'
import GenFieldUserWidget from '@/components/gen/field-widgets/GenFieldUserWidget.vue'
import GenFieldDeptWidget from '@/components/gen/field-widgets/GenFieldDeptWidget.vue'
import GenFieldIconPickerWidget from '@/components/gen/field-widgets/GenFieldIconPickerWidget.vue'
import GenFieldAvatarPickerWidget from '@/components/gen/field-widgets/GenFieldAvatarPickerWidget.vue'
import GenFieldCoverPickerWidget from '@/components/gen/field-widgets/GenFieldCoverPickerWidget.vue'
import GenFieldFileWidget from '@/components/gen/field-widgets/GenFieldFileWidget.vue'
import GenFieldRichEditorWidget from '@/components/gen/field-widgets/GenFieldRichEditorWidget.vue'
import type { GenFieldWidgetContext, GenFieldWidgetEmits } from './types'

const LEGACY_BUILTIN_IDS = [
  'input',
  'input-number',
  'textarea',
  'select',
  'radio',
  'date',
  'datetime',
  'daterange',
  'password',
  'tree-select',
  'checkbox',
] as const

function legacyFieldWidget(forcedKind: string): Component {
  return defineComponent({
    name: `GenFieldLegacy_${forcedKind}`,
    props: {
      ctx: { type: Object, required: true },
    },
    emits: ['update:modelValue', 'enter', 'avatar-uploaded'],
    setup(props, { emit }) {
      const ctx = props.ctx as GenFieldWidgetContext
      return () =>
        h(GenFormFieldLegacy, {
          config: ctx.config,
          modelValue: ctx.modelValue,
          placeholder: ctx.placeholder,
          mode: ctx.mode,
          disabled: ctx.disabled,
          getDict: ctx.getDict,
          getFieldOptions: ctx.getFieldOptions,
          treeData: ctx.treeData,
          treeProps: ctx.treeProps,
          formModel: ctx.formModel,
          gp: ctx.gp,
          forcedKind,
          'onUpdate:modelValue': (v: unknown) => emit('update:modelValue', v),
          onEnter: () => emit('enter'),
          onAvatarUploaded: (p: unknown) => emit('avatar-uploaded', p),
        })
    },
  })
}

function registerCtxWidget(id: string, component: Component) {
  registerFieldWidget(id, component)
}

let registered = false

export function registerCoreFieldWidgets() {
  if (registered) return
  registered = true

  for (const id of LEGACY_BUILTIN_IDS) {
    registerFieldWidget(id, legacyFieldWidget(id))
  }

  registerCtxWidget('user', GenFieldUserWidget)
  registerCtxWidget('dept', GenFieldDeptWidget)
  registerCtxWidget('icon-picker', GenFieldIconPickerWidget)
  registerCtxWidget('icon', GenFieldIconPickerWidget)
  registerCtxWidget('avatar-picker', GenFieldAvatarPickerWidget)
  registerCtxWidget('cover-picker', GenFieldCoverPickerWidget)
  registerCtxWidget('file', GenFieldFileWidget)
  registerCtxWidget('rich-editor', GenFieldRichEditorWidget)
}

export type { GenFieldWidgetContext, GenFieldWidgetEmits }
