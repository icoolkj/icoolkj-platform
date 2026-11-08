<template>
  <div class="icon-picker">
    <div class="icon-picker-input">
      <el-input
        :model-value="modelValue ?? ''"
        :placeholder="placeholder"
        :disabled="disabled"
        readonly
        class="icon-picker-readonly"
      >
        <template #prefix>
          <el-icon v-if="previewIcon" class="icon-picker-preview">
            <component :is="previewIcon" />
          </el-icon>
        </template>
      </el-input>
      <el-button v-if="modelValue && !disabled" @click="clearValue">
        {{ gp('clear') }}
      </el-button>
      <el-button type="primary" :disabled="disabled" @click="openDialog">
        {{ gp('select') }}
      </el-button>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="gp('title')"
      width="760px"
      destroy-on-close
      append-to-body
      class="icon-picker-dialog"
      @open="handleDialogOpen"
    >
      <el-input
        v-model="keyword"
        :placeholder="gp('searchPlaceholder')"
        clearable
        class="icon-picker-search"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <div v-if="filteredIcons.length" class="icon-grid">
        <button
          v-for="name in filteredIcons"
          :key="name"
          type="button"
          class="icon-item"
          :class="{ active: pendingValue === name }"
          :title="name"
          @click="pendingValue = name"
        >
          <el-icon :size="22"><component :is="iconMap[name]" /></el-icon>
          <span class="icon-name">{{ name }}</span>
        </button>
      </div>
      <el-empty v-else :description="gp('empty')" />

      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :disabled="!pendingValue" @click="confirmSelect">
          {{ t('common.confirm') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useGenI18n } from '@/composables/useGenI18n'
import type { GenI18nMessages } from '@/types/tool/genI18n'
import { Search } from '@element-plus/icons-vue'
import { iconMap, listIconNames, resolveIconComponent } from '@/utils/icon'
import genI18nZhCN from '@/i18n/gen/iconPicker/zh-CN.json'
import genI18nEnUS from '@/i18n/gen/iconPicker/en-US.json'

const props = defineProps<{
  modelValue?: string | null
  placeholder?: string
  disabled?: boolean
}>()

const emit = defineEmits<{ 'update:modelValue': [value: string | undefined] }>()

const { t } = useI18n()
const GEN_I18N_FILES: Record<string, GenI18nMessages> = {
  'zh-CN': genI18nZhCN as GenI18nMessages,
  'en-US': genI18nEnUS as GenI18nMessages,
}
const { gp } = useGenI18n('iconPicker', 'common', GEN_I18N_FILES)

const dialogVisible = ref(false)
const keyword = ref('')
const pendingValue = ref('')

const allIcons = listIconNames()

const previewIcon = computed(() => resolveIconComponent(props.modelValue))

const filteredIcons = computed(() => {
  const q = keyword.value.trim().toLowerCase()
  if (!q) return allIcons
  return allIcons.filter((name) => name.toLowerCase().includes(q))
})

function openDialog() {
  dialogVisible.value = true
}

function handleDialogOpen() {
  keyword.value = ''
  pendingValue.value = props.modelValue ?? ''
}

function confirmSelect() {
  if (!pendingValue.value) return
  emit('update:modelValue', pendingValue.value)
  dialogVisible.value = false
}

function clearValue() {
  emit('update:modelValue', undefined)
}
</script>

<style scoped>
.icon-picker-input {
  display: flex;
  gap: 8px;
  width: 100%;
}
.icon-picker-readonly {
  flex: 1;
}
.icon-picker-preview {
  font-size: 16px;
}
.icon-picker-search {
  margin-bottom: 12px;
}
.icon-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(96px, 1fr));
  gap: 8px;
  max-height: 420px;
  overflow: auto;
  padding: 4px;
}
.icon-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 10px 6px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  background: var(--el-bg-color);
  cursor: pointer;
  transition:
    border-color 0.15s,
    background 0.15s;
}
.icon-item:hover {
  border-color: var(--el-color-primary-light-5);
  background: var(--el-color-primary-light-9);
}
.icon-item.active {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}
.icon-name {
  font-size: 11px;
  color: var(--el-text-color-secondary);
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
