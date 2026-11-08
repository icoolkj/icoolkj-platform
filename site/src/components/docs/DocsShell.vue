<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import type { SiteLocale } from '@/config/site'
import { DOC_CATEGORY_IDS, type DocCategoryId, isDocCategoryId } from '@/config/docs'
import { localizedPath } from '@/i18n/paths'
import type { DocCategory } from '@/i18n/docs-types'
import { useSiteView } from '@/composables/useSiteView'

const props = defineProps<{
  categoryId?: DocCategoryId
}>()

const { t, locale, tm } = useI18n()
const route = useRoute()
const { related } = useSiteView('docs')
const currentLocale = computed(() => locale.value as SiteLocale)

const categories = computed(() => tm('docs.categories') as DocCategory[])

const activeId = computed<DocCategoryId | null>(() => {
  if (props.categoryId) return props.categoryId
  const raw = String(route.params.category ?? '')
  return isDocCategoryId(raw) ? raw : null
})

const activeCategory = computed(() =>
  categories.value.find((item) => item.id === activeId.value) ?? null,
)

const activeIndex = computed(() =>
  activeId.value ? DOC_CATEGORY_IDS.indexOf(activeId.value) : -1,
)

const prevCategory = computed(() => {
  const i = activeIndex.value
  if (i <= 0) return null
  return categories.value.find((c) => c.id === DOC_CATEGORY_IDS[i - 1]) ?? null
})

const nextCategory = computed(() => {
  const i = activeIndex.value
  if (i < 0 || i >= DOC_CATEGORY_IDS.length - 1) return null
  return categories.value.find((c) => c.id === DOC_CATEGORY_IDS[i + 1]) ?? null
})

function categoryPath(id: string) {
  return localizedPath(`/docs/${id}`, currentLocale.value)
}

function isNavActive(id: string) {
  return activeId.value === id
}
</script>

<template>
  <div class="docs-layout">
    <aside class="sidebar" :aria-label="t('docs.categoriesNav')">
      <RouterLink class="hub-link" :to="localizedPath('/docs', currentLocale)">
        {{ t('docs.title') }}
      </RouterLink>
      <nav class="side-nav">
        <RouterLink
          v-for="item in categories"
          :key="item.id"
          :to="categoryPath(item.id)"
          class="side-link"
          :class="{ active: isNavActive(item.id) }"
        >
          {{ item.title }}
        </RouterLink>
      </nav>
      <div class="related">
        <p class="related-title">{{ t('docs.relatedTitle') }}</p>
        <RouterLink
          v-for="item in related"
          :key="item.id"
          :to="localizedPath(item.to, currentLocale)"
        >
          {{ t(`docs.${item.labelKey}`) }}
        </RouterLink>
      </div>
    </aside>

    <div class="content">
      <slot
        :categories="categories"
        :active-category="activeCategory"
        :prev-category="prevCategory"
        :next-category="nextCategory"
        :category-path="categoryPath"
      />
    </div>
  </div>
</template>

<style scoped>
.docs-layout {
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  gap: 40px;
  align-items: start;
}

.sidebar {
  position: sticky;
  top: calc(var(--site-header-h) + 24px);
  padding: 18px 16px;
  border: 1px solid var(--site-line);
  border-radius: var(--site-radius);
  background: var(--site-surface);
  box-shadow: var(--site-shadow);
}

.hub-link {
  display: block;
  margin-bottom: 14px;
  padding: 0 8px;
  color: var(--site-ink);
  font-family: var(--font-display);
  font-weight: 750;
  font-size: 0.95rem;
  letter-spacing: -0.02em;
}

.hub-link:hover {
  color: var(--site-primary);
}

.side-nav {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.side-link {
  padding: 9px 10px;
  border-radius: 8px;
  color: var(--site-ink-soft);
  font-size: 0.9rem;
  font-weight: 600;
  line-height: 1.35;
  transition:
    color 0.15s ease,
    background 0.15s ease;
}

.side-link:hover {
  color: var(--site-ink);
  background: var(--site-overlay);
}

.side-link.active {
  color: var(--site-primary);
  background: var(--site-primary-soft);
}

.related {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px solid var(--site-line);
}

.related-title {
  margin: 0 0 2px;
  padding: 0 8px;
  color: var(--site-muted);
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.related a {
  padding: 0 8px;
  color: var(--site-muted);
  font-size: 0.86rem;
  font-weight: 500;
}

.related a:hover {
  color: var(--site-primary);
}

.content {
  min-width: 0;
}

@media (max-width: 900px) {
  .docs-layout {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .sidebar {
    position: static;
  }

  .side-nav {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 4px;
  }
}

@media (max-width: 560px) {
  .side-nav {
    grid-template-columns: 1fr;
  }
}
</style>
