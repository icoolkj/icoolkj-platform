<template>
  <div
    v-if="showMain || $slots.extra"
    class="page-header"
    :class="{ 'page-header--actions-only': !showMain }"
  >
    <div v-if="showMain" class="page-header__main">
      <h2 class="page-header__title">{{ title }}</h2>
      <p v-if="description" class="page-header__desc">{{ description }}</p>
      <p v-if="meta" class="page-header__meta">{{ meta }}</p>
    </div>
    <div v-if="$slots.extra" class="page-header__extra">
      <slot name="extra" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useLayoutStore } from '@/stores/layout'

const props = defineProps<{
  title: string
  description?: string
  /** 第三行说明（如当前审批节点），纯文本不可聚焦 */
  meta?: string
  /** 忽略布局「隐藏页头」偏好，审批等详情页强制展示标题 */
  force?: boolean
}>()

const layoutStore = useLayoutStore()
const { pageHeader } = storeToRefs(layoutStore)
const showMain = computed(() => props.force || pageHeader.value)
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 16px;
  gap: 16px;
}

.page-header--actions-only {
  justify-content: flex-end;
  margin-bottom: 12px;
}
.page-header__title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  line-height: 1.4;
  color: #1f1f1f;
}
.page-header__desc {
  margin: 4px 0 0;
  font-size: 14px;
  color: var(--ic-text-secondary);
}

.page-header__meta {
  margin: 6px 0 0;
  font-size: 13px;
  line-height: 1.4;
  color: var(--el-color-primary);
}
</style>
