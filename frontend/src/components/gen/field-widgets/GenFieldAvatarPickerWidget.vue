<template>
  <AvatarPicker
    :model-value="(ctx.modelValue as string) ?? ''"
    :avatar-file-id="String(ctx.formModel?.avatarFileId ?? '')"
    :avatar-url-sm="String(ctx.formModel?.avatarUrlSm ?? '')"
    mode="admin"
    :target-user-id="String(ctx.formModel?.id ?? '')"
    :disabled="ctx.disabled"
    :display-name="String(ctx.formModel?.nickname || ctx.formModel?.username || '')"
    :gp="ctx.gp"
    @update:model-value="emit('update:modelValue', $event)"
    @update:avatar-file-id="updateFormField('avatarFileId', $event)"
    @uploaded="onUploaded"
  />
</template>

<script setup lang="ts">
import { defineAsyncComponent } from 'vue'
import type { UserProfile } from '@/types/profile'
import type { GenFieldWidgetContext, GenFieldWidgetEmits } from '@/components/gen/registry/field-widgets/types'

const AvatarPicker = defineAsyncComponent(() => import('@/components/AvatarPicker.vue'))

const props = defineProps<{ ctx: GenFieldWidgetContext }>()
const emit = defineEmits<GenFieldWidgetEmits>()

function updateFormField(field: string, value: string) {
  if (!props.ctx.formModel) return
  // eslint-disable-next-line vue/no-mutating-props -- shared form bag
  props.ctx.formModel[field] = value
}

function onUploaded(payload: UserProfile) {
  if (props.ctx.formModel) {
    /* eslint-disable vue/no-mutating-props -- shared form bag */
    props.ctx.formModel.avatarFileId = payload.avatarFileId
    props.ctx.formModel.avatarUrl = payload.avatarUrl
    props.ctx.formModel.avatarUrlSm = payload.avatarUrlSm
    /* eslint-enable vue/no-mutating-props */
  }
  emit('avatar-uploaded', payload)
}
</script>
