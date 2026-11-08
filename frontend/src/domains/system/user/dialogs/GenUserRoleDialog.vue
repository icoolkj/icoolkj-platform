<template>
  <el-dialog
    :model-value="modelValue"
    :title="title"
    width="480px"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div v-loading="loading" class="role-list">
      <el-empty v-if="!loading && roleOptions.length === 0" :description="emptyText" />
      <el-checkbox-group v-else v-model="selectedRoleIds">
        <el-checkbox v-for="role in roleOptions" :key="String(role.id)" :value="String(role.id)">
          {{ role.roleName }}
          <span v-if="role.roleCode" class="role-code">({{ role.roleCode }})</span>
        </el-checkbox>
      </el-checkbox-group>
    </div>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">{{ cancelText }}</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">{{ confirmText }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { pageRole } from '@/api/system/role'
import { assignUserRoles, getUserRoleIds } from '@/api/system/user'
import { STATUS_NORMAL } from '@/constants/flags'
import type { SysRole } from '@/types/system/role'

const props = defineProps<{
  modelValue: boolean
  userId?: string | null
  title: string
  cancelText: string
  confirmText: string
  successText: string
  emptyText: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  success: []
}>()

const loading = ref(false)
const submitting = ref(false)
const roleOptions = ref<SysRole[]>([])
const selectedRoleIds = ref<string[]>([])

async function loadData() {
  if (!props.userId) return
  loading.value = true
  try {
    const [roleRes, idsRes] = await Promise.all([
      pageRole({ pageNo: 1, pageSize: 1000, status: STATUS_NORMAL }),
      getUserRoleIds(props.userId),
    ])
    roleOptions.value = roleRes.data.data.list
    selectedRoleIds.value = [...(idsRes.data.data ?? [])]
  } finally {
    loading.value = false
  }
}

async function submit() {
  if (!props.userId) return
  submitting.value = true
  try {
    await assignUserRoles(props.userId, selectedRoleIds.value)
    ElMessage.success(props.successText)
    emit('update:modelValue', false)
    emit('success')
  } finally {
    submitting.value = false
  }
}

watch(
  () => [props.modelValue, props.userId] as const,
  ([visible, userId]) => {
    if (visible && userId) {
      loadData()
    }
  },
)
</script>

<style scoped>
.role-list {
  min-height: 120px;
  max-height: 360px;
  overflow-y: auto;
}
.role-list :deep(.el-checkbox) {
  display: flex;
  margin-right: 0;
  margin-bottom: 8px;
}
.role-code {
  margin-left: 4px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>
