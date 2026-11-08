<!-- OA 审批只读附件区：分组 + 多类型预览/下载 -->
<template>
  <div v-if="visibleGroups.length" class="oa-attachment-panel">
    <div v-for="group in visibleGroups" :key="group.key" class="oa-attachment-panel__group">
      <div v-if="visibleGroups.length > 1 || group.label" class="oa-attachment-panel__group-title">
        {{ group.label }}
      </div>
      <div class="oa-attachment-panel__list">
        <div v-for="item in group.items" :key="item.id" class="oa-attachment-panel__item">
          <div class="oa-attachment-panel__thumb" @click="canPreview(item) && openPreview(item)">
            <el-image
              v-if="item.kind === 'image' && item.thumbUrl"
              :src="item.thumbUrl"
              fit="cover"
              class="oa-attachment-panel__image"
            />
            <div v-else class="oa-attachment-panel__icon" :data-kind="item.kind">
              {{ kindLabel(item.kind) }}
            </div>
          </div>
          <div class="oa-attachment-panel__meta">
            <div class="oa-attachment-panel__name" :title="item.name">{{ item.name }}</div>
            <div class="oa-attachment-panel__sub">
              <span>{{ item.sizeText }}</span>
              <span v-if="item.ext">· {{ item.ext.toUpperCase() }}</span>
            </div>
          </div>
          <div class="oa-attachment-panel__actions">
            <el-button v-if="canPreview(item)" link type="primary" @click="openPreview(item)">
              {{ gp('preview') }}
            </el-button>
            <el-button link type="primary" @click="downloadFile(item)">
              {{ gp('download') }}
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <FilePreviewViewer
      v-model="previewVisible"
      :title="previewTitle"
      :kind="previewKind"
      :object-url="previewObjectUrl"
      :loading="previewLoading"
      :gp="previewGp"
      @closed="resetPreviewState"
      @download="handlePreviewDownload"
    />
  </div>
  <el-empty v-else :description="gp('emptyAttachments')" :image-size="56" />
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useGenPageShellI18n } from '@/composables/gen/useGenPageShellI18n'
import { ElMessage } from 'element-plus'
import FilePreviewViewer from '@/views/infra/file/FilePreviewViewer.vue'
import { getInfraFile, getInfraFilePreviewUrl } from '@/api/infra/file'
import type { FileVO } from '@/types/infra/file'
import { downloadByApi } from '@/utils/download'
import { appendAccessTokenToApiPath, toApiPath } from '@/utils/infraFileUrl'
import { getToken } from '@/utils/request'
const { gp } = useGenPageShellI18n('oaWidget')

export type OaAttachmentGroupInput = {
  key?: string
  label: string
  fileIds: string[]
}

type FileKind = 'image' | 'pdf' | 'video' | 'office' | 'other'

type FileItem = {
  id: string
  name: string
  ext: string
  kind: FileKind
  sizeText: string
  previewable: boolean
  thumbUrl?: string
}

const props = defineProps<{
  groups: OaAttachmentGroupInput[]
}>()

const loaded = ref<Record<string, FileItem>>({})
const previewVisible = ref(false)
const previewLoading = ref(false)
const previewTitle = ref('')
const previewKind = ref<'image' | 'pdf' | 'video' | ''>('')
const previewObjectUrl = ref('')
const previewFileId = ref<string | null>(null)
const previewFileName = ref('file')

const previewGp = (key: string) => gp(key)

const allIds = computed(() => {
  const ids: string[] = []
  for (const g of props.groups || []) {
    for (const id of g.fileIds || []) {
      if (id && !ids.includes(id)) ids.push(id)
    }
  }
  return ids
})

const visibleGroups = computed(() => {
  return (props.groups || [])
    .map((g, idx) => {
      const items = (g.fileIds || [])
        .map((id) => loaded.value[id])
        .filter((item): item is FileItem => !!item)
      return {
        key: g.key || `g-${idx}`,
        label: g.label,
        items,
      }
    })
    .filter((g) => g.items.length > 0)
})

watch(
  allIds,
  (ids) => {
    void loadFiles(ids)
  },
  { immediate: true },
)

async function loadFiles(ids: string[]) {
  const next: Record<string, FileItem> = { ...loaded.value }
  for (const id of ids) {
    if (next[id]) continue
    try {
      const res = await getInfraFile(id)
      next[id] = toItem(res.data.data)
      if (next[id].kind === 'image') {
        next[id].thumbUrl = await resolveThumbUrl(id)
      }
    } catch {
      next[id] = {
        id,
        name: id,
        ext: '',
        kind: 'other',
        sizeText: '—',
        previewable: false,
      }
    }
  }
  // drop removed
  for (const key of Object.keys(next)) {
    if (!ids.includes(key)) delete next[key]
  }
  loaded.value = next
}

function toItem(file: FileVO): FileItem {
  const name = file.originalName || file.fileName || file.id || 'file'
  const ext = normalizeExt(file.fileExt || extractExt(name))
  const kind = resolveKind(file.contentType, ext)
  return {
    id: file.id || '',
    name,
    ext,
    kind,
    sizeText: file.fileSizeDisplay || formatSize(file.fileSize),
    previewable: kind === 'image' || kind === 'pdf' || kind === 'video',
  }
}

function normalizeExt(raw?: string) {
  if (!raw) return ''
  return raw.replace(/^\./, '').toLowerCase()
}

function extractExt(name: string) {
  const i = name.lastIndexOf('.')
  return i >= 0 ? name.slice(i + 1) : ''
}

function resolveKind(contentType?: string, ext?: string): FileKind {
  const ct = (contentType || '').toLowerCase()
  const e = (ext || '').toLowerCase()
  if (ct.startsWith('image/') || ['png', 'jpg', 'jpeg', 'gif', 'webp', 'bmp', 'svg'].includes(e)) {
    return 'image'
  }
  if (ct === 'application/pdf' || e === 'pdf') return 'pdf'
  if (ct.startsWith('video/') || ['mp4', 'webm', 'ogg', 'ogv', 'mov'].includes(e)) return 'video'
  if (['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'csv', 'txt'].includes(e)) return 'office'
  return 'other'
}

function kindLabel(kind: FileKind) {
  if (kind === 'image') return gp('kindImage')
  if (kind === 'video') return gp('kindVideo')
  if (kind === 'pdf' || kind === 'office') return gp('kindDoc')
  return gp('kindOther')
}

function formatSize(size?: number) {
  if (size == null || !Number.isFinite(size)) return '—'
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}

function canPreview(item: FileItem) {
  return item.previewable
}

async function resolveThumbUrl(id: string) {
  try {
    const res = await getInfraFilePreviewUrl(id)
    const payload = res.data.data
    if (payload?.external && payload.url) return payload.url
    const path = payload?.url ?? `/infra/file/${id}/download?inline=1`
    return appendAccessTokenToApiPath(toApiPath(path), getToken())
  } catch {
    return undefined
  }
}

function clearPreviewObjectUrl() {
  if (previewObjectUrl.value.startsWith('blob:')) {
    URL.revokeObjectURL(previewObjectUrl.value)
  }
  previewObjectUrl.value = ''
}

function resetPreviewState() {
  clearPreviewObjectUrl()
  previewKind.value = ''
  previewFileId.value = null
}

async function openPreview(item: FileItem) {
  if (!canPreview(item)) return
  previewTitle.value = item.name
  previewKind.value = item.kind as 'image' | 'pdf' | 'video'
  previewFileId.value = item.id
  previewFileName.value = item.name
  previewVisible.value = true
  previewLoading.value = true
  try {
    const res = await getInfraFilePreviewUrl(item.id)
    const payload = res.data.data
    if (payload?.external && payload.url) {
      if (item.kind === 'video') {
        clearPreviewObjectUrl()
        previewObjectUrl.value = payload.url
        return
      }
      previewVisible.value = false
      window.open(payload.url, '_blank')
      return
    }
    const path = payload?.url ?? `/infra/file/${item.id}/download?inline=1`
    if (item.kind === 'video') {
      clearPreviewObjectUrl()
      previewObjectUrl.value = appendAccessTokenToApiPath(toApiPath(path), getToken())
      return
    }
    const response = await fetch(`/api${path}`, {
      headers: { Authorization: `Bearer ${getToken() ?? ''}` },
    })
    if (!response.ok) {
      previewVisible.value = false
      ElMessage.error(gp('previewFailed'))
      return
    }
    clearPreviewObjectUrl()
    const blob = await response.blob()
    previewObjectUrl.value = URL.createObjectURL(blob)
  } catch {
    previewVisible.value = false
    ElMessage.error(gp('previewFailed'))
  } finally {
    previewLoading.value = false
  }
}

async function downloadFile(item: FileItem) {
  try {
    await downloadByApi(`/infra/file/${item.id}/download`, item.name)
  } catch (err) {
    ElMessage.error(err instanceof Error ? err.message : gp('downloadFailed'))
  }
}

async function handlePreviewDownload() {
  if (!previewFileId.value) return
  try {
    await downloadByApi(`/infra/file/${previewFileId.value}/download`, previewFileName.value)
  } catch (err) {
    ElMessage.error(err instanceof Error ? err.message : gp('downloadFailed'))
  }
}
</script>

<style scoped>
.oa-attachment-panel__group + .oa-attachment-panel__group {
  margin-top: 16px;
}

.oa-attachment-panel__group-title {
  margin-bottom: 8px;
  font-weight: 500;
  font-size: 13px;
  color: var(--el-text-color-regular);
}

.oa-attachment-panel__list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.oa-attachment-panel__item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 10px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  background: var(--el-fill-color-blank);
}

.oa-attachment-panel__thumb {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  background: var(--el-fill-color-light);
}

.oa-attachment-panel__image {
  width: 48px;
  height: 48px;
}

.oa-attachment-panel__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  font-size: 12px;
  font-weight: 600;
  color: var(--el-color-primary);
}

.oa-attachment-panel__icon[data-kind='pdf'] {
  color: var(--el-color-danger);
}

.oa-attachment-panel__icon[data-kind='office'] {
  color: var(--el-color-success);
}

.oa-attachment-panel__meta {
  flex: 1;
  min-width: 0;
}

.oa-attachment-panel__name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
}

.oa-attachment-panel__sub {
  margin-top: 2px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.oa-attachment-panel__actions {
  flex-shrink: 0;
  display: flex;
  gap: 2px;
}
</style>
