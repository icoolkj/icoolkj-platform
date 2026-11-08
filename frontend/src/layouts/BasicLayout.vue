<template>
  <el-container
    class="layout"
    direction="vertical"
    :class="{ 'layout--studio': designerStudio }"
  >
    <el-header v-show="!designerStudio" class="global-header">
      <div v-if="demoStore.isDemo" class="demo-banner">
        <el-alert :title="t('layout.demoBanner')" type="warning" :closable="false" show-icon />
      </div>
      <div class="global-header-row">
        <div class="global-header-brand">
          <img
            v-if="sidebarLogoUrl"
            class="brand-logo"
            :src="sidebarLogoUrl"
            :alt="sidebarBrandText"
          />
          <span class="brand-text">{{ sidebarBrandText }}</span>
        </div>
        <div class="global-header-nav">
          <ProductNav />
        </div>
        <div class="global-header-right">
          <LayoutNavbar />
        </div>
      </div>
    </el-header>

    <el-container class="layout-body">
      <el-aside
        v-show="!designerStudio"
        :width="sidebarWidth"
        class="aside"
        :class="`aside--${sidebarTheme}`"
      >
        <div v-if="!sidebarCollapsed" class="menu-search">
          <el-input
            v-model="menuKeyword"
            clearable
            :placeholder="t('layout.menuSearchPlaceholder')"
            :prefix-icon="Search"
          />
        </div>
        <el-scrollbar
          class="menu-scroll"
          :class="{ 'menu-scroll--with-search': !sidebarCollapsed }"
        >
          <el-menu
            :key="menuKeyword.trim() ? 'search' : 'nav'"
            :default-active="activeMenu"
            :default-openeds="menuDefaultOpeneds"
            :collapse="sidebarCollapsed"
            :unique-opened="menuUniqueOpenedEffective"
            :collapse-transition="false"
            popper-class="sidebar-menu-popper"
            background-color="transparent"
            :text-color="menuTextColor"
            :active-text-color="menuActiveTextColor"
            router
          >
            <SidebarMenu v-if="visibleMenus.length" :menus="visibleMenus" />
            <el-empty
              v-else
              :description="
                menuKeyword.trim() ? t('layout.menuSearchEmpty') : t('layout.sidebarNoMenus')
              "
              :image-size="48"
            />
          </el-menu>
        </el-scrollbar>
      </el-aside>

      <el-container class="layout-main" direction="vertical">
        <div v-if="!designerStudio" class="sub-bar">
          <el-button link class="collapse-btn" @click="sidebarCollapsed = !sidebarCollapsed">
            <el-icon :size="18">
              <Fold v-if="!sidebarCollapsed" />
              <Expand v-else />
            </el-icon>
          </el-button>
          <el-breadcrumb v-if="breadcrumb" separator="/" class="sub-bar__crumb">
            <el-breadcrumb-item>{{ breadcrumbParent }}</el-breadcrumb-item>
            <el-breadcrumb-item>{{ breadcrumbCurrent }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <TagsView v-if="tagsView && !designerStudio" />

        <el-main class="main" :class="{ 'main--studio': designerStudio }">
          <router-view v-if="!mustChangePassword" v-slot="{ Component, route: viewRoute }">
            <transition name="fade-slide" mode="out-in">
              <div v-if="Component" :key="viewRoute.fullPath" class="route-view">
                <component :is="Component" />
              </div>
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>

    <LayoutWatermark v-if="!designerStudio" :enabled="watermark" />
    <LayoutSettings v-if="!designerStudio" />
    <ForceChangePasswordGate />
  </el-container>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useI18n } from 'vue-i18n'
import { Expand, Fold, Search } from '@element-plus/icons-vue'
import LayoutNavbar from '@/components/layout/LayoutNavbar.vue'
import ProductNav from '@/components/layout/ProductNav.vue'
import LayoutSettings from '@/components/layout/LayoutSettings.vue'
import LayoutWatermark from '@/components/layout/LayoutWatermark.vue'
import ForceChangePasswordGate from '@/components/layout/ForceChangePasswordGate.vue'
import TagsView from '@/components/layout/TagsView.vue'
import SidebarMenu from '@/components/SidebarMenu.vue'
import { useMenuStore } from '@/stores/menu'
import { useLayoutStore } from '@/stores/layout'
import { useTagsViewStore } from '@/stores/tagsView'
import { useDemoStore } from '@/stores/demo'
import { useUserStore } from '@/stores/user'
import { useBrandStore } from '@/stores/brand'
import { useDesignerStudioStore } from '@/stores/designerStudio'
import { useAuthImageUrl } from '@/composables/useAuthImageUrl'
import { canonicalInfraDownloadUrl } from '@/utils/infraFileUrl'
import { resolveMenuBreadcrumb } from '@/utils/menuBreadcrumb'
import { collectOpenMenuIndexes, filterMenuTree } from '@/utils/menuFilter'
import { resolveGenPageText } from '@/utils/genPageI18n'

const SIDEBAR_WIDTH_EXPANDED = '220px'
const SIDEBAR_WIDTH_COLLAPSED = '64px'

const { t, locale } = useI18n()
const route = useRoute()
const menuStore = useMenuStore()
const layoutStore = useLayoutStore()
const tagsViewStore = useTagsViewStore()
const demoStore = useDemoStore()
const userStore = useUserStore()
const brandStore = useBrandStore()
const designerStudioStore = useDesignerStudioStore()
const { sidebarTheme, breadcrumb, tagsView, watermark, menuUniqueOpened } = storeToRefs(layoutStore)
const { active: designerStudio } = storeToRefs(designerStudioStore)

const mustChangePassword = computed(() => !!userStore.user?.mustChangePassword)

const sidebarCollapsed = ref(false)
const sidebarWidth = computed(() =>
  sidebarCollapsed.value ? SIDEBAR_WIDTH_COLLAPSED : SIDEBAR_WIDTH_EXPANDED,
)

const menuKeyword = ref('')
const activeMenu = computed(() => route.path)

const visibleMenus = computed(() => filterMenuTree(menuStore.routers, menuKeyword.value))
const searchingMenus = computed(() => Boolean(menuKeyword.value.trim()))
const menuUniqueOpenedEffective = computed(() =>
  searchingMenus.value ? false : menuUniqueOpened.value,
)
const menuDefaultOpeneds = computed(() =>
  searchingMenus.value ? collectOpenMenuIndexes(visibleMenus.value) : [],
)

watch(sidebarCollapsed, (value) => {
  if (value) {
    menuKeyword.value = ''
  }
})

const sidebarBrandText = computed(() => brandStore.sidebarBrand(locale.value) || t('layout.brand'))
const sidebarLogoSource = computed(() => {
  const fileId = brandStore.sidebarLogoFileId()
  return fileId ? canonicalInfraDownloadUrl(fileId) : ''
})
const sidebarLogoUrl = useAuthImageUrl(sidebarLogoSource, 'infra')

const menuTextColor = computed(() =>
  sidebarTheme.value === 'light' ? 'var(--ic-sidebar-text)' : 'rgba(255, 255, 255, 0.75)',
)
const menuActiveTextColor = computed(() =>
  sidebarTheme.value === 'light' ? 'var(--ic-sidebar-text-active)' : '#ffffff',
)

const breadcrumbParent = computed(() => {
  const match = resolveMenuBreadcrumb(menuStore.routers, route.path)
  if (match.parent) {
    return match.parent.menuName
  }
  return t('layout.home')
})

const breadcrumbCurrent = computed(() => {
  const match = resolveMenuBreadcrumb(menuStore.routers, route.path)
  if (match.current) {
    return match.current.menuName
  }
  const genBusinessName = route.meta.genBusinessName as string | undefined
  const genPageKey = (route.meta.genPageKey as string | undefined) ?? 'title'
  if (genBusinessName) {
    return resolveGenPageText(genBusinessName, locale.value, genPageKey)
  }
  const titleKey = route.meta.titleKey as string | undefined
  if (titleKey) return t(titleKey)
  return (route.meta.title as string) || t('layout.dashboard')
})

watch(
  () => tagsView.value,
  (enabled, prev) => {
    if (prev !== undefined && enabled !== prev) {
      if (!enabled) {
        tagsViewStore.clearViews()
      } else {
        tagsViewStore.addView(route)
      }
    }
  },
)

watch(
  () => route.fullPath,
  () => {
    if (tagsView.value) {
      tagsViewStore.addView(route)
    }
  },
  { immediate: true },
)
</script>

<style scoped>
.layout {
  height: 100vh;
  overflow: hidden;
}

.layout--studio .layout-body {
  flex: 1;
}

.main--studio {
  padding: 0 !important;
  height: 100%;
  overflow: hidden;
}

/* ── 全宽顶栏：品牌 + 产品导航 + 工具栏 ── */
.global-header {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  height: auto;
  min-height: 56px;
  padding: 0;
  background: linear-gradient(
    90deg,
    var(--ic-header-bg, #ffffff) 0%,
    var(--ic-header-bg-end, #ffffff) 100%
  );
  border-bottom: 1px solid var(--ic-header-border, #f0f0f0);
  box-shadow: var(--ic-header-shadow, 0 1px 4px rgba(0, 21, 41, 0.04));
  flex-shrink: 0;
}

.demo-banner {
  padding: 8px 16px 0;
}

.demo-banner :deep(.el-alert) {
  padding: 6px 12px;
}

.global-header-row {
  display: flex;
  align-items: stretch;
  min-height: 56px;
}

.global-header-brand {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 8px;
  flex-shrink: 0;
  padding: 0 20px 0 16px;
  max-width: min(320px, 36vw);
}

.brand-logo {
  width: 28px;
  height: 28px;
  object-fit: contain;
  flex-shrink: 0;
  border-radius: 4px;
}

.brand-text {
  font-size: 15px;
  font-weight: 700;
  color: var(--ic-header-text, #262626);
  letter-spacing: 0.3px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.global-header-nav {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: stretch;
  padding-left: 4px;
}

.global-header-right {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  padding: 0 16px;
}

/* ── 主体：侧栏 + 内容区 ── */
.layout-body {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.aside {
  height: 100%;
  background: linear-gradient(180deg, var(--ic-sidebar-bg) 0%, var(--ic-sidebar-bg-end) 100%);
  transition: width 0.2s;
  overflow: hidden;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.06);
}

.aside--light {
  border-right: 1px solid var(--ic-sidebar-border, #f0f0f0);
  box-shadow: none;
}

.menu-search {
  padding: 10px 12px 6px;
}

.menu-search :deep(.el-input__wrapper) {
  background: var(--ic-sidebar-hover, rgba(255, 255, 255, 0.06));
  box-shadow: none;
}

.aside--dark .menu-search :deep(.el-input__inner),
.aside--dark .menu-search :deep(.el-input__prefix) {
  color: rgba(255, 255, 255, 0.85);
}

.aside--dark .menu-search :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.45);
}

.menu-scroll {
  height: 100%;
}

.menu-scroll--with-search {
  height: calc(100% - 52px);
}

.menu-scroll :deep(.el-empty__description) {
  color: var(--ic-sidebar-text);
}

.layout-main {
  min-width: 0;
  flex-direction: column;
  overflow: hidden;
}

/* ── 次级栏：折叠 + 面包屑 ── */
.sub-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 40px;
  padding: 0 12px 0 8px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.collapse-btn {
  flex-shrink: 0;
  color: #595959;
}

.sub-bar__crumb {
  min-width: 0;
}

.sub-bar__crumb :deep(.el-breadcrumb) {
  font-size: 13px;
  line-height: 1;
}

.sub-bar__crumb :deep(.el-breadcrumb__inner) {
  color: #8c8c8c;
  font-weight: 400;
}

.sub-bar__crumb :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #262626;
  font-weight: 500;
}

.main {
  flex: 1;
  min-height: 0;
  padding: 0;
  overflow: auto;
  background: var(--ic-page-bg);
}

.route-view {
  min-height: 100%;
}

:deep(.el-menu) {
  border-right: none;
}

:deep(.el-sub-menu__title:hover),
:deep(.el-menu-item:hover) {
  background: var(--ic-sidebar-hover) !important;
}

:deep(.el-menu-item.is-active) {
  background: var(--ic-sidebar-active) !important;
  border-radius: 4px;
  margin: 0 8px;
  width: calc(100% - 16px);
}

.aside--light :deep(.el-menu-item.is-active) {
  color: var(--ic-sidebar-text-active) !important;
  font-weight: 600;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.2s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(6px);
}

.fade-slide-leave-to {
  opacity: 0;
}
</style>
