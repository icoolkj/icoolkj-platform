import { ref } from 'vue'
import { defineStore } from 'pinia'
import type { RouteLocationNormalizedLoaded } from 'vue-router'
import { TAGS_VIEW_MAX, type TagView } from '@/types/layout'

export const useTagsViewStore = defineStore('tagsView', () => {
  const visitedViews = ref<TagView[]>([])

  function resolveTitle(route: RouteLocationNormalizedLoaded) {
    const titleKey = route.meta.titleKey as string | undefined
    if (titleKey) {
      return titleKey
    }
    const title = route.meta.title as string | undefined
    if (title) {
      return title
    }
    return route.path
  }

  function isHidden(route: RouteLocationNormalizedLoaded) {
    return route.meta.hidden === true || route.path === '/no-access'
  }

  function trimOverflow() {
    while (visitedViews.value.length > TAGS_VIEW_MAX) {
      const index = visitedViews.value.findIndex((item) => !item.affix)
      if (index < 0) {
        break
      }
      visitedViews.value.splice(index, 1)
    }
  }

  function addView(route: RouteLocationNormalizedLoaded) {
    if (isHidden(route)) {
      return
    }
    const path = route.path
    const existing = visitedViews.value.find((item) => item.path === path)
    if (existing) {
      existing.fullPath = route.fullPath
      existing.title = resolveTitle(route)
      return
    }
    visitedViews.value.push({
      path,
      fullPath: route.fullPath,
      title: resolveTitle(route),
      affix: route.meta.affix === true,
    })
    trimOverflow()
  }

  function removeView(path: string) {
    visitedViews.value = visitedViews.value.filter((item) => item.path !== path)
  }

  function closeOthers(path: string) {
    visitedViews.value = visitedViews.value.filter((item) => item.affix || item.path === path)
  }

  function closeAll() {
    visitedViews.value = visitedViews.value.filter((item) => item.affix)
  }

  function clearViews() {
    closeAll()
  }

  return {
    visitedViews,
    maxCount: TAGS_VIEW_MAX,
    addView,
    removeView,
    closeOthers,
    closeAll,
    clearViews,
  }
})
