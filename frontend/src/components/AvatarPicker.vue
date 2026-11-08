<template>
  <div class="avatar-picker">
    <el-avatar :size="previewSize" :src="displayUrl || undefined" class="avatar-preview">
      {{ fallbackText }}
    </el-avatar>
    <div class="avatar-picker-actions">
      <el-button size="small" :disabled="disabled || uploading" @click="openDialog">
        {{ buttonText }}
      </el-button>
      <span v-if="disabledReason" class="avatar-picker-hint">{{ disabledReason }}</span>
    </div>

    <AvatarCropDialog
      v-if="dialogVisible"
      v-model="dialogVisible"
      :policy="policy"
      :gp="gp"
      @confirm="handleConfirm"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import AvatarCropDialog from '@/components/AvatarCropDialog.vue'
import { useAuthImageUrl } from '@/composables/useAuthImageUrl'
import { fetchAvatarPolicy, uploadProfileAvatar } from '@/api/profile'
import { fetchUserAvatarPolicy, uploadUserAvatar } from '@/api/system/user'
import type { UserAvatarPolicy, UserProfile } from '@/types/profile'

const props = withDefaults(
  defineProps<{
    modelValue?: string
    avatarFileId?: string
    avatarUrlSm?: string
    mode?: 'profile' | 'admin'
    targetUserId?: string
    disabled?: boolean
    previewSize?: number
    displayName?: string
    gp: (key: string, params?: Record<string, string | number>) => string
  }>(),
  {
    modelValue: '',
    avatarFileId: '',
    avatarUrlSm: '',
    mode: 'profile',
    targetUserId: '',
    disabled: false,
    previewSize: 88,
    displayName: '',
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: string]
  'update:avatarFileId': [value: string]
  uploaded: [payload: UserProfile]
}>()

const dialogVisible = ref(false)
const uploading = ref(false)
const policy = ref<UserAvatarPolicy | null>(null)
const localAvatarUrlSm = ref('')

watch(
  () => props.avatarUrlSm,
  (value) => {
    localAvatarUrlSm.value = value || ''
  },
  { immediate: true },
)

const displayUrl = useAuthImageUrl(computed(() => localAvatarUrlSm.value || props.modelValue || ''))

const fallbackText = computed(() => (props.displayName || 'U').charAt(0).toUpperCase())

const buttonText = computed(() => props.gp('uploadAvatar'))

const disabledReason = computed(() => {
  if (props.mode === 'admin' && !props.targetUserId) {
    return props.gp('avatarNeedUserId')
  }
  return ''
})

async function loadPolicy() {
  if (policy.value) {
    return
  }
  policy.value = props.mode === 'admin' ? await fetchUserAvatarPolicy() : await fetchAvatarPolicy()
}

async function openDialog() {
  if (props.disabled || disabledReason.value) {
    return
  }
  try {
    await loadPolicy()
    dialogVisible.value = true
  } catch {
    ElMessage.error(props.gp('cropFailed'))
  }
}

async function handleConfirm(file: File) {
  uploading.value = true
  try {
    const data =
      props.mode === 'admin' && props.targetUserId
        ? await uploadUserAvatar(props.targetUserId, file)
        : await uploadProfileAvatar(file)
    emit('update:modelValue', data.avatarUrl || '')
    emit('update:avatarFileId', data.avatarFileId || '')
    localAvatarUrlSm.value = data.avatarUrlSm || ''
    emit('uploaded', data)
    ElMessage.success(props.gp('saveSuccess'))
  } finally {
    uploading.value = false
  }
}
</script>

<style scoped>
.avatar-picker {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar-preview {
  background: var(--el-color-primary);
  color: #fff;
  font-weight: 600;
}

.avatar-picker-actions {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.avatar-picker-hint {
  font-size: 12px;
  color: var(--ic-text-secondary);
}
</style>
