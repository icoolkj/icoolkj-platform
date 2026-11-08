<template>
  <GenFormShell
    v-model="visible"
    :title="gp('globalPropsTitle')"
    :width="860"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
  >
    <el-alert type="info" :closable="false" show-icon class="mb12">
      <template #title>{{ gp('globalPropsPersistTip') }}</template>
    </el-alert>

    <el-tabs v-model="activeTab">
      <el-tab-pane :label="gp('globalPropsAcl')" name="acl">
        <el-alert type="warning" :closable="false" show-icon class="mb12">
          <template #title>{{ gp('globalPropsAclTip') }}</template>
        </el-alert>
        <el-form label-width="108px">
          <el-form-item :label="gp('globalPropsRoles')">
            <div class="picker-trigger">
              <el-button type="primary" plain @click="rolePickerVisible = true">
                {{ gp('globalPropsPick') }}
              </el-button>
              <div class="picker-trigger__tags">
                <el-tag
                  v-for="item in selectedRoles"
                  :key="item.roleCode || item.id"
                  closable
                  @close="removeRole(item.roleCode!)"
                >
                  {{ formatRoleTag(item) }}
                </el-tag>
                <span v-if="!selectedRoles.length" class="picker-trigger__placeholder">
                  {{ gp('globalPropsRolesPlaceholder') }}
                </span>
              </div>
            </div>
          </el-form-item>
          <el-form-item :label="gp('globalPropsPosts')">
            <div class="picker-trigger">
              <el-button type="primary" plain @click="postPickerVisible = true">
                {{ gp('globalPropsPick') }}
              </el-button>
              <div class="picker-trigger__tags">
                <el-tag
                  v-for="item in selectedPosts"
                  :key="item.id"
                  closable
                  @close="removePost(item.id!)"
                >
                  {{ formatPostTag(item) }}
                </el-tag>
                <span v-if="!selectedPosts.length" class="picker-trigger__placeholder">
                  {{ gp('globalPropsPostsPlaceholder') }}
                </span>
              </div>
            </div>
          </el-form-item>
          <el-form-item :label="gp('globalPropsUsers')">
            <div class="picker-trigger">
              <el-button type="primary" plain @click="userPickerVisible = true">
                {{ gp('globalPropsPick') }}
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
                  {{ gp('globalPropsUsersPlaceholder') }}
                </span>
              </div>
            </div>
          </el-form-item>
          <el-form-item :label="gp('globalPropsDepts')">
            <div class="picker-trigger">
              <el-button type="primary" plain @click="deptPickerVisible = true">
                {{ gp('globalPropsPick') }}
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
                  {{ gp('globalPropsDeptsPlaceholder') }}
                </span>
              </div>
            </div>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane v-if="isPlatformTenant" :label="gp('globalPropsTenantScope')" name="tenant">
        <el-alert type="info" :closable="false" show-icon class="mb12">
          <template #title>{{ gp('globalPropsTenantScopeTip') }}</template>
        </el-alert>
        <el-form label-width="108px">
          <el-form-item :label="gp('globalPropsTenantIds')">
            <el-select
              v-model="local.startAcl!.tenantIds"
              multiple
              filterable
              clearable
              disabled
              style="width: 100%"
              :placeholder="gp('globalPropsTenantIdsPlaceholder')"
            />
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane :label="gp('globalPropsBehavior')" name="behavior">
        <el-form label-width="120px">
          <el-form-item :label="gp('globalPropsTitleTpl')">
            <el-input
              v-model="local.behavior!.titleTemplate"
              type="textarea"
              :rows="2"
              placeholder="{starter} / {processName} - {startTime}"
            />
          </el-form-item>
          <el-form-item :label="gp('globalPropsSummaryTpl')">
            <el-input
              v-model="local.behavior!.summaryTemplate"
              type="textarea"
              :rows="2"
              placeholder="{starter} / {processName} - {startTime}"
            />
          </el-form-item>
          <el-form-item :label="gp('globalPropsAllowWithdraw')">
            <el-switch v-model="local.behavior!.allowWithdraw" />
          </el-form-item>
          <el-form-item :label="gp('globalPropsCommentRequired')">
            <el-switch v-model="local.behavior!.commentRequired" />
          </el-form-item>
          <el-form-item :label="gp('globalPropsAutoDedup')">
            <el-switch v-model="local.behavior!.autoDedup" />
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane :label="gp('globalPropsNotify')" name="notify">
        <el-form label-width="140px">
          <el-form-item :label="gp('notifyOnReturn')">
            <el-switch v-model="local.notify!.onReturn" />
          </el-form-item>
          <el-form-item :label="gp('notifyOnReject')">
            <el-switch v-model="local.notify!.onReject" />
          </el-form-item>
          <el-form-item :label="gp('notifyOnTodo')">
            <el-switch v-model="local.notify!.onTodo" />
          </el-form-item>
          <el-form-item :label="gp('notifyOnCc')">
            <el-switch v-model="local.notify!.onCc" />
          </el-form-item>
          <el-form-item :label="gp('notifyOnComplete')">
            <el-switch v-model="local.notify!.onComplete" />
          </el-form-item>
          <el-form-item :label="gp('notifyOnTimeout')">
            <el-switch v-model="local.notify!.onTimeout" />
          </el-form-item>
          <el-form-item :label="gp('notifyOnUrge')">
            <el-switch v-model="local.notify!.onUrge" />
            <p class="global-props-hint">{{ gp('notifyOnUrgeHint') }}</p>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>

    <UserPickerDialog
      v-model="userPickerVisible"
      :title="gp('globalPropsPickUsers')"
      :selected="selectedUsers"
      :pending-title="gp('pickerPendingList')"
      :selected-text="gp('pickerSelected')"
      :add-current-text="gp('pickerAddCurrentPage')"
      :clear-text="gp('pickerClearSelected')"
      :search-placeholder="gp('pickerUserSearch')"
      :search-text="t('common.search')"
      :reset-text="t('common.reset')"
      :cancel-text="t('common.cancel')"
      :confirm-text="t('common.confirm')"
      :action-label="t('common.actions')"
      :count-unit="gp('pickerCountUnit')"
      :dept-title="gp('pickerDeptTree')"
      :nickname-label="gp('pickerUserNickname')"
      :username-label="gp('pickerUserAccount')"
      @confirm="onUsersPicked"
    />
    <DeptPickerDialog
      v-model="deptPickerVisible"
      :title="gp('globalPropsPickDepts')"
      :selected-ids="local.startAcl?.deptIds ?? []"
      :tree-title="gp('pickerDeptTree')"
      :name-label="gp('pickerDeptName')"
      :selected-text="gp('pickerSelected')"
      :clear-text="gp('pickerClearSelected')"
      :cancel-text="t('common.cancel')"
      :confirm-text="t('common.confirm')"
      :action-label="t('common.actions')"
      @confirm="onDeptsPicked"
    />
    <RolePickerDialog
      v-model="rolePickerVisible"
      :title="gp('globalPropsPickRoles')"
      :selected="selectedRoles"
      :role-list-title="gp('pickerRoleList')"
      :selected-text="gp('pickerSelected')"
      :add-current-text="gp('pickerAddCurrentPage')"
      :clear-text="gp('pickerClearSelected')"
      :search-placeholder="gp('pickerRoleSearch')"
      :search-text="t('common.search')"
      :reset-text="t('common.reset')"
      :cancel-text="t('common.cancel')"
      :confirm-text="t('common.confirm')"
      :action-label="t('common.actions')"
      :count-unit="gp('pickerCountUnit')"
      :name-label="gp('pickerRoleName')"
      :code-label="gp('pickerRoleCode')"
      @confirm="onRolesPicked"
    />
    <PostPickerDialog
      v-model="postPickerVisible"
      :title="gp('globalPropsPickPosts')"
      :selected="selectedPosts"
      :post-list-title="gp('pickerPostList')"
      :selected-text="gp('pickerSelected')"
      :add-current-text="gp('pickerAddCurrentPage')"
      :clear-text="gp('pickerClearSelected')"
      :search-placeholder="gp('pickerPostSearch')"
      :search-text="t('common.search')"
      :reset-text="t('common.reset')"
      :cancel-text="t('common.cancel')"
      :confirm-text="t('common.confirm')"
      :action-label="t('common.actions')"
      :count-unit="gp('pickerCountUnit')"
      :name-label="gp('pickerPostName')"
      :code-label="gp('pickerPostCode')"
      @confirm="onPostsPicked"
    />

    <template #footer>
      <el-button @click="visible = false">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" @click="confirm">{{ t('common.confirm') }}</el-button>
    </template>
  </GenFormShell>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import GenFormShell from '@/components/gen/layout/GenFormShell.vue'
import { useDesignerGenI18n } from '@/composables/useDesignerGenI18n'
import { emptyProcessConfig, type WorkflowProcessConfig } from '@/types/workflow/design'
import { pageRole } from '@/api/system/role'
import { pagePost } from '@/api/system/post'
import { pageDept } from '@/api/system/dept'
import { pageUser } from '@/api/system/user'
import type { RoleVO } from '@/types/system/role'
import type { PostVO } from '@/types/system/post'
import { useUserStore } from '@/stores/user'
import UserPickerDialog, { type PickerUserVO } from '@/components/picker/UserPickerDialog.vue'
import DeptPickerDialog, { type PickerDeptVO } from '@/components/picker/DeptPickerDialog.vue'
import RolePickerDialog, { type PickerRoleVO } from '@/components/picker/RolePickerDialog.vue'
import PostPickerDialog, { type PickerPostVO } from '@/components/picker/PostPickerDialog.vue'
import { isPlatformTenant as matchPlatformTenant, STATUS_NORMAL } from '@/constants/flags'

const props = defineProps<{
  modelValue: boolean
  config: WorkflowProcessConfig
}>()

const emit = defineEmits<{
  'update:modelValue': [boolean]
  confirm: [config: WorkflowProcessConfig]
}>()

const { t } = useI18n()
const userStore = useUserStore()
const { gp } = useDesignerGenI18n()

const isPlatformTenant = computed(() => matchPlatformTenant(userStore.user?.tenantId))

const visible = ref(false)
const activeTab = ref('acl')
const local = reactive<WorkflowProcessConfig>(emptyProcessConfig())

const userPickerVisible = ref(false)
const deptPickerVisible = ref(false)
const rolePickerVisible = ref(false)
const postPickerVisible = ref(false)

const selectedUsers = ref<PickerUserVO[]>([])
const selectedDepts = ref<PickerDeptVO[]>([])
const selectedRoles = ref<PickerRoleVO[]>([])
const selectedPosts = ref<PickerPostVO[]>([])

watch(
  () => props.modelValue,
  async (open) => {
    visible.value = open
    if (open) {
      Object.assign(local, emptyProcessConfig(), clonePlain(props.config || {}))
      local.startAcl = {
        ...emptyProcessConfig().startAcl,
        ...props.config?.startAcl,
        postIds: props.config?.startAcl?.postIds ?? [],
        tenantIds: props.config?.startAcl?.tenantIds ?? [],
      }
      local.behavior = { ...emptyProcessConfig().behavior, ...props.config?.behavior }
      local.notify = { ...emptyProcessConfig().notify, ...props.config?.notify }
      activeTab.value = 'acl'
      await hydrateSelections()
    }
  },
)

watch(visible, (v) => emit('update:modelValue', v))

async function hydrateSelections() {
  const roleCodes = local.startAcl?.roleCodes ?? []
  const postIds = local.startAcl?.postIds ?? []
  const userIds = local.startAcl?.userIds ?? []
  const deptIds = local.startAcl?.deptIds ?? []

  const [rolesRes, postsRes] = await Promise.all([
    roleCodes.length ? pageRole({ pageNo: 1, pageSize: 200 }) : Promise.resolve(null),
    postIds.length ? pagePost({ pageNo: 1, pageSize: 200, status: STATUS_NORMAL }) : Promise.resolve(null),
  ])
  const roleList = (rolesRes?.data.data?.list ?? []) as RoleVO[]
  const postList = (postsRes?.data.data?.list ?? []) as PostVO[]

  selectedRoles.value = roleCodes.map((code) => {
    const hit = roleList.find((r) => r.roleCode === code)
    return hit ? { ...hit } : ({ roleCode: code, roleName: code } as PickerRoleVO)
  })
  selectedPosts.value = postIds.map((id) => {
    const hit = postList.find((p) => p.id === id)
    return hit ? { ...hit } : ({ id, postName: id } as PickerPostVO)
  })

  // users / depts：按 ID 拉一页匹配，未命中则保留 ID 占位标签
  if (userIds.length) {
    const userRes = await pageUser({ pageNo: 1, pageSize: 200, status: STATUS_NORMAL })
    const users = userRes.data.data?.list ?? []
    selectedUsers.value = userIds.map((id) => {
      const hit = users.find((u) => u.id === id)
      return hit ? { ...hit } : ({ id } as PickerUserVO)
    })
  } else {
    selectedUsers.value = []
  }

  if (deptIds.length) {
    const deptRes = await pageDept({ pageNo: 1, pageSize: 500 })
    const depts = deptRes.data.data?.list ?? []
    selectedDepts.value = deptIds.map((id) => {
      const hit = depts.find((d) => d.id === id)
      return hit ? { ...hit } : ({ id, deptName: id } as PickerDeptVO)
    })
  } else {
    selectedDepts.value = []
  }
}

function formatRoleTag(item: PickerRoleVO) {
  if (item.roleName && item.roleCode) return `${item.roleName} (${item.roleCode})`
  return item.roleName || item.roleCode || '—'
}

function formatPostTag(item: PickerPostVO) {
  if (item.postName && item.postCode) return `${item.postName} (${item.postCode})`
  return item.postName || item.postCode || item.id || '—'
}

function formatUserTag(item: PickerUserVO) {
  if (item.nickname) return item.username ? `${item.nickname} (${item.username})` : item.nickname
  return item.username || item.id || '—'
}

function formatDeptTag(item: PickerDeptVO) {
  return item.deptName || item.id || '—'
}

function syncAclFromSelections() {
  local.startAcl!.roleCodes = selectedRoles.value.map((r) => r.roleCode!).filter(Boolean)
  local.startAcl!.postIds = selectedPosts.value.map((p) => p.id!).filter(Boolean)
  local.startAcl!.userIds = selectedUsers.value.map((u) => u.id!).filter(Boolean)
  local.startAcl!.deptIds = selectedDepts.value.map((d) => d.id!).filter(Boolean)
}

function onUsersPicked(rows: PickerUserVO[]) {
  selectedUsers.value = [...rows]
  syncAclFromSelections()
}

function onDeptsPicked(ids: string[], rows: PickerDeptVO[]) {
  selectedDepts.value = rows.length
    ? [...rows]
    : ids.map((id) => ({ id, deptName: id }) as PickerDeptVO)
  syncAclFromSelections()
}

function onRolesPicked(rows: PickerRoleVO[]) {
  selectedRoles.value = [...rows]
  syncAclFromSelections()
}

function onPostsPicked(rows: PickerPostVO[]) {
  selectedPosts.value = [...rows]
  syncAclFromSelections()
}

function removeRole(code: string) {
  selectedRoles.value = selectedRoles.value.filter((r) => r.roleCode !== code)
  syncAclFromSelections()
}

function removePost(id: string) {
  selectedPosts.value = selectedPosts.value.filter((p) => p.id !== id)
  syncAclFromSelections()
}

function removeUser(id: string) {
  selectedUsers.value = selectedUsers.value.filter((u) => u.id !== id)
  syncAclFromSelections()
}

function removeDept(id: string) {
  selectedDepts.value = selectedDepts.value.filter((d) => d.id !== id)
  syncAclFromSelections()
}

function clonePlain<T>(value: T): T {
  return JSON.parse(JSON.stringify(value ?? null)) as T
}

function confirm() {
  syncAclFromSelections()
  emit('confirm', {
    startAcl: clonePlain(local.startAcl),
    behavior: clonePlain(local.behavior),
    notify: clonePlain(local.notify),
  })
  visible.value = false
}
</script>

<style scoped lang="scss">
@use '@/components/picker/picker.scss';

.mb12 {
  margin-bottom: 12px;
}

.global-props-hint {
  margin: 6px 0 0;
  font-size: 12px;
  line-height: 1.4;
  color: var(--el-text-color-secondary);
}
</style>
