<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import { siteConfig, SITE_LOCALES, type SiteLocale } from '@/config/site'
import { localizedPath } from '@/i18n/paths'
import { switchLocaleKeepPage } from '@/router'
import { useSiteTheme } from '@/composables/useSiteTheme'

const { t, locale } = useI18n()
const route = useRoute()
const { theme, toggle } = useSiteTheme()

const currentLocale = computed(() => locale.value as SiteLocale)

const links = computed(() => [
  { to: localizedPath('/', currentLocale.value), label: t('nav.home'), match: 'exact' as const },
  {
    to: localizedPath('/product', currentLocale.value),
    label: t('nav.product'),
    match: 'exact' as const,
  },
  {
    to: localizedPath('/docs', currentLocale.value),
    label: t('nav.docs'),
    match: 'prefix' as const,
  },
  {
    to: localizedPath('/license', currentLocale.value),
    label: t('nav.license'),
    match: 'exact' as const,
  },
  {
    to: localizedPath('/delivery', currentLocale.value),
    label: t('nav.delivery'),
    match: 'exact' as const,
  },
  { to: localizedPath('/about', currentLocale.value), label: t('nav.about'), match: 'exact' as const },
])

function isActive(path: string, match: 'exact' | 'prefix') {
  if (match === 'prefix') {
    return route.path === path || route.path.startsWith(`${path}/`)
  }
  return route.path === path
}

function onLocale(code: SiteLocale) {
  if (code !== currentLocale.value) {
    void switchLocaleKeepPage(code)
  }
}
</script>

<template>
  <header class="header">
    <div class="container header-inner">
      <RouterLink class="brand" :to="localizedPath('/', currentLocale)">
        <span class="brand-mark" aria-hidden="true">
          <span class="brand-mark-core" />
        </span>
        <span class="brand-text">{{ siteConfig.brand }}</span>
      </RouterLink>

      <nav class="nav" :aria-label="t('nav.home')">
        <RouterLink
          v-for="item in links"
          :key="item.to"
          :to="item.to"
          class="nav-link"
          :class="{ active: isActive(item.to, item.match) }"
        >
          {{ item.label }}
        </RouterLink>
      </nav>

      <div class="actions">
        <button
          type="button"
          class="theme-toggle"
          :aria-label="t('nav.theme')"
          :title="theme === 'light' ? t('nav.themeDark') : t('nav.themeLight')"
          @click="toggle"
        >
          <span class="theme-icon" aria-hidden="true">
            <!-- sun -->
            <svg v-if="theme === 'dark'" viewBox="0 0 24 24" width="16" height="16" fill="none">
              <circle cx="12" cy="12" r="4" stroke="currentColor" stroke-width="1.8" />
              <path
                d="M12 2.5v2.2M12 19.3v2.2M4.7 4.7l1.6 1.6M17.7 17.7l1.6 1.6M2.5 12h2.2M19.3 12h2.2M4.7 19.3l1.6-1.6M17.7 6.3l1.6-1.6"
                stroke="currentColor"
                stroke-width="1.8"
                stroke-linecap="round"
              />
            </svg>
            <!-- moon -->
            <svg v-else viewBox="0 0 24 24" width="16" height="16" fill="none">
              <path
                d="M20 13.2A7.4 7.4 0 0 1 10.8 4 7.6 7.6 0 1 0 20 13.2Z"
                stroke="currentColor"
                stroke-width="1.8"
                stroke-linejoin="round"
              />
            </svg>
          </span>
        </button>
        <label class="locale">
          <span class="sr-only">{{ t('nav.language') }}</span>
          <select
            :value="currentLocale"
            :aria-label="t('nav.language')"
            @change="onLocale(($event.target as HTMLSelectElement).value as SiteLocale)"
          >
            <option v-for="item in SITE_LOCALES" :key="item.code" :value="item.code">
              {{ item.label }}
            </option>
          </select>
        </label>
        <a
          class="btn btn-primary console"
          :href="siteConfig.consoleUrl"
          target="_blank"
          rel="noopener noreferrer"
        >
          {{ t('nav.console') }}
        </a>
      </div>
    </div>
  </header>
</template>

<style scoped>
.header {
  position: sticky;
  top: 0;
  z-index: 40;
  height: var(--site-header-h);
  border-bottom: 1px solid var(--site-header-line);
  background: var(--site-header-bg);
  backdrop-filter: blur(16px) saturate(1.1);
}

.header-inner {
  display: flex;
  align-items: center;
  gap: 28px;
  height: 100%;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 11px;
  flex-shrink: 0;
}

.brand-mark {
  display: grid;
  place-items: center;
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: var(--site-primary);
}

.brand-mark-core {
  width: 10px;
  height: 10px;
  border-radius: 3px;
  background: rgba(255, 255, 255, 0.92);
}

.brand-text {
  font-family: var(--font-display);
  font-weight: 750;
  font-size: 0.98rem;
  letter-spacing: -0.03em;
  color: var(--site-ink);
}

.nav {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
  min-width: 0;
}

.nav-link {
  padding: 8px 12px;
  border-radius: 8px;
  color: var(--site-ink-soft);
  font-weight: 600;
  font-size: 0.9rem;
  letter-spacing: -0.01em;
  transition:
    color 0.15s ease,
    background 0.15s ease;
}

.nav-link:hover {
  color: var(--site-ink);
  background: var(--site-overlay);
}

.nav-link.active {
  color: var(--site-primary);
  background: var(--site-primary-soft);
}

.actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.theme-toggle {
  display: inline-grid;
  place-items: center;
  width: 38px;
  height: 38px;
  padding: 0;
  border: 1px solid var(--site-line);
  border-radius: 10px;
  background: var(--site-surface);
  color: var(--site-ink-soft);
  cursor: pointer;
  transition:
    border-color 0.15s ease,
    color 0.15s ease,
    background 0.15s ease;
}

.theme-toggle:hover {
  border-color: var(--site-hover-border);
  color: var(--site-primary);
}

.locale select {
  min-height: 38px;
  padding: 0 12px;
  border: 1px solid var(--site-line);
  border-radius: 10px;
  background: var(--site-surface);
  color: var(--site-ink-soft);
  font-weight: 600;
  font-size: 0.86rem;
  cursor: pointer;
}

.locale select:hover,
.locale select:focus {
  border-color: var(--site-hover-border);
  outline: none;
}

.console {
  min-height: 38px;
  padding: 0 14px;
  font-size: 0.86rem;
  border-radius: 10px;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  border: 0;
}

@media (max-width: 960px) {
  .header {
    height: auto;
  }

  .header-inner {
    flex-wrap: wrap;
    gap: 12px;
    padding: 12px 0;
  }

  .nav {
    order: 3;
    width: 100%;
    overflow-x: auto;
    padding-bottom: 2px;
  }

  .actions {
    margin-left: auto;
  }
}
</style>
