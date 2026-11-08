<!-- 租户策略 — GenListLayout 薄壳（非标准 CRUD，目录列表 + GenFormShell） -->
<template>
  <div class="gen-page-shell">
    <GenListLayout
      v-loading="loading"
      :title="gp('title')"
      :description="gp('description')"
      :show-search="false"
      :show-right-toolbar="false"
      @refresh="loadCatalog"
    >
      <template #table-top>
        <el-tabs v-model="activeCategory">
          <el-tab-pane
            v-for="cat in visibleCategories"
            :key="cat.id"
            :name="cat.id"
            :label="categoryTabLabel(cat.id)"
          />
        </el-tabs>
      </template>

      <template #toolbar-extra>
        <el-button
          type="warning"
          plain
          :icon="RefreshRight"
          :loading="refreshLoading"
          v-hasPermi="[PERM.update]"
          @click="handleRefreshCache"
        >
          {{ gp('refreshCache') }}
        </el-button>
      </template>

      <el-table :data="filteredRows" border stripe>
        <el-table-column :label="gp('configName')" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            {{ keyLabel(row.configKey) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="configKey"
          :label="gp('configKey')"
          min-width="220"
          show-overflow-tooltip
        />
        <el-table-column :label="gp('valueType')" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="info">{{ row.valueType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="gp('platformDefault')" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.sensitive ? '******' : displayValue(row.platformDefaultValue) }}
          </template>
        </el-table-column>
        <el-table-column :label="gp('status')" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.overridden" type="warning">{{ gp('overridden') }}</el-tag>
            <el-tag v-else>{{ gp('inherited') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="gp('editValue')" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            {{
              row.sensitive && !row.overridden
                ? '******'
                : displayValue(row.effectiveValue ?? row.platformDefaultValue)
            }}
          </template>
        </el-table-column>
        <el-table-column :label="t('common.actions')" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" v-hasPermi="[PERM.update]" @click="openEdit(row)">
              {{ t('common.edit') }}
            </el-button>
            <el-button
              link
              type="danger"
              :disabled="!row.overridden || isPlatformTenant"
              :loading="resettingKey === row.configKey"
              v-hasPermi="[PERM.update]"
              @click="handleReset(row)"
            >
              {{ gp('reset') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </GenListLayout>

    <GenFormShell
      v-model="formVisible"
      :title="gp('editTitle')"
      :width="editingRow?.valueType === 'JSON' ? 720 : 560"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <el-form
        v-if="editingRow"
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item :label="gp('configName')">
          <span>{{ keyLabel(editingRow.configKey) }}</span>
        </el-form-item>
        <el-form-item :label="gp('configKey')">
          <span class="mono">{{ editingRow.configKey }}</span>
        </el-form-item>
        <el-form-item :label="gp('valueType')">
          <el-tag type="info">{{ editingRow.valueType }}</el-tag>
        </el-form-item>
        <el-form-item :label="gp('platformDefault')">
          <span>{{ editingRow.sensitive ? '******' : displayValue(editingRow.platformDefaultValue) }}</span>
        </el-form-item>
        <el-form-item :label="gp('editValue')" prop="configValue">
          <el-switch
            v-if="editingRow.valueType === 'BOOL'"
            v-model="form.boolValue"
            @change="(v: string | number | boolean) => (form.configValue = v ? 'true' : 'false')"
          />
          <el-input-number
            v-else-if="editingRow.valueType === 'INT'"
            v-model="form.numberValue"
            controls-position="right"
            class="w-full"
            @change="syncFormNumber"
          />
          <el-input
            v-else-if="editingRow.valueType === 'JSON'"
            v-model="form.configValue"
            type="textarea"
            :rows="10"
            :placeholder="gf('configValue', 'placeholder')"
          />
          <el-input
            v-else-if="editingRow.sensitive"
            v-model="form.configValue"
            type="password"
            show-password
            clearable
            autocomplete="new-password"
            :placeholder="gf('configValue', 'placeholder')"
          />
          <el-input
            v-else
            v-model="form.configValue"
            clearable
            :placeholder="gf('configValue', 'placeholder')"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">
          {{ t('common.confirm') }}
        </el-button>
      </template>
    </GenFormShell>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { RefreshRight } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { useI18n } from 'vue-i18n'
import GenFormShell from '@/components/gen/layout/GenFormShell.vue'
import GenListLayout from '@/components/gen/layout/GenListLayout.vue'
import { isPlatformTenant as matchPlatformTenant, isYes } from '@/constants/flags'
import { useUserStore } from '@/stores/user'
import { useGenPageShellI18n } from '@/composables/gen/useGenPageShellI18n'
import {
  PERM_TenantPolicy as PERM,
  listTenantPolicyCatalog,
  refreshTenantPolicyCache,
  resetTenantPolicy,
  upsertTenantPolicy,
  type TenantPolicyConfigItem,
} from '@/api/system/tenantPolicy'
import {
  categoriesPresentIn,
  matchConfigCategory,
  type ConfigCategoryId,
} from '@/utils/configCategory'

const { t } = useI18n()
const { gp, gf, messages } = useGenPageShellI18n('tenantPolicy')

const userStore = useUserStore()
const isPlatformTenant = computed(() => matchPlatformTenant(userStore.user?.tenantId))

const loading = ref(false)
const refreshLoading = ref(false)
const saving = ref(false)
const resettingKey = ref('')
const formVisible = ref(false)
const formRef = ref<FormInstance>()
const editingRow = ref<TenantPolicyConfigItem | null>(null)
const rows = ref<TenantPolicyConfigItem[]>([])
const activeCategory = ref<ConfigCategoryId>('all')

const form = reactive({
  configValue: '',
  boolValue: false,
  numberValue: undefined as number | undefined,
})

const formRules = computed<FormRules>(() => ({
  configValue: [
    {
      required: true,
      message: gf('configValue', 'placeholder') || gp('editValue'),
      trigger: 'blur',
      validator: (_rule, _value, callback) => {
        if (editingRow.value?.valueType === 'BOOL') {
          callback()
          return
        }
        if (editingRow.value?.valueType === 'INT') {
          if (form.numberValue == null || Number.isNaN(form.numberValue)) {
            callback(new Error(t('common.pleaseInput', { field: gp('editValue') })))
            return
          }
          callback()
          return
        }
        if (!(form.configValue ?? '').trim()) {
          callback(new Error(t('common.pleaseInput', { field: gp('editValue') })))
          return
        }
        callback()
      },
    },
  ],
}))

const visibleCategories = computed(() =>
  categoriesPresentIn(rows.value.map((r) => r.configKey)),
)

const filteredRows = computed(() =>
  rows.value.filter((r) => matchConfigCategory(r.configKey, activeCategory.value)),
)

watch(visibleCategories, (cats) => {
  if (!cats.some((c) => c.id === activeCategory.value)) {
    activeCategory.value = 'all'
  }
})

function displayValue(value: string | null | undefined) {
  if (value == null || value === '') return '—'
  return value
}

function keyLabel(configKey: string) {
  return messages.value?.keys?.[configKey] ?? configKey
}

function categoryTabLabel(id: ConfigCategoryId) {
  const label = gp(`category.${id}`)
  if (id === 'all') {
    return `${label} (${rows.value.length})`
  }
  const count = rows.value.filter((r) => matchConfigCategory(r.configKey, id)).length
  return `${label} (${count})`
}

function syncFormNumber() {
  form.configValue =
    form.numberValue == null || Number.isNaN(form.numberValue) ? '' : String(form.numberValue)
}

function openEdit(row: TenantPolicyConfigItem) {
  editingRow.value = row
  const seed = row.tenantValue ?? row.effectiveValue ?? row.platformDefaultValue ?? ''
  form.configValue = seed
  if (row.valueType === 'BOOL') {
    const v = seed.trim().toLowerCase()
    form.boolValue = v === 'true' || v === 'y' || isYes(v)
    form.configValue = form.boolValue ? 'true' : 'false'
  } else if (row.valueType === 'INT') {
    const n = Number.parseInt(seed, 10)
    form.numberValue = Number.isNaN(n) ? undefined : n
    syncFormNumber()
  } else {
    form.numberValue = undefined
  }
  formVisible.value = true
}

async function submitForm() {
  if (!editingRow.value) return
  if (editingRow.value.valueType === 'INT') {
    syncFormNumber()
  }
  if (editingRow.value.valueType === 'BOOL') {
    form.configValue = form.boolValue ? 'true' : 'false'
  }
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  const value = (form.configValue ?? '').trim()
  if (!value) {
    ElMessage.warning(t('common.pleaseInput', { field: gp('editValue') }))
    return
  }
  saving.value = true
  try {
    await upsertTenantPolicy(editingRow.value.configKey, value)
    ElMessage.success(gp('saveSuccess'))
    formVisible.value = false
    await loadCatalog()
  } finally {
    saving.value = false
  }
}

async function loadCatalog() {
  loading.value = true
  try {
    const res = await listTenantPolicyCatalog()
    rows.value = res.data.data ?? []
  } finally {
    loading.value = false
  }
}

async function handleReset(row: TenantPolicyConfigItem) {
  await ElMessageBox.confirm(gp('resetConfirm'), t('common.tip'))
  resettingKey.value = row.configKey
  try {
    await resetTenantPolicy(row.configKey)
    ElMessage.success(gp('resetSuccess'))
    await loadCatalog()
  } finally {
    resettingKey.value = ''
  }
}

async function handleRefreshCache() {
  refreshLoading.value = true
  try {
    await refreshTenantPolicyCache()
    ElMessage.success(gp('refreshCacheSuccess'))
    await loadCatalog()
  } finally {
    refreshLoading.value = false
  }
}

onMounted(loadCatalog)
</script>

<script lang="ts">
export default { name: 'SystemTenantPolicy' }
</script>

<style scoped>
.w-full {
  width: 100%;
}

.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  word-break: break-all;
}
</style>
