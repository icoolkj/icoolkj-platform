import { THEME_STORAGE_KEY } from '@/constants/storage'

export interface ThemePreset {
  id: string
  primary: string
  primaryLight3: string
  primaryLight5: string
  primaryLight7: string
  primaryLight8: string
  primaryLight9: string
  primaryDark2: string
  sidebarBg: string
  sidebarBgEnd: string
  sidebarActive: string
}

export { THEME_STORAGE_KEY }

export const THEME_PRESETS: ThemePreset[] = [
  {
    id: 'blue',
    primary: '#1677ff',
    primaryLight3: '#4096ff',
    primaryLight5: '#69b1ff',
    primaryLight7: '#91caff',
    primaryLight8: '#bae0ff',
    primaryLight9: '#e6f4ff',
    primaryDark2: '#0958d9',
    sidebarBg: '#001529',
    sidebarBgEnd: '#002140',
    sidebarActive: '#1677ff',
  },
  {
    id: 'green',
    primary: '#13c2c2',
    primaryLight3: '#36cfc9',
    primaryLight5: '#5cdbd3',
    primaryLight7: '#87e8de',
    primaryLight8: '#b5f5ec',
    primaryLight9: '#e6fffb',
    primaryDark2: '#08979c',
    sidebarBg: '#002329',
    sidebarBgEnd: '#00474f',
    sidebarActive: '#13c2c2',
  },
  {
    id: 'purple',
    primary: '#722ed1',
    primaryLight3: '#9254de',
    primaryLight5: '#b37feb',
    primaryLight7: '#d3adf7',
    primaryLight8: '#efdbff',
    primaryLight9: '#f9f0ff',
    primaryDark2: '#531dab',
    sidebarBg: '#120338',
    sidebarBgEnd: '#22075e',
    sidebarActive: '#722ed1',
  },
  {
    id: 'orange',
    primary: '#fa8c16',
    primaryLight3: '#ffa940',
    primaryLight5: '#ffc069',
    primaryLight7: '#ffd591',
    primaryLight8: '#ffe7ba',
    primaryLight9: '#fff7e6',
    primaryDark2: '#d46b08',
    sidebarBg: '#2b1d11',
    sidebarBgEnd: '#442a12',
    sidebarActive: '#fa8c16',
  },
  {
    id: 'rose',
    primary: '#eb2f96',
    primaryLight3: '#f759ab',
    primaryLight5: '#ff85c0',
    primaryLight7: '#ffadd2',
    primaryLight8: '#ffd6e7',
    primaryLight9: '#fff0f6',
    primaryDark2: '#c41d7f',
    sidebarBg: '#291321',
    sidebarBgEnd: '#520339',
    sidebarActive: '#eb2f96',
  },
]

export function getThemePreset(id: string) {
  return THEME_PRESETS.find((item) => item.id === id) ?? THEME_PRESETS[0]
}

/** Apply Element Plus + layout CSS variables for the selected preset */
export function applyThemePreset(preset: ThemePreset) {
  const root = document.documentElement
  root.style.setProperty('--el-color-primary', preset.primary)
  root.style.setProperty('--el-color-primary-light-3', preset.primaryLight3)
  root.style.setProperty('--el-color-primary-light-5', preset.primaryLight5)
  root.style.setProperty('--el-color-primary-light-7', preset.primaryLight7)
  root.style.setProperty('--el-color-primary-light-8', preset.primaryLight8)
  root.style.setProperty('--el-color-primary-light-9', preset.primaryLight9)
  root.style.setProperty('--el-color-primary-dark-2', preset.primaryDark2)

  root.style.setProperty('--ic-primary', preset.primary)
  root.style.setProperty('--ic-primary-light', preset.primaryLight9)
  root.style.setProperty('--ic-sidebar-bg', preset.sidebarBg)
  root.style.setProperty('--ic-sidebar-bg-end', preset.sidebarBgEnd)
  root.style.setProperty('--ic-sidebar-active', preset.sidebarActive)
  root.style.setProperty('--ic-page-bg', colorMixPageBg(preset.primaryLight9))
}

function colorMixPageBg(tint: string) {
  return `color-mix(in srgb, ${tint} 35%, #f0f2f5)`
}
