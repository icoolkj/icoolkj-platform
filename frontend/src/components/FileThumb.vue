<!-- 统一文件类型图标（非真实缩略图） -->
<template>
  <div class="file-thumb" :class="`is-${kind}`">
    <el-icon v-if="kind === 'image'" class="file-thumb__icon image"><Picture /></el-icon>
    <el-icon v-else-if="kind === 'pdf'" class="file-thumb__icon pdf"><Document /></el-icon>
    <el-icon v-else-if="kind === 'video'" class="file-thumb__icon video"><VideoPlay /></el-icon>
    <el-icon v-else class="file-thumb__icon"><Folder /></el-icon>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Document, Folder, Picture, VideoPlay } from '@element-plus/icons-vue'
import { resolveFileThumbKind } from '@/utils/fileThumb'
import type { FileVO } from '@/types/infra/file'

const props = defineProps<{
  file: Pick<FileVO, 'fileExt' | 'contentType'>
}>()

const kind = computed(() =>
  resolveFileThumbKind({
    fileExt: props.file.fileExt,
    contentType: props.file.contentType,
  }),
)
</script>

<style scoped>
.file-thumb {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
}

.file-thumb__icon {
  font-size: 28px;
  color: var(--el-text-color-secondary);
}

.file-thumb__icon.image {
  color: #409eff;
}

.file-thumb__icon.pdf {
  color: #e74c3c;
}

.file-thumb__icon.video {
  color: #9b59b6;
}
</style>
