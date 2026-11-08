<template>
  <div class="gen-page-shell">
    <GenListLayout
      :title="gp('editTitle', { name: info.tableName || t('layout.editGenConfig') })"
      :description="gp('editDescription')"
      :show-search="false"
      :show-right-toolbar="false"
    >
      <template #header-extra>
        <el-button @click="goBack">{{ t('common.back') }}</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">
          {{ t('common.save') }}
        </el-button>
      </template>

      <el-tabs v-model="activeTab" class="edit-tabs">
        <el-tab-pane :label="gp('tabBasic')" name="basic">
          <BasicInfoForm v-model="info" />
        </el-tab-pane>
        <el-tab-pane :label="gp('tabColumns')" name="columns">
          <el-table :data="columns" row-key="id" max-height="560" stripe border>
            <el-table-column type="index" width="50" />
            <el-table-column
              :label="gp('columnName')"
              prop="columnName"
              width="140"
              show-overflow-tooltip
            />
            <el-table-column :label="gp('columnComment')" min-width="120">
              <template #default="{ row }">
                <el-input v-model="row.columnComment" />
              </template>
            </el-table-column>
            <el-table-column
              :label="gp('columnType')"
              prop="columnType"
              width="120"
              show-overflow-tooltip
            />
            <el-table-column :label="gp('javaType')" width="130">
              <template #default="{ row }">
                <el-select v-model="row.javaType">
                  <el-option label="String" value="String" />
                  <el-option label="Integer" value="Integer" />
                  <el-option label="Long" value="Long" />
                  <el-option label="LocalDateTime" value="LocalDateTime" />
                  <el-option label="Boolean" value="Boolean" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column :label="gp('javaField')" width="120">
              <template #default="{ row }">
                <el-input v-model="row.javaField" />
              </template>
            </el-table-column>
            <el-table-column :label="gp('isInsert')" width="70" align="center">
              <template #default="{ row }">
                <el-checkbox v-model="row.isInsert" :true-value="YES" :false-value="NO" />
              </template>
            </el-table-column>
            <el-table-column :label="gp('isEdit')" width="70" align="center">
              <template #default="{ row }">
                <el-checkbox v-model="row.isEdit" :true-value="YES" :false-value="NO" />
              </template>
            </el-table-column>
            <el-table-column :label="gp('isList')" width="70" align="center">
              <template #default="{ row }">
                <el-checkbox v-model="row.isList" :true-value="YES" :false-value="NO" />
              </template>
            </el-table-column>
            <el-table-column :label="gp('isExport')" width="70" align="center">
              <template #default="{ row }">
                <el-checkbox v-model="row.isExport" :true-value="YES" :false-value="NO" />
              </template>
            </el-table-column>
            <el-table-column :label="gp('isImport')" width="70" align="center">
              <template #default="{ row }">
                <el-checkbox v-model="row.isImport" :true-value="YES" :false-value="NO" />
              </template>
            </el-table-column>
            <el-table-column :label="gp('isQuery')" width="70" align="center">
              <template #default="{ row }">
                <el-checkbox v-model="row.isQuery" :true-value="YES" :false-value="NO" />
              </template>
            </el-table-column>
            <el-table-column :label="gp('queryType')" width="120">
              <template #default="{ row }">
                <el-select v-model="row.queryType">
                  <el-option label="=" value="EQ" />
                  <el-option label="LIKE" value="LIKE" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column :label="gp('htmlType')" width="130">
              <template #default="{ row }">
                <el-select v-model="row.htmlType">
                  <el-option :label="gp('htmlInput')" value="input" />
                  <el-option :label="gp('htmlTextarea')" value="textarea" />
                  <el-option :label="gp('htmlSelect')" value="select" />
                  <el-option :label="gp('htmlRadio')" value="radio" />
                  <el-option :label="gp('htmlTree')" value="tree" />
                  <el-option :label="gp('htmlDatetime')" value="datetime" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column :label="gp('dictType')" min-width="180">
              <template #default="{ row }">
                <el-select
                  v-model="row.dictType"
                  filterable
                  clearable
                  allow-create
                  default-first-option
                  :disabled="row.htmlType !== 'select' && row.htmlType !== 'radio'"
                  :placeholder="gp('dictTypePlaceholder')"
                  style="width: 100%"
                >
                  <el-option
                    v-for="dt in dictTypeOptions"
                    :key="dt.dictType"
                    :label="`${dt.dictName} (${dt.dictType})`"
                    :value="dt.dictType"
                  />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column :label="gp('isRequired')" width="70" align="center">
              <template #default="{ row }">
                <el-checkbox v-model="row.isRequired" :true-value="YES" :false-value="NO" />
              </template>
            </el-table-column>
            <el-table-column :label="gp('columnLength')" width="90">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.columnLength"
                  :min="0"
                  :max="65535"
                  controls-position="right"
                />
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane :label="gp('tabI18n')" name="i18n">
          <GenI18nForm
            v-if="info.id"
            :table-id="info.id"
            :business-name="info.businessName ?? ''"
            :api-module="info.apiModule ?? ''"
            :columns="columns"
          />
        </el-tab-pane>
        <el-tab-pane :label="gp('tabView')" name="view">
          <GenViewForm
            v-if="info.id"
            :table-id="info.id"
            :business-name="info.businessName ?? ''"
            :api-module="info.apiModule ?? ''"
          />
        </el-tab-pane>
        <el-tab-pane :label="gp('tabGen')" name="gen">
          <GenInfoForm v-model="info" />
        </el-tab-pane>
      </el-tabs>
    </GenListLayout>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useToolGenI18n } from '@/composables/useToolGenI18n'
import { ElMessage } from 'element-plus'
import GenListLayout from '@/components/gen/layout/GenListLayout.vue'
import { NO, YES } from '@/constants/flags'
import BasicInfoForm from './BasicInfoForm.vue'
import GenInfoForm from './GenInfoForm.vue'
import GenI18nForm from './GenI18nForm.vue'
import GenViewForm from './GenViewForm.vue'
import { getGenTable, updateGenTable } from '@/api/tool/gen'
import { listDictTypeOptions } from '@/api/system/dict'
import type { GenTable, GenTableColumn } from '@/types/tool/gen'
import type { SysDictType } from '@/types/system/dict'

const { t } = useI18n()
const { gp } = useToolGenI18n()
const route = useRoute()
const router = useRouter()
const activeTab = ref('basic')
const submitting = ref(false)
const info = ref<GenTable>({ tableName: '' })
const columns = ref<GenTableColumn[]>([])
const dictTypeOptions = ref<SysDictType[]>([])

async function loadDictTypes() {
  const res = await listDictTypeOptions()
  dictTypeOptions.value = res.data.data ?? []
}

async function loadDetail() {
  const id = route.params.id as string
  const res = await getGenTable(id)
  info.value = res.data.data
  columns.value = res.data.data.columns ?? []
}

async function submit() {
  submitting.value = true
  try {
    await updateGenTable({ ...info.value, columns: columns.value })
    ElMessage.success(gp('saveSuccess'))
    goBack()
  } finally {
    submitting.value = false
  }
}

function goBack() {
  router.push('/tool/gen')
}

onMounted(async () => {
  await Promise.all([loadDetail(), loadDictTypes()])
})
</script>

<style scoped>
.edit-tabs :deep(.el-tabs__content) {
  padding-top: 8px;
}
</style>
