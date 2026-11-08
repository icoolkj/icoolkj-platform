<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { SiteLocale } from '@/config/site'
import { localizedPath } from '@/i18n/paths'
import type { SiteViewSection } from '@/i18n/types'

type CapCopy = { title: string; body: string }

const props = defineProps<{ section: SiteViewSection }>()

const { t, tm, locale } = useI18n()
const currentLocale = computed(() => locale.value as SiteLocale)

const caps = computed(() => {
  const copy = tm('product.items') as Record<string, CapCopy>
  return (props.section.itemIds ?? []).map((id) => ({ id, ...copy[id] }))
})

const docsTo = computed(() =>
  localizedPath(props.section.docsTo ?? '/docs/capabilities', currentLocale.value),
)
</script>

<template>
  <section class="section section-alt">
    <div class="container fade-up">
      <div class="section-head">
        <div>
          <p class="section-eyebrow">{{ t('home.caps.eyebrow') }}</p>
          <h2 class="section-title">{{ t('home.caps.title') }}</h2>
        </div>
        <RouterLink class="more" :to="docsTo">
          {{ t('home.caps.hint') }}
          <span aria-hidden="true">→</span>
        </RouterLink>
      </div>
      <div class="cap-grid">
        <article v-for="item in caps" :key="item.id" class="cap-card">
          <div class="cap-icon" aria-hidden="true">
            <span />
          </div>
          <h3>{{ item.title }}</h3>
          <p>{{ item.body }}</p>
        </article>
      </div>
    </div>
  </section>
</template>

<style scoped>
.more {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 28px;
  color: var(--site-primary);
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 0.92rem;
  white-space: nowrap;
}

.more:hover {
  color: var(--site-primary-dark);
}

.cap-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.cap-card {
  padding: 24px;
  border: 1px solid var(--site-line);
  border-radius: var(--site-radius);
  background: var(--site-surface);
  transition:
    border-color 0.2s ease,
    transform 0.2s ease;
}

.cap-card:hover {
  border-color: var(--site-hover-border);
  transform: translateY(-2px);
}

.cap-icon {
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  margin-bottom: 14px;
  border-radius: 10px;
  background: var(--site-primary-soft);
}

.cap-icon span {
  width: 12px;
  height: 12px;
  border-radius: 4px;
  background: var(--site-icon-fill);
}

.cap-card h3 {
  margin: 0 0 8px;
  font-family: var(--font-display);
  font-size: 1.05rem;
  letter-spacing: -0.02em;
}

.cap-card p {
  margin: 0;
  color: var(--site-muted);
  font-size: 0.94rem;
  line-height: 1.7;
}

@media (max-width: 960px) {
  .cap-grid {
    grid-template-columns: 1fr;
  }
}
</style>
