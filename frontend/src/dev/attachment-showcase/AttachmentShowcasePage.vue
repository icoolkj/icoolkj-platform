<!--
  附件场景测试 — view.json + zh-CN/en-US 驱动（与业务页 gen 规范一致）。
  打开：登录后 /dev/attachment-showcase
-->
<template>
  <div class="attachment-showcase">
    <header class="attachment-showcase__hero">
      <div>
        <h1>{{ gp('title') }}</h1>
        <p>{{ gp('description') }}</p>
      </div>
      <el-tag type="warning" effect="plain">{{ gp('devTag') }}</el-tag>
    </header>

    <el-alert
      class="attachment-showcase__guide"
      type="info"
      :closable="false"
      :title="gp('pickTitle')"
    >
      <ul>
        <li v-for="key in pickGuideKeys" :key="key">{{ gp(key) }}</li>
      </ul>
      <p class="attachment-showcase__biz-hint">{{ gp('bizLoopHint') }}</p>
    </el-alert>

    <el-tabs v-model="activeTab" type="border-card" class="attachment-showcase__tabs">
      <el-tab-pane
        v-for="scenario in scenarios"
        :key="scenario.id"
        :label="gp(scenario.tabLabelKey)"
        :name="scenario.id"
      >
        <ScenarioMeta
          :component-name="resolveComponentLabel(scenario)"
          :when="gp(scenario.whenKey)"
          :import-hint="gp(scenario.snippetKey)"
          :copy-label="gp('copySnippet')"
          :copied-label="gp('copied')"
        />

        <!-- AttachmentManager（单图 / 多文件 / 轮播等同一组件，只换 bizType） -->
        <template v-if="scenario.component === 'AttachmentManager'">
          <AttachmentManager
            v-if="scenario.mode === 'single' && scenario.variant === 'image'"
            v-model="demo.singleId"
            v-model:preview-url="demo.singlePreview"
            :biz-type="resolveBizType(scenario.bizTypeConst)"
            :download-scope="resolveDownloadScope(scenario.downloadScope)"
            mode="single"
            variant="image"
            :gp="gp"
          />
          <AttachmentManager
            v-else
            v-model="demo.multiIds"
            :biz-type="resolveBizType(scenario.bizTypeConst)"
            :download-scope="resolveDownloadScope(scenario.downloadScope)"
            mode="multiple"
            variant="list"
            :max-files="scenario.maxFiles"
            :gp="gp"
          />
          <StateDump
            :label="gp('stateLabel')"
            :value="
              scenario.mode === 'single'
                ? { fileId: demo.singleId, preview: demo.singlePreview }
                : { fileIds: demo.multiIds }
            "
          />
        </template>

        <!-- CatalogAttachmentManager：目录编码来自 view.json fileCatalogs -->
        <template v-else-if="scenario.component === 'CatalogAttachmentManager'">
          <el-form inline class="attachment-showcase__toolbar">
            <el-form-item :label="gf('catalogCode')">
              <el-select
                v-model="catalogCode"
                filterable
                style="width: 260px"
                :placeholder="gf('catalogCode', 'placeholder')"
              >
                <el-option
                  v-for="opt in catalogOptionsFromView"
                  :key="opt.code"
                  :label="opt.label"
                  :value="opt.code"
                />
              </el-select>
            </el-form-item>
            <el-form-item :label="gf('bizId')">
              <el-input
                v-model="demoBizId"
                clearable
                style="width: 200px"
                :placeholder="gf('bizId', 'placeholder')"
              />
            </el-form-item>
          </el-form>
          <CatalogAttachmentManager
            v-if="catalogCode"
            v-model="demo.catalogValue"
            :catalog-code="catalogCode"
            :biz-id="demoBizId || undefined"
            :download-scope="resolveDownloadScope(scenario.downloadScope)"
            :gp="gp"
            @change="onCatalogChange"
          />
          <StateDump
            :label="gp('stateLabel')"
            :value="{ catalog: demo.catalogValue, fileIds: demo.catalogFileIds }"
          />
        </template>

        <!-- 特殊场景说明 -->
        <el-descriptions v-else-if="scenario.component === 'special'" :column="1" border>
          <el-descriptions-item
            v-for="row in specialCases"
            :key="row.nameKey"
            :label="gp(row.nameKey)"
          >
            {{ gp(row.descKey) }}
          </el-descriptions-item>
        </el-descriptions>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import {
  AttachmentManager,
  CatalogAttachmentManager,
  FILE_BIZ_BANNER_IMAGE,
  FILE_BIZ_NOTICE_COVER,
  FILE_BIZ_OA_INSTANCE,
  FILE_DOWNLOAD_SCOPE_INFRA,
  FILE_DOWNLOAD_SCOPE_NOTICE,
  type FileDownloadScope,
} from '@/components/attachment'
import { useGenI18n } from '@/composables/useGenI18n'
import type { GenI18nMessages } from '@/types/tool/genI18n'
import type {
  GenViewAttachmentScenario,
  GenViewFileCatalogBind,
  GenViewSchema,
} from '@/types/tool/genView'
import viewConfigData from '@/i18n/gen/attachmentShowcase/view.json'
import genI18nZhCN from '@/i18n/gen/attachmentShowcase/zh-CN.json'
import genI18nEnUS from '@/i18n/gen/attachmentShowcase/en-US.json'
import ScenarioMeta from './ScenarioMeta.vue'
import StateDump from './StateDump.vue'

const GEN_I18N_FILES: Record<string, GenI18nMessages> = {
  'zh-CN': genI18nZhCN as GenI18nMessages,
  'en-US': genI18nEnUS as GenI18nMessages,
}

const viewConfig = viewConfigData as GenViewSchema
const { gp, gf } = useGenI18n('attachmentShowcase', 'infra', GEN_I18N_FILES)

const BIZ_TYPE_CONST: Record<string, string> = {
  FILE_BIZ_NOTICE_COVER,
  FILE_BIZ_OA_INSTANCE,
  FILE_BIZ_BANNER_IMAGE,
}

const scenarios = computed(() => viewConfig.attachmentScenarios || [])
const pickGuideKeys = computed(() => viewConfig.attachmentPickGuideKeys || [])
const specialCases = computed(() => viewConfig.attachmentSpecialCases || [])
const fileCatalogBinds = computed(() => viewConfig.fileCatalogs || [])

const activeTab = ref(scenarios.value[0]?.id || 'single-image')
const catalogCode = ref('')
const demoBizId = ref('')

const demo = reactive({
  singleId: '',
  singlePreview: '',
  multiIds: [] as string[],
  catalogValue: {} as Record<string, string | string[]>,
  catalogFileIds: [] as string[],
})

const catalogOptionsFromView = computed(() => {
  const catalogScenario = scenarios.value.find((s) => s.component === 'CatalogAttachmentManager')
  const tab = catalogScenario?.catalogBindTab || 'catalog'
  return fileCatalogBinds.value
    .filter((b: GenViewFileCatalogBind) => b.tab === tab)
    .map((b) => ({
      code: b.code,
      label: b.tabLabelKey ? `${gp(b.tabLabelKey)} (${b.code})` : b.code,
    }))
})

watch(
  catalogOptionsFromView,
  (opts) => {
    if (!opts.length) {
      catalogCode.value = ''
      return
    }
    if (!opts.some((o) => o.code === catalogCode.value)) {
      catalogCode.value = opts[0].code
    }
  },
  { immediate: true },
)

function resolveBizType(constName?: string): string {
  if (!constName) return FILE_BIZ_OA_INSTANCE
  return BIZ_TYPE_CONST[constName] || constName
}

function resolveDownloadScope(scope?: string): FileDownloadScope {
  return scope === 'notice' ? FILE_DOWNLOAD_SCOPE_NOTICE : FILE_DOWNLOAD_SCOPE_INFRA
}

function resolveComponentLabel(scenario: GenViewAttachmentScenario): string {
  if (scenario.component === 'AttachmentManager') return gp('componentAttachmentManager')
  if (scenario.component === 'CatalogAttachmentManager') {
    return gp('componentCatalogAttachmentManager')
  }
  return scenario.component
}

function onCatalogChange(payload: {
  value: Record<string, string | string[]>
  fileIds: string[]
}) {
  demo.catalogFileIds = payload.fileIds
}
</script>

<style scoped lang="scss">
.attachment-showcase {
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px 24px 40px;
}

.attachment-showcase__hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;

  h1 {
    margin: 0 0 6px;
    font-size: 22px;
  }

  p {
    margin: 0;
    color: var(--el-text-color-secondary);
    font-size: 13px;
    line-height: 1.5;
  }
}

.attachment-showcase__guide {
  margin-bottom: 16px;

  ul {
    margin: 8px 0 0;
    padding-left: 18px;
    line-height: 1.7;
    font-size: 13px;
  }
}

.attachment-showcase__biz-hint {
  margin: 10px 0 0;
  font-size: 13px;
  font-weight: 600;
}

.attachment-showcase__tabs {
  :deep(.el-tabs__content) {
    padding: 16px;
  }

  h3 {
    margin: 0 0 10px;
    font-size: 14px;
  }
}

.attachment-showcase__toolbar {
  margin-bottom: 8px;
}
</style>
