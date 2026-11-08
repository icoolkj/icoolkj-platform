<!-- 竖向审批设计器：JSON 权威 + Flowable 部署；观感对标钉钉并强化层次 -->
<template>
  <div v-loading="loading" class="vfd">
    <header class="vfd-toolbar">
      <div class="vfd-toolbar-main">
        <div class="vfd-toolbar-leading">
          <div class="vfd-toolbar-title-row">
            <h3 class="vfd-title">{{ resolvedToolbarTitle }}</h3>
            <slot name="toolbar-tags" />
            <el-tooltip
              v-if="showHint"
              :content="gp('designerCanvasHint')"
              placement="bottom-start"
              :show-after="200"
            >
              <button type="button" class="vfd-help" :aria-label="gp('designerCanvasHint')">
                ?
              </button>
            </el-tooltip>
          </div>
          <div v-if="$slots.meta" class="vfd-toolbar-meta">
            <slot name="meta" />
          </div>
        </div>
        <div class="vfd-toolbar-actions">
          <el-button class="vfd-btn-ghost" @click="globalVisible = true">
            {{ gp('globalPropsTitle') }}
          </el-button>
          <el-button
            type="primary"
            class="vfd-btn-primary"
            :loading="saving"
            :disabled="!modelId || !nodes.length"
            v-hasPermi="[PERM_WorkflowModel.deploy]"
            @click="saveAndDeploy"
          >
            {{ gp('designerSaveDeploy') }}
          </el-button>
          <el-button
            link
            type="info"
            class="vfd-btn-advanced"
            :disabled="!modelId"
            v-hasPermi="[PERM_WorkflowModel.deploy]"
            @click="emit('open-xml')"
          >
            {{ gp('designerAdvancedXml') }}
          </el-button>
        </div>
      </div>
    </header>

    <div class="vfd-stage">
      <div class="vfd-flow">
        <!-- 发起 -->
        <div class="vfd-node vfd-node--start">
          <div class="vfd-node-hd">
            <span class="vfd-node-ico" aria-hidden="true">
              <el-icon :size="16"><UserFilled /></el-icon>
            </span>
            <span>{{ gp('designerStart') }}</span>
          </div>
          <div class="vfd-node-bd">{{ gp('designerStartHint') }}</div>
        </div>

        <AddNodeSlot @add="(t) => insertNode(0, t)" />

        <template v-for="(node, index) in nodes" :key="node.taskDefinitionKey + '-' + index">
          <!-- 条件 / 包容：多条件 + 默认，Π 分叉 / ⊔ 汇合连线闭合 -->
          <div
            v-if="node.nodeType === 'condition' || node.nodeType === 'inclusive'"
            class="vfd-fork"
            :class="{ 'vfd-fork--inclusive': node.nodeType === 'inclusive' }"
          >
            <div
              class="vfd-node vfd-fork-head"
              :class="node.nodeType === 'inclusive' ? 'vfd-node--inclusive' : 'vfd-node--condition'"
              role="button"
              tabindex="0"
              :title="gp('designerConditionPriorityTip')"
              @click="openEditorRoot(index)"
              @keydown.enter="openEditorRoot(index)"
            >
              <div class="vfd-node-hd">
                <span class="vfd-node-ico" aria-hidden="true">
                  <el-icon :size="16"><Share /></el-icon>
                </span>
                <span class="vfd-node-hd-title">{{ node.name || node.taskDefinitionKey }}</span>
                <button
                  type="button"
                  class="vfd-fork-add-inline"
                  @click.stop="addConditionBranch(index)"
                >
                  + {{ gp('designerAddConditionBranch') }}
                </button>
                <button
                  v-if="nodes.length > 1"
                  type="button"
                  class="vfd-node-del"
                  @click.stop="removeNode(index)"
                >
                  ×
                </button>
              </div>
              <div class="vfd-node-bd">
                <span class="vfd-node-assignee">{{ nodeSummary(node) }}</span>
              </div>
            </div>

            <div class="vfd-fork-stem" aria-hidden="true" />

            <div class="vfd-fork-scroll">
              <div class="vfd-fork-bundle">
                <div class="vfd-fork-rail vfd-fork-rail--split" aria-hidden="true" />

                <div class="vfd-fork-lanes">
                  <div
                    v-for="(branch, bIdx) in conditionBranches(node)"
                    :key="branch.id"
                    class="vfd-lane"
                    :class="{ 'vfd-lane--default': branch.isDefault }"
                  >
                    <div
                      class="vfd-lane-cap"
                      role="button"
                      tabindex="0"
                      @click="openEditorBranchCap(index, branch.id)"
                      @keydown.enter="openEditorBranchCap(index, branch.id)"
                    >
                      <div class="vfd-lane-cap-hd">
                        <span
                          v-if="branch.isDefault"
                          class="vfd-lane-badge vfd-lane-badge--default"
                        >
                          {{ gp('designerBranchDefaultHint') }}
                        </span>
                        <span class="vfd-lane-title">
                          {{
                            branch.isDefault
                              ? gp('designerBranchDefault')
                              : branch.name || `${gp('designerBranchYes')}${bIdx + 1}`
                          }}
                        </span>
                        <span
                          v-if="!branch.isDefault"
                          class="vfd-lane-pri"
                          :title="
                            gp('designerBranchPriority', { n: yesBranchIndex(node, branch.id) })
                          "
                        >
                          {{ yesBranchIndex(node, branch.id) }}
                        </span>
                        <span
                          v-if="!branch.isDefault && yesBranchCount(node) > 1"
                          class="vfd-lane-order"
                          @click.stop
                        >
                          <button
                            type="button"
                            class="vfd-lane-ord"
                            :disabled="yesBranchIndex(node, branch.id) <= 1"
                            :title="gp('designerBranchMoveLeft')"
                            @click="moveConditionBranchAt(index, branch.id, -1)"
                          >
                            ‹
                          </button>
                          <button
                            type="button"
                            class="vfd-lane-ord"
                            :disabled="yesBranchIndex(node, branch.id) >= yesBranchCount(node)"
                            :title="gp('designerBranchMoveRight')"
                            @click="moveConditionBranchAt(index, branch.id, 1)"
                          >
                            ›
                          </button>
                        </span>
                        <button
                          v-if="!branch.isDefault && yesBranchCount(node) > 1"
                          type="button"
                          class="vfd-lane-del"
                          @click.stop="removeConditionBranchAt(index, branch.id)"
                        >
                          ×
                        </button>
                      </div>
                      <span
                        v-if="!branch.isDefault"
                        class="vfd-lane-expr"
                        :class="{ 'is-empty': !branch.expression }"
                      >
                        {{ formatBranchCondition(branch.expression) }}
                      </span>
                      <span v-else class="vfd-lane-expr vfd-lane-expr--default">
                        {{ gp('designerBranchDefaultHint') }}
                      </span>
                    </div>

                    <AddNodeSlot
                      :allow-condition="false"
                      @add="(t) => insertBranchNode(index, branch.id, 0, t)"
                    />

                    <template
                      v-for="(child, cIdx) in branch.nodes"
                      :key="child.taskDefinitionKey + '-' + cIdx"
                    >
                      <div
                        class="vfd-node"
                        :class="`vfd-node--${child.nodeType || 'approve'}`"
                        role="button"
                        tabindex="0"
                        @click="openEditorBranch(index, branch.id, cIdx)"
                        @keydown.enter="openEditorBranch(index, branch.id, cIdx)"
                      >
                        <div class="vfd-node-hd">
                          <span class="vfd-node-ico" aria-hidden="true">
                            <el-icon v-if="(child.nodeType || 'approve') === 'cc'" :size="16">
                              <Promotion />
                            </el-icon>
                            <el-icon v-else-if="child.nodeType === 'delay'" :size="16">
                              <Timer />
                            </el-icon>
                            <el-icon v-else-if="child.nodeType === 'service'" :size="16">
                              <SetUp />
                            </el-icon>
                            <el-icon v-else-if="child.nodeType === 'subprocess'" :size="16">
                              <Folder />
                            </el-icon>
                            <el-icon v-else :size="16"><User /></el-icon>
                          </span>
                          <span class="vfd-node-hd-title">
                            {{ child.name || child.taskDefinitionKey }}
                          </span>
                          <button
                            type="button"
                            class="vfd-node-del"
                            @click.stop="removeBranchNode(index, branch.id, cIdx)"
                          >
                            ×
                          </button>
                        </div>
                        <div class="vfd-node-bd">
                          <span class="vfd-node-meta">{{ nodeTypeLabel(child.nodeType) }}</span>
                          <span class="vfd-node-assignee">{{ nodeSummary(child) }}</span>
                        </div>
                      </div>
                      <AddNodeSlot
                        :allow-condition="false"
                        @add="(t) => insertBranchNode(index, branch.id, cIdx + 1, t)"
                      />
                    </template>

                    <div class="vfd-lane-tail" aria-hidden="true" />
                  </div>
                </div>

                <div class="vfd-fork-rail vfd-fork-rail--join" aria-hidden="true" />
                <div class="vfd-fork-merge-stack">
                  <span class="vfd-fork-merge-wire" aria-hidden="true" />
                  <span class="vfd-fork-merge">{{ gp('designerBranchMerge') }}</span>
                  <span class="vfd-fork-merge-wire vfd-fork-merge-wire--out" aria-hidden="true" />
                </div>

                <div class="vfd-fork-actions">
                  <p v-if="conditionOverlapTip(node)" class="vfd-fork-tip vfd-fork-tip--warn">
                    {{ conditionOverlapTip(node) }}
                  </p>
                </div>
              </div>
            </div>
          </div>

          <!-- 并行：多路同时，Π / ⊔ 闭合 -->
          <div v-else-if="node.nodeType === 'parallel'" class="vfd-fork vfd-fork--parallel">
            <div
              class="vfd-node vfd-node--parallel vfd-fork-head"
              role="button"
              tabindex="0"
              @click="openEditorRoot(index)"
              @keydown.enter="openEditorRoot(index)"
            >
              <div class="vfd-node-hd">
                <span class="vfd-node-ico" aria-hidden="true">
                  <el-icon :size="16"><Connection /></el-icon>
                </span>
                <span class="vfd-node-hd-title">{{ node.name || node.taskDefinitionKey }}</span>
                <button
                  v-if="nodes.length > 1"
                  type="button"
                  class="vfd-node-del"
                  @click.stop="removeNode(index)"
                >
                  ×
                </button>
              </div>
              <div class="vfd-node-bd">
                <span class="vfd-node-assignee">{{ nodeSummary(node) }}</span>
              </div>
            </div>

            <div class="vfd-fork-stem" aria-hidden="true" />

            <div class="vfd-fork-scroll">
              <div class="vfd-fork-bundle">
                <div class="vfd-fork-rail vfd-fork-rail--split" aria-hidden="true" />

                <div class="vfd-fork-lanes">
                  <div
                    v-for="branch in parallelBranches(node)"
                    :key="branch.id"
                    class="vfd-lane vfd-lane--parallel"
                  >
                    <div class="vfd-lane-cap vfd-lane-cap--static">
                      <div class="vfd-lane-cap-hd">
                        <span class="vfd-lane-title">{{ branch.name }}</span>
                        <button
                          v-if="parallelBranches(node).length > 2"
                          type="button"
                          class="vfd-lane-del"
                          @click.stop="removeParallelBranchAt(index, branch.id)"
                        >
                          ×
                        </button>
                      </div>
                    </div>

                    <AddNodeSlot
                      :allow-condition="false"
                      @add="(t) => insertBranchNode(index, branch.id, 0, t)"
                    />

                    <template
                      v-for="(child, cIdx) in branch.nodes"
                      :key="child.taskDefinitionKey + '-' + cIdx"
                    >
                      <div
                        class="vfd-node"
                        :class="`vfd-node--${child.nodeType || 'approve'}`"
                        role="button"
                        tabindex="0"
                        @click="openEditorBranch(index, branch.id, cIdx)"
                        @keydown.enter="openEditorBranch(index, branch.id, cIdx)"
                      >
                        <div class="vfd-node-hd">
                          <span class="vfd-node-ico" aria-hidden="true">
                            <el-icon v-if="(child.nodeType || 'approve') === 'cc'" :size="16">
                              <Promotion />
                            </el-icon>
                            <el-icon v-else-if="child.nodeType === 'delay'" :size="16">
                              <Timer />
                            </el-icon>
                            <el-icon v-else-if="child.nodeType === 'service'" :size="16">
                              <SetUp />
                            </el-icon>
                            <el-icon v-else-if="child.nodeType === 'subprocess'" :size="16">
                              <Folder />
                            </el-icon>
                            <el-icon v-else :size="16"><User /></el-icon>
                          </span>
                          <span class="vfd-node-hd-title">
                            {{ child.name || child.taskDefinitionKey }}
                          </span>
                          <button
                            type="button"
                            class="vfd-node-del"
                            @click.stop="removeBranchNode(index, branch.id, cIdx)"
                          >
                            ×
                          </button>
                        </div>
                        <div class="vfd-node-bd">
                          <span class="vfd-node-meta">{{ nodeTypeLabel(child.nodeType) }}</span>
                          <span class="vfd-node-assignee">{{ nodeSummary(child) }}</span>
                        </div>
                      </div>
                      <AddNodeSlot
                        :allow-condition="false"
                        @add="(t) => insertBranchNode(index, branch.id, cIdx + 1, t)"
                      />
                    </template>

                    <div class="vfd-lane-tail" aria-hidden="true" />
                  </div>
                </div>

                <div class="vfd-fork-rail vfd-fork-rail--join" aria-hidden="true" />
                <div class="vfd-fork-merge-stack">
                  <span class="vfd-fork-merge-wire" aria-hidden="true" />
                  <span class="vfd-fork-merge">{{ gp('designerBranchMerge') }}</span>
                  <span class="vfd-fork-merge-wire vfd-fork-merge-wire--out" aria-hidden="true" />
                </div>

                <div class="vfd-fork-actions">
                  <button
                    type="button"
                    class="vfd-fork-add-link vfd-fork-add-link--par"
                    @click.stop="addParallelBranch(index)"
                  >
                    + {{ gp('designerAddParallelBranch') }}
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- 普通审批 / 抄送 -->
          <div
            v-else
            class="vfd-node"
            :class="`vfd-node--${node.nodeType || 'approve'}`"
            role="button"
            tabindex="0"
            @click="openEditorRoot(index)"
            @keydown.enter="openEditorRoot(index)"
          >
            <div class="vfd-node-hd">
              <span class="vfd-node-ico" aria-hidden="true">
                <el-icon v-if="(node.nodeType || 'approve') === 'cc'" :size="16">
                  <Promotion />
                </el-icon>
                <el-icon v-else-if="node.nodeType === 'delay'" :size="16"><Timer /></el-icon>
                <el-icon v-else-if="node.nodeType === 'service'" :size="16"><SetUp /></el-icon>
                <el-icon v-else-if="node.nodeType === 'subprocess'" :size="16"><Folder /></el-icon>
                <el-icon v-else :size="16"><User /></el-icon>
              </span>
              <span class="vfd-node-hd-title">{{ node.name || node.taskDefinitionKey }}</span>
              <button
                v-if="nodes.length > 1"
                type="button"
                class="vfd-node-del"
                @click.stop="removeNode(index)"
              >
                ×
              </button>
            </div>
            <div class="vfd-node-bd">
              <span class="vfd-node-meta">{{ nodeTypeLabel(node.nodeType) }}</span>
              <span class="vfd-node-assignee">{{ nodeSummary(node) }}</span>
            </div>
          </div>

          <AddNodeSlot @add="(t) => insertNode(index + 1, t)" />
        </template>

        <div class="vfd-end">{{ gp('designerEnd') }}</div>
      </div>
    </div>

    <GenFormShell
      v-model="editorVisible"
      :title="editorTitle"
      :width="860"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <el-form
        v-if="editing"
        ref="formRef"
        :model="editing"
        :rules="formRules"
        label-position="top"
        class="vfd-editor-form"
      >
        <AssigneeNodeEditor
          v-if="showAssigneeFields"
          v-model="editing"
          v-model:active-tab="editorTab"
          v-model:role-codes="roleCodes"
          v-model:expr-kind="exprKind"
          v-model:expr-var-name="exprVarName"
          v-model:expr-user-id="exprUserId"
          :form-fields="formFields"
          :role-options="roleOptions"
          :strategy-options="strategyOptions"
          :resolve-scope-options="resolveScopeOptions"
          :fixed-user-id="fixedUserId"
          :fixed-user-display="fixedUserDisplay"
          :show-field-perm-panel="showFieldPermPanel"
          :show-advanced-keys="showAdvancedKeys"
          @strategy-change="onStrategyChange"
          @sync-strategy="syncStrategyConfig"
          @open-user-picker="userPickerVisible = true"
          @clear-fixed-user="clearFixedUser"
          @sync-expr="syncExprFromUi"
          @sync-variable-from-key="syncVariableFromKey"
        />
        <ConditionBranchEditor
          v-else-if="editingConditionFields"
          v-model="editing"
          v-model:active-tab="editorTab"
          v-model:condition-mode="conditionMode"
          v-model:condition-field="conditionField"
          v-model:condition-op="conditionOp"
          v-model:condition-value="conditionValue"
          v-model:condition-value2="conditionValue2"
          :condition-fields="conditionFields"
          @rebuild="rebuildConditionExpr"
        />
        <DelayNodeEditor
          v-else-if="editingDelayFields"
          v-model="editing"
          v-model:active-tab="editorTab"
          @sync-variable-from-key="syncVariableFromKey"
        />
        <ServiceNodeEditor
          v-else-if="editingServiceFields"
          v-model="editing"
          v-model:active-tab="editorTab"
          @sync-variable-from-key="syncVariableFromKey"
        />
        <SubprocessNodeEditor
          v-else-if="editingSubprocessFields"
          v-model="editing"
          v-model:active-tab="editorTab"
          @sync-variable-from-key="syncVariableFromKey"
        />
        <GatewayRootEditor
          v-else-if="gatewayEditorVariant"
          v-model="editing"
          v-model:active-tab="editorTab"
          :variant="gatewayEditorVariant"
          @sync-variable-from-key="syncVariableFromKey"
        />
        <DefaultBranchEditor v-else-if="editTarget?.scope === 'branchCap'" v-model="editing" />
      </el-form>
      <template #footer>
        <el-button @click="editorVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="applyEditor">{{ gp('designerApplyNode') }}</el-button>
      </template>
    </GenFormShell>

    <UserPickerDialog
      v-model="userPickerVisible"
      :title="gp('designerFixedUserPick')"
      :selected="fixedUserSelected"
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
      @confirm="onFixedUserPicked"
    />

    <WorkflowGlobalPropsDialog
      v-model="globalVisible"
      :config="processConfig"
      @confirm="onGlobalConfirm"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import {
  Connection,
  Folder,
  Promotion,
  SetUp,
  Share,
  Timer,
  User,
  UserFilled,
} from '@element-plus/icons-vue'
import GenFormShell from '@/components/gen/layout/GenFormShell.vue'
import UserPickerDialog, { type PickerUserVO } from '@/components/picker/UserPickerDialog.vue'
import WorkflowGlobalPropsDialog from '@/views/workflow/designer/WorkflowGlobalPropsDialog.vue'
import AddNodeSlot from '@/views/workflow/designer/AddNodeSlot.vue'
import AssigneeNodeEditor from '@/views/workflow/designer/editor/AssigneeNodeEditor.vue'
import ConditionBranchEditor from '@/views/workflow/designer/editor/ConditionBranchEditor.vue'
import DelayNodeEditor from '@/views/workflow/designer/editor/DelayNodeEditor.vue'
import ServiceNodeEditor from '@/views/workflow/designer/editor/ServiceNodeEditor.vue'
import SubprocessNodeEditor from '@/views/workflow/designer/editor/SubprocessNodeEditor.vue'
import GatewayRootEditor from '@/views/workflow/designer/editor/GatewayRootEditor.vue'
import DefaultBranchEditor from '@/views/workflow/designer/editor/DefaultBranchEditor.vue'
import { useDesignAssigneeLabels } from '@/composables/useDesignAssigneeLabels'
import { useDesignerGenI18n } from '@/composables/useDesignerGenI18n'
import { useDict } from '@/composables/useDict'
import type { WorkflowFormFieldDef } from '@/types/workflow/formField'
import {
  applyAssigneeToNode,
  buildPersistProcessConfig as buildPersistCfg,
  collectAllTaskKeys,
  emptyProcessConfig,
  ensureConditionBranches,
  extractDesignNodes,
  extractDesignNodesFromJson,
  extractFieldLabelsFromJson,
  formatAssigneeSummary,
  formatConditionExpressionHuman,
  normalizeDesignNode,
  normalizePassMode,
  parseAssignee,
  runtimeProcessConfig,
  stripRuleId,
  validateConditionBranches,
  walkLeafNodes,
  type BranchLabelFns,
  type ConditionValidationIssue,
  type WorkflowDesignNode,
  type WorkflowDesignNodeType,
  type WorkflowProcessConfig,
} from '@/types/workflow/design'
import type { WorkflowApproverRuleVO } from '@/types/workflow/approverRule'
import {
  PERM_WorkflowModel,
  designDeployWorkflowModel,
  getWorkflowModel,
} from '@/api/workflow/model'
import { pageWorkflowApproverRule } from '@/api/workflow/approverRule'
import { pageRole } from '@/api/system/role'
import { getUser } from '@/api/system/user'
import type { RoleVO } from '@/types/system/role'
import { buildNodesFromRules } from '@/utils/workflow/bpmnLinearParse'
import {
  defaultApproveNode,
  defaultCcNode,
  defaultConditionNode,
  defaultDelayNode,
  defaultInclusiveNode,
  defaultParallelNode,
  defaultServiceNode,
  defaultSubprocessNode,
  type DesignNodeFactoryLabels,
} from '@/utils/workflow/designNodeFactory'
import {
  applyConditionBranchCap,
  branchCapNodeType,
  buildConditionExpression,
  ensureGatewayNode,
  getConditionBranches,
  getParallelBranches,
  insertConditionBranch,
  insertParallelBranch,
  isConditionLikeGateway,
  moveConditionBranch,
  parseConditionExpressionUi,
  preserveGatewayBranchesOnRootEdit,
  removeConditionBranch,
  removeParallelBranch,
  resolveGatewayKind,
  validateConditionGatewayForDeploy,
  validateParallelGatewayForDeploy,
  yesBranchPriority,
} from '@/utils/workflow/gateway'
import { isValidConditionRange } from '@/utils/workflow/gateway/conditionExpression'

const props = withDefaults(
  defineProps<{
    modelId?: string
    modelKey?: string
    conditionFields?: WorkflowFormFieldDef[]
    /** 全部表单字段（节点字段权限面板）；空则不展示 */
    formFields?: WorkflowFormFieldDef[]
    /** 工具栏主标题；空则用 designerCanvasTitle */
    toolbarTitle?: string
    /** 是否展示操作提示（改为标题旁 ? 气泡） */
    showHint?: boolean
  }>(),
  {
    showHint: true,
  },
)

const emit = defineEmits<{
  deployed: []
  'open-xml': []
}>()

const { t } = useI18n()
const { gp } = useDesignerGenI18n()
const resolvedToolbarTitle = computed(() => props.toolbarTitle?.trim() || gp('designerCanvasTitle'))
const { getDict } = useDict(['wf_approver_strategy', 'wf_approver_resolve_scope'])

const factoryLabels = computed<DesignNodeFactoryLabels>(() => ({
  approveFirst: gp('designerDefaultApproveFirst'),
  approveNth: (n) => gp('designerDefaultApproveNth').replace('{n}', String(n)),
  cc: gp('designerDefaultCc'),
  condition: gp('designerDefaultCondition'),
  parallel: gp('designerDefaultParallel'),
  inclusive: gp('designerDefaultInclusive'),
  delay: gp('designerDefaultDelay'),
  service: gp('designerDefaultService'),
  subprocess: gp('designerDefaultSubprocess'),
  branchYes: (n) => gp('designerBranchYesN').replace('{n}', String(n)),
  branchDefault: gp('designerBranchDefault'),
  parallelBranch: (n) => gp('designerParallelBranchN').replace('{n}', String(n)),
}))

const branchLabels = computed<BranchLabelFns>(() => ({
  yes: factoryLabels.value.branchYes,
  default: factoryLabels.value.branchDefault,
  parallel: factoryLabels.value.parallelBranch,
}))

const summaryLabels = computed(() => ({
  conditionBranches: (total: number, filled: number) =>
    gp('designerSummaryConditionBranches')
      .replace('{total}', String(total))
      .replace('{filled}', String(filled)),
  parallelPaths: (n: number) => gp('designerSummaryParallelPaths').replace('{n}', String(n)),
  passAny: gp('designerPassAnyShort'),
  passAll: gp('designerPassAllShort'),
  passSequentialAll: gp('designerPassSequentialAllShort'),
  listSep: gp('designerListSep'),
  starter: gp('designerStart'),
}))

const loading = ref(false)
const saving = ref(false)
const nodes = ref<WorkflowDesignNode[]>([])
const { resolvers: assigneeResolvers } = useDesignAssigneeLabels(nodes)
const processConfig = ref<WorkflowProcessConfig>(emptyProcessConfig())
const globalVisible = ref(false)
const editorVisible = ref(false)
const editorTab = ref('basic')
const editing = ref<WorkflowDesignNode | null>(null)
const formRef = ref<FormInstance>()
const roleOptions = ref<RoleVO[]>([])
const roleCodes = ref<string[]>([])
const fixedUserId = ref('')
const fixedUserLabel = ref('')
const userPickerVisible = ref(false)
const exprKind = ref<'starter' | 'variable' | 'userId'>('starter')
const exprVarName = ref('')
const exprUserId = ref('')
const conditionField = ref('')
const conditionOp = ref('>')
const conditionValue = ref('')
const conditionValue2 = ref('')
const conditionMode = ref<'compare' | 'range'>('compare')
const dirty = ref(false)

const fixedUserSelected = computed<PickerUserVO[]>(() =>
  fixedUserId.value
    ? [{ id: fixedUserId.value, nickname: fixedUserLabel.value } as PickerUserVO]
    : [],
)
const fixedUserDisplay = computed(() => {
  if (!fixedUserId.value) return ''
  return fixedUserLabel.value ? `${fixedUserLabel.value} (${fixedUserId.value})` : fixedUserId.value
})

/** 当前编辑目标：主链 / 分支内节点 / 某条条件本身 */
type EditTarget =
  | { scope: 'root'; index: number }
  | { scope: 'branch'; rootIndex: number; branchId: string; index: number }
  | { scope: 'branchCap'; rootIndex: number; branchId: string }

const editTarget = ref<EditTarget | null>(null)

const strategyOptions = computed(() => getDict('wf_approver_strategy'))
const resolveScopeOptions = computed(() => getDict('wf_approver_resolve_scope'))
const conditionFields = computed(() => props.conditionFields ?? [])
const formFields = computed(() => (props.formFields ?? []).filter((f) => !!f.fieldCode))
/** 已部署 design 快照里的字段名（无 OA 表单 props 时兜底） */
const designFieldLabels = ref<Record<string, string>>({})

/** 画布条件展示：表单字段名优先，其次 design 快照 */
function resolveConditionFieldLabels(): Record<string, string> {
  const map: Record<string, string> = { ...designFieldLabels.value }
  for (const f of formFields.value) {
    if (f.fieldCode && f.fieldName) map[f.fieldCode] = f.fieldName
  }
  for (const f of conditionFields.value) {
    if (f.fieldCode && f.fieldName) map[f.fieldCode] = f.fieldName
  }
  return map
}

const showFieldPermPanel = computed(() => {
  if (!formFields.value.length || !editing.value) return false
  const t = editing.value.nodeType || 'approve'
  return t === 'approve' || t === 'cc'
})

const editorTitle = computed(() => {
  if (editTarget.value?.scope === 'branchCap') {
    return `${gp('designerNodeEdit')} · ${editing.value?.name || gp('designerNodeCondition')}`
  }
  const type = editing.value?.nodeType || 'approve'
  return `${gp('designerNodeEdit')} · ${nodeTypeLabel(type)}`
})

const formRules = computed<FormRules>(() => {
  const type = editing.value?.nodeType || 'approve'
  const keyRules = [
    { required: true, message: gp('designerTaskKeyRequired'), trigger: 'blur' },
    {
      pattern: /^[a-zA-Z][a-zA-Z0-9_]{0,63}$/,
      message: gp('designerTaskKeyPattern'),
      trigger: 'blur',
    },
  ]
  if (
    editTarget.value?.scope === 'branchCap' ||
    type === 'condition' ||
    type === 'inclusive' ||
    type === 'parallel'
  ) {
    const rules: FormRules = {
      name: [{ required: true, message: gp('designerNodeNameRequired'), trigger: 'blur' }],
    }
    if (editTarget.value?.scope === 'branchCap') {
      const cap = editTarget.value
      const root = nodes.value[cap.rootIndex]
      const branch = conditionBranches(root).find((b) => b.id === cap.branchId)
      if (!branch?.isDefault) {
        rules.conditionExpression = [
          { required: true, message: gp('designerConditionRequired'), trigger: 'blur' },
        ]
      }
    } else {
      rules.taskDefinitionKey = keyRules
    }
    return rules
  }
  if (type === 'delay') {
    return {
      name: [{ required: true, message: gp('designerNodeNameRequired'), trigger: 'blur' }],
      taskDefinitionKey: keyRules,
      dueDuration: [
        { required: true, message: gp('designerDueDurationRequired'), trigger: 'blur' },
      ],
    }
  }
  if (type === 'service' || type === 'subprocess') {
    return {
      name: [{ required: true, message: gp('designerNodeNameRequired'), trigger: 'blur' }],
      taskDefinitionKey: keyRules,
      strategyConfig: [{ required: true, message: gp('designerConfigRequired'), trigger: 'blur' }],
    }
  }
  return {
    name: [{ required: true, message: gp('designerNodeNameRequired'), trigger: 'blur' }],
    taskDefinitionKey: keyRules,
    variableName: [
      { required: true, message: gp('designerVariableRequired'), trigger: 'blur' },
      {
        pattern: /^[a-zA-Z][a-zA-Z0-9_]{0,63}$/,
        message: gp('designerVariablePattern'),
        trigger: 'blur',
      },
    ],
    strategyType: [{ required: true, message: gp('designerStrategyRequired'), trigger: 'change' }],
    resolveScope: [{ required: true, message: gp('designerResolveRequired'), trigger: 'change' }],
  }
})

const editingConditionFields = computed(() => {
  const target = editTarget.value
  if (target?.scope !== 'branchCap') return false
  const root = nodes.value[target.rootIndex]
  const branch = conditionBranches(root).find((b) => b.id === target.branchId)
  return !branch?.isDefault
})

const showAssigneeFields = computed(() => {
  if (editTarget.value?.scope === 'branchCap') return false
  const t = editing.value?.nodeType || 'approve'
  return (
    t !== 'condition' &&
    t !== 'inclusive' &&
    t !== 'parallel' &&
    t !== 'delay' &&
    t !== 'service' &&
    t !== 'subprocess'
  )
})

const editingDelayFields = computed(
  () => editTarget.value?.scope !== 'branchCap' && editing.value?.nodeType === 'delay',
)
const editingServiceFields = computed(
  () => editTarget.value?.scope !== 'branchCap' && editing.value?.nodeType === 'service',
)
const editingSubprocessFields = computed(
  () => editTarget.value?.scope !== 'branchCap' && editing.value?.nodeType === 'subprocess',
)

const showAdvancedKeys = computed(() => editTarget.value?.scope !== 'branchCap')

const gatewayEditorVariant = computed((): 'condition' | 'inclusive' | 'parallel' | null => {
  if (editTarget.value?.scope !== 'root') return null
  return resolveGatewayKind(editing.value?.nodeType)
})

watch(
  () => props.modelId,
  (id) => {
    if (id) void reload()
    else {
      nodes.value = [normalizeDesignNode(defaultApproveNode(1, factoryLabels.value))]
      processConfig.value = emptyProcessConfig()
      designFieldLabels.value = {}
      dirty.value = false
    }
  },
  { immediate: true },
)

async function reload() {
  if (!props.modelId) return
  loading.value = true
  try {
    const [modelRes, rulesRes] = await Promise.all([
      getWorkflowModel(props.modelId),
      pageWorkflowApproverRule({ modelId: props.modelId, pageNo: 1, pageSize: 200 }),
    ])
    const rules = (rulesRes.data.data?.list ?? []) as WorkflowApproverRuleVO[]
    const detail = modelRes.data.data
    let snapshot: WorkflowDesignNode[] = extractDesignNodesFromJson(detail?.designJson)
    designFieldLabels.value = {
      ...extractFieldLabelsFromJson(detail?.designJson),
      ...extractFieldLabelsFromJson(detail?.processConfig),
    }
    try {
      if (detail?.processConfig) {
        const raw = JSON.parse(detail.processConfig) as WorkflowProcessConfig
        processConfig.value = {
          ...emptyProcessConfig(),
          ...runtimeProcessConfig(raw),
          startAcl: { ...emptyProcessConfig().startAcl, ...raw.startAcl },
          behavior: { ...emptyProcessConfig().behavior, ...raw.behavior },
          notify: { ...emptyProcessConfig().notify, ...raw.notify },
        }
        if (!snapshot.length) {
          snapshot = extractDesignNodes(raw)
        }
        if (raw.design?.fieldLabels) {
          designFieldLabels.value = {
            ...designFieldLabels.value,
            ...raw.design.fieldLabels,
          }
        }
      } else {
        processConfig.value = emptyProcessConfig()
      }
    } catch {
      processConfig.value = emptyProcessConfig()
    }

    if (snapshot.length) {
      nodes.value = mergeRuleIds(snapshot, rules)
    } else if (rules.length) {
      ElMessage.warning(gp('designerDesignSotFallback'))
      nodes.value = buildNodesFromRules(rules).map((n, i) => normalizeDesignNode(n, i))
    } else {
      ElMessage.warning(gp('designerDesignSotEmpty'))
      nodes.value = [normalizeDesignNode(defaultApproveNode(1, factoryLabels.value))]
    }
    dirty.value = false
    void ensureRoles()
  } finally {
    loading.value = false
  }
}

function mergeRuleIds(
  list: WorkflowDesignNode[],
  rules: WorkflowApproverRuleVO[],
): WorkflowDesignNode[] {
  const byKey = new Map(rules.map((r) => [r.taskDefinitionKey, r]))
  const mergeOne = (n: WorkflowDesignNode, i: number): WorkflowDesignNode => {
    const rule = byKey.get(n.taskDefinitionKey)
    let base = normalizeDesignNode(
      {
        ...n,
        strategyType: rule?.strategyType || n.strategyType,
        strategyConfig: rule?.strategyConfig || n.strategyConfig,
        variableName: rule?.variableName || n.variableName,
        resolveScope: rule?.resolveScope || n.resolveScope,
        ruleId: rule?.id,
      },
      i,
    )
    if (
      (base.nodeType === 'condition' ||
        base.nodeType === 'inclusive' ||
        base.nodeType === 'parallel') &&
      base.branches?.length
    ) {
      base = {
        ...base,
        branches: base.branches.map((b) => ({
          ...b,
          nodes: b.nodes.map((child, ci) => mergeOne(child, ci)),
        })),
      }
    }
    return base
  }
  return list.map((n, i) => mergeOne(n, i))
}

function conditionBranches(node: WorkflowDesignNode) {
  return getConditionBranches(node)
}

function parallelBranches(node: WorkflowDesignNode) {
  return getParallelBranches(node)
}

function gatewayRoot(node: WorkflowDesignNode): WorkflowDesignNode {
  return ensureGatewayNode(node)
}

function yesBranchCount(node: WorkflowDesignNode) {
  return conditionBranches(node).filter((b) => !b.isDefault).length
}

function yesBranchIndex(node: WorkflowDesignNode, branchId: string) {
  return yesBranchPriority(conditionBranches(node), branchId)
}

function addConditionBranch(rootIndex: number) {
  const { node, branchId } = insertConditionBranch(
    nodes.value[rootIndex],
    branchLabels.value,
    factoryLabels.value.branchYes(1),
  )
  nodes.value[rootIndex] = normalizeDesignNode(node, rootIndex)
  dirty.value = true
  void openEditorBranchCap(rootIndex, branchId)
}

function removeConditionBranchAt(rootIndex: number, branchId: string) {
  const result = removeConditionBranch(nodes.value[rootIndex], branchId, branchLabels.value)
  if (!result.ok) {
    if (result.reason === 'min_one') ElMessage.warning(gp('designerKeepOneCondition'))
    return
  }
  nodes.value[rootIndex] = normalizeDesignNode(result.node, rootIndex)
  dirty.value = true
}

/** 调整条件优先级（左高右低）：与 Flowable 排他网关出边顺序一致 */
function moveConditionBranchAt(rootIndex: number, branchId: string, dir: -1 | 1) {
  const next = moveConditionBranch(nodes.value[rootIndex], branchId, dir, branchLabels.value)
  if (!next) return
  nodes.value[rootIndex] = normalizeDesignNode(next, rootIndex)
  dirty.value = true
}

/** 排他条件严谨问题 → 文案（有问题则部署/应用节点会被拦截） */
function formatConditionIssue(issue: ConditionValidationIssue): string {
  switch (issue.code) {
    case 'empty_expression':
      return gp('designerConditionRequired')
    case 'duplicate_expression':
      return gp('designerConditionDupExpr', { expr: issue.expression })
    case 'invalid_range':
      return gp('designerConditionInvalidRange', {
        field: issue.field,
        low: issue.low,
        high: issue.high,
      })
    case 'unreachable_gt':
      return gp('designerConditionOverlapBlock', {
        field: issue.field,
        first: issue.earlier,
        second: issue.later,
        pri1: issue.earlierPri,
        pri2: issue.laterPri,
      })
    default:
      return gp('designerConditionRequired')
  }
}

function firstConditionIssue(node: WorkflowDesignNode): string {
  const issues = validateConditionBranches(conditionBranches(node))
  return issues.length ? formatConditionIssue(issues[0]) : ''
}

function conditionOverlapTip(node: WorkflowDesignNode): string {
  return firstConditionIssue(node)
}

function addParallelBranch(rootIndex: number) {
  const next = insertParallelBranch(
    nodes.value[rootIndex],
    branchLabels.value,
    factoryLabels.value.parallelBranch(1),
  )
  nodes.value[rootIndex] = normalizeDesignNode(next, rootIndex)
  dirty.value = true
}

function removeParallelBranchAt(rootIndex: number, branchId: string) {
  const result = removeParallelBranch(nodes.value[rootIndex], branchId, branchLabels.value)
  if (!result.ok) {
    if (result.reason === 'min_two') ElMessage.warning(gp('designerKeepOneParallel'))
    return
  }
  nodes.value[rootIndex] = normalizeDesignNode(result.node, rootIndex)
  dirty.value = true
}

function nodeTypeLabel(type?: string) {
  if (type === 'cc') return gp('designerNodeCc')
  if (type === 'condition') return gp('designerNodeCondition')
  if (type === 'inclusive') return gp('designerNodeInclusive')
  if (type === 'parallel') return gp('designerNodeParallel')
  if (type === 'delay') return gp('designerNodeDelay')
  if (type === 'service') return gp('designerNodeService')
  if (type === 'subprocess') return gp('designerNodeSubprocess')
  return gp('designerNodeApprove')
}

function fieldLabelOf(fieldCode: string) {
  const labels = resolveConditionFieldLabels()
  return labels[fieldCode] || fieldCode
}

/** 画布条件摘要：人读优先（字段中文名），失败回退 EL / 空态文案 */
function formatBranchCondition(expression?: string) {
  const expr = (expression || '').trim()
  if (!expr) return gp('designerConditionEmpty')
  return formatConditionExpressionHuman(expr, fieldLabelOf) || expr
}

function nodeSummary(node: WorkflowDesignNode) {
  if (isConditionLikeGateway(node.nodeType)) {
    const ensured = ensureConditionBranches(node)
    const yesList = (ensured.branches || []).filter((b) => !b.isDefault)
    if (!yesList.length) return gp('designerConditionEmpty')
    if (yesList.length === 1) return formatBranchCondition(yesList[0].expression)
    return (
      formatAssigneeSummary(node, assigneeResolvers.value, summaryLabels.value) ||
      gp('designerConditionEmpty')
    )
  }
  if (node.nodeType === 'parallel') {
    return formatAssigneeSummary(node, assigneeResolvers.value, summaryLabels.value) || '—'
  }
  return formatAssigneeSummary(node, assigneeResolvers.value, summaryLabels.value) || '—'
}

function clearFixedUser() {
  fixedUserId.value = ''
  fixedUserLabel.value = ''
  syncStrategyConfig()
}

function onFixedUserPicked(users: PickerUserVO[]) {
  const pick = users[users.length - 1]
  if (!pick?.id) {
    clearFixedUser()
    return
  }
  fixedUserId.value = pick.id
  fixedUserLabel.value = pick.nickname || pick.username || ''
  syncStrategyConfig()
}

async function resolveFixedUserLabel(userId: string) {
  if (!userId) {
    fixedUserLabel.value = ''
    return
  }
  try {
    const res = await getUser(userId)
    fixedUserLabel.value = res.data.data?.nickname || res.data.data?.username || ''
  } catch {
    fixedUserLabel.value = ''
  }
}

function createTypedNode(type: WorkflowDesignNodeType, hintIndex: number): WorkflowDesignNode {
  let node: WorkflowDesignNode
  const labels = factoryLabels.value
  if (type === 'cc') node = defaultCcNode(hintIndex, labels)
  else if (type === 'condition') node = defaultConditionNode(hintIndex, labels)
  else if (type === 'inclusive') node = defaultInclusiveNode(hintIndex, labels)
  else if (type === 'parallel') node = defaultParallelNode(hintIndex, labels)
  else if (type === 'delay') node = defaultDelayNode(hintIndex, labels)
  else if (type === 'service') node = defaultServiceNode(hintIndex, labels)
  else if (type === 'subprocess') node = defaultSubprocessNode(hintIndex, labels)
  else node = defaultApproveNode(hintIndex, labels)

  const suffix = Date.now().toString(36).slice(-4)
  if (type === 'condition') {
    node.taskDefinitionKey = `cond${suffix}`
    node.variableName = node.taskDefinitionKey
    node.name = gp('designerNewCondition')
  } else if (type === 'inclusive') {
    node.taskDefinitionKey = `incl${suffix}`
    node.variableName = node.taskDefinitionKey
    node.name = gp('designerNewInclusive')
  } else if (type === 'parallel') {
    node.taskDefinitionKey = `par${suffix}`
    node.variableName = node.taskDefinitionKey
    node.name = gp('designerNewParallel')
  } else if (type === 'delay') {
    node.taskDefinitionKey = `delay${suffix}`
    node.variableName = node.taskDefinitionKey
    node.name = gp('designerNewDelay')
  } else if (type === 'service') {
    node.taskDefinitionKey = `svc${suffix}`
    node.variableName = node.taskDefinitionKey
    node.name = gp('designerNewService')
    node.strategyType = 'expression'
    node.strategyConfig =
      '{"delegateExpression":"${workflowNoOpService.execute(execution)}"}'
  } else if (type === 'subprocess') {
    node.taskDefinitionKey = `sub${suffix}`
    node.variableName = node.taskDefinitionKey
    node.name = gp('designerNewSubprocess')
    node.strategyType = 'expression'
    node.strategyConfig = '{"calledElement":""}'
  } else if (type === 'cc') {
    node.taskDefinitionKey = `cc${suffix}`
    node.variableName = `${node.taskDefinitionKey}UserId`
    node.name = gp('designerNewCc')
  } else {
    node.taskDefinitionKey = `approve${suffix}`
    node.variableName = `${node.taskDefinitionKey}UserId`
    node.name = gp('designerNewApprove')
    node.passMode = 'sequential'
  }
  return normalizeDesignNode(node, hintIndex)
}

function insertNode(at: number, type: string) {
  const cmd = type as WorkflowDesignNodeType
  const node = createTypedNode(cmd, nodes.value.length + 1)
  nodes.value.splice(at, 0, node)
  renumberSort()
  dirty.value = true
  void openEditorRoot(at)
}

function insertBranchNode(rootIndex: number, branchId: string, at: number, type: string) {
  const cmd = type as WorkflowDesignNodeType
  if (
    cmd === 'condition' ||
    cmd === 'parallel' ||
    cmd === 'inclusive' ||
    cmd === 'delay' ||
    cmd === 'service' ||
    cmd === 'subprocess'
  ) {
    ElMessage.warning(gp('designerNoNestedCondition'))
    return
  }
  const root = gatewayRoot(nodes.value[rootIndex])
  const branches = (root.branches || []).map((b) => {
    if (b.id !== branchId) return b
    const child = createTypedNode(cmd, b.nodes.length + 1)
    const nodesCopy = [...b.nodes]
    nodesCopy.splice(at, 0, child)
    return { ...b, nodes: nodesCopy.map((n, i) => ({ ...n, sortNo: i + 1 })) }
  })
  nodes.value[rootIndex] = normalizeDesignNode({ ...root, branches }, rootIndex)
  dirty.value = true
  void openEditorBranch(rootIndex, branchId, at)
}

function removeNode(index: number) {
  if (nodes.value.length <= 1) {
    ElMessage.warning(gp('designerKeepOne'))
    return
  }
  nodes.value.splice(index, 1)
  renumberSort()
  dirty.value = true
}

function removeBranchNode(rootIndex: number, branchId: string, childIndex: number) {
  const root = gatewayRoot(nodes.value[rootIndex])
  const branches = (root.branches || []).map((b) => {
    if (b.id !== branchId) return b
    const nodesCopy = [...b.nodes]
    nodesCopy.splice(childIndex, 1)
    return { ...b, nodes: nodesCopy.map((n, i) => ({ ...n, sortNo: i + 1 })) }
  })
  nodes.value[rootIndex] = normalizeDesignNode({ ...root, branches }, rootIndex)
  dirty.value = true
}

function renumberSort() {
  nodes.value.forEach((n, i) => {
    n.sortNo = i + 1
  })
}

async function ensureRoles() {
  if (roleOptions.value.length) return
  const res = await pageRole({ pageNo: 1, pageSize: 200 })
  roleOptions.value = res.data.data?.list ?? []
}

async function openEditorRoot(index: number) {
  editTarget.value = { scope: 'root', index }
  await openEditorForNode(nodes.value[index])
}

async function openEditorBranch(rootIndex: number, branchId: string, index: number) {
  const root = gatewayRoot(nodes.value[rootIndex])
  const branch = root.branches?.find((b) => b.id === branchId)
  const child = branch?.nodes[index]
  if (!child) return
  editTarget.value = { scope: 'branch', rootIndex, branchId, index }
  await openEditorForNode(child)
}

async function openEditorBranchCap(rootIndex: number, branchId: string) {
  const root = ensureConditionBranches(nodes.value[rootIndex])
  const branch = root.branches?.find((b) => b.id === branchId)
  if (!branch) return
  editTarget.value = { scope: 'branchCap', rootIndex, branchId }
  editing.value = {
    nodeType: branchCapNodeType(root.nodeType),
    taskDefinitionKey: root.taskDefinitionKey,
    name: branch.name || (branch.isDefault ? gp('designerBranchDefault') : gp('designerBranchYes')),
    variableName: root.taskDefinitionKey,
    strategyType: 'expression',
    strategyConfig: '{}',
    resolveScope: 'biz_tenant',
    conditionExpression: branch.expression || '',
    sortNo: 1,
  }
  editorTab.value = conditionFields.value.length ? 'basic' : 'advanced'
  const ui = parseConditionExpressionUi(branch.expression)
  conditionMode.value = ui.mode
  conditionField.value = ui.field
  conditionOp.value = ui.op
  conditionValue.value = ui.value
  conditionValue2.value = ui.value2
  editorVisible.value = true
}

async function openEditorForNode(source: WorkflowDesignNode) {
  editing.value = normalizeDesignNode({ ...source, nodeType: source.nodeType || 'approve' }, 0)
  await ensureRoles()
  const assignee = parseAssignee(editing.value)
  roleCodes.value = [...(assignee.roleCodes ?? [])]
  fixedUserId.value = assignee.userId || ''
  fixedUserLabel.value = ''
  if (fixedUserId.value) void resolveFixedUserLabel(fixedUserId.value)
  loadExprUiFromNode(editing.value)
  conditionField.value = ''
  conditionOp.value = '>'
  conditionValue.value = ''
  editorTab.value = 'basic'
  editorVisible.value = true
}

function syncVariableFromKey() {
  if (!editing.value) return
  const type = editing.value.nodeType || 'approve'
  if (
    type === 'condition' ||
    type === 'inclusive' ||
    type === 'delay' ||
    type === 'service' ||
    type === 'subprocess'
  ) {
    return
  }
  const key = editing.value.taskDefinitionKey?.trim()
  if (key && (!editing.value.variableName || editing.value.variableName.endsWith('UserId'))) {
    editing.value.variableName = `${key}UserId`
  }
}

function onStrategyChange() {
  if (!editing.value) return
  if (editing.value.strategyType === 'role' || editing.value.strategyType === 'starter_dept_role') {
    if (!roleCodes.value.length) roleCodes.value = ['dept_approver']
    syncStrategyConfig()
  } else if (editing.value.strategyType === 'fixed_user') {
    syncStrategyConfig()
  } else if (editing.value.strategyType === 'expression') {
    exprKind.value = 'starter'
    exprVarName.value = ''
    exprUserId.value = ''
    syncExprFromUi()
    editorTab.value = 'assignee'
  }
}

function syncExprFromUi() {
  if (!editing.value || editing.value.strategyType !== 'expression') return
  let expr = 'starter'
  if (exprKind.value === 'variable') {
    const name = exprVarName.value.trim() || 'assigneeUserId'
    expr = `variable:${name}`
  } else if (exprKind.value === 'userId') {
    const uid = exprUserId.value.trim()
    expr = uid ? `userId:${uid}` : 'userId:'
  }
  editing.value = applyAssigneeToNode(editing.value, {
    strategyType: 'expression',
    expr,
    resolveScope: editing.value.resolveScope || 'biz_tenant',
  })
}

function loadExprUiFromNode(node: WorkflowDesignNode) {
  const assignee = parseAssignee(node)
  const raw = (assignee.expr || 'starter').trim()
  if (raw.startsWith('variable:')) {
    exprKind.value = 'variable'
    exprVarName.value = raw.slice('variable:'.length).trim()
    exprUserId.value = ''
  } else if (raw.startsWith('userId:')) {
    exprKind.value = 'userId'
    exprUserId.value = raw.slice('userId:'.length).trim()
    exprVarName.value = ''
  } else {
    exprKind.value = 'starter'
    exprVarName.value = ''
    exprUserId.value = ''
  }
}

function syncStrategyConfig() {
  if (!editing.value) return
  const st = editing.value.strategyType
  if (st === 'role' || st === 'starter_dept_role') {
    editing.value = applyAssigneeToNode(editing.value, {
      strategyType: st as 'role' | 'starter_dept_role',
      roleCodes: [...roleCodes.value],
      resolveScope: editing.value.resolveScope || 'biz_tenant',
    })
  } else if (st === 'fixed_user') {
    editing.value = applyAssigneeToNode(editing.value, {
      strategyType: 'fixed_user',
      userId: fixedUserId.value.trim(),
      resolveScope: editing.value.resolveScope || 'biz_tenant',
    })
  }
}

function rebuildConditionExpr() {
  if (!editing.value) return
  const built = buildConditionExpression({
    mode: conditionMode.value,
    field: conditionField.value,
    op: conditionOp.value,
    value: conditionValue.value,
    value2: conditionValue2.value,
  })
  if (built) editing.value.conditionExpression = built
}

function isKeyUsedElsewhere(key: string, target: EditTarget): boolean {
  if (target.scope === 'branchCap') return false
  const all = collectAllTaskKeys(nodes.value)
  let selfKey = ''
  if (target.scope === 'root') {
    selfKey = nodes.value[target.index]?.taskDefinitionKey || ''
  } else {
    const root = gatewayRoot(nodes.value[target.rootIndex])
    selfKey =
      root.branches?.find((b) => b.id === target.branchId)?.nodes[target.index]
        ?.taskDefinitionKey || ''
  }
  return all.some((k) => k === key && k !== selfKey)
}

async function applyEditor() {
  syncStrategyConfig()
  if (editing.value?.strategyType === 'expression') {
    syncExprFromUi()
  }
  rebuildConditionExpr()
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid || !editing.value || !editTarget.value) return

  const target = editTarget.value

  if (target.scope === 'branchCap') {
    const root = ensureConditionBranches({ ...nodes.value[target.rootIndex] })
    const isDefault = root.branches?.find((b) => b.id === target.branchId)?.isDefault
    const expr = (editing.value.conditionExpression || '').trim()
    if (!isDefault && !expr) {
      ElMessage.warning(gp('designerConditionRequired'))
      return
    }
    if (
      !isDefault &&
      !isValidConditionRange({
        mode: conditionMode.value,
        field: conditionField.value,
        op: conditionOp.value,
        value: conditionValue.value,
        value2: conditionValue2.value,
      })
    ) {
      const lo = Number(conditionValue.value)
      const hi = Number(conditionValue2.value)
      ElMessage.error(
        gp('designerConditionInvalidRange', {
          field: conditionField.value || 'field',
          low: lo,
          high: hi,
        }),
      )
      return
    }
    const result = applyConditionBranchCap({
      root,
      branchId: target.branchId,
      name: editing.value.name,
      expression: expr,
      labels: branchLabels.value,
    })
    if (!result.ok) {
      if (result.reason === 'empty_expression') {
        ElMessage.warning(gp('designerConditionRequired'))
      } else if (result.issue) {
        ElMessage.error(formatConditionIssue(result.issue))
      }
      return
    }
    nodes.value[target.rootIndex] = normalizeDesignNode(result.node, target.rootIndex)
    dirty.value = true
    editorVisible.value = false
    ElMessage.success(gp('designerNodeApplied'))
    return
  }

  if (
    (editing.value.nodeType || 'approve') !== 'condition' &&
    editing.value.nodeType !== 'inclusive'
  ) {
    if (
      (editing.value.strategyType === 'role' ||
        editing.value.strategyType === 'starter_dept_role') &&
      !roleCodes.value.length
    ) {
      ElMessage.warning(gp('designerRoleRequired'))
      return
    }
    if (editing.value.strategyType === 'fixed_user' && !fixedUserId.value.trim()) {
      ElMessage.warning(gp('designerFixedUserRequired'))
      return
    }
  }
  const key = editing.value.taskDefinitionKey.trim()
  if (isKeyUsedElsewhere(key, target)) {
    ElMessage.warning(gp('designerTaskKeyDup'))
    return
  }

  const next = normalizeDesignNode(
    {
      ...editing.value,
      taskDefinitionKey: key,
      variableName: editing.value.variableName?.trim() || key,
    },
    0,
  )

  if (target.scope === 'root') {
    nodes.value[target.index] = normalizeDesignNode(
      preserveGatewayBranchesOnRootEdit(nodes.value[target.index], {
        ...next,
        passMode: normalizePassMode(next.passMode),
      }),
      target.index,
    )
  } else {
    const root = gatewayRoot(nodes.value[target.rootIndex])
    const branches = (root.branches || []).map((b) => {
      if (b.id !== target.branchId) return b
      const kids = [...b.nodes]
      kids[target.index] = normalizeDesignNode(
        { ...next, passMode: normalizePassMode(next.passMode) },
        target.index,
      )
      return { ...b, nodes: kids }
    })
    nodes.value[target.rootIndex] = normalizeDesignNode({ ...root, branches }, target.rootIndex)
  }

  renumberSort()
  dirty.value = true
  editorVisible.value = false
  ElMessage.success(gp('designerNodeApplied'))
}

function onGlobalConfirm(cfg: WorkflowProcessConfig) {
  processConfig.value = {
    ...emptyProcessConfig(),
    startAcl: { ...emptyProcessConfig().startAcl, ...cfg.startAcl },
    behavior: { ...emptyProcessConfig().behavior, ...cfg.behavior },
    notify: { ...emptyProcessConfig().notify, ...cfg.notify },
  }
  dirty.value = true
}

function persistConfigJson(): string {
  return JSON.stringify(
    buildPersistCfg(processConfig.value, nodes.value, resolveConditionFieldLabels()),
  )
}

function validateTreeForDeploy(): string | null {
  for (const n of nodes.value) {
    const conditionIssue = validateConditionGatewayForDeploy(n)
    if (conditionIssue === 'no_yes_branch') return gp('designerConditionRequired')
    if (conditionIssue) return formatConditionIssue(conditionIssue)
    if (validateParallelGatewayForDeploy(n) === 'min_two') {
      return gp('designerKeepOneParallel')
    }
    if (n.nodeType === 'delay' && !n.dueDuration?.trim()) {
      return gp('designerDueDurationRequired')
    }
    if (n.nodeType === 'service' || n.nodeType === 'subprocess') {
      if (!n.strategyConfig?.trim()) return gp('designerConfigRequired')
      try {
        JSON.parse(n.strategyConfig)
      } catch {
        return gp('designerConfigInvalid')
      }
    }
  }
  let incomplete = false
  walkLeafNodes(nodes.value, (leaf) => {
    const type = leaf.nodeType || 'approve'
    if (type === 'delay' || type === 'service' || type === 'subprocess') return
    if (!leaf.strategyType || !leaf.strategyConfig) {
      incomplete = true
      return
    }
    try {
      JSON.parse(leaf.strategyConfig)
    } catch {
      incomplete = true
    }
  })
  if (incomplete) return gp('designerNodeIncomplete')
  return null
}

async function saveAndDeploy() {
  if (!props.modelId || !nodes.value.length) return
  const err = validateTreeForDeploy()
  if (err) {
    ElMessage.error(err)
    return
  }
  const payloadNodes = nodes.value.map((n, i) =>
    stripRuleId(normalizeDesignNode({ ...n, nodeType: n.nodeType || 'approve' }, i)),
  )
  saving.value = true
  try {
    await designDeployWorkflowModel({
      modelId: props.modelId,
      nodes: payloadNodes,
      processConfig: persistConfigJson(),
      remark: 'vertical design',
    })
    ElMessage.success(gp('designerDeployed'))
    try {
      const rulesRes = await pageWorkflowApproverRule({
        modelId: props.modelId,
        pageNo: 1,
        pageSize: 200,
      })
      const rules = (rulesRes.data.data?.list ?? []) as WorkflowApproverRuleVO[]
      nodes.value = mergeRuleIds(payloadNodes, rules)
      const modelRes = await getWorkflowModel(props.modelId)
      if (modelRes.data.data?.processConfig) {
        const raw = JSON.parse(modelRes.data.data.processConfig) as WorkflowProcessConfig
        processConfig.value = {
          ...emptyProcessConfig(),
          ...runtimeProcessConfig(raw),
          startAcl: { ...emptyProcessConfig().startAcl, ...raw.startAcl },
          behavior: { ...emptyProcessConfig().behavior, ...raw.behavior },
          notify: { ...emptyProcessConfig().notify, ...raw.notify },
        }
      }
    } catch {
      /* keep canvas */
    }
    dirty.value = false
    emit('deployed')
  } finally {
    saving.value = false
  }
}

defineExpose({ reload, isDirty: () => dirty.value })
</script>

<style src="@/styles/vertical-flow-theme.css"></style>
<style scoped>
.vfd-stage {
  min-height: 420px;
}

.vfd-toolbar {
  margin-bottom: 12px;
  padding: 12px 14px;
  border: 1px solid #e6d2b8;
  border-radius: 12px;
  background: linear-gradient(180deg, #fffcf9 0%, #faf4ec 100%);
}

.vfd-toolbar-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.vfd-toolbar-leading {
  min-width: 0;
  flex: 1;
}

.vfd-toolbar-title-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.vfd-title {
  margin: 0;
  font-size: 15px;
  font-weight: 650;
  letter-spacing: -0.01em;
  color: #54483c;
  line-height: 1.3;
}

.vfd-help {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 1px solid #d9b88a;
  background: #fff;
  color: #a67a42;
  font-size: 11px;
  font-weight: 700;
  line-height: 1;
  cursor: help;
  padding: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.vfd-help:hover {
  border-color: #e8a35c;
  color: #d4924a;
}

.vfd-toolbar-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px 8px;
  margin-top: 8px;
}

.vfd-toolbar-meta :deep(.el-tag) {
  border-radius: 6px;
}

.vfd-toolbar-meta__label {
  font-size: 12px;
  color: #9a8570;
}

.vfd-toolbar-meta__sep {
  width: 1px;
  height: 12px;
  background: #e2cbb0;
  margin: 0 2px;
}

.vfd-toolbar-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  flex-shrink: 0;
}

.vfd-btn-advanced {
  color: #9a8570 !important;
}

.vfd-btn-ghost {
  --el-button-bg-color: #fffcf9;
  --el-button-border-color: #e6d2b8;
  --el-button-hover-bg-color: #faf4ec;
  --el-button-hover-border-color: #e8a35c;
  --el-button-text-color: #7a6550;
}

.vfd-btn-primary {
  --el-button-bg-color: #e8a35c;
  --el-button-border-color: #e8a35c;
  --el-button-hover-bg-color: #d4924a;
  --el-button-hover-border-color: #d4924a;
}

.vfd-flow {
  --vfd-node-w: min(300px, 92%);
  --vfd-wire-w: 2.5px;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.vfd-node {
  width: var(--vfd-node-w);
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition:
    transform 0.15s ease,
    box-shadow 0.15s ease;
}

.vfd-node:hover {
  transform: translateY(-1px);
  box-shadow:
    0 2px 6px rgb(180 120 60 / 12%),
    0 10px 22px rgb(180 120 60 / 14%);
}

.vfd-node:focus-visible {
  outline: 2px solid #d4924a;
  outline-offset: 2px;
}

.vfd-node-hd {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
}

.vfd-fork-add-inline {
  border: 1px solid rgb(255 255 255 / 55%);
  background: rgb(255 255 255 / 18%);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  padding: 2px 8px;
  border-radius: 4px;
  white-space: nowrap;
  flex-shrink: 0;
  margin-left: auto;
}

.vfd-fork-add-inline:hover {
  background: rgb(255 255 255 / 28%);
}

.vfd-node-ico {
  display: inline-flex;
  width: 22px;
  height: 22px;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  background: rgb(255 255 255 / 22%);
  flex-shrink: 0;
}

.vfd-node-hd-title {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.vfd-node-del {
  border: none;
  background: rgb(255 255 255 / 18%);
  color: #fff;
  width: 22px;
  height: 22px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  line-height: 1;
  flex-shrink: 0;
}

.vfd-node-del:hover {
  background: rgb(255 255 255 / 32%);
}

.vfd-node-bd {
  padding: 12px 14px 14px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-height: 52px;
}

.vfd-node-meta {
  font-size: 11px;
  letter-spacing: 0.02em;
}

.vfd-node-assignee {
  font-size: 13px;
  line-height: 1.45;
  word-break: break-word;
}

.vfd-node--start {
  cursor: default;
}

.vfd-node--start:hover {
  transform: none;
}

.vfd-end {
  margin-top: 4px;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.04em;
}

.vfd-fork {
  --lane-w: 176px;
  --lane-gap: 16px;
  --vfd-wire-w: 2.5px;
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.vfd-fork-head {
  width: var(--vfd-node-w);
}

.vfd-fork-stem {
  width: var(--vfd-wire-w);
  height: 18px;
  margin-bottom: -1px;
  background: var(--vfd-wire);
  border-radius: 999px;
  flex-shrink: 0;
}

.vfd-fork-scroll {
  width: 100%;
  overflow-x: auto;
  display: flex;
  justify-content: center;
  padding: 0 16px;
  box-sizing: border-box;
}

.vfd-fork-bundle {
  display: inline-flex;
  flex-direction: column;
  align-items: stretch;
  min-width: min-content;
  position: relative;
  margin: 0 auto;
}

/* 分叉/汇合横梁：宽度随车道数拉伸，两端对齐首尾车道中心 */
.vfd-fork-rail {
  position: relative;
  width: 100%;
  height: 18px;
  flex-shrink: 0;
}

.vfd-fork-rail::before {
  content: '';
  position: absolute;
  left: calc(var(--lane-w) / 2);
  right: calc(var(--lane-w) / 2);
  top: 0;
  height: var(--vfd-wire-w);
  background: var(--vfd-wire);
  border-radius: 999px;
}

.vfd-fork-rail--split::after {
  content: '';
  position: absolute;
  left: calc(50% - var(--vfd-wire-w) / 2);
  top: calc(-1 * 18px + 1px);
  width: var(--vfd-wire-w);
  height: 18px;
  background: var(--vfd-wire);
  border-radius: 999px;
}

.vfd-fork-rail--join {
  height: 18px;
  margin-top: -1px;
}

.vfd-fork-rail--join::before {
  top: 0;
}

/* 汇合横梁中心下贯到「汇合」徽章 */
.vfd-fork-rail--join::after {
  content: '';
  position: absolute;
  left: calc(50% - var(--vfd-wire-w) / 2);
  top: 0;
  width: var(--vfd-wire-w);
  height: 18px;
  background: var(--vfd-wire);
  border-radius: 999px;
}

/* 操作区放汇合下方居中，避免右侧绝对定位把视觉中心拽歪 */
.vfd-fork-actions {
  position: static;
  width: 100%;
  max-width: 280px;
  margin: 8px auto 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  z-index: 2;
}

.vfd-fork-add-link {
  border: none;
  background: transparent;
  color: #a8846a;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  padding: 2px 6px;
  border-radius: 4px;
  white-space: nowrap;
}

.vfd-fork-add-link:hover {
  background: rgb(212 165 116 / 14%);
}

.vfd-fork-add-link--par {
  color: #b87f7f;
}

.vfd-fork-add-link--par:hover {
  background: rgb(196 144 144 / 12%);
}

.vfd-fork-tip {
  margin: 0;
  max-width: 240px;
  font-size: 11px;
  line-height: 1.4;
  color: #a8846a;
  text-align: center;
}

.vfd-fork-tip--warn {
  color: #de350b;
  font-weight: 550;
}

.vfd-lane-order {
  display: inline-flex;
  gap: 2px;
  flex-shrink: 0;
}

.vfd-lane-ord {
  border: none;
  background: rgb(122 175 138 / 16%);
  color: #6b9a78;
  width: 18px;
  height: 18px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  line-height: 1;
  padding: 0;
}

.vfd-lane-ord:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.vfd-lane-ord:not(:disabled):hover {
  background: rgb(122 175 138 / 28%);
}

.vfd-lane--parallel .vfd-lane-cap {
  background: #fffdf9;
}

.vfd-lane-cap--static {
  cursor: default;
  padding: 6px 10px;
}

.vfd-lane-cap--static:hover {
  border-color: rgb(124 58 237 / 40%);
}

.vfd-fork-lanes {
  display: flex;
  justify-content: center;
  align-items: stretch;
  gap: var(--lane-gap);
  width: max-content;
  max-width: 100%;
  margin: 0 auto;
  padding-top: 0;
}

.vfd-lane {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: var(--lane-w);
  flex: 0 0 var(--lane-w);
  position: relative;
  min-height: 56px;
}

/* 车道顶：接分叉横梁 → 车道中心 */
.vfd-lane::before {
  content: '';
  position: absolute;
  top: -18px;
  left: calc(50% - var(--vfd-wire-w) / 2);
  width: var(--vfd-wire-w);
  height: 18px;
  background: var(--vfd-wire);
  border-radius: 999px;
  z-index: 0;
}

.vfd-lane-cap {
  width: 100%;
  margin-bottom: 2px;
  padding: 6px 8px;
  border-radius: 6px;
  background: #fffcf8;
  border: 1px solid rgb(122 175 138 / 28%);
  text-align: left;
  cursor: pointer;
  transition:
    border-color 0.12s ease,
    box-shadow 0.12s ease;
}

.vfd-lane-cap:hover {
  border-color: rgb(122 175 138 / 48%);
  box-shadow: 0 1px 4px rgb(120 90 60 / 8%);
}

.vfd-lane--default .vfd-lane-cap:hover {
  border-color: rgb(229 212 192 / 95%);
  box-shadow: none;
}

.vfd-lane-cap-hd {
  display: flex;
  align-items: center;
  gap: 4px;
}

.vfd-lane-cap:has(.vfd-lane-expr) .vfd-lane-cap-hd {
  margin-bottom: 2px;
}

.vfd-lane-title {
  font-size: 12px;
  font-weight: 650;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.vfd-lane-pri {
  flex-shrink: 0;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 700;
  line-height: 16px;
  text-align: center;
}

.vfd-lane-del {
  border: none;
  background: transparent;
  color: #a8846a;
  width: 18px;
  height: 18px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  line-height: 1;
  flex-shrink: 0;
  opacity: 0;
}

.vfd-lane-cap:hover .vfd-lane-del,
.vfd-lane-cap:focus-within .vfd-lane-del {
  opacity: 1;
}

.vfd-lane-del:hover {
  background: rgb(222 53 11 / 10%);
  color: #de350b;
}

.vfd-lane-hint {
  display: block;
  font-size: 11px;
  color: #a8846a;
  line-height: 1.4;
}

.vfd-lane-expr {
  display: block;
  font-size: 12px;
  color: #7a5a42;
  word-break: break-word;
  line-height: 1.35;
}

.vfd-lane-expr.is-empty {
  color: #de350b;
  font-weight: 600;
}

.vfd-lane-expr--default {
  font-style: italic;
}

.vfd-lane-badge {
  display: inline-flex;
  align-items: center;
  padding: 0 6px;
  height: 18px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  flex-shrink: 0;
}

.vfd-lane .vfd-node {
  width: 100%;
}

.vfd-lane-tail {
  flex: 1 1 0;
  width: var(--vfd-wire-w);
  min-height: 28px;
  margin-top: -1px;
  margin-bottom: -1px;
  background: var(--vfd-wire);
  align-self: center;
  border-radius: 999px;
  z-index: 0;
}

.vfd-lane :deep(.add-slot) {
  width: 100%;
}

/* 汇合徽章串在中心竖线上，线上下贯通，再接到主链「+」 */
.vfd-fork-merge-stack {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: -1px;
  width: 100%;
}

.vfd-fork-merge-wire {
  width: var(--vfd-wire-w);
  height: 14px;
  background: var(--vfd-wire);
  border-radius: 999px;
}

.vfd-fork-merge-wire--out {
  height: 12px;
}

.vfd-fork-merge {
  margin: -1px 0;
  padding: 3px 12px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
  border: 1px solid transparent;
  line-height: 1.4;
  position: relative;
  z-index: 1;
  background: var(--vfd-merge-bg, #faf4ec);
  box-shadow: 0 0 0 3px var(--vfd-wire-halo, #f7efe4);
}

/* 网关后紧挨 AddNodeSlot：竖线视觉闭合 */
.vfd-fork + :deep(.add-slot) {
  margin-top: -2px;
  min-height: 40px;
  width: var(--vfd-node-w);
}
</style>
