<!-- P09 菜单 — GenListLayout + 产品 Tab + 内嵌 GenRuntimePage（tree-crud） -->
<template>
  <div class="gen-page-shell menu-admin-page">
    <GenListLayout
      :title="gp('title')"
      :description="gp('description')"
      :show-search="false"
      :show-right-toolbar="false"
    >
      <template #table-top>
        <el-tabs v-model="activeProduct" class="menu-product-tabs">
          <el-tab-pane
            v-for="p in productOptions"
            :key="p.value"
            :label="p.label"
            :name="String(p.value)"
          />
        </el-tabs>
      </template>

      <GenRuntimePage
        ref="crudRef"
        hide-header
        page-key="menu"
        :fixed-query="fixedQuery"
        :omit-query-fields="['productCode']"
        :omit-table-fields="['productCode']"
        :tree-row-filter="treeRowFilter"
        :drop-orphan-rows="true"
        :clear-query-on-fixed-change="true"
        :get-add-child-defaults="getMenuAddChildDefaults"
        :create-defaults="menuCreateDefaults"
        :get-field-options="getFieldOptions"
      >
        <template #toolbar-extra>
          <el-checkbox v-model="showButtons" @change="onToggleButtons">
            {{ gp('showButtons') }}
          </el-checkbox>
        </template>
      </GenRuntimePage>
    </GenListLayout>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { GenRuntimePage, GenListLayout } from '@/components/gen'
import { useGenPageShellI18n } from '@/composables/gen'
import type { DictOption } from '@/types/system/dict'
import { listProducts } from '@/api/system/product'
import { STATUS_NORMAL } from '@/constants/flags'
import { PLATFORM_PRODUCT_CODE } from '@/constants/product'

const { gp } = useGenPageShellI18n('menu')

const productOptions = ref<DictOption[]>([])
const activeProduct = ref(PLATFORM_PRODUCT_CODE)
const showButtons = ref(false)
const crudRef = ref<InstanceType<typeof GenRuntimePage>>()

const fixedQuery = computed(() => ({ productCode: activeProduct.value }))

const menuCreateDefaults = computed(() => ({
  menuType: 'M',
  sortNo: 0,
  status: STATUS_NORMAL,
  assignScope: 'TENANT',
  productCode: activeProduct.value,
}))

function getFieldOptions(field: string): DictOption[] | undefined {
  if (field === 'productCode') return productOptions.value
  return undefined
}

onMounted(async () => {
  try {
    const res = await listProducts()
    const list = (res.data.data ?? []).map((p) => ({
      label: p.name || p.code,
      value: p.code,
    }))
    productOptions.value = list
    if (list.length && !list.some((p) => p.value === activeProduct.value)) {
      activeProduct.value = String(
        list.find((p) => p.value === PLATFORM_PRODUCT_CODE)?.value ?? list[0].value,
      )
    }
  } catch {
    productOptions.value = [{ label: '平台', value: PLATFORM_PRODUCT_CODE }]
  }
})

function treeRowFilter(row: Record<string, unknown>) {
  if (showButtons.value) return true
  return row.menuType !== 'F'
}

function onToggleButtons() {
  crudRef.value?.rebuildTree()
}

function getMenuAddChildDefaults(parent: Record<string, unknown>) {
  const productCode =
    typeof parent.productCode === 'string' ? parent.productCode : activeProduct.value
  if (!parent.id) return { menuType: 'M', productCode }
  if (parent.menuType === 'M') return { menuType: 'C', productCode }
  return { menuType: 'F', productCode }
}
</script>

<style scoped>
.menu-admin-page :deep(.page-container) {
  padding: 0;
}

.menu-product-tabs :deep(.el-tabs__header) {
  margin: 0 0 12px;
}

.menu-product-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
}
</style>
