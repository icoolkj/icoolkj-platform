<template>
  <div class="gen-page-shell">
    <GenListLayout
      plain
      :title="gp('title')"
      :description="gp('description')"
      class="profile-page"
    >
    <div class="profile-layout">
      <el-card shadow="never" class="profile-card">
        <div class="profile-card-body">
          <div class="avatar-block">
            <AvatarPicker
              :model-value="profile?.avatarUrlMd || profile?.avatarUrl || ''"
              :avatar-file-id="profile?.avatarFileId || ''"
              :avatar-url-sm="profile?.avatarUrlSm || ''"
              mode="profile"
              :disabled="demoStore.isDemo"
              :display-name="displayName"
              :gp="gp"
              @uploaded="handleAvatarUploaded"
            />
          </div>

          <h3 class="display-name">{{ displayName }}</h3>
          <p v-if="profile?.nickname" class="display-subtitle">{{ profile.nickname }}</p>

          <ul class="profile-meta">
            <li v-if="profile?.deptName">
              <span class="meta-label">{{ gp('dept') }}</span>
              <span>{{ profile.deptName }}</span>
            </li>
            <li v-if="profile?.tenantName || profile?.tenantCode">
              <span class="meta-label">{{ gp('tenant') }}</span>
              <span>{{ profile?.tenantName || profile?.tenantCode }}</span>
            </li>
            <li v-if="profile?.email">
              <span class="meta-label">{{ gf('email') }}</span>
              <span>{{ profile.email }}</span>
            </li>
            <li v-if="profile?.phone">
              <span class="meta-label">{{ gf('phone') }}</span>
              <span>{{ profile.phone }}</span>
            </li>
          </ul>
        </div>
      </el-card>

      <el-card shadow="never" class="profile-main">
        <el-tabs v-model="activeTab">
          <el-tab-pane :label="gp('basicInfo')" name="basic">
            <el-form
              ref="basicFormRef"
              :model="basicForm"
              :rules="basicRules"
              label-width="96px"
              class="profile-form"
            >
              <el-form-item :label="gf('username')">
                <el-input :model-value="profile?.username || ''" disabled />
              </el-form-item>
              <el-form-item :label="gf('realName')" prop="realName">
                <el-input
                  v-model="basicForm.realName"
                  :placeholder="gf('realName', 'placeholder')"
                  clearable
                />
              </el-form-item>
              <el-form-item :label="gf('nickname')" prop="nickname">
                <el-input
                  v-model="basicForm.nickname"
                  :placeholder="gf('nickname', 'placeholder')"
                  clearable
                />
              </el-form-item>
              <el-form-item :label="gf('gender')" prop="gender">
                <el-radio-group v-model="basicForm.gender">
                  <el-radio :value="GENDER_MALE">{{ gp('genderMale') }}</el-radio>
                  <el-radio :value="GENDER_FEMALE">{{ gp('genderFemale') }}</el-radio>
                  <el-radio :value="GENDER_UNKNOWN">{{ gp('genderUnknown') }}</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item :label="gf('email')" prop="email">
                <el-input
                  v-model="basicForm.email"
                  :placeholder="gf('email', 'placeholder')"
                  clearable
                />
              </el-form-item>
              <el-form-item :label="gf('phone')" prop="phone">
                <el-input
                  v-model="basicForm.phone"
                  :placeholder="gf('phone', 'placeholder')"
                  clearable
                />
              </el-form-item>
              <el-form-item v-if="!demoStore.isDemo">
                <el-button type="primary" :loading="basicSaving" @click="saveBasicInfo">
                  {{ gp('saveBasic') }}
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane :label="gp('organization')" name="org">
            <el-descriptions :column="1" border class="org-descriptions">
              <el-descriptions-item :label="gp('tenant')">
                {{ profile?.tenantName || profile?.tenantCode || gp('none') }}
              </el-descriptions-item>
              <el-descriptions-item :label="gp('dept')">
                {{ profile?.deptName || gp('none') }}
              </el-descriptions-item>
              <el-descriptions-item :label="gp('roles')">
                <template v-if="profile?.roles?.length">
                  <el-tag v-for="role in profile.roles" :key="role.id" class="info-tag">
                    {{ role.roleName }}
                  </el-tag>
                </template>
                <span v-else>{{ gp('none') }}</span>
              </el-descriptions-item>
              <el-descriptions-item :label="gp('posts')">
                <template v-if="profile?.posts?.length">
                  <el-tag v-for="post in profile.posts" :key="post.id" type="info" class="info-tag">
                    {{ post.postName }}
                  </el-tag>
                </template>
                <span v-else>{{ gp('none') }}</span>
              </el-descriptions-item>
              <el-descriptions-item :label="gp('lastLogin')">
                {{ formatDateTime(profile?.lastLoginAt) }}
              </el-descriptions-item>
              <el-descriptions-item :label="gp('lastLoginIp')">
                {{ profile?.lastLoginIp || gp('none') }}
              </el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>

          <el-tab-pane :label="gp('account')" name="account">
            <el-form
              ref="passwordFormRef"
              :model="passwordForm"
              :rules="passwordRules"
              label-width="120px"
              class="profile-form password-form"
            >
              <el-descriptions :column="1" border class="account-meta">
                <el-descriptions-item :label="gp('pwdUpdatedAt')">
                  {{ formatDateTime(profile?.pwdUpdatedAt) }}
                </el-descriptions-item>
              </el-descriptions>

              <template v-if="!demoStore.isDemo">
                <el-form-item :label="gf('oldPassword')" prop="oldPassword">
                  <el-input
                    v-model="passwordForm.oldPassword"
                    type="password"
                    show-password
                    autocomplete="current-password"
                    :placeholder="gf('oldPassword', 'placeholder')"
                  />
                </el-form-item>
                <el-form-item :label="gf('newPassword')" prop="newPassword">
                  <el-input
                    v-model="passwordForm.newPassword"
                    type="password"
                    show-password
                    autocomplete="new-password"
                    :placeholder="gf('newPassword', 'placeholder')"
                  />
                </el-form-item>
                <el-form-item :label="gf('confirmPassword')" prop="confirmPassword">
                  <el-input
                    v-model="passwordForm.confirmPassword"
                    type="password"
                    show-password
                    autocomplete="new-password"
                    :placeholder="gf('confirmPassword', 'placeholder')"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="passwordSaving" @click="submitPassword">
                    {{ gp('changePassword') }}
                  </el-button>
                </el-form-item>
              </template>
            </el-form>
          </el-tab-pane>

          <el-tab-pane :label="gp('mfa')" name="mfa">
            <el-alert
              :title="userStore.user?.mfaEnabled ? gp('mfaEnabled') : gp('mfaDisabled')"
              :type="userStore.user?.mfaEnabled ? 'success' : 'info'"
              :closable="false"
              show-icon
              class="mfa-status"
            />
            <template v-if="!demoStore.isDemo">
              <div v-if="!userStore.user?.mfaEnabled" class="mfa-enroll">
                <el-button type="primary" :loading="mfaStarting" @click="startProfileMfa">
                  {{ gp('mfaStart') }}
                </el-button>
                <div v-if="mfaOtpUri" class="mfa-qr-block">
                  <img
                    :src="mfaQrUrl"
                    :alt="gp('mfa')"
                    width="180"
                    height="180"
                    class="mfa-qr"
                  />
                  <el-input v-model="mfaCode" :placeholder="gp('mfaCodePlaceholder')" maxlength="6" />
                  <el-button type="primary" :loading="mfaSaving" @click="confirmProfileMfa">
                    {{ gp('mfaConfirm') }}
                  </el-button>
                </div>
              </div>
              <div v-else class="mfa-disable">
                <el-input v-model="mfaCode" :placeholder="gp('mfaCodePlaceholder')" maxlength="6" />
                <el-button type="danger" plain :loading="mfaSaving" @click="disableProfileMfa">
                  {{ gp('mfaDisable') }}
                </el-button>
              </div>
            </template>
          </el-tab-pane>

          <el-tab-pane :label="gp('shortcuts')" name="shortcuts">
            <el-empty :description="gp('shortcutsPlaceholder')" />
          </el-tab-pane>

          <el-tab-pane :label="gp('messages')" name="messages">
            <ProfileSiteMessagePanel :pp="gp" />
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>
    </GenListLayout>
  </div>
</template>

<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import GenListLayout from '@/components/gen/layout/GenListLayout.vue'
import { changePassword, fetchProfile, updateProfile } from '@/api/profile'
import { confirmMfaEnroll, disableMfa, startMfaEnroll } from '@/api/auth'
import type { UserProfile } from '@/types/profile'
import { useDemoStore } from '@/stores/demo'
import { useGenPageShellI18n } from '@/composables/gen/useGenPageShellI18n'
import { useUserStore } from '@/stores/user'
import { GENDER_FEMALE, GENDER_MALE, GENDER_UNKNOWN } from '@/constants/flags'
const AvatarPicker = defineAsyncComponent(() => import('@/components/AvatarPicker.vue'))
const ProfileSiteMessagePanel = defineAsyncComponent(
  () => import('@/views/account/profile/ProfileSiteMessagePanel.vue'),
)

const { gp, gf, formRules } = useGenPageShellI18n('profile')

const demoStore = useDemoStore()
const userStore = useUserStore()
const route = useRoute()

const PROFILE_TABS = new Set(['basic', 'org', 'account', 'mfa', 'shortcuts', 'messages'])

const activeTab = ref('basic')
const profile = ref<UserProfile | null>(null)
const basicSaving = ref(false)
const passwordSaving = ref(false)
const mfaStarting = ref(false)
const mfaSaving = ref(false)
const mfaOtpUri = ref('')
const mfaCode = ref('')

const mfaQrUrl = computed(() =>
  mfaOtpUri.value
    ? `https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=${encodeURIComponent(mfaOtpUri.value)}`
    : '',
)

const basicFormRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()

const basicForm = reactive({
  nickname: '',
  realName: '',
  email: '',
  phone: '',
  gender: GENDER_UNKNOWN,
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const basicRules = computed<FormRules>(() => ({
  nickname: formRules.value.nickname,
  realName: formRules.value.realName,
  email: formRules.value.email,
  phone: formRules.value.phone,
  gender: formRules.value.gender,
}))

const passwordRules = computed<FormRules>(() => {
  const confirmBase = formRules.value.confirmPassword
  const confirmRules = Array.isArray(confirmBase) ? confirmBase : confirmBase ? [confirmBase] : []
  return {
    oldPassword: formRules.value.oldPassword,
    newPassword: formRules.value.newPassword,
    confirmPassword: [
      ...confirmRules,
      {
        validator: (_rule, value, callback) => {
          if (value !== passwordForm.newPassword) {
            callback(new Error(gp('passwordMismatch')))
            return
          }
          callback()
        },
        trigger: 'blur',
      },
    ],
  }
})

const displayName = computed(
  () => profile.value?.realName || profile.value?.nickname || profile.value?.username || '-',
)

function formatDateTime(value?: string) {
  if (!value) {
    return gp('none')
  }
  return value.replace('T', ' ').slice(0, 19)
}

function applyProfile(data: UserProfile) {
  profile.value = data
  basicForm.nickname = data.nickname || ''
  basicForm.realName = data.realName || ''
  basicForm.email = data.email || ''
  basicForm.phone = data.phone || ''
  basicForm.gender = data.gender || GENDER_UNKNOWN
}

async function loadProfile() {
  const data = await fetchProfile()
  applyProfile(data)
}

async function saveBasicInfo() {
  const valid = await basicFormRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  basicSaving.value = true
  try {
    const data = await updateProfile({
      nickname: basicForm.nickname.trim(),
      realName: basicForm.realName.trim() || undefined,
      email: basicForm.email.trim() || undefined,
      phone: basicForm.phone.trim() || undefined,
      gender: basicForm.gender,
    })
    applyProfile(data)
    ElMessage.success(gp('saveSuccess'))
  } finally {
    basicSaving.value = false
  }
}

function handleAvatarUploaded(data: UserProfile) {
  applyProfile(data)
  userStore.loadProfile()
}

async function submitPassword() {
  const valid = await passwordFormRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  passwordSaving.value = true
  try {
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    })
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    passwordFormRef.value?.clearValidate()
    await loadProfile()
    ElMessage.success(gp('passwordChanged'))
  } finally {
    passwordSaving.value = false
  }
}

async function startProfileMfa() {
  mfaStarting.value = true
  try {
    const data = await startMfaEnroll()
    mfaOtpUri.value = data.otpAuthUri
    mfaCode.value = ''
  } finally {
    mfaStarting.value = false
  }
}

async function confirmProfileMfa() {
  if (!/^\d{6}$/.test(mfaCode.value)) {
    ElMessage.warning(gp('mfaCodePlaceholder'))
    return
  }
  mfaSaving.value = true
  try {
    await confirmMfaEnroll(mfaCode.value)
    mfaOtpUri.value = ''
    mfaCode.value = ''
    await userStore.loadProfile()
    ElMessage.success(gp('mfaBound'))
  } finally {
    mfaSaving.value = false
  }
}

async function disableProfileMfa() {
  if (!/^\d{6}$/.test(mfaCode.value)) {
    ElMessage.warning(gp('mfaCodePlaceholder'))
    return
  }
  mfaSaving.value = true
  try {
    await disableMfa(mfaCode.value)
    mfaCode.value = ''
    await userStore.loadProfile()
    ElMessage.success(gp('mfaDisabledOk'))
  } finally {
    mfaSaving.value = false
  }
}

onMounted(() => {
  const tab = route.query.tab
  if (typeof tab === 'string' && PROFILE_TABS.has(tab)) {
    activeTab.value = tab
  }
  loadProfile()
})
</script>

<style scoped>
.profile-layout {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 16px;
  align-items: start;
}

.profile-card,
.profile-main {
  border: none;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.profile-card-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 8px 0 12px;
}

.avatar-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.avatar-block :deep(.avatar-picker) {
  flex-direction: column;
  align-items: center;
}

.profile-avatar {
  background: var(--el-color-primary);
  color: #fff;
  font-size: 32px;
  font-weight: 600;
}

.display-name {
  margin: 0 0 4px;
  font-size: 20px;
  font-weight: 600;
  color: #1f1f1f;
}

.display-subtitle {
  margin: 0 0 16px;
  font-size: 14px;
  color: var(--ic-text-secondary);
}

.profile-meta {
  width: 100%;
  margin: 0;
  padding: 0;
  list-style: none;
  text-align: left;
}

.profile-meta li {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 0;
  border-top: 1px solid #f0f0f0;
  font-size: 14px;
  color: #434343;
}

.meta-label {
  font-size: 12px;
  color: var(--ic-text-secondary);
}

.profile-form {
  max-width: 560px;
  padding-top: 8px;
}

.password-form {
  max-width: 560px;
}

.account-meta {
  margin-bottom: 20px;
}

.mfa-status {
  margin-bottom: 16px;
  max-width: 560px;
}

.mfa-enroll,
.mfa-disable {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-width: 360px;
}

.mfa-qr-block {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: flex-start;
}

.mfa-qr {
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
}

.org-descriptions,
.account-meta {
  max-width: 720px;
}

.info-tag {
  margin: 0 8px 8px 0;
}

@media (max-width: 960px) {
  .profile-layout {
    grid-template-columns: 1fr;
  }
}
</style>
