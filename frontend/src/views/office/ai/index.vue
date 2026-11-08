<!-- 在线办公 · 办公助手（会话落库 + 对话优先） -->
<template>
  <div class="office-ai-page">
    <GenListLayout plain :title="gp('title')" :description="gp('description')" class="office-ai-inner">
      <div class="office-ai-layout">
        <aside class="history-panel">
          <div class="history-header">
            <span>{{ gp('historyTitle') }}</span>
            <el-button type="primary" size="small" :disabled="!canChat || sending" @click="handleNewChat">
              {{ gp('clear') }}
            </el-button>
          </div>
          <div v-loading="sessionsLoading" class="history-list">
            <el-empty v-if="!sessions.length" :description="gp('historyEmpty')" :image-size="56" />
            <button
              v-for="item in sessions"
              :key="item.id"
              type="button"
              class="history-item"
              :class="{ active: item.id === activeSessionId }"
              @click="selectSession(item.id)"
            >
              <div class="history-item__title">{{ item.title || gp('untitledSession') }}</div>
              <div class="history-item__meta">{{ item.lastMessageAt || item.createdAt }}</div>
              <el-button
                class="history-item__delete"
                link
                type="danger"
                :icon="Delete"
                @click.stop="handleDeleteSession(item.id)"
              />
            </button>
          </div>
        </aside>

        <section class="chat-shell">
          <header class="chat-toolbar">
            <div class="toolbar-skills" v-if="allowedSkills.length">
              <span class="toolbar-label">{{ gp('skillsTitle') }}</span>
              <el-button
                v-for="skill in allowedSkills"
                :key="skill.code"
                size="small"
                round
                :disabled="!canChat || sending"
                @click="sendQuick(skill.name)"
              >
                {{ skill.name }}
              </el-button>
            </div>
          </header>

          <div ref="threadRef" class="chat-thread">
            <div class="chat-thread-inner">
              <div v-if="!messages.length && !sending" class="welcome">
                <div class="welcome-icon" aria-hidden="true">
                  <el-icon :size="28"><ChatDotRound /></el-icon>
                </div>
                <h3 class="welcome-title">{{ gp('welcomeTitle') }}</h3>
                <p class="welcome-hint">{{ gp('welcomeHint') }}</p>
                <el-skeleton v-if="skillsLoading" :rows="2" animated class="welcome-skeleton" />
                <el-empty
                  v-else-if="!skills.length"
                  :description="gp('skillsEmpty')"
                  :image-size="64"
                />
                <div v-else class="skill-cards">
                  <button
                    v-for="skill in skills"
                    :key="skill.code"
                    type="button"
                    class="skill-card"
                    :class="{ 'is-denied': !skill.allowed }"
                    :disabled="!skill.allowed || !canChat || sending"
                    @click="sendQuick(skill.name)"
                  >
                    <div class="skill-card__name">
                      {{ skill.name }}
                      <el-tag v-if="!skill.allowed" size="small" type="info">{{ gp('skillDenied') }}</el-tag>
                    </div>
                    <p class="skill-card__desc">{{ skill.description }}</p>
                  </button>
                </div>
              </div>

              <div
                v-for="msg in messages"
                :key="msg.id || `${msg.role}-${msg.createdAt}-${msg.content.slice(0, 12)}`"
                class="message-row"
                :class="msg.role === 'user' ? 'is-user' : 'is-assistant'"
              >
                <div class="message-avatar" aria-hidden="true">
                  <el-icon v-if="msg.role === 'assistant'"><ChatDotRound /></el-icon>
                  <span v-else>{{ gp('roleUser').slice(0, 1) }}</span>
                </div>
                <div class="message-content">
                  <div class="message-meta">
                    <span class="message-role">
                      {{ msg.role === 'user' ? gp('roleUser') : gp('roleAssistant') }}
                    </span>
                  </div>
                  <div
                    v-if="msg.role === 'assistant'"
                    class="message-bubble md-body"
                    v-html="renderMarkdown(msg.content)"
                  />
                  <div v-else class="message-bubble">{{ msg.content }}</div>
                  <div v-if="msg.usedSkills?.length" class="skill-tags">
                    <span class="skill-tags__label">{{ gp('usedSkills') }}</span>
                    <el-tag
                      v-for="code in msg.usedSkills"
                      :key="code"
                      size="small"
                      effect="plain"
                      type="info"
                    >
                      {{ skillLabel(code) }}
                    </el-tag>
                  </div>
                </div>
              </div>

              <div v-if="sending" class="message-row is-assistant">
                <div class="message-avatar" aria-hidden="true">
                  <el-icon><ChatDotRound /></el-icon>
                </div>
                <div class="message-content">
                  <div class="message-bubble thinking">
                    <span class="thinking-dots" aria-hidden="true" />
                    {{ gp('thinking') }}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="composer">
            <div class="composer-box">
              <el-input
                v-model="draft"
                type="textarea"
                :autosize="{ minRows: 2, maxRows: 6 }"
                :placeholder="gp('inputPlaceholder')"
                :disabled="!canChat || sending"
                @keydown.enter.exact.prevent="handleSend"
              />
              <div class="composer-footer">
                <span class="composer-tip">{{ gp('composerTip') }}</span>
                <el-button
                  type="primary"
                  :loading="sending"
                  :disabled="!canChat || !draft.trim()"
                  @click="handleSend"
                >
                  {{ gp('send') }}
                </el-button>
              </div>
            </div>
          </div>
        </section>
      </div>
    </GenListLayout>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ChatDotRound, Delete } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import GenListLayout from '@/components/gen/layout/GenListLayout.vue'
import { hasPermission } from '@/utils/permission'
import { useUserStore } from '@/stores/user'
import { useGenPageShellI18n } from '@/composables/gen/useGenPageShellI18n'
import { renderOfficeAiMarkdown } from '@/utils/officeAiMarkdown'
import {
  chatOfficeAi,
  createOfficeAiSession,
  deleteOfficeAiSession,
  listOfficeAiMessages,
  listOfficeAiSkills,
  pageOfficeAiSessions,
  PERM_OfficeAi,
  type OfficeAiMessageVO,
  type OfficeAiSessionVO,
  type OfficeAiSkillVO,
} from '@/api/oa/officeAi'
const { gp } = useGenPageShellI18n('officeAi')
const { locale } = useI18n()

const userStore = useUserStore()
const canChat = computed(() => hasPermission(userStore.permissions, PERM_OfficeAi.chat))

const skills = ref<OfficeAiSkillVO[]>([])
const skillsLoading = ref(false)
const sessions = ref<OfficeAiSessionVO[]>([])
const sessionsLoading = ref(false)
const activeSessionId = ref<string | null>(null)
const messages = ref<OfficeAiMessageVO[]>([])
const draft = ref('')
const sending = ref(false)
const threadRef = ref<HTMLElement | null>(null)

const allowedSkills = computed(() => skills.value.filter((s) => s.allowed))

const skillNameMap = computed(() => {
  const map = new Map<string, string>()
  for (const skill of skills.value) {
    map.set(skill.code, skill.name)
  }
  return map
})

function skillLabel(code: string) {
  return skillNameMap.value.get(code) || code
}

function renderMarkdown(content: string) {
  return renderOfficeAiMarkdown(content)
}

async function loadSkills() {
  skillsLoading.value = true
  try {
    const res = await listOfficeAiSkills()
    skills.value = res.data.data ?? []
  } finally {
    skillsLoading.value = false
  }
}

async function loadSessions() {
  sessionsLoading.value = true
  try {
    const res = await pageOfficeAiSessions({ pageNo: 1, pageSize: 50 })
    sessions.value = res.data.data?.list ?? []
  } finally {
    sessionsLoading.value = false
  }
}

async function selectSession(sessionId: string) {
  if (sending.value || activeSessionId.value === sessionId) {
    return
  }
  activeSessionId.value = sessionId
  const res = await listOfficeAiMessages(sessionId)
  messages.value = (res.data.data ?? []).map((m) => ({
    ...m,
    usedSkills: m.usedSkills ?? [],
  }))
  await scrollToBottom()
}

async function handleNewChat() {
  if (!canChat.value || sending.value) {
    return
  }
  const res = await createOfficeAiSession()
  const session = res.data.data
  activeSessionId.value = session.id
  messages.value = []
  await loadSessions()
}

async function handleDeleteSession(sessionId: string) {
  try {
    await ElMessageBox.confirm(gp('deleteSessionConfirm'), gp('deleteSession'), { type: 'warning' })
  } catch {
    return
  }
  await deleteOfficeAiSession(sessionId)
  if (activeSessionId.value === sessionId) {
    activeSessionId.value = null
    messages.value = []
  }
  await loadSessions()
}

async function scrollToBottom() {
  await nextTick()
  const el = threadRef.value
  if (el) {
    el.scrollTop = el.scrollHeight
  }
}

function sendQuick(text: string) {
  draft.value = text
  void handleSend()
}

async function handleSend() {
  const text = draft.value.trim()
  if (!text || sending.value || !canChat.value) {
    return
  }
  messages.value.push({
    id: `local-user-${Date.now()}`,
    sessionId: activeSessionId.value || '',
    role: 'user',
    content: text,
  })
  draft.value = ''
  sending.value = true
  await scrollToBottom()
  try {
    const res = await chatOfficeAi(text, activeSessionId.value)
    const data = res.data.data
    if (data?.sessionId) {
      activeSessionId.value = data.sessionId
    }
    messages.value.push({
      id: `local-assistant-${Date.now()}`,
      sessionId: data?.sessionId || activeSessionId.value || '',
      role: 'assistant',
      content: data?.reply || gp('emptyReply'),
      usedSkills: data?.usedSkills ?? [],
    })
    await loadSessions()
  } catch {
    // request interceptor already toasts
  } finally {
    sending.value = false
    await scrollToBottom()
  }
}

onMounted(async () => {
  await Promise.all([loadSkills(), loadSessions()])
})

/** Skills names/descriptions come from DB i18n; re-fetch when UI locale changes. */
watch(locale, () => {
  void loadSkills()
})
</script>

<script lang="ts">
export default { name: 'OfficeAi' }
</script>

<style scoped lang="scss">
.office-ai-page {
  height: calc(100vh - 56px);
  overflow: hidden;
  box-sizing: border-box;
}

.office-ai-inner {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.office-ai-inner :deep(.page-header) {
  flex-shrink: 0;
  margin-bottom: 10px;
}

.office-ai-layout {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 12px;
}

.history-panel,
.chat-shell {
  min-height: 0;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.history-header {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 12px 14px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  font-size: 13px;
  font-weight: 600;
}

.history-list {
  flex: 1;
  min-height: 0;
  overflow: auto;
  padding: 8px;
}

.history-item {
  position: relative;
  width: 100%;
  appearance: none;
  border: none;
  background: transparent;
  border-radius: 8px;
  padding: 10px 28px 10px 10px;
  text-align: left;
  cursor: pointer;
  margin-bottom: 4px;

  &:hover,
  &.active {
    background: var(--el-fill-color-light);
  }

  &.active {
    background: var(--el-color-primary-light-9);
  }
}

.history-item__title {
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-item__meta {
  margin-top: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.history-item__delete {
  position: absolute;
  top: 8px;
  right: 4px;
  opacity: 0;
}

.history-item:hover .history-item__delete {
  opacity: 1;
}

.chat-toolbar {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  padding: 10px 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  background: var(--el-fill-color-blank);
}

.toolbar-skills {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.toolbar-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.chat-thread {
  flex: 1;
  min-height: 0;
  overflow: auto;
  background: var(--el-bg-color-page);
}

.chat-thread-inner {
  max-width: 860px;
  margin: 0 auto;
  padding: 20px 20px 28px;
  width: 100%;
  box-sizing: border-box;
}

.welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 36px 12px 20px;
}

.welcome-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  margin-bottom: 14px;
}

.welcome-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.welcome-hint {
  margin: 8px 0 0;
  max-width: 420px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--el-text-color-secondary);
}

.welcome-skeleton {
  width: min(560px, 100%);
  margin-top: 24px;
}

.skill-cards {
  width: 100%;
  max-width: 640px;
  margin-top: 24px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  text-align: left;
}

.skill-card {
  appearance: none;
  border: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color);
  border-radius: 10px;
  padding: 14px 16px;
  cursor: pointer;
  text-align: left;
  transition:
    border-color 0.15s ease,
    box-shadow 0.15s ease,
    transform 0.15s ease;

  &:hover:not(:disabled) {
    border-color: var(--el-color-primary-light-5);
    box-shadow: 0 4px 14px rgba(0, 0, 0, 0.04);
    transform: translateY(-1px);
  }

  &:disabled {
    cursor: not-allowed;
  }

  &.is-denied {
    opacity: 0.55;
  }
}

.skill-card__name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
}

.skill-card__desc {
  margin: 8px 0 0;
  font-size: 12px;
  line-height: 1.55;
  color: var(--el-text-color-secondary);
}

.message-row {
  display: flex;
  gap: 12px;
  margin-bottom: 18px;

  &.is-user {
    flex-direction: row-reverse;

    .message-content {
      align-items: flex-end;
    }

    .message-meta {
      flex-direction: row-reverse;
    }

    .message-avatar {
      background: var(--el-color-primary);
      color: #fff;
    }

    .message-bubble {
      background: var(--el-color-primary-light-9);
      white-space: pre-wrap;
    }
  }

  &.is-assistant {
    .message-avatar {
      background: var(--el-color-primary-light-8);
      color: var(--el-color-primary);
    }

    .message-bubble {
      background: var(--el-bg-color);
      border: 1px solid var(--el-border-color-extra-light);
    }
  }
}

.message-avatar {
  flex-shrink: 0;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
}

.message-content {
  min-width: 0;
  flex: 1;
  max-width: calc(100% - 46px);
  display: flex;
  flex-direction: column;
}

.message-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.message-role {
  font-weight: 600;
  color: var(--el-text-color-regular);
}

.message-bubble {
  max-width: 100%;
  padding: 12px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.65;
  word-break: break-word;
}

.thinking {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: var(--el-text-color-secondary);
}

.thinking-dots {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--el-color-primary);
  animation: pulse 1s ease-in-out infinite;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 0.35;
    transform: scale(0.85);
  }
  50% {
    opacity: 1;
    transform: scale(1);
  }
}

.skill-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
}

.skill-tags__label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.composer {
  flex-shrink: 0;
  padding: 12px 16px 16px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.composer-box {
  max-width: 860px;
  margin: 0 auto;
  border: 1px solid var(--el-border-color);
  border-radius: 12px;
  padding: 10px 12px;
  background: var(--el-fill-color-blank);

  &:focus-within {
    border-color: var(--el-color-primary-light-5);
  }
}

.composer-box :deep(.el-textarea__inner) {
  box-shadow: none;
  border: none;
  background: transparent;
  padding: 4px 2px;
  resize: none;
}

.composer-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 6px;
}

.composer-tip {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

.md-body :deep(p) {
  margin: 0 0 10px;

  &:last-child {
    margin-bottom: 0;
  }
}

.md-body :deep(h1),
.md-body :deep(h2),
.md-body :deep(h3) {
  margin: 0 0 10px;
  font-size: 15px;
  font-weight: 600;
}

.md-body :deep(ul),
.md-body :deep(ol) {
  margin: 0 0 10px;
  padding-left: 1.25em;
}

.md-body :deep(code) {
  padding: 1px 5px;
  border-radius: 4px;
  background: var(--el-fill-color);
  font-size: 12px;
}

.md-body :deep(.md-table-wrap) {
  overflow-x: auto;
  margin: 8px 0 12px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
}

.md-body :deep(table) {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.md-body :deep(th),
.md-body :deep(td) {
  padding: 8px 10px;
  border-bottom: 1px solid var(--el-border-color-extra-light);
  text-align: left;
  vertical-align: top;
}

.md-body :deep(th) {
  background: var(--el-fill-color-light);
  font-weight: 600;
  white-space: nowrap;
}

.md-body :deep(tr:last-child td) {
  border-bottom: none;
}

@media (max-width: 960px) {
  .office-ai-layout {
    grid-template-columns: 1fr;
    grid-template-rows: 160px 1fr;
  }

  .skill-cards {
    grid-template-columns: 1fr;
  }
}
</style>
