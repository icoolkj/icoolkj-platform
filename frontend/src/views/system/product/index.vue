<!-- 产品宇宙 — GenRuntimePage + view.json -->
<template>
  <GenRuntimePage page-key="product" :create-defaults="createDefaults" />
</template>

<script setup lang="ts">
import { GenRuntimePage } from '@/components/gen'
import { STATUS_NORMAL } from '@/constants/flags'

const createDefaults = {
  sortNo: 10,
  status: STATUS_NORMAL,
}
</script>

