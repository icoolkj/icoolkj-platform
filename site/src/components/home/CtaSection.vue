<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { siteConfig } from '@/config/site'
import type { SiteLocale } from '@/config/site'
import { localizedPath } from '@/i18n/paths'
import type { SiteViewSection } from '@/i18n/types'

const props = defineProps<{ section: SiteViewSection }>()

const { t, locale } = useI18n()
const currentLocale = computed(() => locale.value as SiteLocale)

const primaryTo = computed(() =>
  localizedPath(props.section.ctaPrimary?.to ?? '/license', currentLocale.value),
)

const primaryLabel = computed(() =>
  t(`home.${props.section.ctaPrimary?.labelKey ?? 'ctaBand.primary'}`),
)

const secondaryLabel = computed(() =>
  t(`home.${props.section.ctaSecondary?.labelKey ?? 'ctaBand.secondary'}`),
)
</script>

<template>
  <section class="cta-band">
    <div class="container band-inner fade-up">
      <div>
        <h2>{{ t('home.ctaBand.title') }}</h2>
        <p>{{ t('home.ctaBand.body') }}</p>
      </div>
      <div class="actions">
        <RouterLink class="btn btn-primary" :to="primaryTo">
          {{ primaryLabel }}
        </RouterLink>
        <a
          class="btn btn-outline"
          :href="siteConfig.consoleUrl"
          target="_blank"
          rel="noopener noreferrer"
        >
          {{ secondaryLabel }}
        </a>
      </div>
    </div>
  </section>
</template>

<style scoped>
.cta-band {
  padding: 72px 0;
  background: var(--site-cta-bg);
  border-top: 1px solid var(--site-line);
}

.band-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 32px;
}

h2 {
  margin: 0 0 10px;
  font-family: var(--font-display);
  font-size: clamp(1.45rem, 2.2vw, 1.85rem);
  font-weight: 760;
  letter-spacing: -0.03em;
}

p {
  margin: 0;
  max-width: 36rem;
  color: var(--site-muted);
  line-height: 1.7;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  flex-shrink: 0;
}

@media (max-width: 860px) {
  .band-inner {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
