<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { SiteLocale } from '@/config/site'
import { localizedPath } from '@/i18n/paths'
import { useSiteView } from '@/composables/useSiteView'

type LineCopy = { title: string; body: string }
type CapCopy = { title: string; body: string }

const { t, tm, locale } = useI18n()
const { sections } = useSiteView('product')
const currentLocale = computed(() => locale.value as SiteLocale)

const linesSection = computed(() => sections.find((s) => s.type === 'product-lines-intro'))
const capsSection = computed(() => sections.find((s) => s.type === 'caps-grid'))

const lines = computed(() => {
  const copy = tm('home.lines.items') as Record<string, LineCopy>
  return (linesSection.value?.itemIds ?? []).map((id) => ({ id, ...copy[id] }))
})

const items = computed(() => {
  const copy = tm('product.items') as Record<string, CapCopy>
  return (capsSection.value?.itemIds ?? []).map((id) => ({ id, ...copy[id] }))
})

const docsTo = computed(() =>
  localizedPath(capsSection.value?.docsTo ?? '/docs/capabilities', currentLocale.value),
)
</script>

<template>
  <div>
    <section class="section section-alt lines-band">
      <div class="container fade-up">
        <p class="section-eyebrow">{{ t('product.linesEyebrow') }}</p>
        <h1 class="section-title">{{ t('product.linesTitle') }}</h1>
        <p class="section-lead">{{ t('product.linesLead') }}</p>
        <div class="line-grid">
          <article v-for="(item, index) in lines" :key="item.id" class="line-card">
            <span class="num">{{ String(index + 1).padStart(2, '0') }}</span>
            <h2>{{ item.title }}</h2>
            <p>{{ item.body }}</p>
          </article>
        </div>
      </div>
    </section>

    <section class="section">
      <div class="container fade-up">
        <p class="section-eyebrow">{{ t('home.caps.eyebrow') }}</p>
        <h2 class="section-title">{{ t('product.title') }}</h2>
        <p class="section-lead">{{ t('product.lead') }}</p>
        <p class="docs-hint">
          <RouterLink :to="docsTo">{{ t('product.docsLink') }} →</RouterLink>
        </p>
        <div class="grid">
          <article v-for="(item, index) in items" :key="item.id" class="card">
            <span class="index">{{ String(index + 1).padStart(2, '0') }}</span>
            <h3>{{ item.title }}</h3>
            <p>{{ item.body }}</p>
          </article>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.lines-band {
  padding-bottom: 64px;
}

.line-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.line-card {
  padding: 22px 20px;
  border: 1px solid var(--site-line);
  border-radius: var(--site-radius);
  background: var(--site-surface);
  box-shadow: var(--site-shadow);
}

.num {
  display: inline-block;
  margin-bottom: 10px;
  color: var(--site-primary);
  font-family: var(--font-display);
  font-weight: 750;
  font-size: 0.78rem;
  letter-spacing: 0.08em;
}

.line-card h2 {
  margin: 0 0 8px;
  font-family: var(--font-display);
  font-size: 1.08rem;
  letter-spacing: -0.02em;
}

.line-card p {
  margin: 0;
  color: var(--site-muted);
  font-size: 0.92rem;
  line-height: 1.65;
}

.docs-hint {
  margin: -8px 0 28px;
}

.docs-hint a {
  color: var(--site-primary);
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 0.92rem;
}

.docs-hint a:hover {
  color: var(--site-primary-dark);
}

.grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.card {
  padding: 26px 24px;
  border: 1px solid var(--site-line);
  border-radius: var(--site-radius);
  background: var(--site-surface);
  box-shadow: var(--site-shadow);
  transition:
    border-color 0.2s ease,
    transform 0.2s ease;
}

.card:hover {
  border-color: var(--site-hover-border);
  transform: translateY(-2px);
}

.index {
  display: inline-block;
  margin-bottom: 12px;
  color: var(--site-primary);
  font-family: var(--font-display);
  font-weight: 750;
  font-size: 0.78rem;
  letter-spacing: 0.08em;
}

.card h3 {
  margin: 0 0 10px;
  font-family: var(--font-display);
  font-size: 1.12rem;
  letter-spacing: -0.02em;
}

.card p {
  margin: 0;
  color: var(--site-muted);
  line-height: 1.7;
}

@media (max-width: 960px) {
  .line-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
