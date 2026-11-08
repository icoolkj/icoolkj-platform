<template>
  <el-dialog
    :model-value="modelValue"
    :title="title"
    width="720px"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
  >
    <el-tabs v-model="activeProduct" class="menu-auth-tabs">
      <el-tab-pane
        v-for="group in productGroups"
        :key="group.code"
        :label="group.label"
        :name="group.code"
      >
        <div class="menu-auth-toolbar">
          <el-button link type="primary" @click="selectAllInProduct(group.code)">
            {{ t('layout.menuAuthSelectProduct') }}
          </el-button>
          <el-button link @click="clearProduct(group.code)">
            {{ t('layout.menuAuthClearProduct') }}
          </el-button>
        </div>
        <div class="menu-auth-tree">
          <el-tree
            :ref="setTreeRefFor(group.code)"
            v-loading="loading"
            :data="group.tree"
            show-checkbox
            node-key="id"
            :props="{ label: 'menuName', children: 'children' }"
            @check="onTreeCheck"
          />
        </div>
      </el-tab-pane>
    </el-tabs>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">{{ cancelText }}</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">{{ confirmText }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, type ElTree } from 'element-plus'
import { assignRoleMenus, getRoleAssignableMenus, getRoleMenuIds } from '@/api/system/role'
import { listProducts } from '@/api/system/product'
import { TREE_ROOT_PARENT_ID } from '@/constants/flags'
import { buildGenTree, sortGenTree, walkGenTree } from '@/utils/genTree'
import { i18n } from '@/i18n'
import type { MenuVO } from '@/types/system/menu'
import { PLATFORM_PRODUCT_CODE, sortProductCodes } from '@/constants/product'

const props = defineProps<{
  modelValue: boolean
  roleId?: string | null
  title: string
  cancelText: string
  confirmText: string
  successText: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  success: []
}>()

const { t } = useI18n()
const loading = ref(false)
const submitting = ref(false)
const flatMenus = ref<MenuVO[]>([])
const checkedIds = ref<string[]>([])
const activeProduct = ref(PLATFORM_PRODUCT_CODE)
const productLabels = ref<Record<string, string>>({})
const treeRefs = ref<Record<string, InstanceType<typeof ElTree>>>({})

function setTreeRef(code: string, el: InstanceType<typeof ElTree> | null) {
  if (el) {
    treeRefs.value[code] = el
  }
}

/** Typed ref callback factory so vue-tsc does not treat the template param as any. */
function setTreeRefFor(code: string) {
  return (el: InstanceType<typeof ElTree> | null) => setTreeRef(code, el)
}

function resolveMenuLabel(menu: MenuVO): string {
  const locale = String(i18n.global.locale.value || 'zh-CN')
  const fromI18n = menu.nameI18n?.[locale] || menu.nameI18n?.['zh-CN']
  return fromI18n || menu.menuName
}

const productGroups = computed(() => {
  const byProduct = new Map<string, MenuVO[]>()
  for (const menu of flatMenus.value) {
    const code = menu.productCode || PLATFORM_PRODUCT_CODE
    const list = byProduct.get(code) ?? []
    list.push(menu)
    byProduct.set(code, list)
  }
  const codes = sortProductCodes(byProduct.keys())
  return codes.map((code) => {
    const flat = (byProduct.get(code) ?? []).map((m) => ({
      ...m,
      menuName: resolveMenuLabel(m),
    })) as unknown as Record<string, unknown>[]
    const tree = buildGenTree(flat, 'parentId', TREE_ROOT_PARENT_ID)
    sortGenTree(tree, 'sortNo')
    return {
      code,
      label: productLabels.value[code] || code,
      tree,
      flatIds: flat.map((m) => String(m.id)),
    }
  })
})

function collectTreeNodeIds(nodes: Record<string, unknown>[]): string[] {
  const ids: string[] = []
  walkGenTree(nodes as Parameters<typeof walkGenTree>[0], (node) => {
    const id = (node as Record<string, unknown>).id
    if (id) ids.push(String(id))
  })
  return ids
}

function syncCheckedToTrees() {
  for (const group of productGroups.value) {
    const tree = treeRefs.value[group.code]
    tree?.setCheckedKeys(checkedIds.value.filter((id) => group.flatIds.includes(id)), false)
  }
}

function onTreeCheck(_data: unknown, state: { checkedKeys: string[]; halfCheckedKeys: string[] }) {
  const group = productGroups.value.find((g) => g.code === activeProduct.value)
  if (!group) return
  const groupIdSet = new Set(group.flatIds)
  const others = checkedIds.value.filter((id) => !groupIdSet.has(id))
  checkedIds.value = [...new Set([...others, ...state.checkedKeys, ...state.halfCheckedKeys])]
}

function selectAllInProduct(code: string) {
  const group = productGroups.value.find((g) => g.code === code)
  if (!group) return
  const allIds = collectTreeNodeIds(group.tree as Record<string, unknown>[])
  const groupIdSet = new Set(group.flatIds)
  const others = checkedIds.value.filter((id) => !groupIdSet.has(id))
  checkedIds.value = [...new Set([...others, ...allIds])]
  syncCheckedToTrees()
}

function clearProduct(code: string) {
  const group = productGroups.value.find((g) => g.code === code)
  if (!group) return
  const groupIdSet = new Set(group.flatIds)
  checkedIds.value = checkedIds.value.filter((id) => !groupIdSet.has(id))
  syncCheckedToTrees()
}

async function loadProductLabels() {
  try {
    const res = await listProducts()
    const map: Record<string, string> = {}
    for (const p of res.data.data ?? []) {
      map[p.code] = p.name || p.code
    }
    productLabels.value = map
  } catch {
    productLabels.value = {}
  }
}

async function loadData() {
  if (!props.roleId) return
  loading.value = true
  try {
    await loadProductLabels()
    const [menuRes, idsRes] = await Promise.all([
      getRoleAssignableMenus(props.roleId),
      getRoleMenuIds(props.roleId),
    ])
    flatMenus.value = menuRes.data.data ?? []
    checkedIds.value = idsRes.data.data ?? []
    if (productGroups.value.length) {
      activeProduct.value = productGroups.value[0].code
    }
    await nextTick()
    syncCheckedToTrees()
  } finally {
    loading.value = false
  }
}

async function submit() {
  if (!props.roleId) return
  submitting.value = true
  try {
    await assignRoleMenus(props.roleId, checkedIds.value)
    ElMessage.success(props.successText)
    emit('update:modelValue', false)
    emit('success')
  } finally {
    submitting.value = false
  }
}

watch(
  () => [props.modelValue, props.roleId] as const,
  ([visible, roleId]) => {
    if (visible && roleId) {
      treeRefs.value = {}
      loadData()
    }
  },
)
</script>

<style scoped>
.menu-auth-tabs :deep(.el-tabs__content) {
  padding-top: 4px;
}

.menu-auth-toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
}

.menu-auth-tree {
  max-height: min(420px, 55vh);
  overflow: auto;
}
</style>
