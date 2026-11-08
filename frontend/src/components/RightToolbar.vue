<template>
  <div class="right-toolbar">
    <el-tooltip :content="t('common.refresh')" placement="top">
      <el-button circle :icon="Refresh" @click="emit('refresh')" />
    </el-tooltip>
    <el-tooltip
      :content="showSearch ? t('common.hideSearch') : t('common.showSearch')"
      placement="top"
    >
      <el-button circle :icon="Search" @click="emit('toggleSearch')" />
    </el-tooltip>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { Refresh, Search } from '@element-plus/icons-vue'

defineProps<{ showSearch: boolean }>()
const emit = defineEmits<{ refresh: []; toggleSearch: [] }>()
const { t } = useI18n()
</script>

<style scoped>
.right-toolbar {
  display: flex;
  gap: 8px;
}
</style>
