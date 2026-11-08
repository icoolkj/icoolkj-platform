<!-- 文件配置中心 · 文件目录 -->
<template>
  <div class="gen-page-shell">
    <GenListLayout
      v-loading="loading"
      :title="gp('title')"
      :description="gp('description')"
      :show-search="false"
      @refresh="loadCatalogs"
    >
      <template #table-top>
        <p v-if="!isPlatform" class="hint">{{ gp('platformOnlyHint') }}</p>
        <p v-else class="hint">{{ gp('platformOnlyHint') }}</p>
      </template>

      <template #toolbar>
        <el-button
          v-if="isPlatform"
          type="primary"
          :icon="Plus"
          v-hasPermi="[PERM.add]"
          @click="openCreate"
        >
          {{ gp('add') }}
        </el-button>
      </template>

      <el-table :data="catalogRows" border stripe>
        <el-table-column :label="gf('catalogName', 'label')" min-width="160">
          <template #default="{ row }">
            {{ displayCatalogName(row) }}
          </template>
        </el-table-column>
        <el-table-column :label="gf('catalogCode', 'label')" prop="catalogCode" min-width="140" />
        <el-table-column :label="gp('itemCount')" width="90" align="center">
          <template #default="{ row }">
            {{ row.items?.length ?? 0 }}
          </template>
        </el-table-column>
        <el-table-column :label="gf('status', 'label')" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="isStatusDisabled(row.status) ? 'danger' : 'success'" size="small">
              {{ isStatusDisabled(row.status) ? gp('statusDisabled') : gp('statusNormal') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="gf('remark', 'label')" prop="remark" min-width="180" show-overflow-tooltip />
        <el-table-column :label="t('common.actions')" width="240" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="goItems(row)">
              {{ gp('manageItems') }}
            </el-button>
            <el-button
              v-if="isPlatform"
              link
              type="primary"
              v-hasPermi="[PERM.update]"
              @click="openEdit(row)"
            >
              {{ gp('edit') }}
            </el-button>
            <el-button
              v-if="isPlatform"
              link
              type="danger"
              v-hasPermi="[PERM.delete]"
              @click="handleDelete(row)"
            >
              {{ gp('delete') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </GenListLayout>

    <el-dialog v-model="dialogVisible" :title="gp('formTitle')" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="140px">
        <el-form-item :label="gf('catalogCode', 'label')" prop="catalogCode">
          <el-input
            v-model="form.catalogCode"
            :disabled="!form.isCreate"
            :placeholder="gf('catalogCode', 'placeholder')"
          />
        </el-form-item>
        <I18nTextMapEditor
          v-model="form.nameI18n"
          :label="gf('catalogName', 'label')"
          :placeholder="gf('catalogName', 'placeholder')"
          prop-path="nameI18n"
          required
        />
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
        <el-button type="primary" :loading="saving" @click="save">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import GenListLayout from '@/components/gen/layout/GenListLayout.vue'
import I18nTextMapEditor from '@/components/I18nTextMapEditor.vue'
import {
  isPlatformTenant,
  isStatusDisabled,
  STATUS_DISABLE,
  STATUS_NORMAL,
} from '@/constants/flags'
import { useLocaleStore } from '@/stores/locale'
import { useGenPageShellI18n } from '@/composables/gen/useGenPageShellI18n'
import { useUserStore } from '@/stores/user'
import {
  deleteFileCatalog,
  listFileCatalogs,
  PERM_FileProfile,
  upsertFileCatalog,
} from '@/api/infra/fileProfile'
import type { FileCatalogVO } from '@/types/infra/fileProfile'
import {
  compactI18nTextMap,
  primaryI18nText,
  resolveI18nText,
  seedI18nTextMap,
  type I18nTextMap,
} from '@/utils/i18nTextMap'
const { gp, gf, gv } = useGenPageShellI18n('fileConfigCatalog')
const { t, locale } = useI18n()
const router = useRouter()
const userStore = useUserStore()
const localeStore = useLocaleStore()
const PERM = PERM_FileProfile
const isPlatform = computed(() => isPlatformTenant(userStore.user?.tenantId))

const localeCodes = computed(() => localeStore.localeOptions.map((o) => o.value))

const loading = ref(false)
const saving = ref(false)
const catalogRows = ref<FileCatalogVO[]>([])
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({
  isCreate: true,
  catalogCode: '',
  nameI18n: {} as I18nTextMap,
  status: STATUS_NORMAL,
  remark: '',
})

const rules = computed<FormRules>(() => ({
  catalogCode: [{ required: true, message: gv('catalogCode', 'required'), trigger: 'blur' }],
}))

function displayCatalogName(row: FileCatalogVO) {
  return resolveI18nText(row.catalogNameI18n, row.catalogName, String(locale.value))
}

function emptyNameI18n(): I18nTextMap {
  return seedI18nTextMap({}, '', localeCodes.value)
}

async function loadCatalogs() {
  loading.value = true
  try {
    const res = await listFileCatalogs()
    catalogRows.value = res.data.data ?? []
  } finally {
    loading.value = false
  }
}

function openCreate() {
  form.isCreate = true
  form.catalogCode = ''
  form.nameI18n = emptyNameI18n()
  form.status = STATUS_NORMAL
  form.remark = ''
  dialogVisible.value = true
}

function openEdit(row: FileCatalogVO) {
  form.isCreate = false
  form.catalogCode = row.catalogCode
  form.nameI18n = seedI18nTextMap(row.catalogNameI18n, row.catalogName, localeCodes.value)
  form.status = row.status || STATUS_NORMAL
  form.remark = row.remark || ''
  dialogVisible.value = true
}

async function save() {
  await formRef.value?.validate()
  const nameI18n = compactI18nTextMap(form.nameI18n)
  const catalogName = primaryI18nText(nameI18n)
  if (!catalogName) {
    ElMessage.warning(gv('catalogName', 'required'))
    return
  }
  saving.value = true
  try {
    await upsertFileCatalog({
      catalogCode: form.catalogCode.trim(),
      catalogName,
      catalogNameI18n: nameI18n ?? {},
      status: form.status,
      remark: form.remark || undefined,
    })
    ElMessage.success(gp('saveSuccess'))
    dialogVisible.value = false
    await loadCatalogs()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: FileCatalogVO) {
  await ElMessageBox.confirm(gp('deleteConfirm', { code: row.catalogCode }), { type: 'warning' })
  await deleteFileCatalog(row.catalogCode)
  ElMessage.success(gp('deleteSuccess'))
  await loadCatalogs()
}

function goItems(row: FileCatalogVO) {
  router.push({
    path: '/infra/fileConfig/catalogItem',
    query: { catalog: row.catalogCode },
  })
}

onMounted(() => loadCatalogs())
</script>

<style scoped>
.hint {
  margin: 0 0 8px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>
