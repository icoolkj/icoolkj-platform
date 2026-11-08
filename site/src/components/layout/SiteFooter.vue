<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { siteConfig, type SiteLocale } from '@/config/site'
import { localizedPath } from '@/i18n/paths'

const { t, locale } = useI18n()
const year = new Date().getFullYear()
const currentLocale = computed(() => locale.value as SiteLocale)

const footerLinks = computed(() => [
  { to: localizedPath('/docs', currentLocale.value), label: t('nav.docs') },
  { to: localizedPath('/product', currentLocale.value), label: t('nav.product') },
  { to: localizedPath('/license', currentLocale.value), label: t('nav.license') },
  { to: localizedPath('/delivery', currentLocale.value), label: t('nav.delivery') },
  { to: localizedPath('/about', currentLocale.value), label: t('nav.about') },
])
</script>

<template>
  <footer class="footer">
    <div class="container footer-inner">
      <div class="col brand-col">
        <div class="brand-row">
          <span class="mark" aria-hidden="true" />
          <strong>{{ siteConfig.brand }}</strong>
        </div>
        <p>{{ t('meta.description') }}</p>
      </div>

      <div class="col">
        <h3>{{ t('nav.docs') }}</h3>
        <RouterLink v-for="item in footerLinks" :key="item.to" :to="item.to">
          {{ item.label }}
        </RouterLink>
      </div>

      <div class="col">
        <h3>{{ t('about.contactTitle') }}</h3>
        <a :href="`mailto:${siteConfig.contact.email}`">{{ siteConfig.contact.email }}</a>
        <a :href="siteConfig.contact.siteUrl" target="_blank" rel="noopener noreferrer">
          {{ siteConfig.contact.domain }}
        </a>
        <a :href="siteConfig.contact.icpUrl" target="_blank" rel="noopener noreferrer">
          {{ siteConfig.contact.icpNo }}
        </a>
      </div>
    </div>
    <div class="container copy">
      © {{ year }} {{ siteConfig.brand }}. {{ t('footer.rights') }}.
    </div>
  </footer>
</template>

<style scoped>
.footer {
  margin-top: auto;
  color: var(--site-footer-fg);
  background: var(--site-footer-bg);
  border-top: 1px solid var(--site-footer-line);
}

.footer-inner {
  display: grid;
  grid-template-columns: 1.4fr 0.8fr 1fr;
  gap: 36px;
  padding: 56px 0 28px;
}

.brand-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.mark {
  width: 18px;
  height: 18px;
  border-radius: 5px;
  background: var(--site-primary);
}

.brand-col strong {
  color: var(--site-footer-title);
  font-family: var(--font-display);
  font-size: 1.02rem;
  letter-spacing: -0.02em;
}

.brand-col p {
  margin: 0;
  max-width: 360px;
  color: var(--site-footer-muted);
  font-size: 0.92rem;
  line-height: 1.7;
}

.col {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.col h3 {
  margin: 0 0 4px;
  color: var(--site-footer-title);
  font-family: var(--font-display);
  font-size: 0.82rem;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.col a {
  color: var(--site-footer-muted);
  font-size: 0.92rem;
  font-weight: 500;
  transition: color 0.15s ease;
}

.col a:hover {
  color: var(--site-footer-title);
}

.copy {
  padding: 18px 0 28px;
  border-top: 1px solid var(--site-footer-line);
  color: var(--site-footer-muted);
  font-size: 0.84rem;
}

@media (max-width: 800px) {
  .footer-inner {
    grid-template-columns: 1fr;
    gap: 28px;
    padding-top: 40px;
  }
}
</style>
