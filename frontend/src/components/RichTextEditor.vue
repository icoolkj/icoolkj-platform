<template>
  <div class="rich-text-editor" :class="{ 'is-disabled': disabled }">
    <Toolbar
      v-if="editorRef"
      class="rich-text-editor__toolbar"
      :editor="editorRef"
      :default-config="toolbarConfig"
      mode="default"
    />
    <Editor
      v-model="htmlValue"
      class="rich-text-editor__body"
      style="height: 320px; overflow-y: hidden"
      :default-config="editorConfig"
      mode="default"
      @on-created="handleCreated"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, reactive, shallowRef, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import type { IDomEditor, IEditorConfig } from '@wangeditor/editor'
import { getFileStorageOptions, uploadInfraFile } from '@/api/infra/file'
import {
  FILE_BIZ_NOTICE_RICHTEXT,
  FILE_DOWNLOAD_SCOPE_NOTICE,
  type FileDownloadScope,
} from '@/constants/file'
import { useGenI18n } from '@/composables/useGenI18n'
import { useRichEditorFlushRegister } from '@/composables/useRichEditorRegistry'
import { useRichHtmlSession } from '@/composables/useRichHtmlSession'
import { ApiError } from '@/utils/apiError'
import { canonicalInfraDownloadUrl } from '@/utils/infraFileUrl'
import { isPlaceholderHtml } from '@/utils/richHtmlImagesCore'
import type { GenI18nMessages } from '@/types/tool/genI18n'
import genI18nZhCN from '@/i18n/gen/notice/zh-CN.json'
import genI18nEnUS from '@/i18n/gen/notice/en-US.json'
import '@wangeditor/editor/dist/css/style.css'

const GEN_I18N_FILES: Record<string, GenI18nMessages> = {
  'zh-CN': genI18nZhCN as GenI18nMessages,
  'en-US': genI18nEnUS as GenI18nMessages,
}

const props = withDefaults(
  defineProps<{
    modelValue?: string
    placeholder?: string
    disabled?: boolean
    /** Upload profileCode；公告页默认 notice_richtext，OA 应由宿主传入 oa_instance */
    bizType?: string
    /** 图片预览/下载通道；OA 用 infra，公告用 notice */
    downloadScope?: FileDownloadScope
  }>(),
  {
    bizType: FILE_BIZ_NOTICE_RICHTEXT,
    downloadScope: FILE_DOWNLOAD_SCOPE_NOTICE,
  },
)

const emit = defineEmits<{ 'update:modelValue': [string] }>()
const { gp } = useGenI18n('notice', 'system', GEN_I18N_FILES)
const sessionOptions = reactive<{ downloadScope: FileDownloadScope }>({
  downloadScope: props.downloadScope ?? FILE_DOWNLOAD_SCOPE_NOTICE,
})
watch(
  () => props.downloadScope,
  (scope) => {
    sessionOptions.downloadScope = scope ?? FILE_DOWNLOAD_SCOPE_NOTICE
  },
)
const richHtml = useRichHtmlSession(sessionOptions)

function uploadErrorMessage(error: unknown, fallback: string): string {
  if (error instanceof ApiError && error.message.trim()) {
    return error.message.trim()
  }
  if (error instanceof Error && error.message.trim()) {
    return error.message.trim()
  }
  if (error && typeof error === 'object') {
    const ax = error as { response?: { data?: { message?: string } }; message?: string }
    const fromApi = ax.response?.data?.message?.trim()
    if (fromApi) return fromApi
  }
  return fallback
}

const editorRef = shallowRef<IDomEditor>()
const htmlValue = shallowRef('')
let resolveVersion = 0
let suppressEmit = false
let hydrating = false
let lastEmittedCanonical: string | null = null

const toolbarConfig = {}

function emitCanonical(html: string) {
  if (hydrating && isPlaceholderHtml(html)) {
    return
  }
  const canonical = richHtml.canonicalize(html)
  if (canonical === lastEmittedCanonical) return
  lastEmittedCanonical = canonical
  emit('update:modelValue', canonical)
}

function flushModelValue() {
  if (!editorRef.value) return
  emitCanonical(editorRef.value.getHtml())
}

useRichEditorFlushRegister(flushModelValue)

defineExpose({ flushModelValue })

async function waitForEditor(maxAttempts = 50): Promise<IDomEditor | null> {
  for (let i = 0; i < maxAttempts; i += 1) {
    if (editorRef.value) return editorRef.value
    await nextTick()
  }
  return null
}

async function loadDisplayHtml(sourceHtml: string) {
  const version = ++resolveVersion
  hydrating = true
  suppressEmit = true
  richHtml.revokeAll()
  try {
    const display = sourceHtml ? await richHtml.resolveForDisplay(sourceHtml) : ''
    if (version !== resolveVersion) return
    // 只走 v-model → Editor，禁止再 editor.setHtml，避免双重 setHtml 导致 Slate 失同步
    htmlValue.value = display
    await waitForEditor()
    await nextTick()
    if (version !== resolveVersion) return
    lastEmittedCanonical = richHtml.canonicalize(sourceHtml || '')
  } finally {
    if (version === resolveVersion) {
      hydrating = false
      suppressEmit = false
    }
  }
}

async function uploadEditorFile(
  file: File,
  insertFn: (url: string, altOrPoster: string, href?: string) => void,
  options: { kind: 'image' | 'video' },
) {
  const isImage = options.kind === 'image'
  if (isImage && !file.type.startsWith('image/')) {
    ElMessage.warning(gp('richEditorImageRequired'))
    return
  }
  if (!isImage && !file.type.startsWith('video/')) {
    ElMessage.warning(gp('richEditorVideoRequired'))
    return
  }
  suppressEmit = true
  const fallback = isImage ? gp('richEditorUploadFailed') : gp('richEditorVideoUploadFailed')
  try {
    const optionsRes = await getFileStorageOptions({ silentError: true })
    const provider = optionsRes.data.data.defaultProvider ?? 'minio'
    const formData = new FormData()
    formData.append('file', file)
    formData.append('storageType', provider)
    formData.append('bizType', props.bizType || FILE_BIZ_NOTICE_RICHTEXT)
    const res = await uploadInfraFile(formData, { silentError: true })
    const data = res.data.data
    const fileId = data?.id?.trim()
    // enrichVo 在可见性未通过时会清空 fileUrl（如 OA 草稿未绑定 instance）；用 id 拼鉴权下载路径
    const fileUrl = data?.fileUrl?.trim() || (fileId ? canonicalInfraDownloadUrl(fileId) : '')
    if (!fileUrl) {
      ElMessage.error(fallback)
      return
    }
    const previewUrl =
      (await richHtml.fetchBlobUrl(fileUrl)) ||
      richHtml.resolveDirectMediaUrl(fileUrl) ||
      fileUrl
    if (isImage) {
      insertFn(previewUrl, file.name, previewUrl)
    } else {
      insertFn(previewUrl, '')
    }
    await nextTick()
    if (editorRef.value) {
      emitCanonical(editorRef.value.getHtml())
    }
  } catch (error) {
    ElMessage.error(uploadErrorMessage(error, fallback))
  } finally {
    suppressEmit = false
  }
}

async function uploadEditorImage(
  file: File,
  insertFn: (url: string, altOrPoster: string, href?: string) => void,
) {
  await uploadEditorFile(file, insertFn, { kind: 'image' })
}

async function uploadEditorVideo(file: File, insertFn: (url: string, poster: string) => void) {
  await uploadEditorFile(file, insertFn, { kind: 'video' })
}

const editorConfig = computed<Partial<IEditorConfig>>(() => ({
  placeholder: props.placeholder ?? '',
  MENU_CONF: {
    uploadImage: {
      base64LimitSize: 0,
      customUpload: uploadEditorImage,
    },
    uploadVideo: {
      maxFileSize: 100 * 1024 * 1024,
      allowedFileTypes: ['video/*'],
      customUpload: uploadEditorVideo,
    },
  },
}))

watch(
  () => props.modelValue,
  async (value) => {
    const incoming = value ?? ''
    if (!hydrating && incoming === lastEmittedCanonical) return
    await loadDisplayHtml(incoming)
  },
  { immediate: true },
)

watch(
  () => props.disabled,
  (disabled) => {
    if (!editorRef.value) return
    if (disabled) {
      editorRef.value.disable()
    } else {
      editorRef.value.enable()
    }
  },
)

function handleCreated(editor: IDomEditor) {
  editorRef.value = editor
  if (props.disabled) {
    editor.disable()
  }
  editor.on('change', () => {
    if (suppressEmit || hydrating) return
    emitCanonical(editor.getHtml())
  })
  if (
    htmlValue.value &&
    isPlaceholderHtml(editor.getHtml()) &&
    !isPlaceholderHtml(htmlValue.value)
  ) {
    suppressEmit = true
    editor.setHtml(htmlValue.value)
    void nextTick(() => {
      suppressEmit = false
    })
  }
}

onBeforeUnmount(() => {
  editorRef.value?.destroy()
  richHtml.revokeAll()
})
</script>

<style scoped lang="scss">
.rich-text-editor {
  width: 100%;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  overflow: hidden;

  &__toolbar {
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  &__body {
    height: 320px;
    overflow-y: hidden;

    :deep(video) {
      max-width: 100%;
    }
  }

  &.is-disabled {
    opacity: 0.75;
  }
}
</style>
