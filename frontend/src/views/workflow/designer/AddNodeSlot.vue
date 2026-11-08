<!-- 连线上的「+」：竖线贯通，按钮叠在线上，避免断线感 -->
<template>
  <div class="add-slot">
    <el-popover
      v-model:visible="open"
      placement="right-start"
      :width="popoverWidth"
      trigger="click"
      popper-class="vfd-add-popper"
    >
      <template #reference>
        <button type="button" class="add-slot-btn" :aria-label="gp('designerAddNode')">
          <el-icon :size="14"><Plus /></el-icon>
        </button>
      </template>
      <div class="add-grid" :class="{ 'add-grid--compact': !allowGateway }">
        <button type="button" class="add-item add-item--approve" @click="pick('approve')">
          <span class="add-item-ico">
            <el-icon :size="20"><User /></el-icon>
          </span>
          <span class="add-item-label">{{ gp('designerNodeApprove') }}</span>
        </button>
        <button type="button" class="add-item add-item--cc" @click="pick('cc')">
          <span class="add-item-ico">
            <el-icon :size="20"><Promotion /></el-icon>
          </span>
          <span class="add-item-label">{{ gp('designerNodeCc') }}</span>
        </button>
        <button
          v-if="allowGateway"
          type="button"
          class="add-item add-item--condition"
          @click="pick('condition')"
        >
          <span class="add-item-ico">
            <el-icon :size="20"><Share /></el-icon>
          </span>
          <span class="add-item-label">{{ gp('designerNodeCondition') }}</span>
        </button>
        <button
          v-if="allowGateway"
          type="button"
          class="add-item add-item--parallel"
          @click="pick('parallel')"
        >
          <span class="add-item-ico">
            <el-icon :size="20"><Connection /></el-icon>
          </span>
          <span class="add-item-label">{{ gp('designerNodeParallel') }}</span>
        </button>
        <button
          v-if="allowGateway"
          type="button"
          class="add-item add-item--inclusive"
          @click="pick('inclusive')"
        >
          <span class="add-item-ico">
            <el-icon :size="20"><Share /></el-icon>
          </span>
          <span class="add-item-label">{{ gp('designerNodeInclusive') }}</span>
        </button>
        <button
          v-if="allowGateway"
          type="button"
          class="add-item add-item--delay"
          @click="pick('delay')"
        >
          <span class="add-item-ico">
            <el-icon :size="20"><Timer /></el-icon>
          </span>
          <span class="add-item-label">{{ gp('designerNodeDelay') }}</span>
        </button>
        <button
          v-if="allowGateway"
          type="button"
          class="add-item add-item--service"
          @click="pick('service')"
        >
          <span class="add-item-ico">
            <el-icon :size="20"><SetUp /></el-icon>
          </span>
          <span class="add-item-label">{{ gp('designerNodeService') }}</span>
        </button>
        <button
          v-if="allowGateway"
          type="button"
          class="add-item add-item--subprocess"
          @click="pick('subprocess')"
        >
          <span class="add-item-ico">
            <el-icon :size="20"><Folder /></el-icon>
          </span>
          <span class="add-item-label">{{ gp('designerNodeSubprocess') }}</span>
        </button>
      </div>
    </el-popover>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import {
  Connection,
  Folder,
  Plus,
  Promotion,
  SetUp,
  Share,
  Timer,
  User,
} from '@element-plus/icons-vue'
import { useDesignerGenI18n } from '@/composables/useDesignerGenI18n'
import type { WorkflowDesignNodeType } from '@/types/workflow/design'

const props = withDefaults(
  defineProps<{
    /** 分支内不支持再嵌套网关（条件/并行）；兼容旧名 allowCondition */
    allowCondition?: boolean
    allowGateway?: boolean
  }>(),
  { allowCondition: true, allowGateway: undefined },
)

const emit = defineEmits<{
  add: [type: WorkflowDesignNodeType]
}>()

const { gp } = useDesignerGenI18n()

const open = ref(false)
const allowGateway = computed(() =>
  props.allowGateway !== undefined ? props.allowGateway : props.allowCondition,
)
const popoverWidth = computed(() => (allowGateway.value ? 420 : 200))

function pick(type: WorkflowDesignNodeType) {
  open.value = false
  emit('add', type)
}
</script>

<style scoped>
.add-slot {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-height: 40px;
  padding: 0;
  z-index: 1;
}

/* 贯通竖线：不被 + 打断（避免 translate 半像素偏移） */
.add-slot::before {
  content: '';
  position: absolute;
  top: 0;
  bottom: 0;
  left: calc(50% - var(--vfd-wire-w, 2.5px) / 2);
  width: var(--vfd-wire-w, 2.5px);
  background: var(--vfd-wire, #c4a07a);
  border-radius: 999px;
  z-index: 0;
  pointer-events: none;
}

.add-slot-btn {
  position: relative;
  z-index: 1;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 2px solid #fff;
  background: #e8a35c;
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow:
    0 0 0 3px var(--vfd-wire-halo, #f7efe4),
    0 2px 6px rgb(200 140 70 / 28%);
  transition:
    transform 0.12s ease,
    background 0.12s ease,
    box-shadow 0.12s ease;
}

.add-slot-btn:hover {
  background: #d4924a;
  transform: scale(1.08);
  box-shadow:
    0 0 0 3px var(--vfd-wire-halo, #f7efe4),
    0 3px 10px rgb(200 140 70 / 36%);
}

.add-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  padding: 4px;
}

.add-grid--compact {
  grid-template-columns: repeat(2, 1fr);
}

.add-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 10px 6px;
  border: 1px solid #e8d5c0;
  border-radius: 10px;
  background: #fffcf8;
  cursor: pointer;
  transition:
    border-color 0.12s,
    background 0.12s;
}

.add-item:hover {
  background: #faf4ec;
  border-color: #e8a35c;
}

.add-item-ico {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.add-item--approve .add-item-ico {
  background: #e8a35c;
}

.add-item--cc .add-item-ico {
  background: #5ba8c4;
}

.add-item--condition .add-item-ico {
  background: #5ea87a;
}

.add-item--parallel .add-item-ico {
  background: #d47a7a;
}

.add-item--inclusive .add-item-ico {
  background: #4fad9a;
}

.add-item--delay .add-item-ico {
  background: #e09658;
}

.add-item--service .add-item-ico {
  background: #5f9fc0;
}

.add-item--subprocess .add-item-ico {
  background: #d0b04e;
}

.add-item-label {
  font-size: 12px;
  color: #5e5348;
  text-align: center;
  line-height: 1.3;
}
</style>
