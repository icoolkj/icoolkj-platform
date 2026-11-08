<template>
  <el-form ref="formRef" :model="info" label-width="120px">
    <el-row :gutter="16">
      <el-col :span="12">
        <el-form-item :label="gp('backendModule')">
          <el-select v-model="info.backendModule">
            <el-option label="system" value="system" />
            <el-option label="infra" value="infra" />
            <el-option label="biz" value="biz" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item :label="gp('apiModule')">
          <el-select v-model="info.apiModule">
            <el-option label="system" value="system" />
            <el-option label="tenant" value="tenant" />
            <el-option label="infra" value="infra" />
            <el-option label="biz" value="biz" />
            <el-option label="tool" value="tool" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item :label="gp('baseType')">
          <el-select v-model="info.baseType">
            <el-option label="tenant (TenantBaseEntity)" value="tenant" />
            <el-option label="platform (BaseEntity+tenantId)" value="platform" />
            <el-option label="base (BaseEntity)" value="base" />
            <el-option label="plain (@Data)" value="plain" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item :label="gp('businessName')">
          <el-input
            v-model="info.businessName"
            :placeholder="gp('businessNamePlaceholder')"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item :label="gp('functionName')">
          <el-input v-model="info.functionName" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item :label="gp('routePrefix')">
          <el-input v-model="info.routePrefix" placeholder="/system" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item :label="gp('permissionPrefix')">
          <el-input v-model="info.permissionPrefix" placeholder="system:user" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item :label="gp('menuProductCode')">
          <el-select v-model="menuOptions.productCode" clearable :placeholder="PLATFORM_PRODUCT_CODE">
            <el-option
              v-for="item in productOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item :label="gp('menuAssignScope')">
          <el-select v-model="menuOptions.assignScope">
            <el-option :label="gp('menuAssignScopeTenant')" value="TENANT" />
            <el-option :label="gp('menuAssignScopePlatform')" value="PLATFORM" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="24">
        <el-form-item :label="gp('menuParentMenuId')">
          <el-input
            v-model="menuOptions.parentMenuId"
            :placeholder="gp('menuParentMenuIdPlaceholder')"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item :label="gp('tplCategory')">
          <el-select v-model="info.tplCategory">
            <el-option :label="gp('tplCrud')" value="crud" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item :label="gp('tplWebType')">
          <el-select v-model="info.tplWebType">
            <el-option :label="gp('tplElementTs')" value="element-plus-ts" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item :label="gp('genType')">
          <el-radio-group v-model="info.genType">
            <el-radio :value="GEN_ZIP">{{ gp('genZip') }}</el-radio>
            <el-radio :value="GEN_PATH">{{ gp('genCustom') }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-col>
      <el-col v-if="isGenCustomPath(info.genType)" :span="24">
        <el-form-item :label="gp('genPath')">
          <el-input v-model="info.genPath" :placeholder="gp('genPathPlaceholder')" />
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>

<script setup lang="ts">
import { onMounted, reactive, watch } from 'vue'
import { useToolGenI18n } from '@/composables/useToolGenI18n'
import { GEN_PATH, GEN_ZIP, isGenCustomPath } from '@/constants/flags'
import { listProducts } from '@/api/system/product'
import type { DictOption } from '@/types/system/dict'
import type { GenTable } from '@/types/tool/gen'
import { mergeGenTableOptions, parseGenTableOptions } from '@/utils/genTableOptions'
import { PLATFORM_PRODUCT_CODE } from '@/constants/product'

const { gp } = useToolGenI18n()
const info = defineModel<GenTable>({ required: true })

const productOptions = reactive<DictOption[]>([])
const menuOptions = reactive({
  productCode: PLATFORM_PRODUCT_CODE,
  assignScope: 'TENANT' as 'TENANT' | 'PLATFORM',
  parentMenuId: '',
})

function syncFromOptions() {
  const parsed = parseGenTableOptions(info.value.options)
  menuOptions.productCode = parsed.productCode || PLATFORM_PRODUCT_CODE
  menuOptions.assignScope = parsed.assignScope || 'TENANT'
  menuOptions.parentMenuId = parsed.parentMenuId || ''
}

function syncToOptions() {
  info.value.options = mergeGenTableOptions(info.value.options, {
    productCode: menuOptions.productCode,
    assignScope: menuOptions.assignScope,
    parentMenuId: menuOptions.parentMenuId || undefined,
  })
}

watch(
  () => info.value.options,
  () => syncFromOptions(),
  { immediate: true },
)

watch(menuOptions, () => syncToOptions(), { deep: true })

onMounted(async () => {
  try {
    const res = await listProducts()
    const list = (res.data.data ?? []).map((p) => ({
      label: p.name || p.code,
      value: p.code,
    }))
    productOptions.splice(0, productOptions.length, ...list)
  } catch {
    productOptions.splice(0, productOptions.length, {
      label: PLATFORM_PRODUCT_CODE,
      value: PLATFORM_PRODUCT_CODE,
    })
  }
})
</script>
