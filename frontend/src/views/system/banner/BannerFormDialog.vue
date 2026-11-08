<template>
  <GenFormShell
    v-model="visibleModel"
    :title="dialogTitle"
    :width="860"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="108px">
      <el-form-item :label="gf('bannerTitle', 'label')" prop="bannerTitle">
        <el-input
          v-model="form.bannerTitle"
          :placeholder="gf('bannerTitle', 'placeholder')"
          maxlength="200"
        />
      </el-form-item>
      <el-form-item :label="gf('sortNo', 'label')" prop="sortNo">
        <el-input-number v-model="form.sortNo" :min="0" controls-position="right" />
      </el-form-item>
      <el-form-item :label="gf('imageFileId', 'label')" prop="imageFileId">
        <AttachmentManager
          v-model="form.imageFileId"
          v-model:preview-url="form.imagePreviewUrl"
          :biz-type="FILE_BIZ_BANNER_IMAGE"
          :download-scope="FILE_DOWNLOAD_SCOPE_INFRA"
          mode="single"
          variant="image"
          :gp="gp"
        />
      </el-form-item>
      <el-form-item :label="gf('displayPositions', 'label')" prop="displayPositions">
        <el-checkbox-group v-model="selectedPositions" @change="syncPositionConfigRows">
          <el-checkbox v-for="item in positionOptions" :key="item.value" :value="item.value">
            {{ item.label }}
          </el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <div v-if="positionConfigRows.length" class="position-config-block">
        <div class="position-config-title">{{ gp('positionConfigTitle') }}</div>
        <el-table :data="positionConfigRows" border size="small">
          <el-table-column :label="gp('positionColumn')" min-width="140">
            <template #default="{ row }">
              {{ resolvePositionLabel(row.position) }}
            </template>
          </el-table-column>
          <el-table-column :label="gp('clickEvent')" width="160" align="center">
            <template #default="{ row }">
              <el-radio-group v-model="row.clickEnabled" size="small">
                <el-radio :value="true">{{ gp('clickEnabled') }}</el-radio>
                <el-radio :value="false">{{ gp('clickDisabled') }}</el-radio>
              </el-radio-group>
            </template>
          </el-table-column>
          <el-table-column :label="gp('linkType')" width="160">
            <template #default="{ row }">
              <el-select
                v-model="row.linkType"
                :disabled="!row.clickEnabled"
                size="small"
                style="width: 100%"
              >
                <el-option
                  v-for="item in linkTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column :label="gp('linkUrl')" min-width="220">
            <template #default="{ row }">
              <el-input
                v-model="row.linkUrl"
                :disabled="!row.clickEnabled || row.linkType === BANNER_LINK_NONE"
                size="small"
                :placeholder="gp('linkUrlPlaceholder')"
              />
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-form>
    <template #footer>
      <el-button @click="visibleModel = false">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">
        {{ t('common.confirm') }}
      </el-button>
    </template>
  </GenFormShell>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import GenFormShell from '@/components/gen/layout/GenFormShell.vue'
import { AttachmentManager } from '@/components/attachment'
import { FILE_BIZ_BANNER_IMAGE, FILE_DOWNLOAD_SCOPE_INFRA } from '@/constants/file'
import { useDict } from '@/composables/useDict'
import { BANNER_LINK_NONE, STATUS_NORMAL } from '@/constants/flags'
import type { BannerForm, BannerPositionConfigItem } from '@/types/system/banner'
import type { GenFieldValidation } from '@/types/tool/genI18n'
import { createBanner, getBanner, updateBanner } from '@/api/system/banner'

const props = defineProps<{
  modelValue: boolean
  bannerId?: string
  gp: (key: string, params?: Record<string, string | number>) => string
  gf: (field: string, prop?: 'label' | 'placeholder') => string
  gv: (field: string, rule: keyof GenFieldValidation) => string
}>()

const emit = defineEmits<{
  'update:modelValue': [boolean]
  success: []
}>()

const { t } = useI18n()
const { getDict } = useDict(['sys_banner_display_position', 'sys_banner_link_type'])

const formRef = ref<FormInstance>()
const submitting = ref(false)
const selectedPositions = ref<string[]>([])
const positionConfigRows = ref<BannerPositionConfigItem[]>([])

const form = reactive<BannerForm>({
  bannerTitle: '',
  imageFileId: '',
  imagePreviewUrl: '',
  displayPositions: '',
  positionConfig: '',
  sortNo: 1,
  status: STATUS_NORMAL,
})

const visibleModel = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
})

const dialogTitle = computed(() => (props.bannerId ? props.gp('editTitle') : props.gp('addTitle')))

const positionOptions = computed(() => getDict('sys_banner_display_position'))
const linkTypeOptions = computed(() => getDict('sys_banner_link_type'))

const rules: FormRules = {
  bannerTitle: [{ required: true, message: props.gv('bannerTitle', 'required'), trigger: 'blur' }],
  sortNo: [{ required: true, message: props.gv('sortNo', 'required'), trigger: 'change' }],
  imageFileId: [
    { required: true, message: props.gv('imageFileId', 'required'), trigger: 'change' },
  ],
  displayPositions: [
    {
      validator: (_rule, _value, callback) => {
        if (!selectedPositions.value.length) {
          callback(new Error(props.gp('displayPositionsRequired')))
          return
        }
        callback()
      },
      trigger: 'change',
    },
  ],
}

watch(
  () => props.modelValue,
  async (visible) => {
    if (!visible) return
    resetForm()
    if (props.bannerId) {
      await loadDetail(props.bannerId)
    }
  },
)

function resetForm() {
  form.id = undefined
  form.bannerTitle = ''
  form.imageFileId = ''
  form.imagePreviewUrl = ''
  form.displayPositions = ''
  form.positionConfig = ''
  form.sortNo = 1
  form.status = STATUS_NORMAL
  selectedPositions.value = []
  positionConfigRows.value = []
  formRef.value?.clearValidate()
}

async function loadDetail(id: string) {
  const res = await getBanner(id)
  const data = res.data.data
  form.id = data.id
  form.bannerTitle = data.bannerTitle ?? ''
  form.imageFileId = data.imageFileId ?? ''
  form.imagePreviewUrl = data.imagePreviewUrl ?? ''
  form.displayPositions = data.displayPositions ?? ''
  form.sortNo = data.sortNo ?? 1
  form.status = data.status ?? STATUS_NORMAL
  selectedPositions.value = splitCsv(form.displayPositions)
  positionConfigRows.value = parsePositionConfig(data.positionConfig, selectedPositions.value)
}

function splitCsv(value?: string): string[] {
  if (!value) return []
  return value
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

function parsePositionConfig(
  raw: string | undefined,
  positions: string[],
): BannerPositionConfigItem[] {
  let parsed: BannerPositionConfigItem[] = []
  if (raw) {
    try {
      parsed = JSON.parse(raw) as BannerPositionConfigItem[]
    } catch {
      parsed = []
    }
  }
  const map = new Map(parsed.map((item) => [item.position, item]))
  return positions.map((position) => {
    const existing = map.get(position)
    return {
      position,
      clickEnabled: existing?.clickEnabled ?? false,
      linkType: existing?.linkType ?? BANNER_LINK_NONE,
      linkUrl: existing?.linkUrl ?? '',
    }
  })
}

function syncPositionConfigRows() {
  const current = new Map(positionConfigRows.value.map((row) => [row.position, row]))
  positionConfigRows.value = selectedPositions.value.map((position) => {
    const existing = current.get(position)
    return (
      existing ?? {
        position,
        clickEnabled: false,
        linkType: BANNER_LINK_NONE,
        linkUrl: '',
      }
    )
  })
  form.displayPositions = selectedPositions.value.join(',')
}

function resolvePositionLabel(value: string) {
  return positionOptions.value.find((item) => item.value === value)?.label ?? value
}

function buildPayload(): BannerForm {
  return {
    ...form,
    displayPositions: selectedPositions.value.join(','),
    positionConfig: JSON.stringify(positionConfigRows.value),
    status: STATUS_NORMAL,
  }
}

async function submit() {
  if (!formRef.value) return
  syncPositionConfigRows()
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const payload = buildPayload()
    if (form.id) {
      await updateBanner(payload)
    } else {
      await createBanner(payload)
    }
    ElMessage.success(t('common.success'))
    visibleModel.value = false
    emit('success')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.position-config-block {
  margin-top: 8px;
}

.position-config-title {
  margin-bottom: 8px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}
</style>
