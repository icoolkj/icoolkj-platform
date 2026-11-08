<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useSiteView } from '@/composables/useSiteView'

type ModeCopy = { title: string; body: string }

const { t, tm } = useI18n()
const { sections } = useSiteView('delivery')

const modesSection = computed(() => sections.find((s) => s.type === 'delivery-modes'))

const modes = computed(() => {
  const copy = tm('delivery.modes.items') as Record<string, ModeCopy>
  return (modesSection.value?.itemIds ?? []).map((id) => ({ id, ...copy[id] }))
})

const outItems = computed(() => tm('delivery.out.items') as string[])
</script>

<template>
  <section class="section">
    <div class="container fade-up">
      <p class="section-eyebrow">{{ t('delivery.eyebrow') }}</p>
      <h1 class="section-title">{{ t('delivery.title') }}</h1>
      <p class="section-lead">{{ t('delivery.lead') }}</p>

      <h2 class="sub">{{ t('delivery.modes.title') }}</h2>
      <div class="grid">
        <article v-for="(mode, index) in modes" :key="mode.id" class="card">
          <span class="badge">{{ String(index + 1).padStart(2, '0') }}</span>
          <h3>{{ mode.title }}</h3>
          <p>{{ mode.body }}</p>
        </article>
      </div>

      <div class="out-panel">
        <h2 class="sub">{{ t('delivery.out.title') }}</h2>
        <ul class="out">
          <li v-for="(item, index) in outItems" :key="index">{{ item }}</li>
        </ul>
      </div>
    </div>
  </section>
</template>

<style scoped>
.sub {
  margin: 0 0 14px;
  font-family: var(--font-display);
  font-size: 1.08rem;
  letter-spacing: -0.02em;
}

.grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 28px;
}

.card {
  padding: 22px;
  border: 1px solid var(--site-line);
  border-radius: var(--site-radius);
  background: var(--site-surface);
  box-shadow: var(--site-shadow);
}

.badge {
  display: inline-block;
  margin-bottom: 12px;
  color: var(--site-primary);
  font-family: var(--font-display);
  font-size: 0.78rem;
  font-weight: 750;
  letter-spacing: 0.06em;
}

.card h3 {
  margin: 0 0 8px;
  font-family: var(--font-display);
  font-size: 1.05rem;
}

.card p {
  margin: 0;
  color: var(--site-muted);
  font-size: 0.94rem;
  line-height: 1.7;
}

.out-panel {
  padding: 22px 24px;
  border-radius: var(--site-radius);
  border: 1px solid var(--site-line);
  background: var(--site-bg-alt);
}

.out {
  margin: 0;
  padding-left: 1.15rem;
  color: var(--site-ink-soft);
}

.out li {
  margin: 8px 0;
}

@media (max-width: 860px) {
  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
