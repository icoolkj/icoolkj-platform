<template>
  <nav v-if="products.length" class="product-nav" :aria-label="t('layout.productSwitcher')">
    <button
      v-for="p in products"
      :key="p.code"
      type="button"
      class="product-nav__item"
      :class="{ 'is-active': p.code === currentCode }"
      :title="p.name"
      @click="onSelect(p.code)"
    >
      <el-icon v-if="resolveProductIcon(p)" class="product-nav__icon">
        <component :is="resolveProductIcon(p)" />
      </el-icon>
      <span class="product-nav__label">{{ p.name }}</span>
    </button>
  </nav>
</template>

<script setup lang="ts">
import type { Component } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { storeToRefs } from 'pinia'
import { useProductStore } from '@/stores/product'
import { resolveIconComponent } from '@/utils/icon'
import type { ProductVO } from '@/types/system/product'

const PRODUCT_ICON_FALLBACK: Record<string, string> = {
  oa: 'Briefcase',
  files: 'FolderOpened',
  ai: 'MagicStick',
  platform: 'Monitor',
}

const { t } = useI18n()
const router = useRouter()
const productStore = useProductStore()
const { available: products, currentCode } = storeToRefs(productStore)

function resolveProductIcon(product: ProductVO): Component | null {
  const fromApi = product.icon?.trim()
  if (fromApi) {
    return resolveIconComponent(fromApi)
  }
  return resolveIconComponent(PRODUCT_ICON_FALLBACK[product.code] ?? 'Grid')
}

async function onSelect(code: string) {
  const hit = await productStore.switchProduct(code)
  if (hit?.homePath) {
    await router.replace(hit.homePath.startsWith('/') ? hit.homePath : `/${hit.homePath}`)
  }
}
</script>

<style scoped>
.product-nav {
  display: flex;
  align-items: stretch;
  flex: 1;
  min-width: 0;
  height: 100%;
  overflow-x: auto;
  scrollbar-width: none;
}

.product-nav::-webkit-scrollbar {
  display: none;
}

.product-nav__item {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0 18px;
  border: none;
  background: transparent;
  color: var(--ic-header-text-muted, #595959);
  font-size: 14px;
  line-height: 1;
  cursor: pointer;
  white-space: nowrap;
  transition:
    color 0.2s,
    background 0.2s;
}

.product-nav__icon {
  flex-shrink: 0;
  font-size: 16px;
  line-height: 1;
}

.product-nav__icon :deep(svg) {
  display: block;
}

.product-nav__label {
  line-height: 1.2;
}

.product-nav__item:hover {
  color: var(--ic-header-nav-hover-text, var(--ic-header-nav-active, var(--el-color-primary)));
  background: var(--ic-header-hover, rgba(0, 0, 0, 0.04));
}

.product-nav__item.is-active {
  color: var(--ic-header-nav-active, var(--el-color-primary));
  background: var(--ic-header-nav-active-bg, transparent);
  font-weight: 600;
}

.product-nav__item.is-active::after {
  content: '';
  position: absolute;
  left: 12px;
  right: 12px;
  bottom: 0;
  height: 2px;
  border-radius: 2px 2px 0 0;
  background: var(--ic-header-nav-indicator, var(--ic-header-nav-active, var(--el-color-primary)));
}
</style>
