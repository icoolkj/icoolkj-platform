<!-- 文件访问策略 -->
<template>
  <div class="gen-page-shell">
    <GenListLayout
      v-loading="loading"
      :title="gp('title')"
      :show-search="false"
      @refresh="load"
    >
      <template #table-top>
        <div class="policy-mode-bar">
          <el-radio-group v-model="mode" size="small" @change="load">
            <el-radio-button value="stored">{{ gp('showStored') }}</el-radio-button>
            <el-radio-button value="effective">{{ gp('showEffective') }}</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <template #toolbar>
        <el-button
          v-if="mode === 'stored'"
          type="primary"
          :icon="Plus"
          v-hasPermi="[PERM.add]"
          @click="openCreate"
        >
          {{ gp('add') }}
        </el-button>
        <el-button :icon="Refresh" v-hasPermi="[PERM.update]" @click="handleRefresh">
          {{ gp('refresh') }}
        </el-button>
      </template>

      <el-table :data="rows" border stripe :empty-text="emptyText">
        <el-table-column
          :label="gf('bizTypePattern', 'label')"
          prop="bizTypePattern"
          min-width="140"
        >
          <template #default="{ row }">
            <span>{{ row.bizTypePattern }}</span>
            <el-tag v-if="row.pinned" size="small" type="warning" class="policy-pinned-tag">
              {{ gp('pinned') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="gf('authMode', 'label')" min-width="130">
          <template #default="{ row }">{{ enumLabel('authMode', row.authMode) }}</template>
        </el-table-column>
        <el-table-column :label="gf('tenantScope', 'label')" min-width="140">
          <template #default="{ row }">{{ enumLabel('tenantScope', row.tenantScope) }}</template>
        </el-table-column>
        <el-table-column :label="gf('visibility', 'label')" min-width="140">
          <template #default="{ row }">{{ enumLabel('visibility', row.visibility) }}</template>
        </el-table-column>
        <el-table-column
          :label="gf('anonPurposes', 'label')"
          prop="anonPurposes"
          min-width="140"
          show-overflow-tooltip
        />
        <el-table-column
          :label="gf('permAnyOf', 'label')"
          prop="permAnyOf"
          min-width="140"
          show-overflow-tooltip
        />
        <el-table-column :label="gf('sortNo', 'label')" prop="sortNo" width="80" align="center" />
        <el-table-column v-if="mode === 'stored'" :label="gf('enabled', 'label')" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="isYes(row.enabled) ? 'success' : 'info'" size="small">
              {{ isYes(row.enabled) ? gp('enabledYes') : gp('enabledNo') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="gf('remark', 'label')" prop="remark" min-width="180" show-overflow-tooltip />
        <el-table-column
          v-if="mode === 'stored'"
          :label="t('common.actions')"
          width="160"
          fixed="right"
          align="center"
        >
          <template #default="{ row }">
            <template v-if="!row.pinned">
              <el-button link type="primary" v-hasPermi="[PERM.update]" @click="openEdit(row)">
                {{ gp('edit') }}
              </el-button>
              <el-button link type="danger" v-hasPermi="[PERM.delete]" @click="handleDelete(row)">
                {{ gp('delete') }}
              </el-button>
            </template>
            <span v-else class="policy-pinned-hint">{{ gp('pinnedHint') }}</span>
          </template>
        </el-table-column>
      </el-table>
    </GenListLayout>

    <el-dialog v-model="dialogVisible" :title="gp('formTitle')" width="640px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="140px">
        <el-form-item :label="gf('bizTypePattern', 'label')" prop="bizTypePattern">
          <el-input
            v-model="form.bizTypePattern"
            :placeholder="gf('bizTypePattern', 'placeholder')"
            :disabled="!!form.id"
          />
        </el-form-item>
        <el-form-item :label="gf('authMode', 'label')" prop="authMode">
          <el-select v-model="form.authMode" style="width: 100%">
            <el-option
              v-for="opt in authModeOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="gf('tenantScope', 'label')" prop="tenantScope">
          <el-select v-model="form.tenantScope" style="width: 100%">
            <el-option
              v-for="opt in tenantScopeOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="gf('visibility', 'label')" prop="visibility">
          <el-select v-model="form.visibility" style="width: 100%">
            <el-option
              v-for="opt in visibilityOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="gf('anonPurposes', 'label')">
          <el-input v-model="form.anonPurposes" :placeholder="gf('anonPurposes', 'placeholder')" />
        </el-form-item>
        <el-form-item :label="gf('permAnyOf', 'label')">
          <el-input v-model="form.permAnyOf" :placeholder="gf('permAnyOf', 'placeholder')" />
        </el-form-item>
        <el-form-item :label="gf('sortNo', 'label')">
          <el-input-number v-model="form.sortNo" :min="0" />
        </el-form-item>
        <el-form-item :label="gf('enabled', 'label')">
          <el-switch
            :model-value="isYes(form.enabled)"
            @change="(v: boolean) => (form.enabled = v ? YES : NO)"
          />
        </el-form-item>
        <el-form-item :label="gf('remark', 'label')">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="save">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useGenPageShellI18n } from '@/composables/gen/useGenPageShellI18n'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import GenListLayout from '@/components/gen/layout/GenListLayout.vue'
import { isYes, NO, YES } from '@/constants/flags'
import {
  deleteFileAccessPolicy,
  listEffectiveFileAccessPolicies,
  listFileAccessPolicies,
  PERM_FileAccessPolicy,
  refreshFileAccessPolicyRegistry,
  upsertFileAccessPolicy,
} from '@/api/infra/fileAccessPolicy'
import type { FileAccessPolicyVO } from '@/types/infra/fileAccessPolicy'
const { gp, gf, gv } = useGenPageShellI18n('fileAccessPolicy')
const { t } = useI18n()
const PERM = PERM_FileAccessPolicy

const loading = ref(false)
const saving = ref(false)
const mode = ref<'stored' | 'effective'>('stored')
const rows = ref<FileAccessPolicyVO[]>([])
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({
  id: '',
  bizTypePattern: '',
  authMode: 'SESSION',
  tenantScope: 'OWN',
  visibility: 'ALWAYS',
  anonPurposes: '',
  permAnyOf: '',
  sortNo: 100,
  enabled: YES,
  remark: '',
})

const emptyText = computed(() =>
  mode.value === 'effective' ? gp('emptyEffective') : gp('emptyStored'),
)

const authModeOptions = computed(() => [
  { value: 'SESSION', label: gp('authModeSESSION') },
  { value: 'ANON', label: gp('authModeANON') },
  { value: 'SESSION_OR_ANON', label: gp('authModeSESSION_OR_ANON') },
])
const tenantScopeOptions = computed(() => [
  { value: 'OWN', label: gp('tenantScopeOWN') },
  { value: 'OWN_OR_PLATFORM', label: gp('tenantScopeOWN_OR_PLATFORM') },
])
const visibilityOptions = computed(() => [
  { value: 'ALWAYS', label: gp('visibilityALWAYS') },
  { value: 'CUSTOM', label: gp('visibilityCUSTOM') },
])

function enumLabel(kind: 'authMode' | 'tenantScope' | 'visibility', code?: string) {
  if (!code) return '—'
  const key = `${kind}${code}` as
    | 'authModeSESSION'
    | 'authModeANON'
    | 'authModeSESSION_OR_ANON'
    | 'tenantScopeOWN'
    | 'tenantScopeOWN_OR_PLATFORM'
    | 'visibilityALWAYS'
    | 'visibilityCUSTOM'
  const label = gp(key)
  return label && label !== key ? label : code
}

const rules = computed<FormRules>(() => ({
  bizTypePattern: [{ required: true, message: gv('bizTypePattern', 'required'), trigger: 'blur' }],
  authMode: [{ required: true, trigger: 'change' }],
  tenantScope: [{ required: true, trigger: 'change' }],
  visibility: [{ required: true, trigger: 'change' }],
}))

async function load() {
  loading.value = true
  try {
    const res =
      mode.value === 'effective'
        ? await listEffectiveFileAccessPolicies()
        : await listFileAccessPolicies()
    rows.value = res.data.data ?? []
  } catch {
    rows.value = []
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.id = ''
  form.bizTypePattern = ''
  form.authMode = 'SESSION'
  form.tenantScope = 'OWN'
  form.visibility = 'ALWAYS'
  form.anonPurposes = ''
  form.permAnyOf = ''
  form.sortNo = 100
  form.enabled = YES
  form.remark = ''
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row: FileAccessPolicyVO) {
  if (row.pinned) {
    ElMessage.warning(gp('pinnedHint'))
    return
  }
  form.id = row.id || ''
  form.bizTypePattern = row.bizTypePattern
  form.authMode = row.authMode || 'SESSION'
  form.tenantScope = row.tenantScope || 'OWN'
  form.visibility = row.visibility || 'ALWAYS'
  form.anonPurposes = row.anonPurposes || ''
  form.permAnyOf = row.permAnyOf || ''
  form.sortNo = row.sortNo ?? 100
  form.enabled = row.enabled || YES
  form.remark = row.remark || ''
  dialogVisible.value = true
}

async function save() {
  await formRef.value?.validate()
  saving.value = true
  try {
    await upsertFileAccessPolicy({
      id: form.id || undefined,
      bizTypePattern: form.bizTypePattern.trim(),
      authMode: form.authMode,
      tenantScope: form.tenantScope,
      visibility: form.visibility,
      anonPurposes: form.anonPurposes || undefined,
      permAnyOf: form.permAnyOf || undefined,
      sortNo: form.sortNo,
      enabled: form.enabled,
      remark: form.remark || undefined,
    })
    ElMessage.success(gp('saveSuccess'))
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: FileAccessPolicyVO) {
  if (!row.id || row.pinned) return
  await ElMessageBox.confirm(gp('deleteConfirm', { pattern: row.bizTypePattern }), {
    type: 'warning',
  })
  await deleteFileAccessPolicy(row.id)
  ElMessage.success(gp('deleteSuccess'))
  await load()
}

async function handleRefresh() {
  await refreshFileAccessPolicyRegistry()
  ElMessage.success(gp('refreshSuccess'))
  mode.value = 'effective'
  await load()
}

onMounted(() => load())
</script>

<style scoped>
.policy-mode-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-bottom: 4px;
}

.policy-pinned-tag {
  margin-left: 8px;
}

.policy-pinned-hint {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>
