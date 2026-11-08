<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { siteConfig } from '@/config/site'
import { useSiteView } from '@/composables/useSiteView'

const { t, tm } = useI18n()
const { sections } = useSiteView('license')

const steps = computed(() => tm('license.steps') as string[])
const consoleSection = computed(() => sections.find((s) => s.type === 'license-console'))
</script>

<template>
  <section class="section">
    <div class="container fade-up">
      <p class="section-eyebrow">{{ t('license.eyebrow') }}</p>
      <h1 class="section-title">{{ t('license.title') }}</h1>
      <p class="section-lead">{{ t('license.lead') }}</p>

      <div class="panel">
        <h2>{{ t('license.stepsTitle') }}</h2>
        <ol class="steps">
          <li v-for="(step, index) in steps" :key="index">
            <span>{{ String(index + 1).padStart(2, '0') }}</span>
            <p>{{ step }}</p>
          </li>
        </ol>
      </div>

      <div class="console-card">
        <div>
          <h2>{{ t('license.consoleTitle') }}</h2>
          <p>{{ t('license.consoleBody') }}</p>
          <p class="url">{{ siteConfig.consoleUrl }}</p>
        </div>
        <a
          class="btn btn-primary"
          :href="siteConfig.consoleUrl"
          target="_blank"
          rel="noopener noreferrer"
        >
          {{ t(`license.${consoleSection?.cta?.labelKey ?? 'consoleCta'}`) }}
        </a>
      </div>

      <p class="note">{{ t('license.note') }}</p>
    </div>
  </section>
</template>

<style scoped>
.panel {
  margin-bottom: 20px;
  padding: 24px 26px;
  border: 1px solid var(--site-line);
  border-radius: var(--site-radius);
  background: var(--site-surface);
  box-shadow: var(--site-shadow);
}

.panel h2,
.console-card h2 {
  margin: 0 0 16px;
  font-family: var(--font-display);
  font-size: 1.1rem;
  letter-spacing: -0.02em;
}

.steps {
  margin: 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 14px;
}

.steps li {
  display: grid;
  grid-template-columns: 40px 1fr;
  gap: 12px;
  align-items: start;
}

.steps span {
  color: var(--site-primary);
  font-family: var(--font-display);
  font-weight: 750;
  font-size: 0.85rem;
  letter-spacing: 0.04em;
}

.steps p {
  margin: 0;
  color: var(--site-ink-soft);
  line-height: 1.65;
}

.console-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 26px;
  border-radius: var(--site-radius);
  border: 1px solid var(--site-hover-border);
  background: var(--site-primary-soft);
  box-shadow: var(--site-shadow);
}

.console-card p {
  margin: 0 0 8px;
  color: var(--site-muted);
  max-width: 520px;
}

.url {
  margin: 0 !important;
  color: var(--site-ink-soft) !important;
  font-size: 0.85rem;
  font-weight: 600;
  word-break: break-all;
}

.note {
  margin: 18px 0 0;
  color: var(--site-muted);
  font-size: 0.9rem;
}

@media (max-width: 720px) {
  .console-card {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
