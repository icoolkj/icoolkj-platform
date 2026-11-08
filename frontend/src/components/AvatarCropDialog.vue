<template>
  <GenFormShell v-model="visible" :title="gp('cropTitle')" :width="920">
    <div class="avatar-crop-layout">
      <div class="crop-workspace">
        <div class="crop-stage">
          <img v-if="imageSrc" ref="imageRef" :src="imageSrc" alt="" class="crop-image" />
          <div v-else class="crop-empty">{{ gp('cropSelectHint') }}</div>
        </div>
        <div class="crop-toolbar">
          <el-button @click="zoom(0.1)">{{ gp('cropZoomIn') }}</el-button>
          <el-button @click="zoom(-0.1)">{{ gp('cropZoomOut') }}</el-button>
          <el-button @click="rotate(-90)">{{ gp('cropRotateLeft') }}</el-button>
          <el-button @click="rotate(90)">{{ gp('cropRotateRight') }}</el-button>
        </div>
        <div class="crop-upload-row">
          <el-upload
            :show-file-list="false"
            accept="image/jpeg,image/png,image/webp,image/gif"
            :before-upload="handleSelect"
          >
            <el-button type="primary" :icon="Upload">{{ gp('cropUpload') }}</el-button>
          </el-upload>
          <span class="crop-tip">{{ gp('cropSizeHint', { size: policy?.maxSizeMb ?? 2 }) }}</span>
        </div>
      </div>

      <div class="crop-preview-panel">
        <div class="preview-title">{{ gp('cropPreview') }}</div>
        <div class="preview-lg preview-box" />
        <div class="preview-md preview-box" />
        <div class="preview-sm preview-box" />
      </div>
    </div>

    <template #footer>
      <el-button @click="visible = false">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" :loading="submitting" :disabled="!cropper" @click="confirm">
        {{ t('common.confirm') }}
      </el-button>
    </template>
  </GenFormShell>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import type Cropper from 'cropperjs'
import { Upload } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import GenFormShell from '@/components/gen/layout/GenFormShell.vue'
import type { UserAvatarPolicy } from '@/types/profile'

const props = defineProps<{
  modelValue: boolean
  policy: UserAvatarPolicy | null
  gp: (key: string, params?: Record<string, string | number>) => string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  confirm: [file: File]
}>()

const { t } = useI18n()

const visible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value),
})

const imageRef = ref<HTMLImageElement>()
const imageSrc = ref('')
const cropper = ref<Cropper | null>(null)
const submitting = ref(false)
let cropperModule: typeof import('cropperjs') | null = null

watch(
  () => props.modelValue,
  (open) => {
    if (!open) {
      destroyCropper()
      imageSrc.value = ''
    }
  },
)

onBeforeUnmount(() => destroyCropper())

async function ensureCropperModule() {
  if (!cropperModule) {
    await import('cropperjs/dist/cropper.css')
    cropperModule = await import('cropperjs')
  }
  return cropperModule.default
}

function destroyCropper() {
  cropper.value?.destroy()
  cropper.value = null
}

async function initCropper() {
  await nextTick()
  if (!imageRef.value) return
  const CropperCtor = await ensureCropperModule()
  destroyCropper()
  cropper.value = new CropperCtor(imageRef.value, {
    aspectRatio: 1,
    viewMode: 1,
    dragMode: 'move',
    autoCropArea: 1,
    preview: '.preview-box',
    background: false,
  })
}

function handleSelect(file: File) {
  const maxMb = props.policy?.maxSizeMb ?? 2
  if (file.size > maxMb * 1024 * 1024) {
    ElMessage.warning(props.gp('cropSizeHint', { size: maxMb }))
    return false
  }
  const reader = new FileReader()
  reader.onload = async () => {
    imageSrc.value = String(reader.result || '')
    await initCropper()
  }
  reader.readAsDataURL(file)
  return false
}

function zoom(ratio: number) {
  cropper.value?.zoom(ratio)
}

function rotate(degree: number) {
  cropper.value?.rotate(degree)
}

async function confirm() {
  if (!cropper.value) {
    return
  }
  submitting.value = true
  try {
    const canvas = cropper.value.getCroppedCanvas({
      width: props.policy?.outputSize ?? 200,
      height: props.policy?.outputSize ?? 200,
      imageSmoothingQuality: 'high',
    })
    const blob = await new Promise<Blob | null>((resolve) =>
      canvas.toBlob(resolve, 'image/jpeg', 0.92),
    )
    if (!blob) {
      ElMessage.error(props.gp('cropFailed'))
      return
    }
    emit('confirm', new File([blob], 'avatar.jpg', { type: 'image/jpeg' }))
    visible.value = false
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.avatar-crop-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 180px;
  gap: 20px;
}

.crop-stage {
  width: 100%;
  height: 360px;
  background:
    linear-gradient(45deg, #eee 25%, transparent 25%) 0 0 / 16px 16px,
    linear-gradient(45deg, transparent 75%, #eee 75%) 0 0 / 16px 16px,
    linear-gradient(45deg, transparent 75%, #eee 75%) 8px 8px / 16px 16px,
    linear-gradient(45deg, #eee 25%, #fff 25%) 8px 8px / 16px 16px;
  overflow: hidden;
}

.crop-image {
  display: block;
  max-width: 100%;
}

.crop-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--ic-text-secondary);
}

.crop-toolbar,
.crop-upload-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
  align-items: center;
}

.crop-tip {
  font-size: 12px;
  color: var(--ic-text-secondary);
}

.crop-preview-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.preview-title {
  font-size: 14px;
  color: #434343;
}

.preview-lg {
  width: 120px;
  height: 120px;
}

.preview-md {
  width: 80px;
  height: 80px;
}

.preview-sm {
  width: 40px;
  height: 40px;
}

.preview-box {
  overflow: hidden;
  border-radius: 50%;
  border: 1px solid #e8e8e8;
  background: #fafafa;
}

@media (max-width: 860px) {
  .avatar-crop-layout {
    grid-template-columns: 1fr;
  }
}
</style>
