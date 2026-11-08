<template>
  <div class="gen-page-shell">
    <GenListLayout plain :title="title" :description="desc">
      <el-row :gutter="16">
        <el-col v-for="card in cards" :key="card.key" :xs="12" :sm="8" :md="6">
          <el-card shadow="never" class="ov-card">
            <div class="ov-card__label">{{ card.label }}</div>
            <div class="ov-card__value">{{ card.value }}</div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty class="ov-empty" :description="t('layout.overviewPlaceholder')" :image-size="72" />
    </GenListLayout>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import GenListLayout from '@/components/gen/layout/GenListLayout.vue'
import { PLATFORM_PRODUCT_CODE, type BuiltinProductCode } from '@/constants/product'

const props = defineProps<{
  productKey: Exclude<BuiltinProductCode, typeof PLATFORM_PRODUCT_CODE>
}>()

const { t } = useI18n()

const title = computed(() => t(`layout.overview.${props.productKey}.title`))
const desc = computed(() => t(`layout.overview.${props.productKey}.desc`))
const cards = computed(() => [
  { key: 'a', label: t(`layout.overview.${props.productKey}.stat1`), value: '—' },
  { key: 'b', label: t(`layout.overview.${props.productKey}.stat2`), value: '—' },
  { key: 'c', label: t(`layout.overview.${props.productKey}.stat3`), value: '—' },
])
</script>

<style scoped>
.ov-card {
  margin-bottom: 16px;
}
.ov-card__label {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
.ov-card__value {
  margin-top: 8px;
  font-size: 22px;
  font-weight: 600;
}
.ov-empty {
  margin-top: 24px;
}
</style>
