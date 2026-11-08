<template>
  <template v-for="menu in menus" :key="menu.id">
    <el-sub-menu
      v-if="menu.menuType === 'M' && menu.children?.length"
      :index="menu.path || menu.id"
    >
      <template #title>
        <el-icon v-if="iconComponent(menu.icon)">
          <component :is="iconComponent(menu.icon)" />
        </el-icon>
        <span>{{ menu.menuName }}</span>
      </template>
      <SidebarMenu :menus="menu.children!" />
    </el-sub-menu>
    <el-menu-item v-else-if="menu.menuType === 'C' && !isStatusDisabled(menu.visible)" :index="menu.path">
      <el-icon v-if="iconComponent(menu.icon)">
        <component :is="iconComponent(menu.icon)" />
      </el-icon>
      <span>{{ menu.menuName }}</span>
    </el-menu-item>
  </template>
</template>

<script setup lang="ts">
import { isStatusDisabled } from '@/constants/flags'
import { resolveIconComponent } from '@/utils/icon'
import type { MenuRouter } from '@/types/system/menu'

defineProps<{ menus: MenuRouter[] }>()

function iconComponent(name?: string) {
  return resolveIconComponent(name)
}
</script>
