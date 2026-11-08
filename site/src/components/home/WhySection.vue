<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { SiteViewSection } from '@/i18n/types'

type ValueCopy = { title: string; body: string }

const props = defineProps<{ section: SiteViewSection }>()

const { t, tm } = useI18n()

const values = computed(() => {
  const copy = tm('home.values.items') as Record<string, ValueCopy>
  return (props.section.itemIds ?? []).map((id) => ({ id, ...copy[id] }))
})
</script>

<template>
  <section class="section">
    <div class="container fade-up">
      <p class="section-eyebrow">{{ t('home.values.eyebrow') }}</p>
      <h2 class="section-title">{{ t('home.values.title') }}</h2>
      <div class="value-grid">
        <article
          v-for="(item, index) in values"
          :key="item.id"
          class="value-card"
          :style="{ '--i': index }"
        >
          <span class="num">{{ String(index + 1).padStart(2, '0') }}</span>
          <h3>{{ item.title }}</h3>
          <p>{{ item.body }}</p>
        </article>
      </div>
    </div>
  </section>
</template>

<style scoped>
.value-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
  margin-top: 8px;
}

.value-card {
  position: relative;
  padding: 26px 24px 24px;
  border: 1px solid var(--site-line);
  border-radius: var(--site-radius);
  background: var(--site-surface);
  box-shadow: var(--site-shadow);
  transition:
    transform 0.22s ease,
    border-color 0.22s ease,
    box-shadow 0.22s ease;
}

.value-card:hover {
  transform: translateY(-4px);
  border-color: var(--site-hover-border);
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

.value-card h3 {
  margin: 0 0 10px;
  font-family: var(--font-display);
  font-size: 1.08rem;
  letter-spacing: -0.02em;
}

.value-card p {
  margin: 0;
  color: var(--site-muted);
  font-size: 0.94rem;
  line-height: 1.7;
}

@media (max-width: 960px) {
  .value-grid {
    grid-template-columns: 1fr;
  }
}
</style>
