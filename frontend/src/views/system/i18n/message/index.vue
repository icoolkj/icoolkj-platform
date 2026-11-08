<!-- AUTO-GENERATED 国际化文案 — GenRuntimePage + view.json -->
<template>
  <GenRuntimePage page-key="i18nMessage">
    <template #toolbar-extra>
      <el-button
        type="warning"
        plain
        :icon="RefreshRight"
        v-hasPermi="['system:i18nMessage:update']"
        @click="handleRefreshCache"
      >
        {{ gp('refreshCache') }}
      </el-button>
    </template>
  </GenRuntimePage>
</template>

<script setup lang="ts">
import { RefreshRight } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { GenRuntimePage } from '@/components/gen'
import { refreshI18nCache } from '@/api/system/i18nMessage'
import { useGenPageShellI18n } from '@/composables/gen'

const { t } = useI18n()
const { gp } = useGenPageShellI18n('i18nMessage')

async function handleRefreshCache() {
  await ElMessageBox.confirm(gp('refreshCacheConfirm'), t('common.tip'))
  await refreshI18nCache()
  ElMessage.success(gp('refreshCacheSuccess'))
}
</script>

