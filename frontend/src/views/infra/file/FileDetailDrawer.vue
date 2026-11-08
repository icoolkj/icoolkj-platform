<template>
  <GenFormShell v-model="visible" :title="gp('detailTitle')" :width="860">
    <el-descriptions v-if="detail" :column="1" border>
      <el-descriptions-item :label="gf('originalName', 'label')">
        {{ detail.originalName }}
      </el-descriptions-item>
      <el-descriptions-item :label="gf('fileExt', 'label')">
        {{ detail.fileExt }}
      </el-descriptions-item>
      <el-descriptions-item :label="gf('fileSize', 'label')">
        {{ detail.fileSizeDisplay }}
      </el-descriptions-item>
      <el-descriptions-item :label="gf('storageType', 'label')">
        <DictTag :options="storageDict" :value="detail.storageType" />
      </el-descriptions-item>
      <el-descriptions-item :label="gf('bizType', 'label')">
        {{ detail.bizType || '-' }}
      </el-descriptions-item>
      <el-descriptions-item :label="gf('bizId', 'label')">
        {{ detail.bizId || '-' }}
      </el-descriptions-item>
      <el-descriptions-item :label="gf('userId', 'label')">
        {{ detail.userId || '-' }}
      </el-descriptions-item>
      <el-descriptions-item :label="gf('createdAt', 'label')">
        {{ detail.createdAt || '-' }}
      </el-descriptions-item>
      <el-descriptions-item :label="gf('contentType', 'label')">
        {{ detail.contentType || '-' }}
      </el-descriptions-item>
      <el-descriptions-item :label="gf('fileName', 'label')">
        {{ detail.fileName || '-' }}
      </el-descriptions-item>
      <el-descriptions-item :label="gf('bucketName', 'label')">
        {{ detail.bucketName || '-' }}
      </el-descriptions-item>
      <el-descriptions-item :label="gf('objectKey', 'label')">
        {{ detail.objectKey || '-' }}
      </el-descriptions-item>
      <el-descriptions-item :label="gf('md5', 'label')">
        {{ detail.md5 || '-' }}
      </el-descriptions-item>
      <el-descriptions-item :label="gf('tenantId', 'label')">
        {{ detail.tenantId || '-' }}
      </el-descriptions-item>
      <el-descriptions-item :label="gp('referenceCount')">
        {{ detail.referenceCount ?? 0 }}
      </el-descriptions-item>
      <el-descriptions-item :label="gf('remark', 'label')">
        {{ detail.remark || '-' }}
      </el-descriptions-item>
    </el-descriptions>

    <div v-if="refs.length" class="refs-block">
      <div class="refs-title">{{ gp('refsTitle') }}</div>
      <el-table :data="refs" size="small" border stripe>
        <el-table-column prop="bizType" :label="gf('bizType', 'label')" min-width="100" />
        <el-table-column prop="bizId" :label="gf('bizId', 'label')" min-width="120" />
        <el-table-column prop="refField" :label="gp('refField')" min-width="100" />
      </el-table>
    </div>
  </GenFormShell>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import DictTag from '@/components/DictTag.vue'
import GenFormShell from '@/components/gen/layout/GenFormShell.vue'
import { getInfraFile, listInfraFileRefs } from '@/api/infra/file'
import type { DictOption } from '@/types/system/dict'
import type { FileRefVO, FileVO } from '@/types/infra/file'

const props = defineProps<{
  modelValue: boolean
  fileId: string | null
  storageDict: DictOption[]
  gp: (key: string, params?: Record<string, string | number>) => string
  gf: (field: string, prop?: 'label' | 'placeholder') => string
}>()

const emit = defineEmits<{ 'update:modelValue': [value: boolean] }>()

const visible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value),
})

const detail = ref<FileVO | null>(null)
const refs = ref<FileRefVO[]>([])

watch(
  () => [props.modelValue, props.fileId] as const,
  async ([open, id]) => {
    if (!open || !id) {
      detail.value = null
      refs.value = []
      return
    }
    const [detailRes, refsRes] = await Promise.all([getInfraFile(id), listInfraFileRefs(id)])
    detail.value = detailRes.data.data ?? null
    refs.value = refsRes.data.data ?? []
  },
)
</script>

<style scoped>
.refs-block {
  margin-top: 20px;
}
.refs-title {
  margin-bottom: 8px;
  font-weight: 600;
}
</style>
