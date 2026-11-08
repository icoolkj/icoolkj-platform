<!-- 参数配置 — GenRuntimePage + view.extensions；分类 Tab / 刷新缓存走插槽 -->
<template>
  <GenRuntimePage
    ref="pageRef"
    page-key="config"
    :api-override="apiOverride"
  >
    <template #table-top>
      <el-tabs v-model="activeCategory" @tab-change="onCategoryChange">
        <el-tab-pane
          v-for="cat in CONFIG_CATEGORIES"
          :key="cat.id"
          :name="cat.id"
          :label="gp(`category.${cat.id}`)"
        />
      </el-tabs>
    </template>
    <template #toolbar-extra>
      <el-button
        type="warning"
        plain
        :icon="RefreshRight"
        v-hasPermi="['system:config:update']"
        @click="handleRefreshCache"
      >
        {{ gp('refreshCache') }}
      </el-button>
    </template>
  </GenRuntimePage>
</template>

<script setup lang="ts">
import { computed, nextTick, ref } from 'vue'
import { RefreshRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { GenRuntimePage } from '@/components/gen'
import type { GenCrudApi } from '@/types/gen/crudApi'
import { pageConfig, refreshConfigCache } from '@/api/system/config'
import { useGenPageShellI18n } from '@/composables/gen'
import {
  CONFIG_CATEGORIES,
  categoryQueryFilter,
  type ConfigCategoryId,
} from '@/utils/configCategory'

const pageRef = ref<InstanceType<typeof GenRuntimePage>>()
const { gp } = useGenPageShellI18n('config')

const activeCategory = ref<ConfigCategoryId>('all')

const apiOverride = computed<Partial<GenCrudApi>>(() => ({
  page: (params) => pageConfig({ ...params, ...categoryQueryFilter(activeCategory.value) }),
}))

function onCategoryChange() {
  nextTick(() => {
    if (pageRef.value?.queryParams) {
      pageRef.value.queryParams.pageNo = 1
    }
    pageRef.value?.loadPage?.()
  })
}

async function handleRefreshCache() {
  await refreshConfigCache()
  ElMessage.success(gp('refreshCacheSuccess'))
}
</script>

