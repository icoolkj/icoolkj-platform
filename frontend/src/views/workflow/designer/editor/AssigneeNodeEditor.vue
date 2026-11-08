<template>
  <el-tabs v-model="activeTab" class="vfd-editor-tabs">
    <el-tab-pane :label="gp('designerTabBasic')" name="basic">
      <el-form-item :label="gp('designerNodeName')" prop="name">
        <el-input v-model="node.name" maxlength="100" />
      </el-form-item>
      <template v-if="isApprove">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item :label="gp('designerPassMode')">
              <el-radio-group v-model="node.passMode" class="pass-mode-group">
                <el-radio value="sequential">{{ gp('designerPassSequential') }}</el-radio>
                <el-radio value="sequentialAll">{{ gp('designerPassSequentialAll') }}</el-radio>
                <el-radio value="any">{{ gp('designerPassAny') }}</el-radio>
                <el-radio value="all">{{ gp('designerPassAll') }}</el-radio>
              </el-radio-group>
              <div
                v-if="
                  node.passMode === 'any' ||
                  node.passMode === 'all' ||
                  node.passMode === 'sequentialAll'
                "
                class="pass-mode-hint"
              >
                {{
                  node.passMode === 'sequentialAll'
                    ? gp('designerPassSequentialAllHint')
                    : gp('designerPassModeHint')
                }}
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="gp('designerRejectMode')">
              <el-radio-group v-model="node.rejectMode" class="pass-mode-group">
                <el-radio value="end">{{ gp('designerRejectModeEnd') }}</el-radio>
                <el-radio value="previous">{{ gp('designerRejectModePrevious') }}</el-radio>
                <el-radio value="starter">{{ gp('designerRejectModeStarter') }}</el-radio>
                <el-radio value="starterEdit">{{ gp('designerRejectModeStarterEdit') }}</el-radio>
              </el-radio-group>
              <p v-if="node.rejectMode === 'starterEdit'" class="pass-mode-hint">
                {{ gp('designerRejectModeStarterEditHint') }}
              </p>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item :label="gp('designerDueDuration')">
              <el-input
                v-model="node.dueDuration"
                maxlength="64"
                :placeholder="gp('designerDueDurationPh')"
              />
              <p class="pass-mode-hint">{{ gp('designerDueDurationHint') }}</p>
            </el-form-item>
          </el-col>
          <el-col v-if="node.dueDuration" :span="12">
            <el-form-item :label="gp('designerTimeoutAction')">
              <el-radio-group v-model="node.timeoutAction" class="pass-mode-group">
                <el-radio value="notify">{{ gp('designerTimeoutNotify') }}</el-radio>
                <el-radio value="rejectEnd">{{ gp('designerTimeoutRejectEnd') }}</el-radio>
                <el-radio value="autoApprove">{{ gp('designerTimeoutAutoApprove') }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <div class="vfd-switch-group">
          <label class="vfd-switch-item">
            <span>{{ gp('designerAllowTransfer') }}</span>
            <el-switch v-model="node.allowTransfer" />
          </label>
          <label class="vfd-switch-item">
            <span>{{ gp('designerAllowDelegate') }}</span>
            <el-switch v-model="node.allowDelegate" />
          </label>
          <label class="vfd-switch-item">
            <span>{{ gp('designerAllowAddSign') }}</span>
            <el-switch v-model="node.allowAddSign" />
          </label>
        </div>
      </template>
    </el-tab-pane>

    <el-tab-pane :label="gp('designerTabAssignee')" name="assignee">
      <el-form-item :label="gp('wizardStrategyType')" prop="strategyType">
        <el-select
          v-model="node.strategyType"
          style="width: 100%"
          @change="emit('strategy-change')"
        >
          <el-option
            v-for="item in strategyOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        v-if="node.strategyType === 'role' || node.strategyType === 'starter_dept_role'"
        :label="gp('designerRoleCodes')"
        required
      >
        <el-select
          v-model="roleCodes"
          multiple
          filterable
          collapse-tags
          collapse-tags-tooltip
          style="width: 100%"
          :placeholder="gp('designerRoleCodesPlaceholder')"
          @change="emit('sync-strategy')"
        >
          <el-option
            v-for="r in roleOptions"
            :key="r.roleCode"
            :label="`${r.roleName} (${r.roleCode})`"
            :value="r.roleCode"
          />
        </el-select>
        <div v-if="roleCodes.length" class="role-tags">
          <el-tag
            v-for="code in roleCodes"
            :key="code"
            size="small"
            closable
            class="role-tag"
            @close="removeRoleCode(code)"
          >
            {{ roleLabel(code) }}
          </el-tag>
        </div>
      </el-form-item>
      <el-form-item
        v-else-if="node.strategyType === 'fixed_user'"
        :label="gp('designerFixedUserId')"
        required
      >
        <div class="fixed-user-row">
          <el-input
            :model-value="fixedUserDisplay"
            readonly
            :placeholder="gp('designerFixedUserPickPlaceholder')"
          />
          <el-button @click="emit('open-user-picker')">{{ gp('designerFixedUserPick') }}</el-button>
          <el-button v-if="fixedUserId" link type="danger" @click="emit('clear-fixed-user')">
            {{ gp('designerFixedUserClear') }}
          </el-button>
        </div>
      </el-form-item>
      <template v-if="showExpressionStrategy">
        <el-form-item :label="gp('designerExprKind')">
          <el-select v-model="exprKind" style="width: 100%" @change="emit('sync-expr')">
            <el-option value="starter" :label="gp('designerExprStarter')" />
            <el-option value="variable" :label="gp('designerExprVariable')" />
            <el-option value="userId" :label="gp('designerExprUserId')" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="exprKind === 'variable'" :label="gp('designerExprVarName')" required>
          <el-input
            v-model="exprVarName"
            maxlength="64"
            :placeholder="gp('designerExprVarNamePh')"
            @change="emit('sync-expr')"
          />
        </el-form-item>
        <el-form-item v-if="exprKind === 'userId'" :label="gp('designerExprUserIdValue')" required>
          <el-input
            v-model="exprUserId"
            maxlength="20"
            :placeholder="gp('designerExprUserIdPh')"
            @change="emit('sync-expr')"
          />
        </el-form-item>
        <p class="pass-mode-hint">{{ gp('designerExprHint') }}</p>
      </template>
    </el-tab-pane>

    <el-tab-pane v-if="showFieldPermPanel" :label="gp('designerTabFieldPerm')" name="fieldPerm">
      <div class="field-perm-toolbar">
        <el-button link type="primary" @click="setAllFieldPerms('R')">
          {{ gp('designerFieldPermAllR') }}
        </el-button>
        <el-button link type="primary" @click="setAllFieldPerms('W')">
          {{ gp('designerFieldPermAllW') }}
        </el-button>
        <el-button link type="primary" @click="setAllFieldPerms('H')">
          {{ gp('designerFieldPermAllH') }}
        </el-button>
      </div>
      <el-table :data="formFields" size="small" max-height="360" border>
        <el-table-column :label="gp('designerFieldPermCode')" min-width="180">
          <template #default="{ row }">
            {{ row.fieldName || row.fieldCode }}
            <span class="field-perm-code">{{ row.fieldCode }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="gp('designerFieldPermValue')" width="240">
          <template #default="{ row }">
            <el-radio-group
              :model-value="fieldPermOf(row.fieldCode)"
              size="small"
              @update:model-value="
                (v: string | number | boolean | undefined) =>
                  setFieldPerm(row.fieldCode, v as 'R' | 'W' | 'H')
              "
            >
              <el-radio-button value="R">R</el-radio-button>
              <el-radio-button value="W">W</el-radio-button>
              <el-radio-button value="H">H</el-radio-button>
            </el-radio-group>
          </template>
        </el-table-column>
      </el-table>
      <p class="pass-mode-hint">{{ gp('designerFieldPermHint') }}</p>
    </el-tab-pane>

    <el-tab-pane v-if="showAdvancedKeys" :label="gp('designerTabAdvanced')" name="advanced">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item :label="gp('wizardTaskKey')" prop="taskDefinitionKey">
            <el-input
              v-model="node.taskDefinitionKey"
              maxlength="64"
              @blur="emit('sync-variable-from-key')"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item :label="gp('wizardVariableName')" prop="variableName">
            <el-input v-model="node.variableName" maxlength="64" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item :label="gp('designerResolveScope')" prop="resolveScope">
        <el-select v-model="node.resolveScope" style="width: 100%">
          <el-option
            v-for="item in resolveScopeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
    </el-tab-pane>
  </el-tabs>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { WorkflowFormFieldDef } from '@/types/workflow/formField'
import type { DictOption } from '@/types/system/dict'
import type { RoleVO } from '@/types/system/role'
import type { WorkflowDesignNode } from '@/types/workflow/design'
import { useDesignerGenI18n } from '@/composables/useDesignerGenI18n'

const node = defineModel<WorkflowDesignNode>({ required: true })
const activeTab = defineModel<string>('activeTab', { default: 'basic' })
const roleCodes = defineModel<string[]>('roleCodes', { default: () => [] })
const exprKind = defineModel<'starter' | 'variable' | 'userId'>('exprKind', {
  default: 'starter',
})
const exprVarName = defineModel<string>('exprVarName', { default: '' })
const exprUserId = defineModel<string>('exprUserId', { default: '' })

const props = defineProps<{
  formFields: WorkflowFormFieldDef[]
  roleOptions: RoleVO[]
  strategyOptions: DictOption[]
  resolveScopeOptions: DictOption[]
  fixedUserId: string
  fixedUserDisplay: string
  showFieldPermPanel: boolean
  showAdvancedKeys: boolean
}>()

const emit = defineEmits<{
  'strategy-change': []
  'sync-strategy': []
  'open-user-picker': []
  'clear-fixed-user': []
  'sync-expr': []
  'sync-variable-from-key': []
}>()

const { gp } = useDesignerGenI18n()

const isApprove = computed(() => (node.value.nodeType || 'approve') === 'approve')
const showExpressionStrategy = computed(() => node.value.strategyType === 'expression')

function roleLabel(code: string) {
  const hit = props.roleOptions.find((r) => r.roleCode === code)
  return hit ? `${hit.roleName} (${code})` : code
}

function removeRoleCode(code: string) {
  roleCodes.value = roleCodes.value.filter((c) => c !== code)
  emit('sync-strategy')
}

function fieldPermOf(fieldCode?: string): 'R' | 'W' | 'H' {
  if (!fieldCode || !node.value.fieldPerms) return 'R'
  const p = node.value.fieldPerms[fieldCode]
  return p === 'W' || p === 'H' ? p : 'R'
}

function setFieldPerm(fieldCode: string | undefined, perm: 'R' | 'W' | 'H') {
  if (!fieldCode) return
  node.value = {
    ...node.value,
    fieldPerms: { ...(node.value.fieldPerms || {}), [fieldCode]: perm },
  }
}

function setAllFieldPerms(perm: 'R' | 'W' | 'H') {
  const next: Record<string, 'R' | 'W' | 'H'> = {}
  for (const f of props.formFields) {
    if (f.fieldCode) next[f.fieldCode] = perm
  }
  node.value = { ...node.value, fieldPerms: next }
}
</script>

<style scoped lang="scss">
@use '@/styles/vertical-flow-editor.scss';
</style>
