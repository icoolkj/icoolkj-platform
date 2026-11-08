<template>
  <el-button
    v-if="mode === 'add'"
    circle
    plain
    type="primary"
    size="small"
    class="picker-transfer-btn picker-transfer-btn--add"
    :disabled="disabled"
    @click="emit('click')"
  >
    +
  </el-button>
  <el-button
    v-else
    circle
    plain
    type="danger"
    size="small"
    class="picker-transfer-btn picker-transfer-btn--remove"
    @click="emit('click')"
  >
    -
  </el-button>
</template>

<script setup lang="ts">
defineProps<{
  mode: 'add' | 'remove'
  disabled?: boolean
}>()

const emit = defineEmits<{
  click: []
}>()
</script>

<style scoped lang="scss">
.picker-transfer-btn {
  width: 24px;
  height: 24px;
  padding: 0;
  font-size: 14px;
  font-weight: 600;
  line-height: 1;
}
</style>
