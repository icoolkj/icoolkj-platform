<script setup lang="ts">
/**
 * 联调测试账号页 — 可整目录删除 frontend/src/dev/test-accounts
 */
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { TEST_ACCOUNT_GROUPS, TEST_PASSWORD } from './data'

const { locale } = useI18n()
const router = useRouter()
const en = computed(() => locale.value === 'en-US')

const groups = computed(() =>
  TEST_ACCOUNT_GROUPS.map((group) => ({
    id: group.id,
    title: en.value ? group.titleEn : group.titleZh,
    note: en.value ? group.noteEn : group.noteZh,
    accounts: group.accounts.map((a) => ({
      ...a,
      label: en.value ? a.labelEn : a.labelZh,
    })),
  })),
)

function goLogin(tenantCode: string, username: string) {
  void router.push({
    path: '/login',
    query: { tenantCode, username },
  })
}

async function copyCred(tenantCode: string, username: string) {
  const text = `${tenantCode} / ${username} / ${TEST_PASSWORD}`
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success(en.value ? 'Copied' : '已复制')
  } catch {
    ElMessage.warning(text)
  }
}
</script>

<template>
  <div class="dev-ta">
    <header class="dev-ta__head">
      <div>
        <h1>{{ en ? 'System test accounts' : '系统登录测试账号' }}</h1>
        <p class="dev-ta__hint">
          {{
            en
              ? `Password for all: ${TEST_PASSWORD}. Click a row to fill the login form.`
              : `统一密码：${TEST_PASSWORD}。点击账号跳转登录并自动填入。`
          }}
        </p>
      </div>
      <button type="button" class="dev-ta__back" @click="goLogin('platform', 'admin')">
        {{ en ? 'Back to login' : '返回登录' }}
      </button>
    </header>

    <section v-for="group in groups" :key="group.id" class="dev-ta__group">
      <h2>{{ group.title }}</h2>
      <p v-if="group.note" class="dev-ta__note">{{ group.note }}</p>
      <ul>
        <li v-for="account in group.accounts" :key="`${account.tenantCode}-${account.username}`">
          <button type="button" class="dev-ta__row" @click="goLogin(account.tenantCode, account.username)">
            <span class="dev-ta__label">{{ account.label }}</span>
            <span class="dev-ta__cred">{{ account.tenantCode }} / {{ account.username }}</span>
          </button>
          <button
            type="button"
            class="dev-ta__copy"
            :title="en ? 'Copy' : '复制'"
            @click="copyCred(account.tenantCode, account.username)"
          >
            {{ en ? 'Copy' : '复制' }}
          </button>
        </li>
      </ul>
    </section>
  </div>
</template>

<style scoped>
.dev-ta {
  min-height: 100vh;
  padding: 32px 20px 48px;
  max-width: 720px;
  margin: 0 auto;
  color: #1a1a1a;
  background: linear-gradient(165deg, #f3f6fa 0%, #e8eef5 50%, #f7f4ef 100%);
}
.dev-ta__head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 28px;
}
.dev-ta__eyebrow {
  margin: 0 0 6px;
  font-size: 12px;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: #6b7280;
}
.dev-ta__head h1 {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 700;
}
.dev-ta__hint {
  margin: 0 0 4px;
  font-size: 14px;
  color: #374151;
  line-height: 1.5;
}
.dev-ta__hint.muted {
  font-size: 12px;
  color: #6b7280;
}
.dev-ta__back {
  flex-shrink: 0;
  padding: 8px 14px;
  border-radius: 8px;
  border: 1px solid #c5d0dc;
  background: #fff;
  cursor: pointer;
  font-size: 13px;
}
.dev-ta__group {
  margin-bottom: 22px;
  padding: 16px 18px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid #dde5ee;
}
.dev-ta__group h2 {
  margin: 0 0 6px;
  font-size: 16px;
}
.dev-ta__note {
  margin: 0 0 12px;
  font-size: 12px;
  color: #6b7280;
  line-height: 1.45;
}
.dev-ta__group ul {
  list-style: none;
  margin: 0;
  padding: 0;
}
.dev-ta__group li {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}
.dev-ta__row {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
  padding: 10px 12px;
  border-radius: 8px;
  border: 1px solid #d5dee8;
  background: #f8fafc;
  text-align: left;
  cursor: pointer;
}
.dev-ta__row:hover {
  border-color: #94a3b8;
  background: #fff;
}
.dev-ta__label {
  font-size: 13px;
  font-weight: 600;
}
.dev-ta__cred {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 12px;
  color: #4b5563;
}
.dev-ta__copy {
  flex-shrink: 0;
  padding: 0 12px;
  border-radius: 8px;
  border: 1px solid #d5dee8;
  background: #fff;
  cursor: pointer;
  font-size: 12px;
}
</style>
