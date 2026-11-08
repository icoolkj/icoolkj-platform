<template>
  <el-form :model="queryParams" inline @submit.prevent>
    <el-form-item
      v-for="field in queryFields"
      :key="field.field"
      :label="gf(field.field, 'label')"
    >
      <GenFormField
        v-model="queryParams[field.field]"
        :config="field"
        mode="query"
        :placeholder="gf(field.field, 'placeholder')"
        :get-dict="getDict"
        :tree-data="resolveTreeData(field)"
        :tree-props="resolveTreeProps(field)"
        @enter="handleQuery"
      />
    </el-form-item>
    <el-form-item>
      <el-button type="primary" :icon="Search" @click="handleQuery">
        {{ t('common.search') }}
      </el-button>
      <el-button :icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Refresh, Search } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import GenFormField from '@/components/gen/fields/GenFormField.vue'
import { resolveGenTreeData, resolveGenTreeProps } from '@/composables/gen/genCrudConstants'
import { useGenCrudContext } from '@/composables/gen/useGenCrudContext'
import type { GenViewQueryField } from '@/types/tool/genView'

const { t } = useI18n()
const { list, gf, getDict, treeSources } = useGenCrudContext()

const queryParams = list.queryParams
const queryFields = list.queryFields
const handleQuery = list.handleQuery
const resetQuery = list.resetQuery
const treeSourcesMap = computed(() => treeSources.value)

function resolveTreeData(field: GenViewQueryField) {
  return resolveGenTreeData(field, treeSourcesMap.value)
}

function resolveTreeProps(field: GenViewQueryField) {
  return resolveGenTreeProps(field)
}
</script>
