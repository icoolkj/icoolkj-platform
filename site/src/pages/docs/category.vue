<script setup lang="ts">
import { computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import type { SiteLocale } from '@/config/site'
import { isDocCategoryId } from '@/config/docs'
import DocsShell from '@/components/docs/DocsShell.vue'
import { localizedPath } from '@/i18n/paths'
import type { DocCategory, DocSection } from '@/i18n/docs-types'

const { t, locale } = useI18n()
const route = useRoute()
const router = useRouter()
const currentLocale = computed(() => locale.value as SiteLocale)

const categoryParam = computed(() => String(route.params.category ?? ''))

watch(
  categoryParam,
  (id) => {
    if (!isDocCategoryId(id)) {
      void router.replace(localizedPath('/docs', currentLocale.value))
    }
  },
  { immediate: true },
)

const categoryId = computed(() =>
  isDocCategoryId(categoryParam.value) ? categoryParam.value : undefined,
)
</script>

<template>
  <section class="section">
    <div class="container fade-up">
      <DocsShell
        v-if="categoryId"
        :category-id="categoryId"
        v-slot="{ activeCategory, prevCategory, nextCategory, categoryPath }"
      >
        <template v-if="activeCategory">
          <RouterLink class="back" :to="localizedPath('/docs', currentLocale)">
            ← {{ t('docs.backHub') }}
          </RouterLink>
          <p class="section-eyebrow">{{ t('docs.eyebrow') }}</p>
          <h1 class="section-title">{{ (activeCategory as DocCategory).title }}</h1>
          <p class="section-lead">{{ (activeCategory as DocCategory).summary }}</p>

          <nav class="toc" :aria-label="t('docs.toc')">
            <p class="toc-title">{{ t('docs.toc') }}</p>
            <a
              v-for="(section, index) in (activeCategory as DocCategory).sections"
              :key="index"
              :href="`#doc-s-${index}`"
            >
              {{ section.title }}
            </a>
          </nav>

          <article
            v-for="(section, index) in (activeCategory as DocCategory).sections"
            :id="`doc-s-${index}`"
            :key="index"
            class="doc-section"
          >
            <h2>{{ section.title }}</h2>
            <p>{{ section.body }}</p>
            <ul v-if="(section as DocSection).bullets?.length">
              <li v-for="(bullet, bi) in (section as DocSection).bullets" :key="bi">
                {{ bullet }}
              </li>
            </ul>
          </article>

          <div class="pager">
            <RouterLink
              v-if="prevCategory"
              class="pager-link"
              :to="categoryPath((prevCategory as DocCategory).id)"
            >
              <span class="pager-label">{{ t('docs.prev') }}</span>
              <strong>{{ (prevCategory as DocCategory).title }}</strong>
            </RouterLink>
            <span v-else />
            <RouterLink
              v-if="nextCategory"
              class="pager-link next"
              :to="categoryPath((nextCategory as DocCategory).id)"
            >
              <span class="pager-label">{{ t('docs.next') }}</span>
              <strong>{{ (nextCategory as DocCategory).title }}</strong>
            </RouterLink>
          </div>
        </template>
      </DocsShell>
    </div>
  </section>
</template>

<style scoped>
.back {
  display: inline-flex;
  margin-bottom: 18px;
  color: var(--site-primary);
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 0.9rem;
}

.back:hover {
  color: var(--site-primary-dark);
}

.toc {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin: 8px 0 28px;
  padding: 18px 20px;
  border: 1px solid var(--site-line);
  border-radius: var(--site-radius-sm);
  background: var(--site-primary-soft);
}

.toc-title {
  margin: 0 0 4px;
  color: var(--site-ink-soft);
  font-size: 0.78rem;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.toc a {
  color: var(--site-ink-soft);
  font-weight: 600;
  font-size: 0.92rem;
}

.toc a:hover {
  color: var(--site-primary);
}

.doc-section {
  padding: 22px 0;
  border-top: 1px solid var(--site-line);
}

.doc-section:first-of-type {
  border-top: 0;
  padding-top: 0;
}

.doc-section h2 {
  margin: 0 0 10px;
  font-family: var(--font-display);
  font-size: 1.2rem;
  letter-spacing: -0.02em;
}

.doc-section p {
  margin: 0;
  color: var(--site-ink-soft);
  line-height: 1.75;
}

.doc-section ul {
  margin: 12px 0 0;
  padding-left: 1.2em;
  color: var(--site-ink-soft);
  line-height: 1.75;
}

.doc-section li + li {
  margin-top: 6px;
}

.pager {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
  margin-top: 28px;
  padding-top: 22px;
  border-top: 1px solid var(--site-line);
}

.pager-link {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 16px 18px;
  border: 1px solid var(--site-line);
  border-radius: var(--site-radius-sm);
  background: var(--site-surface);
  transition: border-color 0.15s ease;
}

.pager-link:hover {
  border-color: var(--site-hover-border);
}

.pager-link.next {
  text-align: right;
  justify-self: end;
  width: 100%;
}

.pager-label {
  color: var(--site-muted);
  font-size: 0.78rem;
  font-weight: 600;
}

.pager-link strong {
  color: var(--site-ink);
  font-family: var(--font-display);
  font-size: 0.98rem;
  letter-spacing: -0.02em;
}

@media (max-width: 640px) {
  .pager {
    grid-template-columns: 1fr;
  }

  .pager-link.next {
    text-align: left;
  }
}
</style>
