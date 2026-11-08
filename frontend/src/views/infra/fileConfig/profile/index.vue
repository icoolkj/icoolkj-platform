<!-- 文件配置中心 · 槽位配置 -->
<template>
  <div class="gen-page-shell">
    <GenListLayout
      v-loading="loading"
      :title="gp('title')"
      :description="gp('description')"
      :show-search="showSearch"
      @refresh="loadProfiles"
      @toggle-search="showSearch = !showSearch"
    >
      <template #table-top>
        <p v-if="!isPlatform" class="hint">{{ gp('platformOnlyHint') }}</p>
      </template>

      <template #search>
        <el-form :model="query" inline @submit.prevent>
          <el-form-item :label="gf('categoryTag', 'label')">
            <el-select
              v-model="query.categoryTag"
              clearable
              :placeholder="gp('categoryAll')"
              style="width: 160px"
            >
              <el-option v-for="tag in categoryOptions" :key="tag" :label="tag" :value="tag" />
            </el-select>
          </el-form-item>
          <el-form-item :label="gf('profileCode', 'label')">
            <el-input
              v-model="query.keyword"
              :placeholder="gp('keywordPlaceholder')"
              clearable
              style="width: 200px"
              @keyup.enter="loadProfiles"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="loadProfiles">
              {{ t('common.search') }}
            </el-button>
            <el-button :icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
          </el-form-item>
        </el-form>
      </template>

      <template #toolbar>
        <el-button
          v-if="isPlatform"
          type="primary"
          :icon="Plus"
          v-hasPermi="[PERM.add]"
          @click="openCreateProfile"
        >
          {{ gp('add') }}
        </el-button>
        <el-button
          v-if="isPlatform"
          type="danger"
          plain
          :disabled="!selectedCodes.length"
          v-hasPermi="[PERM.delete]"
          @click="handleBatchDelete"
        >
          {{ gp('batchDelete') }}
        </el-button>
      </template>

      <el-table :data="profileRows" border stripe @selection-change="onSelectionChange">
        <el-table-column v-if="isPlatform" type="selection" width="48" align="center" />
        <el-table-column :label="gf('profileCode', 'label')" prop="profileCode" min-width="140" />
        <el-table-column :label="gf('profileName', 'label')" min-width="140">
          <template #default="{ row }">
            {{ displayProfileName(row) }}
          </template>
        </el-table-column>
        <el-table-column :label="gf('categoryTag', 'label')" prop="categoryTag" width="110" />
        <el-table-column :label="gf('allowedExts', 'label')" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            {{ (row.allowedExts || []).join(',') || '—' }}
          </template>
        </el-table-column>
        <el-table-column :label="gf('maxSizeBytes', 'label')" width="120" align="right">
          <template #default="{ row }">
            {{ row.maxSizeBytes ? formatBytes(row.maxSizeBytes) : '—' }}
          </template>
        </el-table-column>
        <el-table-column :label="gf('maxNum', 'label')" prop="maxNum" width="90" align="center" />
        <el-table-column :label="gf('required', 'label')" width="90" align="center">
          <template #default="{ row }">
            {{ row.required ? gp('requiredYes') : gp('requiredNo') }}
          </template>
        </el-table-column>
        <el-table-column :label="gf('status', 'label')" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="isStatusDisabled(row.status) ? 'danger' : 'success'" size="small">
              {{ isStatusDisabled(row.status) ? gp('statusDisabled') : gp('statusNormal') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column width="110" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.tenantOverride" type="warning" size="small">
              {{ gp('tenantOverride') }}
            </el-tag>
            <el-tag v-else type="info" size="small">{{ gp('platformBaseline') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('common.actions')" width="240" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" v-hasPermi="[PERM.update]" @click="openEditProfile(row)">
              {{ isPlatform ? gp('edit') : gp('editRule') }}
            </el-button>
            <el-button
              v-if="row.tenantOverride"
              link
              type="warning"
              v-hasPermi="[PERM.update]"
              @click="handleReset(row)"
            >
              {{ gp('resetOverride') }}
            </el-button>
            <el-button
              v-if="isPlatform"
              link
              type="danger"
              v-hasPermi="[PERM.delete]"
              @click="handleDeleteProfile(row)"
            >
              {{ gp('delete') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </GenListLayout>

    <el-dialog
      v-model="profileDialogVisible"
      :title="profileForm.isCreate ? gp('addTitle') : gp('editTitle', { code: profileForm.profileCode })"
      width="640px"
      destroy-on-close
    >
      <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-width="120px">
        <el-form-item :label="gf('profileCode', 'label')" prop="profileCode">
          <el-input
            v-model="profileForm.profileCode"
            :disabled="!profileForm.isCreate"
            :placeholder="gf('profileCode', 'placeholder')"
          />
        </el-form-item>
        <I18nTextMapEditor
          v-if="isPlatform"
          v-model="profileForm.nameI18n"
          :label="gf('profileName', 'label')"
          :placeholder="gf('profileName', 'placeholder')"
          prop-path="nameI18n"
          :required="profileForm.isCreate"
        />
        <el-form-item v-if="isPlatform" :label="gf('categoryTag', 'label')">
          <el-input v-model="profileForm.categoryTag" :placeholder="gf('categoryTag', 'placeholder')" />
        </el-form-item>
        <el-form-item :label="gf('allowedExts', 'label')">
          <el-input v-model="profileForm.allowedExts" :placeholder="gf('allowedExts', 'placeholder')" />
        </el-form-item>
        <el-form-item :label="gf('allowedContentTypes', 'label')">
          <el-input
            v-model="profileForm.allowedContentTypes"
            :placeholder="gf('allowedContentTypes', 'placeholder')"
          />
        </el-form-item>
        <el-form-item :label="gf('required', 'label')">
          <el-switch v-model="profileForm.required" />
        </el-form-item>
        <el-form-item :label="gf('minNum', 'label')">
          <el-input-number v-model="profileForm.minNum" :min="0" />
        </el-form-item>
        <el-form-item :label="gf('maxNum', 'label')">
          <el-input-number v-model="profileForm.maxNum" :min="0" />
        </el-form-item>
        <el-form-item :label="gf('minSizeBytes', 'label')">
          <el-input-number v-model="profileForm.minSizeBytes" :min="0" :step="1024" />
        </el-form-item>
        <el-form-item :label="gf('maxSizeBytes', 'label')">
          <el-input-number v-model="profileForm.maxSizeBytes" :min="0" :step="1024" />
        </el-form-item>
        <el-form-item :label="gf('sortNo', 'label')">
          <el-input-number v-model="profileForm.sortNo" :min="0" />
        </el-form-item>
        <el-form-item v-if="isPlatform" :label="gf('status', 'label')">
          <el-radio-group v-model="profileForm.status">
            <el-radio :value="STATUS_NORMAL">{{ gp('statusNormal') }}</el-radio>
            <el-radio :value="STATUS_DISABLE">{{ gp('statusDisabled') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="isPlatform" :label="gf('remark', 'label')">
          <el-input v-model="profileForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="profileDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="saveProfile">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import GenListLayout from '@/components/gen/layout/GenListLayout.vue'
import I18nTextMapEditor from '@/components/I18nTextMapEditor.vue'
import {
  isPlatformTenant,
  isStatusDisabled,
  NO,
  STATUS_DISABLE,
  STATUS_NORMAL,
  YES,
} from '@/constants/flags'
import { useLocaleStore } from '@/stores/locale'
import { useGenPageShellI18n } from '@/composables/gen/useGenPageShellI18n'
import { useUserStore } from '@/stores/user'
import {
  batchDeleteFileProfiles,
  deleteFileProfile,
  listFileProfiles,
  PERM_FileProfile,
  resetFileProfileOverride,
  upsertFileProfile,
} from '@/api/infra/fileProfile'
import type { FileProfileVO } from '@/types/infra/fileProfile'
import {
  compactI18nTextMap,
  primaryI18nText,
  resolveI18nText,
  seedI18nTextMap,
  type I18nTextMap,
} from '@/utils/i18nTextMap'
const { gp, gf, gv } = useGenPageShellI18n('fileConfigProfile')
const { t, locale } = useI18n()
const userStore = useUserStore()
const localeStore = useLocaleStore()
const PERM = PERM_FileProfile

const isPlatform = computed(() => isPlatformTenant(userStore.user?.tenantId))
const localeCodes = computed(() => localeStore.localeOptions.map((o) => o.value))

const loading = ref(false)
const saving = ref(false)
const showSearch = ref(true)
const profileRows = ref<FileProfileVO[]>([])
const selectedCodes = ref<string[]>([])
const query = reactive({ categoryTag: '', keyword: '' })
const allCategoryTags = ref<string[]>([])
const categoryOptions = computed(() => {
  const set = new Set(allCategoryTags.value)
  for (const row of profileRows.value) {
    if (row.categoryTag) set.add(row.categoryTag)
  }
  return [...set].sort()
})

const profileDialogVisible = ref(false)
const profileFormRef = ref<FormInstance>()
const profileForm = reactive({
  isCreate: true,
  profileCode: '',
  nameI18n: {} as I18nTextMap,
  categoryTag: '',
  allowedExts: '',
  allowedContentTypes: '',
  required: false,
  minNum: 0,
  maxNum: 1,
  minSizeBytes: 0,
  maxSizeBytes: 0,
  sortNo: 0,
  status: STATUS_NORMAL,
  remark: '',
})

const profileRules = computed<FormRules>(() => ({
  profileCode: [{ required: true, message: gv('profileCode', 'required'), trigger: 'blur' }],
}))

function displayProfileName(row: FileProfileVO) {
  return resolveI18nText(row.profileNameI18n, row.profileName, String(locale.value))
}

function emptyNameI18n(): I18nTextMap {
  return seedI18nTextMap({}, '', localeCodes.value)
}

function formatBytes(n: number) {
  if (n < 1024) return `${n} B`
  if (n < 1048576) return `${(n / 1024).toFixed(1)} KB`
  return `${(n / 1048576).toFixed(1)} MB`
}

async function loadProfiles() {
  loading.value = true
  try {
    const res = await listFileProfiles({
      categoryTag: query.categoryTag || undefined,
      keyword: query.keyword || undefined,
    })
    profileRows.value = res.data.data ?? []
    if (!query.categoryTag && !query.keyword) {
      allCategoryTags.value = profileRows.value
        .map((r) => r.categoryTag)
        .filter((v): v is string => !!v)
    }
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.categoryTag = ''
  query.keyword = ''
  loadProfiles()
}

function onSelectionChange(rows: FileProfileVO[]) {
  selectedCodes.value = rows.map((r) => r.profileCode)
}

function resetProfileForm() {
  profileForm.isCreate = true
  profileForm.profileCode = ''
  profileForm.nameI18n = emptyNameI18n()
  profileForm.categoryTag = ''
  profileForm.allowedExts = ''
  profileForm.allowedContentTypes = ''
  profileForm.required = false
  profileForm.minNum = 0
  profileForm.maxNum = 1
  profileForm.minSizeBytes = 0
  profileForm.maxSizeBytes = 0
  profileForm.sortNo = 0
  profileForm.status = STATUS_NORMAL
  profileForm.remark = ''
}

function openCreateProfile() {
  resetProfileForm()
  profileDialogVisible.value = true
}

function openEditProfile(row: FileProfileVO) {
  profileForm.isCreate = false
  profileForm.profileCode = row.profileCode
  profileForm.nameI18n = seedI18nTextMap(row.profileNameI18n, row.profileName, localeCodes.value)
  profileForm.categoryTag = row.categoryTag || ''
  profileForm.allowedExts = (row.allowedExts || []).join(',')
  profileForm.allowedContentTypes = (row.allowedContentTypes || []).join(',')
  profileForm.required = !!row.required
  profileForm.minNum = row.minNum ?? 0
  profileForm.maxNum = row.maxNum ?? 1
  profileForm.minSizeBytes = row.minSizeBytes ?? 0
  profileForm.maxSizeBytes = row.maxSizeBytes ?? 0
  profileForm.sortNo = row.sortNo ?? 0
  profileForm.status = row.status || STATUS_NORMAL
  profileForm.remark = row.remark || ''
  profileDialogVisible.value = true
}

async function saveProfile() {
  await profileFormRef.value?.validate()
  const nameI18n = isPlatform.value ? compactI18nTextMap(profileForm.nameI18n) : undefined
  const profileName = isPlatform.value ? primaryI18nText(nameI18n) : undefined
  if (isPlatform.value && profileForm.isCreate && !profileName) {
    ElMessage.warning(gv('profileName', 'required'))
    return
  }
  saving.value = true
  try {
    await upsertFileProfile({
      profileCode: profileForm.profileCode.trim(),
      profileName: profileName || undefined,
      profileNameI18n: isPlatform.value ? nameI18n ?? {} : undefined,
      categoryTag: profileForm.categoryTag || undefined,
      allowedExts: profileForm.allowedExts,
      allowedContentTypes: profileForm.allowedContentTypes,
      required: profileForm.required ? YES : NO,
      minNum: profileForm.minNum,
      maxNum: profileForm.maxNum,
      minSizeBytes: profileForm.minSizeBytes,
      maxSizeBytes: profileForm.maxSizeBytes,
      sortNo: profileForm.sortNo,
      status: profileForm.status,
      remark: profileForm.remark || undefined,
    })
    ElMessage.success(gp('saveSuccess'))
    profileDialogVisible.value = false
    await loadProfiles()
  } finally {
    saving.value = false
  }
}

async function handleDeleteProfile(row: FileProfileVO) {
  await ElMessageBox.confirm(gp('deleteConfirm', { code: row.profileCode }), { type: 'warning' })
  await deleteFileProfile(row.profileCode)
  ElMessage.success(gp('deleteSuccess'))
  await loadProfiles()
}

async function handleBatchDelete() {
  await ElMessageBox.confirm(gp('batchDeleteConfirm', { count: selectedCodes.value.length }), {
    type: 'warning',
  })
  await batchDeleteFileProfiles({ profileCodes: selectedCodes.value })
  ElMessage.success(gp('deleteSuccess'))
  await loadProfiles()
}

async function handleReset(row: FileProfileVO) {
  await ElMessageBox.confirm(gp('resetConfirm', { code: row.profileCode }), { type: 'warning' })
  await resetFileProfileOverride(row.profileCode)
  ElMessage.success(gp('resetSuccess'))
  await loadProfiles()
}

onMounted(() => loadProfiles())
</script>

<style scoped>
.hint {
  margin: 0 0 8px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>
