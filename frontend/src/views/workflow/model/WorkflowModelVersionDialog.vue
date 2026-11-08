<template>
  <GenFormShell v-model="visibleModel" :title="gp('versionTitle')" :width="760">
    <el-table v-if="versions.length" :data="versions" border stripe>
      <el-table-column
        prop="versionNo"
        :label="gf('versionNo', 'label')"
        width="80"
        align="center"
      />
      <el-table-column prop="published" :label="gf('published', 'label')" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.published ? 'success' : 'info'">
            {{ row.published ? gp('publishedYes') : gp('publishedNo') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        prop="deploymentId"
        :label="gf('deploymentId', 'label')"
        min-width="180"
        show-overflow-tooltip
      />
      <el-table-column
        prop="remark"
        :label="gf('versionRemark', 'label')"
        min-width="120"
        show-overflow-tooltip
      />
    </el-table>
    <el-empty v-else :description="gp('emptyVersion')" />
  </GenFormShell>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useGenPageShellI18n } from '@/composables/gen/useGenPageShellI18n'
import GenFormShell from '@/components/gen/layout/GenFormShell.vue'
import { listWorkflowModelVersions } from '@/api/workflow/model'
import type { WorkflowModelVersionVO } from '@/types/workflow/model'

const props = defineProps<{
  modelId?: string
}>()

const visibleModel = defineModel<boolean>({ default: false })

const { gp, gf } = useGenPageShellI18n('workflowModel')

const versions = ref<WorkflowModelVersionVO[]>([])

watch(visibleModel, async (open) => {
  if (!open || !props.modelId) return
  const res = await listWorkflowModelVersions(props.modelId)
  versions.value = res.data.data ?? []
})
</script>
