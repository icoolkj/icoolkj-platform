<template>
  <el-form ref="formRef" :model="info" label-width="120px">
    <el-row :gutter="16">
      <el-col :span="12">
        <el-form-item :label="gp('tableName')">
          <el-input v-model="info.tableName" disabled />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item :label="gp('tableComment')">
          <el-input v-model="info.tableComment" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item :label="gp('entityClassName')">
          <el-input v-model="info.className" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item :label="gp('author')">
          <el-input v-model="info.functionAuthor" />
        </el-form-item>
      </el-col>
      <el-col :span="24">
        <el-form-item :label="t('common.remark')">
          <el-input v-model="info.remark" type="textarea" />
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useToolGenI18n } from '@/composables/useToolGenI18n'
import type { GenTable } from '@/types/tool/gen'

const { t } = useI18n()
const { gp } = useToolGenI18n()
const info = defineModel<GenTable>({ required: true })
</script>
