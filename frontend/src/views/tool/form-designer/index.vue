<template>
  <div class="gen-page-shell form-designer-page" :class="{ 'form-designer-page--studio': studioActive }">
    <PageHeader v-if="!studioActive" :title="gp('title')" />
    <FormDesigner v-model="document" class="form-designer-page__body" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { storeToRefs } from 'pinia'
import PageHeader from '@/components/PageHeader.vue'
import FormDesigner from '@/components/form-designer/FormDesigner.vue'
import { buildDemoFormDocument } from '@/components/form-designer/schemaUtils'
import type { FormDocument } from '@/types/form-designer/schema'
import { useDesignerStudioStore } from '@/stores/designerStudio'
import { useGenPageShellI18n } from '@/composables/gen/useGenPageShellI18n'
const { gp } = useGenPageShellI18n('formDesigner')

const { active: studioActive } = storeToRefs(useDesignerStudioStore())

const document = ref<FormDocument>(
  buildDemoFormDocument({
    basic: gp('demoBasic'),
    title: gp('demoTitle'),
    details: gp('demoDetails'),
    amount: gp('demoAmount'),
    applyDate: gp('demoApplyDate'),
    priority: gp('demoPriority'),
    reason: gp('demoReason'),
    urgent: gp('demoUrgent'),
  }),
)
</script>

<script lang="ts">
export default { name: 'ToolFormDesigner' }
</script>

<style scoped>
/* Pin page to viewport so left/canvas/props scroll independently (same as studio). */
.form-designer-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
  max-height: calc(100vh - 120px);
  min-height: 0;
  overflow: hidden;
  box-sizing: border-box;
}
.form-designer-page--studio {
  height: 100vh;
  max-height: 100vh;
}
.form-designer-page :deep(.page-header) {
  flex-shrink: 0;
  margin-bottom: 12px;
}
.form-designer-page__body {
  flex: 1 1 0;
  min-height: 0;
  overflow: hidden;
}
.form-designer-page--studio .form-designer-page__body {
  height: 100%;
}
</style>
