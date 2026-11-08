import request from '@/utils/request'
import type { ApiResponse } from '@/types/common'

import type { LayoutSettings } from '@/types/layout'

export interface UserPreference {
  locale: string
  theme: string
  layout?: Partial<LayoutSettings>
}

export interface UserPreferenceUpdate {
  locale?: string
  theme?: string
  layout?: Partial<LayoutSettings>
}

export async function getPreference() {
  const res = await request.get<ApiResponse<UserPreference>>('/auth/preference')
  return res.data.data
}

export async function updatePreference(data: UserPreferenceUpdate) {
  const res = await request.put<ApiResponse<UserPreference>>('/auth/preference', data)
  return res.data.data
}
