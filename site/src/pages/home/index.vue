<script setup lang="ts">
import { type Component } from 'vue'
import { useSiteView } from '@/composables/useSiteView'
import HeroSection from '@/components/home/HeroSection.vue'
import ProductLinesSection from '@/components/home/ProductLinesSection.vue'
import CapsSection from '@/components/home/CapsSection.vue'
import WhySection from '@/components/home/WhySection.vue'
import PathSection from '@/components/home/PathSection.vue'
import CtaSection from '@/components/home/CtaSection.vue'
import type { SiteViewSection } from '@/i18n/types'

const { sections } = useSiteView('home')

const sectionMap: Record<string, Component> = {
  hero: HeroSection,
  'product-lines': ProductLinesSection,
  caps: CapsSection,
  why: WhySection,
  path: PathSection,
  'cta-band': CtaSection,
}

function resolve(section: SiteViewSection) {
  return sectionMap[section.type]
}
</script>

<template>
  <div>
    <component
      :is="resolve(section)"
      v-for="section in sections"
      :key="section.id"
      :section="section"
    />
  </div>
</template>
