<!--
  Gen 列表页布局壳：统一 PageHeader + search-card + table-card 分区。
  封闭 slot（勿再往 GenCrudPage 加布局插槽，先扩本组件）：
  - header-extra  页头右侧
  - search        搜索区（整块）
  - table-top     表格卡内、工具栏上方（如分类 Tab）
  - toolbar       工具栏左侧主操作（CRUD 默认「新增」写在此）
  - toolbar-extra 工具栏左侧附加按钮
  - toolbar-right 工具栏右侧（默认 RightToolbar）
  - default       表格主体（plain 模式下为整页自由内容）
  - footer        分页等

  plain=true：仅 PageHeader + default（工作台 / 个人中心 / 全屏对话等，无 search/table-card）
-->
<template>
  <div class="page-container" v-bind="$attrs">
    <PageHeader v-if="!hideHeader" :title="title" :description="description">
      <template v-if="$slots['header-extra']" #extra>
        <slot name="header-extra" />
      </template>
    </PageHeader>

    <template v-if="plain">
      <slot />
    </template>

    <template v-else>
      <el-card v-if="$slots.search && showSearch" shadow="never" class="search-card">
        <slot name="search" />
      </el-card>

      <el-card shadow="never" class="table-card">
        <div v-if="$slots['table-top']" class="gen-list-table-top">
          <slot name="table-top" />
        </div>

        <div
          v-if="$slots.toolbar || $slots['toolbar-extra'] || showRightToolbar || $slots['toolbar-right']"
          class="toolbar-row"
        >
          <div class="toolbar-left">
            <slot name="toolbar" />
            <slot name="toolbar-extra" />
          </div>
          <slot name="toolbar-right">
            <RightToolbar
              v-if="showRightToolbar"
              :show-search="showSearch"
              @refresh="emit('refresh')"
              @toggle-search="emit('toggle-search')"
            />
          </slot>
        </div>

        <slot />

        <div v-if="$slots.footer" class="table-footer">
          <slot name="footer" />
        </div>
      </el-card>
    </template>
  </div>
</template>

<script setup lang="ts">
import PageHeader from '@/components/PageHeader.vue'
import RightToolbar from '@/components/RightToolbar.vue'

defineOptions({ inheritAttrs: false })

withDefaults(
  defineProps<{
    title: string
    description?: string
    hideHeader?: boolean
    showSearch?: boolean
    /** 是否渲染默认 RightToolbar（自定义 #toolbar-right 时忽略） */
    showRightToolbar?: boolean
    /** 自由布局：仅页头 + 内容，无 search/table-card（工作台、个人中心等） */
    plain?: boolean
  }>(),
  {
    hideHeader: false,
    showSearch: true,
    showRightToolbar: true,
    plain: false,
  },
)

const emit = defineEmits<{
  refresh: []
  'toggle-search': []
}>()
</script>

<style scoped>
.gen-list-table-top {
  margin-bottom: 8px;
}

.gen-list-table-top :deep(.el-tabs__header) {
  margin-bottom: 0;
}
</style>
