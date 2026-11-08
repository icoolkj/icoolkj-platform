<!-- 文件配置中心 · 目录槽位（列表 + 逐条新增/修改，支持停用） -->
<template>
  <div class="gen-page-shell">
    <GenListLayout
      v-loading="loading"
      :title="gp('title')"
      :description="gp('description')"
      :show-search="true"
      @refresh="reload"
      @toggle-search="showSearch = !showSearch"
    >
      <template #table-top>
        <p class="hint">{{ gp('platformOnlyHint') }}</p>
      </template>

      <template #search>
        <el-form v-if="showSearch" inline @submit.prevent>
          <el-form-item :label="gp('selectCatalog')">
            <el-select
              v-model="selectedCatalogCode"
              filterable
              clearable
              :placeholder="gp('catalogPlaceholder')"
              style="width: 280px"
              @change="onCatalogChange"
            >
              <el-option
                v-for="c in catalogRows"
                :key="c.catalogCode"
                :label="`${displayCatalogName(c)} (${c.catalogCode})`"
                :value="c.catalogCode"
              />
            </el-select>
          </el-form-item>
        </el-form>
      </template>

      <template #toolbar>
        <el-button
          v-if="isPlatform && selectedCatalog"
          type="primary"
          :icon="Plus"
          v-hasPermi="[PERM.add]"
          @click="openCreate"
        >
          {{ gp('add') }}
        </el-button>
      </template>

      <el-empty v-if="!selectedCatalog" :description="gp('emptyHint')" />
      <el-table v-else :data="itemRows" border stripe>
        <el-table-column :label="gf('profileCode', 'label')" prop="profileCode" min-width="140" />
        <el-table-column :label="gf('profileName', 'label')" min-width="140">
          <template #default="{ row }">
            {{ displayItemName(row) }}
          </template>
        </el-table-column>
        <el-table-column :label="gf('required', 'label')" width="90" align="center">
          <template #default="{ row }">
            {{
              isYes(row.required ?? (row.resolvedProfile?.required ? YES : NO))
                ? gp('requiredYes')
                : gp('requiredNo')
            }}
          </template>
        </el-table-column>
        <el-table-column :label="gf('minNum', 'label')" width="90" align="center">
          <template #default="{ row }">
            {{ row.minNum ?? row.resolvedProfile?.minNum ?? '—' }}
          </template>
        </el-table-column>
        <el-table-column :label="gf('maxNum', 'label')" width="90" align="center">
          <template #default="{ row }">
            {{ row.maxNum ?? row.resolvedProfile?.maxNum ?? '—' }}
          </template>
        </el-table-column>
        <el-table-column :label="gf('sortNo', 'label')" prop="sortNo" width="80" align="center" />
        <el-table-column :label="gf('allowedExts', 'label')" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">
            {{ (row.resolvedProfile?.allowedExts || []).join(',') || profileExts(row.profileCode) }}
          </template>
        </el-table-column>
        <el-table-column :label="gf('status', 'label')" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="isStatusDisabled(row.status) ? 'danger' : 'success'" size="small">
              {{ isStatusDisabled(row.status) ? gp('statusDisabled') : gp('statusNormal') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          v-if="isPlatform"
          :label="t('common.actions')"
          width="160"
          fixed="right"
          align="center"
        >
          <template #default="{ row }">
            <el-button link type="primary" v-hasPermi="[PERM.update]" @click="openEdit(row)">
              {{ gp('edit') }}
            </el-button>
            <el-button link type="danger" v-hasPermi="[PERM.delete]" @click="handleDelete(row)">
              {{ gp('delete') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </GenListLayout>

    <el-dialog
      v-model="dialogVisible"
      :title="form.isCreate ? gp('addTitle') : gp('editTitle', { code: form.originalProfileCode || form.profileCode })"
      width="560px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item :label="gf('profileCode', 'label')" prop="profileCode">
          <el-select
            v-model="form.profileCode"
            filterable
            allow-create
            :disabled="!form.isCreate"
            style="width: 100%"
            :placeholder="gf('profileCode', 'placeholder')"
          >
            <el-option
              v-for="p in availableProfiles"
              :key="p.profileCode"
              :label="`${p.profileCode} · ${resolveI18nText(p.profileNameI18n, p.profileName, String(locale))}`"
              :value="p.profileCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="gf('required', 'label')">
          <el-switch
            :model-value="isYes(form.required)"
            @change="(v: boolean) => (form.required = v ? YES : NO)"
          />
        </el-form-item>
        <el-form-item :label="gf('minNum', 'label')">
          <el-input-number v-model="form.minNum" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item :label="gf('maxNum', 'label')">
          <el-input-number v-model="form.maxNum" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item :label="gf('sortNo', 'label')">
          <el-input-number v-model="form.sortNo" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item :label="gf('status', 'label')">
          <el-radio-group v-model="form.status">
            <el-radio :value="STATUS_NORMAL">{{ gp('statusNormal') }}</el-radio>
            <el-radio :value="STATUS_DISABLE">{{ gp('statusDisabled') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="gf('remark', 'label')">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="saveItem">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import GenListLayout from '@/components/gen/layout/GenListLayout.vue'
import {
  isPlatformTenant,
  isStatusDisabled,
  isYes,
  NO,
  STATUS_DISABLE,
  STATUS_NORMAL,
  YES,
} from '@/constants/flags'
import { useUserStore } from '@/stores/user'
import { useGenPageShellI18n } from '@/composables/gen/useGenPageShellI18n'
import {
  listFileCatalogs,
  listFileProfiles,
  PERM_FileProfile,
  upsertFileCatalog,
} from '@/api/infra/fileProfile'
import type {
  FileCatalogItemUpsertRequest,
  FileCatalogItemVO,
  FileCatalogVO,
  FileProfileVO,
} from '@/types/infra/fileProfile'
import { resolveI18nText } from '@/utils/i18nTextMap'
const { gp, gf, gv } = useGenPageShellI18n('fileConfigCatalogItem')
const { t, locale } = useI18n()
const route = useRoute()
const userStore = useUserStore()
const PERM = PERM_FileProfile
const isPlatform = computed(() => isPlatformTenant(userStore.user?.tenantId))

const loading = ref(false)
const saving = ref(false)
const showSearch = ref(true)
const catalogRows = ref<FileCatalogVO[]>([])
const profileRows = ref<FileProfileVO[]>([])
const selectedCatalogCode = ref('')
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  isCreate: true,
  originalProfileCode: '',
  profileCode: '',
  required: NO,
  minNum: null as number | null,
  maxNum: null as number | null,
  sortNo: 10,
  status: STATUS_NORMAL,
  remark: '',
})

const rules = computed<FormRules>(() => ({
  profileCode: [{ required: true, message: gv('profileCode', 'required'), trigger: 'change' }],
}))

const selectedCatalog = computed(
  () => catalogRows.value.find((c) => c.catalogCode === selectedCatalogCode.value) ?? null,
)

const itemRows = computed(() => selectedCatalog.value?.items ?? [])

/** 新增时可选槽位：排除已绑定（编辑时保留当前） */
const availableProfiles = computed(() => {
  const bound = new Set(
    itemRows.value
      .map((i) => i.profileCode)
      .filter((code) => form.isCreate || code !== form.originalProfileCode),
  )
  return profileRows.value.filter((p) => !bound.has(p.profileCode))
})

function displayCatalogName(row: FileCatalogVO) {
  return resolveI18nText(row.catalogNameI18n, row.catalogName, String(locale.value))
}

function displayItemName(row: FileCatalogItemVO) {
  const p = row.resolvedProfile
  if (p) {
    return resolveI18nText(p.profileNameI18n, p.profileName || row.profileName, String(locale.value))
  }
  return row.profileName || '—'
}

function profileExts(code: string) {
  const p = profileRows.value.find((x) => x.profileCode === code)
  return (p?.allowedExts || []).join(',') || '—'
}

function onCatalogChange() {
  // itemRows 随 selectedCatalog 计算
}

function toUpsertPayload(items: FileCatalogItemVO[]): FileCatalogItemUpsertRequest[] {
  return items.map((it) => ({
    profileCode: it.profileCode,
    required: it.required ?? undefined,
    minNum: it.minNum ?? null,
    maxNum: it.maxNum ?? null,
    sortNo: it.sortNo ?? 0,
    status: it.status || STATUS_NORMAL,
    chromeRoles: it.chromeRoles,
    remark: undefined,
  }))
}

async function persistItems(nextItems: FileCatalogItemUpsertRequest[]) {
  const cat = selectedCatalog.value
  if (!cat) return
  await upsertFileCatalog({
    catalogCode: cat.catalogCode,
    catalogName: cat.catalogName,
    catalogNameI18n: cat.catalogNameI18n,
    status: cat.status || STATUS_NORMAL,
    remark: cat.remark || undefined,
    items: nextItems,
  })
}

function openCreate() {
  form.isCreate = true
  form.originalProfileCode = ''
  form.profileCode = ''
  form.required = NO
  form.minNum = null
  form.maxNum = null
  form.sortNo = (itemRows.value.length + 1) * 10
  form.status = STATUS_NORMAL
  form.remark = ''
  dialogVisible.value = true
}

function openEdit(row: FileCatalogItemVO) {
  form.isCreate = false
  form.originalProfileCode = row.profileCode
  form.profileCode = row.profileCode
  form.required = row.required ?? NO
  form.minNum = row.minNum ?? null
  form.maxNum = row.maxNum ?? null
  form.sortNo = row.sortNo ?? 0
  form.status = row.status || STATUS_NORMAL
  form.remark = ''
  dialogVisible.value = true
}

async function saveItem() {
  await formRef.value?.validate()
  const cat = selectedCatalog.value
  if (!cat) return

  const code = form.profileCode.trim()
  if (form.isCreate && itemRows.value.some((i) => i.profileCode === code)) {
    ElMessage.warning(gp('duplicateProfile', { code }))
    return
  }

  const next = toUpsertPayload(itemRows.value)
  const existing = itemRows.value.find(
    (i) => i.profileCode === (form.isCreate ? code : form.originalProfileCode),
  )
  const payload: FileCatalogItemUpsertRequest = {
    profileCode: code,
    required: form.required,
    minNum: form.minNum,
    maxNum: form.maxNum,
    sortNo: form.sortNo,
    status: form.status,
    chromeRoles: existing?.chromeRoles,
    remark: form.remark || undefined,
  }

  if (form.isCreate) {
    next.push(payload)
  } else {
    const idx = next.findIndex((i) => i.profileCode === form.originalProfileCode)
    if (idx >= 0) next[idx] = payload
    else next.push(payload)
  }

  saving.value = true
  try {
    await persistItems(next)
    ElMessage.success(gp('saveSuccess'))
    dialogVisible.value = false
    await reload()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: FileCatalogItemVO) {
  const cat = selectedCatalog.value
  if (!cat) return
  await ElMessageBox.confirm(
    gp('deleteConfirm', { catalog: cat.catalogCode, code: row.profileCode }),
    { type: 'warning' },
  )
  const next = toUpsertPayload(itemRows.value.filter((i) => i.profileCode !== row.profileCode))
  await persistItems(next)
  ElMessage.success(gp('deleteSuccess'))
  await reload()
}

async function reload() {
  loading.value = true
  try {
    const [cr, pr] = await Promise.all([listFileCatalogs(), listFileProfiles()])
    catalogRows.value = cr.data.data ?? []
    profileRows.value = pr.data.data ?? []
  } finally {
    loading.value = false
  }
}

watch(
  () => route.query.catalog,
  (code) => {
    if (typeof code === 'string' && code) {
      selectedCatalogCode.value = code
    }
  },
)

onMounted(async () => {
  await reload()
  const q = route.query.catalog
  if (typeof q === 'string' && q) {
    selectedCatalogCode.value = q
  } else if (catalogRows.value[0]) {
    selectedCatalogCode.value = catalogRows.value[0].catalogCode
  }
})
</script>

<style scoped>
.hint {
  margin: 0 0 8px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>
