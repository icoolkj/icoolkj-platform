<!--
  文件管理 — GenRuntimePage（standard-crud / readOnly 列表）
  上传走 toolbar-extra 扩展；预览 / 详情 / 下载为页面本地行操作 handler；
  批量删除走 toolbarActions builtin:batchDelete（仅可删行可勾选）。
-->
<template>
  <div class="gen-page-shell">
    <GenRuntimePage ref="pageRef" page-key="file" :action-handlers="actionHandlers">
      <template #toolbar-extra>
        <el-button
          v-if="!demoStore.isDemo"
          type="primary"
          :icon="Upload"
          v-hasPermi="[PERM.upload]"
          @click="uploadVisible = true"
        >
          {{ gp('upload') }}
        </el-button>
      </template>
    </GenRuntimePage>

    <FileUploadDialog
      v-model="uploadVisible"
      :options="storageOptions"
      :gp="gp"
      @success="reloadPage"
    />

    <FileDetailDrawer
      v-model="detailVisible"
      :file-id="detailFileId"
      :storage-dict="storageDict"
      :gp="gp"
      :gf="gf"
    />

    <FilePreviewViewer
      v-model="previewVisible"
      :title="previewTitle"
      :kind="previewKind"
      :object-url="previewObjectUrl"
      :loading="previewLoading"
      :gp="gp"
      @closed="revokePreviewUrl"
      @download="handlePreviewDownload"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Upload } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { GenRuntimePage } from '@/components/gen'
import type { GenRowActionHandler } from '@/components/gen/registry/extension-blocks/registry'
import { useGenPageShellI18n } from '@/composables/gen'
import { useDict } from '@/composables/useDict'
import { useDemoStore } from '@/stores/demo'
import { downloadByApi } from '@/utils/download'
import { appendAccessTokenToApiPath, toApiPath } from '@/utils/infraFileUrl'
import { resolveFileThumbKind } from '@/utils/fileThumb'
import { getToken } from '@/utils/request'
import {
  PERM_InfraFile as PERM,
  getFileStorageOptions,
  getInfraFilePreviewUrl,
} from '@/api/infra/file'
import type { FileStorageOptionsVO, FileVO } from '@/types/infra/file'
import FileUploadDialog from './FileUploadDialog.vue'
import FileDetailDrawer from './FileDetailDrawer.vue'
import FilePreviewViewer from './FilePreviewViewer.vue'

const { t } = useI18n()
const demoStore = useDemoStore()
const { gp, gf } = useGenPageShellI18n('file')
const { getDict } = useDict(['infra_storage_type'])
const storageDict = computed(() => getDict('infra_storage_type'))

const pageRef = ref<InstanceType<typeof GenRuntimePage>>()
const storageOptions = ref<FileStorageOptionsVO | null>(null)

const uploadVisible = ref(false)
const detailVisible = ref(false)
const detailFileId = ref<string | null>(null)
const previewVisible = ref(false)
const previewLoading = ref(false)
const previewTitle = ref('')
const previewKind = ref<'image' | 'pdf' | 'video' | ''>('')
const previewObjectUrl = ref('')
const previewFileId = ref<string | null>(null)
const previewFileName = ref('')

function reloadPage() {
  return pageRef.value?.loadPage()
}

function isPdfRow(row: FileVO) {
  return resolveFileThumbKind(row) === 'pdf'
}

function isVideoRow(row: FileVO) {
  return resolveFileThumbKind(row) === 'video'
}

function openDetail(row: FileVO) {
  detailFileId.value = row.id ?? null
  detailVisible.value = true
}

async function handleDownload(row: FileVO) {
  if (!row.id) return
  try {
    await downloadByApi(
      `/infra/file/${row.id}/download`,
      row.originalName ?? row.fileName ?? 'file',
    )
  } catch (err) {
    ElMessage.error(err instanceof Error ? err.message : t('common.downloadFailed'))
  }
}

async function handlePreview(row: FileVO) {
  if (!row.id || !row.previewable) return
  previewTitle.value = row.originalName ?? ''
  previewKind.value = isVideoRow(row) ? 'video' : isPdfRow(row) ? 'pdf' : 'image'
  previewFileId.value = row.id
  previewFileName.value = row.originalName ?? row.fileName ?? 'file'
  previewVisible.value = true
  previewLoading.value = true
  try {
    const res = await getInfraFilePreviewUrl(row.id)
    const payload = res.data.data
    if (payload?.external && payload.url) {
      if (previewKind.value === 'video') {
        revokePreviewUrl()
        previewObjectUrl.value = payload.url
        return
      }
      previewVisible.value = false
      window.open(payload.url, '_blank')
      return
    }
    const path = payload?.url ?? `/infra/file/${row.id}/download?inline=1`
    if (previewKind.value === 'video') {
      revokePreviewUrl()
      previewObjectUrl.value = appendAccessTokenToApiPath(toApiPath(path), getToken())
      return
    }
    const response = await fetch(`/api${path}`, {
      headers: { Authorization: `Bearer ${getToken() ?? ''}` },
    })
    if (!response.ok) {
      previewVisible.value = false
      ElMessage.error(t('common.requestFailed'))
      return
    }
    revokePreviewUrl()
    const blob = await response.blob()
    previewObjectUrl.value = URL.createObjectURL(blob)
  } catch {
    previewVisible.value = false
    ElMessage.error(t('common.requestFailed'))
  } finally {
    previewLoading.value = false
  }
}

async function handlePreviewDownload() {
  if (!previewFileId.value) return
  try {
    await downloadByApi(`/infra/file/${previewFileId.value}/download`, previewFileName.value)
  } catch (err) {
    ElMessage.error(err instanceof Error ? err.message : t('common.downloadFailed'))
  }
}

function revokePreviewUrl() {
  if (previewObjectUrl.value.startsWith('blob:')) {
    URL.revokeObjectURL(previewObjectUrl.value)
  }
  previewObjectUrl.value = ''
}

const actionHandlers: Record<string, GenRowActionHandler> = {
  'infraFile.preview': async ({ row }) => {
    await handlePreview(row as unknown as FileVO)
  },
  'infraFile.detail': async ({ row }) => {
    openDetail(row as unknown as FileVO)
  },
  'infraFile.download': async ({ row }) => {
    await handleDownload(row as unknown as FileVO)
  },
}

onMounted(async () => {
  const res = await getFileStorageOptions()
  storageOptions.value = res.data.data ?? null
})
</script>
