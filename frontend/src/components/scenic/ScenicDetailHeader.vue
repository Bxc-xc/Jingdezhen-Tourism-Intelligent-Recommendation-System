<template>
  <div class="scenic-detail-header">
    <!-- 图片展示区：左大图 + 右侧2张小图 -->
    <div class="hero-gallery">
      <div class="gallery-layout" ref="galleryLayoutRef">
        <!-- 左侧大图 -->
        <div class="gallery-main" @click="openPreview(0)" :style="`--gallery-main-bg: url('${safeImages[0]}')`">
          <img
            :src="safeImages[0]"
            :alt="scenic.name"
            loading="lazy"
            @error="handleImageError"
          />
        </div>

        <!-- 右侧2张小图 -->
        <div class="gallery-side">
          <div
            v-for="(img, idx) in sideImages"
            :key="idx"
            class="gallery-side-item"
            @click="openPreview(idx + 1)"
          >
            <img
              :src="img"
              :alt="`${scenic.name}-图片${idx + 2}`"
              loading="lazy"
              @error="handleImageError"
            />
          </div>
        </div>

        <!-- 右下角数量角标 -->
        <div class="gallery-counter" @click="openPreview(0)">
          共 {{ safeImages.length }} 张图片
        </div>
      </div>
    </div>

    <!-- 图片预览弹窗 -->
    <Teleport to="body">
      <div v-if="previewVisible" class="preview-overlay" @click.self="closePreview">
        <!-- 关闭按钮 -->
        <button class="preview-close" @click="closePreview" aria-label="close">&#x2715;</button>

        <!-- 左箭头 -->
        <button
          class="preview-arrow preview-arrow-left"
          @click="prevImage"
          :disabled="previewIndex === 0"
          aria-label="prev"
        >&#8249;</button>

        <!-- 图片 -->
        <div class="preview-img-wrap">
          <img
            :src="safeImages[previewIndex]"
            :alt="`${scenic.name}-预览${previewIndex + 1}`"
            @error="handleImageError"
          />
          <div class="preview-counter-badge">{{ previewIndex + 1 }} / {{ safeImages.length }}</div>
        </div>

        <!-- 右箭头 -->
        <button
          class="preview-arrow preview-arrow-right"
          @click="nextImage"
          :disabled="previewIndex === safeImages.length - 1"
          aria-label="next"
        >&#8250;</button>
      </div>
    </Teleport>

  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { normalizeUrl, FALLBACK_IMAGE } from '../../utils/image'

const props = defineProps({
  scenic: {
    type: Object,
    required: true
  },
  images: {
    type: Array,
    default: () => []
  },
  liveReviewCount: {
    type: Number,
    default: null
  },
  hideInfo: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['preview', 'scroll-to-reviews'])

// 图片区高度控制已改用 CSS aspect-ratio，无需 JS 动态计算
const galleryLayoutRef = ref(null)
const infoSectionRef = ref(null)
let resizeObserver = null

const syncGalleryHeight = () => {
  // 已改用 aspect-ratio CSS 控制，无需 JS 动态计算
}

onMounted(() => {
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
  resizeObserver?.disconnect()
})

// 统一处理图片路径，保证至少有一张占位图
const safeImages = computed(() => {
  const normalized = (props.images || [])
    .map(img => normalizeUrl(img))
    .filter(Boolean)
  return normalized.length > 0 ? normalized : [FALLBACK_IMAGE]
})

// 右侧固定取第 1、2 张（index 1 和 2）
const sideImages = computed(() => {
  const imgs = safeImages.value
  const first = imgs[0] || FALLBACK_IMAGE
  return [
    imgs[1] || first,
    imgs[2] || first
  ]
})

// 弹窗状态
const previewVisible = ref(false)
const previewIndex = ref(0)

const openPreview = (index) => {
  previewIndex.value = index
  previewVisible.value = true
}

const closePreview = () => {
  previewVisible.value = false
}

const prevImage = () => {
  if (previewIndex.value > 0) previewIndex.value--
}

const nextImage = () => {
  if (previewIndex.value < safeImages.value.length - 1) previewIndex.value++
}

const handleKeydown = (e) => {
  if (!previewVisible.value) return
  if (e.key === 'ArrowLeft') prevImage()
  else if (e.key === 'ArrowRight') nextImage()
  else if (e.key === 'Escape') closePreview()
}


const handleImageError = (e) => {
  e.target.src = FALLBACK_IMAGE
}
</script>

<style scoped>
.scenic-detail-header {
  display: block;
  margin-bottom: 16px;
}

/* hideInfo 模式：图片占满全宽 */
.scenic-detail-header:not(:has(.info-section)) {
  grid-template-columns: 1fr;
}

/* ===== 图片展示区 ===== */
.hero-gallery {
  position: relative;
  border-radius: 18px;
  overflow: hidden;
  box-shadow:
    0 18px 40px rgba(15, 23, 42, 0.16),
    0 4px 16px rgba(15, 23, 42, 0.12);
  /* 无黑色背景，浅灰兜底 */
  background: #f3f4f6;
}

.gallery-layout {
  display: flex;
  gap: 0;
  padding: 0;
  margin: 0;
  /* 固定宽高比 16:9，适配所有景点图片 */
  aspect-ratio: 16 / 9;
  position: relative;
}

/* 左侧大图：3份宽，contain 完整显示，无圆角避免视觉缝隙 */
.gallery-main {
  flex: 3;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 0;
  transition: opacity 0.22s ease;
  position: relative;
  background: #e5e7eb;
}

/* 模糊背景层：用同一张图片放大模糊填满容器，消除 contain 留白 */
.gallery-main::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: var(--gallery-main-bg);
  background-size: cover;
  background-position: center;
  filter: blur(18px) brightness(0.85) saturate(1.2);
  transform: scale(1.08);
  z-index: 0;
}

.gallery-main:hover {
  opacity: 0.92;
}

.gallery-main img {
  position: relative;
  z-index: 1;
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
}

/* 右侧2张小图：1份宽，上下排列，无缝 */
.gallery-side {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0;
}

.gallery-side-item {
  flex: 1;
  overflow: hidden;
  cursor: pointer;
  border-radius: 0;
  transition: opacity 0.22s ease;
  /* 上下两张之间用极细白线分隔，代替 gap */
  border-top: 1px solid rgba(255, 255, 255, 0.3);
}

.gallery-side-item:first-child {
  border-top: none;
}

.gallery-side-item:hover {
  opacity: 0.88;
}

.gallery-side-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

/* 右下角数量角标：毛玻璃白色，禁止黑色背景 */
.gallery-counter {
  position: absolute;
  right: 16px;
  bottom: 16px;
  padding: 5px 14px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 500;
  color: #1f2937;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
  cursor: pointer;
  user-select: none;
  z-index: 2;
}

/* ===== 预览弹窗 ===== */
.preview-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: rgba(0, 0, 0, 0.88);
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-close {
  position: absolute;
  top: 20px;
  right: 24px;
  background: rgba(255, 255, 255, 0.15);
  border: none;
  color: #fff;
  font-size: 22px;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
  z-index: 10;
}

.preview-close:hover {
  background: rgba(255, 255, 255, 0.3);
}

.preview-img-wrap {
  position: relative;
  max-width: 88vw;
  max-height: 88vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-img-wrap img {
  max-width: 88vw;
  max-height: 88vh;
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.5);
  display: block;
}

.preview-counter-badge {
  position: absolute;
  bottom: -36px;
  left: 50%;
  transform: translateX(-50%);
  color: rgba(255, 255, 255, 0.75);
  font-size: 14px;
  white-space: nowrap;
}

.preview-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(255, 255, 255, 0.15);
  border: none;
  color: #fff;
  font-size: 48px;
  line-height: 1;
  width: 52px;
  height: 52px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
  z-index: 10;
}

.preview-arrow:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.3);
}

.preview-arrow:disabled {
  opacity: 0.25;
  cursor: not-allowed;
}

.preview-arrow-left { left: 24px; }
.preview-arrow-right { right: 24px; }

/* ===== 响应式 ===== */
@media (max-width: 1024px) {
  .hero-gallery {
    border-radius: 0;
    margin: 0 -16px;
  }
}

@media (max-width: 768px) {
  .gallery-layout {
    aspect-ratio: 4 / 3;
  }

  /* 移动端隐藏右侧小图 */
  .gallery-side { display: none; }
  .gallery-main { flex: 1; }

  .preview-arrow-left { left: 8px; }
  .preview-arrow-right { right: 8px; }
}
</style>
