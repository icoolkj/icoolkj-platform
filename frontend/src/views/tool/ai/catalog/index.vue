<template>
  <div class="gen-page-shell">
    <GenListLayout
      :title="gp('menuCatalog')"
      :description="gp('catalogDescription')"
      :show-search="false"
      :show-right-toolbar="false"
    >
      <el-tabs v-model="activeTab">
        <el-tab-pane :label="gp('catalogTabBind')" name="bind">
          <div class="toolbar">
            <el-button v-hasPermi="[PERM.update]" :loading="saving" @click="testChat">{{ gp('testChat') }}</el-button>
            <el-button v-hasPermi="[PERM.update]" :loading="saving" @click="testImage">{{ gp('testImage') }}</el-button>
          </div>
          <el-table v-loading="loading" :data="bindings" border stripe>
            <el-table-column prop="capability" :label="gp('catalogColCapability')" width="120" />
            <el-table-column :label="gp('catalogColModel')" min-width="200">
              <template #default="{ row }">
                {{ row.modelName || '—' }}
                <span v-if="row.modelCode" class="muted">({{ row.modelCode }})</span>
              </template>
            </el-table-column>
            <el-table-column prop="providerName" :label="gp('catalogColProvider')" min-width="140" />
            <el-table-column :label="gp('catalogColSource')" width="120">
              <template #default="{ row }">
                <el-tag size="small" :type="row.usingPlatformDefault ? 'info' : 'success'">
                  {{ row.usingPlatformDefault ? gp('catalogSourcePlatform') : gp('catalogSourceTenant') }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column :label="gp('catalogColActions')" width="220" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" v-hasPermi="[PERM.update]" @click="openBind(row)">
                  {{ gp('catalogSelectBind') }}
                </el-button>
                <el-button
                  v-if="!row.usingPlatformDefault"
                  link
                  v-hasPermi="[PERM.update]"
                  @click="resetBind(row.capability)"
                >
                  {{ gp('catalogResetPlatform') }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane :label="gp('catalogTabProvider')" name="provider">
          <div class="toolbar">
            <el-button type="primary" v-hasPermi="[PERM.add]" @click="openProvider()">
              {{ gp('catalogCreateProvider') }}
            </el-button>
          </div>
          <el-table v-loading="loading" :data="providers" border stripe>
            <el-table-column prop="providerName" :label="gp('catalogColName')" min-width="140" />
            <el-table-column prop="providerCode" :label="gp('catalogColCode')" width="140" />
            <el-table-column prop="baseUrl" label="Base URL" min-width="200" show-overflow-tooltip />
            <el-table-column label="Key" width="120">
              <template #default="{ row }">
                {{ row.apiKeyConfigured ? row.apiKeyMasked : '—' }}
              </template>
            </el-table-column>
            <el-table-column :label="gp('catalogColOwner')" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="row.platformOwned ? 'info' : ''">
                  {{ row.platformOwned ? gp('catalogOwnerPlatform') : gp('catalogOwnerTenant') }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column :label="gp('catalogColActions')" width="220" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="row.platformOwned && !isPlatformTenant"
                  link
                  type="primary"
                  v-hasPermi="[PERM.update]"
                  @click="openAdopt(row)"
                >
                  {{ gp('catalogAdoptKey') }}
                </el-button>
                <el-button
                  v-if="!row.platformOwned || isPlatformTenant"
                  link
                  type="primary"
                  v-hasPermi="[PERM.update]"
                  @click="openProvider(row)"
                >
                  {{ t('common.edit') }}
                </el-button>
                <el-button
                  v-if="!row.platformOwned || isPlatformTenant"
                  link
                  type="danger"
                  v-hasPermi="[PERM.delete]"
                  @click="removeProvider(row)"
                >
                  {{ t('common.delete') }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane :label="gp('catalogTabModel')" name="model">
          <div class="toolbar">
            <el-button type="primary" v-hasPermi="[PERM.add]" @click="openModel()">
              {{ gp('catalogCreateModel') }}
            </el-button>
          </div>
          <el-table v-loading="loading" :data="models" border stripe>
            <el-table-column prop="modelName" :label="gp('catalogColName')" min-width="140" />
            <el-table-column prop="modelCode" label="Model" width="160" />
            <el-table-column prop="capabilities" :label="gp('catalogColCapability')" width="140" />
            <el-table-column prop="providerName" :label="gp('catalogColProvider')" min-width="140" />
            <el-table-column :label="gp('catalogColOwner')" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="row.platformOwned ? 'info' : ''">
                  {{ row.platformOwned ? gp('catalogOwnerPlatform') : gp('catalogOwnerTenant') }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column :label="gp('catalogColActions')" width="160" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="!row.platformOwned || isPlatformTenant"
                  link
                  type="primary"
                  v-hasPermi="[PERM.update]"
                  @click="openModel(row)"
                >
                  {{ t('common.edit') }}
                </el-button>
                <el-button
                  v-if="!row.platformOwned || isPlatformTenant"
                  link
                  type="danger"
                  v-hasPermi="[PERM.delete]"
                  @click="removeModel(row)"
                >
                  {{ t('common.delete') }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </GenListLayout>

    <el-dialog
      v-model="providerVisible"
      :title="providerForm.id ? gp('catalogEditProvider') : gp('catalogCreateProvider')"
      width="560px"
      destroy-on-close
    >
      <el-form label-width="110px">
        <el-form-item :label="gp('catalogFieldCode')" required>
          <el-input v-model="providerForm.providerCode" :disabled="!!providerForm.id" maxlength="64" />
        </el-form-item>
        <el-form-item :label="gp('catalogFieldName')" required>
          <el-input v-model="providerForm.providerName" maxlength="128" />
        </el-form-item>
        <el-form-item :label="gp('catalogFieldProtocol')">
          <el-select v-model="providerForm.protocol" style="width: 100%">
            <el-option
              v-for="item in AI_PROTOCOLS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="Base URL">
          <el-input
            v-model="providerForm.baseUrl"
            :placeholder="gp('catalogBaseUrlPlaceholder')"
          />
        </el-form-item>
        <el-form-item label="API Key">
          <el-input
            v-model="providerForm.apiKey"
            type="password"
            show-password
            :placeholder="gp('catalogApiKeyKeepPlaceholder')"
          />
        </el-form-item>
        <el-form-item :label="gp('catalogFieldTimeout')">
          <el-input-number v-model="providerForm.timeoutSeconds" :min="5" :max="600" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="providerVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="saveProvider">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="adoptVisible" :title="gp('catalogAdoptKey')" width="520px" destroy-on-close>
      <p class="hint">
        {{ gp('catalogAdoptHint', { name: adoptTarget?.providerName || '' }) }}
      </p>
      <el-form label-width="110px">
        <el-form-item label="API Key" required>
          <el-input
            v-model="adoptApiKey"
            type="password"
            show-password
            :placeholder="gp('catalogApiKeyRequiredPlaceholder')"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adoptVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="saveAdopt">{{ t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="modelVisible"
      :title="modelForm.id ? gp('catalogEditModel') : gp('catalogCreateModel')"
      width="560px"
      destroy-on-close
    >
      <el-form label-width="110px">
        <el-form-item :label="gp('catalogColProvider')" required>
          <el-select v-model="modelForm.providerId" style="width: 100%">
            <el-option
              v-for="p in ownedProviders"
              :key="p.id"
              :label="p.providerName"
              :value="p.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="Model" required>
          <el-input v-model="modelForm.modelCode" maxlength="128" />
        </el-form-item>
        <el-form-item :label="gp('catalogFieldName')" required>
          <el-input v-model="modelForm.modelName" maxlength="128" />
        </el-form-item>
        <el-form-item :label="gp('catalogFieldCapability')" required>
          <el-checkbox-group v-model="modelCapabilityCodes">
            <el-checkbox
              v-for="item in AI_CAPABILITIES"
              :key="item.value"
              :value="item.value"
              :disabled="!item.implemented"
            >
              {{ item.label }}
              <span v-if="!item.implemented" class="muted">{{ gp('catalogCapabilityNotImplemented') }}</span>
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="modelVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="saveModel">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="bindVisible" :title="gp('catalogSelectModelTitle')" width="480px" destroy-on-close>
      <el-form label-width="90px">
        <el-form-item :label="gp('catalogFieldCapability')">{{ bindCapability }}</el-form-item>
        <el-form-item :label="gp('catalogFieldModel')">
          <el-select v-model="bindModelId" filterable style="width: 100%" clearable>
            <el-option
              v-for="m in modelsForCapability"
              :key="m.id"
              :label="`${m.modelName} (${m.providerName || ''})`"
              :value="m.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bindVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="saveBind">{{ t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import GenListLayout from '@/components/gen/layout/GenListLayout.vue'
import { useToolAiI18n } from '@/composables/useToolAiI18n'
import { useUserStore } from '@/stores/user'
import { isPlatformTenant as matchPlatformTenant } from '@/constants/flags'
import {
  AI_CAPABILITIES,
  AI_CAPABILITY_DEFAULT,
  AI_PROTOCOL_DEFAULT,
  AI_PROTOCOLS,
  joinCapabilities,
  parseCapabilities,
} from '@/constants/aiCatalog'
import {
  PERM_AiCatalog as PERM,
  bindAiCapability,
  createAiModel,
  createAiProvider,
  adoptPlatformProvider,
  deleteAiModel,
  deleteAiProvider,
  listAiCapabilityBindings,
  listAiModels,
  listAiProviders,
  testAiCapability,
  updateAiModel,
  updateAiProvider,
  type AiCapabilityBindingVO,
  type AiModelVO,
  type AiProviderVO,
} from '@/api/tool/ai/catalog'

const { gp } = useToolAiI18n()
const { t } = useI18n()
const userStore = useUserStore()
const isPlatformTenant = computed(() => matchPlatformTenant(userStore.user?.tenantId))

const activeTab = ref('bind')
const loading = ref(false)
const saving = ref(false)
const providers = ref<AiProviderVO[]>([])
const models = ref<AiModelVO[]>([])
const bindings = ref<AiCapabilityBindingVO[]>([])

const providerVisible = ref(false)
const providerForm = reactive({
  id: '',
  providerCode: '',
  providerName: '',
  protocol: AI_PROTOCOL_DEFAULT,
  baseUrl: '',
  apiKey: '',
  timeoutSeconds: 60,
})

const adoptVisible = ref(false)
const adoptTarget = ref<AiProviderVO | null>(null)
const adoptApiKey = ref('')

const modelVisible = ref(false)
const modelForm = reactive({
  id: '',
  providerId: '',
  modelCode: '',
  modelName: '',
})
const modelCapabilityCodes = ref<string[]>([AI_CAPABILITY_DEFAULT])

const bindVisible = ref(false)
const bindCapability = ref('')
const bindModelId = ref<string | undefined>()

const ownedProviders = computed(() =>
  providers.value.filter((p) => !p.platformOwned || isPlatformTenant.value),
)

const modelsForCapability = computed(() =>
  models.value.filter((m) =>
    (m.capabilities || '')
      .split(',')
      .map((x) => x.trim())
      .includes(bindCapability.value),
  ),
)

async function loadAll() {
  loading.value = true
  try {
    const [p, m, b] = await Promise.all([
      listAiProviders(),
      listAiModels(),
      listAiCapabilityBindings(),
    ])
    providers.value = p.data.data || []
    models.value = m.data.data || []
    bindings.value = b.data.data || []
  } finally {
    loading.value = false
  }
}

function openProvider(row?: AiProviderVO) {
  providerForm.id = row?.id || ''
  providerForm.providerCode = row?.providerCode || ''
  providerForm.providerName = row?.providerName || ''
  providerForm.protocol = (row?.protocol || AI_PROTOCOL_DEFAULT) as typeof providerForm.protocol
  providerForm.baseUrl = row?.baseUrl || ''
  providerForm.apiKey = ''
  providerForm.timeoutSeconds = row?.timeoutSeconds ?? 60
  providerVisible.value = true
}

async function saveProvider() {
  saving.value = true
  try {
    const payload = { ...providerForm, id: providerForm.id || undefined }
    if (providerForm.id) await updateAiProvider(payload)
    else await createAiProvider(payload)
    ElMessage.success(t('common.success'))
    providerVisible.value = false
    await loadAll()
  } finally {
    saving.value = false
  }
}

async function removeProvider(row: AiProviderVO) {
  await ElMessageBox.confirm(gp('catalogDeleteProviderConfirm', { name: row.providerName }), t('common.tip'), {
    type: 'warning',
  })
  await deleteAiProvider(row.id)
  ElMessage.success(t('common.success'))
  await loadAll()
}

function openAdopt(row: AiProviderVO) {
  adoptTarget.value = row
  adoptApiKey.value = ''
  adoptVisible.value = true
}

async function saveAdopt() {
  if (!adoptTarget.value) return
  if (!adoptApiKey.value.trim()) {
    ElMessage.warning(gp('catalogAdoptKeyRequired'))
    return
  }
  saving.value = true
  try {
    await adoptPlatformProvider(adoptTarget.value.id, adoptApiKey.value.trim())
    ElMessage.success(gp('catalogAdoptSuccess'))
    adoptVisible.value = false
    await loadAll()
  } finally {
    saving.value = false
  }
}

function openModel(row?: AiModelVO) {
  modelForm.id = row?.id || ''
  modelForm.providerId = row?.providerId || ownedProviders.value[0]?.id || ''
  modelForm.modelCode = row?.modelCode || ''
  modelForm.modelName = row?.modelName || ''
  modelCapabilityCodes.value = parseCapabilities(row?.capabilities)
  modelVisible.value = true
}

async function saveModel() {
  if (!modelCapabilityCodes.value.length) {
    ElMessage.warning(gp('catalogCapabilityRequired'))
    return
  }
  saving.value = true
  try {
    const payload = {
      ...modelForm,
      id: modelForm.id || undefined,
      capabilities: joinCapabilities(modelCapabilityCodes.value),
    }
    if (modelForm.id) await updateAiModel(payload)
    else await createAiModel(payload)
    ElMessage.success(t('common.success'))
    modelVisible.value = false
    await loadAll()
  } finally {
    saving.value = false
  }
}

async function removeModel(row: AiModelVO) {
  await ElMessageBox.confirm(gp('catalogDeleteModelConfirm', { name: row.modelName }), t('common.tip'), {
    type: 'warning',
  })
  await deleteAiModel(row.id)
  ElMessage.success(t('common.success'))
  await loadAll()
}

function openBind(row: AiCapabilityBindingVO) {
  bindCapability.value = row.capability
  bindModelId.value = row.modelId
  bindVisible.value = true
}

async function saveBind() {
  saving.value = true
  try {
    await bindAiCapability({ capability: bindCapability.value, modelId: bindModelId.value })
    ElMessage.success(gp('catalogUpdated'))
    bindVisible.value = false
    await loadAll()
  } finally {
    saving.value = false
  }
}

async function resetBind(capability: string) {
  await ElMessageBox.confirm(gp('catalogResetBindConfirm'), t('common.tip'), { type: 'warning' })
  await bindAiCapability({ capability, modelId: undefined })
  ElMessage.success(gp('catalogResetSuccess'))
  await loadAll()
}

async function testChat() {
  saving.value = true
  try {
    await testAiCapability('chat')
    ElMessage.success(gp('testSuccess'))
  } finally {
    saving.value = false
  }
}

async function testImage() {
  saving.value = true
  try {
    await testAiCapability('image')
    ElMessage.success(gp('testSuccess'))
  } finally {
    saving.value = false
  }
}

onMounted(loadAll)
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
}
.muted {
  color: var(--el-text-color-secondary);
  margin-left: 4px;
}
.hint {
  margin: 0 0 16px;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
  font-size: 13px;
}
</style>
