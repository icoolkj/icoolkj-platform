<!-- 审批详情只读流程图：复用设计器竖向拓扑，叠加 progressNodes 当前节点标识 -->
<template>
  <div class="vfv">
    <div v-if="nodes.length" class="vfd-stage">
      <div class="vfd-flow">
        <div class="vfd-node vfd-node--start">
          <div class="vfd-node-hd">
            <span class="vfd-node-ico" aria-hidden="true">
              <el-icon :size="16"><UserFilled /></el-icon>
            </span>
            <span>{{ L.start }}</span>
          </div>
          <div class="vfd-node-bd">{{ L.startHint }}</div>
        </div>

        <span class="vfd-link" aria-hidden="true" />

        <template v-for="(node, index) in nodes" :key="node.taskDefinitionKey + '-' + index">
          <!-- 条件 / 包容 -->
          <div
            v-if="node.nodeType === 'condition' || node.nodeType === 'inclusive'"
            class="vfd-fork"
            :class="{ 'vfd-fork--inclusive': node.nodeType === 'inclusive' }"
          >
            <div
              class="vfd-node vfd-fork-head"
              :class="[
                node.nodeType === 'inclusive' ? 'vfd-node--inclusive' : 'vfd-node--condition',
                statusClass(node.taskDefinitionKey),
              ]"
            >
              <div class="vfd-node-hd">
                <span class="vfd-node-ico" aria-hidden="true">
                  <el-icon :size="16"><Share /></el-icon>
                </span>
                <span class="vfd-node-hd-title">{{ displayName(node) }}</span>
                <el-tag
                  v-if="isActive(node.taskDefinitionKey)"
                  size="small"
                  effect="dark"
                  class="vfd-current-tag"
                >
                  {{ L.current }}
                </el-tag>
              </div>
              <div class="vfd-node-bd">
                <span class="vfd-node-meta">{{ nodeTypeLabel(node.nodeType) }}</span>
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
                    <div class="vfd-lane-cap vfd-lane-cap--static">
                      <div class="vfd-lane-cap-hd">
                        <span
                          v-if="branch.isDefault"
                          class="vfd-lane-badge vfd-lane-badge--default"
                        >
                          {{ L.branchDefaultHint }}
                        </span>
                        <span class="vfd-lane-title">
                          {{
                            branch.isDefault
                              ? L.branchDefault
                              : branch.name || `${L.branchYes}${bIdx + 1}`
                          }}
                        </span>
                        <span v-if="!branch.isDefault" class="vfd-lane-pri">
                          {{ yesBranchIndex(node, branch.id) }}
                        </span>
                      </div>
                      <span
                        v-if="!branch.isDefault"
                        class="vfd-lane-expr"
                        :class="{ 'is-empty': !branch.expression }"
                      >
                        {{ formatBranchCondition(branch.expression) }}
                      </span>
                      <span v-else class="vfd-lane-expr vfd-lane-expr--default">
                        {{ L.branchDefaultHint }}
                      </span>
                    </div>

                    <template
                      v-for="(child, cIdx) in branch.nodes || []"
                      :key="child.taskDefinitionKey + '-' + cIdx"
                    >
                      <span v-if="cIdx > 0" class="vfd-link" aria-hidden="true" />
                      <div
                        class="vfd-node"
                        :class="[
                          `vfd-node--${child.nodeType || 'approve'}`,
                          statusClass(child.taskDefinitionKey),
                        ]"
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
                          <span class="vfd-node-hd-title">{{ displayName(child) }}</span>
                          <el-tag
                            v-if="isActive(child.taskDefinitionKey)"
                            size="small"
                            effect="dark"
                            class="vfd-current-tag"
                          >
                            {{ L.current }}
                          </el-tag>
                        </div>
                        <div class="vfd-node-bd">
                          <span class="vfd-node-meta">{{ nodeTypeLabel(child.nodeType) }}</span>
                          <span class="vfd-node-assignee">{{ leafSummary(child) }}</span>
                        </div>
                      </div>
                    </template>

                    <div class="vfd-lane-tail" aria-hidden="true" />
                  </div>
                </div>
                <div class="vfd-fork-rail vfd-fork-rail--join" aria-hidden="true" />
                <div class="vfd-fork-merge-stack">
                  <span class="vfd-fork-merge-wire" aria-hidden="true" />
                  <span class="vfd-fork-merge">{{ L.branchMerge }}</span>
                  <span class="vfd-fork-merge-wire vfd-fork-merge-wire--out" aria-hidden="true" />
                </div>
              </div>
            </div>
          </div>

          <!-- 并行 -->
          <div v-else-if="node.nodeType === 'parallel'" class="vfd-fork vfd-fork--parallel">
            <div
              class="vfd-node vfd-node--parallel vfd-fork-head"
              :class="statusClass(node.taskDefinitionKey)"
            >
              <div class="vfd-node-hd">
                <span class="vfd-node-ico" aria-hidden="true">
                  <el-icon :size="16"><Connection /></el-icon>
                </span>
                <span class="vfd-node-hd-title">{{ displayName(node) }}</span>
                <el-tag
                  v-if="isActive(node.taskDefinitionKey)"
                  size="small"
                  effect="dark"
                  class="vfd-current-tag"
                >
                  {{ L.current }}
                </el-tag>
              </div>
              <div class="vfd-node-bd">
                <span class="vfd-node-meta">{{ nodeTypeLabel(node.nodeType) }}</span>
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
                      </div>
                    </div>

                    <template
                      v-for="(child, cIdx) in branch.nodes || []"
                      :key="child.taskDefinitionKey + '-' + cIdx"
                    >
                      <span v-if="cIdx > 0" class="vfd-link" aria-hidden="true" />
                      <div
                        class="vfd-node"
                        :class="[
                          `vfd-node--${child.nodeType || 'approve'}`,
                          statusClass(child.taskDefinitionKey),
                        ]"
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
                          <span class="vfd-node-hd-title">{{ displayName(child) }}</span>
                          <el-tag
                            v-if="isActive(child.taskDefinitionKey)"
                            size="small"
                            effect="dark"
                            class="vfd-current-tag"
                          >
                            {{ L.current }}
                          </el-tag>
                        </div>
                        <div class="vfd-node-bd">
                          <span class="vfd-node-meta">{{ nodeTypeLabel(child.nodeType) }}</span>
                          <span class="vfd-node-assignee">{{ leafSummary(child) }}</span>
                        </div>
                      </div>
                    </template>

                    <div class="vfd-lane-tail" aria-hidden="true" />
                  </div>
                </div>
                <div class="vfd-fork-rail vfd-fork-rail--join" aria-hidden="true" />
                <div class="vfd-fork-merge-stack">
                  <span class="vfd-fork-merge-wire" aria-hidden="true" />
                  <span class="vfd-fork-merge">{{ L.branchMerge }}</span>
                  <span class="vfd-fork-merge-wire vfd-fork-merge-wire--out" aria-hidden="true" />
                </div>
              </div>
            </div>
          </div>

          <!-- 普通节点 -->
          <div
            v-else
            class="vfd-node"
            :class="[
              `vfd-node--${node.nodeType || 'approve'}`,
              statusClass(node.taskDefinitionKey),
            ]"
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
              <span class="vfd-node-hd-title">{{ displayName(node) }}</span>
              <el-tag
                v-if="isActive(node.taskDefinitionKey)"
                size="small"
                effect="dark"
                class="vfd-current-tag"
              >
                {{ L.current }}
              </el-tag>
            </div>
            <div class="vfd-node-bd">
              <span class="vfd-node-meta">{{ nodeTypeLabel(node.nodeType) }}</span>
              <span class="vfd-node-assignee">{{ leafSummary(node) }}</span>
            </div>
          </div>

          <span v-if="index < nodes.length - 1" class="vfd-link" aria-hidden="true" />
        </template>

        <span class="vfd-link" aria-hidden="true" />
        <div class="vfd-end">{{ L.end }}</div>
      </div>
    </div>
    <el-empty v-else :description="emptyLabel" :image-size="64" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
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
import { useDesignAssigneeLabels } from '@/composables/useDesignAssigneeLabels'
import { useGenI18n } from '@/composables/useGenI18n'
import type { GenI18nMessages } from '@/types/tool/genI18n'
import genI18nZhCN from '@/i18n/gen/workflowFlowView/zh-CN.json'
import genI18nEnUS from '@/i18n/gen/workflowFlowView/en-US.json'
import {
  ensureConditionBranches,
  ensureParallelBranches,
  extractDesignNodesFromJson,
  extractFieldLabelsFromJson,
  formatAssigneeSummary,
  formatConditionExpressionHuman,
  normalizeDesignNode,
  type WorkflowDesignNode,
} from '@/types/workflow/design'
import type { WorkflowProgressNodeVO } from '@/types/workflow/instance'

const props = defineProps<{
  designJson?: string | null
  progressNodes?: WorkflowProgressNodeVO[]
  emptyText?: string
  /** fieldCode → 展示名；优先生效，其次读 designJson.fieldLabels */
  fieldLabels?: Record<string, string> | null
}>()

const GEN_I18N_FILES: Record<string, GenI18nMessages> = {
  'zh-CN': genI18nZhCN as GenI18nMessages,
  'en-US': genI18nEnUS as GenI18nMessages,
}
const { gp } = useGenI18n('workflowFlowView', 'workflow', GEN_I18N_FILES)

const L = computed(() => ({
  start: gp('start'),
  startHint: gp('startHint'),
  end: gp('end'),
  current: gp('current'),
  empty: gp('empty'),
  approve: gp('approve'),
  cc: gp('cc'),
  condition: gp('condition'),
  inclusive: gp('inclusive'),
  parallel: gp('parallel'),
  delay: gp('delay'),
  service: gp('service'),
  subprocess: gp('subprocess'),
  branchYes: gp('branchYes'),
  branchDefault: gp('branchDefault'),
  branchDefaultHint: gp('branchDefaultHint'),
  branchMerge: gp('branchMerge'),
  conditionEmpty: gp('conditionEmpty'),
  pass: gp('pass'),
  reject: gp('reject'),
  pending: gp('pending'),
  passAny: gp('passAny'),
  passAll: gp('passAll'),
  passSequential: gp('passSequential'),
  starter: gp('starter'),
  conditionBranches: gp('conditionBranches'),
  parallelPaths: gp('parallelPaths'),
  listSep: gp('listSep'),
}))

const emptyLabel = computed(() => props.emptyText || L.value.empty)

const nodes = computed(() =>
  extractDesignNodesFromJson(props.designJson).map((n, i) => normalizeDesignNode(n, i)),
)

const conditionFieldLabels = computed(() => ({
  ...extractFieldLabelsFromJson(props.designJson),
  ...(props.fieldLabels || {}),
}))

const { resolvers: assigneeResolvers } = useDesignAssigneeLabels(nodes)

const progressByKey = computed(() => {
  const map = new Map<string, WorkflowProgressNodeVO>()
  for (const p of props.progressNodes || []) {
    if (p.nodeKey) map.set(p.nodeKey, p)
  }
  return map
})

const summaryLabels = computed(() => ({
  conditionBranches: (total: number, filled: number) =>
    L.value.conditionBranches.replace('{total}', String(total)).replace('{filled}', String(filled)),
  parallelPaths: (n: number) => L.value.parallelPaths.replace('{n}', String(n)),
  passAny: L.value.passAny,
  passAll: L.value.passAll,
  passSequentialAll: L.value.passSequential,
  listSep: L.value.listSep,
  starter: L.value.starter,
}))

function conditionBranches(node: WorkflowDesignNode) {
  return ensureConditionBranches(node).branches || []
}

function parallelBranches(node: WorkflowDesignNode) {
  return ensureParallelBranches(node).branches || []
}

function yesBranchIndex(node: WorkflowDesignNode, branchId: string) {
  const yes = conditionBranches(node).filter((b) => !b.isDefault)
  const i = yes.findIndex((b) => b.id === branchId)
  return i >= 0 ? i + 1 : 1
}

function displayName(node: WorkflowDesignNode) {
  const prog = progressByKey.value.get(node.taskDefinitionKey)
  return prog?.nodeName || node.name || node.taskDefinitionKey
}

function isActive(key: string) {
  return progressByKey.value.get(key)?.nodeStatus === 'active'
}

function statusClass(key: string) {
  const status = progressByKey.value.get(key)?.nodeStatus
  return status ? `is-${status}` : ''
}

function nodeTypeLabel(type?: string) {
  const l = L.value
  if (type === 'cc') return l.cc
  if (type === 'condition') return l.condition
  if (type === 'inclusive') return l.inclusive
  if (type === 'parallel') return l.parallel
  if (type === 'delay') return l.delay
  if (type === 'service') return l.service
  if (type === 'subprocess') return l.subprocess
  return l.approve
}

function formatBranchCondition(expression?: string) {
  const expr = (expression || '').trim()
  if (!expr) return L.value.conditionEmpty
  return formatConditionExpressionHuman(expr, conditionFieldLabels.value) || expr
}

function nodeSummary(node: WorkflowDesignNode) {
  if ((node.nodeType || 'approve') === 'condition' || node.nodeType === 'inclusive') {
    const yesList = conditionBranches(node).filter((b) => !b.isDefault)
    if (!yesList.length) return L.value.conditionEmpty
    if (yesList.length === 1) return formatBranchCondition(yesList[0].expression)
    return (
      formatAssigneeSummary(node, assigneeResolvers.value, summaryLabels.value) ||
      L.value.conditionEmpty
    )
  }
  if (node.nodeType === 'parallel') {
    return formatAssigneeSummary(node, assigneeResolvers.value, summaryLabels.value) || '—'
  }
  return formatAssigneeSummary(node, assigneeResolvers.value, summaryLabels.value) || '—'
}

function leafSummary(node: WorkflowDesignNode) {
  const prog = progressByKey.value.get(node.taskDefinitionKey)
  if (prog) {
    const parts: string[] = []
    const who = prog.assigneeUserName || prog.assigneeUserId
    if (who) parts.push(who)
    if (prog.nodeStatus === 'pending') parts.push(L.value.pending)
    if (prog.approved === true) parts.push(L.value.pass)
    if (prog.approved === false) parts.push(L.value.reject)
    if (parts.length) return parts.join(' · ')
  }
  return nodeSummary(node)
}
</script>

<style src="@/styles/vertical-flow-theme.css"></style>
<style scoped>
.vfv {
  min-height: 280px;
}

.vfd-flow {
  --vfd-node-w: min(300px, 92%);
  --vfd-wire-w: 2.5px;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.vfd-link {
  width: var(--vfd-wire-w, 2.5px);
  height: 20px;
  margin: -1px 0;
  background: var(--vfd-wire, #c4a07a);
  border-radius: 999px;
  flex-shrink: 0;
}

.vfd-node {
  width: var(--vfd-node-w);
  border-radius: 10px;
  overflow: hidden;
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

.vfd-current-tag {
  flex-shrink: 0;
  margin-left: auto;
  --el-tag-bg-color: rgb(255 255 255 / 28%);
  --el-tag-border-color: rgb(255 255 255 / 45%);
  --el-tag-text-color: #fff;
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

.vfd-node.is-rejected .vfd-node-hd {
  background: #d47a7a !important;
}

.vfd-node.is-skipped {
  opacity: 0.48;
  filter: saturate(0.75);
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

.vfd-fork-lanes {
  display: flex;
  justify-content: center;
  align-items: stretch;
  gap: var(--lane-gap);
  width: max-content;
  max-width: 100%;
  margin: 0 auto;
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

.vfd-lane::before {
  content: '';
  position: absolute;
  top: -18px;
  left: calc(50% - var(--vfd-wire-w) / 2);
  width: var(--vfd-wire-w);
  height: 18px;
  background: var(--vfd-wire);
  border-radius: 999px;
}

.vfd-lane-cap {
  width: 100%;
  margin-bottom: 8px;
  padding: 6px 8px;
  border-radius: 6px;
  background: #fff;
  border: 1px solid rgb(15 157 88 / 28%);
  text-align: left;
}

.vfd-lane-cap--static {
  cursor: default;
}

.vfd-lane--default .vfd-lane-cap {
  border-style: dashed;
  border-color: rgb(95 99 104 / 35%);
  background: #fafbfc;
}

.vfd-lane--parallel .vfd-lane-cap {
  border-color: rgb(124 58 237 / 30%);
}

.vfd-lane--parallel .vfd-lane-title {
  color: #7c3aed;
}

.vfd-lane-cap-hd {
  display: flex;
  align-items: center;
  gap: 4px;
}

.vfd-lane-title {
  font-size: 12px;
  font-weight: 650;
  color: #0f9d58;
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
  color: #0f9d58;
  background: rgb(15 157 88 / 12%);
}

.vfd-lane-expr {
  display: block;
  margin-top: 2px;
  font-size: 12px;
  color: #646a73;
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

.vfd-lane-badge--default {
  display: inline-flex;
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
}

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
  box-shadow: 0 0 0 3px var(--vfd-wire-halo, #f7efe4);
}
</style>
