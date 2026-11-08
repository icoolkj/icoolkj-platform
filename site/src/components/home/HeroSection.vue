<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { siteConfig } from '@/config/site'
import { localizedPath } from '@/i18n/paths'
import type { SiteLocale } from '@/config/site'
import type { SiteViewSection } from '@/i18n/types'

const props = defineProps<{ section: SiteViewSection }>()

const { t, locale } = useI18n()
const currentLocale = computed(() => locale.value as SiteLocale)

const primaryTo = computed(() =>
  props.section.ctaPrimary?.to
    ? localizedPath(props.section.ctaPrimary.to, currentLocale.value)
    : localizedPath('/license', currentLocale.value),
)

const primaryLabel = computed(() =>
  t(`home.${props.section.ctaPrimary?.labelKey ?? 'ctaPrimary'}`),
)

const secondaryLabel = computed(() =>
  t(`home.${props.section.ctaSecondary?.labelKey ?? 'ctaSecondary'}`),
)
</script>

<template>
  <section class="hero">
    <div class="hero-grid" aria-hidden="true" />

    <div class="container hero-layout">
      <div class="hero-copy fade-up">
        <p class="brand-line">{{ t('home.brand') }}</p>
        <h1>{{ t('home.headline') }}</h1>
        <p class="subhead">{{ t('home.subhead') }}</p>
        <div class="cta-row">
          <RouterLink class="btn btn-primary" :to="primaryTo">
            {{ primaryLabel }}
          </RouterLink>
          <a
            class="btn btn-ghost"
            :href="siteConfig.consoleUrl"
            target="_blank"
            rel="noopener noreferrer"
          >
            {{ secondaryLabel }}
          </a>
        </div>
      </div>

      <div class="hero-visual fade-up-delay" aria-hidden="true">
        <div class="window">
          <div class="window-bar">
            <span /><span /><span />
            <em>{{ siteConfig.brand }}</em>
          </div>
          <div class="window-body">
            <aside class="side">
              <i /><i /><i /><i />
            </aside>
            <div class="main-pane">
              <div class="kpi-row">
                <b /><b /><b />
              </div>
              <div class="chart">
                <div class="bars">
                  <s style="--h: 42%" /><s style="--h: 68%" /><s style="--h: 54%" />
                  <s style="--h: 78%" /><s style="--h: 62%" /><s style="--h: 88%" />
                </div>
              </div>
              <div class="table">
                <u /><u /><u />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.hero {
  position: relative;
  overflow: hidden;
  color: var(--site-hero-fg);
  background:
    linear-gradient(165deg, var(--site-hero-0) 0%, var(--site-hero-1) 48%, var(--site-hero-2) 100%);
  border-bottom: 1px solid var(--site-line);
}

.hero-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(var(--site-hero-grid) 1px, transparent 1px),
    linear-gradient(90deg, var(--site-hero-grid) 1px, transparent 1px);
  background-size: 48px 48px;
  mask-image: radial-gradient(ellipse 70% 60% at 50% 30%, #000 20%, transparent 75%);
  opacity: 0.9;
  pointer-events: none;
}

.hero-layout {
  position: relative;
  display: grid;
  grid-template-columns: 1.05fr 0.95fr;
  gap: 48px;
  align-items: center;
  min-height: max(560px, calc(100vh - var(--site-header-h)));
  padding: 64px 0 72px;
}

.brand-line {
  margin: 0 0 16px;
  font-family: var(--font-display);
  font-weight: 750;
  font-size: clamp(1.2rem, 2vw, 1.5rem);
  letter-spacing: -0.03em;
  color: var(--site-hero-brand);
}

.hero h1 {
  margin: 0;
  max-width: 11em;
  font-family: var(--font-display);
  font-size: clamp(2.15rem, 4.4vw, 3.35rem);
  font-weight: 800;
  line-height: 1.12;
  letter-spacing: -0.04em;
  color: var(--site-hero-fg);
}

.subhead {
  margin: 18px 0 0;
  max-width: 34rem;
  color: var(--site-hero-muted);
  font-size: 1.08rem;
  line-height: 1.7;
}

.cta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 30px;
}

.hero-visual {
  display: flex;
  justify-content: flex-end;
}

.window {
  width: min(100%, 460px);
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid var(--site-hero-mock-border);
  background: var(--site-hero-mock-bg);
  box-shadow: var(--site-shadow-lg);
  transform: perspective(1200px) rotateY(-6deg) rotateX(3deg);
  transform-origin: left center;
  animation: floatCard 6s ease-in-out infinite;
}

@keyframes floatCard {
  0%,
  100% {
    transform: perspective(1200px) rotateY(-6deg) rotateX(3deg) translateY(0);
  }
  50% {
    transform: perspective(1200px) rotateY(-6deg) rotateX(3deg) translateY(-8px);
  }
}

.window-bar {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 14px;
  background: var(--site-hero-mock-bar);
  border-bottom: 1px solid var(--site-hero-mock-border);
}

.window-bar span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--site-muted);
  opacity: 0.55;
}

.window-bar span:nth-child(1) {
  background: #de7802;
  opacity: 0.85;
}
.window-bar span:nth-child(2) {
  background: #d4a017;
  opacity: 0.85;
}
.window-bar span:nth-child(3) {
  background: #2ba471;
  opacity: 0.85;
}

.window-bar em {
  margin-left: 8px;
  color: var(--site-muted);
  font-style: normal;
  font-size: 0.72rem;
  font-weight: 600;
}

.window-body {
  display: grid;
  grid-template-columns: 72px 1fr;
  min-height: 280px;
}

.side {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 16px 12px;
  background: var(--site-hero-mock-side);
  border-right: 1px solid var(--site-hero-mock-border);
}

.side i {
  display: block;
  height: 10px;
  border-radius: 999px;
  background: var(--site-hero-mock-chip);
}

.side i:first-child {
  background: var(--site-hero-mock-active);
}

.main-pane {
  padding: 16px;
  display: grid;
  gap: 12px;
}

.kpi-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.kpi-row b {
  display: block;
  height: 42px;
  border-radius: 8px;
  background: var(--site-hero-mock-chip);
  border: 1px solid var(--site-hero-mock-border);
}

.chart {
  height: 110px;
  padding: 12px 12px 8px;
  border-radius: 10px;
  border: 1px solid var(--site-hero-mock-border);
  background: var(--site-hero-mock-side);
}

.bars {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  height: 100%;
}

.bars s {
  flex: 1;
  height: var(--h);
  border-radius: 4px 4px 0 0;
  background: var(--site-hero-mock-bar-fill);
  text-decoration: none;
  opacity: 0.92;
}

.table {
  display: grid;
  gap: 8px;
}

.table u {
  display: block;
  height: 12px;
  border-radius: 999px;
  background: var(--site-hero-mock-chip);
  text-decoration: none;
}

.table u:nth-child(2) {
  width: 86%;
}
.table u:nth-child(3) {
  width: 72%;
}

@media (max-width: 960px) {
  .hero-layout {
    grid-template-columns: 1fr;
    min-height: auto;
    padding: 48px 0 56px;
    gap: 36px;
  }

  .hero-visual {
    justify-content: center;
  }

  .window {
    transform: none;
    animation: none;
  }
}
</style>
