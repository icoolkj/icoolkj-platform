<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { siteConfig } from '@/config/site'
import { useSiteView } from '@/composables/useSiteView'

useSiteView('about')
const { t } = useI18n()
</script>

<template>
  <section class="section">
    <div class="container fade-up">
      <p class="section-eyebrow">{{ t('about.eyebrow') }}</p>
      <h1 class="section-title">{{ t('about.title') }}</h1>
      <p class="section-lead">{{ t('about.lead') }}</p>

      <div class="card">
        <h2>{{ t('about.contactTitle') }}</h2>
        <dl>
          <div>
            <dt>{{ t('about.emailLabel') }}</dt>
            <dd>
              <a :href="`mailto:${siteConfig.contact.email}`">{{ siteConfig.contact.email }}</a>
            </dd>
          </div>
          <div>
            <dt>{{ t('about.domainLabel') }}</dt>
            <dd>
              <a :href="siteConfig.contact.siteUrl" target="_blank" rel="noopener noreferrer">
                {{ siteConfig.contact.domain }}
              </a>
            </dd>
          </div>
          <div>
            <dt>{{ t('about.icpLabel') }}</dt>
            <dd>
              <a :href="siteConfig.contact.icpUrl" target="_blank" rel="noopener noreferrer">
                {{ siteConfig.contact.icpNo }}
              </a>
            </dd>
          </div>
        </dl>
      </div>
    </div>
  </section>
</template>

<style scoped>
.card {
  padding: 28px;
  border: 1px solid var(--site-line);
  border-radius: var(--site-radius);
  background: var(--site-surface);
  box-shadow: var(--site-shadow);
}

.card h2 {
  margin: 0 0 18px;
  font-family: var(--font-display);
  font-size: 1.08rem;
  letter-spacing: -0.02em;
}

dl {
  margin: 0;
  display: grid;
  gap: 16px;
}

dl > div {
  display: grid;
  grid-template-columns: 100px 1fr;
  gap: 10px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--site-line);
}

dl > div:last-child {
  padding-bottom: 0;
  border-bottom: 0;
}

dt {
  color: var(--site-muted);
  font-weight: 600;
  font-size: 0.9rem;
}

dd {
  margin: 0;
  font-weight: 650;
  letter-spacing: -0.01em;
}

a:hover {
  color: var(--site-primary);
}

@media (max-width: 560px) {
  dl > div {
    grid-template-columns: 1fr;
  }
}
</style>
