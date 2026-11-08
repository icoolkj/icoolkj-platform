/**
 * Browser storage key SoT — values from {@code shell.config.json} → {@link shellConfig.storageKeys}.
 * Keep Vite DevTools inject / index.html / request.ts in sync via this module.
 */
import { shellConfig } from '../../shell.config'

const keys = shellConfig.storageKeys

export const ACCESS_TOKEN_KEY = keys.accessToken
export const REFRESH_TOKEN_KEY = keys.refreshToken
export const LOCALE_STORAGE_KEY = keys.locale
export const LAYOUT_STORAGE_KEY = keys.layout
export const THEME_STORAGE_KEY = keys.theme
export const LOGIN_REMEMBER_KEY = keys.loginRemember
export const LOGIN_LOCALE_PENDING_KEY = keys.loginLocalePending
