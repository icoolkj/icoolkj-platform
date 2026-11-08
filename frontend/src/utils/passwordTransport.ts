import { sm2 } from 'sm-crypto'
import request from '@/utils/request'
import type { ApiResponse } from '@/types/common'

/** Wire contract from GET /auth/password-public-key */
export interface PasswordPublicKey {
  algorithm: string
  publicKey: string
  cipherPrefix: string
  payloadFormat: string
  maxSkewSeconds: number
}

const FALLBACK_CIPHER_PREFIX = 'enc:sm2:v1:'
/** SEC1 uncompressed: 04 + 32-byte X + 32-byte Y (sm-crypto expects leading 04). */
const PUBLIC_KEY_HEX_LENGTH = 130

let cachedKey: PasswordPublicKey | null = null
let cachedAt = 0
const KEY_TTL_MS = 5 * 60 * 1000

export async function fetchPasswordPublicKey(force = false): Promise<PasswordPublicKey> {
  const now = Date.now()
  if (!force && cachedKey && now - cachedAt < KEY_TTL_MS) {
    return cachedKey
  }
  const res = await request.get<ApiResponse<PasswordPublicKey>>('/auth/password-public-key')
  const data = res.data.data
  if (!data?.publicKey || !data.cipherPrefix) {
    throw new Error('error.auth.password_transport_invalid')
  }
  cachedKey = data
  cachedAt = now
  return cachedKey
}

function normalizePublicKeyHex(publicKeyHex: string): string {
  const hex = publicKeyHex.trim().toLowerCase()
  if (!hex.startsWith('04') || hex.length !== PUBLIC_KEY_HEX_LENGTH) {
    throw new Error('error.auth.password_transport_invalid')
  }
  return hex
}

export async function encryptPasswordForTransport(plainPassword: string): Promise<string> {
  const key = await fetchPasswordPublicKey()
  if (key.algorithm && key.algorithm.toUpperCase() !== 'SM2') {
    throw new Error('error.auth.password_transport_invalid')
  }
  const payload = `${Date.now()}|${plainPassword}`
  let cipherHex: string
  try {
    cipherHex = sm2.doEncrypt(payload, normalizePublicKeyHex(key.publicKey), 1)
  } catch {
    throw new Error('error.auth.password_transport_invalid')
  }
  if (!cipherHex) {
    throw new Error('error.auth.password_transport_invalid')
  }
  const prefix = key.cipherPrefix || FALLBACK_CIPHER_PREFIX
  return `${prefix}${cipherHex}`
}

export function clearPasswordPublicKeyCache() {
  cachedKey = null
  cachedAt = 0
}
