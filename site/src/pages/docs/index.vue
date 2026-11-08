<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import DocsShell from '@/components/docs/DocsShell.vue'
import type { DocCategory } from '@/i18n/docs-types'

const { t } = useI18n()
</script>

<template>
  <section class="section">
    <div class="container fade-up">
      <DocsShell v-slot="{ categories, categoryPath }">
        <p class="section-eyebrow">{{ t('docs.eyebrow') }}</p>
        <h1 class="section-title">{{ t('docs.title') }}</h1>
        <p class="section-lead">{{ t('docs.lead') }}</p>

        <div class="hub-grid">
          <RouterLink
            v-for="(item, index) in categories as DocCategory[]"
            :key="item.id"
            class="hub-card"
            :to="categoryPath(item.id)"
          >
            <span class="index">{{ String(index + 1).padStart(2, '0') }}</span>
            <h2>{{ item.title }}</h2>
            <p>{{ item.summary }}</p>
            <span class="cta">{{ t('docs.hubCta') }} →</span>
          </RouterLink>
        </div>
      </DocsShell>
    </div>
  </section>
</template>

<style scoped>
.hub-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.hub-card {
  display: flex;
  flex-direction: column;
  padding: 24px;
  border: 1px solid var(--site-line);
  border-radius: var(--site-radius);
  background: var(--site-surface);
  box-shadow: var(--site-shadow);
  transition:
    border-color 0.2s ease,
    transform 0.2s ease;
}

.hub-card:hover {
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

.hub-card h2 {
  margin: 0 0 10px;
  font-family: var(--font-display);
  font-size: 1.12rem;
  letter-spacing: -0.02em;
}

.hub-card p {
  margin: 0;
  flex: 1;
  color: var(--site-muted);
  line-height: 1.7;
}

.cta {
  margin-top: 16px;
  color: var(--site-primary);
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 0.9rem;
}

@media (max-width: 720px) {
  .hub-grid {
    grid-template-columns: 1fr;
  }
}
</style>
