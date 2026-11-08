<template>
  <div class="chat-layout">
    <aside class="chat-sidebar">
      <div class="sidebar-header">
        <el-button type="primary" class="new-chat-btn" :disabled="!aiReady" @click="handleNewChat">
          {{ gp('newChat') }}
        </el-button>
      </div>
      <div v-loading="sessionsLoading" class="session-list">
        <el-empty v-if="!sessions.length" :description="gp('noSessions')" :image-size="64" />
        <div
          v-for="item in sessions"
          :key="item.id"
          class="session-item"
          :class="{ active: item.id === activeSessionId }"
          @click="selectSession(item.id)"
        >
          <div class="session-title">{{ displayTitle(item) }}</div>
          <div class="session-time">{{ item.updatedAt || item.createdAt }}</div>
          <el-button
            class="session-delete"
            link
            type="danger"
            :icon="Delete"
            @click.stop="handleDeleteSession(item.id)"
          />
        </div>
      </div>
    </aside>

    <section class="chat-main">
      <div v-if="!activeSessionId" class="chat-empty">
        <el-empty :description="gp('chatEmptyHint')" />
      </div>
      <template v-else>
        <header class="chat-thread-header">
          <span class="thread-title">{{ activeSessionTitle }}</span>
        </header>

        <div class="chat-thread-body">
          <div ref="messageListRef" class="message-list" @scroll="onMessageScroll">
            <div class="message-list-inner">
              <div
                v-for="msg in messages"
                :key="msg.id"
                class="message-row"
                :class="msg.role === 'user' ? 'is-user' : 'is-assistant'"
              >
                <div class="message-avatar" aria-hidden="true">
                  {{
                    msg.role === 'user'
                      ? gp('roleUser').slice(0, 1)
                      : gp('roleAssistant')
                  }}
                </div>
                <div class="message-content">
                  <div class="message-meta">
                    <span class="message-role">
                      {{ msg.role === 'user' ? gp('roleUser') : gp('roleAssistant') }}
                    </span>
                    <span class="message-time">{{ msg.createdAt }}</span>
                    <el-button link type="primary" @click="copyText(msg.content)">
                      {{ gp('copyMessage') }}
                    </el-button>
                  </div>
                  <div class="message-bubble">{{ msg.content }}</div>
                </div>
              </div>
              <div v-if="sending" class="message-row is-assistant">
                <div class="message-avatar" aria-hidden="true">{{ gp('roleAssistant') }}</div>
                <div class="message-content">
                  <div class="message-meta">
                    <span class="message-role">{{ gp('roleAssistant') }}</span>
                  </div>
                  <div class="message-bubble thinking">{{ gp('chatThinking') }}</div>
                </div>
              </div>
            </div>
          </div>

          <Transition name="fade">
            <button
              v-if="showScrollBottom"
              type="button"
              class="scroll-bottom-btn"
              :title="gp('scrollToBottom')"
              @click="scrollToBottom(true)"
            >
              <el-icon><ArrowDown /></el-icon>
            </button>
          </Transition>
        </div>

        <div class="composer">
          <div class="composer-box">
            <el-input
              v-model="draft"
              type="textarea"
              :autosize="{ minRows: 1, maxRows: 8 }"
              :placeholder="gp('chatInputPlaceholder')"
              :disabled="sending || !aiReady"
              resize="none"
              @keydown.enter="onEnter"
            />
            <div class="composer-actions">
              <el-button
                type="primary"
                :loading="sending"
                :disabled="!aiReady || !draft.trim()"
                @click="handleSend"
              >
                {{ gp('sendMessage') }}
              </el-button>
            </div>
          </div>
        </div>
      </template>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { ArrowDown, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { useToolAiI18n } from '@/composables/useToolAiI18n'
import {
  archiveAiChatSession,
  createAiChatSession,
  getAiChatSession,
  pageAiChatSessions,
  sendAiChatMessage,
  type ToolAiChatMessage,
  type ToolAiChatSession,
} from '@/api/tool/aiChat'

defineProps<{ aiReady: boolean }>()

const { t } = useI18n()
const { gp } = useToolAiI18n()

const SCROLL_THRESHOLD = 96

const sessionsLoading = ref(false)
const sending = ref(false)
const sessions = ref<ToolAiChatSession[]>([])
const activeSessionId = ref<string>()
const messages = ref<ToolAiChatMessage[]>([])
const draft = ref('')
const messageListRef = ref<HTMLElement>()
const showScrollBottom = ref(false)
const stickToBottom = ref(true)

const activeSessionTitle = computed(() => {
  const session = sessions.value.find((s) => s.id === activeSessionId.value)
  return session ? displayTitle(session) : gp('newChat')
})

function displayTitle(item: ToolAiChatSession) {
  return item.title?.trim() || gp('newChat')
}

function isNearBottom(el: HTMLElement) {
  return el.scrollHeight - el.scrollTop - el.clientHeight <= SCROLL_THRESHOLD
}

function onMessageScroll() {
  const el = messageListRef.value
  if (!el) return
  stickToBottom.value = isNearBottom(el)
  showScrollBottom.value = !stickToBottom.value
}

async function loadSessions(selectFirst = false) {
  sessionsLoading.value = true
  try {
    const res = await pageAiChatSessions({ pageNo: 1, pageSize: 50, mineOnly: true })
    sessions.value = res.data.data.list ?? []
    if (selectFirst && sessions.value.length && !activeSessionId.value) {
      await selectSession(sessions.value[0].id)
    }
  } finally {
    sessionsLoading.value = false
  }
}

async function selectSession(id: string) {
  activeSessionId.value = id
  stickToBottom.value = true
  const res = await getAiChatSession(id)
  messages.value = res.data.data.messages ?? []
  await scrollToBottom()
}

async function handleNewChat() {
  const res = await createAiChatSession()
  const session = res.data.data
  sessions.value = [session, ...sessions.value.filter((s) => s.id !== session.id)]
  activeSessionId.value = session.id
  messages.value = []
  draft.value = ''
  stickToBottom.value = true
  showScrollBottom.value = false
}

async function handleSend() {
  const text = draft.value.trim()
  if (!text || !activeSessionId.value || sending.value) return
  sending.value = true
  stickToBottom.value = true
  const backup = text
  draft.value = ''
  try {
    const res = await sendAiChatMessage(activeSessionId.value, backup)
    const data = res.data.data
    messages.value.push(data.userMessage, data.assistantMessage)
    const idx = sessions.value.findIndex((s) => s.id === activeSessionId.value)
    if (idx >= 0) {
      sessions.value[idx] = {
        ...sessions.value[idx],
        title: sessions.value[idx].title || data.userMessage.content.slice(0, 40),
        updatedAt: data.assistantMessage.createdAt,
      }
      const [current] = sessions.value.splice(idx, 1)
      sessions.value.unshift(current)
    }
    await scrollToBottom()
  } catch {
    draft.value = backup
  } finally {
    sending.value = false
  }
}

function onEnter(e: KeyboardEvent) {
  if (e.shiftKey) return
  e.preventDefault()
  handleSend()
}

async function handleDeleteSession(id: string) {
  await ElMessageBox.confirm(gp('deleteSessionConfirm'), t('common.tip'))
  await archiveAiChatSession(id)
  sessions.value = sessions.value.filter((s) => s.id !== id)
  if (activeSessionId.value === id) {
    activeSessionId.value = undefined
    messages.value = []
    showScrollBottom.value = false
    if (sessions.value.length) {
      await selectSession(sessions.value[0].id)
    }
  }
  ElMessage.success(t('common.success'))
}

async function copyText(text: string) {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success(t('common.copied'))
  } catch {
    ElMessage.error(gp('copyDdlFailed'))
  }
}

async function scrollToBottom(smooth = false) {
  await nextTick()
  const el = messageListRef.value
  if (!el) return
  el.scrollTo({ top: el.scrollHeight, behavior: smooth ? 'smooth' : 'auto' })
  stickToBottom.value = true
  showScrollBottom.value = false
}

watch(
  () => messages.value.length,
  async () => {
    if (stickToBottom.value || sending.value) {
      await scrollToBottom()
    }
  },
)

watch(sending, async (value) => {
  if (value) {
    await scrollToBottom()
  }
})

onMounted(() => loadSessions(true))
</script>

<style scoped lang="scss">
.chat-layout {
  display: flex;
  height: 100%;
  min-height: 0;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: var(--ic-card-radius, 8px);
  overflow: hidden;
  background: var(--el-bg-color);
}

.chat-sidebar {
  width: 260px;
  flex-shrink: 0;
  min-height: 0;
  border-right: 1px solid var(--el-border-color-lighter);
  display: flex;
  flex-direction: column;
  background: var(--el-fill-color-blank);
}

.sidebar-header {
  flex-shrink: 0;
  padding: 12px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.new-chat-btn {
  width: 100%;
}

.session-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 8px;
}

.session-item {
  position: relative;
  padding: 10px 32px 10px 10px;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 4px;

  &:hover {
    background: var(--el-fill-color-light);
  }

  &.active {
    background: var(--el-color-primary-light-9);
  }
}

.session-title {
  font-size: 13px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-time {
  margin-top: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.session-delete {
  position: absolute;
  right: 4px;
  top: 8px;
  opacity: 0;
}

.session-item:hover .session-delete {
  opacity: 1;
}

.chat-main {
  flex: 1;
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-empty {
  flex: 1;
  min-height: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-thread-header {
  flex-shrink: 0;
  padding: 12px 20px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  background: var(--el-fill-color-blank);
}

.thread-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-thread-body {
  position: relative;
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  background: var(--el-bg-color);
}

.message-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 16px 20px 24px;
}

.message-list-inner {
  max-width: 820px;
  margin: 0 auto;
  width: 100%;
}

.message-row {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;

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
    }
  }

  &.is-assistant {
    .message-avatar {
      background: var(--el-fill-color);
      color: var(--el-text-color-primary);
    }

    .message-bubble {
      background: var(--el-fill-color-light);
    }
  }
}

.message-avatar {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
}

.message-content {
  min-width: 0;
  flex: 1;
  max-width: calc(100% - 44px);
  display: flex;
  flex-direction: column;
}

.message-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
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
  padding: 12px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.65;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: min(420px, 45vh);
  overflow-y: auto;
}

.message-bubble.thinking {
  color: var(--el-text-color-secondary);
  font-style: italic;
  max-height: none;
  overflow: visible;
}

.scroll-bottom-btn {
  position: absolute;
  right: 24px;
  bottom: 16px;
  z-index: 2;
  width: 36px;
  height: 36px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 50%;
  background: var(--el-bg-color);
  color: var(--el-text-color-regular);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;

  &:hover {
    background: var(--el-fill-color-light);
  }
}

.composer {
  flex-shrink: 0;
  padding: 12px 20px 16px;
  border-top: 1px solid var(--el-border-color-lighter);
  background: var(--el-fill-color-blank);
}

.composer-box {
  max-width: 820px;
  margin: 0 auto;
  border: 1px solid var(--el-border-color);
  border-radius: 14px;
  padding: 10px 12px 8px;
  background: var(--el-bg-color);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);

  :deep(.el-textarea__inner) {
    box-shadow: none;
    padding: 0;
    border: none;
    background: transparent;
  }
}

.composer-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
