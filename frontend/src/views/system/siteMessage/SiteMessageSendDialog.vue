<template>
  <GenFormShell v-model="visible" :title="gp('sendTitle')" :width="760">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="108px">
      <el-form-item v-if="showTenantSelect" :label="gp('targetTenant')" required>
        <div class="picker-trigger">
          <el-button type="primary" plain @click="tenantPickerVisible = true">
            {{ gp('pickTenants') }}
          </el-button>
          <div class="picker-trigger__tags">
            <el-tag
              v-for="item in selectedTenants"
              :key="item.id"
              closable
              @close="removeTenant(item.id!)"
            >
              {{ item.tenantName }} ({{ item.tenantCode }})
            </el-tag>
            <span v-if="!selectedTenants.length" class="picker-trigger__placeholder">
              {{ gp('targetTenantPlaceholder') }}
            </span>
          </div>
        </div>
        <div class="form-tip">{{ gp('targetTenantHint') }}</div>
        <div v-if="multiTenantMode" class="form-tip">{{ gp('multiTenantHint') }}</div>
      </el-form-item>

      <el-form-item :label="gf('messageSubject', 'label')" prop="messageSubject">
        <el-input
          v-model="form.messageSubject"
          :placeholder="gf('messageSubject', 'placeholder')"
          maxlength="200"
        />
      </el-form-item>

      <el-form-item :label="gf('messageCategory', 'label')" prop="messageCategory">
        <el-select
          v-model="form.messageCategory"
          :placeholder="gf('messageCategory', 'placeholder')"
          style="width: 100%"
        >
          <el-option
            v-for="item in getDict('sys_site_message_category')"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item :label="gf('messageContent', 'label')" prop="messageContent">
        <el-input
          v-model="form.messageContent"
          type="textarea"
          :rows="6"
          :placeholder="gf('messageContent', 'placeholder')"
        />
      </el-form-item>

      <el-form-item :label="gf('recipientType', 'label')" prop="recipientType">
        <el-radio-group v-model="form.recipientType">
          <el-radio value="user">{{ gp('recipientUsers') }}</el-radio>
          <el-radio value="dept">{{ gp('recipientDepts') }}</el-radio>
          <el-radio value="role">{{ gp('recipientRoles') }}</el-radio>
          <el-radio value="all">{{ gp('recipientAll') }}</el-radio>
        </el-radio-group>
        <div v-if="form.recipientType === 'all'" class="form-tip">{{ gp('recipientAllHint') }}</div>
      </el-form-item>

      <el-form-item v-if="form.recipientType === 'user'" :label="gp('selectUsers')">
        <div class="picker-trigger">
          <el-button
            type="primary"
            plain
            :disabled="!canPickRecipients"
            @click="userPickerVisible = true"
          >
            {{ gp('pickUsers') }}
          </el-button>
          <div class="picker-trigger__tags">
            <el-tag
              v-for="item in selectedUsers"
              :key="item.id"
              closable
              @close="removeUser(item.id!)"
            >
              {{ formatUserTag(item) }}
            </el-tag>
            <span v-if="!selectedUsers.length" class="picker-trigger__placeholder">
              {{ gp('selectUsers') }}
            </span>
          </div>
        </div>
      </el-form-item>

      <el-form-item v-else-if="form.recipientType === 'dept'" :label="gp('selectDepts')">
        <div class="picker-trigger">
          <el-button
            type="primary"
            plain
            :disabled="!canPickRecipients"
            @click="deptPickerVisible = true"
          >
            {{ gp('pickDepts') }}
          </el-button>
          <div class="picker-trigger__tags">
            <el-tag
              v-for="item in selectedDepts"
              :key="item.id"
              closable
              @close="removeDept(item.id!)"
            >
              {{ formatDeptTag(item) }}
            </el-tag>
            <span v-if="!selectedDepts.length" class="picker-trigger__placeholder">
              {{ gp('selectDepts') }}
            </span>
          </div>
        </div>
      </el-form-item>

      <el-form-item v-else-if="form.recipientType === 'role'" :label="gp('selectRoles')">
        <div class="picker-trigger">
          <el-button
            type="primary"
            plain
            :disabled="!canPickRecipients"
            @click="rolePickerVisible = true"
          >
            {{ gp('pickRoles') }}
          </el-button>
          <div class="picker-trigger__tags">
            <el-tag
              v-for="item in selectedRoles"
              :key="item.id"
              closable
              @close="removeRole(item.id!)"
            >
              {{ formatRoleTag(item) }}
            </el-tag>
            <span v-if="!selectedRoles.length" class="picker-trigger__placeholder">
              {{ gp('selectRoles') }}
            </span>
          </div>
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">
        {{ t('common.confirm') }}
      </el-button>
    </template>
  </GenFormShell>

  <TenantPickerDialog
    v-model="tenantPickerVisible"
    :title="gp('pickTenantsTitle')"
    :selected="selectedTenants"
    :pending-title="gp('pendingListTitle')"
    :search-placeholder="gp('tenantSearchPlaceholder')"
    :search-text="t('common.search')"
    :reset-text="t('common.reset')"
    :cancel-text="t('common.cancel')"
    :confirm-text="t('common.confirm')"
    :selected-text="gp('selectedCount')"
    :clear-text="gp('clearSelected')"
    :add-current-text="gp('addCurrentPage')"
    :action-label="t('common.actions')"
    :count-unit="gp('listCountUnit')"
    :name-label="gp('tenantNameLabel')"
    :code-label="gp('tenantCodeLabel')"
    :type-label="gp('tenantTypeLabel')"
    @confirm="handleTenantsPicked"
  />

  <UserPickerDialog
    v-if="canPickRecipients"
    v-model="userPickerVisible"
    :tenants="pickerTenants"
    :title="gp('pickUsersTitle')"
    :selected="selectedUsers"
    :dept-title="gp('deptTreeTitle')"
    :tenant-title="gp('tenantNameLabel')"
    :pending-title="gp('pendingListTitle')"
    :selected-title="gp('selectedListTitle')"
    :add-current-text="gp('addCurrentPage')"
    :clear-text="gp('clearSelected')"
    :selected-text="gp('selectedCount')"
    :search-placeholder="gp('userSearchPlaceholder')"
    :search-text="t('common.search')"
    :reset-text="t('common.reset')"
    :cancel-text="t('common.cancel')"
    :confirm-text="t('common.confirm')"
    :action-label="t('common.actions')"
    :count-unit="gp('listCountUnit')"
    :nickname-label="gp('userNicknameLabel')"
    :username-label="gp('userAccountLabel')"
    @confirm="handleUsersPicked"
  />

  <DeptPickerDialog
    v-if="canPickRecipients"
    v-model="deptPickerVisible"
    :tenants="pickerTenants"
    :title="gp('pickDeptsTitle')"
    :tree-title="gp('deptTreeTitle')"
    :tenant-title="gp('tenantNameLabel')"
    :name-label="gp('deptTreeTitle')"
    :selected-ids="form.deptIds"
    :selected-text="gp('selectedCount')"
    :clear-text="gp('clearSelected')"
    :cancel-text="t('common.cancel')"
    :confirm-text="t('common.confirm')"
    :action-label="t('common.actions')"
    @confirm="handleDeptsPicked"
  />

  <RolePickerDialog
    v-if="canPickRecipients"
    v-model="rolePickerVisible"
    :tenants="pickerTenants"
    :title="gp('pickRolesTitle')"
    :tenant-title="gp('tenantNameLabel')"
    :role-list-title="gp('roleListTitle')"
    :selected-text="gp('selectedCount')"
    :selected="selectedRoles"
    :search-placeholder="gp('roleSearchPlaceholder')"
    :search-text="t('common.search')"
    :reset-text="t('common.reset')"
    :cancel-text="t('common.cancel')"
    :confirm-text="t('common.confirm')"
    :clear-text="gp('clearSelected')"
    :add-current-text="gp('addCurrentPage')"
    :action-label="t('common.actions')"
    :count-unit="gp('listCountUnit')"
    :name-label="gp('roleNameLabel')"
    :code-label="gp('roleCodeLabel')"
    @confirm="handleRolesPicked"
  />
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import GenFormShell from '@/components/gen/layout/GenFormShell.vue'
import TenantPickerDialog from '@/components/picker/TenantPickerDialog.vue'
import UserPickerDialog, { type PickerUserVO } from '@/components/picker/UserPickerDialog.vue'
import DeptPickerDialog, { type PickerDeptVO } from '@/components/picker/DeptPickerDialog.vue'
import RolePickerDialog, { type PickerRoleVO } from '@/components/picker/RolePickerDialog.vue'
import { sendSiteMessage } from '@/api/system/siteMessage'
import { useDict } from '@/composables/useDict'
import { useUserStore } from '@/stores/user'
import { isPlatformTenant } from '@/constants/flags'
import type { SiteMessageSendPayload } from '@/types/system/siteMessage'
import type { TenantVO } from '@/types/tenant/tenant'

const props = defineProps<{
  modelValue: boolean
  gp: (key: string, params?: Record<string, string | number>) => string
  gf: (field: string, key?: string) => string
}>()

const emit = defineEmits<{
  'update:modelValue': [boolean]
  success: []
}>()

const { t } = useI18n()
const userStore = useUserStore()
const { getDict, reload } = useDict(['sys_site_message_category'])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
})

const showTenantSelect = computed(() => isPlatformTenant(userStore.user?.tenantId))
const selectedTenants = ref<TenantVO[]>([])
const selectedUsers = ref<PickerUserVO[]>([])
const selectedDepts = ref<PickerDeptVO[]>([])
const selectedRoles = ref<PickerRoleVO[]>([])

const pickerTenants = computed(() => {
  if (showTenantSelect.value) {
    return selectedTenants.value.filter((item) => item.id)
  }
  const tenantId = userStore.user?.tenantId
  return tenantId ? [{ id: tenantId, tenantName: '', tenantCode: '' }] : []
})

const multiTenantMode = computed(() => showTenantSelect.value && selectedTenants.value.length > 1)
const canPickRecipients = computed(() => pickerTenants.value.length > 0)

const tenantPickerVisible = ref(false)
const userPickerVisible = ref(false)
const deptPickerVisible = ref(false)
const rolePickerVisible = ref(false)

const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = reactive<SiteMessageSendPayload>({
  messageSubject: '',
  messageCategory: 'business',
  messageContent: '',
  recipientType: 'user',
  userIds: [],
  deptIds: [],
  roleIds: [],
})

const rules = computed<FormRules>(() => ({
  messageSubject: [
    { required: true, message: props.gf('messageSubject', 'placeholder'), trigger: 'blur' },
  ],
  messageCategory: [
    { required: true, message: props.gf('messageCategory', 'placeholder'), trigger: 'change' },
  ],
  messageContent: [
    { required: true, message: props.gf('messageContent', 'placeholder'), trigger: 'blur' },
  ],
  recipientType: [
    { required: true, message: props.gf('recipientType', 'placeholder'), trigger: 'change' },
  ],
}))

watch(
  () => props.modelValue,
  async (open) => {
    if (!open) return
    await reload()
    resetForm()
  },
)

watch(
  () => [...pickerTenants.value.map((item) => item.id ?? '')],
  () => {
    clearRecipientSelection()
  },
)

function resetForm() {
  form.messageSubject = ''
  form.messageCategory = 'business'
  form.messageContent = ''
  form.recipientType = 'user'
  selectedTenants.value = []
  clearRecipientSelection()
}

function clearRecipientSelection() {
  form.userIds = []
  form.deptIds = []
  form.roleIds = []
  selectedUsers.value = []
  selectedDepts.value = []
  selectedRoles.value = []
}

function formatUserTag(user: PickerUserVO) {
  const name = `${user.nickname || user.username}${user.username ? ` (${user.username})` : ''}`
  return user.tenantLabel && multiTenantMode.value ? `[${user.tenantLabel}] ${name}` : name
}

function formatDeptTag(dept: PickerDeptVO) {
  const name = dept.deptName || dept.id || ''
  return dept.tenantLabel && multiTenantMode.value ? `[${dept.tenantLabel}] ${name}` : name
}

function formatRoleTag(role: PickerRoleVO) {
  const name = role.roleName || role.roleCode || role.id || ''
  return role.tenantLabel && multiTenantMode.value ? `[${role.tenantLabel}] ${name}` : name
}

function handleTenantsPicked(tenants: TenantVO[]) {
  selectedTenants.value = tenants
}

function removeTenant(id: string) {
  selectedTenants.value = selectedTenants.value.filter((item) => item.id !== id)
}

function handleUsersPicked(users: PickerUserVO[]) {
  selectedUsers.value = users
  form.userIds = users.map((item) => item.id!).filter(Boolean)
}

function removeUser(id: string) {
  selectedUsers.value = selectedUsers.value.filter((item) => item.id !== id)
  form.userIds = selectedUsers.value.map((item) => item.id!).filter(Boolean)
}

function handleDeptsPicked(ids: string[], depts: PickerDeptVO[]) {
  form.deptIds = ids
  selectedDepts.value = depts
}

function removeDept(id: string) {
  form.deptIds = form.deptIds?.filter((item) => item !== id) ?? []
  selectedDepts.value = selectedDepts.value.filter((item) => item.id !== id)
}

function handleRolesPicked(roles: PickerRoleVO[]) {
  selectedRoles.value = roles
  form.roleIds = roles.map((item) => item.id!).filter(Boolean)
}

function removeRole(id: string) {
  selectedRoles.value = selectedRoles.value.filter((item) => item.id !== id)
  form.roleIds = selectedRoles.value.map((item) => item.id!).filter(Boolean)
}

function resolveSendTenants(): TenantVO[] {
  return pickerTenants.value
}

async function handleSubmit() {
  await formRef.value?.validate()
  const tenants = resolveSendTenants()
  if (showTenantSelect.value && !tenants.length) {
    ElMessage.warning(props.gp('targetTenantRequired'))
    return
  }
  if (form.recipientType === 'user' && !form.userIds?.length) {
    ElMessage.warning(props.gp('selectUsers'))
    return
  }
  if (form.recipientType === 'dept' && !form.deptIds?.length) {
    ElMessage.warning(props.gp('selectDepts'))
    return
  }
  if (form.recipientType === 'role' && !form.roleIds?.length) {
    ElMessage.warning(props.gp('selectRoles'))
    return
  }

  submitting.value = true
  try {
    const payload: SiteMessageSendPayload = {
      messageSubject: form.messageSubject,
      messageCategory: form.messageCategory,
      messageContent: form.messageContent,
      recipientType: form.recipientType,
      userIds: [...(form.userIds ?? [])],
      deptIds: [...(form.deptIds ?? [])],
      roleIds: [...(form.roleIds ?? [])],
    }
    if (showTenantSelect.value) {
      const tenantIds = tenants.map((item) => item.id!).filter(Boolean)
      if (tenantIds.length === 1) {
        payload.targetTenantId = tenantIds[0]
      } else {
        payload.targetTenantIds = tenantIds
      }
    }
    await sendSiteMessage(payload)
    ElMessage.success(props.gp('sendSuccess'))
    visible.value = false
    emit('success')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
@use '@/components/picker/picker.scss';

.form-tip {
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.5;
  color: var(--el-text-color-secondary);
}
</style>
