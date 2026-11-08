<template>
  <div v-loading="loading">
    <el-carousel v-if="slides.length" height="180px" indicator-position="outside">
      <el-carousel-item v-for="item in slides" :key="item.id">
        <div class="banner-slide" :class="{ clickable: item.clickable }" @click="handleClick(item)">
          <AuthImage
            :src="item.imageUrl"
            download-scope="infra"
            fit="cover"
            image-class="banner-slide__image"
          />
          <div v-if="item.title" class="banner-slide__title">{{ item.title }}</div>
        </div>
      </el-carousel-item>
    </el-carousel>
    <el-empty v-else :description="gp('noBanner')" :image-size="72" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import AuthImage from '@/components/AuthImage.vue'
import { listPublishedBanner } from '@/api/system/banner'
import { BANNER_LINK_NONE } from '@/constants/flags'
import { normalizeExternalUrl } from '@/utils/normalizeExternalUrl'
import type { BannerPositionConfigItem, BannerPublishedVO } from '@/types/system/banner'

interface BannerSlide {
  id: string
  title?: string
  imageUrl?: string
  clickable: boolean
  linkType?: string
  linkUrl?: string
}

const props = defineProps<{
  gp: (key: string, params?: Record<string, string | number>) => string
  /** admin_system = 平台运营首页；admin_biz = 租户业务首页 */
  displayPosition: string
}>()

const router = useRouter()
const loading = ref(false)
const slides = ref<BannerSlide[]>([])

async function loadBanners() {
  if (!props.displayPosition) {
    slides.value = []
    return
  }
  loading.value = true
  try {
    const res = await listPublishedBanner({
      displayPosition: props.displayPosition,
      limit: 8,
    })
    slides.value = (res.data.data ?? []).map(toSlide).filter((item) => Boolean(item.imageUrl))
  } finally {
    loading.value = false
  }
}

function toSlide(banner: BannerPublishedVO): BannerSlide {
  const config = resolvePositionConfig(banner.positionConfig, props.displayPosition)
  const clickable = Boolean(
    config?.clickEnabled && config.linkType && config.linkType !== BANNER_LINK_NONE,
  )
  return {
    id: String(banner.id),
    title: banner.bannerTitle,
    imageUrl: banner.imagePreviewUrl,
    clickable,
    linkType: config?.linkType,
    linkUrl: config?.linkUrl,
  }
}

function resolvePositionConfig(
  raw: string | undefined,
  position: string,
): BannerPositionConfigItem | undefined {
  if (!raw) return undefined
  try {
    const list = JSON.parse(raw) as BannerPositionConfigItem[]
    return list.find((item) => item.position === position)
  } catch {
    return undefined
  }
}

function handleClick(item: BannerSlide) {
  if (!item.clickable || !item.linkUrl) return
  if (item.linkType === '2') {
    const routePath = item.linkUrl.startsWith('/') ? item.linkUrl : `/${item.linkUrl}`
    router.push(routePath)
    return
  }
  window.open(normalizeExternalUrl(item.linkUrl), '_blank', 'noopener,noreferrer')
}

onMounted(loadBanners)
watch(() => props.displayPosition, loadBanners)
</script>

<style scoped lang="scss">
.banner-slide {
  position: relative;
  height: 180px;
  border-radius: 10px;
  overflow: hidden;
  background: var(--el-fill-color-light);

  &.clickable {
    cursor: pointer;
  }

  :deep(.banner-slide__image) {
    width: 100%;
    height: 180px;
  }

  &__title {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    padding: 10px 14px;
    color: #fff;
    font-size: 14px;
    background: linear-gradient(transparent, rgba(0, 0, 0, 0.55));
  }
}
</style>
