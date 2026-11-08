<!-- AUTO-GENERATED 区划 — GenRuntimePage + view.json -->
<template>
  <GenRuntimePage page-key="region" :create-defaults="createDefaults" />
</template>

<script setup lang="ts">
import { GenRuntimePage } from '@/components/gen'
import { STATUS_NORMAL, TREE_ROOT_PARENT_ID } from '@/constants/flags'

const createDefaults = {
  parentId: TREE_ROOT_PARENT_ID,
  sortNo: 0,
  status: STATUS_NORMAL,
  regionLevel: '0',
}
</script>

