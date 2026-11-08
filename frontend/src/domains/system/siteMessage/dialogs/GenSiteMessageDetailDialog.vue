<template>
  <GenFormShell v-model="visible" :title="title || gp('detailTitle')" :width="dialogWidth">
    <div v-loading="detailLoading">
      <el-descriptions v-if="detail" :column="2" border size="small" class="detail-meta">
        <el-descriptions-item
          v-for="item in metaFields"
          :key="item.field"
          :label="gf(item.field, 'label')"
          :span="item.span ?? 1"
        >
          <template v-if="item.field === 'messageCategory'">
            <DictTag
              :options="getDict('sys_site_message_category')"
              :value="detail.messageCategory"
            />
          </template>
          <template v-else-if="item.field === 'sentAt'">
            {{ formatDateTime(detail.sentAt) }}
          </template>
          <template v-else>
            {{ detail[item.field as keyof SiteMessageVO] ?? '-' }}
          </template>
        </el-descriptions-item>
      </el-descriptions>

      <el-tabs
        v-if="detail"
        v-model="activeTab"
        type="border-card"
        class="detail-tabs"
        @tab-change="handleTabChange"
      >
        <el-tab-pane :label="gp('detailTabContent')" name="content">
          <div class="message-content">{{ detail.messageContent }}</div>
        </el-tab-pane>

        <el-tab-pane :label="gp('detailTabReceipt')" name="receipt">
          <div class="receipt-panel">
            <el-form :model="receiptQuery" inline class="receipt-search" @submit.prevent>
              <el-form-item
                v-for="field in receiptQueryFields"
                :key="field.field"
                :label="gf(field.field, 'label')"
              >
                <GenFormField
                  v-model="receiptQuery[field.field]"
                  :config="field"
                  mode="query"
                  :placeholder="gf(field.field, 'placeholder')"
                  :get-dict="getDict"
                  @enter="handleReceiptQuery"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :icon="Search" @click="handleReceiptQuery">
                  {{ t('common.search') }}
                </el-button>
                <el-button :icon="Refresh" @click="resetReceiptQuery">
                  {{ t('common.reset') }}
                </el-button>
              </el-form-item>
            </el-form>

            <el-table
              v-loading="receiptLoading"
              :data="receiptList"
              size="small"
              stripe
              border
              highlight-current-row
            >
              <el-table-column :label="t('common.index')" type="index" width="52" align="center" />
              <el-table-column
                v-for="col in receiptTableFields"
                :key="col.field"
                :label="gf(col.field, 'label')"
                :prop="col.component === 'text' ? col.field : undefined"
                :min-width="col.minWidth"
                :width="col.width"
                :align="col.align"
                :show-overflow-tooltip="col.showOverflowTooltip"
              >
                <template v-if="col.component === 'dict-tag'" #default="{ row }">
                  <DictTag :options="getDict(col.dictType!)" :value="row[col.field]" />
                </template>
                <template v-else-if="col.field === 'readAt'" #default="{ row }">
                  {{ formatDateTime(row.readAt) }}
                </template>
              </el-table-column>
            </el-table>

            <div class="receipt-footer">
              <Pagination
                v-show="receiptTotal > 0"
                v-model:page="receiptQuery.pageNo"
                v-model:limit="receiptQuery.pageSize"
                :total="receiptTotal"
                @pagination="loadReceiptPage"
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <template #footer>
      <el-button type="primary" @click="visible = false">{{ t('common.confirm') }}</el-button>
    </template>
  </GenFormShell>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { Refresh, Search } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import DictTag from '@/components/DictTag.vue'
import GenFormField from '@/components/gen/fields/GenFormField.vue'
import GenFormShell from '@/components/gen/layout/GenFormShell.vue'
import { useDict } from '@/composables/useDict'
import { useGenPageShellI18n } from '@/composables/gen/useGenPageShellI18n'
import { getSiteMessage, pageSiteMessageRecipients } from '@/api/system/siteMessage'
import type { GenViewQueryField, GenViewSchema, GenViewTableField } from '@/types/tool/genView'
import type {
  SiteMessageRecipientPageQuery,
  SiteMessageRecipientStatusVO,
  SiteMessageVO,
} from '@/types/system/siteMessage'
import viewConfigData from '@/i18n/gen/siteMessage/view.json'
interface SiteMessageDetailDialogConfig {
  dialogWidth?: number
  metaFields?: { field: string; span?: number }[]
  receiptQuery?: { fields?: GenViewQueryField[] }
  receiptTable?: { fields?: GenViewTableField[] }
}

type SiteMessageViewSchema = GenViewSchema & {
  detailDialog?: SiteMessageDetailDialogConfig
}

defineOptions({ inheritAttrs: false })

const props = defineProps<{
  modelValue: boolean
  messageId?: string | null
  title?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [boolean]
}>()

const viewConfig = viewConfigData as SiteMessageViewSchema
const detailDialogConfig = viewConfig.detailDialog ?? {}
const metaFields = detailDialogConfig.metaFields ?? []
const receiptQueryFields = detailDialogConfig.receiptQuery?.fields ?? []
const receiptTableFields = detailDialogConfig.receiptTable?.fields ?? []
const dialogWidth = detailDialogConfig.dialogWidth ?? 960

const { gp, gf } = useGenPageShellI18n('siteMessage')

const { t } = useI18n()
const { getDict } = useDict(['sys_site_message_category', 'sys_site_message_read_status'])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
})

const activeTab = ref('content')
const detailLoading = ref(false)
const detail = ref<SiteMessageVO | null>(null)
const receiptLoading = ref(false)
const receiptList = ref<SiteMessageRecipientStatusVO[]>([])
const receiptTotal = ref(0)
const receiptQuery = reactive<{ pageNo: number; pageSize: number; [key: string]: unknown }>({
  pageNo: 1,
  pageSize: 10,
  readStatus: undefined,
})

watch(
  () => [props.modelValue, props.messageId] as const,
  async ([open, messageId]) => {
    if (!open || !messageId) {
      detail.value = null
      return
    }
    activeTab.value = 'content'
    receiptQuery.pageNo = 1
    receiptQuery.readStatus = undefined
    receiptList.value = []
    receiptTotal.value = 0
    detailLoading.value = true
    try {
      detail.value = await getSiteMessage(messageId)
    } finally {
      detailLoading.value = false
    }
  },
)

function formatDateTime(value?: string) {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 19)
}

function buildReceiptQuery(): SiteMessageRecipientPageQuery {
  const params: SiteMessageRecipientPageQuery = {
    pageNo: receiptQuery.pageNo,
    pageSize: receiptQuery.pageSize,
  }
  const readStatus = receiptQuery.readStatus
  if (readStatus != null && readStatus !== '') {
    params.readStatus = String(readStatus)
  }
  return params
}

async function loadReceiptPage() {
  if (!props.messageId) return
  receiptLoading.value = true
  try {
    const res = await pageSiteMessageRecipients(props.messageId, buildReceiptQuery())
    receiptList.value = res.data.data.list ?? []
    receiptTotal.value = res.data.data.total ?? 0
  } finally {
    receiptLoading.value = false
  }
}

function handleReceiptQuery() {
  receiptQuery.pageNo = 1
  loadReceiptPage()
}

function resetReceiptQuery() {
  receiptQuery.readStatus = undefined
  handleReceiptQuery()
}

function handleTabChange(name: string | number) {
  if (name === 'receipt' && props.messageId) {
    loadReceiptPage()
  }
}
</script>

<style scoped lang="scss">
.detail-meta {
  margin-bottom: 12px;
}

.detail-tabs {
  margin-top: 4px;

  :deep(.el-tabs__content) {
    padding: 10px 12px;
  }
}

.message-content {
  white-space: pre-wrap;
  line-height: 1.6;
}

.receipt-panel {
  .receipt-search {
    margin-bottom: 8px;

    :deep(.el-form-item) {
      margin-bottom: 0;
      margin-right: 12px;
    }
  }

  .receipt-footer {
    display: flex;
    justify-content: flex-end;
    margin-top: 8px;
  }
}
</style>
