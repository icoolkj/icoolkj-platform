<template>
  <div v-if="tagsViewStore.visitedViews.length" class="tags-view">
    <el-scrollbar class="tags-scroll">
      <div class="tags-inner">
        <router-link
          v-for="tag in tagsViewStore.visitedViews"
          :key="tag.path"
          :to="tag.fullPath"
          class="tags-item"
          :class="{ active: isActive(tag.path) }"
        >
          <span class="tags-title">{{ resolveTagTitle(tag.title) }}</span>
          <el-icon
            v-if="!tag.affix && closableCount > 0"
            class="tags-close"
            @click.prevent.stop="closeTag(tag)"
          >
            <Close />
          </el-icon>
        </router-link>
      </div>
    </el-scrollbar>

    <el-dropdown
      trigger="click"
      placement="bottom-end"
      popper-class="tags-view-dropdown"
      @command="handleBatch"
    >
      <button
        type="button"
        class="tags-menu-btn"
        :title="t('layout.tags.more')"
        :aria-label="t('layout.tags.more')"
      >
        <el-icon><ArrowDown /></el-icon>
      </button>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item command="others" :disabled="!canCloseOthers">
            {{ t('layout.tags.closeOthers') }}
          </el-dropdown-item>
          <el-dropdown-item command="all" :disabled="!canCloseAll">
            {{ t('layout.tags.closeAll') }}
          </el-dropdown-item>
          <el-dropdown-item divided disabled>
            {{ t('layout.tags.maxHint', { max: tagsViewStore.maxCount }) }}
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown, Close } from '@element-plus/icons-vue'
import { useTagsViewStore } from '@/stores/tagsView'
import type { TagView } from '@/types/layout'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const tagsViewStore = useTagsViewStore()

const closableCount = computed(
  () => tagsViewStore.visitedViews.filter((item) => !item.affix).length,
)

const canCloseOthers = computed(() => {
  const current = route.path
  return tagsViewStore.visitedViews.some((item) => !item.affix && item.path !== current)
})

const canCloseAll = computed(() => closableCount.value > 0)

function isActive(path: string) {
  return route.path === path
}

function resolveTagTitle(title: string) {
  if (title.includes('.')) {
    const translated = t(title)
    return translated !== title ? translated : title
  }
  return title
}

async function navigateAfterClose() {
  const views = tagsViewStore.visitedViews
  const currentStillOpen = views.some((item) => item.path === route.path)
  if (currentStillOpen) {
    return
  }
  const last = views[views.length - 1]
  if (last) {
    await router.push(last.fullPath)
    return
  }
  await router.push('/')
}

async function closeTag(tag: TagView) {
  tagsViewStore.removeView(tag.path)
  await navigateAfterClose()
}

async function handleBatch(command: string) {
  if (command === 'others') {
    tagsViewStore.closeOthers(route.path)
  } else if (command === 'all') {
    tagsViewStore.closeAll()
  }
  await navigateAfterClose()
}
</script>

<style scoped>
.tags-view {
  display: flex;
  align-items: flex-start;
  gap: 4px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  padding: 6px 8px 0 12px;
}

.tags-scroll {
  flex: 1;
  min-width: 0;
}

.tags-inner {
  display: flex;
  align-items: center;
  gap: 6px;
  padding-bottom: 6px;
}

.tags-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: 28px;
  padding: 0 10px;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  background: #fafafa;
  color: #595959;
  font-size: 12px;
  text-decoration: none;
  transition: all 0.2s;
  flex-shrink: 0;
}

.tags-item:hover {
  color: var(--ic-primary, var(--el-color-primary));
  border-color: var(--el-color-primary-light-7);
}

.tags-item.active {
  color: var(--ic-primary, var(--el-color-primary));
  background: var(--ic-primary-light, var(--el-color-primary-light-9));
  border-color: var(--el-color-primary-light-7);
}

.tags-title {
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tags-close {
  font-size: 12px;
  border-radius: 50%;
}

.tags-close:hover {
  color: #ff4d4f;
  background: rgba(0, 0, 0, 0.04);
}

.tags-menu-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  margin-bottom: 6px;
  padding: 0;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  background: #fafafa;
  color: #595959;
  cursor: pointer;
  flex-shrink: 0;
  transition: all 0.2s;
}

.tags-menu-btn:hover {
  color: var(--ic-primary, var(--el-color-primary));
  border-color: var(--el-color-primary-light-7);
}
</style>

<style>
.tags-view-dropdown.el-popper {
  z-index: 3000 !important;
}
</style>
