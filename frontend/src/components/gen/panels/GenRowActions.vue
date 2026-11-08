<template>
  <template v-if="ext.declaredRowActions.value.length">
    <template v-for="action in ext.declaredRowActions.value" :key="action.id">
      <el-button
        v-if="ext.actionVisible(action, row) && !ext.actionPerm(action).length"
        link
        :type="action.type ?? (action.builtin === 'delete' ? 'danger' : 'primary')"
        :disabled="ext.actionDisabled(action, row)"
        @click="ext.runRowAction(action, row)"
      >
        {{ ext.actionLabel(action) }}
      </el-button>
      <el-button
        v-else-if="ext.actionVisible(action, row)"
        link
        :type="action.type ?? (action.builtin === 'delete' ? 'danger' : 'primary')"
        :disabled="ext.actionDisabled(action, row)"
        v-hasPermi="ext.actionPerm(action)"
        @click="ext.runRowAction(action, row)"
      >
        {{ ext.actionLabel(action) }}
      </el-button>
    </template>
  </template>
  <template v-else-if="!hasRowActionsSlot">
    <el-button
      v-if="ext.tableActions.value.edit"
      link
      type="primary"
      v-hasPermi="[perm.update!]"
      @click="form.openForm(row)"
    >
      {{ t('common.edit') }}
    </el-button>
    <el-button
      v-if="ext.tableActions.value.delete"
      link
      type="danger"
      v-hasPermi="[perm.delete!]"
      @click="form.handleDelete(row)"
    >
      {{ t('common.delete') }}
    </el-button>
  </template>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useGenCrudContext } from '@/composables/gen/useGenCrudContext'

defineProps<{
  row: Record<string, unknown>
}>()

const { t } = useI18n()
const { ext, form, perm, hasRowActionsSlot } = useGenCrudContext()
</script>
