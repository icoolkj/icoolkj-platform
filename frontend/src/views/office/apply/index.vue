<!-- 在线办公 · 发起申请 — gen-page-shell + GenListLayout（目录卡片非标准 CRUD） -->
<template>
  <div class="gen-page-shell">
    <GenListLayout
      v-loading="loading"
      :title="gp('title')"
      :description="gp('description')"
      :show-search="showSearch"
      @refresh="loadCatalog"
      @toggle-search="showSearch = !showSearch"
    >
      <template #search>
        <el-form :model="query" inline @submit.prevent>
          <el-form-item :label="gf('keyword', 'label')">
            <el-input
              v-model="query.keyword"
              clearable
              :placeholder="gf('keyword', 'placeholder')"
              style="width: 240px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search">{{ t('common.search') }}</el-button>
            <el-button :icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
          </el-form-item>
        </el-form>
      </template>

      <template #toolbar>
        <span class="office-apply__panel-title">{{ gp('catalogPanel') }}</span>
        <span class="office-apply__panel-count">
          {{ gp('totalCount', { count: filteredItems.length }) }}
        </span>
      </template>

      <div class="office-apply__layout">
        <aside class="office-apply__aside">
          <button
            type="button"
            class="office-apply__cat"
            :class="{ 'is-active': !activeCategory }"
            @click="activeCategory = ''"
          >
            <span>{{ gp('allCategories') }}</span>
            <em>{{ items.length }}</em>
          </button>
          <button
            v-for="cat in categories"
            :key="cat.code"
            type="button"
            class="office-apply__cat"
            :class="{ 'is-active': activeCategory === cat.code }"
            @click="activeCategory = cat.code"
          >
            <span>{{ cat.label }}</span>
            <em>{{ cat.count }}</em>
          </button>
        </aside>

        <section class="office-apply__main">
          <el-empty v-if="!loading && !filteredItems.length" :description="gp('empty')" />
          <div v-else class="office-apply__grid">
            <button
              v-for="item in filteredItems"
              :key="item.tenantTemplateId"
              type="button"
              class="office-card"
              @click="launch(item)"
            >
              <div class="office-card__icon" :class="`office-card__icon--${toneOf(item)}`">
                <el-icon :size="22"><component :is="iconOf(item)" /></el-icon>
              </div>
              <div class="office-card__body">
                <div class="office-card__title">{{ item.templateName }}</div>
                <div class="office-card__subtitle">{{ categoryLabel(item.categoryCode) }}</div>
              </div>
            </button>
          </div>
        </section>
      </div>
    </GenListLayout>

    <OaInstanceFormDialog v-model="formVisible" :category-code="launchCategory" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { Refresh, Search } from '@element-plus/icons-vue'
import GenListLayout from '@/components/gen/layout/GenListLayout.vue'
import OaInstanceFormDialog from '@/views/oa/components/OaInstanceFormDialog.vue'
import { listOfficeCatalog } from '@/api/oa/oaOffice'
import { useDict } from '@/composables/useDict'
import { useGenPageShellI18n } from '@/composables/gen/useGenPageShellI18n'
import type { OaInitiateCatalogItemVO } from '@/types/oa/oaInitiate'
import {
  findBizTypeOption,
  resolveOaCatalogIcon,
  resolveOaCatalogTone,
} from '@/constants/oaCatalogUi'
const { gp, gf } = useGenPageShellI18n('officeApply')
const { t } = useI18n()
const { getDict } = useDict(['wf_biz_type'])

const loading = ref(false)
const showSearch = ref(true)
const activeCategory = ref('')
const items = ref<OaInitiateCatalogItemVO[]>([])
const formVisible = ref(false)
const launchCategory = ref<string>()
const query = reactive({ keyword: '' })

function resetQuery() {
  query.keyword = ''
  activeCategory.value = ''
}

function categoryLabel(categoryCode?: string) {
  if (!categoryCode) return gp('categoryFallback')
  const hit = findBizTypeOption(getDict('wf_biz_type'), categoryCode)
  return hit?.label || categoryCode || gp('categoryFallback')
}

const categories = computed(() => {
  const map = new Map<string, number>()
  for (const item of items.value) {
    const code = item.categoryCode || '_'
    map.set(code, (map.get(code) || 0) + 1)
  }
  return [...map.entries()].map(([code, count]) => ({
    code,
    label: categoryLabel(code === '_' ? undefined : code),
    count,
  }))
})

const filteredItems = computed(() => {
  let list = items.value
  if (activeCategory.value) {
    list = list.filter((i) => (i.categoryCode || '_') === activeCategory.value)
  }
  const q = query.keyword.trim().toLowerCase()
  if (!q) return list
  return list.filter((item) => {
    const name = (item.templateName || '').toLowerCase()
    const code = (item.categoryCode || '').toLowerCase()
    const cat = categoryLabel(item.categoryCode).toLowerCase()
    return name.includes(q) || code.includes(q) || cat.includes(q)
  })
})

function iconOf(item: OaInitiateCatalogItemVO) {
  const meta = findBizTypeOption(getDict('wf_biz_type'), item.categoryCode)
  return resolveOaCatalogIcon(meta?.icon)
}

function toneOf(item: OaInitiateCatalogItemVO) {
  const meta = findBizTypeOption(getDict('wf_biz_type'), item.categoryCode)
  return resolveOaCatalogTone(meta?.tagType)
}

function launch(item: OaInitiateCatalogItemVO) {
  launchCategory.value = item.categoryCode
  formVisible.value = true
}

async function loadCatalog() {
  loading.value = true
  try {
    const res = await listOfficeCatalog()
    items.value = res.data.data ?? []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void loadCatalog()
})
</script>

<style scoped>
.office-apply__panel-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.office-apply__panel-count {
  margin-left: 8px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.office-apply__layout {
  display: grid;
  grid-template-columns: 200px minmax(0, 1fr);
  gap: 16px;
  min-height: 360px;
}

.office-apply__aside {
  padding: 4px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  background: var(--el-fill-color-blank);
}

.office-apply__cat {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  margin: 0 0 2px;
  padding: 10px 12px;
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: var(--el-text-color-regular);
  cursor: pointer;
  text-align: left;
}

.office-apply__cat em {
  font-style: normal;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.office-apply__cat:hover {
  background: var(--el-fill-color-light);
}

.office-apply__cat.is-active {
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}

.office-apply__cat.is-active em {
  color: var(--el-color-primary);
}

.office-apply__main {
  min-width: 0;
}

.office-apply__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}

.office-card {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  margin: 0;
  padding: 16px 14px;
  text-align: left;
  cursor: pointer;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  background: var(--el-bg-color);
  transition:
    border-color 0.15s ease,
    box-shadow 0.15s ease;
}

.office-card:hover {
  border-color: var(--el-color-primary-light-5);
  box-shadow: 0 2px 10px rgb(0 0 0 / 5%);
}

.office-card:focus-visible {
  outline: 2px solid var(--el-color-primary);
  outline-offset: 2px;
}

.office-card__icon {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 8px;
  color: #fff;
}

.office-card__icon--success {
  background: var(--el-color-success);
}
.office-card__icon--warning {
  background: var(--el-color-warning);
}
.office-card__icon--info {
  background: var(--el-color-info);
}
.office-card__icon--primary {
  background: var(--el-color-primary);
}
.office-card__icon--danger {
  background: var(--el-color-danger);
}

.office-card__title {
  font-size: 14px;
  font-weight: 600;
  line-height: 1.35;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.office-card__subtitle {
  margin-top: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

@media (max-width: 900px) {
  .office-apply__layout {
    grid-template-columns: 1fr;
  }
}
</style>
