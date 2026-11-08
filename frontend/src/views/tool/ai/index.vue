<template>
  <div />
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

onMounted(() => {
  router.replace('/tool/ai/catalog')
})
</script>

<script lang="ts">
export default { name: 'ToolAiIndex' }
</script>
