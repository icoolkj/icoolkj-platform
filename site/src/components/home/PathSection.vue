<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { SiteLocale } from '@/config/site'
import { localizedPath } from '@/i18n/paths'
import type { SiteViewSection } from '@/i18n/types'

type PathCopy = { title: string; body: string; linkText: string }

const props = defineProps<{ section: SiteViewSection }>()

const { t, tm, locale } = useI18n()
const currentLocale = computed(() => locale.value as SiteLocale)

const steps = computed(() => {
  const copy = tm('home.path.items') as Record<string, PathCopy>
  return (props.section.items ?? []).map((item) => ({
    ...item,
    ...copy[item.id],
  }))
})
</script>

<template>
  <section class="section section-alt">
    <div class="container fade-up">
      <p class="section-eyebrow">{{ t('home.path.eyebrow') }}</p>
      <h2 class="section-title">{{ t('home.path.title') }}</h2>
      <p class="section-lead">{{ t('home.path.lead') }}</p>
      <ol class="path-grid">
        <li v-for="(item, index) in steps" :key="item.id" class="path-card">
          <span class="num">{{ String(index + 1).padStart(2, '0') }}</span>
          <h3>{{ item.title }}</h3>
          <p>{{ item.body }}</p>
          <RouterLink class="more" :to="localizedPath(item.to, currentLocale)">
            {{ item.linkText }}
            <span aria-hidden="true">→</span>
          </RouterLink>
        </li>
      </ol>
    </div>
  </section>
</template>

<style scoped>
.path-grid {
  margin: 0;
  padding: 0;
  list-style: none;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.path-card {
  display: flex;
  flex-direction: column;
  padding: 24px;
  border: 1px solid var(--site-line);
  border-radius: var(--site-radius);
  background: var(--site-surface);
  box-shadow: var(--site-shadow);
}

.num {
  display: inline-block;
  margin-bottom: 12px;
  color: var(--site-primary);
  font-family: var(--font-display);
  font-weight: 750;
  font-size: 0.78rem;
  letter-spacing: 0.08em;
}

.path-card h3 {
  margin: 0 0 8px;
  font-family: var(--font-display);
  font-size: 1.08rem;
  letter-spacing: -0.02em;
}

.path-card p {
  margin: 0 0 16px;
  flex: 1;
  color: var(--site-muted);
  font-size: 0.94rem;
  line-height: 1.7;
}

.more {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--site-primary);
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 0.92rem;
}

.more:hover {
  color: var(--site-primary-dark);
}

@media (max-width: 960px) {
  .path-grid {
    grid-template-columns: 1fr;
  }
}
</style>
