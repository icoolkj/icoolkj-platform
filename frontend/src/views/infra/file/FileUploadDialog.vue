<!-- 管理端文件上传：须选 profileCode 作为 bizType；存储引擎 Tab 为特殊场景，勿换成 AttachmentManager -->
<template>
  <GenFormShell v-model="visible" :title="gp('uploadTitle')" :width="860">
    <el-form label-width="120px" class="upload-form" @submit.prevent>
      <el-form-item :label="gp('uploadBizType')" required>
        <el-select
          v-model="bizType"
          filterable
          clearable
          style="width: 100%"
          :loading="profilesLoading"
          :placeholder="gp('uploadBizTypePlaceholder')"
          :disabled="uploading"
        >
          <el-option
            v-for="item in enabledProfiles"
            :key="item.profileCode"
            :label="profileOptionLabel(item)"
            :value="item.profileCode"
          />
        </el-select>
        <div v-if="!profilesLoading && !enabledProfiles.length" class="upload-tip warn">
          {{ gp('uploadProfileEmpty') }}
        </div>
        <div v-else class="upload-tip">{{ gp('uploadBizTypeHint') }}</div>
        <div v-if="selectedProfileHint" class="upload-tip">{{ selectedProfileHint }}</div>
      </el-form-item>
    </el-form>

    <el-tabs v-model="activeStorage" class="upload-tabs">
      <el-tab-pane
        v-for="provider in enabledProviders"
        :key="provider"
        :label="storageLabel(provider)"
        :name="provider"
      />
    </el-tabs>

    <el-upload
      drag
      :auto-upload="false"
      :limit="1"
      :on-change="handleFileChange"
      :on-exceed="handleExceed"
      :on-remove="handleRemove"
      :disabled="uploading"
    >
      <el-icon class="upload-icon"><UploadFilled /></el-icon>
      <div class="el-upload__text">{{ gp('uploadHint') }}</div>
      <template #tip>
        <div class="el-upload__tip">{{ gp('uploadSingleHint', { size: maxFileSizeMb }) }}</div>
      </template>
    </el-upload>

    <template #footer>
      <el-button @click="visible = false">{{ t('common.cancel') }}</el-button>
      <el-button
        type="primary"
        :loading="uploading"
        :disabled="!selectedFile || !bizType"
        @click="submit"
      >
        {{ t('common.confirm') }}
      </el-button>
    </template>
  </GenFormShell>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import type { UploadFile, UploadFiles } from 'element-plus'
import { useI18n } from 'vue-i18n'
import GenFormShell from '@/components/gen/layout/GenFormShell.vue'
import { uploadInfraFile } from '@/api/infra/file'
import { listFileProfiles } from '@/api/infra/fileProfile'
import { isStatusNormal } from '@/constants/flags'
import { formatByteSize } from '@/utils/fileUploadConstraints'
import type { FileStorageOptionsVO } from '@/types/infra/file'
import type { FileProfileVO } from '@/types/infra/fileProfile'

const props = defineProps<{
  modelValue: boolean
  options: FileStorageOptionsVO | null
  gp: (key: string, params?: Record<string, string | number>) => string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  success: []
}>()

const { t, locale } = useI18n()

const visible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value),
})

const activeStorage = ref('')
const bizType = ref('')
const selectedFile = ref<File | null>(null)
const uploading = ref(false)
const profilesLoading = ref(false)
const profiles = ref<FileProfileVO[]>([])

const enabledProviders = computed(() => props.options?.enabledProviders ?? [])
const maxFileSizeMb = computed(() => props.options?.maxFileSizeMb ?? 100)

const enabledProfiles = computed(() =>
  profiles.value.filter((p) => p.profileCode && isStatusNormal(p.status)),
)

const selectedProfile = computed(() =>
  enabledProfiles.value.find((p) => p.profileCode === bizType.value),
)

const selectedProfileHint = computed(() => {
  const p = selectedProfile.value
  if (!p) return ''
  const parts: string[] = []
  if (p.allowedExts?.length) {
    parts.push(props.gp('uploadProfileExts', { exts: p.allowedExts.join(', ') }))
  }
  if (p.maxSizeBytes && p.maxSizeBytes > 0) {
    parts.push(props.gp('uploadProfileMaxSize', { size: formatByteSize(p.maxSizeBytes) }))
  }
  return parts.join(' · ')
})

watch(
  () => props.modelValue,
  async (open) => {
    if (open) {
      activeStorage.value = props.options?.defaultProvider ?? enabledProviders.value[0] ?? 'minio'
      await loadProfiles()
    } else {
      handleClosed()
    }
  },
)

async function loadProfiles() {
  profilesLoading.value = true
  try {
    const res = await listFileProfiles()
    profiles.value = res.data.data ?? []
  } catch {
    profiles.value = []
  } finally {
    profilesLoading.value = false
  }
}

function profileOptionLabel(item: FileProfileVO): string {
  const loc = String(locale.value)
  const localized = item.profileNameI18n?.[loc] || item.profileNameI18n?.['zh-CN']
  const name = localized || item.profileName || item.profileCode
  return `${name} (${item.profileCode})`
}

function storageLabel(provider: string) {
  const map: Record<string, string> = {
    local: props.gp('storageLocal'),
    minio: props.gp('storageMinio'),
  }
  return map[provider] ?? provider
}

function handleFileChange(file: UploadFile, _files: UploadFiles) {
  selectedFile.value = file.raw ?? null
}

function handleRemove() {
  selectedFile.value = null
}

function handleExceed() {
  ElMessage.warning(props.gp('uploadSingleOnly'))
}

function handleClosed() {
  selectedFile.value = null
  bizType.value = ''
  uploading.value = false
}

async function submit() {
  if (!selectedFile.value || !activeStorage.value) return
  const code = bizType.value.trim()
  if (!code) {
    ElMessage.warning(props.gp('bizTypeRequired'))
    return
  }
  const maxBytes = maxFileSizeMb.value * 1024 * 1024
  if (selectedFile.value.size > maxBytes) {
    ElMessage.error(props.gp('uploadSizeExceeded', { size: maxFileSizeMb.value }))
    return
  }
  const formData = new FormData()
  formData.append('file', selectedFile.value)
  formData.append('storageType', activeStorage.value)
  formData.append('bizType', code)
  uploading.value = true
  try {
    await uploadInfraFile(formData)
    ElMessage.success(t('common.success'))
    visible.value = false
    emit('success')
  } finally {
    uploading.value = false
  }
}
</script>

<style scoped>
.upload-form {
  margin-bottom: 8px;
}
.upload-tabs {
  margin-bottom: 12px;
}
.upload-icon {
  font-size: 48px;
  color: var(--el-color-primary);
}
.upload-tip {
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.5;
  color: var(--el-text-color-secondary);
}
.upload-tip.warn {
  color: var(--el-color-warning);
}
</style>
