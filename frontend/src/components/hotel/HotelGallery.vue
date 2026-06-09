<template>
  <section class="hotel-gallery">
    <div class="grid">
      <!-- 左侧大图 -->
      <div class="main-img" @click="openPreview(0)">
        <img :src="valid(imgs[0])" alt="主图" @error="onErr" />
        <div class="hover-mask"><el-icon><ZoomIn /></el-icon></div>
      </div>

      <!-- 右侧两张小图 -->
      <div class="side-col">
        <div
          v-for="(img, idx) in sideImgs"
          :key="idx"
          class="side-img"
          @click="openPreview(idx + 1)"
        >
          <img :src="valid(img)" :alt="`图${idx + 2}`" @error="onErr" />
          <div class="hover-mask"><el-icon><ZoomIn /></el-icon></div>
          <!-- 最后一张不显示"查看全部"，直接点击预览 -->
        </div>
      </div>

      <!-- 收藏按钮 -->
      <button class="fav-btn" :class="{ active: favorited }" @click.stop="$emit('favorite')">
        <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
          <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 6 4 4 6.5 4c1.74 0 3.41 1.01 4.22 2.53C11.09 5.01 12.76 4 14.5 4 17 4 19 6 19 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
        </svg>
      </button>
    </div>

    <!-- 隐藏的 el-image 用于触发预览 -->
    <el-image
      ref="previewRef"
      style="display:none"
      :src="imgs[previewIndex] || ''"
      :preview-src-list="imgs"
      :initial-index="previewIndex"
      preview-teleported
    />
  </section>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ZoomIn } from '@element-plus/icons-vue'
import { normalizeUrl, FALLBACK_IMAGE } from '../../utils/image'

const props = defineProps({
  images:    { type: Array,   default: () => [] },
  favorited: { type: Boolean, default: false }
})
defineEmits(['favorite'])

const previewRef   = ref(null)
const previewIndex = ref(0)

const imgs = computed(() => {
  const list = (props.images || []).map(u => normalizeUrl(u) || FALLBACK_IMAGE).filter(Boolean)
  return list.length ? list : [FALLBACK_IMAGE]
})

// 右侧最多2张，不足用第一张补
const sideImgs = computed(() => {
  const arr = imgs.value.slice(1, 3)
  while (arr.length < 2) arr.push(imgs.value[0])
  return arr
})

const valid = (u) => normalizeUrl(u) || FALLBACK_IMAGE
const onErr = (e) => { e.target.src = FALLBACK_IMAGE }

function openPreview(idx) {
  previewIndex.value = Math.min(idx, imgs.value.length - 1)
  // 触发 el-image 内部预览
  previewRef.value?.$el?.querySelector('img')?.click()
}
</script>

<style scoped>
.hotel-gallery {
  background: #fff;
  border-radius: 14px;
  border: 1px solid #eef1f6;
  padding: 12px;
  box-shadow: 0 6px 18px rgba(0,0,0,0.06);
  max-height: 384px;
  overflow: hidden;
}

.grid {
  display: grid;
  grid-template-columns: 3fr 2fr;
  gap: 10px;
  position: relative;
  height: 360px;
  max-height: 360px;
}

.main-img {
  border-radius: 10px;
  overflow: hidden;
  position: relative;
  cursor: pointer;
  height: 100%;
}

.side-col {
  display: flex;
  flex-direction: column;
  gap: 10px;
  height: 100%;
}

.side-img {
  flex: 1;
  border-radius: 10px;
  overflow: hidden;
  position: relative;
  cursor: pointer;
  min-height: 0;
}

.main-img img,
.side-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.25s ease;
  display: block;
}

.hover-mask {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 28px;
  opacity: 0;
  transition: all 0.2s ease;
}

.main-img:hover img,
.side-img:hover img {
  transform: scale(1.03);
}

.main-img:hover .hover-mask,
.side-img:hover .hover-mask {
  background: rgba(0,0,0,0.25);
  opacity: 1;
}

.view-all-mask {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 700;
  font-size: 13px;
  cursor: pointer;
}
.view-all-mask:hover {
  background: rgba(0,0,0,0.6);
}

.fav-btn {
  position: absolute;
  right: 16px;
  top: 16px;
  background: rgba(255,255,255,0.92);
  color: #606266;
  border: none;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.25s;
  box-shadow: 0 2px 12px rgba(0,0,0,0.15);
  z-index: 10;
  backdrop-filter: blur(8px);
}
.fav-btn:hover { transform: scale(1.1); background: #fff; }
.fav-btn.active { color: #e0245e; }

@media (max-width: 1280px) {
  .grid { height: 300px; max-height: 300px; }
  .hotel-gallery { max-height: 324px; }
}
</style>
