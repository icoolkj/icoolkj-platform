<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { SiteLocale } from '@/config/site'
import { localizedPath } from '@/i18n/paths'
import type { SiteViewSection } from '@/i18n/types'

type LineCopy = {
  title: string
  body: string
  points: string[]
  linkText: string
}

const props = defineProps<{ section: SiteViewSection }>()

const { t, tm, locale } = useI18n()
const currentLocale = computed(() => locale.value as SiteLocale)

const items = computed(() => {
  const copy = tm('home.lines.items') as Record<string, LineCopy>
  return (props.section.items ?? []).map((item) => ({
    ...item,
    ...copy[item.id],
  }))
})
</script>

<template>
  <section class="section">
    <div class="container fade-up">
      <p class="section-eyebrow">{{ t('home.lines.eyebrow') }}</p>
      <h2 class="section-title">{{ t('home.lines.title') }}</h2>
      <p class="section-lead">{{ t('home.lines.lead') }}</p>
      <div class="line-grid">
        <article
          v-for="(item, index) in items"
          :key="item.id"
          class="line-card"
          :style="{ '--i': index }"
        >
          <span class="num">{{ String(index + 1).padStart(2, '0') }}</span>
          <h3>{{ item.title }}</h3>
          <p class="body">{{ item.body }}</p>
          <ul>
            <li v-for="(point, pi) in item.points" :key="pi">{{ point }}</li>
          </ul>
          <RouterLink class="more" :to="localizedPath(item.to, currentLocale)">
            {{ item.linkText }}
            <span aria-hidden="true">→</span>
          </RouterLink>
        </article>
      </div>
    </div>
  </section>
</template>

<style scoped>
.line-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.line-card {
  display: flex;
  flex-direction: column;
  padding: 28px 24px 24px;
  border: 1px solid var(--site-line);
  border-radius: var(--site-radius);
  background: var(--site-surface);
  box-shadow: var(--site-shadow);
  transition:
    transform 0.22s ease,
    border-color 0.22s ease,
    box-shadow 0.22s ease;
  animation: riseIn 0.7s cubic-bezier(0.22, 1, 0.36, 1) both;
  animation-delay: calc(var(--i, 0) * 80ms);
}

.line-card:hover {
  transform: translateY(-4px);
  border-color: var(--site-hover-border);
}

@keyframes riseIn {
  from {
    opacity: 0;
    transform: translateY(14px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.num {
  display: inline-block;
  margin-bottom: 14px;
  color: var(--site-primary);
  font-family: var(--font-display);
  font-size: 0.78rem;
  font-weight: 750;
  letter-spacing: 0.08em;
}

.line-card h3 {
  margin: 0 0 10px;
  font-family: var(--font-display);
  font-size: 1.14rem;
  letter-spacing: -0.02em;
}

.body {
  margin: 0 0 16px;
  color: var(--site-muted);
  font-size: 0.94rem;
  line-height: 1.7;
}

ul {
  margin: 0 0 20px;
  padding: 0;
  list-style: none;
  flex: 1;
}

li {
  position: relative;
  padding: 6px 0 6px 16px;
  color: var(--site-ink-soft);
  font-size: 0.9rem;
  line-height: 1.55;
}

li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0.85em;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--site-primary);
}

.more {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: auto;
  color: var(--site-primary);
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 0.92rem;
}

.more:hover {
  color: var(--site-primary-dark);
}

@media (max-width: 960px) {
  .line-grid {
    grid-template-columns: 1fr;
  }
}
</style>
